# Repository Guidelines

## Project Structure & Module Organization

Waggy is a single-module Android application. Gradle configuration lives in `build.gradle.kts`, `app/build.gradle.kts`, and `gradle/libs.versions.toml`. Production Kotlin code is under `app/src/main/java/ru/bitvibe/waggy/` and follows a layered structure:

- `data/`: Room entities and DAOs, Retrofit APIs, preferences, and repository implementations.
- `domain/`: models, repository contracts, and use cases.
- `presentation/`: Compose screens, navigation, ViewModels, and the Glance widget.

Android resources are in `app/src/main/res/`. Local JVM tests belong in `app/src/test/`; device and Compose UI tests belong in `app/src/androidTest/`. Keep code in the narrowest feature package.

## Build, Test, and Development Commands

Use the checked-in Gradle wrapper with JDK 17:

- `./gradlew assembleDebug` builds a debug APK.
- `./gradlew testDebug` runs local unit tests, as CI does.
- `./gradlew connectedDebugAndroidTest` runs instrumented tests on a connected emulator or device.
- `./gradlew lintDebug` performs Android static analysis.
- `./gradlew bundleRelease` creates the release AAB and requires release configuration.

Run `./gradlew clean` only when stale build output is suspected.

## Coding Style & Naming Conventions

Follow standard Kotlin and Android Studio formatting: four-space indentation, trailing commas in multiline declarations, and explicit imports. Use `PascalCase` for classes and composables, `camelCase` for functions and properties, and `UPPER_SNAKE_CASE` for constants. Name UI types consistently with existing patterns such as `BreedsScreen`, `BreedsViewModel`, `BreedsUiState`, and `GetAllBreedsUseCase`. Keep Compose functions focused and move reusable UI into a feature's `widgets/` package.

## Testing Guidelines

Tests use JUnit 4, AndroidX Test, Espresso, and Compose UI testing. Name test classes after the subject and methods after observable behavior, for example `loadBreeds_updatesStateOnSuccess`. Add local tests for domain and ViewModel logic; use instrumented tests for framework, database, widget, or UI behavior. There is no enforced coverage threshold, but new behavior and regressions should include focused tests.

## Commit & Pull Request Guidelines

Recent history generally uses Conventional Commit-style subjects such as `feat(widget): ...` and `chore: ...`. Write imperative, concise subjects and include a scope when useful. Pull requests should explain the user-visible change, list verification commands, link relevant issues, and include screenshots or recordings for UI/widget changes. Keep PRs focused and ensure `testDebug` passes.

## Security & Configuration

Copy `.env.example` to `.env` and keep `.env`, signing keys, and CI secrets out of version control. `BASE_URL` is read during Gradle configuration. Use the correct Firebase `app/google-services.json` for the application ID, but never place private service-account credentials in the repository.
