# Israel-Simulator — Playtesting Guide & Checklist

> Guida operativa e checklist di playtesting per la mod Minecraft Israel-Simulator (NeoForge 26.2 / MC 26.2).
>
> Questo documento fornisce scenari di test dettagliati, comandi di verifica in-game, passaggi riproducibili e criteri di accettazione per validare il gameplay su client singolo e server multigiocatore.

---

## Indice

- [0. Setup & Istruzioni per il Playtest](#0-setup--istruzioni-per-il-playtest)
- [1. Generazione del Mondo & Biomi](#1-generazione-del-mondo--biomi)
- [2. Esplorazione Strutture & Worldgen](#2-esplorazione-strutture--worldgen)
- [3. Torri Gemelle & Striscia di Gaza](#3-torri-gemelle--striscia-di-gaza)
- [4. NPC, Abitanti & Vita Cittadina](#4-npc-abitanti--vita-cittadina)
- [5. Shabbat & Calendario Festività](#5-shabbat--calendario-festività)
- [6. Oggetti Culturali, Candelabro & Equipaggiamento](#6-oggetti-culturali-candelabro--equipaggiamento)
- [7. Agricoltura, Cibo & Economia](#7-agricoltura-cibo--economia)
- [8. Boss Fight: Bibi Boss & Minion](#8-boss-fight-bibi-boss--minion)
- [9. Trasporti & Mobilità](#9-trasporti--mobilità)
- [10. UI, Grafica, Suoni & Schermate Config](#10-ui-grafica-suoni--schermate-config)
- [11. Multigiocatore, Sincronizzazione & Anti-Exploit](#11-multigiocatore-sincronizzazione--anti-exploit)
- [12. Registro Bug & Note di Playtest](#12-registro-bug--note-di-playtest)

---

## 0. Setup & Istruzioni per il Playtest

Prima di iniziare il test, avviare il gioco con uno dei seguenti metodi:
* **Client di sviluppo**: `./gradlew.bat runClient`
* **Server dedicato di sviluppo**: `./gradlew.bat runServer`

### Comandi utili per il tester
```text
/gamemode creative | survival
/locate biome israel_simulator:<bioma>
/locate structure israel_simulator:<struttura>
/tp @s <x> <y> <z>
/time set day | night | 13000
/weather clear
```

- [ ] Verificare che il gioco si avvii senza crash e senza errori critici nel log (`run/logs/latest.log`).
- [ ] Verificare che la creazione di un nuovo mondo proceda fino al 100% senza blocchi o schermate di errore datapack.
- [ ] Verificare che gazzelle di montagna e iraci compaiano con gruppi contenuti su costa/colline e che stambecchi nubiani e iraci compaiano nel deserto, sul territorio del Mar Morto e sulle colline di Gerusalemme; nessun animale terrestre deve comparire nell'acqua del Mar Morto o nell'isola tropicale.
- [ ] Avvicinarsi agli animali e colpirli: devono fuggire senza attaccare; nutrire due adulti con grano o carote per verificare la riproduzione e controllare i drop di carne e l'uso degli spawn egg.

---

## 1. Generazione del Mondo & Biomi

Verificare che la fascia climatica dei biomi israeliani si generi correttamente e senza artefatti visivi.

### 1.1 Bioma: Costa Mediterranea (`israel_simulator:mediterranean_coast`)
* **Comando:** `/locate biome israel_simulator:mediterranean_coast`
- [ ] La costa si genera adiacente al mare con sabbia, palme e transizioni morbide verso l'entroterra.
- [ ] Tartarughe marine, merluzzi e delfini compaiono negli habitat costieri/acquatici; le creature acquatiche non appaiono sulla terraferma.
- [ ] Le creature vanilla mantengono i propri loot table e rilasciano gli oggetti previsti una sola volta.
- [ ] Nessuna presenza anomala di arenaria esposta fuori dai parametri previsti.

### 1.2 Bioma: Agricoltura (`israel_simulator:israeli_agriculture`)
* **Comando:** `/locate biome israel_simulator:israeli_agriculture`
- [ ] Campi verdi aperti con terreno fertile e vegetazione coltivabile.
- [ ] Coltivazioni di uva, grano e ulivi si integrano naturalmente con il terreno.
- [ ] Pecore, bovini, polli, capre e conigli compaiono come fauna/agricoltura locale; non compaiono pesci tropicali o delfini nell'entroterra.
- [ ] Le gazzelle di montagna si incontrano raramente ai margini dei campi e fuggono quando minacciate.
- [ ] Ceci, pomodori, melanzane e sesamo crescono su terreno zappato, mostrano fasi di crescita e rilasciano raccolti piantabili.
- [ ] I forzieri di fattoria e mercato possono fornire i raccolti iniziali da ripiantare.

### 1.3 Bioma: Deserto Giudaico (`israel_simulator:judean_desert`)
* **Comando:** `/locate biome israel_simulator:judean_desert`
- [ ] Terreno caratteristico in sabbia rossa (`red_sand`) e arenaria rossa (`red_sandstone`).
- [ ] Formazioni rocciose e colline tipiche dell'ambiente desertico.
- [ ] Cammelli, conigli, volpi e capre compaiono con gruppi contenuti.
- [ ] Stambecchi nubiani e iraci del Capo compaiono in gruppi piccoli tra aree rocciose e terreno desertico.

### 1.4 Bioma: Mar Morto (`israel_simulator:dead_sea`)
* **Comando:** `/locate biome israel_simulator:dead_sea`
- [ ] Acqua ad altissima salinità con formazioni e croste saline lungo la riva (`israel_simulator:salt_block`).
- [ ] Il nuoto nel Mar Morto applica l'effetto di galleggiamento accentuato e resistenza.
- [ ] Nessun albero convenzionale presente nelle immediate vicinanze dell'acqua salata.
- [ ] Volpi e capre possono comparire sulle sponde; nessun pesce, delfino o altra fauna acquatica compare nell'acqua ipersalina.
- [ ] Stambecchi nubiani e iraci del Capo si limitano alla terraferma rocciosa e non compaiono nell'acqua.

### 1.5 Bioma: Area Urbana (`israel_simulator:urban_area`)
* **Comando:** `/locate biome israel_simulator:urban_area`
- [ ] Terreno pianeggiante con asfalto/strade lastricate (`israel_simulator:paved_road`).
- [ ] Presenza di parchi urbani, arredo stradale e transizione graduale verso Tel Aviv.
- [ ] Gatti e polli sono presenti nel tessuto urbano; la volpe è rara e non domina le aree costruite.

### 1.6 Bioma: Gerusalemme (`israel_simulator:jerusalem`)
* **Comando:** `/locate biome israel_simulator:jerusalem`
- [ ] Superficie naturale in pietra di Gerusalemme (`israel_simulator:jerusalem_stone`).
- [ ] Colline e dislivelli che rispecchiano la topografia storica della città.
- [ ] Pecore, gatti e capre compaiono sulle colline senza sovrappopolare le strade.
- [ ] Gazzelle, stambecchi nubiani e iraci del Capo compaiono raramente sulle colline e scappano dai giocatori.

### 1.7 Bioma: Isola Tropicale (`israel_simulator:tropical_island`)
* **Comando:** `/locate biome israel_simulator:tropical_island`
- [ ] Isola remota in oceano caldo profondo con spiagge di sabbia bianca e palme tropicali.
- [ ] Al centro sorge la tenuta con padiglione a cupola dorata a strisce.
- [ ] Pappagalli, ocelot, tartarughe, pesci tropicali e delfini compaiono negli habitat corretti dell'isola e dell'oceano.

---

## 2. Esplorazione Strutture & Worldgen

Verificare la presenza, la coerenza architettonica e il corretto riempimento delle casse delle 17 strutture storiche e urbane.

| Struttura | Comando di localizzazione | Bioma Atteso |
| :--- | :--- | :--- |
| **Città di Gerusalemme** | `/locate structure israel_simulator:jerusalem_city` | Gerusalemme |
| **Muro del Pianto** | `/locate structure israel_simulator:western_wall` | Gerusalemme |
| **Grande Sinagoga** | `/locate structure israel_simulator:great_synagogue` | Gerusalemme |
| **Sinagoga Storica** | `/locate structure israel_simulator:synagogue` | Gerusalemme |
| **Casa Storica** | `/locate structure israel_simulator:historical_house` | Gerusalemme |
| **Gran Mercato** | `/locate structure israel_simulator:grand_market` | Gerusalemme |
| **Città di Tel Aviv** | `/locate structure israel_simulator:tel_aviv_city` | Area Urbana |
| **Ufficio Startup** | `/locate structure israel_simulator:startup_office` | Area Urbana |
| **Palazzo del Governo** | `/locate structure israel_simulator:government_building` | Area Urbana |
| **Porto di Giaffa** | `/locate structure israel_simulator:jaffa_port` | Costa Mediterranea |
| **Villaggio Mediterraneo**| `/locate structure israel_simulator:mediterranean_village` | Costa Mediterranea |
| **Fattoria Agricola** | `/locate structure israel_simulator:agricultural_farm` | Agricoltura |
| **Resort del Mar Morto**| `/locate structure israel_simulator:dead_sea_resort` | Mar Morto |
| **Rovine del Deserto** | `/locate structure israel_simulator:desert_ruins` | Deserto Giudaico |
| **Oasi di Ein Gedi** | `/locate structure israel_simulator:ein_gedi_oasis` | Deserto Giudaico |
| **Antico Santuario** | `/locate structure israel_simulator:ancient_sanctuary` | Deserto Giudaico |
| **Isola Tropicale** | `/locate structure israel_simulator:tropical_island` | Isola Tropicale |

### Checklist Strutture
- [ ] Tutte le strutture si fondono correttamente con il terreno circostante senza tagli netti (chunk borders).
- [ ] Porte, vetrate e recinzioni sono nello stato di connessione corretto (nessuna porta volante o vetro disconnesso).
- [ ] Le casse all'interno contengono il loot corretto e non sono vuote.
- [ ] **Muro del Pianto:** Interagire con un blocco del muro tenendo in mano un foglietto di carta (`minecraft:paper`) inserisce una preghiera, applica un buff di benedizione e avvia il cooldown server-side.

---

## 3. Torri Gemelle & Striscia di Gaza

Verifica specifica delle strutture aggiunte e aggiornate di recente.

### 3.1 Torri Gemelle (`israel_simulator:twin_towers`)
* **Comando:** `/locate structure israel_simulator:twin_towers`
- [ ] La struttura spawna nel bioma delle pianure (`minecraft:plains`).
- [ ] Le due torri si ergono per oltre 180 blocchi di altezza mantenendo l'architettura esterna in quarzo, pietra levigata e vetrate.
- [ ] **Mob Spawner:** Verificare che all'interno di entrambe le torri, ai diversi piani ($Y=12, 22, 32, \dots$), siano presenti spawner funzionanti di:
  - [ ] Zombie (`minecraft:zombie`)
  - [ ] Scheletri (`minecraft:skeleton`)
  - [ ] Ragni (`minecraft:spider` / `minecraft:cave_spider`)
- [ ] **Casse con Loot:** Le casse accanto agli spawner contengono il loot della tabella `twin_towers.json` (ferro, oro, smeraldi, diamanti, mele d'oro, libri incantati, falafel).
- [ ] Nessun blocco di arenaria compare nella struttura.
- [ ] **Aereo ogni minuto:** con un giocatore entro 160 blocchi, ogni 60 s parte un aereo da fuori della struttura (48-96 blocchi oltre il bordo), diretto al centro di una delle due torri scelta a caso; colpisce la torre ed esplode. Mai più di un aereo in volo per struttura; se non colpisce sparisce dopo 30 s. Esplosioni, fuoco e rottami restano dentro la torre colpita. Comandi: `/israelsim event plane_crash start|stop`.

### 3.2 Striscia di Gaza (`israel_simulator:gaza_strip`)
* **Comando:** `/locate structure israel_simulator:gaza_strip` (solo nel bioma `mediterranean_coast`)
- [ ] È un quartiere in rovina enorme (di solito 100-160 blocchi per lato, circa 10 volte la vecchia struttura da 32×32), con strade di macerie, incroci, vicoli e posti di blocco.
- [ ] Non ci sono case intatte: solo fondamenta a raso terra con macerie e sbarre di ferro, cortili di macerie, tratti di mura con brecce e compound in rovina.
- [ ] La piazza centrale ha il pozzo prosciugato con i pilastri spezzati.
- [ ] **Torri di guardia:** di altezze diverse (fino a 15 blocchi), con scale interne, feritoie in sbarre di ferro e merlature in parte crollate.
- [ ] **Casualità:** due Striscie di Gaza (seed diversi o la stessa mappa) non sono mai uguali: cambiano la pianta, le varianti dei pezzi, la rotazione, i materiali e i blocchi crollati.
- [ ] **Verifica materiali:** solo `cobblestone`, `stone`, `cobbled_deepslate`, `gravel` e `cracked_stone_bricks` (con muretti/lastre), sbarre e scale. **Nessuna arenaria o sabbia nei pezzi o sotto di essi** (sotto c'è un basamento di pietra).

---

## 4. NPC, Abitanti & Vita Cittadina

### 4.1 Sostituzione Villager con Goyim
- [ ] Evocare un villager o trovare un villaggio naturale:
  * `/summon minecraft:villager ~ ~ ~`
- [ ] Il villager viene convertito in entità `GoyimEntity`.
- [ ] La nametag sopra la testa mostra `"Goy"` visibile.
- [ ] L'entità mantiene le professioni vanilla, il livello di esperienza e l'interfaccia di scambio (`trade`).
- [ ] La conversione non provoca lag, blocchi rientranti né loop infiniti di spawn nei log.

### 4.2 Tipi di NPC Cittadini
- [ ] A Tel Aviv spawnano residenti moderni, programmatori e turisti.
- [ ] A Gerusalemme spawnano rabbini, residenti religiosi e commercianti del mercato.
- [ ] Gli NPC reagiscono al ciclo giorno/notte dirigendosi verso piazze e mercati di giorno e rientrando al tramonto.

---

## 5. Shabbat & Calendario Festività

Verifica del sistema settimanale di riposo e preghiera.

### 5.1 Ciclo dello Shabbat
* Il ciclo dello Shabbat scatta ogni 7 giorni di gioco: dal tramonto del venerdì (tick 12000 del giorno 6, 13, 20...) fino alla notte di sabato.
* **Comando per avanzare il tempo:** `/time set 13000` (al giorno corretto)
- [ ] Quando lo Shabbat è attivo, i commercianti osservanti e le botteghe chiudono gli scambi.
- [ ] Gli NPC non accettano trade durante lo Shabbat e mostrano un messaggio/animazione di preghiera o riposo.
- [ ] I dialoghi e i saluti degli NPC cambiano in auguri di "Shabbat Shalom".
- [ ] Alla fine dello Shabbat (domenica mattina), le attività commerciali riaprono regolarmente.

---

## 6. Oggetti Culturali, Candelabro & Equipaggiamento

### 6.1 Candelabro (Menorah)
* **Comando:** `/give @s israel_simulator:menorah`
- [ ] **Modello 3D:** Piazzare la Menorah a terra. Il blocco presenta un modello 3D dettagliato con piedistallo a gradini, fusto centrale rialzato e 7 bracci arcuati simmetrici (non un blocco cubico piatto).
- [ ] **Interazione candele:** Cliccando con il tasto destro con le candele in mano, queste vengono alloggiate sui calici (`branch_tips`).
- [ ] **Accensione:** Cliccando con un acciarino (`flint_and_steel`), i bracci e lo Shamash centrale si accendono emettendo luce e particelle di fiamma.

### 6.2 Kippah
* **Comando:** `/give @s israel_simulator:kippah`
- [ ] Equipaggiabile nello slot dell'elmo.
- [ ] Si renderizza visivamente sopra la testa del giocatore con texture personalizzata.

### 6.3 Talit & Tefillin
* **Comando:** `/give @s israel_simulator:talit`, `/give @s israel_simulator:tefillin`
- [ ] Equipaggiabili negli slot corazzati/accessori.
- [ ] L'uso durante la preghiera mattutina conferisce un buff temporaneo di rigenerazione/protezione.

### 6.4 Corona del Rabbino (Rabbi's Crown) — Oggetto Mitico
* **Comando:** `/give @s israel_simulator:rabbis_crown`
- [ ] Riconosciuta con rarità **MYTHIC**.
- [ ] Renderizza un modello 3D tridimensionale dorato sulla testa del giocatore.
- [ ] Emette particelle auree e conferisce le abilità previste dal game design.

### 6.5 Disco Musicale Hava Nagila — Oggetto Leggendario
* **Comando:** `/give @s israel_simulator:music_disc_hava_nagila`
- [ ] Inserito in un jukebox (`minecraft:jukebox`), riproduce la traccia musicale completa e priva di distorsioni audio.

---

## 7. Agricoltura, Cibo & Economia

### 7.1 Coltivazione dell'Uva
* **Comando:** `/give @s israel_simulator:grapes`
- [ ] L'uva è piantabile su terra zappata (`farmland`).
- [ ] La pianta d'uva attraversa le varie fasi di crescita con la polvere d'osso (`bone_meal`).
- [ ] Al raccolto rilascia uva fresca e semi/tralci.
- [ ] Il nome in italiano e inglese è visualizzato correttamente (nessuna chiave di traduzione mancante).

### 7.2 Cibo Tradizionale
- [ ] Verificare che i seguenti cibi siano commestibili e conferiscano nutrizione e saturazione adeguati:
  - [ ] Falafel (`israel_simulator:falafel`)
  - [ ] Hummus (`israel_simulator:hummus`)
  - [ ] Shakshuka (`israel_simulator:shakshuka`)
  - [ ] Sabich (`israel_simulator:sabich`)
  - [ ] Tahina (`israel_simulator:tahini`)
  - [ ] Challah (`israel_simulator:challah`)
  - [ ] Ceci, pomodoro, melanzana e semi di sesamo (`israel_simulator:chickpeas`, `tomato`, `eggplant`, `sesame_seeds`)
- [ ] Raccolti e item ingredienti sono piantabili/commestibili; i blocchi coltivati rilasciano loot coerente, anche se raccolti prima della maturazione.
- [ ] Le ricette usano ceci per hummus/falafel, pomodoro per shakshuka, melanzana per sabich e sesamo per tahina; datteri, olive e agrumi provengono dai rispettivi alberi anziché da ricette-segnaposto.
- [ ] Semi e raccolti mostrano modello, texture e traduzione corrette in italiano e inglese.

### 7.3 Economia e Shekel
* **Comando:** `/give @s israel_simulator:shekel`
- [ ] La valuta Shekel è accettata nei negozi di Tel Aviv, nei mercati di Giaffa e a Gerusalemme.
- [ ] Il tasso di cambio tra smeraldi e shekel è coerente con la tabella economica.

---

## 8. Boss Fight: Bibi Boss & Minion

Verifica del combattimento boss opzionale e satirico.

### 8.1 Evocazione e Fasi del Boss
* **Comando:** `/summon israel_simulator:bibi_boss ~ ~ ~`
- [ ] La barra del boss (BossBar) appare in alto con il nome del boss e la salute corretta.
- [ ] La colonna sonora del boss parte in sottofondo.
- [ ] Il boss esegue le abilità di discorso pubblico ("Speech") stordendo o respingendo i giocatori.
- [ ] All'abbassarsi della vita, entra nella fase Enrage aumentando velocità e danno.

### 8.2 Minion Bambini Zombie (Child Zombie Minions)
- [ ] Durante lo scontro, il boss evoca i suoi piccoli minion.
- [ ] **Varianti Skin:** Verificare che i minion evocati mostrino le **5 diverse skin colorate** (blu, giallo, verde, arancione, viola) in base al loro UUID e non abbiano tutti lo stesso vestito.
- [ ] I minion sono veloci, attaccano il giocatore e scompaiono alla sconfitta del boss.
- [ ] Alla morte del boss, viene rilasciato il loot endgame previsto senza duplicazioni.

---

## 9. Trasporti & Mobilità

- [ ] **Bicicletta (`israel_simulator:bicycle`):**
  - [ ] Piazzabile a terra e cavalcabile dal giocatore.
  - [ ] Il tasto del campanello riproduce il suono dedicato.
  - [ ] Velocità aumentata sui blocchi di asfalto (`paved_road`).
- [ ] **Monopattino Elettrico (`israel_simulator:electric_scooter`):**
  - [ ] Alta manovrabilità nelle strade di Tel Aviv.
- [ ] **Autobus / Treno:**
  - [ ] Possono trasportare più passeggeri contemporaneamente.

---

## 10. UI, Grafica, Suoni & Schermate Config

### 10.1 Schermata di Configurazione della Mod
* Accedere a: *Opzioni -> Mods -> Israel Simulator -> Config*.
- [ ] I nomi delle opzioni sono chiaramente leggibili in linguaggio naturale (es. `Bibi Max Health` al posto delle chiavi tecniche raw come `israel_simulator.configuration.bibibossMaxhealth`).
- [ ] I valori modificati (es. vita boss, frequenza eventi) vengono salvati e ricaricati correttamente senza riavvio forzato.

### 10.2 Schermata degli Achievement (Avanzamenti)
* Premere `L` in gioco per aprire gli Avanzamenti.
- [ ] Lo sfondo della scheda della mod mostra la texture della bandiera d'Israele (nessun pattern viola/nero di texture mancante).
- [ ] Gli avanzamenti si sbloccano regolarmente compiendo le azioni (es. mangiare un falafel, pregare al Muro, completare lo Shabbat).

### 10.3 Log di Gioco
- [ ] Verificare che in `run/logs/latest.log` non compaiano warning su `logoFile` deprecato né errori di sampler shader.

---

## 11. Multigiocatore, Sincronizzazione & Anti-Exploit

Per eseguire questo test è consigliato collegare due client a `./gradlew.bat runServer`.

- [ ] **Autorità del Server:** I drop dei boss, le ricompense del Muro del Pianto e gli scambi sono convalidati lato server (nessun client può iniettare pacchetti per ottenere reward doppi).
- [ ] **Cooldown:** Ricollegarsi al server (disconnessione/riconnessione) non resetta il cooldown della preghiera al Muro.
- [ ] **Visibilità Entità:** Le 5 skin dei minion e il modello 3D della Menorah appaiono identici su tutti i client connessi.
- [ ] **Shabbat Globale:** L'orario e lo stato dello Shabbat sono sincronizzati per tutti i giocatori del mondo.
- [ ] **Stabilità TPS:** Muovendosi ad alta velocità tra Tel Aviv, Gerusalemme e le Torri Gemelle, il TPS del server rimane stabile a 20.0.

---

## 12. Registro Bug & Note di Playtest

| Data | Tester | Sistema | Descrizione del Problema | Gravità (Bassa/Media/Alta) | Stato |
| :--- | :--- | :--- | :--- | :--- | :--- |
|      |        |         |                          |                            |       |
|      |        |         |                          |                            |       |
|      |        |         |                          |                            |       |

---

> *Checklist compilata e mantenuta per Israel-Simulator — Aggiornata a NeoForge 26.2.*
