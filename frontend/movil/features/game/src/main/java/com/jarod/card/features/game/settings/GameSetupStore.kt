package com.jarod.card.features.game.settings

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Almacén de la configuración elegida en "Personalizar juego". */
interface GameSetupStore {
    fun read(): GameSetup
    fun save(setup: GameSetup)
}

@Singleton
class SharedPrefsGameSetupStore @Inject constructor(
    @ApplicationContext context: Context
) : GameSetupStore {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun read(): GameSetup {
        val rounds = prefs.getString(KEY_ROUNDS, null)
            ?.split(",")
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?.filter { it in 1..9 }
            ?: (1..9).toList()
        return GameSetup(
            players = prefs.getInt(KEY_PLAYERS, 4).coerceIn(2, 4),
            rounds = rounds.ifEmpty { (1..9).toList() },
            cutBonusEnabled = prefs.getBoolean(KEY_CUT_BONUS, false),
            initialRound = prefs.getInt(KEY_INITIAL_ROUND, 1).coerceIn(1, 9)
        )
    }

    override fun save(setup: GameSetup) {
        prefs.edit()
            .putInt(KEY_PLAYERS, setup.players.coerceIn(2, 4))
            .putString(KEY_ROUNDS, setup.rounds.filter { it in 1..9 }.distinct().sorted().joinToString(","))
            .putBoolean(KEY_CUT_BONUS, setup.cutBonusEnabled)
            .putInt(KEY_INITIAL_ROUND, setup.initialRound.coerceIn(1, 9))
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "game_setup"
        private const val KEY_PLAYERS = "players"
        private const val KEY_ROUNDS = "rounds"
        private const val KEY_CUT_BONUS = "cut_bonus"
        private const val KEY_INITIAL_ROUND = "initial_round"
    }
}
