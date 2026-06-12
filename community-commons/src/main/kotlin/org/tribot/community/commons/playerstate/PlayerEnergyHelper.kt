package org.tribot.community.commons.playerstate

import net.runelite.api.gameval.InterfaceID
import net.runelite.api.gameval.VarPlayerID
import net.runelite.api.widgets.Widget
import org.tribot.automation.script.ScriptContext
import org.tribot.script.sdk.util.TribotRandom

/**
 * Configurable helper class for interacting with the player's energy.
 * - Exposes two methods, enable() and disable()
 * - Can be configured to enable logging
 * - Sleeps and thresholds can be randomly or explicitly configured.
 */
object PlayerEnergyHelper {
    private lateinit var ctx: ScriptContext
    private var energyThreshold: Int = 0
    private var sleep: Boolean = true
    private var sleepTime: Long = TribotRandom.uniform(650, 1_150).toLong()

    private var enableLogging: Boolean = false

    fun configure(
        context: ScriptContext,
        loggingEnabled: Boolean = false,
        threshold: Int = TribotRandom.uniform(44, 63),
        sleepBeforeValidation: Boolean = true,
    ) {
        ctx = context
        enableLogging = loggingEnabled
        energyThreshold = threshold
        sleep = sleepBeforeValidation
    }

    fun enable(): Boolean {
        if (isEnabled()) return true

        val currentEnergy = ctx.client.energy / 100

        if (currentEnergy >= energyThreshold && !isEnabled()) {
            val toggle = getToggleWidget() ?: return false

            if (!ctx.interaction.click(toggle)) {
                trace { "Failed to click run button" }
                return false
            }

            if (sleep) {
                trace { "Sleeping for $sleepTime ms" }
                ctx.waiting.sleep(sleepTime)
            }

            trace { "Current energy: $currentEnergy, Threshold: $energyThreshold, Enabled: ${isEnabled()}" }
        }

        return isEnabled()
    }

    fun disable(): Boolean {
        if (!isEnabled()) return false

        val toggle = getToggleWidget() ?: return false

        if (!ctx.interaction.click(toggle)) {
            return false
        }

        return isEnabled()
    }

    private fun getToggleWidget(): Widget? {
        return ctx.client.getWidget(InterfaceID.Orbs.RUNBUTTON)
    }

    private fun isEnabled(): Boolean = ctx.client.getVarpValue(VarPlayerID.OPTION_RUN) == 1

    private inline fun trace(message: () -> String) {
        if (enableLogging) {
            ctx.logger.trace(message())
        }
    }
}