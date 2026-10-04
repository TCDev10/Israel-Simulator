# Test log

## Città che spawnavano come villaggi vanilla

Problema riscontrato: Tel Aviv, Jaffa, Jerusalem e il Western Wall usavano come `start_pool` i villaggi vanilla (`minecraft:village/plains/town_centers` o `minecraft:village/desert/town_centers`). In mondo sarebbero comparsi villaggi normali, non strutture del mod. I quartieri di Tel Aviv (Rothschild, Florentin, Sarona, White City, Bauhaus, startup) restano solo nomi nel codice, senza edifici propri.

Funzionamento aspettato: ogni città parte da un template pool di `israel_simulator` e piazza un pezzo del mod, non un villaggio vanilla.

Come è stato risolto: pool e NBT propri per le quattro strutture (cubo Bauhaus, capanno del porto, casa di pietra con cupola, piazza del muro). Il test `StructureFrameworkTest` fallisce se uno di quei `start_pool` contiene `minecraft:village`. `./gradlew test` è passato (181 test). Merge in [PR 3](https://github.com/TCDev10/Israel-Simulator/pull/3). Un mondo Minecraft non è stato generato.

## Strutture rimaste sui villaggi vanilla

Problema riscontrato: synagogue, government_building, ancient_sanctuary, grand_market, startup_office, agricultural_farm, great_synagogue, dead_sea_resort, desert_ruins, historical_house, mediterranean_village e ein_gedi_oasis avevano ancora `start_pool` su un pool vanilla (`minecraft:village/plains/town_centers`, `minecraft:village/desert/town_centers` o `minecraft:village/plains/houses`). In generazione sarebbero comparsi villaggi vanilla, non pezzi del mod. Le quattro città già sistemate (Tel Aviv, Jaffa, Jerusalem, Western Wall) non erano coinvolte.

Funzionamento aspettato: nessuna struttura in `data/israel_simulator/worldgen/structure` usa un `start_pool` che contiene `minecraft:village`. Ognuna parte da un template pool `israel_simulator:<nome>` che piazza un NBT del mod con un blocco marker proprio.

Come è stato risolto: per le dodici strutture sono stati aggiunti un template pool e un NBT piccolo (DataVersion 4903) con palette distinta e marker proprio (vetro viola, deepslate levigato, arenaria cesellata, terracotta rossa, blocco di ferro, fieno, vetro blu, fango compatto, mattoni di pietra crepati, mattoni, terracotta grigio chiaro, blocco di muschio). `StructureFrameworkTest` ora fallisce se qualunque JSON di struttura usa un pool `minecraft:village`, e verifica che il pool punti al template del mod e che l'NBT contenga il marker. `./gradlew test --tests com.israelsimulator.world.StructureFrameworkTest` e `./gradlew test` sono passati. Un mondo Minecraft non è stato generato.
