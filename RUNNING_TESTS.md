# Running Tests

This guide explains how to run all tests in the BabaPhone project.

## Prerequisites

### Android App Tests
- JDK 17 or higher
- Android SDK (automatically managed by Gradle)

### Backend Tests
- PHP 7.4 or higher
- Composer

## Android App Tests

### Unit Tests

Run all unit tests:
```bash
./gradlew test
```

Run tests with coverage:
```bash
./gradlew testDebugUnitTest jacocoTestReport
```

View coverage report:
```bash
# Open in browser
open app/build/reports/jacoco/jacocoTestReport/html/index.html
# Or on Linux
xdg-open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

### Instrumented Tests

**Note:** Requires Android device or emulator

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

### Lint

Run lint checks:
```bash
./gradlew lint
```

## Backend Tests

### Unit Tests

Install dependencies:
```bash
cd backend
composer install
```

Run tests:
```bash
composer test
```

Run tests with coverage (requires Xdebug):
```bash
composer test-coverage
```

### Integration Tests

Start PHP development server:
```bash
cd backend
php -S localhost:8080 -t babyphone &
```

Run integration tests:
```bash
./test-backend.sh http://localhost:8080
```

Stop server:
```bash
pkill -f "php -S"
```

## Run All Tests

### Quick Test (No instrumented tests)
```bash
# Android unit tests
./gradlew test

# Backend unit tests
cd backend && composer test && cd ..
```

### Full Test Suite
```bash
# Android tests with coverage
./gradlew test jacocoTestReport

# Backend tests
cd backend
composer install
composer test

# Backend integration test
php -S localhost:8080 -t babyphone &
sleep 2
./test-backend.sh http://localhost:8080
pkill -f "php -S"
cd ..

# Android instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

## Continuous Integration

Tests run automatically via GitHub Actions on:
- Every push to main branch
- Every pull request to main

View CI results:
1. Go to GitHub repository
2. Click "Actions" tab
3. Select latest workflow run

## Test Results

### Android Test Results
- Location: `app/build/test-results/`
- Format: XML (JUnit format)

### Android Coverage Reports
- Location: `app/build/reports/jacoco/jacocoTestReport/html/`
- Format: HTML

### Backend Test Results
- Location: `backend/.phpunit.result.cache`
- Format: PHPUnit cache

### Backend Coverage Reports
- Location: `backend/coverage/`
- Format: HTML

## Troubleshooting

### Gradle Tests Fail with Network Error
This may happen in restricted networks. Try:
```bash
./gradlew test --offline
```

### Backend Tests Fail
Ensure PHP version is correct:
```bash
php --version  # Should be 7.4 or higher
```

### Integration Tests Fail
Check if port 8080 is available:
```bash
lsof -i :8080  # Should show nothing
```

## Writing New Tests

See [TESTING.md](TESTING.md) for comprehensive testing strategy and guidelines.

### Android Unit Test Example
```kotlin
package de.felixdieterle.babaphone

import org.junit.Test
import org.junit.Assert.*

class MyTest {
    @Test
    fun myFunction_validInput_returnsExpectedValue() {
        // Arrange
        val input = "test"
        
        // Act
        val result = myFunction(input)
        
        // Assert
        assertEquals("expected", result)
    }
}
```

### Backend Unit Test Example
```php
<?php
use PHPUnit\Framework\TestCase;

class MyTest extends TestCase
{
    public function testMyFunction()
    {
        // Arrange
        $input = 'test';
        
        // Act
        $result = myFunction($input);
        
        // Assert
        $this->assertEquals('expected', $result);
    }
}
```

## Coverage Goals

- Android business logic: > 70%
- Backend API logic: > 80%
- Critical paths: 100%

Current coverage can be viewed in the generated reports after running tests with coverage enabled.
