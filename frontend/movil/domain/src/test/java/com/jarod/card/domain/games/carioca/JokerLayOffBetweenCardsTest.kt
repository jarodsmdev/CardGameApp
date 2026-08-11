package com.jarod.card.domain.games.carioca

import com.jarod.card.domain.core.Card
import com.jarod.card.domain.core.JokerCard
import com.jarod.card.domain.core.JokerType
import com.jarod.card.domain.core.PlayingCard
import com.jarod.card.domain.core.Rank
import com.jarod.card.domain.core.Suit
import com.jarod.card.domain.engine.PlayerId
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regla de comodines en escalas (rules.md §5), versión resuelta:
 * "los comodines deben ir entre tres cartas". Tras la bajada se pueden agregar
 * comodines a una escala en la mesa (en un extremo, FRONT o BACK) sin máximo
 * fijo; el límite es que no queden juntos ni separados por menos de 3 cartas
 * naturales. Al bajarse sigue vigente el máximo de 1 comodín por escala.
 */
class JokerLayOffBetweenCardsTest {

    private val players = listOf(PlayerId("p1"), PlayerId("p2"), PlayerId("p3"), PlayerId("p4"))
    private val p1 = players[0]
    private val rules = CariocaRuleset()

    private fun card(suit: Suit, rank: Rank, set: Int = 0): PlayingCard =
        PlayingCard("$set:${suit.symbol}:${rank.symbol}", set, suit, rank)

    private fun joker(set: Int = 0): JokerCard =
        JokerCard("$set:JOKER:${JokerType.COLORED.symbol}", set, JokerType.COLORED)

    private fun club(vararg ranks: Rank): List<Card> = ranks.map { card(Suit.CLUB, it) }

    /** Estado con p1 ya bajado y en turno de hacer lay-off. */
    private fun layOffState(run: Meld.Run, hand: List<Card>): CariocaState {
        val base = CariocaGame.createGame(players, rules, 12345L).state
        return base.copy(
            currentPlayer = p1,
            stage = Stage.ACTIONS,
            table = base.table + (p1 to listOf(run)),
            meldedThisRound = setOf(p1),
            meldedThisTurn = emptySet(),
            hands = base.hands + (p1 to hand)
        )
    }

    // ── MeldValidator.validateLayOff (nivel directo) ─────────────────

    @Test
    fun `3-4-5-6 + joker FRONT queda Joker-3-4-5-6`() {
        val run = Meld.Run(club(Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX))
        val m = MeldValidator.validateLayOff(run, joker(), rules, RunSide.FRONT)
        assertNotNull("El joker puede añadirse a una escala sin joker", m)
        assertEquals(5, m!!.cards.size)
        assertTrue("Queda Joker-3-4-5-6", m.cards.first() is JokerCard)
        assertNotNull("La escala resultante es válida", MeldValidator.validateRun(m.cards, rules))
    }

    @Test
    fun `3-4-5-6 + joker BACK queda 3-4-5-6-Joker`() {
        val run = Meld.Run(club(Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX))
        val m = MeldValidator.validateLayOff(run, joker(), rules, RunSide.BACK)
        assertNotNull(m)
        assertEquals(5, m!!.cards.size)
        assertTrue("Queda 3-4-5-6-Joker", m.cards.last() is JokerCard)
    }

    @Test
    fun `4-5-6-Joker + joker queda Joker-4-5-6-Joker con 3 naturales entre`() {
        val run = Meld.Run(club(Rank.FOUR, Rank.FIVE, Rank.SIX) + listOf(joker()))
        val m = MeldValidator.validateLayOff(run, joker(), rules)
        assertNotNull("2 jokers separados por 3 naturales es válido", m)
        val cards = m!!.cards
        assertEquals(5, cards.size)
        assertEquals(2, cards.count { it is JokerCard })
        assertEquals(3, cards.filterIsInstance<PlayingCard>().size)
        assertTrue(cards.first() is JokerCard && cards.last() is JokerCard)
    }

    @Test
    fun `4-5-Joker-7-8 + joker rechazado - solo 2 naturales entre comodines`() {
        val run = Meld.Run(club(Rank.FOUR, Rank.FIVE) + listOf(joker()) + club(Rank.SEVEN, Rank.EIGHT))
        assertNull("Menos de 3 naturales entre comodines", MeldValidator.validateLayOff(run, joker(), rules))
    }

    @Test
    fun `escala de 13 cartas no acepta joker`() {
        val full = Rank.values().sortedBy { it.cycleIndex }.map { card(Suit.SPADE, it) }
        assertNull(MeldValidator.validateLayOff(Meld.Run(full), joker(), rules))
    }

    @Test
    fun `al bajarse sigue vigente el maximo de 1 joker por escala`() {
        val twoJokers = listOf(joker(set = 0)) + club(Rank.THREE, Rank.FOUR, Rank.FIVE) + listOf(joker(set = 1))
        assertNull("2 jokers al bajarse no es válido (maxPerMeld)", MeldValidator.validate(twoJokers, rules))
    }

    // ── A nivel de juego: flujos documentados ────────────────────────

    @Test
    fun `4-5-6-7 + joker a la derecha (=8) y luego 9 es válido`() {
        val run = Meld.Run(club(Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN))
        val j = joker()
        val st = layOffState(run, listOf(j, card(Suit.CLUB, Rank.NINE)))
        assertTrue("Joker a la derecha representa el 8", CariocaGame.canPerform(st, LayOffAction(p1, j.id, p1, 0, RunSide.BACK)).valid)
        val st2 = CariocaGame.perform(st, LayOffAction(p1, j.id, p1, 0, RunSide.BACK)).state
        val nine = card(Suit.CLUB, Rank.NINE)
        assertTrue("El 9 puede agregarse después del joker (4-5-6-7-Joker-9)", CariocaGame.canPerform(st2, LayOffAction(p1, nine.id, p1, 0)).valid)
    }

    @Test
    fun `4-5-6-7 + joker a la izquierda (=3) bloquea el 9`() {
        val run = Meld.Run(club(Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN))
        val j = joker()
        val st = layOffState(run, listOf(j, card(Suit.CLUB, Rank.NINE)))
        assertTrue("Joker a la izquierda representa el 3", CariocaGame.canPerform(st, LayOffAction(p1, j.id, p1, 0, RunSide.FRONT)).valid)
        val st2 = CariocaGame.perform(st, LayOffAction(p1, j.id, p1, 0, RunSide.FRONT)).state
        val nine = card(Suit.CLUB, Rank.NINE)
        assertFalse("Falta el 8: el 9 no puede ir tras Joker(=3)", CariocaGame.canPerform(st2, LayOffAction(p1, nine.id, p1, 0)).valid)
    }

    @Test
    fun `3-4-5-6 + comodines en ambos extremos queda Joker-3-4-5-6-Joker`() {
        val st0 = layOffState(Meld.Run(club(Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX)), listOf(joker(set = 0), joker(set = 1)))
        val j1 = joker(set = 0)
        val st = CariocaGame.perform(st0, LayOffAction(p1, j1.id, p1, 0, RunSide.FRONT)).state
        val j2 = joker(set = 1)
        assertTrue("Segundo joker en el extremo opuesto", CariocaGame.canPerform(st, LayOffAction(p1, j2.id, p1, 0, RunSide.BACK)).valid)
        val st2 = CariocaGame.perform(st, LayOffAction(p1, j2.id, p1, 0, RunSide.BACK)).state
        val runNow = st2.table[p1]!![0] as Meld.Run
        assertEquals(6, runNow.cards.size)
        assertEquals(2, runNow.cards.count { it is JokerCard })
        assertTrue(runNow.cards.first() is JokerCard && runNow.cards.last() is JokerCard)
    }

    @Test
    fun `findLayOff coloca el joker en el extremo que habilita el 9`() {
        val st = layOffState(Meld.Run(club(Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN)), listOf(joker(), card(Suit.CLUB, Rank.NINE)))
        val act = CariocaBot.findLayOff(st, p1)
        assertNotNull(act)
        assertEquals("Sugiere el Joker (el 9 aún no cabe)", joker().id, act!!.cardId)
        assertEquals("Al extremo derecho para que luego entre el 9", RunSide.BACK, act.position)
        assertTrue(CariocaGame.canPerform(st, act).valid)
    }
}
