package org.tribot.gemstoneminer.gui

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.tribot.gemstoneminer.gui.screens.GemStoneMinerScreen
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
                size = DpSize(width = 980.dp, height = 675.dp)
            )

            Window(
                onCloseRequest = {
                    didStart = false
                    latch.countDown()
                    exitApplication()
                },
                title = "CrazyDavy's Gem Stone Miner",
                state = windowState
            ) {
                GemStoneMinerScreen(
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