package com.jarod.card.domain.games.carioca

import com.jarod.card.domain.core.Card
import com.jarod.card.domain.core.JokerCard
import com.jarod.card.domain.core.PlayingCard
import com.jarod.card.domain.core.Rank
import com.jarod.card.domain.core.Suit
import com.jarod.card.domain.engine.PlayerId
import org.junit.Assert.*
import org.junit.Test

class LayOffJokerEdgeCasesTest {

    private val players = listOf(PlayerId("p1"), PlayerId("p2"), PlayerId("p3"), PlayerId("p4"))

    private fun card(suit: Suit, rank: Rank, set: Int = 0): PlayingCard =
        PlayingCard("$set:${suit.symbol}:${rank.symbol}", set, suit, rank)

    private fun joker(set: Int = 0): JokerCard =
        JokerCard("$set:JOKER:${com.jarod.card.domain.core.JokerType.COLORED.symbol}", set, com.jarod.card.domain.core.JokerType.COLORED)

    @Test
    fun `lay-off a escala con joker al principio - 2 choca, A y 6 permitidos`() {
        val runRules = CariocaRuleset(rounds = listOf(CariocaRound(1, listOf(ComboSpec(ComboType.RUN, 1, minLength = 4)))))
        val res = CariocaGame.createGame(players, runRules, 12345L)
        var st = res.state
        val current = st.currentPlayer!!
        val other = players.first { it != current }

        st = CariocaGame.perform(st, DrawFromStock(current)).state
        val runCards = listOf(joker(), card(Suit.HEART, Rank.THREE), card(Suit.HEART, Rank.FOUR), card(Suit.HEART, Rank.FIVE))
        val hand = runCards + st.hands[current]!!.filter { it !in runCards }.take(9)
        var st2 = st.copy(hands = st.hands + (current to hand))
        st2 = CariocaGame.perform(st2, MeldAction(current, listOf(Meld.Run(runCards)))).state

        st2 = st2.copy(table = st2.table.plus(other to listOf(Meld.Triple(listOf(card(Suit.HEART, Rank.NINE), card(Suit.SPADE, Rank.NINE), card(Suit.CLUB, Rank.NINE))))), meldedThisRound = st2.meldedThisRound + other)

        val two = card(Suit.HEART, Rank.TWO)
        st2 = st2.copy(hands = st2.hands.plus(other to (st2.hands[other]!! + two)))
        assertFalse("El 2 debe chocar con el joker (que ya es 2)", CariocaGame.canPerform(st2, LayOffAction(other, two.id, current, 0)).valid)

        val ace = card(Suit.HEART, Rank.ACE)
        st2 = st2.copy(hands = st2.hands.plus(other to (st2.hands[other]!! + ace)))
        assertTrue("El A debe poder añadirse", CariocaGame.canPerform(st2, LayOffAction(other, ace.id, current, 0)).valid)

        val six = card(Suit.HEART, Rank.SIX)
        st2 = st2.copy(hands = st2.hands.plus(other to (st2.hands[other]!! + six)))
        assertTrue("El 6 debe poder añadirse", CariocaGame.canPerform(st2, LayOffAction(other, six.id, current, 0)).valid)
    }

    @Test
    fun `lay-off a escala con joker en medio - 3 choca, 1 y 6 permitidos`() {
        val runRules = CariocaRuleset(rounds = listOf(CariocaRound(1, listOf(ComboSpec(ComboType.RUN, 1, minLength = 4)))))
        val res = CariocaGame.createGame(players, runRules, 12345L)
        var st = res.state
        val current = st.currentPlayer!!
        val other = players.first { it != current }

        st = CariocaGame.perform(st, DrawFromStock(current)).state
        val runCards = listOf(card(Suit.HEART, Rank.TWO), joker(), card(Suit.HEART, Rank.FOUR), card(Suit.HEART, Rank.FIVE))
        val hand = runCards + st.hands[current]!!.filter { it !in runCards }.take(9)
        var st2 = st.copy(hands = st.hands + (current to hand))
        st2 = CariocaGame.perform(st2, MeldAction(current, listOf(Meld.Run(runCards)))).state

        st2 = st2.copy(table = st2.table.plus(other to listOf(Meld.Triple(listOf(card(Suit.HEART, Rank.NINE), card(Suit.SPADE, Rank.NINE), card(Suit.CLUB, Rank.NINE))))), meldedThisRound = st2.meldedThisRound + other)

        val three = card(Suit.HEART, Rank.THREE)
        st2 = st2.copy(hands = st2.hands.plus(other to (st2.hands[other]!! + three)))
        assertFalse("El 3 debe chocar con el joker (que ya es 3)", CariocaGame.canPerform(st2, LayOffAction(other, three.id, current, 0)).valid)

        val ace = card(Suit.HEART, Rank.ACE)
        st2 = st2.copy(hands = st2.hands.plus(other to (st2.hands[other]!! + ace)))
        assertTrue("El A debe poder añadirse (wraparound)", CariocaGame.canPerform(st2, LayOffAction(other, ace.id, current, 0)).valid)

        val six = card(Suit.HEART, Rank.SIX)
        st2 = st2.copy(hands = st2.hands.plus(other to (st2.hands[other]!! + six)))
        assertTrue("El 6 debe poder añadirse", CariocaGame.canPerform(st2, LayOffAction(other, six.id, current, 0)).valid)
    }

    @Test
    fun `lay-off a escala con joker al final - 5 choca, 6 permitido`() {
        val runRules = CariocaRuleset(rounds = listOf(CariocaRound(1, listOf(ComboSpec(ComboType.RUN, 1, minLength = 4)))))
        val res = CariocaGame.createGame(players, runRules, 12345L)
        var st = res.state
        val current = st.currentPlayer!!
        val other = players.first { it != current }

        st = CariocaGame.perform(st, DrawFromStock(current)).state
        val runCards = listOf(card(Suit.HEART, Rank.TWO), card(Suit.HEART, Rank.THREE), card(Suit.HEART, Rank.FOUR), joker())
        val hand = runCards + st.hands[current]!!.filter { it !in runCards }.take(9)
        var st2 = st.copy(hands = st.hands + (current to hand))
        st2 = CariocaGame.perform(st2, MeldAction(current, listOf(Meld.Run(runCards)))).state

        st2 = st2.copy(table = st2.table.plus(other to listOf(Meld.Triple(listOf(card(Suit.HEART, Rank.NINE), card(Suit.SPADE, Rank.NINE), card(Suit.CLUB, Rank.NINE))))), meldedThisRound = st2.meldedThisRound + other)

        val five = card(Suit.HEART, Rank.FIVE)
        st2 = st2.copy(hands = st2.hands.plus(other to (st2.hands[other]!! + five)))
        assertFalse("El 5 debe chocar con el joker (que ya es 5)", CariocaGame.canPerform(st2, LayOffAction(other, five.id, current, 0)).valid)

        val six = card(Suit.HEART, Rank.SIX)
        st2 = st2.copy(hands = st2.hands.plus(other to (st2.hands[other]!! + six)))
        assertTrue("El 6 debe poder añadirse", CariocaGame.canPerform(st2, LayOffAction(other, six.id, current, 0)).valid)
    }
}
