package org.tribot.wrdefriender.tasks

import org.tribot.automation.script.ScriptContext
import org.tribot.wrdefriender.contracts.TaskContract
import org.tribot.script.sdk.Waiting as SdkWaiting

class LogoutTask(private val ctx: ScriptContext) : TaskContract {
    override val name = "Logging out"

    override fun perform(): Boolean {
        if (!ctx.login.isLoggedIn()) return true
        if (!ctx.logout.logout()) return false

        return SdkWaiting.waitUntil(7_500) {
            !ctx.login.isLoggedIn()
        }
    }
}
