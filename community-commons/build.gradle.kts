plugins {
    kotlin("jvm")
    id("org.tribot.dev") version "latest.release"
}

// community-commons is a shared utility library. It applies the dev plugin so that
// helpers here can import from `org.tribot.automation.*`, `org.tribot.script.sdk.*`,
// and `net.runelite.api.*`, but declares no scripts or plugins — the dev plugin
// short-circuits the fatJar/deployLocally registration for library-style consumers.
//
// Consumers pull this in via `bundled(project(":community-commons"))`. That flows into
// `implementation` transitively (so classes are on the compile classpath) AND packs
// the classes into the consumer's fatJar at deploy time.
tribot {
    useScriptSdk = true
    useLegacyApi = true
    useCompose = false
    useJavaFx = false
}
