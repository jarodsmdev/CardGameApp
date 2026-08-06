package com.jarod.card.features.game.cardskin

import org.junit.Assert.assertEquals
import org.junit.Test

class CardDesignViewModelTest {

    private class FakeStore(var stored: CardSkin = CardSkin()) : CardSkinStore {
        override fun read(): CardSkin = stored
        override fun save(skin: CardSkin) {
            stored = skin
        }
    }

    @Test
    fun `el skin por defecto es mazo 1 rojo, mazo 2 negro, clásico y a color`() {
        val vm = CardDesignViewModel(FakeStore())
        assertEquals(CardSkin(), vm.skin.value)
        assertEquals(BackDesign.ROJO, vm.skin.value.deck0)
        assertEquals(BackDesign.NEGRO, vm.skin.value.deck1)
    }

    @Test
    fun `cargar el skin guardado del almacén`() {
        val saved = CardSkin(
            deck0 = BackDesign.ROMBOS,
            deck1 = BackDesign.ANILLOS,
            front = FrontDesign.DORADO,
            joker = JokerStyle.ORO
        )
        val vm = CardDesignViewModel(FakeStore(saved))
        assertEquals(BackDesign.ROMBOS, vm.skin.value.deck0)
        assertEquals(BackDesign.ANILLOS, vm.skin.value.deck1)
        assertEquals(FrontDesign.DORADO, vm.skin.value.front)
        assertEquals(JokerStyle.ORO, vm.skin.value.joker)
    }

    @Test
    fun `seleccionar el reverso del mazo 1 actualiza y persiste sin tocar el mazo 2`() {
        val store = FakeStore()
        val vm = CardDesignViewModel(store)
        vm.selectDeck0Back(BackDesign.ROMBOS)
        assertEquals(BackDesign.ROMBOS, vm.skin.value.deck0)
        assertEquals(BackDesign.NEGRO, vm.skin.value.deck1)
        assertEquals(BackDesign.ROMBOS, store.stored.deck0)
        assertEquals(BackDesign.NEGRO, store.stored.deck1)
    }

    @Test
    fun `seleccionar el reverso del mazo 2 actualiza y persiste sin tocar el mazo 1`() {
        val store = FakeStore()
        val vm = CardDesignViewModel(store)
        vm.selectDeck1Back(BackDesign.ANILLOS)
        assertEquals(BackDesign.ROJO, vm.skin.value.deck0)
        assertEquals(BackDesign.ANILLOS, vm.skin.value.deck1)
        assertEquals(BackDesign.ROJO, store.stored.deck0)
        assertEquals(BackDesign.ANILLOS, store.stored.deck1)
    }

    @Test
    fun `cambiar frontal y joker no altera los reversos`() {
        val vm = CardDesignViewModel(FakeStore(CardSkin(deck0 = BackDesign.ANILLOS, deck1 = BackDesign.ROMBOS)))
        vm.selectFront(FrontDesign.AZUL)
        vm.selectJoker(JokerStyle.MONO)
        assertEquals(BackDesign.ANILLOS, vm.skin.value.deck0)
        assertEquals(BackDesign.ROMBOS, vm.skin.value.deck1)
        assertEquals(FrontDesign.AZUL, vm.skin.value.front)
        assertEquals(JokerStyle.MONO, vm.skin.value.joker)
    }
}
