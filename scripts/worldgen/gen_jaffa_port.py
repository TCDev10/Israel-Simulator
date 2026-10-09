#!/usr/bin/env python3
"""Generate Old Jaffa / Jaffa port as a jigsaw town (DataVersion 4903).

Layout (like jerusalem_city):
  * start piece  jaffa/clock_square (rigid): Ottoman clock tower + sabil fountain; the
    harbour (quay, pier, boats, breakwater, lighthouse) is guaranteed on its north side
  * alleys       stone alleys (terrain_matching) plus rigid stair and arch pieces
  * buildings    rigid stone houses and the flea market (Shuk HaPishpeshim), porch row z=0
                 with the entrance jigsaw at (cx,1,0) north_up
  Every rigid piece sits on a buried FOOTING-deep sandstone footing.

Run:  /workspace/nbtvenv/bin/python scripts/worldgen/gen_jaffa_port.py
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
STRUCT = ROOT / "src/main/resources/data/israel_simulator/structure/jaffa"
POOL = ROOT / "src/main/resources/data/israel_simulator/worldgen/template_pool/jaffa"
START_POOL = ROOT / "src/main/resources/data/israel_simulator/worldgen/template_pool/jaffa_port.json"
STRUCTURE_JSON = ROOT / "src/main/resources/data/israel_simulator/worldgen/structure/jaffa_port.json"

LOOT_HOUSE = f"{NS}:chests/jaffa_house"
LOOT_MARKET = f"{NS}:chests/jaffa_flea_market"
LOOT_HARBOUR = f"{NS}:chests/jaffa_harbour"


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
    """Old Jaffa paving: worn limestone and stone setts."""
    h = (x * 7349 + z * 9151) % 13
    if h < 5:
        return B("stone_bricks")
    if h < 8:
        return B("smooth_sandstone")
    if h < 10:
        return B("cracked_stone_bricks")
    if h < 12:
        return B("cut_sandstone")
    return B("mossy_stone_bricks")


# ---------------------------------------------------------------- rigid footing
class Rigid(Structure):
    """Rigid piece authored with the floor at y=0; shifted up by FOOTING with
    sandstone below the footprint so it never floats (same approach as Tel Aviv)."""

    FOOTING = 4

    def __init__(self, sx: int, sy: int, sz: int):
        self._ready = False
        super().__init__(sx, sy + self.FOOTING, sz)
        self._ready = True
        self.no_footing: set[tuple[int, int]] = set()   # columns over water (pier, boats)

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
                if (x, z) in self.no_footing:
                    continue
                if str(self.palette[self.blocks[(x, F, z)]]["Name"]) not in ("minecraft:air",
                                                                              "minecraft:structure_void"):
                    for y in range(F):
                        self.set(x, y, z, B("smooth_sandstone"))
        super().save(path)


# ---------------------------------------------------------------- jigsaws
def street_j(o):
    return jigsaw(o, "minecraft:street", "minecraft:street", f"{NS}:jaffa/streets",
                  "minecraft:structure_void")


def bld_j(o, pool="buildings"):
    return jigsaw(o, "minecraft:building_entrance", "minecraft:building_entrance",
                  f"{NS}:jaffa/{pool}", "minecraft:structure_void")


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


# ---------------------------------------------------------------- houses
def house(name: str, W: int, D: int, floors: int, mat: str, *, upper=None, cut=None,
          door="warped", shutter="warped", trim="smooth_quartz", beds=("light_blue",),
          loot=LOOT_HOUSE, pergola=True, vines=True, extra=None) -> None:
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
    if extra:
        extra(s)
    s.save(STRUCT / f"{name}.nbt")


# ---------------------------------------------------------------- Jaffa props
SS = lambda: B("smooth_sandstone")
CS = lambda: B("cut_sandstone")
CH = lambda: B("chiseled_sandstone")


def palm(s, x, z, y=1, trunk=5) -> None:
    for dy in range(trunk):
        s.set(x, y + dy, z, B("jungle_log", axis="y"))
    top = y + trunk
    s.set(x, top, z, LEAF("jungle"))
    for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        s.set(x + dx, top, z + dz, LEAF("jungle"))
        s.set(x + 2 * dx, top, z + 2 * dz, LEAF("jungle"))
        s.set(x + 2 * dx, top - 1, z + 2 * dz, LEAF("jungle"))
    for dx, dz in ((1, 1), (-1, 1), (1, -1), (-1, -1)):
        s.set(x + dx, top, z + dz, LEAF("jungle"))


def iron_bars():
    return B("iron_bars", north="false", south="false", east="false", west="false", waterlogged="false")


# ---------------------------------------------------------------- start: clock square
def clock_square() -> None:
    """Kikar HaShaon: the Ottoman clock tower (1903) and a sabil fountain."""
    s = Rigid(17, 24, 17)
    for x in range(17):
        for z in range(17):
            edge = x in (0, 16) or z in (0, 16)
            s.set(x, 0, z, B("stone_bricks") if edge else (CS() if (x + z) % 2 else SS()))
    # tower: tiers 5x5 (y1-9), 5x5 clock stage (y10-14), 3x3 belfry (y16-18), cap
    for y in range(1, 15):
        for x in range(6, 11):
            for z in range(6, 11):
                corner = x in (6, 10) and z in (6, 10)
                s.set(x, y, z, CH() if corner and y in (1, 9, 14) else (CS() if corner else SS()))
    for x in range(5, 12):          # cornices
        for z in range(5, 12):
            if x in (5, 11) or z in (5, 11):
                s.set(x, 9, z, B("smooth_sandstone_slab", type="top", waterlogged="false"))
                s.set(x, 15, z, B("smooth_sandstone_slab", type="bottom", waterlogged="false"))
    # arched windows (tier 1) and clock faces (tier 2) on all four sides
    for (x, z) in ((8, 6), (8, 10), (6, 8), (10, 8)):
        s.set(x, 5, z, PANE()); s.set(x, 6, z, PANE())
    for face in ("n", "s", "w", "e"):
        for a in (-1, 0, 1):
            for y in (11, 12, 13):
                x, z = {"n": (8 + a, 6), "s": (8 + a, 10), "w": (6, 8 + a), "e": (10, 8 + a)}[face]
                s.set(x, y, z, B("black_concrete") if (a, y) in ((0, 12), (0, 13)) else B("smooth_quartz"))
    s.fills(6, 15, 6, 10, 15, 10, SS())
    for y in (16, 17, 18):
        for x in range(7, 10):
            for z in range(7, 10):
                corner = x in (7, 9) and z in (7, 9)
                if corner or y == 18:
                    s.set(x, y, z, CS())
    s.set(8, 17, 8, B("bell", attachment="ceiling", facing="north", powered="false"))
    s.set(8, 19, 8, B("chiseled_sandstone"))
    s.set(8, 20, 8, B("lightning_rod", facing="up", powered="false", waterlogged="false"))
    for x, z in ((7, 7), (9, 7), (7, 9), (9, 9)):
        s.set(x, 19, z, B("sandstone_slab", type="bottom", waterlogged="false"))
    # sabil (public fountain); prismarine trim = framework marker for jaffa_port
    for x in range(1, 4):
        for z in range(12, 15):
            s.set(x, 1, z, B("prismarine_bricks") if (x, z) != (2, 13) else B("water_cauldron", level="3"))
    s.set(2, 2, 14, CS()); s.set(2, 3, 14, B("sandstone_slab", type="bottom", waterlogged="false"))
    for x, z in ((2, 2), (14, 2), (14, 14)):
        palm(s, x, z)
    for x, z, f in ((8, 3, "north"), (8, 13, "south"), (3, 8, "west"), (13, 8, "east")):
        s.set(x, 1, z, STAIR(f, "spruce"))
    for x, z in ((5, 1), (11, 15), (1, 6), (15, 11)):
        lamp(s, x, z)
    s.set(8, 1, 0, bld_j("north_up", "landmarks"))
    s.set(8, 1, 16, street_j("south_up"))
    s.set(0, 1, 8, street_j("west_up"))
    s.set(16, 1, 8, street_j("east_up"))
    s.save(STRUCT / "clock_square.nbt")


# ---------------------------------------------------------------- alleys
def alley_straight() -> None:
    s = Structure(11, 4, 5)
    for x in range(11):
        for z in range(5):
            s.set(x, 0, z, path_block(x, z))
    lamp(s, 5, 0)
    s.set(5, 1, 4, B("decorated_pot", cracked="true", facing="north", waterlogged="false"))
    s.set(0, 1, 2, street_j("west_up")); s.set(10, 1, 2, street_j("east_up"))
    for x in (2, 8):
        s.set(x, 1, 0, bld_j("north_up")); s.set(x, 1, 4, bld_j("south_up"))
    s.save(STRUCT / "alley_straight.nbt")


def alley_arch() -> None:
    """Vaulted passage typical of the old city (rigid so the arch stays square)."""
    s = Rigid(9, 7, 5)
    for x in range(9):
        for z in range(5):
            s.set(x, 0, z, path_block(x, z))
    for x in (3, 5):
        for z in (0, 4):
            for y in range(1, 4):
                s.set(x, y, z, CS())
    for x in range(3, 6):
        for z in range(5):
            s.set(x, 4, z, SS())
        s.set(x, 3, 1, B("sandstone_stairs", facing="north", half="top", shape="straight", waterlogged="false"))
        s.set(x, 3, 3, B("sandstone_stairs", facing="south", half="top", shape="straight", waterlogged="false"))
        s.set(x, 5, 0, B("sandstone_slab", type="bottom", waterlogged="false"))
        s.set(x, 5, 4, B("sandstone_slab", type="bottom", waterlogged="false"))
    s.set(4, 3, 2, HLANTERN())
    for z in (0, 4):
        s.set(4, 1, z, CS()); s.set(4, 2, z, CS()); s.set(4, 3, z, CS())
    s.fills(4, 5, 1, 4, 5, 3, BOUG())
    s.set(0, 1, 2, street_j("west_up")); s.set(8, 1, 2, street_j("east_up"))
    s.save(STRUCT / "alley_arch.nbt")


def alley_stairs() -> None:
    """Stone stair lane climbing 4 blocks (rigid; next lane follows the terrain again)."""
    s = Rigid(9, 8, 5)
    h = lambda x: 0 if x <= 1 else (x - 1 if x <= 5 else 4)
    for x in range(9):
        top = h(x)
        for z in range(5):
            for y in range(0, top):
                s.set(x, y, z, CS())
            if z in (0, 4):
                s.set(x, top, z, CS())
                s.set(x, top + 1, z, B("sandstone_wall", north="none", south="none", east="none",
                                        west="none", up="true", waterlogged="false"))
            elif 2 <= x <= 5:
                s.set(x, top, z, B("stone_brick_stairs", facing="east", half="bottom", shape="straight",
                                   waterlogged="false"))
                if top:
                    s.set(x, top - 1, z, B("stone_bricks"))
            else:
                s.set(x, top, z, path_block(x, z))
    s.set(7, 6, 0, LANTERN()); s.set(1, 2, 4, LANTERN())
    s.set(0, 1, 2, street_j("west_up"))
    s.set(8, 5, 2, street_j("east_up"))
    s.save(STRUCT / "alley_stairs.nbt")


def _junction(arms: set[str], name: str) -> None:
    s = Structure(5, 4, 5)
    for x in range(5):
        for z in range(5):
            s.set(x, 0, z, path_block(x, z))
    for o, x, z, a in (("north_up", 2, 0, "n"), ("south_up", 2, 4, "s"),
                       ("west_up", 0, 2, "w"), ("east_up", 4, 2, "e")):
        s.set(x, 1, z, street_j(o) if a in arms else bld_j(o))
    s.save(STRUCT / f"{name}.nbt")


def alley_junctions() -> None:
    _junction({"n", "s", "w", "e"}, "alley_crossroad")
    _junction({"n", "e"}, "alley_corner")
    _junction({"w", "e", "s"}, "alley_t")


def terminator() -> None:
    s = Structure(3, 3, 5)
    for x in range(3):
        for z in range(5):
            s.set(x, 0, z, path_block(x, z))
    s.set(2, 1, 2, B("decorated_pot", cracked="false", facing="west", waterlogged="false"))
    s.set(0, 1, 2, end_j("west_up"))
    s.save(STRUCT / "terminator.nbt")


# ---------------------------------------------------------------- buildings
def small_dome(s) -> None:
    """Little dome on the roof (houses with domed rooms are typical of Old Jaffa)."""
    y = 9
    for x in range(3, 6):
        for z in range(4, 7):
            s.set(x, y, z, SS())
    s.set(4, y + 1, 5, SS())
    for x, z in ((3, 5), (5, 5), (4, 4), (4, 6)):
        s.set(x, y + 1, z, B("smooth_sandstone_slab", type="bottom", waterlogged="false"))


def gallery_front(s) -> None:
    """Ground-floor art gallery: wide display windows and paintings stand-ins."""
    for x in (2, 3, 7, 8):
        s.set(x, 1, 1, PANE()); s.set(x, 2, 1, PANE())
    s.set(2, 3, 0, B("blue_wool")); s.set(3, 3, 0, B("blue_wool"))
    s.set(7, 3, 0, B("blue_wool")); s.set(8, 3, 0, B("blue_wool"))


def flea_market() -> None:
    """Shuk HaPishpeshim: arcade of stone arches, stalls full of junk, rugs, lanterns."""
    W, D = 15, 11
    s = Rigid(W, 7, D)
    for x in range(W):
        for z in range(D):
            s.set(x, 0, z, path_block(x, z))
    s.set(7, 1, 0, entrance_j())
    # arcade z7..10
    for x in range(W):
        for z in range(7, 11):
            s.set(x, 4, z, CS())
        for y in range(1, 4):
            s.set(x, y, 10, SS())
        s.set(x, 5, 7, B("sandstone_slab", type="bottom", waterlogged="false"))
    for z in range(7, 11):
        for y in range(1, 4):
            s.set(0, y, z, SS()); s.set(W - 1, y, z, SS())
    for px in (1, 4, 7, 10, 13):
        for y in range(1, 4):
            s.set(px, y, 7, CS())
    for px in (1, 4, 7, 10):
        s.set(px + 1, 3, 7, B("sandstone_stairs", facing="west", half="top", shape="straight", waterlogged="false"))
        s.set(px + 2, 3, 7, B("sandstone_stairs", facing="east", half="top", shape="straight", waterlogged="false"))
    junk = [B("jukebox", has_record="false"), B("bookshelf"), B("anvil", facing="north"),
            B("lectern", facing="south", has_book="false", powered="false"), B("cauldron"),
            B("decorated_pot", cracked="true", facing="south", waterlogged="false"),
            B("potted_cactus"), B("grindstone", face="floor", facing="south"), B("loom", facing="south")]
    for i, x in enumerate(range(1, 14)):
        if x in (1, 4, 7, 10, 13):
            s.set(x, 1, 9, BARREL("up", LOOT_MARKET))
        else:
            s.set(x, 1, 9, junk[i % len(junk)])
    s.set(3, 1, 8, CHEST("south", LOOT_MARKET)); s.set(11, 1, 8, CHEST("south", LOOT_MARKET))
    for x in (2, 6, 9, 12):
        s.set(x, 3, 9, HLANTERN())
    # open-air part: rugs, two awninged tables, old chairs
    rugs = ("red", "orange", "blue", "purple")
    for i, (x0, z0) in enumerate(((1, 2), (5, 3), (10, 2))):
        for x in range(x0, x0 + 3):
            for z in range(z0, z0 + 2):
                s.set(x, 1, z, B(f"{rugs[(i + x + z) % 4]}_carpet"))
    for x0, col in ((2, "red"), (10, "blue")):
        for x, z in ((x0, 4), (x0 + 2, 4), (x0, 6), (x0 + 2, 6)):
            s.set(x, 1, z, SFENCE()); s.set(x, 2, z, SFENCE())
        for x in range(x0, x0 + 3):
            for z in range(4, 7):
                s.set(x, 3, z, B(f"{col}_wool" if (x + z) % 2 else "white_wool"))
        s.set(x0 + 1, 1, 5, SLAB("spruce", "top"))
        s.set(x0 + 1, 2, 5, B("decorated_pot", cracked="false", facing="north", waterlogged="false"))
    s.set(7, 1, 5, STAIR("south", "dark_oak")); s.set(8, 1, 5, STAIR("south", "spruce"))
    s.save(STRUCT / "flea_market.nbt")


def boat(s, x0: int, z0: int, color: str) -> None:
    """Small Jaffa fishing boat (blocks) floating in the basin; hull at y=-1."""
    L = 7
    for i in range(L):
        z = z0 + i
        if i == 0:
            s.set(x0 + 1, -1, z, B("spruce_planks"))
            s.set(x0 + 1, 0, z, B("spruce_stairs", facing="north", half="bottom", shape="straight",
                                   waterlogged="false"))
            cells = [x0 + 1]
        else:
            cells = [x0, x0 + 1, x0 + 2]
            for x in cells:
                s.set(x, -1, z, B("spruce_planks"))
            s.set(x0, 0, z, B(f"{color}_concrete") if i % 2 else B("white_concrete"))
            s.set(x0 + 2, 0, z, B(f"{color}_concrete") if i % 2 else B("white_concrete"))
            if i == L - 1:
                s.set(x0 + 1, 0, z, B(f"{color}_concrete"))
        for x in cells:
            s.no_footing.add((x, z))
    s.set(x0 + 1, 0, z0 + 3, SLAB("spruce"))
    for y in range(1, 5):
        s.set(x0 + 1, y, z0 + 2, SFENCE())
    for y in range(2, 5):
        s.set(x0 + 1, y, z0 + 3, B("white_wool"))
    s.set(x0 + 1, 0, z0 + 5, BARREL("up", LOOT_HARBOUR))


def harbour() -> None:
    """Quay, fish stall, wooden pier, two fishing boats, breakwater and the lighthouse."""
    W, D = 23, 28
    s = Rigid(W, 21, D)
    # quay (z0..6)
    for x in range(W):
        for z in range(0, 7):
            s.set(x, 0, z, path_block(x, z))
    s.set(11, 1, 0, entrance_j())
    # basin x1..18, z7..26 ; walls around; breakwater x19..22
    for x in range(W):
        for z in range(7, D):
            basin = 1 <= x <= 18 and 7 <= z <= D - 2
            if basin:
                s.set(x, -4, z, B("sand") if (x + z) % 3 else B("gravel"))
                for y in (-3, -2, -1):
                    s.set(x, y, z, B("water", level="0"))
            else:
                for y in range(-4, 1):
                    s.set(x, y, z, B("stone_bricks") if y == 0 or x >= 19 else B("cobblestone"))
    for x in range(1, 19):                     # quay edge
        s.set(x, 0, 6, B("smooth_stone"))
    for x in (2, 6, 14, 17):
        s.set(x, 1, 6, B("stone_brick_wall", north="none", south="none", east="none", west="none",
                         up="true", waterlogged="false"))
    # quay life: nets, crates, fish stall, anchor, lamps
    for x in (1, 4):
        s.set(x, 1, 2, SFENCE()); s.set(x, 2, 2, SFENCE())
    s.set(2, 2, 2, B("cobweb")); s.set(3, 2, 2, B("cobweb"))
    s.set(1, 1, 4, BARREL("up", LOOT_HARBOUR)); s.set(2, 1, 4, BARREL("up", LOOT_HARBOUR))
    s.set(1, 1, 5, B("hay_block", axis="x"))
    for x, z in ((14, 2), (16, 2), (14, 4), (16, 4)):
        s.set(x, 1, z, SFENCE()); s.set(x, 2, z, SFENCE())
    for x in range(14, 17):
        for z in range(2, 5):
            s.set(x, 3, z, B("blue_wool" if (x + z) % 2 else "white_wool"))
    s.set(15, 1, 2, SLAB("spruce", "top")); s.set(15, 1, 3, CHEST("north", LOOT_HARBOUR))
    s.set(19, 1, 3, B("anvil", facing="north"))
    lamp(s, 8, 5); lamp(s, 13, 5); lamp(s, 21, 1)
    # pier x8..10, z7..20 with posts and railings
    for z in range(7, 21):
        for x in range(8, 11):
            s.set(x, 0, z, B("spruce_planks"))
            s.no_footing.add((x, z))
        for x in (8, 10):
            if z % 4 != 2:
                s.set(x, 1, z, SFENCE())
        if z % 4 == 0:
            for x in (8, 10):
                for y in range(-4, 0):
                    s.set(x, y, z, B("spruce_log", axis="y"))
    s.set(8, 2, 12, LANTERN()); s.set(10, 2, 16, LANTERN()); s.set(9, 1, 20, BARREL("up", LOOT_HARBOUR))
    boat(s, 3, 10, "blue")
    boat(s, 13, 12, "red")
    # lighthouse on the breakwater head (x18..22, z22..26)
    for x in range(18, 23):
        for z in range(22, 27):
            for y in range(-4, 1):
                s.set(x, y, z, B("stone_bricks"))
            s.no_footing.discard((x, z))
    for y in range(1, 13):
        for x in range(19, 22):
            for z in range(23, 26):
                if (x, z) != (20, 24):
                    s.set(x, y, z, SS() if y % 4 else CS())
    s.set(20, 1, 23, B("spruce_door", facing="north", half="lower", hinge="left", open="false", powered="false"))
    s.set(20, 2, 23, B("spruce_door", facing="north", half="upper", hinge="left", open="false", powered="false"))
    s.set(20, 7, 23, PANE()); s.set(21, 9, 24, PANE())
    for y in range(1, 16):
        s.set(20, y, 24, LADDER("north"))
    for x in range(18, 23):                      # gallery
        for z in range(22, 27):
            if not (19 <= x <= 21 and 23 <= z <= 25):
                s.set(x, 13, z, B("stone_brick_slab", type="top", waterlogged="false"))
                if x in (18, 22) or z in (22, 26):
                    s.set(x, 14, z, iron_bars())
            elif (x, z) != (20, 24):
                s.set(x, 13, z, CS())
    for y in (14, 15, 16):                       # lantern room
        for x in range(19, 22):
            for z in range(23, 26):
                if (x, z) == (20, 24):
                    continue
                s.set(x, y, z, B("glass"))
    s.set(20, 14, 23, AIR()); s.set(20, 15, 23, AIR())
    s.set(20, 16, 24, B("sea_lantern"))
    s.fills(19, 17, 23, 21, 17, 25, B("oxidized_cut_copper"))
    s.set(20, 18, 24, B("oxidized_copper"))
    s.set(20, 19, 24, B("lightning_rod", facing="up", powered="false", waterlogged="false"))
    s.save(STRUCT / "harbour.nbt")


# ---------------------------------------------------------------- json
def update_structure_json() -> None:
    data = {
        "type": "minecraft:jigsaw",
        "biomes": ["israel_simulator:mediterranean_coast"],
        "step": "surface_structures",
        "spawn_overrides": {},
        "terrain_adaptation": "beard_thin",
        "start_pool": f"{NS}:jaffa_port",
        "size": 6,
        "start_height": {"absolute": -Rigid.FOOTING},
        "project_start_to_heightmap": "WORLD_SURFACE_WG",
        "max_distance_from_center": 80,
        "use_expansion_hack": True,
    }
    STRUCTURE_JSON.write_text(json.dumps(data, indent=2) + "\n")
    print(f"wrote {STRUCTURE_JSON.relative_to(ROOT)}")


def main() -> None:
    STRUCT.mkdir(parents=True, exist_ok=True)
    clock_square(); alley_straight(); alley_arch(); alley_stairs(); alley_junctions(); terminator()
    house("house_a", 9, 10, 2, "smooth_sandstone", door="spruce", shutter="spruce",
          trim="cut_sandstone", beds=("red", "orange"))
    house("house_b", 11, 11, 3, "sandstone", upper=(1, 4, 9, 9), door="warped", shutter="warped",
          trim="cut_sandstone", beds=("white", "blue", "cyan"))
    house("house_c", 9, 9, 2, "cut_sandstone", door="spruce", shutter="warped", trim="smooth_sandstone",
          beds=("brown", "yellow"), pergola=False, extra=small_dome)
    house("gallery_house", 11, 9, 2, "smooth_sandstone", door="warped", shutter="spruce",
          trim="cut_sandstone", beds=("purple",), extra=gallery_front)
    flea_market(); harbour()
    update_structure_json()
    P = lambda n: f"{NS}:jaffa/{n}"
    write_pool(START_POOL, [(P("clock_square"), 1, "rigid")], "minecraft:empty")
    write_pool(POOL / "streets.json", [
        (P("alley_straight"), 10, "terrain_matching"),
        (P("alley_crossroad"), 3, "terrain_matching"),
        (P("alley_corner"), 4, "terrain_matching"),
        (P("alley_t"), 4, "terrain_matching"),
        (P("alley_arch"), 3, "rigid"),
        (P("alley_stairs"), 3, "rigid"),
    ], P("terminators"))
    write_pool(POOL / "terminators.json", [(P("terminator"), 1, "terrain_matching")], "minecraft:empty")
    write_pool(POOL / "buildings.json", [
        (P("house_a"), 4, "rigid"), (P("house_b"), 3, "rigid"), (P("house_c"), 3, "rigid"),
        (P("gallery_house"), 3, "rigid"), (P("flea_market"), 3, "rigid"),
    ], "minecraft:empty")
    write_pool(POOL / "landmarks.json", [(P("harbour"), 1, "rigid")], "minecraft:empty")


if __name__ == "__main__":
    main()
