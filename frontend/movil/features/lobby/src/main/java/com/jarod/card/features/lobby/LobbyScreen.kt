package com.jarod.card.features.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LobbyScreen(
    onCustomizeGame: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LobbyViewModel = hiltViewModel(),
    onOpenSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Salas",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Crear / unirse / listar salas (próximamente)",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Dispatcher IO: ${uiState.dispatcherLabel}",
            style = MaterialTheme.typography.bodySmall
        )
        Button(onClick = onCustomizeGame) {
            Text("Abrir partida demo")
        }
        OutlinedButton(onClick = onOpenSettings) {
            Text("Ajustes de cartas")
        }
    }
}
