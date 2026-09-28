# AGENTS.md — Israel-Simulator

## 1. Repository Purpose

Israel-Simulator is a Minecraft Java Edition mod focused on:

* Open-world exploration
* Israeli-inspired environments and cities
* Jewish cultural content
* Dynamic NPCs and city life
* Food and trading
* Festivals and world events
* Rare structures and discoveries
* Collectible and endgame items
* Multiplayer sandbox gameplay
* Optional comedy, satire, and easter eggs

The authoritative game and technical specification is:

```text
GAME_DESIGN.md
```

Read it before implementing or substantially changing gameplay systems.

The public project overview is:

```text
README.md
```

Do not duplicate the entire design document inside this file.

---

## 2. Core Agent Rule

Implement working game functionality, not demonstrations.

Never replace a required system with:

* Placeholder classes
* Fake implementations
* TODO-only methods
* Hardcoded visual simulations
* Non-functional UI
* Dummy NPCs
* Fake structures
* Temporary rewards
* Comments claiming that something is implemented when it is not

If a feature is not implemented, state that it is not implemented.

Never claim a build, test, launch, or gameplay result that was not actually verified.

---

## 3. Source of Truth

When implementing the project, use this priority:

1. Explicit user request for the current task
2. Applicable `AGENTS.md` instructions
3. `GAME_DESIGN.md`
4. Existing architecture and established project conventions
5. Tests and executable behavior
6. `README.md`
7. Comments and informal notes

If two authoritative sources conflict:

* Do not silently choose one.
* Identify the conflict.
* Prefer the more specific/current requirement.
* Make the smallest change necessary.
* Report unresolved ambiguity when it materially affects implementation.

---

## 4. Before Making Changes

For every non-trivial task:

1. Inspect the relevant repository files.
2. Identify the existing implementation.
3. Read the relevant section of `GAME_DESIGN.md`.
4. Determine which systems are affected.
5. Check existing registries, utilities, data files, networking, and configuration.
6. Reuse existing project infrastructure where appropriate.
7. Make a small implementation plan internally.
8. Implement the feature completely within the requested scope.
9. Compile and run the relevant validation.
10. Inspect the final diff for accidental or unrelated changes.

Do not begin implementing a large feature before understanding the existing architecture.

---

## 5. Scope Control

Only modify files necessary for the requested task.

Do not perform unrelated:

* Refactors
* Renames
* Dependency upgrades
* Formatting changes
* Architecture rewrites
* File moves
* Cleanup
* API migrations

Do not rewrite working systems merely because another architecture could be used.

If an existing architecture prevents the requested feature from being implemented correctly, explain the problem and make the smallest architectural change required.

---

## 6. Minecraft Modding Rules

The project must use the mod loader and Minecraft version defined by the repository configuration.

Never assume:

* Fabric
* Forge
* NeoForge
* A Minecraft version
* An API version

Inspect the actual project configuration before writing loader-specific code.

Use the APIs appropriate to the project's configured Minecraft/mod-loader version.

Do not invent Minecraft APIs, registry methods, event hooks, rendering APIs, or networking APIs.

If an API is uncertain, inspect the project dependencies and existing usages before implementing it.

---

## 7. Architecture

Keep the project modular.

Expected conceptual areas include:

```text
world/
biomes/
structures/
cities/
blocks/
items/
equipment/
entities/
npc/
bosses/
animation/
events/
festivals/
food/
trading/
economy/
transport/
quests/
achievements/
network/
client/
server/
config/
audio/
```

Do not create giant classes responsible for unrelated systems.

Prefer:

* Small focused classes
* Reusable services
* Clear registries
* Data-driven definitions
* Server/client separation
* Explicit ownership of state
* Existing project utilities

Do not introduce abstractions without a concrete reason.

---

## 8. Server / Client Authority

Gameplay state must be authoritative on the server.

The server is responsible for:

* Rewards
* Item grants
* Trades
* Boss state
* Event state
* Cooldowns
* Progression
* Reputation
* Persistent gameplay state

The client may handle:

* Rendering
* Animation
* Particles
* Sounds
* UI
* Client-side presentation

Never trust client-provided gameplay rewards or progression.

Any network packet that can affect gameplay must validate its input server-side.

---

## 9. Multiplayer Requirements

Every gameplay system must be considered for multiplayer.

When adding a feature, verify:

* Server/client synchronization
* Player-specific state
* Persistent state
* Concurrent players
* Duplicate reward prevention
* Cooldowns
* Entity ownership
* Event participation
* Chunk loading/unloading
* Player disconnect/reconnect behavior

Do not implement a single-player-only shortcut for a system specified as multiplayer-compatible.

---

## 10. Items & Rewards

Items must be real registered Minecraft items.

A new item normally requires all applicable:

* Registry entry
* Item definition
* Texture
* Model
* Language entry
* Recipe or loot source when applicable
* Gameplay implementation
* Multiplayer-safe behavior
* Testing

Do not create an item that exists only in Java code without the required client resources.

Rewards must be granted exactly once per valid completion.

Cooldowns must be enforced server-side.

---

## 11. Rare Items

Rare and endgame items must respect the rarity defined by the game design.

In particular:

```text
Rabbi's Crown → MYTHIC
First Amendment → LEGENDARY
Hava Nagila → LEGENDARY
```

Do not make rare items casually craftable, common loot, or easily obtainable unless the design document explicitly changes.

Do not weaken rarity simply to make testing easier in production logic.

For testing, use controlled test/debug mechanisms rather than changing production drop rates.

---

## 12. NPCs

NPCs must be implemented as actual functional entities when the design requires entities.

A complete NPC system may require:

* Entity registration
* Attributes
* AI/goals
* Navigation
* Targeting
* Trades
* Dialogue
* Schedule
* Persistence
* Spawn rules
* Client rendering
* Animation where required
* Multiplayer synchronization

Do not create NPCs that only stand still unless standing NPCs are explicitly intended.

Avoid expensive global AI or unnecessary pathfinding.

---

## 13. World Generation

World generation must be deterministic and compatible with Minecraft's generation model.

When implementing:

* Biomes
* Cities
* Structures
* Landmarks
* Terrain
* Vegetation
* Roads
* Buildings
* Special regions

consider:

* Chunk boundaries
* Generation order
* Performance
* Structure placement
* Seed reproducibility
* Multiplayer servers
* Existing vanilla generation
* Chunk loading

Do not generate large cities using expensive per-tick logic.

Prefer Minecraft's world-generation systems and data-driven mechanisms where appropriate.

---

## 14. Cities

Major cities must have distinct identities.

At minimum:

```text
Tel Aviv ≠ Jerusalem ≠ Jaffa
```

Do not generate all cities from the same generic building templates without meaningful variation.

City systems should support:

* Architecture
* Roads
* Buildings
* NPC activity
* Shops
* Events
* Landmarks
* Local economy
* Transportation

---

## 15. Events

World events must have explicit lifecycle handling.

An event should define, where applicable:

```text
spawn conditions
duration
participants
state
rewards
cooldown
cleanup
configuration
```

Do not leave spawned entities, timers, scheduled tasks, or temporary structures running indefinitely.

Events must survive or recover correctly from:

* Chunk unloads
* Server restarts
* Player disconnects
* Multiple simultaneous players

---

## 16. Cooldowns & Anti-Exploit

All important cooldowns and reward restrictions must be server-side.

Pay particular attention to:

* Western Wall rewards
* Rare item drops
* Boss rewards
* Event rewards
* Villager discounts
* Achievement rewards
* Repeated interactions

Prevent:

* Double rewards
* Item duplication
* Packet replay
* Death/reconnect abuse
* Chunk reload abuse
* Multiple simultaneous reward claims

Never rely exclusively on client-side state.

---

## 17. Data-Driven Design

Prefer data-driven systems when Minecraft already provides appropriate mechanisms.

Use where appropriate:

* JSON
* Tags
* Recipes
* Loot tables
* Structures
* Config files
* Registries
* Datapack-compatible data

Do not hardcode large content tables in Java when they can reasonably be represented as data.

However, do not force a data-driven architecture where it makes the implementation unnecessarily complex.

---

## 18. Configuration

Gameplay values that are explicitly configurable in the design should not be scattered as magic numbers.

Examples:

* Boss HP
* Boss damage
* Event frequency
* Cooldowns
* Rewards
* Item rarity
* Structure frequency
* NPC spawn rates
* Festival settings
* Particle settings
* Music settings

Centralize configuration according to the project's existing configuration system.

---

## 19. Performance

Performance is a first-class requirement.

Avoid:

* Heavy global tick handlers
* Per-tick scans of large areas
* Unnecessary pathfinding
* Excessive entity counts
* Excessive particles
* Repeated expensive world lookups
* Unbounded collections
* Memory leaks
* Expensive chunk-generation operations

Prefer:

* Event-driven logic
* Cached data where appropriate
* Bounded searches
* Scheduled work
* Chunk-aware processing
* Server-authoritative state
* Lazy computation

Do not optimize blindly. Measure or identify an actual expensive path before introducing complicated optimization.

---

## 20. Assets

Use only:

* Original assets
* Procedurally generated assets
* Assets with compatible licenses
* Assets explicitly permitted for the project's intended distribution

For every external asset, preserve licensing and attribution information.

Maintain:

```text
CREDITS.md
ASSET_LICENSES.md
```

Do not add copyrighted assets merely because they are easy to find online.

Do not remove existing attribution.

---

## 21. Audio

All audio must have compatible redistribution rights.

For music or recordings:

* Verify the license.
* Preserve attribution requirements.
* Do not redistribute commercial recordings without permission.
* Prefer original or appropriately licensed recordings.

This is especially important for the Hava Nagila music disc.

---

## 22. Satirical / Real-Person Content

Real people referenced by the mod must remain clearly identifiable as fictionalized or satirical game content.

Gameplay mechanics must not be presented as real-world events.

For example, the Bibi boss is a fictional fantasy boss inspired by a real public figure.

Do not implement fictional events in a way that falsely presents them as historical or real-world facts.

---

## 23. Cultural Content

Cultural and religious systems should be implemented as optional gameplay content.

Do not make cultural or religious mechanics mandatory for ordinary progression unless explicitly required by the game design.

Avoid reducing cultural systems to a single joke or stereotype.

Mechanic names should describe their gameplay function rather than rely on ethnic stereotypes.

---

## 24. Testing

After implementation, run the narrowest relevant tests first.

At minimum, when applicable:

```text
compile
unit tests
game/client launch
dedicated server launch
relevant gameplay test
```

For gameplay systems, verify actual behavior rather than only compilation.

Examples:

* Items appear correctly.
* Textures render.
* Recipes work.
* Rewards are granted.
* Cooldowns work.
* NPCs behave correctly.
* Structures generate.
* Events start and end.
* Multiplayer state synchronizes.
* No duplication exploit exists.

Never report a test as passed unless it was actually executed.

---

## 25. Build Validation

A feature is not complete merely because the source code compiles.

For substantial changes, validate:

```text
BUILD
↓
GAME LAUNCH
↓
FEATURE INITIALIZATION
↓
ACTUAL FEATURE BEHAVIOR
↓
MULTIPLAYER BEHAVIOR
↓
REGRESSION CHECK
```

If a validation step cannot be executed because the required environment is unavailable, report it explicitly.

---

## 26. Debugging

When debugging:

1. Reproduce the issue.
2. Identify the actual failure.
3. Inspect logs/stack traces.
4. Trace the relevant code path.
5. Determine the root cause.
6. Apply the smallest correct fix.
7. Re-run the failing scenario.
8. Check for regressions.

Do not mask crashes with broad exception handling.

Do not delete functionality merely because it causes an error.

Do not replace the real implementation with a mock unless the task explicitly concerns testing.

---

## 27. Dependencies

Do not add a dependency without a concrete reason.

Before adding one:

* Check whether Minecraft or the existing project already provides the required functionality.
* Check existing dependencies.
* Prefer the smallest appropriate dependency.
* Ensure compatibility with the project's Minecraft/mod-loader version.

Do not upgrade unrelated dependencies while implementing a feature.

---

## 28. Documentation

Keep documentation synchronized with implementation.

When a major system changes, update the appropriate documentation.

Use:

```text
README.md
GAME_DESIGN.md
AGENTS.md
```

for their respective purposes.

Do not put implementation details into `README.md` unless they are useful to repository users.

Do not turn `AGENTS.md` into a complete technical manual.

---

## 29. Git & Changes

Keep changes focused and reviewable.

Do not:

* Commit generated build output
* Commit secrets
* Commit local configuration
* Commit IDE-specific temporary files
* Modify unrelated files
* Rewrite history
* Force-push
* Create releases

unless explicitly requested.

Do not claim a commit, push, release, or deployment occurred unless it actually occurred.

---

## 30. Secrets & Security

Never hardcode:

* API keys
* Passwords
* Tokens
* Private credentials
* Signing keys
* Personal access tokens

Do not commit secrets.

If a required secret is missing, report the requirement rather than inventing a value.

---

## 31. Completion Standard

A task is complete only when:

* The requested behavior is implemented.
* The implementation follows the project architecture.
* Required resources exist.
* Relevant validation has been performed.
* No known critical error remains from the change.
* Documentation is updated when necessary.
* No unrelated changes were introduced.

Do not stop at "the code should work."

---

## 32. Final Response

After completing a task, report concisely:

1. What changed.
2. Which important files changed.
3. What was tested.
4. Test/build results.
5. Any known limitations or unresolved issues.

Do not claim successful validation without evidence.

If the task is incomplete, say exactly what remains.

---

## 33. Important Design Reference

For gameplay requirements, consult:

```text
GAME_DESIGN.md
```

For project overview:

```text
README.md
```

For repository-specific implementation rules:

```text
AGENTS.md
```

These files have different responsibilities and should not become duplicates of one another.
