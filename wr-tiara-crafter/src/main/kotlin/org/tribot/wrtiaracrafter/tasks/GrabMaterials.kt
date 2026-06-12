package org.tribot.wrtiaracrafter.tasks

import nullablelib.NullableLib.ctx
import nullablelib.antiban.sleepColdReaction
import nullablelib.antiban.sleepHotReaction
import nullablelib.core.definition.Definitions
import nullablelib.core.tabs.Inventory
import nullablelib.core.widgets.Banking
import org.tribot.community.commons.BankingHelpers
import org.tribot.script.sdk.util.TribotRandom
import org.tribot.wrtiaracrafter.antiban.BreaksHelper
import org.tribot.wrtiaracrafter.contracts.TaskContract
import org.tribot.wrtiaracrafter.data.Altars
import org.tribot.wrtiaracrafter.data.Banks

class GrabMaterials(val altar: Altars) : TaskContract {
    override val name: String
        get() = "Banking at Falador West bank"

    override fun perform(): Boolean {
        val playerLocation = ctx.client.localPlayer?.worldLocation ?: return false

        // Ensure we're near Falador west bank.
        if (Banks.FALADOR_WEST.location.distanceTo(playerLocation) > 10) {
            MoveToLocation(
                ctx,
                Banks.FALADOR_WEST.location,
                arrivalRadius = 10,
            ).execute()
            return false
        }

        if (!Banking.isOpen()) {
            BankingHelpers.ensureBankOpen()
            return false
        }

        BreaksHelper.afkBreak(
            probabilityRange = 0.02..0.07,
            sleepTime = TribotRandom.uniform(7_500, 20_000)
                .minus(TribotRandom.uniform(0, 1000))
                .toLong(),
            alwaysLeaveScreen = true,
        )

        BankingHelpers.depositAllExcept(
            Definitions.item(altar.tiaraId)?.name ?: "",
            Definitions.item(altar.talismanId)?.name ?: "",
        )

        val talismanCount = Banking.getCount(altar.talismanId)
        val tiaraCount = Banking.getCount(altar.tiaraId)
        if (talismanCount == 0 || tiaraCount == 0) {
            return false
        }

        when (TribotRandom.uniform(0, 8) <= 3) {
            true -> {
                withdrawIfMissing(altar.talismanId)
                sleepColdReaction()
                withdrawIfMissing(altar.tiaraId)
            }

            else -> {
                withdrawIfMissing(altar.tiaraId)
                sleepColdReaction()
                withdrawIfMissing(altar.talismanId)
            }
        }

        sleepHotReaction()

        val hasCraftablePair =
            Inventory.getCount(altar.talismanId) > 0 &&
                    Inventory.getCount(altar.tiaraId) > 0

        if (hasCraftablePair) {
            ctx.logger.info("Grabbed materials")
            Banking.close()
        }

        return hasCraftablePair
    }

    private fun withdrawIfMissing(itemId: Int) {
        val count = Banking.getCount(itemId)
        if (Inventory.getCount(itemId) < 14) {
            Banking.withdraw(
                itemId,
                count.coerceAtMost(14),
            )
        }
    }
}
