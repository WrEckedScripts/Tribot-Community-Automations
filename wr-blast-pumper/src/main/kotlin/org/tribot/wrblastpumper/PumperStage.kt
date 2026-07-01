package org.tribot.wrblastpumper

enum class PumperStage {
    LOGIN,
    UNSUPPORTED_WORLD,
    FIND_PUMP,
    REFUEL_STOVE,
    OPERATE_PUMP,
    REFRESH_PUMP,
    PUMPING,
}
