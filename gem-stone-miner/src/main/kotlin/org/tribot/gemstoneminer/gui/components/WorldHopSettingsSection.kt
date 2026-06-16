package org.tribot.gemstoneminer.gui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun WorldHopSettingsSection(
    worldHopMinutes: String,
    onWorldHopMinutesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        NumberField(
            label = "Hop Every (min, 0 off)",
            value = worldHopMinutes,
            onValueChange = onWorldHopMinutesChange,
            enabled = true,
            modifier = Modifier.width(220.dp)
        )
    }
}