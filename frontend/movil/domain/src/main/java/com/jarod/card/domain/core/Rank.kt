package com.jarod.card.domain.core

/**
 * Rangos de la baraja inglesa/francesa usada en Carioca: `2..9, 10, J, Q, K, A`.
 * `points` = valor de puntuación (rules.md §3): 2–10 su valor, J/Q/K = 10, A = 20.
 * `cycleIndex` = posición en el ciclo de escalas (A→2→…→10→J→Q→K→A), 0..12.
 */
enum class Rank(val symbol: String, val points: Int, val cycleIndex: Int) {
    TWO("2", 2, 0),
    THREE("3", 3, 1),
    FOUR("4", 4, 2),
    FIVE("5", 5, 3),
    SIX("6", 6, 4),
    SEVEN("7", 7, 5),
    EIGHT("8", 8, 6),
    NINE("9", 9, 7),
    TEN("10", 10, 8),
    JACK("J", 10, 9),
    QUEEN("Q", 10, 10),
    KING("K", 10, 11),
    ACE("A", 20, 12);

    companion object {
        const val CYCLE_SIZE = 13
    }
}
