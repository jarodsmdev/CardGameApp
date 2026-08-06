# CARD_GAME — Juego de naipes online multijugador

Plataforma de juegos de naipes multijugador online para Android.

- **App Android** con **Jetpack Compose** (Kotlin).
- **Backend** en **Java** (Spring Boot).
- **Login obligatorio con Google OAuth 2.0** (no hay juego sin sesión).
- **Juego base:** *Carioca* (reglas oficiales, 9 rondas, **de 2 a 4 jugadores**)
  con baraja inglesa configurable.
- **Plataforma multi-juego:** el motor es genérico; Carioca es solo una de las
  estrategias que puede ejecutar.

> ⚠️ **Estado actual: fase de diseño y documentación.** No hay código todavía.
> Este repositorio contiene la especificación y el diseño previo al desarrollo.

---

## 📚 Documentación

| Documento | Descripción |
|---|---|
| [`ERS.md`](ERS.md) | **Especificación de Requisitos de Software:** producto, usuarios, historias, panel admin, avisos y eventos. |
| [`SPECS.md`](SPECS.md) | **Documento principal de requisitos técnicos.** Requisitos funcionales y no funcionales, restricciones, criterios de aceptación y estándares obligatorios. |
| [`docs/architecture.md`](docs/architecture.md) | Arquitectura de alto nivel: capas del cliente Android y del backend, componentes, diagramas y secuencias. |
| [`docs/game-engine.md`](docs/game-engine.md) | Diseño del motor de juego genérico multi-juego (domain model, estados, eventos, reglas configurables). |
| [`docs/adding-a-game.md`](docs/adding-a-game.md) | **Guía paso a paso para agregar un nuevo tipo de juego.** |
| [`docs/games/`](docs/games/README.md) | **Índice de juegos.** Cada juego vive en `docs/games/<juego>/` (reglas oficiales). |
| [`docs/games/carioca/rules.md`](docs/games/carioca/rules.md) | Reglas oficiales de **Carioca** (9 rondas, 2–4 jugadores). |
| [`docs/admin-web.md`](docs/admin-web.md) | **Web de administración (Angular 22):** gestión de juegos, monitoreo, estadísticas, login Google OAuth2 + JWT. |
| [`docs/api.md`](docs/api.md) | Contrato de comunicación: API REST + WebSocket/STOMP. |
| [`docs/database.md`](docs/database.md) | Modelo de datos y persistencia. |
| [`docs/design-patterns.md`](docs/design-patterns.md) | Patrones de diseño aplicados y mapeo a principios SOLID. |
| [`docs/best-practices.md`](docs/best-practices.md) | Buenas prácticas de código Android y Java, pruebas, CI/CD. |
| [`docs/security.md`](docs/security.md) | Seguridad: autenticación, códigos, anti-trampas, OWASP. |
| [`docs/roadmap.md`](docs/roadmap.md) | Plan de fases y entregables del proyecto. |
| [`docs/adr.md`](docs/adr.md) | Decisiones de arquitectura (ADRs) con estado y motivación. |
| [`docs/gitflow.md`](docs/gitflow.md) | **Guía de flujo de trabajo Git (Git Flow):** ramas, releases, hotfixes, CI por paths del monorepo. |

## 🃏 Concepto

- **Acceso:** login **obligatorio** con **Google OAuth 2.0** (Google Sign-In en
  Android; el backend valida el ID token y emite sus propios JWTs de sesión).
- Baraja inglesa: **2–9, 10, J, Q, K, A** en **4 palos** (corazones, picas, diamantes,
  tréboles), jugada con **2 juegos de cartas distinguidos por color** (rojo/negro)
  y **4 jokers** → **108 cartas**, siempre con el mismo mazo sin importar si son
  2, 3 o 4 jugadores.
- **Carioca** es el juego inicial (reglas oficiales en
  `docs/games/carioca/rules.md`), pero el **motor es multi-juego**: agregar otro
  juego (Ronda, Truco, etc.) no requiere tocar el núcleo (guía:
  `docs/adding-a-game.md`).
- La configuración de baraja (número de juegos, pares de colores, jokers) es **configurable**,
  de modo que a futuro el usuario pueda elegir su par de colores (rojo/negro, rojo/azul, etc.).

## 🧱 Stack propuesto

### Android (cliente)
- Kotlin, Jetpack Compose, Material 3
- **Google Sign-In (play-services-auth)**
- Clean Architecture + MVVM, Hilt (DI), Coroutines/Flow
- Retrofit + OkHttp (REST), OkHttp/Ktor WebSocket + STOMP (tiempo real)
- Room (persistencia local), DataStore (preferencias), Navigation Compose

### Backend (servidor)
- Java 21 (LTS), Spring Boot 3.x
- **Spring Security OAuth2 (Google)** + JWT propio de sesión
- Spring Web, Spring WebSocket (STOMP), Spring Data JPA
- PostgreSQL (persistencia), Redis (sesiones/caché/rate-limit)
- Flyway (migraciones), Docker

> Ver [`docs/architecture.md`](docs/architecture.md) para el detalle y justificación.

## 🗂 Estructura del repositorio (monorepo)

> Decisión `ADR-014` (`docs/adr.md`). Monorepo con frontends agrupados; los
> proyectos se crean manualmente. CI con path filters (`docs/gitflow.md §7`).

```
CARD_GAME/
├── ERS.md                     # Requisitos de software (producto)
├── SPECS.md                   # Requisitos técnicos (principal)
├── README.md
├── docs/                      # Documentación de diseño
│   ├── architecture.md
│   ├── game-engine.md
│   ├── adding-a-game.md       # Guía para agregar juegos nuevos
│   ├── admin-web.md           # Web de administración (Angular 22)
│   ├── gitflow.md             # Guía Git Flow del monorepo
│   ├── games/                 # Reglas por juego
│   │   ├── README.md          # Índice de juegos
│   │   └── carioca/rules.md   # Reglas oficiales de Carioca
│   ├── api.md
│   ├── database.md
│   ├── design-patterns.md
│   ├── best-practices.md
│   ├── security.md
│   ├── roadmap.md
│   └── adr.md
├── frontend/
│   ├── movil/                 # (futuro) App Android Kotlin + Jetpack Compose
│   └── web/                   # (futuro) Web admin Angular 22
├── backend/                   # (futuro) Backend Java 21 + Spring Boot
├── terraform/                 # (futuro) Infraestructura AWS: environments/ + modules/ (ec2, s3, vpc, sg…)
├── .github/                   # (futuro) CI/CD — GitHub Actions
├── docker/                    # (futuro) docker-compose + Dockerfiles
└── scripts/                   # (futuro) utilidades de desarrollo
```

## 🚀 Hoja de ruta resumida

1. **Fase 0 — Diseño:** completar SPECS, ADRs y validar preguntas abiertas. *(actual)*
2. **Fase 1 — MVP local:** motor de juego genérico + Carioca vs. bots, sin red.
3. **Fase 2 — Online:** login con Google, amigos, salas, tiempo real (WebSocket), partidas multijugador.
4. **Fase 3 — Plataforma:** estadísticas, historial, matchmaking automático.
5. **Fase 4 — Expansión:** más juegos con la misma baraja, pares de colores configurables.

Detalle y criterios de salida en [`docs/roadmap.md`](docs/roadmap.md).

## 🤝 Contribuciones

Antes de escribir código, seguir las convenciones de:
- [`docs/design-patterns.md`](docs/design-patterns.md)
- [`docs/best-practices.md`](docs/best-practices.md)
- Criterios de aceptación en [`SPECS.md`](SPECS.md)
