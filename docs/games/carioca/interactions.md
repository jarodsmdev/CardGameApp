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
| **Confirmada** | Carta enviada a la mesa | El overlay la anima desde su slot hasta el **target real** y muestra un borde pulsante al aterrizar |
| **Cancelada** | Selección revertida | Vuelve con spring a su posición original |

## 2.1 Animación a la posición final (cartas voladoras)

Cuando se confirma una acción (descartar o lay-off), la carta **no** desaparece
ni vuela "hacia arriba" genéricamente: una capa encima de toda la mesa la anima
desde su **slot real en la mano** hasta la **posición final exacta** donde la
carta quedará renderizada:

| Destino | Target real medido |
|---|---|
| **Descartar** | El pozo (esquina del rectángulo de la carta superior, desde `onGloballyPositioned` del `DiscardPile`) |
| **Lay-off** | El slot exacto dentro de la combinación: la carta ya está en el meld cuando el layout final mide su posición (`MeldRow` reporta cada carta) |

Detalles del sistema (`FlyingCardController` + `FlyingCardsOverlay`):

- **Origen real**: `HandRow` reporta con `onGloballyPositioned` la posición en
  raíz (`localToRoot`) de cada slot de la mano; al confirmar se captura ese
  valor, nunca una coordenada aproximada ni la del dedo.
- **Target real**: los targets los reportan los composables del layout final
  (pozo y melds), de modo que la animación termina exactamente donde la carta
  quedará renderizada; el vuelo **espera** a que llegue ese valor antes de
  moverse (la carta reposa en su slot hasta que el estado se aplica y el layout
  se mide).
- **Feedback de llegada**: al aterrizar, la carta muestra un **borde dorado
  pulsante ≈1.2 s** (`PulsingArrivalBorder`) y luego se desvanece.
- **Acciones inválidas no vuelan**: si la acción es rechazada (p. ej. un JOKER
  que no se puede descartar), no se pide el vuelo y la carta permanece en su
  lugar; si el motor rechazara una acción tras lanzarse, la carta se cancela a
  los ~1.2 s sin animación de llegada.
- La capa no consume toques: la interacción con la mesa/pozo sigue disponible
  durante el vuelo.

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

### 4.1 Hints contextuales (fase Acciones)

El `ActionBar` es contextual y guía la acción del momento en lugar de explicar
todos los gestos de una vez:

| Situación | Texto |
|---|---|
| Sin selección | "Descarta una carta para terminar tu turno: arrastra hacia arriba o toca y haz doble tap." |
| Carta seleccionada | "Arrastra hacia arriba para descartarla · doble tap también descarta" |
| **Durante el arrastre** | "Arrastra hacia arriba para descartar · hacia abajo para cancelar" |

Así el jugador recibe información extra solo cuando está haciendo el gesto, y al
cancelar simplemente vuelve al mensaje que corresponde a la situación.

## 5. Reutilización para futuras acciones

La mecánica no está atada a "descartar": la confirmación dispara un callback
`onPlay(cardId)` y el sistema de cartas voladoras es **genérico y reutilizable**:

- `FlyingCardController` — conecta origen (slot de la mano) con target (layout
  final) sin acoplarse a ninguna acción concreta.
- `FlyingCardsOverlay` / `FlyingCardView` / `PulsingArrivalBorder` — capa de
  vuelo + feedback de llegada, comunes para **descarte y lay-off** y válidas
  para cualquier zona futura de la mesa.
- El **lay-off** se resuelve hoy por el bot (`CariocaBot.findLayOff`); el
  sistema de vuelo ya está preparado para que una futura interacción manual
  apunte a un slot de una combinación concreta.

## 6. Implementación

- `GameScreen.kt` — `HandRow` (estado de selección, gestos, render y reporte
  de slots), `CariocaBoard` (estado `selectedCardId` compartido con
  `ActionBar`, `dragActive` y el `FlyingCardController` que cablea
  descarte/lay-off), `ActionBar` (hint contextual según fase, arrastre y
  selección), `TableSection`/`MeldRow` (reportan targets de melds),
  `StockDiscardRow`/`DiscardPile` (reportan el target del pozo).
- `HandInteraction.kt` — clasificador puro de gestos (`isVerticalDominant`,
  `classifyHandSwipe`), probado en `HandInteractionTest`.
- `GameViewModel.kt` — `proposeLayOff()` (captura el `LayOffAction` sin
  aplicarlo, para conocer el origen de la carta antes del cambio de estado) y
  `performLayOff(action)` (aplica la acción), en sustitución de `autoLayOff`.
- Umbrales (px/dp): touch slop del sistema, confirm ↑ `40dp`, cancel ↓ `28dp`,
  separación de vecinas `8dp`, ventana de doble tap `300ms`, vuelo `300ms`,
  borde pulsante `1.2 s`, desvanecimiento `150ms`.
