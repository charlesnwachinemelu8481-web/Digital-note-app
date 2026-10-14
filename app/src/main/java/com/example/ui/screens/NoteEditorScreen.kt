package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NoteColor
import com.example.ui.FontSize
import com.example.ui.NotesViewModel
import com.example.ui.SaveStatus
import com.example.ui.components.ColorPickerRow
import com.example.ui.components.FormattingToolbar
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
import com.example.ui.theme.CharcoalSubtle
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.SlateBorder

@Composable
fun NoteEditorScreen(
    viewModel: NotesViewModel,
    modifier: Modifier = Modifier
) {
    val editorState by viewModel.editorState.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val fontSizePreference by viewModel.fontSize.collectAsState()
    val context = LocalContext.current

    var isColorPickerVisible by remember { mutableStateOf(false) }
    var isFolderDropdownOpen by remember { mutableStateOf(false) }
    var isMoreMenuOpen by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val noteColor = NoteColor.fromHex(editorState.colorHex)

    // Handle system back press
    BackHandler {
        viewModel.navigateBack()
    }

    // Dynamic font sizing based on user setting
    val contentFontSize = when (fontSizePreference) {
        FontSize.SMALL -> 14.sp
        FontSize.MEDIUM -> 16.sp
        FontSize.LARGE -> 19.sp
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(noteColor.color)
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Bar
            EditorHeader(
                saveStatus = editorState.saveStatus,
                isPinned = editorState.isPinned,
                folderName = folders.firstOrNull { it.id.equals(editorState.folderId, ignoreCase = true) }?.name ?: editorState.folderId,
                onBackClick = { viewModel.navigateBack() },
                onTogglePin = { viewModel.toggleEditorPin() },
                onFolderClick = { isFolderDropdownOpen = true },
                onDeleteClick = { showDeleteDialog = true },
                onShareClick = {
                    val shareText = "${editorState.title}\n\n${editorState.content}"
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share Note")
                    context.startActivity(shareIntent)
                },
                onMoreClick = { isMoreMenuOpen = true }
            )

            // Folder Dropdown Menu
            Box(modifier = Modifier.padding(start = 64.dp)) {
                DropdownMenu(
                    expanded = isFolderDropdownOpen,
                    onDismissRequest = { isFolderDropdownOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Notes / None") },
                        onClick = {
                            viewModel.updateEditorFolder("All")
                            isFolderDropdownOpen = false
                        }
                    )
                    folders.forEach { folder ->
                        DropdownMenuItem(
                            text = { Text(folder.name) },
                            onClick = {
                                viewModel.updateEditorFolder(folder.id)
                                isFolderDropdownOpen = false
                            }
                        )
                    }
                }
            }

            // Collapsible Color Picker Palette
            AnimatedVisibility(
                visible = isColorPickerVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.95f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 2.dp
                ) {
                    ColorPickerRow(
                        selectedColorHex = editorState.colorHex,
                        onColorSelected = { viewModel.updateEditorColor(it) },
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Title Field
                BasicTextField(
                    value = editorState.title,
                    onValueChange = { viewModel.updateEditorTitle(it) },
                    textStyle = TextStyle(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = CharcoalText,
                        lineHeight = 30.sp
                    ),
                    cursorBrush = SolidColor(AmberDark),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    decorationBox = { innerTextField ->
                        Box {
                            if (editorState.title.isEmpty()) {
                                Text(
                                    text = "Title",
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        color = CharcoalSubtle.copy(alpha = 0.45f)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("editor_title_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Content Editor (Rich Checklist or Free Text)
                if (editorState.isChecklist) {
                    ChecklistEditorSection(
                        content = editorState.content,
                        onContentChange = { viewModel.updateEditorContent(it) },
                        onToggleItem = { idx -> viewModel.toggleChecklistItemAt(idx) },
                        onAddItem = { viewModel.appendChecklistItem("") },
                        fontSize = contentFontSize
                    )
                } else {
                    BasicTextField(
                        value = editorState.content,
                        onValueChange = { viewModel.updateEditorContent(it) },
                        textStyle = TextStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                            fontWeight = FontWeight.Normal,
                            fontSize = contentFontSize,
                            color = CharcoalText,
                            lineHeight = (contentFontSize.value * 1.5).sp
                        ),
                        cursorBrush = SolidColor(AmberDark),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        decorationBox = { innerTextField ->
                            Box {
                                if (editorState.content.isEmpty()) {
                                    Text(
                                        text = "Start typing your note, ideas, or markdown…",
                                        style = TextStyle(
                                            fontSize = contentFontSize,
                                            color = CharcoalSubtle.copy(alpha = 0.45f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("editor_content_input")
                    )
                }
            }

            // Bottom Formatting Toolbar
            FormattingToolbar(
                onBoldClick = { viewModel.insertFormatting("**", "**") },
                onItalicClick = { viewModel.insertFormatting("*", "*") },
                onBulletClick = { viewModel.insertBulletPoint() },
                onChecklistClick = { viewModel.toggleEditorChecklistMode() },
                onHeadingClick = { viewModel.insertFormatting("# ", "") },
                onToggleColorPicker = { isColorPickerVisible = !isColorPickerVisible },
                isChecklistActive = editorState.isChecklist,
                isColorPickerOpen = isColorPickerVisible,
                modifier = Modifier.navigationBarsPadding()
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Move to Trash?") },
            text = { Text("This note will be moved to Trash. You can restore it anytime or delete it forever.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteEditorNote()
                    }
                ) {
                    Text("Move to Trash", color = Color(0xFFDC2626))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EditorHeader(
    saveStatus: SaveStatus,
    isPinned: Boolean,
    folderName: String,
    onBackClick: () -> Unit,
    onTogglePin: () -> Unit,
    onFolderClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.testTag("editor_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Save and Back",
                tint = CharcoalText
            )
        }

        // Folder Badge & Auto-save status indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Folder chip
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CharcoalText.copy(alpha = 0.08f),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onFolderClick)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = CharcoalText,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (folderName.equals("all", ignoreCase = true)) "Add to folder" else folderName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = CharcoalText
                        )
                    )
                }
            }

            // Auto-save indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.testTag("auto_save_indicator")
            ) {
                if (saveStatus == SaveStatus.SAVING) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Saving",
                        tint = AmberDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Saving…",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AmberDark,
                            fontSize = 11.sp
                        )
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Saved",
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Saved",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF059669),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Action Icons: Pin, Delete, Share
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onTogglePin,
                modifier = Modifier.testTag("editor_pin_button")
            ) {
                Icon(
                    imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = if (isPinned) "Unpin Note" else "Pin Note",
                    tint = if (isPinned) AmberDark else CharcoalText
                )
            }

            IconButton(
                onClick = onShareClick,
                modifier = Modifier.testTag("editor_share_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = CharcoalText
                )
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.testTag("editor_delete_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Move to Trash",
                    tint = CharcoalText
                )
            }
        }
    }
}

@Composable
private fun ChecklistEditorSection(
    content: String,
    onContentChange: (String) -> Unit,
    onToggleItem: (Int) -> Unit,
    onAddItem: () -> Unit,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier
) {
    val lines = content.lines()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        var checklistIndex = 0
        lines.forEachIndexed { lineIdx, line ->
            if (line.startsWith("[ ] ") || line.startsWith("[x] ")) {
                val isChecked = line.startsWith("[x] ")
                val itemText = line.substring(4)
                val currentIndex = checklistIndex
                checklistIndex++

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onToggleItem(currentIndex) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = "Toggle",
                            tint = if (isChecked) CharcoalSubtle else CharcoalText,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    BasicTextField(
                        value = itemText,
                        onValueChange = { newText ->
                            val updatedLines = lines.toMutableList()
                            val prefix = if (isChecked) "[x] " else "[ ] "
                            updatedLines[lineIdx] = prefix + newText
                            onContentChange(updatedLines.joinToString("\n"))
                        },
                        textStyle = TextStyle(
                            fontSize = fontSize,
                            color = if (isChecked) CharcoalSubtle.copy(alpha = 0.6f) else CharcoalText,
                            textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        cursorBrush = SolidColor(AmberDark),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            val updatedLines = lines.toMutableList()
                            updatedLines.removeAt(lineIdx)
                            onContentChange(updatedLines.joinToString("\n"))
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove item",
                            tint = CharcoalSubtle.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                // Free text line within checklist
                BasicTextField(
                    value = line,
                    onValueChange = { newLine ->
                        val updatedLines = lines.toMutableList()
                        updatedLines[lineIdx] = newLine
                        onContentChange(updatedLines.joinToString("\n"))
                    },
                    textStyle = TextStyle(
                        fontSize = fontSize,
                        color = CharcoalText
                    ),
                    cursorBrush = SolidColor(AmberDark),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // "+ Add Checklist Item" Button
        OutlinedButton(
            onClick = onAddItem,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .padding(top = 10.dp)
                .testTag("add_checklist_item_button")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = CharcoalText,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Add checklist item",
                style = MaterialTheme.typography.labelLarge.copy(color = CharcoalText)
            )
        }
    }
}
