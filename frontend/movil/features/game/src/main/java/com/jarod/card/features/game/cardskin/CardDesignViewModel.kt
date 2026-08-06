package com.jarod.card.features.game.cardskin

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class CardDesignViewModel @Inject constructor(
    private val store: CardSkinStore
) : ViewModel() {

    private val _skin = MutableStateFlow(store.read())
    val skin: StateFlow<CardSkin> = _skin.asStateFlow()

    fun selectDeck0Back(design: BackDesign) = update { it.copy(deck0 = design) }

    fun selectDeck1Back(design: BackDesign) = update { it.copy(deck1 = design) }

    fun selectFront(design: FrontDesign) = update { it.copy(front = design) }

    fun selectJoker(style: JokerStyle) = update { it.copy(joker = style) }

    private fun update(transform: (CardSkin) -> CardSkin) {
        val next = transform(_skin.value)
        _skin.value = next
        store.save(next)
    }
}
