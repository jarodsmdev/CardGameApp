package com.jarod.card.core.util

/**
 * Reloj MM:SS (o HH:MM:SS a partir de 1h). Pensado para estadísticas de
 * partida/ronda donde el tiempo se muestra en formato de reloj.
 *
 * ```
 * formatClock(2_750_000) == "45:50"
 * formatClock(3_867_000) == "01:04:27"
 * ```
 */
fun formatClock(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    fun two(n: Long) = n.toString().padStart(2, '0')
    return if (h > 0) "${two(h)}:${two(m)}:${two(s)}" else "${two(m)}:${two(s)}"
}

/**
 * Duración en lenguaje natural: "Xs" o "Xm Ys" (minutos completos; por debajo
 * de un minuto se omite). Pensado para desgloses compactos.
 *
 * ```
 * formatDuration(5_000) == "5s"
 * formatDuration(95_000) == "1m 35s"
 * ```
 */
fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return if (min > 0) "${min}m ${sec}s" else "${sec}s"
}
