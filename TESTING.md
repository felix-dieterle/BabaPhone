# BabaPhone Testing Strategy

## Overview

This document outlines the comprehensive testing strategy for the BabaPhone project, covering all critical components and abstraction levels.

## Testing Levels

### 1. Android App Testing

#### Unit Tests (JUnit)
Located in: `app/src/test/java/`

**Core Components Tested:**
- **BabaPhoneUnitTest.kt**: Audio calculation and settings validation
- **NetworkManagersUnitTest.kt**: Connection modes and hotspot configuration
- **DeviceInfoTest.kt**: Device information data structures
- **MobileDataManagerTest.kt**: Mobile data connectivity validation
- **AudioStreamManagerTest.kt**: Audio streaming parameters
- **NetworkDiscoveryManagerTest.kt**: Device discovery logic
- **AudioMonitorServiceTest.kt**: Audio monitoring calculations

**Run tests:**
```bash
./gradlew test
```

#### Instrumented Tests (AndroidJUnit4 + Espresso)
Located in: `app/src/androidTest/java/`

**UI Components Tested:**
- **BabaPhoneInstrumentedTest.kt**: Application context validation
- **MainActivityTest.kt**: Main UI functionality
- **SettingsActivityTest.kt**: Settings screen validation

**Run tests:**
```bash
./gradlew connectedAndroidTest
```

### 2. Backend Testing (PHP)

#### Unit Tests (PHPUnit)
Located in: `backend/tests/`

**API Components Tested:**
- **RegisterApiTest.php**: Device registration logic
- **DiscoverApiTest.php**: Device discovery endpoints
- **SignalApiTest.php**: WebRTC signaling
- **RelayApiTest.php**: Audio relay functionality

**Run tests:**
```bash
cd backend
composer install
composer test
```

#### Integration Tests
Located in: `backend/test-backend.sh`

**Workflow tested:**
1. Server availability check
2. Device registration (child and parent)
3. Device discovery
4. Heartbeat mechanism
5. Signal exchange
6. Signal retrieval
7. Cleanup and unregistration

**Run tests:**
```bash
cd backend
# Start PHP server
php -S localhost:8080 -t babyphone &

# Run integration tests
./test-backend.sh http://localhost:8080

# Stop server
pkill -f "php -S"
```

### 3. Code Coverage

#### Android Coverage (Jacoco)
**Configuration**: `app/build.gradle` includes Jacoco plugin

**Generate coverage report:**
```bash
./gradlew jacocoTestReport
```

**View report:**
Open `app/build/reports/jacoco/jacocoTestReport/html/index.html` in browser

#### Backend Coverage (PHPUnit)
**Generate coverage report:**
```bash
cd backend
composer test-coverage
```

**View report:**
Open `backend/coverage/index.html` in browser

## CI/CD Pipeline

### GitHub Actions Workflows

#### Android CI (`android-ci.yml`)
**Triggers:** Push/PR to main branch

**Steps:**
1. Set up JDK 17
2. Run unit tests
3. Run lint analysis
4. Build debug APK
5. Generate coverage reports
6. Upload test artifacts

#### Backend CI (included in `android-ci.yml`)
**Steps:**
1. Set up PHP 7.4
2. Install Composer dependencies
3. Run PHPUnit tests
4. Run integration test script
5. Upload test results

## Critical Test Coverage Areas

### High Priority Components

#### 1. Audio Processing
- ✅ RMS calculation accuracy
- ✅ Audio level detection
- ✅ Sensitivity threshold validation
- ✅ Volume scaling

#### 2. Network Management
- ✅ Connection mode detection (WiFi, Hotspot, Mobile Data)
- ✅ Device discovery protocol
- ✅ Hotspot configuration
- ✅ Mobile data backend communication

#### 3. Device Communication
- ✅ Device registration/unregistration
- ✅ Heartbeat mechanism
- ✅ Signal exchange (WebRTC)
- ✅ Audio relay

#### 4. Data Validation
- ✅ Device information structures
- ✅ API request/response formats
- ✅ JSON serialization
- ✅ Input validation

### Medium Priority Components

#### 5. UI Components
- ✅ Activity launches
- ✅ Settings persistence
- ⚠️ User interactions (limited Espresso tests)

#### 6. Configuration
- ✅ Build configuration
- ✅ API endpoint validation
- ✅ Timeout values

## Test Execution Guide

### Local Development

**Run all Android tests:**
```bash
./gradlew test jacocoTestReport
```

**Run all backend tests:**
```bash
cd backend
composer install
composer test
```

**Run complete integration test:**
```bash
# Start backend
cd backend
php -S localhost:8080 -t babyphone &

# Run integration test
./test-backend.sh http://localhost:8080

# Stop backend
pkill -f "php -S"
```

### Continuous Integration

Tests run automatically on:
- Every push to main branch
- Every pull request to main branch

**View results:**
- GitHub Actions tab in repository
- Artifacts include test results and coverage reports

## Test Maintenance

### Adding New Tests

**Android Unit Test:**
1. Create test file in `app/src/test/java/de/felixdieterle/babaphone/`
2. Follow naming convention: `ClassNameTest.kt`
3. Use JUnit 4 annotations: `@Test`

**Android Instrumented Test:**
1. Create test file in `app/src/androidTest/java/de/felixdieterle/babaphone/`
2. Use `@RunWith(AndroidJUnit4::class)`
3. Use Espresso for UI testing

**Backend Unit Test:**
1. Create test file in `backend/tests/`
2. Extend `PHPUnit\Framework\TestCase`
3. Follow naming convention: `ClassNameTest.php`

### Test Quality Standards

**All tests must:**
- ✅ Have descriptive names
- ✅ Test one specific behavior
- ✅ Be independent and isolated
- ✅ Be deterministic (no random failures)
- ✅ Run quickly (< 1 second per test)
- ✅ Include assertions

## Coverage Goals

### Target Coverage
- **Android Unit Tests**: > 70% of core business logic
- **Backend Unit Tests**: > 80% of API logic
- **Integration Tests**: 100% of critical user workflows

### Current Status
- ✅ Core audio processing: Covered
- ✅ Network managers: Covered
- ✅ Data structures: Covered
- ✅ Backend APIs: Covered
- ⚠️ UI components: Partially covered
- ⚠️ Service lifecycle: Partially covered (Android framework limitations)

## Known Limitations

### Android Testing
- Full service testing requires instrumentation tests
- UI testing limited without mock servers
- Permission-related code difficult to unit test

### Backend Testing
- Database operations require test database
- File system operations require test directories
- Network operations may need mocking

## Security Testing

### Automated Checks
- Input validation tests
- SQL injection prevention (via parameterized queries)
- XSS prevention (API only, no HTML output)

### Manual Review Areas
- Audio data encryption (future enhancement)
- WebRTC security (DTLS/SRTP)
- API authentication (future enhancement)

## Performance Testing

### Current Coverage
- Audio buffer size validation
- Network timeout configuration
- Packet size limits

### Future Enhancements
- Load testing for backend
- Audio latency measurement
- Memory usage profiling

## Conclusion

This testing strategy ensures comprehensive coverage of critical BabaPhone components through:
- Automated unit tests for business logic
- Instrumented tests for UI components
- Integration tests for end-to-end workflows
- Continuous integration via GitHub Actions
- Code coverage reporting for visibility

All tests are automated and run on every code change, ensuring quality and preventing regressions.
