package com.jarod.card.features.game

import kotlin.math.abs

/**
 * Clasificación de gestos de la mano del jugador.
 *
 * La mecánica de cartas diferencia dos ejes:
 *  - **Horizontal** → arrastrar para reordenar la mano.
 *  - **Vertical** → swipe sobre la carta seleccionada: arriba confirma
 *    (jugar/descartar) y abajo cancela la selección.
 */
internal enum class HandSwipe { NONE, UP, DOWN }

/** El movimiento es claramente vertical (y por encima del touch slop). */
internal fun isVerticalDominant(dx: Float, dy: Float, slop: Float): Boolean =
    abs(dy) > abs(dx) && abs(dy) > slop

/**
 * Clasifica el swipe vertical sobre la carta seleccionada a partir del
 * desplazamiento vertical acumulado en el momento de soltar.
 *
 * - [HandSwipe.UP]: `dy` por debajo de `-confirmUpPx` → confirmar/jugar.
 * - [HandSwipe.DOWN]: `dy` por encima de `cancelDownPx` → cancelar selección.
 * - [HandSwipe.NONE]: movimiento corto, horizontal o por debajo de umbrales.
 *
 * @param dx desplazamiento horizontal (para descartar arrastres de reorden).
 * @param dy desplazamiento vertical acumulado.
 */
internal fun classifyHandSwipe(
    dx: Float,
    dy: Float,
    slop: Float,
    confirmUpPx: Float,
    cancelDownPx: Float
): HandSwipe {
    if (!isVerticalDominant(dx, dy, slop)) return HandSwipe.NONE
    return when {
        dy <= -confirmUpPx -> HandSwipe.UP
        dy >= cancelDownPx -> HandSwipe.DOWN
        else -> HandSwipe.NONE
    }
}
