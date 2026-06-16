package org.tribot.gemstoneminer.gui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    TextInputField(
        label = label,
        value = value,
        onValueChange = { next -> onValueChange(next.filter { it.isDigit() }.take(4)) },
        enabled = enabled,
        modifier = modifier
    )
}