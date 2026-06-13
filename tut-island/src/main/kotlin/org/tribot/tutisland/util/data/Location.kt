package org.tribot.tutisland.util.data

import org.tribot.script.sdk.types.Area

enum class Location(val displayName: String, val walkingArea: Area?) {
    NONE("None",null),
    GRAND_EXCHANGE("Grand Exchange", Constants.grandExchange),
    VARROCK_BANK("Varrock Bank", Constants.varrockBank),
    LUMB_BANK("Lumbridge Bank", Constants.lumbBank);

    override fun toString(): String = displayName
}