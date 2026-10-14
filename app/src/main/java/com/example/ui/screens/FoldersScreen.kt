package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Folder
import com.example.ui.NotesViewModel
import com.example.ui.Screen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
import com.example.ui.theme.CharcoalSubtle
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.WarmOffWhite

@Composable
fun FoldersScreen(
    viewModel: NotesViewModel,
    modifier: Modifier = Modifier
) {
    val folders by viewModel.folders.collectAsState()
    val allNotes by viewModel.allActiveNotes.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var folderToDelete by remember { mutableStateOf<Folder?>(null) }

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
                        text = "Folders & Tags",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )
                    Text(
                        text = "${folders.size} organized categories",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = CharcoalSubtle
                        )
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberAccent,
                        contentColor = CharcoalText
                    ),
                    modifier = Modifier.testTag("button_new_folder")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New Folder",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // List of Folders
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("folders_list")
            ) {
                // "All Notes" virtual folder
                item {
                    val allCount = allNotes.size
                    FolderListItem(
                        name = "All Notes",
                        color = AmberDark,
                        count = allCount,
                        canDelete = false,
                        onClick = {
                            viewModel.selectFolder("all")
                            viewModel.navigateTo(Screen.HOME)
                        },
                        onDelete = {}
                    )
                }

                items(folders, key = { it.id }) { folder ->
                    val noteCount = allNotes.count { it.folderId.equals(folder.id, ignoreCase = true) }
                    FolderListItem(
                        name = folder.name,
                        color = Color(android.graphics.Color.parseColor(folder.colorHex)),
                        count = noteCount,
                        canDelete = true,
                        onClick = {
                            viewModel.selectFolder(folder.id)
                            viewModel.navigateTo(Screen.HOME)
                        },
                        onDelete = { folderToDelete = folder }
                    )
                }
            }
        }
    }

    // New Folder Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                newFolderName = ""
            },
            title = {
                Text(
                    text = "Create New Folder",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter a name for the new folder to organize your notes:",
                        style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalSubtle)
                    )
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        placeholder = { Text("e.g. Travel, Recipes, Work") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberAccent,
                            cursorColor = AmberDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_folder_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            viewModel.createFolder(newFolderName)
                            newFolderName = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberAccent,
                        contentColor = CharcoalText
                    ),
                    modifier = Modifier.testTag("confirm_create_folder")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateDialog = false
                    newFolderName = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Folder Dialog
    folderToDelete?.let { folder ->
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            title = { Text("Delete folder \"${folder.name}\"?") },
            text = { Text("The folder will be deleted. Any notes inside will be kept in All Notes.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFolder(folder)
                        folderToDelete = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFDC2626))
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun FolderListItem(
    name: String,
    color: Color,
    count: Int,
    canDelete: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("folder_item_$name"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = CharcoalText
                        )
                    )
                    Text(
                        text = "$count ${if (count == 1) "note" else "notes"}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = CharcoalSubtle,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Folder",
                            tint = CharcoalSubtle.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = CharcoalSubtle.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
