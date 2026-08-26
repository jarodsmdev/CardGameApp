# Landing Page — Royal Meld

> **Documento de diseño** de la landing page de Royal Meld. Define la
> estructura, contenido visual (con descripción detallada de cada imagen
> requerida), copywriting y flujo de la página.
>
> **Stack:** Angular 22 + Angular Material / PrimeNG + Animate on Scroll.
> **Directorio:** `frontend/web/landing/` (SPA separada del admin en
> `frontend/web/admin/`).
> **Rama futura:** `feature/landing-page`.

---

## 1. Objetivo

Convertir visitantes en **descargas de la app Android**. La landing comunica:

1. **Qué es** Royal Meld en una frase.
2. **Por qué jugar** — ventajas sobre otras plataformas.
3. **Cómo se ve** — gameplay con imágenes reales del juego.
4. **Cómo empezar** — 3 pasos simples.

---

## 2. Identidad visual

| Elemento | Valor |
|---|---|
| **Nombre** | Royal Meld |
| **Tagline** | *"Reúne, desafía y reina."* |
| **Logo** | `assets/logo.png` (carta con "R" ornata, borde azul, acentos dorados) |
| **Paleta principal** | Azul royal `#1E88E5`, Dorado `#F9A825`, Blanco `#FFFFFF`, Gris oscuro `#1B1B1B` |
| **Paleta de acento** | Rojo `#C62828` (palos rojos), Verde `#2E7D32` (palos verdes) |
| **Tipografía** | Títulos: **Playfair Display** (serif elegante). Cuerpo: **Inter** (sans legible) |
| **Estilo general** | Elegancia card-game moderna. Fondos oscuros con degradados sutiles, highlights dorados. No infantil. |

---

## 3. Estructura de la página (secciones)

### 3.1 — Hero (pantalla completa)

**Objetivo:** impacto inmediato. El visitante debe entender qué es en < 3 segundos.

**Layout:**
```
┌──────────────────────────────────────────────┐
│  [Navbar: Logo · Cómo jugar · Características │
│          · Descargar]                         │
│                                               │
│          REÚNE, DESAFÍA Y REINA.             │
│   El juego de cartas multijugador favorito    │
│        de Latinoamérica, ahora online.        │
│                                               │
│     [ 📱 Descargar gratis ]  ▶ Ver gameplay   │
│                                               │
│         ┌─────────────────────┐               │
│         │   IMAGEN HERO_01   │               │
│         │  Mockup del juego  │               │
│         └─────────────────────┘               │
└──────────────────────────────────────────────┘
```

**IMAGEN — `HERO_01`: Mockup de pantalla de juego**
> **Descripción:** Un smartphone (iPhone o Pixel) en perspectiva isométrica
> ligeramente rotado hacia la derecha (~15°), flotando sobre un fondo oscuro
> con degradado radial de azul royal (#1E88E5) a negro. En la pantalla del
> teléfono se ve una partida de Carioca en curso: mano de 10-12 cartas
> organizadas en arco, mazo y pozo abajo, tablero con combinaciones
> visibles arriba. Las cartas tienen diseños de palos clásicos (corazones
> rojos, picas negras) con bordes brillantes. Un halo dorado sutil rodea el
> teléfono. Partículas de luz dorada flotan alrededor.
>
> **Mood:** premium, moderno, vibrante. Transmite que es un juego pulido
> y profesional, no amateur.
>
> **Dimensiones:** 1200×800px (desktop), responsive a 600×400px (mobile).
> **Fondo:** transparente o degradado que se mezcle con el hero background.

---

### 3.2 — ¿Qué es Royal Meld? (about)

**Objetivo:** explicar la propuesta de valor en 3 puntos.

**Layout:**
```
┌──────────────────────────────────────────────┐
│            ¿QUÉ ES ROYAL MELD?               │
│                                               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ 🃏        │  │ 🌐        │  │ 🏆        │   │
│  │ CARTAS   │  │ MULTI-    │  │ COMPITE  │   │
│  │ CLÁSICAS │  │ JUGADOR   │  │ EN VIVO  │   │
│  │          │  │           │  │          │   │
│  │ Juega    │  │ Enfrenta  │  │ Sube de  │   │
│  │ Carioca  │  │ a amigos  │  │ nivel con│   │
│  │ con las  │  │ o rivales │  │ cada     │   │
│  │ reglas   │  │ en salas  │  │ partida  │   │
│  │ oficiales│  │ privadas  │  │          │   │
│  └──────────┘  └──────────┘  └──────────┘   │
│                                               │
│   "La plataforma definitiva para jugar       │
│    Carioca con tus amigos, sin trampas       │
│    y con reglas que tú controlas."           │
└──────────────────────────────────────────────┘
```

**Texto del cuerpo:**
> Royal Meld es una plataforma multijugador online de juegos de naipes
> para Android. Juega Carioca — el juego de cartas más popular de
> Latinoamérica — con amigos o rivales en salas privadas, con reglas
> oficiales, puntuación automática y animaciones fluidas.

---

### 3.3 — Gameplay (galería visual)

**Objetivo:** mostrar cómo se ve y se siente el juego. Esta es la sección más
visual de toda la landing.

**Layout:**
```
┌──────────────────────────────────────────────┐
│              MIRA CÓMO SE JUEGA              │
│                                               │
│  ┌─────────────────────────────────────────┐ │
│  │         IMAGEN GAMEPLAY_01              │ │
│  │    Pantalla completa del tablero        │ │
│  └─────────────────────────────────────────┘ │
│                                               │
│  ┌────────────┐ ┌────────────┐ ┌───────────┐│
│  │GAMEPLAY_02 │ │GAMEPLAY_03 │ │GAMEPLAY_04││
│  │  Mano del  │ │  Pozo /    │ │  Bajarse  ││
│  │  jugador   │ │  Descarte  │ │  Mesa     ││
│  └────────────┘ └────────────┘ └───────────┘│
│                                               │
│  ── Descripción del gameplay ────────────    │
│  "Roba cartas, forma combinaciones y         │
│   descarta para terminar tu turno.           │
│   Desliza para jugar, toca para elegir."     │
└──────────────────────────────────────────────┘
```

**IMAGEN — `GAMEPLAY_01`: Tablero completo (vista principal)**
> **Descripción:** Captura de pantalla real del juego en un tablet o
> teléfono en horizontal. Se ve el tablero completo de una partida de
> Carioca en la ronda 5 (Escala Real): la mano del jugador con ~10 cartas
> en arco en la parte inferior, el mazo boca abajo y el pozo con cartas
> boca arriba en el centro, y en la parte superior las combinaciones
> bajadas por los oponentes (3 escaleras de color visibles). Los oponentes
> se muestran como avatares con contador de cartas restantes. La barra
> inferior muestra el puntaje y el turno actual. El fondo de la mesa es
> verde bosque oscuro con textura de fieltro sutil.
>
> **Detalles técnicos:** composición limpia, sin overlays de debug, sin
> texto de error. Las cartas deben mostrarse con skins personalizadas
> (back rojo y negro, front clásico). La interfaz debe verse pulida y
> completa.
>
> **Dimensiones:** 1920×1080px (landscape), responsive.
> **Fondo:** verde de mesa oscuro (#2E7D32 con textura de fieltro).

**IMAGEN — `GAMEPLAY_02`: Mano del jugador (close-up)**
> **Descripción:** Close-up de la mano inferior del jugador. Se ven 8-10
> cartas organizadas en arco suave, cada una con un ligero levantamiento
> (lift) que indica selección. Una carta (el As de Corazones) está
> elevada por encima de las demás con un borde dorado brillante que indica
> que está seleccionada. Las cartas muestran palos clásicos: corazones
> rojos (#C62828), picas negras (#1B1B1B), diamantes rojos, tréboles
> negros. El fondo es el fieltro verde oscuro borroso (depth of field).
> Una mano humana virtual (gesture hint) aparece semi-transparente
> haciendo un gesto de swipe hacia arriba sobre la carta seleccionada.
>
> **Detalles técnicos:** las cartas deben verse nítidas y con sombra
> suave debajo. El borde dorado de selección es `#C9A227`. El arcraise
> de las cartas vecinas es visible (3-4° de inclinación).
>
> **Dimensiones:** 800×600px (close-up landscape).

**IMAGEN — `GAMEPLAY_03`: Pozo y descarte (close-up)**
> **Descripción:** Close-up del centro de la mesa. A la izquierda el mazo
> boca abajo con un diseño de respaldo rojo clásico con patrón de rombos.
> A la derecha el pozo con 5-6 cartas boca arriba apiladas con offset,
> la carta más visible es un 10 de Picas. Un destello sutil (pulse
> animation congelada) indica que la última carta fue recién descartada.
> El fondo es el fieltro verde. Un dado dorado decorativo aparece en una
> esquina como elemento estético.
>
> **Dimensiones:** 800×600px.

**IMAGEN — `GAMEPLAY_04`: Momento de bajarse (acción clave)**
> **Descripción:** Captura del momento en que un jugador se "baja" con
> una combinación. Se ve el tablero con 3 combinaciones recién colocadas
> en la mesa del jugador: una escalera (7-8-9-10 de Diamantes), un trío
> (3 Ases de Distintos Palos) y un grupo (4 Jotas). Las combinaciones
> tienen un borde brillante dorado animado (congelado en su punto más
> intenso). En la parte inferior, la mano del jugador ahora tiene solo 2
> cartas restantes. Un indicador visual de "¡Bajado!" con estilo
> celebratorio aparece en la esquina. Confeti dorado sutil en el aire.
>
> **Dimensiones:** 800×600px.
>
> **Mood de todas las imágenes de gameplay:** screenshot real del juego
> en el dispositivo, no mockup 3D. Transmite autenticidad — "esto es lo
> que realmente verás en tu teléfono".

---

### 3.4 — Características (features)

**Objetivo:** listar las ventajas competitivas del juego.

**Layout:**
```
┌──────────────────────────────────────────────┐
│           ¿POR QUÉ ROYAL MELD?               │
│                                               │
│  ┌────────────────┐  ┌────────────────┐      │
│  │ 🔒              │  │ 🎯              │      │
│  │ SIN TRAMPAS     │  │ REGLAS          │      │
│  │                 │  │ CONFIGURABLES   │      │
│  │ El servidor     │  │                 │      │
│  │ valida cada     │  │ Cada sala define│      │
│  │ jugada. No hay  │  │ sus rondas,     │      │
│  │ forma de hacer  │  │ cantidad de     │      │
│  │ trampa.         │  │ rondas y        │      │
│  │                 │  │ variantes.      │      │
│  └────────────────┘  └────────────────┘      │
│                                               │
│  ┌────────────────┐  ┌────────────────┐      │
│  │ 👥              │  │ 🎨              │      │
│  │ AMIGOS Y        │  │ BARAJA          │      │
│  │ SALAS           │  │ PERSONALIZABLE  │      │
│  │ PRIVADAS        │  │                 │      │
│  │                 │  │ Elige el diseño │      │
│  │ Comparte un     │  │ de tus cartas:  │      │
│  │ código y juega  │  │ backs, fronts   │      │
│  │ solo con quien  │  │ y jokers.       │      │
│  │ tú quieras.     │  │                 │      │
│  └────────────────┘  └────────────────┘      │
│                                               │
│  ┌────────────────┐  ┌────────────────┐      │
│  │ ⚡              │  │ 📊              │      │
│  │ RECONEXIÓN      │  │ ESTADÍSTICAS    │      │
│  │ AUTOMÁTICA      │  │ Y HISTORIAL     │      │
│  │                 │  │                 │      │
│  │ Se cortó la     │  │ Revisa tus      │      │
│  │ conexión?       │  │ partidas,       │      │
│  │ Vuelve sin      │  │ puntajes y      │      │
│  │ perder tu       │  │ progreso.       │      │
│  │ turno.          │  │                 │      │
│  └────────────────┘  └────────────────┘      │
└──────────────────────────────────────────────┘
```

---

### 3.5 — Cómo se juega (gameplay instructions)

**Objetivo:** enseñar las reglas básicas de Carioca de forma visual y
rápida. No reemplaza el tutorial de la app, pero genera confianza.

**Layout:**
```
┌──────────────────────────────────────────────┐
│           ¿CÓMO SE JUEGA CARIOCA?            │
│                                               │
│  ┌──────┐    ┌──────┐    ┌──────┐           │
│  │ PASO │ ── │ PASO │ ── │ PASO │           │
│  │  1   │    │  2   │    │  3   │           │
│  └──────┘    └──────┘    └──────┘           │
│                                               │
│  ROBA      FORMA       DESCARTA             │
│  UNA       COMBINA-    Y REpite             │
│  CARTA     CIONES      HASTA                │
│            EN LA MESA  VACIAR               │
│                        TU MANO              │
│                                               │
│  [IMAGEN_JUGAR_01] [IMAGEN_JUGAR_02]        │
│  [IMAGEN_JUGAR_03] [IMAGEN_JUGAR_04]        │
│                                               │
│  "9 rondas. Cada una más desafiante.        │
│   ¿Listo para la Escala Real?"              │
│                                               │
│         [ Ver reglas completas → ]           │
└──────────────────────────────────────────────┘
```

**IMAGEN — `JUGAR_01`: Robar del mazo**
> **Descripción:** Ilustración estilo flat/minimal del mazo boca abajo con
> una flecha dorada curva apuntando desde el mazo hacia una mano de
> cartas. Fondo oscuro. Texto superpuesto "ROBA" en tipografía bold.
> Estilo ilustración vectorial, no screenshot.
>
> **Dimensiones:** 400×300px.

**IMAGEN — `JUGAR_02`: Formar combinaciones**
> **Descripción:** Ilustración flat de 3 cartas (7, 8, 9 de Diamantes)
> alineadas horizontalmente con una llave dorada abrazándolas, indicando
> que forman una escalera. Un signo ✓ verde aparece arriba.
> Texto superpuesto "COMBINA".
>
> **Dimensiones:** 400×300px.

**IMAGEN — `JUGAR_03`: Bajarse a la mesa**
> **Descripción:** Ilustración flat de una mano de cartas extendiéndose
> hacia una mesa verde, dejando caer 3 combinaciones (escalera, trío,
> grupo) que aterrizan con un pequeño efecto de brillo dorado.
> Texto superpuesto "BAJATE".
>
> **Dimensiones:** 400×300px.

**IMAGEN — `JUGAR_04`: Descartar y ganar**
> **Descripción:** Ilustración flat de una última carta siendo lanzada
> hacia el pozo con una estela dorada. Detrás, un trofeo o corona dorada
> aparece con un efecto de celebración (rayos de luz, confeti sutil).
> Texto superpuesto "GANA".
>
> **Dimensiones:** 400×300px.

---

### 3.6 — Galería de cartas (showcase de personalización)

**Objetivo:** mostrar la variedad de diseños de cartas disponibles.

**Layout:**
```
┌──────────────────────────────────────────────┐
│           PERSONALIZA TU BARAJA              │
│                                               │
│  [CARDS_01]  [CARDS_02]  [CARDS_03]          │
│   Azul        Rojo        Negro               │
│  Clásico      Rombos      Rayas               │
│                                               │
│  [CARDS_04]  [CARDS_05]  [CARDS_06]          │
│   Verde       Dorado      Monocromo           │
│  Anillos      Borde       Joker               │
│                                               │
│  "Elige entre 7 diseños de respaldo,         │
│   4 frentes y 3 estilos de Joker."          │
└──────────────────────────────────────────────┘
```

**IMAGEN — `CARDS_01` a `CARDS_06`: Showcases de diseños de cartas**
> **Descripción de cada imagen:** Cada imagen muestra 3 cartas boca abajo
> (para backs) o boca arriba (para fronts/jokers) en perspectiva
> isométrica con sombra proyectada. Cada grupo tiene un fondo circular
> semitransparente del color correspondiente:
>
> - `CARDS_01` — Back "Azul Clásico": 3 cartas con respaldo azul royal
>   (#1E88E5) con patrón de líneas diagonales doradas. Borde blanco.
> - `CARDS_02` — Back "Rojo Rombos": 3 cartas con respaldo rojo carmesí
>   (#C62828) con patrón de rombos en relieve. Borde dorado.
> - `CARDS_03` — Back "Negro Rayas": 3 cartas con respaldo negro
>   (#1B1B1B) con rayas horizontales gris oscuro. Borde plateado.
> - `CARDS_04` — Back "Verde Anillos": 3 cartas con respaldo verde
>   bosque (#2E7D32) con patrón de anillos concéntricos. Borde dorado.
> - `CARDS_05` — Front "Borde Dorado": 3 cartas boca arriba (As de
>   Corazones, Rey de Picas, 10 de Diamantes) con un borde dorado
>   grueso alrededor del marco interior.
> - `CARDS_06` — Joker "Dorado": 2 jokers — uno coloreado (payaso
>   clásico con colores vibrantes) y uno monocromo (escala de grises
>   elegante). Ambos con fondo púrpura oscuro (#6A1B9A).
>
> **Dimensiones:** 400×300px cada una.
> **Estilo:** renders 3D limpios sobre fondo transparente.

---

### 3.7 — Cómo empezar (CTA final)

**Objetivo:** convertir. Dar los pasos claros para descargar y jugar.

**Layout:**
```
┌──────────────────────────────────────────────┐
│          EMPEZAR A JUGAR ES FÁCIL            │
│                                               │
│     ┌─────┐      ┌─────┐      ┌─────┐       │
│     │  1  │ ───  │  2  │ ───  │  3  │       │
│     └─────┘      └─────┘      └─────┘       │
│     Descarga     Inicia     Crea una         │
│     la app       sesión     sala y           │
│     gratis       con Google  juega           │
│                                               │
│           [ 📱 Descargar gratis ]            │
│                                               │
│  ┌─────────────────────────────────────────┐ │
│  │         IMAGEN CTA_01                   │ │
│  │   Mockup de phones con diferentes       │ │
│  │   pantallas de la app                   │ │
│  └─────────────────────────────────────────┘ │
└──────────────────────────────────────────────┘
```

**IMAGEN — `CTO_01`: Mockup de phones con múltiples pantallas**
> **Descripción:** 3 smartphones flotando en perspectiva isométrica,
> cada uno mostrando una pantalla diferente de la app:
>
> - Phone izquierdo: **Pantalla de login** — botón de "Iniciar con
>   Google" sobre fondo oscuro con el logo de Royal Meld.
> - Phone central (más grande, al frente): **Pantalla de sala** —
>   lista de amigos con avatares, botón "Crear sala", código de sala
>   visible.
> - Phone derecho: **Pantalla de perfil/estadísticas** — gráfico de
>   progreso con partidas jugadas, Victorias, nivel.
>
> Los phones tienen un halo dorado sutil y están sobre un fondo de
> gradiente oscuro azul-negro. Partículas de luz dorada flotan alrededor.
>
> **Mood:** el mismo que HERO_01 — premium, coherente con la marca.
>
> **Dimensiones:** 1200×600px.

---

### 3.8 — Footer

**Layout:**
```
┌──────────────────────────────────────────────┐
│  [Logo Royal Meld]                           │
│                                               │
│  © 2026 Royal Meld. Todos los derechos       │
│  reservados.                                  │
│                                               │
│  [Términos] [Privacidad] [Contacto]          │
│                                               │
│  Síguenos: [🐦] [📸] [💬]                   │
└──────────────────────────────────────────────┘
```

---

## 4. Resumen de imágenes requeridas

| ID | Nombre | Tipo | Dimensiones | Uso |
|---|---|---|---|---|
| `HERO_01` | Mockup phone con juego | Render 3D | 1200×800 | Hero section |
| `GAMEPLAY_01` | Tablero completo | Screenshot real | 1920×1080 | Gameplay (principal) |
| `GAMEPLAY_02` | Mano del jugador close-up | Screenshot real | 800×600 | Gameplay (detalle) |
| `GAMEPLAY_03` | Pozo y descarte close-up | Screenshot real | 800×600 | Gameplay (detalle) |
| `GAMEPLAY_04` | Momento de bajarse | Screenshot real | 800×600 | Gameplay (acción) |
| `JUGAR_01` | Robar del mazo | Ilustración flat | 400×300 | Instrucciones paso 1 |
| `JUGAR_02` | Formar combinaciones | Ilustración flat | 400×300 | Instrucciones paso 2 |
| `JUGAR_03` | Bajarse a la mesa | Ilustración flat | 400×300 | Instrucciones paso 3 |
| `JUGAR_04` | Descartar y ganar | Ilustración flat | 400×300 | Instrucciones paso 4 |
| `CARDS_01` | Back Azul Clásico | Render 3D | 400×300 | Showcase cartas |
| `CARDS_02` | Back Rojo Rombos | Render 3D | 400×300 | Showcase cartas |
| `CARDS_03` | Back Negro Rayas | Render 3D | 400×300 | Showcase cartas |
| `CARDS_04` | Back Verde Anillos | Render 3D | 400×300 | Showcase cartas |
| `CARDS_05` | Front Borde Dorado | Render 3D | 400×300 | Showcase cartas |
| `CARDS_06` | Joker Dorado | Render 3D | 400×300 | Showcase cartas |
| `CTO_01` | Mockup phones múltiples | Render 3D | 1200×600 | CTA final |

**Total: 16 imágenes**

---

## 5. Copywriting (textos)

### Hero
- **Título:** Reúne, desafía y reina.
- **Subtítulo:** El juego de cartas multijugador favorito de Latinoamérica, ahora online.
- **CTA principal:** Descargar gratis
- **CTA secundario:** Ver gameplay

### About
- **Título:** ¿Qué es Royal Meld?
- **Cuerpo:** La plataforma definitiva para jugar Carioca con tus amigos, sin trampas y con reglas que tú controlas.
- **Punto 1:** Cartas clásicas — Juega Carioca con las reglas oficiales, 108 cartas y hasta 4 jugadores.
- **Punto 2:** Multijugador real — Enfrenta a amigos o rivales en salas privadas con código.
- **Punto 3:** Compite en vivo — Sube de nivel con cada partida y demuestra quién es el rey de la mesa.

### Features
- **Título:** ¿Por qué Royal Meld?
- **Sin trampas:** El servidor valida cada jugada. No hay forma de hacer trampa.
- **Reglas configurables:** Cada sala define sus rondas, cantidad de rondas y variantes.
- **Amigos y salas privadas:** Comparte un código y juega solo con quien tú quieras.
- **Baraja personalizable:** Elige el diseño de tus cartas: backs, fronts y jokers.
- **Reconexión automática:** Se cortó la conexión? Vuelve sin perder tu turno.
- **Estadísticas e historial:** Revisa tus partidas, puntajes y progreso.

### Gameplay
- **Título:** Mira cómo se juega
- **Cuerpo:** Roba cartas, forma combinaciones y descarta para terminar tu turno. Desliza para jugar, toca para elegir.

### Cómo se juega
- **Título:** ¿Cómo se juega Carioca?
- **Paso 1:** Roba una carta del mazo o del pozo.
- **Paso 2:** Forma escaleras o grupos en la mesa.
- **Paso 3:** Bajate cuando tengas las combinaciones listas.
- **Paso 4:** Descarta y repite hasta vaciar tu mano.
- **Cierre:** 9 rondas. Cada una más desafiante. ¿Listo para la Escala Real?

### Cartas
- **Título:** Personaliza tu baraja
- **Cuerpo:** Elige entre 7 diseños de respaldo, 4 frentes y 3 estilos de Joker.

### CTA Final
- **Título:** Empezar a jugar es fácil
- **Paso 1:** Descarga la app gratis.
- **Paso 2:** Inicia sesión con Google.
- **Paso 3:** Crea una sala y juega.

---

## 6. Comportamiento y animaciones

| Elemento | Comportamiento |
|---|---|
| **Navbar** | Fija (sticky). Transparente al inicio, sólida al hacer scroll. Logo a la izquierda, links a la derecha. En mobile: hamburger menu. |
| **Hero** | Fade-in del título (0.5s), slide-up del subtítulo (0.3s delay), fade-in del phone mockup (0.6s delay). Partículas doradas animadas en loop. |
| **Cards de features** | AOS (Animate On Scroll): fade-in + slide-up al entrar al viewport. Stagger de 0.1s entre cada card. |
| **Gameplay gallery** | Carrusel horizontal en mobile. Grid 2×2 en tablet. Full-width en desktop. Click abre lightbox con zoom. |
| **Instrucciones** | Pasos numerados con línea conectora dorada animada que se dibuja de izquierda a derecha al hacer scroll. |
| **Showcase de cartas** | Hover effect: la carta levanta ligeramente (translateY -8px) y proyecta sombra más grande. En mobile: tap para efecto. |
| **CTA button** | Gradiente dorado (#F9A825 → #FF8F00), hover: brillo (box-shadow dorado pulsante). Scale 1.05 en hover. |
| **Scroll indicator** | Flecha animada apuntando hacia abajo en el hero, se oculta al hacer scroll. |

---

## 7. Responsive breakpoints

| Breakpoint | Comportamiento |
|---|---|
| **≥1200px** (desktop) | Layout completo, 3 columnas para features, grid 2×2 para gameplay. |
| **768px–1199px** (tablet) | 2 columnas para features, carrusel para gameplay. Phone mockup más pequeño. |
| **<768px** (mobile) | 1 columna, todo apilado verticalmente. Navbar colapsa a hamburger. Gameplay en carrusel. Imágenes escaladas al 100% del ancho. |

---

## 8. SEO y meta

| Campo | Valor |
|---|---|
| **Title** | Royal Meld — Juega Carioca Online con Amigos |
| **Description** | Plataforma multijugador de Carioca para Android. Salas privadas, reglas configurables, sin trampas. Descarga gratis. |
| **Keywords** | carioca, juego de cartas, multijugador, online, android, cartas, rummy, naipes |
| **OG Image** | `HERO_01` |
| **Canonical** | `https://royalmeld.com` |

---

## 9. Stack técnico propuesto

| Capa | Tecnología |
|---|---|
| **Framework** | Angular 22 (standalone components, signals) |
| **UI** | Angular Material (navbar, botones) + Custom CSS |
| **Animaciones** | AOS.js (scroll) + Angular animations |
| **Icons** | Material Icons + custom SVG (logo) |
| **Imágenes** | WebP con fallback JPEG, lazy loading |
| **Hosting** | AWS S3 + CloudFront (estático, CDN global) |
| **Domain** | `royalmeld.com` (o subdominio `landing.royalmeld.com`) |
| **Analytics** | Google Analytics 4 + Google Tag Manager |

---

## 10. Estructura de archivos

```
frontend/web/landing/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   ├── navbar/
│   │   │   ├── hero/
│   │   │   ├── about/
│   │   │   ├── gameplay/
│   │   │   ├── features/
│   │   │   ├── how-to-play/
│   │   │   ├── card-showcase/
│   │   │   ├── cta/
│   │   │   └── footer/
│   │   ├── sections/          # Secciones como routes (scroll-to)
│   │   ├── assets/
│   │   │   ├── images/        # Todas las imágenes (HERO_01, GAMEPLAY_*, etc.)
│   │   │   ├── icons/
│   │   │   └── logo/
│   │   └── styles/
│   │       ├── _variables.scss
│   │       ├── _typography.scss
│   │       └── _animations.scss
│   ├── index.html
│   └── environments/
├── angular.json
├── package.json
└── README.md
```

---

## 11. Checklist de implementación

- [ ] Generar las 16 imágenes (usar descriptions de la sección 4)
- [ ] Crear proyecto Angular con `ng new landing --style=scss`
- [ ] Implementar Navbar sticky responsive
- [ ] Implementar Hero con animación de entrada
- [ ] Implementar About (3 features)
- [ ] Implementar Gameplay gallery con lightbox
- [ ] Implementar Features (6 cards con AOS)
- [ ] Implementar Cómo se juega (4 pasos con línea conectora)
- [ ] Implementar Card Showcase (6 diseños con hover)
- [ ] Implementar CTA final con mockup
- [ ] Implementar Footer
- [ ] Responsive testing (desktop, tablet, mobile)
- [ ] SEO: meta tags, OG image, sitemap.xml
- [ ] Deploy a S3 + CloudFront
- [ ] Configurar dominio y certificado SSL
- [ ] Integrar Google Analytics 4
