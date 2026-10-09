#!/usr/bin/env python3
"""Generate the dead_sea_resort jigsaw (Ein Bokek-style resort strip), DataVersion 4903.

  * start      dsr/resort_plaza (rigid): plaza with a packed-mud spa fountain (framework
               marker) and palms; the hotel tower is guaranteed on its north side
  * promenade  terrain_matching sandstone promenade; over the lake it becomes a birch
               boardwalk (rule processor, like vanilla street_plains)
  * buildings  rigid on the shared buried footing: mud spa, pool club, beach bar,
               salt & cosmetics shop, beach with salt formations, guesthouse

Run:  /workspace/nbtvenv/bin/python scripts/worldgen/gen_dead_sea_resort.py
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from jigsaw_kit import (  # noqa: E402
    AIR, B, BARREL, blk, CHEST, DATA, NS, PANE, SFENCE, SLAB, STAIR, HLANTERN, Kit, Rigid, entrance_j,
    house, lamp, palm, table,
)

LOOT = f"{NS}:chests/dead_sea_resort"
LOOT_BAR = f"{NS}:chests/grand_market_food"
PROCESSOR = f"{NS}:dead_sea_promenade"
SALT = lambda: blk(f"{NS}:salt_block")
WATER = lambda: B("water", level="0")


def promenade(x: int, z: int):
    v = (x * 31 + z * 17) % 7
    return B("smooth_sandstone") if v < 3 else B("cut_sandstone") if v < 5 else B("white_terracotta") \
        if v < 6 else B("sandstone")


kit = Kit("dead_sea_resort", "dsr", porch=promenade)


def umbrella(s, x, z, color, y=1) -> None:
    s.set(x, y, z, SFENCE("birch")); s.set(x, y + 1, z, SFENCE("birch"))
    for dx in (-1, 0, 1):
        for dz in (-1, 0, 1):
            if s.free(x + dx, y + 2, z + dz):
                s.set(x + dx, y + 2, z + dz, B(f"{color}_carpet") if (dx, dz) != (0, 0) else B(f"{color}_wool"))


def lounger(s, x, z, y=1) -> None:
    s.set(x, y, z, STAIR("south", "birch")); s.set(x, y, z + 1, SLAB("birch"))


def pool(s: Rigid, x0, z0, x1, z1, deck_mat="smooth_quartz") -> None:
    """Two-deep pool: columns skip the footing and get their own bottom."""
    for x in range(x0 - 1, x1 + 2):
        for z in range(z0 - 1, z1 + 2):
            inside = x0 <= x <= x1 and z0 <= z <= z1
            if inside:
                s.no_footing.add((x, z))
                s.set(x, 0, z, WATER()); s.set(x, -1, z, WATER())
                s.set(x, -2, z, B("light_blue_terracotta") if (x + z) % 2 else B("white_terracotta"))
                s.set(x, -3, z, B("smooth_sandstone")); s.set(x, -4, z, B("smooth_sandstone"))
            else:
                s.set(x, 0, z, B(deck_mat))


# ---------------------------------------------------------------- start
def resort_plaza() -> None:
    s = Rigid(17, 8, 15)
    for x in range(17):
        for z in range(15):
            s.set(x, 0, z, promenade(x, z))
    # mud spa fountain: packed-mud ring (framework marker), mud pool, water spouts
    for x in range(5, 12):
        for z in range(4, 11):
            ring = x in (5, 11) or z in (4, 10)
            if ring:
                s.set(x, 1, z, B("packed_mud"))
            else:
                s.set(x, 0, z, B("mud"))
    for x, z in ((8, 7),):
        s.set(x, 1, z, B("mud_bricks")); s.set(x, 2, z, B("mud_brick_wall", north="none", south="none",
                                                            east="none", west="none", up="true",
                                                            waterlogged="false"))
        s.set(x, 3, z, B("water_cauldron", level="3"))
    for x, z in ((6, 5), (10, 9)):
        s.set(x, 1, z, B("water_cauldron", level="3"))
    for x, z in ((2, 2), (14, 2), (2, 12), (14, 12)):
        palm(s, x, z, trunk=6)
    for x in (3, 13):
        for z in (6, 8):
            s.set(x, 1, z, STAIR("east" if x == 3 else "west", "birch"))
    lamp(s, 8, 12); lamp(s, 8, 2)
    s.set(8, 1, 0, kit.bld_j("north_up", "landmarks"))
    s.set(8, 1, 14, kit.street_j("south_up"))
    s.set(0, 1, 7, kit.street_j("west_up"))
    s.set(16, 1, 7, kit.street_j("east_up"))
    s.save(kit.struct / "resort_plaza.nbt")


# ---------------------------------------------------------------- promenade
def promenades() -> None:
    s = kit.straight("promenade", 13, 5, promenade, lamp_at=(6, 0), entrances=(3, 9))
    for x in (1, 11):
        s.set(x, 1, 4, STAIR("north", "birch"))
    s.save(kit.struct / "promenade_straight.nbt")
    kit.junction("promenade_crossroad", 5, {"n", "s", "w", "e"}, promenade).save(kit.struct / "promenade_crossroad.nbt")
    kit.junction("promenade_corner", 5, {"n", "e"}, promenade).save(kit.struct / "promenade_corner.nbt")
    kit.junction("promenade_t", 5, {"w", "e", "s"}, promenade).save(kit.struct / "promenade_t.nbt")
    kit.terminator(5, promenade, SALT()).save(kit.struct / "terminator.nbt")


def write_processor() -> None:
    rules = [{"input_predicate": {"predicate_type": "minecraft:block_match", "block": f"minecraft:{b}"},
              "location_predicate": {"predicate_type": "minecraft:block_match", "block": "minecraft:water"},
              "output_state": {"Name": "minecraft:birch_planks"}}
             for b in ("smooth_sandstone", "cut_sandstone", "white_terracotta", "sandstone")]
    p = DATA / "worldgen/processor_list/dead_sea_promenade.json"
    p.write_text(json.dumps({"processors": [{"processor_type": "minecraft:rule", "rules": rules}]}, indent=2) + "\n")
    for name in ("streets", "terminators"):
        pp = kit.pool / f"{name}.json"
        d = json.loads(pp.read_text())
        for el in d["elements"]:
            el["element"]["processors"] = PROCESSOR
        pp.write_text(json.dumps(d, indent=2) + "\n")


# ---------------------------------------------------------------- buildings
def front(s, W) -> None:
    for x in range(W):
        s.set(x, 0, 0, promenade(x, 0))
    s.set(W // 2, 1, 0, entrance_j())


def box(s, x0, z0, x1, z1, h, mat, glass_every=3) -> None:
    for y in range(1, h + 1):
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                if x in (x0, x1) or z in (z0, z1):
                    edge = x in (x0, x1) and z in (z0, z1)
                    along = x if z in (z0, z1) else z
                    s.set(x, y, z, PANE() if y == 2 and not edge and along % glass_every == 1 else B(mat))
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            s.set(x, h + 1, z, B("smooth_quartz") if x in (x0, x1) or z in (z0, z1) else B(mat))


def spa() -> None:
    s = Rigid(11, 5, 10)
    front(s, 11)
    for x in range(11):
        for z in range(1, 10):
            s.set(x, 0, z, B("smooth_sandstone"))
    box(s, 0, 1, 10, 9, 3, "white_concrete")
    for y in (1, 2):
        s.set(5, y, 1, AIR())
    for x in range(2, 5):                               # mud bath
        for z in range(4, 8):
            s.set(x, 0, z, B("mud"))
    for x in (1, 5):
        for z in range(3, 9):
            s.set(x, 1, z, B("packed_mud"))
    for x in range(2, 5):
        s.set(x, 1, 3, B("packed_mud")); s.set(x, 1, 8, B("packed_mud"))
    for z in (3, 5, 7):                                 # rinse cauldrons + benches
        s.set(9, 1, z, B("water_cauldron", level="3")); s.set(8, 1, z + 1, STAIR("west", "birch"))
    s.set(7, 1, 8, BARREL("up", LOOT)); s.set(9, 1, 8, CHEST("west", LOOT))
    for x in range(3, 8):                               # skylight
        s.set(x, 4, 5, B("glass"))
    s.set(5, 3, 5, HLANTERN())
    s.save(kit.struct / "mud_spa.nbt")


def pool_club() -> None:
    s = Rigid(13, 4, 12)
    front(s, 13)
    for x in range(13):
        for z in range(1, 12):
            s.set(x, 0, z, B("smooth_quartz"))
    pool(s, 3, 4, 9, 8)
    for x in (2, 4, 8, 10):
        lounger(s, x, 10)
    umbrella(s, 6, 10, "orange")
    for x in range(13):                                 # low glass fence
        for z in (1, 11):
            if x != 6:
                s.set(x, 1, z, PANE())
    for z in range(2, 11):
        s.set(0, 1, z, PANE()); s.set(12, 1, z, PANE())
    s.set(11, 1, 2, BARREL("up", LOOT)); s.set(1, 1, 2, B("water_cauldron", level="3"))
    s.save(kit.struct / "pool_club.nbt")


def beach_bar() -> None:
    s = Rigid(11, 5, 9)
    front(s, 11)
    for x in range(11):
        for z in range(1, 9):
            s.set(x, 0, z, B("sand") if z < 4 else B("birch_planks"))
    for x, z in ((1, 4), (9, 4), (1, 8), (9, 8)):
        for y in range(1, 4):
            s.set(x, y, z, B("stripped_jungle_log", axis="y"))
    for x in range(1, 10):
        for z in range(4, 9):
            s.set(x, 4, z, B("hay_block", axis="x") if (x + z) % 2 else B("bamboo_mosaic"))
    for x in range(3, 8):
        s.set(x, 1, 5, B("bamboo_mosaic_slab", type="top", waterlogged="false"))
        s.set(x, 1, 4, B("bamboo_mosaic_stairs", facing="south", half="bottom", shape="straight",
                         waterlogged="false") if x % 2 else AIR())
    s.set(4, 1, 7, BARREL("up", LOOT_BAR)); s.set(6, 1, 7, BARREL("up", LOOT_BAR))
    s.set(5, 1, 7, B("brewing_stand", has_bottle_0="true", has_bottle_1="true", has_bottle_2="false"))
    s.set(5, 3, 6, HLANTERN())
    table(s, 2, 1, 2); table(s, 8, 1, 2)
    s.save(kit.struct / "beach_bar.nbt")


def salt_shop() -> None:
    s = Rigid(9, 5, 8)
    front(s, 9)
    for x in range(9):
        for z in range(1, 8):
            s.set(x, 0, z, B("smooth_sandstone"))
    box(s, 0, 1, 8, 7, 3, "white_concrete", glass_every=2)
    for y in (1, 2):
        s.set(4, y, 1, AIR())
    for x in (2, 6):
        s.set(x, 1, 3, SALT()); s.set(x, 2, 3, B("brewing_stand", has_bottle_0="true", has_bottle_1="false",
                                                   has_bottle_2="true"))
    for x in range(1, 8):
        s.set(x, 1, 6, SALT() if x % 2 else B("packed_mud"))
        if x % 2 == 0:
            s.set(x, 2, 6, B("decorated_pot", cracked="false", facing="north", waterlogged="false"))
    s.set(1, 1, 4, CHEST("east", LOOT)); s.set(7, 1, 4, BARREL("up", LOOT))
    for x in range(9):
        s.set(x, 3, 0, B("light_blue_wool") if x % 2 else B("white_wool"))
    s.set(4, 3, 4, HLANTERN())
    s.save(kit.struct / "salt_shop.nbt")


def beach() -> None:
    """Sandy beach with umbrellas, loungers, a shower, lifeguard chair and salt formations."""
    s = Rigid(13, 4, 11)
    front(s, 13)
    for x in range(13):
        for z in range(1, 11):
            s.set(x, 0, z, B("sand") if z < 8 else (SALT() if (x * 7 + z) % 3 == 0 else B("calcite")))
    for x, c in ((2, "light_blue"), (6, "white"), (10, "yellow")):
        umbrella(s, x, 3, c); lounger(s, x - 1, 4); lounger(s, x + 1, 4)
    s.set(12, 1, 1, SFENCE("birch")); s.set(12, 2, 1, SFENCE("birch")); s.set(12, 3, 1, B("water_cauldron", level="3"))
    for y in range(1, 3):                               # lifeguard chair
        s.set(5, y, 7, SFENCE("birch")); s.set(7, y, 7, SFENCE("birch"))
    s.set(6, 3, 7, STAIR("north", "birch")); s.set(6, 2, 7, B("red_wool"))
    for x, ht in ((1, 2), (2, 1), (4, 3), (9, 2), (10, 1), (11, 3)):   # salt formations
        for y in range(1, ht + 1):
            s.set(x, y, 9, SALT())
        if ht > 1:
            s.set(x, 1, 10, SALT())
    s.set(0, 1, 5, BARREL("up", LOOT))
    s.save(kit.struct / "beach.nbt")


def main() -> None:
    resort_plaza(); promenades(); spa(); pool_club(); beach_bar(); salt_shop(); beach()
    house(kit, "guesthouse", 9, 9, 2, "white_concrete", door="birch", shutter="birch",
          trim="smooth_quartz", beds=("light_blue", "white"), loot=LOOT, vines=False,
          floor_block="smooth_sandstone")

    def hotel_extra(s) -> None:
        for x in range(2, 13):                         # rooftop sign band
            s.set(x, 18, 1, B("light_blue_concrete") if x % 2 else B("white_concrete"))
        for x in (1, 13):
            for z in range(3, 10, 3):
                for y in (6, 10, 14):
                    if s.free(x - 1 if x == 1 else x + 1, y, z) and 0 <= (x - 1 if x == 1 else x + 1) < 15:
                        s.set(x - 1 if x == 1 else x + 1, y, z, B("smooth_quartz_slab", type="top",
                                                                    waterlogged="false"))
    house(kit, "hotel", 15, 12, 4, "white_concrete", door="birch", shutter=None, trim="smooth_quartz",
          beds=("white", "light_blue"), loot=LOOT, pergola=True, vines=False, extra=hotel_extra,
          floor_block="smooth_sandstone")
    kit.write_structure_json("israel_simulator:dead_sea", size=5, max_distance=64)
    kit.write_pools(
        "resort_plaza",
        streets=[("promenade_straight", 8, "terrain_matching"), ("promenade_crossroad", 2, "terrain_matching"),
                 ("promenade_corner", 3, "terrain_matching"), ("promenade_t", 3, "terrain_matching")],
        terminators=[("terminator", 1, "terrain_matching")],
        buildings=[("mud_spa", 3, "rigid"), ("pool_club", 2, "rigid"), ("beach_bar", 3, "rigid"),
                   ("salt_shop", 3, "rigid"), ("beach", 3, "rigid"), ("guesthouse", 3, "rigid")],
        landmarks=[("hotel", 1, "rigid")],
    )
    write_processor()


if __name__ == "__main__":
    main()
