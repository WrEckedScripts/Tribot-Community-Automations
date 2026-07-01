package org.tribot.wrscript.utilities.tasks

import net.runelite.api.gameval.VarPlayerID
import org.tribot.automation.script.ScriptContext
import org.tribot.script.sdk.Waiting

class EnsureLoggedInTask(private val context: ScriptContext, private val mustBeMember: Boolean = false) : TaskContract {
    override val name = "Ensure logged in"

    override fun perform(): Boolean {
        val playerIsInGame = context.login.isLoggedIn() && context.client.localPlayer != null

        if (mustBeMember && playerIsInGame) {
            val membershipDays = context.client.getServerVarpValue(
                VarPlayerID.ACCOUNT_CREDIT
            )

            if (membershipDays == 0) throw Exception(
                "Your account ran out of membership, please bond up and re-run."
            )
        }

        if (playerIsInGame) return true

        val isLoggedIn = Waiting.waitUntil(15_000) {
            context.login.login() &&
                    context.login.isLoggedIn() &&
                    context.client.localPlayer != null
        }

        if (!isLoggedIn) {
            return false
        }

        if (mustBeMember) {
            val membershipDays = context.client.getServerVarpValue(
                VarPlayerID.ACCOUNT_CREDIT
            )

            if (membershipDays == 0) {
                context.logout.logout()

                throw Exception(
                    "Your account ran out of membership, please bond up and re-run."
                )
            }
        }

        return isLoggedIn
    }
}
