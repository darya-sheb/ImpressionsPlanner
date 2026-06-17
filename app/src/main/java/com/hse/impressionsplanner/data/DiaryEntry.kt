package com.hse.impressionsplanner.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DiaryEntry(
    val id: String = "",
    val userId: String = "",
    val date: Long = System.currentTimeMillis(),
    val rawText: String = "",
    val place: String = "",
    val duration: String = "",
    val company: String = "",
    val weather: String = "",
    val food: String = "",
    val surprise: String = "",
    val emotions: String = "",
    val summary: String = ""
) {
    fun formattedDate(): String =
        SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.forLanguageTag("ru")).format(Date(date))
}
