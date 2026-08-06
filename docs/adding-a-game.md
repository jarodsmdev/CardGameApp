# Cómo agregar un juego nuevo a la plataforma

> Guía operativa para añadir un **nuevo tipo de juego** al motor genérico.
> El motor está diseñado para esto (`SPECS §6.4`, `docs/game-engine.md`): **se
> agrega, no se modifica el núcleo** (Open/Closed). Carioca es solo un ejemplo.
>
> Referencias: `docs/game-engine.md`, `docs/games/carioca/rules.md` (ejemplo),
> `SPECS §6.6 FR-EXT-02`.

---

## 1. Idea central (por qué esto es fácil)

El motor separa **lo genérico** (que ya existe) de **lo específico del juego**:

| Capa | Genérico (ya hecho, NO se toca) | Específico del juego (tú lo creas) |
|---|---|---|
| Baraja | `Card`, `Suit`, `Rank`, `Color`, `Shoe`, `DeckConfig`, `DeckBuilder` | Elegir/reutilizar una `DeckConfig` |
| Flujo | Estados, turnos, eventos, timers, persistencias, `SURRENDER`, replay | Las **reglas** (qué es legal) |
| Reglas | `RulesetValidator`, motor de validación | Un `Ruleset` (datos) |
| Juego | `Game` interface, `GameRegistry` | Una clase `XGame implements Game` |
| Puntuación | `ScoringStrategy` (estrategia) | Implementación del scoring del juego |
| UI cliente | Proyección `GameView`, eventos genéricos | Composable(s) que dibujan el tablero |

**Regla de oro:** si para agregar el juego necesitas modificar `domain/core`,
detente: es un *vacio de abstracción*. Documentalo en un ADR y refactoriza el
núcleo **una vez**, no por juego.

---

## 2. Pasos (orden recomendado)

### Paso 1 — Decidir la baraja (`DeckConfig`)

- Reutiliza una existente (p. ej. la base de Carioca: 4 palos, 13 rangos,
  2 juegos por color, 4 jokers) o define una nueva en `deck_configs`.
- Define: palos, rangos (orden), mapa color→palos, nº de juegos (fijo —como
  Carioca, siempre 2 juegos = 108— u otra regla), jokers por juego.
- **Máximo de jugadores** del juego (p. ej. Carioca = 4) se declara en el
  ruleset (`maxPlayers`), no en la baraja.

### Paso 2 — Definir el `Ruleset` (las reglas como datos)

- Crea una **versión nueva** del ruleset en BD/JSON (`rulesets`), con `gameType`
  propio y `version`.
- El motor ya soporta: combinaciones (tríos/escaleras), comodines, lay-off,
  scoring, rondas, políticas de abandono, etc. Expresa tus reglas con esos
  bloques. Si necesitas un bloque nuevo → es un vacío del motor → ADR.
- **Valida el ruleset** con el `RulesetValidator` antes de activarlo
  (`active: true`). Un ruleset inválido nunca llega a producción.

### Paso 3 — Implementar la estrategia (`XGame implements Game`)

Crea `domain/games/<id-juego>/XGame.java` implementando la interfaz:

```
GameType type()                 → identifica el juego
GameState initialState(ctx)     → estado inicial (baraja, manos, mesa, ronda 1)
ValidationResult canPerform(s,p,a) → ¿es legal la acción en este estado?
List<GameEvent> perform(s,p,a)  → aplica la acción legal y emite eventos
GameResult resolve(s)           → resultado/puntuación al terminar
GameView viewFor(s, viewer)     → proyección para cada jugador (sin info oculta)
```

- **Solo reglas.** El reparto, turnos, eventos, persistencia y `SURRENDER` los
  da el core.
- Usa los validadores reutilizables del motor (combinaciones, comodines, etc.)
  si aplican; compón, no reescribas.

### Paso 4 — Registrar el juego

- Registra la estrategia en el `GameRegistry` (Factory): `gameType → factory`.
- El juego queda disponible para **crear salas** (`FR-SAL-01`).

### Paso 5 — Puntuación (`ScoringStrategy`)

- Si tu juego puntúa distinto, implementa `ScoringStrategy` y asígnala al
  ruleset. El scoring de Carioca es un ejemplo.

### Paso 6 — UI en el cliente Android

- Define la proyección de UI para el nuevo `GameView` (qué se ve en pantalla).
- Crea el/los `Composable`(s) del tablero.
- Mapea los eventos/acciones nuevos (si el juego usa acciones o eventos que no
  existen, agrégalos al contrato — ver Paso 8).
- Integra la navegación y el selector de juego en la creación de sala.

### Paso 7 — Pruebas

- **Fixtures:** versiona tu ruleset como fixture de test.
- **TDD de reglas:** casos legales/ilegales, fin de ronda, empates, abandono.
- **Contract tests:** cada **evento/acción nueva** se agrega a `docs/api.md` y
  se prueba el schema cliente↔servidor.
- **E2E:** una partida completa del juego nuevo con bots y luego online.

### Paso 8 — Contrato (API)

- Cualquier acción o evento nuevo del juego se documenta en `docs/api.md`
  (**en el mismo PR**).
- Los `gameType` nuevos se exponen en el catálogo de juegos de la sala.

### Paso 9 — Lanzar

- Ruleset activado (`active: true`), UI publicada, tests verdes.
- Se juega **sin tocar `domain/core`** si seguiste los pasos.

---

## 3. Checklist de "está listo para agregarse"

- [ ] `DeckConfig` definida/reutilizada y validada.
- [ ] `Ruleset` versionado, con `maxPlayers` correcto (Carioca = 4) y validado.
- [ ] `XGame implements Game` en `domain/games/<x>/` (solo reglas).
- [ ] Registrado en `GameRegistry`.
- [ ] `ScoringStrategy` propia si aplica.
- [ ] Proyección UI + Composable(s) en el cliente.
- [ ] Eventos/acciones nuevos documentados en `docs/api.md`.
- [ ] Tests (unit + contract + e2e) verdes.
- [ ] `domain/core` **sin cambios** (si hubo cambios → ADR que lo justifique).

---

## 4. Errores comunes a evitar

- **Hardcodear reglas en el flujo** en vez de usar el `Ruleset`.
- **Copiar el juego Carioca** y "modificarlo" como plantilla de reglas: crea tu
  propia estrategia; comparte solo los validadores reutilizables del core.
- **No tocar `viewFor`:** si tu juego tiene cartas ocultas, la proyección debe
  ocultarlas (anti-trampa, `docs/security.md §4`).
- **Ignorar `maxPlayers`:** valida el máximo del juego en sala y motor.
- **Olvidar la limpieza:** el `SURRENDER` y el fin de partida deben liberar
  recursos siempre (lo da el core, no lo rompas al sobrescribir).

---

## 5. Ejemplo de plan para "Ronda" o "Truco" (próximo candidato)

| Aspecto | Qué se hace |
|---|---|
| Baraja | Reutilizar `DeckConfig` base (Carioca: siempre 2 juegos = 108 cartas, para 2 a 4 jugadores). |
| Ruleset | Nuevo `gameType` + reglas de envite/valor de cartas (datos). |
| Estrategia | `RondaGame implements Game` (solo reglas de manos y orden). |
| Scoring | `ScoringStrategy` propia (valor de J/Q/K/A según el juego). |
| UI | Composable del tablero + mapeo de eventos. |
| Salas | `maxPlayers` (p. ej. 2–4) validado por el ruleset. |
