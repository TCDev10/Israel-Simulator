# Israel-Simulator — Manuale Completo di Collaudo e Testing (In-Game & Server)

> **Versione target:** Minecraft 26.2 / NeoForge 26.2.0.88  
> **Obiettivo:** Fornire una guida esaustiva e passo-passo per il collaudo manuale in-game (Singleplayer e Server Dedicato) di tutti i sistemi, strutture, oggetti, economie, boss ed eventi di *Israel-Simulator*.

---

## Indice dei Sistemi da Collaudare

1. [Prerequisiti & Comandi di Setup](#1-prerequisiti--comandi-di-setup)
2. [Biomi e Generazione del Mondo](#2-biomi-e-generazione-del-mondo)
3. [Le 16 Strutture Reali e Città](#3-le-16-strutture-reali-e-città)
4. [Western Wall (Muro del Pianto) & Rituale di Preghiera](#4-western-wall-muro-del-pianto--rituale-di-preghiera)
5. [Equipaggiamento e Oggetti Culturali / Religiosi](#5-equipaggiamento-e-oggetti-culturali--religiosi)
6. [Quartieri di Tel Aviv & Dinamiche Urbane](#6-quartieri-di-tel-aviv--dinamiche-urbane)
7. [Agricoltura, Raccolti e Ricette Gastronomiche](#7-agricoltura-raccolti-e-ricette-gastronomiche)
8. [Economia Regionale, Commercio & Valuta (Shekel / Agorot)](#8-economia-regionale-commercio--valuta-shekel--agorot)
9. [Rete dei Trasporti (Bicicletta, Fermate, Rav-Kav, Scarpe)](#9-rete-dei-trasporti-bicicletta-fermate-rav-kav-scarpe)
10. [Mar Morto & Pericoli del Deserto di Giuda](#10-mar-morto--pericoli-del-deserto-di-giuda)
11. [Boss Endgame: Bibi Boss & Coalition Guards](#11-boss-endgame-bibi-boss--coalition-guards)
12. [Feste Ebraiche, Calendario & Mini-giochi (Dreidel, Shofar, Menorah)](#12-feste-ebraiche-calendario--mini-giochi-dreidel-shofar-menorah)
13. [Tracciamento Esplorativo, Mappa d'Israele & Reputazione](#13-tracciamento-esplorativo-mappa-disraele--reputazione)
14. [Persistenza su Server Dedicato & Anti-Exploit](#14-persistenza-su-server-dedicato--anti-exploit)

---

## 1. Prerequisiti & Comandi di Setup

Per collaudare efficacemente le meccaniche, abilitare i cheat nel mondo (`/gamemode creative` o survival con opdritti).

### Comandi utili per il setup dei test:
```text
# Passaggio rapido tra modalità
/gamemode creative
/gamemode survival

# Ottenere gli oggetti chiave del mod
/give @p israel_simulator:kippah
/give @p israel_simulator:talit
/give @p israel_simulator:tefillin
/give @p israel_simulator:rabbis_crown
/give @p israel_simulator:prayer_note 64
/give @p israel_simulator:shekel 64
/give @p israel_simulator:rav_kav
/give @p israel_simulator:bicycle
/give @p israel_simulator:israel_map
/give @p israel_simulator:menorah 8
/give @p israel_simulator:shofar
/give @p israel_simulator:dreidel
/give @p israel_simulator:hava_nagila_disc
```

---

## 2. Biomi e Generazione del Mondo

Il generatore multi-noise crea una striscia climatica Ovest→Est coerente: Costa Mediterranea → Area Urbana → Agricoltura → Gerusalemme → Deserto di Giuda / Tasca Mar Morto.

### Test 2.1: Ricerca e Visita dei 6 Biomi Israeliani
* **Cosa testare:** Generazione naturale e identificazione F3 di tutti e 6 i biomi dedicati.
* **Come testare:**
  1. Esegui `/locate biome israel_simulator:mediterranean_coast` e teletrasportati.
  2. Esegui `/locate biome israel_simulator:urban_area` e teletrasportati.
  3. Esegui `/locate biome israel_simulator:israeli_agriculture` e teletrasportati.
  4. Esegui `/locate biome israel_simulator:jerusalem` e teletrasportati.
  5. Esegui `/locate biome israel_simulator:judean_desert` e teletrasportati.
  6. Esegui `/locate biome israel_simulator:dead_sea` e teletrasportati.
* **Expected Result:**
  - Tutti i 6 biomi vengono localizzati senza errori di generazione o crash.
  - La schermata F3 mostra il nome esatto del bioma `israel_simulator:<nome>`.
  - Non compaiono buchi di chunk o chunk con blocchi viola/neri mancanti.

### Test 2.2: Contiguità Climatica Gerusalemme → Mar Morto
* **Cosa testare:** Il Mar Morto deve trovarsi in continuità ad est di Gerusalemme, anche su seed come `42`.
* **Come testare:**
  1. Crea un mondo con seed `42`.
  2. Teletrasportati a Gerusalemme (`/locate biome israel_simulator:jerusalem`).
  3. Spostati verso est (coordinate X crescenti) volando o con bussola/F3.
* **Expected Result:**
  - Il bioma Gerusalemme transita dolcemente nel Deserto di Giuda e/o nelle basse quote del Mar Morto, senza salti climatici innaturali (es. ghiacciai o giungle improvvise).

---

## 3. Le 16 Strutture Reali e Città

Nessuna struttura deve presentarsi come villaggio vanilla o scatola vuota 7x7.

### Elenco Strutture e Comandi di Collaudo:

| Struttura | ID Struttura | Bioma Dedicato | Dimensioni / Elemento Chiave |
|---|---|---|---|
| **Città Vecchia di Gerusalemme** | `jerusalem_city` | `jerusalem` | Multi-pezzo: piazza, shuk, sinagoga a cupola, mura |
| **Western Wall (Muro del Pianto)** | `western_wall` | `jerusalem` | 56x20x42, facciata in pietra erodiana, mechitza |
| **Sinagoga Comunitaria** | `synagogue` | `jerusalem` | Bimah, banchi, aron kodesh con cassa `synagogue_ark` |
| **Grande Sinagoga Monumentale** | `great_synagogue` | `jerusalem` | 36x18x36, loggiato, navata a doppia altezza, arca dorata |
| **Casa Storica di Gerusalemme** | `historical_house` | `jerusalem` | 24x11x24, cortile alberato (olivo), camino, cassa loot |
| **Gran Mercato dello Shuk** | `grand_market` | `jerusalem` | 32x12x30, banchi colorati, spezie, fontana centrale |
| **Metropoli di Tel Aviv** | `tel_aviv_city` | `urban_area` | 48x18x48, 6 settori per i 6 quartieri urbani |
| **Ufficio Startup Tech** | `startup_office` | `urban_area` | 26x14x26, sale server, postazioni developer, open space |
| **Palazzo Governativo** | `government_building` | `urban_area` | 28x14x26, colonne in quarzo, podio assemblea, archivi |
| **Porto Antico di Jaffa** | `jaffa_port` | `mediterranean_coast` | Molo in pietra, magazzini portuali, fari, banchine |
| **Villaggio Mediterraneo** | `mediterranean_village` | `mediterranean_coast` | Case bianche/azzurre, uliveti costieri, stradine |
| **Fattoria Agricola del Kibbutz** | `agricultural_farm` | `israeli_agriculture` | 48x10x40, serre, campi irrigati, fienile, aranceti |
| **Resort del Mar Morto** | `dead_sea_resort` | `dead_sea` | Struttura termale, vasche di fango, ombrelloni |
| **Oasi di Ein Gedi** | `ein_gedi_oasis` | `judean_desert` | Bacino d'acqua sorgiva nel canyon, canneti, palme |
| **Rovine del Deserto** | `desert_ruins` | `judean_desert` | Antiche fortificazioni di pietra scalfita dal vento |
| **Santuario Antico nel Deserto** | `ancient_sanctuary` | `judean_desert` | 30x12x34, Santo dei Santi, cassa ultra-rara Rabbi's Crown |

### Procedura di Collaudo per ogni struttura:
1. Esegui `/locate structure israel_simulator:<id>`.
2. Teletrasportati alle coordinate indicate e osserva l'ambientazione.
3. Ispeziona gli interni: cerca le casse con loot dedicato e i blocchi caratteristici.
* **Expected Result:**
  - La struttura genera con adattamento al terreno corretto (`beard_box` o `beard_thin`), senza restare sospesa nel vuoto.
  - Le casse contengono loot autentico (shekel, cibo israeliano, rotoli, libri o manufatti).
  - Lo scavo dei blocchi personalizzati (es. `western_wall_stone`, `jerusalem_stone`, `salt_block`, `paved_road`, `transport_stop`) rilascia il blocco corrispondente.

---

## 4. Western Wall (Muro del Pianto) & Rituale di Preghiera

### Test 4.1: Condizioni di Preghiera e Posa Animata
* **Cosa testare:** Requisiti di preghiera, animazione in terza persona (capo chino e braccio teso), consumo del foglietto e cooldown.
* **Come testare:**
  1. Mettiti in modalità `/gamemode survival`.
  2. Prova a cliccare con il tasto destro su una `western_wall_stone` a testa nuda.
     - **Expected Result:** Messaggio di errore *"You must wear a Kippah to pray at the Western Wall"* e suono di diniego.
  3. Indossa la Kippah (`/give @p israel_simulator:kippah`) ma non tenere alcuna Prayer Note.
     - **Expected Result:** Messaggio di errore che richiede una Prayer Note per pregare.
  4. Tieni in mano una `prayer_note` (`/give @p israel_simulator:prayer_note 1`). Clicca col tasto destro sul Muro.
     - **Expected Result:**
       - Inizia la sessione di preghiera (durata 3 secondi / 60 tick).
       - In visuale terza persona (F5), il personaggio abbassa il capo a 35° e distende le braccia verso il muro.
       - La visuale in prima persona si orienta dolcemente verso il muro.
  5. Attendi il termine dei 3 secondi senza muoverti:
     - **Expected Result:**
       - La Prayer Note viene consumata.
       - Vengono conferiti **5 Diamanti**.
       - Si sentono le particelle e il suono `LEVELUP`.
       - Viene impostato il cooldown server-side di 24.000 tick (1 giorno di Minecraft).
  6. Riprova a pregare immediatamente dopo:
     - **Expected Result:** La preghiera viene rifiutata con il messaggio che segnala il cooldown attivo.

### Test 4.2: Annullamento per Movimento
* **Cosa testare:** Se il giocatore si allontana durante la preghiera, la sessione deve annullarsi senza premi.
* **Come testare:**
  1. Inizia a pregare con Kippah e Prayer Note.
  2. Cammina o salta via durante i 3 secondi.
* **Expected Result:**
  - La sessione si interrompe immediatamente.
  - Nessun diamante viene conferito e la Prayer Note non viene consumata.

---

## 5. Equipaggiamento e Oggetti Culturali / Religiosi

### Test 5.1: Kippah, Talit e Tefillin
* **Cosa testare:** Slot corretto di equipaggiamento, render 3D e rituale mattutino dei Tefillin.
* **Come testare:**
  1. Equipaggia la `kippah`: va posizionata nello slot elmo (`HEAD`). Verifica che il modello 3D compaia sulla testa.
  2. Equipaggia il `talit`: va posizionato nello slot corazza (`CHEST`). Verifica il render e le frange (tzitzit).
  3. Prendi i `tefillin` nella mano principale alle prime ore del mattino (tempo gioco compreso tra 0 e 6000 tick) e clicca tasto destro:
     - **Expected Result:** Il rito viene completato con successo, conferendo l'effetto *Blessed* e particelle di benedizione.
  4. Prova a usare i `tefillin` durante la notte o di pomeriggio:
     - **Expected Result:** Rifiuto del rito (i Tefillin sono per la preghiera mattutina nei giorni feriali).

### Test 5.2: Corona del Rabbino (Rabbi's Crown) — Item Mythic
* **Cosa testare:** Valore armatura (+20), render 3D con barba e payot, status *Blessed Trader*.
* **Come testare:**
  1. Ottieni la corona con `/give @p israel_simulator:rabbis_crown`.
  2. Indossala nello slot testa.
* **Expected Result:**
  - La barra dell'armatura si riempie immediatamente (+20 punti armatura).
  - Il render 3D in F5 mostra il copricapo tradizionale arricchito da barba e payot laterali.
  - Lo status *Blessed Trader* è attivo: i commerci con i villager offrono uno sconto immediato del 15%.

### Test 5.3: Menorah
* **Cosa testare:** Piazzamento, accensione candele da 1 a 8, illuminazione progressiva.
* **Come testare:**
  1. Piazza una `israel_simulator:menorah` a terra.
  2. Clicca con tasto destro tenendo un acciarino (`flint_and_steel`) o torcia ripetutamente per 8 volte.
* **Expected Result:**
  - A ogni click il numero di fiammelle accese sale da 1 fino a 8 (più Shamash).
  - Il livello di luce emesso dal blocco sale progressivamente fino al valore massimo di 15.
  - Rompendo il blocco con piccone o a mano, viene rilasciata la Menorah.

---

## 6. Quartieri di Tel Aviv & Dinamiche Urbane

Nel bioma `israel_simulator:urban_area`, lo spazio è suddiviso in 6 quartieri: *White City*, *Rothschild*, *Florentin*, *Sarona*, *Startup District*, *Tayelet Beach*.

### Test 6.1: Riconoscimento Spaziale dei Quartieri
* **Cosa testare:** Cambio quartiere mostrato nell'Action Bar del giocatore e moltiplicatori economici.
* **Come testare:**
  1. Teletrasportati a Tel Aviv (`/locate structure israel_simulator:tel_aviv_city`).
  2. Cammina attraverso la città tra i vari settori (es. muoviti dalla spiaggia a ovest verso la torre tecnologica a est).
* **Expected Result:**
  - Ogni volta che si oltrepassa il confine di una cella del quartiere, sopra la hotbar (Action Bar) compare la notifica stilizzata, ad esempio:  
    `Entering Startup District - High-Tech Hub` oppure `Entering Tayelet Beach - Coastal Promenade`.
  - Nessuno spam continuo: la notifica si attiva solo quando cambia quartiere.

---

## 7. Agricoltura, Raccolti e Ricette Gastronomiche

### Test 7.1: Raccolto degli Alberi Fruttiferi & Vite
* **Cosa testare:** Raccolta di olive, datteri, agrumi e uva dai rispettivi blocchi.
* **Come testare:**
  1. Piazza foglie di olivo (`olive_leaves`), palma da dattero (`date_palm_leaves`) e agrumi (`citrus_leaves`).
  2. Piazza una vite (`israel_simulator:grapevine`) su terreno coltivato e porta lo stadio di crescita al massimo con farina d'ossa (`bone_meal`).
  3. Clicca con tasto destro a maturità.
* **Expected Result:**
  - La vite matura rilascia `israel_simulator:grapes` (uva), e NON grano o datteri.
  - Le foglie da frutto rilasciano i rispettivi frutti (`olives`, `dates`, `citrus`).

### Test 7.2: Preparazione Ricette al Banco da Lavoro
* **Cosa testare:** Realizzazione delle ricette tipiche e consumazione dei cibi.
* **Come testare:**
  1. Apri un banco da lavoro e crafta:
     - **Tahini:** 2x semi di grano + 1x ciotola.
     - **Hummus:** 1x tahini + 1x barbabietola + 1x ciotola.
     - **Falafel:** 1x semi + 1x grano + 1x barbabietola (produce 3 falafel).
     - **Shakshuka:** 1x uovo + 1x barbabietola + 1x ciotola.
     - **Challah:** 3x grano + 1x uovo + 1x zucchero.
     - **Matzo:** 2x grano + 1x secchio d'acqua (il secchio vuoto viene restituito).
     - **Sufganiyah:** 1x grano + 1x zucchero + 1x bacche dolci.
     - **Hamantash:** 1x grano + 1x zucchero + 1x datteri.
  2. Mettiti in survival (`/gamemode survival`), abbassa la fame correndo e mangia ciascuno dei cibi.
* **Expected Result:**
  - Tutte le ricette producono il cibo previsto.
  - Tutti i cibi ripristinano correttamente fame e saturazione conformemente alle proprietà alimentari registrate.

---

## 8. Economia Regionale, Commercio & Valuta (Shekel / Agorot)

### Test 8.1: Baratto di Raccolti nel Bioma Agricolo
* **Cosa testare:** I contadini del bioma agricolo acquistano grano/ortaggi pagando in Shekel/Agorot, mentre fuori da quel bioma il commercio vanilla resta intatto.
* **Come testare:**
  1. Genera un villager in `israel_simulator:israeli_agriculture`. Tieni in mano 20 carote o grano e clicca tasto destro sul villager:
     - **Expected Result:** Il contadino acquista il raccolto, compare il messaggio di ricompensa e ti consegna Shekel o Agorot sonanti.
  2. Genera un villager in una pianura vanilla (`minecraft:plains`). Tieni in mano grano e clicca tasto destro:
     - **Expected Result:** Non avviene il baratto automatico del mod; si apre regolarmente la GUI vanilla di scambio del villager.

### Test 8.2: Acquisto di Giudaica a Gerusalemme
* **Cosa testare:** Cliccando su un villager a Gerusalemme con Shekel in mano si acquistano oggetti sacri.
* **Come testare:**
  1. Teletrasportati a Gerusalemme (`/locate biome israel_simulator:jerusalem`).
  2. Tieni in mano 4 `shekel` e clicca tasto destro su un villager:
     - **Expected Result:** Ricevi 1 `kippah` e 4 shekel vengono scalati.
  3. Tieni in mano 1 `shekel` e clicca:
     - **Expected Result:** Ricevi 1 `prayer_note`.
  4. Tieni in mano 12 o più `shekel`, premi `Shift` (accovacciati) e clicca:
     - **Expected Result:** Ricevi 1 `tefillin`.

### Test 8.3: Sconto Blessed Trader
* **Cosa testare:** Quando si indossa la `rabbis_crown` o si ha l'effetto *Blessed*, i commercianti applicano il 15% di sconto.
* **Come testare:**
  1. Indossa la corona del rabbino.
  2. Interagisci con un NPC commerciale di Tel Aviv o dello Shuk.
* **Expected Result:** Messaggio *"Blessed Trader recognized! Special 15% discount applied"*, con prezzi ribassati rispetto al listino standard.

---

## 9. Rete dei Trasporti (Bicicletta, Fermate, Rav-Kav, Scarpe)

### Test 9.1: Strada Pavimentata (Paved Road)
* **Cosa testare:** Incremento di velocità pedonale camminando sul blocco `paved_road`.
* **Come testare:**
  1. Piazza una striscia di blocchi `israel_simulator:paved_road`.
  2. Cammina sopra di essi.
* **Expected Result:** Viene applicato l'effetto *Speed I* per 3 secondi. Scavando il blocco con piccone, viene droppato il blocco `paved_road`.

### Test 9.2: Bicicletta Cavalacabile
* **Cosa testare:** Piazzamento, guida e campanello.
* **Come testare:**
  1. Piazza l'oggetto `israel_simulator:bicycle` a terra.
  2. Clicca con tasto destro per salirci a cavallo e guidala con WASD.
  3. Premi Spazio mentre guidi:
     - **Expected Result:** La bicicletta suona il campanello (`entity.bicycle.bell`).
  4. La velocità di movimento è sensibilmente superiore alla corsa pedonale.

### Test 9.3: Fermate dei Trasporti & Routing Circolare Dinamico
* **Cosa testare:** Teletrasporto tra fermate tramite Rav-Kav o Shekel, determinazione della destinazione in base alla fermata locale.
* **Circuito delle fermate:**  
  `Tel Aviv Central (100, 70, 100)` → `Jaffa Clock Tower (150, 68, 500)` → `Jaffa Port (120, 64, 600)` → `Jerusalem Navon (800, 110, 800)` → `Dead Sea (1500, 45, 1200)` → `Galilee Hub (-600, 75, -800)` → loop su `Tel Aviv Central`.
* **Come testare:**
  1. Piazza un blocco `israel_simulator:transport_stop` a Gerusalemme (es. vicino a Navon ~[800, 110, 800]).
  2. Tieni in inventario una carta `rav_kav` (o 10 Shekel).
  3. Clicca con tasto destro sul blocco della fermata:
     - **Expected Result:**
       - Il blocco rileva la vicinanza a Gerusalemme Navon e calcola la fermata successiva: **Dead Sea Ein Gedi Stop**.
       - Si ode il suono di transito (`BUS` o `TRAIN`).
       - Il giocatore viene teletrasportato a destinazione con la notifica verde nella chat:  
         `Boarded transit to Dead Sea Ein Gedi Stop! (Rav-Kav Pass)`.
  4. Riprova a cliccare subito dopo il teletrasporto:
     - **Expected Result:** Messaggio giallo di cooldown: *"Transit cooldown: wait 3s before boarding again"*.

---

## 10. Mar Morto & Pericoli del Deserto di Giuda

### Test 10.1: Galleggiamento Spontaneo (Buoyancy) nel Mar Morto
* **Cosa testare:** Nel bioma `israel_simulator:dead_sea`, entrare in acqua non fa affondare.
* **Come testare:**
  1. Teletrasportati nel bioma del Mar Morto (`/locate biome israel_simulator:dead_sea`).
  2. Tuffati nell'acqua salata senza toccare alcun tasto.
* **Expected Result:**
  - Il giocatore galleggia naturalmente in superficie senza affondare, spinto dolcemente verso l'alto.

### Test 10.2: Fango del Mar Morto (Dead Sea Mud)
* **Cosa testare:** Applicazione benefica del fango curativo.
* **Come testare:**
  1. Raccogli o ottieni `israel_simulator:dead_sea_mud`.
  2. Clicca con tasto destro per spalmarlo sul corpo.
* **Expected Result:** Il fango si consuma e conferisce gli effetti *Regeneration* e *Absorption*.

### Test 10.3: Colpo di Calore nel Deserto di Giuda
* **Cosa testare:** Esposizione al sole cocente nel deserto senza copricapo.
* **Come testare:**
  1. Vai nel bioma `israel_simulator:judean_desert` a mezzogiorno (`/time set 6000`).
  2. Rimuovi qualsiasi copricapo e cammina all'aperto sotto la luce diretta del sole.
* **Expected Result:** Dopo alcuni secondi di esposizione, la barra della fame cala rapidamente e può subentrare nausea/affaticamento. Indossare una `kippah` o stare all'ombra protegge dal colpo di calore.

---

## 11. Boss Endgame: Bibi Boss & Coalition Guards

### Test 11.1: Evocazione e Fasi del Combattimento
* **Cosa testare:** Punti vita (10.000 HP), limitatore anti-one-shot (500 max damage per hit), evocazione guardie, fase Enrage e drop finale.
* **Come testare:**
  1. Evoca il boss in un'area aperta: `/summon israel_simulator:bibi_boss`.
  2. Osserva il boss:
     - **Expected Result:** Compare con la barra del boss viola denominata **"Bibi, Master of Coalitions"** con 10.000 HP e la skin in giacca e cravatta.
  3. Attacca il boss con una spada potenziata:
     - **Expected Result:** Nessun attacco singolo può infliggere più di 500 danni (prevenzione cheese).
     - Durante la battaglia, il boss evoca fino a 4 `bibi_guard` (Coalition Guards in abito scuro) che difendono il leader.
     - Vengono riprodotti suoni di discorso satirico (`entity.bibi_boss.speech`).
  4. Riduci la salute del boss sotto il 30% (3.000 HP):
     - **Expected Result:** Il boss entra nella fase **Enraged**: velocizzato, con particelle di fumo scuro e danno d'attacco aumentato.
  5. Sconfiggi il boss:
     - **Expected Result:**
       - Suono di sconfitta epico.
       - Sblocco dell'avanzamento `"Hava Nagila"`.
       - Rilascio garantito del disco musicale leggendario `israel_simulator:hava_nagila_disc` e di una consistente quantità di Shekel.

### Test 11.2: Riproduzione del Disco Hava Nagila
* **Cosa testare:** Ascolto del disco nel Jukebox.
* **Come testare:**
  1. Piazza un `jukebox` vanilla.
  2. Inserisci il disco `israel_simulator:hava_nagila_disc`.
* **Expected Result:** Viene riprodotto il brano integrale di Hava Nagila (`assets/israel_simulator/sounds/records/hava_nagila.ogg`) in streaming limpido con notifica su schermo `"Now Playing: Hava Nagila"`.

---

## 12. Feste Ebraiche, Calendario & Mini-giochi (Dreidel, Shofar, Menorah)

### Test 12.1: Mini-gioco del Dreidel
* **Cosa testare:** Rotazione della trottola a 4 facce con esito casuale (Nun, Gimel, Hei, Shin).
* **Come testare:**
  1. Tieni in mano `israel_simulator:dreidel` con almeno 10 `shekel` nell'inventario.
  2. Clicca con tasto destro per far roteare il dreidel.
* **Expected Result:**
  - Chat notification con la lettera estratta e l'esito:
    - **Nun (נ):** Nisht (Nulla accade).
    - **Gimel (ג):** Gantz (Vincita del piatto, ricevi Shekel extra).
    - **Hei (ה):** Halb (Metà piatto vinto).
    - **Shin (ש):** Shtel (Metti uno Shekel nel piatto).

### Test 12.2: Suono dello Shofar
* **Cosa testare:** Squillo del corno rituale, effetto acustico e rispetto del cooldown da file di configurazione (`shofarCooldownTicks = 600`).
* **Come testare:**
  1. Clicca con tasto destro tenendo `israel_simulator:shofar`.
* **Expected Result:** Viene emesso il caratteristico squillo di tromba/corno. Un secondo tentativo immediato viene respinto fino allo scadere dei 30 secondi (600 tick).

---

## 13. Tracciamento Esplorativo, Mappa d'Israele & Reputazione

### Test 13.1: Rilevamento Landmark & Mappa d'Israele
* **Cosa testare:** Scoperta proattiva dei luoghi storici camminando entro 48 blocchi da essi, fanfare di scoperta e aggiornamento della mappa.
* **Come testare:**
  1. Tieni nell'inventario `israel_simulator:israel_map`.
  2. Avvicinati alle coordinate di un landmark (es. Western Wall a Gerusalemme o la Torre dell'Orologio a Jaffa).
* **Expected Result:**
  - Quando entri nel raggio di 48 blocchi:
    - Risuona la fanfara di scoperta (`ui.landmark_discovered`).
    - Compare il toast/messaggio dorato nella chat: `★ Landmark Discovered: Western Wall (Jerusalem)`.
    - Viene assegnata esperienza (+100 XP) e l'avanzamento di scoperta (es. `1/8 landmarks discovered`).
    - Cliccando con la mappa in mano, la descrizione elenca i luoghi storici scoperti.

### Test 13.2: Fazioni e Reputazione
* **Cosa testare:** Variazione della reputazione tra le 5 fazioni (*City*, *Religious*, *Merchant*, *Tech District*, *Village*).
* **Come testare:**
  1. Prega al Muro del Pianto o alla Sinagoga per aumentare la fazione *Religious*.
  2. Concludi scambi tecnologici a Tel Aviv per aumentare la fazione *Tech District*.
* **Expected Result:** I mercanti appartenenti a quelle fazioni salutano il giocatore con battute amichevoli e applicano prezzi di favore commisurati al tier di reputazione raggiunto.

---

## 14. Persistenza su Server Dedicato & Anti-Exploit

Tutti gli stati di gioco critici devono essere salvati nei file di livello (`SavedData` sul server) e non in semplici mappe RAM statiche.

### Test 14.1: Persistenza al Riavvio del Server
* **Cosa testare:** I cooldown di preghiera, transito e reputazione non si azzerano riavviando il server.
* **Come testare:**
  1. Avvia un server dedicato con `runServer`.
  2. Connettiti con un client, indossa la Kippah e prega al Western Wall ricevendo i 5 diamanti.
  3. Esegui `/stop` sulla console del server per salvare e chiudere il server.
  4. Riavvia il server con `runServer` e riconnettiti.
  5. Prova a pregare nuovamente al Western Wall.
* **Expected Result:**
  - La preghiera viene **respinta** per cooldown ancora attivo: il tempo rimanente è stato preservato nel salvataggio del mondo (`israel_simulator:western_wall_prayers.dat`).
  - È matematicamente impossibile duplicare diamanti riavviando il server.

### Test 14.2: Anti-Arbitraggio Economico
* **Cosa testare:** Il prezzo di acquisto (buy price) supera sempre rigorosamente il prezzo di vendita (sell price) per qualsiasi merce.
* **Come testare:**
  1. Vendi 10 Falafel a un mercante.
  2. Prova a ricomprarli immediatamente dallo stesso mercante.
* **Expected Result:** Il costo per ricomprare gli oggetti è superiore al ricavo ottenuto vendendoli. Impossibile generare valuta infinita tramite compravendita ciclica.

---

## Tabella Rapida di Riferimento Comandi (Cheat Sheet)

| Azione desiderata | Comando Minecraft in-game |
|---|---|
| Trova Città Vecchia di Gerusalemme | `/locate structure israel_simulator:jerusalem_city` |
| Trova Muro del Pianto | `/locate structure israel_simulator:western_wall` |
| Trova Metropoli di Tel Aviv | `/locate structure israel_simulator:tel_aviv_city` |
| Trova Porto Antico di Jaffa | `/locate structure israel_simulator:jaffa_port` |
| Trova Fattoria Kibbutz | `/locate structure israel_simulator:agricultural_farm` |
| Trova Grande Sinagoga | `/locate structure israel_simulator:great_synagogue` |
| Trova Resort del Mar Morto | `/locate structure israel_simulator:dead_sea_resort` |
| Trova Oasi di Ein Gedi | `/locate structure israel_simulator:ein_gedi_oasis` |
| Trova Santuario Antico (Deserto) | `/locate structure israel_simulator:ancient_sanctuary` |
| Trova Bioma Mar Morto | `/locate biome israel_simulator:dead_sea` |
| Trova Bioma Urbano Tel Aviv | `/locate biome israel_simulator:urban_area` |
| Evoca il Bibi Boss | `/summon israel_simulator:bibi_boss` |
| Ottieni Kit Preghiera Completo | `/give @p israel_simulator:kippah`<br>`/give @p israel_simulator:prayer_note 16` |
| Ottieni Corona Mythic | `/give @p israel_simulator:rabbis_crown` |
| Ottieni Carta Trasporti & Bici | `/give @p israel_simulator:rav_kav`<br>`/give @p israel_simulator:bicycle` |
| Ottieni Disco Hava Nagila | `/give @p israel_simulator:hava_nagila_disc` |
