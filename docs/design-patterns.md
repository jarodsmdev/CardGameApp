# Patrones de diseño y principios SOLID

> Catálogo de patrones **obligatorios o recomendados**, y cómo se aplican a
> CARD_GAME. Referencia de requisitos: `SPECS.md §8.3`, `NFR-MAINT-03`.

---

## 1. Principios SOLID — mapeo concreto

| Principio | Cómo se garantiza en el proyecto |
|---|---|
| **S**ingle Responsibility | Cada clase tiene un único motivo de cambio: `CardDealer`, `TurnManager`, `ScoringStrategy`, `MeldValidator`, etc. Sin "God classes" en el motor. |
| **O**pen/Closed | Nuevos juegos/reglas/colores se **agregan** (nueva estrategia + config) sin **modificar** `domain/core` (`FR-JGO-09`). |
| **L**iskov | Las implementaciones de `Game`/`ScoringStrategy`/`Validator` pueden sustituirse sin romper el flujo genérico. |
| **I**nterface Segregation | Interfaces pequeñas y específicas: `Game`, `ScoringStrategy`, `RulesetValidator`, `EventStore`. No interfaces "todopoderosas". |
| **D**ependency Inversion | Dependencias apuntan al **dominio** (Clean Architecture). `application` conoce `domain`, `infrastructure` implementa puertos definidos por `domain`. |

---

## 2. Patrones GoF aplicados

### 2.1 Strategy — reglas de juego y puntuación
- `CariocaGame`, futuros juegos → implementan `Game`.
- `ScoringStrategy` intercambiable por juego.
- **Problema que resuelve:** variar comportamiento (reglas) sin cambiar el flujo.

### 2.2 Factory + Registry — creación de juegos
- `GameRegistry` registra `GameType → factory`.
- **Problema:** crear el juego correcto según `gameType` recibido de la sala.

### 2.3 State — máquina de estados de la partida
- `PREPARING / DEALING / PLAYING / ROUND_END / GAME_END`.
- **Problema:** comportamiento que depende del estado y transiciones validadas.

### 2.4 Command — acciones del jugador
- `GameAction` encapsula la intención; el motor las aplica/valida.
- **Beneficio:** idempotencia por `seq`, log/auditoría, replay.

### 2.5 Observer — notificaciones
- WebSocket/STOMP: los suscriptores del tópico observan los eventos.
- En Android: `StateFlow` (colección de estados) como observador de la capa de datos.

### 2.6 Template Method — flujo del turno
- El core define el esqueleto del turno; cada juego rellena los pasos
  específicos (robar, combinar, descartar).

### 2.7 Builder — construcción de la baraja
- `DeckFactory`/`DeckBuilder` construye el `Shoe` desde `DeckConfig`
  (palos, rangos, colores, juegos, jokers) de forma legible y validada.

### 2.8 Adapter / Mapper — fronteras
- DTO ↔ Entidad de dominio en el backend; DTOs de red ↔ UI en Android.
- **Problema:** aislar el dominio del transporte/persistencia.

### 2.9 Facade — servicios de juego
- `GameService` expone operaciones de alto nivel (start, applyAction, reconnect)
  y orquesta motor + persistence + websocket.

### 2.10 Memento / Snapshot
- Snapshot del estado de la partida para resync y recuperación (`NFR-AVAIL-03`).

### 2.11 Singleton — uso controlado
- Solo donde el framework lo garantiza (singleton por DI) y para recursos de
  sistema: `GameRegistry`, connection pools. **Evitar** singletons manuales con
  estado mutable.

---

## 3. Patrones de arquitectura (no GoF)

| Patrón | Aplicación |
|---|---|
| **Clean Architecture** | Android y backend: `domain → application → infrastructure/presentation`. |
| **MVVM** | Android: `Composable (View) ← ViewModel ← UseCases ← Repositories`. |
| **Repository** | Android: los ViewModels usan repositorios; ocultan fuente (remote/local/cache). |
| **Use Case / Interactor** | Cada caso de uso es una clase (`CreateRoomUseCase`, `PlayTurnUseCase`). |
| **Event Sourcing** | Estado de partida = proyección del `eventLog` (ver `docs/game-engine.md §5`). |
| **Dependency Injection** | **Hilt** (Android) y **Spring DI** (backend). Nunca `new` de dependencias dentro de clases de dominio. |
| **Anti-Corruption Layer** | Aislar integraciones externas (push, matchmaking futuro, pagos). |
| **Unidirectional Data Flow (UDF)** | Android: `UiState` inmutable via `StateFlow` (ver `docs/architecture.md §2.3`). |

---

## 4. Composición sobre herencia

- Preferir **interfaces + composición** (inyectar `TurnManager`, `ScoringStrategy`,
  `Validator`) a jerarquías profundas.
- Las entidades inmutables del dominio usan **records** (Java) / **data classes**
  (Kotlin).

## 5. Manejo de errores

- Errores de dominio como **tipos de valor** (resultados/validation) en vez de
  excepciones para flujo esperado (ej. `ACTION_REJECTED` es un resultado).
- Excepciones solo para condiciones inesperadas; mapeo a códigos HTTP/eventos en
  la frontera (Adapter).

## 6. Checklist por PR / revisión

- [ ] ¿Respeta SOLID? ¿Sigue algún patrón del catálogo?
- [ ] ¿Hay dependencia circular entre paquetes? (no debe)
- [ ] ¿El dominio depende de frameworks? (no debe)
- [ ] ¿La acción nueva es un `GameAction` con evento y validación?
- [ ] ¿Se documentó en SPECS/ADR si cambia comportamiento?
