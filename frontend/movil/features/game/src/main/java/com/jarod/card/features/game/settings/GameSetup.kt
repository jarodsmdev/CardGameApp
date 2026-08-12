package com.jarod.card.features.game.settings

/**
 * Configuración elegida en "Personalizar juego" (FR-SAL-01 / FR-CAR-05):
 * nº de jugadores, rondas a jugar (cantidad y/o modos del catálogo), variantes
 * activadas y ronda inicial (solo debug). Se persiste y se aplica al crear la
 * partida; sin personalizar equivale al Carioca estándar (9 rondas base).
 */
data class GameSetup(
    val players: Int = 4,
    /** Números de ronda del catálogo (1..9) a jugar, en orden. Vacío = 9 base. */
    val rounds: List<Int> = (1..9).toList(),
    /** Variante "−10 por corte" (ADR-018): el ganador de la ronda resta 10. */
    val cutBonusEnabled: Boolean = false,
    /** Ronda inicial para debug (número del catálogo). Solo con DEBUG_TOOLS_ENABLED. */
    val initialRound: Int = 1
)
