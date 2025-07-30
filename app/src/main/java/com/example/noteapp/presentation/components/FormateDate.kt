package com.example.noteapp.presentation.components

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun formatDate(dateString: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val noteDate = dateTime.toLocalDate()

        when {
            noteDate == today -> "Today ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            noteDate == today.minusDays(1) -> "Yesterday ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            else -> dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        }
    } catch (e: Exception) {
        dateString
    }
}
