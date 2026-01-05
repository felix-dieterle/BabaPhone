# UI Screenshot Testing

This document describes the UI screenshot testing infrastructure for BabaPhone.

## Overview

The PR test workflow now includes automated UI screenshot tests that capture the app from different states and perspectives, similar to viewing a 3D model from different angles. These screenshots help reviewers understand the UI changes and ensure the app looks correct across different states.

## What Gets Tested

The screenshot tests capture the app in the following states:

### Different Modes
1. **Parent Mode (Default)** - Shows the main parent interface with device list
2. **Child Mode** - Shows the child interface with audio level indicator

### Different Orientations
3. **Landscape Mode (Parent)** - Parent view in landscape orientation
4. **Landscape Mode (Child)** - Child view in landscape orientation

### Different UI States
5. **Device List View** - Parent mode showing available child devices
6. **Audio Indicator View** - Child mode showing real-time audio levels
7. **Sensitivity Controls** - View of sensitivity and volume controls
8. **Full UI Coverage** - Complete UI element visibility

### 3D Movement Simulation
The tests also include location/GPS mocking infrastructure that can simulate 3D movement through space (latitude, longitude, altitude). This demonstrates:
- Vertical movement (different floors/heights)
- Horizontal movement (different rooms/locations)
- Combined 3D movement paths

## How It Works

### During PR Test Runs

1. **Automated Emulator Launch**: The CI workflow launches an Android emulator
2. **UI Test Execution**: Instrumented tests run and interact with the app
3. **Screenshot Capture**: Screenshots are taken at each important state
4. **Artifact Upload**: Screenshots are uploaded as PR artifacts
5. **PR Comment**: A summary comment is added to the PR with screenshot info

### Screenshot Naming Convention

Screenshots are named with prefixes indicating their purpose:
- `01_parent_mode_default.png` - Parent mode default state
- `02_child_mode_default.png` - Child mode default state
- `03_parent_mode_device_list.png` - Device list view
- `04_child_mode_audio_indicator.png` - Audio level indicator
- `05_parent_mode_landscape.png` - Landscape orientation (parent)
- `06_child_mode_landscape.png` - Landscape orientation (child)
- `07_sensitivity_default.png` - Sensitivity controls
- `08_full_ui_parent_mode.png` - Full UI in parent mode
- `09_full_ui_child_mode.png` - Full UI in child mode

## GPS/Location Mocking

### Do We Need GPS Mocking?

For the current BabaPhone implementation, **GPS is not required** for core functionality. However, the infrastructure is in place for future location-based features:

- **Current Use**: Location permissions are requested for WiFi Direct (required by Android)
- **Not Used For**: Tracking device location or movement
- **Available For Future**: Geofencing, location-based pairing, distance calculations

### Location Mock Tests

The `LocationMockTest` class provides infrastructure for:
- Creating mock GPS coordinates with altitude (3D position)
- Simulating movement through 3D space
- Testing height changes (different floors in a building)
- Distance calculations between positions

Example use cases for future features:
```kotlin
// Mock movement from ground floor to 3rd floor
val groundFloor = createMockLocation(52.5200, 13.4050, 0.0, GPS_PROVIDER)
val thirdFloor = createMockLocation(52.5200, 13.4050, 9.0, GPS_PROVIDER)

// Calculate vertical distance
val heightDifference = thirdFloor.altitude - groundFloor.altitude // 9.0 meters
```

## Running Tests Locally

### Prerequisites
- Android Studio installed
- Android SDK with API level 29
- Android Emulator or physical device

### Run UI Screenshot Tests
```bash
# Start an emulator first, then:
./gradlew connectedDebugAndroidTest

# Screenshots will be saved to:
# /sdcard/Android/data/com.example.babaphone/files/screenshots/
```

### Pull Screenshots from Device
```bash
# After running tests on emulator/device:
adb pull /sdcard/Android/data/com.example.babaphone/files/screenshots/ ./screenshots/
```

## Viewing Screenshots in PRs

1. Go to the PR page on GitHub
2. Click on the "Checks" tab
3. Find the "ui-screenshot-tests" workflow
4. Look for "Artifacts" section
5. Download "ui-screenshots" artifact
6. Extract and view the PNG files

## Technical Details

### Dependencies
- `androidx.test.uiautomator` - For screenshot capture and device interaction
- `androidx.test.espresso` - For UI testing and interaction
- `androidx.test.rules` - For permission granting and test rules

### Emulator Configuration
- **API Level**: 29 (Android 10)
- **Architecture**: x86_64
- **GPU**: swiftshader_indirect (for CI compatibility)
- **Animations**: Disabled for consistent screenshots

### Test Configuration
- **Grant Permissions**: Audio recording, location, notifications
- **Screen Orientation**: Tests both portrait and landscape
- **Wait Times**: Includes delays for UI stability before screenshots

## Future Enhancements

Potential improvements to the screenshot testing:

1. **Comparison Testing**: Compare screenshots against baseline images
2. **Visual Regression**: Detect unintended UI changes
3. **Multiple Device Sizes**: Test on different screen sizes (phone, tablet)
4. **Dark Mode**: Capture screenshots in dark theme
5. **Accessibility**: Test with different text sizes and accessibility settings
6. **Real Location Data**: Use actual GPS data in tests if location features are added

## Troubleshooting

### Screenshots Not Appearing
- Check that permissions are granted in the test
- Verify emulator API level is 29 or higher
- Check CI logs for screenshot file paths
- Ensure external storage is accessible

### Tests Failing in CI
- Check emulator launch logs
- Verify KVM is enabled (for Linux CI)
- Check for timeout issues (increase wait times)
- Review logcat output in test reports

### Location Tests Failing
- Ensure location permissions are granted
- Verify mock location creation code
- Check Android version compatibility (some features require newer APIs)

## Related Documentation

- [Main README](../README.md) - App overview and features
- [CI/CD Setup](CI_CD_SETUP.md) - Continuous integration configuration
- [Contributing](CONTRIBUTING.md) - Development guidelines
