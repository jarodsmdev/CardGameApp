package com.jarod.card.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TurnCountdownTest {

    @Test
    fun `arranca con los segundos configurados y descuenta con cada tick`() {
        val countdown = TurnCountdown(TurnTimeout(seconds = 60))
        assertEquals(60, countdown.remainingSeconds)

        repeat(10) { countdown.tick() }

        assertEquals(50, countdown.remainingSeconds)
        assertFalse(countdown.expired)
    }

    @Test
    fun `expira al llegar a cero`() {
        val countdown = TurnCountdown(TurnTimeout(seconds = 3))
        repeat(3) { countdown.tick() }
        assertTrue(countdown.expired)
        assertEquals(0, countdown.remainingSeconds)
    }

    @Test
    fun `no descuenta por debajo de cero`() {
        val countdown = TurnCountdown(TurnTimeout(seconds = 1))
        repeat(5) { countdown.tick() }
        assertEquals(0, countdown.remainingSeconds)
        assertTrue(countdown.expired)
    }

    @Test
    fun `reset vuelve a los segundos iniciales`() {
        val countdown = TurnCountdown(TurnTimeout(seconds = 60))
        repeat(30) { countdown.tick() }
        countdown.reset()
        assertEquals(60, countdown.remainingSeconds)
        assertFalse(countdown.expired)
    }

    @Test
    fun `timeout desactivado con seconds 0 expira de inmediato`() {
        val timeout = TurnTimeout(seconds = 0)
        assertFalse(timeout.enabled)
        assertEquals(0, timeout.warningAtSeconds)
        assertTrue(TurnCountdown(timeout).expired)
    }

    @Test
    fun `warning se recorta al máximo de 10 segundos`() {
        assertEquals(10, TurnTimeout(seconds = 60).warningAtSeconds)
        assertEquals(4, TurnTimeout(seconds = 4).warningAtSeconds)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `segundos negativos no permitidos`() {
        TurnTimeout(seconds = -1)
    }
}
