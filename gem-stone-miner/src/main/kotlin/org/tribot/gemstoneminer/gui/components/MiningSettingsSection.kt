package org.tribot.gemstoneminer.gui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tribot.gemstoneminer.gui.StopConditionType
import org.tribot.gemstoneminer.util.data.MineLevel

@Composable
internal fun MiningSettingsSection(
    mineLevel: MineLevel,
    onMineLevelChange: (MineLevel) -> Unit,
    stopConditionType: StopConditionType,
    onStopConditionTypeChange: (StopConditionType) -> Unit,
    stopConditionValue: String,
    onStopConditionValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        DropdownField(
            label = "Mine Level",
            value = mineLevel,
            options = MineLevel.entries,
            itemLabel = { it.displayName },
            onSelected = onMineLevelChange,
            modifier = Modifier.width(340.dp)
        )
        Text(
            "Lower mine requires Karamja Medium Diary.",
            color = Color(0xFF93A3AA),
            fontSize = 12.sp
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            DropdownField(
                label = "Stop Condition",
                value = stopConditionType,
                options = StopConditionType.entries,
                itemLabel = { it.displayName },
                onSelected = onStopConditionTypeChange,
                modifier = Modifier.width(180.dp)
            )

            NumberField(
                label = stopConditionType.valueLabel,
                value = stopConditionValue,
                onValueChange = onStopConditionValueChange,
                enabled = stopConditionType != StopConditionType.NONE,
                modifier = Modifier.width(180.dp)
            )
        }
    }
}

private val StopConditionType.valueLabel: String
    get() = when (this) {
        StopConditionType.NONE -> "Target"
        StopConditionType.MINING_LEVEL -> "Target Level"
        StopConditionType.GEM_ROCKS -> "Target Gem Rocks"
    }