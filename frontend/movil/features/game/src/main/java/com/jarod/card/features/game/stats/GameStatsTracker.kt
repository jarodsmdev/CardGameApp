package com.jarod.card.features.game.stats

import com.jarod.card.domain.games.carioca.CariocaPhase
import com.jarod.card.domain.games.carioca.CariocaState

/**
 * Agrega estadísticas por ronda (vueltas, turnos, tiempo) a partir de los
 * estados publicados por el motor. El reloj es inyectable para testearlo.
 */
class GameStatsTracker(
    private val nowMillis: () -> Long = { System.currentTimeMillis() }
) {
    private var started = false
    private var currentRound = 1
    private var roundStartLaps = 0
    private var roundStartTurns = 0
    private var roundStartAt = 0L
    private val finishedRounds = mutableListOf<RoundStats>()

    fun startGame() {
        finishedRounds.clear()
        started = true
        currentRound = 1
        roundStartLaps = 0
        roundStartTurns = 0
        roundStartAt = nowMillis()
    }

    /** Procesa cada estado publicado. Idempotente dentro de la misma ronda. */
    fun onState(st: CariocaState) {
        if (!started) return
        val roundNumber = st.roundIndex + 1
        if (roundNumber != currentRound) {
            finishedRounds += buildRound(st)
            currentRound = roundNumber
            roundStartLaps = st.laps
            roundStartTurns = st.turns
            roundStartAt = nowMillis()
        }
        if (st.phase == CariocaPhase.GAME_END) {
            finishedRounds += buildRound(st)
            started = false
        }
    }

    fun result(): GameStats = GameStats(rounds = finishedRounds.toList())

    private fun buildRound(st: CariocaState) = RoundStats(
        round = currentRound,
        laps = (st.laps - roundStartLaps).coerceAtLeast(0),
        turns = (st.turns - roundStartTurns).coerceAtLeast(0),
        elapsedMillis = (nowMillis() - roundStartAt).coerceAtLeast(0)
    )
}
