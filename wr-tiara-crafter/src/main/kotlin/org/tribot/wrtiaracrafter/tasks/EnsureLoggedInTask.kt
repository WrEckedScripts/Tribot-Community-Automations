package org.tribot.wrtiaracrafter.tasks

import org.tribot.automation.script.ScriptContext
import org.tribot.wrtiaracrafter.contracts.TaskContract
import org.tribot.script.sdk.Waiting as SdkWaiting

/**
 * Ensures the player is logged in;
 * A task like this is mostly useful, within a long task-loop, to ensure over the long-run, that our player is always logged in.
 */
class EnsureLoggedInTask(private val ctx: ScriptContext) : TaskContract {
    override val name: String
        get() = "Ensure logged in"

    override fun perform(): Boolean {
        if (ctx.login.isLoggedIn() && ctx.client.localPlayer != null) return true

        return SdkWaiting.waitUntil(15_000) {
            ctx.login.login() &&
                ctx.login.isLoggedIn() &&
                ctx.client.localPlayer != null
        }
    }
}
