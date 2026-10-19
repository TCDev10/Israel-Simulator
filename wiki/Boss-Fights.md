# Boss: Bibi, Trump ed Epstein

Bibi, Donald Trump e Jeffrey Epstein sono parodie. Sono personaggi satirici, non vanno presi sul serio (vedi **GAME_DESIGN.md §41-44** e **AGENTS.md §22**).

---

## 1. Come si evoca Bibi

Bibi non spawna da solo, devi chiamarlo tu.
- **Ti serve**: la Stella di Davide (`israel_simulator:star_of_david`).
- **Come**: tasto destro con la Stella in mano. Se nella zona non c'è già un boss, arriva Bibi con tuoni, particelle scure e un messaggio in chat per tutti i giocatori entro 96 blocchi.
- **Un Bibi alla volta**: non puoi evocarne due nella stessa arena.

---

## 2. Fase 1 (`bibi_boss`)

- **Vita**: 10.000 (si cambia nel config), barra blu.
- **Niente one-shot**: un singolo colpo non può fare più di 500 danni (`MAX_SINGLE_HIT_DAMAGE`).
- **Attacchi**:
  1. **Missile a ricerca** (`BibiMissileEntity`): un missile veloce che ti insegue e esplode all'impatto, colpendo anche chi ti sta vicino.
  2. **Bombardamento**: fa piovere 8 missili in un'area larga intorno a sé. Esplosioni, fuoco e vegetazione distrutta.
  3. **Onda d'urto (ostruzionismo)**: ti sbalza indietro e ti dà Lentezza. Prima dava anche Cecità, l'abbiamo tolta perché non si vedeva niente.
  4. **Guardie** (`bibi_guard`): ogni tanto ne evoca qualcuna. Hanno la pistola e ti sparano da lontano.

---

## 3. Fase 2: arriva Trump (50% di vita)

Quando Bibi scende al 50% (`ENRAGE_HEALTH_FRACTION = 0.50F`):
- Si **infuria** e la barra diventa rossa.
- **Attacca più spesso**:
  - il missile a ricerca diventa una raffica da due;
  - il bombardamento si ricarica in metà tempo (da 180 a 90 tick) e i missili diventano 16.
- **Arriva Donald Trump** (`trump_miniboss`): smettono di arrivare guardie e scende in campo Trump (1.500 di vita, barra gialla).
- **Scudo**: finché Trump è vivo, Bibi è invulnerabile. Ogni colpo viene bloccato (particelle del Totem e suono dello scudo).
- **Attacchi di Trump**:
  1. **Muro di soldi**: un muro di banconote e monete che ti travolge davanti a lui. Fa molto danno, ti sbalza e toglie 35 di durabilità a ogni pezzo d'armatura. Ha una ricarica lunga.
  2. **Chiama l'ICE**: evoca 3 agenti dell'ICE con armatura e spada di netherite.
- **Lo scudo si rompe** quando muore Trump, e Bibi torna attaccabile.

---

## 4. Fase 3: arriva Epstein (33% di vita)

Sotto un terzo della vita Bibi diventa **disperato** e arriva Jeffrey Epstein con i suoi scagnozzi. Anche lui protegge Bibi: prima devi battere lui.

---

## 5. Armi per il giocatore

- **Pistola** (`israel_simulator:pistol`):
  - colpo istantaneo fino a 32 blocchi;
  - 9 danni (4,5 cuori), 250 di durabilità, si incanta con Indistruttibilità e Riparazione.
- **Missile tattico** (`israel_simulator:missile`):
  - lo lanci dall'inventario e esplode all'impatto.

Per il resto delle armi (fucili, Uzi, granate, drone) guarda il README.

---

## 6. Cosa lascia Bibi

- **Corona del rabbino** (`rabbis_crown`): sempre. È l'unico modo per averla.
- **Disco Hava Nagila** (`hava_nagila_disc`): sempre. Si suona nel jukebox.
- **48 Shekel** (`shekel`).
- **6 monete antiche** (`ancient_coin`).
- **5 diamanti**.
- **Progresso "Hava Nagila"**: uccidi Bibi e raccogli il disco.
