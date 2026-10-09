#!/usr/bin/env python3
"""Shared helpers for the jigsaw town/village generators (DataVersion 4903).

Conventions (same as gen_jerusalem_city.py / gen_tel_aviv_city.py):
  * streets   terrain_matching, floor y=0, jigsaws at y=1, named minecraft:street
  * buildings rigid (Rigid: buried footing), porch row z=0 with the entrance jigsaw
              at (cx,1,0) north_up, name minecraft:building_entrance, pool minecraft:empty
  * structure beard_thin, size 6, start_height -FOOTING, WORLD_SURFACE_WG projection
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from gen_jerusalem_city import (  # noqa: E402,F401
    BARREL, CHEST, CRAFT, FENCE, HLANTERN, KNOWN, LANTERN, POT, ROOT, Structure, blk, jigsaw,
    write_pool,
)

if not KNOWN:
    raise SystemExit("blocks.json report missing (/workspace/mcreports/out/reports/blocks.json)")

NS = "israel_simulator"
DATA = ROOT / "src/main/resources/data/israel_simulator"


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


def stone_paving(x: int, z: int):
    """Worn limestone and stone setts."""
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
    """Rigid piece authored with the floor at y=0 (jigsaws at y=1, like the streets).

    Everything is shifted up by FOOTING and the footprint gets FOOTING layers of
    sandstone below it, so slopes/ravines never leave a floating floor. Columns in
    `no_footing` (piers, boats over water) are skipped."""

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


# ---------------------------------------------------------------- jigsaws / layout
def entrance_j():
    return jigsaw("north_up", "minecraft:building_entrance", "minecraft:building_entrance",
                  "minecraft:empty", "minecraft:air")


def end_j(o):
    return jigsaw(o, "minecraft:street", "minecraft:street", "minecraft:empty", "minecraft:structure_void")


class Kit:
    """Per-structure paths and jigsaw factories. `prefix` is the folder under
    structure/ and template_pool/ (e.g. "market" -> israel_simulator:market/...)."""

    def __init__(self, structure: str, prefix: str, porch=stone_paving):
        self.structure, self.prefix, self.porch = structure, prefix, porch
        self.struct = DATA / "structure" / prefix
        self.pool = DATA / "worldgen/template_pool" / prefix
        self.struct.mkdir(parents=True, exist_ok=True)

    def loc(self, piece: str) -> str:
        return f"{NS}:{self.prefix}/{piece}"

    def street_j(self, o):
        return jigsaw(o, "minecraft:street", "minecraft:street", f"{NS}:{self.prefix}/streets",
                      "minecraft:structure_void")

    def bld_j(self, o, pool="buildings"):
        return jigsaw(o, "minecraft:building_entrance", "minecraft:building_entrance",
                      f"{NS}:{self.prefix}/{pool}", "minecraft:structure_void")

    def write_pools(self, start: str, streets, terminators, buildings, landmarks=None) -> None:
        """streets/terminators/buildings/landmarks: lists of (piece, weight, projection)."""
        L = lambda items: [(self.loc(p), w, proj) for p, w, proj in items]
        write_pool(DATA / f"worldgen/template_pool/{self.structure}.json", [(self.loc(start), 1, "rigid")],
                   "minecraft:empty")
        write_pool(self.pool / "streets.json", L(streets), self.loc("terminators"))
        write_pool(self.pool / "terminators.json", L(terminators), "minecraft:empty")
        write_pool(self.pool / "buildings.json", L(buildings), "minecraft:empty")
        if landmarks:
            write_pool(self.pool / "landmarks.json", L(landmarks), "minecraft:empty")

    def write_structure_json(self, biome: str, size: int = 6, max_distance: int = 80) -> None:
        data = {
            "type": "minecraft:jigsaw",
            "biomes": [biome],
            "step": "surface_structures",
            "spawn_overrides": {},
            "terrain_adaptation": "beard_thin",
            "start_pool": f"{NS}:{self.structure}",
            "size": size,
            # the start piece's floor (above its footing) lands on the surface
            "start_height": {"absolute": -Rigid.FOOTING},
            "project_start_to_heightmap": "WORLD_SURFACE_WG",
            "max_distance_from_center": max_distance,
            "use_expansion_hack": True,
        }
        p = DATA / f"worldgen/structure/{self.structure}.json"
        p.write_text(json.dumps(data, indent=2) + "\n")
        print(f"wrote {p.relative_to(ROOT)}")

    # simple shared street pieces ------------------------------------------------
    def straight(self, name: str, length: int, width: int, paving, lamp_at=None, sy: int = 4,
                 entrances=()) -> Structure:
        """Straight street along x; jigsaws at the ends (z=width//2) and building
        entrances on both sides at the given x positions."""
        s = Structure(length, sy, width)
        c = width // 2
        for x in range(length):
            for z in range(width):
                s.set(x, 0, z, paving(x, z))
        if lamp_at:
            lamp(s, *lamp_at)
        s.set(0, 1, c, self.street_j("west_up")); s.set(length - 1, 1, c, self.street_j("east_up"))
        for x in entrances:
            s.set(x, 1, 0, self.bld_j("north_up")); s.set(x, 1, width - 1, self.bld_j("south_up"))
        return s

    def junction(self, name: str, n: int, arms: set[str], paving, sy: int = 4) -> Structure:
        s = Structure(n, sy, n)
        c = n // 2
        for x in range(n):
            for z in range(n):
                s.set(x, 0, z, paving(x, z))
        for o, x, z, a in (("north_up", c, 0, "n"), ("south_up", c, n - 1, "s"),
                           ("west_up", 0, c, "w"), ("east_up", n - 1, c, "e")):
            s.set(x, 1, z, self.street_j(o) if a in arms else self.bld_j(o))
        return s

    def terminator(self, width: int, paving, prop=None) -> Structure:
        s = Structure(3, 3, width)
        for x in range(3):
            for z in range(width):
                s.set(x, 0, z, paving(x, z))
        if prop is not None:
            s.set(2, 1, width // 2, prop)
        s.set(0, 1, width // 2, end_j("west_up"))
        return s


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


# ---------------------------------------------------------------- houses
def house(kit: "Kit", name: str, W: int, D: int, floors: int, mat: str, *, upper=None, cut=None,
          door="warped", shutter="warped", trim="smooth_quartz", beds=("light_blue",),
          loot: str, pergola=True, vines=True, extra=None, floor_block="smooth_stone") -> None:
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
        s.set(x, 0, 0, kit.porch(x, 0))
    s.set(cx, 1, 0, entrance_j())
    lad = (bx1 - 1, bz1 - 1)
    for k in range(floors):
        yb = 4 * k
        for x in range(W):
            for z in range(D):
                if body(k, x, z):
                    s.set(x, yb, z, B(mat) if is_wall(k, x, z) else B(floor_block))
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
    s.save(kit.struct / f"{name}.nbt")


