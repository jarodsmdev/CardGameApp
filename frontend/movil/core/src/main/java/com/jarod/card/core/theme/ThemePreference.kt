package com.jarod.card.core.theme

/**
 * Preferencia de apariencia de la aplicación.
 *
 * Controla cuándo se aplica el modo oscuro en [CardTheme]:
 *
 * | Valor    | Comportamiento                                          |
 * |----------|---------------------------------------------------------|
 * | SYSTEM   | Sigue la configuración del sistema (auto day/night)     |
 * | LIGHT    | Siempre modo claro                                      |
 * | DARK     | Siempre modo oscuro                                     |
 *
 * Se persiste en DataStore y se restaura al iniciar la app.
 * El usuario puede cambiarla desde Ajustes.
 */
enum class ThemePreference(val label: String) {
    /** Se adapta automáticamente al modo del sistema operativo. */
    SYSTEM("Sistema"),

    /** Fuerza el modo claro independientemente del sistema. */
    LIGHT("Claro"),

    /** Fuerza el modo oscuro independientemente del sistema. */
    DARK("Oscuro")
}
