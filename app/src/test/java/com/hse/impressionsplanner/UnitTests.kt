package com.hse.impressionsplanner

import com.hse.impressionsplanner.data.DiaryEntry
import com.hse.impressionsplanner.data.Place
import com.hse.impressionsplanner.data.repository.parseGigaChatJson
import com.hse.impressionsplanner.ui.auth.validateEmail
import com.hse.impressionsplanner.ui.auth.validatePassword
import com.hse.impressionsplanner.ui.constructor.applyFilters
import com.hse.impressionsplanner.ui.constructor.buildYandexUri
import org.junit.Assert.*
import org.junit.Test

class UnitTests {

    // ── Валидация email ────────────────────────────────────────────────

    @Test fun email_valid_returnsNull() =
        assertNull(validateEmail("user@example.com"))

    @Test fun email_noAt_returnsError() =
        assertNotNull(validateEmail("userexample.com"))

    @Test fun email_noDomain_returnsError() =
        assertNotNull(validateEmail("user@"))

    @Test fun email_empty_returnsError() =
        assertNotNull(validateEmail(""))

    // ── Валидация пароля ───────────────────────────────────────────────

    @Test fun password_tooShort_returnsError() =
        assertNotNull(validatePassword("Ab1"))

    @Test fun password_noUppercase_returnsError() =
        assertNotNull(validatePassword("abcdef1"))

    @Test fun password_noDigit_returnsError() =
        assertNotNull(validatePassword("Abcdefg"))

    @Test fun password_valid_returnsNull() =
        assertNull(validatePassword("Abcdef1"))

    // ── Фильтрация мест ────────────────────────────────────────────────

    private val testPlaces = listOf(
        Place("1", "Третьяковка",   "", "Музеи",              "", "Лаврушинский пер."),
        Place("2", "Парк Горького", "", "Парки",              "", "Крымский Вал"),
        Place("3", "Кафе Пушкинъ", "", "Рестораны",          "", "Тверской бул."),
        Place("4", "Лужники",       "", "Развлечения",        "", "Лужники, 24"),
        Place("5", "Красная пл.",   "", "Исторические места", "", "Красная пл., 1")
    )

    @Test fun filter_noFilters_returnsAll() {
        val result = applyFilters(testPlaces, "", "Все", emptySet(), emptySet())
        assertEquals(testPlaces.size, result.size)
    }

    @Test fun filter_people_pair_excludesEntertainment() {
        val result = applyFilters(testPlaces, "", "Пара", emptySet(), emptySet())
        assertTrue(result.none { it.category == "Развлечения" })
        assertTrue(result.any { it.category == "Рестораны" })
    }

    @Test fun filter_age_children_onlyParksAndEntertainment() {
        val result = applyFilters(testPlaces, "", "Все", setOf("Дети (0-6)"), emptySet())
        assertTrue(result.all { it.category in setOf("Парки", "Развлечения") })
    }

    @Test fun filter_type_gastronomy_onlyRestaurants() {
        val result = applyFilters(testPlaces, "", "Все", emptySet(), setOf("Гастрономия"))
        assertTrue(result.all { it.category == "Рестораны" })
        assertEquals(1, result.size)
    }

    @Test fun filter_search_byName_caseInsensitive() {
        val result = applyFilters(testPlaces, "горького", "Все", emptySet(), emptySet())
        assertEquals(1, result.size)
        assertEquals("Парк Горького", result[0].name)
    }

    @Test fun filter_search_byAddress() {
        val result = applyFilters(testPlaces, "Крымский", "Все", emptySet(), emptySet())
        assertEquals(1, result.size)
        assertEquals("Парк Горького", result[0].name)
    }

    // ── Формирование URI Яндекс Карт ───────────────────────────────────

    @Test fun uri_withCoords_usesRtext() {
        val places = listOf(
            Place("1", "A", "", "", "", "", lat = 55.74, lon = 37.62),
            Place("2", "B", "", "", "", "", lat = 55.73, lon = 37.60)
        )
        val uri = buildYandexUri(places)
        assertTrue(uri.startsWith("yandexmaps://maps.yandex.ru/?rtext="))
        assertTrue(uri.contains("55.74,37.62~55.73,37.6"))
        assertTrue(uri.endsWith("&rtt=pd"))
    }

    @Test fun uri_withoutCoords_usesAddress() {
        val places = listOf(Place("1", "Место", "", "", "", "Красная площадь"))
        val uri = buildYandexUri(places)
        assertTrue(uri.contains("text=Красная площадь"))
    }

    // ── Парсинг JSON от GigaChat ───────────────────────────────────────

    @Test fun json_valid_parsesAllFields() {
        val json = """{"place":"Парк Горького","duration":"2 часа","company":"семья",
            "weather":"солнечно","food":"мороженое","surprise":"фонтан",
            "emotions":"восторг","summary":"Отличная прогулка"}"""
        val entry = parseGigaChatJson(json, "исходный текст")
        assertEquals("Парк Горького", entry.place)
        assertEquals("2 часа", entry.duration)
        assertEquals("восторг", entry.emotions)
        assertEquals("исходный текст", entry.rawText)
        assertEquals("Отличная прогулка", entry.summary)
    }

    @Test fun json_missingFields_fallbackToDefaults() {
        val json = """{"place":"Музей"}"""
        val entry = parseGigaChatJson(json, "мой текст")
        assertEquals("Музей", entry.place)
        assertEquals("", entry.duration)
        assertEquals("мой текст", entry.summary)
    }

    // ── DiaryEntry ─────────────────────────────────────────────────────

    @Test fun diaryEntry_formattedDate_notEmpty() {
        val entry = DiaryEntry(date = System.currentTimeMillis())
        val formatted = entry.formattedDate()
        assertTrue(formatted.isNotEmpty())
        assertTrue(formatted.contains("2025") || formatted.contains("2026"))
    }
}
