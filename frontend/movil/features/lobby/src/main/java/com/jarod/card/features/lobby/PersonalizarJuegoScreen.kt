package com.jarod.card.features.lobby

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jarod.card.domain.games.carioca.CariocaRound
import com.jarod.card.domain.games.carioca.ComboType
import com.jarod.card.domain.games.carioca.defaultRounds

/**
 * "Personalizar juego" (FR-SAL-01 / FR-CAR-05): nº de jugadores y rondas a
 * jugar. Variantes y ronda inicial (debug) quedan deshabilitadas por ahora en
 * la vista, aunque su configuración persiste en [com.jarod.card.features.game.settings.GameSetup].
 */
@Composable
fun PersonalizarJuegoScreen(
    onStart: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PersonalizarJuegoViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Personalizar juego",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(16.dp))

        // Jugadores (Carioca: 2..4, FR-CAR-14)
        Text(
            text = "Jugadores",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (2..4).forEach { n ->
                FilterChip(
                    selected = ui.players == n,
                    onClick = { viewModel.setPlayers(n) },
                    label = { Text("$n") }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Rondas a jugar (cantidad y/o modos del catálogo base, rules.md §9.3)
        Text(
            text = "Rondas a jugar",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Marca las rondas del catálogo (${ui.rounds.size} de 9)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            defaultRounds.forEach { round ->
                RoundRow(
                    round = round,
                    selected = round.number in ui.rounds,
                    onToggle = { viewModel.toggleRound(round.number) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.apply()
                onStart()
            },
            enabled = ui.rounds.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("COMENZAR")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}

/** Fila de una ronda del catálogo, con el mismo estilo que la selección de reverso en Ajustes. */
@Composable
private fun RoundRow(
    round: CariocaRound,
    selected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onToggle)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Ronda ${round.number} — ${describeRound(round)}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (selected) "\u2713" else "\u25CB",
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Describe qué se exige en la ronda (rules.md §9.1), a partir del ComboSpec real. */
private fun describeRound(round: CariocaRound): String {
    val parts = round.combos.map { spec ->
        when (spec.type) {
            ComboType.TRIPLE -> "${spec.count} trío${if (spec.count > 1) "s" else ""}"
            ComboType.RUN -> if (spec.exactLength != null) {
                "Escala Real (${spec.exactLength} cartas)"
            } else {
                "${spec.count} escala${if (spec.count > 1) "s" else ""} de ${spec.minLength} o más cartas"
            }
        }
    }
    return parts.joinToString(" + ")
}
