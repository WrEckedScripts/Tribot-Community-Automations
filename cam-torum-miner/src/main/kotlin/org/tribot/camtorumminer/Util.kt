package org.tribot.camtorumminer

import net.runelite.api.coords.WorldPoint
import nullablelib.NullableLib

/**
 * The current server tick. nullable-lib does not wrap `tickCount`, so we read it off the
 * thread-safe wrapped client directly. This is one of the few bare automation-sdk surfaces
 * left in the script.
 */
val tickCount: Int get() = NullableLib.ctx.client.tickCount

/**
 * Walks to [destination] using the DentistWalker addon. nullable-lib does not wrap the addon
 * walker, so this is the other remaining bare automation-sdk surface we touch.
 */
fun walkTo(destination: WorldPoint): Boolean =
    NullableLib.ctx.addonLibraries.dentistWalker.walkTo(destination)

/** Returns a random tile within [radius] tiles of [tile] (inclusive) on the same plane. */
fun fuzzTile(tile: WorldPoint, radius: Int = 3): WorldPoint {
    val x = (tile.x - radius..tile.x + radius).random()
    val y = (tile.y - radius..tile.y + radius).random()
    return WorldPoint(x, y, tile.plane)
}

/** Formats [ms] as an HH:MM:SS elapsed-time string for paint readouts. */
fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

/** Projects [count] accumulated over [elapsedMs] to a whole-number per-hour rate. */
fun perHour(count: Int, elapsedMs: Long): Int {
    if (elapsedMs <= 0) return 0
    return (count * 3_600_000.0 / elapsedMs).toInt()
}

/** Formats [n] with thousands separators for paint readouts (e.g. 12345 -> "12,345"). */
fun formatNumber(n: Int): String = "%,d".format(n)
