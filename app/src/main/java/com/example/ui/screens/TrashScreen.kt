package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Note
import com.example.data.NoteColor
import com.example.ui.NotesViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
import com.example.ui.theme.CharcoalSubtle
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.WarmOffWhite

@Composable
fun TrashScreen(
    viewModel: NotesViewModel,
    modifier: Modifier = Modifier
) {
    val trashNotes by viewModel.trashNotes.collectAsState()
    var showEmptyTrashDialog by remember { mutableStateOf(false) }
    var noteToDeleteForever by remember { mutableStateOf<Note?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmOffWhite)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Trash",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )
                    Text(
                        text = "${trashNotes.size} deleted ${if (trashNotes.size == 1) "note" else "notes"}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalSubtle)
                    )
                }

                if (trashNotes.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showEmptyTrashDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        modifier = Modifier.testTag("empty_trash_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Empty Trash", fontSize = 13.sp)
                    }
                }
            }

            // Info Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFEF3C7).copy(alpha = 0.7f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = AmberDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Notes in trash can be restored back to your notebook or permanently removed.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.5.sp,
                            color = Color(0xFF92400E)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Trash items list
            if (trashNotes.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = CharcoalSubtle,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Trash is empty",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = CharcoalText
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Notes you delete will appear here.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalSubtle)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("trash_notes_list")
                ) {
                    items(trashNotes, key = { it.id }) { note ->
                        TrashNoteItem(
                            note = note,
                            onRestore = { viewModel.restoreNote(note) },
                            onDeleteForever = { noteToDeleteForever = note }
                        )
                    }
                }
            }
        }
    }

    // Confirmation dialog for Empty Trash
    if (showEmptyTrashDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashDialog = false },
            title = { Text("Empty Trash?") },
            text = { Text("All ${trashNotes.size} notes in trash will be permanently deleted. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.emptyTrash()
                        showEmptyTrashDialog = false
                    }
                ) {
                    Text("Delete All", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation dialog for Delete Forever single note
    noteToDeleteForever?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDeleteForever = null },
            title = { Text("Permanently delete note?") },
            text = { Text("\"${note.title}\" will be permanently erased.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePermanently(note)
                        noteToDeleteForever = null
                    }
                ) {
                    Text("Delete Forever", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDeleteForever = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TrashNoteItem(
    note: Note,
    onRestore: () -> Unit,
    onDeleteForever: () -> Unit
) {
    val noteColor = NoteColor.fromHex(note.colorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, noteColor.borderColor.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            .testTag("trash_item_${note.id}"),
        colors = CardDefaults.cardColors(containerColor = noteColor.color),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (note.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = CharcoalSubtle,
                            fontSize = 13.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Restore button
                IconButton(
                    onClick = onRestore,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("restore_note_${note.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Restore Note",
                        tint = Color(0xFF059669)
                    )
                }

                // Delete Forever button
                IconButton(
                    onClick = onDeleteForever,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("delete_forever_${note.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Delete Forever",
                        tint = Color(0xFFDC2626)
                    )
                }
            }
        }
    }
}
