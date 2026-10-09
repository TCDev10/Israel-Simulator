#!/usr/bin/env python3
"""Generate the grand_market jigsaw (Mahane Yehuda-style shuk), DataVersion 4903.

  * start      market/market_gate (rigid): gateway plaza with a juice bar; the covered
               market hall is guaranteed on its north side (market/landmarks)
  * lanes      terrain_matching stone lanes, open (string lights) or covered (glass roof)
  * buildings  rigid shop fronts (spices, fruit, bakery, fish, halva, cafe) and a
               two-floor market house; all on the shared buried footing

Run:  /workspace/nbtvenv/bin/python scripts/worldgen/gen_grand_market.py
"""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from jigsaw_kit import (  # noqa: E402
    AIR, B, BARREL, CHEST, CRAFT, HLANTERN, NS, PANE, SFENCE, SLAB, STAIR, Kit, Rigid, entrance_j,
    house, lamp, table,
)

LOOT_MARKET = f"{NS}:chests/grand_market"
LOOT_FOOD = f"{NS}:chests/grand_market_food"
LOOT_BAZAAR = f"{NS}:chests/jerusalem_bazaar"


def paving(x: int, z: int):
    h = (x * 5153 + z * 7919) % 10
    return B("stone_bricks") if h < 5 else (B("smooth_stone") if h < 7 else
                                            (B("cracked_stone_bricks") if h < 9 else B("andesite")))


kit = Kit("grand_market", "market", porch=paving)
POST = lambda: B("polished_blackstone_wall", north="none", south="none", east="none", west="none",
                 up="true", waterlogged="false")


# ---------------------------------------------------------------- start
def market_gate() -> None:
    s = Rigid(15, 9, 15)
    for x in range(15):
        for z in range(15):
            s.set(x, 0, z, paving(x, z))
    # iron gateway with the red sign beam (red_terracotta = framework marker)
    for x in (4, 10):
        for y in range(1, 6):
            s.set(x, y, 13, B("stone_bricks"))
    for x in range(4, 11):
        s.set(x, 6, 13, B("red_terracotta"))
    for x in range(5, 10):
        s.set(x, 5, 13, B("iron_bars", north="false", south="false", east="false", west="false",
                          waterlogged="false"))
    # juice bar kiosk in the middle
    for x, z in ((6, 6), (8, 6), (6, 8), (8, 8)):
        s.set(x, 1, z, B("lime_concrete")); s.set(x, 2, z, SFENCE())
    s.set(7, 1, 6, SLAB("spruce", "top")); s.set(7, 1, 8, SLAB("spruce", "top"))
    s.set(6, 1, 7, B("orange_concrete")); s.set(8, 1, 7, B("yellow_concrete"))
    s.set(7, 1, 7, BARREL("up", LOOT_FOOD))
    for x in range(5, 10):
        for z in range(5, 10):
            s.set(x, 3, z, B("orange_wool") if (x + z) % 2 else B("lime_wool"))
    s.set(7, 2, 7, HLANTERN())
    for x, z in ((2, 2), (12, 2), (2, 11), (12, 11)):
        lamp(s, x, z)
    for x, f in ((3, "west"), (11, "east")):
        s.set(x, 1, 7, STAIR(f, "spruce"))
    s.set(7, 1, 0, kit.bld_j("north_up", "landmarks"))
    s.set(7, 1, 14, kit.street_j("south_up"))
    s.set(0, 1, 7, kit.street_j("west_up"))
    s.set(14, 1, 7, kit.street_j("east_up"))
    s.save(kit.struct / "market_gate.nbt")


# ---------------------------------------------------------------- lanes
ENTR = (2, 6, 10)


def lane(covered: bool) -> None:
    s = kit.straight("lane", 13, 5, paving, sy=6, entrances=ENTR)
    for x in (0, 4, 8, 12):
        for z in (0, 4):
            for y in range(1, 4):
                s.set(x, y, z, POST())
    if covered:
        for x in range(13):
            for z in range(5):
                s.set(x, 4, z, B("white_stained_glass") if x % 4 else B("light_gray_stained_glass"))
        for x in (2, 6, 10):
            s.set(x, 3, 2, HLANTERN())
    else:
        for x in range(13):          # string of lights between the posts
            if x % 4:
                s.set(x, 3, 0, B("iron_bars", north="false", south="false", east="false", west="false",
                                 waterlogged="false"))
                s.set(x, 3, 4, B("iron_bars", north="false", south="false", east="false", west="false",
                                 waterlogged="false"))
        for x in (2, 10):
            s.set(x, 2, 0, HLANTERN()); s.set(x, 2, 4, HLANTERN())
        for x, col in ((5, "red"), (6, "white"), (7, "green")):
            for z in range(5):
                s.set(x, 4, z, B(f"{col}_wool"))   # banner of fabric across the lane
        for z in range(5):
            s.set(4, 4, z, B("white_wool")); s.set(8, 4, z, B("white_wool"))
    s.save(kit.struct / ("lane_covered.nbt" if covered else "lane_open.nbt"))


def lanes() -> None:
    lane(True); lane(False)
    kit.junction("lane_crossroad", 5, {"n", "s", "w", "e"}, paving).save(kit.struct / "lane_crossroad.nbt")
    kit.junction("lane_corner", 5, {"n", "e"}, paving).save(kit.struct / "lane_corner.nbt")
    kit.junction("lane_t", 5, {"w", "e", "s"}, paving).save(kit.struct / "lane_t.nbt")
    kit.terminator(5, paving, B("hay_block", axis="y")).save(kit.struct / "terminator.nbt")


# ---------------------------------------------------------------- shops
def shop(name: str, awning: str, goods, back, loot: str) -> None:
    """7x7 stone shop: open front with counter goods, awning, back shelves, loot."""
    W, D = 7, 7
    s = Rigid(W, 7, D)
    for x in range(W):
        s.set(x, 0, 0, paving(x, 0))
    s.set(3, 1, 0, B("air"))
    s.fills(0, 0, 1, 6, 0, 6, B("smooth_stone"))
    for y in range(1, 4):
        for z in range(1, 7):
            s.set(0, y, z, B("smooth_sandstone")); s.set(6, y, z, B("smooth_sandstone"))
        for x in range(7):
            s.set(x, y, 6, B("smooth_sandstone"))
    for x in (0, 6):
        s.set(x, 1, 1, B("cut_sandstone")); s.set(x, 2, 1, B("cut_sandstone")); s.set(x, 3, 1, B("cut_sandstone"))
    s.fills(0, 4, 1, 6, 4, 6, B("cut_sandstone"))
    for x in range(7):
        s.set(x, 5, 1, SLAB("sandstone")); s.set(x, 5, 6, SLAB("sandstone"))
        s.set(x, 3, 0, B(f"{awning}_wool" if x % 2 else "white_wool"))
    # counter row z=2 with goods on/in it (gap at x=3 to step inside)
    for i, x in enumerate((1, 2, 4, 5)):
        g = goods[i % len(goods)]
        s.set(x, 1, 2, g)
    s.set(3, 1, 2, AIR())
    for i, x in enumerate(range(1, 6)):
        s.set(x, 1, 5, back[i % len(back)])
    s.set(1, 1, 4, BARREL("up", loot)); s.set(5, 1, 4, CHEST("west", LOOT_MARKET))
    s.set(3, 3, 4, HLANTERN())
    s.set(3, 1, 0, entrance_j())
    s.save(kit.struct / f"{name}.nbt")


def shops() -> None:
    pot = lambda: B("decorated_pot", cracked="false", facing="north", waterlogged="false")
    shop("spice_shop", "red",
         [B("orange_concrete_powder"), B("red_concrete_powder"), B("yellow_concrete_powder"),
          B("brown_concrete_powder")],
         [pot(), B("potted_fern"), B("dried_kelp_block"), pot(),
          B("potted_azalea_bush")], LOOT_FOOD)
    shop("fruit_shop", "green",
         [B("melon"), B("pumpkin"), B("orange_concrete"), B("yellow_concrete")],
         [B("hay_block", axis="y"), B("melon"), B("composter", level="8"), B("pumpkin"), B("hay_block", axis="x")],
         LOOT_FOOD)
    shop("bakery_shop", "yellow",
         [B("hay_block", axis="x"), B("cake", bites="0"), B("hay_block", axis="z"), SLAB("spruce", "top")],
         [B("smoker", facing="north", lit="true"), B("bricks"), B("smoker", facing="north", lit="false"),
          CRAFT(), B("bricks")], LOOT_FOOD)
    shop("fish_shop", "blue",
         [B("packed_ice"), B("blue_ice"), B("packed_ice"), B("blue_ice")],
         [B("water_cauldron", level="3"), B("packed_ice"), B("dried_kelp_block"), B("water_cauldron", level="2"), B("packed_ice")], LOOT_FOOD)
    shop("halva_shop", "orange",
         [B("honey_block"), B("white_terracotta"), B("brown_terracotta"), B("honeycomb_block")],
         [B("beehive", facing="north", honey_level="5"), pot(), B("honey_block"), pot(),
          B("beehive", facing="north", honey_level="3")], LOOT_BAZAAR)


def cafe() -> None:
    """Corner cafe/bar: counter, stools, tables spilling onto the front terrace."""
    W, D = 9, 8
    s = Rigid(W, 7, D)
    for x in range(W):
        for z in range(0, 3):
            s.set(x, 0, z, paving(x, z))
    s.set(4, 1, 0, entrance_j())
    s.fills(0, 0, 3, 8, 0, 7, B("spruce_planks"))
    for y in range(1, 4):
        for x in range(9):
            s.set(x, y, 7, B("stone_bricks"))
        for z in range(3, 8):
            s.set(0, y, z, B("stone_bricks")); s.set(8, y, z, B("stone_bricks"))
        for x in (1, 2, 6, 7):
            s.set(x, y, 3, PANE() if y < 3 else B("stone_bricks"))
        s.set(4, y, 3, AIR() if y < 3 else B("stone_bricks"))
        for x in (3, 5):
            s.set(x, y, 3, B("stone_bricks"))
    s.fills(0, 4, 3, 8, 4, 7, B("stone_bricks"))
    for x in range(1, 8):
        s.set(x, 1, 6, SLAB("dark_oak", "top"))
        if x % 2:
            s.set(x, 1, 5, STAIR("north", "dark_oak"))
    s.set(1, 2, 6, B("brewing_stand", has_bottle_0="true", has_bottle_1="false", has_bottle_2="true"))
    s.set(7, 1, 4, BARREL("up", LOOT_FOOD)); s.set(1, 1, 4, B("jukebox", has_record="false"))
    s.set(4, 3, 5, HLANTERN())
    table(s, 2, 1, 1); table(s, 6, 1, 1)
    for x in range(9):
        s.set(x, 3, 1, B("green_wool" if x % 2 else "white_wool"))
    for x in (0, 8):
        s.set(x, 1, 1, SFENCE()); s.set(x, 2, 1, SFENCE())
    s.save(kit.struct / "cafe.nbt")


def market_hall() -> None:
    """The covered hall: arched stone walls, skylight roof, rows of stalls."""
    W, D = 17, 15
    s = Rigid(W, 10, D)
    for x in range(W):
        s.set(x, 0, 0, paving(x, 0))
    s.set(8, 1, 0, entrance_j())
    s.fills(1, 0, 1, 15, 0, 14, B("polished_andesite"))
    for y in range(1, 6):
        for x in range(1, 16):
            for z in (1, 14):
                s.set(x, y, z, B("stone_bricks"))
        for z in range(1, 15):
            s.set(1, y, z, B("stone_bricks")); s.set(15, y, z, B("stone_bricks"))
    # arched openings along the front and sides
    for x0 in (3, 7, 11):              # three 3-wide arches; the middle one is the entrance
        for x in range(x0, x0 + 3):
            for y in (1, 2, 3):
                s.set(x, y, 1, AIR())
        s.set(x0, 3, 1, B("stone_brick_stairs", facing="east", half="top", shape="straight",
                          waterlogged="false"))
        s.set(x0 + 2, 3, 1, B("stone_brick_stairs", facing="west", half="top", shape="straight",
                              waterlogged="false"))
    for z in (4, 8, 11):
        for y in (2, 3):
            s.set(1, y, z, PANE()); s.set(15, y, z, PANE())
    # roof: stone ring with a long glass skylight
    for x in range(1, 16):
        for z in range(1, 15):
            s.set(x, 6, z, B("glass") if 4 <= x <= 12 and 3 <= z <= 12 else B("stone_bricks"))
    for x in range(1, 16):
        s.set(x, 7, 1, SLAB("stone_brick")); s.set(x, 7, 14, SLAB("stone_brick"))
    # stall rows
    goods = [B("melon"), B("pumpkin"), B("orange_concrete_powder"), B("red_concrete_powder"),
             B("hay_block", axis="y"), B("honey_block"), B("packed_ice"), B("yellow_concrete")]
    i = 0
    for zr in (4, 8, 11):
        for x0 in (3, 10):
            for x in range(x0, x0 + 4):
                if x == x0 + 1:
                    s.set(x, 1, zr, BARREL("up", LOOT_FOOD if i % 2 else LOOT_MARKET))
                else:
                    s.set(x, 1, zr, goods[i % len(goods)])
                i += 1
            s.set(x0 + 1, 5, zr, HLANTERN())
    s.set(8, 1, 13, CHEST("north", LOOT_MARKET))
    s.save(kit.struct / "market_hall.nbt")


def main() -> None:
    market_gate(); lanes(); shops(); cafe(); market_hall()
    house(kit, "market_house", 9, 9, 2, "smooth_sandstone", door="spruce", shutter="spruce",
          trim="cut_sandstone", beds=("red", "white"), loot=LOOT_MARKET, vines=False)
    kit.write_structure_json("israel_simulator:jerusalem", size=5, max_distance=64)
    kit.write_pools(
        "market_gate",
        streets=[("lane_covered", 6, "terrain_matching"), ("lane_open", 6, "terrain_matching"),
                 ("lane_crossroad", 3, "terrain_matching"), ("lane_corner", 3, "terrain_matching"),
                 ("lane_t", 4, "terrain_matching")],
        terminators=[("terminator", 1, "terrain_matching")],
        buildings=[("spice_shop", 3, "rigid"), ("fruit_shop", 3, "rigid"), ("bakery_shop", 3, "rigid"),
                   ("fish_shop", 2, "rigid"), ("halva_shop", 2, "rigid"), ("cafe", 2, "rigid"),
                   ("market_house", 2, "rigid")],
        landmarks=[("market_hall", 1, "rigid")],
    )


if __name__ == "__main__":
    main()
