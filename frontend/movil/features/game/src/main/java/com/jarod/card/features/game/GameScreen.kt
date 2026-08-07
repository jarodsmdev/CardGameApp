package com.jarod.card.features.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jarod.card.core.ui.ConfirmDialog
import com.jarod.card.domain.engine.PlayerId
import com.jarod.card.domain.games.carioca.CariocaBot
import com.jarod.card.domain.games.carioca.CariocaPhase
import com.jarod.card.domain.games.carioca.CariocaRound
import com.jarod.card.domain.games.carioca.CariocaState
import com.jarod.card.domain.games.carioca.ComboSpec
import com.jarod.card.domain.games.carioca.ComboType
import com.jarod.card.domain.games.carioca.Meld
import com.jarod.card.domain.games.carioca.Stage
import com.jarod.card.features.game.cardskin.CardSkin
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

private val DiscardBadgeRed = Color(0xFFC62828)

@Composable
private fun BoxScope.CountBadge(
    count: Int,
    offsetX: Dp = 6.dp,
    offsetY: Dp = (-8).dp
) {
    Text(
        text = "$count",
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = offsetX, y = offsetY)
            .background(DiscardBadgeRed, CircleShape)
            .padding(horizontal = 7.dp, vertical = 1.dp),
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ScoreChip(score: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Puntos",
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(4.dp))
        Text("$score pts", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun GameScreen(
    roomId: String,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val st = ui.state

    var showExitDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = !showExitDialog) { showExitDialog = true }

    LaunchedEffect(ui.error) {
        if (ui.error != null) {
            delay(3000)
            viewModel.clearError()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (st == null || ui.humanId == null) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else {
            CariocaBoard(
                st = st,
                humanId = ui.humanId!!,
                botsThinking = ui.botsThinking,
                error = ui.error,
                roomId = roomId,
                skin = ui.skin,
                onDrawStock = viewModel::drawFromStock,
                onDrawDiscard = viewModel::drawFromDiscard,
                onMeld = viewModel::autoMeld,
                onLayOff = viewModel::autoLayOff,
                onDiscard = viewModel::discard
            )
        }
    }

    if (st?.phase == CariocaPhase.GAME_END && st.result != null) {
        GameEndDialog(st = st, humanId = ui.humanId, onRestart = viewModel::startGame)
    }

    if (showExitDialog) {
        ConfirmDialog(
            title = "Salir de la partida",
            text = "¿Quieres salir? Perderás la partida en curso.",
            confirmText = "Salir",
            onConfirm = {
                showExitDialog = false
                onExit()
            },
            onDismiss = { showExitDialog = false }
        )
    }
}

@Composable
private fun CariocaBoard(
    st: CariocaState,
    humanId: PlayerId,
    botsThinking: Boolean,
    error: String?,
    roomId: String,
    skin: CardSkin,
    onDrawStock: () -> Unit,
    onDrawDiscard: () -> Unit,
    onMeld: () -> Unit,
    onLayOff: () -> Unit,
    onDiscard: (String) -> Unit
) {
    val myTurn = st.phase == CariocaPhase.PLAYING && st.currentPlayer == humanId
    val round = st.ruleset.rounds[st.roundIndex]
    val human = st.hands[humanId] ?: emptyList()

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        TopInfo(st, round, myTurn, botsThinking, error, roomId)

        OpponentsRow(st, humanId)

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
        ) {
            TableSection(st, humanId, skin)
        }

        ActionBar(st, humanId, myTurn, onMeld, onLayOff)

        StockDiscardRow(st, myTurn, skin, onDrawStock, onDrawDiscard)

        HandRow(st, humanId, myTurn, skin, onDiscard)

        if (human.isEmpty()) {
            Text(
                text = "Sin cartas — esperando nueva ronda…",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), contentAlignment = Alignment.Center) {
            ScoreChip(st.scores[humanId] ?: 0)
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Secciones
// ──────────────────────────────────────────────────────────────

@Composable
private fun TopInfo(
    st: CariocaState,
    round: CariocaRound,
    myTurn: Boolean,
    botsThinking: Boolean,
    error: String?,
    roomId: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Ronda ${st.roundIndex + 1}/${st.ruleset.rounds.size} · ${describeRound(round)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(text = "Sala $roomId", style = MaterialTheme.typography.bodySmall)
        }
        when {
            botsThinking -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(6.dp))
                Text("Jugando…", style = MaterialTheme.typography.bodySmall)
            }
            myTurn -> Text(
                text = "Tu turno",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
    if (error != null) {
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun OpponentsRow(st: CariocaState, humanId: PlayerId) {
    val opponents = st.players.filter { it != humanId }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opponents.forEach { p ->
            PlayerCard(
                name = nameOf(p, humanId),
                score = st.scores[p] ?: 0,
                handCount = st.hands[p]!!.size,
                melded = p in st.meldedThisRound,
                isTurn = st.currentPlayer == p && st.phase == CariocaPhase.PLAYING,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PlayerCard(
    name: String,
    score: Int,
    handCount: Int,
    melded: Boolean,
    isTurn: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (isTurn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isTurn) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Box(Modifier.padding(8.dp)) {
            Column {
                Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                ScoreChip(score)
            }
            CountBadge(handCount, offsetX = 4.dp, offsetY = (-6).dp)
        }
    }
}

@Composable
private fun TableSection(st: CariocaState, humanId: PlayerId, skin: CardSkin) {
    val meldsByPlayer = st.table.filterValues { it.isNotEmpty() }
    Text("Mesa", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    if (meldsByPlayer.isEmpty()) {
        Text(
            text = "Nadie se ha bajado todavía.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        meldsByPlayer.forEach { (owner, melds) ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nameOf(owner, humanId),
                    modifier = Modifier.graphicsLayer { rotationZ = -90f },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    melds.forEach { meld ->
                        MeldRow(meld, skin)
                    }
                }
            }
        }
    }
}

@Composable
private fun MeldRow(meld: Meld, skin: CardSkin) {
    // Las cartas del meld se apilan una encima de otra (mismo solapamiento que la
    // mano del jugador); la separación entre melds se mantiene en TableSection.
    Row(
        modifier = Modifier.padding(end = 12.dp),
        horizontalArrangement = Arrangement.spacedBy((-18).dp)
    ) {
        meld.cards.forEach { card ->
            CardFace(card = card, width = 44.dp, height = 62.dp, skin = skin)
        }
    }
}

@Composable
private fun StockDiscardRow(
    st: CariocaState,
    myTurn: Boolean,
    skin: CardSkin,
    onDrawStock: () -> Unit,
    onDrawDiscard: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mazo
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // El reverso mostrado es el del mazo al que pertenece la carta de
            // arriba: al robar cambia y alterna entre los 2 diseños de reverso.
            val top = st.stock.lastOrNull()
            Box {
                CardBack(
                    modifier = Modifier.clickable(
                        enabled = myTurn && st.stage == Stage.DRAW && st.stock.isNotEmpty(),
                        onClick = onDrawStock
                    ),
                    skin = skin,
                    deckIndex = top?.setIndex ?: 0
                )
                if (st.stock.isNotEmpty()) {
                    CountBadge(st.stock.size)
                }
            }
        }
        // Pozo
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val top = st.discard.lastOrNull()
            Box {
                if (top != null) {
                    CardFace(
                        card = top,
                        skin = skin,
                        modifier = Modifier.clickable(
                            enabled = myTurn && st.stage == Stage.DRAW && st.discard.isNotEmpty(),
                            onClick = onDrawDiscard
                        )
                    )
                } else {
                    CardBack(width = 44.dp, height = 62.dp, skin = skin)
                }
                if (st.discard.isNotEmpty()) {
                    CountBadge(st.discard.size)
                }
            }
        }
    }
}

@Composable
private fun ActionBar(
    st: CariocaState,
    humanId: PlayerId,
    myTurn: Boolean,
    onMeld: () -> Unit,
    onLayOff: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        if (myTurn && st.stage == Stage.ACTIONS) {
            val human = st.hands[humanId] ?: emptyList()
            val round = st.ruleset.rounds[st.roundIndex]
            val canMeld = humanId !in st.meldedThisRound &&
                CariocaBot.findMeldForRound(human, round) != null
            val canLayOff = CariocaBot.findLayOff(st, humanId) != null
            Text(
                text = "Arrastra una carta para ordenar · toca para descartar",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onMeld, enabled = canMeld) {
                    Text("Bajarse")
                }
                Button(onClick = onLayOff, enabled = canLayOff) {
                    Text("Añadir a mesa")
                }
            }
        } else if (!myTurn) {
            Text(
                text = "Esperando a los demás…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HandRow(
    st: CariocaState,
    humanId: PlayerId,
    myTurn: Boolean,
    skin: CardSkin,
    onDiscard: (String) -> Unit
) {
    val hand = st.hands[humanId] ?: emptyList()
    if (hand.isEmpty()) return

    var order by remember { mutableStateOf<List<String>>(emptyList()) }
    val ids = hand.map { it.id }
    val idsSet = ids.toSet()
    if (order.toSet() != idsSet) {
        val kept = order.filter { it in idsSet }
        val added = ids.filter { it !in order }
        order = kept + added
    }

    val cardWidth = 44.dp
    val cardHeight = 62.dp
    val liftHeight = 18.dp
    val discardEnabled = myTurn && st.stage == Stage.ACTIONS
    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight + liftHeight + 8.dp)
    ) {
        val density = LocalDensity.current
        val cardWidthPx = with(density) { cardWidth.toPx() }
        val cardHeightPx = with(density) { cardHeight.toPx() }
        val stepMinPx = with(density) { 18.dp.toPx() }
        val stepMaxPx = with(density) { 50.dp.toPx() }
        val liftPx = with(density) { liftHeight.toPx() }
        val shadowPx = with(density) { 16.dp.toPx() }
        val arcRaisePx = with(density) { 6.dp.toPx() }
        val maxArcRotation = 4f
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val n = order.size

        // La mano completa siempre cabe: el paso de superposición se calcula para que
        // los n rangos quepan en el ancho disponible (n ≤ 13).
        val stepPx = if (n > 1) ((maxWidthPx - cardWidthPx) / (n - 1)).coerceIn(stepMinPx, stepMaxPx) else 0f
        val totalWidthPx = cardWidthPx + (n - 1) * stepPx
        val startX = ((maxWidthPx - totalWidthPx) / 2f).coerceAtLeast(0f)

        val haptics = LocalHapticFeedback.current
        var dragIndex by remember { mutableStateOf(-1) }
        var dragX by remember { mutableStateOf(0f) }
        var dragAnchor by remember { mutableStateOf(0f) }

        // El gesto vive en el CONTENEDOR (coordenadas estables): así la carta sigue al
        // dedo 1:1, porque la posición no se mide respecto a la carta en movimiento.
        Box(
            Modifier
                .matchParentSize()
                .pointerInput(n, startX, stepPx, cardWidthPx, cardHeightPx, discardEnabled) {
                    val slop = viewConfiguration.touchSlop
                    val boxHeightPx = size.height.toFloat()

                    fun pressedCard(x: Float, y: Float): Int {
                        if (y < boxHeightPx - cardHeightPx) return -1
                        if (x < startX || x > startX + totalWidthPx) return -1
                        return if (n == 1) 0
                        else ((x - startX) / stepPx).toInt().coerceIn(0, n - 1)
                    }

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val pointer = down.id
                        val pressIndex = pressedCard(down.position.x, down.position.y)
                        val cardId = if (pressIndex >= 0) order[pressIndex] else null
                        val cardLeft = if (pressIndex >= 0) startX + pressIndex * stepPx else 0f
                        var dragging = false
                        while (true) {
                            val e = awaitPointerEvent()
                            val c = e.changes.firstOrNull { it.id == pointer } ?: break
                            if (!c.pressed) {
                                if (pressIndex >= 0 && !dragging && discardEnabled) onDiscard(cardId!!)
                                break
                            }
                            if (pressIndex < 0) break
                            if (!dragging) {
                                val dx = c.position.x - down.position.x
                                val dy = c.position.y - down.position.y
                                if (abs(dx) > slop || abs(dy) > slop) {
                                    dragging = true
                                    dragIndex = pressIndex
                                    dragAnchor = down.position.x - cardLeft
                                    dragX = down.position.x
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            } else {
                                dragX = c.position.x
                                if (n > 1) {
                                    val center = dragX - dragAnchor + cardWidthPx / 2f
                                    val target = ((center - startX - cardWidthPx / 2f) / stepPx)
                                        .roundToInt()
                                        .coerceIn(0, order.lastIndex)
                                    if (target != dragIndex) {
                                        order = order.toMutableList().also {
                                            val moved = it.removeAt(dragIndex)
                                            it.add(target, moved)
                                        }
                                        dragIndex = target
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                }
                                c.consume()
                            }
                        }
                        dragIndex = -1
                    }
                }
        )

        order.forEachIndexed { index, cardId ->
            key(cardId) {
                val card = hand.first { it.id == cardId }
                val isDragged = dragIndex == index
                val slotTarget = startX + index * stepPx
                val x = remember(cardId) { Animatable(0f) }
                var initDone by remember(cardId) { mutableStateOf(false) }

                // El Animatable se mantiene en la posición del dedo durante el arrastre
                // (para que al soltar la carta aterrice desde ahí), pero la posición
                // RENDERIZADA lee dragX directamente en graphicsLayer → 0 frames de lag.
                LaunchedEffect(isDragged, dragX, dragAnchor) {
                    if (isDragged) x.snapTo(dragX - dragAnchor)
                }
                // Al soltar (o al reordenar en vivo) las cartas se deslizan con spring.
                LaunchedEffect(isDragged, slotTarget) {
                    if (isDragged) {
                        x.snapTo(dragX - dragAnchor)
                    } else if (!initDone) {
                        x.snapTo(slotTarget)
                        initDone = true
                    } else {
                        x.animateTo(slotTarget, springSpec)
                    }
                }

                val lift by animateFloatAsState(
                    targetValue = if (isDragged) 1f else 0f,
                    animationSpec = springSpec,
                    label = "lift"
                )

                val t = if (n > 1) (2f * index - (n - 1)) / (n - 1) else 0f
                val arcRaise = if (isDragged) 0f else arcRaisePx * (1f - t * t)

                CardFace(
                    card = card,
                    width = cardWidth,
                    height = cardHeight,
                    skin = skin,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .zIndex(if (isDragged) 1f else 0f)
                        .graphicsLayer {
                            translationX = if (dragIndex == index) dragX - dragAnchor else x.value
                            translationY = -liftPx * lift - arcRaise
                            rotationZ = if (isDragged) 0f else t * maxArcRotation
                            transformOrigin = TransformOrigin(0.5f, 1f)
                            scaleX = 1f + 0.07f * lift
                            scaleY = 1f + 0.07f * lift
                            shadowElevation = shadowPx * lift
                            shape = RoundedCornerShape(6.dp)
                        }
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Diálogo de fin de partida
// ──────────────────────────────────────────────────────────────

@Composable
private fun GameEndDialog(st: CariocaState, humanId: PlayerId?, onRestart: () -> Unit) {
    val rankings = st.result?.rankings.orEmpty().sortedBy { it.rank }
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Fin de la partida") },
        text = {
            Column {
                rankings.forEach { r ->
                    Text(
                        text = "${r.rank}. ${nameOf(r.playerId, humanId)} — ${r.score} pts (${r.roundsWon} rondas)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onRestart) { Text("Jugar de nuevo") }
        }
    )
}

// ──────────────────────────────────────────────────────────────
// Helpers
// ──────────────────────────────────────────────────────────────

private fun nameOf(id: PlayerId, humanId: PlayerId?): String {
    if (id == humanId) return "Tú"
    return id.value.replace("bot", "Bot ").replaceFirstChar { it.uppercase() }
}

private fun describeRound(round: CariocaRound): String =
    round.combos.joinToString(" + ") { describeCombo(it) }

private fun describeCombo(spec: ComboSpec): String {
    val countText = if (spec.count == 1) "1 " else "${spec.count} "
    return when (spec.type) {
        ComboType.TRIPLE -> "${countText}trío${if (spec.count > 1) "s" else ""}"
        ComboType.RUN -> when (spec.exactLength) {
            13 -> "Escala Real"
            else -> "${countText}escala${if (spec.count > 1) "s" else ""}"
        }
    }
}
