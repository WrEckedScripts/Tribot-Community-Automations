package org.tribot.tutisland.util.taskmanagement

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Log
import org.tribot.script.sdk.Login
import org.tribot.script.sdk.MyPlayer
import org.tribot.tutisland.gui.Settings
import org.tribot.tutisland.util.data.Location

class TaskScheduler(
    private val tasks: List<Task>,
    private val onStopRequested: (() -> Unit)? = null
) {
    var currentTask: Task? = null
        private set

    var stopRequested: Boolean = false
        private set

    private var stopFired = false

    fun tick() {
        if (GameState.getSetting(281) >= 1000 &&
            Login.isLoggedIn() &&
            (Settings.walkLocation == Location.NONE || Settings.walkLocation.walkingArea?.contains(MyPlayer.getTile()) == true)
            ) {
            Log.info("[TaskManager] STOP: Tutorial Island Complete.")
            stopRequested = true
            currentTask = null
            if (!stopFired) {
                stopFired = true
                onStopRequested?.invoke()
            }
            return
        }

        val next = tasks
            .asSequence()
            .filter { it.canRun() }
            .maxByOrNull { it.priority }

        currentTask = next
        next?.execute()
    }
}