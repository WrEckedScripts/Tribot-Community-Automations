package org.tribot.camtorumminer

import net.runelite.api.coords.WorldPoint
import org.tribot.automation.TribotScript
import org.tribot.automation.script.ScriptContext
import org.tribot.automation.script.core.CameraMethod
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Log
import org.tribot.script.sdk.MakeScreen
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.input.Mouse
import org.tribot.script.sdk.query.Query

lateinit var ctx: ScriptContext

class CamTorumMiner : TribotScript {
    // Calcified deposit
    // Smith Anvil
    // Mine Calcified rocks (has decorative object on it if water stream)

    enum class Activity {
        MineCalcified,
        BreakDeposits,
        DepositCalcified,
        Unknown
    }

    var lastAnimTick = -1

    override fun execute(context: ScriptContext) {
        ctx = context

        ctx.events.onGameTick { 
            val player = ctx.client.localPlayer ?: return@onGameTick
            if (player.animation != -1) {
                lastAnimTick = ctx.client.tickCount
            }
        }

        while (true) {
            val activity = determineActivity()
            when (activity) {
                Activity.MineCalcified -> mineCalcified()
                Activity.DepositCalcified -> bank()
                Activity.BreakDeposits -> breakDeposits()
                Activity.Unknown -> {
                    Log.debug("Unknown activity")
                    ctx.waiting.sleep(uniform(500, 1500).toLong())
                }
            }

            ctx.waiting.sleep(uniform(500, 1500).toLong())
        }
    }

    fun determineActivity() : Activity {
        val playerLocation = ctx.client.localPlayer?.getWorldLocationSafe() ?: return Activity.Unknown
        val atMine = playerLocation.distanceTo(depositTileWest) < 10 || playerLocation.distanceTo(depositTileEast) < 10

        val hasCalcifiedDeposit = ctx.inventory.contains(calcifiedDepositId)
        val shouldBreakRocks = (!atMine && hasCalcifiedDeposit)
                || (ctx.inventory.isFull() && hasCalcifiedDeposit)

        if (shouldBreakRocks) {
            return Activity.BreakDeposits
        }

        val shouldBank = !atMine && ctx.inventory.getItems().count() > 2
        if (shouldBank) {
            return Activity.DepositCalcified
        }

        return Activity.MineCalcified
    }

    fun mineCalcified() {
        var localPlayer = ctx.client.localPlayer ?: return

        val playerLocation = localPlayer.getWorldLocationSafe() ?: return
        val atMine = playerLocation.distanceTo(depositTileWest) < 10 || playerLocation.distanceTo(depositTileEast) < 10

        if (!atMine) {
            Log.info("Walking to deposit tile")
            ctx.addonLibraries.dentistWalker.walkTo(depositTileWest)
        }

        if (ctx.client.tickCount - lastAnimTick <= 3) {
            // Probably mining
            return
        }

        val rock = ctx.worldViews.getTopLevelObjects()
            .filter { it.name() == "Calcified rocks" }
            .minByOrNull { it.getWorldLocationSafe()?.distanceTo(playerLocation) ?: Int.MAX_VALUE } ?: return

        if (!rock.isVisible()) {
            rock.getWorldLocationSafe()?.let {
                ctx.camera.turnTo(it, CameraMethod.MOUSE)
            }
        }

        if (ctx.interaction.click(rock, "Mine")) {
            ctx.waiting.sleep(randomSD(600, 250).toLong())
            Mouse.leaveScreen()
        }
    }

    fun bank() {
        val playerLocation = ctx.client.localPlayer?.getWorldLocationSafe() ?: return
        val atBank = playerLocation.distanceTo(bankTile) < 10

        if (!atBank) {
            ctx.addonLibraries.dentistWalker.walkTo(bankTile)
        }

        if (!ctx.banking.isOpen()) {
            openBank()
            ctx.waiting.sleep(uniform(300, 1500).toLong())
        }

        val itemsToDeposit = ctx.inventory.getItems()
            .filter { !pickaxeIds.contains(it.id) && it.id != hammerId }
            .toMutableSet()

        for (item in itemsToDeposit) {
            ctx.banking.depositAll(item.id)
            ctx.waiting.sleep(uniform(1, 25).toLong())
        }

        ctx.banking.close()
    }

    fun breakDeposits() {
        val playerLocation = ctx.client.localPlayer?.getWorldLocationSafe() ?: return
        val atAnvil = playerLocation.distanceTo(anvilTile) < 10

        if (!atAnvil) {
            ctx.addonLibraries.dentistWalker.walkTo(fuzzTile(anvilTile))
        }

        val localPlayer = ctx.client.localPlayer ?: return
        if (localPlayer.animation == -1) {
            val dep = Inventory.getAll().firstOrNull { it.id == calcifiedDepositId } ?: return
            val anvil = Query.gameObjects().maxDistance(10.0).nameEquals("Anvil").findBestInteractable().orElse(null) ?: return
            if (!dep.useOn(anvil)) {
                return
            }
            Waiting.waitUntil(2000) { MakeScreen.isOpen() }
            ctx.waiting.sleep(uniform(10, 400).toLong())
        }

        if (!MakeScreen.isOpen()) {
            return
        }

        if (!MakeScreen.makeAll(calcifiedDepositId)) {
            return
        }

        Waiting.waitUntil(4000) { localPlayer.animation == -1 }
        while (ctx.client.tickCount - lastAnimTick <= 4 && ctx.inventory.contains(calcifiedDepositId)) {
            ctx.waiting.sleep(100)
        }
    }

}