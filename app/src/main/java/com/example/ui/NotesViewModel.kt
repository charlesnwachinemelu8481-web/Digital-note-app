package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ChecklistItem
import com.example.data.Folder
import com.example.data.Note
import com.example.data.NoteColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    SEARCH,
    FOLDERS,
    TRASH,
    SETTINGS,
    EDITOR
}

enum class SaveStatus {
    SAVED,
    SAVING
}

enum class FontSize(val label: String, val scale: Float) {
    SMALL("Small", 0.88f),
    MEDIUM("Medium", 1.0f),
    LARGE("Large", 1.18f)
}

data class EditorState(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val colorHex: String = NoteColor.CREAM.bgHex,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val folderId: String = "All",
    val tags: String = "",
    val isChecklist: Boolean = false,
    val saveStatus: SaveStatus = SaveStatus.SAVED,
    val lastSavedTime: Long = System.currentTimeMillis()
)

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val noteDao = database.noteDao()

    // Navigation state
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _previousScreen = MutableStateFlow(Screen.HOME)

    // Layout preference: Staggered Masonry (true) vs List (false)
    private val _isGridLayout = MutableStateFlow(true)
    val isGridLayout: StateFlow<Boolean> = _isGridLayout.asStateFlow()

    // Active folder filter ("all" for all folders)
    private val _selectedFolderId = MutableStateFlow("all")
    val selectedFolderId: StateFlow<String> = _selectedFolderId.asStateFlow()

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _recentSearches = MutableStateFlow(
        listOf("Groceries", "Ideas", "Tahoe", "Pancakes", "Roadmap")
    )
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private val _searchFilterColorHex = MutableStateFlow<String?>(null)
    val searchFilterColorHex: StateFlow<String?> = _searchFilterColorHex.asStateFlow()

    // Settings state
    private val _defaultNoteColor = MutableStateFlow(NoteColor.CREAM)
    val defaultNoteColor: StateFlow<NoteColor> = _defaultNoteColor.asStateFlow()

    private val _fontSize = MutableStateFlow(FontSize.MEDIUM)
    val fontSize: StateFlow<FontSize> = _fontSize.asStateFlow()

    private val _isBackupSyncEnabled = MutableStateFlow(true)
    val isBackupSyncEnabled: StateFlow<Boolean> = _isBackupSyncEnabled.asStateFlow()

    private val _lastBackupTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastBackupTimestamp: StateFlow<Long> = _lastBackupTimestamp.asStateFlow()

    // Editor state
    private val _editorState = MutableStateFlow(EditorState())
    val editorState: StateFlow<EditorState> = _editorState.asStateFlow()

    private var autoSaveJob: Job? = null

    // Notes Flows
    val allActiveNotes = noteDao.getAllActiveNotes().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val trashNotes = noteDao.getTrashNotes().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val folders = noteDao.getAllFolders().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Filtered notes for Home screen (by selected folder)
    val homeNotes = combine(allActiveNotes, _selectedFolderId) { notes, folderId ->
        if (folderId.equals("all", ignoreCase = true)) {
            notes
        } else {
            notes.filter { it.folderId.equals(folderId, ignoreCase = true) }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Filtered search results
    val searchResults = combine(
        allActiveNotes,
        _searchQuery,
        _searchFilterColorHex
    ) { notes, query, colorHex ->
        if (query.isBlank() && colorHex == null) {
            emptyList()
        } else {
            notes.filter { note ->
                val matchesQuery = if (query.isBlank()) true else {
                    note.title.contains(query, ignoreCase = true) ||
                            note.content.contains(query, ignoreCase = true) ||
                            note.tags.contains(query, ignoreCase = true)
                }
                val matchesColor = if (colorHex == null) true else {
                    note.colorHex.equals(colorHex, ignoreCase = true)
                }
                matchesQuery && matchesColor
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Navigation actions
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            _previousScreen.value = _currentScreen.value
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        return if (_currentScreen.value == Screen.EDITOR) {
            // Save and return
            flushEditorSave()
            _currentScreen.value = _previousScreen.value
            true
        } else if (_currentScreen.value != Screen.HOME) {
            _currentScreen.value = Screen.HOME
            true
        } else {
            false
        }
    }

    fun toggleGridLayout() {
        _isGridLayout.update { !it }
    }

    fun selectFolder(folderId: String) {
        _selectedFolderId.value = folderId
    }

    // Search actions
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectSearchColor(colorHex: String?) {
        _searchFilterColorHex.update { if (it == colorHex) null else colorHex }
    }

    fun executeSearch(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            _recentSearches.update { current ->
                val updated = listOf(query) + current.filterNot { it.equals(query, ignoreCase = true) }
                updated.take(8)
            }
        }
    }

    fun removeRecentSearch(query: String) {
        _recentSearches.update { current -> current.filterNot { it.equals(query, ignoreCase = true) } }
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }

    // Settings actions
    fun setDefaultNoteColor(color: NoteColor) {
        _defaultNoteColor.value = color
    }

    fun setFontSize(size: FontSize) {
        _fontSize.value = size
    }

    fun toggleBackupSync() {
        _isBackupSyncEnabled.update { !it }
        if (_isBackupSyncEnabled.value) {
            _lastBackupTimestamp.value = System.currentTimeMillis()
        }
    }

    fun performManualSync() {
        _lastBackupTimestamp.value = System.currentTimeMillis()
    }

    // Note operations
    fun openNewNote(folderId: String? = null) {
        val targetFolder = folderId ?: if (_selectedFolderId.value != "all") _selectedFolderId.value else "All"
        _editorState.value = EditorState(
            id = 0,
            title = "",
            content = "",
            colorHex = _defaultNoteColor.value.bgHex,
            isPinned = false,
            isArchived = false,
            folderId = targetFolder,
            isChecklist = false,
            saveStatus = SaveStatus.SAVED
        )
        _previousScreen.value = _currentScreen.value
        _currentScreen.value = Screen.EDITOR
    }

    fun openNote(note: Note) {
        _editorState.value = EditorState(
            id = note.id,
            title = note.title,
            content = note.content,
            colorHex = note.colorHex,
            isPinned = note.isPinned,
            isArchived = note.isArchived,
            folderId = note.folderId,
            tags = note.tags,
            isChecklist = note.isChecklist,
            saveStatus = SaveStatus.SAVED,
            lastSavedTime = note.updatedAt
        )
        _previousScreen.value = _currentScreen.value
        _currentScreen.value = Screen.EDITOR
    }

    fun updateEditorTitle(title: String) {
        _editorState.update { it.copy(title = title, saveStatus = SaveStatus.SAVING) }
        scheduleAutoSave()
    }

    fun updateEditorContent(content: String) {
        _editorState.update { it.copy(content = content, saveStatus = SaveStatus.SAVING) }
        scheduleAutoSave()
    }

    fun updateEditorColor(colorHex: String) {
        _editorState.update { it.copy(colorHex = colorHex, saveStatus = SaveStatus.SAVING) }
        scheduleAutoSave()
    }

    fun toggleEditorPin() {
        _editorState.update { it.copy(isPinned = !it.isPinned, saveStatus = SaveStatus.SAVING) }
        scheduleAutoSave()
    }

    fun updateEditorFolder(folderId: String) {
        _editorState.update { it.copy(folderId = folderId, saveStatus = SaveStatus.SAVING) }
        scheduleAutoSave()
    }

    fun toggleEditorChecklistMode() {
        _editorState.update { state ->
            val wasChecklist = state.isChecklist
            val newContent = if (!wasChecklist) {
                // Convert text lines to checklist format
                val lines = state.content.lines()
                lines.joinToString("\n") { line ->
                    if (line.isBlank() || line.startsWith("[ ] ") || line.startsWith("[x] ")) line
                    else "[ ] $line"
                }
            } else {
                // Strip checklist markers
                val lines = state.content.lines()
                lines.joinToString("\n") { line ->
                    line.removePrefix("[ ] ").removePrefix("[x] ")
                }
            }
            state.copy(isChecklist = !wasChecklist, content = newContent, saveStatus = SaveStatus.SAVING)
        }
        scheduleAutoSave()
    }

    // Formatting Toolbar Helpers
    fun appendChecklistItem(text: String = "") {
        _editorState.update { state ->
            val separator = if (state.content.isEmpty() || state.content.endsWith("\n")) "" else "\n"
            val newContent = "${state.content}${separator}[ ] $text"
            state.copy(content = newContent, isChecklist = true, saveStatus = SaveStatus.SAVING)
        }
        scheduleAutoSave()
    }

    fun toggleChecklistItemAt(index: Int) {
        _editorState.update { state ->
            val lines = state.content.lines().toMutableList()
            var checklistItemIndex = 0
            for (i in lines.indices) {
                val line = lines[i]
                if (line.startsWith("[ ] ") || line.startsWith("[x] ")) {
                    if (checklistItemIndex == index) {
                        lines[i] = if (line.startsWith("[ ] ")) {
                            line.replaceFirst("[ ] ", "[x] ")
                        } else {
                            line.replaceFirst("[x] ", "[ ] ")
                        }
                        break
                    }
                    checklistItemIndex++
                }
            }
            state.copy(content = lines.joinToString("\n"), saveStatus = SaveStatus.SAVING)
        }
        scheduleAutoSave()
    }

    fun toggleChecklistItemDirect(noteId: Long, lineIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = noteDao.getNoteByIdDirect(noteId) ?: return@launch
            val lines = note.content.lines().toMutableList()
            var checklistIdx = 0
            for (i in lines.indices) {
                val line = lines[i]
                if (line.startsWith("[ ] ") || line.startsWith("[x] ")) {
                    if (checklistIdx == lineIndex) {
                        lines[i] = if (line.startsWith("[ ] ")) {
                            line.replaceFirst("[ ] ", "[x] ")
                        } else {
                            line.replaceFirst("[x] ", "[ ] ")
                        }
                        break
                    }
                    checklistIdx++
                }
            }
            val updated = note.copy(
                content = lines.joinToString("\n"),
                updatedAt = System.currentTimeMillis()
            )
            noteDao.updateNote(updated)
        }
    }

    fun insertFormatting(prefix: String, suffix: String) {
        _editorState.update { state ->
            val separator = if (state.content.isEmpty() || state.content.endsWith(" ") || state.content.endsWith("\n")) "" else " "
            val newContent = "${state.content}$separator$prefix$suffix"
            state.copy(content = newContent, saveStatus = SaveStatus.SAVING)
        }
        scheduleAutoSave()
    }

    fun insertBulletPoint() {
        _editorState.update { state ->
            val separator = if (state.content.isEmpty() || state.content.endsWith("\n")) "" else "\n"
            val newContent = "${state.content}${separator}• "
            state.copy(content = newContent, saveStatus = SaveStatus.SAVING)
        }
        scheduleAutoSave()
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch(Dispatchers.IO) {
            delay(400) // Debounce auto-save
            saveCurrentNote()
        }
    }

    private fun flushEditorSave() {
        autoSaveJob?.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            saveCurrentNote()
        }
    }

    private suspend fun saveCurrentNote() {
        val state = _editorState.value
        // Don't save empty notes that haven't been created yet
        if (state.id == 0L && state.title.isBlank() && state.content.isBlank()) {
            return
        }

        val note = Note(
            id = state.id,
            title = state.title.ifBlank { "Untitled Note" },
            content = state.content,
            colorHex = state.colorHex,
            isPinned = state.isPinned,
            isArchived = state.isArchived,
            isDeleted = false,
            folderId = state.folderId,
            tags = state.tags,
            isChecklist = state.isChecklist,
            updatedAt = System.currentTimeMillis()
        )

        if (state.id == 0L) {
            val newId = noteDao.insertNote(note)
            _editorState.update { it.copy(id = newId, saveStatus = SaveStatus.SAVED, lastSavedTime = System.currentTimeMillis()) }
        } else {
            noteDao.updateNote(note)
            _editorState.update { it.copy(saveStatus = SaveStatus.SAVED, lastSavedTime = System.currentTimeMillis()) }
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.togglePin(note.id, !note.isPinned)
        }
    }

    fun setNoteColor(note: Note, colorHex: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.updateNoteColor(note.id, colorHex)
        }
    }

    fun deleteEditorNote() {
        val id = _editorState.value.id
        if (id != 0L) {
            viewModelScope.launch(Dispatchers.IO) {
                noteDao.moveToTrash(id)
            }
        }
        _currentScreen.value = _previousScreen.value
    }

    fun moveNoteToTrash(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.moveToTrash(note.id)
        }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.restoreFromTrash(note.id)
        }
    }

    fun deletePermanently(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.deletePermanently(note.id)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.emptyTrash()
        }
    }

    // Folders
    fun createFolder(name: String, colorHex: String = "#FBBF24") {
        if (name.isBlank()) return
        val id = name.trim().lowercase().replace("\\s+".toRegex(), "_")
        val folder = Folder(
            id = id,
            name = name.trim(),
            colorHex = colorHex,
            iconName = "folder",
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.insertFolder(folder)
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.resetFolderNotes(folder.id)
            noteDao.deleteFolder(folder.id)
        }
    }

    fun resetSampleData() {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.populateInitialData(noteDao)
        }
    }
}
