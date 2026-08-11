package com.jarod.card.features.game.settings

/**
 * Mano dominante del jugador: condiciona la posición del grupo mazo+pozo en la
 * mesa para una interacción más cómoda (diestros y zurdos).
 */
enum class DominantHand(val label: String) {
    RIGHT("Derecha"),
    LEFT("Izquierda")
}
