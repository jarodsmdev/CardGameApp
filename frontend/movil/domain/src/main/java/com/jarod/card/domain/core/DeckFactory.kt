package com.jarod.card.domain.core

import java.util.Random

/**
 * Configuración de la baraja. Carioca usa `FIXED_2`: siempre los 2 juegos
 * (108 cartas) para 2, 3 o 4 jugadores (rules.md §2 / game-engine.md §9).
 */
data class DeckConfig(
    val sets: Int = 2,
    val jokerTypes: List<JokerType> = listOf(JokerType.COLORED, JokerType.PLAIN)
)

object DeckFactory {

    fun buildSet(setIndex: Int): List<Card> {
        val cards = Suit.entries.flatMap { suit ->
            Rank.entries.map { rank ->
                PlayingCard(
                    id = "$setIndex:${suit.symbol}:${rank.symbol}",
                    setIndex = setIndex,
                    suit = suit,
                    rank = rank
                )
            }
        }
        val jokers = jokerTypes(setIndex)
        return cards + jokers
    }

    fun jokerTypes(setIndex: Int): List<JokerCard> = listOf(
        JokerCard("$setIndex:JOKER:${JokerType.COLORED.symbol}", setIndex, JokerType.COLORED),
        JokerCard("$setIndex:JOKER:${JokerType.PLAIN.symbol}", setIndex, JokerType.PLAIN)
    )

    fun build(config: DeckConfig = DeckConfig()): List<Card> =
        (0 until config.sets).flatMap { buildSet(it) }
}

/**
 * Fisher–Yates determinista con semilla (game-engine.md §7). El mismo seed +
 * misma lógica = misma secuencia en JVM/Android (`java.util.Random` es estable).
 */
fun List<Card>.shuffled(seed: Long): List<Card> {
    val rng = Random(seed)
    val copy = toMutableList()
    for (i in copy.size - 1 downTo 1) {
        val j = rng.nextInt(i + 1)
        val tmp = copy[i]
        copy[i] = copy[j]
        copy[j] = tmp
    }
    return copy
}
