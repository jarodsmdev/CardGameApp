package com.jarod.card.features.game.settings

import androidx.lifecycle.ViewModel
import com.jarod.card.core.theme.ThemePreference
import com.jarod.card.features.game.cardskin.BackDesign
import com.jarod.card.features.game.cardskin.CardSkin
import com.jarod.card.features.game.cardskin.CardSkinStore
import com.jarod.card.features.game.cardskin.FrontDesign
import com.jarod.card.features.game.cardskin.JokerStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val skinStore: CardSkinStore,
    private val handStore: DominantHandStore,
    private val themeStore: ThemePreferenceStore
) : ViewModel() {

    private val _skin = MutableStateFlow(skinStore.read())
    val skin: StateFlow<CardSkin> = _skin.asStateFlow()

    private val _dominantHand = MutableStateFlow(handStore.read())
    val dominantHand: StateFlow<DominantHand> = _dominantHand.asStateFlow()

    val themePreference: StateFlow<ThemePreference> = themeStore.preference

    fun selectDeck0Back(design: BackDesign) = updateSkin { it.copy(deck0 = design) }

    fun selectDeck1Back(design: BackDesign) = updateSkin { it.copy(deck1 = design) }

    fun selectFront(design: FrontDesign) = updateSkin { it.copy(front = design) }

    fun selectJoker(style: JokerStyle) = updateSkin { it.copy(joker = style) }

    fun selectDominantHand(hand: DominantHand) {
        _dominantHand.value = hand
        handStore.save(hand)
    }

    fun selectThemePreference(preference: ThemePreference) {
        themeStore.save(preference)
    }

    private fun updateSkin(transform: (CardSkin) -> CardSkin) {
        val next = transform(_skin.value)
        _skin.value = next
        skinStore.save(next)
    }
}
