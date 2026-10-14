package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NoteColor
import com.example.ui.FontSize
import com.example.ui.NotesViewModel
import com.example.ui.components.ColorPickerRow
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
import com.example.ui.theme.CharcoalSubtle
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.WarmOffWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: NotesViewModel,
    modifier: Modifier = Modifier
) {
    val defaultNoteColor by viewModel.defaultNoteColor.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val isBackupSyncEnabled by viewModel.isBackupSyncEnabled.collectAsState()
    val lastBackupTimestamp by viewModel.lastBackupTimestamp.collectAsState()
    val activeNotes by viewModel.allActiveNotes.collectAsState()
    val folders by viewModel.folders.collectAsState()

    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmOffWhite)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Screen Header
            item {
                Column {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )
                    Text(
                        text = "Customize your notebook preferences",
                        style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalSubtle)
                    )
                }
            }

            // Section 1: Note Appearance Defaults
            item {
                SettingsSectionCard(title = "Default Note Appearance") {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = null,
                                tint = AmberDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Default Note Color",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = CharcoalText
                                )
                            )
                        }

                        ColorPickerRow(
                            selectedColorHex = defaultNoteColor.bgHex,
                            onColorSelected = { hex ->
                                viewModel.setDefaultNoteColor(NoteColor.fromHex(hex))
                            },
                            showLabel = true
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Font Size Selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = null,
                                tint = AmberDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Editor Font Size",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = CharcoalText
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FontSize.entries.forEach { sizeOption ->
                                val isSelected = fontSize == sizeOption
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) AmberDark else SlateBorder,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { viewModel.setFontSize(sizeOption) }
                                        .testTag("font_size_${sizeOption.name}"),
                                    color = if (isSelected) AmberAccent.copy(alpha = 0.25f) else Color.White
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = sizeOption.label,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = CharcoalText
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Backup & Sync
            item {
                SettingsSectionCard(title = "Cloud & Local Backup") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Backup & Sync",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = CharcoalText
                                    )
                                )
                                Text(
                                    text = "Sync notes to device storage & cloud cache",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = CharcoalSubtle,
                                        fontSize = 12.5.sp
                                    )
                                )
                            }

                            Switch(
                                checked = isBackupSyncEnabled,
                                onCheckedChange = { viewModel.toggleBackupSync() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CharcoalText,
                                    checkedTrackColor = AmberAccent
                                ),
                                modifier = Modifier.testTag("backup_sync_toggle")
                            )
                        }

                        if (isBackupSyncEnabled) {
                            val formattedTime = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault()).format(Date(lastBackupTimestamp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDone,
                                            contentDescription = null,
                                            tint = Color(0xFF059669),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Last synced: $formattedTime",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 12.sp,
                                                color = CharcoalSubtle
                                            )
                                        )
                                    }

                                    TextButton(
                                        onClick = {
                                            viewModel.performManualSync()
                                            Toast.makeText(context, "Notes synced successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text(
                                            text = "Sync Now",
                                            style = MaterialTheme.typography.labelSmall.copy(color = AmberDark)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Notebook Statistics
            item {
                SettingsSectionCard(title = "Notebook Statistics") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(label = "Active Notes", value = "${activeNotes.size}")
                        StatItem(label = "Pinned", value = "${activeNotes.count { it.isPinned }}")
                        StatItem(label = "Checklists", value = "${activeNotes.count { it.isChecklist }}")
                        StatItem(label = "Folders", value = "${folders.size}")
                    }
                }
            }

            // Section 4: Data Actions
            item {
                SettingsSectionCard(title = "Manage Notes Data") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("button_reset_sample_notes")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Sample Notes & Folders")
                        }
                    }
                }
            }

            // Section 5: About QuickNote
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "QuickNote v1.0",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )
                    Text(
                        text = "Inspired by Apple Notes & Google Keep with warm pastel aesthetic.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CharcoalSubtle,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Restore Sample Notes?") },
            text = { Text("This will add default sample notes and categories (recipes, ideas, travel checklists).") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetSampleData()
                        showResetDialog = false
                        Toast.makeText(context, "Sample notes reloaded!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberAccent,
                        contentColor = CharcoalText
                    )
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, SlateBorder, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalSubtle,
                    letterSpacing = 0.5.sp
                )
            )
            content()
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = AmberDark
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = CharcoalSubtle,
                fontSize = 11.sp
            )
        )
    }
}
