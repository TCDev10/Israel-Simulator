# Israel-Simulator — Protocollo Completo di Beta Testing & Checklist

> **Versione Target:** Minecraft 26.2 / NeoForge 26.2.0.88  
> **Scopo:** Documento operativo interattivo per la sessione di Beta Testing (QA).  
> **Istruzioni:** Spunta le caselle (`- [x]`) man mano che verifichi ciascuna funzionalita in-game (Singleplayer e Server Dedicato).

---

## Indice Generale

1. [Prerequisiti & Kit Rapido di Setup](#1-prerequisiti--kit-rapido-di-setup)
2. [Checklist Biomi e Generazione Naturale (6 Biomi)](#2-checklist-biomi-e-generazione-naturale-6-biomi)
3. [Checklist Strutture Reali & Citta (16 Strutture)](#3-checklist-strutture-reali--citta-16-strutture)
4. [Checklist Western Wall (Muro del Pianto) & Preghiera](#4-checklist-western-wall-muro-del-pianto--preghiera)
5. [Checklist Oggetti Sacri, Culturali e Leggendari (Judaica)](#5-checklist-oggetti-sacri-culturali-e-leggendari-judaica)
6. [Checklist Tel Aviv, Quartieri e Dinamiche Urbane](#6-checklist-tel-aviv-quartieri-e-dinamiche-urbane)
7. [Checklist Agricoltura, Raccolti e Blocchi Botanici](#7-checklist-agricoltura-raccolti-e-blocchi-botanici)
8. [Checklist Ricette di Crafting (Tutte le 13 Ricette)](#8-checklist-ricette-di-crafting-tutte-le-13-ricette)
9. [Checklist Sistema Alimentare & Kasherut (Kosher & Digestione)](#9-checklist-sistema-alimentare--kasherut-kosher--digestione)
10. [Checklist Economia Regionale, Valute (Shekel / Agorot) & Scambi](#10-checklist-economia-regionale-valute-shekel--agorot--scambi)
11. [Checklist Rete dei Trasporti (Bici, Fermate, Paved Road, Rav-Kav)](#11-checklist-rete-dei-trasporti-bici-fermate-paved-road-rav-kav)
12. [Checklist Mar Morto & Pericoli del Deserto di Giuda](#12-checklist-mar-morto--pericoli-del-deserto-di-giuda)
13. [Checklist Boss Endgame: Bibi Boss & Coalition Guards](#13-checklist-boss-endgame-bibi-boss--coalition-guards)
14. [Checklist Feste Ebraiche & Tradizioni (Dreidel, Shofar, Menorah)](#14-checklist-feste-ebraiche--tradizioni-dreidel-shofar-menorah)
15. [Checklist Mappa d'Israele, Landmark & Reputazione](#15-checklist-mappa-disraele-landmark--reputazione)
16. [Checklist Effetti di Stato & Icone Texture (Mob Effects)](#16-checklist-effetti-di-stato--icone-texture-mob-effects)
17. [Checklist Sound Events & Audio (17 Canali Sonori & Dischi)](#17-checklist-sound-events--audio-17-canali-sonori--dischi)
18. [Checklist Tutti i 13 Avanzamenti (Advancements)](#18-checklist-tutti-i-13-avanzamenti-advancements)
19. [Checklist Inventario Completo (33 Oggetti & 11 Blocchi)](#19-checklist-inventario-completo-33-oggetti--11-blocchi)
20. [Checklist Server Dedicato, Anti-Exploit & Persistenza](#20-checklist-server-dedicato-anti-exploit--persistenza)
21. [Checklist File di Configurazione (israel_simulator-common.toml)](#21-checklist-file-di-configurazione-israel_simulator-commontoml)
22. [Cheat Sheet Comandi Rapidi (Copia & Incolla)](#22-cheat-sheet-comandi-rapidi-copia--incolla)

---

## 1. Prerequisiti & Kit Rapido di Setup

- [ ] Avvia il gioco con Java 25 e NeoForge 26.2.0.88.
- [ ] Crea un mondo Singleplayer abilitando i comandi (`Allow Cheats: ON`).
- [ ] Verifica che la Creative Tab **"Israel Simulator"** esista e contenga tutti gli oggetti e blocchi senza icone viola/nere.
- [ ] Ottieni il Kit di Setup per il testing eseguendo i comandi:

```text
/gamemode creative
/give @p israel_simulator:kippah
/give @p israel_simulator:talit
/give @p israel_simulator:tefillin
/give @p israel_simulator:rabbis_crown
/give @p israel_simulator:prayer_note 64
/give @p israel_simulator:shekel 64
/give @p israel_simulator:agora 64
/give @p israel_simulator:rav_kav
/give @p israel_simulator:bicycle
/give @p israel_simulator:israel_map
/give @p israel_simulator:menorah 8
/give @p israel_simulator:shofar
/give @p israel_simulator:dreidel
/give @p israel_simulator:hava_nagila_disc
```

---

## 2. Checklist Biomi e Generazione Naturale (6 Biomi)

La transizione dei biomi segue una logica Ovest -> Est (Costa -> Urbano -> Campagna -> Gerusalemme -> Deserto di Giuda -> Depressione Mar Morto).

- [ ] **2.1 Mediterranean Coast**
  - **Comando:** `/locate biome israel_simulator:mediterranean_coast`
  - **Verifica F3:** Il bioma riporta `israel_simulator:mediterranean_coast`.
  - **Dettagli:** Presenza di sabbia dorata, acqua marina limpida, transizione verso pianure costiere.
- [ ] **2.2 Urban Area**
  - **Comando:** `/locate biome israel_simulator:urban_area`
  - **Verifica F3:** Il bioma riporta `israel_simulator:urban_area`.
  - **Dettagli:** Terreno pianeggiante predisposto per strade lastricate e quartieri moderni di Tel Aviv.
- [ ] **2.3 Israeli Agriculture**
  - **Comando:** `/locate biome israel_simulator:israeli_agriculture`
  - **Verifica F3:** Il bioma riporta `israel_simulator:israeli_agriculture`.
  - **Dettagli:** Generazione naturale di ulivi, campi di erbe aromatiche e frutteti.
- [ ] **2.4 Jerusalem**
  - **Comando:** `/locate biome israel_simulator:jerusalem`
  - **Verifica F3:** Il bioma riporta `israel_simulator:jerusalem`.
  - **Dettagli:** Altura collinare con affioramenti di Jerusalem Stone, cipressi e pini.
- [ ] **2.5 Judean Desert**
  - **Comando:** `/locate biome israel_simulator:judean_desert`
  - **Verifica F3:** Il bioma riporta `israel_simulator:judean_desert`.
  - **Dettagli:** Rilievi aridi, dune sabbiose, cespugli secchi del deserto e montagnole rocciose.
- [ ] **2.6 Dead Sea**
  - **Comando:** `/locate biome israel_simulator:dead_sea`
  - **Verifica F3:** Il bioma riporta `israel_simulator:dead_sea`.
  - **Dettagli:** Quota Y ribassata (depressione), spiagge di blocchi di sale (`salt_block`) e cluster salini bianchi.
- [ ] **2.7 Coerenza Climatica (Seed 42)**
  - **Test:** Crea un mondo con Seed `42`. Teletrasportati a Gerusalemme e vola verso Est (+X).
  - **Risultato Atteso:** Nessun salto innaturale (niente ghiaccio o giungle monsoniche accanto al deserto).

---

## 3. Checklist Strutture Reali & Citta (16 Strutture)

- [ ] **3.1 `western_wall` (Western Wall / Kotel)**
  - **Comando:** `/locate structure israel_simulator:western_wall`
  - **Expected:** Muro erodiano a blocchi maestosi (56x20x42), mechitza divisoria, piazza pavimentata.
- [ ] **3.2 `jerusalem_city` (Citta Vecchia di Gerusalemme)**
  - **Comando:** `/locate structure israel_simulator:jerusalem_city`
  - **Expected:** Mura storiche merlate, viuzze in pietra, archi orientali e shuk interno.
- [ ] **3.3 `synagogue` (Sinagoga di Quartiere)**
  - **Comando:** `/locate structure israel_simulator:synagogue`
  - **Expected:** Bimah centrale con leggio, banchi in legno, cassa dell'Arca Santa con loot religioso.
- [ ] **3.4 `great_synagogue` (Grande Sinagoga)**
  - **Comando:** `/locate structure israel_simulator:great_synagogue`
  - **Expected:** Scala monumentale (36x18x36), loggiato per le donne, Arca Santa dorata e Menorah illuminate.
- [ ] **3.5 `historical_house` (Casa Storica)**
  - **Comando:** `/locate structure israel_simulator:historical_house`
  - **Expected:** Cortile interno con albero di olivo, soffitti a volta in Jerusalem Stone, focolare.
- [ ] **3.6 `grand_market` (Gran Mercato Shuk)**
  - **Comando:** `/locate structure israel_simulator:grand_market`
  - **Expected:** Banchi coperti da tende colorate, casse con datteri, agrumi, spezie e fontana centrale.
- [ ] **3.7 `tel_aviv_city` (Metropoli di Tel Aviv)**
  - **Comando:** `/locate structure israel_simulator:tel_aviv_city`
  - **Expected:** Griglia stradale moderna (48x18x48), edifici Bauhaus bianchi, negozi e uffici.
- [ ] **3.8 `startup_office` (Hub Tecnologico High-Tech)**
  - **Comando:** `/locate structure israel_simulator:startup_office`
  - **Expected:** Open space con scrivanie, server rack luminosi, laptop e distributori di caffe.
- [ ] **3.9 `government_building` (Palazzo Governativo / Knesset)**
  - **Comando:** `/locate structure israel_simulator:government_building`
  - **Expected:** Colonne in quarzo, podio dell'assemblea con microfono, bandiera e archivi blindati.
- [ ] **3.10 `jaffa_port` (Porto Antico di Jaffa)**
  - **Comando:** `/locate structure israel_simulator:jaffa_port`
  - **Expected:** Moli e banchine in pietra sul mare, lanterne, gru manuali e magazzini portuali.
- [ ] **3.11 `mediterranean_village` (Borgo Costiero)**
  - **Comando:** `/locate structure israel_simulator:mediterranean_village`
  - **Expected:** Case intonacate di bianco con dettagli azzurri, giardini terrazzati e vasi di fiori.
- [ ] **3.12 `agricultural_farm` (Fattoria Kibbutz)**
  - **Comando:** `/locate structure israel_simulator:agricultural_farm`
  - **Expected:** Serre moderne (48x10x40), tralicci per viti, aranceti recintati e trattori/fienili.
- [ ] **3.13 `dead_sea_resort` (Stabilimento Termale del Mar Morto)**
  - **Comando:** `/locate structure israel_simulator:dead_sea_resort`
  - **Expected:** Passerelle di legno, vasche fanghi curative, lettini e ombrelloni a bordo riva.
- [ ] **3.14 `ein_gedi_oasis` (Oasi di Ein Gedi)**
  - **Comando:** `/locate structure israel_simulator:ein_gedi_oasis`
  - **Expected:** Bacino montano di acqua dolce incastonato nel canyon, canne e palme da dattero.
- [ ] **3.15 `desert_ruins` (Rovine Fortificate del Deserto)**
  - **Comando:** `/locate structure israel_simulator:desert_ruins`
  - **Expected:** Bastioni di pietra scalfita dal vento, archi crollati e casse nascoste sotto la sabbia.
- [ ] **3.16 `ancient_sanctuary` (Santuario del Deserto / Tabernacolo)**
  - **Comando:** `/locate structure israel_simulator:ancient_sanctuary`
  - **Expected:** Atrio recintato (30x12x34), Santo dei Santi protetto da velo, cassa rara (possibile drop Rabbi's Crown).

---

## 4. Checklist Western Wall (Muro del Pianto) & Preghiera

- [ ] **4.1 Rifiuto a Testa Nuda:** Clicca con tasto destro su un blocco `western_wall_stone` senza copricapo.
  - **Risultato:** Messaggio rosso di rifiuto: *"You must wear a Kippah to pray at the Western Wall"*.
- [ ] **4.2 Rifiuto Senza Prayer Note:** Indossa la Kippah ma non tenere una Prayer Note in mano. Clicca sul Muro.
  - **Risultato:** Messaggio di rifiuto che richiede una `prayer_note`.
- [ ] **4.3 Preghiera Corretta & Animazione 3D:** Indossa la Kippah, tieni in mano `prayer_note` e clicca sul Muro.
  - **Risultato:** Inizia la preghiera per 3 secondi (60 tick).
  - In F5 (terza persona) la testa del giocatore si china e le braccia si distendono verso il Muro.
- [ ] **4.4 Ricompensa & Cooldown Server:** Attendi fermo per 3 secondi.
  - **Risultato:**
    - La Prayer Note viene consumata.
    - Ricevi esattamente **5 Diamanti** nell'inventario.
    - Particelle `VILLAGER_HAPPY` e suono di avanzamento.
    - Sblocco dell'avanzamento *"Five Diamonds"*.
    - Viene impostato il cooldown server di 24.000 tick (1 giorno di Minecraft).
- [ ] **4.5 Rispetto del Cooldown:** Clicca di nuovo con un'altra Prayer Note immediatamente.
  - **Risultato:** Rifiuto della preghiera con messaggio di attesa del tempo rimanente.
- [ ] **4.6 Annullamento per Movimento:** Avvia la preghiera e muoviti o salta prima dei 3 secondi.
  - **Risultato:** La preghiera si annulla, nessun diamante concesso, Prayer Note intatta nell'inventario.

---

## 5. Checklist Oggetti Sacri, Culturali e Leggendari (Judaica)

- [ ] **5.1 Kippah (Item di Riconoscimento):**
  - Equipaggia nello slot elmo (`HEAD`). Modello 3D aderente sul capo.
  - Sblocca l'avanzamento *"Shalom"*.
  - Protegge dal colpo di calore nel Deserto di Giuda.
- [ ] **5.2 Talit (Scialle di Preghiera):**
  - Equipaggia nello slot corazza (`CHEST`). Modello 3D con frange (tzitzit) e strisce blu.
  - Fornisce +4 punti armatura e resistenza agli elementi.
- [ ] **5.3 Tefillin (Legacci Sacri Mattutini):**
  - Esegui `/time set 1000` (mattina). Tieni i `tefillin` in mano e premi tasto destro.
  - **Risultato:** Il rituale conferisce l'effetto *Blessed* per 60 secondi (1200 tick) e particelle sacre.
  - Esegui `/time set 18000` (mezzanotte) e riprova: il rituale viene rifiutato (consentito solo di mattina).
- [ ] **5.4 Rabbi's Crown (Corona del Rabbino — MYTHIC):**
  - Equipaggia nello slot testa.
  - **Risultato:**
    - Fornisce +20 punti armatura (barra piena).
    - Render 3D con cappello tradizionale, barba fluente e payot laterali.
    - Conferisce l'effetto permanente *Blessed Trader* (15% di sconto su tutti i mercanti).
    - Immunita a colpi fatali (salvataggio stile Totem con ricarica).
- [ ] **5.5 First Amendment (Primo Emendamento — LEGENDARY):**
  - Tieni in mano l'oggetto e premi tasto destro a Tel Aviv.
  - **Risultato:** Conferisce l'effetto *Freedom* (velocita + salto potenziato + resistenza) e sblocca l'avanzamento *"Freedom of Speech"*.
- [ ] **5.6 Hava Nagila Music Disc (Disco Leggendario):**
  - Rilasciato dalla sconfitta del Bibi Boss.
  - Inseriscilo in un Jukebox: musica nitida, display toast *"Now Playing: Hava Nagila"*.

---

## 6. Checklist Tel Aviv, Quartieri e Dinamiche Urbane

- [ ] **6.1 Notifica Action Bar all'Ingresso Quartieri:**
  - Cammina attraverso Tel Aviv:
    - Entrando nel distretto tecnologico compare sopra la hotbar: `Entering Startup District - High-Tech Hub`.
    - Spostandoti verso il lungomare: `Entering Tayelet Beach - Coastal Promenade`.
    - Spostandoti verso Rothschild: `Entering Rothschild - Bauhaus & Boulevards`.
    - Spostandoti verso Florentin: `Entering Florentin - Bohemian & Street Art`.
    - Spostandoti verso Sarona: `Entering Sarona - Culinary Market`.
    - Spostandoti verso White City: `Entering White City - Architectural Heritage`.
- [ ] **6.2 Assenza di Spam Notifiche:** Rimanere all'interno dello stesso quartiere non deve spammare il messaggio ad ogni passo.
- [ ] **6.3 Avanzamento Notturno:** Entra a Tel Aviv tra le 18:00 e le 06:00 del tempo di gioco per sbloccare l'avanzamento *"Tel Aviv Nights"*.

---

## 7. Checklist Agricoltura, Raccolti e Blocchi Botanici

- [ ] **7.1 Foglie di Olivo (`olive_leaves`):**
  - Rompendo o cliccando sulle foglie mature si ottengono le olive (`israel_simulator:olives`).
- [ ] **7.2 Foglie di Palma da Dattero (`date_palm_leaves`):**
  - Rilasciano i datteri della Giudea (`israel_simulator:dates`).
- [ ] **7.3 Foglie di Agrumi (`citrus_leaves`):**
  - Rilasciano agrumi freschi (`israel_simulator:citrus`).
- [ ] **7.4 Vite Mediterranea (`grapevine`):**
  - Piantabile su terra zappata. Cresce in 4 stadi con bone meal.
  - A maturita (stadio 3) il click destro rilascia l'uva (`israel_simulator:grapes`) resettando lo stadio senza distruggere la pianta.
- [ ] **7.5 Erbe Mediterranee (`mediterranean_herbs`):**
  - Generano nel bioma agricolo; rompendole rilasciano semi o foglioline profumate.

---

## 8. Checklist Ricette di Crafting (Tutte le 13 Ricette)

Apri un Banco da Lavoro (`crafting_table`) e testa ciascuna delle ricette registrate:

- [ ] **8.1 Tahini:** 2x Grano + 1x Ciotola (`minecraft:wheat`, `minecraft:bowl`) -> `israel_simulator:tahini`
- [ ] **8.2 Hummus:** 1x Tahini + 1x Barbabietola + 1x Ciotola -> `israel_simulator:hummus`
- [ ] **8.3 Falafel:** 1x Grano + 1x Barbabietola + 1x Semi di grano -> 3x `israel_simulator:falafel`
- [ ] **8.4 Shakshuka:** 1x Uovo + 1x Barbabietola + 1x Ciotola -> `israel_simulator:shakshuka`
- [ ] **8.5 Sabich:** 1x Grano + 1x Uovo + 1x Tahini -> `israel_simulator:sabich`
- [ ] **8.6 Challah:** 3x Grano + 1x Uovo + 1x Zucchero -> `israel_simulator:challah`
- [ ] **8.7 Matzo:** 2x Grano + 1x Secchio d'Acqua -> 4x `israel_simulator:matzo` (restituisce il secchio vuoto)
- [ ] **8.8 Sufganiyah:** 1x Grano + 1x Zucchero + 1x Bacche dolci -> `israel_simulator:sufganiyah`
- [ ] **8.9 Hamantash:** 1x Grano + 1x Zucchero + 1x Datteri -> `israel_simulator:hamantash`
- [ ] **8.10 Rugelach:** 1x Grano + 1x Cacao + 1x Zucchero -> 2x `israel_simulator:rugelach`
- [ ] **8.11 Lavorazione Datteri:** 1x Mazzo di datteri crudi -> `israel_simulator:dates`
- [ ] **8.12 Lavorazione Olive:** 1x Oliva su griglia -> `israel_simulator:olives`
- [ ] **8.13 Lavorazione Agrumi:** 1x Agrume -> `israel_simulator:citrus`

---

## 9. Checklist Sistema Alimentare & Kasherut (Kosher & Digestione)

- [ ] **9.1 Classificazione Cibi:**
  - **Carne (Meat):** Bistecca, pollo, montone.
  - **Latte (Dairy):** Formaggio, latte, dolci al burro.
  - **Parve (Neutro):** Falafel, Hummus, Shakshuka, Frutta, Challah, Matzo, Pesce.
- [ ] **9.2 Timer Digestione Carne:**
  - Consuma un cibo a base di carne in modalita Survival.
  - **Risultato:** Ricevi lo status `israel_simulator:meat_digestion` per 3 minuti (3600 tick).
  - L'icona dell'effetto mostra la bistecca con orologio di attesa.
- [ ] **9.3 Violazione Regola Carne/Latte:**
  - Mentre l'effetto `meat_digestion` e attivo, consuma un cibo latticino.
  - **Risultato:** Viene inflitto un malus digestivo (Nausea e Lentezza per 15 secondi) con notifica nella chat.
- [ ] **9.4 Cibi Parve Senza Restrizioni:**
  - Consuma Falafel o Hummus sia durante la digestione di carne che di latte.
  - **Risultato:** Nessun malus (i cibi Parve sono compatibili con qualsiasi pasto).
- [ ] **9.5 Disattivazione da Configurazione:**
  - Nel file `israel_simulator-common.toml`, imposta `kosherSystemEnabled = false`.
  - **Risultato:** La combinazione carne/latte non applica alcun effetto negativo.

---

## 10. Checklist Economia Regionale, Valute (Shekel / Agorot) & Scambi

- [ ] **10.1 Tagli di Valuta:**
  - 1 Shekel d'argento = 100 Agorot di bronzo.
- [ ] **10.2 Baratto di Prodotti della Terra:**
  - Vai nel bioma `israel_simulator:israeli_agriculture`.
  - Tieni in mano 20 carote o grano e clicca tasto destro su un Villager contadino.
  - **Risultato:** Il contadino acquista il raccolto consegnando Shekel ed Agorot.
- [ ] **10.3 Negozio di Articoli Sacri (Jerusalem):**
  - Teletrasportati a Gerusalemme e clicca su un abitante:
    - Con 1 Shekel: acquista 1x `prayer_note`.
    - Con 4 Shekel: acquista 1x `kippah`.
    - Con 12 Shekel e `Shift` (accovacciato): acquista 1x `tefillin`.
- [ ] **10.4 Mercato Shuk di Tel Aviv (Carmel / Sarona):**
  - Clicca su mercanti di cibo con Shekel per acquistare piatti pronti (Falafel, Hummus, Sabich).
- [ ] **10.5 Sconto Reputazione & Blessed Trader:**
  - Clicca su un mercante indossando la `rabbis_crown` o avendo l'effetto *Blessed*.
  - **Risultato:** Compare l'avviso di sconto del 15% e i prezzi in Shekel sono ribassati. Sblocco avanzamento *"Blessed Trader"*.

---

## 11. Checklist Rete dei Trasporti (Bici, Fermate, Paved Road, Rav-Kav)

- [ ] **11.1 Strada Pavimentata (`paved_road`):**
  - Cammina sopra il blocco `israel_simulator:paved_road`.
  - **Risultato:** Viene conferito un boost di velocita (+40% / Speed I) per 3 secondi.
  - Distruggi il blocco con piccone: droppa se stesso.
- [ ] **11.2 Bicicletta (`bicycle`):**
  - Piazza l'oggetto `bicycle` a terra.
  - Sali in sella col tasto destro e muoviti con i tasti WASD.
  - Premi Spazio mentre guidi: risuona il campanello squillante (`entity.bicycle.bell`).
  - Smonta con Shift: colpendola torna nell'inventario come oggetto.
- [ ] **11.3 Scarpe da Camminata (`walking_shoes`):**
  - Equipaggia le scarpe nello slot stivali: aumentano la velocita di camminata sui sentieri montani e riducono il consumo di fame.
- [ ] **11.4 Fermata dei Trasporti (`transport_stop`) & Carta Rav-Kav:**
  - Piazza un blocco `transport_stop`.
  - Con una carta `rav_kav` in mano (o 10 Shekel), clicca sul blocco.
  - **Risultato:**
    - Routing automatico alla fermata successiva del circuito:  
      *Tel Aviv Central* -> *Jaffa Clock Tower* -> *Jaffa Port* -> *Jerusalem Navon* -> *Dead Sea Ein Gedi* -> *Galilee Hub* -> *Tel Aviv Central*.
    - Risuona il suono del treno o dell'autobus (`TRANSIT_TRAVEL`).
    - Teletrasporto istantaneo alla destinazione e scalata della tariffa.
- [ ] **11.5 Cooldown Fermata:** Clicca subito dopo il viaggio sulla fermata d'arrivo.
  - **Risultato:** Messaggio di cooldown: *"Transit cooldown: wait 3s before boarding again"*.
- [ ] **11.6 Scavo Fermata:** Distruggi il blocco fermata con un piccone in ferro/diamante.
  - **Risultato:** Rilascia esattamente 1x `transport_stop`.

---

## 12. Checklist Mar Morto & Pericoli del Deserto di Giuda

- [ ] **12.1 Flottazione / Galleggiamento Naturale:**
  - Tuffati nell'acqua del Mar Morto (`dead_sea`) senza premere la barra spaziatrice.
  - **Risultato:** Il personaggio galleggia spontaneamente a pelo d'acqua senza andare a fondo.
- [ ] **12.2 Fango del Mar Morto (`dead_sea_mud`):**
  - Raccogli il fango a riva o usa `/give @p israel_simulator:dead_sea_mud`.
  - Clicca tasto destro per applicarlo: si consuma donando *Regeneration II* e *Absorption I* per 45 secondi.
  - Sblocca l'avanzamento *"Dead Sea Tourist"*.
- [ ] **12.3 Blocchi di Sale (`salt_block`):**
  - Generano sulle scogliere del Mar Morto. Minandoli con piccone rilasciano il blocco di sale decorativo.
- [ ] **12.4 Rischio Colpo di Calore nel Deserto:**
  - Nel bioma `judean_desert` a mezzogiorno (`/time set 6000`), togliti il copricapo e cammina al sole.
  - **Risultato:** La saturazione crolla rapidamente; indossare la `kippah` elimina il malus.

---

## 13. Checklist Boss Endgame: Bibi Boss & Coalition Guards

- [ ] **13.1 Evocazione:**
  - Esegui `/summon israel_simulator:bibi_boss`.
  - **Risultato:**
    - Appare l'entita con la skin in completo formale e cravatta.
    - Compare in alto la Boss Bar viola: **"Bibi, Master of Coalitions"** (10.000 HP).
    - Risuona la colonna sonora del boss (`BIBI_THEME`).
- [ ] **13.2 Fase 1 & Discorsi Politici:**
  - Il boss attacca ed emette frammenti oratori satirici a intervalli regolari (`entity.bibi_boss.speech`).
  - Protezione anti-cheese: max 500 danni per singolo colpo.
- [ ] **13.3 Fase 2 & Coalition Guards:**
  - Sotto i 7.000 HP, il boss evoca le guardie della coalizione (`bibi_guard`) fino a un massimo di 4 contemporaneamente.
- [ ] **13.4 Fase 3 (Enraged Mode):**
  - Sotto i 3.000 HP (30% salute), il boss entra in collera: particelle nere/rosse, velocita raddoppiata e attacchi caricati.
  - Suono epico di enrage (`BIBI_ENRAGE`).
- [ ] **13.5 Sconfitta e Loot:**
  - Riduci a zero la vita del boss.
  - **Risultato:** Suono di morte trionfale (`BIBI_DEATH`), drop del disco `hava_nagila_disc`, Shekel, e avanzamento *"Hava Nagila"*.

---

## 14. Checklist Feste Ebraiche & Tradizioni (Dreidel, Shofar, Menorah)

- [ ] **14.1 Mini-gioco del Dreidel (`dreidel`):**
  - Tieni in inventario qualche Shekel. Clicca con tasto destro sul Dreidel.
  - **Risultato:** Notifica con esito estratto:
    - **Nun (נ):** *"Nisht"* — Nulla accade.
    - **Gimel (ג):** *"Gantz"* — Vinci l'intero piatto (+Shekel).
    - **Hei (ה):** *"Halb"* — Vinci meta piatto.
    - **Shin (ש):** *"Shtel"* — Metti uno Shekel nel piatto.
- [ ] **14.2 Squillo dello Shofar (`shofar`):**
  - Clicca tasto destro tenendo lo Shofar.
  - **Risultato:** Squillo potente del corno di montone udibile fino a 32 blocchi. Cooldown di 30 secondi (600 tick).
- [ ] **14.3 Menorah a 8 Candele (`menorah`):**
  - Piazza il blocco `menorah`. Usa un acciarino (`flint_and_steel`) per 8 volte consecutive.
  - **Risultato:** Ad ogni click si accende una fiamma ulteriore, incrementando la luce da 4 fino a 15.

---

## 15. Checklist Mappa d'Israele, Landmark & Reputazione

- [ ] **15.1 Mappa d'Israele (`israel_map`):**
  - Tieni la mappa durante l'esplorazione. Entrando entro 48 blocchi da un landmark storico:
    - Risuona la fanfara di scoperta (`ui.landmark_discovered`).
    - Compare il toast dorato: `★ Landmark Discovered: <Nome Luogo>`.
    - Vengono accreditati +100 punti esperienza.
- [ ] **15.2 Sistema delle 5 Fazioni:**
  - Fazioni: *City*, *Religious*, *Merchant*, *Tech District*, *Village*.
  - Azioni coerenti migliorano la reputazione e i prezzi dei mercanti.

---

## 16. Checklist Effetti di Stato & Icone Texture (Mob Effects)

Verifica che tutti e 5 gli effetti mostrino l'icona 18x18 nitida nell'inventario e nell'HUD (nessuna texture viola/nera mancante):

- [ ] **16.1 Blessed (`israel_simulator:blessed`):**
  - **Comando:** `/effect give @p israel_simulator:blessed 30 0`
  - **Icona:** Stella di David d'oro radiosa con bagliore divino.
- [ ] **16.2 Blessed Trader (`israel_simulator:blessed_trader`):**
  - **Comando:** `/effect give @p israel_simulator:blessed_trader 30 0`
  - **Icona:** Moneta Shekel dorata con scintillio verde smeraldo commerciale.
- [ ] **16.3 Freedom (`israel_simulator:freedom`):**
  - **Comando:** `/effect give @p israel_simulator:freedom 30 0`
  - **Icona:** Colomba bianca della pace con ramo d'ulivo e aura blu liberta.
- [ ] **16.4 Waiting Meat (`israel_simulator:meat_digestion`):**
  - **Comando:** `/effect give @p israel_simulator:meat_digestion 30 0`
  - **Icona:** Bistecca succulenta con anello orologio del timer digestivo.
- [ ] **16.5 Waiting Dairy (`israel_simulator:dairy_digestion`):**
  - **Comando:** `/effect give @p israel_simulator:dairy_digestion 30 0`
  - **Icona:** Bicchiere/bottiglia di latte con anello orologio del timer digestivo.

---

## 17. Checklist Sound Events & Audio (17 Canali Sonori & Dischi)

Esegui `/playsound <id> player @p` per verificare ogni canale:

- [ ] `/playsound israel_simulator:music_disc.hava_nagila player @p`
- [ ] `/playsound israel_simulator:music.cultural.klezmer player @p`
- [ ] `/playsound israel_simulator:music.cultural.shabbat_shalom player @p`
- [ ] `/playsound israel_simulator:ambient.city.tel_aviv player @p`
- [ ] `/playsound israel_simulator:ambient.city.jerusalem player @p`
- [ ] `/playsound israel_simulator:ambient.city.jaffa player @p`
- [ ] `/playsound israel_simulator:ambient.event.market_bustle player @p`
- [ ] `/playsound israel_simulator:audio.festival.hanukkah_chime player @p`
- [ ] `/playsound israel_simulator:entity.bibi_boss.ambient player @p`
- [ ] `/playsound israel_simulator:entity.bibi_boss.speech player @p`
- [ ] `/playsound israel_simulator:entity.bibi_boss.enrage player @p`
- [ ] `/playsound israel_simulator:entity.bibi_boss.death player @p`
- [ ] `/playsound israel_simulator:entity.bicycle.bell player @p`
- [ ] `/playsound israel_simulator:entity.transport.travel player @p`
- [ ] `/playsound israel_simulator:ui.landmark_discovered player @p`

---

## 18. Checklist Tutti i 13 Avanzamenti (Advancements)

- [ ] **18.1 cultural/shalom:** Indossa una Kippah per la prima volta.
- [ ] **18.2 cultural/freedom_of_speech:** Utilizza il documento del Primo Emendamento a Tel Aviv.
- [ ] **18.3 economy/five_diamonds:** Completa una preghiera al Kotel ricevendo 5 diamanti.
- [ ] **18.4 economy/blessed_trader:** Esegui uno scambio con sconto indossando la Rabbi's Crown.
- [ ] **18.5 exploration/welcome_to_israel:** Metti piede in uno qualsiasi dei biomi d'Israele.
- [ ] **18.6 exploration/visit_jerusalem:** Raggiungi le colline di Gerusalemme.
- [ ] **18.7 exploration/jaffa:** Visita il Porto Antico di Jaffa.
- [ ] **18.8 exploration/tel_aviv_nights:** Esplora Tel Aviv durante la notte.
- [ ] **18.9 exploration/dead_sea_tourist:** Galleggia sul Mar Morto e applica il fango curativo.
- [ ] **18.10 exploration/master_explorer:** Scopri tutti i landmark d'Israele con la mappa.
- [ ] **18.11 combat/hava_nagila:** Sconfiggi il Bibi Boss e ottieni il disco musicale.
- [ ] **18.12 technology/startup_founder:** Crea o attiva uno Smartphone o Laptop a Sarona/Startup District.
- [ ] **18.13 secret/hummus_connoisseur:** Assaggia tutti i piatti tipici (Hummus, Falafel, Shakshuka, Sabich).
- [ ] **18.14 secret/secret_kippah_cat:** Fai indossare una Kippah a un gatto addomesticato.

---

## 19. Checklist Inventario Completo (33 Oggetti & 11 Blocchi)

### Gli 11 Blocchi Personalizzati:
| Blocco | Namespace ID | Strumento | Drop Atteso | Spunta Collaudo |
|---|---|---|---|---|
| Foglie di Olivo | `israel_simulator:olive_leaves` | Cesoie / Mano | Olive / Se stesso | - [ ] |
| Foglie di Dattero | `israel_simulator:date_palm_leaves` | Cesoie / Mano | Datteri / Se stesso | - [ ] |
| Foglie di Agrumi | `israel_simulator:citrus_leaves` | Cesoie / Mano | Agrumi / Se stesso | - [ ] |
| Vite d'Uva | `israel_simulator:grapevine` | Mano / Zappa | Uva (a maturita) | - [ ] |
| Erbe Mediterranee | `israel_simulator:mediterranean_herbs` | Cesoie / Mano | Semi / Se stesso | - [ ] |
| Blocco di Sale | `israel_simulator:salt_block` | Piccone | `salt_block` | - [ ] |
| Pietra di Gerusalemme | `israel_simulator:jerusalem_stone` | Piccone | `jerusalem_stone` | - [ ] |
| Pietra del Muro | `israel_simulator:western_wall_stone` | Piccone | `western_wall_stone` | - [ ] |
| Menorah | `israel_simulator:menorah` | Piccone / Mano | `menorah` | - [ ] |
| Strada Pavimentata | `israel_simulator:paved_road` | Piccone | `paved_road` | - [ ] |
| Fermata dei Trasporti | `israel_simulator:transport_stop` | Piccone | `transport_stop` | - [ ] |

### I 33 Oggetti Registrati:
| Categoria | Oggetti da Verificare | Spunta Collaudo |
|---|---|---|
| **Culturali / Religiosi** | `kippah`, `talit`, `tefillin`, `rabbis_crown`, `prayer_note`, `first_amendment`, `hava_nagila_disc` | - [ ] |
| **Cibo & Ingredienti** | `tahini`, `dates`, `grapes`, `olives`, `citrus`, `challah`, `rugelach` | - [ ] |
| **Cibo Preparato** | `falafel`, `hummus`, `shakshuka`, `sabich` | - [ ] |
| **Collezionabili** | `mezuzah`, `star_of_david`, `olive_wood_carving`, `ancient_coin`, `dead_sea_scroll_fragment`, `dead_sea_mud` | - [ ] |
| **Valuta** | `shekel`, `agora` | - [ ] |
| **Feste** | `matzo`, `sufganiyah`, `dreidel`, `hamantash`, `shofar` | - [ ] |
| **Tecnologia** | `smartphone`, `laptop`, `drone_part` | - [ ] |
| **Trasporti & Esplorazione** | `bicycle`, `rav_kav`, `walking_shoes`, `israel_map` | - [ ] |

---

## 20. Checklist Server Dedicato, Anti-Exploit & Persistenza

- [ ] **20.1 Persistenza dei Cooldown dopo Riavvio Server:**
  - Esegui una preghiera al Kotel su Server Dedicato (`./gradlew runServer`).
  - Chiudi il server con `/stop`.
  - Riavvia il server e riconnettiti.
  - Verifica che la preghiera sia ancora in cooldown e non consenta un secondo incasso di diamanti.
- [ ] **20.2 Anti-Duplicazione Shekel:** Saldo autoritativo sul server.
- [ ] **20.3 Anti-Arbitraggio Prezzi:** `Prezzo di Acquisto > Prezzo di Vendita`.
- [ ] **20.4 Despawn & Cleanup Boss Fight:** Reset pulito delle guardie se i giocatori abbandonano l'arena.

---

## 21. Checklist File di Configurazione (israel_simulator-common.toml)

- [ ] `bibiBossMaxHealth = 10000.0`
- [ ] `bibiBossMaxGuards = 4`
- [ ] `kosherSystemEnabled = true`
- [ ] `prayerCooldownTicks = 24000`
- [ ] `citySpacingChunks = 34`
- [ ] `maxNpcPerChunk = 20`
- [ ] `musicVolumeMultiplier = 1.0`

---

## 22. Cheat Sheet Comandi Rapidi (Copia & Incolla)

```text
# Teletrasporto Biomi
/locate biome israel_simulator:mediterranean_coast
/locate biome israel_simulator:urban_area
/locate biome israel_simulator:israeli_agriculture
/locate biome israel_simulator:jerusalem
/locate biome israel_simulator:judean_desert
/locate biome israel_simulator:dead_sea

# Teletrasporto Strutture
/locate structure israel_simulator:western_wall
/locate structure israel_simulator:jerusalem_city
/locate structure israel_simulator:synagogue
/locate structure israel_simulator:great_synagogue
/locate structure israel_simulator:grand_market
/locate structure israel_simulator:tel_aviv_city
/locate structure israel_simulator:startup_office
/locate structure israel_simulator:government_building
/locate structure israel_simulator:jaffa_port
/locate structure israel_simulator:mediterranean_village
/locate structure israel_simulator:agricultural_farm
/locate structure israel_simulator:dead_sea_resort
/locate structure israel_simulator:ein_gedi_oasis
/locate structure israel_simulator:ancient_sanctuary

# Evocazione Boss & Entita
/summon israel_simulator:bibi_boss
/summon israel_simulator:bibi_guard
/summon israel_simulator:bicycle

# Test Effetti con Icone
/effect give @p israel_simulator:blessed 30 0
/effect give @p israel_simulator:blessed_trader 30 0
/effect give @p israel_simulator:freedom 30 0
/effect give @p israel_simulator:meat_digestion 30 0
/effect give @p israel_simulator:dairy_digestion 30 0

# Reset & Assegnazione Avanzamenti
/advancement grant @p everything
/advancement revoke @p everything
```
