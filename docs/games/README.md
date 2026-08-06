# Games — Juegos de la plataforma

> Documentación por juego. Cada juego vive en su propia carpeta bajo `docs/games/`
> y contiene sus **reglas oficiales** (que se traducen a un `Ruleset` del motor).
> Para agregar un juego nuevo seguir la guía:
> **[`docs/adding-a-game.md`](../adding-a-game.md)**.

## Índice de juegos

| Juego | Reglas | Estado | Jugadores | Baraja |
|---|---|---|---|---|
| [**Carioca**](carioca/rules.md) | `docs/games/carioca/rules.md` | Activo (MVP) | 2–4 (máx. 4) | **2 juegos (108)** — fijo |
| *(Ronda / Truco / loba…)* | — | Candidatos futuros | — | misma baraja |

> Carioca es el primer juego del motor multi-juego. La estructura de carpetas
> está preparada para que cada juego nuevo agregue `docs/games/<id-juego>/rules.md`.

## Estructura

```
docs/games/
├── README.md          # este índice
└── carioca/
    └── rules.md       # reglas oficiales de Carioca
```
