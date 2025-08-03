package com.example.noteapp.presentation.viewmodel

import android.os.Build
import android.util.Log // Optional: for logging errors
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.data.repository.NoteRepository
import com.example.noteapp.domain.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch // <<< Import this
import kotlinx.coroutines.flow.launchIn // <<< Import this
import kotlinx.coroutines.flow.onEach // <<< Import this
import kotlinx.coroutines.launch // Keep this for the overall structure if preferred
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(private val noteRepository: NoteRepository) :
    ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    private val _note = MutableStateFlow(Note(title = "", content = "")) // Assuming Note has defaults for other fields

    @RequiresApi(Build.VERSION_CODES.O)
    val note = _note.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadNote(noteId: Long) {
        if (noteId == -1L) {
            // For a new note:
            // Ensure _note is set to a fresh instance.
            // Assuming your Note data class has appropriate defaults or this constructor is sufficient.
            _note.value = Note(title = "", content = "")
            _isLoading.value = false // Ensure isLoading is false for new notes.
            return
        }

        // For an existing note:
        // The viewModelScope.launch here is fine, or .launchIn can directly use it.
        // viewModelScope.launch { // This outer launch is okay
        val noteFlow = noteRepository.getNoteById(noteId)

        if (noteFlow == null) {
            // Handle case where the repository indicates the note doesn't exist by returning a null Flow object.
            Log.w("NoteDetailViewModel", "Note flow is null for noteId: $noteId")
            _note.value = Note(title = "Note not found", content = "") // Or some other appropriate default/error state
            _isLoading.value = false
        } else {
            noteFlow
                .onEach { noteList -> // Called for each emission from the Flow
                    _isLoading.value = true // Set to true at the start of processing each emission
                    _note.value = noteList.firstOrNull() ?: Note(title = "Note details not found", content = "")
                    _isLoading.value = false // Crucially, set isLoading to false after data is processed.
                }
                .catch { e -> // Handle any errors during Flow collection
                    Log.e("NoteDetailViewModel", "Error loading note $noteId", e)
                    _note.value = Note(title = "Error", content = "Could not load note details.")
                    _isLoading.value = false // Also set isLoading to false on error.
                }
                .launchIn(viewModelScope) // Collect the Flow within the viewModelScope. This starts the flow.
        }
        // }
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

            // Assuming 'isEmpty' is a property on your Note model
            if (currentNote.title.isBlank() && currentNote.content.isBlank()) { // More explicit check
                Log.d("NoteDetailViewModel", "Attempted to save an empty note. Aborting.")
                onSaved() // Call onSaved anyway, or handle as desired
                return@launch
            }


            val noteToSave = if (currentNote.id == 0L) { // Assuming 0L is the ID for a new, unsaved note
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

            // Perform DB operation
            _isLoading.value = true // Indicate saving process
            try {
                if (noteToSave.id == 0L) { // Check noteToSave's ID
                    val newId = noteRepository.insertNote(noteToSave)
                    _note.value = noteToSave.copy(id = newId)
                } else {
                    noteRepository.updateNote(noteToSave)
                    // _note.value remains noteToSave (updatedAt is changed)
                    _note.value = noteToSave // Ensure UI reflects the 'updatedAt' change if necessary
                }
            } catch (e: Exception) {
                Log.e("NoteDetailViewModel", "Error saving note", e)
                // Optionally, communicate error to UI
            } finally {
                _isLoading.value = false // Finish loading state
                onSaved()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleFavorite(){
        _note.value = _note.value.copy(isFavorite = !_note.value.isFavorite)
        // Consider saving this change immediately or marking as 'dirty' for next save
        // For now, it only updates local state until next explicit save.
    }
}
