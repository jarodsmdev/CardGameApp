# ERS — Especificación de Requisitos de Software

> **Documento de producto y requisitos de CARD_GAME.** Define *para quién* y *por
> qué* existe el sistema, sus **objetivos**, **alcance**, **usuarios y roles**,
> **historias de usuario**, y las áreas transversales (panel de administración,
> gestión de usuarios, avisos, eventos, seguridad, métricas). Complementa a
> `SPECS.md`, que es la **fuente de verdad técnica** (requisitos funcionales y no
> funcionales detallados). Este ERS añade la visión de negocio y de usuario.

- **Versión:** 1.0.0
- **Estado:** En revisión (Fase 0 — diseño)
- **Última actualización:** 2026-08-05
- **Documentos relacionados:** [`SPECS.md`](SPECS.md) · [`README.md`](README.md) · `docs/` (arquitectura, API, motor, seguridad, base de datos, ADRs, roadmap)

---

## 1. Resumen ejecutivo

**CARD_GAME** es una **plataforma multijugador online de juegos de naipes para
Android**. Los usuarios se autentican de forma **obligatoria con Google OAuth
2.0** (no existe acceso anónimo), crean/usan un **código de amigo**, forman
**salas privadas o públicas** y juegan **partidas en tiempo real** contra otros
jugadores.

El primer (y de momento único) juego es **Carioca**, pero la plataforma está
diseñada como **multi-juego**: el motor de juego es genérico y las reglas de cada
juego son configuración, por lo que agregar juegos (Ronda, Truco, loba…) no
requiere tocar el núcleo.

**Entregable MVP:** una app Android donde 2 a 4 jugadores autenticados con Google
completan una partida online de Carioca (9 rondas), con amigos, salas y
reconexión ante fallos de red. Arquitectura extensible para nuevos juegos,
matchmaking y estadísticas en fases posteriores.

---

## 2. Contexto y problema

### 2.1 Contexto

- Los juegos de naipes (Carioca, Ronda, Truco, loba) se juegan tradicionalmente
  **en persona**. Jugar con amigos **a distancia** requiere coordinación manual
  (llamadas, repartir a mano, contar puntos) y es propenso a errores y trampas.
- Existen plataformas de cartas, pero suelen ser **cerradas a un solo juego**,
  **anónimas** (sin identidad), con **interfaces poco accesibles** o
  **monetización agresiva**.
- Los usuarios quieren jugar **con personas conocidas** en **salas privadas**,
  con reglas tradicionales respetadas y puntuación automática.

### 2.2 Problema a resolver

1. **Coordinación:** jugar Carioca a distancia sin una plataforma obliga a
   acuerdos manuales y a confiar en el "repartidor" humano.
2. **Reglas:** las reglas varían por región; la plataforma debe **parametrizarlas**
   y ejecutarlas de forma **autoritativa** (sin trampas).
3. **Identidad y confianza:** sin cuentas verificadas es difícil mantener amigos,
   historial y buen comportamiento. Se resuelve con **login obligatorio**.
4. **Crecimiento:** el negocio necesita **varios juegos** sobre la misma base sin
   reescribir el sistema.

### 2.3 Propuesta de valor

| Para | Beneficio |
|---|---|
| **Jugador casual** | Jugar Carioca con amigos desde el celular, sin baraja física ni árbitro. |
| **Grupo de amigos** | Salas privadas por código, partidas en tiempo real, puntuación automática. |
| **Operación / producto** | Plataforma multi-juego: un juego hoy, muchos mañana, sin rediseños. |

---

## 3. Objetivos

### 3.1 Objetivo general

Poner en el mercado una plataforma Android de naipes multijugador, **segura,
extensible y multijuego**, cuyo primer caso de éxito es **Carioca online 2–4
jugadores con amigos**.

### 3.2 Objetivos específicos (SMART)

1. **MVP (Fase 1–2):** permitir a 2–4 usuarios autenticados completar una partida
   de Carioca online en tiempo real, con las 9 rondas y Escala Real.
2. **Identidad:** el 100 % de los jugadores accede con Google OAuth 2.0 (cero uso
   anónimo).
3. **Social:** cada usuario puede añadir amigos por código y crear/entrar salas.
4. **Calidad:** latencia de acciones < 500 ms (p95), reconexión sin perder
   partida, cobertura de pruebas del motor ≥ 80 %.
5. **Escalabilidad de producto:** incorporar un juego nuevo sin modificar el
   núcleo (validado con un segundo juego piloto en fase 4).
6. **Gobernanza:** operación desde una **web de administración solo-admin**
   (gestión de juegos, usuarios, monitoreo del backend, estadísticas y quién
   está jugando) en fase 3+.
7. **Datos del jugador:** el jugador ve su **puntaje y estadísticas de juego**
   (partidas jugadas, ganadas, posición, histórico de puntajes).

### 3.3 Indicadores de éxito (KPIs)

| Métrica | Objetivo |
|---|---|
| Partidas completadas / semana | ≥ 1.000 en el trimestre posterior al MVP |
| Retención D7 (jugadores que vuelven a los 7 días) | ≥ 25 % |
| Partidas abandonadas por error técnico | < 5 % |
| Latencia p95 de acciones de juego | < 500 ms |
| Tiempo de agregar un juego nuevo | ≤ 1 iteración de 2 semanas |

---

## 4. Alcance

### 4.1 Dentro de alcance (MVP)

- Login **obligatorio** con Google OAuth 2.0 y perfil (alias, avatar).
- Códigos de amigo, agregar/eliminar/listar amigos.
- Salas **públicas** y **privadas** (código), asientos, expulsión, inicio.
- **Motor de juego genérico** + **Carioca** (2–4 jugadores, 9 rondas, 108 cartas
  fijas en 2 juegos de color).
- Partidas en **tiempo real** (WebSocket/STOMP), **server-authoritative**.
- Persistencia de partidas, historial y estadísticas básicas.
- Reconexión y resincronización de estado.
- Abandono de **ronda** (`SURRENDER` o 2 timeouts): los demás confirman y la
  partida **continúa a la siguiente ronda sin el que abandona**; si no confirman,
  termina y libera recursos.
- App Android (Jetpack Compose) con backend Java/Spring Boot.

### 4.2 Diferido / fuera de alcance

- Matchmaking automático (diseñado como extensión).
- Juegos adicionales (soportados por el motor, no por producto aún).
- Pares de colores personalizados por el usuario.
- Pagos / tienda / monetización.
- Chat por voz, torneos, rankings globales, espectadores.
- **Web de administración** (solo admin, no móvil) **completa**: gestión de
  juegos, monitoreo del backend, estadísticas y "quiénes están jugando" (se
  diseña en §9; se implementa en fase 3+).
- Versión iOS / web (de jugador).

> Detalle técnico completo de alcance: `SPECS.md §2`.

---

## 5. Usuarios y roles

| Rol | Descripción | Permisos principales | Estado |
|---|---|---|---|
| **Invitado (no autenticado)** | Sin sesión. No puede jugar. | Ver solo la pantalla de login con Google. | MVP |
| **Jugador** | Usuario autenticado con Google. | Perfil, amigos, salas, partidas, historial y estadísticas. | MVP |
| **Administrador** | Personal de operación/producto. | **Web de administración (solo admin, no móvil):** gestión de juegos (agregar/modificar), gestión de usuarios, avisos, monitoreo del backend y estadísticas de partidas y jugadores. | Diseñado (fase 3+) |

**Regla:** el acceso anónimo no existe. Todo flujo (amigos, salas, partidas,
historial) requiere sesión autenticada (`FR-AUT-01`).

---

## 6. Historias de usuario (HU)

Formato: `HU-<nº> — <título>` con actor, criterios de aceptación y prioridad
(MoSCoW). Se enlazan con los requisitos funcionales de `SPECS.md`.

### 6.1 Autenticación y cuenta

| ID | Historia | Criterios de aceptación | Ref. SPECS |
|---|---|---|---|
| **HU-01** | **Como jugador, quiero entrar con mi cuenta de Google** para jugar sin crear credenciales nuevas. | Al abrir la app sin sesión solo veo login de Google; al autenticarme entro a mi perfil; sin sesión no accedo a amigos/salas/partidas. | FR-AUT-01..05 |
| **HU-02** | **Como jugador, quiero ver y editar mi perfil** (alias, avatar, idioma). | Puedo cambiar alias/avatar/idioma; se guarda y persiste; no altera mi cuenta de Google. | FR-AUT-07/09 |
| **HU-02b** | **Como jugador, quiero cambiar la cuenta de Google con la que entro** sin perder mi perfil, amigos y partidas. | Estando autenticado vinculo otro Google (ID token nuevo); conservo perfil/amigos/partidas; si el nuevo Google ya pertenece a otra cuenta, se me avisa y no se aplica. | FR-AUT-08, ADR-016 |
| **HU-03** | **Como jugador, quiero cerrar sesión** en un dispositivo o en todos. | Al cerrar sesión dejo de recibir actualizaciones; puedo revocar desde otro dispositivo. | FR-AUT-06 |

### 6.2 Amigos

| ID | Historia | Criterios de aceptación | Ref. SPECS |
|---|---|---|---|
| **HU-04** | **Como jugador, quiero un código de amigo único** para compartirlo con quien quiera agregarme. | Veo mi código; puedo regenerarlo (invalida el anterior). | FR-AMI-01/02 |
| **HU-05** | **Como jugador, quiero enviar solicitud de amistad por código** y ver el estado de mis amigos. | Envío la solicitud con un código válido; veo mi lista de amigos (aceptados) con estado en vivo. | FR-AMI-03/04 |
| **HU-05b** | **Como jugador, quiero ver y aceptar/rechazar solicitudes de amistad** que me lleguen. | Veo mis solicitudes pendientes; al aceptar, la amistad queda activa para ambos; al rechazar, desaparece. | FR-AMI-07 |
| **HU-06** | **Como jugador, quiero eliminar una amistad** para que ya no me "vean" ni yo los vea. | Al eliminar, **ambos** dejamos de vernos el estado (online/en partida) y desaparecemos de las listas respectivas; no queda vínculo ni solicitud pendiente entre nosotros. | FR-AMI-05 |
| **HU-07** | **Como jugador, quiero recibir un aviso cuando un amigo me agrega o se conecta.** | (Opcional) Recibo notificación; puedo ignorarla. | FR-AMI-06 |

### 6.3 Salas

| ID | Historia | Criterios de aceptación | Ref. SPECS |
|---|---|---|---|
| **HU-08** | **Como jugador, quiero crear una sala** (pública o privada) eligiendo juego, visibilidad y nº de jugadores. | Creo la sala; el nº de jugadores se valida contra el máximo del juego (Carioca: máx. 4). | FR-SAL-01 |
| **HU-09** | **Como jugador, quiero unirme a una sala privada con su código** o desde la lista de públicas. | Entro con el código correcto; veo la sala pública en la lista. | FR-SAL-02/03 |
| **HU-10** | **Como jugador dueño de sala, quiero iniciar la partida** cuando hay jugadores suficientes. | El botón habilita con jugadores mínimos; la partida inicia y la sala muestra estado. | FR-SAL-04/06 |
| **HU-11** | **Como jugador, quiero salir de una sala** y, como dueño, **expulsar** a alguien. | Salir/expulsar actualiza la sala; el expulsado sale de la lista. | FR-SAL-05 |
| **HU-12** | **Como jugador, quiero que las salas vacías o inactivas se cierren solas.** | La sala caduca y libera recursos sin aviso manual. | FR-SAL-07/09 |

### 6.4 Juego (Carioca)

| ID | Historia | Criterios de aceptación | Ref. SPECS |
|---|---|---|---|
| **HU-13** | **Como jugador, quiero jugar una partida de Carioca** de 2 a 4 jugadores con las reglas oficiales. | Reparto de 12 cartas; turnos; robar/descartar; bajarse cumpliendo la ronda; puntuación automática. | FR-CAR-01..12 |
| **HU-14** | **Como jugador, quiero saber de quién es el turno y qué puedo hacer.** | La UI resalta turno actual y acciones disponibles; eventos en vivo. | NFR-UX-04, FR-CAR-02/03 |
| **HU-15** | **Como jugador, quiero abandonar la partida** si no quiero seguir. | Confirmo el abandono; aplica a la **ronda actual** (`PLAYER_ABANDONED_ROUND`); los demás confirman si continúan a la siguiente ronda; conservo mis puntos acumulados; se liberan los recursos. | FR-CAR-13 |
| **HU-16** | **Como jugador, quiero reconectarme si pierdo internet** y seguir donde iba. | Al reconectar se resincroniza el estado; la partida continúa. | NFR-AVAIL-02/03 |
| **HU-17** | **Como jugador, quiero ver el resultado final** de la partida y mi historial. | Al terminar veo puntajes y ganador; queda en mi historial/estadísticas. | FR-CAR-07, UC-11/12 |
| **HU-17b** | **Como jugador, quiero ver mi puntaje y estadísticas de juego** (partidas jugadas, ganadas, posición promedio, histórico de puntajes). | Veo mis stats agregadas y el detalle de mis partidas; se actualizan al terminar cada partida. | ERS §7.1 (STA) |

### 6.5 Administración (diseñado, fase 3+)

| ID | Historia | Criterios de aceptación | Ref. |
|---|---|---|---|
| **HU-18** | **Como administrador, quiero ver y gestionar usuarios** (buscar, suspender, reactivar). | Busco por alias/email/sub; puedo suspender/re activar; queda registro de auditoría. | ERS §10 |
| **HU-19** | **Como administrador, quiero publicar avisos** (anuncios, mantenimiento, políticas). | Creo/edito/oculto avisos; se muestran en la app según vigencia. | ERS §11 |
| **HU-20** | **Como administrador, quiero monitorear partidas y eventos** para detectar abusos o fallos. | Veo partidas activas, eventos, alertas de comportamiento y uso. | ERS §12 |
| **HU-21** | **Como administrador, quiero agregar o modificar juegos** desde la web (sin tocar la app ni el código). | Creo un juego con su ruleset/baraja; lo activo/desactivo; queda versionado y auditado. | ERS §9 |
| **HU-22** | **Como administrador, quiero ver el estado del backend** (salud, latencia, errores, recursos). | El dashboard muestra métricas en vivo y alertas; no requiere acceso al servidor. | ERS §9 |
| **HU-23** | **Como administrador, quiero ver estadísticas de juegos y quién está jugando** (concurrentes por juego, partidas activas, históricos). | Veo gráficos por juego/periodo y la lista de jugadores en línea y en partida. | ERS §9 |

---

## 7. Requerimientos

### 7.1 Requerimientos funcionales (resumen)

El detalle completo con IDs y prioridades está en **`SPECS.md §6`**. Resumen por
área:

| Área | Alcance |
|---|---|
| **Autenticación (AUT)** | Login obligatorio Google OAuth 2.0, JWT propio, perfil, cierre de sesión. |
| **Amigos (AMI)** | Código único, agregar/listar/eliminar, estado en línea. |
| **Salas (SAL)** | Crear/entrar/salir, pública/privada, expulsión, caducidad, cierre. |
| **Motor de juego (JGO)** | Genérico, multi-juego, server-authoritative, event sourcing, barajado con semilla. |
| **Carioca (CAR)** | 9 rondas, 108 cartas fijas, 2–4 jugadores, jokers, scoring, abandono. |
| **Estadísticas y puntaje (STA)** | Puntaje por partida y acumulado; historial y estadísticas del jugador (partidas, ganadas, posición). |
| **Administración (ADM)** | Web solo-admin (no móvil): gestión de juegos, monitoreo del backend, estadísticas de juego y quién está jugando, usuarios, avisos, auditoría. |
| **Extensión (EXT)** | Matchmaking, nuevos juegos, colores configurables, espectadores, replay. |

### 7.2 Requerimientos no funcionales (resumen)

Detalle en **`SPECS.md §7`**:

- **Rendimiento:** acciones < 500 ms p95; 1.000 usuarios concurrentes; REST < 300 ms.
- **Seguridad:** TLS/WSS; Google OAuth + JWT; server-authoritative; OWASP; sin secretos en cliente.
- **Disponibilidad:** 99,5 %; reconexión; persistencia y recuperación; liberación de recursos.
- **Usabilidad:** retrato/horizontal, claro/oscuro, localizable (ES/EN), accesibilidad básica.
- **Compatibilidad:** Android min SDK 26; backend en Docker.
- **Mantenibilidad:** Clean Architecture, SOLID, inglés en código, cobertura ≥ 80 % motor.
- **Legales:** privacidad, términos, consentimiento, baja de cuenta.

---

## 8. Arquitectura técnica (alto nivel)

> Detalle completo en `docs/architecture.md` y `docs/game-engine.md`.

### 8.1 Vista general

```
┌─────────────────────────────── Android (Jetpack Compose) ───────────────────────────────┐
│  Login Google Sign-In · UI por juego · ViewModel/UiState · repos/realtime (STOMP)      │
└──────────────┬──────────────────────────────────────────────────────────────────────────┘
               │ REST (auth/perfil/amigos/salas) + WebSocket (partida) · JWT · TLS/WSS
┌──────────────▼──────────────────────────────────────────────────────────────────────────┐
│                                    Backend Java / Spring Boot 3.x                       │
│  ┌────────────┐ ┌──────────────┐ ┌────────────────────┐ ┌───────────────────────────┐   │
│  │ Security   │ │ Salas/Rooms  │ │ Motor de juego     │ │ Módulo de administración  │   │
│  │ OAuth+JWT  │ │ + Amigos     │ │ (Game/Strategy)    │ │ (admin API + métricas)    │   │
│  └────────────┘ └──────────────┘ └────────────────────┘ └───────────┬───────────────┘   │
│  PostgreSQL (estado, eventos, users) · Redis (sesiones, caché, rate-limit)              │
└──────────────────────────────────────────────────────────────────────────────────────────┘
               │ Admin API (REST, rol ADMIN, auditada)
┌──────────────▼──────────────────────────────┐
│  Web de administración (solo admin, no móvil) │
│  Gestión de juegos · usuarios · monitoreo     │
│  del backend · estadísticas y quién juega     │
└──────────────────────────────────────────────┘
```

### 8.2 Decisiones clave

| Tema | Decisión |
|---|---|
| **Autenticación** | Solo Google OAuth 2.0; backend valida ID token y emite JWT propio. |
| **Autoridad** | Servidor autoritativo; el cliente envía intenciones y recibe eventos. |
| **Motor** | Genérico: baraja, turnos, acciones, eventos, scoring como estrategias. Reglas = datos (Ruleset). |
| **Estado de partida** | Event sourcing + snapshots (auditoría, replay, resync). |
| **Comunicación** | REST para CRUD; WebSocket/STOMP para tiempo real por sala/partida. |
| **Persistencia** | PostgreSQL + Redis; Flyway para migraciones. |
| **Infra** | Docker, despliegue en VPS/cloud; CI/CD en el pipeline. |

### 8.3 Estructura de código prevista

> Monorepo (`ADR-014`). Detalle de ramas y releases en `docs/gitflow.md`.

```
frontend/movil/  app (Compose), core/domain, data (REST/WS), juego carioca (UI)
frontend/web/    (fase 3+) Web de administración (Angular 22, solo-admin) — ver `docs/admin-web.md`
backend/         security, users, friends, rooms, game-core, game-carioca, admin-api, metrics, ws
```

---

## 9. Panel de administración

> **Es una aplicación WEB de acceso exclusivo para administradores; NO es parte
> del móvil.** El jugador nunca ve ni accede a esta consola.
>
> **Diseñado en este ERS; implementación en fase 3+** (roadmap fase 3). No es
> parte del MVP.

### 9.1 Objetivo

Dar al equipo de operación/producto **visibilidad y control total** sobre la
plataforma **sin tocar la app móvil ni el código**:

1. **Gestionar juegos** (agregar/modificar/habilitar-deshabilitar) sin releases.
2. **Ver quién está jugando** (concurrentes, partidas activas) en tiempo real.
3. **Monitorear el backend** (salud, latencia, errores, recursos).
4. **Estadísticas** de juegos y de los jugadores que están jugando.

### 9.2 Principios de diseño

| Principio | Regla |
|---|---|
| **Solo administrador** | La web valida rol `ADMIN`; un jugador normal jamás la ve en la app. |
| **Separada del móvil** | No hay pantallas de configuración/admin en la app Android. |
| **Sin tocar código** | Agregar/modificar juegos = editar ruleset/baraja desde la web (queda versionado y auditado). |
| **Solo lectura para operación** | Interrumpir partidas/suspender usuarios son acciones puntuales auditadas. |
| **Web accesible** | Se abre en navegador; responsive de escritorio (no es una app móvil). |

### 9.3 Funcionalidades por módulo

| Módulo | Funcionalidades | Prioridad |
|---|---|---|
| **Gestión de juegos** | Listar juegos; **agregar un juego nuevo** (ruleset + baraja + reglas); **modificar** un ruleset existente (crea versión nueva); **habilitar/deshabilitar** juegos; vista previa de reglas. Todo versionado y con **validación** contra el motor antes de publicarse. | MUST |
| **Quién está jugando** | Jugadores **online y en partida** en tiempo real; partidas **activas** (juego, sala, jugadores, ronda); detalle de una partida (eventos, estado). | MUST |
| **Monitoreo del backend** | Salud del servicio (health checks), **latencia** de acciones/REST, **tasa de errores** y timeouts, uso de **recursos** (CPU/memoria/conexiones), alertas (umbrales), logs de errores recientes. | MUST |
| **Estadísticas** | Por juego y por periodo: partidas iniciadas/completadas, duración media, nº de jugadores únicos, concurrentes, abandonos vs. partidas terminadas, distribución de puntajes/ganadores. | MUST |
| **Gestión de usuarios** | Búsqueda (alias/email/sub), detalle, suspensión/re activación, baja. | MUST |
| **Avisos** | Crear/editar/publicar/ocultar avisos dirigidos a todos o a segmentos. | SHOULD |
| **Auditoría** | Registro de acciones del administrador (quién, qué, cuándo). | MUST |
| **Moderación** | Marcar/penalizar usuarios por reportes de mala conducta (diseñado). | COULD |

### 9.4 Roles de administración

| Rol | Permisos |
|---|---|
| **Operador** | Dashboard, monitoreo, estadísticas, quién está jugando, gestión de usuarios, avisos, interrumpir partidas. |
| **Desarrollador de juegos** | Todo lo del operador + **agregar/modificar juegos y rulesets** (validados antes de publicar). |
| **Superadministrador** | Todo lo anterior + gestión de administradores y configuración global. |

### 9.5 Reglas de negocio del panel

- El acceso es **solo para cuentas administrador** (mismo Google + rol `ADMIN`); no existe acceso desde la app móvil.
- Toda acción administrativa **queda auditada**.
- **Modificar un juego nunca rompe partidas en curso**: el cambio se aplica a
  partidas nuevas (la partida activa sigue con la versión de su ruleset).
- **Publicar un juego** requiere que el ruleset pase la **validación del motor**
  (esquema, límites, rondas) antes de activarse.
- Suspender a un usuario: no puede iniciar sesión de juego ni unirse a salas; las
  partidas en curso donde participa se manejan con política (terminar partida y
  liberar recursos).

---

## 10. Gestión de usuarios

### 10.1 Ciclo de vida de la cuenta

```
No autenticado → (Google login) → Activa → Suspensa → Reactivada → Dada de baja
```

| Estado | Descripción | Transiciones |
|---|---|---|
| **No autenticado** | Sin sesión; solo ve login. | → Activa (Google login). |
| **Activa** | Puede jugar, tener amigos, crear salas. | → Suspensa (admin/penalización); → Dada de baja (auto-baja). |
| **Suspensa** | Bloqueada temporalmente (admin). No juega ni entra a salas. | → Reactivada (admin); → Dada de baja. |
| **Dada de baja** | Datos borrados (RGPD/consentimiento). | Terminal. |

### 10.2 Reglas de gestión

- La cuenta se **crea automáticamente** al primer login de Google (auto-registro con `sub`).
- El **alias inicial** se deriva del nombre de Google; el usuario puede editarlo.
- **Revinculación (`ADR-016`):** un usuario autenticado puede **cambiar su cuenta
  de Google** y conservar perfil/amigos/partidas; se rechaza si el nuevo Google
  ya está vinculado a otra cuenta; al aplicarse se revocan las sesiones previas.
- **Baja de cuenta:** el usuario puede solicitar el borrado de sus datos (NFR-LEGAL-02); se revocan tokens y sesiones.
- **Penalizaciones:** el abandono repetido o reportes pueden suspender la cuenta (decisión de producto, fase 3+).
- **Privacidad:** el email de Google nunca se muestra públicamente; solo el alias.

---

## 11. Avisos y notificaciones

> Avisos = comunicaciones dirigidas (sistema); Notificaciones push = alertas en el
> dispositivo. Detalle de eventos de partida en §12 y `docs/api.md`.

### 11.1 Avisos del sistema (in-app)

| Tipo | Ejemplo | Destino | Prioridad |
|---|---|---|---|
| **Aviso global** | Mantenimiento programado, nueva versión, cambios de reglas. | Todos los usuarios. | MUST (fase 3+) |
| **Aviso segmentado** | Mensaje a usuarios nuevos, a inactivos, a una región. | Segmento definido por admin. | SHOULD |
| **Aviso técnico** | Notificación de actualización obligatoria. | Versiones afectadas. | COULD |

Reglas: tienen **fecha de inicio/fin de vigencia**, se muestran una vez por
usuario (o hasta descartarse), y se marcan como leídas.

### 11.2 Notificaciones push (app)

| Evento | Notificación | Prioridad |
|---|---|---|
| Es mi turno | "Es tu turno en la partida con…" | MUST (fase 3+) |
| Invitación a sala | "X te invitó a una sala" | SHOULD |
| Amigo online | "X está en línea" | COULD |
| Resultado de partida | "Ganaste la partida" | SHOULD |

Reglas: el usuario **elige qué notificaciones recibir**; la app pide permiso
según pautas de Android; las notificaciones **no contienen datos sensibles**
(resultados parciales, cartas).

### 11.3 Flujo técnico (diseño)

- Backend publica eventos → **push provider** (FCM) para el dispositivo, y/o
  mensaje in-app si la app está abierta.
- Avisos administrativos viajan por **REST** (`/avisos`) y se cachean; los de
  partida viajan por **WebSocket**.

---

## 12. Eventos

### 12.1 Modelo de eventos

El sistema es **event-driven y server-authoritative**:

- **Intención (acción):** lo que el cliente envía (robar, descartar, bajarse,
  abandonar). El servidor la valida contra el estado real.
- **Evento (hecho consumado):** lo que el servidor emite tras validar (carta
  repartida, cambio de turno, ronda ganada, partida terminada).

Cada evento tiene un **número de secuencia (`seq`)** por partida, para orden,
resync y detección de huecos/duplicados. Las partidas se persisten como
**eventos + snapshots** (`event sourcing`) → auditoría y replay.

### 12.2 Catálogo de eventos (alto nivel)

| Categoría | Eventos |
|---|---|
| **Partida** | `GAME_STARTED`, `GAME_END` (fin de rondas / `FORFEIT` / `TIMEOUT` / admin), `ROUND_STARTED`, `ROUND_ENDED`. |
| **Turno / mesa** | `TURN_CHANGED`, `CARD_DEALT`, `CARD_DRAWN`, `CARD_DISCARDED`, `MELDED` (bajarse), `LAY_OFF` (añadir a juegos ajenos). |
| **Jugador** | `PLAYER_JOINED`, `PLAYER_LEFT`, `PLAYER_ABANDONED_ROUND` (abandono de ronda, por `SURRENDER` o 2 timeouts), `PLAYER_TIMEOUT`. |
| **Sala** | `ROOM_CREATED`, `ROOM_CLOSED`, `ROOM_UPDATED`. |
| **Sistema** | `NOTICE_PUBLISHED` (aviso), eventos de auditoría del panel admin. |

### 12.3 Consumo de eventos

- **Cliente:** proyecta los eventos a su `GameViewState` (UI).
- **Backend:** persiste eventos, calcula snapshots, publica por WebSocket.
- **Admin (fase 3+):** consume eventos para monitoreo, alertas y auditoría.
- **Análisis (fase 4):** métricas de uso a partir del event log.

Detalle de payloads y tópicos en `docs/api.md §5` y `docs/game-engine.md §5`.

---

## 13. Reglas de negocio clave

| Regla | Descripción | Ref. |
|---|---|---|
| **Login obligatorio** | Sin sesión no hay juego; solo login de Google. | FR-AUT-01 |
| **Carioca 2–4 jugadores** | Máximo 4; el sistema no permite agregar más. | FR-CAR-14 |
| **Baraja fija** | Siempre 2 juegos = 108 cartas, sin importar nº de jugadores. | FR-CAR-01 |
| **Rondas configurables por sala** | Catálogo (9 base + opcionales); al crear la sala se eligen cuántas y cuáles; por defecto, juego completo. | FR-CAR-05, ADR-011 |
| **Servidor autoritativo** | El cliente nunca decide resultados; anti-trampa. | NFR-SEC-04 |
| **Abandono de ronda con continuación** | `SURRENDER` o **2 timeouts** = abandono de la ronda actual; los demás confirman si continúa a la siguiente ronda sin el que abandona; si no, termina (`GAME_END`, `FORFEIT`). | FR-CAR-13/15 |
| **Salas caducan** | Salas vacías/inactivas se cierran automáticamente. | FR-SAL-07/09 |
| **Penalización por abandono** | Configurable por ruleset (`forfeitPenalty`, decisión Q9). | FR-CAR-13, SPECS §13 |
| **Admin solo por web** | El panel de configuración/monitoreo es una **web solo-administrador**; no existe en el móvil. | ERS §9 |

---

## 14. Seguridad y privacidad (nivel usuario)

- **Autenticación única** con Google OAuth 2.0; sin contraseñas propias.
- **Datos mínimos:** se guardan `sub`, alias, avatar, email (para soporte); nada más de Google se almacena. Al **revincular** (`ADR-016`) el `sub` anterior solo queda en el historial interno (`account_links`).
- **Visibilidad:** el email no se expone a otros jugadores; los códigos (amigo/sala) no revelan datos personales.
- **Baja de cuenta:** borrado de datos personales a petición (NFR-LEGAL-02).
- **Menores:** solo mayores de edad o con permiso parental (NFR-LEGAL-03).
- **Consentimiento:** política de privacidad y términos visibles en la app.

Detalle técnico completo: `docs/security.md`.

---

## 15. Riesgos y mitigación

| # | Riesgo | Probabilidad | Impacto | Mitigación |
|---|---|---|---|---|
| R1 | Reglas de Carioca ambigüas entre jugadores | Media | Media | Ruleset configurable; preguntas Q1–Q9 resueltas como ADRs. |
| R2 | Latencia de red arruina la experiencia | Media | Alta | WebSocket dedicado, payloads mínimos, objetivo < 500 ms. |
| R3 | Trampas / acciones inválidas | Media | Alta | Servidor autoritativo + validación de cada acción + eventos con `seq`. |
| R4 | Fallo de red a mitad de partida | Alta | Media | Reconexión + resync; persistencia con snapshots. |
| R5 | Partidas "zombie" por abandono/timeout | Alta | Media | Timeouts de turno y caducidad de salas; liberación garantizada al terminar (normal, abandono no confirmado, timeout, admin). |
| R6 | Crecimiento sin diseño multi-juego | Media | Alta | Motor genérico; guía de extensión (`docs/adding-a-game.md`). |
| R7 | Dependencia de Google OAuth | Baja | Media | Fallback a documentación/estado de servicio; tokens propios de sesión. |
| R8 | Alcance del MVP sobredimensionado | Media | Media | Fases claras (roadmap); lo no crítico queda en EXT/COULD. |

---

## 16. Fases y entregables

| Fase | Entregable | Métrica de salida |
|---|---|---|
| **Fase 0 — Diseño** *(actual)* | SPECS + ERS + docs + ADRs resueltos. | Documentación aprobada. |
| **Fase 1 — MVP local** | Motor genérico + Carioca vs. bots, sin red. | Partida completa de Carioca vs. 3 bots en el dispositivo. |
| **Fase 2 — Online** | Google login, amigos, salas, tiempo real, multijugador. | Partida de Carioca 2–4 online con las 9 rondas. |
| **Fase 3 — Plataforma** | Estadísticas, historial, matchmaking, **web de administración** (gestión de juegos, monitoreo, quién juega), avisos, push. | Cola de matchmaking + web admin + métricas funcionando. |
| **Fase 4 — Expansión** | Más juegos, colores configurables, replay. | Segundo juego jugable sin tocar el núcleo. |

Detalle en `docs/roadmap.md`.

---

## 17. Criterios de aceptación del MVP (release 1.0)

- [ ] Login con Google obligatorio y acceso denegado sin sesión (`FR-AUT-*`).
- [ ] Amigos por código: enviar solicitud, aceptar/rechazar, listar, eliminar (`FR-AMI-*`).
- [ ] Salas públicas/privadas, inicio y cierre de partida (`FR-SAL-*`).
- [ ] Partida de Carioca 2–4 online, 9 rondas + Escala Real, tiempo real (`FR-CAR-*`).
- [ ] Reconexión y resincronización ante caída de red.
- [ ] Abandono de ronda: con confirmación de los demás, la partida continúa a la
      siguiente ronda sin el que abandona; sin confirmación termina y libera
      recursos (sin fugas/zombies).
- [ ] Latencia de acciones < 500 ms p95; cobertura del motor ≥ 80 %.

---

## 18. Glosario

| Término | Definición |
|---|---|
| **Carioca** | Juego de naipes tipo rummy; primer juego de la plataforma. |
| **Ruleset** | Reglas de un juego como datos (rondas, valores, jugadores, baraja). |
| **Server-authoritative** | El servidor valida y decide; el cliente solo propone. |
| **Event sourcing** | El estado se reconstruye desde eventos + snapshots. |
| **Aviso** | Comunicación del sistema dirigida a usuarios (in-app). |
| **Push / notificación** | Alerta que llega al dispositivo aunque la app esté cerrada. |
| **Código de amigo / sala** | Identificador corto para agregar amigos o entrar a salas. |
| **Abandono de ronda (SURRENDER)** | Decisión de no seguir jugando; aplica a la ronda actual; los demás confirman si la partida continúa sin el que abandona. |

---

## 19. Aprobación

| Rol | Nombre | Fecha | Firma |
|---|---|---|---|
| Product Owner | — | — | — |
| Arquitectura | — | — | — |
| Desarrollo | — | — | — |
| QA | — | — | — |
