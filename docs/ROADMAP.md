# Roadmap

## Completed

- [x] Initialize documentation files.
- [x] Scaffold minimal native Android project structure.
- [x] Add Gradle wrapper files.
- [x] Add Kotlin + Jetpack Compose app module.
- [x] Add local storage for selected package names and the default daily limit.
- [x] Add initial MVP screens: dashboard, app selection, Usage Access, and settings.
- [x] Implement usage stats reading for selected apps after Usage Access is granted.
- [x] Add dependency-free usage limit logic test wired into Gradle `check`.

## Next

- [ ] Configure Android SDK API 35 locally and verify `./gradlew build`.
- [ ] Add basic UI tests where practical.
- [ ] Replace the temporary dependency-free usage limit test with a standard Android/JUnit test setup when dependency policy allows.
- [ ] Design enforcement behavior before adding any blocking or launcher-mode work.
- [ ] Keep notification reading out of scope until explicitly planned.
