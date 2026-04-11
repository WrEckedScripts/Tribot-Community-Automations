package org.tribot.camtorumminer

import net.runelite.api.GameObject
import net.runelite.api.Perspective
import net.runelite.api.Player
import net.runelite.api.TileObject
import net.runelite.api.coords.WorldPoint
import org.tribot.automation.script.core.CameraMethod
import java.awt.Point
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random


fun fuzzTile(tile: WorldPoint, radius: Int = 3): WorldPoint {
    val x = uniform(tile.x - radius, tile.x + radius)
    val y = uniform(tile.y - radius, tile.y + radius)
    return WorldPoint(x, y, tile.plane)
}


fun TileObject.name() = ctx.definitions.getObject(this.id)?.name ?: ""

fun Player.getWorldLocationSafe() = clientThread { this.worldLocation }
fun TileObject.getWorldLocationSafe() = clientThread { this.worldLocation }

fun <T>clientThread(block: () -> T): T? {
    var result: T? = null
    ctx.clientThread.executeBlocking {
        result = block()
    }
    return result
}

fun TileObject.isVisible(): Boolean = clientThread {
    val pt = this.localLocation
    val point = Perspective.localToCanvas(ctx.client, pt, this.worldLocation.plane) ?: return@clientThread false
    ctx.screen.isPointInViewport(Point(point.x, point.y))
} ?: false

fun waitUntil(maxTicks: Int = 10, condition: () -> Boolean): Boolean {
    var ticks = 0
    var conditionMet = false
    val listener = ctx.events.onGameTick {
        ticks++
        if (ticks >= maxTicks) {
            return@onGameTick
        }
        if (condition()) {
            conditionMet = true
            return@onGameTick
        }
    }

    try {
        while (!conditionMet && ticks < maxTicks) {
            ctx.waiting.sleep(10)
        }
        return conditionMet
    } finally {
        listener.remove()
    }
}

fun openBank(): Boolean {
    val player = ctx.client.localPlayer ?: return false
    val playerLocation = player.getWorldLocationSafe() ?: return false
    val bankObj = ctx.worldViews.getTopLevelObjects()
        .filter {
            val def = ctx.definitions.getObject(it.id) ?: return@filter false
            def.actions.contains("Bank")
        }
        .minByOrNull { it.getWorldLocationSafe()?.distanceTo(playerLocation) ?: Int.MAX_VALUE } ?: return false

    if (!bankObj.isVisible()) {
        val loc = bankObj.getWorldLocationSafe() ?: return false
        ctx.camera.turnTo(loc, CameraMethod.MOUSE)
        ctx.waiting.sleep(uniform(0, 200).toLong())
    }

    ctx.interaction.click(bankObj, "Bank")
    return waitUntil { ctx.banking.isOpen() }
}


// Random util

private fun rng() = ThreadLocalRandom.current()

fun randomSD(mean: Int, sd: Int): Int {
    return (mean + (rng().nextGaussian() * sd)).toInt()
}

fun randomSD(mean: Double, sd: Double): Double {
    return (mean + (rng().nextGaussian() * sd))
}

fun randomSD(min: Int, max: Int, mean: Int, sd: Int): Int {
    var random = randomSD(mean, sd)
    while (random < min || random > max) {
        random = randomSD(mean, sd)
    }
    return random
}

fun uniform(min: Int, max: Int): Int {
    return rng().nextInt(min, max)
}

fun uniform(min: Double, max: Double): Double {
    return rng().nextDouble() * (max - min) + min
}
