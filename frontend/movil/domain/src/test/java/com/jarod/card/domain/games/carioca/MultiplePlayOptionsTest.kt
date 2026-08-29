package com.jarod.card.domain.games.carioca

import com.jarod.card.domain.core.JokerCard
import com.jarod.card.domain.core.JokerType
import com.jarod.card.domain.core.PlayingCard
import com.jarod.card.domain.core.Rank
import com.jarod.card.domain.core.Suit
import com.jarod.card.domain.engine.PlayerId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifica la mecánica de "opciones de jugada": cuando el motor encuentra MÁS DE
 * UNA forma válida de jugar (bajarse con varias agrupaciones, o varios lay-offs),
 * expone todas las opciones para que el jugador elija en lugar de usar una sola.
 */
class MultiplePlayOptionsTest {

    private val rules = CariocaRuleset()
    private val players = listOf(
        PlayerId("p1"), PlayerId("p2"), PlayerId("p3"), PlayerId("p4")
    )

    private fun card(suit: Suit, rank: Rank, set: Int = 0): PlayingCard =
        PlayingCard("$set:${suit.symbol}:${rank.symbol}", set, suit, rank)

    private fun joker(set: Int = 0): JokerCard =
        JokerCard("$set:JOKER:${JokerType.COLORED.symbol}", set, JokerType.COLORED)

    // ─────────────────────────────────────────────────────────────
    // Bajarse con varias agrupaciones posibles (findAllMeldForRound)
    // ─────────────────────────────────────────────────────────────

    /**
     * Estado: ronda de 1 trío + 1 escala; el jugador tiene un trío natural y DOS
     * escalas de 4 cartas. Solo debe jugar una escala (la otra queda en la mano),
     * por lo que hay DOS agrupaciones alternativas para bajarse: usar la escala
     * de corazones o la de picas. Es una elección genuina para el selector.
     */
    @Test
    fun `con dos escalas posibles se bajan agrupaciones alternativas`() {
        val round = CariocaRound(
            number = 2,
            combos = listOf(
                ComboSpec(ComboType.TRIPLE, count = 1),
                ComboSpec(ComboType.RUN, count = 1)
            )
        )
        val hand = listOf(
            card(Suit.HEART, Rank.ACE),
            card(Suit.SPADE, Rank.ACE),
            card(Suit.CLUB, Rank.ACE),
            card(Suit.HEART, Rank.FIVE),
            card(Suit.HEART, Rank.SIX),
            card(Suit.HEART, Rank.SEVEN),
            card(Suit.HEART, Rank.EIGHT),
            card(Suit.SPADE, Rank.FIVE),
            card(Suit.SPADE, Rank.SIX),
            card(Suit.SPADE, Rank.SEVEN),
            card(Suit.SPADE, Rank.EIGHT)
        )

        val options = CariocaBot.findAllMeldForRound(hand, round)

        // Debe haber una agrupación por cada escala posible.
        val conCorazones = options.any { groups -> groups.any { it is Meld.Run && it.cardIds().any { id -> id.startsWith("0:H:") } } }
        val conPicas = options.any { groups -> groups.any { it is Meld.Run && it.cardIds().any { id -> id.startsWith("0:S:") } } }

        assertTrue("Debe existir una agrupación usando la escala de corazones", conCorazones)
        assertTrue("Debe existir una agrupación usando la escala de picas", conPicas)
        assertTrue("En total hay al menos dos agrupaciones alternativas (${options.size})", options.size > 1)
    }

    /** Con una sola forma de bajarse, findAllMeldForRound devuelve una sola solución. */
    @Test
    fun `con una unica forma de bajarse hay una sola agrupacion`() {
        val round = CariocaRound(
            number = 1,
            combos = listOf(ComboSpec(ComboType.TRIPLE, count = 1))
        )
        val hand = listOf(
            card(Suit.HEART, Rank.ACE),
            card(Suit.SPADE, Rank.ACE),
            card(Suit.CLUB, Rank.ACE),
            card(Suit.HEART, Rank.SEVEN)
        )

        val options = CariocaBot.findAllMeldForRound(hand, round)
        assertEquals(1, options.size)
        assertEquals(3, options.single().sumOf { it.cards.size })
    }

    // ─────────────────────────────────────────────────────────────
    // Varios lay-offs posibles (findAllLayOffs)
    // ─────────────────────────────────────────────────────────────

    /**
     * Estado: jugador ya bajado con UN trío de 9s dentro de un rango de tríos
     * (9 9 9 9) en la mesa propia y otro trío de treses ajeno. Mano = {JOKER}.
     * El joker puede ir a cualquiera de los dos extremos/tríos → hay varias
     * opciones de lay-off.
     */
    @Test
    fun `con varias combinaciones en la mesa hay varios lay-offs posibles`() {
        val st = stateConJokerAmbiguaEnMano()
        val p1 = players[0]

        val options = CariocaBot.findAllLayOffs(st, p1)

        assertTrue("Debe haber más de un lay-off posible (${options.size})", options.size > 1)
        // Todas deben ser válidas en el motor
        options.forEach { assertTrue(CariocaGame.canPerform(st, it).valid) }
        // Todas usan el JOKER
        options.forEach { assertEquals(joker().id, it.cardId) }
    }

    @Test
    fun `con una sola combinacion solo hay un lay-off`() {
        var st = stateConJokerAmbiguaEnMano()
        val p1 = players[0]
        // Dejar solo la mesa propia
        val jkr = st.hands[p1]!!.first { it is JokerCard }
        val table = st.table.filterKeys { it == p1 }
        st = st.copy(table = table)

        val options = CariocaBot.findAllLayOffs(st, p1)
        assertEquals(1, options.size)
        assertEquals(jkr.id, options.single().cardId)
    }

    /** Estado: p1 bajado (dos tríos en mesa propia), con un JOKER en la mano. */
    private fun stateConJokerAmbiguaEnMano(): CariocaState {
        val res = CariocaGame.createGame(players, rules, 54321L)
        var st = res.state
        val p1 = players[0]

        val triple9 = Meld.Triple(
            listOf(
                card(Suit.HEART, Rank.NINE), card(Suit.SPADE, Rank.NINE),
                card(Suit.DIAMOND, Rank.NINE), card(Suit.CLUB, Rank.NINE)
            )
        )
        val triple3 = Meld.Triple(
            listOf(
                card(Suit.HEART, Rank.THREE, 1), card(Suit.SPADE, Rank.THREE, 1),
                card(Suit.DIAMOND, Rank.THREE, 1)
            )
        )

        st = st.copy(
            hands = st.hands + (p1 to listOf(joker())),
            table = mapOf(
                p1 to listOf(triple9),
                players[1] to listOf(triple3)
            ),
            meldedThisRound = setOf(p1, players[1]),
            meldedThisTurn = emptySet(),
            currentPlayer = p1,
            stage = Stage.ACTIONS
        )
        return st
    }
}
