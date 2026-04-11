plugins {
    kotlin("jvm")
    id("org.tribot.dev") version "latest.release"
}

tribot {
    useCompose = false
    useJavaFx = false

    scripts {
        register("camtorumminer") {
            className = "org.tribot.camtorumminer.CamTorumMiner"
            scriptName = "Cam Torum Miner"
            version = "1.0.0"
            author = "Nullable"
            description = "Mines blessed bone shards and banks"
            category = "mining"
        }
    }
}

dependencies {
    bundled(project(":community-commons"))
}
