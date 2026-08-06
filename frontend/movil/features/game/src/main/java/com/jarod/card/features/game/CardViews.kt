package com.jarod.card.features.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarod.card.domain.core.Card
import com.jarod.card.domain.core.CardColor
import com.jarod.card.domain.core.JokerCard
import com.jarod.card.domain.core.PlayingCard
import com.jarod.card.domain.core.Suit
import com.jarod.card.domain.core.color

private val CardRed = Color(0xFFC62828)
private val CardBlack = Color(0xFF1B1B1B)
private val CardWhite = Color(0xFFFFFFFF)
private val CardBorder = Color(0xFFC4C4C4)
private val JokerPurple = Color(0xFF6A1B9A)

private fun suitGlyph(suit: Suit) = when (suit) {
    Suit.HEART -> "\u2665"
    Suit.DIAMOND -> "\u2666"
    Suit.SPADE -> "\u2660"
    Suit.CLUB -> "\u2663"
}

/** Cara de una carta: rango en la esquina y la figura (palo) al centro. */
@Composable
fun CardFace(
    card: Card,
    modifier: Modifier = Modifier,
    width: Dp = 44.dp,
    height: Dp = 62.dp
) {
    val isJoker = card is JokerCard
    val textColor = when (card) {
        is PlayingCard -> if (card.suit.color() == CardColor.RED) CardRed else CardBlack
        is JokerCard -> JokerPurple
    }

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .background(CardWhite, RoundedCornerShape(6.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
            .padding(4.dp)
    ) {
        if (isJoker) {
            Column(
                modifier = Modifier.align(Alignment.TopStart),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "JOKER",
                    color = JokerPurple,
                    fontSize = (height * 0.18f).value.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "\u2605",
                    color = JokerPurple,
                    fontSize = (height * 0.16f).value.sp
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

/** Dorso de carta (mazo). */
@Composable
fun CardBack(
    modifier: Modifier = Modifier,
    width: Dp = 44.dp,
    height: Dp = 62.dp
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
            .border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(4.dp)
    )
}
