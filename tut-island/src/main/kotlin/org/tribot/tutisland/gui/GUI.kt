package org.tribot.tutisland.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.tribot.script.sdk.Log
import org.tribot.tutisland.gui.components.ActionButtons
import org.tribot.tutisland.gui.components.DropdownField
import org.tribot.tutisland.gui.components.ProfileDropdownField
import org.tribot.tutisland.gui.components.ProfileNameField
import org.tribot.tutisland.util.data.IronmanMode
import org.tribot.tutisland.util.data.Location
import java.util.concurrent.CountDownLatch

object GUI {
    fun showComposeGuiAndWait(
        store: ProfileStore = ProfileStore(),
        saveAsProfileName: String = "lastRun"
    ): Pair<Settings, Boolean> {
        store.load(saveAsProfileName)?.let { Settings.fromSerializable(it) }

        var didStart = false
        val latch = CountDownLatch(1)

        application(exitProcessOnExit = false) {
            val windowState = rememberWindowState(
                position = WindowPosition.Aligned(Alignment.Center),
                size = DpSize(width = 780.dp, height = 400.dp)
            )

            Window(
                onCloseRequest = {
                    didStart = false
                    latch.countDown()
                    exitApplication()
                },
                title = "CrazyDavy Tutorial Island",
                state = windowState
            ) {
                TutIslandGuiContent(
                    store = store,
                    initialProfileName = saveAsProfileName,
                    onStart = {
                        store.save(saveAsProfileName, Settings.toSerializable())

                        didStart = true
                        latch.countDown()
                        exitApplication()
                    }
                )
            }
        }

        latch.await()
        return Settings to didStart
    }
}

@Composable
private fun TutIslandGuiContent(
    store: ProfileStore,
    initialProfileName: String,
    onStart: () -> Unit
) {
    var ironmanMode by remember { mutableStateOf(Settings.ironmanMode) }
    var endLocation by remember { mutableStateOf(Settings.walkLocation) }
    var selectedProfile by remember { mutableStateOf(initialProfileName) }
    var saveProfileName by remember { mutableStateOf(initialProfileName) }
    var profileMessage by remember { mutableStateOf("") }
    var pendingDeleteProfile by remember { mutableStateOf<String?>(null) }
    val profiles = remember { mutableStateListOf<String>().apply { addAll(store.listProfiles()) } }

    fun syncSettings() {
        Settings.ironmanMode = ironmanMode
        Settings.walkLocation = endLocation
    }

    fun refreshProfiles() {
        profiles.clear()
        profiles.addAll(store.listProfiles())
    }

    MaterialTheme(
        colors = darkColors(
            primary = Color(0xFF63D6B0),
            secondary = Color(0xFFE9C46A),
            surface = Color(0xFF18201D),
            background = Color(0xFF0D1411),
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onSurface = Color.White,
            onBackground = Color.White
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
                    .padding(22.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    backgroundColor = Color(0xFF18201D),
                    shape = RoundedCornerShape(8.dp),
                    elevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "CrazyDavy Tutorial Island",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DropdownField(
                                label = "Account Type",
                                value = ironmanMode,
                                options = IronmanMode.entries,
                                itemLabel = { it.displayName },
                                onSelected = { ironmanMode = it },
                                modifier = Modifier.weight(1f)
                            )

                            DropdownField(
                                label = "End Location",
                                value = endLocation,
                                options = Location.entries,
                                itemLabel = { it.displayName },
                                onSelected = { endLocation = it },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                ProfileDropdownField(
                                    label = "Load Profile",
                                    value = selectedProfile,
                                    options = (listOf(initialProfileName, "default") + profiles).distinct(),
                                    onSelected = { name ->
                                        val loaded = store.load(name)
                                        if (loaded != null) {
                                            Settings.fromSerializable(loaded)
                                            ironmanMode = Settings.ironmanMode
                                            endLocation = Settings.walkLocation
                                            profileMessage = "Loaded $name"
                                            Log.info("[TutIsland] Loaded profile \"$name\"")
                                        } else {
                                            profileMessage = if (name == "default") "Using default profile" else "Profile not found"
                                        }
                                        selectedProfile = name
                                        saveProfileName = name
                                    },
                                    onDeleteRequested = { name ->
                                        pendingDeleteProfile = name
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                            }

                            ProfileNameField(
                                value = saveProfileName,
                                onValueChange = { saveProfileName = it },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        ActionButtons(
                            onSave = {
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
                            },
                            onStart = {
                                syncSettings()
                                onStart()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(profileMessage.ifBlank { " " }, color = Color(0xFFB7C8BE), fontSize = 12.sp)
                    }
                }
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
                                if (selectedProfile == name) {
                                    selectedProfile = "default"
                                }
                                if (saveProfileName == name) {
                                    saveProfileName = "default"
                                }
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