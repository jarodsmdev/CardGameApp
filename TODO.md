## Bug / Test de regresión: JOKER no puede ser utilizado ni descartado después de completar una escala

Estoy desarrollando un juego móvil de **Carioca con naipe inglés** y encontré un bug que puede dejar al jugador en un estado en el que **no puede continuar ni ganar la partida**.

Necesito que primero conviertas este escenario en un **test unitario/integración reproducible**, utilizando la arquitectura de testing existente del proyecto. Una vez que el test reproduzca el problema, identifica la causa raíz y corrige la implementación sin romper las reglas existentes de escalas, JOKER o Lay-off.

---

# Escenario exacto para reproducir el bug

La partida se encuentra en:

```text
Ronda 3/9
2 escalas
```

Soy el único jugador que se ha bajado hasta ese momento.

Me bajo con **dos escalas del mismo palo: Picas (♠)**.

### Primera escala

```text
8♠ 9♠ 10♠ J♠
```

### Segunda escala

```text
Q♠ K♠ A♠ 2♠
```

Estas dos escalas son válidas según las reglas actuales del juego.

---

## Siguiente turno

En el siguiente turno agrego mediante Lay-off:

```text
3♠ 4♠
```

a la segunda escala.

Por lo tanto, la segunda escala pasa a ser:

```text
Q♠ K♠ A♠ 2♠ 3♠ 4♠
```

Conceptualmente, la secuencia completa es:

```text
Q♠ K♠ A♠ 2♠ 3♠ 4♠
```

La primera escala permanece:

```text
8♠ 9♠ 10♠ J♠
```

---

# Estado posterior

Posteriormente robo una carta y obtengo un **JOKER**.

Mi mano queda:

```text
JOKER
6♠
J♠
```

En este punto, según la interfaz, el botón de **añadir a mesa** se activa.

Sin embargo, cuando intento realizar la jugada, aparece un mensaje de error en rojo:

> "añadir la carta rompe la combinación o choca con un comodín"

El problema es que el juego considera que la jugada no es válida, aunque el estado debería permitir continuar.

Además, actualmente parece que **no puedo descartar el JOKER**, por lo que quedo completamente bloqueado.

Puedo seguir robando cartas, pero ninguna acción me permite avanzar hacia la victoria.

---

# Comportamiento problemático

El estado termina siendo aproximadamente:

```text
Mesa:

Escala 1:
8♠ 9♠ 10♠ J♠

Escala 2:
Q♠ K♠ A♠ 2♠ 3♠ 4♠


Mi mano:

JOKER
6♠
J♠
```

El juego debería permitir determinar correctamente qué jugada es posible con estas cartas.

Actualmente ocurre:

```text
JOKER + 6♠ + J♠
        ↓
Botón "bajar" habilitado
        ↓
Intentar jugar
        ↓
ERROR:
"añadir la carta rompe la combinación o choca con un comodín"
        ↓
JOKER tampoco puede descartarse
        ↓
Jugador queda bloqueado
```

---

# Objetivo del test

Crear un test que reproduzca exactamente este estado y permita verificar el comportamiento esperado.

El test debe:

1. Crear/inicializar una partida en la **ronda 3/9**.
2. Configurar al jugador humano como el único jugador que se ha bajado.
3. Crear las dos escalas iniciales:

```text
8♠ 9♠ 10♠ J♠
Q♠ K♠ A♠ 2♠
```

4. Ejecutar el Lay-off:

```text
3♠ 4♠
```

sobre la segunda escala.
5. Verificar que la escala resultante sea válida:

```text
Q♠ K♠ A♠ 2♠ 3♠ 4♠
```

6. Agregar a la mano del jugador:

```text
JOKER
6♠
J♠
```

7. Ejecutar exactamente la misma operación que actualmente activa el error.
8. Verificar el resultado real.
9. El test debe fallar actualmente si reproduce el bug.

---

# Importante: no asumir cuál es el error

Antes de corregirlo, analiza qué está fallando realmente.

Hay varias posibilidades que deben investigarse:

### A. Validación incorrecta de la escala

La lógica podría estar interpretando incorrectamente:

```text
Q K A 2 3 4
```

como una secuencia inválida.

Revisa cómo está implementada actualmente la lógica del AS y las secuencias que cruzan:

```text
Q → K → A → 2
```

No inventes una nueva regla. Utiliza la regla que ya utiliza el juego.

---

### B. Problema con el JOKER

El mensaje:

> "añadir la carta rompe la combinación o choca con un comodín"

podría indicar que el sistema está interpretando incorrectamente la presencia del JOKER.

Revisa especialmente:

* Validación de JOKER.
* Posición lógica del JOKER.
* Valor que representa.
* Detección de conflictos con cartas existentes.
* Validación de una mano que contiene un JOKER.
* Validación de combinaciones que contienen un JOKER.
* Reglas para agregar cartas a escalas que contienen o no contienen JOKER.

---

### C. Problema al determinar dónde agregar una carta

También podría existir un problema en la lógica que intenta determinar si:

```text
6♠
```

puede continuar alguna de las escalas.

Revisa ambas escalas existentes:

```text
8♠ 9♠ 10♠ J♠
```

y:

```text
Q♠ K♠ A♠ 2♠ 3♠ 4♠
```

La lógica debe determinar correctamente si `6♠` puede agregarse a alguna combinación, teniendo en cuenta las reglas actuales del juego.

---

### D. Problema con el descarte del JOKER

Existe además un segundo bug potencial:

> Si el jugador no puede utilizar el JOKER en una combinación, debería poder descartarlo si las reglas actuales del juego permiten descartar un JOKER.

Actualmente parece que el JOKER tampoco puede ser utilizado como carta de descarte, dejando al jugador sin una forma válida de terminar el turno.

Esto debe investigarse por separado.

No asumir que un JOKER necesariamente puede o no puede descartarse: **revisa las reglas y la implementación existente del juego**.

Si el juego permite descartar JOKER, debe existir un test específico para verificarlo.

---

# Tests que quiero que agregues

Además del test que reproduce exactamente el bug, agrega tests específicos para separar las posibles causas.

### Test 1 — Escala Q-K-A-2-3-4

Verificar que la combinación:

```text
Q♠ K♠ A♠ 2♠ 3♠ 4♠
```

sea considerada válida según las reglas actuales del juego.

---

### Test 2 — Lay-off sobre Q-K-A-2

Partiendo de:

```text
Q♠ K♠ A♠ 2♠
```

agregar:

```text
3♠ 4♠
```

y verificar que resulte:

```text
Q♠ K♠ A♠ 2♠ 3♠ 4♠
```

y que la combinación continúe siendo válida.

---

### Test 3 — Mano con JOKER

Crear una mano:

```text
JOKER
6♠
J♠
```

y verificar que el sistema pueda determinar correctamente qué jugadas son posibles.

---

### Test 4 — JOKER descartable

Si las reglas actuales permiten descartar un JOKER, crear un test que verifique explícitamente:

```text
mano:
JOKER
6♠
J♠
```

→ seleccionar JOKER para descartar

→ el descarte es aceptado

→ el turno termina correctamente.

Si las reglas actuales NO permiten descartar un JOKER, documentar claramente la razón y verificar que el jugador tenga alguna otra acción válida para no quedar bloqueado.

---

### Test 5 — 6♠ sobre las escalas existentes

Con:

```text
8♠ 9♠ 10♠ J♠

Q♠ K♠ A♠ 2♠ 3♠ 4♠
```

verificar si:

```text
6♠
```

puede agregarse a alguna de las escalas según las reglas actuales.

El test debe comprobar el resultado real esperado según la implementación de las reglas del Carioca, no una regla inventada.

---

### Test 6 — J♠

Con las mismas escalas, verificar qué ocurre al intentar utilizar:

```text
J♠
```

y confirmar que el motor determine correctamente si puede utilizarse mediante Lay-off o si debe permanecer en la mano.

---

# Criterio fundamental

El objetivo no es simplemente hacer que este caso específico pase.

Necesito encontrar y corregir la **causa raíz** que provoca que el jugador quede bloqueado.

La solución debe garantizar que:

* El motor pueda validar correctamente las escalas involucradas.
* Las secuencias que utilizan `Q-K-A-2` se manejen según las reglas existentes.
* Los Lay-off se validen correctamente.
* Los JOKER sean tratados correctamente.
* El sistema pueda determinar correctamente qué cartas de la mano pueden utilizarse.
* Si el JOKER puede descartarse según las reglas actuales, el jugador pueda descartarlo.
* El jugador nunca quede en un estado en el que tenga cartas pero no exista ninguna acción válida para continuar, salvo que las reglas del juego realmente contemplen ese estado.
* El comportamiento de los casos existentes no se rompa.

---

# Análisis / causa raíz del bug (diagnóstico documentado)

Escenario de reproducción (ver tests de regresión en
`frontend/movil/domain/src/test/java/com/jarod/card/domain/games/carioca/JokerBlockedRegressionTest.kt`):

- Ronda 3/9 (2 escalas). p1 se bajó con escala1 = 8♠ 9♠ 10♠ J♠ y
  escala2 = Q♠ K♠ A♠ 2♠ 3♠ 4♠ (Q-K-A-2 bajada + lay-off de 3♠ 4♠ en un turno anterior).
- Mano de p1 = JOKER + 6♠ + J♠. Turno de p1 en `ACTIONS`, turno posterior al de bajarse.
- La UI habilitaba "Añadir a mesa" pero el motor rechazaba la jugada:
  *"añadir la carta rompe la combinación o choca con un comodín"*.

Conclusión: la heurística `CariocaBot.findLayOff` (que alimenta el botón
"Añadir a mesa") validaba con `MeldValidator.validate(meld + carta)`, que
**acepta el JOKER en cualquier posición** (forma no canónica, ignorando el
orden). Con la escala 2 = Q-K-A-2-3-4, el JOKER (sin rank) se insertaba de
forma "válida" junto a la escala 1 (8-9-10-J), y además el único hueco real
disponible (J♠ para J-Q-K-A-2-3-4) quedaba oculto porque el JOKER ocupaba el
candidato. En cambio el motor (el usuario en el chat, `CariocaGame.validateLayOff`
→ `MeldValidator.validateLayOff`) exige que el joker quede **a un extremo** de la
escala y con **3 cartas naturales** de separación (rule "entre tres cartas",
§5), y rechaza añadir el JOKER por lay-off en cualquier otra posición. Resultado:
UI sugiere lo que el motor rechaza → error en rojo → jugador bloqueado (con 6♠ y
J♠ en mano, solo puede descartar 6♠ y pasar turnos sin poder salir).

Fix: `CariocaBot.findLayOff` debe usar la **misma** validación que el motor
(`MeldValidator.validateLayOff`, con `RunSide` FRONT/BACK), no
`MeldValidator.validate`. Así el botón se deshabilita cuando no hay jugada real y,
cuando se habilita, sugiere el único lay-off válido (J♠ sobre la escala 2).

Actualización de regla (ADR §5, "entre tres cartas"): el joker **sí puede**
añadirse por lay-off a una escala sin joker (a un extremo, quedando fijo). Sin
tope fijo por escala: el límite es la separación (≥3 naturales entre comodines).

Tests de causas aisladas que pasan (confirman que el resto del dominio está OK):

1. `Q♠ K♠ A♠ 2♠ 3♠ 4♠` es escala válida (wraparound A→2, `validateRun`).
2. El lay-off de 3♠ 4♠ sobre Q-K-A-2 deja la escala Q-K-A-2-3-4 válida y no cierra la ronda.
3. El JOKER puede añadirse por lay-off a una escala sin joker (test3, regla §5 actualizada).
4. El JOKER no se puede descartar (regla `cannotDiscard`), pero el jugador sí puede descartar 6♠.
5. 6♠ no puede añadirse a ninguna escala.
6. J♠ se añade a la escala 2 (J-Q-K-A-2-3-4) pero no a la escala 1 (ya la contiene).

---

# Forma de trabajo

Primero:

1. Inspecciona la arquitectura existente.
2. Localiza la lógica de validación de escalas.
3. Localiza la lógica de JOKER.
4. Localiza la lógica de Lay-off.
5. Localiza la lógica de descarte.
6. Localiza cómo se determina si el botón "bajar" puede habilitarse.
7. Crea el test de regresión que reproduzca exactamente este escenario.

Después:

8. Ejecuta el test y confirma que reproduce el bug.
9. Identifica la causa raíz.
10. Implementa la corrección mínima necesaria.
11. Ejecuta nuevamente el test.
12. Agrega los tests adicionales necesarios para evitar regresiones.
13. Ejecuta toda la suite de tests relacionada con cartas, escalas, JOKER, Lay-off y descarte.

No hagas un workaround específico para esta mano.

La solución debe corregir la lógica general para que otros escenarios equivalentes también funcionen correctamente.

Al finalizar, explica:

* Qué causaba el bug.
* Qué archivos/componentes/funciones fueron modificados.
* Qué comportamiento se corrigió.
* Qué tests nuevos fueron agregados.
* Qué casos quedaron cubiertos.

