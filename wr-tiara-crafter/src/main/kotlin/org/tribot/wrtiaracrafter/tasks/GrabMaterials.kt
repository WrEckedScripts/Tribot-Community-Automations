package org.tribot.wrtiaracrafter.tasks

import nullablelib.NullableLib
import nullablelib.antiban.sleepColdReaction
import nullablelib.antiban.sleepHotReaction
import nullablelib.core.definition.Definitions
import nullablelib.core.tabs.Inventory
import nullablelib.core.widgets.Banking
import org.tribot.community.commons.BankingHelpers
import org.tribot.wrtiaracrafter.contracts.TaskContract
import org.tribot.wrtiaracrafter.data.Altars
import org.tribot.wrtiaracrafter.data.Banks

class GrabMaterials(val altar: Altars) : TaskContract {
    override val name: String
        get() = "Banking at Falador West bank"

    override fun execute(): Boolean {
        updateActiveTask()

        val playerLocation = NullableLib.ctx.client.localPlayer?.worldLocation ?: return false

        // Ensure we're near Falador west bank.
        if (Banks.FALADOR_WEST.location.distanceTo(playerLocation) > 10) {
            MoveToLocation(
                NullableLib.ctx,
                Banks.FALADOR_WEST.location,
                arrivalRadius = 10,
            ).execute()
            return false
        }

        if (!Banking.isOpen()) {
            BankingHelpers.ensureBankOpen()
            return false
        }

        BankingHelpers.depositAllExcept(
            Definitions.item(altar.tiaraId)?.name ?: "",
            Definitions.item(altar.talismanId)?.name ?: "",
        )

        val talismanCount = Banking.getCount(altar.talismanId)
        val tiaraCount = Banking.getCount(altar.tiaraId)
        if (talismanCount == 0 || tiaraCount == 0) {
            return false
        }

        if (Inventory.getCount(altar.talismanId) < 14) {
            Banking.withdraw(
                altar.talismanId,
                talismanCount.coerceAtMost(14),
            )
        }

        sleepColdReaction()

        if (Inventory.getCount(altar.tiaraId) < 14) {
            Banking.withdraw(
                altar.tiaraId,
                tiaraCount.coerceAtMost(14),
            )
        }

        sleepHotReaction()

        val hasCraftablePair =
            Inventory.getCount(altar.talismanId) > 0 &&
                    Inventory.getCount(altar.tiaraId) > 0

        if (hasCraftablePair) {
            NullableLib.ctx.logger.info("Grabbed materials")
            Banking.close()
        }

        return hasCraftablePair
    }
}
