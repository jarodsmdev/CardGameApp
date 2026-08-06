package com.jarod.card.domain.games.carioca

import com.jarod.card.domain.core.Card
import com.jarod.card.domain.core.JokerCard
import com.jarod.card.domain.core.PlayingCard
import com.jarod.card.domain.core.Rank

/**
 * Combinaciones válidas (rules.md §4):
 * - Trío: 3+ cartas del mismo valor, sin importar pinta ni color.
 * - Escala: 4+ cartas consecutivas de la misma pinta, con giro permitido.
 * Comodines: máximo 1 por combinación en la base (rules.md §5).
 */
sealed interface Meld {
    val cards: List<Card>
    fun cardIds(): List<String> = cards.map { it.id }

    data class Triple(override val cards: List<Card>) : Meld

    data class Run(override val cards: List<Card>) : Meld
}

object MeldValidator {

    fun validate(cards: List<Card>, rules: CariocaRuleset): Meld? {
        if (cards.size < 3) return null
        val jokers = cards.count { it is JokerCard }
        if (jokers > rules.jokerRules.maxPerMeld) return null
        return validateTriple(cards) ?: validateRun(cards, rules)
    }

    /** Trío: 3+ cartas del mismo rango; los jokers rellenan (mismo rango). */
    fun validateTriple(cards: List<Card>): Meld.Triple? {
        val real = cards.filterIsInstance<PlayingCard>()
        if (real.isEmpty()) return null
        if (real.map { it.rank }.distinct().size != 1) return null
        if (cards.size < 3) return null
        return Meld.Triple(cards)
    }

    /**
     * Escala: 4+ cartas consecutivas de la misma pinta (o 13 con el giro del
     * ciclo A→1→…→K→A). Los jokers rellenan un rango faltante.
     */
    fun validateRun(cards: List<Card>, rules: CariocaRuleset): Meld.Run? {
        val real = cards.filterIsInstance<PlayingCard>()
        if (real.isEmpty()) return null
        if (real.size < rules.runRules.minLength - 1) return null
        if (rules.runRules.sameSuit && real.map { it.suit }.distinct().size != 1) return null
        val realRanks = real.map { it.rank.cycleIndex }
        if (realRanks.size != realRanks.distinct().size) return null
        val jokers = cards.count { it is JokerCard }
        if (jokers > rules.jokerRules.maxPerMeld) return null
        if (cards.size < rules.runRules.minLength) return null

        // Los jokers rellenan huecos: es válida si existe un arco contiguo de
        // tamaño `cards.size` sobre el ciclo que contenga todos los rangos reales.
        val ranksSet = realRanks.toSet()
        val len = cards.size
        for (start in 0 until Rank.CYCLE_SIZE) {
            if (start + len > Rank.CYCLE_SIZE && !rules.runRules.wraparound) continue
            val window = (0 until len).map { (start + it) % Rank.CYCLE_SIZE }.toSet()
            if (ranksSet.all { it in window }) return Meld.Run(cards)
        }
        return null
    }

    /** Arco contiguo sobre el ciclo de 13 rangos; admite un solo giro (rules.md §4). */
    fun isContiguousRun(indices: List<Int>, rules: CariocaRuleset): Boolean {
        if (indices.size < 2) return true
        val s = indices.sorted()
        if (s.zipWithNext().all { it.second - it.first == 1 }) return true
        if (!rules.runRules.wraparound) return false
        val gaps = s.zipWithNext().map { it.second - it.first }
        return gaps.count { it != 1 } == 1 && (s.first() + Rank.CYCLE_SIZE - s.last()) == 1
    }

    /** ¿Los melds dados cumplen los combos exigidos por la ronda? (rules.md §9.1) */
    fun roundSatisfied(melds: List<Meld>, round: CariocaRound): Boolean {
        for (spec in round.combos) {
            val count = when (spec.type) {
                ComboType.TRIPLE -> melds.count { it is Meld.Triple }
                ComboType.RUN -> melds.count { meld ->
                    if (spec.exactLength != null) meld.cards.size == spec.exactLength
                    else meld.cards.size >= spec.minLength
                }
            }
            if (count < spec.count) return false
        }
        return true
    }

    /** No se valen juegos duplicados: dos tríos del mismo rango (rules.md §4). */
    fun hasDuplicateTripleRank(melds: List<Meld>): Boolean {
        val ranks = mutableListOf<Rank>()
        for (triple in melds.filterIsInstance<Meld.Triple>()) {
            val real = triple.cards.filterIsInstance<PlayingCard>()
            if (real.isNotEmpty()) ranks.add(real.first().rank)
        }
        return ranks.size != ranks.distinct().size
    }
}