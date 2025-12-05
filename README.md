# Calory

Calory is a Kotlin Multiplatform app for tracking what you eat and watching your daily macros. One codebase ships Android, desktop, and iOS builds while sharing the domain, data, and most UI.

## What it does
- Track consumed foods with weight, calories, proteins, fats, carbs, and day.
- See daily lists and totals in a Compose-based UI.
- Persist entries locally via SQLDelight; keeps domain logic shared across targets.

## Tech stack
- Kotlin Multiplatform with shared domain/data.
- Compose Multiplatform UI (Android, Desktop; iOS host via Swift).
- SQLDelight for schema-first database and typed queries.
- Koin for dependency injection.
- Coroutines for async work.

## Project layout
- `composeApp/`: UI entry points per platform; shared Compose screens in `composeApp/src/commonMain/kotlin/com/kurban/calory`.
- `shared/`: domain and data layer, SQLDelight schemas in `shared/src/commonMain/sqldelight`, DI setup in `shared/src/commonMain/kotlin/com/kurban/calory/core/di`.
- `iosApp/iosApp`: Xcode host project for iOS builds.
- `gradle/libs.versions.toml`: version catalog for dependencies and plugins.

## Prerequisites
- JDK 11+
- Android Studio (for Android/Desktop) or Xcode (for iOS)
- Gradle wrapper is included; no extra install needed.

## Run it locally
- Android debug APK: `./gradlew :composeApp:assembleDebug`
- Desktop app: `./gradlew :composeApp:run`
- iOS: open `iosApp/iosApp` in Xcode, or `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`

## Useful tasks
- Shared checks/tests: `./gradlew :shared:check`
- Regenerate SQLDelight after schema changes: `./gradlew generateSqlDelightInterface`

## Notes
- Keep new dependencies in the version catalog (`gradle/libs.versions.toml`).
- SQLDelight schemas live under `shared/src/commonMain/sqldelight/dbscheme/*`; queries must stay inside their package folders.
