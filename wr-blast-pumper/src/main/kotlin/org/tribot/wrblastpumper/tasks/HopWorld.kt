package org.tribot.wrblastpumper.tasks

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
        if (context.client.world == targetWorld) return true
        if (context.client.world !in acceptedWorlds) return true

        TaskLabelTracker.label = "Hopping world to: $targetWorld"
        sleepHotReaction()
        context.client.hopToWorld(context.client.worldList.first { it.id == resolveTargetWorld() })

        return SdkWaiting.waitUntil(timeout, stepTimeout) {
            context.client.world in acceptedWorlds && context.client.world == targetWorld
        }
    }

    private fun resolveTargetWorld(): Int {
        return targetWorld ?: acceptedWorlds.random()
    }
}