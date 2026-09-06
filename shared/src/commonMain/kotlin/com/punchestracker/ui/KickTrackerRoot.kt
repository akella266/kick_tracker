package com.punchestracker.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private enum class Tab {
    Main,
    History,
}

@OptIn(ExperimentalTime::class)
@Composable
fun KickTrackerRoot(
    observeKickMoments: ObserveKickMomentsUseCase,
    addKickMoment: AddKickMomentUseCase,
    deleteKickMoment: DeleteKickMomentUseCase,
    refreshKickMoments: RefreshKickMomentsUseCase,
    dateTimeFormatter: DateTimeFormatter,
    refreshEvents: Flow<Unit>,
) {
    val scope = rememberCoroutineScope()
    val mainPresenter = remember {
        MainPresenter(observeKickMoments, addKickMoment, dateTimeFormatter, scope)
    }
    val historyPresenter = remember {
        HistoryPresenter(observeKickMoments, deleteKickMoment, dateTimeFormatter, scope)
    }
    val tabs = remember { listOf(Tab.Main, Tab.History) }
    val pagerState = rememberPagerState(pageCount = tabs::size)
    val selectedTab = tabs[pagerState.currentPage]
    val mainState by mainPresenter.state.collectAsState()
    val historyState by historyPresenter.state.collectAsState()

    LaunchedEffect(refreshEvents) {
        refreshEvents
            .onStart { emit(Unit) }
            .collect {
                refreshKickMoments().onFailure {
                    // Presenters keep their current state if external storage cannot be read.
                }
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            mainPresenter.close()
            historyPresenter.close()
        }
    }

    AppTheme {
        KickTrackerScaffold(
            selectedTab = selectedTab,
            onTabSelected = { tab ->
                scope.launch {
                    pagerState.animateScrollToPage(tabs.indexOf(tab))
                }
            },
        ) { modifier ->
            HorizontalPager(
                state = pagerState,
                modifier = modifier,
            ) { page ->
                when (tabs[page]) {
                    Tab.Main -> MainScreen(
                        state = mainState,
                        onRecordKick = {
                            mainPresenter.onRecordKick(Clock.System.now().toEpochMilliseconds())
                        },
                    )

                    Tab.History -> HistoryScreen(
                        state = historyState,
                        onDelete = historyPresenter::onDelete,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KickTrackerScaffold(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                title = {
                    Text(
                        when (selectedTab) {
                            Tab.Main -> "Шевеления"
                            Tab.History -> "История"
                        }
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                NavigationBarItem(
                    selected = selectedTab == Tab.Main,
                    onClick = { onTabSelected(Tab.Main) },
                    icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
                    label = { Text("Главная") },
                )
                NavigationBarItem(
                    selected = selectedTab == Tab.History,
                    onClick = { onTabSelected(Tab.History) },
                    icon = { Icon(Icons.Rounded.History, contentDescription = null) },
                    label = { Text("История") },
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { contentPadding ->
        content(
            Modifier
                .fillMaxSize()
                .padding(contentPadding)
        )
    }
}
