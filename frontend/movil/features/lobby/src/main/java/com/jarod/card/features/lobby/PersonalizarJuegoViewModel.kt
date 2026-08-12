package com.jarod.card.features.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarod.card.core.debug.DEBUG_TOOLS_ENABLED
import com.jarod.card.features.game.settings.GameSetup
import com.jarod.card.features.game.settings.GameSetupStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Opciones de "Personalizar juego" (FR-SAL-01 / FR-CAR-05). */
data class PersonalizarUiState(
    val players: Int = 4,
    /** Números de ronda (1..9) seleccionados, ordenados. */
    val rounds: List<Int> = (1..9).toList(),
    val cutBonusEnabled: Boolean = false,
    /** Ronda inicial (número del catálogo) seleccionada para debug. */
    val initialRound: Int = 1,
    val debugToolsEnabled: Boolean = DEBUG_TOOLS_ENABLED
)

@HiltViewModel
class PersonalizarJuegoViewModel @Inject constructor(
    private val setupStore: GameSetupStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(PersonalizarUiState())
    val uiState: StateFlow<PersonalizarUiState> = _uiState.asStateFlow()

    init {
        val saved = setupStore.read()
        _uiState.value = PersonalizarUiState(
            players = saved.players,
            rounds = saved.rounds,
            cutBonusEnabled = saved.cutBonusEnabled,
            // Normaliza configuraciones viejas: la partida siempre empieza por
            // la ronda menor seleccionada.
            initialRound = saved.rounds.minOrNull() ?: 1
        )
    }

    fun setPlayers(players: Int) {
        _uiState.value = _uiState.value.copy(players = players.coerceIn(2, 4))
    }

    fun toggleRound(number: Int) {
        val cur = _uiState.value
        val rounds = if (number in cur.rounds) cur.rounds - number else (cur.rounds + number).sorted()
        // La partida siempre empieza por la ronda MENOR seleccionada: al elegir
        // en otro orden (p.ej. 9, 5, 3) se juega 3 → 5 → 9.
        _uiState.value = cur.copy(rounds = rounds, initialRound = rounds.firstOrNull() ?: 1)
    }

    fun setCutBonusEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(cutBonusEnabled = enabled)
    }

    fun setInitialRound(number: Int) {
        val cur = _uiState.value
        if (number in cur.rounds) {
            _uiState.value = cur.copy(initialRound = number)
        }
    }

    /** Guarda la configuración; la partida la leerá al iniciarse. */
    fun apply() {
        val s = _uiState.value
        setupStore.save(
            GameSetup(
                players = s.players,
                rounds = s.rounds,
                cutBonusEnabled = s.cutBonusEnabled,
                initialRound = s.initialRound
            )
        )
    }
}
