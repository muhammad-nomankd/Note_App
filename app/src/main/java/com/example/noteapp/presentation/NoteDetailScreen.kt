package com.example.noteapp.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.noteapp.presentation.viewmodel.NoteDetailViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import java.time.LocalDateTime // Added for the new formatter
import java.time.format.DateTimeFormatter // Added for the new formatter
import java.util.Locale

// New helper function to reformat ISO_LOCAL_DATE_TIME string
@RequiresApi(Build.VERSION_CODES.O)
private fun formatIsoDateTimeString(isoDateTime: String): String {
    if (isoDateTime.isBlank()) return "Unknown date" // Or handle as you see fit
    return try {
        val localDateTime = LocalDateTime.parse(isoDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        localDateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm", Locale.getDefault()))
    } catch (e: Exception) {
        // Log an error or return the original string if parsing fails
        // For example: Log.e("NoteDetailScreen", "Failed to parse date: $isoDateTime", e)
        isoDateTime // Fallback to original string
    }
}

// Keep your existing formatDate(Long) if it's used elsewhere, or remove if not.
// For this screen, we'll use formatIsoDateTimeString.

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Long,
    onBackClick: () -> Unit,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    // ... (rest of your state and LaunchedEffect code) ...
    val note by viewModel.note.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val contentFocusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
        if (noteId == -1L && note.title.isEmpty() && note.content.isEmpty()) {
            contentFocusRequester.requestFocus()
        }
    }
    
    val topBarTitle = when {
        noteId == -1L && note.title.isBlank() -> "New Note" 
        note.title.isNotBlank() -> note.title
        else -> "Edit Note"
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topBarTitle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            viewModel.saveNote(
                                onSaved = {} // Callback can be used for UI updates if needed
                            )
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleFavorite() }
                    ) {
                        Icon(
                            imageVector = if (note.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Toggle favorite",
                            tint = if (note.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ... (Title TextField) ...
                TextField(
                    value = note.title,
                    onValueChange = viewModel::updateTitle,
                    label = { Text("Title") },
                    placeholder = { Text("Note title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    ),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall,
                    shape = MaterialTheme.shapes.medium
                )

                // Last Updated Timestamp - CORRECTED
                if (noteId != -1L && note.updatedAt.isNotBlank()) { // Use isNotBlank() for String
                    Text(
                        // Use the new helper function for String dates
                        text = "Last updated: ${formatIsoDateTimeString(note.updatedAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 0.dp, bottom = 4.dp)
                    )
                }

                // ... (Content TextField) ...
                 TextField(
                    value = note.content,
                    onValueChange = viewModel::updateContent,
                    label = { Text("Note") },
                    placeholder = { Text("Start writing your note here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) 
                        .focusRequester(contentFocusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = MaterialTheme.shapes.medium,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                )
            }
        }
    }
}
