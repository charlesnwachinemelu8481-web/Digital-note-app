package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CharcoalText

@Composable
fun FormattingToolbar(
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onBulletClick: () -> Unit,
    onChecklistClick: () -> Unit,
    onHeadingClick: () -> Unit,
    onToggleColorPicker: () -> Unit,
    isChecklistActive: Boolean,
    isColorPickerOpen: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF1F5F9),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBoldClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("toolbar_bold")
            ) {
                Icon(
                    imageVector = Icons.Default.FormatBold,
                    contentDescription = "Bold",
                    tint = CharcoalText
                )
            }

            IconButton(
                onClick = onItalicClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("toolbar_italic")
            ) {
                Icon(
                    imageVector = Icons.Default.FormatItalic,
                    contentDescription = "Italic",
                    tint = CharcoalText
                )
            }

            IconButton(
                onClick = onHeadingClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("toolbar_heading")
            ) {
                Icon(
                    imageVector = Icons.Default.Title,
                    contentDescription = "Heading",
                    tint = CharcoalText
                )
            }

            IconButton(
                onClick = onBulletClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("toolbar_bullet")
            ) {
                Icon(
                    imageVector = Icons.Default.FormatListBulleted,
                    contentDescription = "Bullet List",
                    tint = CharcoalText
                )
            }

            IconButton(
                onClick = onChecklistClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (isChecklistActive) AmberAccent else Color.Transparent
                ),
                modifier = Modifier
                    .size(44.dp)
                    .testTag("toolbar_checklist")
            ) {
                Icon(
                    imageVector = Icons.Default.CheckBox,
                    contentDescription = "Checklist Toggle",
                    tint = CharcoalText
                )
            }

            IconButton(
                onClick = onToggleColorPicker,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (isColorPickerOpen) AmberAccent else Color.Transparent
                ),
                modifier = Modifier
                    .size(44.dp)
                    .testTag("toolbar_color_palette")
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Color Picker",
                    tint = CharcoalText
                )
            }
        }
    }
}
