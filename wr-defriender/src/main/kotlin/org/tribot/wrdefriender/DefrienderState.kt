package org.tribot.wrdefriender

object DefrienderState {
    @Volatile
    var task = "Starting"

    @Volatile
    var removed = 0
}
