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
        register("tutisland") {
            className = "org.tribot.tutisland.TutIsland"
            scriptName = "CrazyDavy Tutorial Island"
            version = "1.0.0"
            author = "CrazyDavy"
            description = "Runs Tutorial Island."
            category = "Tools"
        }
    }
}

dependencies {
    bundled(project(":community-commons"))
    implementation(compose.desktop.currentOs)
}
