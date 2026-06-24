package org.tribot.tutisland.util.data

import org.tribot.script.sdk.types.Area
import org.tribot.script.sdk.types.WorldTile

object Constants {
    const val IRONMAN = 22
    const val ULTIMATE = 23
    const val HCIM = 24
    const val GROUP = 25
    const val HCGROUP = 26

    val gielinorGuideArea = Area.fromRadius(WorldTile(3094, 3107, 0), 1)
    val survivalGuideArea = Area.fromRadius(WorldTile(3102, 3096, 0), 1)
    val masterChefArea = Area.fromRadius(WorldTile(3079, 3084, 0), 2)
    val masterChefAreaInside = Area.fromRadius(WorldTile(3076, 3084, 0), 1)
    val questGuideArea = Area.fromRadius(WorldTile(3086, 3127, 0), 2)
    val questGuideAreaInside = Area.fromRadius(WorldTile(3086, 3122, 0), 1)
    val miningInstructorArea = Area.fromRadius(WorldTile(3079, 9504, 0), 1)
    val combatInstructorArea = Area.fromRadius(WorldTile(3108, 9510, 0), 2)
    val combatInstructorLadderArea = Area.fromRadius(WorldTile(3111, 9525, 0), 2)
    val bankArea = Area.fromRadius(WorldTile(3094, 3122, 0), 1)
    val accountGuideArea = Area.fromRadius(WorldTile(3126, 3123, 0), 1)
    val brotherBraceArea = Area.fromRadius(WorldTile(3126, 3107, 0), 1)
    val ironmanTutorArea = Area.fromRadius(WorldTile(3130, 3084, 0), 1)
    val magicInstructorArea = Area.fromRadius(WorldTile(3141, 3088, 0), 1)
    val magicInstructorChickenArea = Area.fromRadius(WorldTile(3140, 3090, 0), 1)

    val grandExchange = Area.fromRectangle(WorldTile(3156, 3486, 0), WorldTile(3173, 3481, 0))
    val varrockBank = Area.fromRectangle(WorldTile(3180, 3446, 0), WorldTile(3184, 3434, 0))
    val lumbBank = Area.fromRectangle(WorldTile(3208, 3220, 2), WorldTile(3210, 3218, 2))
}