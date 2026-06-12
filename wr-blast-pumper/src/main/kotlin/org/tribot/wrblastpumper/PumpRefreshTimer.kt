package org.tribot.wrblastpumper

import org.tribot.script.sdk.util.TribotRandom
import java.time.Duration

private const val MIN_REFRESH_MINUTES = 15
private const val MAX_REFRESH_MINUTES = 29

class PumpRefreshTimer internal constructor(
    private val currentTime: () -> Long = System::currentTimeMillis,
    private val delayMinutes: () -> Int = {
        TribotRandom.uniform(MIN_REFRESH_MINUTES, MAX_REFRESH_MINUTES + 1)
    },
) {
    @Volatile
    private var nextRefreshAt: Long? = null

    fun scheduleIfMissing() {
        if (nextRefreshAt == null) {
            scheduleNext()
        }
    }

    fun scheduleNext() {
        nextRefreshAt = currentTime() + Duration.ofMinutes(delayMinutes().toLong()).toMillis()
    }

    fun isDue(): Boolean =
        nextRefreshAt?.let { currentTime() >= it } ?: false

    fun remainingTime(): Duration? =
        nextRefreshAt?.let {
            Duration.ofMillis((it - currentTime()).coerceAtLeast(0))
        }
}
