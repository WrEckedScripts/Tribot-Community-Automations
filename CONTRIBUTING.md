# Contributing to tribot-community-scripts

Thanks for your interest in contributing! This repo is open to pull requests from
anyone. Both community members and the Tribot team. The bar is:

1. **It builds.** `./gradlew build` from the repo root has to pass.
2. **It's useful.** Either a usable script, a reusable library component, or a
   meaningful improvement to an existing one.
3. **It's readable.** Comments explain *why* for anything non-obvious. No numbered
   comments that'll go stale.
4. **It plays nicely.** No obfuscation, no code that pretends
   to be something it isn't. Scripts that get flagged on ethical grounds will be
   removed.

---

## Setting up a dev environment

1. Install JDK 21. If you don't have it, Gradle's Foojay toolchain will pull one
   down automatically on the first build.
2. Install IntelliJ IDEA (Community Edition is fine) and open the repo as a Gradle
   project.
3. Wait for Gradle to sync — the first sync takes a couple of minutes because it
   downloads the Tribot SDK from JitPack.
4. Run the `build` task to make sure everything compiles on your machine before
   you start editing.

If IntelliJ complains about Kotlin version drift, check that `gradle.properties`
has `kotlin.version=2.1.21` and that your Kotlin plugin in IntelliJ is a compatible
version. The Tribot runtime is pinned to this specific Kotlin version — do not
bump it locally, even if IntelliJ suggests it.

---

## Adding a script

The fastest path:

```bash
# 1. Copy the SDK template into a new module
cp -R example-sdk-script my-cool-script

# 2. Rename the package to match your module
mv my-cool-script/src/main/kotlin/com/tribot/community/examples/sdk \
   my-cool-script/src/main/kotlin/com/tribot/community/mycoolscript

# 3. Edit:
#    - my-cool-script/build.gradle.kts           (rename register(), className, scriptName)
#    - my-cool-script/src/.../*.kt               (package statement + class name + @TribotScriptManifest)
#    - settings.gradle.kts                       (add include("my-cool-script") at the bottom)

# 4. Build it
./gradlew :my-cool-script:build
```

Then test it by running `./gradlew :my-cool-script:deployLocally` and launching
Tribot — your script should show up in the script list.

### Metadata

Fill in `author`, `description`, and `category` in `build.gradle.kts`. `author`
should be a name or handle users can recognize. Discord username is fine. The
`category` value is free-form but try to reuse an existing one (`Combat`,
`Skilling`, `Money Making`, `Quests`, `Utilities`, …) so similar scripts cluster
together in the UI.

---

## Adding a library

If you're building something shared across multiple modules, it probably belongs
in `community-commons` or as a new sibling library module.

Put it in `community-commons` when:
- It's a small, general-purpose helper (a few dozen lines).
- It doesn't pull in new 3rd-party dependencies.

Create a new library module when:
- It's a larger subsystem (banking state machine, pathfinding helper, etc).
- It pulls in 3rd-party dependencies that not every consumer needs.
- It has its own maintainers who want review authority over it.

New library modules follow the same shape as `community-commons` — copy that
directory, rename, and add an `include(...)` entry to `settings.gradle.kts`.

---

## Pull requests

PRs will get reviewed by a member of Tribot staff. Community scripts in this repository will be released periodically.
