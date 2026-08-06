package com.jarod.card.domain.engine

import com.jarod.card.domain.core.Card

@JvmInline
value class PlayerId(val value: String)

enum class GameType { CARIOCA }

/** Acción: intención del cliente (game-engine.md §5.1). */
interface GameAction {
    val playerId: PlayerId
}

/** Evento: hecho consumado emitido por el motor (game-engine.md §5.2). */
interface GameEvent {
    val seq: Long
}

/**
 * Estado base del juego (marker open class para herencia cross-package).
 */
open class GameState

data class ValidationResult(val valid: Boolean, val reason: String? = null) {
    companion object {
        fun ok(): ValidationResult = ValidationResult(true, null)
        fun reject(reason: String): ValidationResult = ValidationResult(false, reason)
    }
}

data class GameTransition<S : GameState>(
    val state: S,
    val events: List<GameEvent>
)

data class PlayerRanking(
    val playerId: PlayerId,
    val score: Int,
    val roundsWon: Int,
    val rank: Int
)

data class GameResult(
    val gameType: GameType,
    val rankings: List<PlayerRanking>
)

/**
 * Abstracción de juego (game-engine.md §3). Cada juego nuevo implementa esta
 * interfaz y se registra en [GameRegistry]; el core no se modifica.
 */
interface Game<S : GameState> {
    val type: GameType
    fun canPerform(state: S, action: GameAction): ValidationResult
    fun perform(state: S, action: GameAction): GameTransition<S>
    fun resolve(state: S): GameResult
}

object GameRegistry {
    private val games = mutableMapOf<GameType, Game<*>>()

    fun register(game: Game<*>) {
        games[game.type] = game
    }

    @Suppress("UNCHECKED_CAST")
    fun <S : GameState> get(type: GameType): Game<S> = games.getValue(type) as Game<S>
}

/** Puntos de una mano (para scoring de fin de ronda). */
fun Iterable<Card>.sumPoints(): Int = sumOf { it.points }
