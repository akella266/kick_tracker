package com.punchestracker

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.punchestracker.data.IosAppGroupKickMomentFileDataSource
import com.punchestracker.data.KickMomentRepositoryImpl
import com.punchestracker.platform.IosRussianDateTimeFormatter
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    val repository = remember { KickMomentRepositoryImpl(IosAppGroupKickMomentFileDataSource()) }
    val formatter = remember { IosRussianDateTimeFormatter() }
    App(
        repository = repository,
        dateTimeFormatter = formatter,
    )
}
