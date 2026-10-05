# Test log

## Città che spawnavano come villaggi vanilla

Problema riscontrato: Tel Aviv, Jaffa, Jerusalem e il Western Wall usavano come `start_pool` i villaggi vanilla (`minecraft:village/plains/town_centers` o `minecraft:village/desert/town_centers`). In mondo sarebbero comparsi villaggi normali, non strutture del mod. I quartieri di Tel Aviv (Rothschild, Florentin, Sarona, White City, Bauhaus, startup) restano solo nomi nel codice, senza edifici propri.

Funzionamento aspettato: ogni città parte da un template pool di `israel_simulator` e piazza un pezzo del mod, non un villaggio vanilla.

Come è stato risolto: pool e NBT propri per le quattro strutture (cubo Bauhaus, capanno del porto, casa di pietra con cupola, piazza del muro). Il test `StructureFrameworkTest` fallisce se uno di quei `start_pool` contiene `minecraft:village`. `./gradlew test` è passato (181 test). Merge in [PR 3](https://github.com/TCDev10/Israel-Simulator/pull/3). Un mondo Minecraft non è stato generato.

## Strutture rimaste sui villaggi vanilla

Problema riscontrato: synagogue, government_building, ancient_sanctuary, grand_market, startup_office, agricultural_farm, great_synagogue, dead_sea_resort, desert_ruins, historical_house, mediterranean_village e ein_gedi_oasis avevano ancora `start_pool` su un pool vanilla (`minecraft:village/plains/town_centers`, `minecraft:village/desert/town_centers` o `minecraft:village/plains/houses`). In generazione sarebbero comparsi villaggi vanilla, non pezzi del mod. Le quattro città già sistemate (Tel Aviv, Jaffa, Jerusalem, Western Wall) non erano coinvolte.

Funzionamento aspettato: nessuna struttura in `data/israel_simulator/worldgen/structure` usa un `start_pool` che contiene `minecraft:village`. Ognuna parte da un template pool `israel_simulator:<nome>` che piazza un NBT del mod con un blocco marker proprio.

Come è stato risolto: per le dodici strutture sono stati aggiunti un template pool e un NBT piccolo (DataVersion 4903) con palette distinta e marker proprio (vetro viola, deepslate levigato, arenaria cesellata, terracotta rossa, blocco di ferro, fieno, vetro blu, fango compatto, mattoni di pietra crepati, mattoni, terracotta grigio chiaro, blocco di muschio). `StructureFrameworkTest` ora fallisce se qualunque JSON di struttura usa un pool `minecraft:village`, e verifica che il pool punti al template del mod e che l'NBT contenga il marker. `./gradlew test --tests com.israelsimulator.world.StructureFrameworkTest` e `./gradlew test` sono passati. Un mondo Minecraft non è stato generato.

## Equip culturale già a posto prima di questo giro

Problema riscontrato: in un controllo precedente kippah, talit e Rabbi's Crown risultavano item normali, senza slot, senza +20 e senza Blessed Trader.

Funzionamento aspettato: la kippah sta in testa, il talit sul petto, la corona dà +20 armatura e, se equipaggiata, lo sconto Blessed Trader del 15%.

Come è stato risolto: non in questa sessione. Sul main `6ed821b`, prima di questo passaggio, `CulturalItems` registra già la kippah su `EquipmentSlot.HEAD`, il talit su `EquipmentSlot.CHEST` e la corona con modificatore armatura 20. `IsraelVillagerTrades.calculateAdjustedPrice` applica `0.85` quando il trader è blessed. Nessuna modifica di codice qui. Non è stato avviato Minecraft per indossarli in gioco.

## Cooldown della preghiera al Western Wall

Problema riscontrato: `WesternWallManager` teneva l'ultimo orario di preghiera in una `ConcurrentHashMap` statica `LAST_PRAYER_TIMES`, non in `SavedData` e non nell'NBT del giocatore. Al riavvio del server la mappa si svuota e lo stesso giocatore può ricevere di nuovo 5 diamanti. In più `instabuild` saltava il cooldown ma i diamanti venivano dati lo stesso, e la mappa del client non è quella del server.

Funzionamento aspettato: cooldown di 24000 tick, ricompensa di 5 diamanti, Prayer Note consumata (in creativa la nota non si consuma, ma il cooldown vale comunque). Il tempo resta sul mondo del server e sopravvive a un riavvio. Il client non decide la ricompensa.

Come è stato risolto: i tempi stanno in `WesternWallCooldowns`, `SavedData` della dimensione (`israel_simulator:western_wall_prayers`), con codec NBT `prayers`. Non c'è una mappa statica. `tryPray` legge e scrive solo su `ServerLevel`. `enforcesCooldown` è sempre vero, anche in creativa. Il test `WesternWallInteractionTest` fallisce se manager o saved data hanno una `Map` statica, e ricarica il cooldown dal codec come dopo un riavvio. Un server Minecraft non è stato avviato.

## Cooldown di sinagoga, tefillin e shofar

Problema riscontrato: dopo il salvataggio del Western Wall, i cooldown di sinagoga, tefillin e shofar erano ancora mappe statiche. `SynagogueManager.LAST_PRAYER_TIMES`, `TefillinManager.LAST_TEFILLIN_USE` (e una seconda mappa inutilizzata su `TefillinItem`) e `ShofarManager.LAST_SHOFAR_USE` sono `ConcurrentHashMap` in memoria. Al riavvio del server si svuotano: la preghiera all'arca, il rito dei tefillin e il suono dello shofar si possono ripetere subito. Il client e il server non condividono quella mappa.

Funzionamento aspettato: ogni cooldown resta sul mondo del server, per dimensione, e sopravvive a un riavvio. Non deve restare una mappa statica come fonte dei tempi. La durata non cambia: sinagoga e tefillin 12000 tick, shofar 100 tick (`ShofarManager.COOLDOWN_TICKS`, non il valore di config). Il Western Wall resta `israel_simulator:western_wall_prayers`.

Come è stato risolto: tre `SavedData` sullo stesso schema di `WesternWallCooldowns`. Sinagoga: `SynagogueCooldowns`, id `israel_simulator:synagogue_prayers`, campo NBT `prayers`. Tefillin: `TefillinCooldowns`, id `israel_simulator:tefillin_prayers`, campo `prayers`. Shofar: `ShofarCooldowns`, id `israel_simulator:shofar_blasts`, campo `blasts`. `tryArkPray`, `TefillinItem.use` e `ShofarItem.use` leggono e scrivono solo su `ServerLevel`. La creativa salta ancora il cooldown di sinagoga e tefillin, come prima; lo shofar non lo saltava e non lo salta. I test `SynagogueFrameworkTest`, `TalitAndTefillinTest` e `FestivalsAndCalendarTest` falliscono se manager, saved data o item hanno una `Map` statica, e ricaricano il codec come dopo un riavvio. Un server Minecraft non è stato avviato. Nessun commit.

## Cooldown dello shofar ignorava la config

Problema riscontrato: `ShofarManager` usava `COOLDOWN_TICKS = 100` (5 secondi) per decidere se il suono era ancora in cooldown. In `IsraelSimulatorConfig` esiste già `shofarCooldownTicks`, default 600 (30 secondi, intervallo 60–6000), con il getter `shofarCooldownTicks()`, ma nessuno lo leggeva. Il valore salvato in `ShofarCooldowns` restava giusto; a cambiare era solo la durata, che non seguiva la config. Sinagoga e tefillin non c'entrano: restano a 12000 tick.

Funzionamento aspettato: la durata del cooldown dello shofar è `shofarCooldownTicks`. Se la config non è caricata, si usa il default 600, non 100. I tempi restano in `ShofarCooldowns` (`israel_simulator:shofar_blasts`).

Come è stato risolto: `ShofarManager.cooldownTicks()` legge `IsraelSimulatorConfig.shofarCooldownTicks()`, e `isOnCooldown` confronta con quel valore. La costante 100 è stata tolta. `FestivalsAndCalendarTest` carica una config in memoria con 250 tick e fallisce se a 249 tick il cooldown è già finito o se a 250 è ancora attivo: così non passa né con 100 né con un 600 fisso. Senza config caricata il test si aspetta 600. Un server Minecraft non è stato avviato. Nessun commit.

## I villaggi comprano i raccolti ovunque

Problema riscontrato: `ModGameEvents.onEntityInteract` prova in catena agricoltura, Mar Morto, deserto, Tel Aviv, Jaffa, Jerusalem, sinagoga rurale e `IsraelNpcManager`. Se uno di questi torna `true`, l'evento viene annullato e la GUI del villager vanilla non si apre. Mar Morto, deserto, Tel Aviv, Jaffa e Jerusalem controllavano già il bioma. `AgriculturalTrades.tryTrade` no: bastava avere in mano grano, carote, patate, barbabietole, olive, datteri o agrumi, e qualunque villager in qualunque bioma comprava. `RuralSynagogueTrades` faceva lo stesso con una Prayer Note, o con uno shekel se in testa c'era qualcosa. `handleNpcInteraction` chiamava `getOrAssignNpcData`, assegnava una professione a ogni villager e tornava sempre `true`, anche solo per il saluto. La GUI vanilla non si apriva mai.

Funzionamento aspettato: le economie restano regionali. Fuori dal contesto giusto l'interazione non viene annullata e si apre il commercio vanilla. I trade non vanno tolti. Non serve un nuovo tipo di entità NPC.

Come è stato risolto: il gate è il bioma `israel_simulator:israeli_agriculture` (`ModBiomes.ISRAELI_AGRICULTURE`), la regione agricola/rurale del design (§29). `AgriculturalTrades.acceptsBiome` e `RuralSynagogueTrades.acceptsBiome` sono veri solo per quell'id; `tryTrade` e `tryInteract` tornano `false` prima di consumare oggetti se il bioma del villager non coincide. Gli altri trade regionali restano sui biomi che avevano già. `IsraelNpcManager.handleNpcInteraction` non assegna più una professione al volo: interviene solo se `getNpcData` trova un NPC già registrato con `registerNpcData` (`shouldHandleRegisteredNpc`). `VillagerInteractGate.shouldCancelVanillaGui` annulla l'evento solo se un trade regionale è andato a buon fine oppure se quell'NPC registrato ha gestito l'interazione. `VillagerTradeHijackTest` fallisce se il grano è accettato fuori da `israeli_agriculture`, se la sinagoga rurale non ha lo stesso gate, se `handleNpcInteraction` usa ancora `getOrAssignNpcData`, o se la GUI viene annullata quando nessuno ha gestito l'interazione. Un mondo Minecraft non è stato avviato. Nessun commit.

## La vite dava datteri

Problema riscontrato: `GrapevineBlock.getBaseSeedId` e il raccolto al click destro usavano `ModItems.DATES`. A maturità il click destro droppava 1–3 datteri e riportava l'età a 3. La loot table `data/israel_simulator/loot_table/blocks/grapevine.json` a età 7 dava `israel_simulator:dates`, altrimenti `minecraft:wheat_seeds`. Non esisteva un item uva e non c'era un BlockItem della vite: si piantava con i datteri. Il blockstate usa `minecraft:block/wheat_stage0`–`wheat_stage7`, quindi la vite si vede come grano. I datteri restano il frutto della palma (`date_palm_leaves`).

Funzionamento aspettato: la vigna produce uva, non datteri e non semi di grano. I datteri restano un cibo a parte, sulla palma. Si pianta con l'uva, come prima si piantava con i datteri. Non serve una ricetta nuova.

Come è stato risolto: aggiunto `ModItems.GRAPES`, cibo pareve con gli stessi valori degli agrumi (nutrizione 2, saturazione 0.3), più leggero dei datteri (3 e 0.4). Seme e raccolto della vite, e entrambi i rami della loot table, puntano a `israel_simulator:grapes`. I datteri non sono stati tolti. Nessuna texture nuova: l'item riusa `minecraft:item/sweet_berries`. Il modello del blocco resta il grano, perché è l'unica coltura vanilla con gli stadi 0–7; carote, patate e barbabietole arrivano solo allo stadio 3 e cambiare i model romperebbe il blockstate. Non è una modifica da una riga. `GrapevineHarvestTest` fallisce se il codice del raccolto o la loot table citano datteri o `wheat_seeds`, e se manca l'uva; controlla anche che i datteri restino sulla palma. Un mondo Minecraft non è stato avviato. Nessun commit.

## Ricette finte e cibi delle feste non mangiabili

Problema riscontrato: i JSON in `data/israel_simulator/recipes` erano `minecraft:crafting_shapeless` con un solo ingrediente vanilla. Grano dava challah, falafel e matzo. Zucchero dava hamantash, rugelach, sufganiyah e tahini. Barbabietola dava hummus, patata sabich, uovo shakshuka. L'hummus non usava il tahini. In più la cartella è `recipes`, mentre Minecraft 26.2 carica `recipe` (come già fanno `loot_table` e `advancement`), quindi quelle ricette non entravano in gioco. `FestivalItems.matzo`, `sufganiyah` e `hamantash` costruivano un `Item` normale, senza cibo. In `ModItems` i tre erano già registrati con `.food(...)`, e in `IsraelFoodProperties` c'erano già nutrizione e saturazione. Il design (§17) non dà numeri: chiede ricetta, ingredienti, nutrizione e saturazione. I tre cibi sono nel tag pareve.

Funzionamento aspettato: ogni ricetta usa almeno due ingredienti veri, con gli id risultato invariati. L'hummus include il tahini. Matzo, sufganiyah e hamantash si mangiano e restituiscono fame e saturazione modeste, già definite accanto agli altri cibi. Nessuna coltura nuova.

Come è stato risolto: le ricette stanno in `data/israel_simulator/recipe`, stesso contenuto di `ModRecipeProvider`. Nutrizione lasciata com'era: matzo 4 e saturazione 0.5, sufganiyah 5 e 0.6, hamantash 4 e 0.5. `FestivalItems` ora applica quei `FoodProperties` e `ModItems` registra da lì. Ingredienti: tahini = semi di grano x2 + ciotola (non esiste il sesamo); falafel = semi, grano e barbabietola, ne escono 3 (la barbabietola sta per i ceci); hummus = tahini + barbabietola + ciotola; shakshuka = uovo + barbabietola + ciotola (la barbabietola sta per il pomodoro); sabich = pane + uovo + patata + tahini (non c'è la melanzana, la patata è il ripieno); challah = grano x3 + uovo + zucchero; rugelach = grano + zucchero + fave di cacao, ne escono 2; matzo = grano x2 + secchio d'acqua (niente uovo né zucchero; il secchio torna vuoto come resto di crafting); sufganiyah = grano + zucchero + bacche dolci; hamantash = grano + zucchero + datteri. Datteri, olive e agrumi erano solo nel generatore e non nei JSON: ora sono committati insieme, così non divergono. Gli agrumi non sono più solo bacche luminose: bacche luminose + zucchero, perché non c'è un frutto agrume. Datteri = bacche dolci + zucchero. Olive = alga + semi, lo stesso sostituto di prima. `DataAndResourceValidationTest` fallisce se una ricetta ha un solo ingrediente, se l'hummus non ha il tahini, se manca il componente cibo, o se i JSON non stanno in `recipe`. Un mondo Minecraft non è stato avviato. Nessun commit.

## Cooldown rimasti in mappe statiche

Problema riscontrato: dopo sinagoga, tefillin, shofar e Western Wall, restavano due orologi di gioco in `ConcurrentHashMap` statiche. `EasterEggManager.LAST_EASTER_EGG_INTERACTION` (100 tick, 5 secondi) blocca la battuta del cittadino e il gatto con la kippah. `TransportStopBlock.LAST_TRANSIT_TIME` blocca il giocatore per 60 tick (3 secondi, così non si ripaga la corsa a raffica) e il suono del villager sulla fermata per 600 tick (30 secondi). Al riavvio del server le mappe si svuotano. Le altre mappe statiche non sono cooldown: `ReputationManager.REPUTATION_MAP` è il punteggio di fazione, non un orario; `QuestManager.COMPLETED_QUESTS` è l'insieme delle quest già chiuse, non un timer; `CityLifeManager.LAST_PATHFINDING_TICKS` è un freno di pathfinding da 40 tick, in memoria, non una ricompensa; `WorldEventManager.ACTIVE_EVENTS` è lo stato dell'evento in corso, con i tick di partecipazione e i premiati dentro l'evento, non una mappa UUID-tempo salvata; `PlayerLandmarkTracker` tiene landmark e regioni visitate; `IsraelNpcManager.NPC_DATA_MAP` è la cache degli NPC; `IsraelEconomy.BASE_PRICES` è il listino; `TransportNetwork.STOPS` è il registro delle fermate; `PerformanceManager.CHUNK_NPC_COUNTS` conta gli NPC nel chunk.

Funzionamento aspettato: i due orologi restano sul mondo del server, per dimensione, e sopravvivono a un riavvio. La durata non cambia: easter egg 100 tick, transito giocatore 60, suono villager 600. Le mappe che non sono cooldown non vanno spostate su `SavedData`.

Come è stato risolto: `EasterEggCooldowns` (`israel_simulator:easter_egg_interactions`, campo NBT `interactions`) e `TransitCooldowns` (`israel_simulator:transit_times`, campo `transits`), stesso schema di `WesternWallCooldowns`. `EasterEggManager` e `TransportStopBlock` non hanno più una `Map` statica. I test `EasterEggAndRarityTest` e `TransportationTest` falliscono se manager, blocco o saved data hanno una `Map` statica, e ricaricano il codec come dopo un riavvio. Un server Minecraft non è stato avviato. Nessun commit.

## Reputazione e quest finite solo in memoria

Problema riscontrato: `ReputationManager.REPUTATION_MAP` e `QuestManager.COMPLETED_QUESTS` erano ancora `ConcurrentHashMap` statiche. Non sono cooldown: la prima tiene i punteggi di fazione (-100..100), la seconda l'insieme delle discovery quest già chiuse. Al riavvio del server entrambe si svuotano: i punteggi tornano a 0 e le quest si possono richiudere per rifare le ricompense. Pathfinding, cache NPC e stato eventi non c'entrano.

Funzionamento aspettato: punteggi e quest finite restano sul mondo del server, per dimensione, e sopravvivono a un riavvio. La semantica non cambia: clamp -100..100, una sola chiusura per quest, ricompense solo alla prima chiusura. Non devono diventare timer/cooldown. Niente mappe statiche come fonte di verità.

Come è stato risolto: `ReputationScores` (`israel_simulator:reputation_scores`, campo NBT `scores` con player/faction/score) e `CompletedQuests` (`israel_simulator:completed_quests`, campo `completed` con player/quest), stesso schema SavedData di `WesternWallCooldowns`. `ReputationManager` e `QuestManager` leggono e scrivono solo tramite quelle istanze (da `ServerLevel` in produzione). `IsraelNpcManager` e `IsraelVillagerTrades.calculateAdjustedPrice` passano `ReputationScores`. I test `ReputationFrameworkTest` e `QuestAndDiscoveryTest` falliscono se manager o saved data hanno una `Map` statica, e ricaricano un punteggio campione e una quest campione dal codec come dopo un riavvio. Un server Minecraft non è stato avviato. Nessun commit.

## Jerusalem fuori dal tag comune is_overworld

Problema riscontrato: `data/c/tags/worldgen/biome/is_overworld.json` elenca `mediterranean_coast`, `israeli_agriculture`, `judean_desert`, `dead_sea` e `urban_area`, ma non `israel_simulator:jerusalem`. Jerusalem c'è già nel tag del mod `israel_simulator:is_israel_region`. Il tag `c:` non piazza i biomi nel mondo: ci pensa `OverworldBiomeBuilderMixin`. Resta un buco per chi legge `c:is_overworld` come elenco degli overworld del mod. Il JSON è scritto a mano: `IsraelSimulatorData` genera solo tag item, ricette e lingua, non questo tag.

Funzionamento aspettato: `israel_simulator:jerusalem` sta in `c:is_overworld` insieme agli altri biomi israeliani, stesso stile (stringa nell'array `values`, `replace: false`). Il tag del mod non cambia. Il mixin non cambia.

Come è stato risolto: aggiunta la riga `israel_simulator:jerusalem` in `is_overworld.json`, dopo `urban_area`. Nessun generatore da aggiornare. `CommonOverworldBiomeTagTest` fallisce se gli altri cinque biomi ci sono e jerusalem no. Prima della riga il test falliva; dopo, `./gradlew test --tests com.israelsimulator.world.CommonOverworldBiomeTagTest` e `./gradlew test` sono passati. Un mondo Minecraft non è stato generato. Nessun commit.

## Il disco Hava Nagila suonava una traccia vanilla

Problema riscontrato: `music_disc.hava_nagila` in `sounds.json` puntava a `minecraft:music/game/calm1`, una musica vanilla, non a Hava Nagila. Non c'era un ogg del mod. L'item `hava_nagila_disc` era già un disco da jukebox (`jukeboxPlayable` sulla canzone `israel_simulator:hava_nagila`), ma la canzone durava 150 secondi e non corrispondeva a un file vero.

Funzionamento aspettato: il jukebox suona l'ogg di Hava Nagila del mod, non una lista vuota e non un file vanilla mancante o sostitutivo. L'id dell'item resta `hava_nagila_disc`.

Come è stato risolto: il file di Wikimedia Commons (`File:Hava_nagila.ogg`) era la registrazione sbagliata ed è stato sovrascritto. L'audio ora viene dal video indicato dall'utente, https://www.youtube.com/watch?v=vHSNZK4Je-Y , che ha descritto come la registrazione originale e di pubblico dominio. Oltre a quella affermazione non è stata verificata una licenza. Il file è `assets/israel_simulator/sounds/records/hava_nagila.ogg` (Ogg Vorbis, 165,3 secondi). `sounds.json` indica `israel_simulator:records/hava_nagila` con `stream: true`. La durata in `jukebox_song/hava_nagila.json` è 165.3. `DataAndResourceValidationTest.testHavaNagilaDiscOggPresent` resta il controllo che l'ogg esista e non sia vuoto, e che il nome non sia vanilla. I test non sono stati rieseguiti, perché Gradle è occupato dal client già aperto. Nessun commit.

## Jerusalem city era un guscio 7×7, non una città

Problema riscontrato: `israel_simulator:jerusalem_city` era un solo NBT 7×7 (guscio di arenaria con marker `yellow_terracotta` a `[3,6,3]`). In gioco sembrava un tetto di arenaria, non una città. Il JSON era già `minecraft:jigsaw` con `start_pool` del mod, ma il pool aveva un solo pezzo.

Funzionamento aspettato: una piccola città vecchia multi-pezzo come un villaggio vanilla: piazza centrale con strade, case in pietra/arenaria a tetto piatto, bancarelle dello shuk, una piccola sinagoga a cupola, un pozzo e un tratto di mura con porta. Stesso id `jerusalem_city`, stessi biomi, `terrain_adaptation: beard_thin`. Il marker giallo resta.

Come è stato risolto: pezzi NBT in `structure/jerusalem/` (DataVersion 4903) e pool `jerusalem_city` (start → plaza), `jerusalem/streets`, `jerusalem/terminators`, `jerusalem/buildings`. Generatore `scripts/worldgen/gen_jerusalem_city.py`. `StructureFrameworkTest` accetta il start sulla plaza; `JerusalemCityStructureTest` controlla pool e NBT. Il file root `structure/jerusalem_city.nbt` resta come marker. Unit test Gradle, non un passaggio in-game.


## Jerusalem city: sinagoga assente, edifici su mucchi, porta chiusa nel gate

Problema riscontrato: in un mondo fresco (`jerusalem-city-test`, città a `[3680,~,2752]`) la città generava case, shuk, gatehouse e villager, ma (1) `synagogue_small` non compariva mai (peso 1 nel pool buildings e pezzo grande che perdeva per collisione), (2) case/shuk restavano su mucchi di terra/erba sopra il livello strada, con terra sotto le fondamenta, (3) `wall_gate` aveva il passaggio N–S mentre il jigsaw guardava ovest: entrando dalla strada si finiva contro un muro di arenaria con porta di quercia chiusa.

Funzionamento aspettato: ogni città ha la sinagoga a cupola attaccata alla piazza; edifici a livello strada anche in collina; il gate è un passaggio aperto allineato alla strada, senza porta che lo chiude.

Come è stato risolto: pool `jerusalem/landmarks` (solo sinagoga) collegato al lato nord della plaza (i tre lati restano strade). Fondamenta di 3 blocchi di arenaria sotto case/shuk/sinagoga/pozzo/gate/plaza; `terrain_adaptation` da `beard_thin` a `beard_box`; strade ancora `terrain_matching`, edifici `rigid`. `wall_gate` riscritto con tunnel E–W aperto (niente porta). Rigenerato con `scripts/worldgen/gen_jerusalem_city.py`. I test `JerusalemCityStructureTest` controllano landmarks, beard_box e assenza di porta nel gate. Unit test Gradle, non un passaggio in-game su mondo nuovo.

## Jerusalem city: fondamenta ancora falliscono sulle colline

Problema riscontrato: in `jerusalem-city-test2` (città a `[304,~,−2976]`, collina verso sud) sinagoga e gate ok, ma le fondamenta no. Piazza ~Y112 e strada ovest ~Y89: stall e strada `terrain_matching` su mensole di erba/terra sopra grotte, piattaforme di sabbia con strapiombo, terra sotto l'arenaria. Il quartiere sud su terreno dolce era a posto. `beard_box` era già nel JSON ma i pezzi `terrain_matching` non ricevono la densità del beard.

Funzionamento aspettato: città a un solo livello con la piazza, piedistallo di arenaria sotto i pezzi rigid, niente mensole fluttuanti sopra grotte, meno spawn su biomi ripidi.

Come è stato risolto: fondamenta da 3 a **8** blocchi di arenaria su plaza/case/shuk/sinagoga/pozzo/gate **e strade**; pool strade/terminator da `terrain_matching` a **`rigid`** (allineate alla piazza; `beard_box` riempie sotto); biomi: tolti `minecraft:meadow` e `minecraft:windswept_hills`, restano `israel_simulator:jerusalem` e `minecraft:savanna_plateau`. `terrain_adaptation` resta `beard_box`. Unit test aggiornati. Non un passaggio in-game.

## Jerusalem city: plinto di arenaria e gate come muro pieno

Problema riscontrato: in `jerusalem-city-test3` la città era una piattaforma rigid con muri di arenaria alti 8 blocchi sul pendio, mensole di erba a ovest e vuoti sotto i ledges. Il `wall_gate` attaccato come building laterale dalla piazza sembrava un muro pieno di stone brick (il fianco), con una buca di arenaria davanti; il passaggio non seguiva l'asse della strada.

Funzionamento aspettato: città immersa nel terreno (beard_box, niente scogliere di fondamenta), strade che seguono il pendio come un villaggio vanilla, gate con tunnel aperto allineato alla strada.

Come è stato risolto: strade/terminator di nuovo `terrain_matching` senza sottostrato profondo; pezzi rigid (plaza/case/shuk/sinagoga/pozzo) con fondamenta **2** blocchi; `beard_box` e biomi `jerusalem`+`savanna_plateau` invariati. `wall_gate` spostato nel pool strade come arco con jigsaw `street` a ovest e est e tunnel E–W aperto (niente porta, niente attachment laterale come building). La sinagoga a sud della piazza è rotazione casuale, non un bug. Unit test aggiornati. Non un passaggio in-game.

## Jerusalem city allineata allo stile villaggio vanilla

Problema riscontrato: le fondamenta profonde e `beard_box` producevano un plinto/fortezza; serviva lo stesso adattamento dei villaggi vanilla.

Funzionamento aspettato: come `minecraft:village_desert` / plains in 26.2 — strade `terrain_matching`, edifici `rigid`, `terrain_adaptation: beard_thin`, size 6, max_distance 80, `WORLD_SURFACE_WG`, start_height 0, case senza piedistallo profondo (pavimento a y=0, jigsaw ingresso a y=1).

Come è stato risolto: `FOUNDATION = 0`; JSON struttura allineato a village_desert (`beard_thin`, stessi size/distance/heightmap/hack); strade restano `terrain_matching`; gate resta arco nel pool strade. Biomi invariati (`jerusalem` + `savanna_plateau`). Unit test aggiornati.

## Jerusalem city: troppi wall_gate e fontana senza acqua (verifica save)

Problema riscontrato: in `jerusalem-city-test4` (start a X=-2784 Z=-336) il terreno si adatta come un villaggio, ma dalla collina alta non si vedevano piazza/sinagoga (sono a Y≈101; le strade salgono a Y≈130–145). Nel save: 6× `wall_gate` su 93 pezzi; fontana con pilastro chiseled/lantern ma **senza acqua** (il blocco `minecraft:water` del template non risulta nel chunk).

Funzionamento aspettato: 1–2 gate per città; fontana riconoscibile; piazza+sinagoga presenti (anche se più in basso sul pendio).

Come è stato risolto: pesi strade 12/6/6 e gate 1 (~1/25); fontana con `water_cauldron[level=3]` al posto dell'acqua fluida; marker yellow terracotta sul pavimento. Analisi headless del save (strutture + blocchi). Unit test sul peso del gate. Serve mondo nuovo per i pezzi.

## Jerusalem city: troppi pozzi (verifica headless 8e8de2b)

Problema riscontrato: in `jerusalem-verify5` (seed -2399866279920343874, start [-6608,~,-7104]) gate=0 OK, sinagoga×1+dome+glowstone OK, fontana `water_cauldron[level=3]` OK, marker yellow OK, streets-over-air 0.26%, plaza Y93 allineata alle strade (76–99, non in una fossa). Ma **9× well** (peso 2 nel pool buildings).

Come è stato risolto: peso `well` da 2 a 1 nel pool `jerusalem/buildings` (generatore + JSON + unit test). Re-verifica headless su mondo nuovo.

Re-verifica `jerusalem-verify6` (commit `1619fb5`, seed -6555682282253742967, start [4672,~,6256]): well 9→6 (migliorato ma ancora >4), gate=3, sinagoga×1+glowstone OK, cauldron level=3 OK, marker OK, streets-over-air 0.74%, floating rigid 0, plaza Y110 aperta (92% aria sopra; strade 62–111). Un solo ciclo fix/re-verify come da brief.

## Advancement JSON: errori di parse su NeoForge 26.2

Problema riscontrato: all'avvio del dedicated server tre advancement di `israel_simulator` fallivano il parse (`Couldn't parse data file`):
1. `combat/hava_nagila` — predicato entita legacy `{"type":"israel_simulator:bibi_boss"}` (entity_sub_predicate_type invalido su 26.2; l'entita `bibi_boss` e registrata)
2. `exploration/dead_sea_tourist` — item inesistente `israel_simulator:dead_sea_salt` (nel mod c'e `salt_block`)
3. `technology/startup_founder` — item inesistente `israel_simulator:drone` (nel mod c'e `drone_part`)

Come e stato risolto: predicato di kill allineato a vanilla 26.2 (`minecraft:entity_properties` + `minecraft:entity_type`); tourist usa `salt_block`; founder usa `drone_part`; descrizioni en/it aggiornate. Aggiunto unit test che valida ogni id item/entity negli advancement JSON contro i registry del mod. Verifica headless su mondo `advancement-parse-verify1`: nessuna riga `Couldn't parse` per israel_simulator; `Loaded 1702 advancements`. Grep su recipe/loot: i food (falafel, hummus, ecc.) sono registrati; nessun altro riferimento advancement della stessa classe da correggere ora.

## Superfici biome israeliani (override noise_settings)

Problema riscontrato: i biome `israel_simulator:*` usavano la superficie vanilla (erba/terra) perche i biome modifier NeoForge 26.2 non espongono un API surface, e le `surface_rule` vanilla filtrano solo id biome hardcoded.

Funzionamento aspettato: ogni biome israeliano ha una superficie coerente (es. `jerusalem_stone` a Gerusalemme, sabbia/sale al Mar Morto, sabbia rossa nel Negev) senza cambiare i biome non-israeliani.

Come e stato risolto: script `scripts/worldgen/gen_israel_surface_rules.py` copia i JSON vanilla 26.2 (`overworld`/`amplified`/`large_biomes`) dal jar e antepone regole biome dentro il ramo `above_preliminary_surface`. Agricoltura resta vanilla. Verifica headless `israel-surfaces-verify1` (seed -3194586807213286118): jerusalem_stone dominante a jerusalem; red_sand a judean_desert; sand+grass a mediterranean_coast; paved_road/stone patch in urban_area; plains invariato (grass/dirt). `dead_sea` locate ha timeout (biome raro); nessun errore codec/worldgen nel log.

