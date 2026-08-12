package com.jarod.card.features.game.settings

import android.content.Context
import com.jarod.card.core.theme.ThemePreference
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Almacén de la preferencia de apariencia (claro/oscuro/sistema). */
interface ThemePreferenceStore {
    val preference: StateFlow<ThemePreference>
    fun read(): ThemePreference
    fun save(preference: ThemePreference)
}

@Singleton
class SharedPrefsThemePreferenceStore @Inject constructor(
    @ApplicationContext context: Context
) : ThemePreferenceStore {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _preference = MutableStateFlow(read())
    override val preference: StateFlow<ThemePreference> = _preference.asStateFlow()

    override fun read(): ThemePreference =
        prefs.getString(KEY_THEME_PREFERENCE, null)
            ?.let { runCatching { ThemePreference.valueOf(it) }.getOrNull() }
            ?: ThemePreference.SYSTEM

    override fun save(preference: ThemePreference) {
        prefs.edit().putString(KEY_THEME_PREFERENCE, preference.name).apply()
        _preference.value = preference
    }

    companion object {
        private const val PREFS_NAME = "user_settings"
        private const val KEY_THEME_PREFERENCE = "theme_preference"
    }
}
