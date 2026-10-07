# Il Muro Occidentale e le Preghiere (Western Wall & Kotel)

Il **Muro Occidentale** (Kotel HaMa'aravi) a Gerusalemme rappresenta il cuore spirituale della mappa (specifiche in **GAME_DESIGN.md §10–11**).

---

## 1. Il Blocco Kotel e l'Interazione

Il blocco del Kotel (`israel_simulator:western_wall`) possiede logiche server-authoritative con cooldown e protezione anti-exploit.

### Come Inserire una Preghiera (Kvitel)
1. **Scrivere la nota**: Tieni in mano una **Prayer Note** (`israel_simulator:prayer_note`).
2. **Inserimento nella fessura**: Clicca con il tasto destro sul blocco del Muro Occidentale.
3. **Animazione e Particelle**:
   - Vengono generate particelle dorate e sacre lungo il muro.
   - Viene riprodotto il suono della preghiera e del raccoglimento.
   - Il giocatore entra nello stato visivo di preghiera (inchino/shuckling sincronizzato).

---

## 2. Benedizioni e Ricompense

Pregare al Kotel conferisce benefici proporzionali all'equipaggiamento indossato e al contesto:
- **Benedizione Divina (Blessed)**: Rigenerazione aumentata, resistenza ai danni e protezione spirituale.
- **Bonus Minyan**: Se almeno 10 giocatori (o NPC religiosi) pregano contemporaneamente nella piazza del Kotel, tutti i partecipanti ricevono l'effetto potenziato **Minyan Blessing** con velocità e rigenerazione prolungata.
- **Bonus Equipaggiamento**: Indossare **Kippah**, **Talit** o **Tefillin** prolunga la durata della benedizione e azzera il rischio di interruzione.
- **Cooldown Server-Side**: La ricompensa spirituale può essere riscossa una volta ogni giorno di gioco (24.000 tick) per evitare abusi di farming.

