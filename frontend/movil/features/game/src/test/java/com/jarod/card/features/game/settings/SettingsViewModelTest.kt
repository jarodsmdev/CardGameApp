package com.jarod.card.features.game.settings

import com.jarod.card.core.theme.ThemePreference
import com.jarod.card.features.game.cardskin.BackDesign
import com.jarod.card.features.game.cardskin.CardSkin
import com.jarod.card.features.game.cardskin.CardSkinStore
import com.jarod.card.features.game.cardskin.FrontDesign
import com.jarod.card.features.game.cardskin.JokerStyle
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsViewModelTest {

    private class FakeSkinStore(var stored: CardSkin = CardSkin()) : CardSkinStore {
        override fun read(): CardSkin = stored
        override fun save(skin: CardSkin) {
            stored = skin
        }
    }

    private class FakeHandStore(var stored: DominantHand = DominantHand.RIGHT) : DominantHandStore {
        override fun read(): DominantHand = stored
        override fun save(hand: DominantHand) {
            stored = hand
        }
    }

    private class FakeThemeStore(
        var stored: ThemePreference = ThemePreference.SYSTEM
    ) : ThemePreferenceStore {
        private val flow = kotlinx.coroutines.flow.MutableStateFlow(stored)
        override val preference: kotlinx.coroutines.flow.StateFlow<ThemePreference> = flow

        override fun read(): ThemePreference = stored

        override fun save(preference: ThemePreference) {
            stored = preference
            flow.value = preference
        }
    }

    private class FakeMusicStore(var muted: Boolean = false) : MusicPreferenceStore {
        override fun isMuted(): Boolean = muted
        override fun saveMuted(muted: Boolean) {
            this.muted = muted
        }
    }

    private fun newVm(
        skinStore: FakeSkinStore = FakeSkinStore(),
        handStore: FakeHandStore = FakeHandStore(),
        themeStore: FakeThemeStore = FakeThemeStore(),
        musicStore: FakeMusicStore = FakeMusicStore()
    ) = SettingsViewModel(skinStore, handStore, themeStore, musicStore)

    @Test
    fun `el skin por defecto es mazo 1 rojo, mazo 2 negro, clásico y a color`() {
        val vm = newVm()
        assertEquals(CardSkin(), vm.skin.value)
        assertEquals(BackDesign.ROJO, vm.skin.value.deck0)
        assertEquals(BackDesign.NEGRO, vm.skin.value.deck1)
    }

    @Test
    fun `la mano dominante por defecto es derecha`() {
        val vm = newVm()
        assertEquals(DominantHand.RIGHT, vm.dominantHand.value)
    }

    @Test
    fun `cargar el skin guardado del almacén`() {
        val saved = CardSkin(
            deck0 = BackDesign.ROMBOS,
            deck1 = BackDesign.ANILLOS,
            front = FrontDesign.DORADO,
            joker = JokerStyle.ORO
        )
        val vm = newVm(skinStore = FakeSkinStore(saved))
        assertEquals(BackDesign.ROMBOS, vm.skin.value.deck0)
        assertEquals(BackDesign.ANILLOS, vm.skin.value.deck1)
        assertEquals(FrontDesign.DORADO, vm.skin.value.front)
        assertEquals(JokerStyle.ORO, vm.skin.value.joker)
    }

    @Test
    fun `cargar la mano dominante guardada del almacén`() {
        val vm = newVm(handStore = FakeHandStore(DominantHand.LEFT))
        assertEquals(DominantHand.LEFT, vm.dominantHand.value)
    }

    @Test
    fun `seleccionar el reverso del mazo 1 actualiza y persiste sin tocar el mazo 2`() {
        val store = FakeSkinStore()
        val vm = newVm(skinStore = store)
        vm.selectDeck0Back(BackDesign.ROMBOS)
        assertEquals(BackDesign.ROMBOS, vm.skin.value.deck0)
        assertEquals(BackDesign.NEGRO, vm.skin.value.deck1)
        assertEquals(BackDesign.ROMBOS, store.stored.deck0)
        assertEquals(BackDesign.NEGRO, store.stored.deck1)
    }

    @Test
    fun `seleccionar el reverso del mazo 2 actualiza y persiste sin tocar el mazo 1`() {
        val store = FakeSkinStore()
        val vm = newVm(skinStore = store)
        vm.selectDeck1Back(BackDesign.ANILLOS)
        assertEquals(BackDesign.ROJO, vm.skin.value.deck0)
        assertEquals(BackDesign.ANILLOS, vm.skin.value.deck1)
        assertEquals(BackDesign.ROJO, store.stored.deck0)
        assertEquals(BackDesign.ANILLOS, store.stored.deck1)
    }

    @Test
    fun `cambiar frontal y joker no altera los reversos`() {
        val vm = newVm(skinStore = FakeSkinStore(CardSkin(deck0 = BackDesign.ANILLOS, deck1 = BackDesign.ROMBOS)))
        vm.selectFront(FrontDesign.AZUL)
        vm.selectJoker(JokerStyle.MONO)
        assertEquals(BackDesign.ANILLOS, vm.skin.value.deck0)
        assertEquals(BackDesign.ROMBOS, vm.skin.value.deck1)
        assertEquals(FrontDesign.AZUL, vm.skin.value.front)
        assertEquals(JokerStyle.MONO, vm.skin.value.joker)
    }

    @Test
    fun `seleccionar la mano izquierda actualiza y persiste sin tocar el skin`() {
        val store = FakeHandStore()
        val vm = newVm(handStore = store)
        vm.selectDominantHand(DominantHand.LEFT)
        assertEquals(DominantHand.LEFT, vm.dominantHand.value)
        assertEquals(DominantHand.LEFT, store.stored)
        assertEquals(CardSkin(), vm.skin.value)
    }

    @Test
    fun `la preferencia de apariencia por defecto es sistema`() {
        val vm = newVm()
        assertEquals(ThemePreference.SYSTEM, vm.themePreference.value)
    }

    @Test
    fun `seleccionar apariencia oscura actualiza y persiste`() {
        val store = FakeThemeStore()
        val vm = newVm(themeStore = store)
        vm.selectThemePreference(ThemePreference.DARK)
        assertEquals(ThemePreference.DARK, vm.themePreference.value)
        assertEquals(ThemePreference.DARK, store.stored)
        assertEquals(ThemePreference.DARK, store.preference.value)
    }

    @Test
    fun `cargar la apariencia guardada del almacén`() {
        val vm = newVm(themeStore = FakeThemeStore(ThemePreference.LIGHT))
        assertEquals(ThemePreference.LIGHT, vm.themePreference.value)
    }

    @Test
    fun `la música al iniciar es on cuando el almacén no está silenciado`() {
        val vm = newVm(musicStore = FakeMusicStore(muted = false))
        assertEquals(true, vm.musicAtStart.value)
    }

    @Test
    fun `la música al iniciar es off cuando el almacén está silenciado`() {
        val vm = newVm(musicStore = FakeMusicStore(muted = true))
        assertEquals(false, vm.musicAtStart.value)
    }

    @Test
    fun `activar la música al iniciar persiste el estado no silenciado`() {
        val store = FakeMusicStore(muted = true)
        val vm = newVm(musicStore = store)
        vm.setMusicAtStart(true)
        assertEquals(true, vm.musicAtStart.value)
        assertEquals(false, store.muted)
    }

    @Test
    fun `desactivar la música al iniciar persiste el estado silenciado`() {
        val store = FakeMusicStore(muted = false)
        val vm = newVm(musicStore = store)
        vm.setMusicAtStart(false)
        assertEquals(false, vm.musicAtStart.value)
        assertEquals(true, store.muted)
    }
}
