# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FlySight Companion is an Android application for managing FlySight GPS device configuration files through Bluetooth Low Energy (BLE). The app allows users to create/edit configuration files locally and sync them with FlySight 2 devices via Bluetooth.

## Build Commands

**Building the project:**
```bash
./gradlew build                    # Build all modules
./gradlew app:build               # Build main app only
./gradlew assembleDebug           # Build debug APK
./gradlew assembleRelease         # Build release APK
```

**Testing:**
```bash
./gradlew test                    # Run unit tests for all modules
./gradlew app:test               # Run app unit tests only
./gradlew connectedAndroidTest   # Run instrumented tests
```

**Running the app:**
```bash
./gradlew installDebug           # Install debug version on connected device
```

## Modular Architecture

The project uses a clean, modular architecture with dependency injection:

### Module Structure
- **Feature modules**: `Feature/*` - Self-contained business features (ConfigFilesModule, FSDeviceModule, RecordsModule, etc.)
- **Middleware modules**: `Middleware/*` - Cross-cutting services (BluetoothModule, AudioModule, LocationModule)
- **Tooling modules**: `Tooling/*` - Reusable UI/utility components (DesignSystem, ComposableCommons, DialogModule)
- **Model module**: `model/` - Shared data models across all modules
- **App module**: `app/` - Main application and dependency injection wiring

### Dependency Injection (Dagger 2)
- **BaseComponent**: Root DI component in `app/src/main/java/fr/hozakan/flysightcompanion/tools/di/BaseComponent.kt`
- **ServiceModule**: Wires all modular services together - this is the main integration point
- **Injectable interface**: Activities/Fragments implement this for automatic injection
- **Service pattern**: Each feature/middleware module exposes a service interface with default implementation

### Key Services
The `ServiceModule` provides these critical services:
- `BluetoothService` - BLE device communication
- `FsDeviceService` - FlySight device management
- `ConfigFileService` - Configuration file operations
- `AudioService` - Audio feedback/alerts
- `LocationService` - GPS location handling
- `RecordService` - Flight record management

## Technology Stack

- **Language**: Kotlin with Java 11 target
- **UI**: Jetpack Compose with Material 3
- **DI**: Dagger 2 with Android support
- **Navigation**: Jetpack Navigation Compose
- **Architecture**: MVVM with ViewModels
- **Async**: Coroutines and Flow
- **Storage**: DataStore for preferences
- **Network**: Ktor client
- **Maps**: Google Maps Compose

## Development Guidelines

### Module Dependencies
- Feature modules can depend on: model, middleware, tooling modules and, in a few cases, on other feature modules
- Middleware modules can depend on: model, other middleware, tooling modules  
- Tooling modules should only depend on: model, other tooling modules
- All modules expose service interfaces for loose coupling

### Version Management
- Version names derived from git tags using custom logic in `app/build.gradle.kts:versionName()`
- Version codes calculated from semantic version components
- Target SDK: 35, Min SDK: 26

### Adding New Features
1. Create feature module in `Feature/` directory
2. Add service interface and default implementation
3. Register service in `ServiceModule.kt`
4. Add ViewModel binding in `ViewModelBindings.kt` if needed
5. Update `settings.gradle.kts` to include new module