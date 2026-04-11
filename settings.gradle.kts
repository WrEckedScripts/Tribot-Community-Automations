// Build entry point for tribot-community-scripts.
//
// The layout intentionally mirrors `tribot-automations` so a contributor who's worked
// in either repo feels immediately at home. Every subproject is independently buildable
// and deployable via `./gradlew :<module>:deployLocally`.

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://jitpack.io")
    }

    plugins {
        kotlin("jvm").version(extra["kotlin.version"] as String)
    }

    // JitPack doesn't publish the Gradle plugin marker that maps the `org.tribot.dev`
    // plugin ID to its module coordinates, so we redirect the plugin request to the
    // JitPack module directly. Gradle then resolves whatever version string subprojects
    // pass (including the `latest.release` dynamic selector) against JitPack's own
    // `maven-metadata.xml`.
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.tribot.dev") {
                useModule("com.github.TribotRS.tribot-dev-plugin:plugin:${requested.version}")
            }
        }
    }
}

rootProject.name = "tribot-community-scripts"

// Shared libraries — contributed utilities that other modules depend on via
// `bundled(project(":community-commons"))`.
include("community-commons")

// Starter templates — copy any of these as a starting point for a new contribution.
// Keep them small, well-commented, and guaranteed to compile against the current SDK.
include("example-automation-plugin")
include("cam-torum-miner")

// NOTE to contributors: add your own module here. See CONTRIBUTING.md for the full
// walkthrough, but the short version is:
//   1. Create a directory at the repo root: `my-awesome-script/`
//   2. Drop in a `build.gradle.kts` (copy from an example above)
//   3. Add `include("my-awesome-script")` below this comment
//   4. Open a PR
