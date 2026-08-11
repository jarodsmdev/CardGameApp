package com.jarod.card.domain.games.carioca

import com.jarod.card.domain.core.JokerCard
import com.jarod.card.domain.core.JokerType
import com.jarod.card.domain.core.PlayingCard
import com.jarod.card.domain.core.Rank
import com.jarod.card.domain.core.Suit
import com.jarod.card.domain.engine.PlayerId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Bug TODO.md: el Joker no puede utilizarse como Lay-off cuando queda en la
 * mano junto a una carta descartable, pese a existir una combinación legal
 * donde agregarlo.
 *
 * Escenario: todos bajados; el jugador roba y queda con `2 + JOKER`. Debería
 * poder hacer lay-off del JOKER sobre un trío (`9 9 9 9 9 + JOKER`) y luego
 * descartar el `2`, ganando la ronda. El hecho de que el JOKER no se pueda
 * DESCARTAR no debe impedir que se pueda JUGAR como lay-off.
 */
class JokerLayOffToTripleTest {

    private val rules = CariocaRuleset()
    private val players = listOf(
        PlayerId("p1"), PlayerId("p2"), PlayerId("p3"), PlayerId("p4")
    )

    private fun card(suit: Suit, rank: Rank, set: Int): PlayingCard =
        PlayingCard("$set:${suit.symbol}:${rank.symbol}", set, suit, rank)

    private fun joker(set: Int): JokerCard =
        JokerCard("$set:JOKER:${JokerType.COLORED.symbol}", set, JokerType.COLORED)

    /** Estado con todos los jugadores bajados y p1 en ACTIONS con `2 + JOKER`. */
    private fun stateCon2yJokerEnManoDeP1(): CariocaState {
        val res = CariocaGame.createGame(players, rules, 12345L)
        var st = res.state
        val p1 = players[0]

        // Mesa: p1 tiene dos tríos (nueves y treses); el resto también está bajado.
        val triple9 = Meld.Triple(
            listOf(
                card(Suit.HEART, Rank.NINE, 0),
                card(Suit.SPADE, Rank.NINE, 0),
                card(Suit.DIAMOND, Rank.NINE, 0),
                card(Suit.CLUB, Rank.NINE, 0),
                card(Suit.HEART, Rank.NINE, 1)
            )
        )
        val triple3 = Meld.Triple(
            listOf(
                card(Suit.HEART, Rank.THREE, 1),
                card(Suit.SPADE, Rank.THREE, 1),
                card(Suit.DIAMOND, Rank.THREE, 1)
            )
        )
        val triple7 = Meld.Triple(
            listOf(
                card(Suit.HEART, Rank.SEVEN, 0),
                card(Suit.SPADE, Rank.SEVEN, 0),
                card(Suit.DIAMOND, Rank.SEVEN, 0)
            )
        )
        val tripleA = Meld.Triple(
            listOf(card(Suit.HEART, Rank.ACE, 0), card(Suit.SPADE, Rank.ACE, 0), card(Suit.DIAMOND, Rank.ACE, 0))
        )
        val tripleQ = Meld.Triple(
            listOf(card(Suit.HEART, Rank.QUEEN, 0), card(Suit.SPADE, Rank.QUEEN, 0), card(Suit.DIAMOND, Rank.QUEEN, 0))
        )

        // Mano de p1: `2 + JOKER` (las últimas cartas que le quedaban).
        val two = card(Suit.CLUB, Rank.TWO, 0)
        val jkr = joker(0)
        val hand = listOf(two, jkr)

        st = st.copy(
            hands = st.hands + (p1 to hand),
            table = mapOf(
                p1 to listOf(triple9, triple3),
                players[1] to listOf(triple7),
                players[2] to listOf(tripleA),
                players[3] to listOf(tripleQ)
            ),
            meldedThisRound = players.toSet(),
            meldedThisTurn = emptySet(),
            currentPlayer = p1,
            stage = Stage.ACTIONS
        )
        return st
    }

    @Test
    fun `el JOKER puede utilizarse como lay-off sobre un trio propio cuando queda en la mano`() {
        val st = stateCon2yJokerEnManoDeP1()
        val p1 = players[0]
        val jkr = st.hands[p1]!!.first { it is JokerCard }

        val res = CariocaGame.canPerform(st, LayOffAction(p1, jkr.id, meldOwner = p1, meldIndex = 0))
        assertTrue("El JOKER debe poder añadirse a 9 9 9 9 9", res.valid)
    }

    @Test
    fun `el JOKER puede utilizarse como lay-off sobre un trio ajeno`() {
        val st = stateCon2yJokerEnManoDeP1()
        val p1 = players[0]
        val jkr = st.hands[p1]!!.first { it is JokerCard }

        val res = CariocaGame.canPerform(st, LayOffAction(p1, jkr.id, meldOwner = players[1], meldIndex = 0))
        assertTrue("El JOKER debe poder añadirse a un trío ajeno (7 7 7)", res.valid)
    }

    @Test
    fun `el bot sugiere el JOKER como lay-off al quedar en la mano con una carta descartable`() {
        val st = stateCon2yJokerEnManoDeP1()
        val p1 = players[0]

        val suggested = CariocaBot.findLayOff(st, p1)
        assertNotNull("Debe existir un lay-off válido (el JOKER sobre un trío)", suggested)
        assertTrue(
            "El lay-off sugerido debe usar el JOKER, no la carta descartable",
            suggested!!.cardId == st.hands[p1]!!.first { it is JokerCard }.id
        )
    }

    @Test
    fun `lay-off del JOKER + descarte del 2 gana la ronda`() {
        var st = stateCon2yJokerEnManoDeP1()
        val p1 = players[0]
        val two = st.hands[p1]!!.first { it !is JokerCard }
        val jkr = st.hands[p1]!!.first { it is JokerCard }

        // 1. JOKER → trío de 9s
        st = CariocaGame.perform(st, LayOffAction(p1, jkr.id, meldOwner = p1, meldIndex = 0)).state
        assertEquals("Solo queda el 2 en la mano", listOf(two), st.hands[p1])
        val extended = st.table[p1]!![0]
        assertTrue("El JOKER queda integrado en el trío", extended.cardIds().contains(jkr.id))

        // 2. Descartar el 2 → mano vacía → fin de ronda
        assertTrue("El 2 se puede descartar", CariocaGame.canPerform(st, DiscardAction(p1, two.id)).valid)
        st = CariocaGame.perform(st, DiscardAction(p1, two.id)).state
        assertEquals(CariocaPhase.ROUND_END, st.phase)
        assertTrue("La mano de p1 queda vacía", st.hands[p1]!!.isEmpty())
        assertEquals("p1 gana la ronda", 1, st.roundsWon[p1])
    }

    @Test
    fun `el JOKER no puede descartarse pero eso no bloquea el lay-off`() {
        val st = stateCon2yJokerEnManoDeP1()
        val p1 = players[0]
        val jkr = st.hands[p1]!!.first { it is JokerCard }

        assertFalse("Descartar el JOKER sigue prohibido", CariocaGame.canPerform(st, DiscardAction(p1, jkr.id)).valid)
        assertTrue(
            "Aunque no se descarte, sí se puede jugar como lay-off",
            CariocaGame.canPerform(st, LayOffAction(p1, jkr.id, meldOwner = p1, meldIndex = 0)).valid
        )
    }
}
