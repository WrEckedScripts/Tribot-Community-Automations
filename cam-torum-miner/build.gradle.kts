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
    // nullable-lib wraps the automation-sdk with Kotlin-idiomatic helpers. `bundled`
    // packages it into the script jar so it's available at runtime inside Tribot.
    bundled("com.github.Nullable-TB:nullable-lib:latest.release")
}
