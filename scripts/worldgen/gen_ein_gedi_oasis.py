#!/usr/bin/env python3
"""Generate the ein_gedi_oasis jigsaw (Ein Gedi nature reserve), DataVersion 4903.

  * start      oasis/spring_falls (rigid): pale limestone cliff with David's waterfall dropping
               into a mossy pool (framework marker minecraft:moss_block), reeds, ferns, palms
  * trails     terrain_matching sand/gravel hiking trails edged with stones
  * pieces     rigid on a buried sandstone footing, entrance on the north porch row:
               ancient synagogue (mosaic floor under a shade canopy, ark niche, loot),
               ranger station, hidden cave (loot), palm spring, balsam terraces,
               stream crossing with stepping stones, cliff lookout

Run:  /workspace/nbtvenv/bin/python scripts/worldgen/gen_ein_gedi_oasis.py
"""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from jigsaw_kit import (  # noqa: E402
    AIR, B, BARREL, CHEST, HLANTERN, LANTERN, NS, PANE, SFENCE, SLAB, STAIR, Kit, Rigid,
    entrance_j, lamp, table,
)
from gen_jerusalem_city import blk  # noqa: E402

LOOT = f"{NS}:chests/ein_gedi_oasis"


class Piece(Rigid):
    FOOTING_BLOCK = "sandstone"


def h(x: int, z: int, salt: int = 0) -> int:
    return (x * 73856093 ^ z * 19349663 ^ salt * 83492791) & 0xFFFF


def trail(x: int, z: int):
    v = h(x, z, 3) % 10
    return (B("sand") if v < 4 else B("coarse_dirt") if v < 6 else B("gravel") if v < 8
            else B("smooth_sandstone") if v < 9 else B("packed_mud"))


def rock(x: int, y: int, z: int):
    """Pale Ein Gedi limestone/dolomite."""
    v = h(x * 3 + y, z, 7) % 14
    return (B("smooth_sandstone") if v < 4 else B("calcite") if v < 7 else B("sandstone") if v < 9
            else B("white_terracotta") if v < 11 else B("cut_sandstone") if v < 12
            else B("diorite") if v < 13 else B("andesite"))


kit = Kit("ein_gedi_oasis", "oasis", porch=trail)

WATER = lambda: B("water", level="0")
FALL = lambda: B("water", level="8")


def mod(name: str, **p: str):
    return blk(f"{NS}:{name}", **p)


def date_leaf(x, z, y):
    return mod("date_palm_leaves", age=str(3 if h(x, z, y) % 3 else 2), distance="1",
               persistent="true", waterlogged="false")


def date_palm(s, x, z, trunk, y=1) -> None:
    for dy in range(trunk):
        s.set(x, y + dy, z, B("jungle_log", axis="y"))
    top = y + trunk
    s.set(x, top, z, date_leaf(x, z, top))
    for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        for k in (1, 2):
            if 0 <= x + k * dx < s.sx and 0 <= z + k * dz < s.sz:
                s.set(x + k * dx, top, z + k * dz, date_leaf(x + k * dx, z + k * dz, top))
        xx, zz = x + 3 * dx, z + 3 * dz
        if 0 <= xx < s.sx and 0 <= zz < s.sz and s.free(xx, top - 1, zz):
            s.set(xx, top - 1, zz, date_leaf(xx, zz, top))
    for dx, dz in ((1, 1), (-1, 1), (1, -1), (-1, -1)):
        if 0 <= x + dx < s.sx and 0 <= z + dz < s.sz:
            s.set(x + dx, top, z + dz, date_leaf(x + dx, z + dz, top))


def reeds(s, x, z, n=3, y=1) -> None:
    for dy in range(n):
        s.set(x, y + dy, z, B("sugar_cane", age="0"))


def front(s, W) -> None:
    for x in range(W):
        s.set(x, 0, 0, trail(x, 0))
    s.set(W // 2, 1, 0, entrance_j())


def greenery(x, z):
    v = h(x, z, 11) % 7
    return B("moss_block") if v < 2 else B("grass_block", snowy="false") if v < 5 else B("rooted_dirt")


def plants_on(s, cells, salt) -> None:
    for i, (x, z) in enumerate(cells):
        v = h(x, z, salt) % 9
        if not s.free(x, 1, z):
            continue
        if v == 0:
            s.set(x, 1, z, B("fern"))
        elif v == 1:
            for dy, half in ((1, "lower"), (2, "upper")):
                s.set(x, dy, z, B("large_fern", half=half))
        elif v == 2:
            s.set(x, 1, z, B("short_grass"))
        elif v == 3:
            s.set(x, 1, z, B("azalea"))
        elif v == 4 and s.get(x, 0, z) in ("minecraft:grass_block", "minecraft:rooted_dirt", "minecraft:moss_block"):
            s.set(x, 1, z, mod("mediterranean_herbs", age="3"))


# ---------------------------------------------------------------- start
def spring_falls() -> None:
    W, D, H = 23, 23, 17
    s = Piece(W, H, D)
    c = W // 2
    # cliff height profile: tall wall at the back, broken ledges toward the pool
    def cliff_top(x, z):
        if z < 13:
            return 0
        base = 6 + (z - 13) * 2
        wob = h(x, z, 21) % 3
        return min(14, base + wob - (2 if abs(x - c) > 8 else 0))
    for x in range(W):
        for z in range(D):
            top = cliff_top(x, z)
            s.set(x, 0, z, B("sand") if h(x, z, 2) % 3 else B("coarse_dirt"))
            for y in range(1, top + 1):
                s.set(x, y, z, rock(x, y, z))
    # gorge cut for the waterfall
    for z in range(12, D):
        for x in (c - 1, c, c + 1):
            for y in range(1, 15):
                if z < 16 or y > 11:
                    s.set(x, y, z, AIR())
    # stream on top of the cliff feeding the fall
    for z in range(16, D):
        s.set(c, 11, z, rock(c, 11, z))
        s.set(c, 12, z, WATER() if z < D - 1 else rock(c, 12, z))   # closed at the back
        for x in (c - 1, c + 1):
            s.set(x, 11, z, rock(x, 11, z)); s.set(x, 12, z, rock(x, 12, z))
            s.set(x, 13, z, B("moss_block"))
    for y in range(1, 12):
        s.set(c, y, 15, FALL())
    for y in range(1, 11):                        # wet rock behind the fall
        s.set(c, y, 16, B("mossy_cobblestone") if y % 3 else B("moss_block"))
    # pool: ellipse in front of the cliff, 2 deep, with a sand/clay bed
    pool = []
    for x in range(W):
        for z in range(4, 16):
            if ((x - c) / 6.5) ** 2 + ((z - 11) / 4.5) ** 2 <= 1.0:
                pool.append((x, z))
                s.set(x, 0, z, WATER()); s.set(x, -1, z, WATER()); s.set(x, -2, z, B("clay"))
                for y in range(1, 6):
                    if s.get(x, y, z) != "minecraft:air" and (x, z) != (c, 15):
                        s.set(x, y, z, AIR())
    ps = set(pool)
    for x, z in pool:
        if h(x, z, 31) % 11 == 0 and (x, z) != (c, 15):
            s.set(x, 1, z, B("lily_pad"))
    s.set(c - 2, -1, 9, B("big_dripleaf_stem", facing="north", waterlogged="true"))
    s.set(c - 2, 0, 9, B("big_dripleaf", facing="north", tilt="none", waterlogged="true"))
    # mossy banks, reeds and ferns around the pool
    bank = [(x, z) for x in range(W) for z in range(1, 13)
            if (x, z) not in ps and any((x + a, z + b) in ps for a in (-1, 0, 1) for b in (-1, 0, 1))]
    for x, z in bank:
        s.set(x, 0, z, B("moss_block"))
    for i, (x, z) in enumerate(bank):
        if i % 4 == 0:
            reeds(s, x, z, 2 + i % 3)
    plants_on(s, [(x, z) for x in range(W) for z in range(1, 13) if (x, z) not in ps
                  and s.get(x, 0, z) in ("minecraft:moss_block",) and s.free(x, 1, z)], 41)
    # vines and glow lichen down the cliff face
    for x in range(W):
        for z in range(13, 16):
            for y in range(2, 12):
                if s.free(x, y, z) and not s.free(x, y, z + 1) and (x, z) != (c, 15) \
                        and h(x, y, z) % 3 == 0:
                    s.set(x, y, z, B("vine", north="false", south="true", east="false", west="false",
                                     up="false"))
    for x in range(W):
        top = cliff_top(x, D - 1)
        if top and s.free(x, top + 1, D - 1) and h(x, 0, 51) % 3 == 0:
            s.set(x, top + 1, D - 1, B("short_grass") if x % 2 else B("dead_bush"))
    # palms
    for (x, z, t) in ((3, 5, 7), (19, 4, 6), (2, 11, 8), (20, 11, 7)):
        if not s.free(x, 1, z):
            continue
        s.set(x, 0, z, B("rooted_dirt"))
        date_palm(s, x, z, t)
    # trailhead: sign-post lantern and bench
    lamp(s, c + 3, 1)
    s.set(c - 3, 1, 1, STAIR("south", "spruce")); s.set(c - 4, 1, 1, STAIR("south", "spruce"))
    # jigsaws
    s.set(c, 1, 0, kit.street_j("north_up"))
    s.set(0, 1, 2, kit.street_j("west_up"))
    s.set(W - 1, 1, 2, kit.street_j("east_up"))
    s.save(kit.struct / "spring_falls.nbt")


# ---------------------------------------------------------------- trails
def trails() -> None:
    W = 5
    def paving(x, z):
        if z in (0, W - 1):
            v = h(x, z, 9) % 6
            return B("sand") if v < 3 else B("coarse_dirt") if v < 5 else B("smooth_sandstone")
        return trail(x, z)
    s = kit.straight("trail", 13, W, paving, entrances=(3, 9))
    for x, z in ((1, 0), (6, W - 1), (11, 0)):
        s.set(x, 1, z, B("cobblestone_wall", north="none", south="none", east="none", west="none",
                         up="true", waterlogged="false") if x == 6 else B("dead_bush"))
    s.save(kit.struct / "trail_straight.nbt")
    s = kit.straight("trail", 9, W, paving, entrances=(4,))
    s.set(1, 1, W - 1, B("dead_bush"))
    s.save(kit.struct / "trail_short.nbt")
    kit.junction("trail_crossroad", W, {"n", "s", "w", "e"}, trail).save(kit.struct / "trail_crossroad.nbt")
    kit.junction("trail_corner", W, {"n", "e"}, trail).save(kit.struct / "trail_corner.nbt")
    kit.junction("trail_t", W, {"w", "e", "s"}, trail).save(kit.struct / "trail_t.nbt")
    kit.terminator(W, trail, B("cobblestone_wall", north="none", south="none", east="none", west="none",
                                up="true", waterlogged="false")).save(kit.struct / "terminator.nbt")


# ---------------------------------------------------------------- pieces
MOSAIC = ["white_terracotta", "yellow_terracotta", "orange_terracotta", "brown_terracotta",
          "black_terracotta", "light_gray_terracotta"]


def synagogue() -> None:
    """Ancient Ein Gedi synagogue: mosaic floor with a zodiac wheel, ruined walls, ark niche,
    benches, under a modern excavation shade canopy."""
    W, D = 17, 15
    s = Piece(W, 9, D)
    front(s, W)
    x0, z0, x1, z1 = 1, 1, W - 2, D - 2
    cx, cz = W // 2, (z0 + z1) // 2 + 1
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            edge = x in (x0, x1) or z in (z0, z1)
            if edge:
                s.set(x, 0, z, B("smooth_sandstone"))
                continue
            inner = x in (x0 + 1, x1 - 1) or z in (z0 + 1, z1 - 1)
            r2 = (x - cx) ** 2 + (z - cz) ** 2
            if inner:                                   # guilloche border
                m = "black_terracotta" if (x + z) % 2 else "orange_terracotta"
            elif r2 <= 2:
                m = "yellow_terracotta"                 # sun at the centre
            elif r2 <= 10:
                m = MOSAIC[(int((x - cx) * 7 + (z - cz) * 3)) % 4]   # zodiac wheel
            elif r2 <= 13:
                m = "brown_terracotta"
            else:
                m = "white_terracotta" if (x * 3 + z) % 5 else "light_gray_terracotta"
            s.set(x, 0, z, B(m))
    # ruined outer walls of rough stone, door gap on the north side
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if not (x in (x0, x1) or z in (z0, z1)):
                continue
            if z == z0 and abs(x - cx) <= 1:
                continue
            top = 1 + h(x, z, 61) % 3
            for y in range(1, top + 1):
                s.set(x, y, z, B("cobblestone") if (x + y) % 3 else B("stone_bricks"))
    # benches along the side walls (stepped)
    for z in range(z0 + 2, z1 - 1):
        for x, f in ((x0 + 1, "east"), (x1 - 1, "west")):
            s.set(x, 1, z, STAIR(f, "stone_brick"))
    # ark niche (Torah shrine) on the south wall
    for x in range(cx - 1, cx + 2):
        s.set(x, 1, z1 - 1, B("chiseled_sandstone"))
        s.set(x, 2, z1, B("chiseled_sandstone")); s.set(x, 3, z1, B("cut_sandstone"))
    s.set(cx, 2, z1 - 1, B("candle", candles="3", lit="false", waterlogged="false"))
    s.set(cx - 1, 2, z1 - 1, B("decorated_pot", cracked="true", facing="north", waterlogged="false"))
    s.set(cx + 1, 1, z1 - 2, CHEST("north", LOOT))
    # column bases
    for x in (cx - 4, cx + 4):
        for z in (z0 + 3, z1 - 3):
            s.set(x, 1, z, B("chiseled_stone_bricks"))
            s.set(x, 2, z, B("stone_brick_wall", north="none", south="none", east="none", west="none",
                             up="true", waterlogged="false"))
    # excavation shade canopy: posts + light fabric
    for x in (x0, x1):
        for z in (z0, z1):
            for y in range(1, 7):
                s.set(x, y, z, B("iron_bars", north="false", south="false", east="false", west="false",
                                 waterlogged="false"))
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            s.set(x, 7, z, B("white_wool") if (x + z) % 6 else B("light_gray_wool"))
    s.set(cx, 6, cz, HLANTERN())
    # visitor rail and info lectern
    for x in range(0, W):
        if abs(x - cx) > 1:
            s.set(x, 1, 0, SFENCE("spruce"))
    s.set(cx + 2, 1, 0, AIR())
    s.set(cx - 3, 1, 2, B("lectern", facing="south", has_book="false", powered="false"))
    s.save(kit.struct / "ancient_synagogue.nbt")


def ranger_station() -> None:
    W, D = 11, 10
    s = Piece(W, 7, D)
    front(s, W)
    for x in range(W):
        for z in range(1, D):
            s.set(x, 0, z, trail(x, z))
    x0, z0, x1, z1 = 1, 4, W - 2, D - 2
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            s.set(x, 0, z, B("smooth_stone"))
            if x in (x0, x1) or z in (z0, z1):
                for y in range(1, 4):
                    s.set(x, y, z, rock(x, y, z) if y < 3 else B("cut_sandstone"))
            s.set(x, 4, z, SLAB("smooth_stone"))
    for x in (x0 + 2, x1 - 2):
        s.set(x, 2, z0, PANE())
    for z in (z0 + 2,):
        s.set(x0, 2, z, PANE()); s.set(x1, 2, z, PANE())
    for y in (1, 2):
        s.set(W // 2, y, z0, AIR())
    s.set(W // 2, 1, z0, B("spruce_door", facing="north", half="lower", hinge="left", open="false",
                          powered="false"))
    s.set(W // 2, 2, z0, B("spruce_door", facing="north", half="upper", hinge="left", open="false",
                          powered="false"))
    # interior: desk with map lectern, water cooler cauldron, bunk, first aid barrel
    s.set(x0 + 1, 1, z1 - 1, B("lectern", facing="north", has_book="false", powered="false"))
    s.set(x1 - 1, 1, z1 - 1, B("water_cauldron", level="3"))
    s.set(x1 - 1, 1, z0 + 1, BARREL("up"))
    s.set(x0 + 1, 1, z0 + 1, B("brown_bed", facing="east", part="foot", occupied="false"))
    s.set(x0 + 2, 1, z0 + 1, B("brown_bed", facing="east", part="head", occupied="false"))
    s.set(W // 2, 3, z0 + 2, HLANTERN())
    # shaded porch: pergola of spruce with palm-frond roof
    for x in (x0, x1):
        for y in range(1, 4):
            s.set(x, y, 1, SFENCE("spruce"))
    for x in range(x0, x1 + 1):
        for z in range(1, z0):
            s.set(x, 4, z, B("bamboo_mosaic_slab", type="bottom", waterlogged="false"))
    table(s, 3, 1, 2)
    s.set(W - 3, 1, 2, B("potted_cactus")); s.set(W - 2, 1, 2, B("cauldron"))
    s.save(kit.struct / "ranger_station.nbt")


def cave() -> None:
    """Rock mound with a cave mouth (like the caves David hid in), hidden chamber with loot."""
    W, D, H = 13, 13, 9
    s = Piece(W, H, D)
    front(s, W)
    cx, cz = W // 2, 7
    for x in range(W):
        for z in range(1, D):
            s.set(x, 0, z, B("sand") if h(x, z) % 3 else B("gravel"))
            r = ((x - cx) ** 2 + (z - cz) ** 2) ** 0.5
            top = int(8 - r * 1.3 + (h(x, z, 71) % 2))
            for y in range(1, max(0, top) + 1):
                s.set(x, y, z, rock(x, y, z))
    # tunnel from the north face to a chamber
    for z in range(1, cz + 1):
        for x in (cx, cx + 1 if z > 3 else cx):
            for y in (1, 2):
                s.set(x, y, z, AIR())
    for x in range(cx - 2, cx + 3):
        for z in range(cz - 1, cz + 3):
            for y in range(1, 4):
                s.set(x, y, z, AIR())
            s.set(x, 0, z, B("sand") if (x + z) % 2 else B("packed_mud"))
    s.set(cx - 2, 1, cz + 2, CHEST("north", LOOT))
    s.set(cx + 2, 1, cz + 2, B("decorated_pot", cracked="false", facing="north", waterlogged="false"))
    s.set(cx + 2, 1, cz - 1, B("decorated_pot", cracked="true", facing="north", waterlogged="false"))
    s.set(cx, 1, cz + 2, B("campfire", facing="north", lit="false", signal_fire="false", waterlogged="false"))
    s.set(cx - 1, 3, cz, B("pointed_dripstone", thickness="tip", vertical_direction="down",
                           waterlogged="false"))
    s.set(cx - 1, 4, cz, B("dripstone_block"))
    s.set(cx + 1, 3, cz + 1, B("glow_lichen", down="false", up="true", north="false", south="false",
                               east="false", west="false", waterlogged="false"))
    s.set(cx - 2, 2, cz + 2, B("candle", candles="2", lit="false", waterlogged="false"))
    # ferns and vines over the mouth
    for x in (cx - 1, cx + 1):
        if s.free(x, 1, 1):
            s.set(x, 1, 1, B("fern"))
    s.save(kit.struct / "hidden_cave.nbt")


def palm_spring() -> None:
    W, D = 13, 13
    s = Piece(W, 11, D)
    front(s, W)
    cx, cz = W // 2, 7
    for x in range(W):
        for z in range(1, D):
            r2 = ((x - cx) / 3.2) ** 2 + ((z - cz) / 2.6) ** 2
            if r2 <= 1:
                s.set(x, 0, z, WATER()); s.set(x, -1, z, B("clay"))
            elif r2 <= 2.0:
                s.set(x, 0, z, greenery(x, z))
            else:
                s.set(x, 0, z, B("sand") if h(x, z) % 4 else B("coarse_dirt"))
    for x, z in ((cx - 4, cz), (cx + 4, cz - 1), (cx, cz + 3), (cx + 3, cz + 2)):
        if s.get(x, 0, z) != "minecraft:water":
            reeds(s, x, z, 2 + h(x, z) % 2)
    plants_on(s, [(x, z) for x in range(W) for z in range(1, D) if s.get(x, 0, z) in
                  ("minecraft:grass_block", "minecraft:moss_block", "minecraft:rooted_dirt")], 81)
    s.set(cx - 1, 1, cz, B("lily_pad"))
    for (x, z, t) in ((2, 3, 6), (10, 4, 7), (3, 11, 8), (10, 11, 6)):
        s.set(x, 0, z, B("rooted_dirt"))
        for dy in range(1, t + 3):
            if not s.free(x, dy, z):
                s.set(x, dy, z, AIR())
        date_palm(s, x, z, t)
    s.set(cx, 1, 1, B("cobblestone_wall", north="none", south="none", east="none", west="none",
                      up="true", waterlogged="false"))
    s.save(kit.struct / "palm_spring.nbt")


def balsam_terraces() -> None:
    """Terraced gardens of the ancient Ein Gedi balsam (persimmon) perfume industry."""
    W, D = 13, 14
    s = Piece(W, 9, D)
    front(s, W)
    for x in range(W):
        s.set(x, 0, 1, trail(x, 1))
    steps = [(2, 5, 0), (6, 9, 1), (10, 13, 2)]       # z-range, terrace level
    for z0, z1, lvl in steps:
        for z in range(z0, z1):
            for x in range(W):
                for y in range(1, lvl + 1):
                    s.set(x, y, z, rock(x, y, z))
                ground = lvl
                if x == W // 2:                      # irrigation channel down the terraces
                    s.set(x, ground, z, WATER() if z == z0 + 1 else B("mud_bricks"))
                    continue
                if x in (0, W - 1):
                    s.set(x, ground + 1, z, B("cobblestone_wall", north="none", south="none", east="none",
                                              west="none", up="true", waterlogged="false"))
                    continue
                s.set(x, ground, z, B("grass_block", snowy="false") if (x + z) % 3 else B("rooted_dirt"))
                v = h(x, z, 91) % 6
                if v < 3:
                    s.set(x, ground + 1, z, mod("mediterranean_herbs", age=str(2 + v % 2)))
                elif v == 3:
                    s.set(x, ground + 1, z, B("flowering_azalea"))
                elif v == 4:
                    s.set(x, ground + 1, z, B("sweet_berry_bush", age="3"))
        # front edge of each terrace is a stone retaining wall
        for x in range(W):
            if lvl:
                s.set(x, lvl, z0, B("stone_bricks") if x != W // 2 else B("mud_bricks"))
    # stairs up the centre beside the channel
    s.set(W // 2 + 1, 1, 6, STAIR("south", "stone_brick")); s.set(W // 2 + 1, 2, 10, STAIR("south", "stone_brick"))
    s.set(1, 3, 12, BARREL("up")); s.set(2, 3, 12, B("decorated_pot", cracked="false", facing="north",
                                                     waterlogged="false"))
    s.set(W - 2, 3, 12, B("cauldron"))
    s.save(kit.struct / "balsam_terraces.nbt")


def stream_crossing() -> None:
    W, D = 13, 11
    s = Piece(W, 6, D)
    front(s, W)
    for x in range(W):
        for z in range(1, D):
            if 4 <= z <= 6:
                s.set(x, 0, z, WATER()); s.set(x, -1, z, B("gravel") if (x + z) % 2 else B("sand"))
            elif z in (3, 7):
                s.set(x, 0, z, greenery(x, z))
            else:
                s.set(x, 0, z, trail(x, z))
    for z in range(4, 7):                            # stepping stones
        s.set(W // 2, 0, z, B("smooth_sandstone") if z % 2 else B("calcite"))
        s.set(W // 2, 1, z, AIR())
    for x in (1, 3, 9, 11):
        s.set(x, 1, 3 if x < 6 else 7, B("sugar_cane", age="0"))
        s.set(x, 2, 3 if x < 6 else 7, B("sugar_cane", age="0"))
    plants_on(s, [(x, z) for x in range(W) for z in (3, 7)], 101)
    s.set(2, 1, 5, B("lily_pad")); s.set(10, 1, 4, B("lily_pad"))
    for x in (0, W - 1):
        s.set(x, 1, 8, B("moss_block")); s.set(x, 1, 2, B("mossy_cobblestone"))
    s.set(W // 2 + 2, 1, 9, B("fern"))
    s.save(kit.struct / "stream_crossing.nbt")


def lookout() -> None:
    W, D = 11, 11
    s = Piece(W, 10, D)
    front(s, W)
    for x in range(W):
        for z in range(1, D):
            top = min(6, 1 + (z - 1))
            if abs(x - W // 2) > 3:
                top = max(1, top - 2)
            for y in range(1, top + 1):
                s.set(x, y, z, rock(x, y, z))
            s.set(x, 0, z, B("sand"))
    # carved stairs up the middle
    for z in range(1, 7):
        s.set(W // 2, z, z, STAIR("south", "sandstone"))
        for y in range(z + 1, 8):
            s.set(W // 2, y, z, AIR())
    # viewing platform with railing, bench and lantern
    for x in range(1, W - 1):
        for z in range(7, D):
            s.set(x, 6, z, B("smooth_sandstone"))
            for y in range(7, 10):
                if s.get(x, y, z) != "minecraft:air":
                    s.set(x, y, z, AIR())
            if x in (1, W - 2) or z == D - 1:
                s.set(x, 7, z, SFENCE("spruce"))
    s.set(3, 7, 8, STAIR("south", "spruce")); s.set(4, 7, 8, STAIR("south", "spruce"))
    s.set(W - 3, 7, 8, SFENCE("spruce")); s.set(W - 3, 8, 8, LANTERN())
    s.save(kit.struct / "lookout.nbt")


def main() -> None:
    spring_falls(); trails(); synagogue(); ranger_station(); cave(); palm_spring(); balsam_terraces()
    stream_crossing(); lookout()
    kit.write_structure_json("israel_simulator:judean_desert", size=4, max_distance=64)
    kit.write_pools(
        "spring_falls",
        streets=[("trail_straight", 8, "terrain_matching"), ("trail_short", 4, "terrain_matching"),
                 ("trail_crossroad", 1, "terrain_matching"), ("trail_corner", 3, "terrain_matching"),
                 ("trail_t", 3, "terrain_matching")],
        terminators=[("terminator", 1, "terrain_matching")],
        buildings=[("ancient_synagogue", 2, "rigid"), ("ranger_station", 2, "rigid"), ("hidden_cave", 3, "rigid"),
                   ("palm_spring", 4, "rigid"), ("balsam_terraces", 3, "rigid"), ("stream_crossing", 3, "rigid"),
                   ("lookout", 2, "rigid")],
    )


if __name__ == "__main__":
    main()
