# Punches Tracker

Русскоязычное iOS-приложение на Compose Multiplatform для записи моментов шевеления ребёнка.

## Product targets

- iOS app: Compose Multiplatform UI hosted from Swift.
- iOS widgets: native WidgetKit/AppIntent Home Screen and Lock Screen widgets.

## Local storage

The app and widgets share local JSON storage through App Group `group.com.punchestracker.KickTracker`.

## Windows checks

```bash
./gradlew :shared:compileKotlinMetadata :shared:jvmTest
```

## macOS checks

```bash
./gradlew :shared:embedAndSignAppleFrameworkForXcode
xcodegen generate
open KickTracker.xcodeproj
```
