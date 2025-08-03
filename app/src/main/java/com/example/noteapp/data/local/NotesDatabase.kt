package com.example.noteapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.noteapp.domain.model.Note

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class NotesDatabase(): RoomDatabase() {
    abstract fun noteDao(): NoteDao

}