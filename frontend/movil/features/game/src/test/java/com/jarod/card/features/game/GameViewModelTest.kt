package com.jarod.card.features.game

import com.jarod.card.core.util.DispatchersProvider
import com.jarod.card.domain.games.carioca.CariocaPhase
import com.jarod.card.domain.games.carioca.CariocaRound
import com.jarod.card.domain.games.carioca.CariocaRuleset
import com.jarod.card.domain.games.carioca.ComboSpec
import com.jarod.card.domain.games.carioca.ComboType
import com.jarod.card.domain.games.carioca.LayOffAction
import com.jarod.card.domain.games.carioca.Stage
import com.jarod.card.features.game.cardskin.BackDesign
import com.jarod.card.features.game.cardskin.CardSkin
import com.jarod.card.features.game.cardskin.CardSkinStore
import com.jarod.card.features.game.cardskin.FrontDesign
import com.jarod.card.features.game.cardskin.JokerStyle
import com.jarod.card.features.game.settings.DominantHand
import com.jarod.card.features.game.settings.DominantHandStore
import com.jarod.card.features.game.stats.CumulativeStats
import com.jarod.card.features.game.stats.GameStatsStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    private class FakeStatsStore(var stored: CumulativeStats = CumulativeStats()) : GameStatsStore {
        override fun read(): CumulativeStats = stored
        override fun add(played: CumulativeStats) {
            stored += played
        }
    }

    private class FakeHandStore(var stored: DominantHand = DominantHand.RIGHT) : DominantHandStore {
        override fun read(): DominantHand = stored
        override fun save(hand: DominantHand) {
            stored = hand
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

    private fun newViewModel(
        ruleset: CariocaRuleset = shortRules(),
        seed: Long = 999L,
        handStore: FakeHandStore = FakeHandStore()
    ): GameViewModel {
        val vm = GameViewModel(provider(), FakeSkinStore(), FakeStatsStore(), handStore)
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
        val vm = GameViewModel(provider(), FakeSkinStore(saved), FakeStatsStore(), FakeHandStore())
        vm.startGame(shortRules(), 999L)
        advance()
        assertEquals(saved, vm.uiState.value.skin)
    }

    @Test
    fun `startGame expone la mano dominante guardada del almacén (default derecha)`() {
        val right = newViewModel()
        assertEquals(DominantHand.RIGHT, right.uiState.value.dominantHand)

        val left = newViewModel(handStore = FakeHandStore(DominantHand.LEFT))
        assertEquals(DominantHand.LEFT, left.uiState.value.dominantHand)
    }

    @Test
    fun `al terminar la partida se generan stats y se acumulan en el almacén`() {
        val statsStore = FakeStatsStore()
        val vm = GameViewModel(provider(), FakeSkinStore(), statsStore, FakeHandStore())
        vm.startGame(shortRules(), 999L)
        advance()
        playUntilEnd(vm)
        val stats = vm.uiState.value.gameStats
        assertNotNull(stats)
        assertTrue(stats!!.totalTurns > 0)
        assertTrue(stats.totalLaps > 0)
        assertEquals(1, statsStore.stored.gamesPlayed)
        assertEquals(stats.rounds.size, statsStore.stored.roundsPlayed)
        assertEquals(stats.totalTurns, statsStore.stored.turns)
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
    fun `canDiscard es false para un joker y el descarte fallido deja la mano intacta`() {
        // Seed 4: la mano inicial del humano contiene un JOKER (0:JOKER:JOKER_PLAIN).
        val vm = newViewModel(seed = 4)
        advanceToHumanTurn(vm)
        val human = vm.uiState.value.humanId!!
        if (vm.uiState.value.state!!.stage == Stage.DRAW) vm.drawFromStock()

        val st = vm.uiState.value.state!!
        val joker = st.hands[human]!!.first { it is com.jarod.card.domain.core.JokerCard }
        val before = st.hands[human]!!.toList()

        assertFalse("El JOKER no se puede descartar (canDiscard)", vm.canDiscard(joker.id))
        vm.discard(joker.id)
        assertNotNull("Se muestra el error de descarte", vm.uiState.value.error)

        val after = vm.uiState.value.state!!.hands[human]!!
        assertEquals("El tamaño de la mano no cambia", before.size, after.size)
        assertEquals("La mano queda idéntica (sin hueco vacío)", before, after)
        assertTrue("El JOKER sigue en la mano", after.any { it.id == joker.id })
        assertEquals("El turno no cambia", human, vm.uiState.value.state!!.currentPlayer)
        assertEquals("Se sigue en ACTIONS", Stage.ACTIONS, vm.uiState.value.state!!.stage)
    }

    @Test
    fun `canDiscard es true para una carta normal y el descarte valido pasa el turno`() {
        val vm = newViewModel()
        advanceToHumanTurn(vm)
        val human = vm.uiState.value.humanId!!
        if (vm.uiState.value.state!!.stage == Stage.DRAW) vm.drawFromStock()

        val st = vm.uiState.value.state!!
        val card = st.hands[human]!!.first { it !is com.jarod.card.domain.core.JokerCard }

        assertTrue("Una carta normal sí se puede descartar", vm.canDiscard(card.id))
        vm.discard(card.id)
        assertTrue("El turno pasa al siguiente jugador", vm.uiState.value.state!!.currentPlayer != human)
    }

    @Test
    fun `tras descartar el juego queda pausado hasta que termina el pulso de llegada`() {
        val vm = newViewModel()
        advanceToHumanTurn(vm)
        val human = vm.uiState.value.humanId!!
        if (vm.uiState.value.state!!.stage == Stage.DRAW) vm.drawFromStock()
        advance()

        val hand = vm.uiState.value.state!!.hands[human]!!
        val card = hand.first { it !is com.jarod.card.domain.core.JokerCard }

        vm.discard(card.id)
        val afterDiscard = vm.uiState.value.state!!
        assertTrue("El turno pasa al siguiente jugador", afterDiscard.currentPlayer != human)
        val nextPlayer = afterDiscard.currentPlayer!!
        val nextHandSize = afterDiscard.hands[nextPlayer]!!.size

        // Antes de que termine el pulso de llegada el juego sigue pausado: el
        // siguiente bot no ha actuado todavía.
        mainRule.testDispatcher.scheduler.advanceTimeBy(ARRIVAL_PULSE_PAUSE_MS - 500L)
        val during = vm.uiState.value.state!!
        assertEquals("El siguiente jugador no cambia durante la pausa", nextPlayer, during.currentPlayer)
        assertEquals("El bot no roba ni descarta durante la pausa", nextHandSize, during.hands[nextPlayer]!!.size)
        assertFalse("El bot aún no está pensando", vm.uiState.value.botsThinking)

        // Al terminar el pulso el juego se reanuda.
        mainRule.testDispatcher.scheduler.advanceTimeBy(ARRIVAL_PULSE_PAUSE_MS + 500L)
        val resumed = vm.uiState.value.state!!
        assertTrue(
            "El juego avanzó tras el pulso",
            resumed.currentPlayer != nextPlayer || resumed.hands[nextPlayer]!!.size != nextHandSize
        )
    }

    @Test
    fun `proposeLayOff devuelve null fuera del turno del humano`() {
        val vm = newViewModel()
        assertNull(vm.proposeLayOff())
    }

    @Test
    fun `proposeLayOff devuelve null en fase de acciones sin lay-off posible`() {
        val vm = newViewModel()
        advanceToHumanTurn(vm)
        if (vm.uiState.value.state!!.stage == Stage.DRAW) vm.drawFromStock()
        advance()

        val st = vm.uiState.value.state!!
        assertEquals(Stage.ACTIONS, st.stage)
        assertTrue("Nadie se ha bajado aún, no hay lay-off", st.table.values.all { it.isEmpty() })
        assertNull(vm.proposeLayOff())
    }

    @Test
    fun `performLayOff con una accion invalida setea el error y no modifica el estado`() {
        val vm = newViewModel()
        advanceToHumanTurn(vm)
        val human = vm.uiState.value.humanId!!
        if (vm.uiState.value.state!!.stage == Stage.DRAW) vm.drawFromStock()

        val before = vm.uiState.value.state!!
        vm.performLayOff(LayOffAction(human, "carta-que-no-existe", meldOwner = human, meldIndex = 0))
        assertNotNull("Se muestra el error de lay-off", vm.uiState.value.error)

        val after = vm.uiState.value.state!!
        assertEquals("El tamaño de la mano no cambia", before.hands[human]!!.size, after.hands[human]!!.size)
        assertEquals("El turno no cambia", human, after.currentPlayer)
        assertEquals("La mesa no cambia", before.table, after.table)
    }

    @Test
    fun `partida completa via viewmodel llega a GAME_END`() {
        val vm = newViewModel()
        playUntilEnd(vm)
        assertEquals(CariocaPhase.GAME_END, vm.uiState.value.state!!.phase)
    }

    @Test
    fun `al terminar ronda, el juego queda pausado y solo avanza al continuar desde el scoreboard`() {
        val vm = newViewModel()
        var guard = 0
        // Jugar hasta que termine la primera ronda
        while (vm.uiState.value.roundEndInfo == null && guard < 2000) {
            val st = vm.uiState.value.state!!
            val human = vm.uiState.value.humanId!!
            if (st.phase == CariocaPhase.ROUND_END) {
                vm.clearRoundEnd()
            } else if (st.currentPlayer == human) {
                if (st.stage == Stage.DRAW) vm.drawFromStock()
                else {
                    val hand = st.hands[human]!!
                    val discardable = hand.firstOrNull { it !is com.jarod.card.domain.core.JokerCard } ?: hand.first()
                    vm.discard(discardable.id)
                }
            }
            advance()
            guard++
        }
        // Ronda 1 terminó: scoreboard visible, juego pausado en ROUND_END
        val info = vm.uiState.value.roundEndInfo
        assertNotNull(info)
        assertEquals(1, info!!.round)
        val pausedRound = vm.uiState.value.state!!
        assertEquals(CariocaPhase.ROUND_END, pausedRound.phase)
        val pausedIndex = pausedRound.roundIndex

        // Mientras el scoreboard está abierto, la siguiente ronda NO avanza
        advance()
        advance()
        val still = vm.uiState.value.state!!
        assertEquals(CariocaPhase.ROUND_END, still.phase)
        assertEquals(pausedIndex, still.roundIndex)
        assertNotNull("El scoreboard sigue visible", vm.uiState.value.roundEndInfo)

        // Al continuar desde el scoreboard recién comienza la siguiente ronda
        vm.clearRoundEnd()
        advance()
        val next = vm.uiState.value.state!!
        assertEquals(CariocaPhase.PLAYING, next.phase)
        assertEquals(pausedIndex + 1, next.roundIndex)
        assertEquals(rules().handSize, next.hands[vm.uiState.value.humanId!!]!!.size)
        assertTrue("Diálogo cerrado", vm.uiState.value.roundEndInfo == null)
    }

    private fun rules() = CariocaRuleset()

    private fun playUntilEnd(vm: GameViewModel) {
        var guard = 0
        while (vm.uiState.value.state?.phase != CariocaPhase.GAME_END && guard < 2000) {
            val st = vm.uiState.value.state!!
            val human = vm.uiState.value.humanId!!
            // Al terminar una ronda el juego queda pausado: continuar desde el scoreboard
            if (st.phase == CariocaPhase.ROUND_END) {
                vm.clearRoundEnd()
                advance()
                guard++
                continue
            }
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
