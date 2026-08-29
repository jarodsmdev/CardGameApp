package com.jarod.card.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Tipografía de Royal Meld.
 *
 * Utiliza la familia de fuentes del sistema ([FontFamily.Default]) para
 * máxima compatibilidad y rendimiento. Solo se personaliza [bodyLarge];
 * el resto de estilos usan los valores por defecto de Material3.
 *
 * ### Jeraría de estilos usada en la app
 *
 * | Estilo          | Uso                                                  |
 * |-----------------|------------------------------------------------------|
 * | titleLarge      | Títulos de pantalla, encabezados principales         |
 * | titleMedium     | Nombre de jugadores, encabezados de sección          |
 * | titleSmall      | Nombre en PlayerCard                                 |
 * | bodyLarge       | Texto principal del cuerpo                           |
 * | bodySmall       | Hints, subtítulos, timestamps                        |
 * | labelMedium     | Chips de puntaje, badges                             |
 * | labelSmall      | Contadores pequeños                                  |
 *
 * ### Tamaño de fuente
 *
 * El texto del tablero del juego usa tamaños reducidos (`0.7rem`-`0.8rem`)
 * para maximizar el espacio visible en pantallas pequeñas. Los hints y
 * mensajes de estado usan `bodySmall` (12sp).
 */
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)
