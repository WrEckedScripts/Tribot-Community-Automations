package org.tribot.playermover

import net.runelite.api.coords.WorldPoint
import org.tribot.automation.TribotScript
import org.tribot.automation.script.ScriptContext
import org.tribot.community.commons.ScriptArgsHelper
import org.tribot.playermover.tasks.EnsureLoggedInTask
import org.tribot.playermover.tasks.MoveToLocation
import org.tribot.script.sdk.Waiting as SdkWaiting
import org.tribot.script.sdk.util.Retry


/**
 * A simple script that moves the player around the map, using DentistWalker.
 * Supported arguments:
 *  - location: "x,y,plane"
 *  - x: int
 *  - y: int
 *  - plane: int
 *  - logout: true|false (optional, defaults to true)
 *
 * An argument string should use pipes (|) to separate argument key:value pairs.
 */
class PlayerMover : TribotScript {

    private lateinit var ctx: ScriptContext

    override fun execute(context: ScriptContext) {
        setup(context)

        val location: WorldPoint = resolveTarget()
        val shouldLogout = resolveLogoutPreference()

        ctx.logger.info(
            "Script arguments: '${context.runtime.scriptArgs}', logout resolved to $shouldLogout"
        )

        val taskList = listOf(
            EnsureLoggedInTask(ctx),
            MoveToLocation(ctx, location)
        )

        taskList.forEach { task ->
            val completed = Retry.retry(5) {
                task.execute()
            }

            if (!completed) throw RuntimeException(
                "Failed to complete task ${task.name} after 5 attempts"
            )

            ctx.logger.info("Task ${task.name} succeeded")
        }

        ctx.logger.info("All tasks completed successfully")

        if (shouldLogout) {
            ctx.logout.logout()

            SdkWaiting.waitUntil(7_500) {
                !ctx.login.isLoggedIn()
            }
        }
    }

    private fun setup(context: ScriptContext) {
        ctx = context
        ScriptArgsHelper.load(ctx.runtime.scriptArgs)
    }

    /**
     * Script argument "logout" is optional. If not provided, defaults to true.
     */
    private fun resolveLogoutPreference(): Boolean {
        val value = ScriptArgsHelper.get("logout") ?: return true

        return requireNotNull(value.toBooleanStrictOrNull()) {
            "Invalid logout argument: '$value'. Expected true or false."
        }
    }

    /**
     * Script argument "location" is optional. If not provided, uses x, y, plane arguments instead.
     */
    private fun resolveTarget(): WorldPoint {
        ScriptArgsHelper.get("location")?.let { location ->
            val (x, y, plane) = location.split(",").map(String::toInt)
            ctx.logger.info("Resolved location: $location -> $x, $y, $plane")
            return WorldPoint(x, y, plane)
        }

        ctx.logger.info("No location argument provided, using x, y, plane arguments instead:")

        val point = WorldPoint(
            requireNotNull(ScriptArgsHelper.getInt("x")) { "Missing or invalid x argument" },
            requireNotNull(ScriptArgsHelper.getInt("y")) { "Missing or invalid y argument" },
            requireNotNull(ScriptArgsHelper.getInt("plane")) { "Missing or invalid plane argument" }
        )

        ctx.logger.info("Resolved location: $point")

        return point
    }
}
