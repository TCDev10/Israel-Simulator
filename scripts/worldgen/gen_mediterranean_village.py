#!/usr/bin/env python3
"""Generate the Mediterranean coastal village jigsaw (DataVersion 4903).

Vanilla-village layout (like jerusalem_city / tel_aviv_city):
  * start piece  mediterranean/square  (rigid): limestone square with a roofed well,
    olive trees; a chapel is guaranteed on its north side (mediterranean/landmarks)
  * paths        terrain_matching dirt-path / gravel / cobble lanes, floor y=0, jigsaws y=1
  * buildings    rigid, porch row z=0 with the entrance jigsaw at (cx,1,0) north_up;
                 every rigid piece sits on a buried FOOTING-deep sandstone footing

Run:  /workspace/nbtvenv/bin/python scripts/worldgen/gen_mediterranean_village.py
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from gen_jerusalem_city import (  # noqa: E402
    BARREL, CHEST, CRAFT, FENCE, HLANTERN, KNOWN, LANTERN, POT, ROOT, Structure, blk, jigsaw,
    write_pool,
)

if not KNOWN:
    raise SystemExit("blocks.json report missing (/workspace/mcreports/out/reports/blocks.json)")

NS = "israel_simulator"
STRUCT = ROOT / "src/main/resources/data/israel_simulator/structure/mediterranean"
POOL = ROOT / "src/main/resources/data/israel_simulator/worldgen/template_pool/mediterranean"
START_POOL = ROOT / "src/main/resources/data/israel_simulator/worldgen/template_pool/mediterranean_village.json"
STRUCTURE_JSON = ROOT / "src/main/resources/data/israel_simulator/worldgen/structure/mediterranean_village.json"

LOOT_HOUSE = f"{NS}:chests/mediterranean_village"
LOOT_BAKERY = f"{NS}:chests/mediterranean_bakery"
LOOT_FISH = f"{NS}:chests/mediterranean_fisherman"


def B(name: str, **p: str):
    return blk(f"minecraft:{name}", **p)


AIR = lambda: B("air")
LEAF = lambda kind="pale_oak": B(f"{kind}_leaves", distance="1", persistent="true", waterlogged="false")
BOUG = lambda: LEAF("flowering_azalea")          # bougainvillea stand-in (pink blossoms)
PLATE = lambda: B("oak_pressure_plate", powered="false")
LADDER = lambda f="north": B("ladder", facing=f, waterlogged="false")


def STAIR(facing: str, mat: str = "oak", half: str = "bottom"):
    return B(f"{mat}_stairs", facing=facing, half=half, shape="straight", waterlogged="false")


def SLAB(mat: str, typ: str = "bottom"):
    return B(f"{mat}_slab", type=typ, waterlogged="false")


def PANE(color: str | None = None):
    n = f"{color}_stained_glass_pane" if color else "glass_pane"
    return B(n, north="false", south="false", east="false", west="false", waterlogged="false")


def WALL(mat: str = "cobblestone"):
    return B(f"{mat}_wall", north="none", south="none", east="none", west="none", up="true",
             waterlogged="false")


def SFENCE(mat: str = "spruce"):
    return B(f"{mat}_fence", north="false", south="false", east="false", west="false", waterlogged="false")


def DOOR2(s, x, y, z, wood: str, facing: str = "north") -> None:
    for half, dy in (("lower", 0), ("upper", 1)):
        s.set(x, y + dy, z, B(f"{wood}_door", facing=facing, half=half, hinge="left", open="false",
                              powered="false"))


def SHUTTER(wood: str, facing: str):
    return B(f"{wood}_trapdoor", facing=facing, half="bottom", open="true", powered="false",
             waterlogged="false")


def path_block(x: int, z: int):
    h = (x * 7349 + z * 9151) % 11
    if h < 6:
        return B("dirt_path")
    if h < 8:
        return B("gravel")
    if h < 10:
        return B("cobblestone")
    return B("coarse_dirt")


# ---------------------------------------------------------------- rigid footing
class Rigid(Structure):
    """Rigid piece authored with the floor at y=0; shifted up by FOOTING with
    sandstone below the footprint so it never floats (same approach as Tel Aviv)."""

    FOOTING = 4

    def __init__(self, sx: int, sy: int, sz: int):
        self._ready = False
        super().__init__(sx, sy + self.FOOTING, sz)
        self._ready = True

    def set(self, x, y, z, entry) -> None:
        super().set(x, y + self.FOOTING if self._ready else y, z, entry)

    def get(self, x, y, z) -> str:
        return str(self.palette[self.blocks[(x, y + self.FOOTING, z)]]["Name"])

    def free(self, x, y, z) -> bool:
        return (0 <= x < self.sx and 0 <= z < self.sz and 0 <= y + self.FOOTING < self.sy
                and self.get(x, y, z) == "minecraft:air")

    def save(self, path: Path) -> None:
        self._ready = False
        F = self.FOOTING
        for x in range(self.sx):
            for z in range(self.sz):
                if str(self.palette[self.blocks[(x, F, z)]]["Name"]) not in ("minecraft:air",
                                                                              "minecraft:structure_void"):
                    for y in range(F):
                        self.set(x, y, z, B("smooth_sandstone"))
        super().save(path)


# ---------------------------------------------------------------- jigsaws
def street_j(o):
    return jigsaw(o, "minecraft:street", "minecraft:street", f"{NS}:mediterranean/streets",
                  "minecraft:structure_void")


def bld_j(o, pool="buildings"):
    return jigsaw(o, "minecraft:building_entrance", "minecraft:building_entrance",
                  f"{NS}:mediterranean/{pool}", "minecraft:structure_void")


def entrance_j():
    return jigsaw("north_up", "minecraft:building_entrance", "minecraft:building_entrance",
                  "minecraft:empty", "minecraft:air")


def end_j(o):
    return jigsaw(o, "minecraft:street", "minecraft:street", "minecraft:empty", "minecraft:structure_void")


# ---------------------------------------------------------------- props
def olive(s, x, z, y=1) -> None:
    """Gnarled olive tree: pale-oak bark, silvery pale-oak leaves, leaning crown."""
    s.set(x, y, z, B("pale_oak_log", axis="y"))
    s.set(x, y + 1, z, B("pale_oak_log", axis="y"))
    s.set(x + 1 if x + 1 < s.sx else x, y + 2, z, B("pale_oak_log", axis="x"))
    s.set(x, y + 2, z, B("pale_oak_log", axis="y"))
    for dx in (-1, 0, 1, 2):
        for dz in (-1, 0, 1):
            xx, zz = x + dx, z + dz
            if not (0 <= xx < s.sx and 0 <= zz < s.sz):
                continue
            if (dx, dz) in ((-1, -1), (2, 1)):
                continue
            if s.free(xx, y + 2, zz):
                s.set(xx, y + 2, zz, LEAF())
            if s.free(xx, y + 3, zz) and (dx, dz) not in ((2, -1), (-1, 1)):
                s.set(xx, y + 3, zz, LEAF())
    s.set(x, y + 4, z, LEAF())


def lamp(s, x, z, y=1) -> None:
    s.set(x, y, z, SFENCE()); s.set(x, y + 1, z, SFENCE()); s.set(x, y + 2, z, LANTERN())


def table(s, x, y, z) -> None:
    s.set(x, y, z, SFENCE("oak")); s.set(x, y + 1, z, PLATE())
    if s.free(x - 1, y, z):
        s.set(x - 1, y, z, STAIR("west"))
    if s.free(x + 1, y, z):
        s.set(x + 1, y, z, STAIR("east"))


def bed(s, x, y, z, color, facing="south") -> None:
    dz = 1 if facing == "south" else -1
    s.set(x, y, z, B(f"{color}_bed", facing=facing, part="foot", occupied="false"))
    s.set(x, y, z + dz, B(f"{color}_bed", facing=facing, part="head", occupied="false"))


# ---------------------------------------------------------------- start: square
def square() -> None:
    s = Rigid(15, 7, 15)
    for x in range(15):
        for z in range(15):
            edge = x in (0, 14) or z in (0, 14)
            s.set(x, 0, z, B("cobblestone") if edge else
                  (B("cut_sandstone") if (x + z) % 2 else B("smooth_sandstone")))
    for x, z in ((4, 4), (10, 4), (4, 10), (10, 10)):
        s.set(x, 0, z, B("light_gray_terracotta"))      # framework marker
    # roofed well
    for x in range(6, 9):
        for z in range(6, 9):
            if (x, z) != (7, 7):
                s.set(x, 1, z, B("stone_bricks"))
    s.set(7, 0, 7, B("stone_bricks"))
    s.set(7, 1, 7, B("water_cauldron", level="3"))
    for x, z in ((6, 6), (8, 6), (6, 8), (8, 8)):
        s.set(x, 2, z, SFENCE()); s.set(x, 3, z, SFENCE())
    s.fills(6, 4, 6, 8, 4, 8, SLAB("spruce"))
    s.set(7, 3, 7, HLANTERN())
    for x, z in ((2, 2), (11, 2), (2, 11), (11, 11)):
        olive(s, x, z)
    for x, z, f in ((7, 4, "north"), (7, 10, "south"), (4, 7, "west"), (10, 7, "east")):
        s.set(x, 1, z, STAIR(f))
    for x, z in ((5, 1), (9, 13), (1, 9), (13, 5)):
        s.set(x, 1, z, B("decorated_pot", cracked="false", facing="north", waterlogged="false"))
    s.set(7, 1, 0, bld_j("north_up", "landmarks"))
    s.set(7, 1, 14, street_j("south_up"))
    s.set(0, 1, 7, street_j("west_up"))
    s.set(14, 1, 7, street_j("east_up"))
    s.save(STRUCT / "square.nbt")


# ---------------------------------------------------------------- paths
def path_straight() -> None:
    s = Structure(13, 4, 7)
    for x in range(13):
        for z in range(1, 6):
            s.set(x, 0, z, path_block(x, z) if 2 <= z <= 4 or (x + z) % 3 else B("coarse_dirt"))
    for x in (2, 3, 4, 8, 9, 10):     # spurs to doors
        s.set(x, 0, 0, path_block(x, 0)); s.set(x, 0, 6, path_block(x, 6))
    lamp(s, 6, 0)
    s.set(0, 1, 3, street_j("west_up")); s.set(12, 1, 3, street_j("east_up"))
    for x in (3, 9):
        s.set(x, 1, 0, bld_j("north_up")); s.set(x, 1, 6, bld_j("south_up"))
    s.save(STRUCT / "path_straight.nbt")


def _junction(arms: set[str], name: str) -> None:
    s = Structure(7, 4, 7)
    for x in range(1, 6):
        for z in range(1, 6):
            s.set(x, 0, z, path_block(x, z))
    for i in range(1, 6):
        if "n" in arms: s.set(i, 0, 0, path_block(i, 0))
        if "s" in arms: s.set(i, 0, 6, path_block(i, 6))
        if "w" in arms: s.set(0, 0, i, path_block(0, i))
        if "e" in arms: s.set(6, 0, i, path_block(6, i))
    for o, x, z, a in (("north_up", 3, 0, "n"), ("south_up", 3, 6, "s"),
                       ("west_up", 0, 3, "w"), ("east_up", 6, 3, "e")):
        if a in arms:
            s.set(x, 1, z, street_j(o))
        else:
            s.set(x, 0, z, path_block(x, z))
            s.set(x, 1, z, bld_j(o))
    s.save(STRUCT / f"{name}.nbt")


def path_junctions() -> None:
    _junction({"n", "s", "w", "e"}, "path_crossroad")
    _junction({"n", "e"}, "path_corner")
    _junction({"w", "e", "s"}, "path_t")


def terminator() -> None:
    s = Structure(3, 3, 7)
    for x in range(3):
        for z in range(1, 6):
            s.set(x, 0, z, path_block(x, z))
    s.set(2, 1, 3, B("hay_block", axis="y"))
    s.set(0, 1, 3, end_j("west_up"))
    s.save(STRUCT / "terminator.nbt")


# ---------------------------------------------------------------- houses
def house(name: str, W: int, D: int, floors: int, mat: str, *, upper=None, cut=None,
          door="warped", shutter="warped", trim="smooth_quartz", beds=("light_blue",),
          loot=LOOT_HOUSE, pergola=True, vines=True) -> None:
    """Parametric whitewashed house.

    Body = rectangle x1..W-2, z1..D-2 minus `cut` (courtyard); `upper` limits the upper
    floor(s) to a sub-rectangle, leaving the rest of the lower roof as a terrace.
    """
    s = Rigid(W, floors * 4 + 5, D)
    cx = W // 2
    bx0, bz0, bx1, bz1 = 1, 1, W - 2, D - 2

    def in_rect(x, z, r):
        return r is not None and r[0] <= x <= r[2] and r[1] <= z <= r[3]

    def body(k, x, z):
        if not (bx0 <= x <= bx1 and bz0 <= z <= bz1) or in_rect(x, z, cut):
            return False
        return k == 0 or upper is None or in_rect(x, z, upper)

    def is_wall(k, x, z):
        return body(k, x, z) and any(not body(k, x + a, z + b) for a, b in
                                     ((1, 0), (-1, 0), (0, 1), (0, -1)))

    # porch + approach
    for x in range(cx - 1, cx + 2):
        s.set(x, 0, 0, path_block(x, 0))
    s.set(cx, 1, 0, entrance_j())
    lad = (bx1 - 1, bz1 - 1)
    for k in range(floors):
        yb = 4 * k
        for x in range(W):
            for z in range(D):
                if body(k, x, z):
                    s.set(x, yb, z, B(mat) if is_wall(k, x, z) else B("smooth_stone"))
                    for y in range(yb + 1, yb + 4):
                        if is_wall(k, x, z):
                            s.set(x, y, z, B(mat))
                # roof / terrace above this floor
                if body(k, x, z):
                    s.set(x, yb + 4, z, B(trim) if is_wall(k, x, z) else B(mat))
        # windows: along walls, every 3rd cell, not at corners
        for x in range(W):
            for z in range(D):
                if not is_wall(k, x, z):
                    continue
                straight = (body(k, x - 1, z) and body(k, x + 1, z)) or (body(k, x, z - 1) and body(k, x, z + 1))
                along = x if body(k, x - 1, z) and body(k, x + 1, z) else z
                if straight and along % 3 == 1 and (x, z) != (cx, bz0):
                    s.set(x, yb + 2, z, PANE())
                    if k == 0 and z == bz0 and shutter:
                        for sx_ in (x - 1, x + 1):
                            if s.free(sx_, yb + 2, z - 1):
                                s.set(sx_, yb + 2, z - 1, SHUTTER(shutter, "north"))
    DOOR2(s, cx, 1, bz0, door)
    s.set(cx, 3, bz0 - 1, SLAB("smooth_quartz", "top"))       # door canopy
    # parapets on every exposed roof edge
    for x in range(W):
        for z in range(D):
            for k in range(floors):
                if body(k, x, z) and (k == floors - 1 or not body(k + 1, x, z)):
                    if is_wall(k, x, z):
                        s.set(x, 4 * k + 5, z, B(trim) if (x + z) % 4 else SLAB(
                            "smooth_quartz" if trim == "smooth_quartz" else "sandstone"))
    # ladder through all floors to the top roof
    for y in range(1, 4 * floors + 1):
        s.set(lad[0], y, lad[1], LADDER("north"))
    # interiors
    for k in range(floors):
        y = 4 * k + 1
        cells = [(x, z) for x in range(bx0 + 1, bx1) for z in range(bz0 + 1, bz1)
                 if body(k, x, z) and not is_wall(k, x, z) and (x, z) != lad
                 and (x, z) != (cx, bz0 + 1)]
        if not cells:
            continue
        xs = [c[0] for c in cells]; zs = [c[1] for c in cells]
        ix0, ix1, iz0, iz1 = min(xs), max(xs), min(zs), max(zs)
        top = k == floors - 1
        if top or floors == 1:
            if s.free(ix0, y, iz1 - 1) and s.free(ix0, y, iz1):
                bed(s, ix0, y, iz1 - 1, beds[k % len(beds)])
            if s.free(ix0, y, iz0):
                s.set(ix0, y, iz0, CHEST("east", loot))
        if k == 0:
            if s.free(ix1, y, iz0):
                s.set(ix1, y, iz0, B("smoker", facing="west", lit="false"))
            if s.free(ix1, y, iz0 + 1) and (ix1, iz0 + 1) != lad:
                s.set(ix1, y, iz0 + 1, BARREL("up", loot))
            if s.free(ix1 - 1, y, iz0) and (ix1 - 1, iz0) != (cx, iz0):
                s.set(ix1 - 1, y, iz0, CRAFT())
        mx, mz = (ix0 + ix1) // 2, (iz0 + iz1) // 2
        if s.free(mx, y, mz) and (mx, mz) != (cx, bz0 + 1):
            table(s, mx, y, mz)
        if s.free(ix0, y, mz):
            s.set(ix0, y, mz, B("potted_red_tulip"))
        if s.free(mx, y + 2, mz):
            s.set(mx, y + 2, mz, HLANTERN())
    # roof terraces: pergola with bougainvillea, pots, a table
    topk = floors - 1
    terr = [(x, z) for x in range(W) for z in range(D)
            for k in range(floors) if body(k, x, z) and not is_wall(k, x, z)
            and (k == topk or not body(k + 1, x, z))]
    if pergola and terr:
        tx = sorted(terr)
        (px, pz) = tx[0]
        yk = max(k for k in range(floors) if body(k, px, pz))
        y = 4 * yk + 5
        for dx, dz in ((0, 0), (2, 0), (0, 2), (2, 2)):
            if (px + dx, pz + dz) in terr and s.free(px + dx, y, pz + dz):
                s.set(px + dx, y, pz + dz, SFENCE("oak")); s.set(px + dx, y + 1, pz + dz, SFENCE("oak"))
        for dx in range(3):
            for dz in range(3):
                if (px + dx, pz + dz) in terr and s.free(px + dx, y + 2, pz + dz):
                    s.set(px + dx, y + 2, pz + dz, BOUG())
        if (px + 1, pz + 1) in terr and s.free(px + 1, y, pz + 1):
            table(s, px + 1, y, pz + 1)
        for (x, z) in terr[-3:]:
            yk = max(k for k in range(floors) if body(k, x, z))
            if s.free(x, 4 * yk + 5, z):
                s.set(x, 4 * yk + 5, z, B("potted_cactus") if (x + z) % 2 else B("potted_azalea_bush"))
    # vines on the side walls, bougainvillea cascading down the facade
    if vines:
        for z in range(bz0 + 1, bz1, 2):
            for y in range(2, 4):
                if body(0, bx0, z) and s.free(bx0 - 1, y, z):
                    s.set(bx0 - 1, y, z, B("vine", east="true", north="false", south="false",
                                           west="false", up="false"))
        for x in (bx0, bx1):
            if body(0, x, bz0):
                for y in range(2, 5):
                    if s.free(x, y, bz0 - 1):
                        s.set(x, y, bz0 - 1, BOUG())
    s.save(STRUCT / f"{name}.nbt")


# ---------------------------------------------------------------- special buildings
def bakery() -> None:
    W, D = 11, 9
    s = Rigid(W, 9, D)
    for x in range(W):
        s.set(x, 0, 0, path_block(x, 0))
    s.set(5, 1, 0, entrance_j())
    s.fills(1, 0, 1, 9, 0, 7, B("terracotta"))
    for y in range(1, 4):
        for x in range(1, 10):
            s.set(x, y, 1, B("smooth_sandstone")); s.set(x, y, 7, B("smooth_sandstone"))
        for z in range(1, 8):
            s.set(1, y, z, B("smooth_sandstone")); s.set(9, y, z, B("smooth_sandstone"))
    s.fills(1, 4, 1, 9, 4, 7, B("cut_sandstone"))
    for x in (2, 3, 7, 8):              # shop windows
        s.set(x, 1, 1, PANE()); s.set(x, 2, 1, PANE())
    DOOR2(s, 5, 1, 1, "spruce")
    for x in range(1, 10):              # striped awning
        s.set(x, 3, 0, B("blue_wool" if x % 2 else "white_wool"))
    # brick bread oven with chimney in the back-right corner
    s.fills(6, 1, 5, 8, 2, 6, B("bricks"))
    s.set(7, 1, 5, B("smoker", facing="north", lit="true"))
    s.set(7, 3, 6, B("bricks")); s.fills(7, 4, 6, 7, 6, 6, B("bricks"))
    s.set(7, 7, 6, B("campfire", facing="north", lit="false", signal_fire="false", waterlogged="false"))
    # counter, flour, loot
    s.fills(2, 1, 3, 4, 1, 3, SLAB("spruce", "top"))
    s.set(3, 2, 3, B("cake", bites="0"))
    s.set(2, 1, 5, BARREL("up", LOOT_BAKERY)); s.set(2, 1, 6, BARREL("up", LOOT_BAKERY))
    s.set(3, 1, 6, B("hay_block", axis="y")); s.set(4, 1, 6, CRAFT())
    s.set(5, 3, 4, HLANTERN())
    for x in range(1, 10):
        s.set(x, 5, 1, SLAB("sandstone")); s.set(x, 5, 7, SLAB("sandstone"))
    for z in range(2, 7):
        s.set(1, 5, z, SLAB("sandstone")); s.set(9, 5, z, SLAB("sandstone"))
    s.save(STRUCT / "bakery.nbt")


def fisherman_hut() -> None:
    W, D = 13, 9
    s = Rigid(W, 7, D)
    for x in range(W):
        s.set(x, 0, 0, path_block(x, 0))
    s.set(4, 1, 0, entrance_j())
    # hut x1..7, z1..7
    s.fills(1, 0, 1, 7, 0, 7, B("spruce_planks"))
    for y in range(1, 4):
        for x in range(1, 8):
            for z in (1, 7):
                s.set(x, y, z, B("stripped_spruce_log", axis="y") if x in (1, 7) else B("spruce_planks"))
        for z in range(2, 7):
            s.set(1, y, z, B("spruce_planks")); s.set(7, y, z, B("spruce_planks"))
    s.fills(0, 4, 0, 8, 4, 8, SLAB("spruce"))
    DOOR2(s, 4, 1, 1, "warped")
    for x in (2, 6):
        s.set(x, 2, 1, PANE())
        for sx_ in (x - 1, x + 1):
            s.set(sx_, 2, 0, SHUTTER("warped", "north"))
    s.set(1, 2, 4, PANE()); s.set(7, 2, 4, PANE())
    bed(s, 2, 1, 5, "blue")
    s.set(6, 1, 6, BARREL("up", LOOT_FISH)); s.set(6, 1, 5, BARREL("up", LOOT_FISH))
    s.set(6, 1, 2, CRAFT()); table(s, 4, 1, 4)
    s.set(4, 3, 4, HLANTERN())
    # yard: rowboat on trestles, net on poles, crates
    s.fills(8, 0, 1, 12, 0, 8, B("sand"))
    s.set(10, 1, 2, STAIR("south", "spruce")); s.set(10, 1, 7, STAIR("north", "spruce"))
    for z in range(3, 7):
        s.set(10, 1, z, SLAB("spruce"))
        s.set(9, 1, z, B("blue_concrete")); s.set(11, 1, z, B("blue_concrete"))
    s.set(10, 2, 4, SFENCE()); s.set(10, 3, 4, SFENCE())    # mast stub
    for z in (2, 6):
        s.set(12, 1, z, SFENCE()); s.set(12, 2, z, SFENCE())
    for z in range(3, 6):
        s.set(12, 2, z, B("cobweb"))                        # drying net
    s.set(8, 1, 7, BARREL("up", LOOT_FISH)); s.set(8, 1, 8, B("composter", level="3"))
    lamp(s, 8, 1)
    s.save(STRUCT / "fisherman_hut.nbt")


def market_stall() -> None:
    W, D = 11, 7
    s = Rigid(W, 6, D)
    for x in range(W):
        for z in range(D):
            s.set(x, 0, z, path_block(x, z))
    s.set(5, 1, 0, entrance_j())
    for x0, col in ((1, "red"), (6, "blue")):
        for x, z in ((x0, 2), (x0 + 3, 2), (x0, 5), (x0 + 3, 5)):
            s.set(x, 1, z, SFENCE()); s.set(x, 2, z, SFENCE())
        for x in range(x0, x0 + 4):
            for z in range(2, 6):
                s.set(x, 3, z, B(f"{col}_wool" if (x + z) % 2 else "white_wool"))
        for x in range(x0 + 1, x0 + 3):
            s.set(x, 1, 2, SLAB("spruce", "top"))
        s.set(x0 + 1, 1, 4, BARREL("up", LOOT_HOUSE))
        s.set(x0 + 2, 1, 4, B("melon") if x0 == 1 else B("pumpkin"))
        s.set(x0 + 1, 2, 2, B("decorated_pot", cracked="false", facing="north", waterlogged="false"))
        s.set(x0 + 2, 2, 2, B("potted_cactus") if x0 == 1 else B("potted_dandelion"))
    s.set(5, 1, 5, B("hay_block", axis="y")); s.set(5, 1, 4, B("composter", level="5"))
    s.save(STRUCT / "market_stall.nbt")


def olive_grove() -> None:
    W, D = 13, 12
    s = Rigid(W, 7, D)
    for x in range(W):
        for z in range(1, D):
            s.set(x, 0, z, B("coarse_dirt") if (x * 3 + z) % 4 == 0 else B("grass_block", snowy="false"))
    for x in range(5, 8):
        s.set(x, 0, 0, path_block(x, 0)); s.set(x, 0, 1, path_block(x, 1))
    s.set(6, 1, 0, entrance_j())
    for x in range(W):
        if x not in (5, 6, 7):
            s.set(x, 1, 1, WALL())
        s.set(x, 1, D - 1, WALL())
    for z in range(1, D):
        s.set(0, 1, z, WALL()); s.set(W - 1, 1, z, WALL())
    for x, z in ((2, 3), (8, 3), (2, 7), (8, 8), (5, 6)):
        olive(s, x, z)
    # olive press corner
    s.set(10, 1, 9, B("grindstone", face="floor", facing="north"))
    s.set(11, 1, 9, BARREL("up", LOOT_HOUSE)); s.set(11, 1, 10, B("composter", level="4"))
    s.set(10, 1, 10, B("hay_block", axis="y"))
    s.save(STRUCT / "olive_grove.nbt")


def chapel() -> None:
    """Small whitewashed chapel with a blue dome and a bell arch (Greek-island style)."""
    W, D = 9, 12
    s = Rigid(W, 13, D)
    for x in range(3, 6):
        s.set(x, 0, 0, path_block(x, 0))
    s.set(4, 1, 0, entrance_j())
    s.fills(1, 0, 1, 7, 0, 10, B("smooth_quartz"))
    for y in range(1, 5):
        for x in range(1, 8):
            s.set(x, y, 1, B("calcite")); s.set(x, y, 10, B("calcite"))
        for z in range(1, 11):
            s.set(1, y, z, B("calcite")); s.set(7, y, z, B("calcite"))
    s.fills(1, 5, 1, 7, 5, 10, B("smooth_quartz"))
    DOOR2(s, 4, 1, 1, "warped")
    for z in (3, 6, 9):
        s.set(1, 2, z, PANE("light_blue")); s.set(1, 3, z, PANE("light_blue"))
        s.set(7, 2, z, PANE("light_blue")); s.set(7, 3, z, PANE("light_blue"))
    s.set(4, 3, 1, PANE("blue"))
    # bell arch above the door
    for x, y in ((3, 6), (5, 6), (3, 7), (4, 7), (5, 7)):
        s.set(x, y, 1, B("calcite"))
    s.set(4, 8, 1, B("smooth_quartz_slab", type="bottom", waterlogged="false"))
    s.set(4, 6, 1, B("bell", attachment="ceiling", facing="north", powered="false"))
    # blue dome centred on z=6
    cz = 6
    for x in range(2, 7):
        for z in range(cz - 2, cz + 3):
            ring = abs(x - 4) == 2 or abs(z - cz) == 2
            corner = abs(x - 4) == 2 and abs(z - cz) == 2
            if ring and not corner:
                s.set(x, 6, z, B("white_concrete"))
            if not corner and (abs(x - 4) <= 1 or abs(z - cz) <= 1):
                s.set(x, 7, z, B("blue_concrete") if ring else AIR())
    for x in range(3, 6):
        for z in range(cz - 1, cz + 2):
            s.set(x, 8, z, B("blue_concrete"))
    s.set(4, 9, cz, B("blue_concrete"))
    s.set(4, 10, cz, B("white_concrete")); s.set(4, 11, cz, B("white_concrete"))
    s.set(3, 11, cz, B("white_concrete")); s.set(5, 11, cz, B("white_concrete"))
    # interior: pews facing the altar (south end), altar with candles
    for z in (3, 5, 7):
        for x in (2, 3, 5, 6):
            s.set(x, 1, z, STAIR("north", "spruce"))
    s.set(4, 1, 9, B("smooth_quartz")); s.set(3, 1, 9, B("smooth_quartz")); s.set(5, 1, 9, B("smooth_quartz"))
    s.set(3, 2, 9, B("candle", candles="3", lit="false", waterlogged="false"))
    s.set(5, 2, 9, B("candle", candles="2", lit="false", waterlogged="false"))
    s.set(4, 2, 9, B("lectern", facing="north", has_book="false", powered="false"))
    s.set(6, 1, 9, CHEST("west", LOOT_HOUSE))
    s.set(4, 4, 5, HLANTERN())
    for x in (0, 8):
        s.set(x, 1, 1, B("potted_red_tulip"))
    s.save(STRUCT / "chapel.nbt")


# ---------------------------------------------------------------- json
def update_structure_json() -> None:
    data = {
        "type": "minecraft:jigsaw",
        "biomes": ["israel_simulator:mediterranean_coast"],
        "step": "surface_structures",
        "spawn_overrides": {},
        "terrain_adaptation": "beard_thin",
        "start_pool": f"{NS}:mediterranean_village",
        "size": 6,
        # floor of the square (above its footing) lands on the surface
        "start_height": {"absolute": -Rigid.FOOTING},
        "project_start_to_heightmap": "WORLD_SURFACE_WG",
        "max_distance_from_center": 80,
        "use_expansion_hack": True,
    }
    STRUCTURE_JSON.write_text(json.dumps(data, indent=2) + "\n")
    print(f"wrote {STRUCTURE_JSON.relative_to(ROOT)}")


PROCESSOR = ROOT / "src/main/resources/data/israel_simulator/worldgen/processor_list/mediterranean_street.json"
STREET_PROCESSOR = f"{NS}:mediterranean_street"


def write_street_processor() -> None:
    """Like vanilla street_plains: lane blocks that land on water become a spruce boardwalk."""
    rules = [
        {
            "input_predicate": {"predicate_type": "minecraft:block_match", "block": f"minecraft:{b}"},
            "location_predicate": {"predicate_type": "minecraft:block_match", "block": "minecraft:water"},
            "output_state": {"Name": "minecraft:spruce_planks"},
        }
        for b in ("dirt_path", "gravel", "cobblestone", "coarse_dirt")
    ]
    PROCESSOR.parent.mkdir(parents=True, exist_ok=True)
    PROCESSOR.write_text(json.dumps({"processors": [{"processor_type": "minecraft:rule", "rules": rules}]},
                                    indent=2) + "\n")
    print(f"wrote {PROCESSOR.relative_to(ROOT)}")


def use_street_processor(pool_path: Path) -> None:
    data = json.loads(pool_path.read_text())
    for el in data["elements"]:
        el["element"]["processors"] = STREET_PROCESSOR
    pool_path.write_text(json.dumps(data, indent=2) + "\n")


def main() -> None:
    STRUCT.mkdir(parents=True, exist_ok=True)
    square(); path_straight(); path_junctions(); terminator()
    # six houses, different footprints / floors / materials
    house("house_small", 9, 9, 1, "calcite", beds=("light_blue",))
    house("house_terrace", 11, 11, 2, "white_concrete", upper=(1, 5, 9, 9), beds=("white", "blue"))
    house("house_courtyard", 13, 11, 1, "smooth_sandstone", cut=(1, 5, 5, 9), door="spruce",
          trim="cut_sandstone", beds=("yellow",))
    house("house_tall", 9, 11, 2, "smooth_quartz", beds=("cyan", "light_blue"), shutter="spruce")
    house("house_long", 13, 8, 1, "white_concrete", door="warped", beds=("blue",))
    house("house_step", 11, 10, 2, "calcite", upper=(5, 1, 9, 8), shutter="warped", door="spruce",
          beds=("white", "light_blue"))
    bakery(); fisherman_hut(); market_stall(); olive_grove(); chapel()
    update_structure_json()
    P = lambda n: f"{NS}:mediterranean/{n}"
    write_pool(START_POOL, [(P("square"), 1, "rigid")], "minecraft:empty")
    write_pool(POOL / "streets.json", [
        (P("path_straight"), 10, "terrain_matching"),
        (P("path_crossroad"), 3, "terrain_matching"),
        (P("path_corner"), 4, "terrain_matching"),
        (P("path_t"), 4, "terrain_matching"),
    ], P("terminators"))
    write_pool(POOL / "terminators.json", [(P("terminator"), 1, "terrain_matching")], "minecraft:empty")
    write_street_processor()
    use_street_processor(POOL / "streets.json")
    use_street_processor(POOL / "terminators.json")
    write_pool(POOL / "buildings.json", [(loc, w, "rigid") for loc, w in [
        (P("house_small"), 4), (P("house_terrace"), 3), (P("house_courtyard"), 3),
        (P("house_tall"), 3), (P("house_long"), 3), (P("house_step"), 3),
        (P("bakery"), 2), (P("fisherman_hut"), 2), (P("market_stall"), 2), (P("olive_grove"), 2)]],
        "minecraft:empty")
    write_pool(POOL / "landmarks.json", [(P("chapel"), 1, "rigid")], "minecraft:empty")


if __name__ == "__main__":
    main()
