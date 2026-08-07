package com.jarod.card.features.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarod.card.domain.core.Card
import com.jarod.card.domain.core.CardColor
import com.jarod.card.domain.core.JokerCard
import com.jarod.card.domain.core.JokerType
import com.jarod.card.domain.core.PlayingCard
import com.jarod.card.domain.core.Suit
import com.jarod.card.domain.core.color
import com.jarod.card.features.game.cardskin.BackDesign
import com.jarod.card.features.game.cardskin.CardSkin
import com.jarod.card.features.game.cardskin.FrontDesign
import com.jarod.card.features.game.cardskin.JokerStyle
import kotlin.math.ceil

private val CardRed = Color(0xFFC62828)
private val CardBlack = Color(0xFF1B1B1B)
private val CardWhite = Color(0xFFFFFFFF)
private val CardBorder = Color(0xFFC4C4C4)
private val JokerPurple = Color(0xFF6A1B9A)
private val JokerGray = Color(0xFF757575)

private fun suitGlyph(suit: Suit) = when (suit) {
    Suit.HEART -> "\u2665"
    Suit.DIAMOND -> "\u2666"
    Suit.SPADE -> "\u2660"
    Suit.CLUB -> "\u2663"
}

private fun FrontDesign.borderColor(): Color = when (this) {
    FrontDesign.CLASICO -> CardBorder
    FrontDesign.ROJO -> Color(0xFFC62828)
    FrontDesign.AZUL -> Color(0xFF1E88E5)
    FrontDesign.DORADO -> Color(0xFFF9A825)
}

private fun JokerStyle.textColor(): Color = when (this) {
    JokerStyle.COLOR -> JokerPurple
    JokerStyle.MONO -> CardBlack
    JokerStyle.ORO -> Color(0xFFF9A825)
}

private fun BackDesign.baseColor(): Color = when (this) {
    BackDesign.AZUL -> Color(0xFF1E88E5)
    BackDesign.ROJO -> Color(0xFFC62828)
    BackDesign.VERDE -> Color(0xFF2E7D32)
    BackDesign.NEGRO -> Color(0xFF1B1B1B)
    BackDesign.ROMBOS -> Color(0xFF1565C0)
    BackDesign.RAYAS -> Color(0xFF8E24AA)
    BackDesign.ANILLOS -> Color(0xFF00838F)
}

private fun BackDesign.accentColor(): Color = when (this) {
    BackDesign.ROMBOS -> Color(0xFF42A5F5)
    BackDesign.RAYAS -> Color(0xFFCE93D8)
    BackDesign.ANILLOS -> Color(0xFF4DD0E1)
    else -> baseColor()
}

private fun BackDesign.borderColor(): Color = when (this) {
    BackDesign.AZUL -> Color(0xFFBBDEFB)
    BackDesign.ROJO -> Color(0xFFFFCDD2)
    BackDesign.VERDE -> Color(0xFFC8E6C9)
    BackDesign.NEGRO -> Color(0xFF616161)
    BackDesign.ROMBOS -> Color(0xFF90CAF9)
    BackDesign.RAYAS -> Color(0xFFE1BEE7)
    BackDesign.ANILLOS -> Color(0xFFB2EBF2)
}

private fun backDesignModifier(design: BackDesign, shape: Shape): Modifier = when (design) {
    BackDesign.AZUL, BackDesign.ROJO, BackDesign.VERDE, BackDesign.NEGRO ->
        Modifier.background(design.baseColor(), shape)
    BackDesign.ROMBOS ->
        Modifier.clip(shape).background(design.baseColor()).drawBehind { drawDiamonds(design.accentColor()) }
    BackDesign.RAYAS ->
        Modifier.clip(shape).background(design.baseColor()).drawBehind { drawStripes(design.accentColor()) }
    BackDesign.ANILLOS ->
        Modifier.clip(shape).background(design.baseColor()).drawBehind { drawRings(design.accentColor()) }
}

private fun DrawScope.drawDiamonds(color: Color) {
    val s = 14.dp.toPx()
    val rows = ceil(size.height * 2 / s).toInt() + 2
    repeat(rows) { r ->
        val y = r * s / 2f
        val offset = if (r % 2 == 0) 0f else s / 2f
        var x = -s + offset
        while (x < size.width + s) {
            val path = Path().apply {
                moveTo(x, y - s / 2f)
                lineTo(x + s / 2f, y)
                lineTo(x, y + s / 2f)
                lineTo(x - s / 2f, y)
                close()
            }
            drawPath(path, color.copy(alpha = 0.5f))
            x += s
        }
    }
}

private fun DrawScope.drawStripes(color: Color) {
    val spacing = 14.dp.toPx()
    val stroke = spacing * 0.45f
    var start = -size.height
    while (start < size.width) {
        drawLine(
            color = color.copy(alpha = 0.5f),
            start = Offset(start, 0f),
            end = Offset(start + size.height, size.height),
            strokeWidth = stroke
        )
        start += spacing
    }
}

private fun DrawScope.drawRings(color: Color) {
    val spacing = 8.dp.toPx()
    var radius = size.minDimension / 2f
    while (radius > 0f) {
        drawCircle(color.copy(alpha = 0.5f), radius, center)
        radius -= spacing
    }
}

/** Cara de una carta: rango y figura en la esquina, con el frontal elegido. */
@Composable
fun CardFace(
    card: Card,
    modifier: Modifier = Modifier,
    width: Dp = 44.dp,
    height: Dp = 62.dp,
    skin: CardSkin = CardSkin()
) {
    val isJoker = card is JokerCard
    val textColor = when (card) {
        is PlayingCard -> if (card.suit.color() == CardColor.RED) CardRed else CardBlack
        // Cada mazo trae un joker coloreado (sigue el estilo elegido) y uno
        // sin colorear (siempre en gris/negro) (rules.md §2).
        is JokerCard -> if (card.type == JokerType.COLORED) skin.joker.textColor() else JokerGray
    }
    val shape = RoundedCornerShape(6.dp)

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .background(CardWhite, shape)
            .border(1.dp, skin.front.borderColor(), shape)
            .padding(4.dp)
    ) {
        if (isJoker) {
            // "JOKER" en vertical, ocupando la misma zona que el número y la
            // figura de las cartas normales (esquina superior izquierda).
            Column(
                modifier = Modifier.align(Alignment.TopStart),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "J\nO\nK\nE\nR",
                    color = textColor,
                    fontSize = (height * 0.14f).value.sp,
                    lineHeight = (height * 0.14f).value.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "\u2605",
                    color = textColor,
                    fontSize = (height * 0.14f).value.sp
                )
            }
        } else {
            val playing = card as PlayingCard
            Column(
                modifier = Modifier.align(Alignment.TopStart),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = playing.rank.symbol,
                    color = textColor,
                    fontSize = (height * 0.24f).value.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = suitGlyph(playing.suit),
                    color = textColor,
                    fontSize = (height * 0.18f).value.sp
                )
            }
        }
    }
}

/** Dorso de carta (mazo) con el diseño de reverso elegido para ese mazo. */
@Composable
fun CardBack(
    modifier: Modifier = Modifier,
    width: Dp = 44.dp,
    height: Dp = 62.dp,
    skin: CardSkin = CardSkin(),
    deckIndex: Int = 0
) {
    val shape = RoundedCornerShape(6.dp)
    val design = skin.backFor(deckIndex)
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .then(backDesignModifier(design, shape))
            .border(1.dp, design.borderColor(), shape)
            .padding(4.dp)
    )
}
