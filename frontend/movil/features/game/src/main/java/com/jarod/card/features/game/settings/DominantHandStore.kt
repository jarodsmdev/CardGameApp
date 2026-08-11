package com.jarod.card.features.game.settings

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Almacén de la preferencia de mano dominante del jugador. */
interface DominantHandStore {
    fun read(): DominantHand
    fun save(hand: DominantHand)
}

@Singleton
class SharedPrefsDominantHandStore @Inject constructor(
    @ApplicationContext context: Context
) : DominantHandStore {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun read(): DominantHand =
        prefs.getString(KEY_DOMINANT_HAND, null)
            ?.let { runCatching { DominantHand.valueOf(it) }.getOrNull() }
            ?: DominantHand.RIGHT

    override fun save(hand: DominantHand) {
        prefs.edit().putString(KEY_DOMINANT_HAND, hand.name).apply()
    }

    companion object {
        private const val PREFS_NAME = "user_settings"
        private const val KEY_DOMINANT_HAND = "dominant_hand"
    }
}
