package com.jarod.card.features.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarod.card.core.util.DispatchersProvider
import com.jarod.card.domain.core.JokerCard
import com.jarod.card.domain.engine.GameAction
import com.jarod.card.domain.engine.PlayerId
import com.jarod.card.domain.engine.TurnCountdown
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
import com.jarod.card.domain.games.carioca.RoundEnd
import com.jarod.card.domain.games.carioca.Stage
import com.jarod.card.domain.games.carioca.StartNextRound
import com.jarod.card.features.game.cardskin.CardSkin
import com.jarod.card.features.game.cardskin.CardSkinStore
import com.jarod.card.features.game.stats.CumulativeStats
import com.jarod.card.features.game.stats.GameStats
import com.jarod.card.features.game.stats.GameStatsStore
import com.jarod.card.features.game.stats.GameStatsTracker
import com.jarod.card.features.game.stats.RoundStats
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
    val botsThinking: Boolean = false,
    val secondsLeft: Int = -1,
    val gameStats: GameStats? = null,
    val cumulativeStats: CumulativeStats = CumulativeStats(),
    val skin: CardSkin = CardSkin(),
    /** Info de fin de ronda para mostrar diálogo (ganador, puntos, nº ronda). */
    val roundEndInfo: RoundEndInfo? = null,
    /** Resumen congelado de la ronda terminada (duración y turnos), para el scoreboard. */
    val roundSummary: RoundStats? = null,
    /** Puntos ganados por ronda, en orden (índice = ronda - 1), para el desglose del scoreboard. */
    val roundsScores: List<Map<PlayerId, Int>> = emptyList()
)

data class RoundEndInfo(
    val round: Int,
    val winner: PlayerId,
    val pointsGained: Map<PlayerId, Int>
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val dispatchers: DispatchersProvider,
    private val skinStore: CardSkinStore,
    private val statsStore: GameStatsStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState(skin = skinStore.read()))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val statsTracker = GameStatsTracker()
    private val perRoundScores = mutableListOf<Map<PlayerId, Int>>()

    private val rng = java.util.Random()
    private var botJob: Job? = null
    private var turnTimerJob: Job? = null

    init {
        startGame()
    }

    fun startGame(ruleset: CariocaRuleset = CariocaRuleset(), seed: Long? = null) {
        botJob?.cancel()
        turnTimerJob?.cancel()
        turnTimerJob = null
        perRoundScores.clear()
        val gameSeed = seed ?: rng.nextLong()
        viewModelScope.launch {
            val players = listOf(PlayerId("tu")) + (1..3).map { PlayerId("bot$it") }
            val transition = CariocaGame.createGame(players, ruleset, gameSeed)
            _uiState.value = _uiState.value.copy(
                state = transition.state,
                humanId = players.first(),
                botsThinking = false,
                error = null,
                secondsLeft = -1,
                gameStats = null,
                roundSummary = null,
                roundsScores = emptyList(),
                cumulativeStats = statsStore.read()
            )
            statsTracker.startGame()
            trackState(transition.state)
            syncTurnTimer()
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

    fun clearRoundEnd() {
        val st = currentState() ?: return
        val human = _uiState.value.humanId ?: return
        if (st.phase == CariocaPhase.ROUND_END) {
            // Recién al cerrar el scoreboard se reparte y comienza la siguiente ronda
            applyAction(StartNextRound(human))
        } else {
            _uiState.value = _uiState.value.copy(roundEndInfo = null)
        }
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
        applyTransition(transition)
        syncTurnTimer()
        runBotsIfNeeded()
    }

    /** Aplica una transición, captura eventos RoundEnd y actualiza stats. */
    private fun applyTransition(transition: com.jarod.card.domain.engine.GameTransition<CariocaState>) {
        // Buscar evento RoundEnd en la transición
        val roundEndEvent = transition.events.firstOrNull { it is RoundEnd } as? RoundEnd
        val newRoundEndInfo = roundEndEvent?.let {
            perRoundScores += it.pointsGained
            RoundEndInfo(
                round = (_uiState.value.state?.roundIndex ?: 0) + 1, // ronda que acaba de terminar
                winner = it.winner,
                pointsGained = it.pointsGained
            )
        }
        _uiState.value = _uiState.value.copy(
            state = transition.state,
            error = null,
            roundEndInfo = newRoundEndInfo,
            roundsScores = perRoundScores.toList()
        )
        trackState(transition.state)
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

                    withContext(dispatchers.main) {
                        _uiState.value = _uiState.value.copy(state = current, botsThinking = true)
                    }
                    delay(BOT_DELAY_MS)

                    val botRng = java.util.Random()
                    val action = CariocaBot.chooseAction(current, player, botRng)
                    val vr = CariocaGame.canPerform(current, action)
                    val transition = if (vr.valid) {
                        CariocaGame.perform(current, action)
                    } else {
                        fallbackAction(current, player).let { CariocaGame.perform(current, it) }
                    }
                    // Aplicar transición en hilo principal para capturar RoundEnd
                    withContext(dispatchers.main) {
                        applyTransition(transition)
                        current = transition.state
                    }
                }
                withContext(dispatchers.main) {
                    _uiState.value = _uiState.value.copy(state = current, botsThinking = false)
                    if (current != null) trackState(current)
                    syncTurnTimer()
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

    // ──────────────────────────────────────────────────────────────
    // Estadísticas de partida (vueltas, turnos, tiempo por ronda)
    // ──────────────────────────────────────────────────────────────
    private fun trackState(st: CariocaState) {
        statsTracker.onState(st)
        // Resumen congelado de la ronda que acaba de terminar (duración y turnos).
        // Solo queda definido durante ROUND_END: en el scoreboard se muestra fijo.
        _uiState.value = _uiState.value.copy(
            roundSummary = if (st.phase == CariocaPhase.ROUND_END) statsTracker.lastFinishedRound() else null
        )
        if (st.phase == CariocaPhase.GAME_END && _uiState.value.gameStats == null) {
            val stats = statsTracker.result()
            val played = CumulativeStats(
                gamesPlayed = 1,
                roundsPlayed = stats.rounds.size,
                laps = stats.totalLaps,
                turns = stats.totalTurns,
                totalTimeMillis = stats.totalTimeMillis
            )
            _uiState.value = _uiState.value.copy(
                gameStats = stats,
                cumulativeStats = _uiState.value.cumulativeStats + played
            )
            statsStore.add(played)
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Temporizador del turno humano (aviso visual, genérico TurnTimeout)
    // ──────────────────────────────────────────────────────────────
    private fun syncTurnTimer() {
        val st = currentState() ?: return
        val human = _uiState.value.humanId ?: return
        val timeout = st.ruleset.turnTimeout
        val run = st.phase == CariocaPhase.PLAYING && st.currentPlayer == human && timeout.enabled

        if (run && turnTimerJob?.isActive == true) return

        turnTimerJob?.cancel()
        turnTimerJob = null

        if (!run) {
            if (_uiState.value.secondsLeft != -1) {
                _uiState.value = _uiState.value.copy(secondsLeft = -1)
            }
            return
        }

        val countdown = TurnCountdown(timeout)
        _uiState.value = _uiState.value.copy(secondsLeft = countdown.remainingSeconds)
        turnTimerJob = viewModelScope.launch(dispatchers.default) {
            while (!countdown.expired) {
                delay(1000)
                countdown.tick()
                withContext(dispatchers.main) {
                    val cur = currentState()
                    if (cur?.phase == CariocaPhase.PLAYING && cur.currentPlayer == _uiState.value.humanId) {
                        _uiState.value = _uiState.value.copy(secondsLeft = countdown.remainingSeconds)
                    }
                }
            }
        }
    }

    companion object {
        private const val BOT_DELAY_MS = 700L
    }
}
