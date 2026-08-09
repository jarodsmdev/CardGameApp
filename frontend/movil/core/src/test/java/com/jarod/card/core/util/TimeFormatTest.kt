package com.jarod.card.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatTest {

    @Test
    fun `formatClock usa MM_SS por debajo de una hora`() {
        assertEquals("00:00", formatClock(0))
        assertEquals("00:05", formatClock(5_000))
        assertEquals("02:45", formatClock(165_000))
        assertEquals("18:32", formatClock(1_112_000))
        assertEquals("45:50", formatClock(2_750_000))
    }

    @Test
    fun `formatClock usa HH_MM_SS a partir de una hora`() {
        assertEquals("01:00:00", formatClock(3_600_000))
        assertEquals("01:04:27", formatClock(3_867_000))
        assertEquals("02:15:00", formatClock(8_100_000))
    }

    @Test
    fun `formatClock redondea hacia abajo los milisegundos`() {
        assertEquals("00:07", formatClock(7_999))
        assertEquals("00:00", formatClock(999))
    }

    @Test
    fun `formatDuration usa lenguaje natural`() {
        assertEquals("0s", formatDuration(0))
        assertEquals("5s", formatDuration(5_000))
        assertEquals("59s", formatDuration(59_999))
        assertEquals("1m 0s", formatDuration(60_000))
        assertEquals("1m 35s", formatDuration(95_000))
        assertEquals("2m 5s", formatDuration(125_000))
    }

    @Test
    fun `plural agrega s salvo cuando la cantidad es 1`() {
        assertEquals("1 ronda", plural(1, "ronda"))
        assertEquals("3 rondas", plural(3, "ronda"))
        assertEquals("0 vueltas", plural(0, "vuelta"))
        assertEquals("27 turnos", plural(27, "turno"))
    }
}
