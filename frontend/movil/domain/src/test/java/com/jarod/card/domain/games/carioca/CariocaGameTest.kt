package com.jarod.card.domain.games.carioca

import com.jarod.card.domain.core.Card
import com.jarod.card.domain.core.DeckFactory
import com.jarod.card.domain.core.JokerCard
import com.jarod.card.domain.core.PlayingCard
import com.jarod.card.domain.core.Rank
import com.jarod.card.domain.core.Suit
import com.jarod.card.domain.engine.PlayerId
import com.jarod.card.domain.engine.ValidationResult
import org.junit.Assert.*
import org.junit.Test

class CariocaGameTest {

    private val rules = CariocaRuleset()
    private val players = listOf(
        PlayerId("p1"), PlayerId("p2"), PlayerId("p3"), PlayerId("p4")
    )

    private fun card(suit: Suit, rank: Rank, set: Int = 0): PlayingCard =
        PlayingCard("$set:${suit.symbol}:${rank.symbol}", set, suit, rank)

    private fun joker(set: Int = 0): JokerCard =
        JokerCard("$set:JOKER:${com.jarod.card.domain.core.JokerType.COLORED.symbol}", set, com.jarod.card.domain.core.JokerType.COLORED)

    @Test
    fun `crear partida, 4 jugadores, 12 cartas cada uno, stock 60, pozo 1`() {
        val res = CariocaGame.createGame(players, rules, 12345L)
        val st = res.state
        assertEquals(CariocaPhase.PLAYING, st.phase)
        assertEquals(0, st.roundIndex)
        players.forEach { p ->
            assertEquals(12, st.hands[p]!!.size)
        }
        assertEquals(59, st.stock.size) // 108 - 48 - 1 (pozo)
        assertEquals(1, st.discard.size)
        assertNotNull(st.currentPlayer)
        assertEquals(Stage.DRAW, st.stage)
    }

    @Test
    fun `robar del mazo, aumenta mano a 13, pasa a ACTIONS`() {
        var res = CariocaGame.createGame(players, rules, 12345L)
        var st = res.state
        val current = st.currentPlayer!!
        val draw = CariocaGame.perform(st, DrawFromStock(current))
        st = draw.state
        assertEquals(13, st.hands[current]!!.size)
        assertEquals(Stage.ACTIONS, st.stage)
        assertEquals(58, st.stock.size)
    }

    @Test
    fun `no robar fuera de turno`() {
        var res = CariocaGame.createGame(players, rules, 12345L)
        val st = res.state
        val current = st.currentPlayer!!
        val other = players.first { it != current }
        val res2 = CariocaGame.canPerform(st, DrawFromStock(other))
        assertFalse(res2.valid)
    }

    @Test
    fun `bajarse cumple ronda 1 (2 tríos) y pasa turno si mano no vacía`() {
        var res = CariocaGame.createGame(players, rules, 12345L)
        var st = res.state
        val current = st.currentPlayer!!
        // Robar
        st = CariocaGame.perform(st, DrawFromStock(current)).state
        // Forzar 2 tríos en mano: buscar 6 cartas del mismo rango (improbable con seed fijo)
        // Test manual: inyectar mano con 2 tríos válidos
        val hand = mutableListOf<Card>()
        hand.addAll(listOf(card(Suit.HEART, Rank.FIVE), card(Suit.SPADE, Rank.FIVE), card(Suit.DIAMOND, Rank.FIVE)))
        hand.addAll(listOf(card(Suit.HEART, Rank.SIX), card(Suit.SPADE, Rank.SIX), card(Suit.DIAMOND, Rank.SIX)))
        // Rellenar hasta 13 con basura
        val filler = st.hands[current]!!.filter { it !in hand }.take(7)
        hand.addAll(filler)
        st = st.copy(hands = st.hands + (current to hand))
        // Meld
        val t1 = Meld.Triple(hand.take(3))
        val t2 = Meld.Triple(hand.drop(3).take(3))
        st = CariocaGame.perform(st, MeldAction(current, listOf(t1, t2))).state
        assertTrue(st.hands[current]!!.size <= 7) // se quitaron 6
        assertTrue(current in st.meldedThisRound)
        assertEquals(Stage.ACTIONS, st.stage) // aún debe descartar
    }

    @Test
    fun `no se puede bajar dos veces en la misma ronda`() {
        var res = CariocaGame.createGame(players, rules, 12345L)
        var st = res.state
        val current = st.currentPlayer!!
        // Robar
        st = CariocaGame.perform(st, DrawFromStock(current)).state
        // Bajar 2 tríos (ronda 1 exige 2 tríos)
        val t1 = listOf(card(Suit.HEART, Rank.FIVE), card(Suit.SPADE, Rank.FIVE), card(Suit.DIAMOND, Rank.FIVE))
        val t2 = listOf(card(Suit.HEART, Rank.SIX), card(Suit.SPADE, Rank.SIX), card(Suit.DIAMOND, Rank.SIX))
        val hand = t1 + t2 + st.hands[current]!!.filter { it !in t1 && it !in t2 }.take(7)
        st = st.copy(hands = st.hands + (current to hand))
        st = CariocaGame.perform(st, MeldAction(current, listOf(Meld.Triple(t1), Meld.Triple(t2)))).state
        assertTrue(current in st.meldedThisRound)

        // Intento de segunda bajada en la misma ronda → rechazado aunque queden cartas
        val rest = st.hands[current]!!
        val res2 = CariocaGame.canPerform(st, MeldAction(current, listOf(Meld.Triple(rest.take(3)))))
        assertFalse(res2.valid)
        assertTrue(res2.reason!!.contains("Ya te bajaste"))
    }

    @Test
    fun `descartar fin de turno → siguiente jugador`() {
        var res = CariocaGame.createGame(players, rules, 12345L)
        var st = res.state
        val current = st.currentPlayer!!
        // Robar
        st = CariocaGame.perform(st, DrawFromStock(current)).state
        // Descartar la carta robada (última en mano)
        val drawn = st.hands[current]!!.last()
        st = CariocaGame.perform(st, DiscardAction(current, drawn.id)).state
        assertEquals(12, st.hands[current]!!.size)
        val next = players[(players.indexOf(current) - 1 + 4) % 4]
        assertEquals(next, st.currentPlayer)
        assertEquals(Stage.DRAW, st.stage)
        assertEquals(2, st.discard.size) // pozo inicial + descartada
    }

    @Test
    fun `playedThisLap marca quién completó su turno y se reinicia al cerrar la vuelta`() {
        var res = CariocaGame.createGame(players, rules, 12345L)
        var st = res.state
        val first = st.currentPlayer!!
        // Primer turno: roba y descarta
        st = CariocaGame.perform(st, DrawFromStock(first)).state
        val drawn = st.hands[first]!!.last()
        st = CariocaGame.perform(st, DiscardAction(first, drawn.id)).state
        assertEquals(setOf(first), st.playedThisLap)
        assertNotEquals(first, st.currentPlayer)

        // Completar la vuelta (3 descartes más)
        repeat(3) {
            val p = st.currentPlayer!!
            st = CariocaGame.perform(st, DrawFromStock(p)).state
            val c = st.hands[p]!!.last()
            st = CariocaGame.perform(st, DiscardAction(p, c.id)).state
        }
        assertEquals(first, st.currentPlayer)
        assertTrue(st.playedThisLap.isEmpty())
    }

    @Test
    fun `laps y turns contabilizan vueltas completas y turnos jugados`() {
        var res = CariocaGame.createGame(players, rules, 12345L)
        var st = res.state
        assertEquals(0, st.laps)
        assertEquals(0, st.turns)

        fun playTurn(state: CariocaState): CariocaState {
            val p = state.currentPlayer!!
            val afterDraw = CariocaGame.perform(state, DrawFromStock(p)).state
            val c = afterDraw.hands[p]!!.last()
            return CariocaGame.perform(afterDraw, DiscardAction(p, c.id)).state
        }

        // Primera vuelta: 4 turnos = 1 lap
        repeat(4) { st = playTurn(st) }
        assertEquals(1, st.laps)
        assertEquals(4, st.turns)

        // Segunda vuelta completa: 4 turnos más = 2 laps
        repeat(4) { st = playTurn(st) }
        assertEquals(2, st.laps)
        assertEquals(8, st.turns)
    }

    @Test
    fun `fin de ronda, ganador suma 0, otros suman puntos de mano y queda pausado`() {
        var res = CariocaGame.createGame(players, rules, 12345L)
        var st = res.state
        val current = st.currentPlayer!!
        // Robar
        st = CariocaGame.perform(st, DrawFromStock(current)).state
        // Preparar mano vacía para que corte: quitar todas las cartas y descartar una
        val drawn = st.hands[current]!!.last()
        st = st.copy(hands = st.hands + (current to mutableListOf(drawn)))
        // Descartar la única carta → mano vacía = fin de ronda y pausa en ROUND_END
        st = CariocaGame.perform(st, DiscardAction(current, drawn.id)).state
        assertEquals(CariocaPhase.ROUND_END, st.phase)
        assertEquals(0, st.roundIndex) // ronda 1 recién terminada
        assertEquals(0, st.scores[current]!!)
        players.filter { it != current }.forEach { p ->
            assertTrue(st.scores[p]!! > 0)
        }
        assertEquals(1, st.roundsWon[current]!!)

        // La siguiente ronda NO comienza hasta continuar desde el scoreboard
        assertTrue(st.hands[current]!!.isEmpty())
        val human = players.first()
        st = CariocaGame.perform(st, StartNextRound(human)).state
        assertEquals(CariocaPhase.PLAYING, st.phase)
        assertEquals(1, st.roundIndex) // ahora sí, ronda 2
        players.forEach { p ->
            assertEquals(rules.handSize, st.hands[p]!!.size)
        }
    }

    @Test
    fun `laps y turns se reinician en cada ronda`() {
        var res = CariocaGame.createGame(players, rules, 12345L)
        var st = res.state
        // Avanzar una vuelta completa en la ronda 1
        repeat(4) {
            val p = st.currentPlayer!!
            st = CariocaGame.perform(st, DrawFromStock(p)).state
            val c = st.hands[p]!!.last()
            st = CariocaGame.perform(st, DiscardAction(p, c.id)).state
        }
        assertEquals(1, st.laps)
        assertEquals(4, st.turns)
        // Terminar la ronda 1 (mano vacía → corte) y empezar la ronda 2
        val current = st.currentPlayer!!
        st = CariocaGame.perform(st, DrawFromStock(current)).state
        val drawn = st.hands[current]!!.last()
        st = st.copy(hands = st.hands + (current to mutableListOf(drawn)))
        st = CariocaGame.perform(st, DiscardAction(current, drawn.id)).state
        assertEquals(CariocaPhase.ROUND_END, st.phase)
        st = CariocaGame.perform(st, StartNextRound(players.first())).state
        assertEquals(1, st.roundIndex)
        // Nueva ronda: contadores empiezan en 0
        assertEquals(0, st.laps)
        assertEquals(0, st.turns)
    }

    @Test
    fun `StartNextRound rechazado fuera de fase ROUND_END`() {
        var res = CariocaGame.createGame(players, rules, 12345L)
        val st = res.state
        val human = players.first()
        val vr = CariocaGame.canPerform(st, StartNextRound(human))
        assertFalse("No debe avanzar mientras se juega", vr.valid)
    }

    @Test
    fun `no descartar joker`() {
        var res = CariocaGame.createGame(players, rules, 12345L)
        var st = res.state
        val current = st.currentPlayer!!
        st = CariocaGame.perform(st, DrawFromStock(current)).state
        // Inyectar joker en mano
        val jk = joker()
        st = st.copy(hands = st.hands + (current to (st.hands[current]!! + jk)))
        val res2 = CariocaGame.canPerform(st, DiscardAction(current, jk.id))
        assertFalse(res2.valid)
        assertTrue(res2.reason!!.contains("comodines"))
    }

    @Test
    fun `lay-off a juegos ajenos bloqueado si no se ha bajado en la ronda actual`() {
        var res = CariocaGame.createGame(players, rules, 12345L)
        var st = res.state
        val current = st.currentPlayer!!
        // Robar
        st = CariocaGame.perform(st, DrawFromStock(current)).state
        // Bajar 2 tríos (ronda 1 exige 2 tríos)
        val hand = mutableListOf<Card>()
        hand.addAll(listOf(card(Suit.HEART, Rank.FIVE), card(Suit.SPADE, Rank.FIVE), card(Suit.DIAMOND, Rank.FIVE)))
        hand.addAll(listOf(card(Suit.HEART, Rank.SIX), card(Suit.SPADE, Rank.SIX), card(Suit.DIAMOND, Rank.SIX)))
        hand.addAll(st.hands[current]!!.filter { it !in hand }.take(7))
        st = st.copy(hands = st.hands + (current to hand))
        st = CariocaGame.perform(st, MeldAction(current, listOf(Meld.Triple(hand.take(3)), Meld.Triple(hand.drop(3).take(3))))).state
        // Un jugador que nunca se ha bajado en la ronda no puede añadir cartas a
        // los juegos ajenos (rules.md §8)
        val other = players[1]
        val res2 = CariocaGame.canPerform(st, LayOffAction(other, hand[0].id, current, 0))
        assertFalse(res2.valid)
    }

    @Test
    fun `lay-off a juegos ajenos bloqueado en el turno de bajarse y permitido en el siguiente`() {
        var res = CariocaGame.createGame(players, rules, 12345L)
        var st = res.state
        val current = st.currentPlayer!!
        val other = players.first { it != current }

        // Robar
        st = CariocaGame.perform(st, DrawFromStock(current)).state

        // Mano: 2 tríos (para bajar) + 5♣ (extiende trío propio) + 8♦ (extiende trío ajeno)
        val t1 = listOf(card(Suit.HEART, Rank.FIVE), card(Suit.SPADE, Rank.FIVE), card(Suit.DIAMOND, Rank.FIVE))
        val t2 = listOf(card(Suit.HEART, Rank.SIX), card(Suit.SPADE, Rank.SIX), card(Suit.DIAMOND, Rank.SIX))
        val ownLayCard = card(Suit.CLUB, Rank.FIVE)
        val layCard = card(Suit.DIAMOND, Rank.EIGHT)
        val needed = t1 + t2 + ownLayCard + layCard
        val hand = needed + st.hands[current]!!.filter { it !in needed }.take(5)
        st = st.copy(hands = st.hands + (current to hand))

        // other ya bajó un trío de 8s (extensible con el 8♦)
        val otherTriple = Meld.Triple(listOf(card(Suit.HEART, Rank.EIGHT), card(Suit.SPADE, Rank.EIGHT), card(Suit.CLUB, Rank.EIGHT)))
        st = st.copy(table = st.table + (other to listOf(otherTriple)))

        // Se baja
        st = CariocaGame.perform(st, MeldAction(current, listOf(Meld.Triple(t1), Meld.Triple(t2)))).state
        assertTrue(current in st.meldedThisTurn)

        // Mismo turno: no puede añadir cartas ni a juegos ajenos ni a los propios
        assertFalse("No debe añadir a ajenos en el turno de bajarse", CariocaGame.canPerform(st, LayOffAction(current, layCard.id, other, 0)).valid)
        assertFalse("No debe añadir a sus propios juegos en el turno de bajarse", CariocaGame.canPerform(st, LayOffAction(current, ownLayCard.id, current, 0)).valid)

        // Termina su turno → se limpia meldedThisTurn
        val filler = st.hands[current]!!.filter { it != ownLayCard && it != layCard && it !is JokerCard }.first()
        st = CariocaGame.perform(st, DiscardAction(current, filler.id)).state
        assertFalse(current in st.meldedThisTurn)

        // Vuelve el turno a current (los otros 3 juegan)
        repeat(3) {
            val c = st.currentPlayer!!
            st = CariocaGame.perform(st, DrawFromStock(c)).state
            val d = st.hands[c]!!.first { it !is JokerCard }
            st = CariocaGame.perform(st, DiscardAction(c, d.id)).state
        }
        assertEquals(current, st.currentPlayer)
        st = CariocaGame.perform(st, DrawFromStock(current)).state

        // Turno siguiente: ya puede añadir a juegos ajenos
        assertTrue(
            "Debe poder añadir a juegos ajenos tras esperar un turno",
            CariocaGame.canPerform(st, LayOffAction(current, layCard.id, other, 0)).valid
        )
    }

    @Test
    fun `partida completa vs 3 bots termina en GAME_END`() {
        // Test de integración: partida automática con bots. Se usa un ruleset
        // corto (2 rondas de 2 tríos) para que la heurística simple pueda
        // completarla en un presupuesto razonable de pasos.
        val shortRules = CariocaRuleset(rounds = listOf(
            CariocaRound(1, listOf(ComboSpec(ComboType.TRIPLE, 2))),
            CariocaRound(2, listOf(ComboSpec(ComboType.TRIPLE, 2)))
        ))
        val bots = listOf(PlayerId("bot1"), PlayerId("bot2"), PlayerId("bot3"), PlayerId("bot4"))
        val res = CariocaGame.createGame(bots, shortRules, 999L)
        var st = res.state
        val rng = java.util.Random(999L)
        var steps = 0
        while (st.phase != CariocaPhase.GAME_END && steps < 30000) {
            // Al terminar una ronda el juego queda pausado en ROUND_END hasta
            // que se continúa desde el scoreboard (StartNextRound).
            if (st.phase == CariocaPhase.ROUND_END) {
                st = CariocaGame.perform(st, StartNextRound(st.players.first())).state
                steps++
                continue
            }
            val current = st.currentPlayer!!
            val action = CariocaBot.chooseAction(st, current, rng)
            val vr = CariocaGame.canPerform(st, action)
            if (!vr.valid) {
                // Fallback: robar y descartar aleatorio
                val fallback = if (st.stage == Stage.DRAW) DrawFromStock(current) else DiscardAction(current, st.hands[current]!!.first { it !is JokerCard }.id)
                st = CariocaGame.perform(st, fallback).state
            } else {
                st = CariocaGame.perform(st, action).state
            }
            steps++
        }
        assertEquals("Debe terminar la partida (pasos=$steps, ronda=${st.roundIndex})", CariocaPhase.GAME_END, st.phase)
        assertNotNull(st.result)
        assertEquals(4, st.result!!.rankings.size)
        assertTrue(steps < 30000)
        println("Pasos: $steps, Ganador: ${st.winner}, Scores: ${st.scores}")
    }
}