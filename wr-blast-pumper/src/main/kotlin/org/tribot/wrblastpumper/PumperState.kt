package org.tribot.wrblastpumper

data class PumperState(
    val isLoggedIn: Boolean,
    val world: Int = -1,
    val isPumpNearby: Boolean = false,
    val isPumping: Boolean = false,
    val isRefreshDue: Boolean = false,
    val isStoveRefillableNearby: Boolean = false,
    val isRefuelDue: Boolean = false,
)
