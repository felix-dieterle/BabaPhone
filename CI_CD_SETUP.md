# CI/CD Setup Guide

This repository includes automated CI/CD workflows for the BabaPhone Android app.

## Continuous Integration (CI)

The `.github/workflows/android-ci.yml` workflow runs on every pull request and push to main:

### Unit Tests and Build (test job)
- Builds the app
- Runs unit tests
- Runs lint checks
- Uploads test and lint results as artifacts

### UI Screenshot Tests (ui-screenshot-tests job)
- Launches Android emulator
- Runs instrumented UI tests
- Captures screenshots of the app from different states:
  - Parent and Child modes
  - Portrait and landscape orientations
  - Different UI configurations
- Tests location/GPS mocking for 3D movement simulation
- Uploads screenshots as artifacts
- Comments on PR with screenshot information

See [SCREENSHOT_TESTING.md](SCREENSHOT_TESTING.md) for details on the screenshot testing infrastructure.

## Enabling Test-Based Merge Protection

To ensure tests prevent merges (as required), configure branch protection rules:

1. Go to repository **Settings** → **Branches**
2. Add a branch protection rule for `main`:
   - Check **Require status checks to pass before merging**
   - Select **Android CI / test** as required status check
   - Optionally select **Android CI / ui-screenshot-tests** for screenshot verification
   - Check **Require branches to be up to date before merging**
3. Save changes

With this configuration, pull requests cannot be merged unless all tests pass.

## Continuous Deployment (CD)

The `.github/workflows/android-release.yml` workflow runs automatically when code is merged to main:

- Builds a signed release APK (using debug keystore)
- Creates a GitHub release with automatic versioning
- Uploads the APK as a release asset

**Note:** Release builds are signed with the debug keystore, which is suitable for open-source projects and allows the APK to be installed on Android devices without "invalid package" errors.

## Local Testing

To run tests locally:

```bash
# Run unit tests
./gradlew test

# Run lint checks
./gradlew lint

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run instrumented tests (requires emulator or device)
./gradlew connectedDebugAndroidTest

# Pull screenshots from device after running UI tests
adb pull /sdcard/Android/data/com.example.babaphone/files/screenshots/ ./screenshots/
```

## Requirements

- JDK 17 or later
- Android SDK 34
- Gradle 8.2 (included via wrapper)
