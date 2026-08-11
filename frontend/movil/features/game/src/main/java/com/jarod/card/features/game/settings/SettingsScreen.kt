package com.jarod.card.features.game.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jarod.card.core.ui.ConfirmDialog
import com.jarod.card.domain.core.JokerCard
import com.jarod.card.domain.core.JokerType
import com.jarod.card.domain.core.PlayingCard
import com.jarod.card.domain.core.Rank
import com.jarod.card.domain.core.Suit
import com.jarod.card.features.game.CardBack
import com.jarod.card.features.game.CardFace
import com.jarod.card.features.game.cardskin.BackDesign
import com.jarod.card.features.game.cardskin.FrontDesign
import com.jarod.card.features.game.cardskin.JokerStyle

private val SamplePlaying = PlayingCard("sample:\u2665:A", 0, Suit.HEART, Rank.ACE)
private val SampleJokerColored = JokerCard("sample:JOKER:COLORED", 0, JokerType.COLORED)
private val SampleJokerPlain = JokerCard("sample:JOKER:PLAIN", 0, JokerType.PLAIN)

/** Pantalla de ajustes: diseño de cartas y preferencias de accesibilidad. */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val skin by viewModel.skin.collectAsStateWithLifecycle()
    val dominantHand by viewModel.dominantHand.collectAsStateWithLifecycle()

    var showExitDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = !showExitDialog) { showExitDialog = true }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Column {
                Text(
                    text = "Ajustes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Personaliza las cartas y las preferencias de juego.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CardBack(skin = skin, deckIndex = 0)
            CardBack(skin = skin, deckIndex = 1)
            CardFace(card = SamplePlaying, skin = skin)
            CardFace(card = SampleJokerColored, skin = skin)
            CardFace(card = SampleJokerPlain, skin = skin)
        }

        Spacer(Modifier.height(24.dp))

        DesignSection(
            title = "Reverso · Mazo 1",
            options = BackDesign.entries,
            label = { it.label },
            isSelected = { it == skin.deck0 },
            preview = { option -> CardBack(width = 40.dp, height = 56.dp, deckIndex = 0, skin = skin.copy(deck0 = option)) },
            onSelect = viewModel::selectDeck0Back
        )

        DesignSection(
            title = "Reverso · Mazo 2",
            options = BackDesign.entries,
            label = { it.label },
            isSelected = { it == skin.deck1 },
            preview = { option -> CardBack(width = 40.dp, height = 56.dp, deckIndex = 1, skin = skin.copy(deck1 = option)) },
            onSelect = viewModel::selectDeck1Back
        )

        DesignSection(
            title = "Frontal",
            options = FrontDesign.entries,
            label = { it.label },
            isSelected = { it == skin.front },
            preview = { option -> CardFace(card = SamplePlaying, width = 40.dp, height = 56.dp, skin = skin.copy(front = option)) },
            onSelect = viewModel::selectFront
        )

        DesignSection(
            title = "Joker",
            subtitle = "Cada mazo trae un joker coloreado y uno sin colorear; el " +
                "sin colorear siempre se muestra en blanco y negro.",
            options = JokerStyle.entries,
            label = { it.label },
            isSelected = { it == skin.joker },
            preview = { option ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CardFace(card = SampleJokerColored, width = 40.dp, height = 56.dp, skin = skin.copy(joker = option))
                    CardFace(card = SampleJokerPlain, width = 40.dp, height = 56.dp, skin = skin.copy(joker = option))
                }
            },
            onSelect = viewModel::selectJoker
        )

        DesignSection(
            title = "Preferencia de mano",
            subtitle = "Posiciona el mazo y el pozo cerca de tu mano dominante.",
            options = DominantHand.entries,
            label = { "\u270B ${it.label}" },
            isSelected = { it == dominantHand },
            preview = {},
            onSelect = viewModel::selectDominantHand
        )
    }
}

@Composable
private fun <T> DesignSection(
    title: String,
    options: List<T>,
    label: (T) -> String,
    isSelected: (T) -> Boolean,
    preview: @Composable (T) -> Unit,
    onSelect: (T) -> Unit,
    subtitle: String? = null
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    if (subtitle != null) {
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(Modifier.height(8.dp))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            DesignOptionRow(
                label = label(option),
                selected = isSelected(option),
                preview = { preview(option) },
                onClick = { onSelect(option) }
            )
        }
    }
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun DesignOptionRow(
    label: String,
    selected: Boolean,
    preview: @Composable () -> Unit,
    onClick: () -> Unit
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
            .selectable(selected = selected, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            preview()
            Text(
                text = label,
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
