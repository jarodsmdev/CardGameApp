package com.jarod.card.domain.games.carioca

import com.jarod.card.domain.core.Card
import com.jarod.card.domain.core.DeckFactory
import com.jarod.card.domain.core.JokerCard
import com.jarod.card.domain.core.JokerType
import com.jarod.card.domain.core.PlayingCard
import com.jarod.card.domain.core.Rank
import com.jarod.card.domain.core.Suit
import org.junit.Assert.*
import org.junit.Test

class MeldValidatorTest {

    private val rules = CariocaRuleset()

    private fun card(suit: Suit, rank: Rank, set: Int = 0): PlayingCard =
        PlayingCard("$set:${suit.symbol}:${rank.symbol}", set, suit, rank)

    private fun joker(set: Int = 0, type: JokerType = JokerType.COLORED): JokerCard =
        JokerCard("$set:JOKER:${type.symbol}", set, type)

    // ──────────────────────────────────────────────────────────────
    // Tríos
    // ──────────────────────────────────────────────────────────────

    @Test
    fun `trío válido, 3 cartas mismo rango`() {
        val m = MeldValidator.validateTriple(listOf(card(Suit.HEART, Rank.FIVE), card(Suit.SPADE, Rank.FIVE), card(Suit.DIAMOND, Rank.FIVE)))
        assertNotNull(m)
    }

    @Test
    fun `trío con joker, 2 mismo rango + 1 joker`() {
        val m = MeldValidator.validateTriple(listOf(card(Suit.HEART, Rank.FIVE), card(Suit.SPADE, Rank.FIVE), joker()))
        assertNotNull(m)
    }

    @Test
    fun `trío inválido, rangos distintos`() {
        val m = MeldValidator.validateTriple(listOf(card(Suit.HEART, Rank.FIVE), card(Suit.SPADE, Rank.SIX), card(Suit.DIAMOND, Rank.SEVEN)))
        assertNull(m)
    }

    @Test
    fun `trío inválido, solo 2 cartas`() {
        val m = MeldValidator.validateTriple(listOf(card(Suit.HEART, Rank.FIVE), card(Suit.SPADE, Rank.FIVE)))
        assertNull(m)
    }

    // ──────────────────────────────────────────────────────────────
    // Escalas
    // ──────────────────────────────────────────────────────────────

    @Test
    fun `escala válida, 4 consecutivas misma pinta (A-2-3-4)`() {
        val m = MeldValidator.validateRun(
            listOf(card(Suit.HEART, Rank.ACE), card(Suit.HEART, Rank.TWO), card(Suit.HEART, Rank.THREE), card(Suit.HEART, Rank.FOUR)),
            rules
        )
        assertNotNull(m)
    }

    @Test
    fun `escala con giro, Q-K-A-2 (vuelta en ciclo)`() {
        // Q(10), K(11), A(12), 2(0) → contiguo dando la vuelta → válido (2-A-K-Q)
        val m = MeldValidator.validateRun(
            listOf(card(Suit.HEART, Rank.QUEEN), card(Suit.HEART, Rank.KING), card(Suit.HEART, Rank.ACE), card(Suit.HEART, Rank.TWO)),
            rules
        )
        assertNotNull("Q-K-A-2 es contiguo con giro", m)
    }

    @Test
    fun `escala contigua, 9-10-J-Q`() {
        val m = MeldValidator.validateRun(
            listOf(card(Suit.HEART, Rank.NINE), card(Suit.HEART, Rank.TEN), card(Suit.HEART, Rank.JACK), card(Suit.HEART, Rank.QUEEN)),
            rules
        )
        assertNotNull(m)
    }

    @Test
    fun `escala devuelve cartas ordenadas aunque se pasen desordenadas`() {
        val m = MeldValidator.validateRun(
            listOf(card(Suit.HEART, Rank.TEN), card(Suit.HEART, Rank.SEVEN), card(Suit.HEART, Rank.NINE), card(Suit.HEART, Rank.EIGHT)),
            rules
        )
        assertEquals(listOf(Rank.SEVEN, Rank.EIGHT, Rank.NINE, Rank.TEN), m!!.cards.map { (it as PlayingCard).rank })
    }

    @Test
    fun `escala con giro Q-K-A-2 devuelve orden Q-K-A-2`() {
        val m = MeldValidator.validateRun(
            listOf(card(Suit.HEART, Rank.TWO), card(Suit.HEART, Rank.ACE), card(Suit.HEART, Rank.QUEEN), card(Suit.HEART, Rank.KING)),
            rules
        )
        assertEquals(listOf(Rank.QUEEN, Rank.KING, Rank.ACE, Rank.TWO), m!!.cards.map { (it as PlayingCard).rank })
    }

    @Test
    fun `escala con joker rellena hueco, 4-5-joker-7`() {
        val m = MeldValidator.validateRun(
            listOf(card(Suit.HEART, Rank.FOUR), card(Suit.HEART, Rank.FIVE), joker(), card(Suit.HEART, Rank.SEVEN)),
            rules
        )
        assertNotNull("joker rellena 6", m)
    }

    @Test
    fun `escala con joker coloca el joker en su hueco exacto`() {
        val j = joker()
        val m = MeldValidator.validateRun(
            listOf(card(Suit.HEART, Rank.SEVEN), card(Suit.HEART, Rank.FOUR), j, card(Suit.HEART, Rank.FIVE)),
            rules
        )
        assertEquals(listOf(Rank.FOUR, Rank.FIVE, Rank.SEVEN), m!!.cards.mapNotNull { (it as? PlayingCard)?.rank })
        assertEquals(j, m.cards[2])
    }

    @Test
    fun `escala tras lay-off, carta añadida a un extremo queda en orden`() {
        val run = Meld.Run(listOf(
            card(Suit.SPADE, Rank.SEVEN), card(Suit.SPADE, Rank.EIGHT),
            card(Suit.SPADE, Rank.NINE), card(Suit.SPADE, Rank.TEN)
        ))
        val added = card(Suit.SPADE, Rank.SIX)
        val m = MeldValidator.validate(run.cards + added, rules)
        assertEquals(listOf(Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE, Rank.TEN), (m as Meld.Run).cards.map { (it as PlayingCard).rank })
    }

    @Test
    fun `escala tras lay-off, joker añadido a un extremo queda en su hueco`() {
        val j = joker()
        val run = Meld.Run(listOf(
            card(Suit.SPADE, Rank.SEVEN), card(Suit.SPADE, Rank.EIGHT),
            card(Suit.SPADE, Rank.NINE), card(Suit.SPADE, Rank.TEN)
        ))
        val m = MeldValidator.validate(run.cards + j, rules)
        val mm = m as Meld.Run
        assertEquals(j, mm.cards.first())
        assertEquals(listOf(Rank.SEVEN, Rank.EIGHT, Rank.NINE, Rank.TEN), mm.cards.mapNotNull { (it as? PlayingCard)?.rank })
    }

    @Test
    fun `escala tras lay-off, real añadido reordena y joker existente se conserva`() {
        val j = joker()
        val run = Meld.Run(listOf(
            card(Suit.SPADE, Rank.SEVEN), j, card(Suit.SPADE, Rank.NINE), card(Suit.SPADE, Rank.TEN)
        ))
        val added = card(Suit.SPADE, Rank.EIGHT)
        val m = MeldValidator.validate(run.cards + added, rules)
        val mm = m as Meld.Run
        assertEquals(listOf(Rank.SEVEN, Rank.EIGHT, Rank.NINE, Rank.TEN), mm.cards.mapNotNull { (it as? PlayingCard)?.rank })
        assertEquals(j, mm.cards.first { it is JokerCard })
    }

    @Test
    fun `escala inválida, pinta distinta`() {
        val m = MeldValidator.validateRun(
            listOf(card(Suit.HEART, Rank.FOUR), card(Suit.SPADE, Rank.FIVE), card(Suit.HEART, Rank.SIX), card(Suit.HEART, Rank.SEVEN)),
            rules
        )
        assertNull(m)
    }

    @Test
    fun `escala inválida, rangos duplicados`() {
        val m = MeldValidator.validateRun(
            listOf(card(Suit.HEART, Rank.FOUR), card(Suit.HEART, Rank.FOUR), card(Suit.HEART, Rank.FIVE), card(Suit.HEART, Rank.SIX)),
            rules
        )
        assertNull(m)
    }

    @Test
    fun `escala larga 13 cartas (Escala Real) válida`() {
        val run = Rank.entries.map { card(Suit.HEART, it) }
        val m = MeldValidator.validateRun(run, rules)
        assertNotNull("Escala Real A-2-…-K válida", m)
    }

    // ──────────────────────────────────────────────────────────────
    // Cumplimiento de ronda
    // ──────────────────────────────────────────────────────────────

    @Test
    fun `ronda 1, 2 tríos`() {
        val r = defaultRounds[0]
        val m1 = Meld.Triple(listOf(card(Suit.HEART, Rank.FIVE), card(Suit.SPADE, Rank.FIVE), card(Suit.DIAMOND, Rank.FIVE)))
        val m2 = Meld.Triple(listOf(card(Suit.HEART, Rank.SIX), card(Suit.SPADE, Rank.SIX), card(Suit.DIAMOND, Rank.SIX)))
        assertTrue(MeldValidator.roundSatisfied(listOf(m1, m2), r))
        assertFalse(MeldValidator.roundSatisfied(listOf(m1), r))
    }

    @Test
    fun `ronda 2, 1 trío + 1 escala ≥4`() {
        val r = defaultRounds[1]
        val t = Meld.Triple(listOf(card(Suit.HEART, Rank.FIVE), card(Suit.SPADE, Rank.FIVE), card(Suit.DIAMOND, Rank.FIVE)))
        val e = Meld.Run(listOf(card(Suit.HEART, Rank.TWO), card(Suit.HEART, Rank.THREE), card(Suit.HEART, Rank.FOUR), card(Suit.HEART, Rank.FIVE)))
        assertTrue(MeldValidator.roundSatisfied(listOf(t, e), r))
    }

    @Test
    fun `ronda 9, 1 escala exacta 13`() {
        val r = defaultRounds[8]
        val e = Meld.Run(Rank.entries.map { card(Suit.HEART, it) })
        assertTrue(MeldValidator.roundSatisfied(listOf(e), r))
        val e2 = Meld.Run(Rank.entries.take(12).map { card(Suit.HEART, it) })
        assertFalse(MeldValidator.roundSatisfied(listOf(e2), r))
    }

    @Test
    fun `duplicado de tríos mismo rango → false`() {
        val m1 = Meld.Triple(listOf(card(Suit.HEART, Rank.FIVE), card(Suit.SPADE, Rank.FIVE), card(Suit.DIAMOND, Rank.FIVE)))
        val m2 = Meld.Triple(listOf(card(Suit.CLUB, Rank.FIVE), card(Suit.HEART, Rank.FIVE), card(Suit.SPADE, Rank.FIVE)))
        assertTrue(MeldValidator.hasDuplicateTripleRank(listOf(m1, m2)))
    }
}