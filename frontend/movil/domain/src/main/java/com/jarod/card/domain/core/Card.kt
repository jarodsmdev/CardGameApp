package com.jarod.card.domain.core

/**
 * Carta de la baraja. El `id` es único por partida: `setIndex:palo:rango` para
 * cartas normales y `setIndex:JOKER:tipo` para los comodines (rules.md §2).
 */
sealed interface Card {
    val id: String
    val setIndex: Int
    val points: Int
}

data class PlayingCard(
    override val id: String,
    override val setIndex: Int,
    val suit: Suit,
    val rank: Rank
) : Card {
    override val points: Int get() = rank.points
}

/** Comodín. Cada juego trae 2: uno coloreado y uno sin colorear (rules.md §2). */
enum class JokerType(val symbol: String) {
    COLORED("JOKER_COLORED"),
    PLAIN("JOKER_PLAIN")
}

data class JokerCard(
    override val id: String,
    override val setIndex: Int,
    val type: JokerType
) : Card {
    override val points: Int = JOKER_POINTS

    companion object {
        const val JOKER_POINTS = 30
    }
}
