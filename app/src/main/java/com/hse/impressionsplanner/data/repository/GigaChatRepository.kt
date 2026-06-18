package com.hse.impressionsplanner.data.repository

import com.hse.impressionsplanner.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

internal fun parseGigaChatJson(content: String, rawText: String): StructuredEntry {
    val parsed = JSONObject(content.trim())
    return StructuredEntry(
        place    = parsed.optString("place", ""),
        duration = parsed.optString("duration", ""),
        company  = parsed.optString("company", ""),
        weather  = parsed.optString("weather", ""),
        food     = parsed.optString("food", ""),
        surprise = parsed.optString("surprise", ""),
        emotions = parsed.optString("emotions", ""),
        summary  = parsed.optString("summary", rawText),
        rawText  = rawText
    )
}

data class StructuredEntry(
    val place: String,
    val duration: String,
    val company: String,
    val weather: String,
    val food: String,
    val surprise: String,
    val emotions: String,
    val summary: String,
    val rawText: String
)

class GigaChatRepository {

    private val trustAll = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    private val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, arrayOf(trustAll), SecureRandom())
    }

    private val client = OkHttpClient.Builder()
        .sslSocketFactory(sslContext.socketFactory, trustAll)
        .hostnameVerifier { _, _ -> true }
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private var cachedToken: String? = null
    private var tokenExpiresAt: Long = 0L

    private suspend fun fetchToken(): String = withContext(Dispatchers.IO) {
        val body = "scope=GIGACHAT_API_PERS"
            .toRequestBody("application/x-www-form-urlencoded".toMediaType())
        val request = Request.Builder()
            .url("https://ngw.devices.sberbank.ru:9443/api/v2/oauth")
            .post(body)
            .header("Authorization", "Basic ${BuildConfig.GIGACHAT_CLIENT_ID}")
            .header("RqUID", UUID.randomUUID().toString())
            .build()
        val response = client.newCall(request).execute()
        val json = JSONObject(response.body?.string() ?: error("Пустой ответ OAuth"))
        json.getString("access_token")
    }

    private suspend fun getToken(): String {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiresAt) {
            return cachedToken!!
        }
        val token = fetchToken()
        cachedToken = token
        tokenExpiresAt = System.currentTimeMillis() + 29 * 60 * 1000L
        return token
    }

    private suspend fun callChat(token: String, rawText: String): StructuredEntry =
        withContext(Dispatchers.IO) {
            val systemPrompt = """Ты – парсер впечатлений от поездок. Ответь ТОЛЬКО валидным JSON без markdown.
Формат: {"place":"","duration":"","company":"","weather":"","food":"","surprise":"","emotions":"","summary":"","raw_text":""}"""

            val messages = JSONArray().apply {
                put(JSONObject().apply { put("role", "system"); put("content", systemPrompt) })
                put(JSONObject().apply { put("role", "user"); put("content", rawText) })
            }
            val requestBody = JSONObject().apply {
                put("model", "GigaChat-2")
                put("temperature", 0.3)
                put("max_tokens", 1024)
                put("messages", messages)
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://gigachat.devices.sberbank.ru/api/v1/chat/completions")
                .post(requestBody)
                .header("Authorization", "Bearer $token")
                .build()

            val responseStr = client.newCall(request).execute()
                .body?.string() ?: error("Пустой ответ GigaChat")

            val content = JSONObject(responseStr)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

            parseGigaChatJson(content, rawText)
        }

    suspend fun structureEntry(rawText: String): StructuredEntry {
        var lastError: Exception? = null
        repeat(3) { attempt ->
            try {
                return withTimeout(10_000L) {
                    val token = getToken()
                    callChat(token, rawText)
                }
            } catch (e: Exception) {
                lastError = e
                if (attempt < 2) cachedToken = null
            }
        }
        throw lastError ?: Exception("Неизвестная ошибка GigaChat")
    }
}
