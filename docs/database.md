# Modelo de datos

> Esquema relacional (PostgreSQL) para persistencia. Los **eventos de juego**
> usan una tabla de log (event sourcing) + snapshots. Asociado: `docs/api.md`,
> `docs/game-engine.md`.

---

## 1. Diagrama ER

```mermaid
erDiagram
  USERS {
    uuid id PK
    string google_sub UK
    string email UK
    string alias
    string avatar_url
    string locale
    timestamp created_at
    boolean banned
  }

  FRIEND_CODES {
    uuid user_id PK
    string code UK
    timestamp regenerated_at
  }

  FRIENDSHIPS {
    uuid id PK
    uuid requester_id FK
    uuid addressee_id FK
    string status        # PENDING | ACCEPTED
    timestamp created_at
    timestamp responded_at
  }

  REFRESH_TOKENS {
    uuid id PK
    uuid user_id FK
    string token_hash
    timestamp expires_at
    timestamp revoked_at
    string device_id
  }

  ACCOUNT_LINKS {
    uuid id PK
    uuid user_id FK
    string google_sub       # sub vinculado en cada periodo
    timestamp linked_at
    timestamp unlinked_at   # NULL si es el vínculo actual
  }

  ROOMS {
    uuid id PK
    string code UK
    string game_type
    string visibility
    int max_players
    uuid owner_id FK
    uuid deck_config_id FK
    uuid ruleset_id FK
    string status
    timestamp created_at
    timestamp expires_at
  }

  ROOM_MEMBERS {
    uuid room_id FK
    uuid user_id FK
    int seat
    string status
    timestamp joined_at
  }

  DECK_CONFIGS {
    uuid id PK
    string name
    jsonb suits
    jsonb ranks
    jsonb colors
    int deck_sets
    int jokers_per_set
    bool active
  }

  RULESETS {
    uuid id PK
    string game_type
    string version
    jsonb definition
    bool active
  }

  GAME_SESSIONS {
    uuid id PK
    uuid room_id FK
    string game_type
    uuid deck_config_id FK
    uuid ruleset_id FK
    string state
    int current_round
    jsonb snapshot
    long last_seq
    timestamp started_at
    timestamp ended_at
    string winner
  }

  GAME_EVENTS {
    bigint id PK
    uuid game_id FK
    long seq
    string type
    uuid actor_id
    jsonb payload
    timestamp created_at
  }

  SESSION_PLAYERS {
    uuid game_id FK
    uuid user_id FK
    int seat
    string result
    int score
    jsonb final_hand
  }

  CHAT_MESSAGES {
    bigint id PK
    uuid room_id FK
    uuid user_id FK
    text message
    timestamp created_at
  }

  STATS {
    uuid user_id PK
    int games_played
    int wins
    int total_points
    timestamp updated_at
  }

  USERS ||--o| FRIEND_CODES : "1:1"
  USERS ||--o{ FRIENDSHIPS : "friend"
  USERS ||--o{ REFRESH_TOKENS : ""
  USERS ||--o{ ROOMS : "owner"
  USERS ||--o{ SESSION_PLAYERS : ""
  USERS ||--o{ CHAT_MESSAGES : ""
  USERS ||--o| STATS : "1:1"

  ROOMS ||--o{ ROOM_MEMBERS : ""
  ROOMS ||--o| GAME_SESSIONS : ""
  ROOMS ||--o{ CHAT_MESSAGES : ""

  DECK_CONFIGS ||--o{ ROOMS : ""
  RULESETS ||--o{ ROOMS : ""
  DECK_CONFIGS ||--o{ GAME_SESSIONS : ""
  RULESETS ||--o{ GAME_SESSIONS : ""

  GAME_SESSIONS ||--o{ GAME_EVENTS : ""
  GAME_SESSIONS ||--o{ SESSION_PLAYERS : ""
```

---

## 2. Tablas y notas

| Tabla | Notas |
|---|---|
| `users` | Datos de cuenta y perfil. `google_sub` (subject de Google) es el identificador estable único **del vínculo actual** (se actualiza al revincular, `ADR-016`); `email` único. Sin contraseñas propias (identidad delegada a Google). El historial de sub vinculados vive en `account_links`. |
| `friend_codes` | 1:1 con usuario. Código regenerable (`SPECS §8.2`). |
| `friendships` | Solicitudes de amistad mutuas (`ADR-010`). `status`: `PENDING`/`ACCEPTED`; `requester_id` envía, `addressee_id` acepta/rechaza. Índice por ambos usuarios. |
| `refresh_tokens` | Hash del token, rotación, revocación por dispositivo. Duración: **7 días**. |
| `account_links` | Historial de vinculaciones de Google por usuario (`ADR-016`). `google_sub` es el sub vinculado en cada periodo; `unlinked_at` NULL = vínculo actual. Permite trazabilidad al revincular y detección de colisiones (`google_sub` único entre filas activas). |
| `token_blacklist` | `jti` de tokens revocados antes de su expiración (access ≤ 15 min; refresh ≤ 7 días). En producción vive en **Redis** con TTL; esta tabla es el respaldo persistente. |
| `session_versions` | Contador de versión de sesión por usuario para **logout global** (invalida todos los tokens anteriores de golpe). |
| `rooms` | Salas. `status`: OPEN / STARTING / PLAYING / CLOSED. Caducidad (`FR-SAL-07`). |
| `room_members` | Asientos y estado (WAITING / READY / IN_GAME / KICKED / ABANDONED). `ABANDONED` = abandono de ronda confirmado: la partida continúa a la siguiente ronda sin el jugador (`FR-CAR-13`, `ADR-013`). |
| `deck_configs` | Definición configurable de baraja (`SPECS §4`). JSONB para palos/rangos/colores. |
| `rulesets` | Reglas por juego, versionadas. JSONB para flexibilidad + validación. |
| `game_sessions` | Partida. `snapshot` (última proyección) + `last_seq` para resync. |
| `game_events` | Log de eventos (append-only). Índice por `(game_id, seq)`. |
| `session_players` | Resultado y puntaje por jugador al terminar. |
| `chat_messages` | Chat de sala (COULD en MVP). |
| `stats` | Estadísticas agregadas por usuario. |

## 3. Decisiones de modelado

- **JSONB para configuración:** `deck_configs` y `rulesets` son flexibles por
  diseño; las reglas cambian sin migraciones de esquema. La **validación** ocurre
  en el motor (validador de ruleset), no en la BD.
- **Event sourcing:** `game_events` es append-only; `game_sessions.snapshot`
  evita replays largos. Se archiva (COULD) a partidas antiguas.
- **No se guardan manos completas** en medio de la partida en tablas de negocio:
  viven en el estado del motor + eventos.
- Índices recomendados: `friendships(user_id)`, `rooms(code) UK`,
  `game_events(game_id, seq)`, `room_members(room_id)`.

## 4. Migraciones

- **Flyway** (SQL versionado `V1__...`, `V2__...`), integrado en CI.
- Cambios destructivos prohibidos sin ACK de producto y documento de migración.
