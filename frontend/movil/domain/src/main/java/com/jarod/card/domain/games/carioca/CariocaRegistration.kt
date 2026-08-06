package com.jarod.card.domain.games.carioca

import com.jarod.card.domain.engine.GameRegistry

/**
 * Registro automático de Carioca al cargar la clase (game-engine.md §3).
 */
object CariocaRegistration {
    init {
        GameRegistry.register(CariocaGame)
    }
}