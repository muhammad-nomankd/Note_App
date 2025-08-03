package com.example.noteapp.presentation.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.noteapp.domain.model.Note

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // You can also try CardDefaults.elevatedCardElevation()
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh // Softer background
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top // Keep title and icons aligned to the top
            ) {
                // Text content column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp) // Add some padding so text doesn't hit icons
                ) {
                    if (note.title.isNotBlank()) {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface // Ensure good contrast
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (note.preview.isNotBlank()) {
                        Text(
                            text = note.preview,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, // Good for secondary text
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Icons row
                Row(
                    verticalAlignment = Alignment.CenterVertically // Center icons vertically within their row
                ) {
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(40.dp) // Increased touch target
                    ) {
                        Icon(
                            imageVector = if (note.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Toggle favorite",
                            tint = if (note.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, // Better tint for non-favorite state
                            modifier = Modifier.size(20.dp) // Slightly larger icon
                        )
                    }

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(40.dp) // Increased touch target
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete note",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, // Consistent and less stark than 'error' for an item action
                            modifier = Modifier.size(20.dp) // Slightly larger icon
                        )
                    }
                }
            }

            // Ensure there's content to show before adding space for the date
            if (note.title.isNotBlank() || note.preview.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = formatDate(note.updatedAt), // Assuming formatDate is defined elsewhere
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant // Better than 'outline' for legibility
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Helper function (if not already defined elsewhere)
// fun formatDate(timestamp: Long): String {
//     // Implement your date formatting logic here, e.g., using SimpleDateFormat or java.time
//     // return "Formatted Date"
// }
