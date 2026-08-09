package com.jarod.card.features.game.stats

import com.jarod.card.domain.engine.PlayerId
import com.jarod.card.domain.games.carioca.CariocaPhase
import com.jarod.card.domain.games.carioca.CariocaRuleset
import com.jarod.card.domain.games.carioca.CariocaState
import org.junit.Assert.assertEquals
import org.junit.Test

class GameStatsTrackerTest {

    private val players = listOf(PlayerId("p1"), PlayerId("p2"))
    private val rules = CariocaRuleset()

    private fun state(roundIndex: Int, phase: CariocaPhase, laps: Int, turns: Int): CariocaState =
        CariocaState(
            ruleset = rules,
            seed = 1L,
            phase = phase,
            roundIndex = roundIndex,
            players = players,
            laps = laps,
            turns = turns
        )

    @Test
    fun `agrega stats por ronda con vueltas turnos y tiempo`() {
        var now = 0L
        val tracker = GameStatsTracker(nowMillis = { now })
        tracker.startGame()

        now = 0L
        tracker.onState(state(0, CariocaPhase.PLAYING, laps = 0, turns = 0))
        now = 10_000L
        tracker.onState(state(0, CariocaPhase.PLAYING, laps = 2, turns = 8))
        // Ronda 1 termina: se congela al llegar a ROUND_END
        now = 15_000L
        tracker.onState(state(0, CariocaPhase.ROUND_END, laps = 2, turns = 8))

        // El jugador lee el scoreboard 5s; recién al continuar empieza la ronda 2
        now = 20_000L
        tracker.onState(state(1, CariocaPhase.PLAYING, laps = 2, turns = 8))

        now = 25_000L
        tracker.onState(state(1, CariocaPhase.PLAYING, laps = 5, turns = 16))
        // Fin de partida: la última ronda va directo a GAME_END y cierra la 2
        now = 30_000L
        tracker.onState(state(1, CariocaPhase.GAME_END, laps = 5, turns = 16))

        val stats = tracker.result()
        assertEquals(2, stats.rounds.size)

        val r1 = stats.rounds[0]
        assertEquals(1, r1.round)
        assertEquals(2, r1.laps)
        assertEquals(8, r1.turns)
        assertEquals(15_000L, r1.elapsedMillis)

        val r2 = stats.rounds[1]
        assertEquals(2, r2.round)
        assertEquals(3, r2.laps)
        assertEquals(8, r2.turns)
        assertEquals(10_000L, r2.elapsedMillis)

        assertEquals(5, stats.totalLaps)
        assertEquals(16, stats.totalTurns)
        assertEquals(25_000L, stats.totalTimeMillis)
    }

    @Test
    fun `estados repetidos en la misma ronda no duplican stats`() {
        var now = 0L
        val tracker = GameStatsTracker(nowMillis = { now })
        tracker.startGame()

        tracker.onState(state(0, CariocaPhase.PLAYING, laps = 1, turns = 4))
        tracker.onState(state(0, CariocaPhase.PLAYING, laps = 2, turns = 8))
        // ROUND_END repetido: la ronda ya quedó congelada, no se duplica
        tracker.onState(state(0, CariocaPhase.ROUND_END, laps = 3, turns = 12))
        tracker.onState(state(0, CariocaPhase.ROUND_END, laps = 3, turns = 12))
        // Ronda 2 empieza y termina (última)
        tracker.onState(state(1, CariocaPhase.PLAYING, laps = 3, turns = 12))
        tracker.onState(state(1, CariocaPhase.GAME_END, laps = 4, turns = 16))

        val stats = tracker.result()
        assertEquals(2, stats.rounds.size)
        assertEquals(4, stats.totalLaps)
        assertEquals(16, stats.totalTurns)
    }

    @Test
    fun `duracion y turnos se congelan al llegar a ROUND_END`() {
        var now = 0L
        val tracker = GameStatsTracker(nowMillis = { now })
        tracker.startGame()

        now = 0L
        tracker.onState(state(0, CariocaPhase.PLAYING, laps = 0, turns = 0))
        now = 10_000L
        tracker.onState(state(0, CariocaPhase.PLAYING, laps = 2, turns = 8))
        // Ronda termina: stats congelados en 15s / 8 turnos
        now = 15_000L
        tracker.onState(state(0, CariocaPhase.ROUND_END, laps = 2, turns = 8))

        // El scoreboard queda abierto 10s más: la duración no debe crecer
        now = 25_000L
        tracker.onState(state(0, CariocaPhase.ROUND_END, laps = 2, turns = 8))

        assertEquals(RoundStats(round = 1, laps = 2, turns = 8, elapsedMillis = 15_000L), tracker.lastFinishedRound())
        assertEquals(15_000L, tracker.result().rounds[0].elapsedMillis)
    }

    @Test
    fun `lastFinishedRound devuelve null si ninguna ronda termino`() {
        var now = 0L
        val tracker = GameStatsTracker(nowMillis = { now })
        tracker.startGame()
        tracker.onState(state(0, CariocaPhase.PLAYING, laps = 0, turns = 0))
        assertEquals(null, tracker.lastFinishedRound())
    }

    @Test
    fun `acumulados suman partidas rondas vueltas turnos y tiempo`() {
        val a = CumulativeStats(gamesPlayed = 3, roundsPlayed = 27, laps = 12, turns = 100, totalTimeMillis = 6_000L)
        val b = CumulativeStats(gamesPlayed = 1, roundsPlayed = 9, laps = 4, turns = 40, totalTimeMillis = 2_000L)
        assertEquals(
            CumulativeStats(gamesPlayed = 4, roundsPlayed = 36, laps = 16, turns = 140, totalTimeMillis = 8_000L),
            a + b
        )
    }
}
