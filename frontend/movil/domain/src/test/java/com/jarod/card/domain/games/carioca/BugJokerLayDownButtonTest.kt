package com.jarod.card.domain.games.carioca

import com.jarod.card.domain.core.Card
import com.jarod.card.domain.core.JokerCard
import com.jarod.card.domain.core.JokerType
import com.jarod.card.domain.core.PlayingCard
import com.jarod.card.domain.core.Rank
import com.jarod.card.domain.core.Suit
import org.junit.Assert.*
import org.junit.Test

/**
 * Test de regresión
 * El botón «Bajarse» no se activaba a pesar de que la mano contenía una
 * combinación válida para el contrato de 2 tríos + 1 escala, cuando se
 * necesitaban Jokers tanto en un trío como en la escala.
 *
 * Causa raíz: generateCandidates() consumía los jokers de forma golosa
 * durante la generación de tríos, agotándolos antes de que las escalas
 * pudieran usarlos.
 */
class BugJokerLayDownButtonTest {

    private val rules = CariocaRuleset()
    private val round5 = defaultRounds[4] // Ronda 5: 2 tríos + 1 escala (≥4)

    private fun card(suit: Suit, rank: Rank, set: Int = 0): PlayingCard =
        PlayingCard("$set:${suit.symbol}:${rank.symbol}", set, suit, rank)

    private fun joker(id: String = "0:JOKER:JOKER_COLORED", set: Int = 0): JokerCard =
        JokerCard(id, set, JokerType.COLORED)

    /**
     * Mano exacta del caso real:
     *
     * Diamantes: A♦, JOKER, 3♦, 4♦, 6♦, 8♦, 9♦, 3♦
     * Tréboles:  9♣, 9♣
     * Corazones: Q♥
     * Picas:     Q♠
     * Jokers:    JOKER
     *
     * Solución válida:
     * - Trío 1: 9♣ – 9♣ – 9♦
     * - Trío 2: Q♥ – Q♠ – JOKER (joker = Q)
     * - Escala: A♦ – JOKER – 3♦ – 4♦ (joker = 2♦)
     */
    private fun bugHand(): List<Card> {
        val j1 = joker("0:JOKER:JOKER_COLORED")
        val j2 = joker("1:JOKER:JOKER_PLAIN", set = 1)
        return listOf(
            card(Suit.DIAMOND, Rank.ACE),          // A♦
            j1,                                     // JOKER #1
            card(Suit.DIAMOND, Rank.THREE),         // 3♦
            card(Suit.DIAMOND, Rank.FOUR),          // 4♦
            card(Suit.DIAMOND, Rank.SIX),           // 6♦
            card(Suit.DIAMOND, Rank.EIGHT),         // 8♦
            card(Suit.DIAMOND, Rank.NINE),          // 9♦
            card(Suit.CLUB, Rank.NINE),             // 9♣
            card(Suit.CLUB, Rank.NINE),             // 9♣
            card(Suit.HEART, Rank.QUEEN),           // Q♥
            j2,                                     // JOKER #2
            card(Suit.SPADE, Rank.QUEEN),           // Q♠
            card(Suit.DIAMOND, Rank.THREE)          // 3♦ (segunda)
        )
    }

    /**
     * Mano del turno siguiente: se agrega J♥.
     * La solución encontrada por el sistema fue:
     * - Trío: 9♣ 9♣ 9♦
     * - Trío: Q♥ Q♠ JOKER
     * - Escala: 3♦ 4♦ JOKER 6♦ (joker = 5♦)
     */
    private fun handWithJackOfHearts(): List<Card> =
        bugHand() + card(Suit.HEART, Rank.JACK)

    // ──────────────────────────────────────────────────────────────
    // Tests
    // ──────────────────────────────────────────────────────────────

    @Test
    fun `findMeldForRound retorna solucion valida para mano del bug sin JH`() {
        val hand = bugHand()
        val result = CariocaBot.findMeldForRound(hand, round5)

        assertNotNull(
            "La mano debe poder cumplir 2 tríos + 1 escala sin necesitar J♥",
            result
        )
    }

    @Test
    fun `solucion contiene exactamente 2 trios y 1 escala`() {
        val hand = bugHand()
        val result = CariocaBot.findMeldForRound(hand, round5)!!

        val trios = result.filterIsInstance<Meld.Triple>()
        val escalas = result.filterIsInstance<Meld.Run>()

        assertEquals("Debe haber 2 tríos", 2, trios.size)
        assertEquals("Debe haber 1 escala", 1, escalas.size)
    }

    @Test
    fun `trio de 9s usa solo cartas naturales`() {
        val hand = bugHand()
        val result = CariocaBot.findMeldForRound(hand, round5)!!

        val trios = result.filterIsInstance<Meld.Triple>()
        val trioNines = trios.find { t ->
            t.cards.filterIsInstance<PlayingCard>().all { it.rank == Rank.NINE }
        }

        assertNotNull("Debe existir un trío de 9s", trioNines)
        assertEquals("El trío de 9s tiene 3 cartas", 3, trioNines!!.cards.size)
        assertTrue("El trío de 9s no usa jokers",
            trioNines.cards.none { it is JokerCard })
    }

    @Test
    fun `trio de queens usa 1 joker`() {
        val hand = bugHand()
        val result = CariocaBot.findMeldForRound(hand, round5)!!

        val trios = result.filterIsInstance<Meld.Triple>()
        val trioQueens = trios.find { t ->
            val reals = t.cards.filterIsInstance<PlayingCard>()
            reals.all { it.rank == Rank.QUEEN } && reals.size == 2
        }

        assertNotNull("Debe existir un trío de Q con joker", trioQueens)
        assertEquals("El trío de Q tiene 3 cartas", 3, trioQueens!!.cards.size)
        assertEquals("El trío de Q usa 1 joker", 1,
            trioQueens.cards.count { it is JokerCard })
    }

    @Test
    fun `escala de diamantes usa 1 joker`() {
        val hand = bugHand()
        val result = CariocaBot.findMeldForRound(hand, round5)!!

        val escalas = result.filterIsInstance<Meld.Run>()
        val escalaDiamantes = escalas.find { e ->
            e.cards.filterIsInstance<PlayingCard>().all { it.suit == Suit.DIAMOND }
        }

        assertNotNull("Debe existir una escala de diamantes", escalaDiamantes)
        assertTrue("La escala tiene al menos 4 cartas",
            escalaDiamantes!!.cards.size >= 4)
        assertEquals("La escala usa 1 joker", 1,
            escalaDiamantes.cards.count { it is JokerCard })
    }

    @Test
    fun `todas las cartas usadas provienen de la mano`() {
        val hand = bugHand()
        val result = CariocaBot.findMeldForRound(hand, round5)!!

        val usedIds = result.flatMap { it.cardIds() }.toSet()
        val handIds = hand.map { it.id }.toSet()

        assertTrue(
            "Todas las cartas usadas deben estar en la mano",
            usedIds.all { it in handIds }
        )
        // 2 tríos (3+3) + 1 escala (4) = 9 cartas; la mano tiene 13
        assertEquals("Se usan 9 cartas de la mano", 9, usedIds.size)
    }

    @Test
    fun `mano con JH tambien encuentra solucion valida`() {
        val hand = handWithJackOfHearts()
        val result = CariocaBot.findMeldForRound(hand, round5)

        assertNotNull(
            "La mano con J♥ adicional también debe poder cumplir la ronda",
            result
        )

        val trios = result!!.filterIsInstance<Meld.Triple>()
        val escalas = result.filterIsInstance<Meld.Run>()

        assertEquals("2 tríos", 2, trios.size)
        assertEquals("1 escala", 1, escalas.size)
    }

    @Test
    fun `solucion es validada por MeldValidator`() {
        val hand = bugHand()
        val result = CariocaBot.findMeldForRound(hand, round5)!!

        // Cada grupo individual debe ser validado por MeldValidator
        for (meld in result) {
            val validated = MeldValidator.validate(meld.cards, rules)
            assertNotNull(
                "MeldValidator debe aceptar cada grupo: ${meld.cardIds()}",
                validated
            )
        }

        // La ronda debe quedar satisfecha
        assertTrue(
            "roundSatisfied debe retornar true",
            MeldValidator.roundSatisfied(result, round5)
        )
    }
}
