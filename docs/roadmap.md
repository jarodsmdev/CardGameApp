# Roadmap — Plan de fases

> Hoja de ruta del proyecto. Prioridades según `SPECS.md` y criterios de salida
> (DoD por fase). La documentación antecede al código (Fase 0).

---

## Fase 0 — Diseño y especificación *(actual)*

**Objetivo:** tener todo definido antes de escribir código.

- [x] Validar con producto las preguntas abiertas (`SPECS §13`) → fijar `Ruleset` de Carioca. *(Q1–Q9 resueltas)*
- [x] Resolver ADRs pendientes (`docs/adr.md`). *(ADR-001..018)*
- [x] Definir decisiones de amistad (unidireccional/mutua). *(mutua, ADR-010)*
- [x] Aprobar la línea base de la baraja y el flujo de **login con Google OAuth 2.0** (`SPECS §4`, `§8.1`).

> **RESUELTO:** nº de jugadores de Carioca = **2 a 4 máximo** (`FR-CAR-14`, `ADR-009`).
> **RESUELTO:** amistad **mutua** (solicitud + aceptación) (`FR-AMI-03/07`, `ADR-010`).
> **RESUELTO:** rondas **configurables por sala** (cantidad + modos, catálogo base + opcionales) (`FR-CAR-05`, `ADR-011`).
> **RESUELTO:** **timeout** = jugada aleatoria; **2 timeouts = abandono** (`FR-CAR-15`, `ADR-012`).
> **RESUELTO:** **abandono por ronda** con confirmación de los demás para continuar (`FR-CAR-13`, `ADR-013`).
> **RESUELTO:** **monorepo** (`frontend/movil`, `frontend/web`, `backend/`, `docs/`) (`ADR-014`).
> **RESUELTO:** **Git Flow** como modelo de ramas (`ADR-015`, guía en `docs/gitflow.md`).
> **RESUELTO:** **revincular cuenta de Google** permitido (opción B) (`FR-AUT-08`, `ADR-016`).
> **RESUELTO:** **comodines base** = solo los 4 jokers (2 por juego: uno coloreado y uno sin colorear); los 2 rojos NO son comodines (variante desactivada) (`ADR-017`).
> **RESUELTO:** **puntuación estándar** = ganador suma 0, sin −10 por corte (variante desactivada) (`ADR-018`).

**Criterio de salida:** `SPECS.md` aprobado + ADRs resueltos + ruleset de Carioca validado.

---

## Fase 1 — Motor y MVP local (sin red)

**Objetivo:** motor genérico funcionando + Carioca jugable vs. bots.

- Backend:
  - [ ] `domain/core` (baraja, estados, turnos, eventos, snapshot).
  - [ ] `CariocaGame` + `Ruleset` + validador.
  - [ ] Tests TDD del motor (cobertura ≥ 80 %).
- Cliente (Android):
  - [ ] Motor en cliente (misma librería de dominio).
  - [ ] Tablero Carioca (Compose) para partida local vs. bots.
  - [ ] Navegación base y temas.
  - [ ] Feedback de turno del jugador: pulso en mazo/pozo y carta activa,
        tick de jugados y cuenta regresiva con aviso visual de timeout (solo
        avisa, no juega solo en local).

**Criterio de salida:** una partida completa de Carioca vs. 3 bots en el dispositivo.

---

## Fase 2 — Online: auth, amigos, salas y tiempo real

**Objetivo:** multijugador real (MVP de producto).

- Backend:
  - [ ] **Auth con Google OAuth 2.0** (validación de ID token, cuenta local, JWT propio + refresh).
  - [ ] Amigos por código + regeneración.
  - [ ] Salas (crear/unirse/listar/ready/start/kick).
  - [ ] WebSocket/STOMP: sala y partida, eventos, resync/reconexión.
  - [ ] Persistencia: sessions, eventos, snapshots. Flyway.
- Cliente:
  - [ ] **Pantalla de login con Google Sign-In** (obligatorio; sin sesión no hay navegación).
  - [ ] Pantallas de perfil, amigos, salas, partida online.
  - [ ] Reconexión y resincronización por `seq`.
  - [ ] Contract tests cliente↔servidor (`docs/api.md`).

**Criterio de salida:** partida de Carioca 2–4 jugadores online completa (9 rondas
incluida la Escala Real), con login obligatorio de Google, desconexión/reconexión
sin romper el estado. → **Release 1.0** (criterios en `SPECS §12`).

---

## Fase 3 — Plataforma

**Objetivo:** retención, datos y gobernanza.

- [ ] Historial y estadísticas por usuario (`stats`).
- [ ] Matchmaking automático (cola) — `FR-EXT-01` (aprovecha `GameRegistry`).
- [ ] **Web de administración (solo admin, no móvil):** gestión de juegos
      (agregar/modificar rulesets), monitoreo del backend, estadísticas de juego
      y "quiénes están jugando", gestión de usuarios, avisos, auditoría.
- [ ] Notificaciones push (turnos, invitaciones).
- [ ] Modo oscuro completo, accesibilidad, EN (i18n).
- [ ] Pruebas de carga WebSocket y tuning de rendimiento.

**Criterio de salida:** cola de matchmaking funcional + estadísticas + push +
web de administración operativa.

---

## Fase 4 — Expansión

**Objetivo:** escalar el catálogo y la configurabilidad.

- [ ] Segundo juego con la misma baraja (validación de extensibilidad — guía en `docs/adding-a-game.md`).
- [ ] Pares de colores configurables por el usuario (`FR-EXT-03`).
- [ ] Replay de partidas desde el event log (`FR-EXT-05`).
- [ ] Espectadores, chat avanzado (COULD).

**Criterio de salida:** agregar un juego nuevo sin tocar `domain/core`, y
configurar colores sin deploy.

---

## Gestión de prioridades

| Fase | Prioridad | Depende de |
|---|---|---|
| Fase 0 | Bloqueante | — |
| Fase 1 | Alta | Fase 0 (ruleset) |
| Fase 2 | Alta (MVP) | Fase 1 |
| Fase 3 | Media | Fase 2 |
| Fase 4 | Baja | Fase 2 |

> Todo cambio de alcance/fechas se refleja aquí y en `SPECS.md §12`.
