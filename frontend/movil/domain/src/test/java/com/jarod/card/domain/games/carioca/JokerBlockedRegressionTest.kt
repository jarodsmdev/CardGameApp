package com.jarod.card.domain.games.carioca

import com.jarod.card.domain.core.Card
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
 * Regresión (TODO.md): jugador bloqueado en ronda 3/9 (2 escalas) con mano
 * {JOKER, 6♠, J♠}. El botón "añadir a mesa" se habilitaba pero el motor
 * rechazaba la jugada ("añadir la carta rompe la combinación o choca con un
 * comodín"), porque la heurística de la UI usaba una validación distinta de
 * la del motor.
 */
class JokerBlockedRegressionTest {

    private val players = listOf(PlayerId("p1"), PlayerId("p2"), PlayerId("p3"), PlayerId("p4"))
    private val p1 = players[0]

    private fun card(suit: Suit, rank: Rank, set: Int = 0): PlayingCard =
        PlayingCard("$set:${suit.symbol}:${rank.symbol}", set, suit, rank)

    private fun joker(set: Int = 0): JokerCard =
        JokerCard("$set:JOKER:${JokerType.COLORED.symbol}", set, JokerType.COLORED)

    private fun spades(vararg ranks: Rank): List<Card> =
        ranks.map { card(Suit.SPADE, it) }

    /**
     * Estado exacto del bug (TODO.md):
     *  - Ronda 3/9 (2 escalas).
     *  - p1 es el único que se bajó: escala1 = 8♠ 9♠ 10♠ J♠,
     *    escala2 = Q♠ K♠ A♠ 2♠ 3♠ 4♠ (Q-K-A-2 ya melded + lay-off 3♠ 4♠).
     *  - Mano de p1 = JOKER, 6♠, J♠.
     *  - Turno de p1 en Acciones, en un turno posterior al de bajarse.
     */
    private fun blockedScenarioState(): CariocaState {
        val base = CariocaGame.createGame(players, CariocaRuleset(), 12345L).state
        val run1 = Meld.Run(spades(Rank.EIGHT, Rank.NINE, Rank.TEN, Rank.JACK))
        val run2 = Meld.Run(spades(Rank.QUEEN, Rank.KING, Rank.ACE, Rank.TWO, Rank.THREE, Rank.FOUR))
        return base.copy(
            roundIndex = 2,
            currentPlayer = p1,
            stage = Stage.ACTIONS,
            table = base.table + (p1 to listOf(run1, run2)),
            meldedThisRound = setOf(p1),
            meldedThisTurn = emptySet(),
            hands = base.hands + (p1 to listOf(joker(), card(Suit.SPADE, Rank.SIX), card(Suit.SPADE, Rank.JACK)))
        )
    }

    // ─────────────────────────────────────────────────────────────
    // Regresión del bug: el lay-off que sugiere la UI debe ser válido
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `regresion - el lay-off sugerido por la UI es aceptado por el motor`() {
        val st = blockedScenarioState()
        val suggested = CariocaBot.findLayOff(st, p1)
        assertNotNull("Debe existir un lay-off válido (J♠ sobre la escala 2)", suggested)
        val vr = CariocaGame.canPerform(st, suggested!!)
        assertTrue(
            "Botón activo pero jugada rechazada: '${vr.reason}'. " +
                "findLayOff y validateLayOff deben validar igual.",
            vr.valid
        )
    }

    @Test
    fun `regresion - el lay-off sugerido es el comodin a la escala 2 por detras`() {
        val st = blockedScenarioState()
        val suggested = CariocaBot.findLayOff(st, p1)!!
        // Con la regla §5 actualizada el JOKER puede ir a un extremo de una escala
        // sin joker. La heurística prefiere el JOKER sobre la escala 2 (Q-K-A-2-3-4)
        // por el lado alto (=5): deja jugables las dos cartas restantes (6♠ y J♠),
        // mejor que J♠ (que solo habilita al JOKER). Lo importante del bug original
        // es que la jugada sugerida sea aceptada por el motor (test anterior).
        assertEquals("Sugiere el JOKER", joker().id, suggested.cardId)
        assertEquals("A la escala 2 (índice 1)", 1, suggested.meldIndex)
        assertEquals("Al extremo alto", RunSide.BACK, suggested.position)
        assertTrue(CariocaGame.canPerform(st, suggested).valid)
    }

    // ─────────────────────────────────────────────────────────────
    // Tests separadores de causas
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `test1 - Q K A 2 3 4 es una escala valida`() {
        val run = MeldValidator.validateRun(
            spades(Rank.QUEEN, Rank.KING, Rank.ACE, Rank.TWO, Rank.THREE, Rank.FOUR),
            CariocaRuleset()
        )
        assertNotNull("Q-K-A-2-3-4 debe ser escala válida (wraparound)", run)
    }

    @Test
    fun `test2 - lay-off 3 4 de picas sobre Q K A 2 mantiene la escala valida`() {
        val rules = CariocaRuleset(rounds = listOf(CariocaRound(3, listOf(ComboSpec(ComboType.RUN, 2, minLength = 4)))))
        val base = CariocaGame.createGame(players, rules, 12345L).state
        val run2 = Meld.Run(spades(Rank.QUEEN, Rank.KING, Rank.ACE, Rank.TWO))
        var st = base.copy(
            currentPlayer = p1,
            stage = Stage.ACTIONS,
            table = base.table + (p1 to listOf(run2)),
            meldedThisRound = setOf(p1),
            meldedThisTurn = emptySet(),
            hands = base.hands + (p1 to listOf(
                card(Suit.SPADE, Rank.THREE), card(Suit.SPADE, Rank.FOUR),
                card(Suit.SPADE, Rank.SIX), card(Suit.SPADE, Rank.JACK), joker()
            ))
        )
        val three = card(Suit.SPADE, Rank.THREE)
        val four = card(Suit.SPADE, Rank.FOUR)
        assertTrue(CariocaGame.canPerform(st, LayOffAction(p1, three.id, p1, 0)).valid)
        st = CariocaGame.perform(st, LayOffAction(p1, three.id, p1, 0)).state
        assertTrue(CariocaGame.canPerform(st, LayOffAction(p1, four.id, p1, 0)).valid)
        st = CariocaGame.perform(st, LayOffAction(p1, four.id, p1, 0)).state

        val newRun = st.table[p1]!![0] as Meld.Run
        assertEquals("La escala queda Q K A 2 3 4 (6 cartas)", 6, newRun.cards.size)
        assertNotNull("La escala resultante debe seguir siendo válida", MeldValidator.validateRun(newRun.cards, rules))
        assertEquals("Aún quedan cartas (6♠, J♠, JOKER), la ronda no termina", 3, st.hands[p1]!!.size)
    }

    @Test
    fun `test3 - un joker puede añadirse a una escala sin joker`() {
        val st = blockedScenarioState()
        val j = joker()
        // Regla actualizada (rules.md §5): sin tope por separación. Como ninguna de
        // las dos escalas de la mesa tiene joker, el JOKER puede agregarse por lay-off.
        assertTrue("El JOKER se puede añadir a la escala 1 (8-9-10-J)", CariocaGame.canPerform(st, LayOffAction(p1, j.id, p1, 0)).valid)
        assertTrue("El JOKER se puede añadir a la escala 2 (Q-K-A-2-3-4)", CariocaGame.canPerform(st, LayOffAction(p1, j.id, p1, 1)).valid)
    }

    @Test
    fun `test4 - el joker no se puede descartar por regla pero el jugador tiene salida`() {
        val st = blockedScenarioState()
        // Regla documentada (rules.md §5/§7, jokerRules.cannotDiscard): no se descartan jokers.
        assertFalse("El JOKER no se descarta", CariocaGame.canPerform(st, DiscardAction(p1, joker().id)).valid)
        // No bloqueo: el jugador puede descartar 6♠ para terminar el turno.
        assertTrue("6♠ sí se puede descartar", CariocaGame.canPerform(st, DiscardAction(p1, card(Suit.SPADE, Rank.SIX).id)).valid)
    }

    @Test
    fun `test5 - 6 de picas no puede añadirse a ninguna escala`() {
        val st = blockedScenarioState()
        val six = card(Suit.SPADE, Rank.SIX)
        assertFalse("6♠ no extiende 8-9-10-J", CariocaGame.canPerform(st, LayOffAction(p1, six.id, p1, 0)).valid)
        assertFalse("6♠ no extiende Q-K-A-2-3-4", CariocaGame.canPerform(st, LayOffAction(p1, six.id, p1, 1)).valid)
    }

    @Test
    fun `test6 - J de picas se añade a la segunda escala pero no a la primera`() {
        val st = blockedScenarioState()
        val jack = card(Suit.SPADE, Rank.JACK)
        assertFalse("J♠ ya está en la escala 1", CariocaGame.canPerform(st, LayOffAction(p1, jack.id, p1, 0)).valid)
        assertTrue("J♠ extiende Q-K-A-2-3-4 por el lado bajo (wraparound)", CariocaGame.canPerform(st, LayOffAction(p1, jack.id, p1, 1)).valid)
    }
}
