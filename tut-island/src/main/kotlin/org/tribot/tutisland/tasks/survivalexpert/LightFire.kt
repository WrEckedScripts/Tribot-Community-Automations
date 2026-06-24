package org.tribot.tutisland.tasks.survivalexpert

import net.runelite.api.gameval.ItemID
import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.WorldTile
import org.tribot.script.sdk.walking.LocalWalking
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task
import kotlin.math.abs

class LightFire : Task {
    override val displayName = "Lighting Fire"
    override val priority = 0

    override fun canRun(): Boolean {
        return Inventory.getCount(ItemID.TINDERBOX) > 0 &&
                Inventory.getCount(ItemID.NEWBIELOGS) > 0 &&
                GameState.getSetting(281) == 80
    }

    override fun execute() {
        val myTile = MyPlayer.getTile()

        if (isFireOnTile(myTile)) {
            val safe = findTileWithoutFire(myTile)
            safe?.interact("Walk here")
            return
        }

        if (!MyPlayer.isAnimating()) {
            val tinderFirst = TutPreferences.orderAB("light_fire_item_order")
            val (src, dst) = if (tinderFirst) {
                ItemID.TINDERBOX to ItemID.NEWBIELOGS
            } else {
                ItemID.NEWBIELOGS to ItemID.TINDERBOX
            }

            val source = Query.inventory().idEquals(src).findFirst().orElse(null) ?: return
            val target = Query.inventory().idEquals(dst).findFirst().orElse(null) ?: return

            if (source.click("Use") && Waiting.waitUntil(TutPreferences.mediumDelayMs()) { GameState.isAnyItemSelected() }) {
                target.click()
                Waiting.waitUntil(TutPreferences.longDelayMs() * 3) {
                    GameState.getSetting(281) == 90
                }
            }
        }
    }

    private fun isFireOnTile(tile: WorldTile): Boolean =
        Query.gameObjects()
            .idEquals(26185)
            .tileEquals(tile)
            .isAny

    private fun findTileWithoutFire(center: WorldTile): WorldTile? {
        val radius = TutPreferences.choose("light_fire_safe_radius", 2, 3, 4)
        val tiles = mutableListOf<WorldTile>()

        for (dx in -radius..radius) {
            for (dy in -radius..radius) {
                if (dx == 0 && dy == 0) continue
                tiles += WorldTile(center.x + dx, center.y + dy, center.plane)
            }
        }

        tiles.sortBy { abs(it.x - center.x) + abs(it.y - center.y) }

        val walkingMap = LocalWalking.createMap()
        return tiles.firstOrNull { t ->
            !isFireOnTile(t) && walkingMap.canReach(t)
        }
    }
}