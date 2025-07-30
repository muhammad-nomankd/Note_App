package com.example.noteapp.domain.model

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: String = LocalDateTime.now()
        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    val updatedAt: String = LocalDateTime.now()
        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    val isFavorite: Boolean = false,
) {
    var preview =
        content.take(100).replace("\n", " ").trim()

    var isEmpty =
        title.isEmpty() && content.isEmpty()
}