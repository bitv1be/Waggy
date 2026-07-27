# Waggy

Waggy is an Android app for discovering dog breeds and keeping a personal list of favorites. It uses the [Dog CEO API](https://dog.ceo/dog-api/) for breed data and images, stores data locally, and includes a home-screen widget that rotates through favorite breeds.

## Features

- Browse dog breeds and their sub-breeds.
- View breed details and random dog images.
- Save favorite breeds and sub-breeds locally.
- Show random favorites in a configurable Glance widget.
- Choose light or dark theme preferences.
- Create layered dog imagery with on-device ML Kit subject segmentation.

## Tech stack

- Kotlin and Jetpack Compose with Material 3
- Navigation Compose and lifecycle-aware ViewModels
- Hilt for dependency injection
- Retrofit and Kotlin serialization for networking
- Room for local persistence
- Coil for image loading
- Glance and WorkManager for the app widget
- ML Kit subject segmentation

The app follows a layered `data` / `domain` / `presentation` structure and is built as a single Gradle module.

## Requirements

- Android Studio with JDK 17
- Android SDK 37
- Android device or emulator running API 24 or newer

## Getting started

1. Clone the repository and enter its directory:

   ```bash
   git clone https://github.com/SS1GGzxc/Waggy.git
   cd Waggy
   ```

2. Create the local environment file:

   ```bash
   cp .env.example .env
   ```

   The example configures `BASE_URL` for the Dog CEO API. Keep `.env` out of version control.

3. Open the project in Android Studio, let Gradle sync, and run the `app` configuration. To build from the command line instead:

   ```bash
   ./gradlew assembleDebug
   ```

The debug APK is written under `app/build/outputs/apk/debug/`.

## Project structure

```text
app/src/main/java/ru/bitvibe/waggy/
├── data/          # Database, network, preferences, and repositories
├── domain/        # Models, repository contracts, and use cases
└── presentation/  # Compose UI, navigation, ViewModels, and widget
```

Resources are in `app/src/main/res/`, local JVM tests are in `app/src/test/`, and instrumented tests are in `app/src/androidTest/`.

## Verification

```bash
./gradlew testDebug
./gradlew lintDebug
```

Run device and Compose UI tests with a connected emulator or device:

```bash
./gradlew connectedDebugAndroidTest
```

Signed APK and AAB releases can be published from GitHub Actions. Complete the
one-time secret setup and follow the release steps in
[`docs/RELEASING.md`](docs/RELEASING.md).

Contributor conventions and repository-specific workflow guidance are documented in [`AGENTS.md`](AGENTS.md).
