#!/usr/bin/env python3
"""Generate the island_temple pavilion (DataVersion 4903).

A square pavilion with white/blue concrete bands, blue corner pilasters, a zigzag
parapet and a stepped gold dome, raised on a quartz podium above a chevron
terracotta plaza with palms. Used twice:
  * worldgen: pasted on the SW tip of israel_simulator:tropical_island (gen_tropical_island.py)
  * arena:    JeffreyEpsteinEntity places the same template inside its glass dome

Template layout (Rigid coordinates, floor y=0 = plaza floor, FOOTING=4 below):
  plaza     z=0..6,   walk level y=1, Epstein/arena spawn point at (13,1,3)
  stairs    z=7..9,   x=10..16 (quartz, climbing towards +z)
  podium    z=10..26, x=5..21, top block y=3, interior floor y=4
  pavilion  z=11..23, x=7..19 (13x13), walls y=4..13, parapet y=14..15
  dome      centre (13, z=17), y=15..21, spire y=22..23

Run:  /workspace/nbtvenv/bin/python scripts/worldgen/gen_island_temple.py
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from jigsaw_kit import B, DATA, LADDER, LANTERN, Rigid, STAIR, palm  # noqa: E402
from structure_lib import chest  # noqa: E402

NAME = "island_temple"
LOOT = "israel_simulator:chests/island_temple"
SX, SY, SZ = 27, 24, 28
CX = 13                      # centre column
PX0, PX1, PZ0, PZ1 = 7, 19, 11, 23   # pavilion body (13x13)
WALL_Y0, WALL_Y1 = 4, 13     # 10 rows of bands
DOME_CZ = 17


def band(y: int):
    return B("white_concrete") if (y - WALL_Y0) % 2 == 0 else B("blue_concrete")


def chevron(x: int, z: int):
    k = (z + abs(x - CX)) % 6
    if k < 2:
        return B("red_terracotta")
    if k < 4:
        return B("white_terracotta")
    return B("pink_terracotta")


def build() -> Rigid:
    s = Rigid(SX, SY, SZ)

    # ---- ground: grass with dry grass, chevron plaza in front ----------------
    for x in range(SX):
        for z in range(SZ):
            plaza = 2 <= x <= 24 and z <= 6
            stairs = 9 <= x <= 17 and 7 <= z <= 9
            if plaza or stairs:
                s.set(x, 0, z, chevron(x, z))
            else:
                s.set(x, 0, z, B("grass_block", snowy="false"))
                h = (x * 31 + z * 17) % 7
                podium = 5 <= x <= 21 and 10 <= z <= 26
                if not podium:
                    if h == 0:
                        s.set(x, 1, z, B("short_dry_grass"))
                    elif h == 3:
                        s.set(x, 1, z, B("tall_dry_grass"))

    # ---- podium (quartz) ------------------------------------------------------
    for x in range(5, 22):
        for z in range(10, 27):
            for y in (1, 2):
                s.set(x, y, z, B("smooth_quartz"))
            s.set(x, 3, z, B("quartz_block"))
    # rim of quartz slabs around the podium edge
    for x in range(5, 22):
        for z in (10, 26):
            s.set(x, 3, z, B("smooth_quartz"))
    for z in range(10, 27):
        for x in (5, 21):
            s.set(x, 3, z, B("smooth_quartz"))

    # ---- white quartz stairs down to the plaza --------------------------------
    for x in range(10, 17):
        s.set(x, 1, 7, STAIR("south", "quartz"))
        s.set(x, 1, 8, B("smooth_quartz")); s.set(x, 2, 8, STAIR("south", "quartz"))
        s.set(x, 1, 9, B("smooth_quartz")); s.set(x, 2, 9, B("smooth_quartz"))
        s.set(x, 3, 9, STAIR("south", "quartz"))
    # stair cheeks carrying the gold statues
    for x in (9, 17):
        for z in (7, 8, 9):
            for y in range(1, 4):
                s.set(x, y, z, B("smooth_quartz"))
        s.set(x, 4, 8, B("chiseled_quartz_block"))
        # gold figure: legs, torso, head
        s.set(x, 5, 8, B("gold_block"))
        s.set(x, 6, 8, B("gold_block"))
        s.set(x, 7, 8, B("raw_gold_block"))

    # ---- pavilion body ----------------------------------------------------------
    for y in range(WALL_Y0, WALL_Y1 + 1):
        for x in range(PX0, PX1 + 1):
            for z in (PZ0, PZ1):
                s.set(x, y, z, band(y))
        for z in range(PZ0, PZ1 + 1):
            for x in (PX0, PX1):
                s.set(x, y, z, band(y))
        for x, z in ((PX0, PZ0), (PX1, PZ0), (PX0, PZ1), (PX1, PZ1)):
            s.set(x, y, z, B("blue_concrete"))           # corner pilasters
    # interior floor + ceiling
    for x in range(PX0 + 1, PX1):
        for z in range(PZ0 + 1, PZ1):
            s.set(x, 3, z, B("white_glazed_terracotta", facing="north") if (x + z) % 2 == 0
                  else B("blue_glazed_terracotta", facing="north"))
            s.set(x, WALL_Y1 + 1, z, B("white_concrete"))
    # small dark windows on the side walls (like the photo)
    for y in (9, 11):
        for z in (14, 20):
            s.set(PX0, y, z, B("black_stained_glass"))
            s.set(PX1, y, z, B("black_stained_glass"))
        for x in (9, 17):
            s.set(x, y, PZ1, B("black_stained_glass"))

    # zigzag parapet (two rows, blue/white diagonals)
    ring = ([(x, PZ0) for x in range(PX0, PX1 + 1)] + [(PX1, z) for z in range(PZ0 + 1, PZ1 + 1)]
            + [(x, PZ1) for x in range(PX1 - 1, PX0 - 1, -1)] + [(PX0, z) for z in range(PZ1 - 1, PZ0, -1)])
    for i, (x, z) in enumerate(ring):
        s.set(x, 14, z, B("blue_concrete") if i % 4 in (0, 1) else B("white_concrete"))
        s.set(x, 15, z, B("blue_concrete") if i % 4 in (1, 2) else B("white_concrete"))
    for x, z in ((PX0, PZ0), (PX1, PZ0), (PX0, PZ1), (PX1, PZ1)):
        s.set(x, 15, z, B("blue_concrete"))

    # ---- front: 3x5 dark oak arched doorway with sandstone frame -------------
    z = PZ0
    for x in range(CX - 1, CX + 2):
        for y in range(4, 9):
            s.set(x, y, z, B("dark_oak_planks"))
    s.set(CX, 4, z, B("dark_oak_door", facing="north", half="lower", hinge="left", open="false", powered="false"))
    s.set(CX, 5, z, B("dark_oak_door", facing="north", half="upper", hinge="left", open="false", powered="false"))
    for y in range(4, 9):
        s.set(CX - 2, y, z, B("cut_sandstone")); s.set(CX + 2, y, z, B("cut_sandstone"))
    s.set(CX - 1, 8, z, STAIR("east", "sandstone", "top"))     # arch springers
    s.set(CX + 1, 8, z, STAIR("west", "sandstone", "top"))
    for x in range(CX - 2, CX + 3):
        s.set(x, 9, z, B("cut_sandstone"))
    s.set(CX, 9, z, B("chiseled_sandstone"))
    # door threshold on the podium
    for x in range(CX - 1, CX + 2):
        s.set(x, 3, z - 1, B("smooth_sandstone"))

    # ---- matching arched tan panel on the west side --------------------------
    x = PX0
    for zz in range(DOME_CZ - 1, DOME_CZ + 2):
        for y in range(4, 9):
            s.set(x, y, zz, B("smooth_sandstone"))
    for y in range(4, 9):
        s.set(x, y, DOME_CZ - 2, B("cut_sandstone")); s.set(x, y, DOME_CZ + 2, B("cut_sandstone"))
    s.set(x, 8, DOME_CZ - 1, STAIR("south", "sandstone", "top"))
    s.set(x, 8, DOME_CZ + 1, STAIR("north", "sandstone", "top"))
    for zz in range(DOME_CZ - 2, DOME_CZ + 3):
        s.set(x, 9, zz, B("cut_sandstone"))
    s.set(x, 9, DOME_CZ, B("chiseled_sandstone"))
    s.set(x, 6, DOME_CZ, B("chiseled_sandstone"))

    # ---- one simple room inside ------------------------------------------------
    for zz in range(PZ0 + 2, PZ1 - 1):
        s.set(CX, 4, zz, B("blue_carpet"))
    s.set(CX, 4, PZ1 - 1, chest("north", LOOT))
    s.set(CX - 1, 4, PZ1 - 1, B("gold_block"))
    s.set(CX + 1, 4, PZ1 - 1, B("gold_block"))
    s.set(CX - 1, 5, PZ1 - 1, B("lantern", hanging="false", waterlogged="false"))
    s.set(CX + 1, 5, PZ1 - 1, B("lantern", hanging="false", waterlogged="false"))
    for xx, zz in ((PX0 + 2, PZ0 + 2), (PX1 - 2, PZ0 + 2), (PX0 + 2, PZ1 - 2), (PX1 - 2, PZ1 - 2)):
        s.set(xx, 4, zz, B("potted_dead_bush"))
    for xx in (CX - 3, CX + 3):
        for zz in (PZ0 + 4, PZ1 - 4):
            s.set(xx, WALL_Y1, zz, B("lantern", hanging="true", waterlogged="false"))
    s.set(CX, WALL_Y1 + 1, DOME_CZ, B("sea_lantern"))

    # ---- stepped gold dome (9 wide) with a small spire ------------------------
    radii = [(15, 4.6), (16, 4.6), (17, 4.1), (18, 3.6), (19, 2.9), (20, 2.0), (21, 1.0)]
    for y, r in radii:
        for x in range(CX - 5, CX + 6):
            for zz in range(DOME_CZ - 5, DOME_CZ + 6):
                if (x - CX) ** 2 + (zz - DOME_CZ) ** 2 <= r * r:
                    s.set(x, y, zz, B("gold_block"))
    s.set(CX, 22, DOME_CZ, B("lightning_rod", facing="up", powered="false", waterlogged="false"))
    s.set(CX, 23, DOME_CZ, B("end_rod", facing="up"))

    # ---- ladder on the east side, up to a gap in the parapet ------------------
    lz = PZ1 - 2
    for y in range(4, 16):
        s.set(PX1 + 1, y, lz, LADDER("east"))
    s.set(PX1, 15, lz, B("air"))              # gap in the parapet onto the roof

    # ---- palms and dry grass ---------------------------------------------------
    for px, pz, trunk in ((2, 9, 10), (24, 11, 12), (2, 21, 9), (24, 24, 11)):
        s.set(px, 1, pz, B("air"))
        palm(s, px, pz, y=1, trunk=trunk)

    # marker for StructureFrameworkTest: blue_concrete (bands)
    return s


def write_json() -> None:
    struct = {
        "type": "minecraft:jigsaw",
        "biomes": ["israel_simulator:mediterranean_coast"],
        "max_distance_from_center": 80,
        "project_start_to_heightmap": "WORLD_SURFACE_WG",
        "size": 1,
        "spawn_overrides": {},
        "start_height": {"absolute": -Rigid.FOOTING},
        "start_pool": f"israel_simulator:{NAME}",
        "step": "surface_structures",
        "terrain_adaptation": "beard_thin",
        "use_expansion_hack": False,
    }
    pool = {"fallback": "minecraft:empty", "elements": [{"weight": 1, "element": {
        "element_type": "minecraft:single_pool_element", "location": f"israel_simulator:{NAME}",
        "processors": "minecraft:empty", "projection": "rigid"}}]}
    sset = {"structures": [{"structure": f"israel_simulator:{NAME}", "weight": 1}],
            "placement": {"type": "minecraft:random_spread", "spacing": 32, "separation": 12,
                          "salt": 734120985}}
    for rel, obj in ((f"worldgen/structure/{NAME}.json", struct),
                     (f"worldgen/template_pool/{NAME}.json", pool),
                     ("worldgen/structure_set/island_temples.json", sset)):
        p = DATA / rel
        p.write_text(json.dumps(obj, indent=2) + "\n")
        print("wrote", p.relative_to(DATA))


if __name__ == "__main__":
    # worldgen JSON now lives in gen_tropical_island.py; write_json() is kept for reference only
    build().save(DATA / f"structure/{NAME}.nbt")
