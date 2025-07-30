package com.example.noteapp.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.data.repository.NoteRepository
import com.example.noteapp.domain.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(private val noteRepository: NoteRepository): ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

  private val _showFavoriteNotesOnly = MutableStateFlow(false)
    val showFavoriteNotesOnly = _showFavoriteNotesOnly.asStateFlow()

    val notes = combine(
        searchQuery,
        showFavoriteNotesOnly
    ) { query, favoritesOnly ->
        when {
            query.isNotBlank() -> noteRepository.searchNotes(query)
            favoritesOnly -> noteRepository.getFavorite()
            else -> noteRepository.getAllNotes()
        }
    }.flatMapLatest { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query:String){
        _searchQuery.value = query
    }

    fun toggleFavoritesFilter(){
        _showFavoriteNotesOnly.value = !_showFavoriteNotesOnly.value
    }

    fun deleteNote(note: Note){
        viewModelScope.launch {
            noteRepository.deleteNote(note)
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleFavorite(note: Note) {
        viewModelScope.launch {
            noteRepository.updateNote(note.copy(isFavorite = !note.isFavorite))
        }

    }


}