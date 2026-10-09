# Test log

## Avviso "runtime member-stripping behaviour, no longer present" a ogni avvio

Problema riscontrato: a ogni avvio, prima del menu principale, NeoForge 26.2 mostrava l'avviso sul comportamento di member-stripping a runtime non più presente. La causa era l'annotazione deprecata `@OnlyIn(Dist.CLIENT)` su 7 classi client (`DollarBillParticle` e il suo `Provider`, `BicycleModel`, `BicycleRenderer`, `ChildZombieMinionRenderer`, `IceAgentRenderer`, `JeffreyEpsteinRenderer`, `TrumpMinibossRenderer`).

Funzionamento aspettato: nessun `@OnlyIn` nel codice; il codice solo-client resta nel package `client`, registrato da `IsraelSimulatorClient` (`@Mod(dist = Dist.CLIENT)` / `@EventBusSubscriber(value = Dist.CLIENT)`) e mai referenziato dal codice comune o server.

Come è stato risolto: tolti tutti gli `@OnlyIn` e i relativi import, e l'import inutilizzato di `RegisterClientPayloadHandlersEvent` da `ModNetworking` (il payload client si registra già in `IsraelSimulatorClient`). Nuovo `ClientSideSeparationTest`: fallisce se un sorgente contiene `@OnlyIn` o se una classe fuori dai package `client` referenzia `com.israelsimulator.client`, `net.minecraft.client` o `net.neoforged.neoforge.client`. `./gradlew build`: 312/312 test. `runServer` headless: "Done" in 3 s senza errori di class loading. `runClient` fino alla schermata iniziale: l'avviso non compare più nei log e non ci sono errori nuovi (solo quelli dell'ambiente senza audio). Non provato dall'utente in gioco.

## I villager non aprivano più la GUI e parlavano in chat

Problema riscontrato: al click destro su un villager la GUI vanilla non si apriva e arrivavano messaggi in chat (battuta easter egg "[Citizen]" e dialoghi/hijack dei trade regionali). `ModGameEvents.onEntityInteract` intercettava ogni `AbstractVillager`: easter egg, catena di trade regionali (agricoltura, Mar Morto, deserto, Tel Aviv, Jaffa, Jerusalem, sinagoga rurale), dialogo NPC e cancel dell'evento tramite `VillagerInteractGate`. Anche fuori dal bioma giusto il click non era mai "pulito".

Funzionamento aspettato: i villager si comportano come villager vanilla in ogni caso. Lo scambio di valuta (shekel, agorot, monete antiche ↔ oggetti di valore) vive su un'entità separata, il Cambista (Money Changer), non sui villager.

Come è stato risolto: rimosso l'intero ramo `AbstractVillager` da `onEntityInteract` (resta solo l'easter egg del gatto con la kippah) e cancellato `VillagerInteractGate`. Il saluto easter egg è diventato `EasterEggManager.triggerTraderEasterEgg` e ora lo manda il Cambista quando apre la schermata di scambio (server-side, cooldown condiviso, 15%). Nuova entità `MoneyChangerEntity` (`entity/npc/`), estende `AbstractVillager`: GUI merchant vanilla, trade interamente data-driven (`trade_set/money_changer.json` + tag `villager_trade/money_changer` con 11 trade: agorot → lingotto d'oro, shekel → smeraldo/diamante/ametista/mezuzah/intaglio in legno d'ulivo/shofar/drone_part, moneta antica → diamanti/mela d'oro incantata, diamante → 12 shekel con prezzo d'acquisto 16 ≥ vendita 12 per evitare loop), restock giornaliero (`LastRestockDay` salvato in NBT), spawn naturale nei biomi città via biome modifier `add_money_changer_spawns`, uovo generatore `money_changer_spawn_egg` (tab creative Spawn Eggs), texture procedurali originali. Registrazione in `ModEntities` (CREATURE, spawn placement su `ANIMALS_SPAWNABLE_ON`), renderer `MoneyChangerRenderer`, lang en/it. Nome scelto per funzione di gioco (AGENTS.md §23), non per l'appartenenza etnica richiesta in origine. `VillagerTradeHijackTest` ora fallisce se `onEntityInteract` tocca un villager in qualunque modo e verifica i dati del Cambista (niente item rari endgame nei trade, anti-loop diamante). `./gradlew test`: 253/253 dopo aver allineato i due test della Rabbi's Crown al nuovo approccio flat + equipment asset (commit "rabbi v5"): `ClientRenderingValidationTest` e `RabbisCrownAndBlessedTraderTest` prima cercavano gli elementi 3D `hat_brim`/`payot`/`beard` rimossi dal modello; ora verificano la item definition `items/rabbis_crown.json`, il modello flat `minecraft:item/generated` con `layer0`, l'equipment asset `equipment/rabbis_crown.json` con i layer `humanoid` e `humanoid_baby` e le due texture equipment. Server dedicato avviato con `runServer`: il datapack carica senza errori, `summon israel_simulator:money_changer` spawna l'entità (20 HP, NBT `LastRestockDay` presente). L'apertura della GUI in gioco non è stata verificata: nessun client interattivo in questo ambiente.

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

Come e stato risolto: script `scripts/worldgen/gen_israel_surface_rules.py` copia i JSON vanilla 26.2 (`overworld`/`amplified`/`large_biomes`) dal jar e antepone regole biome dentro il ramo `above_preliminary_surface`. Agricoltura resta vanilla. Verifica headless `israel-surfaces-verify1`/`verify3` (seed -3194586807213286118): jerusalem_stone dominante a jerusalem; red_sand a judean_desert; sand+grass a mediterranean_coast; paved_road/stone patch in urban_area; plains invariato (grass/dirt). Nessun errore codec/worldgen nel log.

### Gap-close pre-PR (verify3, stesso seed)

**Dead Sea — generazione (priorita utente):** il biome E iniettato in `OverworldBiomeBuilderMixin` (2 `ParameterPoint`: depth 0.0 e depth 1.0) e compare nei tag `c:is_overworld` / `is_hot` / `is_dry` / `is_israel_region` / `has_dead_sea_salt`. Non e un buco di registrazione. Lo slot climatico e pero ambiguo/quasi vuoto: temp 0.80–1.00, humidity −0.80–−0.20, continentalness 0.25–0.70 (**inland**), erosion 0.40–0.90 (**alta**, tipicamente costiera in vanilla), weirdness 0.30–0.80. Zero overlap 1D su erosion e weirdness con `judean_desert` (isola isolata, non variante del deserto). Empirico senza `/locate`: parse di 10377 chunk in verify3 → `dead_sea` presente in soli **41** chunk di superficie, tutti intorno a **(−24320, −5344)**; altrove e assente. `/locate biome` non “rompe” il mondo: cerca in un raggio ~6400 un hypercube rarissimo e resta bloccato a lungo. Superficie nel cluster: sand/sandstone + patch `salt_block` (es. 112 sand / 48 salt su 212 colonne filtrate). **Fix proposto (non implementato):** dare a `dead_sea` uno slot raggiungibile accanto a `judean_desert` (stesso cluster hot/arid/inland, sotto-range di erosion/weirdness che esiste davvero, eventualmente offset piu basso come variante salt-flat). In piu: `depth point(1.0F)` e etichettato “hills” ma depth 1.0 e sottosuolo — togliere o riservarlo ai cave; spiega anche perche `israeli_agriculture` compare spesso underground.

**Agricoltura (superficie, non bordo):** interior grass a **(−15804, 67, −5919)** e vicini (106 colonne `grass_block`+`dirt` con vicini agriculture). Nessuna regola surface iniettata → resta vanilla come da design.

**Texture/modelli:** `jerusalem_stone`, `salt_block`, `paved_road` avevano gia blockstate + block model + item model. Nessun PNG del mod; riusavano gia texture vanilla (non purple/black). Aggiornati a riferimenti piu chiari in 26.2: `cut_sandstone`, `white_concrete_powder`, `gray_concrete` (PNG presenti nel jar). Aggiunto `RegisteredBlockAssetsTest` che per ogni `registerBlock` verifica blockstate/model/texture risolvibile (mod PNG o jar vanilla).

**Coordinate client (seed −3194586807213286118):** Jerusalem stone sotto i piedi a **x=−3872, y=105, z=−1934**.

## Recinzioni e vetri delle strutture Jerusalem non si collegavano

Problema riscontrato: nei pezzi jigsaw di `jerusalem_city` (case, shuk, sinagoga, pozzo, strade) i blocchi `oak_fence`, `glass_pane` e `sandstone_wall` erano salvati nell'NBT con tutte le facce a `false`/`none` (il muro aveva solo `up=true`). In gioco restavano pali/lastre isolati, senza collegarsi ai vicini. Il generatore `gen_jerusalem_city.py` scriveva helper `FENCE`/`GP`/`SW` con proprieta fisse e non ricalcolava i vicini in `Structure.save()`.

Funzionamento aspettato: ogni fence/pane/muro nel template ha le proprieta di connessione coerenti con i vicini (stessa famiglia o cubo pieno; i muri usano `none`/`low`/`tall` e `up` come in vanilla).

Come e stato risolto: aggiunto `scripts/worldgen/connect_blocks.py` che, data la griglia completa, calcola north/south/east/west (e up per i muri) e viene chiamato da `Structure.save()` prima di scrivere l'NBT. Rigenerati tutti i pezzi `structure/jerusalem/*.nbt` (stesso conteggio blocchi, solo proprieta diverse). `JerusalemConnectedBlocksTest` fallisce se un connectable con vicino stesso-famiglia/solido resta scollegato. Verifica headless su mondo fresco con `jerusalem_city`.

## Baule di Jerusalem senza loot table

Problema riscontrato: case, shuk e (dopo) sinagoga di `jerusalem_city` avevano chest/barrel senza NBT di blocco: niente `LootTable`, quindi in gioco restavano vuoti. Esistevano gia JSON in `loot_table/chests/` (formato legacy min/max senza `minecraft:uniform`) ma non erano collegati ai template. I 15 placeholder non hanno contenitori.

Funzionamento aspettato: ogni chest/barrel di Jerusalem punta a una loot table `israel_simulator:chests/...` con `LootTableSeed` 0 (riempimento al primo open). Le tabelle usano number provider 26.2. Casa → `jerusalem_house` / barile → `jerusalem_pantry`; shuk → `jerusalem_bazaar`; sinagoga → `synagogue` (un chest aggiunto).

Come e stato risolto: normalizzati tutti i JSON in `loot_table/**` a uniform 26.2; aggiunti/aggiornati house/pantry/bazaar/synagogue; `CHEST`/`BARREL` in `gen_jerusalem_city.py` scrivono `id`+`LootTable`+`LootTableSeed`; rigenerati gli NBT. Test `LootTableValidationTest` + `JerusalemChestLootTest`. Verifica headless su seed -3194586807213286118, citta [-1744,~,4192]: 51 contenitori con `israel_simulator:chests/*` (10 house, 10 pantry, 30 bazaar, 1 synagogue), nessun errore di parse loot; `/loot spawn` ha droppato 3–5 item per tabella.

## Biomi israeliani raggruppati e strutture solo nel proprio bioma

Problema riscontrato: i sei biomi israeliani erano sparsi nel climate space (fasce temperate diverse); `dead_sea` stava in un angolo erosione/weirdness quasi vuoto e `/locate` restava appeso. Il mixin registrava anche depth `1.0`, cosi i biomi (soprattutto agriculture) comparivano spesso sottoterra. Le 16 strutture accettavano biomi vanilla di fallback (`savanna_plateau`, plains, desert, tag `is_israel_region` con plains/desert/savanna/beach/badlands): `jerusalem_city` nasceva anche fuori da `jerusalem`.

Funzionamento aspettato: una striscia calda/arida Ovest→Est (costa → urban → agriculture → jerusalem → judean_desert, con `dead_sea` come pocket di weirdness del deserto). Solo depth `0.0`. Ogni struttura solo nel proprio bioma israeliano; `is_israel_region` elenca solo i 6 biomi del mod.

Come e stato risolto: `IsraelBiomeClimateParams` + mixin aggiornato; JSON strutture e `gen_jerusalem_city.py` bloccati a un bioma; tag ristretto. Test climate (reachability/`dead_sea`) e lock strutture; verifica headless su piu seed.

Verifica headless seed `-3194586807213286118`: citta `jerusalem_city` a [-8912, -4352]; `/locate biome dead_sea` ok in ~951 blocchi ([-8112, -4864]); chunk Israel {'israel_simulator:israeli_agriculture': 90, 'israel_simulator:jerusalem': 296, 'israel_simulator:judean_desert': 222, 'israel_simulator:dead_sea': 172}; desert vanilla 49, savanna 891; agri surface 75 vs deep 5; distanze da jerusalem {"israel_simulator:mediterranean_coast": null, "israel_simulator:israeli_agriculture": {"min": 187.3, "median": 385.3, "n_j": 296, "n_o": 90}, "israel_simulator:urban_area": null, "israel_simulator:judean_desert": {"min": 0.0, "median": 851.6, "n_j": 296, "n_o": 222}, "israel_simulator:dead_sea": {"min": 102.4, "median": 919.3, "n_j": 296, "n_o": 172}}. Seed `42`: jerusalem+agriculture+urban+coast trovati; dead_sea/judean fuori raggio locate da quella citta (non ogni patch jerusalem ha deserto adiacente).
Problema aggiuntivo: su alcuni seed (42, 20261005) `judean_desert`/`dead_sea` non comparivano nel raggio di `/locate` dalla citta: jerusalem era troppo fresco/umido/collinare rispetto al deserto (temp da 0.70, erosione solo al bordo -0.22).

Funzionamento aspettato: il deserto giudeo e il Mar Morto restano adiacenti a jerusalem nella stessa striscia calda/arida.

Come e stato risolto (tweak): judean+dead_sea con T 0.60–1.00, H -1.00–-0.30, C 0.28–0.85, E -0.40–0.45; split weirdness judean -1.00–0.25 / dead_sea 0.25–1.00.

## Texture item: kippah e disco Hava Nagila

Problema riscontrato: le texture item di `kippah` e `hava_nagila_disc` erano placeholder/precedenti, non le opere fornite dall'utente.

Funzionamento aspettato: in inventario si vedono i PNG 32x32 dell'utente; i model item puntano a `israel_simulator:item/kippah` e `israel_simulator:item/hava_nagila_disc`.

Come e stato risolto: copiati `/workspace/user-textures/kippah.png` e `hava_nagila_disc.png` in `assets/israel_simulator/textures/item/` senza ridimensionare. I model erano gia sul path del mod.

## Coastal / urban structures

Problema: jaffa_port, mediterranean_village, tel_aviv_city erano placeholder.

Aspettato: geometria reale + loot.

Risolto: `gen_coastal_structures.py` (molo/magazzino, villaggio, torri urbane). Non testato in-game.
## Desert structures (ruins / oasis / resort)

Problema: desert_ruins, ein_gedi_oasis, dead_sea_resort erano placeholder.

Aspettato: geometria reale + chest loot.

Risolto: `gen_desert_structures.py` (archi/rovine, oasi con palme, resort con piscina). Non testato in-game.
## README / TODO alignment

Problema: README/TODO descrivevano visioni come se fossero già complete (strutture placeholder).

Aspettato: stato reale di main esplicito.

Risolto: sezione "Current implementation status" in README + checklist "Recently completed" in TODO.
## Texture uva / vite

Problema: grapevine riusava i modelli wheat; grapes senza texture dedicata.

Aspettato: texture 16x16 stile vanilla (vite/grappoli, non grano).

Risolto: PNG stage0-7 + item grapes; blockstate/modelli aggiornati. Non testato in-game.

## Agricultural farm structure

Problema: `agricultural_farm` era un placeholder 7x5x7.

Aspettato: fattoria reale (campi, irrigazione, serre, capanno, chest loot).

Risolto: `gen_agricultural_farm.py` + NBT 48x10x40, `beard_box`, loot `chests/agricultural_farm`. Non testato in-game.

## Western Wall: da placeholder a Kotel vero

Problema riscontrato: `western_wall.nbt` era una scatola cava 9x6x5 (stone bricks/calcite/gold/sea lantern) senza `western_wall_stone`, senza plaza e senza chest treasury. La preghiera richiede il right-click su `israel_simulator:western_wall_stone` (`ModGameEvents` → `WesternWallManager.tryPray`), quindi al placeholder non si poteva pregare in modo credibile.

Funzionamento aspettato: un muro lungo in pietra pale (corsi herodiani a setback), plaza pavimentata, mechitza (fence collegate), lectern/lanterne, alcova con chest `western_wall_treasury`, bioma solo `jerusalem`, terrain `beard_box`. Il giocatore prega toccando le pietre del muro dalla plaza.

Come e stato risolto: generatore `scripts/worldgen/gen_western_wall.py` (Structure.save + connect_blocks); NBT 56x20x42 con centinaia di `western_wall_stone` sulla facciata; JSON aggiornati. Test `WesternWallStructureTest`. Verifica headless: seed 20261005 wall a [-3488,~,-7040] (67 blocchi dalla citta), chest treasury in BB; seed -3194586807213286118 wall a [-8944,~,-4288].

## Western Wall: posa di preghiera

Problema: serviva una posa di preghiera (~3s), non uno swing di piazzamento.

Aspettato: sessione server di 60 tick, sync ai client, braccio teso e capo chino; poi reward.

Risolto: `WesternWallPrayerSession` + payload; mixin `HumanoidModel.setupAnim`; FP `RenderHandEvent` + pitch camera. Fallimenti immediati; successo differito. Non testato in-game.

## Strutture rimanenti: ricostruzione degli ultimi 7 placeholder

Problema riscontrato: `government_building`, `grand_market`, `startup_office`, `historical_house`, `synagogue`, `ancient_sanctuary` e `great_synagogue` erano scatole 7x6x7 o 9x7x9 vuote/placeholder.

Funzionamento aspettato: strutture reali complete con architettura caratteristica, arredi interni, marker corretti per `StructureFrameworkTest` e chest con loot table `israel_simulator:chests/...`.

Come e stato risolto: script `scripts/worldgen/gen_remaining_structures.py`:
- `government_building` (28x14x26, 10.192 blocchi): facciata con colonne in quarzo, assemblea con podio/leggio, archivio con librerie e chest `government_building`, marker `polished_deepslate`.
- `grand_market` (32x12x30, 11.520 blocchi): shuk coperto con banchi a tendina colorata, spezie, casse di frutta/verdura, fontana centrale e chest `grand_market`, marker `red_terracotta`.
- `startup_office` (26x14x26, 9.464 blocchi): open space moderno in cemento/vetro ciano, postazioni sviluppatori, sala server con rack/copper, area lounge e chest `startup_office` / `tel_aviv_tech_office`, marker `iron_block`.
- `historical_house` (24x11x24, 6.336 blocchi): casa storica di Gerusalemme con cortile alberato (olivo), muratura in pietra/mattoni, camino, tappeto, madia e chest `historical_house`, marker `bricks`.
- `synagogue` (26x13x26, 8.788 blocchi): sala di preghiera comunitaria con atrio, lavacro, banchi, bimah rialzata con leggio, aron kodesh con chest `synagogue_ark` e ner tamid, marker `purple_stained_glass`.
- `ancient_sanctuary` (30x12x34, 12.240 blocchi): santuario biblico del deserto con cortile recintato, altare dei sacrifici, lavacro, Santo con Menorah e Santo dei Santi con cassa `ancient_sanctuary` (fonte ultra-rara Rabbi's Crown), marker `chiseled_sandstone`.
- `great_synagogue` (36x18x36, 23.328 blocchi): grande cattedrale-sinagoga monumentale a Gerusalemme con loggiato a colonne, navata a doppia altezza con matroneo/balaustre, maestoso aron kodesh dorato, grande bimah in marmo e chest `synagogue_ark`, marker `blue_stained_glass`.
Test: `RemainingStructuresTest` + `StructureFrameworkTest` + `SynagogueFrameworkTest`.

## Quartieri di Tel Aviv: implementazione gameplay e spaziale

Problema riscontrato: i 6 quartieri di Tel Aviv (`WHITE_CITY`, `ROTHSCHILD`, `FLORENTIN`, `SARONA`, `STARTUP_DISTRICT`, `TAYELET_BEACH`) esistevano solo come costanti statiche di testo in `TelAvivDistricts.java` senza alcuna corrispondenza spaziale o di gameplay nel mondo.

Funzionamento aspettato: i quartieri devono essere riconosciuti nello spazio del bioma `urban_area`, avere moltiplicatori economici appropriati, professioni caratteristiche, tracciamento esplorativo server-authoritative e rappresentazione fisica nella struttura di Tel Aviv.

Come e stato risolto:
- `TelAvivDistricts.java`: aggiunta zonizzazione spaziale `getDistrictAt(BlockPos)` (griglia modulare 3x2 a celle 64x64 blocchi, garantendo che ogni zona contenga tutti e 6 i quartieri), lookup per ID, moltiplicatori economici specifici per tipologia merceologica (tech in Startup, cibo a Sarona, artigianato/antichità a Florentin, ecc.) e professioni raccomandate per gli NPC.
- `TelAvivDistrictManager.java`: monitoraggio tick server-side del giocatore nel bioma urbano, notifica actionbar al cambio quartiere (`Entering [District] - [Style]`), tracciamento delle scoperte uniche del giocatore.
- `ModGameEvents.java`: integrazione nel ciclo di tick del giocatore ogni 20 tick.
- `gen_coastal_structures.py`: `tel_aviv_city` espansa a 48x18x48 (41.472 blocchi) con settori dedicati a ciascun quartiere (spiaggia/Tayelet a ovest, boulevard alberato di Rothschild con chiosco centrale, White City Bauhaus con balconi, torre tecnologica Startup con server, loft artistici con murales a Florentin, mercato in pietra Templare a Sarona).
Test: `TelAvivDistrictsTest` e `CoastalStructuresTest`.

## Mar Morto: contiguità climatica e presenza vicino a Gerusalemme

Problema riscontrato: su determinati seed (come il seed 42), il Mar Morto non compariva nelle vicinanze di Gerusalemme perché `dead_sea` era confinato a una tasca di weirdness [0.25, 1.00] e temperatura minima 0.60, mentre Gerusalemme aveva weirdness [0.00, 0.45] e temperatura a partire da 0.55. Se una patch di Gerusalemme generava con weirdness inferiore a 0.25 (la maggioranza dei casi nella distribuzione gaussiana del rumore), il Mar Morto non poteva comparire adiacente.

Funzionamento aspettato: il Mar Morto e il Deserto di Giuda devono confinare direttamente con Gerusalemme lungo il gradiente di continentalità e su tutto lo spettro di temperatura e weirdness di Gerusalemme.

Come e stato risolto:
- `IsraelBiomeClimateParams.java`: `dead_sea` e `judean_desert` estesi a temperatura minima 0.55F (coprendo l'intera escursione di Gerusalemme), umidità massima -0.25F, continentalità minima 0.25F ed erosione minima -0.65F.
- Divisione di weirdness impostata a `0.00F`: `judean_desert` [-1.00F, 0.00F] e `dead_sea` [0.00F, 1.00F]. In questo modo, l'intero range di weirdness di Gerusalemme [0.00F, 0.45F] ricade pienamente nel dominio del Mar Morto, garantendo che verso est (continentalità crescente) il Mar Morto sia naturalmente adiacente e raggiungibile su seed come il 42.
Test: `IsraelBiomeClimateTest` (verifica reachability Monte Carlo 1,9M campioni, adiacenza climatica e overlap).

## Loot table mancanti per i blocchi paved_road e transport_stop

Problema riscontrato: i blocchi `paved_road` e `transport_stop` erano privi di un file JSON in `data/israel_simulator/loot_table/blocks/`. Se piazzati o trovati nel mondo e minati, non droppavano il proprio blocco item, scomparendo nel nulla.

Funzionamento aspettato: tutti gli 11 blocchi registrati nel mod devono avere una loot table valida che rilasci il corrispondente blocco/drop.

Come è stato risolto: create le loot table `paved_road.json` e `transport_stop.json` (tipo `minecraft:block`, drop di se stessi), ed esteso il test `DataAndResourceValidationTest` con `testAllModBlocksHaveLootTables` per garantire che tutti gli 11 blocchi registrati abbiano una loot table dedicata.

## Routing dei trasporti fermo sempre su Tel Aviv

Problema riscontrato: in `TransportStopBlock.java`, l'interazione con la fermata calcolava la destinazione usando `TransportNetwork.getNextStop("tel_aviv_central")` con l'ID fissato a Tel Aviv. In questo modo, interagire con qualunque fermata nel mondo (es. a Gerusalemme, a Jaffa o al Mar Morto) trasportava sempre e solo a Jaffa Clock Tower, senza permettere la prosecuzione lungo il circuito dei trasporti.

Funzionamento aspettato: la fermata determina la propria posizione nello spazio, identifica la fermata più vicina (`TransportNetwork.getNearestStop(pos)`) e calcola la fermata successiva nel circuito regionale.

Come è stato risolto: implementato `TransportNetwork.getNearestStop(BlockPos pos)` basato sulla distanza euclidea minima rispetto alle fermate registrate, e aggiornato `TransportStopBlock.handleInteraction` per prelevare la fermata corrente e instradare alla successiva. Aggiunto test di verifica routing in `TransportationTest`.

## Tel Aviv come città jigsaw

Problema riscontrato: `tel_aviv_city` era un unico blocco 48x48 (`beard_box`), una scatola piatta senza strade vere né quartieri.

Funzionamento aspettato: una città jigsaw come `jerusalem_city`, con strade che seguono il terreno ed edifici rigidi, solo nel bioma `urban_area`.

Come è stato risolto: nuovo `scripts/worldgen/gen_tel_aviv_city.py` → `structure/tel_aviv/*`. Pezzi: piazza Dizengoff (start, fontana Agam), strade in asfalto con marciapiedi (dritta, incrocio, curva, T), viale Rothschild con chiosco, 3 palazzi Bauhaus con balconi arrotondati, loft di Florentin con graffiti, casa templare di Sarona, 2 grattacieli in vetro, Tayelet sulla spiaggia. Interni minimi con loot (`tel_aviv_apartment`, `tel_aviv_tech_office`, nuova `tel_aviv_kiosk`). `beard_thin` e fondazione interrata di 4 blocchi sotto gli edifici. Test: `TelAvivCityStructureTest`. Verifica headless (seed 424242): 39 pezzi, 0 celle flottanti sotto gli edifici. Non testato in gioco dal client.

## Villaggio mediterraneo come villaggio jigsaw

Problema riscontrato: `mediterranean_village` era un singolo pezzo rettangolare (`beard_box`), non un villaggio.

Funzionamento aspettato: un villaggio jigsaw come quelli vanilla, con sentieri che seguono il terreno, piazza centrale e case varie, solo nel bioma `mediterranean_coast`.

Come è stato risolto: nuovo `scripts/worldgen/gen_mediterranean_village.py` → `structure/mediterranean/*`. Piazza con pozzo e ulivi (start), cappella con cupola blu (landmark), sentieri in terra/ghiaia/ciottoli (dritto, incrocio, curva, T) che sull'acqua diventano passerella in legno, 6 case bianche/calcare diverse (1-2 piani, tetti piani con terrazze, porte e persiane blu, rampicanti e bouganville), panetteria con forno, capanna del pescatore con barca, bancarella del mercato, uliveto. Loot `mediterranean_village` più le nuove `mediterranean_bakery` e `mediterranean_fisherman`. `beard_thin` e fondazione interrata di 4 blocchi. Test: `MediterraneanVillageStructureTest`. Verifica headless (seed 424242): 61 pezzi, 0 celle flottanti sotto gli edifici. Non testato in gioco dal client.

## Porto di Giaffa come città jigsaw

Problema riscontrato: `jaffa_port` era un singolo pezzo rettangolare (`beard_box`), senza vicoli, torre dell'orologio né porto vero.

Funzionamento aspettato: la vecchia Giaffa come città jigsaw: vicoli in pietra con scale, torre dell'orologio ottomana, mercato delle pulci, porto con barche, faro e molo, solo nel bioma `mediterranean_coast`.

Come è stato risolto: nuovo `scripts/worldgen/gen_jaffa_port.py` → `structure/jaffa/*`. Piazza della torre dell'orologio con sabil (start), porto garantito sul lato nord (banchina, bacino d'acqua, molo in legno, 2 barche da pesca, frangiflutti e faro), vicoli in pietra (dritto, incrocio, curva, T) più arco e scalinata rigidi, 4 case in pietra (una con cupola, una galleria d'arte), mercato delle pulci con portico ad archi. Loot `jaffa_flea_market` più le nuove `jaffa_house` e `jaffa_harbour`. `beard_thin` e fondazione interrata di 4 blocchi. Test: `JaffaPortStructureTest`. Verifica headless (seed 424242): 33 pezzi, 0 celle flottanti sotto gli edifici (a parte molo e barche sull'acqua del bacino). Non testato in gioco dal client.

## grand_market jigsaw (Mahane Yehuda)
- `/locate structure israel_simulator:grand_market` in un bioma jerusalem: piazza d'ingresso con arco e insegna rossa, chiosco dei succhi, vicoli lastricati (coperti con tetto di vetro o aperti con luci), negozi (spezie, frutta, panetteria, pesce, halva, caffè), casa del mercato a 2 piani e la sala coperta del mercato sul lato nord.
- Controllare: negozi appoggiati sul terreno (fondazione di 4 blocchi), vicoli che seguono il terreno, barili/casse con loot `grand_market`/`grand_market_food`.

## desert_ruins jigsaw (insediamento antico stile Qumran)
- `/locate structure israel_simulator:desert_ruins` nel deserto della Giudea: cortile con cisterna asciutta e colonne spezzate, rovina del tempio a nord, sentieri di sabbia/ghiaia, case in rovina, scriptorium con giare, laboratorio di ceramica, torre di guardia, mikveh.
- Controllare: rovine appoggiate sul terreno (fondazione di 4 blocchi) anche sui pendii, loot `desert_ruins`/`desert_ruins_scriptorium`.

## dead_sea_resort jigsaw (stile Ein Bokek)
- `/locate structure israel_simulator:dead_sea_resort` nel bioma dead_sea: piazza con fontana-spa di fango (packed_mud) e palme, hotel a 4 piani a nord, lungomare di arenaria (diventa passerella di betulla sull'acqua), spa di fango, piscina, beach bar, negozio di sale, spiaggia con ombrelloni e formazioni di sale, pensione.
- Controllare: edifici appoggiati sul terreno, piscina piena d'acqua, loot `dead_sea_resort`.

## jerusalem_city: varianti di case
- 6 nuove case nel pool `jerusalem/buildings` (con fondazione di 4 blocchi): casa a corte con olivo, casa con cupola (Città Vecchia), casa a terrazza (Nachlaot), casa templare con tetto rosso (German Colony), palazzina a 3 piani con balconi (Rehavia), casa stretta con porta ad arco e grate.
- `/place structure israel_simulator:jerusalem_city` in un bioma jerusalem: controllare che le nuove case compaiano accanto alle vecchie, con porta sulla strada e loot `jerusalem_house`/`jerusalem_pantry`.

## Esterni: sinagoga, grande sinagoga, casa storica, startup office
- `/place structure israel_simulator:synagogue` (e `great_synagogue`, `historical_house` nel bioma jerusalem, `startup_office` in urban_area).
- Sinagoga: cornicione, parapetto merlato, torrette angolari, tamburo con finestre e cupola bianca, portico a ovest, rosone. Grande sinagoga: cupola di rame su tamburo, due torri frontali con cupolette, portico con frontone e tavole della legge. Casa storica: cupola vera, archi sopra le finestre, persiane verdi, scala esterna al tetto, bouganville. Startup office: pensilina con insegna luminosa, frangisole, fioriere, rastrelliera bici, terrazza sul tetto con ombrelloni e antenna.
- Controllare che interni, marker e loot (anche `synagogue_ark`) siano invariati.

## Landmark reali e pulizia wiki/README
- Rimossi i landmark finti con coordinate fisse (Knesset, Masada, giardini Baha'i, sinagoga della Galilea, formazioni di sale, Shuk HaCarmel). Restano Muro Occidentale (`western_wall`), Torre dell'Orologio e Mercato delle Pulci (pezzi `jaffa/clock_square`, `jaffa/flea_market` di `jaffa_port`) e Tayelet (pezzo `tel_aviv/tayelet` di `tel_aviv_city`).
- Test in gioco: entrare in una di queste strutture/pezzi deve dare il messaggio "landmark scoperto" (+100 XP); usare la Mappa d'Israele per vedere il conteggio (x / 4).
- Wiki/README: tolti biomi e strutture inesistenti (golan_heights, Monte Hermon, Masada, tende beduine, grotte di Qumran, tunnel della Città di David), corretti gli id dei biomi e la tabella delle strutture.

## Controllo finale: ricetta Stella di David
- La ricetta `star_of_david` non veniva caricata (pattern di 5 colonne); ora è 3x3: N S N / D * D / C S C (N lingotto di netherite, S frammento di rotolo, D blocco di diamante, * stella del Nether, C moneta antica). Verificare che sia craftabile nel banco da lavoro.

## Biomi israeliani molto più comuni
- I 6 biomi ora prendono il posto di deserto/savana/badlands secchi (prima perdevano sempre contro i biomi vanilla ed erano rarissimi).
- Nuovo mondo: `/locate biome israel_simulator:jerusalem` (e gli altri 5) deve rispondere in pochi secondi, di solito entro ~2000 blocchi. Controllare che la sequenza costa → città/campi → Gerusalemme → deserto/Mar Morto sia sensata e che il deserto vanilla esista ancora.

## Biomi israeliani accanto a deserto/savana/badlands
- I biomi israeliani non sostituiscono più deserto, savana e badlands: condividono la zona calda (metà per uno), come chiazze vicine.
- Nuovo mondo: `/locate biome` dei 6 biomi israeliani e di `minecraft:desert`, `minecraft:savanna`, `minecraft:badlands` deve rispondere in pochi secondi. Controllare in volo che i biomi vanilla e israeliani si alternino in modo sensato.

## Padiglione a strisce con cupola d'oro (island_temple + arena di Epstein)
- Struttura `israel_simulator:island_temple` solo nel bioma mediterranean_coast (spacing 32/12, beard_thin): padiglione 13x13 a righe bianche/blu, pilastri blu agli angoli, parapetto a zigzag, cupola d'oro a gradoni con guglia, portone ad arco in legno scuro, pannello ad arco color sabbia sul lato, scala in quarzo, due statue d'oro, piazza a chevron rosso/bianco/rosa, 4 palme, erba secca e scala a pioli sul lato est. Una cassa con loot `island_temple`.
- `/locate structure israel_simulator:island_temple` (seed 20261005: ~7400 blocchi) oppure `/place template israel_simulator:island_temple ~ ~ ~`.
- Arena: portare Epstein al 50% di vita. Deve comparire lo stesso padiglione dentro una cupola di vetro (un solo strato) con Epstein sulla piazza davanti alla scala e i giocatori vicini spostati sulla piazza (non dentro i muri). Controllare che nessuno resti incastrato e che la cupola contenga tutto il padiglione.

## Olivo, palma da datteri, agrumi ed erbe mediterranee: raccolto e texture
- Prima: click destro infinito su olive/datteri/agrumi = cibo infinito; foglie di palma ed erbe grigie (texture vanilla `jungle_leaves`/`fern` senza tinta), olivo e agrumi con texture vanilla dell'azalea.
- Ora foglie e erbe hanno `age` 0-3 (foglie → fiori → frutto acerbo → maturo) con texture 16x16 proprie a colori fissi (`scripts/textures/gen_crop_textures.py`), più nuove texture degli oggetti datteri, olive, agrumi ed erbe.
- Test in gioco: click destro su foglie mature → 1-2 frutti, poi tornano a `age=1` (fiori) e un secondo click non dà niente. Su foglie/erbe non mature il click destro non fa niente. Farina d'ossa su foglie o erbe non mature → avanzano di uno stadio. Lasciate stare ricrescono da sole (tick casuali, qualche minuto per stadio).
- Rompere le foglie senza cesoie dà il frutto solo se mature; con le cesoie dà il blocco. Le erbe rotte danno sé stesse (non più semi di grano); mature si raccolgono col click destro.
- Nuovo mondo: gli alberi generati e le erbe hanno stadi misti, quindi alcuni frutti sono subito raccoglibili.
- Test: `CropHarvestTest`. Non testato in gioco dal client.

## Fango del Mar Morto: texture e cooldown
- Nuova texture 16x16 per `dead_sea_mud` (fango grigio-marrone scuro con cristalli di sale e venature minerali, `scripts/textures/gen_dead_sea_mud_texture.py`) al posto della palla d'argilla vanilla.
- Test in gioco: click destro col fango → effetti (rimuove lentezza/debolezza/veleno, assorbimento + rigenerazione), se ne consuma uno e lo slot mostra l'overlay grigio del cooldown per 30 secondi. Durante il cooldown il click destro non fa niente e non consuma fango.
- Test: `DeadSeaMudItemTest`. Non testato in gioco dal client.

## Cooldown degli oggetti a click destro e Dreidel equo
- Cooldown (overlay grigio sullo slot, solo dopo un uso riuscito): Dreidel 10 s, Primo Emendamento 120 s (l'effetto Libertà dura 60 s), disco Hava Nagila 5 s, Mappa d'Israele e Rav-Kav 2 s. Durante il cooldown il click destro non fa niente.
- Dreidel: ogni giro punta 3 Shekel (servono nell'inventario, altrimenti messaggio rosso e niente cooldown). Nun = riprendi la puntata (0), Gimel = +2, Hei = +1, Shin = perdi i 3 Shekel. Valore atteso 0: niente più Shekel infiniti gratis. In creativa non costa e non paga.
- Test: `ItemCooldownTest`, `FestivalsAndCalendarTest`. Non testato in gioco dal client.

## Epstein: arena al 50% e "Palm Beach Pete" al 33%
- La cupola di vetro con il padiglione compare ora al 50% di vita (prima al 30%), una sola volta.
- Al 33% di vita il boss cambia nome in "Palm Beach Pete" (nome sopra la testa e barra del boss), una sola volta.
- Test in gioco: uscire e rientrare nel mondo dopo il 33%: la barra deve ancora dire "Palm Beach Pete" e la cupola non deve ricomparire. Rientrando tra il 50% e il 33% la cupola non si ricostruisce.
- Test: `EpsteinPhaseTriggersTest`. Non testato in gioco dal client.

## Traduzioni mancanti (scudo di Bibi)
- Colpendo Bibi mentre Epstein è vivo, in chat compariva la chiave grezza `message.israel_simulator.bibi_shielded_by_epstein`. Ora: "Bibi è protetto da Epstein! Sconfiggilo prima." (en: "Bibi is shielded by Epstein! Defeat him first.").
- Aggiunte anche le 5 chiavi `category.israel_simulator.*` delle categorie di prodotti. Il nuovo test `TranslationKeysInCodeTest` controlla che ogni chiave usata nel codice esista in en_us e it_it.

## Epstein: l'arena sparisce a fine scontro
- Quando compare la cupola con il padiglione, tutte le posizioni toccate vengono salvate (blocco originale + dati di casse/cartelli) nei dati del mondo (`epstein_arena_snapshots`).
- Test in gioco: uccidere Epstein → cupola e padiglione spariscono e il terreno torna com'era, senza oggetti della cassa del padiglione per terra. Anche con `/kill` o se sparisce (peaceful) il mondo viene ripristinato.
- Salvare ed uscire durante lo scontro, rientrare: l'arena resta finché Epstein è vivo. Se Epstein non c'è più (es. `/kill` da lontano, altra dimensione) l'arena viene ripristinata entro ~10 s da quando la zona è caricata.
- I blocchi del padiglione modificati da un giocatore non vengono sovrascritti. Chi è dentro un blocco ripristinato viene spostato in superficie.
- Test: `EpsteinArenaRestoreTest`. Non testato in gioco dal client.

## Pistola 3D e guardie che la impugnano bene
- La Pistola di Sicurezza ora ha un modello 3D a cuboidi (16 elementi: carrello con zigrinature, canna, fusto, slitta, ponticello, grilletto, impugnatura inclinata, coda di castoro, fondello del caricatore, mirino e tacca di mira) con atlante 32x32 (`textures/item/pistol_model.png`, grigio canna di fucile, fusto nero, impugnatura più calda). Generati da `scripts/textures/gen_pistol_model.py`.
- Nell'inventario e sugli scaffali resta l'icona piatta (`items/pistol.json` usa `select` su `display_context`: `gui`/`on_shelf` → `pistol_icon`, il resto → modello 3D).
- Test in gioco: in prima persona la pistola sta in basso a destra e punta verso il mirino; in terza persona (F5) la canna punta in avanti e l'impugnatura è nel pugno; a terra, in una cornice e in mano sinistra deve apparire dritta.
- Guardie di Bibi: senza bersaglio tengono la pistola come un giocatore (braccio leggermente avanti); quando ti prendono di mira alzano le braccia e la canna punta dove guardano (non più verso il cielo o all'indietro).
- Test: `PistolModelTest`. `runClient` fino al menu principale senza errori di modelli o texture. Non testato in gioco dal client.

## Discorso pubblico in piazza (evento)
- Da op: `/israelsim event speech start` (o `start <x y z>`), `/israelsim event speech status`, `/israelsim event speech stop`. L'evento parte anche da solo: ogni minuto, se sei dentro Tel Aviv o Jerusalem e non c'è stato un evento negli ultimi 10 minuti (config `eventCooldownTicks`), c'è il 35% di probabilità che compaia un palco nella piazza centrale.
- Compare un gazebo con palco in legno, tetto blu, casse (jukebox + note block), microfono (end rod su sbarre di ferro), due file di sedie e due cartelli "Foro Pubblico". Sul palco c'è "L'Oratore" (personaggio inventato) che ogni 12 s dice una frase in chat; i 6 abitanti del pubblico applaudono (particelle verdi, salto, suono) o fischiano (particelle arrabbiate, scuotono la testa).
- Resta entro 16 blocchi per 60 secondi di fila: nella barra azioni vedi "x / 60 s"; se ti allontani riparte da zero. A 60 s ricevi il Primo Emendamento (una sola volta per evento) e la quest "Civic Voice". In alto c'è la barra dell'evento con il tempo rimasto (2 minuti).
- A fine evento (o con `stop`, o `/kill` dell'Oratore) palco, Oratore e pubblico spariscono e la piazza torna com'era. Se esci a metà evento, al rientro l'Oratore se ne va e la piazza viene ripristinata.
- Skin dell'Oratore: `src/main/resources/assets/israel_simulator/textures/entity/orator.png` (skin 64x64 standard, segnaposto da sostituire).
- Test: `PublicSpeechEventTest`. Provato con `runServer` da console (palco, Oratore, pubblico, fine evento e ripristino); non testato in gioco dal client.
