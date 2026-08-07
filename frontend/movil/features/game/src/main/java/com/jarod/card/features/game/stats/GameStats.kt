package com.jarod.card.features.game.stats

/** Estadísticas de una ronda concreta (por partida, en sesión). */
data class RoundStats(
    val round: Int,
    val laps: Int,
    val turns: Int,
    val elapsedMillis: Long
)

/** Resumen de una partida: desglose por ronda + totales. */
data class GameStats(
    val rounds: List<RoundStats> = emptyList(),
    val totalLaps: Int = rounds.sumOf { it.laps },
    val totalTurns: Int = rounds.sumOf { it.turns },
    val totalTimeMillis: Long = rounds.sumOf { it.elapsedMillis }
)

/** Totales acumulados entre partidas (persistidos en el dispositivo). */
data class CumulativeStats(
    val gamesPlayed: Int = 0,
    val roundsPlayed: Int = 0,
    val laps: Int = 0,
    val turns: Int = 0,
    val totalTimeMillis: Long = 0
) {
    operator fun plus(o: CumulativeStats) = CumulativeStats(
        gamesPlayed = gamesPlayed + o.gamesPlayed,
        roundsPlayed = roundsPlayed + o.roundsPlayed,
        laps = laps + o.laps,
        turns = turns + o.turns,
        totalTimeMillis = totalTimeMillis + o.totalTimeMillis
    )
}
