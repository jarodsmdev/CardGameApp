package com.jarod.card.features.game

import com.jarod.card.core.util.DispatchersProvider
import com.jarod.card.domain.games.carioca.CariocaPhase
import com.jarod.card.domain.games.carioca.CariocaRound
import com.jarod.card.domain.games.carioca.CariocaRuleset
import com.jarod.card.domain.games.carioca.ComboSpec
import com.jarod.card.domain.games.carioca.ComboType
import com.jarod.card.domain.games.carioca.Stage
import com.jarod.card.features.game.cardskin.BackDesign
import com.jarod.card.features.game.cardskin.CardSkin
import com.jarod.card.features.game.cardskin.CardSkinStore
import com.jarod.card.features.game.cardskin.FrontDesign
import com.jarod.card.features.game.cardskin.JokerStyle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private class FakeSkinStore(var stored: CardSkin = CardSkin()) : CardSkinStore {
        override fun read(): CardSkin = stored
        override fun save(skin: CardSkin) {
            stored = skin
        }
    }

    private fun provider(): DispatchersProvider =
        DispatchersProvider(main = mainRule.testDispatcher, default = mainRule.testDispatcher, io = mainRule.testDispatcher)

    private fun shortRules() = CariocaRuleset(
        rounds = listOf(
            CariocaRound(1, listOf(ComboSpec(ComboType.TRIPLE, 2))),
            CariocaRound(2, listOf(ComboSpec(ComboType.TRIPLE, 2)))
        )
    )

    private fun newViewModel(ruleset: CariocaRuleset = shortRules(), seed: Long = 999L): GameViewModel {
        val vm = GameViewModel(provider(), FakeSkinStore())
        vm.startGame(ruleset, seed)
        mainRule.testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    private fun advance() {
        mainRule.testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `startGame crea partida de 4 jugadores repartiendo 12 cartas`() {
        val vm = newViewModel()
        val st = vm.uiState.value.state!!
        assertEquals(4, st.players.size)
        assertEquals(CariocaPhase.PLAYING, st.phase)
        assertEquals(12, st.hands[vm.uiState.value.humanId!!]!!.size)
        assertNotNull(st.currentPlayer)
    }

    @Test
    fun `startGame conserva el skin guardado del almacén`() {
        val saved = CardSkin(
            deck0 = BackDesign.ROMBOS,
            deck1 = BackDesign.ANILLOS,
            front = FrontDesign.DORADO,
            joker = JokerStyle.ORO
        )
        val vm = GameViewModel(provider(), FakeSkinStore(saved))
        vm.startGame(shortRules(), 999L)
        advance()
        assertEquals(saved, vm.uiState.value.skin)
    }

    @Test
    fun `robar del mazo pasa a fase de acciones y descartar pasa el turno`() {
        val vm = newViewModel()
        val human = vm.uiState.value.humanId!!
        advanceToHumanTurn(vm)

        vm.drawFromStock()
        assertEquals(Stage.ACTIONS, vm.uiState.value.state!!.stage)
        assertEquals(13, vm.uiState.value.state!!.hands[human]!!.size)

        val hand = vm.uiState.value.state!!.hands[human]!!
        val discardable = hand.firstOrNull { it !is com.jarod.card.domain.core.JokerCard } ?: hand.first()
        vm.discard(discardable.id)
        assertTrue(vm.uiState.value.state!!.currentPlayer != human)
        advance()
        val st = vm.uiState.value.state!!
        if (st.phase == CariocaPhase.PLAYING) {
            assertEquals(Stage.DRAW, st.stage)
        }
    }

    @Test
    fun `descartar una carta ajena setea el error`() {
        val vm = newViewModel()
        advanceToHumanTurn(vm)

        vm.discard("carta-que-no-existe")
        assertNotNull(vm.uiState.value.error)
    }

    @Test
    fun `partida completa via viewmodel llega a GAME_END`() {
        val vm = newViewModel()
        var guard = 0
        while (vm.uiState.value.state?.phase != CariocaPhase.GAME_END && guard < 2000) {
            val st = vm.uiState.value.state!!
            val human = vm.uiState.value.humanId!!
            if (st.currentPlayer == human) {
                if (st.stage == Stage.DRAW) {
                    vm.drawFromStock()
                } else {
                    val hand = st.hands[human]!!
                    val discardable = hand.firstOrNull { it !is com.jarod.card.domain.core.JokerCard } ?: hand.first()
                    vm.discard(discardable.id)
                }
            }
            advance()
            guard++
        }
        assertEquals(CariocaPhase.GAME_END, vm.uiState.value.state!!.phase)
    }

    private fun advanceToHumanTurn(vm: GameViewModel) {
        var guard = 0
        while (vm.uiState.value.state?.currentPlayer != vm.uiState.value.humanId
            && vm.uiState.value.state?.phase != CariocaPhase.GAME_END
            && guard < 50
        ) {
            advance()
            guard++
        }
    }
}
