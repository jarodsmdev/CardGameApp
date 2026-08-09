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
     * Forma canónica de un grupo ya validado, para almacenarlo en la mesa:
     * solo las ESCALAS se reordenan secuencialmente (jokers en su posición
     * lógica). Los tríos se devuelven tal cual los envió el jugador, porque el
     * orden no tiene sentido en un trío.
     */
    fun canonicalize(cards: List<Card>, rules: CariocaRuleset): Meld? {
        validateRun(cards, rules)?.let { return it }
        return validateTriple(cards)
    }

    /**
     * Escala: 4+ cartas consecutivas de la misma pinta (o 13 con el giro del
     * ciclo A→1→…→K→A). Los jokers rellenan un rango faltante.
     * Devuelve la Run con cartas ordenadas secuencialmente (jokers en su posición).
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

        val ranksSet = realRanks.toSet()
        val len = cards.size

        for (start in 0 until Rank.CYCLE_SIZE) {
            if (start + len > Rank.CYCLE_SIZE && !rules.runRules.wraparound) continue
            val window = (0 until len).map { (start + it) % Rank.CYCLE_SIZE }.toSet()
            if (ranksSet.all { it in window }) {
                // Construir cartas ordenadas: reales en su posición + jokers rellenando huecos
                val jokerCards = cards.filterIsInstance<JokerCard>().toMutableList()
                val ordered = (0 until len).map { offset ->
                    val idx = (start + offset) % Rank.CYCLE_SIZE
                    // Buscar carta real con este cycleIndex
                    val realCard = real.find { it.rank.cycleIndex == idx }
                    if (realCard != null) realCard
                    else {
                        // Usar un joker para esta posición
                        require(jokerCards.isNotEmpty()) { "Falta joker para posición $idx" }
                        jokerCards.removeAt(0)
                    }
                }.toList()
                return Meld.Run(ordered)
            }
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

    /**
     * Valida un lay-off sobre un meld YA CANÓNICO, preservando las posiciones
     * de los jokers: en escalas el joker tiene rango fijo y no puede "chocar"
     * con la carta natural que se intenta añadir.
     */
    fun validateLayOff(
        oldMeld: Meld,
        newCard: Card,
        rules: CariocaRuleset
    ): Meld? {
        return when (oldMeld) {
            is Meld.Triple -> validateLayOffTriple(oldMeld, newCard, rules)
            is Meld.Run -> validateLayOffRun(oldMeld, newCard, rules)
        }
    }

    private fun validateLayOffTriple(
        triple: Meld.Triple,
        newCard: Card,
        rules: CariocaRuleset
    ): Meld.Triple? {
        // En tríos el orden no importa; solo se verifica mismo rango y límite de jokers
        val allCards = triple.cards + newCard
        return validateTriple(allCards)
    }

    private fun validateLayOffRun(
        run: Meld.Run,
        newCard: Card,
        rules: CariocaRuleset
    ): Meld.Run? {
        // Solo cartas reales se pueden añadir (no jokers) en lay-off estándar
        val newReal = newCard as? PlayingCard ?: return null

        // Misma pinta si la regla lo exige
        if (rules.runRules.sameSuit) {
            val suit = run.cards.filterIsInstance<PlayingCard>().firstOrNull()?.suit
            if (suit != null && newReal.suit != suit) return null
        }

        // Obtener rango mínimo y máximo representados por la run canónica
        val (minIdx, maxIdx) = runMinMaxCycleIndex(run, rules)
        val newIdx = newReal.rank.cycleIndex

        // Solo se permite extender en los extremos (min-1 o max+1)
        val extendedLow = (minIdx - 1 + Rank.CYCLE_SIZE) % Rank.CYCLE_SIZE
        val extendedHigh = (maxIdx + 1) % Rank.CYCLE_SIZE

        val isLowExtension = newIdx == extendedLow
        val isHighExtension = newIdx == extendedHigh

        // Respetar regla de wraparound
        if (isLowExtension && minIdx == 0 && !rules.runRules.wraparound) return null
        if (isHighExtension && maxIdx == Rank.CYCLE_SIZE - 1 && !rules.runRules.wraparound) return null

        if (!isLowExtension && !isHighExtension) return null

        // Construir la nueva run canónica extendida: insertar la carta en el extremo
        val jokers = run.cards.filterIsInstance<JokerCard>().toMutableList()
        val newCards = if (isLowExtension) {
            // Prepend: la carta nueva va al principio, el resto se desplaza
            listOf(newReal) + run.cards.map { if (it is JokerCard) { jokers.removeAt(0); it } else it }
        } else {
            // Append: la carta nueva va al final
            run.cards + newReal
        }
        return Meld.Run(newCards)
    }

    /** Devuelve (minCycleIndex, maxCycleIndex) representados por una run canónica. */
    private fun runMinMaxCycleIndex(run: Meld.Run, rules: CariocaRuleset): Pair<Int, Int> {
        // Encontrar la primera carta real para calcular el start del ciclo
        val firstReal = run.cards.firstOrNull { it is PlayingCard } as? PlayingCard
            ?: return Pair(0, run.cards.size - 1) // fallback: sin reales
        val firstRealIdx = run.cards.indexOf(firstReal)
        val start = (firstReal.rank.cycleIndex - firstRealIdx + Rank.CYCLE_SIZE) % Rank.CYCLE_SIZE
        val len = run.cards.size
        val max = (start + len - 1) % Rank.CYCLE_SIZE
        return Pair(start, max)
    }
}