package org.tribot.gemstoneminer.gui.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
internal fun FieldLabel(label: String, enabled: Boolean = true) {
    Text(
        label,
        color = if (enabled) Color(0xFFB8C8CF) else Color(0xFF77848A),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )
}