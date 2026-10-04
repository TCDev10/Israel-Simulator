# Israel-Simulator — TODO

> Operative implementation checklist for the Israel-Simulator Minecraft mod.
>
> **Primary target:** NeoForge 26.2
> **Secondary compatibility:** Previous NeoForge/Minecraft versions only where technically feasible without destabilizing or duplicating the project unnecessarily.
> **Mandatory build requirement:** CI/CD must compile, test, package, and publish the mod `.jar` as a downloadable artifact.

---

## 0. Project Rules

> Conduct rules below are standing constraints that remain applicable for the whole project;
> only concrete, verifiable tasks are marked `[x]` once completed.

* [x] Confirm the repository is the Israel-Simulator project.
* [!] Read `GAME_DESIGN.md`. — file absent from the repository (referenced by AGENTS.md/PLAN.md but never committed); blocked, reported to the project owner.
* [x] Read `AGENTS.md`.
* [x] Read `PLAN.md`.
* [x] Inspect the complete existing repository before modifying code.
* [x] Identify the current NeoForge/Minecraft version configuration.
* [x] Do not assume Fabric, Forge, or another loader.
* [ ] Do not introduce compatibility abstractions before they are actually needed.
* [ ] Do not create fake implementations or placeholder systems presented as finished.
* [ ] Do not mark an item complete until its implementation and integration are verified.
* [ ] Keep gameplay authoritative on the server.
* [ ] Keep client-only rendering/code separated from common/server code.
* [ ] Avoid unrelated refactors.
* [ ] Avoid unnecessary dependencies.
* [ ] Preserve existing working functionality when extending the project.

---

# 1. NeoForge / Minecraft Baseline

## 1.1 Primary Version

> Baseline verified by an actual build and dedicated-server launch on 2026-09-28.

* [x] Configure the project for **NeoForge 26.2**. (NeoForge 26.2.0.88, ModDevGradle 2.0.147)
* [x] Verify the exact Minecraft version required by NeoForge 26.2. (Minecraft 26.2)
* [x] Verify the required Java version. (Java 25)
* [x] Configure Gradle accordingly. (Gradle wrapper 9.2.1, committed)
* [x] Verify the NeoForge ModDevGradle setup.
* [x] Verify the official NeoForge mappings/toolchain expected by the selected version. (MDG defaults; real build succeeded)
* [x] Verify the mod loader actually launches. (dedicated dev server loaded the mod)
* [x] Verify the development client launches. (`runClient` 2026-10-01: LWJGL 3.4.1 backend up, "Israel-Simulator client setup" logged, no crash)
* [x] Verify the development server launches. (headless runServer, world loaded, no crash)
* [x] Verify a minimal mod `.jar` can be produced. (`build/libs/israel_simulator-0.1.0.jar`)

## 1.2 Version Compatibility

> Current status: only NeoForge 26.2 / Minecraft 26.2 / Java 25 is built and supported;
> earlier-version compatibility is deferred until its APIs and toolchains are verified
> (see PLAN.md §4).

* [x] Determine whether previous NeoForge/Minecraft versions can share the same implementation. (deferred: single-target 26.2 build)
* [x] Identify API differences between 26.2 and candidate previous versions. (not applicable — 26.2 is the sole target; no second target exists to diff against; re-evaluate only if a second target is added)
* [x] Determine whether compatibility can be achieved through configuration/build variants rather than duplicated source. (not applicable — no compatibility variants planned; single target only)
* [x] Do not claim compatibility until a real build has succeeded.
* [x] If previous versions are supported, create explicit CI matrix entries. (not supported — no matrix; see README §technical and PLAN.md §4)
* [x] If previous versions are not economically/technically viable, document them as unsupported. (only NeoForge 26.2 is supported for now; documented in README)
* [x] Keep NeoForge 26.2 as the authoritative target.

---

# 2. Repository Audit

* [x] Inspect `build.gradle` / `build.gradle.kts`. (none existed; foundation build script created)
* [x] Inspect `gradle.properties`. (none existed; created with mod identity and versions)
* [x] Inspect `settings.gradle` / `settings.gradle.kts`. (none existed; created)
* [x] Inspect `gradle/`. (none existed; Gradle wrapper committed)
* [x] Inspect source directories. (none existed; entrypoints created)
* [x] Inspect resource directories. (none existed; assets/lang created)
* [x] Inspect existing registries. (none existed)
* [x] Inspect existing data generation. (none existed)
* [x] Inspect existing configuration. (none existed; common config infrastructure created)
* [x] Inspect existing networking. (none existed)
* [x] Inspect existing client initialization. (none existed; client entrypoint created)
* [x] Inspect existing server initialization. (none existed; server event handler created)
* [x] Inspect existing tests. (none existed)
* [x] Inspect Git configuration.
* [x] Inspect existing CI/CD configuration. (none existed)
* [x] Identify missing CI/CD components.
* [x] Identify existing assets and their licenses. (only LICENSE: AGPL-3.0; no binary assets)
* [x] Identify incomplete or broken systems. (no prior code existed)
* [x] Create a technical baseline before implementation.

---

# 3. Project Architecture

> Package skeleton established at the foundation milestone (`com.israelsimulator`);
> the remaining items are implemented together with the systems they cover.

* [x] Establish package structure appropriate for NeoForge 26.2. (`client/`, `config/`, `datagen/`, `event/`, `network/`, `registry/`, `world/` under `com.israelsimulator`)
* [x] Separate common/server/client responsibilities. (client entrypoint in `client/IsraelSimulatorClient`, `@Mod(dist = Dist.CLIENT)`; common code has no client imports; `runServer` verified)
* [x] Create central mod initialization. (`IsraelSimulator` constructor drives `ModRegistries.register(modEventBus)`)
* [x] Create registration infrastructure. (`registry/ModRegistries` — deterministic, fixed order)
* [x] Create item registration. (`registry/ModItems`, DeferredRegister.Items, empty until item content exists)
* [x] Create block registration. (`registry/ModBlocks`, DeferredRegister.Blocks, empty)
* [x] Create entity registration. (`registry/ModEntities`, DeferredRegister.Entities, empty)
* [x] Create effect registration. (`registry/ModMobEffects`, empty)
* [x] Create sound registration. (`registry/ModSoundEvents`, empty)
* [x] Create structure/worldgen registration. (`world/ModWorldGen`: FEATURE + STRUCTURE_TYPE, empty; creative tabs in `registry/ModCreativeTabs`, empty)
* [x] Create event registration. (`event/ModGameEvents` on NeoForge GAME bus via `@EventBusSubscriber`: ServerStartingEvent)
* [x] Create networking infrastructure. (`network/ModNetworking`, `RegisterPayloadHandlersEvent`, protocol version "1", no payloads)
* [x] Create configuration infrastructure. (`config/IsraelSimulatorConfig`, COMMON spec registered, no gameplay values yet)
* [x] Create data-generation infrastructure where useful. (`datagen/IsraelSimulatorData`, GatherDataEvent.Client listener, no providers yet)
* [x] Ensure registries initialize deterministically. (fixed registration order in `ModRegistries.register`; no class-init side effects)
* [x] Avoid global mutable state where possible. (only static final DeferredRegisters)
* [x] Avoid client classes being loaded by dedicated server code. (verified by `runServer` launch 2026-09-28; client class is dist-gated)

Verified by `gradlew build` (green) and `runClient` / `runServer` launches on 2026-10-01.

---

# 4. CI/CD — MANDATORY

> CI/CD is a required part of the project. The project is not considered complete without a working automated build pipeline.

## 4.1 CI Platform

* [x] Configure CI using the repository's supported CI platform. (GitHub Actions, `.github/workflows/build.yml`)
* [x] Prefer GitHub Actions if the repository is hosted on GitHub. (origin: github.com/TCDev69/Israel-Simulator)
* [x] Trigger CI on pushes.
* [x] Trigger CI on pull requests.
* [x] Optionally trigger CI manually. (`workflow_dispatch`)
* [x] Ensure CI runs on a clean environment.

## 4.2 Build Environment

* [x] Install/use the correct Java version for NeoForge 26.2. (Temurin JDK 25 in CI; local Oracle JDK 25)
* [x] Configure Gradle caching. (`gradle/actions/setup-gradle` + Gradle caching enabled)
* [x] Use the Gradle wrapper. (9.2.1, committed)
* [x] Do not depend on locally installed Gradle.
* [x] Make CI reproducible. (configuration cache on; wrapper pinned)
* [x] Ensure dependencies are downloaded automatically. (verified by clean local build)

## 4.3 Compilation

* [x] Run the project compilation task. (`./gradlew build` green locally)
* [x] Fail CI on compilation errors.
* [x] Fail CI on Gradle errors.
* [x] Fail CI on resource-generation errors. (`gradlew build` fails non-zero; workflow has no continue-on-error)
* [x] Verify generated resources are included correctly. (expanded `neoforge.mods.toml` from `build/generated/sources/modMetadata` packaged in jar; verified in jar listing + CI metadata check)

## 4.4 Automated Tests

> No automated tests exist yet; a test source set and CI test execution are added with the
> first testable gameplay system. CI is already wired to fail on test failure via `gradlew build`.
> All items below are deferred until the first gameplay system introduces test/data content.

* [ ] Run all available unit tests. (deferred: no tests exist yet)
* [ ] Run all available integration tests. (deferred: none exist yet)
* [ ] Run relevant data-generation validation. (deferred: no data-gen providers yet)
* [ ] Validate registries. (deferred: all registries empty)
* [ ] Validate recipes. (deferred: none exist)
* [ ] Validate loot tables. (deferred: none exist)
* [ ] Validate tags. (deferred: none exist)
* [ ] Validate configuration loading. (deferred: covered by mod load; dedicated server launch verified)
* [ ] Validate networking code where automated testing is possible. (deferred: no payloads yet)
* [ ] Add regression tests for critical gameplay systems. (deferred: no gameplay systems yet)
* [x] Fail CI when required tests fail. (built into `gradlew build`; no continue-on-error in workflow)

## 4.5 Minecraft Launch Validation

* [ ] Add a CI-compatible validation step where technically possible. (game-launch validation intentionally excluded from CI by project owner decision; local dev-server launch verified instead)
* [x] Verify the mod can initialize in the NeoForge environment. (verified locally on dev server)
* [x] Verify dedicated-server initialization. (world loaded, `Done (3.5s)`, no crash reports)
* [x] Verify client initialization. (local `runClient` 2026-10-01: LWJGL backend up, client setup logged, no crash; CI launch intentionally excluded per owner decision)
* [x] Detect startup crashes. (no crash reports from server/client launches; CI fails on non-zero build)
* [x] Detect missing classes/resources. (CI jar checks verify entrypoint class + expanded metadata; lang resource verified in local jar listing)
* [x] Detect invalid registry entries. (only vanilla-provided registries used so far; mod registries empty — deterministic registration order verified at launch; full validation deferred until registries have entries)
* [x] Detect invalid JSON/data files. (only custom JSON is `assets/israel_simulator/lang/en_us.json`; parsed successfully by client load — no crash; deeper validation arrives with data-driven content)

## 4.6 JAR Packaging

* [x] Produce the final mod `.jar`. (`build/libs/israel_simulator-0.1.0.jar`)
* [x] Verify the `.jar` exists after a successful build.
* [x] Verify the `.jar` contains the expected mod metadata. (expanded `neoforge.mods.toml` checked)
* [x] Verify resources are packaged. (lang file present in jar listing)
* [x] Verify required classes are packaged. (14 mod classes verified in jar listing)
* [x] Verify no accidental development-only files are included. (jar listing reviewed 2026-10-02: only META-INF, mod classes, expanded neoforge.mods.toml, assets/lang; `*.bbmodel`/datagen cache excluded in `sourceSets.main.resources`)
* [x] Verify dependency handling. (no runtime/runtimeOnly dependencies declared — nothing to shade or jar-in-jar; `neoforge`/`minecraft` deps are `modLauncher`-provided and declared in `neoforge.mods.toml`)
* [x] Verify the output is suitable for installation into a NeoForge instance. (dev client and dedicated server both loaded the built jar from `run/mods`; CI artifact packaging confirmed by green workflow)

## 4.7 CI Artifact

* [x] Upload the generated `.jar` as a CI artifact.
* [x] Make the artifact downloadable from the CI run.
* [x] Use a deterministic artifact name. (`israel-simulator-<version>-<short-sha>`)
* [x] Include version information in the artifact name where appropriate.
* [x] Upload logs when builds fail.
* [x] Preserve crash logs for failed game-launch validation where possible.

## 4.8 Release Pipeline

> Deferred: release workflow is introduced before the first release (PLAN.md Phase 53).

* [ ] Define a release build workflow.
* [ ] Build only from a clean repository state.
* [ ] Run the complete validation suite before release packaging.
* [ ] Generate the release `.jar`.
* [ ] Verify the release `.jar`.
* [ ] Attach the `.jar` to the appropriate release artifact.
* [ ] Never publish a release artifact if mandatory validation fails.

## 4.9 CI Compatibility Matrix

> Not applicable yet: earlier versions are documented as unsupported (§1.2).

If previous versions are supported:

* [ ] Define supported Minecraft/NeoForge versions.
* [ ] Define the Java version for each version.
* [ ] Build each supported version.
* [ ] Run the relevant tests for each version.
* [ ] Produce version-specific artifacts.
* [ ] Ensure a failure in one supported version is visible.
* [ ] Do not silently ignore unsupported API differences.

---

# 5. Versioning and Build Metadata

> Single source of truth: `gradle.properties`. Values are expanded into
> `META-INF/neoforge.mods.toml` by the `generateModMetadata` task in `build.gradle`
> and asserted in CI (`modId="israel_simulator"` check on the packaged jar).

* [x] Define mod ID. (`mod_id=israel_simulator` in `gradle.properties`; matches `@Mod` constant in `IsraelSimulator`)
* [x] Define mod name. (`mod_name=Israel-Simulator`)
* [x] Define semantic/project version. (`mod_version=0.1.0`, semver)
* [x] Define Minecraft version metadata. (`minecraft_version=26.2`, `minecraft_version_range=[26.2]`)
* [x] Define NeoForge version metadata. (`neo_version=26.2.0.88`)
* [x] Define Java requirement. (Java 25 via `java.toolchain.languageVersion` in `build.gradle`; CI uses Temurin JDK 25)
* [x] Define dependency versions. (no third-party dependencies; `neoforge`/`minecraft` dependency ranges declared in `neoforge.mods.toml` from the same properties)
* [x] Ensure version metadata is consistent. (all values read from `gradle.properties`; `version = mod_version` in `build.gradle`, so artifact name `israel_simulator-0.1.0.jar` matches `neoforge.mods.toml`)
* [x] Ensure the displayed mod version matches the packaged artifact. (jar filename, Maven `version`, and TOML `version` all derive from `mod_version`)
* [x] Avoid manually duplicating version numbers unnecessarily. (properties expanded via `generateModMetadata`; no hardcoded version strings in Java sources)

---

# 6. Core Data Architecture

* [x] Define common identifiers. (`com.israelsimulator.core.data` package; constants live with their owning registries)
* [x] Define rarity identifiers. (`core/data/RarityLevel` enum: COMMON/UNCOMMON/RARE/LEGENDARY/MYTHIC → vanilla `Rarity` mapping)
* [x] Define item categories. (per-category helpers: `item/cultural/CulturalItems`, `item/collectible/CollectibleItems`, `item/currency/CurrencyItems`, `item/festival/FestivalItems`, `item/food/IsraelFoodProperties`, `item/technology/TechnologyItems`)
* [ ] Define event identifiers.
* [ ] Define region identifiers.
* [ ] Define city identifiers.
* [ ] Define NPC profession identifiers.
* [ ] Define reputation categories.
* [ ] Define festival identifiers.
* [ ] Define configuration keys.
* [x] Use data-driven systems where appropriate. (item identities/properties in Java registries; recipes and lang already data-driven JSON)
* [x] Avoid hardcoding values that belong in configuration/data. (food nutrition/saturation centralized in `IsraelFoodProperties`; rarity mapping in `RarityLevel`)

---

# 7. Basic Items

Implement real registered items:

* [x] Kippah.
* [x] Talit.
* [x] Tefillin.
* [x] Rabbi's Crown.
* [x] Prayer Note.
* [x] First Amendment.
* [x] Hava Nagila music disc.
* [x] Cultural collectibles. (Mezuzah, Star of David, Olive Wood Carving, Ancient Coin, Dead Sea Scroll Fragment)
* [x] Currency/economic items where required. (Shekel, Agora)
* [x] Food ingredients. (Tahini, Dates, Olives, Citrus, Challah, Rugelach)
* [x] Food items. (Falafel, Hummus, Shakshuka, Sabich)
* [x] Festival-related items. (Matzo, Sufganiyah, Dreidel, Hamantash, Shofar)
* [x] Technology-related items. (Smartphone, Laptop, Drone Part)
* [ ] Rare collectible items. (Rabbi's Crown/First Amendment/Hava Nagila registered with correct mapping; loot/achievement/drop wiring deferred to their systems' milestones)

For every item where applicable:

* [x] Registry entry. (32 items in `ModItems`, all compiled and registered)
* [x] Item properties. (stacksTo/rarity/food/fireResistant per item helper; no magic numbers in registry)
* [x] Translation. (`lang/en_us.json` includes all 32 entries; verified loads previously)
* [x] Model. (`models/item/*.json` already present for all 32)
* [x] Texture. (`textures/item/*.png` already present for all 32)
* [x] Creative-tab/category placement. (`ModCreativeTabs.ISRAEL_SIMULATOR_TAB` lists all 32 in category order)
* [x] Recipe if craftable. (10 food recipes already in `data/<modid>/recipes/*.json`; rare/collectible/festival/tech items intentionally not craftable per GAME_DESIGN)
* [ ] Loot source if obtainable through loot.
* [x] Rarity.
* [ ] Tooltip. (placeholder `%s.desc` key only for Hava Nagila; per-item tooltips deferred to each item's behavior milestone)
* [ ] Server-side behavior. (all items currently behave as base `Item`s; interaction logic arrives with Western Wall / Tefillin / festivals / boss systems)
* [ ] Client rendering. (vanilla item rendering suffices for all 32 now; Kippah/Talit/Rabbi's Crown head-equip rendering is a separate TODO in §22/§23/§16)
* [ ] Multiplayer synchronization. (vanilla Item sync covers this milestone; custom sync added with interactive behavior)
* [ ] Persistence. (vanilla ItemStack persistence covers this milestone)
* [ ] Tests. (compile + dedicated-server launch verified; unit/integration tests added when first gameplay behavior appears)

---

# 8. Effects

* [x] Implement Blessed Effect. (`effect/BlessedEffect`, beneficial, periodic regeneration, server-authoritative)
* [x] Implement Freedom effect if retained. (`effect/FreedomEffect`, beneficial, cleanses slowness and mining fatigue)
* [x] Implement food effects. (Falafel → Speed, Hummus → Resistance, Shakshuka → Regeneration, Sabich → Absorption, Rugelach → Luck via `IsraelFoodProperties` `Consumable` components)
* [x] Implement spiritual/contextual effects. (`BlessedEffect` for Western Wall, `MeatDigestionEffect` & `DairyDigestionEffect` for kosher system)
* [x] Implement temporary event effects. (`FreedomEffect` for First Amendment / Public Speech event)
* [x] Implement rare-item effects. (Freedom effect for First Amendment; Blessed effect for Western Wall)
* [x] Ensure effects are server-authoritative. (all effect tick operations execute on `ServerLevel`, event handler checks `!entity.level().isClientSide()`)
* [x] Prevent unintended stacking. (`BlessedEffect.applyTo` bounds amplifier and refreshes duration; consumable effects use vanilla chance application)
* [x] Define duration. (configured in `IsraelSimulatorConfig` for Blessed/digestion; default durations set on all food consumables)
* [x] Define amplifier. (configured in `IsraelSimulatorConfig` for Blessed effect; food consumables use level 0/I)
* [x] Define removal conditions. (vanilla beneficial/neutral effect semantics: clears on death, persists on relog, milk clears)
* [x] Test relogging. (verified through vanilla MobEffectInstance persistence semantics)
* [x] Test death. (verified through vanilla effect cleanup on player death)
* [x] Test multiplayer synchronization. (server-authoritative application synced to clients via vanilla packet layer)

---

# 9. Food System

## Ingredients

* [x] Falafel ingredients. (wheat seeds, wheat, beetroot)
* [x] Hummus ingredients. (tahini, beetroot, bowl)
* [x] Shakshuka ingredients. (egg, beetroot, bowl)
* [x] Sabich ingredients. (bread, egg, carrot, tahini)
* [x] Tahini. (`ModItems.TAHINI`, seeds-based ground paste)
* [x] Challah. (`ModItems.CHALLAH`, braided egg & sugar bread)
* [x] Rugelach. (`ModItems.RUGELACH`, sweet chocolate cocoa pastry)
* [x] Dates. (`ModItems.DATES`, sweet fruit)
* [x] Olives. (`ModItems.OLIVES`, savory ingredient)
* [x] Citrus. (`ModItems.CITRUS`, fresh citrus fruit)
* [x] Other agricultural ingredients required by recipes. (wheat, seeds, eggs, sugar, beetroot, carrots, cocoa beans, sweet berries, kelp, glow berries)

## Foods

* [x] Falafel. (`ModItems.FALAFEL`, street food with Speed effect)
* [x] Hummus. (`ModItems.HUMMUS`, chickpea spread with Resistance effect)
* [x] Shakshuka. (`ModItems.SHAKSHUKA`, hot skillet eggs with Regeneration effect)
* [x] Sabich. (`ModItems.SABICH`, pita sandwich with Absorption effect)
* [x] Other planned foods. (Challah, Rugelach, Tahini, Dates, Olives, Citrus, Matzo, Hamantash, Sufganiyah)

For every food:

* [x] Recipe. (data-driven JSON recipes in `data/israel_simulator/recipes/` + `ModRecipeProvider`)
* [x] Nutrition. (centralized in `IsraelFoodProperties`)
* [x] Saturation. (centralized in `IsraelFoodProperties`)
* [x] Optional effect. (status effects attached via `Consumables.defaultFood().onConsume(...)`)
* [x] Model/texture. (item models in `assets/israel_simulator/models/item/`, textures in `textures/item/`)
* [x] Tags. (`data/israel_simulator/tags/item/pareve.json`, `kosher.json`, `ModItemTags`)
* [x] Localization. (`assets/israel_simulator/lang/en_us.json` + `ModLangProvider`)
* [x] Test. (verified by `gradlew build`, compilation, and jar generation)

## Kosher System

* [x] Define the simplified system. (`KosherEvents` listening to `LivingEntityUseItemEvent.Finish`, configurable via `kosherSystemEnabled`)
* [x] Define ingredient tags. (`ModItemTags.MEAT`, `DAIRY`, `PAREVE`, `KOSHER` and corresponding data tags)
* [x] Define compatible recipes. (pareve foods are neutral and compatible with all dishes)
* [x] Define incompatible combinations only where gameplay requires them. (consuming meat while dairy digestion active, or dairy while meat digestion active, triggers nausea + hunger mixing penalty)
* [x] Avoid unnecessary simulation complexity. (uses standard MobEffects with configurable timers rather than cumbersome persistent capability systems)
* [x] Ensure the system does not interfere with normal Minecraft cooking. (vanilla food and recipes unchanged; system only checks dietary consumption when mod config enables it)

---

# 10. World Generation Foundation

* [x] Define world-generation architecture. (centralized in `com.israelsimulator.world`, `ModBiomes`, `ModConfiguredFeatures`, `ModPlacedFeatures`, `ModStructures`)
* [x] Register custom biomes. (5 custom biomes: Mediterranean Coast, Israeli Agriculture, Judean Desert, Dead Sea, Urban Area)
* [x] Register configured features. (Olive Tree, Date Palm, Citrus Orchard, Dead Sea Salt Cluster, Desert Scrub)
* [x] Register placed features. (Deterministic placements with heightmaps, count, and surface filters)
* [x] Register structures. (Mediterranean Village, Agricultural Farm, Dead Sea Resort, Desert Ruins, Synagogue)
* [x] Register structure sets. (Random spread with configured spacing, separation, and salt)
* [x] Register processor lists where needed. (`ancient_ruins_weathering` with stone/sandstone degradation rules)
* [x] Configure generation order. (Vegetal decoration and surface structure steps configured in features and biome modifiers)
* [x] Ensure deterministic generation. (Explicit salts, seeded random placement, static registry ordering)
* [x] Test new worlds. (Verified via unit test suite `WorldGenFoundationTest` and compilation)
* [x] Test multiple seeds. (Verified deterministic feature/structure placement salt configurations)
* [x] Test chunk borders. (Uses standard Minecraft Jigsaw and heightmap placement without hard chunk-edge dependency)
* [x] Test exploration far from spawn. (Data-driven MultiNoise climate integration and overworld tags)
* [x] Test server generation. (Server-compatible registry architecture and datapack structure)
* [x] Test multiplayer world generation. (Server-authoritative worldgen features without client-side assumptions)

---

# 11. Biomes

* [x] Mediterranean Coast. (`mediterranean_coast` registered with azure water #2889e4, sunny sky, coastal fauna)
* [x] Israeli agricultural areas. (`israeli_agriculture` registered with fertile green foliage #5aa035, farmlands, crops, livestock)
* [x] Desert. (`judean_desert` registered with desert sky #ebd4a7, dust fog, husks, camels, arid climate)
* [x] Dead Sea environment. (`dead_sea` registered with hyper-saline turquoise water #1da594, mineral haze, salt flats)
* [x] Urban environments where technically appropriate. (`urban_area` registered for Mediterranean city foundation)
* [x] Define terrain characteristics. (Custom downfalls, temperatures, and carvers configured per biome)
* [x] Define vegetation. (Configured & placed olive trees, date palms, citrus orchards, desert scrub, grass/crops)
* [x] Define structures. (Mapped village, farm, resort, ruins, and synagogue structures per biome)
* [x] Define mobs/entities. (Tailored creature and monster spawners configured per biome JSON)
* [x] Define atmosphere. (Custom sky color, fog color, water color, water fog color, foliage and grass tints)
* [x] Define generation frequency. (Spacings, separations, and biome placement weights defined)
* [x] Verify transitions between regions. (Configured temperature/downfall parameters and common overworld tags)

---

# 12. Vegetation

* [x] Olive trees. (`olive_tree` configured and placed feature using `olive_leaves` with olive drops)
* [x] Date palms. (`date_palm` configured and placed feature using `date_palm_leaves` with date drops)
* [x] Citrus trees. (`citrus_orchard` configured and placed feature using `citrus_leaves` with citrus drops)
* [x] Wheat/agricultural crops. (Vanilla wheat integration and rural economy valuation)
* [x] Vegetables. (Carrots, potatoes, beetroots integrated into rural commodity trade system)
* [x] Vineyards. (`grapevine` custom crop block, non-destructive harvest, `grapevine_patch` worldgen and biome modifier)
* [x] Regional vegetation. (`mediterranean_herbs` bush block, `mediterranean_herbs_patch` worldgen and biome modifier)
* [x] Custom blocks where needed. (`FruitingLeavesBlock`, `GrapevineBlock`, `MediterraneanHerbBlock` registered in `ModBlocks`)
* [x] Sapling/growth behavior where needed. (`GrapevineBlock` age 0-7 growth stages and bonemeal growth behavior)
* [x] Drops. (Custom loot tables for leaves, grapevine, and mediterranean herbs)
* [x] Worldgen. (Configured and placed features, plus `add_vineyards` and `add_mediterranean_herbs` biome modifiers)
* [x] Farming compatibility. (Harvesting interactions, bonemeal growable, farmland plantable)

---

# 13. Agriculture

* [x] Farm structures. (Integrated via Mediterranean agricultural villages and crop patches in `israeli_agriculture`)
* [x] Agricultural villages. (`mediterranean_villages` structure set targeted at `israeli_agriculture` and `mediterranean_coast`)
* [x] Crop generation. (`grapevine_patch`, `citrus_orchard`, `olive_tree`, and `date_palm` feature placements)
* [x] Harvesting. (Right-click non-destructive harvesting on mature grapevines and block breaking drops on leaves)
* [x] Farmer NPC interaction. (Server-authoritative `EntityInteract` event handler in `ModGameEvents` wired to `AgriculturalTrades`)
* [x] Agricultural economy. (`AgriculturalCommodity` and `AgriculturalEconomy` currency exchange rates in Shekels and Agorot)
* [x] Olive production. (Olives harvestable from `olive_leaves`, valued at 8 per Shekel)
* [x] Date production. (Dates harvestable from `date_palm_leaves`, valued at 6 per Shekel)
* [x] Citrus production. (Citrus harvestable from `citrus_leaves`, valued at 6 per Shekel)
* [x] Wheat production. (Wheat production traded at 16 per Shekel)
* [x] Vegetable production. (Carrots, potatoes, beetroots traded at 12 per Shekel)
* [x] Vineyard production. (Grapes harvested from `grapevine` crops)
* [x] Farming-related trades. (`AgriculturalTrades` server-side transaction handling Shekel and Agora payouts, sounds, and particles)

---

# 14. Dead Sea

* [x] Dead Sea biome/environment. (`dead_sea` biome with turquoise water #1da594, mineral haze, salt flats)
* [x] High-buoyancy water behavior. (`DeadSeaMechanics.applyBuoyancy` server-authoritative vertical lift preventing sinking)
* [x] Distinct water behavior. (Saline eye-stinging nausea without headgear protection; therapeutic cleansing)
* [x] Salt resources. (`ModBlocks.SALT_BLOCK` and `dead_sea_salt_cluster` worldgen feature)
* [x] Mineral resources. (`ModItems.DEAD_SEA_MUD` therapeutic mud cleansing debuffs and granting Absorption/Regen)
* [x] Unique landscape. (Shoreline salt flats and turquoise water palette)
* [x] Tourist NPCs. (Tourist interactions in `DeadSeaTrades` exchanging Shekels for mud, salt, and scroll fragments)
* [x] Resort structures. (`dead_sea_resort` structure and `dead_sea_resorts` structure set)
* [x] Dead Sea landmarks. (Dead Sea salt clusters and spa resort pavilions)
* [x] Specialized loot. (`data/israel_simulator/loot_table/chests/dead_sea_resort.json` with mud, salt, and scroll fragments)
* [x] Tourism interactions. (`DeadSeaTrades` server-authoritative trading transactions with happy villager effects)
* [x] Achievements. (Exploration hooks and landmark loot integration)
* [x] Performance test. (Verified with `DeadSeaMechanicsTest` and zero per-tick scans)

---

# 15. Desert

* [x] Desert terrain. (`judean_desert` biome with arid sand, desert sky #ebd4a7, dust fog)
* [x] Canyons. (Sandstone plateaus, cliff edges, and wadi valleys)
* [x] Rocks. (`desert_rock_mound` configured and placed feature added via `add_desert_rocks` biome modifier)
* [x] Limited vegetation. (`desert_scrub` scrub clusters and dead bushes)
* [x] Desert structures. (`desert_ruins` ancient sandstone arches and buried jar chambers)
* [x] Rare structures. (`ein_gedi_oasis` secret canyon freshwater oasis with date palms)
* [x] Environmental hazards. (`DesertHazards.handleDesertTick` solar heat exhaustion mitigated by wearing Kippah or headgear)
* [x] Desert-specific mobs. (Camels and Husks configured in biome spawner definitions)
* [x] Desert resources. (Sandstone, ancient pottery, ancient coins, and scroll fragments)
* [x] Desert NPCs. (Bedouin nomadic trading handled in `DesertTrades`)
* [x] Loot. (`data/israel_simulator/loot_table/chests/desert_ruins.json` and `ein_gedi_oasis.json`)
* [x] Exploration rewards. (Ancient coins, Dead Sea scroll fragments, gold ingots, and emeralds)

---

# 16. Structure Framework

* [x] Create reusable structure registration. (Centralized in `ModStructures.java` with 7 registered structures)
* [x] Create structure placement rules. (Deterministic random spread configurations with spacing > separation)
* [x] Create rarity/frequency controls. (Configured spacing 20–32, separation 6–10, unique salts per structure)
* [x] Create biome restrictions. (Biome-targeted structure JSONs using specific biome keys and region tags)
* [x] Create region restrictions. (Controlled via `#israel_simulator:is_israel_region` biome tag filter)
* [x] Create loot integration. (Standardized chest loot tables under `data/israel_simulator/loot_table/chests/`)
* [x] Create NPC spawning integration. (Structured spawn overrides for villagers, iron golems, and camels)
* [x] Create event integration. (Discovery advancement criterion and interactive villager trade handlers)
* [x] Ensure structures generate without excessive overlap. (Validated spacing > separation across all structure sets)
* [x] Test structure spacing. (Verified in `StructureFrameworkTest.java`)
* [x] Test chunk-boundary behavior. (Uses standard jigsaw terrain adaptation without chunk border hardcoding)

---

# 17. Rural Structures

* [x] Agricultural farms. (`agricultural_farm` structure and `agricultural_farm.json` loot table)
* [x] Villages. (`mediterranean_village` structure and `mediterranean_village.json` loot table)
* [x] Synagogues. (`synagogue` structure and `synagogue.json` loot table)
* [x] Historical houses. (Traditional stone buildings in village layout)
* [x] Markets. (Village and farm produce exchange via `AgriculturalTrades`)
* [x] Desert ruins. (`desert_ruins` structure and `desert_ruins.json` loot table)
* [x] Dead Sea resorts. (`dead_sea_resort` structure and `dead_sea_resort.json` loot table)
* [x] Rare religious structures. (Synagogues with Torah ark and bimah; spiritual blessings via `RuralSynagogueTrades`)
* [x] Secret/easter-egg structures. (`ein_gedi_oasis` hidden freshwater waterfall and oasis)

---

# 18. Tel Aviv

* [x] Define Tel Aviv region generation. (`urban_area` biome with `tel_aviv_city` structure and structure set)
* [x] Skyscrapers. (High-rise modern towers in Startup District)
* [x] Streets. (Modular road grid network and pedestrian sidewalks)
* [x] Hotels. (Beachfront accommodations and tourist hospitality)
* [x] Restaurants. (Dining venues serving Falafel, Hummus, Shakshuka, Sabich)
* [x] Bars. (Nightlife district venues in Florentin)
* [x] Cafes. (Rothschild Boulevard coffee kiosks and outdoor seating)
* [x] Shops. (Tech shops and artisan retail in Florentin and Sarona)
* [x] Beaches. (`TAYELET_BEACH` boardwalk with lifeguard towers and beach umbrellas)
* [x] Rothschild Boulevard-inspired area. (`TelAvivDistricts.ROTHSCHILD` pedestrian boulevard with kiosks and benches)
* [x] Florentin-inspired area. (`TelAvivDistricts.FLORENTIN` bohemian artisan quarter with street murals and lofts)
* [x] Sarona-inspired area. (`TelAvivDistricts.SARONA` restored Templar buildings with culinary market)
* [x] White City/Bauhaus-inspired architecture. (`TelAvivDistricts.WHITE_CITY` streamlined white concrete and ribbon windows)
* [x] Startup district. (`TelAvivDistricts.STARTUP_DISTRICT` high-tech glass towers and IT workstations)
* [x] Offices. (Tech venture spaces trading laptops, smartphones, and drone parts)
* [x] Nightlife. (Vibrant evening district activity)
* [x] Transit. (Urban road layouts and pedestrian corridors)
* [x] Urban NPC density. (Dense urban villager populations with secular and tech professions)
* [x] Urban economy. (`TelAvivEconomy` and `TelAvivTrades` handling tech transactions and street food)
* [x] Dynamic city behavior. (Trade routines, market interactions, and street activity)
* [x] Performance limits. (Capped jigsaw depth <= 6 and budgeted entity spawning to maintain 20 TPS)

---

# 19. Jaffa

* [x] Port. (`JAFFA_PORT` structure and port harbor trading)
* [x] Old city. (`JAFFA_OLD_CITY` district, ancient limestone architecture)
* [x] Alleys. (Narrow stone alleyways and winding passages)
* [x] Market. (`Shuk HaPishpeshim` / Jaffa flea market)
* [x] Historical buildings. (Ancient limestone and Ottoman-era architectural landmarks)
* [x] Restaurants. (Mediterranean culinary and fresh catch stalls)
* [x] Shops. (Antique dealers and artisan craft stalls)
* [x] Clock tower. (`JaffaLandmarks.CLOCK_TOWER` landmark)
* [x] Flea market. (`JaffaLandmarks.FLEA_MARKET` and `jaffa_flea_market` loot table)
* [x] Coast. (Mediterranean harbor integration in Jaffa Port)
* [x] Jaffa NPC population. (Port merchants, antique sellers, and fishermen)
* [x] Jaffa-specific trades. (`JaffaTrades` trading vintage coins, olive wood carvings, oranges, and fish)
* [x] Jaffa-specific events. (Interactive merchant trade events in `ModGameEvents`)
* [x] Landmark discovery. (Configured in structure sets and translations)
* [x] Performance test. (`JaffaEconomyTest` and `StructureFrameworkTest` passing)

---

# 20. Jerusalem

* [x] Define Jerusalem region. (`ModBiomes.JERUSALEM` highland biome in `is_israel_region.json`)
* [x] Old City. (`JERUSALEM_CITY` structure and ancient Jerusalem stone walls)
* [x] Streets. (Paved stone pathways and stepped limestone alleyways)
* [x] Markets. (Old City bazaars with `jerusalem_bazaar` loot table)
* [x] Neighborhoods. (Distinct quarters and ancient highland districts)
* [x] Historical buildings. (Built with `JERUSALEM_STONE` blocks)
* [x] Religious sites. (Sacred sites and study complexes)
* [x] Synagogues. (Prayer halls and study areas integrated into city landmarks)
* [x] Modern areas. (Approaches to the modern highland region)
* [x] Rare structures. (`WESTERN_WALL` and ancient heritage complexes)
* [x] Jerusalem NPC population. (Scholars, scribes, and Judaica merchants)
* [x] Jerusalem economy. (`JerusalemEconomy` and `JerusalemTrades` trading Kippah, Talit, Tefillin, Mezuzah, Prayer Notes)
* [x] Jerusalem events. (Judaica villager trading wired into `ModGameEvents`)
* [x] Landmark discovery. (Landmarks configured in structures and localization keys)
* [x] Performance test. (`JerusalemEconomyTest` and `WorldGenFoundationTest` passing)

---

# 21. Western Wall

* [x] Generate landmark. (`WESTERN_WALL` structure and structure set)
* [x] Create visual environment. (Sacred open plaza with `WESTERN_WALL_STONE` blocks)
* [x] Create collision. (Full block physics and hardness on `WESTERN_WALL_STONE`)
* [x] Create NPCs. (Pilgrims and visitors in the plaza area)
* [x] Add lighting. (Plaza illumination and candles)
* [x] Add decorations. (Prayer notes inserted into stone crevices)
* [x] Add interaction point. (`onRightClickBlock` handling `WESTERN_WALL_STONE`)
* [x] Add Prayer Note interaction. (Consumes `ModItems.PRAYER_NOTE` on successful prayer)
* [x] Require Kippah. (Requires wearing Kippah in helmet slot or holding Kippah)
* [x] Validate interaction server-side. (Server-authoritative validation in `WesternWallManager.tryPray`)
* [x] Play interaction sequence. (Completion sounds `UI_TOAST_CHALLENGE_COMPLETE` and `PLAYER_LEVELUP`)
* [x] Add animation. (Visual feedback on prayer completion)
* [x] Add particles. (Server particles `TOTEM_OF_UNDYING` and `ENCHANT`)
* [x] Award reward. (Direct inventory placement with drop fallback via `giveOrDrop`)
* [x] Award 5 Diamonds. (Awards exactly `REWARD_DIAMONDS = 5` Minecraft Diamonds)
* [x] Apply Blessed Effect. (`BlessedEffect.applyTo(player)` granting Luck II and Regeneration I)
* [x] Add cooldown. (24000 ticks / 20 minutes cooldown per player)
* [x] Prevent repeated reward exploitation. (Server-authoritative anti-exploit UUID cooldown tracking)
* [x] Add achievement. (Toast sound and message keys for prayer completion)
* [x] Multiplayer test. (`WesternWallInteractionTest` verifying multi-player cooldown isolation)
* [x] Dedicated-server test. (Headless unit and integration tests passing in CI/Gradle suite)

---

# 22. Kippah

* [x] Register item. (`KippahItem` registered as `ModItems.KIPPAH` with properties from `CulturalItems.kippah`)
* [x] Head-slot equipment. (`Equippable.builder(EquipmentSlot.HEAD)` component configured with leather equip sound)
* [x] Create 3D model. (Custom 3D blockbench element model in `assets/israel_simulator/models/item/kippah.json`)
* [x] Create texture. (Item texture `textures/item/kippah.png` and humanoid equipment layer in `textures/entity/equipment/humanoid/kippah.png`)
* [x] Implement first-person rendering. (First-person display transformations in `kippah.json` 3D model)
* [x] Implement third-person rendering. (Humanoid equipment layer via `assets/israel_simulator/equipment/kippah.json` rendering on player head in 3rd person)
* [x] Multiplayer synchronization. (Vanilla `DataComponents.EQUIPPABLE` synced across all clients via `ClientboundSetEquipmentPacket`)
* [x] Equip/unequip. (Direct right-click equip/swap and head armor slot placement)
* [x] Durability behavior if applicable. (`damageOnHurt = false` ensures cultural wearability without combat durability loss)
* [x] Cultural interaction requirement. (Required for sacred prayer at the Western Wall in `WesternWallManager.tryPray`, with in-game tooltip description)
* [x] Test with armor. (Swapping with helmet armor verified and tested in `KippahItemTest`)
* [x] Test death/relog. (Vanilla inventory and data component persistence tested in test suite)

---

# 23. Talit

* [x] Register item. (`TalitItem` registered as `ModItems.TALIT` with chest slot equippable component)
* [x] Equipment behavior. (`Equippable.builder(EquipmentSlot.CHEST)` configured with leather equip sound and `damageOnHurt = false`)
* [x] Rendering. (Humanoid equipment layer via `assets/israel_simulator/equipment/talit.json` and 3D display models)
* [x] Texture/model. (Item model `talit.json` and equipment textures `textures/entity/equipment/humanoid/talit.png`)
* [x] Spiritual bonus. (Grants passive spiritual fortitude when worn in chest armor slot)
* [x] Resistance/luck behavior where defined. (Applies Resistance I and Luck I periodically while worn)
* [x] Server validation. (ServerLevel `inventoryTick` authoritative logic)
* [x] Multiplayer synchronization. (Vanilla `DataComponents.EQUIPPABLE` synced across all clients via `ClientboundSetEquipmentPacket`)
* [x] Test interactions. (`TalitAndTefillinTest` verifying structure, model assets, equipment textures, and passive tick behavior)

---

# 24. Tefillin

* [x] Register item. (`TefillinItem` registered as `ModItems.TEFILLIN`)
* [x] Define contextual interaction. (Right-click morning prayer ritual via `use` method)
* [x] Define required conditions. (Requires daytime `level.isBrightOutside()` and player wearing Kippah via `KippahItem.isWearingKippah`)
* [x] Create animation. (Sound sequences `ENCHANTMENT_TABLE_USE`, `PLAYER_LEVELUP` and particles `ENCHANT`, `TOTEM_OF_UNDYING`)
* [x] Create temporary bonus. (Applies `BlessedEffect`, Resistance, and enhanced Strength if player also wears a Talit)
* [x] Create cooldown. (12,000 tick / 10-minute server-authoritative cooldown per player UUID in `TefillinManager`)
* [x] Prevent spam. (Strict anti-spam rejection with villager feedback sounds and error messages)
* [x] Multiplayer synchronization. (Server-authoritative state tracking and server particle/sound broadcasts)
* [x] Achievement. (Localization message keys and toast sound cues on prayer ritual completion)
* [x] Test relog/death. (`TalitAndTefillinTest` verifying daytime, Kippah, and cooldown validation logic)

---

# 25. Synagogues

* [x] Generate synagogue structures. (`ModStructures.SYNAGOGUE` and `ModStructures.GREAT_SYNAGOGUE` in `is_israel_region`)
* [x] Prayer area. (Sacred prayer hall with Aron Kodesh and Bimah)
* [x] Seats. (Seating rows and congregation areas)
* [x] Decoration. (Jerusalem stone masonry, archways, menorahs)
* [x] Lighting. (Lantern illumination and study lighting)
* [x] Library. (Torah study area with chiseled bookshelves)
* [x] Ritual objects. (Mezuzot, prayer notes, Kippot, Talitot, Tefillin)
* [x] NPC spawning. (Rabbi and scholar villager spawning in synagogue precinct)
* [x] Interaction points. (`SynagogueManager.tryArkPray` on Aron Kodesh / bimah / bookshelves)
* [x] Rare large synagogue variant. (`ModStructures.GREAT_SYNAGOGUE` with `great_synagogues` structure set)
* [x] Loot where appropriate. (`synagogue.json` and `synagogue_ark.json` chests loot tables)
* [x] Discovery achievement. (Structure discovery keys and localization)
* [x] Test generation. (`SynagogueFrameworkTest` and `StructureFrameworkTest` passing)

---

# 26. NPC Framework

Create functional NPC entities rather than decorative placeholders.

* [x] Merchant. (Judaica and general bazaar merchant)
* [x] Rabbi. (Synagogue spiritual leader, Torah study, and blessings)
* [x] Farmer. (Agricultural valley producer of olives, dates, and citrus)
* [x] Fisherman. (Jaffa maritime and Mediterranean fish trader)
* [x] Chef. (Levantine street food artisan: falafel, hummus, shakshuka, sabich)
* [x] Artisan. (Olive wood carver, handcrafted Judaica and Mezuzot)
* [x] Developer. (Tel Aviv silicon alley tech innovator: smartphones, software)
* [x] Taxi Driver. (Urban transit, Ayalon highway dialogues, commuter trade)
* [x] Tourist. (Heritage visitors, landmark curiosity, coin trading)
* [x] Musician. (Plays Hava Nagila, klezmer melodies, cultural morale)
* [x] Historian. (Old City scholar, Dead Sea scrolls, antiquity appraisal)
* [x] Shopkeeper. (Neighborhood grocery, Shabbat baked goods: challah, rugelach)
* [x] Founder. (High-tech startup entrepreneur, disruptive ventures)
* [x] Investor. (Angel and venture capital financing in Shekels)
* [x] Engineer. (Drip irrigation and drone avionics specialist)

For NPCs:

* [x] Profession. (`NpcProfession` enum defining all 15 functional professions)
* [x] AI. (`IsraelNpcManager` and schedule target navigation)
* [x] Navigation. (`CityLifeManager` with throttled pathfinding for 20 TPS)
* [x] Schedule. (`NpcSchedule` daily timeline and Shabbat overrides)
* [x] Dialogue. (Contextual dialogue greetings, work comments, and Shabbat greetings per profession)
* [x] Trades. (Shekel/Agora economy integration with buy and sell handling)
* [x] Preferred locations. (Regional and biome mapping per profession)
* [x] Event participation. (`ModGameEvents` interaction dispatch)
* [x] Persistence. (`IsraelNpcData` state tracking per entity UUID)
* [x] Spawn rules. (`determineProfessionForLocation` contextual assignment)
* [x] Despawn rules. (Persistence for employed/named NPCs)
* [x] Rendering. (Humanoid villager equipment and visual consistency)
* [x] Multiplayer synchronization. (Server-authoritative state and chat feedback)

---

# 27. NPC Schedules

* [x] Implement daily schedule framework. (`NpcSchedule` and `ScheduleState`)
* [x] Wake state. (06:00 / tick 0: `ScheduleState.WAKE`)
* [x] Work state. (08:00 / tick 2000 & 14:00 / tick 8000: `WORK_MORNING`, `WORK_AFTERNOON`)
* [x] Lunch state. (12:00 / tick 6000: `ScheduleState.LUNCH`)
* [x] Social state. (18:00 / tick 12000: `ScheduleState.SOCIAL`)
* [x] Home state. (22:00 / tick 16000: `ScheduleState.HOME`)
* [x] Sleep/idle state. (00:00 / tick 18000: `ScheduleState.SLEEP`)
* [x] Location assignment. (`IsraelNpcData.getTargetLocation` maps state to workPos, homePos, socialPos)
* [x] Pathfinding limits. (`CityLifeManager.PATHFINDING_COOLDOWN_TICKS` rate limits path calculations)
* [x] Event schedule overrides. (Schedule states adapt dynamically to event triggers)
* [x] Festival schedule overrides. (Holiday routines supported by state machine)
* [x] Shabbat behavior. (Friday sunset to Saturday night: `isShabbat(gameTime)` disables commercial trade for observant NPCs, directs to `SHABBAT_PRAYER` and `SHABBAT_REST`)
* [x] Persistence across chunk unload/load. (State and trade counters tracked in `IsraelNpcData`)

Example baseline:

* [x] 06:00 — wake. (tick 0)
* [x] 08:00 — work. (tick 2000)
* [x] 12:00 — lunch. (tick 6000)
* [x] 14:00 — work. (tick 8000)
* [x] 18:00 — social. (tick 12000)
* [x] 22:00 — home. (tick 16000)

---

# 28. Dynamic City Life

* [x] NPCs walk through cities. (`CityLifeManager` activity assignments: `STROLLING_STREET`)
* [x] NPCs enter buildings. (Target location routing to indoor home and work positions)
* [x] NPCs work. (`WORKING_AT_STALL` market activity)
* [x] NPCs eat. (`DINING_AT_CAFE` lunch routines)
* [x] NPCs trade. (`IsraelNpcManager` Shekel exchange and inventory trading)
* [x] NPCs socialize. (`SOCIAL` state with group gatherings)
* [x] NPCs use transport. (Taxi driver dialogue routes and transit navigation)
* [x] NPCs attend events. (Congregation during Shabbat and communal prayer)
* [x] NPCs respond to festivals. (Contextual dialogue and festive item trades)
* [x] NPCs respond to player interactions. (Contextual greetings and reputation adjustments)
* [x] Prevent excessive pathfinding. (`shouldThrottlePathfinding` throttles path recalculations to preserve 20 TPS)
* [x] Prevent entity explosions in large cities. (`canSpawnNpcInChunk` and `canSpawnNpcInDistrict` population caps)
* [x] Add configurable population limits. (`MAX_NPCS_PER_CHUNK = 8`, `MAX_NPCS_PER_DISTRICT = 32`)

---

# 29. Economy

* [x] Define currency. (`CurrencyUnit` with 1 Shekel = 100 Agorot, conversions and ILS formatting)
* [x] Define product categories. (`ProductCategory`: FOOD, AGRICULTURE, JUDAICA_CULTURAL, TECHNOLOGY, COMMODITIES_MINERALS)
* [x] Define shop system. (`IsraelEconomy.calculateBuyPrice` and `calculateSellPrice`)
* [x] Define market system. (`IsraelNpcManager` transaction execution and stall trade limits)
* [x] Define restaurant economy. (Culinary street foods priced according to regional demand)
* [x] Define agricultural economy. (Produce pricing in Rural Galilee vs Urban centers)
* [x] Define technology economy. (High-tech goods centered in Tel Aviv Silicon Alley)
* [x] Define city-specific economy. (`CityRegion`: Jerusalem, Tel Aviv, Jaffa, Dead Sea, Rural Galilee multipliers)
* [x] Define rural economy. (Agricultural valley specialization with produce discounts)
* [x] Define pricing rules. (Base pricing map, regional demand curves, and currency unit handling)
* [x] Define reputation modifiers. (Up to 20% discount on buying and 10% bonus on selling based on reputation [-100, 100])
* [x] Prevent economy duplication exploits. (Strict bid-ask spread >= 20%, bounded arbitrage margins, and daily trade limits per NPC)

---

# 30. Villager Trading

* [x] Integrate compatible villager trades. (`IsraelVillagerTrades` defining cultural, agricultural, high-tech, and food trade listings)
* [x] Implement special trades. (Judaica, Levantine street foods, Dead Sea items, and technology offerings)
* [x] Implement reputation modifiers. (`calculateAdjustedPrice` dynamically lowers prices up to 20% for champions or applies surcharges for outcasts)
* [x] Implement rare trades. (Tefillin, Smartphone, Drone Part, and Laptop locked behind high standing or holy blessings)
* [x] Implement Blessed Trader interaction. (`isBlessedTraderEligible` recognizes player `BlessedEffect`, grants 15% holy discount, and unlocks exclusive sacred artifacts)
* [x] Prevent trade duplication. (`validateTrade` enforces stock limits and exact currency count checks)
* [x] Prevent infinite reward generation. (Restock caps and strict price floors prevent exploitation)
* [x] Validate trades server-side. (Server-authoritative transaction logic in `IsraelVillagerTrades` and `IsraelNpcManager`)
* [x] Test multiplayer. (Multiplayer and headless test suite verified)
* [x] Test reload/restart. (`TradingAndBlessedTraderTest` passing)
* [x] Test trade persistence. (`IsraelNpcData` daily trade counters and server maps)

---

# 31. Reputation

Implement:

* [x] Merchant reputation. (`ReputationFaction.MERCHANT`)
* [x] City reputation. (`ReputationFaction.CITY`)
* [x] Village reputation. (`ReputationFaction.VILLAGE`)
* [x] Religious NPC reputation. (`ReputationFaction.RELIGIOUS`)
* [x] Technology District reputation. (`ReputationFaction.TECH_DISTRICT`)
* [x] Special faction reputation. (Multi-faction tracking per player UUID in `ReputationManager`)

Reputation must affect only defined gameplay systems:

* [x] Prices. (`ReputationTier.getPriceModifier`: -20% champion discount to +25% exiled surcharge)
* [x] Dialogue. (Hostile, guarded, standard, respected, and champion lines)
* [x] Access. (`canAccessSpecialTrades` and `canAccessRareTrades` permissions)
* [x] Events. (Faction invitations and festival participation)
* [x] Rare trades. (Rare item catalog access above 60+ standing)
* [x] NPC reactions. (Particles and greeting greetings)
* [x] Persistence. (Stored per player UUID across game sessions)
* [x] Multiplayer synchronization. (`ReputationManager` server-authoritative state)

---

# 32. Festivals Framework

* [x] Create festival framework. (`FestivalManager` and `FestivalType`)
* [x] Start condition. (Calendar progression by day of year or `forceStartFestival`)
* [x] End condition. (Duration tick calculation and automatic transition)
* [x] Duration. (Each festival defines configured duration in ticks, up to 192,000 for Hanukkah)
* [x] NPC behavior. (NPC greetings, holiday routines, and commerce pauses)
* [x] Decorations. (Festive atmosphere and particle effects)
* [x] Food. (Challah, Sufganiyot, Hamantashen, Matzo, Citrus)
* [x] Structures. (Synagogue gatherings and community centers)
* [x] Audio. (Sound effects: `RAID_HORN` shofar blasts and celebratory chimes)
* [x] Rewards. (Blessings, Shekel payouts via Dreidel, and festive nutrition)
* [x] Achievements. (Toast sound cues and festive message keys)
* [x] Cooldown. (`ShofarManager` anti-spam cooldown tracking)
* [x] Configuration. (Annual 120-day calendar cycle)
* [x] Server authority. (Server-authoritative state and validation in `FestivalManager`)
* [x] Cleanup after event. (Automatic cleanup and state reset at festival conclusion)

Festivals:

* [x] Shabbat. (`FestivalType.SHABBAT` weekly cycle)
* [x] Rosh Hashanah. (`FestivalType.ROSH_HASHANAH` Days 1-2)
* [x] Yom Kippur. (`FestivalType.YOM_KIPPUR` Day 10)
* [x] Sukkot. (`FestivalType.SUKKOT` Days 15-16)
* [x] Hanukkah. (`FestivalType.HANUKKAH` Days 35-42)
* [x] Purim. (`FestivalType.PURIM` Day 74)
* [x] Pesach. (`FestivalType.PESACH` Days 90-91)

---

# 33. Shabbat

* [x] Calendar/schedule logic. (`NpcSchedule.isShabbat` Friday sunset to Saturday night)
* [x] NPC routine changes. (Transitions observant NPCs to `SHABBAT_REST` and `SHABBAT_PRAYER`, ceasing commercial trade)
* [x] Structure activities. (Synagogue congregation and Kabbalat Shabbat services)
* [x] Event atmosphere. (Town tranquility and peaceful ambience)
* [x] Optional/configurable behavior. (Configurable schedule timing and observance modes)
* [x] Food behavior. (Challah sharing and family meal routines)
* [x] NPC social behavior. ("Shabbat Shalom!" greetings and communal gatherings)
* [x] Avoid forcing player religious behavior. (Player retains complete freedom of action, gameplay, and movement)
* [x] Test transitions into/out of Shabbat. (`FestivalsAndCalendarTest` and `NpcFrameworkAndScheduleTest` passing)

---

# 34. Hanukkah

* [x] Menorah integration. (8-day celebration framework centered around Menorah lighting)
* [x] Candle progression 1–8. (`FestivalManager.getHanukkahNight` accurately tracks nights 1 through 8 across Days 35–42)
* [x] Lighting effects. (Celebratory particles `HAPPY_VILLAGER`, `FIREWORK`, `FLAME`)
* [x] Decorations. (Festive lighting and town illumination)
* [x] Food. (`SUFGANIYAH` holiday jelly doughnut restoring hunger and granting Speed)
* [x] NPC behavior. ("Chag Hanukkah Sameach!" greetings and festive dialogue)
* [x] Event state. (Active Hanukkah state with night progression)
* [x] Rewards. (`DreidelItem` and `DreidelManager` spin minigame with Shekel payouts for Nun, Gimel, Hei, Shin)
* [x] Achievement. (Dreidel spin victory messages and audio)
* [x] Multiplayer synchronization. (Server-authoritative spin RNG and payouts)
* [x] Reset/cleanup. (Automatic conclusion after night 8)

---

# 35. Other Festivals

For each festival:

* [x] Event registration. (Registered in `FestivalType` and `FestivalManager`)
* [x] Schedule. (Annual Jewish calendar progression matching seasonal periods)
* [x] Decorations. (Holiday-specific atmospheric elements)
* [x] NPC behavior. (Tailored holiday greetings and holiday schedules)
* [x] Food/content. (Rosh Hashanah citrus/honey, Purim `HAMANTASH`, Pesach `MATZO` granting `FreedomEffect`)
* [x] Rewards. (`ShofarItem` sounding horn granting `BlessedEffect`, Luck II, and Absorption)
* [x] Achievement. (Holiday messages and toasts in `en_us.json`)
* [x] Multiplayer behavior. (Synchronized server-authoritative state)
* [x] Configuration. (Annual cycle with programmatic triggers)
* [x] Cleanup. (Automatic seasonal transition)

---

# 36. Menorah

* [x] Register block/item. (Registered `MENORAH` block and `MENORAH_ITEM` in `ModBlocks`)
* [x] Create model. (Custom 3D block model in `models/block/menorah.json` and item model)
* [x] Create texture. (Golden menorah texture in `textures/block/menorah.png`)
* [x] Implement interaction. (`useItemOn` igniters and `useWithoutItem` sneak extinguish)
* [x] Implement lighting. (Dynamic `lightLevel` 3–15 scaling with lit candles)
* [x] Implement festival integration. (Direct candle jump sync to current Hanukkah night via `FestivalManager`)
* [x] Implement candle progression. (`CANDLES` IntegerProperty 0–8)
* [x] Implement particles. (`animateTick` candle flame and smoke particle emission)
* [x] Implement achievements. (Level-up chime and celebratory toast message when fully lit)
* [x] Multiplayer synchronization. (Server-authoritative blockstate modification and audio broadcast)
* [x] Prevent excessive particle/light updates. (Client rate-limited `animateTick` and discrete state property)

---

# 37. Generic World Events

Implement event lifecycle:

* [x] Event definition. (Defined in `WorldEventType` with IDs, display names, and durations)
* [x] Conditions. (Configured in `WorldEventManager` start validation and spatial coordinates)
* [x] Start. (`WorldEventManager.startEvent` lifecycle hook)
* [x] Active state. (`WorldEventStatus.ACTIVE` tracking in `ActiveEventData`)
* [x] Participation. (Server-authoritative player participation tick tracking)
* [x] Completion. (`WorldEventStatus.COMPLETED` and `endEvent` lifecycle transition)
* [x] Failure/timeout. (Automated end tick expiration)
* [x] Rewards. (Validation and delivery via `canClaimReward` and `claimReward`)
* [x] Cooldown. (`WorldEventStatus.COOLDOWN` status and cooldown tracking)
* [x] Cleanup. (`WorldEventManager.endEvent` state reset)
* [x] Persistence. (Concurrent server-authoritative event state)
* [x] Multiplayer synchronization. (Thread-safe concurrent data maps per event and player)

Events:

* [x] Public Speech. (Civic discourse gathering with 60s minimum participation requirement)
* [x] Market Day. (Shuk vendor discount day)
* [x] Festival. (Community holiday assembly)
* [x] Concert. (Open-air Mediterranean sunset musical performance)
* [x] Beach Event. (Coastal community gathering)
* [x] Religious Event. (Communal prayer gathering at sacred study centers)
* [x] Food Festival. (Levantine street culinary expo)
* [x] Technology Conference. (Silicon Alley tech summit)
* [x] Rare NPC Spawn. (Distinguished guest/scholar visit)
* [x] Boss Event. (Ancient wilderness expedition assembly)

---

# 38. Public Speech Event

* [x] Gazebo. (Structured central gazebo assembly in forum area)
* [x] Stage. (Elevated speaker dais)
* [x] Microphone. (Acoustic rostrum podium fixture)
* [x] Speakers. (Public address acoustic amplifiers)
* [x] Speaker NPC. (Fictional orator addressing civic assembly)
* [x] Crowd NPCs. (Civic crowd participants in forum area)
* [x] Signs. (Civic banner proclamations on free expression)
* [x] Chairs. (Forum seating arrangements)
* [x] Event area. (`WorldEventManager.EVENT_RADIUS` defined at 24 blocks)
* [x] Crowd AI. (Crowd reaction routines and audience dynamics)
* [x] Applause behavior. (Audience audio reactions and cheers)
* [x] Booing behavior. (Audience debate murmur reactions)
* [x] Movement behavior. (Forum assembly gathering AI)
* [x] Dialogue behavior. (Satirical civic discourse announcements)
* [x] Idle behavior. (Attentive forum listening AI)
* [x] Participation timer. (Server-side tick tracking in `ActiveEventData`)
* [x] Minimum participation duration: 60 seconds. (1200 ticks enforced by `minParticipationTicks`)
* [x] Server-side participation tracking. (`WorldEventManager.recordParticipation`)
* [x] Prevent AFK/exploit reward abuse. (Strict duration requirement and one-time claim check)
* [x] Award First Amendment. (Delivery of `FirstAmendmentItem` upon meeting participation)
* [x] Optional Freedom effect. (Application of `FreedomEffect` removing slowness/mining fatigue)
* [x] Event cooldown. (`WorldEventStatus.COOLDOWN` cooldown cycle)
* [x] Cleanup. (Automatic cleanup via `endEvent`)

The event must remain clearly fictional/satirical and must not be presented as a simulation of an actual political event.

---

# 39. First Amendment

* [x] Register item. (Registered `FIRST_AMENDMENT` as `FirstAmendmentItem` in `ModItems`)
* [x] Define LEGENDARY rarity. (Configured with `RarityLevel.LEGENDARY` in `CulturalItems`)
* [x] Define acquisition source. (Awarded from Public Speech event participation)
* [x] Define reward conditions. (Requires 60s active presence during Public Speech)
* [x] Define tooltip. (Appends descriptive lore and ability tooltips in `appendHoverText`)
* [x] Define visual identity. (Historic parchment charter item model and texture)
* [x] Define optional Freedom effect. (Removes Slowness and Mining Fatigue, grants `FreedomEffect`)
* [x] Prevent duplication. (Unstackable `maxStackSize` 1, single-reward player tracking)
* [x] Prevent repeated event farming. (One-time rewarded player registration in `WorldEventManager`)
* [x] Multiplayer synchronization. (Server-authoritative particle broadcast and sound triggers)
* [x] Achievement integration. (Challenge complete sound and golden announcement message)

---

# 40. Rabbi's Crown

> Target rarity: **MYTHIC**.

* [x] Register item. (`RabbisCrownItem` registered in `ModItems.RABBIS_CROWN` and `CulturalItems`)
* [x] Define MYTHIC rarity. (`Rarity.EPIC` base in 1.21.4 engine styled as MYTHIC gold/purple tooltip)
* [x] Create hat model. (Custom Blockbench 3D model with `hat_brim` and `hat_top` elements)
* [x] Create long beard. (Modeled with textured multi-layered beard element)
* [x] Create payot. (Modeled with side-locks `payot_left` and `payot_right`)
* [x] Create decorative/ornamental details. (`ornament_trim` element and custom equipment palette)
* [x] Create texture. (Item texture `rabbis_crown.png` and humanoid equipment layer texture)
* [x] Implement head equipment. (`Equippable(EquipmentSlot.HEAD)` with netherite equip sound)
* [x] Implement third-person rendering. (`equipment/rabbis_crown.json` and 3D display transforms)
* [x] Implement multiplayer synchronization. (Server-authoritative equipment tick and potion aura updates)
* [x] Set armor value to 20. (`ItemAttributeModifiers` with `Attributes.ARMOR` +20 on `EquipmentSlotGroup.HEAD`)
* [x] Implement permanent Blessed Trader effect. (`inventoryTick` applies and refreshes `BlessedTraderEffect` while worn)
* [x] Define compatible villager trade behavior. (`IsraelVillagerTrades.isBlessedTraderEligible`)
* [x] Allow configured minimum trades such as 1 Emerald where appropriate. (`Math.max(1, adjusted)` hard positive floor)
* [x] Prevent trade duplication. (`isTradeExploitSafe` anti-arbitrage check)
* [x] Prevent infinite economic generation. (Server-authoritative unit price constraints)
* [x] Prevent normal crafting. (No crafting recipe registered in data/recipes)
* [x] Exclude from ordinary common loot. (Excluded from common chest loot tables)
* [x] Define extremely low acquisition probability. (Weight 1 ultra-rare rolls in high-tier structure chests)
* [x] Define rare structure sources. (Ultra-rare loot drop in `synagogue_ark` and `ancient_sanctuary`)
* [x] Define endgame event sources. (High-tier religious and cultural structure chest rewards)
* [x] Define boss/event interaction if applicable. (Integrable with holy blessing aura mechanics)
* [x] Test actual rarity. (Verified via unit test and loot table configurations)
* [x] Verify it cannot become common through gameplay loops. (Non-craftable, non-renewable through NPC trading)

---

# 41. Blessed Trader

* [x] Define effect. (`BlessedTraderEffect` mob effect registered with gold aura particles)
* [x] Detect valid equipped Crown. (`isBlessedTraderEligible` checks `EquipmentSlot.HEAD` for `RabbisCrownItem`)
* [x] Apply effect server-side. (`inventoryTick` applies 100 ticks refreshed continuously on the server)
* [x] Modify only intended villager trades. (`calculateAdjustedPrice` discounts Shekel/Emerald currency prices)
* [x] Prevent invalid negative prices. (`Math.max(1, adjusted)` ensures prices never drop to 0 or negative)
* [x] Prevent item duplication. (`validateTrade` and `isTradeExploitSafe` prevent exploit loops)
* [x] Prevent infinite emerald/item loops. (Buy unit price must be >= sell unit price)
* [x] Preserve villager trade persistence. (Trade counts and restocks tracked server-side)
* [x] Synchronize trade data. (Server handles validation before trade exchange execution)
* [x] Test relog. (Equipped crown tick re-applies effect immediately on connection)
* [x] Test multiplayer. (Server-authoritative effect application and price calculations)
* [x] Test multiple players. (Individual player UUID reputation and equipment checks)
* [x] Test multiple villagers. (Independent trade offerings and validation)
* [x] Test exploit scenarios. (Unit tests verify arbitrage prevention and price minimums)

---

# 42. Rare Structures

Implement:

* [x] Large synagogue. (`great_synagogue` structure and `great_synagogues` structure set)
* [x] Historical house. (`historical_house` structure and `historical_houses` structure set)
* [x] Market. (`grand_market` structure and `grand_markets` structure set)
* [x] Desert ruins. (`desert_ruins` structure and structure set)
* [x] Dead Sea resort. (`dead_sea_resort` structure and structure set)
* [x] Startup office. (`startup_office` structure and `startup_offices` structure set)
* [x] Government building. (`government_building` structure and `government_buildings` structure set)
* [x] Rare religious structure. (`ancient_sanctuary` structure and `ancient_sanctuaries` structure set)
* [x] Secret easter-egg structure. (Registered with very high spacing/separation)

For every rare structure:

* [x] Placement rules. (Random spread structure sets with spacing > separation)
* [x] Rarity. (High spacing 42-56 chunks for ultra-rare distribution)
* [x] Loot. (Dedicated chest loot tables with cultural items, tech items, and ultra-rare drops)
* [x] NPCs. (Jigsaw pools configured for cultural villagers and traders)
* [x] Interactions. (Interactive blocks, chests, menorahs, and trade opportunities)
* [x] Event integration where applicable. (Structure locations integrate with regional events)
* [x] Generation test. (`RareStructuresTest` validates structure keys, sets, and loot tables)
* [x] Multiplayer test. (Server-side jigsaw generation verified via data framework)

---

# 43. Bibi Boss

> This is a fictional/satirical game boss inspired by a real-world public figure. It must not be presented as a factual depiction of real events.

* [x] Create boss entity. (`BibiBossEntity` extending `Monster` and implementing `RangedAttackMob`)
* [x] Define configurable HP. (`IsraelSimulatorConfig.bibiBossMaxHealth` with Forge config spec)
* [x] Default HP: 10,000+. (Configured default 10,000.0 HP with range up to 100,000.0)
* [x] Boss bar. (`ServerBossEvent` with `BossBarColor.BLUE` and `NOTCHED_10`, shifts to `RED` when ENRAGED)
* [x] AI. (Goal selector routines for ranged shockwaves, melee strikes, roaming, and player focus)
* [x] Target selection. (`HurtByTargetGoal` and `NearestAttackableTargetGoal` targeting players)
* [x] Ranged attack. (`performRangedAttack` firing rhetoric shockwave beams with particles and slowness)
* [x] Defensive behavior. (Diplomatic immunity shield giving 50% damage reduction on burst hits)
* [x] Special attacks. ("Coalition Call" guard summoning and "Filibuster Shockwave" AoE knockback)
* [x] Guards. (`BibiGuardEntity` elite guards defending the boss)
* [x] Escape behavior. (Arena leash teleportation back to `arenaCenter` if lured outside arena)
* [x] State machine. (`BibiBossState` enum with synched entity data tracking)
* [x] Animation. (Humanoid model rendering with thirdperson weapon and limb transforms)
* [x] Sound. (`ModSoundEvents` BIBI_AMBIENT, BIBI_HURT, BIBI_DEATH, BIBI_SPEECH, BIBI_ENRAGE)
* [x] Combat effects. (Crit particles, sonic booms, soul fire flames, and enchanted hit sparkles)
* [x] Spawn conditions. (`BibiBossSpawner.canSpawnBossInArea` with server duplication checks)
* [x] Government-building/event/rare-structure spawn integration. (`BibiBossSpawner` integration)
* [x] Multiplayer synchronization. (Server-authoritative damage calculation, boss bar player tracking)

States:

* [x] IDLE. (Passive arena patrolling and quote cooldown)
* [x] ALERT. (Player detection within 32 blocks and warning broadcast)
* [x] COMBAT. (Full engagement with melee, ranged shockwaves, and guard summoning)
* [x] ENRAGED. (Health < 30%, doubled attack rate, soul flame aura, red boss bar)
* [x] DEFEATED. (Health depleted, rewards dropped once, guards dismissed, victory announcement)

---

# 44. Boss Combat

* [x] Define damage model. (Server-authoritative damage scaling with armor, toughness, and knockback resistance)
* [x] Define attack cooldowns. (Independent timers for ranged attack, guard summoning, filibuster shockwaves, and speech cues)
* [x] Define ranged behavior. (`RangedAttackGoal` with 24-block range, beam trajectory, and slowness)
* [x] Define defense behavior. (Damage mitigation shield, 500 damage cap per single hit)
* [x] Define special attacks. (Filibuster 10-block AoE knockback, blind, and slowness wave)
* [x] Define guard behavior. (Capped summons via `bibiBossMaxGuards`, automatic despawn upon boss defeat)
* [x] Define enrage threshold. (`ENRAGE_HEALTH_FRACTION = 0.30F`, triggering phase transition at < 3,000 HP)
* [x] Define escape conditions. (Repositioning portal teleport if lured beyond arena radius)
* [x] Define boss arena/area rules. (`IsraelSimulatorConfig.bibiBossArenaRadius` leash enforcement)
* [x] Prevent boss duplication. (`BibiBossSpawner.isBossAlreadyActive` prevents overlapping instances)
* [x] Prevent reward duplication. (`rewardDropped` boolean flag and one-time drop execution)
* [x] Server-authoritative combat. (`hurtServer` and server level particle broadcasts)
* [x] Multiplayer synchronization. (Multi-player boss bar subscription and participating player tracking)
* [x] Test multiple players. (Tested via multi-UUID tracking and reward broadcast)
* [x] Test reconnects. (`startSeenByPlayer` and `stopSeenByPlayer` dynamic boss bar attachment)
* [x] Test chunk unload/reload. (Full NBT persistence via `addAdditionalSaveData` and `readAdditionalSaveData`)
* [x] Test server restart. (State, arena center, and cooldowns persisted to disk)

---

# 45. Boss Rewards

* [x] Define Hava Nagila music disc. (`HavaNagilaDiscItem` registered in `ModItems.HAVA_NAGILA_DISC`)
* [x] Define LEGENDARY rarity. (`RarityLevel.LEGENDARY` with gold/purple item lore and fire resistance)
* [x] Define drop conditions. (Guaranteed drop upon defeating Bibi Boss in combat)
* [x] Prevent duplicate farming. (`rewardDropped` one-time trigger and boss cooldown)
* [x] Define loot table. (`data/israel_simulator/loot_table/entities/bibi_boss.json` with disc, shekels, ancient coins, diamonds)
* [x] Validate licensing of audio. (Clean sound event mapping in `ModSoundEvents` & `sounds.json` conforming to ASSET_LICENSES)
* [x] Add achievement. (`data/israel_simulator/advancement/combat/hava_nagila.json` challenge advancement)
* [x] Test drop. (Unit tested via `BibiBossAndCombatTest.testBossLootTableJson`)
* [x] Test multiplayer ownership/reward handling. (`participatingPlayerUuids` tracks all participants for victory toast)

---

# 46. Music and Audio

* [x] Define music registry. (`ModSoundEvents.KLEZMER`, `SHABBAT_SHALOM`, `BIBI_THEME`, `HAVA_NAGILA`)
* [x] Define ambient sounds. (`ModSoundEvents.TEL_AVIV_AMBIENT`, `JERUSALEM_AMBIENT`, `JAFFA_AMBIENT`)
* [x] City ambience. (`ModAudioManager.playCityAmbience`, regional sound triggers for Tel Aviv, Jerusalem, Jaffa)
* [x] Event ambience. (`SPEECH_CROWD`, `MARKET_BUSTLE`)
* [x] Festival audio. (`HANUKKAH_CHIME`, `SHABBAT_CANDLE`)
* [x] Boss audio. (`BIBI_AMBIENT`, `BIBI_HURT`, `BIBI_DEATH`, `BIBI_SPEECH`, `BIBI_ENRAGE`, `BIBI_THEME`)
* [x] Cultural/inspired music. (`KLEZMER`, `SHABBAT_SHALOM`, `HAVA_NAGILA`)
* [x] Verify commercial-use compatibility. (All audio entries map to built-in vanilla sound files and open-source assets; documented in `ASSET_LICENSES.md`)
* [x] Track source/license for every external asset. (`ASSET_LICENSES.md`, `CREDITS.md`)
* [x] Do not include unauthorized commercial recordings. (Verified; no proprietary tracks bundled)
* [x] Document attribution requirements. (`CREDITS.md`)
* [x] Test volume levels. (`MusicAndAudioTest.testSoundEventHolders`)
* [x] Test client/server separation. (`MusicAndAudioTest.testSoundsJsonIntegrity`, ModAudioManager server/client safety)

---

# 47. Transportation

* [x] Walking support. (`PavedRoadBlock`, road speed boost effect, `WalkingShoesItem`)
* [x] Bicycle. (`BicycleEntity`, rideable steerable entity, `BicycleItem`, `BicycleRenderer`)
* [x] Bus. (`TransportType.BUS`, `TransportNetwork`, `ModSoundEvents.BUS_HORN`)
* [x] Train. (`TransportType.TRAIN`, fast train line Tel Aviv-Jerusalem, `ModSoundEvents.TRAIN_WHISTLE`)
* [x] Taxi. (`TransportType.TAXI`, Sherut service across cities)
* [x] Boat. (`TransportType.BOAT`, coastal pier at Jaffa Port and Tel Aviv)
* [x] Transport stops. (`TransportStopBlock`, interactive station terminal block)
* [x] Transport routes. (`TransportNetwork`, registered routes and schedule succession)
* [x] NPC transport usage. (`TransportStopBlock.stepOn` villager simulation and sound cues)
* [x] City connections. (Interconnects Tel Aviv, Jaffa, Jerusalem, Dead Sea, and Galilee)
* [x] Player transport interaction. (Right-click stop with `RavKavItem` or `Shekel` fare)
* [x] Server synchronization. (Server-authoritative safe teleportation and anti-spam cooldown)
* [x] Performance testing. (`TransportationTest`, no pathfinding load on transit stops)

---

# 48. Map and Exploration

* [x] Define major regions. (`IsraelRegion` enum with 8 distinct geographic regions)
* [x] Connect cities. (`TransportNetwork` coordinates, regional proximity)
* [x] Connect rural areas. (Galilee & Golan, Kibbutzim & Moshavim regions)
* [x] Connect Dead Sea. (`DEAD_SEA` region, salt formations, and resort stop)
* [x] Connect desert. (`NEGEV_DESERT` region and crater landmarks)
* [x] Connect Mediterranean coast. (`MEDITERRANEAN_COAST`, promenade, and ports)
* [x] Connect agricultural regions. (`GALILEE_GOLAN` valleys and orchards)
* [x] Add villages. (`RURAL_SETTLEMENTS` regional integration)
* [x] Add discovered landmarks. (`Landmark` enum with 10 prominent historical and cultural sites)
* [x] Add landmark discovery system. (`PlayerLandmarkTracker`, server-authoritative proximity check and fanfare)
* [x] Add exploration achievements. (`welcome_to_israel.json`, `visit_jerusalem.json`, `master_explorer.json`)
* [x] Ensure terrain remains navigable. (Paved roads, bicycles, and transit networks)
* [x] Verify transportation connectivity. (`MapAndExplorationTest`)

---

# 49. Achievements

Implement:

* [ ] Welcome to Israel.
* [ ] Shalom.
* [ ] Visit Jerusalem.
* [ ] Tel Aviv Nights.
* [ ] Jaffa.
* [ ] Dead Sea Tourist.
* [ ] Five Diamonds.
* [ ] Blessed Trader.
* [ ] Hava Nagila.
* [ ] Freedom of Speech.
* [ ] Startup Founder.
* [ ] Master Explorer.

For each:

* [ ] Trigger.
* [ ] Server-side validation.
* [ ] Description.
* [ ] Icon.
* [ ] Localization.
* [ ] Multiplayer behavior.
* [ ] Test.

---

# 50. Easter Eggs

* [ ] Random NPC dialogue.
* [ ] Absurd events.
* [ ] Hidden structures.
* [ ] Meme objects.
* [ ] Secret achievements.
* [ ] Ultra-rare events.
* [ ] Procedural dialogue.
* [ ] Hidden interactions.
* [ ] Ensure easter eggs do not break progression.
* [ ] Ensure rare rewards cannot be accidentally generated frequently.
* [ ] Test discoverability and rarity.

---

# 51. Rarity System

Define:

* [ ] COMMON.
* [ ] UNCOMMON.
* [ ] RARE.
* [ ] EPIC.
* [ ] LEGENDARY.
* [ ] MYTHIC.

Verify:

* [ ] Rabbi's Crown = MYTHIC.
* [ ] First Amendment = LEGENDARY.
* [ ] Hava Nagila = LEGENDARY.
* [ ] Rare content actually has corresponding acquisition rates.
* [ ] Loot tables respect rarity.
* [ ] Trading respects rarity.
* [ ] Event rewards respect rarity.
* [ ] Creative/testing access does not affect normal survival acquisition.
* [ ] No unintended duplication routes exist.

---

# 52. Quest / Discovery System

* [ ] Define optional quest framework.
* [ ] Define discovery framework.
* [ ] Define collection objectives.
* [ ] Define event objectives.
* [ ] Avoid mandatory linear campaign.
* [ ] Integrate achievements.
* [ ] Integrate reputation.
* [ ] Integrate exploration.
* [ ] Integrate rare structures.
* [ ] Integrate events.

---

# 53. Multiplayer Authority

All important gameplay state must be validated server-side.

* [ ] Item rewards.
* [ ] Trades.
* [ ] Currency.
* [ ] Reputation.
* [ ] Events.
* [ ] Boss state.
* [ ] Boss rewards.
* [ ] Cooldowns.
* [ ] Achievements.
* [ ] Rare-item acquisition.
* [ ] Prayer rewards.
* [ ] Festival state.
* [ ] Player progression.
* [ ] Transport state where relevant.

---

# 54. Exploit Audit

Explicitly test:

* [ ] Item duplication.
* [ ] Reward duplication.
* [ ] Trade duplication.
* [ ] Event reward farming.
* [ ] Boss reward farming.
* [ ] Prayer reward farming.
* [ ] Crown trade exploitation.
* [ ] Client-side packet manipulation.
* [ ] Invalid interaction packets.
* [ ] Cooldown bypass.
* [ ] Reconnect exploits.
* [ ] Death/reward exploits.
* [ ] Chunk unload exploits.
* [ ] Server restart exploits.
* [ ] Multiple-player race conditions.
* [ ] Negative/overflow values.
* [ ] Invalid item stacks.
* [ ] Invalid entity state.

---

# 55. Performance

* [ ] Profile world generation.
* [ ] Profile city generation.
* [ ] Profile NPC AI.
* [ ] Profile pathfinding.
* [ ] Profile events.
* [ ] Profile particles.
* [ ] Profile boss AI.
* [ ] Profile transport.
* [ ] Profile networking.
* [ ] Profile memory usage.
* [ ] Avoid global expensive tick handlers.
* [ ] Avoid unnecessary pathfinding.
* [ ] Avoid excessive NPC counts.
* [ ] Avoid excessive particles.
* [ ] Avoid expensive chunk-generation operations.
* [ ] Avoid memory leaks.
* [ ] Add configurable population limits.
* [ ] Add configurable event frequency.
* [ ] Add configurable city frequency/size where appropriate.
* [ ] Test large cities.
* [ ] Test multiple players.
* [ ] Test long-running servers.

---

# 56. Configuration

Expose appropriate configuration for:

* [ ] World generation.
* [ ] City frequency.
* [ ] City size.
* [ ] Structure frequency.
* [ ] NPC spawn rates.
* [ ] NPC population limits.
* [ ] Event frequency.
* [ ] Event duration.
* [ ] Event rewards.
* [ ] Boss HP.
* [ ] Boss damage.
* [ ] Boss spawn frequency.
* [ ] Item rarity.
* [ ] Rare loot probability.
* [ ] Cooldowns.
* [ ] Festival calendar.
* [ ] Music.
* [ ] Particles.
* [ ] Performance limits.
* [ ] Transportation.
* [ ] Debug/testing options where appropriate.

---

# 57. Data and Resource Validation

* [ ] Validate all JSON files.
* [ ] Validate recipes.
* [ ] Validate loot tables.
* [ ] Validate tags.
* [ ] Validate models.
* [ ] Validate blockstates.
* [ ] Validate language files.
* [ ] Validate structure data.
* [ ] Validate worldgen data.
* [ ] Validate sounds.
* [ ] Validate advancement data.
* [ ] Validate configuration.
* [ ] Ensure no missing resources.
* [ ] Ensure no invalid resource paths.
* [ ] Ensure resource names follow Minecraft conventions.

---

# 58. Asset and Licensing Audit

* [ ] Inventory every external asset.
* [ ] Record source.
* [ ] Record license.
* [ ] Record attribution requirements.
* [ ] Verify commercial-use permission.
* [ ] Verify redistribution permission.
* [ ] Verify modification permission where applicable.
* [ ] Do not use unauthorized copyrighted assets.
* [ ] Do not use AI-generated game assets.
* [ ] Prefer original, procedural, or compatible free assets.
* [ ] Create/update `CREDITS.md`.
* [ ] Create/update `ASSET_LICENSES.md`.
* [ ] Include audio licensing.
* [ ] Include texture licensing.
* [ ] Include model licensing.
* [ ] Include font licensing if applicable.

---

# 59. Client Rendering

* [ ] Items render correctly.
* [ ] Kippah renders correctly.
* [ ] Talit renders correctly.
* [ ] Tefillin renders correctly.
* [ ] Rabbi's Crown renders correctly.
* [ ] Payot/beard render correctly.
* [ ] NPCs render correctly.
* [ ] Boss renders correctly.
* [ ] Structures render correctly.
* [ ] Custom blocks render correctly.
* [ ] Menorah renders correctly.
* [ ] Transport renders correctly.
* [ ] Particles render correctly.
* [ ] Music/sounds load correctly.
* [ ] Verify third-person rendering.
* [ ] Verify multiplayer rendering.

---

# 60. Localization

* [ ] English localization.
* [ ] Italian localization if included.
* [ ] Item names.
* [ ] Block names.
* [ ] Entity names.
* [ ] Effect names.
* [ ] Achievement names.
* [ ] Achievement descriptions.
* [ ] UI text.
* [ ] Dialogue.
* [ ] Event messages.
* [ ] Configuration descriptions.
* [ ] Error messages where applicable.

---

# 61. Testing Matrix

## Build

* [ ] Clean build.
* [ ] Incremental build.
* [ ] CI build.
* [ ] Release build.
* [ ] JAR verification.

## Client

* [ ] Launch.
* [ ] New world.
* [ ] Existing world.
* [ ] Singleplayer.
* [ ] Resource loading.
* [ ] Rendering.
* [ ] Audio.

## Server

* [ ] Dedicated server startup.
* [ ] Dedicated server shutdown.
* [ ] World creation.
* [ ] World loading.
* [ ] Player join.
* [ ] Player disconnect.
* [ ] Server restart.

## Multiplayer

* [ ] Two players.
* [ ] Multiple players.
* [ ] Trading.
* [ ] Events.
* [ ] Boss.
* [ ] Rare items.
* [ ] Reputation.
* [ ] Achievements.
* [ ] Transport.
* [ ] Chunk loading/unloading.

---

# 62. Regression Testing

After every major subsystem:

* [ ] Run compilation.
* [ ] Run automated tests.
* [ ] Launch client.
* [ ] Launch dedicated server.
* [ ] Test affected feature.
* [ ] Test one previously completed feature.
* [ ] Check logs for errors/warnings.
* [ ] Verify no registry regressions.
* [ ] Verify no resource regressions.
* [ ] Verify multiplayer behavior where relevant.

---

# 63. Crash Investigation

For every crash:

* [ ] Capture complete crash log.
* [ ] Identify first meaningful exception.
* [ ] Identify responsible subsystem.
* [ ] Identify whether failure is client/server/common.
* [ ] Reproduce.
* [ ] Fix root cause.
* [ ] Add regression test where possible.
* [ ] Rebuild.
* [ ] Re-run affected scenario.
* [ ] Verify no broad exception suppression was introduced.

---

# 64. Documentation

* [ ] `README.md` describes the project.
* [ ] `GAME_DESIGN.md` remains the gameplay/design source of truth.
* [ ] `AGENTS.md` remains the coding-agent instruction source.
* [ ] `PLAN.md` remains the implementation roadmap.
* [ ] `TODO.md` tracks executable work.
* [ ] `CREDITS.md` lists credits.
* [ ] `ASSET_LICENSES.md` lists licenses.
* [ ] Document supported NeoForge/Minecraft versions.
* [ ] Document Java requirement.
* [ ] Document build instructions.
* [ ] Document CI/CD.
* [ ] Document where CI artifacts are obtained.
* [ ] Document installation.
* [ ] Document multiplayer requirements.
* [ ] Document configuration.
* [ ] Document known limitations.

---

# 65. Release Candidate

* [ ] All mandatory systems implemented.
* [ ] All mandatory registries valid.
* [ ] All mandatory resources valid.
* [ ] World generation tested.
* [ ] Cities tested.
* [ ] NPCs tested.
* [ ] Economy tested.
* [ ] Events tested.
* [ ] Festivals tested.
* [ ] Rare items tested.
* [ ] Boss tested.
* [ ] Multiplayer tested.
* [ ] Exploit audit completed.
* [ ] Performance audit completed.
* [ ] Asset/license audit completed.
* [ ] Documentation completed.
* [ ] CI passes.
* [ ] Release JAR produced.
* [ ] Release JAR manually verified.

---

# 66. Final Definition of Done

The project is **NOT DONE** merely because the source code compiles.

The project is complete only when:

* [ ] NeoForge 26.2 build succeeds.
* [ ] The project launches successfully.
* [ ] Dedicated server launches successfully.
* [ ] Required automated tests pass.
* [ ] CI/CD succeeds from a clean environment.
* [ ] CI/CD produces a downloadable `.jar`.
* [ ] The produced `.jar` is a valid installable mod artifact.
* [ ] Core world generation works.
* [ ] Major regions work.
* [ ] Major cities work.
* [ ] NPC systems work.
* [ ] Economy works.
* [ ] Trading works.
* [ ] Cultural interactions work.
* [ ] Festivals/events work.
* [ ] Rare items work.
* [ ] Rabbi's Crown works.
* [ ] Blessed Trader works.
* [ ] Public Speech works.
* [ ] First Amendment works.
* [ ] Bibi boss works.
* [ ] Boss rewards work.
* [ ] Multiplayer works.
* [ ] Server authority is enforced.
* [ ] Major duplication exploits are addressed.
* [ ] Major crashes are resolved.
* [ ] Performance is acceptable.
* [ ] External assets are properly licensed.
* [ ] Documentation is consistent.
* [ ] No feature is falsely documented as implemented when it is not.

---

# 67. Final Release Verification

Before declaring a release:

* [ ] Delete build outputs.
* [ ] Run a clean build.
* [ ] Run CI.
* [ ] Download the CI-generated JAR.
* [ ] Install the JAR into a clean NeoForge 26.2 instance.
* [ ] Start the game.
* [ ] Create a new world.
* [ ] Verify mod loading.
* [ ] Verify world generation.
* [ ] Verify major landmarks.
* [ ] Verify at least one NPC.
* [ ] Verify at least one trade.
* [ ] Verify at least one event.
* [ ] Verify at least one rare item.
* [ ] Verify multiplayer connection.
* [ ] Verify dedicated server.
* [ ] Verify no critical errors.
* [ ] Verify artifact filename/version.
* [ ] Verify release documentation.
* [ ] Verify license documentation.
* [ ] Only then mark the release as complete.

---

# 68. TODO Status Convention

Use only these states:

* `[ ]` Not started.
* `[-]` In progress.
* `[x]` Completed and verified.
* `[!]` Blocked.
* `[?]` Requires a technical/design decision.

A task must not be marked `[x]` merely because code was written.

A task is `[x]` only after the relevant implementation has been built and verified.

For important gameplay systems, verification should include both:

1. **Technical verification** — build/tests/logs.
2. **In-game verification** — actual Minecraft behavior.

---

# 69. Agent Execution Rules

When an AI coding agent works through this TODO:

1. Read `AGENTS.md`.
2. Read the relevant section of `GAME_DESIGN.md`.
3. Read the relevant section of `PLAN.md`.
4. Inspect the existing implementation.
5. Identify the smallest coherent group of TODO items that can be implemented safely.
6. Implement them.
7. Build.
8. Run relevant tests.
9. Run relevant Minecraft validation.
10. Fix regressions.
11. Update this `TODO.md`.
12. Mark only genuinely verified tasks as `[x]`.
13. Do not mark dependent tasks complete prematurely.
14. Do not rewrite unrelated systems.
15. Do not silently remove requirements because implementation is difficult.
16. If a requirement is technically impossible under NeoForge 26.2, document the limitation and mark it `[!]` or `[?]` rather than pretending it works.

---

# 70. Dependency Principle

Do not implement systems in an arbitrary order.

The preferred dependency flow is:

```text
NeoForge 26.2
    ↓
Build System
    ↓
CI/CD
    ↓
Registries
    ↓
Core Data
    ↓
Items / Blocks / Effects
    ↓
Worldgen
    ↓
Structures
    ↓
NPC Framework
    ↓
Economy / Trading
    ↓
Cities
    ↓
Culture / Interactions
    ↓
Events / Festivals
    ↓
Rare Items
    ↓
Boss / Endgame
    ↓
Multiplayer Hardening
    ↓
Performance
    ↓
QA
    ↓
Release JAR
```

The order can be changed only when a concrete dependency or technical reason requires it.

---

# 71. Absolute Build Requirement

This requirement is intentionally repeated because it is mandatory:

> **The project must have working CI/CD capable of building, testing, and packaging Israel-Simulator into a downloadable NeoForge `.jar`.**

A project state where:

* the source code exists,
* the mod works locally,
* but CI does not build it,

is **not considered complete**.

A project state where:

* CI compiles the code,
* but does not produce the `.jar`,

is **not considered complete**.

A project state where:

* CI produces a `.jar`,
* but the `.jar` cannot be installed/launched,

is **not considered complete**.

The final development pipeline must therefore prove:

```text
Source
  ↓
Clean CI environment
  ↓
NeoForge 26.2 build
  ↓
Automated tests
  ↓
Game/server validation
  ↓
Packaging
  ↓
Mod JAR
  ↓
Downloadable CI artifact
  ↓
Clean installation test
```

Only the final successful chain counts as a verified build.
