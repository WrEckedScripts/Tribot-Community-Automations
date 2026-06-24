package org.tribot.tutisland.util

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Log
import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.util.TribotRandom

class MicroBreaks {
    private var nextBreakAtMs = scheduleFrom(System.currentTimeMillis())
    private var breakUntilMs = 0L
    private var active = false

    val isBreaking: Boolean
        get() = active

    fun tick(): Boolean {
        val now = System.currentTimeMillis()

        if (active) {
            if (isUnsafeForBreak()) {
                active = false
                breakUntilMs = 0L
                nextBreakAtMs = scheduleFrom(now)
                Log.info("[TutIsland] Micro break ended early; combat state detected.")
                return false
            }

            if (now >= breakUntilMs) {
                active = false
                breakUntilMs = 0L
                nextBreakAtMs = scheduleFrom(now)
                Log.info("[TutIsland] Micro break finished.")
                return false
            }

            val waitMs = minOf((breakUntilMs - now).toInt(), TribotRandom.uniform(300, 700))
            Waiting.wait(waitMs)
            return true
        }

        if (now < nextBreakAtMs) {
            return false
        }

        if (isUnsafeForBreak()) {
            nextBreakAtMs = scheduleFrom(now, POSTPONE_MIN_MS, POSTPONE_MAX_MS)
            return false
        }

        val durationMs = nextBreakDurationMs()
        active = true
        breakUntilMs = now + durationMs
        Log.info("[TutIsland] Starting micro break for ${durationMs}ms.")
        return true
    }

    private fun nextBreakDurationMs(): Int {
        val useLongBreak = TribotRandom.uniform(0, 100) < LONG_BREAK_CHANCE_PERCENT
        return if (useLongBreak) {
            TribotRandom.uniform(LONG_BREAK_MIN_MS, LONG_BREAK_MAX_MS)
        } else {
            TribotRandom.uniform(SHORT_BREAK_MIN_MS, SHORT_BREAK_MAX_MS)
        }
    }

    private fun isUnsafeForBreak(): Boolean {
        val step = GameState.getSetting(281)
        if (step in 440..460 || step in 480..490 || step == 650) {
            return true
        }

        if (MyPlayer.isHealthBarVisible()) {
            return true
        }

        val player = MyPlayer.get().orElse(null) ?: return true
        if (player.interactingCharacter.orElse(null) != null) {
            return true
        }

        return Query.npcs()
            .filter { npc -> npc.interactingCharacter.map { it == player }.orElse(false) }
            .isAny
    }

    private fun scheduleFrom(
        fromMs: Long,
        minDelayMs: Int = INTERVAL_MIN_MS,
        maxDelayMs: Int = INTERVAL_MAX_MS
    ): Long =
        fromMs + TribotRandom.uniform(minDelayMs, maxDelayMs)

    private companion object {
        const val INTERVAL_MIN_MS = 60000
        const val INTERVAL_MAX_MS = 180000
        const val POSTPONE_MIN_MS = 15000
        const val POSTPONE_MAX_MS = 45000
        const val SHORT_BREAK_MIN_MS = 1500
        const val SHORT_BREAK_MAX_MS = 8000
        const val LONG_BREAK_MIN_MS = 30000
        const val LONG_BREAK_MAX_MS = 40000
        const val LONG_BREAK_CHANCE_PERCENT = 20
    }
}