# Buenas prácticas de código

> Convenciones de calidad para backend Java y cliente Android/Kotlin.
> Obligatorias según `SPECS.md §8` y `NFR-MAINT-*`.

---

## 1. Generales

- **Idioma:** identificadores y comentarios de código en **inglés**; documentación
  externa en español (`NFR-MAINT-01`).
- **No comentar el obvio.** Los comentarios explican *por qué*, no *qué*.
- **Inmutabilidad** por defecto: `record` (Java) / `data class` + `val` (Kotlin).
- **Nombres expresivos:** clases = sustantivos, métodos = verbos, booleanos =
  predicados (`isLegal`, `hasDiscarded`).
- **Sin secretos en el código**; todo secreto vía entorno/gestor (`NFR-SEC-09`).
- **Sin dependencias circulares** entre módulos/paquetes.

## 2. Backend — Java / Spring Boot

- **Java 21 (LTS).** Usar `records`, `sealed interfaces` para jerarquías cerradas
  del dominio (tipos de acciones/eventos), `switch` expressions, `Streams`.
- **Null-safety:** usar `Optional`/`Objects.requireNonNull`; nunca devolver `null`
  como valor de dominio válido.
- **Spring:**
  - Controllers **finos** (delegar a services); sin lógica de negocio.
  - `@Transactional` solo donde hay persistencia; evitar transacciones largas.
  - Preferir inyección por **constructor** (no campo), favorece tests.
  - `@Valid` + bean validation en DTOs de entrada.
- **Validación en el dominio:** el motor valida reglas; la API valida forma.
- **Excepciones:** usar códigos de error de dominio (`ACTION_REJECTED`, `ROOM_NOT_FOUND`)
  en vez de excepciones para flujo esperado (ver `docs/design-patterns.md §5`).
- **Logging:** SLF4J, nivel según contexto; **nunca** loguear credenciales,
  tokens (incluidos **ID tokens** de Google), manos de cartas ni datos personales.
- **Spring Security / OAuth2:**
  - `POST /auth/google` es el único endpoint público; el resto exige sesión.
  - Validar el **ID token** con `NimbusJwtDecoder` (JWKS de Google): firma,
    `iss`, `aud`, `exp`. Rechazar tokens de otro `aud`.
  - El `server_client_id` se inyecta por **variable de entorno** (no en código
    ni en el repositorio).
  - Emitir **JWT propio** con claims mínimos (`sub`, `uid`, `exp`); firmar con
    clave RS256 y rotarla.
  - `refreshToken` rotativo (**7 días**), guardado como hash, revocable.
  - **Revinculación de Google** (`ADR-016`): solo con sesión activa; validar que
    el nuevo `sub` no esté en uso (409) y **revocar todas las sesiones** previas.
  - **Blacklist de `jti`** en Redis (TTL = vida restante) + contador de versión
    de sesión para logout global. Ver `docs/security.md §2.1.1`.
- **Threading:** nunca bloquear el event-loop; usar executors por partida
  (`docs/architecture.md §3.2`).
- **Ciclo de vida y recursos:** toda partida que termina (normal, abandono,
  timeout, admin) debe **cancelar temporizadores, cerrar su executor, persistir
  el estado final y salir del `GameRegistry`** — sin fugas de memoria ni
  partidas "zombie" (`docs/game-engine.md §11`).

## 3. Android — Kotlin / Jetpack Compose

- **Clean Architecture + MVVM** (`docs/architecture.md §2`). ViewModels sin
  referencias a `Activity/Context` (usar `ViewModelProvider`/Hilt).
- **Estado inmutable:** un solo `StateFlow<UiState>` por pantalla; `sealed
  interface` para estados (Loading/Success/Error). Ver UDF (`architecture.md §2.3`).
- **Compose:**
  - Composables **stateless** cuando sea posible; el estado se eleva
    (state hoisting).
  - Sin lógica de negocio ni de navegación dentro de los composables.
  - `remember`/`LaunchedEffect` usados con intención; evitar recomposiciones
    innecesarias (derivar estado con `derivedStateOf` cuando aplique).
  - **Strings en recursos** (`values-es`, `values-en`) — `NFR-UX-02`.
- **Concurrencia:** `Coroutines + Flow`; `Dispatchers.IO` para red/BD;
  `Dispatchers.Main.immediate` para UI. Sin `GlobalScope`.
- **Login (Google Sign-In):** usar el SDK de Google (`play-services-auth`);
  obtener el **ID token** y enviarlo al backend; **nunca** incluir credenciales
  de Google en el app. Almacenar los **JWT propios** en almacenamiento seguro
  (Android Keystore / EncryptedSharedPreferences), no en SharedPreferences planas.
  Renovar `accessToken` con el refresh; si falla → volver al login.
- **WebSocket:** auto-reconnect con backoff + resync por `seq`
  (`docs/api.md §5`).
- **Persistencia local:** Room (ofrece `Flow`) para cache; DataStore para
  preferencias/tokens.
- **Accesibilidad:** `contentDescription` en cartas/iconos, contraste
  (`NFR-UX-03`).

## 4. Pruebas

| Nivel | Alcance | Herramienta (propuesta) |
|---|---|---|
| Unit | Motor de juego (TDD), validadores, scoring, use cases | JUnit 5 (Java) / JUnit + MockK (Kotlin) |
| Integración | Repos, REST, WebSocket flow, persistencia | Testcontainers (PostgreSQL/Redis) |
| Contract | DTOs/eventos compartidos cliente↔servidor | Contrato definido en `docs/api.md`; tests de schema |
| E2E | Login Google → amigos → sala → partida completa | Espresso/Compose UI + backend de staging |
| Carga | Concurrencia WebSocket, latencia | k6 / JMeter |

- **TDD obligatorio en el motor** (`NFR-QA-01`). Cobertura ≥ 80 % en motor.
- Los **rulesets de test** se versionan como fixtures.
- Shuffle: tests estadísticos de distribución + determinismo por semilla.

## 5. Git y flujo de trabajo

- **Git Flow** (`docs/adr.md` **ADR-015**): `main` (prod, tags `vX.Y.Z`) +
  `develop` (integración) + `feature/` · `release/` · `hotfix/`. Guía operativa:
  **`docs/gitflow.md`** (incluye protección de ramas, releases y hotfixes).
- **Commits convencionales:** `feat:`, `fix:`, `docs:`, `refactor:`, `test:`,
  `chore:`, `ci:`, `perf:`.
- **Versionado semántico** de la API (`docs/api.md §7`) y del proyecto (tags).
- **DoD** antes de merge: ver `SPECS.md §11`.
- **Monorepo:** CI por paths (`frontend/movil/**`, `frontend/web/**`,
  `backend/**`, `docs/**`) — `docs/gitflow.md §7`.

## 6. CI/CD y calidad

- Pipeline (GitHub Actions propuesto):
  1. `lint` (Detekt/Ktlint Android; Checkstyle/SpotBugs Java).
  2. `test` (unit + integration con Testcontainers).
  3. `build` (assemble APK; jar de backend).
  4. `docker build` + push de imagen.
  5. `deploy` (staging → prod) manual/gated.
- Análisis estático: **SonarQube** (quality gate) — `NFR-QA-05`.
- **Secrets:** GitHub Actions Secrets / gestor de secretos; nunca en el repo.

## 7. Rendimiento y observabilidad (backend)

- Métricas Micrometer/Prometheus + logs estructurados (JSON) (COULD).
- Cache en **Redis** para lecturas calientes (perfiles, salas públicas).
- **Rate limiting** en auth/lobby/websocket (`NFR-SEC-05`).
- Payloads mínimos: el snapshot de juego no envía cartas ocultas.

## 8. Documentación viva

- Cambio de comportamiento → actualizar `SPECS.md` o crear **ADR** (`docs/adr.md`).
- Nuevo endpoint/evento → actualizar `docs/api.md` **en el mismo PR**.
- Reglas del juego → solo en `docs/games/carioca/rules.md` + Ruleset.
