package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Note::class, Folder::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quicknote_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.noteDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(noteDao: NoteDao) {
            val now = System.currentTimeMillis()

            val initialFolders = listOf(
                Folder(id = "personal", name = "Personal", colorHex = "#38BDF8", iconName = "person", createdAt = now - 10000),
                Folder(id = "work", name = "Work & Projects", colorHex = "#FBBF24", iconName = "work", createdAt = now - 9000),
                Folder(id = "ideas", name = "Creative Ideas", colorHex = "#A78BFA", iconName = "lightbulb", createdAt = now - 8000),
                Folder(id = "groceries", name = "Groceries & Food", colorHex = "#34D399", iconName = "shopping_cart", createdAt = now - 7000)
            )
            noteDao.insertFolders(initialFolders)

            val sampleNotes = listOf(
                Note(
                    title = "Weekly Grocery & Farmers Market 🥦",
                    content = "[x] Organic Hass avocados (3)\n[x] Greek yogurt & organic honey\n[ ] Fresh sourdough loaf from bakery\n[ ] Baby spinach & wild arugula\n[ ] Oat milk & cold brew coffee\n[ ] Golden delicious apples",
                    colorHex = NoteColor.MINT.bgHex,
                    isPinned = true,
                    folderId = "groceries",
                    tags = "groceries, weekend, market",
                    isChecklist = true,
                    createdAt = now - 3600000 * 2,
                    updatedAt = now - 3600000 * 2
                ),
                Note(
                    title = "App Design Inspiration & Vibes ✨",
                    content = "Key thoughts for the next redesign:\n- Apple Notes clean tactile feel meets Google Keep pastel palette\n- Warm off-white background #FFFDF7 gives paper-like warmth\n- Bold amber highlights for buttons and pinned tags\n- Interactive checklist row toggles without opening editor",
                    colorHex = NoteColor.CREAM.bgHex,
                    isPinned = true,
                    folderId = "ideas",
                    tags = "design, compose, ui",
                    isChecklist = false,
                    createdAt = now - 3600000 * 5,
                    updatedAt = now - 3600000 * 3
                ),
                Note(
                    title = "Weekend Trip to Lake Tahoe 🌲",
                    content = "Cabin booking confirmation: #LK-99420.\nCheck-in Friday at 3:00 PM.\n\nPacking checklist:\n[x] Hiking boots and wool socks\n[x] Waterproof jacket\n[ ] Portable Bluetooth speaker\n[ ] Trail snacks & dried fruit\n[ ] Camera & extra batteries",
                    colorHex = NoteColor.SKY.bgHex,
                    isPinned = false,
                    folderId = "personal",
                    tags = "travel, weekend, tahoe",
                    isChecklist = true,
                    createdAt = now - 3600000 * 24,
                    updatedAt = now - 3600000 * 12
                ),
                Note(
                    title = "Q4 Product Strategy Roadmap 🚀",
                    content = "1. Ship Jetpack Compose rewrite with smooth animations\n2. Improve offline performance with Room local storage\n3. Introduce rich markdown checklists and color tagging\n4. Add export/backup JSON functionality for user data privacy",
                    colorHex = NoteColor.LAVENDER.bgHex,
                    isPinned = false,
                    folderId = "work",
                    tags = "work, strategy, roadmap",
                    isChecklist = false,
                    createdAt = now - 3600000 * 30,
                    updatedAt = now - 3600000 * 20
                ),
                Note(
                    title = "Grandma's Lemon Ricotta Pancakes 🥞",
                    content = "Ingredients:\n- 1 cup fresh whole milk ricotta\n- 2 eggs (separated)\n- Zest & juice of 1 Meyer lemon\n- 1 cup unbleached all-purpose flour\n- 1 tsp baking powder\n\nWhisk egg whites to soft peaks for extra fluffiness! Cook on low-medium griddle with golden butter.",
                    colorHex = NoteColor.PEACH.bgHex,
                    isPinned = false,
                    folderId = "personal",
                    tags = "recipe, breakfast, baking",
                    isChecklist = false,
                    createdAt = now - 3600000 * 48,
                    updatedAt = now - 3600000 * 40
                ),
                Note(
                    title = "Books to Read This Autumn 📚",
                    content = "[x] Tomorrow, and Tomorrow, and Tomorrow\n[x] Chip War by Chris Miller\n[ ] Four Thousand Weeks by Oliver Burkeman\n[ ] Build: An Unorthodox Guide by Tony Fadell\n[ ] Project Hail Mary by Andy Weir",
                    colorHex = NoteColor.ROSE.bgHex,
                    isPinned = false,
                    folderId = "ideas",
                    tags = "reading, books, wishlist",
                    isChecklist = true,
                    createdAt = now - 3600000 * 72,
                    updatedAt = now - 3600000 * 60
                )
            )
            noteDao.insertNotes(sampleNotes)
        }
    }
}
