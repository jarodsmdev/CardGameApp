package com.jarod.card.domain.core

enum class Suit(val symbol: String) {
    HEART("H"),
    SPADE("S"),
    DIAMOND("D"),
    CLUB("C")
}

enum class CardColor { RED, BLACK }

fun Suit.color(): CardColor = when (this) {
    Suit.HEART, Suit.DIAMOND -> CardColor.RED
    Suit.SPADE, Suit.CLUB -> CardColor.BLACK
}
