package com.jarod.card.features.game.settings

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Preferencia de música de fondo de la vista de juego (sonando o silenciada). */
interface MusicPreferenceStore {
    fun isMuted(): Boolean
    fun saveMuted(muted: Boolean)
}

@Singleton
class SharedPrefsMusicPreferenceStore @Inject constructor(
    @ApplicationContext context: Context
) : MusicPreferenceStore {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun isMuted(): Boolean =
        prefs.getBoolean(KEY_MUSIC_MUTED, false)

    override fun saveMuted(muted: Boolean) {
        prefs.edit().putBoolean(KEY_MUSIC_MUTED, muted).apply()
    }

    companion object {
        private const val PREFS_NAME = "user_settings"
        private const val KEY_MUSIC_MUTED = "music_muted"
    }
}
