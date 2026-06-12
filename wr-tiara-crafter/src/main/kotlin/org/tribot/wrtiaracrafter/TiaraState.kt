package org.tribot.wrtiaracrafter

data class TiaraState(
    val isLoggedIn: Boolean,
    val isInsideAltar: Boolean = false,
    val inventoryTalismanCount: Int = 0,
    val inventoryTiaraCount: Int = 0,
    val isBankOpen: Boolean = false,
    val bankTalismanCount: Int = 0,
    val bankTiaraCount: Int = 0,
)
