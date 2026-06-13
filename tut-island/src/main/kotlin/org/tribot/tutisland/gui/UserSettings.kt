package org.tribot.tutisland.gui

import org.tribot.tutisland.util.data.IronmanMode
import org.tribot.tutisland.util.data.Location

data class UserSettings(
    val ironmanMode: String = IronmanMode.STANDARD.name,
    val walkLocation: String = Location.NONE.name
)
