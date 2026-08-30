# iOS Kick Tracker Design

Date: 2026-08-30

## Summary

Build an iOS-only Compose Multiplatform application for tracking the first and later fetal kick moments during pregnancy. The app language is Russian. Users can record the current day and time of a kick, view recent records on the main screen, view all records on a second screen, and delete incorrect records. iOS 17+ interactive Home Screen and Lock Screen widgets provide buttons to record a kick without opening the app.

The project must keep business logic, persistence, state, and Compose UI in Kotlin as much as possible. Swift is limited to the iOS app bootstrap and WidgetKit/AppIntent code required by Apple platforms.

## Scope

### In scope

- iOS-only app target using Compose Multiplatform.
- Shared Kotlin code that can compile on Windows.
- Clean architecture split into domain, data, presentation, and UI layers.
- Local-only persistence.
- Shared app/widget storage through an iOS App Group container.
- Main screen with a large record button and the latest 4–5 records.
- Full history screen with all records and delete support.
- Russian UI strings.
- iOS 17+ interactive WidgetKit Home Screen widget with a button to record the current moment.
- iOS 17+ interactive WidgetKit Lock Screen widget with a button to record the current moment after the user adds/enables it.

### Out of scope

- Android target.
- iCloud sync or backup.
- Accounts or cloud backend.
- Notes/comments per kick.
- Editing record timestamps.
- Medical analytics or diagnostic advice.

## Platform and build constraints

- Shared Kotlin code must be compilable on Windows with Gradle.
- Final iOS app assembly, signing, widget integration, and simulator/device runs happen on macOS with Xcode.
- WidgetKit and AppIntent require native Swift/SwiftUI files and iOS 17+.
- Swift code should stay thin and only bridge platform APIs that Kotlin/Compose cannot provide directly.

## Architecture

The project uses a Kotlin shared module containing clean architecture layers:

```text
shared/
  domain/
    model/
    repository/
    usecase/
  data/
    datasource/
    repository/
    serialization/
  presentation/
    main/
    history/
  ui/
    main/
    history/
    theme/
iosApp/
  Swift Compose host
iosWidget/
  WidgetKit + AppIntent extension
```

### Domain layer

Owns app rules and interfaces. It contains no Compose, storage, or iOS-specific APIs.

Core model:

- `KickMoment`
  - `id: String`
  - `timestampMillis: Long`

Repository contract:

- `KickMomentRepository`
  - `observeMoments(): Flow<List<KickMoment>>`
  - `addMoment(timestampMillis: Long): KickMoment`
  - `deleteMoment(id: String)`

Use cases:

- `AddKickMomentUseCase`
- `ObserveKickMomentsUseCase`
- `DeleteKickMomentUseCase`
- Optional `GetRecentKickMomentsUseCase(limit: Int)` if the recent-list derivation is not kept in presentation.

### Data layer

Implements local-only persistence against a JSON file in an App Group container on iOS.

Storage format:

```json
{
  "moments": [
    {
      "id": "uuid-or-generated-id",
      "timestampMillis": 1788100000000
    }
  ]
}
```

Data requirements:

- Records are sorted newest first when exposed to UI.
- Add operation appends a new record with current timestamp.
- Delete operation removes a record by id.
- Storage errors should not crash the UI. The app should show an empty or last-known state plus an error message where appropriate.
- App and widget must use the same App Group id.

Concurrency note:

- The app and widget can write the same JSON file. Writes should be serialized as safely as practical using atomic file replacement on iOS.
- If a conflict occurs, the newest readable full file wins; corrupt JSON should be handled gracefully.

### Presentation layer

State holders expose immutable UI state for Compose screens.

Main screen state:

- `isLoading: Boolean`
- `recentMoments: List<KickMomentUi>` limited to 4 or 5 items
- `lastRecordedMessage: String?`
- `errorMessage: String?`

Main actions:

- Record current kick
- Open full history

History screen state:

- `moments: List<KickMomentUi>`
- `isEmpty: Boolean`
- `errorMessage: String?`

History actions:

- Delete moment by id
- Navigate back

### UI layer

Compose Multiplatform UI is written in Kotlin. All visible text is Russian.

Main screen:

- Header: `Шевеления`
- Large primary button: `Записать шевеление`
- Recent section title: `Последние записи`
- Empty state: `Пока нет записей`
- Navigation action: `Вся история`

History screen:

- Header: `История шевелений`
- Rows display localized day and time.
- Delete action text/icon with confirmation or swipe-to-delete if supported cleanly.
- Empty state: `История пуста`

Date/time formatting:

- Russian user-facing format.
- Main/recent list may show compact format such as `30 августа, 14:25`.
- Full history may group by date later, but initial implementation can use a simple newest-first list.

## iOS app integration

The iOS app target contains a minimal Swift entry point that launches the Compose root view from the Kotlin framework.

Swift responsibilities:

- App lifecycle bootstrap.
- Compose view controller hosting.
- App Group id provision if needed by Kotlin platform code.

Kotlin responsibilities:

- Navigation.
- Screens.
- State holders.
- Use cases.
- Repository behavior.
- Reading/writing local storage.

## Widget integration

Widgets are implemented as iOS 17+ WidgetKit extensions because interactive Home Screen and Lock Screen widgets require Apple-native APIs.

Home Screen widget UI:

- Russian title such as `Шевеление`
- Button: `Записать`
- Optional last-recorded time if easy to read from shared storage.

Lock Screen widget UI:

- Compact Russian label such as `Шевеление`
- Interactive button or compact control: `Записать`
- Designed for the limited Lock Screen widget size.
- This replaces the idea of a persistent Lock Screen notification, because iOS notifications cannot be guaranteed to stay available permanently.

Widget action:

- Swift `AppIntent` receives the button tap from either widget.
- The intent writes a new moment with current timestamp to the same App Group JSON file.
- The intent reloads widget timelines after writing.

Swift should not duplicate broader business logic. It only performs the minimum platform-required write operation using the same storage format as Kotlin.

## Error handling

- If storage is unavailable, show a Russian error message such as `Не удалось сохранить запись`.
- If history cannot be read, show `Не удалось загрузить историю`.
- If deletion fails, keep the item visible and show `Не удалось удалить запись`.
- Widget failures should fail silently or show the previous timeline state, because WidgetKit interactions have limited error UI.

## Testing strategy

Windows-verifiable shared tests:

- Add moment use case creates a record with timestamp.
- Delete moment use case removes only the selected record.
- Repository serialization/deserialization preserves records.
- Recent list limits to 4–5 newest moments.
- Invalid/corrupt JSON is handled without crashing.

macOS/Xcode verification:

- iOS app launches and displays Compose UI.
- App can add and delete records.
- Data persists across app restart.
- Home Screen widget button records a moment while app is closed.
- Lock Screen widget button records a moment while app is closed.
- App sees widget-created records.

## Open implementation decisions

- Exact App Group id, likely `group.<bundle-id>.kicktracker`, must match the final iOS bundle id.
- Exact project scaffold plugin versions should be chosen during implementation based on current Compose Multiplatform compatibility.
- Whether delete uses swipe-to-delete, explicit trash icon, or confirmation dialog can be chosen based on Compose iOS component support during implementation.
