package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Note
import com.example.data.NoteColor
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CharcoalSubtle
import com.example.ui.theme.CharcoalText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onPinClick: () -> Unit,
    onChecklistToggle: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val noteColor = NoteColor.fromHex(note.colorHex)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, noteColor.borderColor.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("note_card_${note.id}"),
        colors = CardDefaults.cardColors(
            containerColor = noteColor.color
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Title and Pin Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText,
                        lineHeight = 22.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (note.isPinned) {
                    Box(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(AmberAccent.copy(alpha = 0.9f))
                            .clickable(onClick = onPinClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = "Pinned note",
                            tint = CharcoalText,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Content preview (Text or Checklists)
            val lines = note.content.lines().filter { it.isNotBlank() }
            val hasChecklistItems = lines.any { it.startsWith("[ ] ") || it.startsWith("[x] ") }

            if (hasChecklistItems) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    var checklistIndex = 0
                    val itemsToShow = lines.take(5)
                    itemsToShow.forEach { line ->
                        if (line.startsWith("[ ] ") || line.startsWith("[x] ")) {
                            val isChecked = line.startsWith("[x] ")
                            val itemText = line.substring(4)
                            val currentIndex = checklistIndex
                            checklistIndex++

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(enabled = onChecklistToggle != null) {
                                        onChecklistToggle?.invoke(currentIndex)
                                    }
                                    .padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = if (isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = if (isChecked) "Checked" else "Unchecked",
                                    tint = if (isChecked) CharcoalSubtle else CharcoalText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = itemText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (isChecked) CharcoalSubtle.copy(alpha = 0.6f) else CharcoalText,
                                        fontSize = 13.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = CharcoalSubtle,
                                    fontSize = 13.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (lines.size > 5) {
                        Text(
                            text = "+${lines.size - 5} more items",
                            style = MaterialTheme.typography.labelSmall.copy(color = CharcoalSubtle),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            } else if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CharcoalSubtle,
                        lineHeight = 18.sp,
                        fontSize = 13.5.sp
                    ),
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Footer: Folder Chip & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (note.folderId.isNotBlank() && !note.folderId.equals("all", ignoreCase = true)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CharcoalText.copy(alpha = 0.08f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = CharcoalText.copy(alpha = 0.7f),
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = note.folderId.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = CharcoalText.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }

                val dateFormatted = formatNoteDate(note.updatedAt)
                Text(
                    text = dateFormatted,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CharcoalSubtle.copy(alpha = 0.75f),
                        fontSize = 10.5.sp
                    )
                )
            }
        }
    }
}

private fun formatNoteDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
        diff < 86400000 * 7 -> SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp))
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
