# Seguridad

> Estrategia de seguridad del proyecto. Requisitos de referencia: `SPECS.md §7.2`,
> `NFR-SEC-*`. Complementa a `docs/api.md` y `docs/game-engine.md §10`.

---

## 1. Modelo de amenazas (resumen)

| Activo | Amenaza principal |
|---|---|
| Cuentas de usuario (Google) | Robo de sesión, reuso de ID tokens, fuerza bruta en `/auth/google` |
| Códigos de amigo/sala | Ingeniería social, abuso, bots |
| Partidas (manos, cartas) | **Trampas:** ver cartas ajenas, decidir resultados, barajado manipulado, desconexión intencional |
| Servidor | DDoS, inyección, abuso de API |
| Datos personales | Fugas, retención indebida |

## 2. Autenticación y sesiones (Google OAuth 2.0 + JWT propio)

**El login es obligatorio para jugar: no hay modo anónimo ni registro con
email/contraseña.** La identidad la gestiona Google.

### 2.1 Flujo

1. **Cliente (Android):** Google Sign-In → el usuario elige su cuenta → la app
   recibe un **ID token** de Google.
2. **Backend:** `POST /auth/google {idToken}` valida el token:
   - **Firma** contra las claves públicas de Google (**JWKS**,
     `https://www.googleapis.com/oauth2/v3/certs`).
   - `iss == https://accounts.google.com` y `aud == server_client_id`.
   - Vigencia (`exp`, `iat`).
   - Se consulta la **revocación** (`tokeninfo`) en casos sensibles (COULD).
3. **Cuenta local:** se busca por `sub` (subject) de Google; si no existe, se
   **auto-registra** (alias/avatar desde el perfil de Google). `sub` es el
   identificador estable.
4. **Sesión propia:** el backend emite **sus** JWTs:
   - `accessToken` (15 min, firmado con clave del servidor).
   - `refreshToken` rotativo (**7 días**), almacenado como **hash** en BD
     (tabla `refresh_tokens`).
5. **Revocación:** logout por dispositivo y "logout de todos" (invalida refresh).
6. **Revincular cuenta** (`POST /auth/relink`, `ADR-016`): el usuario **ya
   autenticado** envía un **nuevo ID token** de Google; el backend:
   - valida el token (firma, `iss`, `aud`) contra el **`server_client_id`**;
   - comprueba que el **nuevo `sub` no esté vinculado a otra cuenta** (si lo
     está → `409 GOOGLE_ALREADY_LINKED`);
   - actualiza `users.google_sub`, registra el cambio en `account_links` y
     **revoca todas las sesiones previas** del usuario.
7. **Baja de cuenta:** `DELETE /auth/account` borra/anónimiza los datos locales.

### 2.1.1 Blacklist de tokens (revocación por estado)

Además de la rotación de `refreshToken`, se mantiene una **blacklist** de tokens
revocados para invalidar de forma inmediata sesiones comprometidas:

| Dato | Detalle |
|---|---|
| **Qué se lista** | `jti` (id) de `accessToken` y `refreshToken` revocados antes de su expiración natural. |
| **Cuándo se agrega** | Logout (dispositivo/todos), cambio/revocación de cuenta, suspensión de usuario, incidente de seguridad, rotación de claves. |
| **Dónde vive** | **Redis** con TTL = tiempo restante de vida del token (`accessToken` ≤ 15 min; `refreshToken` ≤ 7 días). Así la blacklist se auto-limpia. |
| **Verificación** | En cada request protegido, antes de autorizar, se consulta si `jti` está en blacklist. En caso de logout global se incrementa un **contador de versión de sesión** por usuario (más barato que listar todos los tokens). |
| **Persistencia** | Redis (volátil y rápido). Si se requiere sobrevivencia a reinicios, se respalda a PostgreSQL y se reconstruye el TTL. |

Reglas de la blacklist:

- **No se agregan tokens ya expirados** (innecesario).
- La blacklist es **complementaria** a la rotación: un `refreshToken` rotado
  deja de ser válido al usarse (no necesita estar en blacklist).
- El **logout global** usa el contador de versión de sesión (invalida todos los
  tokens emitidos antes de ese momento), no una lista masiva.
- Los `jti` se generan con **UUID v4 / aleatoriedad segura** (no predecibles).
- En la **web admin** aplica la misma blacklist (mismos mecanismos de JWT).

### 2.2 Reglas de seguridad

- El cliente **nunca** envía credenciales de Google: solo el ID token, que es de
  un solo uso y de corta vida.
- El `server_client_id` es un **recurso de servidor** (no va en el APK); el
  `client_id` de Android se usa solo en el SDK de Google.
- **Rate limiting** en `/auth/google`, `/auth/refresh` y en regeneración de códigos.
- Los **ID tokens y JWTs no se loguean** (nunca en logs).
- Rotación de claves de firma propias (HS/RS) y de los clientes de Google.
- `refreshToken` **solo** viaja sobre TLS y se rota en cada uso (replay-safe).

### 2.3 Web de administración (Angular 22) — OAuth2 Authorization Code + PKCE

La **consola admin no es el móvil**: es una **SPA Angular** que autentica al
administrador con Google mediante **Authorization Code + PKCE** (flujo seguro
para SPAs, sin secretos en el navegador).

1. La SPA redirige a Google con `code_challenge` (PKCE) y `redirect_uri` de la
   consola.
2. Google devuelve un `code` en `/admin/auth/callback`.
3. El **backend** canjea `code + code_verifier` por el **ID token** de Google
   (aquí vive el `client_secret`; nunca en el frontend).
4. El backend valida el ID token (JWKS, `iss`, `aud`) y **verifica rol `ADMIN`**
   en la cuenta local.
5. Emite sus propios JWTs (`accessToken` 15 min + `refreshToken` rotativo de
   **7 días**) para las llamadas REST de la consola (`Authorization: Bearer`).
6. `AuthGuard` + `AdminGuard` protegen las rutas; el backend re-valida el rol en
   **cada endpoint `/admin/**`**.

Reglas específicas:
- El `client_id` de Google para la web es público; el **`client_secret` es solo
  del backend**.
- `redirect_uri` debe validarse exactamente (anti open-redirect).
- La SPA guarda tokens de forma protegida (HttpOnly cookie gestionada por el
  backend, o memoria + almacenamiento cifrado según ADR).
- `POST /admin/auth/*` tiene **rate limiting**.
- Nunca loguear `code`, ID tokens ni JWTs de admin.

Detalle de arquitectura de la consola: `docs/admin-web.md`.

## 3. Códigos (amigo y sala)

- No son secretos ni revelan datos: 8 y 6 caracteres alfanuméricos sin caracteres
  ambiguos (`SPECS §8.2`), normalizados a mayúsculas.
- Regeneración invalida el código anterior (`FR-AMI-02`).
- Los códigos no se exponen en logs ni respuestas ajenas.

## 4. Anti-trampas (anti-cheat)

- **Servidor autoritativo:** todas las reglas se validan en el motor
  (`docs/game-engine.md §3`). El cliente solo envía `GameAction`.
- **Visibilidad parcial:** cada cliente recibe solo lo que le corresponde
  (`viewFor`); el servidor jamás envía cartas de otros jugadores.
- **Barajado:** semilla generada por CSPRNG en el servidor, oculta hasta el final;
  sequencia verificable por replay (`game-engine.md §7`).
- **Secuencia y idempotencia:** eventos con `seq`; acciones duplicadas o fuera de
  turno → `ACTION_REJECTED` con motivo.
- **Detección de abuso:** acciones ilegales repetidas → advertencias → expulsión
  (política configurable); patrón de desconexión sospechoso registrado.
- **No confiar en el cliente** para: quien gana, tiempos de turno, cartas, seed.

## 5. Seguridad de red y transporte

- **TLS/WSS** en todo el tráfico (HTTP/2 + WSS).
- HSTS, cabeceras de seguridad estándar (CSP, X-Frame-Options, etc.).
- Terminación TLS en el reverse proxy (Nginx/Traefik).

## 6. Validación de entrada y OWASP

- **OWASP Top 10** como base; revisiones periódicas.
- Validación de entrada en **todas las capas**:
  - API: bean validation, longitud/tamaño máximos de payload.
  - Motor: validación semántica de acciones (siempre contra estado).
  - BD: queries con **parámetros** (JPA/Hibernate); sin concatenación SQL.
- Prevenir **XSS/Inyección** en chat y textos (escape/sanitización).
- **IDOR:** cada endpoint verifica propiedad (dueño de sala, miembro de partida).
- Dependencias: **SCA** (Dependabot/OWASP Dependency-Check) en CI.

## 7. Privacidad y datos

- Datos mínimos: solo los necesarios para la cuenta y el juego (del perfil de
  Google se conservan `sub`, email y avatar/alias inicial).
- **Revinculación (`ADR-016`):** al cambiar el Google vinculado, el `sub`
  anterior queda registrado solo en el historial interno `account_links`
  (trazabilidad/auditoría); no se muestra al usuario.
- Política de privacidad y términos en la app (`NFR-LEGAL-01`).
- **Baja de cuenta** borra/anónimiza los datos locales (`NFR-LEGAL-02`).
- Tokens, **ID tokens** y datos personales **nunca** en logs.

## 8. Infraestructura

- Secrets por variables de entorno/gestor (nunca en repo).
- **Principio de menor privilegio** para la BD (usuario app sin DDL).
- Backups cifrados de BD; retención definida.
- Firewall/red del nodo; contenedores sin privilegios.

## 9. Plan de respuesta a incidentes (básico)

1. Detectar (métricas/alertas). 2. Contener (revocar tokens, bloquear). 3. Erradicar
4. Recuperar (rollback). 5. Lecciones → ADR / actualizar `security.md`.

## 10. Checklist de seguridad por release

- [ ] ¿Endpoints con autenticación/autorización verificadas? (solo `/auth/google` público)
- [ ] ¿ID token de Google validado (firma, `iss`, `aud`, vigencia)?
- [ ] ¿Rate limiting en auth y códigos?
- [ ] ¿No hay secretos en el repo ni en el APK (sin `server_client_id` embebido)?
- [ ] ¿El motor valida todas las acciones en el servidor?
- [ ] ¿Sin cartas ajenas en respuestas/snapshots?
- [ ] ¿TLS/WSS activo y verificado?
- [ ] ¿SCA sin vulnerabilidades críticas?
