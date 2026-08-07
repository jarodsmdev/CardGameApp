# Carioca — Reglas oficiales del producto

> Documento por juego: `docs/games/carioca/rules.md`. Índice de juegos:
> `docs/games/README.md`. Guía para agregar juegos:
> `docs/adding-a-game.md`.
>
> Estas reglas se basan en la **tradición de la Carioca chilena** (familia del
> rummy / *contract rummy*), definidas por el equipo de producto como las **reglas
> oficiales** del juego. No hay ente regulador y las reglas varían por región, por
> lo que toda variante debe quedar **parametrizada en el Ruleset**
> (`docs/game-engine.md §9`), nunca hardcodeada.
>
> **Carioca es uno de los juegos posibles de la plataforma.** El motor es
> multi-juego: estas reglas modelan solo la estrategia `carioca`.

---

## 1. Generalidades

- **Familia:** rummy (contract rummy / continental). Parentesco con la *loba*
  centroamericana.
- **Jugadores:** de **2 a 4 jugadores** como máximo. **No se permite agregar
  más de 4** (validado por el motor y la sala, `FR-CAR-14`).
- **Duración estimada:** 45–60 min por partida completa.
- **Azar alto, estrategia media-baja.**
- Antes de cada partida es normal **ponerse de acuerdo** en las reglas que puedan
  generar conflicto (esto se resuelve en producto como *reglas por sala*).

## 2. Baraja

- **Baraja inglesa** (francesa): palos `HEART, SPADE, DIAMOND, CLUB`; rangos
  `2–9, 10, J, Q, K, A` (13 por palo).
- **Siempre se juega con 2 juegos de cartas**, sin importar si son **2, 3 o 4
  jugadores**: `2 × (4 × 13) + 4 jokers = **108 cartas**`.
  - Los 2 juegos se distinguen por **color** (rojo/negro) según `SPECS §4`; cada
    carta tiene `setIndex` (0/1) que identifica su juego.
  - **Jokers:** cada juego trae **2 jokers: uno coloreado y uno sin colorear**
    (total **4 jokers**: 2 coloreados + 2 sin colorear).
  - **No hay baraja reducida para 2 jugadores**: el mazo de Carioca es fijo
    (108 cartas) para cualquier cantidad de jugadores.
- **Máximo 4 jugadores** en Carioca (no se admite más; a diferencia de otros
  juegos, el ruleset de Carioca **no** habilita mazos extra para +2 jugadores).
- El número de juegos es **fijo (2)** en el ruleset de Carioca; no se elige
  según la capacidad de la sala.

## 3. Valor de las cartas

| Carta | Valor base |
|---|---|
| 2–10 | su valor (2–10) |
| J (Jota/Sota), Q (Reina/Quina), K (Rey/Káiser) | 10 |
| A (As) | 20 |
| Joker / comodín | 30 |

> Hay variantes regionales con otros valores; se parametriza en el Ruleset.
> Si se usan los **2 rojos como comodines** (variante regional), valen 20 como
> comodín y 2 en su posición natural.

## 4. Combinaciones válidas

| Combinación | Definición | Mínimo |
|---|---|---|
| **Trío** | 3 cartas del **mismo valor**, sin importar pinta ni color. | 3 cartas |
| **Escala** | 4+ cartas **consecutivas de la misma pinta**. | 4 cartas |

- Se permiten **2 escalas de la misma pinta**.
- **Giro (vuelta):** permitido en escalas, p. ej. `2 – A – K – Q` (el As puede
  estar "al medio").
- **Reglas de construcción:**
  - No se valen **juegos duplicados** (p. ej. dos tríos de la misma figura).
  - No se permite **sacar cartas bajas del contrincante**.

## 5. Comodines (jokers)

**Regla base (estándar):** los **únicos comodines son los 4 jokers** de la baraja
(2 por juego: uno coloreado y uno sin colorear). Los **2 rojos (2♥ y 2♦) NO son
comodines** en la base; solo actúan como comodines si se activa la **variante
"comodines rojos"** (desactivada por defecto, `ADR-017`, §12).

- El joker es **comodín universal** (reemplaza cualquier carta).
- **Al bajarse:** máximo **1 comodín por trío o escala**.
- **Jokers juntos prohibidos:** si después de bajar otro jugador coloca cartas, los
  comodines **no pueden quedar juntos**.
- **Límite por mano:** no se valen **más de 2 comodines en una misma mano**.
- **Descartes:** en las rondas que permiten jokers, **no se pueden descartar
  jokers al pozo**.
- **Posición en la escala:** el comodín debe ir **entre 3 cartas** (p. ej. en
  `4-5-6-Joker` no se puede lanzar otro joker; en `3-4-5-6` sí, quedando
  `Joker-3-4-5-6-Joker`, cumpliendo "no juntos").
- **Escala Real con giro doble:** se puede ocupar un comodín cuando la escala real
  "da 2 vueltas"; no pueden reemplazar cartas consecutivas (como `5-6` o `A-K`).
  Algunas variantes **no permiten su uso**.
- **Jokers reciclables (variante):** si un jugador baja mano con comodines, otro
  puede reemplazarlos por la carta faltante y **tomar el joker** para su propio
  juego.

## 6. Reparto y orden de juego

- Se elige **repartidor** (si no sabe barajar, reparte el de su **derecha**).
- Se reparten **12 cartas** por jugador, **contra las agujas del reloj**:
  primero el jugador a la derecha del repartidor y **último quien reparte**.
- Empieza el jugador **a la izquierda de quien repartió**.
- El reparto se **delega** en el mismo sentido (contra las agujas del reloj)
  ronda a ronda.
- El **pozo** (basura/montón): se voltea la primera carta del mazo boca arriba.

## 7. Desarrollo del turno

1. **Robar:** sacar una carta del **mazo** (sin ver) **o del pozo** (la superior).
   - La ventaja del pozo es saber qué carta se toma; la contra es que **todos lo
     saben**.
   - **No se puede revisar** el pozo para ver cartas anteriormente descartadas.
2. El jugador pasa a tener **13 cartas**.
3. (Opcional) **Bajarse:** colocar boca arriba en la mesa las combinaciones que
   cumplan la ronda actual.
4. **Descartar:** botar 1 carta al pozo (obligatorio) para volver a 12.
   - En la **última ronda**, el jugador **no puede** tomar la carta recién
     descartada y botarla **si con esa gana**.
   - En rondas con jokers, **no se descartan jokers** al pozo.

## 8. Bajarse y añadir cartas a juegos ajenos (lay-off)

- **Bajarse por primera vez:** quien da vuelta sus cartas **no puede añadir
  cartas a juegos ajenos en ese mismo momento**.
- Quienes **ya se bajaron en una corrida/ronda anterior** sí pueden extender las
  combinaciones de otros bajados.
- **Después** de bajarse (turno siguiente), el jugador puede deshacerse de cartas
  añadiéndolas a sus propios juegos o a los de otros bajados.
- **En un turno** el jugador puede deshacerse de las cartas que quiera, incluso
  bajar **toda su mano**.
- Según la ronda, solo se puede bajar lo que la ronda exige:
  - Si la ronda es solo **tríos**, no se puede bajar nada de escalas (y
    viceversa).
  - Si la ronda exige **ambas**, se pueden bajar ambas (p. ej. un trío de 5 y una
    escala `3♦-2♦-A♦-K♦`), y luego **continuar** cada combinación (otro 5 en el
    trío, un `4♦` en la escala).

## 9. Rondas

El ruleset define un **catálogo de rondas disponibles**. **Antes de jugar, los
jugadores eligen cuántas rondas y qué modos se juegan** (al crear la sala,
`FR-SAL-01`): **no se obliga a un juego completo** ni a las 9 rondas.

### 9.1 Rondas base (catálogo)

En cada ronda hay que cumplir la cantidad de tríos/escalas indicada. **A mayor
ronda, más cartas y más complejidad.**

| Ronda | Requisito | Cartas |
|---|---|---|
| 1 | **2 tríos** | 6 |
| 2 | **1 trío + 1 escala** | 7 |
| 3 | **2 escalas** | 8 |
| 4 | **3 tríos** | 9 |
| 5 | **2 tríos + 1 escala** | 10 |
| 6 | **1 trío + 2 escalas** | 11 |
| 7 | **3 escalas** | 12 |
| 8 | **4 tríos** | 12 |
| 9 | **Escala Real** | 13 |

- **Ronda 9 (Escala Real):** escala completa de una sola pinta **desde el As
  hasta el Rey** (`A,2,3,4,5,6,7,8,9,10,J,Q,K`). Se reparten 12 cartas; el jugador
  usa sus 13 cartas y **gana automáticamente sin descartar**.
  - Con más de un comodín, estos **no pueden** reemplazar cartas consecutivas
    (`5-6` o `A-K`). Hay variantes sin comodines.

### 9.2 Rondas opcionales (modos disponibles para seleccionar)

| Ronda | Requisito | Notas |
|---|---|---|
| **Escala sucia** (13 cartas) | Escala completa As→Rey sin importar pinta ni color | Comodines generalmente acordados en 1 |
| **Escala imperial** (13 cartas) | As→Rey del **mismo color** (rojas ♥♦ o negras ♠♣), pudiendo usar ambas pintas del color | Sin comodines |
| **Escala payaso** (13 cartas) | As→Rey **alternando el color** una por una (roja, negra…) | Sin comodines |
| **Escala real** (13 cartas) | As→Rey del **mismo color y pinta** | Sin comodines |

> En las rondas finales (escalas As→Rey) se reparten 12 cartas y el jugador se
> baja con 13, **ganando automáticamente** sin botar carta.

### 9.3 Selección de rondas y variantes en la sala

- Al **crear la sala** se eligen las rondas a jugar: **cantidad** (1..9) y/o
  **modos específicos** (de las base + opcionales).
- La partida usa **solo las rondas seleccionadas**, en el orden definido.
- Si no se elige, el **modo por defecto** es el "juego completo" (las 9 base).
- Las rondas opcionales están **disponibles para seleccionar** desde el inicio
  (`ADR-011`); ya no se consideran solo "por acuerdo común".
- **Variantes de partida** (todas **desactivadas por defecto**, regla estándar)
  también se eligen al crear la sala:
  - **Comodines rojos** (2♥/2♦ como comodines) — `ADR-017`.
  - **−10 por corte** (bajarse con el juego esperado + corte en la misma jugada) — `ADR-018`.
  - **Jokers reciclables** (§5).
  - Otras variantes regionales (§12).
- Si no se activa ninguna, se juega el **Carioca estándar** (`roomConfig.variants`).

## 10. Fin de ronda y puntuación

- Cuando un jugador **se queda sin cartas** (gana la ronda, "corte"), los demás
  **suman al instante** los puntos de las cartas que conservan en la mano
  (los juegos ya bajados en mesa **no** se cuentan).
- El **ganador de la ronda no suma** puntos (**regla estándar**, sin −10;
  `ADR-018`). Variante: se le **restan 10** si baja el juego esperado y hace el
  **corte** en la misma jugada (desactivada por defecto).
- **Fin de la partida:** tras jugarse **todas las rondas configuradas** se suman
  los puntajes. Gana quien tiene **menos puntos**.
- **Empate:** queda mejor posicionado quien haya **ganado más rondas**.

## 11. Abandono de ronda y timeout

### 11.1 Timeout de turno

- La configuración es **genérica** (`TurnTimeout`, `docs/game-engine.md §6`):
  `seconds` (límite de tiempo), `policy` (`NONE` = solo avisar, `PLAY_RANDOM` =
  el motor juega por el jugador) y `limit` (timeouts permitidos).
- Si el jugador **no elige a tiempo** (X segundos configurable) y la política es
  `PLAY_RANDOM`, el motor **juega aleatorio** por él (roba y descarta legales
  según la ronda).
- Si el jugador acumula **`limit` timeouts** en la partida (por defecto **2**),
  se trata como **abandono** (aplica §11.2).
- **Aviso en el móvil (modo local):** el cliente muestra una **cuenta regresiva**
  y un **aviso visual** (borde rojo pulsante) cuando quedan pocos segundos. El
  aviso **no** juega automáticamente: con `policy = NONE` solo informa.

### 11.2 Abandono de ronda

- **Un jugador puede decidir no seguir jugando en cualquier momento** (con
  confirmación del propio jugador para evitar accidentes).
- El abandono aplica a la **ronda actual**: se registra `PLAYER_ABANDONED_ROUND`.
- **Continuación:** los demás jugadores **confirman si continúan a la siguiente
  ronda**:
  - **Si confirman:** la partida **continúa a la siguiente ronda sin el que
    abandona**. Sus puntos acumulados se conservan; la ronda abandonada no le
    suma puntos (la abandona a medias).
  - **Si no confirman:** la partida **termina** (`GAME_END`, motivo `FORFEIT`).
- **Resultado:** quien abandona se registra como `ABANDONED` y recibe una
  **penalización configurable** por ruleset (`forfeitPenalty`).
- **Recursos:** al terminar (fin de rondas o abandono no confirmado), el servidor
  persiste el estado final y **libera toda la memoria** de la partida (ver
  `docs/game-engine.md §11`).

## 12. Variantes regionales conocidas (parametrizables, NO implementadas por defecto)

- Valores de cartas distintos (según zona).
- **Comodines rojos:** los **2 rojos** (2♥ y 2♦) actúan como comodines además de
  los jokers (dificulta las escalas rojas). Como comodín valen 20; en su posición
  natural valen 2.
- Jokers reciclables (§5).
- Resta de puntos por corte (§10).
- Modo **por equipos** (4 jugadores en 2 parejas) — variante COULD.

## 13. Reglas como configuración (Ruleset)

Todo lo anterior se traduce al **Ruleset de Carioca** (`docs/game-engine.md §9`):

- `deckSets = "FIXED_2"` (siempre los 2 juegos = 108 cartas, para 2, 3 o 4
  jugadores; no es dinámico por jugadores).
- `handSize = 12`.
- **`maxPlayers = {min: 2, max: 4}`** (de 2 a 4 jugadores como máximo; no se
  permite agregar más).
- Orden de reparto/turno: contra agujas del reloj, repartidor último.
- **Catálogo de rondas** (9 base + opcionales) como lista configurable; la
  **selección por sala** (`rounds` elegidos al crear la sala) define qué se
  juega; modo por defecto = juego completo (9 base).
- Reglas de comodines (máx. por combinación, no juntos, sin descarte, etc.).
- `scoring` por carta y regla de ganador/desempate.
- **Timeout:** `TurnTimeout { policy = "PLAY_RANDOM", seconds = 60, limit = 2 }`
  → el motor juega aleatorio por el jugador inactivo; **2 timeouts = abandono**
  (§11.1). En el modo local el cliente solo avisa (`policy = NONE`).
- **Política de abandono:** abandono de **ronda** → confirmación de los demás;
  si confirman, continúa a la siguiente ronda sin el que abandona; si no,
  termina. Penalización configurable (`forfeitPenalty`).
- Variantes desactivadas por defecto (`enabled: false`).

> Ajustar estas reglas = nueva **versión de Ruleset**, sin tocar el motor.
