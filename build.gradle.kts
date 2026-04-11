// Root build for tribot-community-scripts.
//
// Each subproject applies its own plugins in its own `build.gradle.kts`:
//   - Script/plugin subprojects apply `kotlin("jvm")` + `id("org.tribot.dev")` and use
//     the `tribot { ... }` DSL to declare script/plugin metadata. The dev plugin handles
//     the `fatJar`, `deployLocally`, and `generateManifest` tasks, and pulls in the
//     correct compile-only SDK deps automatically.
//   - Library subprojects (like `community-commons`) still apply `id("org.tribot.dev")`
//     so they get the SDK on their compile classpath, but declare no scripts/plugins —
//     the dev plugin short-circuits jar/deploy wiring and leaves them as pure libraries.
//   - Standalone JVM apps (like `example-launcher-tool`) apply `kotlin("jvm")` +
//     `application` directly and depend on `automation-sdk` as a regular `implementation`
//     dependency.
//
// The Kotlin plugin is declared here with `apply false` so subprojects can use the
// `plugins { kotlin("jvm") }` DSL without repeating the version (Gradle requires the
// plugin to be loaded by the root project to avoid the "Kotlin plugin loaded multiple
// times" warning).
//
// Run `./gradlew deployLocally` from this root to build every script/plugin jar in the
// repo and copy each one into your local Tribot automations directory (Gradle runs the
// task in every subproject that defines it).

plugins {
    java
    kotlin("jvm") apply false
    idea
}

allprojects {
    apply(plugin = "idea")
    apply(plugin = "kotlin")
    repositories {
        mavenCentral()
        maven("https://repo.runelite.net")
        maven("https://jitpack.io")
    }
    configurations {
        create("sourcesElements") {
            isCanBeResolved = true
            isCanBeConsumed = false
            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class, Category.DOCUMENTATION))
                attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType::class, DocsType.SOURCES))
            }
        }
    }

    idea {
        module {
            isDownloadJavadoc = true
            isDownloadSources = true
        }
    }
}
