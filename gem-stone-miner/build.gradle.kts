plugins {
    kotlin("jvm")
    kotlin("plugin.compose") version "2.1.21"
    id("org.jetbrains.compose") version "1.8.0"
    id("org.tribot.dev") version "latest.release"
}

tribot {
    useScriptSdk = true
    useLegacyApi = true
    useCompose = true
    useJavaFx = false

    scripts {
        register("gemstoneminer") {
            className = "org.tribot.gemstoneminer.GemStoneMiner"
            scriptName = "CrazyDavy's Gem Stone Miner"
            version = "1.0.0"
            author = "CrazyDavy"
            description = "Mines Gem rocks at the selected upper or lower level."
            category = "Mining"
        }
    }
}

dependencies {
    bundled(project(":community-commons"))
    implementation(compose.desktop.currentOs)
}