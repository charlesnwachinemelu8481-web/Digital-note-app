package com.example.data

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Pastel note card colors matching Apple Notes / Google Keep warm pastel aesthetic.
 */
enum class NoteColor(
    val id: String,
    val displayName: String,
    val bgHex: String,
    val borderHex: String,
    val color: Color,
    val borderColor: Color
) {
    CREAM("cream", "Butter Cream", "#FEF9C3", "#FDE047", Color(0xFFFEF9C3), Color(0xFFFACC15)),
    MINT("mint", "Fresh Mint", "#D1FAE5", "#6EE7B7", Color(0xFFD1FAE5), Color(0xFF34D399)),
    LAVENDER("lavender", "Soft Lavender", "#EDE9FE", "#C4B5FD", Color(0xFFEDE9FE), Color(0xFFA78BFA)),
    PEACH("peach", "Warm Peach", "#FFEDD5", "#FDBA74", Color(0xFFFFEDD5), Color(0xFFFB923C)),
    SKY("sky", "Sky Blue", "#E0F2FE", "#7DD3FC", Color(0xFFE0F2FE), Color(0xFF38BDF8)),
    ROSE("rose", "Pastel Rose", "#FCE7F3", "#F472B6", Color(0xFFFCE7F3), Color(0xFFF472B6)),
    LEMON("lemon", "Lemon Sherbet", "#FEF08A", "#EAB308", Color(0xFFFEF08A), Color(0xFFEAB308)),
    WHITE("white", "Warm White", "#FFFFFF", "#E2E8F0", Color(0xFFFFFFFF), Color(0xFFCBD5E1));

    companion object {
        fun fromHex(hex: String?): NoteColor {
            if (hex == null) return CREAM
            return entries.firstOrNull { it.bgHex.equals(hex, ignoreCase = true) } ?: CREAM
        }

        fun fromId(id: String?): NoteColor {
            if (id == null) return CREAM
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: CREAM
        }
    }
}

/**
 * Checklist item model parsed from note content or checklist JSON.
 */
data class ChecklistItem(
    val id: String,
    val text: String,
    val isChecked: Boolean = false
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val colorHex: String = NoteColor.CREAM.bgHex,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val folderId: String = "All",
    val tags: String = "",
    val isChecklist: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey
    val id: String,
    val name: String,
    val colorHex: String = "#FBBF24",
    val iconName: String = "folder",
    val createdAt: Long = System.currentTimeMillis()
)
