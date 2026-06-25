# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

Build the app:
```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

Run unit tests (JVM, no device needed):
```bash
./gradlew test
./gradlew :app:testDebugUnitTest --tests "com.kam666.mealplanner.ExampleUnitTest"
```

Run instrumented tests (requires connected device or emulator):
```bash
./gradlew connectedAndroidTest
./gradlew :app:connectedDebugAndroidTest --tests "com.kam666.mealplanner.ExampleInstrumentedTest"
```

Install on connected device:
```bash
./gradlew installDebug
```

Clean build:
```bash
./gradlew clean
```

## Architecture & Structure

Single-module Android app (`:app`). Package: `com.kam666.mealplanner`.

- **Min SDK 24**, target/compile SDK 36 (Android 16 QPR1, using the `release(36) { minorApiLevel = 1 }` DSL)
- **AGP 9.2.1**, Java 11 source/target compatibility
- No Kotlin plugin configured yet — the project depends on `core-ktx` but has no Kotlin source files; add the Kotlin Android plugin before writing `.kt` source files
- No Activity declared in `AndroidManifest.xml` yet — the app has no entry point
- Release optimization is explicitly disabled (`enable = false`)

Dependencies are version-catalogued in `gradle/libs.versions.toml`: AppCompat, Material Design 1.10, Core KTX, JUnit 4, Espresso, and AndroidX JUnit.