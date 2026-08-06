# ADR — Architecture Decision Records

> Registro de decisiones de arquitectura. Cada ADR tiene: contexto, decisión,
> consecuencias y estado. Los ADRs **pendientes** deben resolverse en la
> Fase 0 antes de codificar.

**Estado:** `Aceptado` / `Propuesto` / `Pendiente` / `Reemplazado`.

---

## ADR-001 — Tiempo real con WebSocket/STOMP (Spring)

- **Contexto:** la partida necesita eventos en vivo (< 500 ms p95). Se descartó
  polling REST (lento, costoso) y SSE (unidireccional).
- **Decisión:** **WebSocket + STOMP** en el backend; el cliente usa el stack
  OkHttp/STOMP. REST se reserva para CRUD.
- **Consecuencias:** + bidireccionalidad y manejo de tópicos; − complejidad de
  reconexión → mitigada con `seq` + resync (`docs/api.md §5`).
- **Estado:** Aceptado.

## ADR-002 — Motor servidor-autoritativo (anti-trampa)

- **Contexto:** el juego debe ser justo en línea; un cliente que "decida" puede
  hacer trampa.
- **Decisión:** el servidor valida y ejecuta **todas** las acciones; el cliente
  solo envía intenciones y proyecta eventos (`docs/game-engine.md`).
- **Consecuencias:** + integridad; − carga del servidor y latencia de ida/vuelta
  (aceptable: acciones cortas y locales a la sala).
- **Estado:** Aceptado.

## ADR-003 — Motor multi-juego (Strategy + Factory + Registry)

- **Contexto:** el producto arranca con Carioca pero quiere otros juegos con la
  misma baraja; el código debe ser escalable (POO/patrones).
- **Decisión:** abstracción `Game` en `domain/core`; cada juego = estrategia +
  ruleset config. `GameRegistry` crea por `gameType`.
- **Consecuencias:** + extensibilidad y tests por reglas; − coste inicial de la
  abstracción (amortizado por el requisito explícito).
- **Estado:** Aceptado.

## ADR-004 — Estado de partida por Event Sourcing + snapshot

- **Contexto:** se necesita auditoría, replay, resync de cliente y recuperación
  ante caídas (`NFR-AVAIL-03`).
- **Decisión:** las acciones legales generan `GameEvent`s (append-only) y el
  estado actual es una proyección; snapshots periódicos evitan replays largos.
- **Consecuencias:** + integridad/replay; − almacenamiento (acotado con
  snapshots) y complejidad de versionado de eventos.
- **Estado:** Aceptado.

## ADR-005 — Reglas y baraja como configuración (Ruleset/DeckConfig)

- **Contexto:** las reglas de Carioca varían por región y los colores/palos deben
  ser configurables a futuro.
- **Decisión:** `DeckConfig` y `Ruleset` son **datos** (JSONB en BD) validados por
  el motor, versionados.
- **Consecuencias:** + cambios de reglas sin deploy; − riesgo de config inválida
  → mitigado con validador y tests.
- **Estado:** Aceptado.

## ADR-006 — Persistencia: PostgreSQL + Redis

- **Contexto:** datos transaccionales (usuarios, partidas) + estado transitorio
  (sesiones, rate-limit).
- **Decisión:** **PostgreSQL** como fuente de verdad; **Redis** para sesiones,
  cache y rate-limit.
- **Consecuencias:** + robustez; − un componente extra (Docker Compose).
- **Estado:** Aceptado.

## ADR-007 — Autenticación: Google OAuth 2.0 + JWT propio de sesión

- **Contexto:** producto exige **login obligatorio** para jugar y el método es
  **Google OAuth 2.0**. La app es móvil (sin cookies) y se necesita logout y
  revocación.
- **Decisión:**
  - El cliente usa **Google Sign-In** y envía el **ID token** a `POST /auth/google`.
  - El backend **valida el ID token** (JWKS, `iss`, `aud`) y emite **sus propios**
    `accessToken` (**15 min**) + `refreshToken` rotativo (**7 días**, hash en BD).
  - **Blacklist de tokens:** los `jti` revocados se invalidan vía **Redis** (TTL =
    vida restante); logout global con **contador de versión de sesión**
    (`docs/security.md §2.1.1`).
  - **No existe registro con email/contraseña** ni modo anónimo.
- **Consecuencias:** + identidad delegada y segura, sin gestión de contraseñas;
  + revocación/rotación propia; − dependencia de disponibilidad de Google en el
  login (mitigada con token refresh y caché de sesión).
- **Estado:** Aceptado.

## ADR-010 — Amistad mutua (solicitud + aceptación)

- **Contexto:** producto decide cómo funciona la amistad (Q1).
- **Decisión:** la amistad es **mutua y bidireccional**: un usuario envía una
  **solicitud** (usando el código de amigo) y el destinatario debe **aceptarla**
  para que la amistad quede activa. El destinatario puede **rechazar** la
  solicitud. `friendships` tiene estado `PENDING / ACCEPTED`.
- **Eliminación:** la amistad puede **eliminarse**; el efecto es **mutuo** —ambas
  partes dejan de verse el estado online/en partida y desaparecen de sus listas,
  y se elimina cualquier solicitud pendiente entre ellos (`FR-AMI-05`).
- **Consecuencias:** + sin "seguidores" no deseados; + consentimiento explícito;
  − el emisor no ve el estado del otro hasta que acepte (la solicitud se registra
  como pendiente).
- **Estado:** Aceptado.

## ADR-011 — Rondas configurables por sala (Q2)

- **Contexto:** producto decide que **no se obliga a un juego completo de Carioca**.
- **Decisión:** el ruleset define un **catálogo de rondas** (9 base + opcionales
  sucia/imperial/payaso). Al **crear la sala** se eligen **cuántas rondas y qué
  modos** se juegan (`roomConfig.rounds`); la partida usa **solo las seleccionadas**.
  Modo por defecto = juego completo (9 base). Las opcionales están disponibles
  desde el MVP.
- **Consecuencias:** + flexibilidad y partidas cortas; − hay que validar que la
  selección de rondas exista en el ruleset antes de iniciar.
- **Estado:** Aceptado.

## ADR-012 — Timeout: jugada aleatoria y 2 timeouts = abandono (Q6)

- **Contexto:** producto define qué pasa cuando un jugador **no elige a tiempo**.
- **Decisión:** si el jugador no actúa en X segundos, el motor **juega aleatorio**
  por él (roba/descarta legales). Al acumular **2 timeouts** en la partida, se
  trata como **abandono** (política de `FR-CAR-13`/`ADR-013`).
- **Consecuencias:** + la partida no se detiene por un jugador ausente; − una
  jugada aleatoria puede perjudicar al que la sufre (mitigado con el límite de 2
  y aviso/notificación al jugador).
- **Estado:** Aceptado.

## ADR-013 — Abandono de ronda con continuación (Q9)

- **Contexto:** producto redefine el abandono: no siempre termina la partida.
- **Decisión:** el abandono aplica a la **ronda actual** (`PLAYER_ABANDONED_ROUND`).
  Los demás jugadores **confirman** si continúan: si confirman, la partida
  **continúa a la siguiente ronda sin el que abandona** (conserva sus puntos);
  si no, la partida **termina** (`GAME_END`, `FORFEIT`). Penalización del que
  abandona configurable por ruleset (`forfeitPenalty`).
- **Consecuencias:** + partidas que sobreviven a un abandono; + respeto a la
  voluntad de los demás; − requiere flujo de confirmación y manejar el recuento
  de jugadores en las rondas siguientes.
- **Estado:** Aceptado.

## ADR-014 — Monorepo con frontends agrupados (P7)

- **Contexto:** la plataforma tiene tres proyectos (Android, web admin, backend)
  que evolucionan juntos; se decide la estructura del repositorio.
- **Decisión:** **monorepo** con estructura:
  ```
  CARD_GAME/
  ├── frontend/movil/    # App Android (Kotlin + Compose)
  ├── frontend/web/      # Web de administración (Angular 22)
  ├── backend/           # API Java 21 + Spring Boot
  ├── docs/              # Documentación (ERS, SPECS, diseño)
  ├── terraform/         # Infraestructura AWS (entornos + módulos por recurso: ec2, s3, vpc, sg…)
  ├── .github/           # CI/CD (GitHub Actions)
  ├── docker/            # docker-compose (PostgreSQL, Redis) + Dockerfiles
  └── scripts/           # utilidades de desarrollo
  ```
  Los proyectos se crean **manualmente** (no scaffolding automatizado). CI con
  **path filters**: un cambio en `backend/**` no dispara el pipeline de Android
  ni de Angular.
- **Consecuencias:** + un solo historial/issue/PR para cambios transversales;
  + tags globales de release; − CI más complejo (mitigado con path filters) y
  que un mismo commit puede mezclar cambios de varios componentes (evitar con
  PRs por path).
- **Estado:** Aceptado.

## ADR-015 — Git Flow como modelo de ramas (P6)

- **Contexto:** se decide el flujo de trabajo con Git para el monorepo.
- **Decisión:** **Git Flow** con `main` (producción, tags `vX.Y.Z`), `develop`
  (integración), y ramas `feature/`, `release/`, `hotfix/`. Commits
  **convencionales**; `main`/`develop` con **protección** (PR + revisión + CI).
  Guía operativa: `docs/gitflow.md`.
- **Consecuencias:** + releases trazables y desplegables en cualquier momento;
  + separación clara feature/release/hotfix; − más ramas que trunk-based
  (aceptable para equipo pequeño, se mantiene con PRs squash y CI por paths).
- **Estado:** Aceptado.

## ADR-016 — Permitir revincular la cuenta de Google (Q8)

- **Contexto:** el único login es Google. ¿Qué pasa si el usuario cambia de cuenta
  de Google (perdió acceso, correo corporativo dado de baja, etc.)?
- **Decisión:** **se permite revincular** (opción B). Un usuario **autenticado**
  puede **desvincular su Google actual y asociar otro**, conservando su cuenta
  local (perfil, amigos, partidas, estadísticas). Reglas:
  - El flujo usa un **nuevo ID token** de Google del usuario ya logueado
    (`POST /auth/relink`).
  - Se valida el token (firma, `iss`, `aud`) y que el **nuevo `sub` no esté
    vinculado a otra cuenta** (si lo está → `409 GOOGLE_ALREADY_LINKED`).
  - Al revincular se **revocan todas las sesiones previas** (refuerza la
    seguridad y fuerza re-login con la nueva cuenta).
  - El `google_sub` anterior queda **libre** y el cambio queda registrado en el
    log de auditoría (`account_links`) para trazabilidad/administración.
- **Consecuencias:** + el usuario no pierde su cuenta ante un cambio de Google;
  + sin cuentas duplicadas por error; − requiere validación de no-colisión y
  revocación de sesiones (flujo de seguridad adicional).
- **Estado:** Aceptado.

## ADR-017 — Comodines base: solo los 4 jokers estándar (Q4)

- **Contexto:** ¿la variante regional "**comodines rojos**" (los 2♥ y 2♦ también
  comodines) forma parte del ruleset base de Carioca?
- **Decisión:** **regla estándar**: los **únicos comodines son los 4 jokers** de
  la baraja. Cada juego de cartas trae **2 jokers: uno coloreado y uno sin
  colorear** (total 4 jokers). Los **2 rojos NO son comodines** en la base; la
  variante "comodines rojos" queda **desactivada por defecto** y **seleccionable
  al crear la sala** (junto con las demás variantes, `roomConfig.variants`).
- **Consecuencias:** + reglas oficiales sencillas de implementar/validar (TDD);
  + el jugador elige la variante antes de jugar; − se añade un flag
  configurable más al ruleset (mínimo).
- **Estado:** Aceptado.

## ADR-018 — Puntuación estándar: sin −10 por corte (Q5)

- **Contexto:** ¿se aplica la variante de restar **−10 puntos** al ganador que se
  baja con el juego esperado **y** hace el corte en la misma jugada?
- **Decisión:** **regla estándar**: el ganador de la ronda **suma 0 puntos**
  (los demás suman las cartas en mano). La variante **"−10 por corte"** queda
  **desactivada por defecto** y **seleccionable al crear la sala** (junto con
  las rondas y las demás variantes, `roomConfig.variants`), configurada en el
  ruleset (`cutBonus`).
- **Consecuencias:** + scoring estándar predecible (TDD sencillo); + el jugador
  elige la variante antes de jugar (misma UX que la selección de rondas);
  − si se activa la variante hay que validar "mismo turno = bajada + corte" en
  el motor (opcional, no en la base).
- **Estado:** Aceptado.

## ADR-008 — Login obligatorio para toda la plataforma

- **Contexto:** el producto decide que **no hay juego sin sesión autenticada**.
- **Decisión:** todos los endpoints de negocio exigen `Authorization: Bearer`;
  sin sesión la app solo muestra la pantalla de login con Google. El botón
  "jugar sin cuenta" no existe.
- **Consecuencias:** + datos consistentes y anti-bots; − fricción de entrada
  (mitigada con Google Sign-In de un toque).
- **Estado:** Aceptado.

## ADR-009 — Carioca: de 2 a 4 jugadores como máximo

- **Contexto:** producto define que Carioca se juega de **2 a 4 jugadores** y que
  **no se permite agregar más** de 4.
- **Decisión:** el ruleset de Carioca declara `maxPlayers = 4` (y mínimo 2). El
  motor, la sala y el backend **validan** este límite: no se puede crear/llenar
  una sala con más de 4 ni iniciar con menos de 2. La baraja es **fija**: siempre
  los **2 juegos (108 cartas)**, sin importar si juegan 2, 3 o 4.
- **Consecuencias:** + reglas claras y validación única en el ruleset; − el
  límite es por juego (otros juegos podrán tener otro `maxPlayers`).
- **Estado:** Aceptado.

---

## ADRs pendientes (resolver en Fase 0)

_Todos los ADRs pendientes están resueltos. Si surge una nueva decisión, añadirla
aquí y resolverla antes de codificar._

> Al resolver un ADR pendiente: mover a la lista de ADRs con estado `Aceptado`
> y, si cambia comportamiento, actualizar `SPECS.md`.
