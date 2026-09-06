package com.punchestracker

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import com.punchestracker.data.IosAppGroupKickMomentFileDataSource
import com.punchestracker.data.KickMomentRepositoryImpl
import com.punchestracker.platform.IosRussianDateTimeFormatter
import kotlinx.coroutines.flow.MutableSharedFlow
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIViewController

@OptIn(ExperimentalComposeUiApi::class)
fun MainViewController(): UIViewController = ComposeUIViewController(
    configure = {
        enableBackGesture = false
    }
) {
    val repository = remember { KickMomentRepositoryImpl(IosAppGroupKickMomentFileDataSource()) }
    val formatter = remember { IosRussianDateTimeFormatter() }
    val refreshEvents = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }

    DisposableEffect(refreshEvents) {
        val notificationCenter = NSNotificationCenter.defaultCenter
        val observer = notificationCenter.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) {
            refreshEvents.tryEmit(Unit)
        }

        onDispose {
            notificationCenter.removeObserver(observer)
        }
    }

    App(
        repository = repository,
        dateTimeFormatter = formatter,
        refreshEvents = refreshEvents,
    )
}
