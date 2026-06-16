package org.tribot.gemstoneminer.util

import org.tribot.gemstoneminer.gui.Settings
import org.tribot.gemstoneminer.util.data.MineLevel
import org.tribot.script.sdk.*
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.util.TribotRandom

class WorldHopManager(
    private val statusConsumer: (String) -> Unit = {}
) {
    private val upperMineCompetitionRadius = 12.0
    private var nextHopAtMs = scheduleFrom(System.currentTimeMillis())

    fun tick(): Boolean {
        if (!Login.isLoggedIn()) {
            return false
        }

        val now = System.currentTimeMillis()

        if (Bank.isOpen() || Bank.isDepositBoxOpen()) {
            return false
        }

        val hopReason = hopReason(now) ?: return false

        val target = Worlds.getRandom { world ->
            world.worldNumber != WorldHopper.getCurrentWorld() &&
                world.isMembers == WorldHopper.isInMembersWorld() &&
                world.isMainGame &&
                world.isRequirementsMet &&
                !world.isDangerous
        }.orElse(null)

        if (target == null) {
            nextHopAtMs = now + TribotRandom.uniform(60000, 120000)
            Log.warn("[GemStoneMiner] No suitable world-hop target found.")
            return false
        }

        statusConsumer("World hopping to ${target.worldNumber}")
        Log.info("[GemStoneMiner] $hopReason Hopping to world ${target.worldNumber}.")

        if (WorldHopper.hop(target.worldNumber)) {
            Waiting.waitUntil(GemStoneMinerPreferences.longDelayMs() * 5) {
                WorldHopper.getCurrentWorld() == target.worldNumber
            }
            Log.info("[GemStoneMiner] Hopped to world ${target.worldNumber}.")
        }

        nextHopAtMs = scheduleFrom(System.currentTimeMillis())
        Waiting.wait(GemStoneMinerPreferences.mediumDelayMs())
        return true
    }

    fun timeUntilNextHopText(): String {
        if (!Settings.worldHopEnabled) {
            return ""
        }
        return formatDuration((nextHopAtMs - System.currentTimeMillis()).coerceAtLeast(0))
    }

    private fun scheduleFrom(fromMs: Long): Long {
        if (!Settings.worldHopEnabled) {
            return Long.MAX_VALUE
        }

        val baseMs = Settings.worldHopMinutes.coerceAtLeast(5) * 60000L
        return fromMs + GemStoneMinerPreferences.randomizeWorldHopInterval(baseMs)
    }

    private fun hopReason(now: Long): String? {
        if (hasUpperMineCompetition()) {
            return "Another player is in the upper mine area."
        }

        if (!Settings.worldHopEnabled || now < nextHopAtMs) {
            return null
        }

        return "World hop timer elapsed."
    }

    private fun hasUpperMineCompetition(): Boolean =
        Settings.mineLevel == MineLevel.UPPER &&
            Region.getCurrentRegionID() == MineLevel.UPPER.regionId &&
            Query.players()
                .maxDistance(MineLevel.UPPER.walkingTile, upperMineCompetitionRadius)
                .isAny

    private fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return if (h > 0) {
            "%02d:%02d:%02d".format(h, m, s)
        } else {
            "%02d:%02d".format(m, s)
        }
    }
}