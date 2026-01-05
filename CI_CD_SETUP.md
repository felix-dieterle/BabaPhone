# CI/CD Setup Guide

This repository includes automated CI/CD workflows for the BabaPhone Android app.

## Continuous Integration (CI)

The `.github/workflows/android-ci.yml` workflow runs on every pull request and push to main:

- Builds the app
- Runs unit tests
- Runs lint checks
- Uploads test and lint results as artifacts

## Enabling Test-Based Merge Protection

To ensure tests prevent merges (as required), configure branch protection rules:

1. Go to repository **Settings** → **Branches**
2. Add a branch protection rule for `main`:
   - Check **Require status checks to pass before merging**
   - Select **Android CI / test** as required status check
   - Check **Require branches to be up to date before merging**
3. Save changes

With this configuration, pull requests cannot be merged unless all tests pass.

## Continuous Deployment (CD)

The `.github/workflows/android-release.yml` workflow runs automatically when code is merged to main:

- Builds a release APK
- Creates a GitHub release with automatic versioning
- Uploads the APK as a release asset

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
```

## Requirements

- JDK 17 or later
- Android SDK 34
- Gradle 8.2 (included via wrapper)
