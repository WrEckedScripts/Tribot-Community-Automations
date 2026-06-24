package org.tribot.gemstoneminer.gui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ProfileSettingsSection(
    selectedProfile: String,
    profileOptions: List<String>,
    onProfileSelected: (String) -> Unit,
    onDeleteProfileRequested: (String) -> Unit,
    saveProfileName: String,
    onSaveProfileNameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        ProfileDropdownField(
            value = selectedProfile,
            options = profileOptions,
            onSelected = onProfileSelected,
            onDeleteRequested = onDeleteProfileRequested,
            modifier = Modifier.width(340.dp)
        )

        TextInputField(
            label = "Profile Name",
            value = saveProfileName,
            onValueChange = onSaveProfileNameChange,
            modifier = Modifier.width(340.dp)
        )
    }
}