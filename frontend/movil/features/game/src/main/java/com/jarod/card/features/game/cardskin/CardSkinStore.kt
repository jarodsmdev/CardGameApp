package com.jarod.card.features.game.cardskin

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Almacén de la elección de diseño de cartas. */
interface CardSkinStore {
    fun read(): CardSkin
    fun save(skin: CardSkin)
}

@Singleton
class SharedPrefsCardSkinStore @Inject constructor(
    @ApplicationContext context: Context
) : CardSkinStore {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun read(): CardSkin {
        val deck0 = prefs.getString(KEY_BACK_DECK_0, null)
            ?.let { runCatching { BackDesign.valueOf(it) }.getOrNull() }
            ?: BackDesign.ROJO
        val deck1 = prefs.getString(KEY_BACK_DECK_1, null)
            ?.let { runCatching { BackDesign.valueOf(it) }.getOrNull() }
            ?: BackDesign.NEGRO
        val front = prefs.getString(KEY_FRONT, null)
            ?.let { runCatching { FrontDesign.valueOf(it) }.getOrNull() }
            ?: FrontDesign.CLASICO
        val joker = prefs.getString(KEY_JOKER, null)
            ?.let { runCatching { JokerStyle.valueOf(it) }.getOrNull() }
            ?: JokerStyle.COLOR
        return CardSkin(deck0, deck1, front, joker)
    }

    override fun save(skin: CardSkin) {
        prefs.edit()
            .putString(KEY_BACK_DECK_0, skin.deck0.name)
            .putString(KEY_BACK_DECK_1, skin.deck1.name)
            .putString(KEY_FRONT, skin.front.name)
            .putString(KEY_JOKER, skin.joker.name)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "card_skin"
        private const val KEY_BACK_DECK_0 = "back_design"
        private const val KEY_BACK_DECK_1 = "back_design_deck1"
        private const val KEY_FRONT = "front_design"
        private const val KEY_JOKER = "joker_style"
    }
}
