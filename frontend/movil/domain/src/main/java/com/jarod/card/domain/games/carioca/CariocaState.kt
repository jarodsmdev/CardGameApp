package com.jarod.card.domain.games.carioca

import com.jarod.card.domain.core.Card
import com.jarod.card.domain.engine.GameAction
import com.jarod.card.domain.engine.GameEvent
import com.jarod.card.domain.engine.GameResult
import com.jarod.card.domain.engine.GameState
import com.jarod.card.domain.engine.PlayerId

enum class CariocaPhase { PREPARING, PLAYING, ROUND_END, GAME_END }
enum class Stage { DRAW, ACTIONS }

/** Estado inmutable de la partida de Carioca (game-engine.md §4). */
data class CariocaState(
    val ruleset: CariocaRuleset,
    val seed: Long,
    val phase: CariocaPhase = CariocaPhase.PREPARING,
    val roundIndex: Int = 0,
    val dealerSeat: Int = 0,
    val players: List<PlayerId> = emptyList(),
    val hands: Map<PlayerId, List<Card>> = emptyMap(),
    val stock: List<Card> = emptyList(),
    val discard: List<Card> = emptyList(),
    val table: Map<PlayerId, List<Meld>> = emptyMap(),
    val meldedThisRound: Set<PlayerId> = emptySet(),
    val meldedThisTurn: Set<PlayerId> = emptySet(),
    val playedThisLap: Set<PlayerId> = emptySet(),
    val everMelded: Set<PlayerId> = emptySet(),
    val currentPlayer: PlayerId? = null,
    val stage: Stage = Stage.DRAW,
    val scores: Map<PlayerId, Int> = emptyMap(),
    val roundsWon: Map<PlayerId, Int> = emptyMap(),
    val winner: PlayerId? = null,
    val result: GameResult? = null,
    val seq: Long = 0L
) : GameState()

/** Acciones específicas de Carioca (game-engine.md §5.1). */
sealed interface CariocaAction : GameAction {
    override val playerId: PlayerId
}
data class DrawFromStock(override val playerId: PlayerId) : CariocaAction
data class DrawFromDiscard(override val playerId: PlayerId) : CariocaAction
data class MeldAction(override val playerId: PlayerId, val groups: List<Meld>) : CariocaAction
data class DiscardAction(override val playerId: PlayerId, val cardId: String) : CariocaAction
data class LayOffAction(override val playerId: PlayerId, val cardId: String, val meldOwner: PlayerId, val meldIndex: Int) : CariocaAction

/** Eventos emitidos por Carioca (game-engine.md §5.2). */
sealed interface CariocaEvent : GameEvent
data class RoundStarted(override val seq: Long, val round: Int, val dealer: PlayerId, val handSize: Int) : CariocaEvent
data class CardDrawn(override val seq: Long, val playerId: PlayerId, val fromStock: Boolean, val cardId: String) : CariocaEvent
data class CardsMeld(override val seq: Long, val playerId: PlayerId, val groupCardIds: List<List<String>>) : CariocaEvent
data class CardDiscarded(override val seq: Long, val playerId: PlayerId, val cardId: String) : CariocaEvent
data class CardLaidOff(override val seq: Long, val playerId: PlayerId, val cardId: String, val meldOwner: PlayerId, val meldIndex: Int) : CariocaEvent
data class TurnChanged(override val seq: Long, val playerId: PlayerId) : CariocaEvent
data class RoundEnd(override val seq: Long, val winner: PlayerId, val pointsGained: Map<PlayerId, Int>) : CariocaEvent
data class GameEndEvent(override val seq: Long, val result: GameResult) : CariocaEvent