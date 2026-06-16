package org.tribot.gemstoneminer.gui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun BreakSettingsSection(
    breakEveryMinutes: String,
    onBreakEveryMinutesChange: (String) -> Unit,
    breakLengthSeconds: String,
    onBreakLengthSecondsChange: (String) -> Unit,
    logoutDuringBreak: Boolean,
    onLogoutDuringBreakChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            NumberField(
                label = "Break Every (min, 0 off)",
                value = breakEveryMinutes,
                onValueChange = onBreakEveryMinutesChange,
                enabled = true,
                modifier = Modifier.width(180.dp)
            )
            NumberField(
                label = "Break Length (sec, 0 off)",
                value = breakLengthSeconds,
                onValueChange = onBreakLengthSecondsChange,
                enabled = true,
                modifier = Modifier.width(180.dp)
            )
        }

        CheckField(
            label = "Logout During Break",
            checked = logoutDuringBreak,
            onCheckedChange = onLogoutDuringBreakChange,
            enabled = true,
            modifier = Modifier.width(340.dp)
        )
    }
}