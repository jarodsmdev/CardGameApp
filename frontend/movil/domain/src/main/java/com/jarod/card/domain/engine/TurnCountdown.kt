package com.jarod.card.domain.engine

/**
 * Cuenta regresiva de turno (pura, sin coroutines), reutilizable por cualquier
 * juego. El conductor (ViewModel, estado online…) la hace avanzar con [tick] y
 * consulta [expired]; la política de [TurnTimeout] decide el comportamiento al
 * agotarse (aviso visual o jugada aleatoria por el jugador).
 */
class TurnCountdown(
    private val timeout: TurnTimeout = TurnTimeout()
) {
    var remainingSeconds: Int = timeout.seconds
        private set

    val expired: Boolean get() = remainingSeconds <= 0

    val policy: TimeoutPolicy get() = timeout.policy

    fun tick() {
        if (remainingSeconds > 0) remainingSeconds -= 1
    }

    fun reset() {
        remainingSeconds = timeout.seconds
    }
}
