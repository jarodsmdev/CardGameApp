package com.jarod.card.features.game.cardskin

/**
 * Catálogo de diseños predefinidos para las cartas (reverso, frontal y joker).
 * La elección se renderiza en `CardViews.kt` y se persiste con [CardSkinStore].
 */
enum class BackDesign(val label: String) {
    AZUL("Azul clásico"),
    ROJO("Rojo"),
    VERDE("Verde"),
    NEGRO("Negro"),
    ROMBOS("Rombos"),
    RAYAS("Rayas"),
    ANILLOS("Círculos")
}

enum class FrontDesign(val label: String) {
    CLASICO("Clásico"),
    ROJO("Borde rojo"),
    AZUL("Borde azul"),
    DORADO("Borde dorado")
}

enum class JokerStyle(val label: String) {
    COLOR("A color"),
    MONO("Monocromo"),
    ORO("Dorado")
}

/**
 * Diseño combinado del reverso, frontal y joker de las cartas. Hay 2 mazos
 * (SPECS §4.1), distinguidos por color vía `setIndex`; cada uno puede tener su
 * propio reverso. Los valores por defecto siguen SPECS §4.1 (mazo 1 rojo, mazo
 * 2 negro). Cada mazo trae 2 jokers: uno coloreado y uno sin colorear.
 */
data class CardSkin(
    val deck0: BackDesign = BackDesign.ROJO,
    val deck1: BackDesign = BackDesign.NEGRO,
    val front: FrontDesign = FrontDesign.CLASICO,
    val joker: JokerStyle = JokerStyle.COLOR
) {
    /** Reverso del mazo indicado por `setIndex` (0 o 1). */
    fun backFor(deckIndex: Int): BackDesign = if (deckIndex == 1) deck1 else deck0
}
