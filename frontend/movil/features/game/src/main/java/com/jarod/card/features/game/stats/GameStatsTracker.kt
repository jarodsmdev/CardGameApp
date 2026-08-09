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
    private var currentRoundFinished = false
    private val finishedRounds = mutableListOf<RoundStats>()

    fun startGame() {
        finishedRounds.clear()
        started = true
        currentRound = 1
        roundStartLaps = 0
        roundStartTurns = 0
        roundStartAt = nowMillis()
        currentRoundFinished = false
    }

    /**
     * Procesa cada estado publicado. La ronda en curso se "congela" al llegar a
     * ROUND_END (o GAME_END en la última): duración y turnos quedan fijos aunque
     * el scoreboard siga abierto o lleguen estados repetidos. Idempotente dentro
     * de la misma ronda.
     */
    fun onState(st: CariocaState) {
        if (!started) return
        val roundNumber = st.roundIndex + 1

        // Cambió de ronda: recién al repartir (continuar desde el scoreboard)
        // se reinicia la línea base y se reactiva la cuenta.
        if (roundNumber != currentRound) {
            currentRound = roundNumber
            roundStartLaps = st.laps
            roundStartTurns = st.turns
            roundStartAt = nowMillis()
            currentRoundFinished = false
        }

        // La ronda termina: congelar stats con lo acumulado hasta ahora.
        if ((st.phase == CariocaPhase.ROUND_END || st.phase == CariocaPhase.GAME_END) && !currentRoundFinished) {
            finishedRounds += buildRound(st)
            currentRoundFinished = true
            if (st.phase == CariocaPhase.GAME_END) started = false
        }
    }

    /** Stats congelados de la última ronda cerrada (para el scoreboard de ronda). */
    fun lastFinishedRound(): RoundStats? = finishedRounds.lastOrNull()

    fun result(): GameStats = GameStats(rounds = finishedRounds.toList())

    private fun buildRound(st: CariocaState) = RoundStats(
        round = currentRound,
        laps = (st.laps - roundStartLaps).coerceAtLeast(0),
        turns = (st.turns - roundStartTurns).coerceAtLeast(0),
        elapsedMillis = (nowMillis() - roundStartAt).coerceAtLeast(0)
    )
}
