# Kyudo Tachi Tempo Guide

[![CI](https://github.com/alexfandos/KyudoTempo/actions/workflows/ci.yml/badge.svg)](https://github.com/alexfandos/KyudoTempo/actions/workflows/ci.yml)

An Android app that guides kyudo archers through the timing of a tachi, step by step.

At each step it shows what every archer in the line is doing (preparing, standing, shooting, sitting, leaving), so you can learn who moves when, for both Haya (the first arrow) and Otoya (the second arrow).

## Features

- Tachi of **3, 4 or 5 archers**
- Correct positions for every size:
  - 3: Omae, Ochimae, Ochi
  - 4: Omae, Ni-teki, Ochimae, Ochi
  - 5: Omae, Ni-teki, San-teki, Ochimae, Ochi
- Action hints for each step (e.g. *Stands up & Ashibumi & Dozukuri*, *Yugamae → … → Hanare*)
- Arrow count per archer
- Previous / Next navigation and reset

## Building

Requirements: Android Studio (recent version) with JDK 25. The app runs on Android 7.0 (API 24) and above.

1. Clone the repository and open it in Android Studio.
2. Let Gradle sync, then run the `app` configuration on a device or emulator.

From the command line:

```
./gradlew assembleDebug
```

## Tests

Unit tests cover the tachi logic in `KyudoViewModel` (names, navigation, choreography, arrow counts):

```
./gradlew testDebugUnitTest
```

They run automatically on every pull request and on pushes to `main`.

## Tech stack

Kotlin, Jetpack Compose, Material 3.
