package org.tribot.gemstoneminer.gui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tribot.gemstoneminer.gui.ProfileStore
import org.tribot.gemstoneminer.gui.Settings
import org.tribot.gemstoneminer.gui.StopConditionType
import org.tribot.gemstoneminer.gui.UserSettings
import org.tribot.gemstoneminer.gui.components.*
import org.tribot.script.sdk.Log

@Composable
internal fun GemStoneMinerScreen(
    store: ProfileStore,
    initialProfileName: String,
    onStart: () -> Unit
) {
    var mineLevel by remember { mutableStateOf(Settings.mineLevel) }
    var stopConditionType by remember { mutableStateOf(stopConditionTypeFromSettings()) }
    var stopConditionValue by remember { mutableStateOf(stopConditionValueFromSettings(stopConditionType)) }
    var breakEveryMinutes by remember { mutableStateOf(Settings.breakEveryMinutes.toString()) }
    var breakLengthSeconds by remember { mutableStateOf(Settings.breakLengthSeconds.toString()) }
    var logoutDuringBreak by remember { mutableStateOf(Settings.logoutDuringBreak) }
    var worldHopMinutes by remember { mutableStateOf(Settings.worldHopMinutes.toString()) }
    val initialProfiles = remember { store.listProfiles() }
    var selectedProfile by remember { mutableStateOf(initialProfileName) }
    var saveProfileName by remember { mutableStateOf(initialProfileName) }
    var profileMessage by remember { mutableStateOf("") }
    var pendingDeleteProfile by remember { mutableStateOf<String?>(null) }
    val profiles = remember { mutableStateListOf<String>().apply { addAll(initialProfiles) } }

    fun syncSettings() {
        Settings.mineLevel = mineLevel
        val stopValue = stopConditionValue.toIntOrNull() ?: 0
        Settings.stopAtMiningLevel = if (stopConditionType == StopConditionType.MINING_LEVEL) {
            stopValue.coerceIn(40, 100)
        } else {
            0
        }
        Settings.stopAfterGemRocks = if (stopConditionType == StopConditionType.GEM_ROCKS) {
            stopValue
        } else {
            0
        }
        Settings.breakEveryMinutes = breakEveryMinutes.toIntOrNull()?.coerceIn(0, 240) ?: 0
        Settings.breakLengthSeconds = breakLengthSeconds.toIntOrNull()?.coerceIn(0, 3600) ?: 0
        Settings.logoutDuringBreak = logoutDuringBreak
        Settings.worldHopMinutes = worldHopMinutes.toIntOrNull()?.coerceIn(0, 1440) ?: 0
    }

    fun applyLoaded(settings: UserSettings) {
        Settings.fromSerializable(settings)
        mineLevel = Settings.mineLevel
        stopConditionType = stopConditionTypeFromSettings()
        stopConditionValue = stopConditionValueFromSettings(stopConditionType)
        breakEveryMinutes = Settings.breakEveryMinutes.toString()
        breakLengthSeconds = Settings.breakLengthSeconds.toString()
        logoutDuringBreak = Settings.logoutDuringBreak
        worldHopMinutes = Settings.worldHopMinutes.toString()
    }

    fun refreshProfiles() {
        profiles.clear()
        profiles.addAll(store.listProfiles())
    }

    fun saveProfile() {
        syncSettings()
        val name = saveProfileName.trim().ifBlank { "default" }
        profileMessage = if (store.save(name, Settings.toSerializable())) {
            refreshProfiles()
            selectedProfile = name
            saveProfileName = name
            "Saved $name"
        } else {
            "Save failed"
        }
    }

    fun loadProfile(name: String) {
        val loaded = store.load(name)
        if (loaded != null) {
            applyLoaded(loaded)
            profileMessage = "Loaded $name"
            Log.info("[GemStoneMiner] Loaded profile \"$name\"")
        } else if (name == "default") {
            applyLoaded(UserSettings())
            profileMessage = "Using default profile"
        } else {
            profileMessage = "Profile not found"
        }
        selectedProfile = name
        saveProfileName = name
    }

    MaterialTheme(
        colors = darkColors(
            primary = Color(0xFF72C8D6),
            secondary = Color(0xFFD8BE63),
            surface = Color(0xFF172126),
            background = Color(0xFF0D1114),
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onSurface = Color.White,
            onBackground = Color.White
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
                    .padding(horizontal = 28.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "CrazyDavy Gem Stone Miner",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 2.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(28.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SettingsSection(
                                title = "Mining",
                                modifier = Modifier.weight(1f)
                            ) {
                                MiningSettingsSection(
                                    mineLevel = mineLevel,
                                    onMineLevelChange = { mineLevel = it },
                                    stopConditionType = stopConditionType,
                                    onStopConditionTypeChange = { selected ->
                                        stopConditionType = selected
                                        if (selected == StopConditionType.NONE) {
                                            stopConditionValue = "0"
                                        }
                                    },
                                    stopConditionValue = stopConditionValue,
                                    onStopConditionValueChange = { stopConditionValue = it },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            SettingsSection(
                                title = "Micro Breaks",
                                modifier = Modifier.weight(1f)
                            ) {
                                BreakSettingsSection(
                                    breakEveryMinutes = breakEveryMinutes,
                                    onBreakEveryMinutesChange = { breakEveryMinutes = it },
                                    breakLengthSeconds = breakLengthSeconds,
                                    onBreakLengthSecondsChange = { breakLengthSeconds = it },
                                    logoutDuringBreak = logoutDuringBreak,
                                    onLogoutDuringBreakChange = { logoutDuringBreak = it },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        SettingsDivider()

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(28.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SettingsSection(
                                title = "Profiles",
                                modifier = Modifier.weight(1f)
                            ) {
                                ProfileSettingsSection(
                                    selectedProfile = selectedProfile,
                                    profileOptions = (listOf(initialProfileName, "default") + profiles).distinct(),
                                    onProfileSelected = ::loadProfile,
                                    onDeleteProfileRequested = { pendingDeleteProfile = it },
                                    saveProfileName = saveProfileName,
                                    onSaveProfileNameChange = { saveProfileName = it },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            SettingsSection(
                                title = "World Hop",
                                modifier = Modifier.weight(1f)
                            ) {
                                WorldHopSettingsSection(
                                    worldHopMinutes = worldHopMinutes,
                                    onWorldHopMinutesChange = { worldHopMinutes = it },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Divider(color = Color(0xFF27343A))

                ActionButtons(
                    statusMessage = profileMessage,
                    onSave = ::saveProfile,
                    onStart = {
                        syncSettings()
                        onStart()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        pendingDeleteProfile?.let { name ->
            AlertDialog(
                onDismissRequest = { pendingDeleteProfile = null },
                title = { Text("Delete Profile") },
                text = { Text("Delete \"$name\"?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val deleted = store.delete(name)
                            if (deleted) {
                                refreshProfiles()
                                if (selectedProfile == name) selectedProfile = "default"
                                if (saveProfileName == name) saveProfileName = "default"
                                profileMessage = "Deleted $name"
                            } else {
                                profileMessage = "Delete failed"
                            }
                            pendingDeleteProfile = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDeleteProfile = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        Text(
            title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        content()
    }
}

@Composable
private fun SettingsDivider() {
    Divider(color = Color(0xFF243137))
}

private fun stopConditionTypeFromSettings(): StopConditionType =
    when {
        Settings.stopAtMiningLevel > 0 -> StopConditionType.MINING_LEVEL
        Settings.stopAfterGemRocks > 0 -> StopConditionType.GEM_ROCKS
        else -> StopConditionType.NONE
    }

private fun stopConditionValueFromSettings(type: StopConditionType): String =
    when (type) {
        StopConditionType.NONE -> "0"
        StopConditionType.MINING_LEVEL -> Settings.stopAtMiningLevel.toString()
        StopConditionType.GEM_ROCKS -> Settings.stopAfterGemRocks.toString()
    }