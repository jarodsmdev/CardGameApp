package com.jarod.card.features.game.stats

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Almacén persistente de estadísticas acumuladas entre partidas. */
interface GameStatsStore {
    fun read(): CumulativeStats
    fun add(played: CumulativeStats)
}

@Singleton
class SharedPrefsGameStatsStore @Inject constructor(
    @ApplicationContext context: Context
) : GameStatsStore {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun read(): CumulativeStats = CumulativeStats(
        gamesPlayed = prefs.getInt(KEY_GAMES, 0),
        roundsPlayed = prefs.getInt(KEY_ROUNDS, 0),
        laps = prefs.getInt(KEY_LAPS, 0),
        turns = prefs.getInt(KEY_TURNS, 0),
        totalTimeMillis = prefs.getLong(KEY_TIME_MS, 0L)
    )

    override fun add(played: CumulativeStats) {
        val total = read() + played
        prefs.edit()
            .putInt(KEY_GAMES, total.gamesPlayed)
            .putInt(KEY_ROUNDS, total.roundsPlayed)
            .putInt(KEY_LAPS, total.laps)
            .putInt(KEY_TURNS, total.turns)
            .putLong(KEY_TIME_MS, total.totalTimeMillis)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "game_stats"
        private const val KEY_GAMES = "games_played"
        private const val KEY_ROUNDS = "rounds_played"
        private const val KEY_LAPS = "laps"
        private const val KEY_TURNS = "turns"
        private const val KEY_TIME_MS = "total_time_ms"
    }
}
