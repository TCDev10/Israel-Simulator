# Asset Licenses & Provenance Audit — Israel-Simulator

All assets in Israel-Simulator follow strict licensing, originality, and provenance guidelines as outlined in `AGENTS.md` (§20–21) and `GAME_DESIGN.md` (§58, §60).

---

## 1. Compliance Statement

- **No Unauthorized Copyrighted Assets**: Israel-Simulator does not bundle, distribute, or link to unauthorized copyrighted content or commercial recordings.
- **No AI-Generated Game Art**: All pixel art textures and 3D model geometry are human-authored, original creations developed specifically for this project.
- **Open Source Licensing**: All original artwork, models, scripts, and code are distributed under the **GNU Affero General Public License v3.0 (AGPL-3.0)**.
- **Mojang EULA Adherence**: Built-in sound mappings refer to Minecraft's vanilla client audio streams and assets in full accordance with the Minecraft End User License Agreement (EULA) and Modding Guidelines.

---

## 2. Complete Asset Inventory

### 2.1 Sound & Audio Assets

| Sound Identifier | In-Game Event | Provenance / Type | License | Redistribution & Usage |
| :--- | :--- | :--- | :--- | :--- |
| `music_disc.hava_nagila` | Hava Nagila Music Disc | Bundled `sounds/records/hava_nagila.ogg`. Replaces the earlier Wikimedia Commons `File:Hava_nagila.ogg` (MIDI render). Audio taken from the recording the user supplied: https://www.youtube.com/watch?v=vHSNZK4Je-Y . The user stated this is the original public-domain recording. Encoded here as Ogg Vorbis, 44100 Hz, stereo, 165.3 s. | User-stated public domain. Copyright status was not verified beyond that statement. Not the Commons `{{PD/1923|1938}}` file. | Bundled because the user supplied this source. Do not treat this row as a verified Commons PD grant. |
| `music.cultural.klezmer` | Cultural Klezmer music | Traditional melody stream mapping | Public Domain folk theme / Vanilla audio event | Permitted under Mojang EULA |
| `music.cultural.shabbat_shalom` | Shabbat melody | Traditional liturgy stream mapping | Public Domain melody / Vanilla audio event | Permitted under Mojang EULA |
| `ambient.city.tel_aviv` | Mediterranean coastal ambiance | Ambient wave stream mapping | Mojang EULA | Built-in vanilla fallback |
| `ambient.city.jerusalem` | Jerusalem Old City stone ambiance | Acoustic cave/stone stream mapping | Mojang EULA | Built-in vanilla fallback |
| `ambient.city.jaffa` | Jaffa port clock chime | Resonating bell stream mapping | Mojang EULA | Built-in vanilla fallback |
| `ambient.event.speech_crowd` | Civic Speech crowd murmurs | Attentive villager chatter mapping | Mojang EULA | Built-in vanilla fallback |
| `ambient.event.market_bustle` | Shuk market activity | Vendor trade stream mapping | Mojang EULA | Built-in vanilla fallback |
| `audio.festival.hanukkah_chime` | Menorah lighting chime | Resonant bell chime mapping | Mojang EULA | Built-in vanilla fallback |
| `audio.festival.shabbat_candle` | Shabbat candle lighting | Soft acoustic chime mapping | Mojang EULA | Built-in vanilla fallback |
| `entity.bibi_boss.ambient` | Bibi Boss idle voice | Parodic entity vocalization | Mojang EULA | Built-in vanilla fallback |
| `entity.bibi_boss.hurt` | Bibi Boss damage reaction | Entity impact audio | Mojang EULA | Built-in vanilla fallback |
| `entity.bibi_boss.death` | Bibi Boss defeat | Entity defeat audio | Mojang EULA | Built-in vanilla fallback |
| `entity.bibi_boss.speech` | Bibi Boss podium proclamation | Dramatic summoner invocation | Mojang EULA | Built-in vanilla fallback |
| `entity.bibi_boss.enrage` | Bibi Boss enraged state | Powerful boss phase audio | Mojang EULA | Built-in vanilla fallback |
| `music.boss.bibi_theme` | Bibi Boss encounter theme (looping, music category) | `sounds/music/bibi_boss_theme.ogg`. PLACEHOLDER: 2 s of silence, mono Ogg Vorbis. Intended song: "netanyahu song trap remix - full version" by twoby4, https://www.youtube.com/watch?v=yYm_3kvFQok . No Creative Commons or public-domain licence found (standard YouTube licence), so it is NOT bundled. | Placeholder: original silence, AGPL-3.0. Song: copyrighted, all rights reserved by its owner. | Drop a mono OGG Vorbis at the same path, only with permission from the rights holder. |
| `entity.bicycle.bell` | Bicycle handlebar bell | Bell chime audio | Mojang EULA | Built-in vanilla fallback |
| `entity.bus.horn` | Egged transit bus horn | Deep horn resonance | Mojang EULA | Built-in vanilla fallback |
| `entity.train.whistle` | Railway transit whistle | Steam/air whistle resonance | Mojang EULA | Built-in vanilla fallback |
| `entity.transport.travel` | Rapid transit travel | Teleport whoosh transition | Mojang EULA | Built-in vanilla fallback |
| `ui.landmark_discovered` | Landmark discovery fanfare | Achievement fanfare | Mojang EULA | Built-in vanilla fallback |

### 2.2 Texture Assets

| Texture Path | Asset Category | Resolution | Provenance & Author | License |
| :--- | :--- | :--- | :--- | :--- |
| `textures/entity/bibi_boss.png` | Entity Texture | 64x64 | Original pixel art by Israel-Simulator team | AGPL-3.0 |
| `textures/entity/bibi_guard.png` | Entity Texture | 64x64 | Original pixel art by Israel-Simulator team | AGPL-3.0 |
| `textures/entity/equipment/humanoid/kippah.png` | Equipment Layer | 64x32 | Original pixel art by Israel-Simulator team | AGPL-3.0 |
| `textures/entity/equipment/humanoid/talit.png` | Equipment Layer | 64x32 | Original pixel art by Israel-Simulator team | AGPL-3.0 |
| `textures/entity/equipment/humanoid/tefillin.png` | Equipment Layer | 64x32 | Original pixel art by Israel-Simulator team | AGPL-3.0 |
| `textures/item/rabbis_crown.png` | Item Texture | 64x64 | Procedurally generated by `tools/gen_rabbis_crown.py` (Israel-Simulator team) | AGPL-3.0 |
| `textures/entity/equipment/humanoid_baby/*` | Equipment Layer (Baby) | 64x32 | Original pixel art by Israel-Simulator team | AGPL-3.0 |
| `textures/block/menorah.png` | Block Texture | 16x16 | Original pixel art by Israel-Simulator team | AGPL-3.0 |
| `textures/item/*.png` (32 items) | Item Icons | 16x16 | Original pixel art by Israel-Simulator team | AGPL-3.0 |

### 2.3 3D Models and Geometry

| Model File | Type | Feature Set | License |
| :--- | :--- | :--- | :--- |
| `models/item/rabbis_crown.json` | Custom 3D Item Model | 48 cuboids: filigree gold band and rims, 7 jewelled points, velvet cap, Star of David ornament; also worn on the head | AGPL-3.0 |
| `models/item/kippah.json` | Custom 3D Item Model | Polygonal dome, woven trim | AGPL-3.0 |
| `models/item/talit.json` | Custom 3D Item Model | Folded prayer shawl body, corner fringes | AGPL-3.0 |
| `models/item/tefillin.json` | Custom 3D Item Model | Black leather bayit box, strap loop | AGPL-3.0 |
| `models/block/menorah.json` | Custom 3D Block Model | 9 branches, shamash center, candle progression | AGPL-3.0 |
| All other item & block models | JSON standard geometry | Declarative cube and generated 2.5D layers | AGPL-3.0 |

### 2.4 Typography and Fonts

- No third-party or proprietary fonts are distributed with this mod.
- All in-game text rendering relies strictly on Minecraft's standard font engine (`minecraft:default` and `minecraft:uniform`).

---

## 3. Permissions Summary

| Right | Granted | Conditions |
| :--- | :--- | :--- |
| **Commercial Use** | Yes | Permitted under AGPL-3.0 and subject to Mojang Commercial Usage Guidelines. |
| **Redistribution** | Yes | Permitted with source code availability under AGPL-3.0. |
| **Modification** | Yes | Permitted; modified versions must retain attribution and AGPL-3.0 license. |
| **Attribution** | Mandatory | Must credit Israel-Simulator developers and preserve `CREDITS.md`. |

