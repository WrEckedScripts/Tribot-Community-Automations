package org.tribot.playermover.tasks

import org.tribot.automation.script.ScriptContext
import org.tribot.playermover.contracts.TaskContract
import org.tribot.script.sdk.Waiting as SdkWaiting

/**
 * Ensures the player is logged in;
 * A task like this is mostly useful, within a long task-loop, to ensure over the long-run, that our player is always logged in.
 */
class EnsureLoggedInTask(private val ctx: ScriptContext) : TaskContract {
    override val name: String
        get() = "Ensure logged in"

    override fun execute(): Boolean {
        if (ctx.login.isLoggedIn()) return true

        return SdkWaiting.waitUntil(15_000) {
            ctx.login.login() && ctx.login.isLoggedIn()
        }
    }
}