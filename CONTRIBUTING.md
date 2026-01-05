# Contributing to BabaPhone

Thank you for your interest in contributing to BabaPhone!

## Development Workflow

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Make your changes
4. Run tests locally: `./gradlew test`
5. Commit your changes: `git commit -m "Add my feature"`
6. Push to your fork: `git push origin feature/my-feature`
7. Create a Pull Request

## Pull Request Guidelines

- All tests must pass before merging (enforced by CI)
- Keep changes focused and minimal
- Update documentation if needed
- Follow existing code style

## Testing

### Running Tests Locally

```bash
# Run all unit tests
./gradlew test

# Run lint checks
./gradlew lint

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

### Writing Tests

- Add unit tests for business logic in `app/src/test/`
- Add instrumented tests for UI and Android-specific code in `app/src/androidTest/`
- Follow existing test patterns

## Code Style

- Use Kotlin for all new code
- Follow Android Kotlin style guide
- Use meaningful variable and function names
- Add comments for complex logic

## Permissions and Security

When adding features that require permissions:

1. Declare permissions in `AndroidManifest.xml`
2. Request runtime permissions in the appropriate Activity
3. Handle permission denial gracefully
4. Document why the permission is needed

## Architecture

- **MainActivity**: UI and user interaction
- **AudioMonitorService**: Background audio monitoring
- Future networking components will go in a `network` package

## Questions?

Feel free to open an issue for questions or discussions.
