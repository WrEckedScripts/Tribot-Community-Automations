package org.tribot.gemstoneminer.gui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun <T> DropdownField(
    label: String,
    value: T,
    options: List<T>,
    itemLabel: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var menuWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(label)

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .onGloballyPositioned { coordinates ->
                        menuWidth = with(density) { coordinates.size.width.toDp() }
                    }
            ) {
                Text(itemLabel(value), modifier = Modifier.weight(1f), color = Color.White, fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Text("▼", color = Color(0xFFB8C8CF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = if (menuWidth > 0.dp) Modifier.width(menuWidth) else Modifier
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            onSelected(option)
                        }
                    ) {
                        Text(itemLabel(option))
                    }
                }
            }
        }
    }
}