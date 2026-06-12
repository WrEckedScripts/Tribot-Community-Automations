package org.tribot.wrblastpumper

import nullablelib.NullableLib
import org.tribot.script.sdk.util.TribotRandom
import java.time.Duration

private const val MIN_REFUEL_DELAY_SECONDS = 20
private const val MAX_REFUEL_DELAY_SECONDS = 45

class StoveRefuelTimer internal constructor(
    private val currentTime: () -> Long = System::currentTimeMillis,
    private val delaySeconds: () -> Int = {
        TribotRandom.uniform(MIN_REFUEL_DELAY_SECONDS, MAX_REFUEL_DELAY_SECONDS + 1)
    },
) {
    @Volatile
    private var refuelAt: Long? = null

    fun observe(stoveIsLow: Boolean, stoveIsFull: Boolean) {
        if (stoveIsFull) {
            clear()
        } else if (refuelAt == null) {
            if (stoveIsLow) {
                NullableLib.ctx.logger.error("Refueling stove in ${delaySeconds()} seconds")
                refuelAt = currentTime() + Duration.ofSeconds(delaySeconds().toLong()).toMillis()
            }
        }
    }

    fun isDue(): Boolean {
        return refuelAt?.let { currentTime() >= it } ?: false
    }

    fun getDueLabel(): String {
        if (refuelAt == null) return "Operational"
        return "Refuel in ${
            Duration.ofMillis(
                refuelAt?.let { (it - currentTime()).coerceAtLeast(0) } ?: 0)
                .toSeconds()
        } seconds"
    }


    fun clear() {
        refuelAt = null
    }
}
