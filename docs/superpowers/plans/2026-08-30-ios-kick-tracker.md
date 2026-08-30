# iOS Kick Tracker Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an iOS-only Compose Multiplatform app in Russian that records fetal kick date/time entries, shows recent and full history lists, supports deletion, and provides interactive Home Screen and Lock Screen widgets.

**Architecture:** Shared Kotlin owns clean architecture layers, local JSON persistence, presentation state, and Compose UI. Native Swift is limited to hosting the Compose view and implementing WidgetKit/AppIntent entry points that write the same App Group JSON format.

**Tech Stack:** Kotlin Multiplatform 2.4.10, Compose Multiplatform 1.9.3, Kotlinx Coroutines 1.11.0, Kotlinx Serialization 1.11.0, Kotlinx Datetime 0.8.0, Gradle wrapper 8.14.3, XcodeGen project generation, SwiftUI WidgetKit/AppIntent for iOS 17+ widgets.

**Spec:** `docs/superpowers/specs/2026-08-30-ios-kick-tracker-design.md`

## Global Constraints

- iOS-only product app target using Compose Multiplatform.
- Shared Kotlin code must compile on Windows with Gradle.
- Final iOS app assembly, signing, widget integration, and simulator/device runs happen on macOS with Xcode.
- Swift is limited to iOS app bootstrap and WidgetKit/AppIntent code required by Apple platforms.
- App language is Russian.
- Local-only persistence; no iCloud, account, cloud backend, or sync.
- Shared app/widget storage uses an iOS App Group container.
- Minimum iOS version is 17.0 because interactive Home Screen and Lock Screen widgets require iOS 17+ WidgetKit/AppIntent APIs.
- Kick entries contain only `id` and `timestampMillis`.
- Full history screen supports deleting incorrect records.
- Default bundle id is `com.punchestracker.KickTracker`.
- Default App Group id is `group.com.punchestracker.KickTracker`.

---

## File Structure

Create this structure:

```text
.
├── .gitignore
├── README.md
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── gradle/
│   └── libs.versions.toml
├── shared/
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/com/punchestracker/
│       │   ├── App.kt
│       │   ├── data/
│       │   │   ├── KickMomentFileDataSource.kt
│       │   │   ├── KickMomentRepositoryImpl.kt
│       │   │   ├── KickMomentStorageModels.kt
│       │   │   └── KickStorageConstants.kt
│       │   ├── domain/
│       │   │   ├── KickMoment.kt
│       │   │   ├── KickMomentRepository.kt
│       │   │   └── usecase/
│       │   │       ├── AddKickMomentUseCase.kt
│       │   │       ├── DeleteKickMomentUseCase.kt
│       │   │       ├── ObserveKickMomentsUseCase.kt
│       │   │       └── RefreshKickMomentsUseCase.kt
│       │   ├── presentation/
│       │   │   ├── DateTimeFormatter.kt
│       │   │   ├── KickMomentUi.kt
│       │   │   ├── history/HistoryPresenter.kt
│       │   │   └── main/MainPresenter.kt
│       │   └── ui/
│       │       ├── KickTrackerRoot.kt
│       │       ├── history/HistoryScreen.kt
│       │       ├── main/MainScreen.kt
│       │       └── theme/AppTheme.kt
│       ├── commonTest/kotlin/com/punchestracker/
│       │   ├── data/KickMomentRepositoryImplTest.kt
│       │   ├── domain/KickMomentUseCaseTest.kt
│       │   └── presentation/PresenterTest.kt
│       ├── iosMain/kotlin/com/punchestracker/
│       │   ├── MainViewController.kt
│       │   ├── data/IosAppGroupKickMomentFileDataSource.kt
│       │   └── platform/IosRussianDateTimeFormatter.kt
│       └── jvmTest/kotlin/com/punchestracker/data/TempFileKickMomentFileDataSource.kt
├── iosApp/
│   ├── Info.plist
│   ├── KickTracker.entitlements
│   └── KickTrackerApp.swift
├── iosWidget/
│   ├── Info.plist
│   ├── KickMomentWidgetModels.swift
│   ├── KickMomentWidgetStore.swift
│   ├── KickTrackerWidget.entitlements
│   └── KickTrackerWidget.swift
└── project.yml
```

Responsibilities:

- `shared/src/commonMain/.../domain`: pure app rules and repository contracts.
- `shared/src/commonMain/.../data`: JSON storage models and repository implementation independent of Apple APIs.
- `shared/src/iosMain/.../data`: App Group file location and atomic iOS file writes.
- `shared/src/commonMain/.../presentation`: state reducers/presenters with no Compose dependencies beyond coroutine state.
- `shared/src/commonMain/.../ui`: Russian Compose UI and in-app navigation.
- `iosApp`: minimal SwiftUI app that hosts `MainViewController()` from the Kotlin framework.
- `iosWidget`: native iOS 17 WidgetKit/AppIntent widgets for Home Screen and Lock Screen.
- `project.yml`: XcodeGen definition for the app target, widget extension target, entitlements, and Gradle framework build phase.

---

### Task 1: Project scaffold and build configuration

**Files:**
- Create: `.gitignore`
- Create: `README.md`
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/libs.versions.toml`
- Create: `shared/build.gradle.kts`
- Generate: `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`

**Interfaces:**
- Consumes: approved design spec at `docs/superpowers/specs/2026-08-30-ios-kick-tracker-design.md`.
- Produces: Gradle project with one `shared` Kotlin Multiplatform module, iOS framework named `shared`, and a JVM test target used only for Windows-verifiable tests.

- [ ] **Step 1: Initialize git repository**

Run:

```bash
git init
```

Expected: repository initialized in `D:/projects/punches_tracker`.

- [ ] **Step 2: Generate Gradle wrapper**

Run:

```bash
gradle wrapper --gradle-version 8.14.3
```

Expected: `gradlew`, `gradlew.bat`, and `gradle/wrapper/*` are created.

- [ ] **Step 3: Create `.gitignore`**

Write:

```gitignore
.gradle/
build/
**/build/
.idea/
.kotlin/
*.iml
.DS_Store
derivedData/
*.xcworkspace/xcuserdata/
*.xcodeproj/xcuserdata/
*.xcodeproj/project.xcworkspace/xcuserdata/
iosApp/build/
iosWidget/build/
shared/build/
```

- [ ] **Step 4: Create `settings.gradle.kts`**

Write:

```kotlin
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PunchesTracker"
include(":shared")
```

- [ ] **Step 5: Create root `build.gradle.kts`**

Write:

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
}
```

- [ ] **Step 6: Create `gradle.properties`**

Write:

```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
org.gradle.configuration-cache=true
org.gradle.caching=true
kotlin.code.style=official
kotlin.mpp.enableCInteropCommonization=true
kotlin.native.ignoreDisabledTargets=true
compose.kotlin.native.manageCacheKind=false
```

- [ ] **Step 7: Create `gradle/libs.versions.toml`**

Write:

```toml
[versions]
kotlin = "2.4.10"
compose = "1.9.3"
coroutines = "1.11.0"
serialization = "1.11.0"
datetime = "0.8.0"

[libraries]
coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "datetime" }
kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }

[plugins]
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
composeMultiplatform = { id = "org.jetbrains.compose", version.ref = "compose" }
composeCompiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

- [ ] **Step 8: Create `shared/build.gradle.kts`**

Write:

```kotlin
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.coroutines.core)
            implementation(libs.serialization.json)
            implementation(libs.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
        }
        iosMain.dependencies {
            implementation(compose.ui)
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}
```

- [ ] **Step 9: Create initial `README.md`**

Write:

```markdown
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
```

- [ ] **Step 10: Run Gradle project check**

Run:

```bash
./gradlew projects
```

Expected: Gradle lists root project `PunchesTracker` and subproject `:shared`.

- [ ] **Step 11: Commit scaffold**

Run:

```bash
git add .gitignore README.md settings.gradle.kts build.gradle.kts gradle.properties gradle shared/build.gradle.kts gradlew gradlew.bat
git commit -m "chore: scaffold compose multiplatform project"
```

Expected: one commit containing only build scaffold and README.

---

### Task 2: Domain model and use cases

**Files:**
- Create: `shared/src/commonMain/kotlin/com/punchestracker/domain/KickMoment.kt`
- Create: `shared/src/commonMain/kotlin/com/punchestracker/domain/KickMomentRepository.kt`
- Create: `shared/src/commonMain/kotlin/com/punchestracker/domain/usecase/AddKickMomentUseCase.kt`
- Create: `shared/src/commonMain/kotlin/com/punchestracker/domain/usecase/DeleteKickMomentUseCase.kt`
- Create: `shared/src/commonMain/kotlin/com/punchestracker/domain/usecase/ObserveKickMomentsUseCase.kt`
- Create: `shared/src/commonMain/kotlin/com/punchestracker/domain/usecase/RefreshKickMomentsUseCase.kt`
- Create: `shared/src/commonTest/kotlin/com/punchestracker/domain/KickMomentUseCaseTest.kt`

**Interfaces:**
- Consumes: no app code from later tasks.
- Produces:
  - `data class KickMoment(val id: String, val timestampMillis: Long)`
  - `interface KickMomentRepository`
  - use cases with `operator fun invoke(...)` signatures used by data, presentation, and UI tasks.

- [ ] **Step 1: Write failing domain tests**

Create `shared/src/commonTest/kotlin/com/punchestracker/domain/KickMomentUseCaseTest.kt`:

```kotlin
package com.punchestracker.domain

import com.punchestracker.domain.usecase.AddKickMomentUseCase
import com.punchestracker.domain.usecase.DeleteKickMomentUseCase
import com.punchestracker.domain.usecase.ObserveKickMomentsUseCase
import com.punchestracker.domain.usecase.RefreshKickMomentsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class KickMomentUseCaseTest {
    private class FakeRepository : KickMomentRepository {
        val moments = MutableStateFlow(emptyList<KickMoment>())
        var deletedId: String? = null
        var refreshed = false

        override fun observeMoments() = moments

        override suspend fun addMoment(timestampMillis: Long): Result<KickMoment> {
            val moment = KickMoment(id = "moment-${timestampMillis}", timestampMillis = timestampMillis)
            moments.value = listOf(moment) + moments.value
            return Result.success(moment)
        }

        override suspend fun deleteMoment(id: String): Result<Unit> {
            deletedId = id
            moments.value = moments.value.filterNot { it.id == id }
            return Result.success(Unit)
        }

        override suspend fun refresh(): Result<Unit> {
            refreshed = true
            return Result.success(Unit)
        }
    }

    @Test
    fun addUseCaseReturnsMomentFromRepository() = runTest {
        val repository = FakeRepository()
        val result = AddKickMomentUseCase(repository).invoke(123_456L)

        assertEquals(KickMoment("moment-123456", 123_456L), result.getOrThrow())
        assertEquals(listOf(KickMoment("moment-123456", 123_456L)), repository.moments.value)
    }

    @Test
    fun deleteUseCaseDeletesSelectedId() = runTest {
        val repository = FakeRepository()
        DeleteKickMomentUseCase(repository).invoke("abc").getOrThrow()

        assertEquals("abc", repository.deletedId)
    }

    @Test
    fun observeUseCaseReturnsRepositoryFlow() = runTest {
        val repository = FakeRepository()
        repository.moments.value = listOf(KickMoment("1", 10L))

        assertEquals(listOf(KickMoment("1", 10L)), ObserveKickMomentsUseCase(repository).invoke().value)
    }

    @Test
    fun refreshUseCaseCallsRepositoryRefresh() = runTest {
        val repository = FakeRepository()
        RefreshKickMomentsUseCase(repository).invoke().getOrThrow()

        assertEquals(true, repository.refreshed)
    }
}
```

- [ ] **Step 2: Run tests to verify failure**

Run:

```bash
./gradlew :shared:jvmTest --tests "com.punchestracker.domain.KickMomentUseCaseTest"
```

Expected: FAIL because `KickMoment`, `KickMomentRepository`, and use case classes do not exist.

- [ ] **Step 3: Create domain model**

Create `KickMoment.kt`:

```kotlin
package com.punchestracker.domain

data class KickMoment(
    val id: String,
    val timestampMillis: Long,
)
```

- [ ] **Step 4: Create repository contract**

Create `KickMomentRepository.kt`:

```kotlin
package com.punchestracker.domain

import kotlinx.coroutines.flow.StateFlow

interface KickMomentRepository {
    fun observeMoments(): StateFlow<List<KickMoment>>
    suspend fun addMoment(timestampMillis: Long): Result<KickMoment>
    suspend fun deleteMoment(id: String): Result<Unit>
    suspend fun refresh(): Result<Unit>
}
```

- [ ] **Step 5: Create use cases**

Create the four files:

```kotlin
package com.punchestracker.domain.usecase

import com.punchestracker.domain.KickMomentRepository

class AddKickMomentUseCase(
    private val repository: KickMomentRepository,
) {
    suspend operator fun invoke(timestampMillis: Long) = repository.addMoment(timestampMillis)
}
```

```kotlin
package com.punchestracker.domain.usecase

import com.punchestracker.domain.KickMomentRepository

class DeleteKickMomentUseCase(
    private val repository: KickMomentRepository,
) {
    suspend operator fun invoke(id: String) = repository.deleteMoment(id)
}
```

```kotlin
package com.punchestracker.domain.usecase

import com.punchestracker.domain.KickMomentRepository

class ObserveKickMomentsUseCase(
    private val repository: KickMomentRepository,
) {
    operator fun invoke() = repository.observeMoments()
}
```

```kotlin
package com.punchestracker.domain.usecase

import com.punchestracker.domain.KickMomentRepository

class RefreshKickMomentsUseCase(
    private val repository: KickMomentRepository,
) {
    suspend operator fun invoke() = repository.refresh()
}
```

- [ ] **Step 6: Run domain tests**

Run:

```bash
./gradlew :shared:jvmTest --tests "com.punchestracker.domain.KickMomentUseCaseTest"
```

Expected: PASS.

- [ ] **Step 7: Commit domain layer**

Run:

```bash
git add shared/src/commonMain/kotlin/com/punchestracker/domain shared/src/commonTest/kotlin/com/punchestracker/domain
git commit -m "feat: add kick moment domain layer"
```

Expected: one commit containing domain model, repository interface, use cases, and tests.

---

### Task 3: JSON repository and local file abstraction

**Files:**
- Create: `shared/src/commonMain/kotlin/com/punchestracker/data/KickStorageConstants.kt`
- Create: `shared/src/commonMain/kotlin/com/punchestracker/data/KickMomentStorageModels.kt`
- Create: `shared/src/commonMain/kotlin/com/punchestracker/data/KickMomentFileDataSource.kt`
- Create: `shared/src/commonMain/kotlin/com/punchestracker/data/KickMomentRepositoryImpl.kt`
- Create: `shared/src/jvmTest/kotlin/com/punchestracker/data/TempFileKickMomentFileDataSource.kt`
- Create: `shared/src/commonTest/kotlin/com/punchestracker/data/KickMomentRepositoryImplTest.kt`

**Interfaces:**
- Consumes: `KickMoment`, `KickMomentRepository` from Task 2.
- Produces:
  - `object KickStorageConstants { const val FILE_NAME = "kick_moments.json" }`
  - `interface KickMomentFileDataSource`
  - `class KickMomentRepositoryImpl(fileDataSource, json, idProvider)`.

- [ ] **Step 1: Write failing repository tests**

Create `shared/src/commonTest/kotlin/com/punchestracker/data/KickMomentRepositoryImplTest.kt`:

```kotlin
package com.punchestracker.data

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KickMomentRepositoryImplTest {
    private class MemoryFileDataSource(initial: String? = null) : KickMomentFileDataSource {
        var content: String? = initial
        override suspend fun readText(): String? = content
        override suspend fun writeTextAtomically(text: String) {
            content = text
        }
    }

    @Test
    fun refreshReadsMomentsNewestFirst() = runTest {
        val source = MemoryFileDataSource(
            """
            {"moments":[{"id":"old","timestampMillis":10},{"id":"new","timestampMillis":20}]}
            """.trimIndent()
        )
        val repository = KickMomentRepositoryImpl(source, idProvider = { "unused" })

        repository.refresh().getOrThrow()

        assertEquals(listOf("new", "old"), repository.observeMoments().value.map { it.id })
    }

    @Test
    fun addMomentWritesJsonAndUpdatesState() = runTest {
        val source = MemoryFileDataSource()
        val repository = KickMomentRepositoryImpl(source, idProvider = { "fixed-id" })

        val result = repository.addMoment(30L).getOrThrow()

        assertEquals("fixed-id", result.id)
        assertEquals(30L, result.timestampMillis)
        assertEquals(listOf("fixed-id"), repository.observeMoments().value.map { it.id })
        assertTrue(source.content!!.contains("fixed-id"))
        assertTrue(source.content!!.contains("30"))
    }

    @Test
    fun deleteMomentRemovesOnlyMatchingId() = runTest {
        val source = MemoryFileDataSource(
            """
            {"moments":[{"id":"keep","timestampMillis":40},{"id":"remove","timestampMillis":50}]}
            """.trimIndent()
        )
        val repository = KickMomentRepositoryImpl(source, idProvider = { "unused" })
        repository.refresh().getOrThrow()

        repository.deleteMoment("remove").getOrThrow()

        assertEquals(listOf("keep"), repository.observeMoments().value.map { it.id })
        assertTrue(source.content!!.contains("keep"))
        assertTrue(!source.content!!.contains("remove"))
    }

    @Test
    fun corruptJsonRefreshReturnsFailureAndKeepsEmptyState() = runTest {
        val source = MemoryFileDataSource("not json")
        val repository = KickMomentRepositoryImpl(source, idProvider = { "unused" })

        val result = repository.refresh()

        assertTrue(result.isFailure)
        assertEquals(emptyList(), repository.observeMoments().value)
    }
}
```

- [ ] **Step 2: Run tests to verify failure**

Run:

```bash
./gradlew :shared:jvmTest --tests "com.punchestracker.data.KickMomentRepositoryImplTest"
```

Expected: FAIL because data classes and repository implementation do not exist.

- [ ] **Step 3: Create storage constants and models**

Create `KickStorageConstants.kt`:

```kotlin
package com.punchestracker.data

object KickStorageConstants {
    const val FILE_NAME = "kick_moments.json"
    const val APP_GROUP_ID = "group.com.punchestracker.KickTracker"
}
```

Create `KickMomentStorageModels.kt`:

```kotlin
package com.punchestracker.data

import kotlinx.serialization.Serializable

@Serializable
data class KickMomentFile(
    val moments: List<KickMomentRecord> = emptyList(),
)

@Serializable
data class KickMomentRecord(
    val id: String,
    val timestampMillis: Long,
)
```

- [ ] **Step 4: Create file data source interface**

Create `KickMomentFileDataSource.kt`:

```kotlin
package com.punchestracker.data

interface KickMomentFileDataSource {
    suspend fun readText(): String?
    suspend fun writeTextAtomically(text: String)
}
```

- [ ] **Step 5: Create repository implementation**

Create `KickMomentRepositoryImpl.kt`:

```kotlin
package com.punchestracker.data

import com.punchestracker.domain.KickMoment
import com.punchestracker.domain.KickMomentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

class KickMomentRepositoryImpl(
    private val fileDataSource: KickMomentFileDataSource,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        encodeDefaults = true
    },
    private val idProvider: () -> String = { createDefaultId() },
) : KickMomentRepository {
    private val moments = MutableStateFlow<List<KickMoment>>(emptyList())

    override fun observeMoments(): StateFlow<List<KickMoment>> = moments

    override suspend fun addMoment(timestampMillis: Long): Result<KickMoment> = runCatching {
        val current = readFileOrEmpty()
        val newMoment = KickMoment(id = idProvider(), timestampMillis = timestampMillis)
        val updated = (listOf(newMoment) + current).sortedByDescending { it.timestampMillis }
        writeMoments(updated)
        moments.value = updated
        newMoment
    }

    override suspend fun deleteMoment(id: String): Result<Unit> = runCatching {
        val current = readFileOrEmpty()
        val updated = current.filterNot { it.id == id }.sortedByDescending { it.timestampMillis }
        writeMoments(updated)
        moments.value = updated
    }

    override suspend fun refresh(): Result<Unit> = runCatching {
        moments.value = readFileOrEmpty().sortedByDescending { it.timestampMillis }
    }

    private suspend fun readFileOrEmpty(): List<KickMoment> {
        val text = fileDataSource.readText()?.takeIf { it.isNotBlank() } ?: return emptyList()
        return json.decodeFromString<KickMomentFile>(text)
            .moments
            .map { KickMoment(id = it.id, timestampMillis = it.timestampMillis) }
            .sortedByDescending { it.timestampMillis }
    }

    private suspend fun writeMoments(value: List<KickMoment>) {
        val file = KickMomentFile(
            moments = value
                .sortedByDescending { it.timestampMillis }
                .map { KickMomentRecord(id = it.id, timestampMillis = it.timestampMillis) }
        )
        fileDataSource.writeTextAtomically(json.encodeToString(KickMomentFile.serializer(), file))
    }

    companion object {
        fun createDefaultId(): String = "kick-${Clock.System.now().toEpochMilliseconds()}-${RandomId.next()}"
    }
}

private object RandomId {
    private var counter: Long = 0L

    fun next(): Long {
        counter += 1
        return counter
    }
}
```

- [ ] **Step 6: Create JVM temp file data source for tests**

Create `shared/src/jvmTest/kotlin/com/punchestracker/data/TempFileKickMomentFileDataSource.kt`:

```kotlin
package com.punchestracker.data

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class TempFileKickMomentFileDataSource(
    private val path: Path = Files.createTempFile("kick-moments", ".json"),
) : KickMomentFileDataSource {
    override suspend fun readText(): String? = if (path.exists()) path.readText() else null

    override suspend fun writeTextAtomically(text: String) {
        val tempPath = path.resolveSibling("${path.fileName}.tmp")
        tempPath.writeText(text)
        Files.move(tempPath, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
    }
}
```

- [ ] **Step 7: Run repository tests**

Run:

```bash
./gradlew :shared:jvmTest --tests "com.punchestracker.data.KickMomentRepositoryImplTest"
```

Expected: PASS.

- [ ] **Step 8: Run all shared JVM tests**

Run:

```bash
./gradlew :shared:jvmTest
```

Expected: PASS.

- [ ] **Step 9: Commit data layer**

Run:

```bash
git add shared/src/commonMain/kotlin/com/punchestracker/data shared/src/commonTest/kotlin/com/punchestracker/data shared/src/jvmTest/kotlin/com/punchestracker/data
git commit -m "feat: add local kick moment repository"
```

Expected: one commit containing storage abstraction, JSON repository, and repository tests.

---

### Task 4: Presentation state and Russian date formatting

**Files:**
- Create: `shared/src/commonMain/kotlin/com/punchestracker/presentation/DateTimeFormatter.kt`
- Create: `shared/src/commonMain/kotlin/com/punchestracker/presentation/KickMomentUi.kt`
- Create: `shared/src/commonMain/kotlin/com/punchestracker/presentation/main/MainPresenter.kt`
- Create: `shared/src/commonMain/kotlin/com/punchestracker/presentation/history/HistoryPresenter.kt`
- Create: `shared/src/commonTest/kotlin/com/punchestracker/presentation/PresenterTest.kt`

**Interfaces:**
- Consumes: domain model and use cases from Task 2.
- Produces:
  - `interface DateTimeFormatter { fun format(timestampMillis: Long): String }`
  - `data class KickMomentUi(val id: String, val formattedDateTime: String)`
  - `MainPresenter` and `HistoryPresenter` state holders used by Compose UI.

- [ ] **Step 1: Write failing presentation tests**

Create `PresenterTest.kt`:

```kotlin
package com.punchestracker.presentation

import com.punchestracker.domain.KickMoment
import com.punchestracker.domain.KickMomentRepository
import com.punchestracker.domain.usecase.AddKickMomentUseCase
import com.punchestracker.domain.usecase.DeleteKickMomentUseCase
import com.punchestracker.domain.usecase.ObserveKickMomentsUseCase
import com.punchestracker.domain.usecase.RefreshKickMomentsUseCase
import com.punchestracker.presentation.history.HistoryPresenter
import com.punchestracker.presentation.main.MainPresenter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PresenterTest {
    private class FakeFormatter : DateTimeFormatter {
        override fun format(timestampMillis: Long) = "formatted-$timestampMillis"
    }

    private class FakeRepository : KickMomentRepository {
        val moments = MutableStateFlow(emptyList<KickMoment>())
        var addResult: Result<KickMoment> = Result.success(KickMoment("new", 100L))
        var deleteResult: Result<Unit> = Result.success(Unit)

        override fun observeMoments() = moments

        override suspend fun addMoment(timestampMillis: Long): Result<KickMoment> {
            addResult.getOrNull()?.let { moments.value = listOf(it) + moments.value }
            return addResult
        }

        override suspend fun deleteMoment(id: String): Result<Unit> {
            if (deleteResult.isSuccess) moments.value = moments.value.filterNot { it.id == id }
            return deleteResult
        }

        override suspend fun refresh(): Result<Unit> = Result.success(Unit)
    }

    @Test
    fun mainPresenterShowsOnlyFiveRecentMoments() = runTest {
        val repository = FakeRepository()
        repository.moments.value = (1L..6L).map { KickMoment("id-$it", it) }.sortedByDescending { it.timestampMillis }
        val presenter = mainPresenter(repository, this)

        advanceUntilIdle()

        assertEquals(listOf("id-6", "id-5", "id-4", "id-3", "id-2"), presenter.state.value.recentMoments.map { it.id })
    }

    @Test
    fun mainPresenterRecordsMomentAndShowsRussianSuccessMessage() = runTest {
        val repository = FakeRepository()
        val presenter = mainPresenter(repository, this)

        presenter.onRecordKick(100L)
        advanceUntilIdle()

        assertEquals("Запись сохранена", presenter.state.value.lastRecordedMessage)
        assertNull(presenter.state.value.errorMessage)
    }

    @Test
    fun historyPresenterDeletesMoment() = runTest {
        val repository = FakeRepository()
        repository.moments.value = listOf(KickMoment("delete-me", 10L))
        val presenter = historyPresenter(repository, this)

        presenter.onDelete("delete-me")
        advanceUntilIdle()

        assertEquals(emptyList(), presenter.state.value.moments)
    }

    private fun mainPresenter(repository: KickMomentRepository, scope: TestScope): MainPresenter {
        return MainPresenter(
            observeKickMoments = ObserveKickMomentsUseCase(repository),
            addKickMoment = AddKickMomentUseCase(repository),
            refreshKickMoments = RefreshKickMomentsUseCase(repository),
            dateTimeFormatter = FakeFormatter(),
            scope = scope,
            dispatcher = StandardTestDispatcher(scope.testScheduler),
        )
    }

    private fun historyPresenter(repository: KickMomentRepository, scope: TestScope): HistoryPresenter {
        return HistoryPresenter(
            observeKickMoments = ObserveKickMomentsUseCase(repository),
            deleteKickMoment = DeleteKickMomentUseCase(repository),
            refreshKickMoments = RefreshKickMomentsUseCase(repository),
            dateTimeFormatter = FakeFormatter(),
            scope = scope,
            dispatcher = StandardTestDispatcher(scope.testScheduler),
        )
    }
}
```

- [ ] **Step 2: Run tests to verify failure**

Run:

```bash
./gradlew :shared:jvmTest --tests "com.punchestracker.presentation.PresenterTest"
```

Expected: FAIL because presentation classes do not exist.

- [ ] **Step 3: Create presentation models**

Create `DateTimeFormatter.kt`:

```kotlin
package com.punchestracker.presentation

interface DateTimeFormatter {
    fun format(timestampMillis: Long): String
}
```

Create `KickMomentUi.kt`:

```kotlin
package com.punchestracker.presentation

data class KickMomentUi(
    val id: String,
    val formattedDateTime: String,
)
```

- [ ] **Step 4: Create main presenter**

Create `MainPresenter.kt`:

```kotlin
package com.punchestracker.presentation.main

import com.punchestracker.domain.KickMoment
import com.punchestracker.domain.usecase.AddKickMomentUseCase
import com.punchestracker.domain.usecase.ObserveKickMomentsUseCase
import com.punchestracker.domain.usecase.RefreshKickMomentsUseCase
import com.punchestracker.presentation.DateTimeFormatter
import com.punchestracker.presentation.KickMomentUi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainPresenter(
    private val observeKickMoments: ObserveKickMomentsUseCase,
    private val addKickMoment: AddKickMomentUseCase,
    private val refreshKickMoments: RefreshKickMomentsUseCase,
    private val dateTimeFormatter: DateTimeFormatter,
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val mutableState = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = mutableState

    init {
        scope.launch {
            observeKickMoments().collect { moments ->
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        recentMoments = moments.take(5).map(::toUi),
                    )
                }
            }
        }
        scope.launch {
            refreshKickMoments().onFailure {
                mutableState.update { state -> state.copy(isLoading = false, errorMessage = "Не удалось загрузить историю") }
            }
        }
    }

    fun onRecordKick(timestampMillis: Long) {
        scope.launch {
            withContext(dispatcher) { addKickMoment(timestampMillis) }
                .onSuccess {
                    mutableState.update { state ->
                        state.copy(lastRecordedMessage = "Запись сохранена", errorMessage = null)
                    }
                }
                .onFailure {
                    mutableState.update { state ->
                        state.copy(lastRecordedMessage = null, errorMessage = "Не удалось сохранить запись")
                    }
                }
        }
    }

    fun clearMessages() {
        mutableState.update { it.copy(lastRecordedMessage = null, errorMessage = null) }
    }

    private fun toUi(moment: KickMoment) = KickMomentUi(
        id = moment.id,
        formattedDateTime = dateTimeFormatter.format(moment.timestampMillis),
    )
}

data class MainState(
    val isLoading: Boolean = true,
    val recentMoments: List<KickMomentUi> = emptyList(),
    val lastRecordedMessage: String? = null,
    val errorMessage: String? = null,
)
```

- [ ] **Step 5: Create history presenter**

Create `HistoryPresenter.kt`:

```kotlin
package com.punchestracker.presentation.history

import com.punchestracker.domain.KickMoment
import com.punchestracker.domain.usecase.DeleteKickMomentUseCase
import com.punchestracker.domain.usecase.ObserveKickMomentsUseCase
import com.punchestracker.domain.usecase.RefreshKickMomentsUseCase
import com.punchestracker.presentation.DateTimeFormatter
import com.punchestracker.presentation.KickMomentUi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryPresenter(
    private val observeKickMoments: ObserveKickMomentsUseCase,
    private val deleteKickMoment: DeleteKickMomentUseCase,
    private val refreshKickMoments: RefreshKickMomentsUseCase,
    private val dateTimeFormatter: DateTimeFormatter,
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val mutableState = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = mutableState

    init {
        scope.launch {
            observeKickMoments().collect { moments ->
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        moments = moments.map(::toUi),
                    )
                }
            }
        }
        scope.launch {
            refreshKickMoments().onFailure {
                mutableState.update { state -> state.copy(isLoading = false, errorMessage = "Не удалось загрузить историю") }
            }
        }
    }

    fun onDelete(id: String) {
        scope.launch {
            withContext(dispatcher) { deleteKickMoment(id) }
                .onFailure {
                    mutableState.update { state -> state.copy(errorMessage = "Не удалось удалить запись") }
                }
        }
    }

    fun clearError() {
        mutableState.update { it.copy(errorMessage = null) }
    }

    private fun toUi(moment: KickMoment) = KickMomentUi(
        id = moment.id,
        formattedDateTime = dateTimeFormatter.format(moment.timestampMillis),
    )
}

data class HistoryState(
    val isLoading: Boolean = true,
    val moments: List<KickMomentUi> = emptyList(),
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean = !isLoading && moments.isEmpty()
}
```

- [ ] **Step 6: Run presentation tests**

Run:

```bash
./gradlew :shared:jvmTest --tests "com.punchestracker.presentation.PresenterTest"
```

Expected: PASS.

- [ ] **Step 7: Commit presentation layer**

Run:

```bash
git add shared/src/commonMain/kotlin/com/punchestracker/presentation shared/src/commonTest/kotlin/com/punchestracker/presentation
git commit -m "feat: add kick tracker presentation state"
```

Expected: one commit containing state models, presenters, and tests.

---

### Task 5: iOS App Group data source and date formatter

**Files:**
- Create: `shared/src/iosMain/kotlin/com/punchestracker/data/IosAppGroupKickMomentFileDataSource.kt`
- Create: `shared/src/iosMain/kotlin/com/punchestracker/platform/IosRussianDateTimeFormatter.kt`

**Interfaces:**
- Consumes: `KickMomentFileDataSource`, `KickStorageConstants`, and `DateTimeFormatter`.
- Produces:
  - `class IosAppGroupKickMomentFileDataSource(appGroupId: String, fileName: String) : KickMomentFileDataSource`
  - `class IosRussianDateTimeFormatter : DateTimeFormatter`.

- [ ] **Step 1: Create iOS App Group data source**

Create `IosAppGroupKickMomentFileDataSource.kt`:

```kotlin
package com.punchestracker.data

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.writeToURL

@OptIn(ExperimentalForeignApi::class)
class IosAppGroupKickMomentFileDataSource(
    private val appGroupId: String = KickStorageConstants.APP_GROUP_ID,
    private val fileName: String = KickStorageConstants.FILE_NAME,
) : KickMomentFileDataSource {
    override suspend fun readText(): String? {
        val url = fileUrl() ?: return null
        val data = NSData.dataWithContentsOfURL(url) ?: return null
        return NSString.create(data = data, encoding = NSUTF8StringEncoding) as String?
    }

    override suspend fun writeTextAtomically(text: String) {
        val url = fileUrl() ?: error("App Group container is unavailable: $appGroupId")
        val nsString = text as NSString
        val data = nsString.dataUsingEncoding(NSUTF8StringEncoding) ?: error("Unable to encode kick moment JSON")
        val success = data.writeToURL(url, atomically = true)
        if (!success) error("Unable to write kick moment JSON")
    }

    private fun fileUrl(): NSURL? {
        val containerUrl = NSFileManager.defaultManager.containerURLForSecurityApplicationGroupIdentifier(appGroupId)
            ?: return null
        return containerUrl.URLByAppendingPathComponent(fileName)
    }
}
```

- [ ] **Step 2: Create Russian iOS date formatter**

Create `IosRussianDateTimeFormatter.kt`:

```kotlin
package com.punchestracker.platform

import com.punchestracker.presentation.DateTimeFormatter
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.dateWithTimeIntervalSince1970

class IosRussianDateTimeFormatter : DateTimeFormatter {
    private val formatter = NSDateFormatter().apply {
        locale = NSLocale(localeIdentifier = "ru_RU")
        dateFormat = "d MMMM, HH:mm"
    }

    override fun format(timestampMillis: Long): String {
        val date = NSDate.dateWithTimeIntervalSince1970(timestampMillis.toDouble() / 1000.0)
        return formatter.stringFromDate(date)
    }
}
```

- [ ] **Step 3: Run metadata compile from Windows**

Run:

```bash
./gradlew :shared:compileKotlinMetadata
```

Expected: PASS. This verifies common shared code compilation on Windows without requiring Apple linking.

- [ ] **Step 4: Run native compile on macOS**

On macOS, run:

```bash
./gradlew :shared:compileKotlinIosSimulatorArm64
```

Expected: PASS. This verifies iOS-specific Kotlin code compiles with Apple target tooling.

- [ ] **Step 5: Commit iOS platform layer**

Run:

```bash
git add shared/src/iosMain/kotlin/com/punchestracker/data shared/src/iosMain/kotlin/com/punchestracker/platform
git commit -m "feat: add ios app group storage"
```

Expected: one commit containing only iOS data source and formatter.

---

### Task 6: Compose UI, navigation, and Kotlin iOS entry point

**Files:**
- Create: `shared/src/commonMain/kotlin/com/punchestracker/App.kt`
- Create: `shared/src/commonMain/kotlin/com/punchestracker/ui/KickTrackerRoot.kt`
- Create: `shared/src/commonMain/kotlin/com/punchestracker/ui/main/MainScreen.kt`
- Create: `shared/src/commonMain/kotlin/com/punchestracker/ui/history/HistoryScreen.kt`
- Create: `shared/src/commonMain/kotlin/com/punchestracker/ui/theme/AppTheme.kt`
- Create: `shared/src/iosMain/kotlin/com/punchestracker/MainViewController.kt`

**Interfaces:**
- Consumes: repository implementation, iOS data source, date formatter, and presenters.
- Produces:
  - `@Composable fun App(repository: KickMomentRepository, dateTimeFormatter: DateTimeFormatter)`
  - `fun MainViewController(): UIViewController`
  - Russian main and history screens.

- [ ] **Step 1: Create common app root wrapper**

Create `App.kt`:

```kotlin
package com.punchestracker

import androidx.compose.runtime.Composable
import com.punchestracker.domain.KickMomentRepository
import com.punchestracker.domain.usecase.AddKickMomentUseCase
import com.punchestracker.domain.usecase.DeleteKickMomentUseCase
import com.punchestracker.domain.usecase.ObserveKickMomentsUseCase
import com.punchestracker.domain.usecase.RefreshKickMomentsUseCase
import com.punchestracker.presentation.DateTimeFormatter
import com.punchestracker.ui.KickTrackerRoot

@Composable
fun App(
    repository: KickMomentRepository,
    dateTimeFormatter: DateTimeFormatter,
) {
    KickTrackerRoot(
        observeKickMoments = ObserveKickMomentsUseCase(repository),
        addKickMoment = AddKickMomentUseCase(repository),
        deleteKickMoment = DeleteKickMomentUseCase(repository),
        refreshKickMoments = RefreshKickMomentsUseCase(repository),
        dateTimeFormatter = dateTimeFormatter,
    )
}
```

- [ ] **Step 2: Create theme**

Create `AppTheme.kt`:

```kotlin
package com.punchestracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColors = lightColorScheme(
    primary = Color(0xFFB45A7A),
    onPrimary = Color.White,
    secondary = Color(0xFF7D5260),
    background = Color(0xFFFFFBFF),
    surface = Color(0xFFFFFBFF),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColors,
        content = content,
    )
}
```

- [ ] **Step 3: Create main screen**

Create `MainScreen.kt`:

```kotlin
package com.punchestracker.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punchestracker.presentation.main.MainState

@Composable
fun MainScreen(
    state: MainState,
    onRecordKick: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Шевеления",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )

        Button(
            onClick = onRecordKick,
            modifier = Modifier.fillMaxWidth().height(96.dp),
        ) {
            Text(text = "Записать шевеление", fontSize = 22.sp)
        }

        state.lastRecordedMessage?.let { Text(text = it, color = MaterialTheme.colorScheme.primary) }
        state.errorMessage?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Последние записи", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            if (!state.isLoading && state.recentMoments.isEmpty()) {
                Text(text = "Пока нет записей")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.recentMoments, key = { it.id }) { moment ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = moment.formattedDateTime,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))
        TextButton(onClick = onOpenHistory) {
            Text("Вся история")
        }
    }
}
```

- [ ] **Step 4: Create history screen**

Create `HistoryScreen.kt`:

```kotlin
package com.punchestracker.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.punchestracker.presentation.history.HistoryState

@Composable
fun HistoryScreen(
    state: HistoryState,
    onBack: () -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("Назад") }
            Text(
                text = "История шевелений",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        state.errorMessage?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

        if (state.isEmpty) {
            Text(text = "История пуста")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.moments, key = { it.id }) { moment ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = moment.formattedDateTime, style = MaterialTheme.typography.bodyLarge)
                            IconButton(onClick = { onDelete(moment.id) }) {
                                Text("Удалить", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 5: Create root composable navigation**

Create `KickTrackerRoot.kt`:

```kotlin
package com.punchestracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.punchestracker.domain.usecase.AddKickMomentUseCase
import com.punchestracker.domain.usecase.DeleteKickMomentUseCase
import com.punchestracker.domain.usecase.ObserveKickMomentsUseCase
import com.punchestracker.domain.usecase.RefreshKickMomentsUseCase
import com.punchestracker.presentation.DateTimeFormatter
import com.punchestracker.presentation.history.HistoryPresenter
import com.punchestracker.presentation.main.MainPresenter
import com.punchestracker.ui.history.HistoryScreen
import com.punchestracker.ui.main.MainScreen
import com.punchestracker.ui.theme.AppTheme
import kotlinx.datetime.Clock

private enum class Screen { Main, History }

@Composable
fun KickTrackerRoot(
    observeKickMoments: ObserveKickMomentsUseCase,
    addKickMoment: AddKickMomentUseCase,
    deleteKickMoment: DeleteKickMomentUseCase,
    refreshKickMoments: RefreshKickMomentsUseCase,
    dateTimeFormatter: DateTimeFormatter,
) {
    val scope = rememberCoroutineScope()
    val mainPresenter = remember {
        MainPresenter(observeKickMoments, addKickMoment, refreshKickMoments, dateTimeFormatter, scope)
    }
    val historyPresenter = remember {
        HistoryPresenter(observeKickMoments, deleteKickMoment, refreshKickMoments, dateTimeFormatter, scope)
    }
    var screen by remember { mutableStateOf(Screen.Main) }

    DisposableEffect(Unit) {
        onDispose {
            mainPresenter.clearMessages()
            historyPresenter.clearError()
        }
    }

    AppTheme {
        when (screen) {
            Screen.Main -> {
                val state by mainPresenter.state.collectAsState()
                MainScreen(
                    state = state,
                    onRecordKick = { mainPresenter.onRecordKick(Clock.System.now().toEpochMilliseconds()) },
                    onOpenHistory = { screen = Screen.History },
                )
            }
            Screen.History -> {
                val state by historyPresenter.state.collectAsState()
                HistoryScreen(
                    state = state,
                    onBack = { screen = Screen.Main },
                    onDelete = historyPresenter::onDelete,
                )
            }
        }
    }
}
```

- [ ] **Step 6: Create Kotlin iOS view controller entry point**

Create `MainViewController.kt`:

```kotlin
package com.punchestracker

import androidx.compose.ui.window.ComposeUIViewController
import com.punchestracker.data.IosAppGroupKickMomentFileDataSource
import com.punchestracker.data.KickMomentRepositoryImpl
import com.punchestracker.platform.IosRussianDateTimeFormatter
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    App(
        repository = KickMomentRepositoryImpl(IosAppGroupKickMomentFileDataSource()),
        dateTimeFormatter = IosRussianDateTimeFormatter(),
    )
}
```

- [ ] **Step 7: Run shared compile**

Run on Windows:

```bash
./gradlew :shared:compileKotlinMetadata :shared:jvmTest
```

Expected: PASS.

- [ ] **Step 8: Commit Compose UI layer**

Run:

```bash
git add shared/src/commonMain/kotlin/com/punchestracker/App.kt shared/src/commonMain/kotlin/com/punchestracker/ui shared/src/iosMain/kotlin/com/punchestracker/MainViewController.kt
git commit -m "feat: add compose kick tracker ui"
```

Expected: one commit containing Compose app, screens, theme, and iOS Kotlin entry point.

---

### Task 7: iOS app host and XcodeGen project

**Files:**
- Create: `iosApp/KickTrackerApp.swift`
- Create: `iosApp/Info.plist`
- Create: `iosApp/KickTracker.entitlements`
- Create: `project.yml`

**Interfaces:**
- Consumes: Kotlin framework generated from `shared`, specifically `MainViewControllerKt.MainViewController()`.
- Produces: Xcode project definition with an iOS app target that embeds/signs the Kotlin framework and has App Group entitlement.

- [ ] **Step 1: Create Swift app host**

Create `iosApp/KickTrackerApp.swift`:

```swift
import SwiftUI
import shared

@main
struct KickTrackerApp: App {
    var body: some Scene {
        WindowGroup {
            ComposeRootView()
                .ignoresSafeArea(.keyboard)
        }
    }
}

struct ComposeRootView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
```

- [ ] **Step 2: Create iOS app `Info.plist`**

Create `iosApp/Info.plist`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleDevelopmentRegion</key>
    <string>ru</string>
    <key>CFBundleDisplayName</key>
    <string>Шевеления</string>
    <key>CFBundleExecutable</key>
    <string>$(EXECUTABLE_NAME)</string>
    <key>CFBundleIdentifier</key>
    <string>$(PRODUCT_BUNDLE_IDENTIFIER)</string>
    <key>CFBundleInfoDictionaryVersion</key>
    <string>6.0</string>
    <key>CFBundleName</key>
    <string>KickTracker</string>
    <key>CFBundlePackageType</key>
    <string>$(PRODUCT_BUNDLE_PACKAGE_TYPE)</string>
    <key>CFBundleShortVersionString</key>
    <string>1.0</string>
    <key>CFBundleVersion</key>
    <string>1</string>
    <key>LSRequiresIPhoneOS</key>
    <true/>
    <key>UIApplicationSceneManifest</key>
    <dict>
        <key>UIApplicationSupportsMultipleScenes</key>
        <false/>
    </dict>
</dict>
</plist>
```

- [ ] **Step 3: Create iOS app entitlements**

Create `iosApp/KickTracker.entitlements`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>com.apple.security.application-groups</key>
    <array>
        <string>group.com.punchestracker.KickTracker</string>
    </array>
</dict>
</plist>
```

- [ ] **Step 4: Create XcodeGen project definition**

Create `project.yml`:

```yaml
name: KickTracker
options:
  bundleIdPrefix: com.punchestracker
  deploymentTarget:
    iOS: "17.0"
settings:
  base:
    SWIFT_VERSION: "5.10"
    IPHONEOS_DEPLOYMENT_TARGET: "17.0"
    DEVELOPMENT_TEAM: ""
targets:
  KickTracker:
    type: application
    platform: iOS
    deploymentTarget: "17.0"
    sources:
      - path: iosApp
    info:
      path: iosApp/Info.plist
    entitlements:
      path: iosApp/KickTracker.entitlements
    settings:
      base:
        PRODUCT_BUNDLE_IDENTIFIER: com.punchestracker.KickTracker
        INFOPLIST_FILE: iosApp/Info.plist
        CODE_SIGN_ENTITLEMENTS: iosApp/KickTracker.entitlements
        FRAMEWORK_SEARCH_PATHS: $(SRCROOT)/shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)
        LD_RUNPATH_SEARCH_PATHS: $(inherited) @executable_path/Frameworks
    preBuildScripts:
      - name: Build Kotlin Framework
        basedOnDependencyAnalysis: false
        script: |
          cd "$SRCROOT"
          ./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

- [ ] **Step 5: Generate Xcode project on macOS**

Run:

```bash
xcodegen generate
```

Expected: `KickTracker.xcodeproj` is generated.

- [ ] **Step 6: Build app target on macOS simulator**

Run:

```bash
xcodebuild -project KickTracker.xcodeproj -scheme KickTracker -destination 'platform=iOS Simulator,name=iPhone 15' build
```

Expected: PASS after selecting a valid development team in Xcode or passing signing settings from the command line.

- [ ] **Step 7: Commit iOS app host**

Run:

```bash
git add iosApp project.yml
git commit -m "feat: add ios compose app host"
```

Expected: one commit containing the Swift app host, app plist, entitlements, and XcodeGen config.

---

### Task 8: Interactive Home Screen and Lock Screen widgets

**Files:**
- Create: `iosWidget/Info.plist`
- Create: `iosWidget/KickTrackerWidget.entitlements`
- Create: `iosWidget/KickMomentWidgetModels.swift`
- Create: `iosWidget/KickMomentWidgetStore.swift`
- Create: `iosWidget/KickTrackerWidget.swift`
- Modify: `project.yml`

**Interfaces:**
- Consumes: App Group id `group.com.punchestracker.KickTracker` and JSON file format from `KickMomentFile`.
- Produces: iOS 17+ WidgetKit extension with Home Screen and Lock Screen families and an `AppIntent` button that saves a kick moment.

- [ ] **Step 1: Create widget `Info.plist`**

Create `iosWidget/Info.plist`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleDevelopmentRegion</key>
    <string>ru</string>
    <key>CFBundleDisplayName</key>
    <string>Шевеления</string>
    <key>CFBundleExecutable</key>
    <string>$(EXECUTABLE_NAME)</string>
    <key>CFBundleIdentifier</key>
    <string>$(PRODUCT_BUNDLE_IDENTIFIER)</string>
    <key>CFBundleInfoDictionaryVersion</key>
    <string>6.0</string>
    <key>CFBundleName</key>
    <string>KickTrackerWidget</string>
    <key>CFBundlePackageType</key>
    <string>$(PRODUCT_BUNDLE_PACKAGE_TYPE)</string>
    <key>CFBundleShortVersionString</key>
    <string>1.0</string>
    <key>CFBundleVersion</key>
    <string>1</string>
    <key>NSExtension</key>
    <dict>
        <key>NSExtensionPointIdentifier</key>
        <string>com.apple.widgetkit-extension</string>
    </dict>
</dict>
</plist>
```

- [ ] **Step 2: Create widget entitlements**

Create `iosWidget/KickTrackerWidget.entitlements`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>com.apple.security.application-groups</key>
    <array>
        <string>group.com.punchestracker.KickTracker</string>
    </array>
</dict>
</plist>
```

- [ ] **Step 3: Create Swift storage models matching Kotlin JSON**

Create `KickMomentWidgetModels.swift`:

```swift
import Foundation

struct KickMomentFile: Codable {
    var moments: [KickMomentRecord]
}

struct KickMomentRecord: Codable, Identifiable {
    let id: String
    let timestampMillis: Int64
}
```

- [ ] **Step 4: Create Swift widget store**

Create `KickMomentWidgetStore.swift`:

```swift
import Foundation

struct KickMomentWidgetStore {
    private let appGroupId = "group.com.punchestracker.KickTracker"
    private let fileName = "kick_moments.json"

    func loadMoments() -> [KickMomentRecord] {
        guard let url = fileUrl(), FileManager.default.fileExists(atPath: url.path) else {
            return []
        }
        do {
            let data = try Data(contentsOf: url)
            let file = try JSONDecoder().decode(KickMomentFile.self, from: data)
            return file.moments.sorted { $0.timestampMillis > $1.timestampMillis }
        } catch {
            return []
        }
    }

    func saveCurrentMoment() throws {
        let current = loadMoments()
        let timestampMillis = Int64(Date().timeIntervalSince1970 * 1000)
        let newRecord = KickMomentRecord(
            id: "kick-\(timestampMillis)-widget",
            timestampMillis: timestampMillis
        )
        let updated = ([newRecord] + current).sorted { $0.timestampMillis > $1.timestampMillis }
        let file = KickMomentFile(moments: updated)
        let data = try JSONEncoder().encode(file)
        guard let url = fileUrl() else {
            throw WidgetStoreError.appGroupUnavailable
        }
        try data.write(to: url, options: [.atomic])
    }

    private func fileUrl() -> URL? {
        FileManager.default
            .containerURL(forSecurityApplicationGroupIdentifier: appGroupId)?
            .appendingPathComponent(fileName)
    }
}

enum WidgetStoreError: Error {
    case appGroupUnavailable
}
```

- [ ] **Step 5: Create widgets and intent**

Create `KickTrackerWidget.swift`:

```swift
import AppIntents
import SwiftUI
import WidgetKit

struct RecordKickIntent: AppIntent {
    static var title: LocalizedStringResource = "Записать шевеление"
    static var description = IntentDescription("Сохраняет текущее время шевеления.")

    func perform() async throws -> some IntentResult {
        try KickMomentWidgetStore().saveCurrentMoment()
        WidgetCenter.shared.reloadAllTimelines()
        return .result()
    }
}

struct KickTrackerEntry: TimelineEntry {
    let date: Date
    let lastMoment: KickMomentRecord?
}

struct KickTrackerProvider: TimelineProvider {
    func placeholder(in context: Context) -> KickTrackerEntry {
        KickTrackerEntry(date: Date(), lastMoment: nil)
    }

    func getSnapshot(in context: Context, completion: @escaping (KickTrackerEntry) -> Void) {
        completion(makeEntry())
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<KickTrackerEntry>) -> Void) {
        completion(Timeline(entries: [makeEntry()], policy: .never))
    }

    private func makeEntry() -> KickTrackerEntry {
        KickTrackerEntry(date: Date(), lastMoment: KickMomentWidgetStore().loadMoments().first)
    }
}

struct KickTrackerWidgetView: View {
    let entry: KickTrackerEntry
    @Environment(\.widgetFamily) private var family

    var body: some View {
        switch family {
        case .accessoryRectangular, .accessoryInline, .accessoryCircular:
            lockScreenBody
        default:
            homeScreenBody
        }
    }

    private var homeScreenBody: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Шевеление")
                .font(.headline)
            Button(intent: RecordKickIntent()) {
                Text("Записать")
                    .frame(maxWidth: .infinity)
            }
            if let lastMoment = entry.lastMoment {
                Text("Последняя: \(format(timestampMillis: lastMoment.timestampMillis))")
                    .font(.caption)
            } else {
                Text("Пока нет записей")
                    .font(.caption)
            }
        }
        .padding()
        .containerBackground(.background, for: .widget)
    }

    private var lockScreenBody: some View {
        VStack(spacing: 4) {
            Text("Шевеление")
                .font(.caption)
            Button(intent: RecordKickIntent()) {
                Text("Записать")
            }
            .buttonStyle(.bordered)
        }
        .containerBackground(.background, for: .widget)
    }

    private func format(timestampMillis: Int64) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(timestampMillis) / 1000)
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ru_RU")
        formatter.dateFormat = "d MMMM, HH:mm"
        return formatter.string(from: date)
    }
}

struct KickTrackerWidget: Widget {
    let kind = "KickTrackerWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: KickTrackerProvider()) { entry in
            KickTrackerWidgetView(entry: entry)
        }
        .configurationDisplayName("Шевеления")
        .description("Быстрая запись шевеления ребёнка.")
        .supportedFamilies([
            .systemSmall,
            .accessoryRectangular,
            .accessoryCircular,
            .accessoryInline,
        ])
    }
}

@main
struct KickTrackerWidgetBundle: WidgetBundle {
    var body: some Widget {
        KickTrackerWidget()
    }
}
```

- [ ] **Step 6: Modify `project.yml` to add widget target**

Replace the `targets:` block in `project.yml` with:

```yaml
targets:
  KickTracker:
    type: application
    platform: iOS
    deploymentTarget: "17.0"
    sources:
      - path: iosApp
    info:
      path: iosApp/Info.plist
    entitlements:
      path: iosApp/KickTracker.entitlements
    settings:
      base:
        PRODUCT_BUNDLE_IDENTIFIER: com.punchestracker.KickTracker
        INFOPLIST_FILE: iosApp/Info.plist
        CODE_SIGN_ENTITLEMENTS: iosApp/KickTracker.entitlements
        FRAMEWORK_SEARCH_PATHS: $(SRCROOT)/shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)
        LD_RUNPATH_SEARCH_PATHS: $(inherited) @executable_path/Frameworks
    dependencies:
      - target: KickTrackerWidgetExtension
    preBuildScripts:
      - name: Build Kotlin Framework
        basedOnDependencyAnalysis: false
        script: |
          cd "$SRCROOT"
          ./gradlew :shared:embedAndSignAppleFrameworkForXcode

  KickTrackerWidgetExtension:
    type: app-extension
    platform: iOS
    deploymentTarget: "17.0"
    sources:
      - path: iosWidget
    info:
      path: iosWidget/Info.plist
    entitlements:
      path: iosWidget/KickTrackerWidget.entitlements
    settings:
      base:
        PRODUCT_BUNDLE_IDENTIFIER: com.punchestracker.KickTracker.widget
        INFOPLIST_FILE: iosWidget/Info.plist
        CODE_SIGN_ENTITLEMENTS: iosWidget/KickTrackerWidget.entitlements
        SKIP_INSTALL: YES
```

- [ ] **Step 7: Generate and build Xcode project on macOS**

Run:

```bash
xcodegen generate
xcodebuild -project KickTracker.xcodeproj -scheme KickTracker -destination 'platform=iOS Simulator,name=iPhone 15' build
```

Expected: PASS after selecting a valid development team in Xcode or passing signing settings from the command line.

- [ ] **Step 8: Manual widget verification on macOS simulator or device**

Run the app from Xcode, then verify:

```text
1. Add the Home Screen widget named “Шевеления”.
2. Tap “Записать”.
3. Open the app.
4. Confirm the new moment appears in “Последние записи”.
5. Add the Lock Screen widget named “Шевеления”.
6. Tap “Записать”.
7. Open the app.
8. Confirm the Lock Screen-created moment appears in “История шевелений”.
```

Expected: app shows both widget-created records.

- [ ] **Step 9: Commit widgets**

Run:

```bash
git add iosWidget project.yml
git commit -m "feat: add interactive ios widgets"
```

Expected: one commit containing widget source, entitlements, plist, and project target update.

---

### Task 9: Final verification and documentation

**Files:**
- Modify: `README.md`

**Interfaces:**
- Consumes: all previous tasks.
- Produces: documented Windows and macOS verification commands and final verified project state.

- [ ] **Step 1: Update README with build/run instructions**

Replace `README.md` with:

```markdown
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
```

- [ ] **Step 2: Run full Windows verification**

Run:

```bash
./gradlew :shared:compileKotlinMetadata :shared:jvmTest
```

Expected: PASS.

- [ ] **Step 3: Run full macOS verification**

Run on macOS:

```bash
xcodegen generate
xcodebuild -project KickTracker.xcodeproj -scheme KickTracker -destination 'platform=iOS Simulator,name=iPhone 15' build
```

Expected: PASS with valid local signing settings.

- [ ] **Step 4: Manual app verification**

Run through this checklist on macOS simulator/device:

```text
1. Launch app.
2. Confirm main title is “Шевеления”.
3. Tap “Записать шевеление”.
4. Confirm “Запись сохранена” appears.
5. Confirm the new date/time appears under “Последние записи”.
6. Tap “Вся история”.
7. Confirm the same date/time appears in “История шевелений”.
8. Tap “Удалить” for the record.
9. Confirm the record disappears.
10. Restart app.
11. Confirm deleted record remains deleted.
```

Expected: every checklist item succeeds.

- [ ] **Step 5: Manual widget verification**

Run through this checklist on iOS 17+ simulator/device:

```text
1. Add Home Screen widget “Шевеления”.
2. Tap widget button “Записать”.
3. Open app and confirm a new record appears.
4. Add Lock Screen widget “Шевеления”.
5. Tap widget button “Записать”.
6. Open app and confirm a new record appears.
```

Expected: records created from both widgets are visible in app history.

- [ ] **Step 6: Commit documentation and verified state**

Run:

```bash
git add README.md
git commit -m "docs: add build and verification guide"
```

Expected: one commit containing final documentation.
