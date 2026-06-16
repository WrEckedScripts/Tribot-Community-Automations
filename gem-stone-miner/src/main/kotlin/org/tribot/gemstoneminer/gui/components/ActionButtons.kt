package org.tribot.gemstoneminer.gui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ActionButtons(
    statusMessage: String,
    onSave: () -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            statusMessage.ifBlank { " " },
            color = Color(0xFFB8C8CF),
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )

        OutlinedButton(
            onClick = onSave,
            modifier = Modifier
                .width(148.dp)
                .height(40.dp)
        ) {
            Text("Save Profile")
        }

        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.Black
            ),
            modifier = Modifier
                .width(120.dp)
                .height(40.dp)
        ) {
            Text("Start", fontWeight = FontWeight.Bold)
        }
    }
}