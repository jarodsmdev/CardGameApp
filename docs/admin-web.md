# Web de Administración — Angular 22

> **Consola de administración (solo admin, no móvil)** de CARD_GAME. Es una
> **SPA en Angular 22** que se comunica con el backend por **REST + JWT** y usa
> **Google OAuth 2.0** para el login del administrador.
>
> NO es parte del móvil ni de la app del jugador. Se implementa en **Fase 3**
> (ver `docs/roadmap.md` y `ERS.md §9`).

---

## 1. Objetivos

1. **Gestión de juegos:** agregar/modificar/habilitar-deshabilitar juegos y
   rulesets **sin tocar código** (versionado y auditado).
2. **Quién está jugando:** jugadores online/en partida, partidas activas en
   tiempo real.
3. **Monitoreo del backend:** salud, latencia, errores, recursos y alertas.
4. **Estadísticas:** partidas, jugadores, puntajes y abandonos por juego/periodo.
5. **Gestión de usuarios, avisos y auditoría.**

---

## 2. Stack técnico

| Capa | Tecnología | Justificación |
|---|---|---|
| **Framework** | **Angular 22** (TypeScript, standalone components, signals, zoneless/signal-based) | SPA madura, tipada, escalable para consolas administrativas. |
| **UI** | Angular Material / PrimeNG (tablas, dashboards, gráficos) | Componentes de administración listos. |
| **Gráficos** | Chart.js / ECharts (widget Angular) | KPIs y estadísticas. |
| **HTTP** | Angular `HttpClient` + **HTTP interceptor** (JWT) | Token automático, refresh y errores. |
| **Auth** | **Google OAuth 2.0** (login) + **JWT propio** (sesión backend) | Misma identidad que la plataforma. |
| **Build/CI** | Angular CLI, Docker, CI/CD | Despliegue estático + contenedor. |

---

## 3. Autenticación y autorización

### 3.1 Modelo

- **Login con Google OAuth 2.0** (Authorization Code **+ PKCE**, el flujo
  recomendado para SPA — el frontend no guarda secretos).
- Tras validar con Google, el backend emite sus **propios JWTs** de sesión
  (`accessToken` + `refreshToken`), igual que el resto de la plataforma.
- Solo cuentas con **rol `ADMIN`** pueden operar la consola. Un usuario normal
  autenticado recibe `403`.

### 3.2 Flujo de login

```
Browser (Angular 22)                          Backend (Spring Security)
──────────────────────                        ──────────────────────────
1. GET /admin/login → redirect a Google
   (Authorization Code + PKCE)
2. Usuario elige cuenta en Google
3. Google → redirect a /admin/callback?code=...
   ─────────────────────────────────────►
4. POST /admin/auth/google {code, verifier}
                                              5. Backend canjea el code por
                                                 ID token de Google (client secret)
                                              6. Valida ID token (JWKS, iss, aud)
                                              7. Verifica rol ADMIN en la cuenta local
                                               8. Emite {accessToken (JWT, 15 min),
                                                          refreshToken (rotativo, 7 días)}
   ◄─────────────────────────────────────
9. Guarda tokens (memoria/HttpOnly cookie opcional)
   y redirige al dashboard
```

> El **canje del `code` por el ID token** se hace en el **backend** (nunca en el
> navegador): ahí vive el `client_secret` de Google.

### 3.3 Endpoints de auth (admin)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/admin/auth/login` | Inicia el flujo OAuth2 (redirect a Google con PKCE). |
| GET | `/admin/auth/callback` | Recibe `code` y redirige a la SPA con la sesión. |
| POST | `/admin/auth/google` | `{code, codeVerifier}` → valida Google + rol `ADMIN`, devuelve tokens. |
| POST | `/admin/auth/refresh` | `{refreshToken}` → renueva tokens. |
| POST | `/admin/auth/logout` | Revoca sesión admin. |

### 3.4 JWT en la SPA

- El `accessToken` viaja en **`Authorization: Bearer <jwt>`**.
- Un **HTTP interceptor** (`adminHttpInterceptor`) agrega el header en cada
  request, maneja **401** (intenta refresh; si falla → login) y evita duplicar
  refreshes en peticiones concurrentes.
- El `refreshToken` **no va en la URL ni en headers persistentes**; se envía solo
  al endpoint de refresh. Almacenamiento: en memoria + storage cifrado seguro
  (o HttpOnly cookie gestionada por el backend, decisión ADR).
- **Blacklist:** los `jti` de tokens revocados (logout, suspensión, incidente) se
  invalidan vía blacklist en Redis (ver `docs/security.md §2.1.1`).
- **Guards (Route Guards):** `AuthGuard` (¿sesión válida?) y `AdminGuard`
  (¿rol ADMIN?) protegen todas las rutas de la consola.

---

## 4. Comunicación con el backend

- **REST** (`/admin/api/v1/*`) para el CRUD administrativo (juegos, users,
  avisos, stats). Todas las rutas bajo `/admin/**` requieren **JWT + rol ADMIN**.
- **WebSocket/STOMP** (opcional) para el **monitoreo en vivo** (quiénes están
  jugando, partidas activas). Alternativa simple: **polling** periódico con
  `HttpClient` sobre `/admin/api/v1/metrics`.

### 4.1 Áreas de API (admin)

| Área | Endpoints (alto nivel) |
|---|---|
| **Juegos / Rulesets** | `GET/POST /admin/api/v1/games` · `POST /admin/api/v1/games/{id}/rulesets` · `POST /admin/api/v1/games/{id}/activate` / `deactivate` |
| **Monitoreo** | `GET /admin/api/v1/health` · `GET /admin/api/v1/metrics` · `GET /admin/api/v1/errors` |
| **Quién juega** | `GET /admin/api/v1/online` · `GET /admin/api/v1/games/active` · `GET /admin/api/v1/games/{id}/detail` |
| **Estadísticas** | `GET /admin/api/v1/stats/{gameType}?from=&to=` |
| **Usuarios** | `GET/PATCH /admin/api/v1/users` · `POST /admin/api/v1/users/{id}/suspend` / `reactivate` |
| **Avisos** | `GET/POST/PATCH /admin/api/v1/notices` |
| **Auditoría** | `GET /admin/api/v1/audit` |

---

## 5. Arquitectura de la SPA (Angular 22)

```
frontend/web/
├── src/
│   ├── app/
│   │   ├── core/                    # Singleton: HttpClient, interceptores, guards
│   │   │   ├── auth/                # AuthService (Google OAuth + JWT), guards
│   │   │   ├── interceptors/        # adminHttpInterceptor (JWT + 401/refresh)
│   │   │   └── http/                # ApiService base (endpoints admin)
│   │   ├── shared/                  # Componentes reutilizables, pipes, UI
│   │   ├── features/
│   │   │   ├── dashboard/           # KPIs, alertas, monitoreo en vivo
│   │   │   ├── games/               # Gestión de juegos y rulesets (editor)
│   │   │   ├── users/               # Gestión de usuarios
│   │   │   ├── stats/               # Estadísticas y gráficos
│   │   │   ├── notices/             # Avisos
│   │   │   └── audit/               # Auditoría
│   │   └── login/                   # Pantalla de login (Google)
│   ├── environments/                # Config por entorno (API URL, client_id)
│   └── styles/                      # Tema (Material)
```

### 5.1 Flujo de datos (ej. dashboard)

```
DashboardComponent → StatsService → ApiService (HttpClient + interceptor JWT)
     → GET /admin/api/v1/metrics → señales (signals) → template (gráficos/tablas)
     → (opcional) STOMP suscripción a partidas activas
```

### 5.2 Reglas Angular

- **Standalone components** + **signals** (sin NgModules salvo `bootstrap`).
- **Lazy loading** por feature (routes `loadChildren`).
- **Typed reactive forms** en formularios (editor de rulesets, usuarios).
- **OnPush** / signal-based change detection por defecto.
- **HTTP interceptor** único para auth (no lógica de negocio en el interceptor).
- **Error handling** centralizado (interceptor → servicio de toasts).

---

## 6. Seguridad específica de la SPA

| Riesgo | Mitigación |
|---|---|
| **Robo de accessToken (XSS)** | Output escaping automático de Angular; CSP estricta; sin `innerHTML` con datos; el JWT de sesión se guarda protegido (HttpOnly cookie si se decide). |
| **CSRF** | El backend usa el patrón **JWT en header `Authorization`** (no cookies), lo que reduce CSRF; en endpoints por cookie se usa token anti-CSRF. |
| **Rol escalado** | `AdminGuard` + **verificación en el backend** por endpoint (nunca confiar solo en el frontend). |
| **Tokens en logs** | Nunca loguear JWTs ni códigos. |
| **Open redirect** | Validar `redirect_uri` y el origen de la llamada en `/admin/auth/callback`. |
| **Rate limiting** | Backend limita `/admin/auth/*` y acciones administrativas. |

---

## 7. Criterios de aceptación (web admin)

- [ ] Login con Google OAuth 2.0 (PKCE) → solo cuentas con rol `ADMIN` acceden.
- [ ] JWT en headers con refresh automático ante `401`.
- [ ] Gestión de juegos: agregar/modificar/habilitar-deshabilitar rulesets
      **sin deploy** y con validación del motor antes de publicar.
- [ ] Ver **quiénes están jugando** (online/en partida, partidas activas).
- [ ] Monitoreo del backend: salud, latencia, errores y alertas.
- [ ] Estadísticas por juego/periodo (partidas, jugadores, puntajes, abandonos).
- [ ] Toda acción administrativa queda en **auditoría**.
- [ ] Build de producción con Angular CLI; despliegue en Docker detrás de TLS.

---

## 8. Referencias

- `ERS.md §9` — Panel de administración (requisitos de producto).
- `docs/architecture.md` — Arquitectura general y módulos del backend.
- `docs/api.md` — Contrato REST/WebSocket (extender con el subconjunto `/admin`).
- `docs/security.md` — Autenticación OAuth2/JWT y OWASP.
- `docs/roadmap.md` — Fase 3 (implementación).
