# Carioca — Interacciones de cartas en el tablero

> Guía de UX de las interacciones con las cartas de la mano en la app móvil.
> Complementa `rules.md` (reglas de juego) y `TODO.md` (mecánica de selección).

---

## 1. Principio general

La interacción con la mano transmite una regla sencilla:

> **Toca para seleccionar. Swipe arriba para jugar. Swipe abajo para cancelar.**
> El doble tap es una alternativa para jugar (accesibilidad).

Modelo conceptual:

```
Mano → Carta seleccionada → Mesa → Resolución de la acción
                                     → Pozo (descartar, hoy)
                                     → Lay-off (futuro)
                                     → Otras acciones futuras
```

La selección es **reversible hasta el momento de confirmar**: explorar una carta
no ejecuta ninguna acción.

## 2. Estados de la carta

| Estado | Descripción | Visual |
|---|---|---|
| **Normal** | Carta en la mano, en su posición del arco | Solapadas, sin resalte |
| **Seleccionada** | Carta preparada para jugar | Elevada sobre el resto, borde dorado, vecinas separadas |
| **Confirmada** | Carta enviada a la mesa | Sale volando hacia arriba y desaparece (→ resolución) |
| **Cancelada** | Selección revertida | Vuelve con spring a su posición original |

## 3. Gestos

### 3.1 Tap → seleccionar / deseleccionar

- Un **tap** sobre una carta la **selecciona**: se eleva `18dp`, se escala un 7 %,
  se le dibuja un **borde dorado de 2dp** y sus vecinas inmediatas se separan
  `8dp` cada una para que destaque.
- Tocar de nuevo la misma carta **deselecciona** (vuelve a la mano).
- Tocar otra carta **mueve** la selección.
- Feedback háptico (`TextHandleMove`) al seleccionar.

### 3.2 Swipe ↑ → confirmar / jugar

Sobre la carta seleccionada, deslizar **hacia arriba** más de `40dp` y soltar:

- Mientras el dedo está sobre la pantalla, la carta **sigue el dedo 1:1**
  (arriba y abajo): el gesto es **reversible** hasta que se suelta.
- Al soltar por encima del umbral, **sale volando** hacia arriba (2,5 alturas de
  carta, 300ms, fade-out) y se ejecuta la **resolución**.
- Resolución actual: **descartar al pozo** (`DiscardAction`). El destino final
  depende del contexto (ver §5).

### 3.3 Swipe ↓ → cancelar

Deslizar la carta seleccionada **hacia abajo** más de `28dp` y soltar:

- Igual que en el swipe ↑, la carta **sigue el dedo 1:1** mientras no se suelte:
  se puede subir y volver a bajar sin comprometerse.
- Al soltar por encima del umbral, la carta **vuelve con spring** a su posición
  original y la selección queda cancelada. No se ejecuta ninguna acción.

### 3.4 Doble tap → jugar

Dos taps sobre la **misma carta** dentro de `300ms` equivalen a confirmar
(se envía a la mesa). Alternativa de accesibilidad al swipe ↑.

### 3.5 Arrastre horizontal → ordenar

El arrastre **horizontal** sigue usado para **reordenar la mano** (mecánica
preexistente, se conserva). Empezar un arrastre de reorden **cancela** cualquier
selección activa.

## 4. Cuándo está disponible

- La selección solo se puede iniciar en **tu turno** y en la fase **Acciones**
  (tras robar). Fuera de ese momento los taps no tienen efecto y cualquier
  selección activa se limpia automáticamente.

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
