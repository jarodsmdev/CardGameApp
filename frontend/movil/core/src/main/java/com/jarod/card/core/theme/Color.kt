package com.jarod.card.core.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de colores de Royal Meld.
 *
 * Cada tono tiene una variante "80" (clara, para dark mode) y "40" (oscura,
 * para light mode). El sufijo sigue la convención de Material3: el número
 * indica la luminosidad relativa en la escala de tonos (0 = negro, 100 = blanco).
 *
 * ### Significado semántico
 * | Token         | Uso principal                                         |
 * |---------------|-------------------------------------------------------|
 * | Green         | Color primario: acentos, botones, barra de progreso   |
 * | GreenGrey     | Color secundario: badges, chips, elementos neutros    |
 * | Gold          | Color terciario: medallas, destacados premium         |
 *
 * ### Fuente del diseño
 * Los valores fueron extraídos de la paleta "verde mesa de póker" (#1A472A)
 * usada en el tablero del juego, extendidos con tonos complementarios para
 * crear una paleta armónica con contraste adecuado en ambos modos.
 */

// ── Tonos claros (para dark mode) ──────────────────────────────

/** Verde claro — primario en dark mode. Botones, acentos activos. */
val Green80 = Color(0xFF81C784)

/** Verde-gris claro — secundario en dark mode. Chips, badges neutros. */
val GreenGrey80 = Color(0xFFBCAAA4)

/** Dorado claro — terciario en dark mode. Medallas, highlights premium. */
val Gold80 = Color(0xFFFFD54F)

// ── Tonos oscuros (para light mode) ────────────────────────────

/** Verde oscuro — primario en light mode. */
val Green40 = Color(0xFF2E7D32)

/** Verde-gris oscuro — secundario en light mode. */
val GreenGrey40 = Color(0xFF5D4037)

/** Dorado — terciario en light mode. */
val Gold40 = Color(0xFFF9A825)
