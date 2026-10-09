#!/usr/bin/env python3
"""Generate the Tel Aviv jigsaw city (DataVersion 4903).

Village-style layout mirroring gen_jerusalem_city.py:
  * start piece  tel_aviv/square (Dizengoff Square, rigid) -> streets on 3 sides,
    a guaranteed glass high-rise on the north side (tel_aviv/landmarks)
  * streets      terrain_matching, floor y=0, jigsaws at y=1
  * buildings    rigid, porch row z=0 with the entrance jigsaw at (cx,1,0) north_up

Run:  /workspace/nbtvenv/bin/python scripts/worldgen/gen_tel_aviv_city.py
"""
from __future__ import annotations

import json
import math
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from gen_jerusalem_city import (  # noqa: E402
    BARREL, BED, CHEST, CRAFT, DOOR, FENCE, GP, HLANTERN, KNOWN, LANTERN, POT, ROOT,
    Structure, blk, jigsaw, write_pool,
)

if not KNOWN:
    raise SystemExit("blocks.json report missing (/workspace/mcreports/out/reports/blocks.json)")

NS = "israel_simulator"
STRUCT = ROOT / "src/main/resources/data/israel_simulator/structure/tel_aviv"
POOL = ROOT / "src/main/resources/data/israel_simulator/worldgen/template_pool/tel_aviv"
START_POOL = ROOT / "src/main/resources/data/israel_simulator/worldgen/template_pool/tel_aviv_city.json"
STRUCTURE_JSON = ROOT / "src/main/resources/data/israel_simulator/worldgen/structure/tel_aviv_city.json"

LOOT_APT = f"{NS}:chests/tel_aviv_apartment"
LOOT_TECH = f"{NS}:chests/tel_aviv_tech_office"
LOOT_KIOSK = f"{NS}:chests/tel_aviv_kiosk"

# ---------------------------------------------------------------- blocks
def B(name: str, **p: str):
    return blk(f"minecraft:{name}", **p)

ASPH = lambda: B("gray_concrete")
LINE = lambda: B("white_concrete")
WALK = lambda: B("light_gray_concrete")          # sidewalk tiles
WHITE = lambda: B("white_concrete")
QUARTZ = lambda: B("smooth_quartz")
SLABF = lambda: B("smooth_stone")                  # interior floor slab
GRASS = lambda: B("grass_block", snowy="false")
SAND = lambda: B("sand")
BRICK = lambda: B("bricks")
LAMP_POST = lambda: B("polished_blackstone_wall", east="none", west="none", north="none",
                      south="none", up="true", waterlogged="false")
LOG = lambda axis="y": B("jungle_log", axis=axis)
LEAF = lambda: B("jungle_leaves", distance="1", persistent="true", waterlogged="false")
OLEAF = lambda: B("oak_leaves", distance="1", persistent="true", waterlogged="false")
PLATE = lambda: B("oak_pressure_plate", powered="false")
SLAB_TOP = lambda wood="spruce": B(f"{wood}_slab", type="top", waterlogged="false")
LADDER = lambda facing="north": B("ladder", facing=facing, waterlogged="false")


def STAIR(facing: str, mat: str = "oak", half: str = "bottom"):
    return B(f"{mat}_stairs", facing=facing, half=half, shape="straight", waterlogged="false")


def PANE(color: str | None = None):
    name = f"{color}_stained_glass_pane" if color else "glass_pane"
    return B(name, north="false", south="false", east="false", west="false", waterlogged="false")


def CURB(i: int, color: str):
    # Israeli kerb paint: red/white = no parking, blue/white = paid parking
    return B(f"{color}_concrete") if i % 2 == 0 else WHITE()


class Rigid(Structure):
    """Rigid piece with a buried footing.

    Pieces are authored with the floor at y=0 (jigsaws at y=1, matching the streets);
    everything is shifted up by FOOTING and the footprint gets FOOTING layers of
    sandstone below it, so slopes/ravines up to ~4 blocks (plus beard_thin) never leave
    a floating floor. On flat ground the footing is simply underground.
    """

    FOOTING = 4

    def __init__(self, sx: int, sy: int, sz: int):
        self._ready = False
        super().__init__(sx, sy + self.FOOTING, sz)
        self._ready = True

    def set(self, x: int, y: int, z: int, entry) -> None:
        super().set(x, y + self.FOOTING if self._ready else y, z, entry)

    def save(self, path: Path) -> None:
        self._ready = False
        F = self.FOOTING
        for x in range(self.sx):
            for z in range(self.sz):
                name = str(self.palette[self.blocks[(x, F, z)]]["Name"])
                if name not in ("minecraft:air", "minecraft:structure_void"):
                    for y in range(F):
                        self.set(x, y, z, B("smooth_sandstone"))
        super().save(path)


# ---------------------------------------------------------------- jigsaws
def street_j(orient: str):
    return jigsaw(orient, "minecraft:street", "minecraft:street", f"{NS}:tel_aviv/streets",
                  "minecraft:structure_void")


def bld_j(orient: str, pool: str = "buildings"):
    return jigsaw(orient, "minecraft:building_entrance", "minecraft:building_entrance",
                  f"{NS}:tel_aviv/{pool}", "minecraft:structure_void")


def entrance_j():
    return jigsaw("north_up", "minecraft:building_entrance", "minecraft:building_entrance",
                  "minecraft:empty", "minecraft:air")


def end_j(orient: str):
    return jigsaw(orient, "minecraft:street", "minecraft:street", "minecraft:empty",
                  "minecraft:structure_void")


# ---------------------------------------------------------------- props
def lamp(s: Structure, x: int, z: int, y: int = 1, h: int = 3) -> None:
    for dy in range(h):
        s.set(x, y + dy, z, LAMP_POST())
    s.set(x, y + h, z, LANTERN())


def tree(s: Structure, x: int, z: int, y: int = 1, trunk: int = 3, leaf=LEAF) -> None:
    """Small street tree (ficus-ish): trunk + 3x3 crown + cap."""
    for dy in range(trunk):
        s.set(x, y + dy, z, LOG())
    top = y + trunk
    for dx in (-1, 0, 1):
        for dz in (-1, 0, 1):
            if 0 <= x + dx < s.sx and 0 <= z + dz < s.sz:
                s.set(x + dx, top - 1 if (dx or dz) else top - 1, z + dz,
                      leaf() if (dx or dz) else LOG())
                s.set(x + dx, top, z + dz, leaf())
    s.set(x, top + 1, z, leaf())


def palm(s: Structure, x: int, z: int, y: int = 1, trunk: int = 5) -> None:
    for dy in range(trunk):
        s.set(x, y + dy, z, LOG())
    top = y + trunk
    s.set(x, top, z, LEAF())
    for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        s.set(x + dx, top, z + dz, LEAF())
        s.set(x + 2 * dx, top, z + 2 * dz, LEAF())
        s.set(x + 2 * dx, top - 1, z + 2 * dz, LEAF())   # drooping frond tips
    for dx, dz in ((1, 1), (-1, 1), (1, -1), (-1, -1)):
        s.set(x + dx, top, z + dz, LEAF())


def table(s: Structure, x: int, y: int, z: int, chairs: str = "x") -> None:
    s.set(x, y, z, FENCE())
    s.set(x, y + 1, z, PLATE())
    if chairs == "x":
        s.set(x - 1, y, z, STAIR("west"))   # back to the west, facing the table
        s.set(x + 1, y, z, STAIR("east"))
    else:
        s.set(x, y, z - 1, STAIR("north"))
        s.set(x, y, z + 1, STAIR("south"))


def bed(s: Structure, x: int, y: int, z: int, color: str = "white", facing: str = "south") -> None:
    dz = {"south": 1, "north": -1}.get(facing, 0)
    dx = {"east": 1, "west": -1}.get(facing, 0)
    s.set(x, y, z, B(f"{color}_bed", facing=facing, part="foot", occupied="false"))
    s.set(x + dx, y, z + dz, B(f"{color}_bed", facing=facing, part="head", occupied="false"))


def solar_heater(s: Structure, x: int, y: int, z: int) -> None:
    """Dud shemesh: collector panels + horizontal white tank behind (south)."""
    s.set(x, y, z, B("daylight_detector", inverted="false", power="0"))
    s.set(x + 1, y, z, B("daylight_detector", inverted="false", power="0"))
    s.set(x, y, z + 1, B("quartz_pillar", axis="x"))
    s.set(x + 1, y, z + 1, B("quartz_pillar", axis="x"))


def ladder_shaft(s: Structure, x: int, z: int, top: int, facing: str = "north") -> None:
    """Ladder from y=1 up to `top` (inclusive), cutting holes in floor slabs."""
    for y in range(1, top + 1):
        s.set(x, y, z, LADDER(facing))


def apartment(s: Structure, x0: int, z0: int, x1: int, z1: int, y: int, *,
              bed_color: str = "white", loot: str | None = None, avoid=()) -> None:
    """Minimal flat: bed at the back-left, table in the middle, chest front-left."""
    bed(s, x0, y, z1 - 1 - 1, bed_color, "south")
    cx, cz = (x0 + x1) // 2, (z0 + z1) // 2
    if (cx, cz) not in avoid:
        table(s, cx, y, cz)
    if (x1, z0 + 1) not in avoid:
        s.set(x1, y, z0 + 1, CRAFT())
    if loot:
        s.set(x0, y, z0 + 1, CHEST("east", loot))
    else:
        s.set(x0, y, z0 + 1, B("bookshelf"))
    s.set(x0, y, cz + 1 if (x0, cz + 1) not in avoid else cz, POT())
    s.set(cx, y + 2, cz, HLANTERN())


def office(s: Structure, x0: int, z0: int, x1: int, z1: int, y: int, *, loot: str | None,
           avoid=()) -> None:
    """Open-space office: rows of desks with monitors and chairs."""
    for z in range(z0 + 1, z1, 3):
        for x in range(x0 + 1, x1, 3):
            if (x, z) in avoid or (x, z + 1) in avoid:
                continue
            s.set(x, y, z, SLAB_TOP("spruce"))
            s.set(x, y + 1, z, B("black_stained_glass_pane", north="false", south="false",
                                  east="false", west="false", waterlogged="false"))
            s.set(x, y, z + 1, STAIR("south", "dark_oak"))
    if loot:
        s.set(x0, y, z1, CHEST("east", loot))
    s.set(x1, y, z0, B("potted_bamboo"))
    s.set((x0 + x1) // 2, y + 2, (z0 + z1) // 2, B("sea_lantern"))


# ---------------------------------------------------------------- start piece
def square() -> None:
    """Dizengoff Square with an Agam-style 'Fire and Water' colour fountain."""
    s = Rigid(17, 9, 17)
    s.fills(0, 0, 0, 16, 0, 16, WALK())
    c = 8
    for x in range(17):
        for z in range(17):
            d = math.hypot(x - c, z - c)
            if d <= 2.5:
                s.set(x, 0, z, B("smooth_stone"))
            elif 4.5 <= d <= 5.5:
                s.set(x, 0, z, B("white_terracotta"))
            elif 6.5 <= d <= 7.4:
                s.set(x, 0, z, GRASS())
    # fountain: water cauldrons ring + stacked coloured "gears"
    for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        s.set(c + dx, 1, c + dz, B("water_cauldron", level="3"))
    for dx, dz in ((1, 1), (-1, 1), (1, -1), (-1, -1)):
        s.set(c + dx, 1, c + dz, B("smooth_stone_slab", type="bottom", waterlogged="false"))
    for i, col in enumerate(("red_concrete", "orange_concrete", "yellow_concrete", "lime_concrete",
                             "light_blue_stained_glass", "blue_concrete")):
        s.set(c, 1 + i, c, B(col))   # light_blue_stained_glass = framework marker
    s.set(c, 7, c, LANTERN())
    # benches facing the fountain
    s.set(c, 1, c - 4, STAIR("north")); s.set(c, 1, c + 4, STAIR("south"))
    s.set(c - 4, 1, c, STAIR("west")); s.set(c + 4, 1, c, STAIR("east"))
    for x, z in ((3, 3), (13, 3), (3, 13), (13, 13)):
        tree(s, x, z)
    for x, z in ((5, 2), (11, 14), (2, 11), (14, 5)):
        lamp(s, x, z)
    for x, z in ((6, 4), (10, 4), (4, 10), (12, 10)):
        s.set(x, 1, z, B("potted_red_tulip"))
    s.set(c, 1, 0, bld_j("north_up", "landmarks"))
    s.set(c, 1, 16, street_j("south_up"))
    s.set(0, 1, c, street_j("west_up"))
    s.set(16, 1, c, street_j("east_up"))
    s.save(STRUCT / "square.nbt")


# ---------------------------------------------------------------- streets
def street_straight() -> None:
    s = Structure(15, 5, 9)
    for x in range(15):
        s.set(x, 0, 0, WALK()); s.set(x, 0, 8, WALK())
        s.set(x, 0, 1, CURB(x, "red")); s.set(x, 0, 7, CURB(x, "blue"))
        for z in range(2, 7):
            s.set(x, 0, z, ASPH())
        if x % 4 in (1, 2):
            s.set(x, 0, 4, LINE())
    lamp(s, 7, 0); lamp(s, 7, 8)
    s.set(0, 1, 4, street_j("west_up")); s.set(14, 1, 4, street_j("east_up"))
    for x in (3, 11):
        s.set(x, 1, 0, bld_j("north_up")); s.set(x, 1, 8, bld_j("south_up"))
    s.save(STRUCT / "street_straight.nbt")


def _junction(arms: set[str]) -> Structure:
    """9x9 junction; asphalt band x/z 2..6 along each arm, sidewalks elsewhere."""
    s = Structure(9, 5, 9)
    s.fills(0, 0, 0, 8, 0, 8, WALK())
    s.fills(2, 0, 2, 6, 0, 6, ASPH())
    if "n" in arms: s.fills(2, 0, 0, 6, 0, 1, ASPH())
    if "s" in arms: s.fills(2, 0, 7, 6, 0, 8, ASPH())
    if "w" in arms: s.fills(0, 0, 2, 1, 0, 6, ASPH())
    if "e" in arms: s.fills(7, 0, 2, 8, 0, 6, ASPH())
    # zebra crossings on each arm
    for i in range(2, 7):
        if i % 2 == 0:
            if "n" in arms: s.set(i, 0, 1, LINE())
            if "s" in arms: s.set(i, 0, 7, LINE())
            if "w" in arms: s.set(1, 0, i, LINE())
            if "e" in arms: s.set(7, 0, i, LINE())
    # kerbs where a side has no arm
    if "n" not in arms:
        for x in range(9): s.set(x, 0, 1, CURB(x, "red"))
    if "s" not in arms:
        for x in range(9): s.set(x, 0, 7, CURB(x, "blue"))
    if "w" not in arms:
        for z in range(9): s.set(1, 0, z, CURB(z, "red"))
    if "e" not in arms:
        for z in range(9): s.set(7, 0, z, CURB(z, "blue"))
    for o, x, z, a in (("north_up", 4, 0, "n"), ("south_up", 4, 8, "s"),
                       ("west_up", 0, 4, "w"), ("east_up", 8, 4, "e")):
        s.set(x, 1, z, street_j(o) if a in arms else bld_j(o))
    return s


def street_crossroad() -> None:
    s = _junction({"n", "s", "w", "e"})
    for x, z in ((0, 0), (8, 8)):
        lamp(s, x, z)
    # traffic light pole at the other corners
    for x, z in ((8, 0), (0, 8)):
        for y in (1, 2, 3):
            s.set(x, y, z, LAMP_POST())
        s.set(x, 4, z, B("redstone_lamp", lit="false"))
    s.save(STRUCT / "street_crossroad.nbt")


def street_corner() -> None:
    s = _junction({"n", "e"})
    lamp(s, 0, 8)
    s.set(8, 1, 8, B("potted_cactus"))
    s.save(STRUCT / "street_corner.nbt")


def street_t() -> None:
    s = _junction({"w", "e", "s"})
    lamp(s, 0, 0); lamp(s, 8, 0)
    s.save(STRUCT / "street_t.nbt")


def rothschild() -> None:
    """Rothschild Boulevard: two carriageways, tree-lined central walkway, bike lane, kiosk."""
    s = Structure(15, 8, 17)
    for x in range(15):
        s.set(x, 0, 0, WALK()); s.set(x, 0, 16, WALK())
        for z in list(range(1, 5)) + list(range(12, 16)):
            s.set(x, 0, z, ASPH())
        if x % 4 in (1, 2):
            s.set(x, 0, 3, LINE()); s.set(x, 0, 13, LINE())
        median = x not in (0, 14)
        s.set(x, 0, 5, GRASS() if median else ASPH())
        s.set(x, 0, 11, GRASS() if median else ASPH())
        s.set(x, 0, 6, B("red_terracotta") if median else ASPH())   # bike lane
        for z in range(7, 11):
            s.set(x, 0, z, B("packed_mud") if median else ASPH())
    for x in (2, 7, 12):
        tree(s, x, 5); tree(s, x, 11)
    for x in (4, 10):
        lamp(s, x, 5); lamp(s, x, 11)
    # kiosk (x6..8, z7..9)
    for x, z in ((6, 7), (8, 7), (6, 9), (8, 9)):
        s.set(x, 1, z, B("green_concrete")); s.set(x, 2, z, B("green_concrete"))
    s.set(7, 1, 7, SLAB_TOP("oak")); s.set(7, 1, 9, SLAB_TOP("oak"))
    s.set(6, 1, 8, B("green_concrete")); s.set(8, 1, 8, B("green_concrete"))
    s.set(6, 2, 8, PANE()); s.set(8, 2, 8, PANE())
    s.set(7, 1, 8, BARREL("up", LOOT_KIOSK))
    s.fills(6, 3, 7, 8, 3, 9, B("green_concrete"))
    s.set(7, 4, 8, LANTERN())
    # cafe tables + benches on the walkway
    table(s, 3, 1, 8); table(s, 11, 1, 8)
    for x in (3, 4, 10, 11):
        s.set(x, 1, 10, STAIR("south"))
    s.set(0, 1, 8, street_j("west_up")); s.set(14, 1, 8, street_j("east_up"))
    for x in (3, 11):
        s.set(x, 1, 0, bld_j("north_up")); s.set(x, 1, 16, bld_j("south_up"))
    s.save(STRUCT / "rothschild.nbt")


def terminator() -> None:
    s = Structure(3, 3, 9)
    s.fills(0, 0, 0, 2, 0, 8, WALK())
    for z in (2, 4, 6):
        s.set(1, 1, z, LAMP_POST())
    s.set(2, 1, 0, B("potted_cactus"))
    s.set(0, 1, 4, end_j("west_up"))
    s.save(STRUCT / "terminator.nbt")


# ---------------------------------------------------------------- building helpers
def porch(s: Structure, cx: int) -> None:
    s.fills(0, 0, 0, s.sx - 1, 0, 0, WALK())
    s.set(cx, 1, 0, entrance_j())


def shell(s: Structure, x0, z0, x1, z1, floors: int, wall, *, h: int = 4, start: int = 0,
          band=None) -> int:
    """Floors k=start..floors-1 with floor slab at y=4k; returns roof y."""
    for k in range(start, floors):
        yb = k * h
        s.fills(x0, yb, z0, x1, yb, z1, SLABF())
        if band:
            for x in range(x0, x1 + 1):
                s.set(x, yb, z0, band()); s.set(x, yb, z1, band())
            for z in range(z0, z1 + 1):
                s.set(x0, yb, z, band()); s.set(x1, yb, z, band())
        for y in range(yb + 1, yb + h):
            for x in range(x0, x1 + 1):
                s.set(x, y, z0, wall(x, y, z0, k)); s.set(x, y, z1, wall(x, y, z1, k))
            for z in range(z0 + 1, z1):
                s.set(x0, y, z, wall(x0, y, z, k)); s.set(x1, y, z, wall(x1, y, z, k))
    roof = floors * h
    s.fills(x0, roof, z0, x1, roof, z1, band() if band else SLABF())
    return roof


def parapet(s: Structure, x0, z0, x1, z1, y: int, block) -> None:
    for x in range(x0, x1 + 1):
        s.set(x, y, z0, block()); s.set(x, y, z1, block())
    for z in range(z0, z1 + 1):
        s.set(x0, y, z, block()); s.set(x1, y, z, block())


def front_door(s: Structure, x: int, z: int, wood: str = "oak") -> None:
    s.set(x, 1, z, B(f"{wood}_door", facing="north", half="lower", hinge="left", open="false",
                     powered="false"))
    s.set(x, 2, z, B(f"{wood}_door", facing="north", half="upper", hinge="left", open="false",
                     powered="false"))


def bauhaus_wall(x0, z0, x1, z1, ribbon_rows=(2,), mullion=4, glass=None):
    """White plaster with horizontal ribbon windows (International Style)."""
    def f(x, y, z, k):
        yy = y - 4 * k
        corner = (x in (x0, x1)) and (z in (z0, z1))
        if corner or yy not in ribbon_rows:
            return WHITE()
        along = x if z in (z0, z1) else z
        if along % mullion == 0:
            return WHITE()
        return PANE(glass)
    return f


def rounded_balconies(s: Structure, x0, x1, z_front, depth: int, y: int, wrap: int = 3) -> None:
    """Balcony slab running along the front (z_front-1) and wrapping the corners, with
    the outer corner cut off so it reads rounded; white parapet on the outer edge."""
    zf = z_front - 1
    cells = [(x, zf) for x in range(x0, x1 + 1)]
    cells += [(x0 - 1, z) for z in range(z_front, z_front + wrap)]
    cells += [(x1 + 1, z) for z in range(z_front, z_front + wrap)]
    for x, z in cells:
        s.set(x, y, z, QUARTZ())
        s.set(x, y + 1, z, B("smooth_quartz_slab", type="bottom", waterlogged="false")
              if (x, z) in ((x0, zf), (x1, zf)) else WHITE())
    # rounded corner: diagonal step between the front run and the side wrap
    s.set(x0 - 1, y + 1, z_front, B("quartz_stairs", facing="west", half="bottom",
                                    shape="outer_left", waterlogged="false"))
    s.set(x1 + 1, y + 1, z_front, B("quartz_stairs", facing="east", half="bottom",
                                    shape="outer_right", waterlogged="false"))


# ---------------------------------------------------------------- buildings
def bauhaus_a() -> None:
    """3 floors, 11x12, rounded wrap balconies on floors 1-2."""
    W, D, F = 11, 13, 3
    s = Rigid(W, F * 4 + 3, D)
    porch(s, 5)
    s.fills(0, 0, 1, W - 1, 0, 1, WALK())
    x0, z0, x1, z1 = 1, 2, 9, 12
    roof = shell(s, x0, z0, x1, z1, F, bauhaus_wall(x0, z0, x1, z1, ribbon_rows=(2,)),
                 band=lambda: QUARTZ())
    front_door(s, 5, z0)
    for x in (3, 4, 6, 7):
        s.set(x, 2, z0, PANE())
    for x in range(3, 8):    # entrance canopy
        s.set(x, 3, 1, B("smooth_quartz_slab", type="top", waterlogged="false"))
    for k in (1, 2):
        rounded_balconies(s, x0, x1, z0, 1, 4 * k, wrap=3)
        for x in (4, 5, 6):   # french windows onto the balcony
            s.set(x, 4 * k + 1, z0, PANE()); s.set(x, 4 * k + 2, z0, PANE())
    lad = (8, 11)
    for k in range(F):
        apartment(s, x0 + 1, z0 + 1, x1 - 1, z1 - 1, 4 * k + 1,
                  bed_color=("light_blue", "white", "yellow")[k],
                  loot=LOOT_APT if k == 1 else None, avoid={lad, (5, 3)})
    ladder_shaft(s, *lad, top=roof)
    parapet(s, x0, z0, x1, z1, roof + 1, WHITE)
    solar_heater(s, 2, roof + 1, 9); solar_heater(s, 5, roof + 1, 9)
    s.save(STRUCT / "bauhaus_a.nbt")


def bauhaus_b() -> None:
    """4 floors on pilotis (Le Corbusier style), 13x11, roof pergola."""
    W, D, F = 13, 11, 4
    s = Rigid(W, F * 4 + 4, D)
    porch(s, 6)
    x0, z0, x1, z1 = 1, 2, 11, 10
    s.fills(0, 0, 1, W - 1, 0, D - 1, WALK())
    # pilotis ground floor: columns + small lobby
    for x, z in ((x0, z0), (x1, z0), (x0, z1), (x1, z1), (x0, 6), (x1, 6), (4, z0), (8, z0)):
        s.fills(x, 1, z, x, 3, z, WHITE())
    s.fills(5, 1, 5, 7, 3, 8, WHITE())
    s.fills(6, 1, 6, 6, 3, 7, B("air"))
    front_door(s, 6, 5)
    s.set(2, 1, 4, STAIR("west", "birch")); s.set(2, 1, 5, STAIR("west", "birch"))
    s.set(10, 1, 4, B("potted_fern")); s.set(10, 1, 8, B("potted_fern"))
    s.set(3, 1, 9, B("potted_bamboo"))
    roof = shell(s, x0, z0, x1, z1, F, bauhaus_wall(x0, z0, x1, z1, ribbon_rows=(2, 3), mullion=3),
                 start=1, band=lambda: QUARTZ())
    for k in (1, 2, 3):
        rounded_balconies(s, x0, x1, z0, 1, 4 * k, wrap=2)
        s.set(6, 4 * k + 1, z0, PANE()); s.set(6, 4 * k + 2, z0, PANE())
    lad = (6, 7)
    # floor slab at y=4 is the lobby ceiling; ladder starts in the lobby
    for k in (1, 2, 3):
        apartment(s, x0 + 1, z0 + 1, x1 - 1, z1 - 1, 4 * k + 1,
                  bed_color=("white", "cyan", "orange", "white")[k],
                  loot=LOOT_APT if k in (1, 3) else None, avoid={lad, (6, 8)})
    s.fills(6, 4, 8, 6, roof - 1, 8, WHITE())   # ladder backing / service shaft
    ladder_shaft(s, *lad, top=roof)
    # roof terrace + pergola
    parapet(s, x0, z0, x1, z1, roof + 1, WHITE)
    for x, z in ((3, 4), (3, 8), (9, 4), (9, 8)):
        s.set(x, roof + 1, z, FENCE()); s.set(x, roof + 2, z, FENCE())
    s.fills(3, roof + 3, 4, 9, roof + 3, 8, SLAB_TOP("birch"))
    solar_heater(s, 8, roof + 1, 2 + 1)
    s.save(STRUCT / "bauhaus_b.nbt")


def bauhaus_c() -> None:
    """5 floors, narrow 9x14, 'thermometer' stairwell window, ground-floor cafe."""
    W, D, F = 10, 14, 5
    s = Rigid(W, F * 4 + 3, D)
    porch(s, 4)
    s.fills(0, 0, 1, W - 1, 0, 1, WALK())
    x0, z0, x1, z1 = 1, 2, 7, 13
    roof = shell(s, x0, z0, x1, z1, F, bauhaus_wall(x0, z0, x1, z1, ribbon_rows=(2,), mullion=3),
                 band=lambda: QUARTZ())
    # cafe shop window
    for x in (2, 3, 5, 6):
        s.set(x, 1, z0, PANE()); s.set(x, 2, z0, PANE())
    front_door(s, 4, z0)
    for x in range(1, 8):
        s.set(x, 3, 1, B("orange_wool"))   # awning
    table(s, 2, 1, 1, chairs="none")
    # thermometer window
    for y in range(5, roof):
        if y % 4:
            s.set(4, y, z0, PANE("light_blue"))
    # side balconies (east) on alternate floors, rounded ends
    for k in (1, 3):
        y = 4 * k
        for z in range(5, 10):
            s.set(x1 + 1, y, z, QUARTZ())
            s.set(x1 + 1, y + 1, z, WHITE() if z not in (5, 9) else
                  B("smooth_quartz_slab", type="bottom", waterlogged="false"))
        s.set(x1, y + 1, 7, PANE()); s.set(x1, y + 2, 7, PANE())
    # cafe interior
    table(s, 3, 1, 6); table(s, 3, 1, 9)
    s.set(6, 1, 4, BARREL("up", LOOT_KIOSK)); s.set(6, 1, 5, SLAB_TOP("oak"))
    lad = (6, 12)
    for k in range(1, F):
        apartment(s, x0 + 1, z0 + 2, x1 - 1, z1 - 1, 4 * k + 1,
                  bed_color=("white", "lime", "white", "pink", "light_blue")[k],
                  loot=LOOT_APT if k in (2, 4) else None, avoid={lad})
    ladder_shaft(s, *lad, top=roof)
    parapet(s, x0, z0, x1, z1, roof + 1, WHITE)
    solar_heater(s, 2, roof + 1, 9)
    s.save(STRUCT / "bauhaus_c.nbt")


def graffiti(x: int, y: int) -> str | None:
    """Deterministic graffiti blobs on a wall (returns a concrete colour or None)."""
    cols = ("magenta", "lime", "cyan", "yellow", "orange", "pink", "light_blue", "purple")
    h = (x * 73856093) ^ (y * 19349663)
    if y > 6 or (h >> 3) % 5 > 1:
        return None
    return cols[(x // 2 + y // 2) % len(cols)]


def florentin_loft() -> None:
    """Florentin: brick light-industrial loft, workshop below, graffiti, rooftop garden."""
    W, D, F = 11, 12, 3
    s = Rigid(W, F * 4 + 3, D)
    porch(s, 5)
    x0, z0, x1, z1 = 1, 1, 9, 11

    def wall(x, y, z, k):
        yy = y - 4 * k
        corner = (x in (x0, x1)) and (z in (z0, z1))
        if not corner and yy in (1, 2) and k > 0:
            along = x if z in (z0, z1) else z
            if along % 3 != 0:
                return PANE()
        if z == z0 and k == 0 and yy in (1, 2) and x in (2, 3, 7, 8):
            return PANE()
        if x in (x0, x1) and not corner:
            g = graffiti(z, y)
            if g:
                return B(f"{g}_concrete")
        if yy == 3:
            return B("gray_concrete") if k == 0 else BRICK()
        return BRICK()

    roof = shell(s, x0, z0, x1, z1, F, wall, band=lambda: B("polished_andesite"))
    # roll-up garage door look for the workshop
    s.set(5, 1, z0, B("air")); s.set(5, 2, z0, B("air"))
    s.set(5, 1, z0, B("iron_door", facing="north", half="lower", hinge="left", open="true", powered="false"))
    s.set(5, 2, z0, B("iron_door", facing="north", half="upper", hinge="left", open="true", powered="false"))
    for x in range(x0, x1 + 1):
        g = graffiti(x + 20, 3)
        if g and x != 5:
            s.set(x, 3, z0, B(f"{g}_concrete"))
    # workshop
    s.set(2, 1, 3, B("smithing_table")); s.set(2, 1, 4, CRAFT())
    s.set(2, 1, 6, B("loom", facing="east")); s.set(8, 1, 3, B("anvil", facing="west"))
    s.set(8, 1, 6, CHEST("west", LOOT_APT)); s.set(5, 3, 5, HLANTERN())
    lad = (8, 10)
    apartment(s, 2, 2, 8, 10, 5, bed_color="red", loot=None, avoid={lad})
    office(s, 2, 2, 8, 10, 9, loot=LOOT_TECH, avoid={lad, (7, 9), (7, 10)})
    ladder_shaft(s, *lad, top=roof)
    # rooftop garden
    parapet(s, x0, z0, x1, z1, roof + 1, BRICK)
    s.fills(2, roof, 2, 4, roof, 4, GRASS())
    s.set(3, roof + 1, 3, B("rose_bush", half="lower")); s.set(3, roof + 2, 3, B("rose_bush", half="upper"))
    s.set(2, roof + 1, 2, B("poppy")); s.set(4, roof + 1, 4, B("dandelion"))
    solar_heater(s, 5, roof + 1, 7)
    s.save(STRUCT / "florentin_loft.nbt")


def sarona_house() -> None:
    """Sarona Templer colony house: cream stone, green shutters, red-tile gable roof."""
    W, D = 11, 10
    s = Rigid(W, 13, D)
    porch(s, 5)
    x0, z0, x1, z1 = 1, 2, 9, 8
    # front garden
    s.fills(0, 0, 1, W - 1, 0, 1, GRASS())
    s.set(5, 0, 1, B("smooth_stone"))
    for x in (4, 6):
        s.set(x, 1, 1, B("red_tulip"))
    s.fills(0, 0, 9, W - 1, 0, 9, GRASS())

    def wall(x, y, z, k):
        corner = (x in (x0, x1)) and (z in (z0, z1))
        return B("cut_sandstone") if corner else B("smooth_sandstone")

    shell(s, x0, z0, x1, z1, 2, wall)
    front_door(s, 5, z0, "spruce")
    # windows with green (warped) shutters
    for k in (0, 1):
        yb = 4 * k
        for x in (2, 8) if k == 0 else (2, 5, 8):
            s.set(x, yb + 1, z0, PANE()); s.set(x, yb + 2, z0, PANE())
            for sx_ in (x - 1, x + 1):
                for y in (yb + 1, yb + 2):
                    s.set(sx_, y, z0 - 1, B("warped_trapdoor", facing="north", half="bottom",
                                            open="true", powered="false", waterlogged="false"))
            s.set(x, yb + 3, z0, B("cut_sandstone"))
        for z in (4, 6):
            s.set(x0, yb + 2, z, PANE()); s.set(x1, yb + 2, z, PANE())
            s.set(x0, yb + 1, z, PANE()); s.set(x1, yb + 1, z, PANE())
    # gable roof (ridge along x), eaves overhang one block
    for i in range(4):
        y = 8 + i
        for x in range(0, W):
            s.set(x, y, 1 + i, B("brick_stairs", facing="south", half="bottom", shape="straight",
                                 waterlogged="false"))
            s.set(x, y, 9 - i, B("brick_stairs", facing="north", half="bottom", shape="straight",
                                 waterlogged="false"))
        for z in range(2 + i, 9 - i):
            s.set(x0, y, z, B("smooth_sandstone")); s.set(x1, y, z, B("smooth_sandstone"))
    for x in range(0, W):
        s.set(x, 11, 5, BRICK()); s.set(x, 12, 5, B("brick_slab", type="bottom", waterlogged="false"))
    s.set(x0, 9, 5, PANE()); s.set(x1, 9, 5, PANE())        # attic round windows
    # interiors: ground = Sarona market deli, upper = bedroom
    s.fills(2, 1, 7, 4, 1, 7, SLAB_TOP("spruce"))
    s.set(2, 1, 6, BARREL("up", LOOT_KIOSK)); s.set(3, 2, 7, B("potted_oak_sapling"))
    table(s, 4, 1, 4)
    lad = (8, 7)
    bed(s, 2, 5, 6, "red", "south")
    s.set(4, 5, 7, CHEST("north", LOOT_APT)); s.set(5, 5, 7, B("bookshelf"))
    s.set(6, 5, 3, CRAFT()); s.set(5, 7, 5, HLANTERN()); s.set(5, 3, 5, HLANTERN())
    ladder_shaft(s, *lad, top=8)
    s.save(STRUCT / "sarona_house.nbt")


def curtain_wall(x0, z0, x1, z1, glass="light_blue_stained_glass", mullion="light_gray_concrete"):
    def f(x, y, z, k):
        corner = (x in (x0, x1)) and (z in (z0, z1))
        along = x if z in (z0, z1) else z
        if corner:
            return QUARTZ()
        if along % 3 == 1:
            return B(mullion)
        return B(glass)
    return f


def highrise_a() -> None:
    """Startup tower: 8 floors glass curtain wall, offices, server room, helipad."""
    W, D, F = 13, 13, 8
    s = Rigid(W, F * 4 + 5, D)
    porch(s, 6)
    x0, z0, x1, z1 = 1, 1, 11, 12
    roof = shell(s, x0, z0, x1, z1, F, curtain_wall(x0, z0, x1, z1), band=lambda: B("gray_concrete"))
    # lobby
    for x in (5, 7):
        s.set(x, 1, z0, PANE()); s.set(x, 2, z0, PANE())
    s.set(6, 1, z0, B("iron_door", facing="north", half="lower", hinge="left", open="false", powered="false"))
    s.set(6, 2, z0, B("iron_door", facing="north", half="upper", hinge="left", open="false", powered="false"))
    s.fills(3, 1, 6, 9, 1, 6, SLAB_TOP("birch"))   # reception desk
    s.set(6, 1, 7, STAIR("south", "dark_oak"))
    s.set(2, 1, 2, B("potted_bamboo")); s.set(10, 1, 2, B("potted_bamboo"))
    s.set(6, 3, 4, B("sea_lantern"))
    lad = (10, 11)
    for k in range(1, F):
        y = 4 * k + 1
        if k == 3:   # server room
            for x in (3, 5, 7):
                for z in range(3, 10):
                    s.set(x, y, z, B("iron_block"))
                    s.set(x, y + 1, z, B("redstone_lamp", lit="true") if z % 2 else B("iron_block"))
            s.set(9, y, 3, CHEST("west", LOOT_TECH))
            s.set(6, y + 2, 6, B("sea_lantern"))
        else:
            office(s, x0 + 1, z0 + 1, x1 - 1, z1 - 1, y,
                   loot=LOOT_TECH if k in (1, 5, 7) else None, avoid={lad, (10, 10)})
    ladder_shaft(s, *lad, top=roof, facing="north")
    # helipad
    parapet(s, x0, z0, x1, z1, roof + 1, lambda: B("iron_bars", north="false", south="false",
                                                     east="false", west="false", waterlogged="false"))
    for x, z in ((4, 4), (4, 5), (4, 6), (4, 7), (4, 8), (8, 4), (8, 5), (8, 6), (8, 7), (8, 8),
                 (5, 6), (6, 6), (7, 6)):
        s.set(x, roof, z, B("white_concrete"))
    s.set(2, roof + 1, 2, B("lightning_rod", facing="up", powered="false", waterlogged="false"))
    for y in (roof + 1, roof + 2, roof + 3):
        s.set(10, y, 2, B("iron_bars", north="false", south="false", east="false", west="false",
                          waterlogged="false"))
    s.set(10, roof + 4, 2, B("lightning_rod", facing="up", powered="false", waterlogged="false"))
    s.save(STRUCT / "highrise_a.nbt")


def highrise_b() -> None:
    """6-floor cyan glass tower with set-back penthouse and roof garden."""
    W, D, F = 11, 11, 6
    s = Rigid(W, F * 4 + 4, D)
    porch(s, 5)
    x0, z0, x1, z1 = 1, 1, 9, 10
    shell(s, x0, z0, x1, z1, F - 1, curtain_wall(x0, z0, x1, z1, "cyan_stained_glass", "white_concrete"),
          band=lambda: WHITE())
    # penthouse (set back by one)
    px0, pz0, px1, pz1 = 2, 2, 8, 9
    roof = shell(s, px0, pz0, px1, pz1, F, curtain_wall(px0, pz0, px1, pz1, "cyan_stained_glass",
                                                        "white_concrete"), start=F - 1,
                 band=lambda: WHITE())
    # restore the full-size terrace floor around the penthouse
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if not (px0 <= x <= px1 and pz0 <= z <= pz1):
                s.set(x, 4 * (F - 1), z, WHITE())
    parapet(s, x0, z0, x1, z1, 4 * (F - 1) + 1, lambda: PANE())
    for x in (5,):
        s.set(x, 1, z0, B("iron_door", facing="north", half="lower", hinge="left", open="false", powered="false"))
        s.set(x, 2, z0, B("iron_door", facing="north", half="upper", hinge="left", open="false", powered="false"))
    s.set(4, 1, 2, B("potted_fern")); s.set(6, 1, 2, B("potted_fern"))
    lad = (7, 8)
    office(s, x0 + 1, z0 + 1, x1 - 1, z1 - 1, 1, loot=LOOT_TECH, avoid={lad, (5, 2), (7, 7)})
    for k in range(1, F - 1):
        apartment(s, x0 + 1, z0 + 1, x1 - 1, z1 - 1, 4 * k + 1,
                  bed_color=("white", "gray", "white", "light_gray", "white")[k],
                  loot=LOOT_APT if k in (2, 4) else None, avoid={lad})
    apartment(s, px0 + 1, pz0 + 1, px1 - 1, pz1 - 1, 4 * (F - 1) + 1, bed_color="black",
              loot=LOOT_TECH, avoid={lad})
    s.fills(7, 1, 9, 7, 4 * (F - 1) - 1, 9, WHITE())   # ladder backing / service shaft
    ladder_shaft(s, *lad, top=roof)
    parapet(s, px0, pz0, px1, pz1, roof + 1, WHITE)
    s.fills(3, roof, 3, 5, roof, 5, GRASS())
    s.set(4, roof + 1, 4, B("azalea"))
    solar_heater(s, 5, roof + 1, 7)
    s.save(STRUCT / "highrise_b.nbt")


def tayelet() -> None:
    """Beach promenade: wave-pattern tiles, palms, benches, umbrellas, lifeguard tower."""
    W, D = 15, 14
    s = Rigid(W, 9, D)
    porch(s, 7)
    wave = (0, 1, 2, 1)
    for x in range(W):
        for z in range(1, 4):
            s.set(x, 0, z, B("light_gray_terracotta") if z == 1 + wave[x % 4] else B("white_terracotta"))
        s.set(x, 0, 4, B("smooth_sandstone"))
        if x not in (6, 7, 8):
            s.set(x, 1, 4, B("sandstone_wall", east="none", west="none", north="none", south="none",
                             up="true", waterlogged="false"))
        for z in range(5, D):
            s.set(x, 0, z, SAND())
    palm(s, 2, 2); palm(s, 12, 2)
    for x in (4, 5, 9, 10):
        s.set(x, 1, 3, STAIR("north"))
    lamp(s, 7, 3)
    # umbrellas + towels
    for (ux, uz, col) in ((3, 8, "red"), (7, 11, "blue"), (12, 6, "yellow")):
        s.set(ux, 1, uz, FENCE()); s.set(ux, 2, uz, FENCE())
        s.set(ux, 3, uz, B(f"{col}_wool"))
        for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            s.set(ux + dx, 3, uz + dz, B("white_wool"))
        s.set(ux + 1, 1, uz, B(f"{col}_carpet"))
    s.set(3, 1, 10, B("white_carpet")); s.set(4, 1, 10, B("white_carpet"))
    # matkot paddles... a barrel of beach snacks
    s.set(1, 1, 6, BARREL("up", LOOT_KIOSK))
    # lifeguard tower on stilts
    for x, z in ((10, 10), (12, 10), (10, 12), (12, 12)):
        s.set(x, 1, z, B("spruce_fence", north="false", south="false", east="false", west="false",
                         waterlogged="false"))
        s.set(x, 2, z, B("spruce_fence", north="false", south="false", east="false", west="false",
                         waterlogged="false"))
    s.fills(11, 1, 10, 11, 2, 10, WHITE())
    for y in (1, 2, 3):
        s.set(11, y, 9, LADDER("north"))
    s.fills(10, 3, 10, 12, 3, 12, WHITE())
    for y in (4, 5):
        for x, z in ((10, 10), (12, 10), (10, 11), (12, 11), (10, 12), (11, 12), (12, 12)):
            s.set(x, y, z, WHITE())
        s.set(11, y, 10, B("air"))
    s.set(10, 5, 11, PANE()); s.set(12, 5, 11, PANE()); s.set(11, 5, 12, PANE())
    s.set(11, 4, 11, CHEST("north", LOOT_APT))
    s.fills(10, 6, 10, 12, 6, 12, B("blue_concrete"))
    s.set(11, 7, 11, B("red_banner", rotation="0"))
    s.save(STRUCT / "tayelet.nbt")


# ---------------------------------------------------------------- pools / json
def update_structure_json() -> None:
    data = {
        "type": "minecraft:jigsaw",
        "biomes": ["israel_simulator:urban_area"],
        "step": "surface_structures",
        "spawn_overrides": {},
        "terrain_adaptation": "beard_thin",
        "start_pool": f"{NS}:tel_aviv_city",
        "size": 6,
        # the square's floor sits above a Rigid.FOOTING-deep footing; sink it so the
        # floor (not the footing) lands on the surface
        "start_height": {"absolute": -Rigid.FOOTING},
        "project_start_to_heightmap": "WORLD_SURFACE_WG",
        "max_distance_from_center": 80,
        "use_expansion_hack": True,
    }
    STRUCTURE_JSON.write_text(json.dumps(data, indent=2) + "\n")
    print(f"wrote {STRUCTURE_JSON.relative_to(ROOT)}")


def main() -> None:
    STRUCT.mkdir(parents=True, exist_ok=True)
    square()
    street_straight(); street_crossroad(); street_corner(); street_t(); rothschild(); terminator()
    bauhaus_a(); bauhaus_b(); bauhaus_c(); florentin_loft(); sarona_house()
    highrise_a(); highrise_b(); tayelet()
    update_structure_json()
    P = lambda n: f"{NS}:tel_aviv/{n}"
    write_pool(START_POOL, [(P("square"), 1, "rigid")], "minecraft:empty")
    write_pool(POOL / "streets.json", [
        (P("street_straight"), 10, "terrain_matching"),
        (P("street_crossroad"), 4, "terrain_matching"),
        (P("street_corner"), 4, "terrain_matching"),
        (P("street_t"), 4, "terrain_matching"),
        (P("rothschild"), 3, "terrain_matching"),
    ], P("terminators"))
    write_pool(POOL / "terminators.json", [(P("terminator"), 1, "terrain_matching")], "minecraft:empty")
    write_pool(POOL / "buildings.json", [
        (P("bauhaus_a"), 4, "rigid"),
        (P("bauhaus_b"), 3, "rigid"),
        (P("bauhaus_c"), 3, "rigid"),
        (P("florentin_loft"), 3, "rigid"),
        (P("sarona_house"), 2, "rigid"),
        (P("highrise_b"), 2, "rigid"),
        (P("highrise_a"), 1, "rigid"),
        (P("tayelet"), 1, "rigid"),
    ], "minecraft:empty")
    write_pool(POOL / "landmarks.json", [(P("highrise_a"), 1, "rigid")], "minecraft:empty")


if __name__ == "__main__":
    main()
