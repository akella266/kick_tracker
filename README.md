# Punches Tracker

Русскоязычное iOS-приложение на Compose Multiplatform для записи моментов шевеления ребёнка.

## Возможности

- Большая кнопка “Записать шевеление” на главном экране.
- Последние 5 записей на главном экране.
- Полная история записей на втором экране.
- Удаление ошибочных записей из истории.
- Интерактивный виджет для экрана “Домой”.
- Интерактивный виджет для экрана блокировки.
- Локальное хранение на устройстве без облака.

## Технологии

- Kotlin Multiplatform 2.4.10
- Compose Multiplatform 1.9.3
- Kotlinx Coroutines 1.11.0
- Kotlinx Serialization 1.11.0
- Kotlinx Datetime 0.8.0
- WidgetKit + AppIntent для iOS 17+
- XcodeGen для генерации Xcode project

## Bundle and App Group ids

- App bundle id: `com.punchestracker.KickTracker`
- Widget bundle id: `com.punchestracker.KickTracker.widget`
- App Group id: `group.com.punchestracker.KickTracker`

The App Group id must be enabled for both app and widget targets in Apple Developer settings before installing on a physical device.

## Windows verification

Windows can compile and test shared Kotlin code:

```bash
./gradlew :shared:compileKotlinMetadata :shared:jvmTest
```

## macOS verification

macOS with Xcode is required for Apple targets and WidgetKit:

```bash
brew install xcodegen
xcodegen generate
xcodebuild -project KickTracker.xcodeproj -scheme KickTracker -destination 'platform=iOS Simulator,name=iPhone 15' build
```

To run on a physical iPhone, open `KickTracker.xcodeproj`, select a development team for the app and widget targets, enable App Group `group.com.punchestracker.KickTracker`, then run the `KickTracker` scheme.
