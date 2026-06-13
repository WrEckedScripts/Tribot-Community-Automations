package org.tribot.tutisland.gui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ActionButtons(
    onSave: () -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.width(360.dp)) {
            OutlinedButton(
                onClick = onSave,
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Profile")
            }

            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = Color.Black
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Start", fontWeight = FontWeight.Bold)
            }
        }
    }
}