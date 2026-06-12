package org.tribot.wrtiaracrafter

import org.tribot.automation.script.ScriptContext
import org.tribot.wrtiaracrafter.data.Altars

private const val ALTAR_INTERIOR_RADIUS = 15

class TiaraStageResolver(
    private val context: ScriptContext,
    private val altar: Altars,
) {
    fun resolve(): TiaraStage = resolve(readState())

    private fun readState(): TiaraState {
        if (!context.login.isLoggedIn()) {
            return TiaraState(isLoggedIn = false)
        }

        val player = context.client.localPlayer
            ?: return TiaraState(isLoggedIn = false)
        val isBankOpen = context.banking.isOpen()

        return TiaraState(
            isLoggedIn = true,
            isInsideAltar = altar.exitLocation.distanceTo(player.worldLocation) <= ALTAR_INTERIOR_RADIUS,
            inventoryTalismanCount = context.inventory.getCount(altar.talismanId),
            inventoryTiaraCount = context.inventory.getCount(altar.tiaraId),
            isBankOpen = isBankOpen,
            bankTalismanCount = if (isBankOpen) context.banking.getCount(altar.talismanId) else 0,
            bankTiaraCount = if (isBankOpen) context.banking.getCount(altar.tiaraId) else 0,
        )
    }

    companion object {
        internal fun resolve(state: TiaraState): TiaraStage {
            if (!state.isLoggedIn) {
                return TiaraStage.LOGIN
            }

            val hasCraftablePair =
                state.inventoryTalismanCount > 0 && state.inventoryTiaraCount > 0

            if (state.isInsideAltar) {
                return if (hasCraftablePair) {
                    TiaraStage.CRAFT_TIARAS
                } else {
                    TiaraStage.EXIT_ALTAR
                }
            }

            if (hasCraftablePair) {
                return TiaraStage.TRAVEL_TO_ALTAR
            }

            if (
                state.isBankOpen &&
                (state.bankTalismanCount == 0 || state.bankTiaraCount == 0)
            ) {
                return TiaraStage.OUT_OF_MATERIALS
            }

            return TiaraStage.BANK
        }
    }
}
