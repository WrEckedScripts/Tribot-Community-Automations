package org.tribot.gemstoneminer.util

import org.tribot.gemstoneminer.gui.Settings
import org.tribot.script.sdk.Log
import org.tribot.script.sdk.Login
import org.tribot.script.sdk.Waiting

class MicroBreaks {
    private var nextBreakAtMs = scheduleFrom(System.currentTimeMillis())
    private var breakUntilMs = 0L
    private var active = false

    val isBreaking: Boolean
        get() = active

    val statusText: String
        get() = if (Settings.logoutDuringBreak) "Micro break (logged out)" else "Micro break"

    fun tick(): Boolean {
        if (!Settings.microBreaksEnabled) {
            return false
        }

        val now = System.currentTimeMillis()

        if (active) {
            if (Settings.logoutDuringBreak && Login.isLoggedIn()) {
                Login.logout()
                Waiting.waitUntil(GemStoneMinerPreferences.longDelayMs() * 2) { !Login.isLoggedIn() }
            }

            if (now >= breakUntilMs) {
                active = false
                breakUntilMs = 0L
                nextBreakAtMs = scheduleFrom(now)
                if (!Login.isLoggedIn()) {
                    Login.login()
                    Waiting.waitUntil(GemStoneMinerPreferences.longDelayMs() * 3) { Login.isLoggedIn() }
                }
                Log.info("[GemStoneMiner] Micro break finished.")
                return false
            }

            Waiting.wait(minOf((breakUntilMs - now).toInt(), GemStoneMinerPreferences.mediumDelayMs()))
            return true
        }

        if (now < nextBreakAtMs) {
            return false
        }

        val durationMs = GemStoneMinerPreferences.randomizeMicroBreakLength(
            Settings.breakLengthSeconds.coerceAtLeast(5) * 1000L
        )
        active = true
        breakUntilMs = now + durationMs
        Log.info("[GemStoneMiner] Starting micro break for ${durationMs}ms.")
        return true
    }

    fun timeUntilNextBreakText(): String {
        if (!Settings.microBreaksEnabled) {
            return ""
        }
        if (active) {
            return "Now (${formatDuration((breakUntilMs - System.currentTimeMillis()).coerceAtLeast(0))})"
        }
        return formatDuration((nextBreakAtMs - System.currentTimeMillis()).coerceAtLeast(0))
    }

    private fun scheduleFrom(fromMs: Long): Long {
        if (!Settings.microBreaksEnabled) {
            return Long.MAX_VALUE
        }

        val baseMs = Settings.breakEveryMinutes.coerceAtLeast(1) * 60_000L
        return fromMs + GemStoneMinerPreferences.randomizeMicroBreakInterval(baseMs)
    }

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