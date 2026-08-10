# Carioca — Interacciones de cartas en el tablero

> Guía de UX de las interacciones con las cartas de la mano en la app móvil.
> Complementa `rules.md` (reglas de juego) y `TODO.md` (mecánica de selección).

---

## 1. Principio general

La interacción con la mano transmite una regla sencilla:

> **Arrastra ↑ para jugar (descartar). Arrastra ↓ para cancelar.**
> **Arrastra ←→ para ordenar. Toca para seleccionar.** El doble tap es una
> alternativa para jugar (accesibilidad).

Modelo conceptual:

```
Mano → Carta seleccionada → Mesa → Resolución de la acción
                                     → Pozo (descartar, hoy)
                                     → Lay-off (futuro)
                                     → Otras acciones futuras
```

La interacción es **una única pulsación y arrastre unificado**: se toca una
carta sin soltar la pantalla y el eje del movimiento decide qué ocurre. La
acción es **reversible hasta el momento de soltar**: subir y volver a bajar el
dedo no confirma nada.

## 2. Estados de la carta

| Estado | Descripción | Visual |
|---|---|---|
| **Normal** | Carta en la mano, en su posición del arco | Solapadas, sin resalte |
| **Seleccionada** | Carta preparada para jugar | Elevada sobre el resto, borde dorado, vecinas separadas |
| **En arrastre** | Carta tocada sin soltar (cualquier carta) | Sigue el dedo 1:1, borde dorado |
| **Confirmada** | Carta enviada a la mesa | Sale volando hacia arriba desde donde se soltó y desaparece (→ resolución) |
| **Cancelada** | Selección revertida | Vuelve con spring a su posición original |

## 3. Gestos

### 3.1 Tap → seleccionar / deseleccionar

- Un **tap** sobre una carta la **selecciona**: se eleva `18dp`, se escala un 7 %,
  se le dibuja un **borde dorado de 2dp** y sus vecinas inmediatas se separan
  `8dp` cada una para que destaque.
- Tocar de nuevo la misma carta **deselecciona** (vuelve a la mano).
- Tocar otra carta **mueve** la selección.
- Feedback háptico (`TextHandleMove`) al seleccionar.

### 3.2 Pulsar y arrastrar → interacción unificada

Tocar **cualquier carta** sin soltar la pantalla la "levanta" (borde dorado) y
la hace seguir el dedo **1:1 en ambos ejes**. El eje dominante del arrastre
decide la acción:

| Arrastre | Acción | Al soltar |
|---|---|---|
| **Horizontal** (`←→`) | **Ordenar** la baraja (reordena en vivo) | La carta se desliza con spring a su nueva posición |
| **Vertical ↑** (> `40dp`) | **Jugar / descartar** al pozo | Sale volando hacia arriba (desde donde se soltó) y se descarta |
| **Vertical ↓** (> `28dp`) | **Cancelar** selección | Vuelve con spring a la mano, sin ejecutar acción |
| Movimiento corto | Nada | Vuelve con spring a su posición (la selección se mantiene) |

Mientras el dedo está sobre la pantalla, el gesto es **reversible**: subir y
bajar libremente, y solo al soltar se decide. Haptic `LongPress` al confirmar.

El **reordenado horizontal** no es una mecánica aparte: es el mismo arrastre.
Solo cuando el eje dominante es horizontal la mano se reordena en vivo (así un
arrastre vertical nunca baraja las cartas). Empezar el arrastre sobre una carta
**distinta** de la seleccionada limpia la selección activa; arrastrar la propia
seleccionada la mantiene.

### 3.4 Doble tap → jugar

Dos taps sobre la **misma carta** dentro de `300ms` equivalen a confirmar
(se envía a la mesa). Alternativa de accesibilidad al arrastre ↑.

## 4. Cuándo está disponible

- **Inicio de turno (fase Robar):** `ActionBar` muestra **"Roba una carta: toca
  el mazo o el pozo"** para indicar al jugador que debe robar.
- La **selección** y el arrastre de la mano solo se pueden iniciar en **tu
  turno** y en la fase **Acciones** (tras robar). Fuera de ese momento los taps
  no tienen efecto y cualquier selección activa se limpia automáticamente.

## 5. Reutilización para futuras acciones

La mecánica no está atada a "descartar": la confirmación dispara un callback
`onPlay(cardId)` (hoy cableado a `discard`). Para un futuro **lay-off manual**,
la misma selección/swipe podrá resolver hacia una combinación de la mesa,
y hacia cualquier otra zona de juego en el futuro.

## 6. Implementación

- `GameScreen.kt` — `HandRow` (estado de selección, gestos y render),
  `CariocaBoard` (estado `selectedCardId` compartido con `ActionBar`),
  `ActionBar` (hint contextual según haya o no selección).
- `HandInteraction.kt` — clasificador puro de gestos (`isVerticalDominant`,
  `classifyHandSwipe`), probado en `HandInteractionTest`.
- Umbrales (px/dp): touch slop del sistema, confirm ↑ `40dp`, cancel ↓ `28dp`,
  separación de vecinas `8dp`, ventana de doble tap `300ms`.
