# SPECS — Especificación de CARD_GAME

> **Documento principal de requisitos.** Define *qué* debe construirse y *qué
> estándares deben cumplirse*. Es la fuente de verdad para desarrollo, pruebas y
> aceptación. Los documentos de diseño (`docs/`) complementan el *cómo*.

- **Versión:** 0.2.0
- **Estado:** En diseño — reglas de Carioca definidas; ADRs de la Fase 0 **resueltos**
- **Última actualización:** 2026-08-05

---

## 1. Resumen ejecutivo

Plataforma multijugador online de juegos de naipes para Android. **El acceso
requiere login obligatorio mediante Google OAuth 2.0** (no existe modo anónimo).
El juego inicial es **Carioca**, jugado con una baraja inglesa configurable
(4 palos, 2 juegos de cartas por color, 4 jokers), y la plataforma está diseñada
como **multi-juego**: Carioca es solo una de las estrategias que el motor puede
ejecutar. La arquitectura debe ser **genérica y extensible** para soportar
múltiples juegos con la misma baraja, salas privadas con códigos de amigo, y
—a futuro— matchmaking automático y pares de colores configurables.

Principios rectores:

1. **Login obligatorio con Google OAuth 2.0:** sin sesión autenticada no hay
   juego. La cuenta de producto se crea/vincula a partir del perfil de Google.
2. **Motor de juego genérico y multi-juego:** el código del juego es desacoplado
   de las reglas específicas. Carioca es una estrategia más (ver `FR-EXT-02`).
3. **Servidor autoritativo (server-authoritative):** el servidor valida cada
   acción; el cliente nunca decide resultados. Base del anti-trampa.
4. **SOLID + patrones de diseño + POO:** obligatorios en backend (Java) y cliente
   (Kotlin/Compose).
5. **Escalabilidad por diseño:** de amigos/salas hacia matchmaking, de un juego a
   muchos, de colores fijos a configurables.

---

## 2. Alcance

### 2.1 Incluido (dentro de alcance)

- **Autenticación obligatoria con Google OAuth 2.0** (login requerido para jugar) y perfiles.
- Sistema de **amigos con códigos** (el código permite "encontrarse").
- **Salas** de juego privadas (código) y públicas, con creación y gestión.
- **Motor de juego genérico y multi-juego** (baraja, mesa, turnos, acciones, eventos, puntuación).
- Juego **Carioca** implementado sobre el motor (9 rondas — `docs/games/carioca/rules.md`).
- Comunicación en tiempo real (WebSocket) para la partida.
- Persistencia de partidas, historial y estadísticas básicas.
- Aplicación Android con Jetpack Compose.

### 2.2 Diferido / fuera de alcance (para fases posteriores)

- Matchmaking automático (diseñado como extensión, no implementado en MVP).
- Juegos adicionales distintos de Carioca (soportados por el motor, no por producto).
- Selección de pares de colores personalizados por el usuario.
- Pagos / tienda / monetización.
- Chat por voz, torneos, rankings globales.
- Versión iOS / web.

---

## 3. Glosario

| Término | Definición |
|---|---|
| **Baraja** | Conjunto de cartas de una partida. Configurable: N juegos, palos, colores, jokers. |
| **Juego (deck set)** | Cada baraja estándar completa dentro del mazo. En Carioca se usan 2. |
| **Color** | Grupo que agrupa palos (p. ej. rojo = corazones+diamantes). Configurable. |
| **Palo (suit)** | Corazones, picas, diamantes, tréboles. |
| **Rango (rank)** | 2–9, 10, J, Q, K, A (13 rangos). Con valor ordinal para comparaciones y escaleras. |
| **Joker / comodín** | Carta comodín (4 en la configuración base). Reemplaza cualquier carta (reglas en `docs/games/carioca/rules.md`). |
| **Login / OAuth 2.0** | Autenticación obligatoria con cuenta de Google; el sistema emite su propio JWT de sesión tras validar el ID token. |
| **Sala** | Espacio de juego con jugadores, config y estado (previo al inicio). |
| **Partida** | Sesión de juego en ejecución de un juego específico. |
| **Ronda** | Cada mano/fase dentro de una partida de Carioca. |
| **Acción** | Intento de un jugador (robar, descartar, bajarse, pasar…). El servidor lo valida. |
| **Evento** | Hecho validado y emitido por el servidor (carta repartida, cambio de turno…). |
| **Código de amigo** | Identificador corto y no secreto que permite añadir a otro usuario. |
| **Código de sala** | Identificador corto para unirse a una sala privada. |
| **Abandonar / rendirse** | Decisión de un jugador de no seguir jugando (`SURRENDER`): abandona la **ronda actual**; los demás confirman si la partida continúa a la siguiente ronda sin él (`FR-CAR-13`). |

---

## 4. La baraja (modelo configurable)

La baraja **no se hardcodea**: se define como **configuración** que el motor consume.
Esto habilita: cambiar el número de juegos, los palos, los pares de colores y los
jokers sin tocar código.

### 4.1 Configuración base (Carioca)

| Propiedad | Valor base | Tipo |
|---|---|---|
| Palos | `HEART`, `SPADE`, `DIAMOND`, `CLUB` | lista |
| Rangos (orden) | `1,2,3,4,5,6,7,8,9,J,Q,K,A` | lista ordenada (13) |
| Colores | `RED = {HEART, DIAMOND}`, `BLACK = {SPADE, CLUB}` | mapa color→palos |
| Juegos de cartas (deck sets) | 2 (uno identificado como rojo, otro como negro) | entero |
| Jokers | 4 (2 por juego: uno **coloreado** y uno **sin colorear**) | entero |
| **Total de cartas** | `2 × (4 × 13) + 4 = 108` | — |

**Baraja fija para Carioca:** se usan **siempre los 2 juegos de cartas**
(108 cartas), **sin importar si juegan 2, 3 o 4 jugadores**. No existe baraja
reducida para 2 jugadores. **Carioca admite un máximo de 4 jugadores: no se
permite agregar más.** El motor valida `maxPlayers` contra el ruleset del juego
(`deckConfig.deckSets = "FIXED_2"`).

### 4.2 Identidad de carta

Cada carta física tiene un **ID único global** dentro de la partida:

```
CARD(id, setIndex, color, suit, rank, jokerId?)
```

El `setIndex` distingue los 2 juegos (rojo/negro). Los jokers tienen `suit=null` y
`rank=null` pero un `jokerId`.

### 4.3 Extensiones futuras (diseñadas, no implementadas)

- Pares de colores alternativos: `RED/BLUE`, `RED/GREEN`, etc. → solo cambia el
  mapa `color→palos` en la config.
- Número variable de juegos y jokers por juego.
- Barajas de otros juegos (misma baraja u otras) → nueva `DeckConfig`.

---

## 5. Actores y casos de uso principales

| Actor | Descripción |
|---|---|
| **Jugador (no autenticado)** | Sin sesión. Solo ve la pantalla de **login con Google**. No accede a nada más. |
| **Jugador (autenticado con Google)** | Usuario con cuenta vinculada a Google. Perfil, amigos, salas y partidas. |
| **Administrador** | Usuario con rol `ADMIN`. Opera desde una **web de administración (no móvil)**: gestión de juegos y rulesets, usuarios, avisos, monitoreo del backend y estadísticas. |

Casos de uso de alto nivel:

```
UC-01 Login con Google OAuth 2.0 (obligatorio para jugar)
UC-02 Ver/editar perfil
UC-03 Obtener/renovar código de amigo
UC-04 Añadir amigo por código
UC-05 Ver/eliminar amigos
UC-06 Crear sala (privada o pública)
UC-07 Unirse a sala (por código o lista pública)
UC-08 Salir de sala
UC-09 Iniciar partida desde sala
UC-10 Jugar una mano de Carioca (robar, bajar, descartar, declarar)
UC-11 Terminar partida y ver resultado
UC-12 Ver historial y estadísticas
```

> Detalle de flujos y mensajes en `docs/api.md`.

---

## 6. Requisitos funcionales (FR)

Prioridad con **MoSCoW**: `MUST` (imprescindible), `SHOULD` (importante),
`COULD` (deseable), `WONT` (explícitamente fuera por ahora).

### 6.1 Autenticación y perfil (AUT)

**El login es obligatorio para jugar: no existe uso anónimo.** El único método de
autenticación es **Google OAuth 2.0**. El backend valida el **ID token** de Google
(firma + `aud` + `iss`), crea/vincula la cuenta local y emite sus propios JWTs
de sesión (`access` + `refresh`).

| ID | Requisito | Prioridad |
|---|---|---|
| FR-AUT-01 | **Login obligatorio para jugar.** Todo acceso a amigos, salas, partidas, historial o estadísticas exige sesión autenticada. Sin sesión, la app solo muestra la pantalla de login con Google. | MUST |
| FR-AUT-02 | El usuario inicia sesión **exclusivamente con Google OAuth 2.0** (Google Sign-In en Android; flujo ID token → backend). No hay registro con email/contraseña. | MUST |
| FR-AUT-03 | El backend **valida el ID token** de Google (firma con JWKS, `iss=https://accounts.google.com`, `aud` del cliente) y solo acepta tokens válidos y vigentes. | MUST |
| FR-AUT-04 | El backend **crea la cuenta local la primera vez** (login auto-registro) usando `sub` (subject) de Google como identificador estable, y la **vincula** al perfil (alias, avatar, email). | MUST |
| FR-AUT-05 | Tras validar Google, el backend emite su **propio JWT de sesión** (`access` corto + `refresh` rotativo). Ver `docs/security.md`. | MUST |
| FR-AUT-06 | El usuario puede **cerrar sesión** en todos los dispositivos o en uno (revoca refresh tokens). | SHOULD |
| FR-AUT-07 | El usuario puede ver y editar su **perfil** (alias, avatar, idioma), desvinculado de los datos de Google (no se edita la cuenta de Google). | SHOULD |
| FR-AUT-08 | El sistema permite **revincular la cuenta de Google** (`ADR-016`): un usuario autenticado puede **desvincular su Google actual y asociar otro**, conservando perfil, amigos y partidas. Se rechaza si el nuevo Google ya está vinculado a otra cuenta. Y permite la **baja de cuenta** (borrado de datos, `NFR-LEGAL-02`). | MUST |
| FR-AUT-09 | El **alias inicial** se deriva del nombre de Google; el avatar se toma del perfil de Google al primer login. | SHOULD |

### 6.2 Amigos (AMI)

| ID | Requisito | Prioridad |
|---|---|---|
| FR-AMI-01 | Cada usuario tiene un **código de amigo** único (formato definido en §8.2). | MUST |
| FR-AMI-02 | El usuario puede **regenerar** su código (invalida el anterior). | SHOULD |
| FR-AMI-03 | El usuario puede **enviar solicitud de amistad** a otro usuario usando su código. La amistad es **mutua (bidireccional)**: requiere que el destinatario **acepte**. | MUST |
| FR-AMI-04 | El usuario puede **ver su lista de amigos** y su estado (online/en partida). | MUST |
| FR-AMI-05 | El usuario puede **eliminar una amistad**. Al hacerlo, **ambas partes dejan de verse**: el estado online/en partida y la lista de amigos se actualizan para los dos (se borra el vínculo `ACCEPTED`), y no se conserva la solicitud pendiente entre ellos. | MUST |
| FR-AMI-06 | El usuario recibe **notificación** cuando un amigo lo agrega o está online. | COULD |
| FR-AMI-07 | El usuario puede **ver sus solicitudes de amistad pendientes**, **aceptar** o **rechazar** cada una. Al aceptar, la amistad queda activa para ambos. | MUST |

### 6.3 Salas (SAL)

| ID | Requisito | Prioridad |
|---|---|---|
| FR-SAL-01 | El usuario puede **crear una sala** indicando: juego, visibilidad (pública/privada), nº de jugadores, configuración de baraja y **rondas a jugar** (cantidad y/o modos, para Carioca). El **nº de jugadores se valida contra el máximo del juego** (Carioca: **máximo 4**; no admite más). | MUST |
| FR-SAL-02 | Las salas **privadas** tienen un **código de acceso** para unirse. | MUST |
| FR-SAL-03 | El usuario puede **unirse** a una sala por código o desde la lista de públicas. | MUST |
| FR-SAL-04 | El dueño puede **iniciar la partida** cuando hay jugadores suficientes (config). | MUST |
| FR-SAL-05 | Los jugadores pueden **abandonar** la sala; el dueño puede **expulsar**. | MUST |
| FR-SAL-06 | La sala muestra estado de los jugadores (listo/no listo), config y posición (asiento). | SHOULD |
| FR-SAL-07 | Las salas **caducan** si quedan vacías o tras X tiempo inactivas. | SHOULD |
| FR-SAL-08 | El sistema permite **emparejamiento automático** (cola de matchmaking). | WONT (extensión, ver §6.6) |
| FR-SAL-09 | Al **terminar la partida** (fin de rondas, abandono o timeout), la sala se **cierra** y se liberan sus recursos (se emite `GAME_END` + `ROOM_CLOSED`). | MUST |

### 6.4 Motor de juego genérico (JGO)

| ID | Requisito | Prioridad |
|---|---|---|
| FR-JGO-01 | El motor expone una **abstracción de juego** (`Game`) que permite registrar múltiples juegos (Strategy + Factory + Registry). | MUST |
| FR-JGO-02 | Las **reglas** de cada juego se definen como **configuración/datos** (ruleset), no embebidas en el flujo. | MUST |
| FR-JGO-03 | El motor gestiona: **baraja, reparto, mesa, turnos, pozo/descarte, acciones y eventos**. | MUST |
| FR-JGO-04 | El motor es **autoritativo**: valida cada acción contra el estado y solo acepta las legales. | MUST |
| FR-JGO-05 | El estado de juego se modela como **máquina de estados** (State pattern) con transiciones validadas. | MUST |
| FR-JGO-06 | Todas las acciones se registran como **eventos** para auditoría y **replay** (event sourcing). | MUST |
| FR-JGO-07 | El barajado es **determinista con semilla** (reproducible para auditoría/replay) y estadísticamente seguro. | MUST |
| FR-JGO-08 | El motor es **agnóstico al transporte**: puede ejecutarse en servidor (online) o en el cliente (vs. bots / offline). | SHOULD |
| FR-JGO-09 | Las **extensiones** (nuevo juego, nuevo palo, nuevo color) no requieren cambios en el núcleo. | MUST |
| FR-JGO-10 | El motor soporta **puntuación configurable** por juego (Strategy de scoring). | MUST |

### 6.5 Juego Carioca (CAR)

Reglas oficiales del producto definidas en `docs/games/carioca/rules.md` (basadas en la
tradición de la Carioca chilena). Resumen de requisitos:

| ID | Requisito | Prioridad |
|---|---|---|
| FR-CAR-01 | Se juega con la **configuración de baraja** de §4.1 (**siempre los 2 juegos = 108 cartas**, para 2, 3 o 4 jugadores) y las **reglas oficiales** de `docs/games/carioca/rules.md`. | MUST |
| FR-CAR-02 | Se reparten **12 cartas** por jugador (contra las agujas del reloj; el repartidor recibe la última y juega primero el jugador a su izquierda). Hay **pozo** (stock) y **pozo de descarte** (basura/montón). | MUST |
| FR-CAR-03 | En su turno el jugador **roba** del pozo o del descarte (solo la carta superior; no se revisan cartas previas del descarte) y debe **descartar** una carta. No puede botar una carta recién descartada si con esa gana (última ronda). | MUST |
| FR-CAR-04 | El jugador puede **bajarse** cuando cumple la combinación requerida de la ronda actual: **tríos** (3 cartas del mismo valor, sin importar pinta) y **escalas** (4+ cartas consecutivas de la misma pinta, con giro permitido: `2-A-K-Q`). | MUST |
| FR-CAR-05 | **Rondas y variantes configurables antes de jugar:** el ruleset define el **catálogo de rondas** (9 base + opcionales: sucia, imperial, payaso, real) y las **variantes** (comodines rojos, −10 por corte, jokers reciclables…, desactivadas por defecto). Al **crear la sala** los jugadores eligen **qué rondas / cuántas rondas** y qué **variantes** activar; **no se obliga a un juego completo**. La partida usa solo lo seleccionado (`roomConfig.rounds` + `roomConfig.variants`). Tabla en `docs/games/carioca/rules.md §7` y `§9`. | MUST |
| FR-CAR-06 | **Comodines (jokers):** máx. 1 comodín por trío/escala al bajarse; no pueden quedar jokers juntos; sin descartar jokers en rondas que los permiten; no se valen más de 2 comodines en una misma mano. Reglas en `docs/games/carioca/rules.md §6`. | MUST |
| FR-CAR-07 | **Bajarse y "corte":** quien se baja completo (sin cartas) gana la ronda; los demás suman los puntos de su mano al instante. El ganador **no suma** (regla estándar; variante **−10 por corte** seleccionable al crear la sala, `ADR-018`). Gana la partida quien acumula **menos puntos** tras las **rondas configuradas**; desempate por más rondas ganadas. | MUST |
| FR-CAR-08 | **Añadir a juegos ajenos (lay-off):** solo quienes ya se bajaron pueden extender combinaciones de otros bajados; el que se baja por primera vez no puede añadir en ese mismo momento, sino desde su **turno siguiente** (y añadir a juegos propios sí está permitido desde la misma bajada). | MUST |
| FR-CAR-09 | **Valor de cartas** (base): 2–10 su valor, J/Q/K = 10, A = 20, Joker = 30. Configurable por ruleset. | MUST |
| FR-CAR-10 | La **última ronda (Escala Real, 13 cartas)** se reparte con 12 cartas y el jugador se baja con sus 13 cartas ganando sin descartar. | MUST |
| FR-CAR-11 | Las reglas (rondas, valores, comodines, variantes regionales) se parametrizan en el **Ruleset** (`docs/game-engine.md §9`). | SHOULD |
| FR-CAR-12 | El juego **no es por equipos por defecto** (individual). Modo equipos queda como variante COULD para la versión 4 jugadores. | COULD |
| FR-CAR-13 | **Abandono de ronda:** el jugador puede **decidir no seguir jugando** en cualquier momento (con confirmación). El abandono aplica a la **ronda actual**: se registra `PLAYER_ABANDONED_ROUND`. Los demás jugadores **confirman si continúan a la siguiente ronda**; si confirman, la partida **continúa a la siguiente ronda sin el que abandona**; si no, la partida **termina** (`GAME_END`, `FORFEIT`). En todos los casos el servidor persiste y **libera los recursos**. | MUST |
| FR-CAR-14 | **Carioca admite de 2 a 4 jugadores como máximo.** El sistema **no permite agregar más** (validación en sala, motor y backend). La baraja es **fija**: siempre los 2 juegos (108 cartas), sin importar el nº de jugadores. | MUST |
| FR-CAR-15 | **Timeout de turno:** si el jugador **no elige a tiempo**, el motor juega **aleatorio** por él (roba y descarta legales). Si el jugador acumula **2 timeouts** en la partida, se considera **abandono** (aplica la política de `FR-CAR-13`). | MUST |
| FR-CAR-16 | **Diseño de cartas personalizable** con opciones predefinidas: reverso (colores sólidos y patrones, **configurable por separado para cada uno de los 2 mazos** de §4.1), frontal (bordes) y joker (estilos; el joker **sin colorear** siempre se muestra en blanco y negro). La elección se hace en la **pantalla de ajustes** con **vista previa en vivo** y se **persiste** entre sesiones; aplica al renderizado de todas las cartas de la partida. | SHOULD |

### 6.6 Extensiones futuras (EXT)

| ID | Requisito | Prioridad |
|---|---|---|
| FR-EXT-01 | **Matchmaking automático**: el usuario entra en una cola y el sistema lo empareja por config deseada. | WONT (diseñado) |
| FR-EXT-02 | **Nuevos juegos** con la misma baraja (p. ej. Ronda, Truco, loba). Solo se agrega un ruleset + UI. **Carioca es un juego más de la plataforma, no el único.** | WONT (diseñado) |
| FR-EXT-03 | **Pares de colores configurables** por el usuario (rojo/negro, rojo/azul…). | WONT (diseñado) |
| FR-EXT-04 | **Espectadores** en salas y partidas. | WONT |
| FR-EXT-05 | **Replay de partidas** desde el registro de eventos. | COULD |

---

## 7. Requisitos no funcionales (NFR)

### 7.1 Rendimiento y escalabilidad (PERF)

| ID | Requisito |
|---|---|
| NFR-PERF-01 | **Latencia:** las acciones de juego se aplican y propagan en < **500 ms** (p95) en condiciones normales. |
| NFR-PERF-02 | **Concurrencia:** el backend soporta al menos **1.000 usuarios conectados** simultáneamente en el MVP (diseño permite escalar horizontalmente). |
| NFR-PERF-03 | El tiempo de respuesta de la API REST (no juego) es < **300 ms** (p95). |
| NFR-PERF-04 | Los mensajes WebSocket se emiten por sala/partida; no se retransmite a salas ajenas. |
| NFR-PERF-05 | El consumo de datos móviles es optimizado (payloads mínimos, sin polling de estado). |
| NFR-PERF-06 | El barajado/reparto no bloquea el hilo de eventos (diseño asíncrono). |
| NFR-PERF-07 | **Limpieza de partidas inactivas:** idle timeout y caducidad de salas vacías para no acumular memoria ni conexiones (anti-"zombies"). Ver `docs/game-engine.md §11.3`. |

### 7.2 Seguridad (SEC)

| ID | Requisito |
|---|---|
| NFR-SEC-01 | Toda la comunicación usa **TLS/WSS** en producción. |
| NFR-SEC-02 | Autenticación con **Google OAuth 2.0** (única vía) + **JWT** propio de sesión (access corto + refresh rotativo). Ver `docs/security.md`. |
| NFR-SEC-03 | **Autorización por recurso:** cada endpoint verifica permisos (dueño de sala, miembro de partida, etc.). |
| NFR-SEC-04 | **Servidor autoritativo:** el cliente solo envía intenciones; el servidor valida contra el estado real (anti-trampa). |
| NFR-SEC-05 | **Protección anti-abuso:** rate limiting (login, creación de salas, códigos), validación estricta del ID token de Google. |
| NFR-SEC-06 | **Validación de entrada** en todas las capas (sanitización, tamaño máximo de payloads). |
| NFR-SEC-07 | **Privacidad:** códigos de amigo y sala no revelan datos personales; los datos sensibles no se exponen en logs (incluidos ID tokens). |
| NFR-SEC-08 | Cumplir **OWASP Top 10** y revisión de dependencias (SCA). |
| NFR-SEC-09 | **No almacenar secretos en el cliente**; el `client_id`/`server_client_id` de Google se gestionan como recursos de la app; las claves del servidor se rotan. |

### 7.3 Disponibilidad y confiabilidad (AVAIL)

| ID | Requisito |
|---|---|
| NFR-AVAIL-01 | Disponibilidad objetivo **99,5 %** (tolerable en MVP). |
| NFR-AVAIL-02 | **Reconexión:** si el cliente pierde conexión, puede reconectarse y resincronizar el estado de la partida. |
| NFR-AVAIL-03 | Las **partidas se persisten** (event log + snapshots) para recuperación ante caídas. |
| NFR-AVAIL-04 | **Graceful shutdown** del backend sin corromper partidas activas. |
| NFR-AVAIL-05 | **Timeouts de jugador:** si un jugador no actúa en X segundos, el motor **juega aleatorio** por él. **2 timeouts** en la partida → se trata como **abandono** (`FR-CAR-13`). |
| NFR-AVAIL-06 | **Liberación de recursos:** toda partida que termina (normal, abandono, timeout, admin) **persiste su estado final y libera memoria** (cancela temporizadores, cierra el executor, se elimina del registro). Sin partidas "zombie" ni fugas. Ver `docs/game-engine.md §11`. |

### 7.4 Usabilidad y accesibilidad (UX)

| ID | Requisito |
|---|---|
| NFR-UX-01 | App usable en **modo retrato y horizontal**, y en **modo oscuro/claro**. |
| NFR-UX-02 | Textos **localizables** (ES base; EN como COULD). Sin texto hardcodeado en la UI. |
| NFR-UX-03 | **Accesibilidad básica:** contraste, tamaños de fuente, acciones con significado (contentDescription en cartas/iconos). |
| NFR-UX-04 | **Feedback** de estado: turno actual, acciones disponibles, reconexión, latencia de red. |
| NFR-UX-05 | Orientación/onboarding corto para el nuevo jugador (COULD). |

### 7.5 Compatibilidad y plataforma (COMP)

| ID | Requisito |
|---|---|
| NFR-COMP-01 | Android con **min SDK 26** (Android 8.0) y target al último estable. |
| NFR-COMP-02 | Se verifica en **pantallas** de teléfonos (y tabletas como COULD), distintas densidades y resoluciones. |
| NFR-COMP-03 | Soporte de **notificaciones** (llegadas de turno, invitaciones) (SHOULD). |
| NFR-COMP-04 | El backend corre sobre **Docker** en cualquier proveedor (Cloud VPS/VM). |

### 7.6 Mantenibilidad y extensibilidad (MAINT)

| ID | Requisito |
|---|---|
| NFR-MAINT-01 | Código en **inglés** (identificadores), documentación en español. |
| NFR-MAINT-02 | **Clean Architecture** en ambos lados; dependencias apuntan al dominio. |
| NFR-MAINT-03 | **SOLID** obligatorio; se documentan los patrones aplicados (ver `docs/design-patterns.md`). |
| NFR-MAINT-04 | **Cambios que no rompan el núcleo**: añadir juegos/colores/reglas = agregar config o estrategia, sin modificar el motor. |
| NFR-MAINT-05 | **Testing** con cobertura mínima del 80 % en el motor de juego; el resto según prioridad. |
| NFR-MAINT-06 | Documentación viva: cualquier cambio de comportamiento actualiza `SPECS.md` o el ADR correspondiente. |

### 7.7 Calidad y pruebas (QA)

| ID | Requisito |
|---|---|
| NFR-QA-01 | El motor de juego usa **TDD** (estado, acciones legales/ilegales, barajado). |
| NFR-QA-02 | Pruebas unitarias, de integración y **contract tests** entre app y backend. |
| NFR-QA-03 | **Pruebas end-to-end** del flujo crítico (registro → amigos → sala → partida completa). |
| NFR-QA-04 | **Pruebas de carga** de WebSocket (concurrencia y latencia) antes de producción. |
| NFR-QA-05 | CI/CD con lint, build, test y análisis estático (SonarQube/SpotBugs). |

### 7.8 Legales y privacidad (LEGAL)

| ID | Requisito |
|---|---|
| NFR-LEGAL-01 | Política de **privacidad** y **términos de servicio** visibles en la app. |
| NFR-LEGAL-02 | Consentimiento para datos personales y **baja de cuenta** (borrado de datos). |
| NFR-LEGAL-03 | Solo mayores de edad o con permiso parental (según jurisdicción). |
| NFR-LEGAL-04 | Sin publicidad invasiva ni telemetría de datos innecesaria. |

---

## 8. Restricciones y estándares obligatorios

### 8.1 Restricciones técnicas

- Backend en **Java 21 (LTS)** con **Spring Boot 3.x** (Framework ya decidido por producto).
- Cliente en **Kotlin + Jetpack Compose** (decidido).
- **Autenticación única con Google OAuth 2.0** (Google Sign-In en Android;
  validación de ID token con Spring Security OAuth2 Resource Server/JWKS).
- Comunicación real-time con **WebSocket/STOMP**.
- Persistencia: **PostgreSQL** (+ **Redis** para sesiones/caché/rate-limit).

### 8.2 Formatos de códigos

| Código | Formato | Ejemplo |
|---|---|---|
| Código de amigo | 8 caracteres alfanuméricos sin caracteres ambiguos (`O/0`, `I/1`, `l`), insensible a mayúsculas | `A7K3QM8X` |
| Código de sala | 6 caracteres alfanuméricos, mismo set | `XY12AZ` |

- No son secretos, pero se regeneran a petición.
- Se normalizan a mayúsculas en el servidor.

### 8.3 Estándares de diseño

- **Clean Architecture** + **MVVM** (Android) y **arquitectura por capas** (Backend).
- **Programación orientada a objetos** con énfasis en **encapsulamiento**, **inmutabilidad** y **composición sobre herencia**.
- **Patrones obligatorios** y **mapeo SOLID**: ver `docs/design-patterns.md`.
- **Sin dependencias circulares** entre módulos.

---

## 9. Comunicación (alto nivel)

- **REST** (`/api/v1`) para: auth, perfil, amigos, salas, historial, stats.
- **WebSocket/STOMP** para: estado de sala y partida, turnos, acciones y chat.
- El cliente **nunca** muta el estado de juego por su cuenta: envía acciones y recibe eventos.

Detalle de endpoints, tópicos y payloads en `docs/api.md`.

---

## 10. Modelo de datos (alto nivel)

Entidades principales:

```
User ──< FriendCode (1:1)        User ──< Friendship (N:M)
User ──< RoomMembership >── Room ──< Game
Room ──> GameSession ──< GameEvent (event sourcing)
GameSession ──< PlayerHand / PlayerScore
DeckConfig ──< GameSession          Ruleset ──< GameSession
```

Detalle y diagrama ER en `docs/database.md`.

---

## 11. Definición de Hecho (DoD)

Una historia/feature se considera **terminada** solo si cumple:

1. Código implementado según patrones y SOLID (`docs/design-patterns.md`).
2. Pruebas unitarias (y de integración si aplica) **verdes**, cobertura según `NFR-MAINT-05`.
3. Lint y análisis estático sin errores bloqueantes.
4. Sin secretos en el código; contrato de seguridad respetado (`docs/security.md`).
5. El comportamiento está documentado (SPECS/ADR) si cambia lo establecido.
6. La UI cumple `NFR-UX-01` a `NFR-UX-04` y no introduce textos hardcodeados.
7. Flujo validado en al menos un dispositivo real (si aplica UI).

## 12. Criterios de aceptación del MVP (release 1.0)

- [ ] Login con **Google OAuth 2.0** funcionando (ID token → JWT propio) y **acceso denegado sin sesión** (`FR-AUT-*`).
- [ ] Amigos por código: enviar solicitud, aceptar/rechazar, listar, eliminar (`FR-AMI-*`).
- [ ] Crear/entrar/salir de salas, inicio de partida (`FR-SAL-*`).
- [ ] Partida completa de **Carioca 2–4 jugadores online** con tiempo real, con las **rondas configuradas en la sala** (incluida la Escala Real si se selecciona) (`FR-CAR-*` + `FR-JGO-*`).
- [ ] Estado de partida persistido y recuperable ante desconexión (`NFR-AVAIL-02/03`).
- [ ] Reconexión y resincronización de estado.
- [ ] **Timeout:** un jugador que no actúa es jugado **aleatorio**; con **2 timeouts** se trata como abandono (`FR-CAR-15`).
- [ ] **Abandono de ronda:** un jugador abandona, los demás **confirman** y la partida **continúa a la siguiente ronda sin él** (o termina si no confirman); el servidor libera los recursos (`FR-CAR-13`, `NFR-AVAIL-06`).
- [ ] Cobertura de pruebas del motor ≥ 80 %.
- [ ] Cumplimiento de requisitos NFR de rendimiento (`NFR-PERF-01`).

---

## 13. Preguntas abiertas (requieren decisión de producto)

> Las reglas de Carioca ya están definidas (`docs/games/carioca/rules.md`). Quedan
> decisiones de producto no técnicas:

| # | Pregunta | Impacto |
|---|---|---|
| Q1 | ~~¿La amistad es **unidireccional** (seguir) o **mutua** (ambos aceptan)?~~ **RESUELTO:** amistad **mutua y bidireccional**: uno envía la solicitud por código y el otro **acepta** (`FR-AMI-03/07`, `ADR-010`). | — |
| Q2 | ~~¿Se permiten las **rondas opcionales** (sucia, imperial, payaso) desde el MVP o solo las 9 obligatorias?~~ **RESUELTO:** las rondas son **configurables al crear la sala**: se eligen cuántas y qué modos jugar (9 base + opcionales); **no se obliga a un juego completo** (`FR-CAR-05`, `FR-SAL-01`, `ADR-011`). | — |
| Q3 | ~~¿Cuántos jugadores por partida?~~ **RESUELTO:** máximo **4 jugadores** en Carioca (`FR-CAR-14`, `ADR-009`). | — |
| Q4 | ~~¿**Comodines rojos** (2 rojos como comodín, variante regional) fuera o dentro del ruleset base?~~ **RESUELTO:** **regla estándar**: los **únicos comodines son los 4 jokers** (2 por juego: uno coloreado y uno sin colorear). Los **2 rojos NO son comodines** en la base; quedan como **variante opcional desactivada por defecto** (`ADR-017`). | Ruleset |
| Q5 | ~~¿Se aplica el **−10 por corte** al ganador (variante) o no?~~ **RESUELTO:** **regla estándar**: el ganador de la ronda **suma 0** (sin −10). La variante **−10 por corte** queda **desactivada por defecto** pero **seleccionable al crear la sala** (junto con rondas y variantes, `roomConfig.variants`) (`ADR-018`). | Scoring |
| Q6 | ~~Política de **timeout** de jugador (pasar turno automático, bot, expulsión).~~ **RESUELTO:** timeout → el motor **juega aleatorio**; **2 timeouts** → **abandono** (`FR-CAR-15`, `ADR-012`). | — |
| Q7 | ¿Ranking/estadísticas desde el inicio o en fase 3? | Alcance MVP |
| Q8 | ~~¿Vincular un usuario a **una sola cuenta de Google** o permitir revincular?~~ **RESUELTO:** **se permite revincular** (opción B): un usuario autenticado puede cambiar su cuenta de Google y conservar su cuenta local (perfil, amigos, partidas). Se valida que el nuevo Google no esté ya vinculado a otra cuenta y se revocan las sesiones previas (`FR-AUT-08`, `ADR-016`). | Cuenta |
| Q9 | ~~**Abandono de partida:** ¿qué penalización recibe quien abandona (p. ej. +puntos, se cuenta como último) y se confirma la regla de "terminar la partida" (no continuar con bot)?~~ **RESUELTO:** el **abandono es por ronda**; los demás jugadores confirman y, si aceptan, la partida **continúa a la siguiente ronda sin el que abandona** (si no, termina). La penalización del que abandona es configurable por ruleset (`FR-CAR-13`, `ADR-013`). | — |

> Cada decisión debe quedar registrada como **ADR** (`docs/adr.md`).
