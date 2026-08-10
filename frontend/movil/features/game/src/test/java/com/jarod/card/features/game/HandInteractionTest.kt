package com.jarod.card.features.game

import org.junit.Assert.assertEquals
import org.junit.Test

class HandInteractionTest {

    private val slop = 18f
    private val confirmUp = 40f
    private val cancelDown = 28f

    private fun classify(dx: Float, dy: Float) =
        classifyHandSwipe(dx, dy, slop, confirmUp, cancelDown)

    // ── isVerticalDominant ──────────────────────────────────────────────

    @Test
    fun `movimiento vertical puro es dominante`() {
        assertEquals(true, isVerticalDominant(2f, 60f, slop))
    }

    @Test
    fun `movimiento horizontal es reorden no swipe`() {
        assertEquals(false, isVerticalDominant(60f, 2f, slop))
    }

    @Test
    fun `movimiento por debajo del slop no es vertical`() {
        assertEquals(false, isVerticalDominant(2f, 10f, slop))
    }

    @Test
    fun `movimiento diagonal con dx mayor no es vertical`() {
        assertEquals(false, isVerticalDominant(50f, 40f, slop))
    }

    @Test
    fun `movimiento diagonal con dy mayor es vertical`() {
        assertEquals(true, isVerticalDominant(20f, 50f, slop))
    }

    // ── classifyHandSwipe ───────────────────────────────────────────────

    @Test
    fun `swipe hacia arriba superando el umbral confirma`() {
        assertEquals(HandSwipe.UP, classify(0f, -41f))
    }

    @Test
    fun `swipe hacia arriba justo en el umbral confirma`() {
        assertEquals(HandSwipe.UP, classify(0f, -40f))
    }

    @Test
    fun `swipe hacia abajo superando el umbral cancela`() {
        assertEquals(HandSwipe.DOWN, classify(0f, 29f))
    }

    @Test
    fun `swipe hacia abajo justo en el umbral cancela`() {
        assertEquals(HandSwipe.DOWN, classify(0f, 28f))
    }

    @Test
    fun `movimiento vertical corto no produce accion`() {
        assertEquals(HandSwipe.NONE, classify(0f, 20f))
        assertEquals(HandSwipe.NONE, classify(0f, -20f))
    }

    @Test
    fun `arrastre horizontal por mas vertical que hubiera es reorden`() {
        assertEquals(HandSwipe.NONE, classify(60f, 40f))
    }

    @Test
    fun `sin movimiento no hay accion`() {
        assertEquals(HandSwipe.NONE, classify(0f, 0f))
    }
}
