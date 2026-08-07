package com.jarod.card.domain.games.carioca

import com.jarod.card.domain.core.Card
import com.jarod.card.domain.core.DeckFactory.build
import com.jarod.card.domain.core.JokerCard
import com.jarod.card.domain.core.PlayingCard
import com.jarod.card.domain.core.Rank
import com.jarod.card.domain.core.Suit
import com.jarod.card.domain.core.shuffled
import com.jarod.card.domain.engine.Game
import com.jarod.card.domain.engine.GameAction
import com.jarod.card.domain.engine.GameEvent
import com.jarod.card.domain.engine.GameResult
import com.jarod.card.domain.engine.GameTransition
import com.jarod.card.domain.engine.PlayerId
import com.jarod.card.domain.engine.PlayerRanking
import com.jarod.card.domain.engine.ValidationResult
import com.jarod.card.domain.engine.sumPoints

private fun ok() = ValidationResult.ok()
private fun reject(reason: String) = ValidationResult.reject(reason)

object CariocaGame : Game<CariocaState> {
    override val type = com.jarod.card.domain.engine.GameType.CARIOCA

    // ──────────────────────────────────────────────────────────────
    // Entrada: crear partida y repartir la primera ronda
    // ──────────────────────────────────────────────────────────────
    fun createGame(
        players: List<PlayerId>,
        ruleset: CariocaRuleset = CariocaRuleset(),
        seed: Long = System.currentTimeMillis()
    ): GameTransition<CariocaState> {
        require(players.size in ruleset.maxPlayers) { "Carioca: 2-4 jugadores" }
        val events = mutableListOf<GameEvent>()
        var st = CariocaState(
            ruleset = ruleset,
            seed = seed,
            phase = CariocaPhase.PREPARING,
            players = players,
            dealerSeat = 0,
            scores = players.associate { it to 0 },
            roundsWon = players.associate { it to 0 }
        )
        // Primera ronda: deal
        st = dealRound(st, events)
        return GameTransition(st.copy(phase = CariocaPhase.PLAYING), events.toList())
    }

    // ──────────────────────────────────────────────────────────────
    // Game interface
    // ──────────────────────────────────────────────────────────────
    override fun canPerform(state: CariocaState, action: GameAction): ValidationResult {
        if (state.phase != CariocaPhase.PLAYING) return reject("Fase no jugable")
        if (action !is CariocaAction) return reject("Acción no de Carioca")
        val a = action
        val p = a.playerId
        require(p in state.players) { "Jugador no existe" }
        val current = state.currentPlayer
        require(current != null) { "Sin jugador actual" }

        return when (a) {
            is DrawFromStock -> if (p == current && state.stage == Stage.DRAW && state.stock.isNotEmpty()) ok()
            else reject("No es tu turno o no hay mazo")

            is DrawFromDiscard -> if (p == current && state.stage == Stage.DRAW && state.discard.isNotEmpty()) ok()
            else reject("No es tu turno o pozo vacío")

            is MeldAction -> validateMeld(state, a)

            is DiscardAction -> if (p == current && state.stage == Stage.ACTIONS) {
                val hand = state.hands[p]!!
                val card = hand.find { it.id == a.cardId }
                if (card == null) reject("Carta no en mano")
                else if (card is JokerCard && state.ruleset.jokerRules.cannotDiscard) reject("No se descartan comodines")
                else ok()
            } else reject("No es tu turno o fase incorrecta")

            is LayOffAction -> validateLayOff(state, a)
        }
    }

    override fun perform(state: CariocaState, action: GameAction): GameTransition<CariocaState> {
        if (action !is CariocaAction) throw IllegalArgumentException("Acción no de Carioca")
        val a = action
        val res = canPerform(state, a)
        if (!res.valid) throw IllegalArgumentException(res.reason)
        return when (a) {
            is DrawFromStock -> draw(state, a, fromStock = true)
            is DrawFromDiscard -> draw(state, a, fromStock = false)
            is MeldAction -> meld(state, a)
            is DiscardAction -> discard(state, a)
            is LayOffAction -> layOff(state, a)
        }
    }

    override fun resolve(state: CariocaState): GameResult = state.result ?: buildResult(state)

    // ──────────────────────────────────────────────────────────────
    // Reparto (CCW: dealer último, primero a la derecha del dealer)
    // ──────────────────────────────────────────────────────────────
    private fun dealRound(state: CariocaState, events: MutableList<GameEvent>): CariocaState {
        val n = state.players.size
        val roundSeed = state.seed + state.roundIndex
        val shoe = build().shuffled(roundSeed)
        val handSize = state.ruleset.handSize

        // Orden de reparto CCW: dealer-1, dealer-2, ..., dealer
        val dealOrder = (0 until n).map { (state.dealerSeat - 1 - it + n) % n }
        val hands = mutableMapOf<PlayerId, MutableList<Card>>()
        state.players.forEach { hands[it] = mutableListOf() }
        var idx = 0
        repeat(handSize) {
            for (seat in dealOrder) {
                val p = state.players[seat]
                hands[p]!!.add(shoe[idx])
                idx++
            }
        }
        val stock = shoe.drop(idx)
        val discard = mutableListOf<Card>()
        if (stock.isNotEmpty()) {
            // Voltear la primera carta del mazo (rules.md §6)
            discard.add(stock[0])
        }
        val nextDealer = (state.dealerSeat - 1 + n) % n
        val firstToPlay = (state.dealerSeat + 1) % n

        events.add(RoundStarted(
            seq = state.seq + 1,
            round = state.roundIndex + 1,
            dealer = state.players[state.dealerSeat],
            handSize = handSize
        ))

        return state.copy(
            phase = CariocaPhase.PLAYING,
            hands = hands.mapValues { (k, v) -> v.toList() },
            stock = stock.drop(1).toList(),
            discard = discard.toList(),
            table = state.players.associate { it to emptyList() },
            meldedThisRound = emptySet(),
            meldedThisTurn = emptySet(),
            playedThisLap = emptySet(),
            currentPlayer = state.players[firstToPlay],
            stage = Stage.DRAW,
            dealerSeat = nextDealer,
            seq = state.seq + 1
        )
    }

    // ──────────────────────────────────────────────────────────────
    // Robar
    // ──────────────────────────────────────────────────────────────
    private fun draw(state: CariocaState, action: CariocaAction, fromStock: Boolean): GameTransition<CariocaState> {
        val p = action.playerId
        val card: Card
        val newStock: List<Card>
        val newDiscard: List<Card>
        if (fromStock) {
            card = state.stock.last()
            val remaining = state.stock.dropLast(1)
            if (remaining.isNotEmpty()) {
                newStock = remaining
                newDiscard = state.discard
            } else {
                // Mazo agotado: se recicla el pozo (excepto la carta superior)
                // como nuevo mazo (regla estándar de Carioca).
                val recycled = state.discard.dropLast(1).shuffled(state.seed + state.roundIndex * 31L + state.seq)
                newStock = recycled
                newDiscard = state.discard.takeLast(1)
            }
        } else {
            card = state.discard.last()
            newStock = state.stock
            newDiscard = state.discard.dropLast(1)
        }
        val newHand = state.hands[p]!! + card
        val newHands = state.hands + (p to newHand)
        val events = listOf(CardDrawn(state.seq + 1, p, fromStock, card.id))
        return GameTransition(
            state.copy(
                hands = newHands,
                stock = newStock,
                discard = newDiscard,
                stage = Stage.ACTIONS,
                seq = state.seq + 1
            ),
            events
        )
    }

    // ──────────────────────────────────────────────────────────────
    // Bajarse (MeldAction) — combos que cumplen la ronda actual
    // ──────────────────────────────────────────────────────────────
    private fun validateMeld(state: CariocaState, a: MeldAction): ValidationResult {
        val p = a.playerId
        val hand = state.hands[p]!!
        val allCards = a.groups.flatMap { it.cards }
        // Cartas están en mano y no repetidas
        val usedIds = allCards.map { it.id }
        if (usedIds.size != usedIds.distinct().size) return reject("Cartas repetidas en la jugada")
        if (allCards.any { c -> !hand.any { h -> h.id == c.id } }) return reject("Carta no en mano")
        // Jokers totales ≤ maxPerHand
        val jokers = allCards.count { it is JokerCard }
        if (jokers > state.ruleset.jokerRules.maxPerHand) return reject("Máx ${state.ruleset.jokerRules.maxPerHand} comodines por mano")
        // Cada grupo válido
        for (g in a.groups) {
            MeldValidator.validate(g.cards, state.ruleset) ?: return reject("Combinación inválida: ${g.cards.map { it.id }}")
        }
        // No duplicar tríos del mismo rango
        val newTable = state.table[p]!! + a.groups
        if (MeldValidator.hasDuplicateTripleRank(newTable)) return reject("No se valen juegos duplicados (dos tríos del mismo rango)")
        // Cumple la ronda actual
        val round = state.ruleset.rounds[state.roundIndex]
        if (!MeldValidator.roundSatisfied(newTable, round)) return reject("No cumple la ronda ${round.number}")
        return ok()
    }

    private fun meld(state: CariocaState, a: MeldAction): GameTransition<CariocaState> {
        val p = a.playerId
        val hand = state.hands[p]!!
        val usedIds = a.groups.flatMap { it.cards.map { it.id } }.toSet()
        val newHand = hand.filter { it.id !in usedIds }
        val newHands = state.hands + (p to newHand)
        val newTable = state.table + (p to (state.table[p]!! + a.groups))
        val newMelded = state.meldedThisRound + p
        val newEver = state.everMelded + p
        val groupIds = a.groups.map { it.cardIds() }
        val events = listOf(CardsMeld(state.seq + 1, p, groupIds))

        // ¿Gana la ronda tras bajar? (mano vacía — p.ej. Escala Real)
        val nextState = state.copy(
            hands = newHands,
            table = newTable,
            meldedThisRound = newMelded,
            meldedThisTurn = state.meldedThisTurn + p,
            everMelded = newEver,
            seq = state.seq + 1
        )
        return if (newHand.isEmpty()) {
            endRound(nextState, p, events)
        } else {
            GameTransition(nextState, events)
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Descartar (fin de turno)
    // ──────────────────────────────────────────────────────────────
    private fun discard(state: CariocaState, a: DiscardAction): GameTransition<CariocaState> {
        val p = a.playerId
        val hand = state.hands[p]!!
        val card = hand.first { it.id == a.cardId }
        val newHand = hand.filter { it.id != a.cardId }
        val newDiscard = state.discard + card
        val newHands = state.hands + (p to newHand)
        val nextPlayer = nextPlayer(state, p)
        val events = listOf(CardDiscarded(state.seq + 1, p, a.cardId))
        val playedThisLap = state.playedThisLap + p
        val newPlayedThisLap = if (playedThisLap.size == state.players.size) emptySet() else playedThisLap

        if (newHand.isEmpty()) {
            // Gana la ronda (corte)
            return endRound(state.copy(
                hands = newHands,
                discard = newDiscard,
                meldedThisTurn = emptySet(),
                playedThisLap = newPlayedThisLap,
                seq = state.seq + 1
            ), p, events)
        }
        return GameTransition(
            state.copy(
                hands = newHands,
                discard = newDiscard,
                meldedThisTurn = emptySet(),
                playedThisLap = newPlayedThisLap,
                currentPlayer = nextPlayer,
                stage = Stage.DRAW,
                seq = state.seq + 1
            ),
            events + TurnChanged(state.seq + 2, nextPlayer)
        )
    }

    // ──────────────────────────────────────────────────────────────
    // Añadir a juegos ajenos/propios (lay-off) — bajado y no en el
    // mismo turno en que te bajaste (rules.md §8)
    // ──────────────────────────────────────────────────────────────
    private fun validateLayOff(state: CariocaState, a: LayOffAction): ValidationResult {
        val p = a.playerId
        val hand = state.hands[p]!!
        val card = hand.find { it.id == a.cardId } ?: return reject("Carta no en mano")
        val ownerMelds = state.table[a.meldOwner] ?: return reject("Dueño no tiene juegos")
        if (a.meldIndex !in ownerMelds.indices) return reject("Índice de juego inválido")
        // Solo si el jugador ya se bajó alguna vez y no en este mismo turno (rules.md §8)
        val canLayOffToOthers = p in state.everMelded && p !in state.meldedThisTurn
        if (a.meldOwner != p && !canLayOffToOthers) return reject("Solo se puede añadir a juegos ajenos si ya te bajaste en un turno anterior")
        // Validar resultado
        val oldMeld = ownerMelds[a.meldIndex]
        val newMeld = MeldValidator.validate(oldMeld.cards + card, state.ruleset)
        return if (newMeld == null) reject("Añadir la carta rompe la combinación")
        else ok()
    }

    private fun layOff(state: CariocaState, a: LayOffAction): GameTransition<CariocaState> {
        val p = a.playerId
        val hand = state.hands[p]!!
        val card = hand.first { it.id == a.cardId }
        val newHand = hand.filter { it.id != a.cardId }
        val newHands = state.hands + (p to newHand)
        val ownerMelds = state.table[a.meldOwner]!!.toMutableList()
        val oldMeld = ownerMelds[a.meldIndex]
        val newMeld = MeldValidator.validate(oldMeld.cards + card, state.ruleset)!!
        ownerMelds[a.meldIndex] = newMeld
        val newTable = state.table + (a.meldOwner to ownerMelds)
        val events = listOf(CardLaidOff(state.seq + 1, p, a.cardId, a.meldOwner, a.meldIndex))

        // Si mano vacía tras lay-off → fin de ronda
        val nextState = state.copy(
            hands = newHands,
            table = newTable,
            seq = state.seq + 1
        )
        return if (newHand.isEmpty()) {
            endRound(nextState, p, events)
        } else {
            GameTransition(nextState, events)
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Fin de ronda / fin de partida
    // ──────────────────────────────────────────────────────────────
    private fun endRound(state: CariocaState, winner: PlayerId, priorEvents: List<GameEvent>): GameTransition<CariocaState> {
        val scores = state.players.associate { p ->
            if (p == winner) p to state.ruleset.scoringWinner // 0 estándar (ADR-018)
            else p to state.hands[p]!!.sumPoints()
        }
        val newScores = state.scores.map { (k, v) -> k to (v + scores[k]!!) }.toMap()
        val newRoundsWon = state.roundsWon.map { (k, v) -> k to (v + if (k == winner) 1 else 0) }.toMap()

        val roundEndEvent = RoundEnd(
            seq = state.seq + 1,
            winner = winner,
            pointsGained = scores
        )

        val isLastRound = state.roundIndex == state.ruleset.rounds.lastIndex
        val nextState = if (isLastRound) {
            val result = buildResult(state.copy(
                scores = newScores,
                roundsWon = newRoundsWon
            ))
            state.copy(
                phase = CariocaPhase.GAME_END,
                scores = newScores,
                roundsWon = newRoundsWon,
                winner = result.rankings.first().playerId,
                result = result,
                seq = state.seq + 2
            )
        } else {
            // Siguiente ronda: nuevo reparto
            val s = state.copy(
                phase = CariocaPhase.ROUND_END,
                roundIndex = state.roundIndex + 1,
                scores = newScores,
                roundsWon = newRoundsWon,
                seq = state.seq + 1
            )
            val events = mutableListOf<GameEvent>()
            val s2 = dealRound(s, events)
            return GameTransition(
                s2.copy(phase = CariocaPhase.PLAYING),
                priorEvents + roundEndEvent + events
            )
        }
        return GameTransition(
            nextState,
            priorEvents + roundEndEvent + GameEndEvent(state.seq + 2, nextState.result!!)
        )
    }

    private fun buildResult(state: CariocaState): GameResult {
        val rankings = state.players
            .map { p ->
                val s = state.scores[p]!!
                val w = state.roundsWon[p]!!
                PlayerRanking(p, s, w, 0)
            }
            .sortedWith(compareBy({ it.score }, { -it.roundsWon }))
            .withIndex()
            .map { (i, r) -> r.copy(rank = i + 1) }
        return GameResult(type, rankings)
    }

    // ──────────────────────────────────────────────────────────────
    // Utilidades
    // ──────────────────────────────────────────────────────────────
    private fun nextPlayer(state: CariocaState, current: PlayerId): PlayerId {
        val n = state.players.size
        val idx = state.players.indexOf(current)
        // Turno CCW (game-engine.md §6): índice - 1 módulo n
        return state.players[(idx - 1 + n) % n]
    }
}
