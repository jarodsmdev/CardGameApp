package com.jarod.card.features.lobby

import com.jarod.card.features.game.settings.GameSetup
import com.jarod.card.features.game.settings.GameSetupStore
import org.junit.Assert.assertEquals
import org.junit.Test

class PersonalizarJuegoViewModelTest {

    private class FakeSetupStore(var stored: GameSetup = GameSetup()) : GameSetupStore {
        override fun read(): GameSetup = stored
        override fun save(setup: GameSetup) {
            stored = setup
        }
    }

    private fun vm(store: FakeSetupStore = FakeSetupStore()) = PersonalizarJuegoViewModel(store)

    @Test
    fun `arranca con la configuracion guardada`() {
        val store = FakeSetupStore(GameSetup(players = 2, rounds = listOf(3, 5), cutBonusEnabled = true, initialRound = 5))
        val ui = vm(store).uiState.value
        assertEquals(2, ui.players)
        assertEquals(listOf(3, 5), ui.rounds)
        assertEquals(true, ui.cutBonusEnabled)
        assertEquals(3, ui.initialRound)
    }

    @Test
    fun `toggleRound agrega y quita rondas del catalogo`() {
        val v = vm()
        v.toggleRound(5)
        assertEquals("Se quita la ronda 5 de las 9", 8, v.uiState.value.rounds.size)
        assertEquals(false, 5 in v.uiState.value.rounds)
        v.toggleRound(5)
        assertEquals("Se vuelve a agregar", 9, v.uiState.value.rounds.size)
    }

    @Test
    fun `se puede dejar cero rondas y la ronda inicial vuelve a 1`() {
        val store = FakeSetupStore(GameSetup(rounds = listOf(1)))
        val v = vm(store)
        v.toggleRound(1)
        assertEquals(emptyList<Int>(), v.uiState.value.rounds)
        assertEquals(1, v.uiState.value.initialRound)
    }

    @Test
    fun `el orden de seleccion no altera la ronda inicial siempre la menor`() {
        val v = vm(FakeSetupStore(GameSetup(rounds = emptyList())))
        v.toggleRound(9)
        v.toggleRound(5)
        v.toggleRound(3)
        assertEquals(listOf(3, 5, 9), v.uiState.value.rounds)
        assertEquals(3, v.uiState.value.initialRound)
    }

    @Test
    fun `si la ronda inicial se deselecciona, pasa a la primera seleccionada`() {
        val store = FakeSetupStore(GameSetup(rounds = listOf(2, 3), initialRound = 2))
        val v = vm(store)
        v.toggleRound(2)
        assertEquals(listOf(3), v.uiState.value.rounds)
        assertEquals(3, v.uiState.value.initialRound)
    }

    @Test
    fun `setInitialRound solo acepta rondas seleccionadas`() {
        val store = FakeSetupStore(GameSetup(rounds = listOf(2, 4), initialRound = 2))
        val v = vm(store)
        v.setInitialRound(7)
        assertEquals(2, v.uiState.value.initialRound)
        v.setInitialRound(4)
        assertEquals(4, v.uiState.value.initialRound)
    }

    @Test
    fun `apply guarda la configuracion en el store`() {
        val store = FakeSetupStore()
        val v = vm(store)
        v.setPlayers(3)
        v.toggleRound(9)
        v.setCutBonusEnabled(true)
        v.setInitialRound(1)
        v.apply()
        assertEquals(
            GameSetup(players = 3, rounds = (1..8).toList(), cutBonusEnabled = true, initialRound = 1),
            store.stored
        )
    }

    @Test
    fun `setPlayers limita entre 2 y 4`() {
        val v = vm()
        v.setPlayers(6)
        assertEquals(4, v.uiState.value.players)
        v.setPlayers(1)
        assertEquals(2, v.uiState.value.players)
    }
}
