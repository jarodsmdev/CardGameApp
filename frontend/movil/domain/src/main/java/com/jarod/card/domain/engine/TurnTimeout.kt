package com.jarod.card.domain.engine

/** Política de timeout de turno (game-engine.md §6 / ADR-012). */
enum class TimeoutPolicy {
    /** Solo aviso visual: el turno no se juega automáticamente al agotarse. */
    NONE,

    /** El motor juega aleatorio por el jugador; [TurnTimeout.limit] timeouts = abandono. */
    PLAY_RANDOM
}

/**
 * Configuración genérica de límite de turno, válida para cualquier juego.
 * El motor/UI la consumen de forma agnóstica; `seconds = 0` desactiva el límite.
 */
data class TurnTimeout(
    val seconds: Int = 45,
    val policy: TimeoutPolicy = TimeoutPolicy.NONE,
    val limit: Int = 2
) {
    init {
        require(seconds >= 0) { "TurnTimeout: seconds no puede ser negativo" }
        require(limit >= 1) { "TurnTimeout: limit debe ser >= 1" }
    }

    /** ¿Hay temporizador activo? */
    val enabled: Boolean get() = seconds > 0

    /** Segundos a partir de los cuales se muestra el aviso de poco tiempo. */
    val warningAtSeconds: Int get() = minOf(10, seconds)
}
