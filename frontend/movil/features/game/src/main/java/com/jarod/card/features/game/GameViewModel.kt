package com.jarod.card.features.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarod.card.core.util.DispatchersProvider
import com.jarod.card.domain.core.JokerCard
import com.jarod.card.domain.engine.GameAction
import com.jarod.card.domain.engine.PlayerId
import com.jarod.card.domain.games.carioca.CariocaAction
import com.jarod.card.domain.games.carioca.CariocaBot
import com.jarod.card.domain.games.carioca.CariocaGame
import com.jarod.card.domain.games.carioca.CariocaPhase
import com.jarod.card.domain.games.carioca.CariocaRuleset
import com.jarod.card.domain.games.carioca.CariocaState
import com.jarod.card.domain.games.carioca.DiscardAction
import com.jarod.card.domain.games.carioca.DrawFromDiscard
import com.jarod.card.domain.games.carioca.DrawFromStock
import com.jarod.card.domain.games.carioca.MeldAction
import com.jarod.card.domain.games.carioca.Stage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GameUiState(
    val state: CariocaState? = null,
    val humanId: PlayerId? = null,
    val error: String? = null,
    val botsThinking: Boolean = false
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val dispatchers: DispatchersProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val rng = java.util.Random()
    private var botJob: Job? = null

    init {
        startGame()
    }

    fun startGame(ruleset: CariocaRuleset = CariocaRuleset(), seed: Long? = null) {
        botJob?.cancel()
        val gameSeed = seed ?: rng.nextLong()
        viewModelScope.launch {
            val players = listOf(PlayerId("tu")) + (1..3).map { PlayerId("bot$it") }
            val transition = CariocaGame.createGame(players, ruleset, gameSeed)
            _uiState.value = GameUiState(state = transition.state, humanId = players.first())
            runBotsIfNeeded()
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Acciones del humano
    // ──────────────────────────────────────────────────────────────
    fun drawFromStock() = humanAction { DrawFromStock(it) }

    fun drawFromDiscard() = humanAction { DrawFromDiscard(it) }

    fun autoMeld() = humanAction { human ->
        val st = currentState() ?: return@humanAction null
        val hand = st.hands[human] ?: return@humanAction null
        val round = st.ruleset.rounds[st.roundIndex]
        CariocaBot.findMeldForRound(hand, round)?.let { MeldAction(human, it) }
    }

    fun autoLayOff() = humanAction { human ->
        val st = currentState() ?: return@humanAction null
        CariocaBot.findLayOff(st, human)
    }

    fun discard(cardId: String) = humanAction { human -> DiscardAction(human, cardId) }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private inline fun humanAction(build: (PlayerId) -> CariocaAction?) {
        val st = currentState() ?: return
        val human = _uiState.value.humanId ?: return
        if (st.phase != CariocaPhase.PLAYING) return
        if (st.currentPlayer != human) return
        val action = build(human) ?: return
        applyAction(action)
    }

    private fun applyAction(action: GameAction) {
        val st = currentState() ?: return
        val vr = CariocaGame.canPerform(st, action)
        if (!vr.valid) {
            _uiState.value = _uiState.value.copy(error = vr.reason)
            return
        }
        val transition = CariocaGame.perform(st, action)
        _uiState.value = _uiState.value.copy(state = transition.state, error = null)
        runBotsIfNeeded()
    }

    // ──────────────────────────────────────────────────────────────
    // Turnos de los bots (off main thread)
    // ──────────────────────────────────────────────────────────────
    private fun runBotsIfNeeded() {
        val st = currentState() ?: return
        val human = _uiState.value.humanId ?: return
        if (st.phase != CariocaPhase.PLAYING) return
        if (st.currentPlayer == human) return
        if (_uiState.value.botsThinking) return
        _uiState.value = _uiState.value.copy(botsThinking = true)

        botJob?.cancel()
        botJob = viewModelScope.launch(dispatchers.default) {
            try {
                var current = currentState()
                while (current != null && current.phase == CariocaPhase.PLAYING) {
                    val player = current.currentPlayer ?: break
                    if (player == _uiState.value.humanId) break

                    val botRng = java.util.Random()
                    val action = CariocaBot.chooseAction(current, player, botRng)
                    val vr = CariocaGame.canPerform(current, action)
                    val next = if (vr.valid) {
                        CariocaGame.perform(current, action).state
                    } else {
                        fallbackAction(current, player).let { CariocaGame.perform(current, it).state }
                    }
                    current = next
                    delay(BOT_DELAY_MS)
                }
                withContext(dispatchers.main) {
                    _uiState.value = _uiState.value.copy(state = current, botsThinking = false)
                }
            } catch (e: Exception) {
                withContext(dispatchers.main) {
                    _uiState.value = _uiState.value.copy(error = "Error del bot: ${e.message}", botsThinking = false)
                }
            }
        }
    }

    private fun fallbackAction(state: CariocaState, player: PlayerId): CariocaAction {
        if (state.stage == Stage.DRAW) return DrawFromStock(player)
        val hand = state.hands[player]!!
        val discardable = hand.firstOrNull { it !is JokerCard } ?: hand.first()
        return DiscardAction(player, discardable.id)
    }

    private fun currentState(): CariocaState? = _uiState.value.state

    companion object {
        private const val BOT_DELAY_MS = 350L
    }
}
