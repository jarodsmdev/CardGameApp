package com.jarod.card.features.game

import android.graphics.BlurMaskFilter
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jarod.card.core.ui.ConfirmDialog
import com.jarod.card.core.util.formatClock
import com.jarod.card.core.util.formatDuration
import com.jarod.card.core.util.plural
import com.jarod.card.domain.engine.PlayerId
import com.jarod.card.domain.engine.PlayerRanking
import com.jarod.card.domain.games.carioca.CariocaBot
import com.jarod.card.domain.games.carioca.CariocaPhase
import com.jarod.card.domain.games.carioca.CariocaRound
import com.jarod.card.domain.games.carioca.CariocaState
import com.jarod.card.domain.games.carioca.ComboSpec
import com.jarod.card.domain.games.carioca.ComboType
import com.jarod.card.domain.games.carioca.Meld
import com.jarod.card.domain.games.carioca.Stage
import com.jarod.card.features.game.cardskin.CardSkin
import com.jarod.card.features.game.stats.CumulativeStats
import com.jarod.card.features.game.stats.GameStats
import com.jarod.card.features.game.stats.RoundStats
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt

private val DiscardBadgeRed = Color(0xFFC62828)
private val PlayedCheckGreen = Color(0xFF2E7D32)
private val HandSelectionGold = Color(0xFFC9A227)
private val TimeoutBorderRed = Color(0xFFD32F2F)
private val MedalGold = Color(0xFFC9A227)
private val MedalSilver = Color(0xFF9CA3AF)
private val MedalBronze = Color(0xFFB07C3E)
private val MedalFourth = Color(0xFF6B7280)

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
private fun rememberPulse(): State<Float> {
    val transition = rememberInfiniteTransition(label = "turnPulse")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
}

/**
 * Aviso de poco tiempo: borde rojo difuminado que pulsa en todo el perímetro
 * de la pantalla. No consume gestos, la interacción sigue activa.
 */
@Composable
private fun TurnTimeoutVignette(active: Boolean, modifier: Modifier = Modifier) {
    if (!active) return
    val pulse by rememberPulse()
    Box(
        modifier
            .fillMaxSize()
            .drawWithCache {
                val paint = android.graphics.Paint().apply {
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 12.dp.toPx()
                    maskFilter = BlurMaskFilter(28.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                }
                val radius = 16.dp.toPx()
                onDrawBehind {
                    val alpha = 0.35f + 0.25f * pulse
                    paint.color = TimeoutBorderRed.copy(alpha = alpha).toArgb()
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawRoundRect(
                            0f, 0f, size.width, size.height, radius, radius, paint
                        )
                    }
                }
            }
    )
}

/**
 * Medalla de posición: cinta superior + disco, con el número de la posición
 * (1, 2, 3, 4) centrado. Deja claro quién va ganando (menos puntos en Carioca).
 */
@Composable
private fun RankMedal(rank: Int, modifier: Modifier = Modifier) {
    val color = when (rank) {
        1 -> MedalGold
        2 -> MedalSilver
        3 -> MedalBronze
        else -> MedalFourth
    }
    Box(modifier.size(22.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val discCenter = Offset(size.width / 2f, size.height * 0.60f)
            val radius = size.minDimension * 0.40f
            val ribbon = Path().apply {
                moveTo(size.width * 0.14f, size.height * 0.04f)
                lineTo(size.width * 0.30f, size.height * 0.04f)
                lineTo(size.width * 0.50f, size.height * 0.36f)
                lineTo(size.width * 0.70f, size.height * 0.04f)
                lineTo(size.width * 0.86f, size.height * 0.04f)
                lineTo(size.width * 0.50f, size.height * 0.52f)
                close()
            }
            drawPath(ribbon, lerp(color, Color.Black, 0.35f))
            drawCircle(color, radius = radius, center = discCenter)
            drawCircle(
                color = Color.White.copy(alpha = 0.45f),
                radius = radius,
                center = discCenter,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.20f),
                radius = radius * 0.70f,
                center = discCenter,
                style = Stroke(width = 1.dp.toPx())
            )
        }
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.offset(y = 2.dp)
        )
    }
}

@Composable
private fun ScoreChip(score: Int, rank: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RankMedal(rank)
        Spacer(Modifier.width(4.dp))
        Text("$score pts", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

/** Posición actual (1 = líder): menos puntos, desempate por más rondas ganadas (igual que el motor). */
private fun rankOf(st: CariocaState, p: PlayerId): Int {
    val ordered = st.players.sortedWith(
        compareBy({ st.scores[it] ?: Int.MAX_VALUE }, { -(st.roundsWon[it] ?: 0) }, { it.value })
    )
    return ordered.indexOf(p) + 1
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
                secondsLeft = ui.secondsLeft,
                onDrawStock = viewModel::drawFromStock,
                onDrawDiscard = viewModel::drawFromDiscard,
                onMeld = viewModel::autoMeld,
                onLayOff = viewModel::autoLayOff,
                onDiscard = viewModel::discard
            )
        }
        val human = ui.humanId
        val lowTime = st != null && human != null &&
            st.phase == CariocaPhase.PLAYING && st.currentPlayer == human &&
            ui.secondsLeft in 0..st.ruleset.turnTimeout.warningAtSeconds
        TurnTimeoutVignette(active = lowTime)
    }

    if (st?.phase == CariocaPhase.GAME_END && st.result != null) {
        GameEndDialog(
            st = st,
            humanId = ui.humanId,
            gameStats = ui.gameStats,
            cumulativeStats = ui.cumulativeStats,
            onRestart = viewModel::startGame
        )
    }

    // Diálogo de fin de ronda: solo cuando hay una ronda siguiente (la última
    // ronda conduce directamente a GameEndDialog, sin "Continuar").
    if (st?.phase == CariocaPhase.ROUND_END) {
        ui.roundEndInfo?.let { info ->
            RoundEndDialog(
                info = info,
                st = st,
                humanId = ui.humanId,
                roundSummary = ui.roundSummary,
                onContinue = { viewModel.clearRoundEnd() },
                onExit = { showExitDialog = true }
            )
        }
    }

    if (showExitDialog) {
        ConfirmDialog(
            title = "Salir de la partida",
            text = "¿Quieres salir? Perderás la partida en curso.",
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
    secondsLeft: Int,
    onDrawStock: () -> Unit,
    onDrawDiscard: () -> Unit,
    onMeld: () -> Unit,
    onLayOff: () -> Unit,
    onDiscard: (String) -> Unit
) {
    val myTurn = st.phase == CariocaPhase.PLAYING && st.currentPlayer == humanId
    val round = st.ruleset.rounds[st.roundIndex]
    val human = st.hands[humanId] ?: emptyList()

    var selectedCardId by remember { mutableStateOf<String?>(null) }
    val canSelect = myTurn && st.stage == Stage.ACTIONS
    LaunchedEffect(canSelect) {
        if (!canSelect && selectedCardId != null) selectedCardId = null
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        TopInfo(st, round, myTurn, botsThinking, error, roomId, secondsLeft)

        OpponentsRow(st, humanId)

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
        ) {
            TableSection(st, humanId, skin)
        }

        ActionBar(st, humanId, myTurn, selectedCardId, onMeld, onLayOff)

        StockDiscardRow(st, myTurn, skin, onDrawStock, onDrawDiscard)

        HandRow(
            st, humanId, myTurn, skin, onDiscard,
            selectedCardId = selectedCardId,
            onSelectionChange = { selectedCardId = it }
        )

        if (human.isEmpty()) {
            Text(
                text = "Sin cartas — esperando nueva ronda…",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), contentAlignment = Alignment.Center) {
            ScoreChip(score = st.scores[humanId] ?: 0, rank = rankOf(st, humanId))
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
    roomId: String,
    secondsLeft: Int
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Vuelta ${st.laps + 1}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            when {
                botsThinking -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(6.dp))
                    Text("Jugando…", style = MaterialTheme.typography.bodySmall)
                }
                myTurn -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Tu turno",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (secondsLeft >= 0) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "· ${secondsLeft}s",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (secondsLeft <= st.ruleset.turnTimeout.warningAtSeconds) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
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
                rank = rankOf(st, p),
                handCount = st.hands[p]!!.size,
                melded = p in st.meldedThisRound,
                played = p in st.playedThisLap,
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
    rank: Int,
    handCount: Int,
    melded: Boolean,
    played: Boolean,
    isTurn: Boolean,
    modifier: Modifier = Modifier
) {
    val pulse by rememberPulse()
    Surface(
        modifier = modifier
            .graphicsLayer {
                val s = if (isTurn) 1f + 0.03f * pulse else 1f
                scaleX = s
                scaleY = s
            },
        shape = RoundedCornerShape(10.dp),
        color = if (isTurn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isTurn) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Box(Modifier.padding(8.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (played) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Ya jugó",
                            modifier = Modifier.size(14.dp),
                            tint = PlayedCheckGreen
                        )
                    }
                }
                ScoreChip(score, rank)
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
                // Nombre en vertical dentro de un hueco cuadrado del alto de línea:
                // con la rotación alrededor del centro, las letras quedan pegadas al
                // borde izquierdo y el espaciado hacia las cartas se fija con un
                // Spacer, idéntico para todos los jugadores (antes un hueco ancho
                // dejaba demasiado margen al borde de la pantalla).
                Box(
                    modifier = Modifier.size(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = nameOf(owner, humanId),
                        modifier = Modifier
                            .wrapContentSize(unbounded = true)
                            .graphicsLayer { rotationZ = -90f },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.width(8.dp))
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
    val pulse by rememberPulse()
    val canDrawStock = myTurn && st.stage == Stage.DRAW && st.stock.isNotEmpty()
    val canDrawDiscard = myTurn && st.stage == Stage.DRAW && st.discard.isNotEmpty()
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
                    modifier = Modifier
                        .clickable(enabled = canDrawStock, onClick = onDrawStock)
                        .graphicsLayer {
                            val s = if (canDrawStock) 1f + 0.04f * pulse else 1f
                            scaleX = s
                            scaleY = s
                        },
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
                        modifier = Modifier
                            .clickable(enabled = canDrawDiscard, onClick = onDrawDiscard)
                            .graphicsLayer {
                                val s = if (canDrawDiscard) 1f + 0.04f * pulse else 1f
                                scaleX = s
                                scaleY = s
                            }
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
    selectedCardId: String?,
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
                text = if (selectedCardId != null)
                    "Swipe ↑ para descartar · Swipe ↓ para cancelar · Doble tap para descartar"
                else
                    "Toca para seleccionar · arrastra para ordenar",
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
    onDiscard: (String) -> Unit,
    selectedCardId: String?,
    onSelectionChange: (String?) -> Unit
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

    LaunchedEffect(idsSet, selectedCardId) {
        if (selectedCardId != null && selectedCardId !in idsSet) onSelectionChange(null)
    }

    val currentSelectedId by rememberUpdatedState(selectedCardId)
    var swipingCardId by remember { mutableStateOf<String?>(null) }
    var swipeDy by remember { mutableStateOf(0f) }
    var confirmedCardId by remember { mutableStateOf<String?>(null) }
    var lastTapCardId by remember { mutableStateOf<String?>(null) }
    var lastTapTime by remember { mutableStateOf(0L) }

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
        val selectionGapPx = with(density) { 8.dp.toPx() }
        val confirmUpPx = with(density) { 40.dp.toPx() }
        val cancelDownPx = with(density) { 28.dp.toPx() }
        val launchDistancePx = cardHeightPx * 2.5f
        val doubleTapWindowMs = 300L
        val maxArcRotation = 4f
        val availableWidth = maxWidth
        val maxWidthPx = with(density) { availableWidth.toPx() }
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
                        var swiping = false
                        swipingCardId = null
                        swipeDy = 0f
                        while (true) {
                            val e = awaitPointerEvent()
                            val c = e.changes.firstOrNull { it.id == pointer } ?: break
                            if (!c.pressed) {
                                if (pressIndex >= 0 && !dragging && !swiping && discardEnabled) {
                                    val now = SystemClock.uptimeMillis()
                                    val isDoubleTap = cardId == lastTapCardId &&
                                        now - lastTapTime <= doubleTapWindowMs
                                    if (isDoubleTap) {
                                        confirmedCardId = cardId
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    } else {
                                        val selectingNew = cardId != currentSelectedId
                                        onSelectionChange(if (selectingNew) cardId else null)
                                        if (selectingNew) {
                                            haptics.performHapticFeedback(
                                                HapticFeedbackType.TextHandleMove
                                            )
                                        }
                                    }
                                    lastTapCardId = cardId
                                    lastTapTime = now
                                } else if (swiping) {
                                    when (classifyHandSwipe(
                                        0f, swipeDy, slop, confirmUpPx, cancelDownPx
                                    )) {
                                        HandSwipe.UP -> {
                                            confirmedCardId = cardId
                                            haptics.performHapticFeedback(
                                                HapticFeedbackType.LongPress
                                            )
                                        }
                                        HandSwipe.DOWN -> {
                                            onSelectionChange(null)
                                            haptics.performHapticFeedback(
                                                HapticFeedbackType.TextHandleMove
                                            )
                                        }
                                        HandSwipe.NONE -> Unit
                                    }
                                }
                                break
                            }
                            if (pressIndex < 0) break
                            if (!dragging && !swiping) {
                                val dx = c.position.x - down.position.x
                                val dy = c.position.y - down.position.y
                                if (isVerticalDominant(dx, dy, slop) &&
                                    discardEnabled && cardId == currentSelectedId
                                ) {
                                    swiping = true
                                    swipingCardId = cardId
                                    swipeDy = dy
                                    c.consume()
                                } else if (abs(dx) > slop || abs(dy) > slop) {
                                    if (currentSelectedId != null) onSelectionChange(null)
                                    dragging = true
                                    dragIndex = pressIndex
                                    dragAnchor = down.position.x - cardLeft
                                    dragX = down.position.x
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            } else if (swiping) {
                                swipeDy = c.position.y - down.position.y
                                c.consume()
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
                        swipingCardId = null
                        swipeDy = 0f
                    }
                }
        )

        val selIndex = selectedCardId?.let { order.indexOf(it) } ?: -1

        order.forEachIndexed { index, cardId ->
            key(cardId) {
                val card = hand.first { it.id == cardId }
                val isDragged = dragIndex == index
                val isSelected = cardId == selectedCardId
                val isSwiping = cardId == swipingCardId
                val isConfirmed = cardId == confirmedCardId

                // La carta seleccionada separa ligeramente a sus vecinas.
                var slotTarget = startX + index * stepPx
                if (selIndex >= 0) {
                    slotTarget += when (index) {
                        selIndex - 1 -> -selectionGapPx
                        selIndex + 1 -> selectionGapPx
                        else -> 0f
                    }
                }

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
                    targetValue = if (isDragged || isSelected || isSwiping) 1f else 0f,
                    animationSpec = springSpec,
                    label = "lift"
                )

                // Al confirmar (swipe ↑ o doble tap) la carta sale volando hacia la mesa
                // y, al terminar, se ejecuta la acción (descartar al pozo).
                val launch = remember(cardId) { Animatable(0f) }
                LaunchedEffect(isConfirmed) {
                    if (isConfirmed) {
                        launch.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
                        confirmedCardId = null
                        onSelectionChange(null)
                        onDiscard(cardId)
                    }
                }

                val t = if (n > 1) (2f * index - (n - 1)) / (n - 1) else 0f
                val arcRaise = if (isDragged || isSelected || isSwiping)
                    0f else arcRaisePx * (1f - t * t)

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .zIndex(
                            when {
                                isConfirmed -> 2f
                                isDragged || isSelected || isSwiping -> 1f
                                else -> 0f
                            }
                        )
                        .graphicsLayer {
                            val baseY = -liftPx * lift - arcRaise
                            translationX = if (dragIndex == index) dragX - dragAnchor else x.value
                            // Durante el swipe la carta sigue el dedo 1:1 (arriba y abajo);
                            // la resolución solo se decide al soltar.
                            translationY = if (isSwiping) {
                                swipeDy
                            } else {
                                baseY - launch.value * launchDistancePx
                            }
                            rotationZ = if (isDragged || isSelected || isSwiping)
                                0f else t * maxArcRotation
                            transformOrigin = TransformOrigin(0.5f, 1f)
                            val extra = 0.12f * launch.value
                            scaleX = 1f + 0.07f * lift + extra
                            scaleY = 1f + 0.07f * lift + extra
                            alpha = 1f - launch.value
                            shadowElevation = shadowPx * lift
                            shape = RoundedCornerShape(6.dp)
                        }
                ) {
                    CardFace(
                        card = card,
                        width = cardWidth,
                        height = cardHeight,
                        skin = skin,
                        modifier = Modifier
                    )
                    if (isSelected) {
                        Box(
                            Modifier
                                .matchParentSize()
                                .border(2.dp, HandSelectionGold, RoundedCornerShape(6.dp))
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Diálogo de fin de partida
// ──────────────────────────────────────────────────────────────

@Composable
private fun GameEndDialog(
    st: CariocaState,
    humanId: PlayerId?,
    gameStats: GameStats?,
    cumulativeStats: CumulativeStats,
    onRestart: () -> Unit
) {
    val rankings = st.result?.rankings.orEmpty().sortedBy { it.rank }
    // Construir entries para el Scoreboard
    val entries = rankings.map { r ->
        val isMe = humanId != null && r.playerId == humanId
        ScoreboardEntry(
            rank = r.rank,
            playerId = r.playerId,
            name = nameOf(r.playerId, humanId),
            totalScore = r.score,
            roundsWon = r.roundsWon,
            totalRounds = st.ruleset.rounds.size,
            isCurrentPlayer = isMe,
            isRoundWinner = r.rank == 1
        )
    }

    ScoreboardDialog(
        title = "Fin de la partida",
        onDismissRequest = {}
    ) {
        Scoreboard(
            entries = entries,
            gameStats = gameStats,
            cumulativeStats = cumulativeStats,
            onDismiss = onRestart
        )
    }
}

// ──────────────────────────────────────────────────────────────
// Diálogo de fin de ronda
// ──────────────────────────────────────────────────────────────

@Composable
private fun RoundEndDialog(
    info: RoundEndInfo,
    st: CariocaState,
    humanId: PlayerId?,
    roundSummary: RoundStats?,
    onContinue: () -> Unit,
    onExit: () -> Unit
) {
    // Construir entries del scoreboard con scores actuales
    val rankings = st.players
        .map { p ->
            val s = st.scores[p]!!
            val w = st.roundsWon[p]!!
            PlayerRanking(p, s, w, 0)
        }
        .sortedWith(compareBy({ it.score }, { -it.roundsWon }))
        .withIndex()
        .map { (i, r) -> r.copy(rank = i + 1) }

    val entries = rankings.map { r ->
        val isMe = humanId != null && r.playerId == humanId
        ScoreboardEntry(
            rank = r.rank,
            playerId = r.playerId,
            name = nameOf(r.playerId, humanId),
            totalScore = r.score,
            roundsWon = r.roundsWon,
            totalRounds = info.round,
            isCurrentPlayer = isMe,
            roundPoints = info.pointsGained[r.playerId] ?: 0,
            isRoundWinner = r.playerId == info.winner
        )
    }

    ScoreboardDialog(
        title = "Fin de la ronda ${info.round}",
        onDismissRequest = {},
        actions = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onExit) { Text("Salir de la partida") }
                Spacer(Modifier.weight(1f))
                Button(onClick = onContinue) { Text("Continuar") }
            }
        }
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
            // Ganador de la ronda
            val winnerName = nameOf(info.winner, humanId)
            val pointsGained = info.pointsGained[info.winner] ?: 0
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = MedalGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$winnerName gana la ronda (${pointsGained} pts)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MedalGold,
                    textAlign = TextAlign.Center
                )
            }

            // Objetivo de la ronda (lo que todos debían bajar, ej. "2 tríos")
            val round = st.ruleset.rounds[st.roundIndex]
            RoundObjectiveRow(objective = describeRoundObjective(round))

            // Resumen congelado de la ronda (duración y turnos)
            roundSummary?.let { summary ->
                RoundSummaryHeader(summary = summary)
            }

            // Scoreboard compacto (sin "Jugar de nuevo": esa acción solo
            // corresponde al final de la partida, en GameEndDialog)
            Scoreboard(
                entries = entries,
                gameStats = null,
                cumulativeStats = null,
                onDismiss = null
            )
        }
    }
}

/** Diálogo a ancho casi completo para el scoreboard: aprovecha la pantalla
 *  para que las cards de jugadores se vean grandes, sin los márgenes anchos
 *  que deja un AlertDialog por defecto. */
@Composable
private fun ScoreboardDialog(
    title: String,
    onDismissRequest: () -> Unit,
    actions: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val maxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.92f
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .heightIn(max = maxHeight),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    content()
                }
                actions()
            }
        }
    }
}

/**
 * Resumen congelado de la ronda terminada (duración y turnos), con una breve
 * animación de entrada. Los valores vienen de RoundStats ya cerrado en
 * ROUND_END, así que no cambian mientras el scoreboard esté abierto.
 */
@Composable
private fun RoundSummaryHeader(summary: RoundStats) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val animAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "roundSummaryAlpha"
    )
    val animScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "roundSummaryScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = animAlpha
                scaleX = animScale
                scaleY = animScale
            }
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = "RONDA TERMINADA",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(vertical = 12.dp)) {
                SummaryStat(
                    icon = Icons.Filled.AccessTime,
                    contentDescription = "Duración",
                    label = "Duración",
                    value = formatClock(summary.elapsedMillis)
                )
                SummaryStat(
                    icon = Icons.Filled.Loop,
                    contentDescription = "Vueltas",
                    label = "Vueltas",
                    value = summary.laps.toString()
                )
            }
        }
    }
}

@Composable
private fun RowScope.SummaryStat(
    icon: ImageVector,
    contentDescription: String,
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Fila del objetivo de la ronda (lo que todos debían bajar, ej. "2 tríos"). */
@Composable
private fun RoundObjectiveRow(objective: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.TrackChanges,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "Objetivo de la ronda: $objective",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ──────────────────────────────────────────────────────────────
// Scoreboard genérico (reutilizable en cualquier juego)
// ──────────────────────────────────────────────────────────────

/** Entrada del scoreboard para un jugador. */
data class ScoreboardEntry(
    val rank: Int,
    val playerId: PlayerId,
    val name: String,
    val totalScore: Int,
    val roundsWon: Int,
    /** Rondas jugadas hasta ahora: el denominador del contador "X/Y rondas ganadas". */
    val totalRounds: Int,
    val isCurrentPlayer: Boolean = false,
    /** Puntos obtenidos en la ronda que acaba de terminar. Null si no aplica. */
    val roundPoints: Int? = null,
    /** Si este jugador es el ganador de la ronda (o campeón al final de la partida). */
    val isRoundWinner: Boolean = false,
)

/** Descripción del objetivo de la ronda (lo que todos deben bajar, ej. "2 tríos"). */
private fun describeRoundObjective(round: CariocaRound): String =
    round.combos.joinToString(" · ") { spec ->
        when (spec.type) {
            ComboType.TRIPLE -> plural(spec.count, "trío")
            ComboType.RUN -> if (spec.exactLength == 13) {
                plural(spec.count, "escala real")
            } else {
                plural(spec.count, "escala")
            }
        }
    }

/**
 * Card de un jugador en el scoreboard: medalla + nombre en la cabecera y,
 * debajo, dos columnas etiquetadas — "Esta ronda" y "Puntaje total" — para que
 * ambos conceptos se diferencien de un vistazo. El ganador destaca con borde
 * dorado, fondo tenue y chip de trofeo.
 */
@Composable
private fun ScoreboardPlayerCard(
    entry: ScoreboardEntry,
    revealProgress: Float
) {
    val medalColor = when (entry.rank) {
        1 -> MedalGold
        2 -> MedalSilver
        3 -> MedalBronze
        else -> MedalFourth
    }
    val containerColor = when {
        entry.isRoundWinner -> MedalGold.copy(alpha = 0.12f)
        entry.isCurrentPlayer -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
    }

    // Pulso del borde dorado del ganador (va de tenue a intenso y vuelve)
    val pulse = rememberInfiniteTransition(label = "winnerPulse")
    val borderAlpha by pulse.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "winnerBorderAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .graphicsLayer {
                alpha = revealProgress
                translationY = (1f - revealProgress) * 24f.dp.toPx()
            }
            .then(
                if (entry.isRoundWinner) Modifier.drawWithContent {
                    drawContent()
                    // Efecto inset: segunda línea dorada desplazada 2dp hacia
                    // abajo-derecha, que da sensación de profundidad/sombra sin
                    // el blur gris de la elevación de la Card.
                    val d = 2.dp.toPx()
                    drawRoundRect(
                        color = MedalGold.copy(alpha = 0.28f),
                        topLeft = Offset(d, d),
                        size = Size(size.width - d * 2, size.height - d * 2),
                        cornerRadius = CornerRadius(13.dp.toPx(), 13.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (entry.isRoundWinner) BorderStroke(1.dp, MedalGold.copy(alpha = borderAlpha)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            // Cabecera: medalla + nombre + chip de ganador
            Row(verticalAlignment = Alignment.CenterVertically) {
                RankMedal(entry.rank)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (entry.isCurrentPlayer) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "(Tú)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (entry.isRoundWinner) {
                            Spacer(Modifier.width(6.dp))
                            WinnerChip()
                        }
                    }
                    RoundsWonStat(roundsWon = entry.roundsWon, totalRounds = entry.totalRounds)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 10.dp))
            // Puntajes etiquetados: esta ronda (principal) vs total
            Row {
                if (entry.roundPoints != null) {
                    ScoreStatColumn(
                        label = "Esta ronda",
                        value = entry.roundPoints,
                        valueColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    ScoreStatColumn(
                        label = "Puntaje total",
                        value = entry.totalScore,
                        valueColor = medalColor,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    ScoreStatColumn(
                        label = "Puntaje total",
                        value = entry.totalScore,
                        valueColor = medalColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.ScoreStatColumn(
    label: String,
    value: Int,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        AnimatedScore(
            value = value,
            style = MaterialTheme.typography.headlineSmall,
            color = valueColor
        )
    }
}

/** Valor con conteo animado (queda fijo al terminar la animación). */
@Composable
private fun AnimatedScore(
    value: Int,
    style: TextStyle,
    color: Color
) {
    val animated = remember { Animatable(0f) }
    LaunchedEffect(value) {
        animated.animateTo(
            targetValue = value.toFloat(),
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
    }
    Text(
        text = "${animated.value.roundToInt()} pts",
        style = style,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

/** Contador de rondas ganadas en lenguaje natural: "Ganó 2 de 7 rondas". */
@Composable
private fun RoundsWonStat(roundsWon: Int, totalRounds: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.EmojiEvents,
            contentDescription = "Rondas ganadas",
            tint = MedalGold,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = "Ganó $roundsWon de $totalRounds ${if (totalRounds == 1) "ronda" else "rondas"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Chip dorado "Ganador" con trofeo. */
@Composable
private fun WinnerChip() {
    Row(
        modifier = Modifier
            .background(MedalGold.copy(alpha = 0.15f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.EmojiEvents,
            contentDescription = "Ganador",
            tint = MedalGold,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "Ganador",
            style = MaterialTheme.typography.labelSmall,
            color = MedalGold,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Scoreboard genérico: tabla de posiciones con medalla, stats de la partida
 * y stats acumuladas.
 *
 * Uso típico: fin de partida (GAME_END), lobby de resultados, perfil de jugador.
 */
@Composable
fun Scoreboard(
    entries: List<ScoreboardEntry>,
    gameStats: GameStats? = null,
    cumulativeStats: CumulativeStats? = null,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
) {
    Column(modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        // ─── Cabecera: cards de jugadores (medalla + nombre + puntajes) ───
        Column {
            entries.forEachIndexed { index, entry ->
                var revealed by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(index * 90L)
                    revealed = true
                }
                val revealProgress by animateFloatAsState(
                    targetValue = if (revealed) 1f else 0f,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                    label = "cardReveal$index"
                )
                ScoreboardPlayerCard(entry = entry, revealProgress = revealProgress)
            }
        }

        // ─── Stats de la partida ───
        gameStats?.let { gs ->
            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            Text("Estadísticas de la partida", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Column {
                gs.rounds.forEach { rs ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("Ronda ${rs.round}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text("${plural(rs.laps, "vuelta")} · ${plural(rs.turns, "turno")} · ${formatDuration(rs.elapsedMillis)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text("Total", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("${plural(gs.totalLaps, "vuelta")} · ${plural(gs.totalTurns, "turno")} · ${formatDuration(gs.totalTimeMillis)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                }
            }
        }

        // ─── Stats acumuladas ───
        cumulativeStats?.let { cs ->
            if (cs.gamesPlayed > 0) {
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
                Text("Tus totales (${plural(cs.gamesPlayed, "partida")})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text("${plural(cs.roundsPlayed, "ronda")} · ${plural(cs.laps, "vuelta")} · ${plural(cs.turns, "turno")} · ${formatDuration(cs.totalTimeMillis)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                }
            }
        }

        // Botón de acción si se provee
        onDismiss?.let {
            Spacer(Modifier.height(24.dp))
            TextButton(onClick = it, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("Jugar de nuevo", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
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
