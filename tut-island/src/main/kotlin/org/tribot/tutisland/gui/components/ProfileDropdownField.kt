package org.tribot.tutisland.gui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    onDeleteRequested: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var menuWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = Color(0xFFB7C8BE), fontSize = 12.sp, fontWeight = FontWeight.Bold)

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        menuWidth = with(density) { coordinates.size.width.toDp() }
                    }
            ) {
                Text(value, modifier = Modifier.weight(1f), color = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("v", color = Color(0xFFB7C8BE))
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = if (menuWidth > 0.dp) Modifier.width(menuWidth) else Modifier
            ) {
                options.forEach { profile ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                onSelected(profile)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(profile)
                        }

                        if (profile != "default") {
                            TextButton(
                                onClick = {
                                    expanded = false
                                    onDeleteRequested(profile)
                                },
                                modifier = Modifier.width(44.dp)
                            ) {
                                Text("x", color = Color(0xFFE76F51), fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Spacer(Modifier.width(44.dp))
                        }
                    }
                }
            }
        }
    }
}