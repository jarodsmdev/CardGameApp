package com.jarod.card.domain.games.carioca

import com.jarod.card.domain.core.Card
import com.jarod.card.domain.core.JokerCard
import com.jarod.card.domain.core.PlayingCard
import com.jarod.card.domain.core.Rank
import com.jarod.card.domain.core.Suit
import com.jarod.card.domain.engine.PlayerId
import java.util.Random

/**
 * Bot simple para Fase 1 (local vs bots). Heurística:
 * - Roba del pozo si la carta superior completa una combinación; sino del mazo.
 * - Bajarse: busca combos que cumplan la ronda actual (backtracking).
 * - Lay-off: extiende sus propios juegos, luego ajenos si ya bajó antes.
 * - Descarta: carta de mayor valor que no sirve en combos parciales; nunca joker.
 */
object CariocaBot {

    fun chooseAction(state: CariocaState, playerId: PlayerId, rng: Random): CariocaAction {
        return when (state.stage) {
            Stage.DRAW -> decideDraw(state, playerId)
            Stage.ACTIONS -> decideAction(state, playerId, rng)
        }
    }

    private fun decideDraw(state: CariocaState, playerId: PlayerId): CariocaAction {
        val topDiscard = state.discard.lastOrNull()
        if (topDiscard != null && helpsCombo(state, playerId, topDiscard)) {
            return DrawFromDiscard(playerId)
        }
        return DrawFromStock(playerId)
    }

    private fun decideAction(state: CariocaState, playerId: PlayerId, rng: Random): CariocaAction {
        val hand = state.hands[playerId]!!

        // 1. Si no se ha bajado, intentar bajarse
        if (playerId !in state.meldedThisRound) {
            val round = state.ruleset.rounds[state.roundIndex]
            findMeldForRound(hand, round)?.let { groups ->
                return MeldAction(playerId, groups)
            }
        }

        // 2. Lay-off (extender juegos propios y ajenos; solo desde el turno
        //    siguiente a bajarse, rules.md §8)
        findLayOff(state, playerId)?.let { return it }

        // 3. Descartar: carta de mayor valor inútil (no joker, no en combos parciales)
        val discard = chooseDiscard(hand, state, playerId, rng)
        return DiscardAction(playerId, discard.id)
    }

    /** Encuentra un lay-off válido (juegos propios y ajenos) para quien ya
     *  se bajó en la ronda actual y en un turno anterior (rules.md §8). */
    fun findLayOff(state: CariocaState, playerId: PlayerId): LayOffAction? {
        val hand = state.hands[playerId]!!
        if (hand.isEmpty()) return null
        // Solo quien se bajó en la ronda actual y no en el mismo turno puede dar cartas
        if (playerId !in state.meldedThisRound || playerId in state.meldedThisTurn) return null
        // Propios
        val ownMelds = state.table[playerId]!!
        for ((i, meld) in ownMelds.withIndex()) {
            for (card in hand) {
                if (MeldValidator.validate(meld.cards + card, state.ruleset) != null) {
                    return LayOffAction(playerId, card.id, playerId, i)
                }
            }
        }
        // Ajenos
        for ((owner, melds) in state.table) {
            if (owner == playerId) continue
            for ((i, meld) in melds.withIndex()) {
                for (card in hand) {
                    if (MeldValidator.validate(meld.cards + card, state.ruleset) != null) {
                        return LayOffAction(playerId, card.id, owner, i)
                    }
                }
            }
        }
        return null
    }

    private fun helpsCombo(state: CariocaState, playerId: PlayerId, card: Card): Boolean {
        val hand = state.hands[playerId]!!
        val round = state.ruleset.rounds[state.roundIndex]
        val newHand = hand + card
        // ¿Completa un trío?
        if (card is PlayingCard) {
            val sameRank = newHand.filterIsInstance<PlayingCard>().count { it.rank == card.rank }
            if (sameRank >= 3) return true
        }
        // ¿Completa una escala?
        Suit.entries.forEach { suit ->
            val suitCards = newHand.filterIsInstance<PlayingCard>().filter { it.suit == suit }
            val indices = suitCards.map { it.rank.cycleIndex }.toSet()
            if (indices.size >= 3 && MeldValidator.isContiguousRun(indices.toList(), state.ruleset)) return true
        }
        return false
    }

    /** Busca combinación de grupos que cumplan la ronda (backtracking simple). */
    fun findMeldForRound(hand: List<Card>, round: CariocaRound): List<Meld>? {
        val candidates = generateCandidates(hand)
        return searchMelds(candidates, round.combos, mutableSetOf(), 0)
    }

    private fun generateCandidates(hand: List<Card>): List<Meld> {
        val out = mutableListOf<Meld>()
        val jokers = hand.filterIsInstance<JokerCard>().toMutableList()
        val byRank = hand.filterIsInstance<PlayingCard>().groupBy { it.rank }

        // Tríos
        for ((rank, cards) in byRank) {
            if (cards.size >= 3) out += Meld.Triple(cards.take(3))
            else if (cards.size == 2 && jokers.isNotEmpty()) {
                out += Meld.Triple(cards + jokers.removeAt(0))
            }
        }

        // Escalas por pinta (ventana deslizante sobre el ciclo con jokers)
        val bySuit = hand.filterIsInstance<PlayingCard>().groupBy { it.suit }
        for ((suit, cards) in bySuit) {
            // Rangos distintos por pinta (hay 2 juegos en la baraja)
            val distinctIdx = cards.map { it.rank.cycleIndex }.distinct()
            for (len in 4..Rank.CYCLE_SIZE) {
                for (start in 0 until Rank.CYCLE_SIZE) {
                    val window = (0 until len).map { (start + it) % Rank.CYCLE_SIZE }
                    val present = distinctIdx.count { it in window }
                    val missing = len - present
                    if (missing < 0 || missing > jokers.size) continue
                    val runCards = cards.filter { it.rank.cycleIndex in window }.distinctBy { it.rank.cycleIndex }
                    val run = runCards + jokers.take(missing)
                    out += Meld.Run(run)
                }
            }
        }
        return out
    }

    private fun searchMelds(
        candidates: List<Meld>,
        specs: List<ComboSpec>,
        usedCards: MutableSet<String>,
        start: Int
    ): List<Meld>? {
        // Contar ya cumplidos
        val tripleCount = specs.sumOf { if (it.type == ComboType.TRIPLE) it.count else 0 }
        val runCount = specs.sumOf { if (it.type == ComboType.RUN) it.count else 0 }
        if (tripleCount == 0 && runCount == 0) return emptyList()

        for (i in start until candidates.size) {
            val m = candidates[i]
            if (m.cardIds().any { it in usedCards }) continue
            val isTriple = m is Meld.Triple
            val isRun = m is Meld.Run
            if (isTriple && tripleCount == 0) continue
            if (isRun && runCount == 0) continue
            val newSpecs = specs.map { s ->
                if (s.type == ComboType.TRIPLE && isTriple) s.copy(count = s.count - 1)
                else if (s.type == ComboType.RUN && isRun) {
                    val minLen = if (s.exactLength != null) s.exactLength!! else s.minLength
                    if (m.cards.size >= minLen) s.copy(count = s.count - 1) else s
                } else s
            }
            usedCards.addAll(m.cardIds())
            val rest = searchMelds(candidates, newSpecs, usedCards, i + 1)
            if (rest != null) {
                usedCards.removeAll(m.cardIds())
                return listOf(m) + rest
            }
            usedCards.removeAll(m.cardIds())
        }
        return null
    }

    private fun chooseDiscard(
        hand: List<Card>,
        state: CariocaState,
        playerId: PlayerId,
        rng: Random
    ): Card {
        val round = state.ruleset.rounds[state.roundIndex]
        // Cartas que no sirven en ningún combo parcial
        val usefulIds = hand.filter { helpsCombo(state, playerId, it) }.map { it.id }.toSet()
        val discardable = hand.filter { it !is JokerCard && it.id !in usefulIds }
        val pool = if (discardable.isNotEmpty()) discardable else hand.filter { it !is JokerCard }
        // Mayor valor
        return pool.maxByOrNull { it.points } ?: hand.first { it !is JokerCard }
    }
}