# Comandi e configurazione

---

## 1. Comandi

| Comando | Chi può usarlo | Cosa fa |
| :--- | :--- | :--- |
| `/festival status` | Tutti | Dice quale festa (o Shabbat) è in corso. |
| `/festival start <tipo>` | Operatori | Forza una festa, per esempio `hanukkah` o `shabbat`. |
| `/festival stop` | Operatori | Ferma la festa forzata. |
| `/mossad accept <missione>` | Tutti | Accetta una missione. Devi essere vicino a un Referente del Mossad. |
| `/mossad status` | Tutti | Mostra la missione in corso e la tua reputazione. |
| `/mossad abandon` | Tutti | Abbandona la missione (reputazione -5). |
| `/mossad spawn_agents [numero]` | Operatori | Fa spawnare agenti del Mossad (default 2, massimo 8). |
| `/mossad reputation <valore>` | Operatori | Imposta la tua reputazione col Mossad (da -100 a 100). |
| `/israelsim event speech start [pos]` | Operatori | Avvia il discorso in piazza. |
| `/israelsim event speech stop` | Operatori | Ferma il discorso. |
| `/israelsim event speech status` | Operatori | Dice se c'è un discorso in corso e quanto manca. |
| `/israelsim event speech duration [secondi]` | Operatori | Mostra o cambia quanto dura il discorso. |
| `/israelsim event plane_crash start [pos]` | Operatori | Fa partire l'evento dell'aereo. |
| `/israelsim event plane_crash stop` | Operatori | Ferma l'evento dell'aereo. |
| `/israelsim towers setpos [pos]` | Operatori | Imposta dove stanno le Torri Gemelle per l'evento. |

Per trovare una struttura usa il comando vanilla `/locate structure israel_simulator:<nome>`.

---

## 2. File di config (`config/israel_simulator-common.toml`)

Si crea al primo avvio. Le opzioni principali:
- `blessedDurationTicks`, `blessedAmplifier`: durata e livello di Benedizione.
- `kosherSystemEnabled`, `meatDigestionTicks`, `dairyDigestionTicks`: regole kosher e attese tra carne e latticini.
- `bibiBossMaxHealth` (default 10.000), `bibiBossBaseDamage`, `bibiBossMaxGuards`, `bibiBossArenaRadius`: lo scontro con Bibi.
- `citySpacingChunks`, `rareStructureSpacingChunks`: quanto sono distanti città e strutture rare.
- `maxNpcPerChunk`, `villagerTradeDiscountMax`: NPC e sconto massimo.
- `eventDurationTicks`, `eventCooldownTicks`, `speechDurationTicks`, `speechMinParticipationTicks`: eventi nel mondo e discorso in piazza.
- `prayerCooldownTicks`, `shofarCooldownTicks`, `transitCooldownTicks`: tempi di attesa.
- `maxParticlesPerEvent`, `enablePerformanceThrottling`: prestazioni.
- `musicVolumeMultiplier`, `cityAmbienceEnabled`: audio.
- `weaponDamageMultiplier`: danno delle armi.
