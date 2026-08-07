package com.jarod.card.domain.games.carioca

import com.jarod.card.domain.engine.TurnTimeout

/**
 * Configuración de reglas de Carioca como datos (game-engine.md §9 / rules.md §13).
 * Ajustar reglas = nueva versión de ruleset, sin tocar el motor.
 */
enum class ComboType { TRIPLE, RUN }

data class ComboSpec(
    val type: ComboType,
    val count: Int,
    val minLength: Int = 4,
    val exactLength: Int? = null
)

data class CariocaRound(
    val number: Int,
    val combos: List<ComboSpec>
)

data class RunRules(
    val minLength: Int = 4,
    val wraparound: Boolean = true,
    val sameSuit: Boolean = true
)

data class JokerRules(
    val maxPerMeld: Int = 1,
    val maxPerHand: Int = 2,
    val cannotDiscard: Boolean = true,
    val jokersNotAdjacent: Boolean = true
)

data class CariocaRuleset(
    val gameType: String = "carioca",
    val deckSets: String = "FIXED_2",
    val handSize: Int = 12,
    val maxPlayers: IntRange = 2..4,
    val rounds: List<CariocaRound> = defaultRounds,
    val runRules: RunRules = RunRules(),
    val jokerRules: JokerRules = JokerRules(),
    val dealOrder: String = "COUNTER_CLOCKWISE",
    val scoringWinner: Int = 0,
    val cutBonus: Int = 0,
    val turnTimeout: TurnTimeout = TurnTimeout()
)

/** Catálogo base de rondas (rules.md §9.1): 1–8 combos, 9 = Escala Real. */
val defaultRounds: List<CariocaRound> = listOf(
    CariocaRound(1, listOf(ComboSpec(ComboType.TRIPLE, 2))),
    CariocaRound(2, listOf(ComboSpec(ComboType.TRIPLE, 1), ComboSpec(ComboType.RUN, 1, minLength = 4))),
    CariocaRound(3, listOf(ComboSpec(ComboType.RUN, 2, minLength = 4))),
    CariocaRound(4, listOf(ComboSpec(ComboType.TRIPLE, 3))),
    CariocaRound(5, listOf(ComboSpec(ComboType.TRIPLE, 2), ComboSpec(ComboType.RUN, 1, minLength = 4))),
    CariocaRound(6, listOf(ComboSpec(ComboType.TRIPLE, 1), ComboSpec(ComboType.RUN, 2, minLength = 4))),
    CariocaRound(7, listOf(ComboSpec(ComboType.RUN, 3, minLength = 4))),
    CariocaRound(8, listOf(ComboSpec(ComboType.TRIPLE, 4))),
    CariocaRound(9, listOf(ComboSpec(ComboType.RUN, 1, exactLength = 13)))
)
