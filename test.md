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
