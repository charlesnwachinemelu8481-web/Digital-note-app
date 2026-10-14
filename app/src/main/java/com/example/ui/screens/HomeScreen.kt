package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Note
import com.example.ui.NotesViewModel
import com.example.ui.Screen
import com.example.ui.components.NoteCard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
import com.example.ui.theme.CharcoalSubtle
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateLight
import com.example.ui.theme.WarmOffWhite

@Composable
fun HomeScreen(
    viewModel: NotesViewModel,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.homeNotes.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val selectedFolderId by viewModel.selectedFolderId.collectAsState()
    val isGridLayout by viewModel.isGridLayout.collectAsState()

    val pinnedNotes = notes.filter { it.isPinned }
    val otherNotes = notes.filter { !it.isPinned }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmOffWhite)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // App Bar with Quick Search Input
            TopSearchBar(
                onSearchClick = { viewModel.navigateTo(Screen.SEARCH) },
                isGridLayout = isGridLayout,
                onToggleLayout = { viewModel.toggleGridLayout() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )

            // Horizontal Filter Chips (All, Folders...)
            FoldersFilterChipsRow(
                selectedFolderId = selectedFolderId,
                folders = folders,
                onSelectFolder = { viewModel.selectFolder(it) },
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Content Area: Staggered Masonry Grid or List
            if (notes.isEmpty()) {
                EmptyNotesState(
                    selectedFolder = selectedFolderId,
                    onCreateNote = { viewModel.openNewNote() }
                )
            } else {
                if (isGridLayout) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 96.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalItemSpacing = 10.dp,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("notes_staggered_grid")
                    ) {
                        if (pinnedNotes.isNotEmpty()) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                SectionHeader(title = "PINNED", count = pinnedNotes.size)
                            }
                            items(pinnedNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    onClick = { viewModel.openNote(note) },
                                    onPinClick = { viewModel.togglePin(note) },
                                    onChecklistToggle = { idx ->
                                        viewModel.toggleChecklistItemDirect(note.id, idx)
                                    }
                                )
                            }
                            if (otherNotes.isNotEmpty()) {
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    SectionHeader(title = "OTHERS", count = otherNotes.size)
                                }
                            }
                        }

                        items(otherNotes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                onClick = { viewModel.openNote(note) },
                                onPinClick = { viewModel.togglePin(note) },
                                onChecklistToggle = { idx ->
                                    viewModel.toggleChecklistItemDirect(note.id, idx)
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("notes_list_view")
                    ) {
                        if (pinnedNotes.isNotEmpty()) {
                            item {
                                SectionHeader(title = "PINNED", count = pinnedNotes.size)
                            }
                            items(pinnedNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    onClick = { viewModel.openNote(note) },
                                    onPinClick = { viewModel.togglePin(note) },
                                    onChecklistToggle = { idx ->
                                        viewModel.toggleChecklistItemDirect(note.id, idx)
                                    }
                                )
                            }
                            if (otherNotes.isNotEmpty()) {
                                item {
                                    SectionHeader(title = "OTHERS", count = otherNotes.size)
                                }
                            }
                        }

                        items(otherNotes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                onClick = { viewModel.openNote(note) },
                                onPinClick = { viewModel.togglePin(note) },
                                onChecklistToggle = { idx ->
                                    viewModel.toggleChecklistItemDirect(note.id, idx)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button for New Note
        FloatingActionButton(
            onClick = { viewModel.openNewNote() },
            containerColor = AmberAccent,
            contentColor = CharcoalText,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 84.dp)
                .size(60.dp)
                .testTag("fab_new_note")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Note",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun TopSearchBar(
    onSearchClick: () -> Unit,
    isGridLayout: Boolean,
    onToggleLayout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, SlateBorder, RoundedCornerShape(24.dp))
            .clickable(onClick = onSearchClick)
            .testTag("home_search_bar"),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = CharcoalSubtle,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Search your notes & checklists…",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = CharcoalSubtle.copy(alpha = 0.8f),
                    fontSize = 14.5.sp
                ),
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onToggleLayout,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("toggle_layout_button")
            ) {
                Icon(
                    imageVector = if (isGridLayout) Icons.Default.ViewAgenda else Icons.Default.GridView,
                    contentDescription = if (isGridLayout) "Switch to List View" else "Switch to Grid View",
                    tint = CharcoalText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun FoldersFilterChipsRow(
    selectedFolderId: String,
    folders: List<com.example.data.Folder>,
    onSelectFolder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isAllSelected = selectedFolderId.equals("all", ignoreCase = true)
        FilterChip(
            selected = isAllSelected,
            onClick = { onSelectFolder("all") },
            label = { Text("All Notes", fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AmberAccent,
                selectedLabelColor = CharcoalText,
                containerColor = Color.White,
                labelColor = CharcoalSubtle
            ),
            border = FilterChipDefaults.filterChipBorder(
                borderColor = if (isAllSelected) AmberDark else SlateBorder,
                selectedBorderColor = AmberDark,
                enabled = true,
                selected = isAllSelected
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("filter_chip_all")
        )

        folders.forEach { folder ->
            val isSelected = selectedFolderId.equals(folder.id, ignoreCase = true)
            FilterChip(
                selected = isSelected,
                onClick = { onSelectFolder(folder.id) },
                label = { Text(folder.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AmberAccent,
                    selectedLabelColor = CharcoalText,
                    containerColor = Color.White,
                    labelColor = CharcoalSubtle
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (isSelected) AmberDark else SlateBorder,
                    selectedBorderColor = AmberDark,
                    enabled = true,
                    selected = isSelected
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("filter_chip_${folder.id}")
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = CharcoalSubtle.copy(alpha = 0.8f)
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "($count)",
            style = MaterialTheme.typography.labelSmall.copy(
                color = CharcoalSubtle.copy(alpha = 0.6f)
            )
        )
    }
}

@Composable
private fun EmptyNotesState(
    selectedFolder: String,
    onCreateNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(AmberAccent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = AmberDark,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (selectedFolder == "all") "No notes yet" else "No notes in this folder",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = CharcoalText
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Tap the yellow '+' button below to write your first note or checklist.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = CharcoalSubtle
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
