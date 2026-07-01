package org.tribot.wrblastpumper.tasks

import nullablelib.antiban.sleepClickReaction
import nullablelib.antiban.sleepHotReaction
import nullablelib.antiban.truncatedGaussian
import org.tribot.automation.script.ScriptContext
import org.tribot.wrscript.utilities.hud.TaskLabelTracker
import org.tribot.wrscript.utilities.tasks.TaskContract
import org.tribot.script.sdk.Waiting as SdkWaiting

class HopWorld(
    val context: ScriptContext,
    val acceptedWorlds: Set<Int>,
    val targetWorld: Int? = null,
    val timeout: Int = 30_000,
    val stepTimeout: Int = truncatedGaussian(mean = 350.0, stdDev = 120.0, min = 1_750.0, max = 3_000.0).toInt(),
) : TaskContract {
    override val name: String
        get() = "Hopping world"

    override fun perform(): Boolean {
        val resolvedTargetWorld = resolveTargetWorld()

        context.logger.debug("Hopping world to: $resolvedTargetWorld")
        if (context.client.world == resolvedTargetWorld) return true
        if (context.client.world in acceptedWorlds) return true

        TaskLabelTracker.label = "Hopping world to: $resolvedTargetWorld"
        sleepHotReaction()

        context.client.openWorldHopper()
        sleepClickReaction()

        try {
            val world = context.client.worldList.first { it.id == resolvedTargetWorld }
            context.client.hopToWorld(world)
        } catch (e: Exception) {
            // In case the worldList hasn't fully loaded yet (saw some must not be null errors when hopping)
            // - without calling the openWorldHopper before that is, so might be related.
            // - - Since we actually don't seem to open the UI anyway. and just hop via the RL plugin / builtin commands I'd say?
            context.logger.error("Failed to hop to world: $resolvedTargetWorld", e)
            sleepClickReaction()
            return false
        }

        return SdkWaiting.waitUntil(timeout, stepTimeout) {
            context.client.world == resolvedTargetWorld && context.login.isLoggedIn() && !context.login.isOnWelcomeScreen()
        }
    }

    private fun resolveTargetWorld(): Int {
        return targetWorld ?: acceptedWorlds.random()
    }
}
