package org.tribot.tutisland.util.data

enum class IronmanMode(val displayName: String) {
    STANDARD("Standard"),
    IRON("Ironman"),
    HCIM("Hardcore Ironman"),
    UIM("Ultimate Ironman"),
    GROUP_IRONMAN("Group Ironman"),
    GROUP_HCIM("Hardcore Group Ironman");

    override fun toString(): String = displayName
}
