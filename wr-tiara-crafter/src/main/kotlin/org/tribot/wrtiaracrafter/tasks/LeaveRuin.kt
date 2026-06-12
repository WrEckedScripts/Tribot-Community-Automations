package org.tribot.wrtiaracrafter.tasks

import nullablelib.NullableLib.ctx
import nullablelib.antiban.sleepHotReaction
import nullablelib.core.definition.Definitions
import nullablelib.core.input.click
import nullablelib.core.query.TileObjects
import org.tribot.script.sdk.Waiting as SdkWaiting

import org.tribot.wrtiaracrafter.contracts.TaskContract
import org.tribot.wrtiaracrafter.data.Altars

class LeaveRuin(private val altar: Altars) : TaskContract {
    override val name: String
        get() = "Leave ruin"

    override fun perform(): Boolean {
        val clicked = TileObjects.closestMatching {
            Definitions.getObject(it.id)?.actions?.contains("Use") ?: false
        }?.click("Use")

        sleepHotReaction()

        SdkWaiting.waitUntil {
            val playerLocation = ctx.client.localPlayer?.worldLocation
                ?: return@waitUntil false
            altar.exitLocation.distanceTo(playerLocation) > 10
        }

        return clicked != null
    }
}
