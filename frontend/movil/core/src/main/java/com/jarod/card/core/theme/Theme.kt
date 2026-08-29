package com.jarod.card.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Tema visual de Royal Meld — esquemas de color completos para modo claro y oscuro.
 *
 * ## Paleta
 *
 * La paleta se basa en verde de mesa de póker como color dominante, con verde-gris
 * como tono neutro secundario y dorado como acento premium (medallas, rankings).
 *
 * | Rol               | Claro                  | Oscuro                   |
 * |-------------------|------------------------|--------------------------|
 * | primary           | `#2E7D32` (verde osc)  | `#81C784` (verde claro)  |
 * | secondary         | `#5D4037` (gris verd)  | `#BCAAA4` (gris verd cl) |
 * | tertiary          | `#F9A825` (dorado)     | `#FFD54F` (dorado claro) |
 * | surface           | `#FAFDF7` (casi blanco)| `#1C1F1C` (casi negro)   |
 * | surfaceVariant    | `#DEE5D9` (verde gris) | `#2A2D2A` (verde gris os)|
 *
 * ## Uso de surface vs surfaceVariant
 *
 * - **surface**: fondo principal de la pantalla.
 * - **surfaceVariant**: fondos de cards, chips y paneles secundarios.
 * - **onSurfaceVariant**: texto legible sobre `surfaceVariant` en ambos modos.
 *
 * ## Dynamic Color
 *
 * Este tema **no** usa Dynamic Color (Material You). Usa paletas fijas para
 * mantener consistencia de marca. La paleta fue diseñada para que el verde del
 * tablero del juego (TableGreen = `#1A472A`) sea coherente con los colores de
 * la interfaz.
 *
 * ## Preferencia de tema
 *
 * La preferencia se controla via [ThemePreference] y se persiste en
 * DataStore. Ver [CardTheme] para la función composable raíz.
 */

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = Color(0xFF1A472A),
    primaryContainer = Color(0xFF2E5E3A),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = GreenGrey80,
    onSecondary = Color(0xFF3E2723),
    secondaryContainer = Color(0xFF4E342E),
    onSecondaryContainer = Color(0xFFBCAAA4),
    tertiary = Gold80,
    onTertiary = Color(0xFF3E2723),
    tertiaryContainer = Color(0xFF5D4037),
    onTertiaryContainer = Color(0xFFFFD54F),
    background = Color(0xFF0F110F),
    onBackground = Color(0xFFE2E3DE),
    surface = Color(0xFF1C1F1C),
    onSurface = Color(0xFFE2E3DE),
    onSurfaceVariant = Color(0xFFC2C9BD),
    surfaceVariant = Color(0xFF2A2D2A),
    surfaceContainerLow = Color(0xFF1E211E),
    surfaceContainer = Color(0xFF232623),
    surfaceContainerHigh = Color(0xFF2E312E),
    surfaceContainerHighest = Color(0xFF383C38),
    outline = Color(0xFF8A8F8A),
    outlineVariant = Color(0xFF4A4F4A),
    error = Color(0xFFEF5350),
    onError = Color(0xFF601410),
    scrim = Color(0xFF000000),
)

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF1A472A),
    secondary = GreenGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEFEBE9),
    onSecondaryContainer = Color(0xFF5D4037),
    tertiary = Gold40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF8E1),
    onTertiaryContainer = Color(0xFF5D4037),
    surface = Color(0xFFFAFDF7),
    onSurface = Color(0xFF1A1C1A),
    onSurfaceVariant = Color(0xFF424940),
    surfaceVariant = Color(0xFFDEE5D9),
    outlineVariant = Color(0xFFC2C9BD),
    error = Color(0xFFC62828),
    onError = Color.White,
)

/**
 * Composable raíz del tema. Aplica el esquema de color seleccionado por el usuario.
 *
 * - [ThemePreference.SYSTEM]: sigue el modo del sistema operativo.
 * - [ThemePreference.LIGHT]: fuerza modo claro.
 * - [ThemePreference.DARK]: fuerza modo oscuro.
 *
 * Debe envolver todo el contenido de la app en [MainActivity]:
 * ```kotlin
 * CardTheme(themePreference = themePreference) {
 *     // contenido de la app
 * }
 * ```
 */
@Composable
fun CardTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themePreference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
