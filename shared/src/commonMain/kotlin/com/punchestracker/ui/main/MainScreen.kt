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
