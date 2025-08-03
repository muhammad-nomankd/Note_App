package com.example.noteapp.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.noteapp.presentation.components.NoteItem
import com.example.noteapp.presentation.components.SearchBar
import com.example.noteapp.presentation.viewmodel.NoteViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun NotesListScreen(
    onNoteClick: (Long) -> Unit,
    onAddNoteClick: () -> Unit,
    viewModel: NoteViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val showFavoritesOnly by viewModel.showFavoriteNotesOnly.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = if (showFavoritesOnly) "Favorite Notes" else "Notes",
                        // Consider using a more specific headline style if available, e.g., MaterialTheme.typography.headlineLarge
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleFavoritesFilter() }
                    ) {
                        Icon(
                            imageVector = if (showFavoritesOnly) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Toggle favorites filter",
                            tint = if (showFavoritesOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNoteClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer, // M3 recommendation
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer  // M3 recommendation
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add note")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp) // Adjusted padding
            )

            AnimatedContent(
                targetState = notes.isEmpty(),
                label = "EmptyListAnimation",
                transitionSpec = {
                    // Compare the incoming target state with the initial state.
                    if (targetState) { // Transitioning to empty state
                        fadeIn() togetherWith fadeOut()
                    } else { // Transitioning to list state
                        fadeIn() togetherWith fadeOut()
                    }.using(
                        // Disable clipping since the faded content might be temporarily clipped.
                        SizeTransform(clip = false)
                    )
                }
            ) { isEmpty ->
                if (isEmpty) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp), // Added padding for the content
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center // Ensure vertical centering
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Note, // Consider a more illustrative icon if available e.g. Icons.Outlined.EventNote
                                contentDescription = null,
                                modifier = Modifier.size(80.dp), // Slightly larger icon
                                tint = MaterialTheme.colorScheme.secondary // Use a secondary or outline color
                            )
                            Spacer(modifier = Modifier.height(24.dp)) // Increased spacer
                            Text(
                                text = if (searchQuery.isNotBlank()) "No notes found" else "No notes yet",
                                style = MaterialTheme.typography.titleLarge, // Larger text for emphasis
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "Try a different search term or clear the filter." else "Tap the '+' button to create your first note!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp) // Slightly increased spacing
                    ) {
                        items(
                            items = notes,
                            key = { it.id }
                        ) { note ->
                            NoteItem(
                                modifier = Modifier.animateItem(), // Animate item placement
                                note = note,
                                onClick = { onNoteClick(note.id) },
                                onFavoriteClick = { viewModel.toggleFavorite(note) },
                                onDeleteClick = { viewModel.deleteNote(note) }
                            )
                        }
                    }
                }
            }
        }
    }
}
