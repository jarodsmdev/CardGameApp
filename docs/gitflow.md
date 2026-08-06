# Flujo de trabajo Git (Git Flow)

> Modelo de ramas del monorepo `CARD_GAME` (decisión: `docs/adr.md` **ADR-015**).
> Guía operativa para seguir Git Flow de forma correcta y consistente.

---

## 1. Ramas permanentes

| Rama | Descripción | Reglas |
|---|---|---|
| `main` | **Producción.** Solo código desplegable. | Cada merge = una release con **tag `vX.Y.Z`**. Push directo **prohibido**. |
| `develop` | **Integración.** Código estable y verde (CI). | Origen de las ramas `feature/`. Push directo **prohibido**. |

> La rama **`main` nunca se rompe**. Si `develop` se rompe, se arregla de inmediato
> (no se sigue desarrollando sobre una base roja).

## 2. Ramas de soporte (corta vida)

| Rama | Base | Destino | Regla |
|---|---|---|---|
| `feature/<área>-<desc>` | `develop` | `develop` | Una por tarea; se elimina tras el merge. |
| `release/vX.Y.Z` | `develop` | `main` + `develop` | Solo bugs, versión y changelog. |
| `hotfix/vX.Y.Z+1` | `main` | `main` + `develop` | Para bugs críticos en producción. |

### Nombrado de ramas

- `feature/<área>-<desc>` → `feature/carioca-motor`, `feature/admin-web-auth`.
- Áreas del monorepo: `backend`, `movil`, `web`, `docs`, `infra`.
- `release/v1.2.0`, `hotfix/v1.2.1`.

---

## 3. Ciclo normal (feature)

```
main ───────────────────────────────────────────────►  (solo releases)
develop ──●──────────────────────────────────●────────
          │                                  │
          └── feature/carioca-motor ──► PR ──┘
```

1. Actualizar `develop` y crear la rama:

```bash
git switch develop
git pull --ff-only origin develop
git switch -c feature/carioca-motor
```

2. Trabajar con **commits convencionales** (§5). Pequeños y atómicos.

3. Publicar y abrir **PR** hacia `develop`:

```bash
git push -u origin feature/carioca-motor
```

4. En el PR: **CI verde + revisión obligatoria** (al menos 1 aprobación) +
   **DoD** (`SPECS.md §11`). Merge con **squash**.
5. Eliminar la rama remota y local tras el merge.

---

## 4. Release

```
develop ──●─────────────►  release/v1.2.0  ──►  main (tag v1.2.0)
          │                    │                 │
          └────── (merge back) └───────────────► develop
```

1. Desde `develop` (con CI verde) crear la rama de release:

```bash
git switch develop
git pull --ff-only origin develop
git switch -c release/v1.2.0 develop
git push -u origin release/v1.2.0
```

2. En `release/` **solo**:
   - Correcciones de bugs (PR de `release/vX.Y.Z`).
   - Bump de versión (`vX.Y.Z`, semver).
   - Changelog.
   - **Nada de features nuevas.**

3. Publicar la release (merge a `main`, tag, y merge de vuelta a `develop`):

```bash
git switch main
git pull --ff-only origin main
git merge --no-ff release/v1.2.0
git tag -a v1.2.0 -m "Release v1.2.0"
git push origin main --tags

git switch develop
git pull --ff-only origin develop
git merge --no-ff release/v1.2.0
git push origin develop
git branch -d release/v1.2.0
```

4. CI despliega `main` a producción (deploy gated/manual si aplica).

> **Orden correcto:** merge a `main` → tag → merge de vuelta a `develop`. Hacer el
> merge a `develop` **antes** que a `main` es el pitfall más común.

---

## 5. Hotfix (bug crítico en producción)

```
main ──●────────────────► hotfix/v1.2.1  ──►  main (tag v1.2.1)
       │                         │              │
       └── (merge back) ─────────┴────────────► develop
```

1. Desde `main`:

```bash
git switch main
git pull --ff-only origin main
git switch -c hotfix/v1.2.1
```

2. Aplicar el fix con commit convencional `fix(...):`.
3. PR hacia `main`; merge (**squash** o merge commit) + tag:

```bash
git switch main
git merge --no-ff hotfix/v1.2.1
git tag -a v1.2.1 -m "Hotfix v1.2.1"
git push origin main --tags
```

4. Sincronizar `develop` (el fix debe llegar a integración):

```bash
git switch develop
git pull --ff-only origin develop
git merge --no-ff hotfix/v1.2.1
git push origin develop
git branch -d hotfix/v1.2.1
```

---

## 6. Convenciones de commits (Conventional Commits)

| Tipo | Uso |
|---|---|
| `feat` | Nueva funcionalidad |
| `fix` | Corrección de bug |
| `docs` | Documentación (incluye `docs/`, `SPECS.md`, `ERS.md`) |
| `refactor` | Cambio sin cambiar comportamiento |
| `test` | Pruebas |
| `chore` | Build, dependencias, herramientas |
| `ci` | Pipeline (GitHub Actions) |
| `perf` | Mejora de rendimiento |

Formato: `tipo(ámbito): descripción` → `feat(motor): validar escalas con jokers`.

- Los **commits en `develop`** quedan como **squash** (1 commit por PR).
- **Prohibido:** `git commit` directamente en `main`/`develop`.

---

## 7. Monorepo: consideraciones

- **Un solo repo** con un único `develop`, `main` y tags globales (`vX.Y.Z`).
- **CI por paths** (GitHub Actions `paths:`): un cambio en `backend/**` **no**
  dispara el build de Android ni el de Angular.

| Path | Pipeline |
|---|---|
| `backend/**` | lint + test Java + build jar + docker |
| `frontend/movil/**` | lint (ktlint) + test Kotlin + assemble APK |
| `frontend/web/**` | lint + build Angular |
| `docs/**` | sin CI (o lint de markdown opcional) |
| `docker/**`, `scripts/**` | build de infra/validación de compose |

- **Una release publica todos los componentes con la misma versión.** Si un
  componente no cambió, igualmente se etiqueta la release (versión compartida del
  producto).
- Un PR de un solo componente toca **solo ese path**; un PR que toque varios
  módulos debe justificarse (cambio transversal) y correr todos los pipelines.

---

## 8. Protección de ramas (GitHub)

En **Settings → Branches → Branch protection rules** para `main` y `develop`:

- [ ] Require pull request reviews (1 approval mínimo).
- [ ] Require status checks (CI verde de los paths afectados).
- [ ] Require up-to-date branches.
- [ ] No direct pushes.
- [ ] Deletar ramas de feature automáticamente tras el merge.

---

## 9. Checklist de release (DoD)

- [ ] CI verde en `develop` (todos los paths afectados).
- [ ] PR de release revisado y aprobado.
- [ ] Bump de versión semver + changelog actualizado.
- [ ] Merge a `main` con merge commit.
- [ ] **Tag `vX.Y.Z`** creado y subido.
- [ ] Merge de vuelta a `develop` (release/hotfix).
- [ ] Deploy a producción (si es manual, ejecutado por el responsable).

---

## 10. Errores comunes

- **Merge a `develop` antes que a `main`** → la release queda publicada sin tag
  o con conflictos. Seguir el orden de §4 paso 3.
- **Olvidar el tag** → la release no es trazable ni reproducible.
- **Feature muy larga** → integrar a `develop` con frecuencia; ramas de días no
  de semanas.
- **Hotfix sin merge de vuelta a `develop`** → el fix se pierde en la siguiente
  release.
- **Cambios fuera del path del PR** → rompe el aislamiento del monorepo.
- **Push directo a `main`/`develop`** → bypass de CI y revisión.

---

## 11. Alternativa manual (sin plugin `git-flow`)

Todos los flujos anteriores se pueden hacer con `git` puro (los comandos de esta
guía ya son manuales). El plugin `git-flow` (`git flow init`, `git flow feature
start <x>`, `git flow release finish <v>`) solo automatiza ramas, merges y tags;
si se instala, la **configuración por defecto** coincide con esta guía:

```
git flow init        # main = main, develop = develop, prefijos por defecto
```
