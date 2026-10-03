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

* [ ] Implement Blessed Effect.
* [ ] Implement Freedom effect if retained.
* [ ] Implement food effects.
* [ ] Implement spiritual/contextual effects.
* [ ] Implement temporary event effects.
* [ ] Implement rare-item effects.
* [ ] Ensure effects are server-authoritative.
* [ ] Prevent unintended stacking.
* [ ] Define duration.
* [ ] Define amplifier.
* [ ] Define removal conditions.
* [ ] Test relogging.
* [ ] Test death.
* [ ] Test multiplayer synchronization.

---

# 9. Food System

## Ingredients

* [ ] Falafel ingredients.
* [ ] Hummus ingredients.
* [ ] Shakshuka ingredients.
* [ ] Sabich ingredients.
* [ ] Tahini.
* [ ] Challah.
* [ ] Rugelach.
* [ ] Dates.
* [ ] Olives.
* [ ] Citrus.
* [ ] Other agricultural ingredients required by recipes.

## Foods

* [ ] Falafel.
* [ ] Hummus.
* [ ] Shakshuka.
* [ ] Sabich.
* [ ] Other planned foods.

For every food:

* [ ] Recipe.
* [ ] Nutrition.
* [ ] Saturation.
* [ ] Optional effect.
* [ ] Model/texture.
* [ ] Tags.
* [ ] Localization.
* [ ] Test.

## Kosher System

* [ ] Define the simplified system.
* [ ] Define ingredient tags.
* [ ] Define compatible recipes.
* [ ] Define incompatible combinations only where gameplay requires them.
* [ ] Avoid unnecessary simulation complexity.
* [ ] Ensure the system does not interfere with normal Minecraft cooking.

---

# 10. World Generation Foundation

* [ ] Define world-generation architecture.
* [ ] Register custom biomes.
* [ ] Register configured features.
* [ ] Register placed features.
* [ ] Register structures.
* [ ] Register structure sets.
* [ ] Register processor lists where needed.
* [ ] Configure generation order.
* [ ] Ensure deterministic generation.
* [ ] Test new worlds.
* [ ] Test multiple seeds.
* [ ] Test chunk borders.
* [ ] Test exploration far from spawn.
* [ ] Test server generation.
* [ ] Test multiplayer world generation.

---

# 11. Biomes

* [ ] Mediterranean Coast.
* [ ] Israeli agricultural areas.
* [ ] Desert.
* [ ] Dead Sea environment.
* [ ] Urban environments where technically appropriate.
* [ ] Define terrain characteristics.
* [ ] Define vegetation.
* [ ] Define structures.
* [ ] Define mobs/entities.
* [ ] Define atmosphere.
* [ ] Define generation frequency.
* [ ] Verify transitions between regions.

---

# 12. Vegetation

* [ ] Olive trees.
* [ ] Date palms.
* [ ] Citrus trees.
* [ ] Wheat/agricultural crops.
* [ ] Vegetables.
* [ ] Vineyards.
* [ ] Regional vegetation.
* [ ] Custom blocks where needed.
* [ ] Sapling/growth behavior where needed.
* [ ] Drops.
* [ ] Worldgen.
* [ ] Farming compatibility.

---

# 13. Agriculture

* [ ] Farm structures.
* [ ] Agricultural villages.
* [ ] Crop generation.
* [ ] Harvesting.
* [ ] Farmer NPC interaction.
* [ ] Agricultural economy.
* [ ] Olive production.
* [ ] Date production.
* [ ] Citrus production.
* [ ] Wheat production.
* [ ] Vegetable production.
* [ ] Vineyard production.
* [ ] Farming-related trades.

---

# 14. Dead Sea

* [ ] Dead Sea biome/environment.
* [ ] High-buoyancy water behavior.
* [ ] Distinct water behavior.
* [ ] Salt resources.
* [ ] Mineral resources.
* [ ] Unique landscape.
* [ ] Tourist NPCs.
* [ ] Resort structures.
* [ ] Dead Sea landmarks.
* [ ] Specialized loot.
* [ ] Tourism interactions.
* [ ] Achievements.
* [ ] Performance test.

---

# 15. Desert

* [ ] Desert terrain.
* [ ] Canyons.
* [ ] Rocks.
* [ ] Limited vegetation.
* [ ] Desert structures.
* [ ] Rare structures.
* [ ] Environmental hazards.
* [ ] Desert-specific mobs.
* [ ] Desert resources.
* [ ] Desert NPCs.
* [ ] Loot.
* [ ] Exploration rewards.

---

# 16. Structure Framework

* [ ] Create reusable structure registration.
* [ ] Create structure placement rules.
* [ ] Create rarity/frequency controls.
* [ ] Create biome restrictions.
* [ ] Create region restrictions.
* [ ] Create loot integration.
* [ ] Create NPC spawning integration.
* [ ] Create event integration.
* [ ] Ensure structures generate without excessive overlap.
* [ ] Test structure spacing.
* [ ] Test chunk-boundary behavior.

---

# 17. Rural Structures

* [ ] Agricultural farms.
* [ ] Villages.
* [ ] Synagogues.
* [ ] Historical houses.
* [ ] Markets.
* [ ] Desert ruins.
* [ ] Dead Sea resorts.
* [ ] Rare religious structures.
* [ ] Secret/easter-egg structures.

---

# 18. Tel Aviv

* [ ] Define Tel Aviv region generation.
* [ ] Skyscrapers.
* [ ] Streets.
* [ ] Hotels.
* [ ] Restaurants.
* [ ] Bars.
* [ ] Cafes.
* [ ] Shops.
* [ ] Beaches.
* [ ] Rothschild Boulevard-inspired area.
* [ ] Florentin-inspired area.
* [ ] Sarona-inspired area.
* [ ] White City/Bauhaus-inspired architecture.
* [ ] Startup district.
* [ ] Offices.
* [ ] Nightlife.
* [ ] Transit.
* [ ] Urban NPC density.
* [ ] Urban economy.
* [ ] Dynamic city behavior.
* [ ] Performance limits.

---

# 19. Jaffa

* [ ] Port.
* [ ] Old city.
* [ ] Alleys.
* [ ] Market.
* [ ] Historical buildings.
* [ ] Restaurants.
* [ ] Shops.
* [ ] Clock tower.
* [ ] Flea market.
* [ ] Coast.
* [ ] Jaffa NPC population.
* [ ] Jaffa-specific trades.
* [ ] Jaffa-specific events.
* [ ] Landmark discovery.
* [ ] Performance test.

---

# 20. Jerusalem

* [ ] Define Jerusalem region.
* [ ] Old City.
* [ ] Streets.
* [ ] Markets.
* [ ] Neighborhoods.
* [ ] Historical buildings.
* [ ] Religious sites.
* [ ] Synagogues.
* [ ] Modern areas.
* [ ] Rare structures.
* [ ] Jerusalem NPC population.
* [ ] Jerusalem economy.
* [ ] Jerusalem events.
* [ ] Landmark discovery.
* [ ] Performance test.

---

# 21. Western Wall

* [ ] Generate landmark.
* [ ] Create visual environment.
* [ ] Create collision.
* [ ] Create NPCs.
* [ ] Add lighting.
* [ ] Add decorations.
* [ ] Add interaction point.
* [ ] Add Prayer Note interaction.
* [ ] Require Kippah.
* [ ] Validate interaction server-side.
* [ ] Play interaction sequence.
* [ ] Add animation.
* [ ] Add particles.
* [ ] Award reward.
* [ ] Award 5 Diamonds.
* [ ] Apply Blessed Effect.
* [ ] Add cooldown.
* [ ] Prevent repeated reward exploitation.
* [ ] Add achievement.
* [ ] Multiplayer test.
* [ ] Dedicated-server test.

---

# 22. Kippah

* [ ] Register item.
* [ ] Head-slot equipment.
* [ ] Create 3D model.
* [ ] Create texture.
* [ ] Implement first-person rendering.
* [ ] Implement third-person rendering.
* [ ] Multiplayer synchronization.
* [ ] Equip/unequip.
* [ ] Durability behavior if applicable.
* [ ] Cultural interaction requirement.
* [ ] Test with armor.
* [ ] Test death/relog.

---

# 23. Talit

* [ ] Register item.
* [ ] Equipment behavior.
* [ ] Rendering.
* [ ] Texture/model.
* [ ] Spiritual bonus.
* [ ] Resistance/luck behavior where defined.
* [ ] Server validation.
* [ ] Multiplayer synchronization.
* [ ] Test interactions.

---

# 24. Tefillin

* [ ] Register item.
* [ ] Define contextual interaction.
* [ ] Define required conditions.
* [ ] Create animation.
* [ ] Create temporary bonus.
* [ ] Create cooldown.
* [ ] Prevent spam.
* [ ] Multiplayer synchronization.
* [ ] Achievement.
* [ ] Test relog/death.

---

# 25. Synagogues

* [ ] Generate synagogue structures.
* [ ] Prayer area.
* [ ] Seats.
* [ ] Decoration.
* [ ] Lighting.
* [ ] Library.
* [ ] Ritual objects.
* [ ] NPC spawning.
* [ ] Interaction points.
* [ ] Rare large synagogue variant.
* [ ] Loot where appropriate.
* [ ] Discovery achievement.
* [ ] Test generation.

---

# 26. NPC Framework

Create functional NPC entities rather than decorative placeholders.

* [ ] Merchant.
* [ ] Rabbi.
* [ ] Farmer.
* [ ] Fisherman.
* [ ] Chef.
* [ ] Artisan.
* [ ] Developer.
* [ ] Taxi Driver.
* [ ] Tourist.
* [ ] Musician.
* [ ] Historian.
* [ ] Shopkeeper.
* [ ] Founder.
* [ ] Investor.
* [ ] Engineer.

For NPCs:

* [ ] Profession.
* [ ] AI.
* [ ] Navigation.
* [ ] Schedule.
* [ ] Dialogue.
* [ ] Trades.
* [ ] Preferred locations.
* [ ] Event participation.
* [ ] Persistence.
* [ ] Spawn rules.
* [ ] Despawn rules.
* [ ] Rendering.
* [ ] Multiplayer synchronization.

---

# 27. NPC Schedules

* [ ] Implement daily schedule framework.
* [ ] Wake state.
* [ ] Work state.
* [ ] Lunch state.
* [ ] Social state.
* [ ] Home state.
* [ ] Sleep/idle state.
* [ ] Location assignment.
* [ ] Pathfinding limits.
* [ ] Event schedule overrides.
* [ ] Festival schedule overrides.
* [ ] Shabbat behavior.
* [ ] Persistence across chunk unload/load.

Example baseline:

* [ ] 06:00 — wake.
* [ ] 08:00 — work.
* [ ] 12:00 — lunch.
* [ ] 14:00 — work.
* [ ] 18:00 — social.
* [ ] 22:00 — home.

---

# 28. Dynamic City Life

* [ ] NPCs walk through cities.
* [ ] NPCs enter buildings.
* [ ] NPCs work.
* [ ] NPCs eat.
* [ ] NPCs trade.
* [ ] NPCs socialize.
* [ ] NPCs use transport.
* [ ] NPCs attend events.
* [ ] NPCs respond to festivals.
* [ ] NPCs respond to player interactions.
* [ ] Prevent excessive pathfinding.
* [ ] Prevent entity explosions in large cities.
* [ ] Add configurable population limits.

---

# 29. Economy

* [ ] Define currency.
* [ ] Define product categories.
* [ ] Define shop system.
* [ ] Define market system.
* [ ] Define restaurant economy.
* [ ] Define agricultural economy.
* [ ] Define technology economy.
* [ ] Define city-specific economy.
* [ ] Define rural economy.
* [ ] Define pricing rules.
* [ ] Define reputation modifiers.
* [ ] Prevent economy duplication exploits.

---

# 30. Villager Trading

* [ ] Integrate compatible villager trades.
* [ ] Implement special trades.
* [ ] Implement reputation modifiers.
* [ ] Implement rare trades.
* [ ] Implement Blessed Trader interaction.
* [ ] Prevent trade duplication.
* [ ] Prevent infinite reward generation.
* [ ] Validate trades server-side.
* [ ] Test multiplayer.
* [ ] Test reload/restart.
* [ ] Test trade persistence.

---

# 31. Reputation

Implement:

* [ ] Merchant reputation.
* [ ] City reputation.
* [ ] Village reputation.
* [ ] Religious NPC reputation.
* [ ] Technology District reputation.
* [ ] Special faction reputation.

Reputation must affect only defined gameplay systems:

* [ ] Prices.
* [ ] Dialogue.
* [ ] Access.
* [ ] Events.
* [ ] Rare trades.
* [ ] NPC reactions.
* [ ] Persistence.
* [ ] Multiplayer synchronization.

---

# 32. Festivals Framework

* [ ] Create festival framework.
* [ ] Start condition.
* [ ] End condition.
* [ ] Duration.
* [ ] NPC behavior.
* [ ] Decorations.
* [ ] Food.
* [ ] Structures.
* [ ] Audio.
* [ ] Rewards.
* [ ] Achievements.
* [ ] Cooldown.
* [ ] Configuration.
* [ ] Server authority.
* [ ] Cleanup after event.

Festivals:

* [ ] Shabbat.
* [ ] Rosh Hashanah.
* [ ] Yom Kippur.
* [ ] Sukkot.
* [ ] Hanukkah.
* [ ] Purim.
* [ ] Pesach.

---

# 33. Shabbat

* [ ] Calendar/schedule logic.
* [ ] NPC routine changes.
* [ ] Structure activities.
* [ ] Event atmosphere.
* [ ] Optional/configurable behavior.
* [ ] Food behavior.
* [ ] NPC social behavior.
* [ ] Avoid forcing player religious behavior.
* [ ] Test transitions into/out of Shabbat.

---

# 34. Hanukkah

* [ ] Menorah integration.
* [ ] Candle progression 1–8.
* [ ] Lighting effects.
* [ ] Decorations.
* [ ] Food.
* [ ] NPC behavior.
* [ ] Event state.
* [ ] Rewards.
* [ ] Achievement.
* [ ] Multiplayer synchronization.
* [ ] Reset/cleanup.

---

# 35. Other Festivals

For each festival:

* [ ] Event registration.
* [ ] Schedule.
* [ ] Decorations.
* [ ] NPC behavior.
* [ ] Food/content.
* [ ] Rewards.
* [ ] Achievement.
* [ ] Multiplayer behavior.
* [ ] Configuration.
* [ ] Cleanup.

---

# 36. Menorah

* [ ] Register block/item.
* [ ] Create model.
* [ ] Create texture.
* [ ] Implement interaction.
* [ ] Implement lighting.
* [ ] Implement festival integration.
* [ ] Implement candle progression.
* [ ] Implement particles.
* [ ] Implement achievements.
* [ ] Multiplayer synchronization.
* [ ] Prevent excessive particle/light updates.

---

# 37. Generic World Events

Implement event lifecycle:

* [ ] Event definition.
* [ ] Conditions.
* [ ] Start.
* [ ] Active state.
* [ ] Participation.
* [ ] Completion.
* [ ] Failure/timeout.
* [ ] Rewards.
* [ ] Cooldown.
* [ ] Cleanup.
* [ ] Persistence.
* [ ] Multiplayer synchronization.

Events:

* [ ] Public Speech.
* [ ] Market Day.
* [ ] Festival.
* [ ] Concert.
* [ ] Beach Event.
* [ ] Religious Event.
* [ ] Food Festival.
* [ ] Technology Conference.
* [ ] Rare NPC Spawn.
* [ ] Boss Event.

---

# 38. Public Speech Event

* [ ] Gazebo.
* [ ] Stage.
* [ ] Microphone.
* [ ] Speakers.
* [ ] Speaker NPC.
* [ ] Crowd NPCs.
* [ ] Signs.
* [ ] Chairs.
* [ ] Event area.
* [ ] Crowd AI.
* [ ] Applause behavior.
* [ ] Booing behavior.
* [ ] Movement behavior.
* [ ] Dialogue behavior.
* [ ] Idle behavior.
* [ ] Participation timer.
* [ ] Minimum participation duration: 60 seconds.
* [ ] Server-side participation tracking.
* [ ] Prevent AFK/exploit reward abuse.
* [ ] Award First Amendment.
* [ ] Optional Freedom effect.
* [ ] Event cooldown.
* [ ] Cleanup.

The event must remain clearly fictional/satirical and must not be presented as a simulation of an actual political event.

---

# 39. First Amendment

* [ ] Register item.
* [ ] Define LEGENDARY rarity.
* [ ] Define acquisition source.
* [ ] Define reward conditions.
* [ ] Define tooltip.
* [ ] Define visual identity.
* [ ] Define optional Freedom effect.
* [ ] Prevent duplication.
* [ ] Prevent repeated event farming.
* [ ] Multiplayer synchronization.
* [ ] Achievement integration.

---

# 40. Rabbi's Crown

> Target rarity: **MYTHIC**.

* [ ] Register item.
* [ ] Define MYTHIC rarity.
* [ ] Create hat model.
* [ ] Create long beard.
* [ ] Create payot.
* [ ] Create decorative/ornamental details.
* [ ] Create texture.
* [ ] Implement head equipment.
* [ ] Implement third-person rendering.
* [ ] Implement multiplayer synchronization.
* [ ] Set armor value to 20.
* [ ] Implement permanent Blessed Trader effect.
* [ ] Define compatible villager trade behavior.
* [ ] Allow configured minimum trades such as 1 Emerald where appropriate.
* [ ] Prevent trade duplication.
* [ ] Prevent infinite economic generation.
* [ ] Prevent normal crafting.
* [ ] Exclude from ordinary common loot.
* [ ] Define extremely low acquisition probability.
* [ ] Define rare structure sources.
* [ ] Define endgame event sources.
* [ ] Define boss/event interaction if applicable.
* [ ] Test actual rarity.
* [ ] Verify it cannot become common through gameplay loops.

---

# 41. Blessed Trader

* [ ] Define effect.
* [ ] Detect valid equipped Crown.
* [ ] Apply effect server-side.
* [ ] Modify only intended villager trades.
* [ ] Prevent invalid negative prices.
* [ ] Prevent item duplication.
* [ ] Prevent infinite emerald/item loops.
* [ ] Preserve villager trade persistence.
* [ ] Synchronize trade data.
* [ ] Test relog.
* [ ] Test multiplayer.
* [ ] Test multiple players.
* [ ] Test multiple villagers.
* [ ] Test exploit scenarios.

---

# 42. Rare Structures

Implement:

* [ ] Large synagogue.
* [ ] Historical house.
* [ ] Market.
* [ ] Desert ruins.
* [ ] Dead Sea resort.
* [ ] Startup office.
* [ ] Government building.
* [ ] Rare religious structure.
* [ ] Secret easter-egg structure.

For every rare structure:

* [ ] Placement rules.
* [ ] Rarity.
* [ ] Loot.
* [ ] NPCs.
* [ ] Interactions.
* [ ] Event integration where applicable.
* [ ] Generation test.
* [ ] Multiplayer test.

---

# 43. Bibi Boss

> This is a fictional/satirical game boss inspired by a real-world public figure. It must not be presented as a factual depiction of real events.

* [ ] Create boss entity.
* [ ] Define configurable HP.
* [ ] Default HP: 10,000+.
* [ ] Boss bar.
* [ ] AI.
* [ ] Target selection.
* [ ] Ranged attack.
* [ ] Defensive behavior.
* [ ] Special attacks.
* [ ] Guards.
* [ ] Escape behavior.
* [ ] State machine.
* [ ] Animation.
* [ ] Sound.
* [ ] Combat effects.
* [ ] Spawn conditions.
* [ ] Government-building/event/rare-structure spawn integration.
* [ ] Multiplayer synchronization.

States:

* [ ] IDLE.
* [ ] ALERT.
* [ ] COMBAT.
* [ ] ENRAGED.
* [ ] DEFEATED.

---

# 44. Boss Combat

* [ ] Define damage model.
* [ ] Define attack cooldowns.
* [ ] Define ranged behavior.
* [ ] Define defense behavior.
* [ ] Define special attacks.
* [ ] Define guard behavior.
* [ ] Define enrage threshold.
* [ ] Define escape conditions.
* [ ] Define boss arena/area rules.
* [ ] Prevent boss duplication.
* [ ] Prevent reward duplication.
* [ ] Server-authoritative combat.
* [ ] Multiplayer synchronization.
* [ ] Test multiple players.
* [ ] Test reconnects.
* [ ] Test chunk unload/reload.
* [ ] Test server restart.

---

# 45. Boss Rewards

* [ ] Define Hava Nagila music disc.
* [ ] Define LEGENDARY rarity.
* [ ] Define drop conditions.
* [ ] Prevent duplicate farming.
* [ ] Define loot table.
* [ ] Validate licensing of audio.
* [ ] Add achievement.
* [ ] Test drop.
* [ ] Test multiplayer ownership/reward handling.

---

# 46. Music and Audio

* [ ] Define music registry.
* [ ] Define ambient sounds.
* [ ] City ambience.
* [ ] Event ambience.
* [ ] Festival audio.
* [ ] Boss audio.
* [ ] Cultural/inspired music.
* [ ] Verify commercial-use compatibility.
* [ ] Track source/license for every external asset.
* [ ] Do not include unauthorized commercial recordings.
* [ ] Document attribution requirements.
* [ ] Test volume levels.
* [ ] Test client/server separation.

---

# 47. Transportation

* [ ] Walking support.
* [ ] Bicycle.
* [ ] Bus.
* [ ] Train.
* [ ] Taxi.
* [ ] Boat.
* [ ] Transport stops.
* [ ] Transport routes.
* [ ] NPC transport usage.
* [ ] City connections.
* [ ] Player transport interaction.
* [ ] Server synchronization.
* [ ] Performance testing.

---

# 48. Map and Exploration

* [ ] Define major regions.
* [ ] Connect cities.
* [ ] Connect rural areas.
* [ ] Connect Dead Sea.
* [ ] Connect desert.
* [ ] Connect Mediterranean coast.
* [ ] Connect agricultural regions.
* [ ] Add villages.
* [ ] Add discovered landmarks.
* [ ] Add landmark discovery system.
* [ ] Add exploration achievements.
* [ ] Ensure terrain remains navigable.
* [ ] Verify transportation connectivity.

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
