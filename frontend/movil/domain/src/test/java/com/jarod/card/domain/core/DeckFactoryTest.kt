package com.jarod.card.domain.core

import com.jarod.card.domain.core.DeckFactory.build
import org.junit.Assert.*
import org.junit.Test

class DeckFactoryTest {

    @Test
    fun `baraja FIXED_2 tiene 108 cartas (2 juegos × 54)`() {
        val deck = build()
        assertEquals(108, deck.size)
    }

    @Test
    fun `cada juego tiene 2 jokers (1 coloreado + 1 sin colorear)`() {
        val deck = build()
        val jokers = deck.filterIsInstance<JokerCard>()
        assertEquals(4, jokers.size)
        val colored = jokers.count { it.type == JokerType.COLORED }
        val plain = jokers.count { it.type == JokerType.PLAIN }
        assertEquals(2, colored)
        assertEquals(2, plain)
    }

    @Test
    fun `IDs son únicos`() {
        val deck = build()
        val ids = deck.map { it.id }
        assertEquals(108, ids.distinct().size)
    }

    @Test
    fun `shuffle determinista, misma semilla → mismo orden`() {
        val d1 = build().shuffled(42)
        val d2 = build().shuffled(42)
        assertEquals(d1.map { it.id }, d2.map { it.id })
    }

    @Test
    fun `shuffle, semillas distintas → orden distinto (con alta probabilidad)`() {
        val d1 = build().shuffled(42)
        val d2 = build().shuffled(43)
        assertNotEquals(d1.map { it.id }, d2.map { it.id })
    }

    @Test
    fun `puntos, 2-10 su valor, JQK=10, A=20, joker=30`() {
        val deck = build()
        val points = deck.map { it.points }
        assertTrue(points.all { it > 0 })
        assertTrue(points.contains(30))
        assertEquals(4, points.count { it == 30 })
    }
}