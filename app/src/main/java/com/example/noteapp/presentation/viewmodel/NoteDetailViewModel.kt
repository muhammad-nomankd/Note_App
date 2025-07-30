package com.example.noteapp.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.data.repository.NoteRepository
import com.example.noteapp.domain.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(private val noteRepository: NoteRepository) :
    ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    private val _note = MutableStateFlow(Note(title = "", content = ""))

    @RequiresApi(Build.VERSION_CODES.O)
    val note = _note.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadNote(noteId: Long) {
        if (noteId == -1L) return

        viewModelScope.launch {
            _isLoading.value = true
            noteRepository.getNoteById(noteId)?.collect { noteList ->
                _note.value = noteList.firstOrNull() ?: (Note(title = "", content = ("")))
            }
            _isLoading.value = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateTitle(title: String) {
        _note.value = _note.value.copy(title = title)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateContent(content: String) {
        _note.value = _note.value.copy(content = content)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveNote(onSaved: () -> Unit) {

        viewModelScope.launch {
            val currentNote = _note.value

            if (currentNote.isEmpty) return@launch

            val noteToSave = if (currentNote.id == 0L) {
                currentNote.copy(
                    createdAt = java.time.LocalDateTime.now()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    updatedAt = java.time.LocalDateTime.now()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )

            } else {
                currentNote.copy(
                    updatedAt = java.time.LocalDateTime.now()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
            }

            if (currentNote.id == 0L) {
                val newId = noteRepository.insertNote(noteToSave)
                _note.value = noteToSave.copy(id = newId)
            } else {
                noteRepository.updateNote(noteToSave)
            }
            onSaved()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleFavorite(){
        _note.value = _note.value.copy(isFavorite = !_note.value.isFavorite)
    }
}