# Comandi e Configurazione (Commands & Configuration)

Israel-Simulator offre comandi dedicati per amministratori e giocatori per gestire il tempo festivo, l'economia e la navigazione rapida.

---

## 1. Comandi di Gioco (`/israel`)

| Comando | Permesso | Descrizione |
| :--- | :--- | :--- |
| `/israel shabbat status` | Tutti | Mostra lo stato attuale dello Shabbat e il tempo mancante all'inizio o alla fine. |
| `/israel shabbat start` | Operatore | Attiva forzatamente lo Shabbat nel mondo per test o celebrazioni. |
| `/israel shabbat end` | Operatore | Conclude anticipatamente lo Shabbat. |
| `/israel festival info` | Tutti | Mostra la festività attiva nel calendario del mondo. |
| `/israel economy prices` | Tutti | Visualizza i prezzi correnti delle materie prime nei vari distretti. |
| `/israel transit list` | Tutti | Mostra tutte le fermate registrate della rete di trasporto rapido. |

---

## 2. File di Configurazione (`config/israel_simulator-server.toml`)

Il server consente di calibrare:
- `shabbat_cycle_days`: Intervallo in giorni di gioco tra ogni Shabbat (default: 7).
- `boss_health`: Punti salute base del Boss Bibi (default: 300).
- `economy_inflation_rate`: Moltiplicatore di fluttuazione dei prezzi di mercato.
- `heatstroke_enabled`: Abilita/disabilita i colpi di calore nel deserto della Giudea.

