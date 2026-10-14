package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.Note
import com.example.data.NoteColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies QuickNote app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("QuickNote", appName)
    }

    @Test
    fun `note color fromHex and defaults work correctly`() {
        val mint = NoteColor.fromHex("#D1FAE5")
        assertEquals(NoteColor.MINT, mint)

        val invalid = NoteColor.fromHex("#000000")
        assertEquals(NoteColor.CREAM, invalid)
    }

    @Test
    fun `note creation preserves properties`() {
        val note = Note(
            title = "Test Note",
            content = "[ ] Task 1\n[x] Task 2",
            colorHex = NoteColor.MINT.bgHex,
            isPinned = true,
            isChecklist = true
        )
        assertEquals("Test Note", note.title)
        assertTrue(note.isPinned)
        assertTrue(note.isChecklist)
        assertEquals(NoteColor.MINT.bgHex, note.colorHex)
    }
}
