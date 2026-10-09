#!/usr/bin/env python3
"""Generate the tropical_island structure: a whole island (terrain + buildings) as one NBT.

The tropical_island biome is a rare warm deep-ocean patch; the structure raises the island out
of it. The layout follows the reference map of Little St. James (north up):

  * outline   main body, NE lobe (main complex), W lobe (small pool), SW tip (pavilion),
              long SE arm, a north bay with a sandy beach; rocky stone shores elsewhere
  * hill      central hill with the white round water-tank dome
  * NE        main complex: white villa with light-blue hip roofs, big pool on a quartz deck,
              four pool houses, palms; utility sheds, a helipad circle on the NE point
  * bay       sandy beach, dock/pier with a moored boat, beach gazebo
  * centre    tennis court below the hill, a second helipad east of the complex
  * SW tip    the island_temple pavilion (same template generator as the Epstein arena)
  * roads     winding coarse-dirt roads between everything; palms, dry scrub, guest cottages

Template origin is placed at world y = BASE (start_height absolute); sea level 63.
Run:  /workspace/nbtvenv/bin/python scripts/worldgen/gen_tropical_island.py
"""
from __future__ import annotations

import heapq
import json
import math
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from gen_jerusalem_city import Structure, blk, jigsaw  # noqa: E402
from jigsaw_kit import B, DATA, LEAF, LANTERN, HLANTERN, PANE, SFENCE, SLAB, STAIR, palm  # noqa: E402
from structure_lib import chest  # noqa: E402
import gen_island_temple  # noqa: E402

NAME = "tropical_island"
LOOT = "israel_simulator:chests/island_temple"
SX, SZ = 240, 170
BASE = 40                 # world y of template y=0
SEA = 63 - BASE           # template y of the sea surface block row (world 63 = first air above water is 63)
SY = 72
WATER_TOP = 62 - BASE     # highest water block (world 62)
ANCHOR = "israel_simulator:island_center"
CENTER = (120, 0, 85)


class Sparse(Structure):
    """Structure without the air fill: unset cells keep the world (ocean water)."""

    def __init__(self, sx, sy, sz):
        self.sx, self.sy, self.sz = sx, sy, sz
        self.foundation = 0
        self.palette, self.index, self.blocks, self.block_nbt = [], {}, {}, {}

    def free(self, x, y, z):
        return 0 <= x < self.sx and 0 <= y < self.sy and 0 <= z < self.sz and (x, y, z) not in self.blocks

    def get(self, x, y, z):
        k = self.blocks.get((x, y, z))
        return None if k is None else str(self.palette[k]["Name"])


def h2(x, z, s=0):
    v = (x * 73856093) ^ (z * 19349663) ^ (s * 83492791)
    return (v & 0xFFFF) / 65535.0


# ------------------------------------------------------------------ outline
def ell(x, z, cx, cz, rx, rz):
    return ((x - cx) / rx) ** 2 + ((z - cz) / rz) ** 2


def capsule(x, z, a, b, r):
    ax, az = a; bx, bz = b
    t = max(0.0, min(1.0, ((x - ax) * (bx - ax) + (z - az) * (bz - az)) / ((bx - ax) ** 2 + (bz - az) ** 2)))
    px, pz = ax + t * (bx - ax), az + t * (bz - az)
    return math.hypot(x - px, z - pz) / r


def land_value(x, z):
    ang = math.atan2(z - 85, x - 120)
    wob = 0.10 * math.sin(ang * 5 + 1.3) + 0.07 * math.sin(ang * 11 + 0.4) + 0.05 * math.sin(ang * 17 + 2.0)
    v = min(ell(x, z, 110, 88, 72, 46), ell(x, z, 175, 42, 42, 30), ell(x, z, 42, 72, 34, 24),
            ell(x, z, 44, 132, 30, 25), capsule(x, z, (150, 108), (222, 152), 17) ** 2,
            ell(x, z, 78, 112, 40, 26))
    bay = ell(x, z, 112, 6, 50, 34)            # north bay
    if bay < 1:
        v = max(v, 1.0 + (1 - bay) * 0.6)
    return v + wob


LAND = np.zeros((SX, SZ), bool)
for x in range(SX):
    for z in range(SZ):
        LAND[x, z] = land_value(x, z) < 1.0
# keep only the biggest connected piece
seen = np.zeros_like(LAND)
best = []
for x in range(SX):
    for z in range(SZ):
        if LAND[x, z] and not seen[x, z]:
            comp, st = [], [(x, z)]
            seen[x, z] = True
            while st:
                a, b = st.pop(); comp.append((a, b))
                for da, db in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    c, d = a + da, b + db
                    if 0 <= c < SX and 0 <= d < SZ and LAND[c, d] and not seen[c, d]:
                        seen[c, d] = True; st.append((c, d))
            if len(comp) > len(best):
                best = comp
LAND[:] = False
for a, b in best:
    LAND[a, b] = True


def dist_field(src):
    """8-neighbour distance from every cell to the nearest `src` cell."""
    D = np.full((SX, SZ), 1e9)
    pq = []
    for x, z in zip(*np.nonzero(src)):
        D[x, z] = 0; pq.append((0.0, int(x), int(z)))
    heapq.heapify(pq)
    while pq:
        d, x, z = heapq.heappop(pq)
        if d > D[x, z]:
            continue
        for dx in (-1, 0, 1):
            for dz in (-1, 0, 1):
                c, e = x + dx, z + dz
                if (dx or dz) and 0 <= c < SX and 0 <= e < SZ:
                    nd = d + (1.4142 if dx and dz else 1.0)
                    if nd < D[c, e]:
                        D[c, e] = nd; heapq.heappush(pq, (nd, c, e))
    return D


DSEA = dist_field(~LAND)        # inland distance
DLAND = dist_field(LAND)        # offshore distance


def is_beach(x, z):
    # north bay beach, a short east beach (like the map), and a sliver on the SE arm
    return ((70 <= x <= 155 and z <= 52) or (196 <= x <= 214 and 78 <= z <= 100)
            or (150 <= x <= 200 and 120 <= z <= 140 and z > 0.5 * x + 40))


def gauss(x, z, cx, cz, amp, sig):
    return amp * math.exp(-((x - cx) ** 2 + (z - cz) ** 2) / (2 * sig * sig))


HILL = (124, 92)
H = np.zeros((SX, SZ), int)     # template y of the top solid block
for x in range(SX):
    for z in range(SZ):
        if not LAND[x, z]:
            continue
        d = DSEA[x, z]
        beach = is_beach(x, z)
        rise = min(d / 3.0, 2.5) if beach else min(d * 0.9, 5.0)
        hill = (gauss(x, z, *HILL, 19, 24) + gauss(x, z, 176, 40, 5, 22) + gauss(x, z, 44, 134, 5, 13)
                + gauss(x, z, 40, 72, 4, 14) + gauss(x, z, 190, 132, 4, 16) + gauss(x, z, 80, 110, 6, 18))
        hill *= min(1.0, d / 10.0)
        noise = 1.2 * math.sin(x * 0.21 + z * 0.13) * math.sin(z * 0.17 - x * 0.07)
        H[x, z] = SEA + int(round(rise + hill + noise * min(1.0, d / 8)))
        H[x, z] = max(H[x, z], SEA)

OCCUPIED: set[tuple[int, int]] = set()   # building footprints (no vegetation)
ROAD: set[tuple[int, int]] = set()


PADS: dict[tuple, int] = {}


def pad(x0, z0, x1, z1, y=None, margin=3):
    """Flatten a building pad (top block at template y) with a gentle blend around it.
    Idempotent: the second (building) pass reuses the height chosen in the first pass."""
    key = (x0, z0, x1, z1)
    if key in PADS:
        return PADS[key]
    if y is None:
        y = int(np.median(H[x0:x1 + 1, z0:z1 + 1][LAND[x0:x1 + 1, z0:z1 + 1]]))
    for x in range(x0 - margin, x1 + margin + 1):
        for z in range(z0 - margin, z1 + margin + 1):
            if not (0 <= x < SX and 0 <= z < SZ):
                continue
            inside = x0 <= x <= x1 and z0 <= z <= z1
            if inside:
                LAND[x, z] = True; H[x, z] = y
            elif LAND[x, z]:
                k = max(abs(x - min(max(x, x0), x1)), abs(z - min(max(z, z0), z1))) / (margin + 1)
                H[x, z] = int(round(y * (1 - k) + H[x, z] * k))
    PADS[key] = y
    return y


# ------------------------------------------------------------------ layout (grid coords)
COMPLEX = (150, 22, 206, 60)          # x0, z0, x1, z1 of the main complex terrace
DOCK = (118, None)
TENNIS = (92, 104, 109, 139)          # 18 x 36? (x0,z0,x1,z1) -> rotated court 36 long in z
HELI_NE = (205, 22)
HELI_E = (196, 72)
TEMPLE_X0, TEMPLE_Z0 = 30, 116        # island_temple template origin (27 x 28)
WPOOL = (24, 60, 40, 72)
GAZEBO = (90, 49)
COTTAGES = [(140, 64, "s"), (96, 60, "n"), (66, 64, "e"), (162, 96, "w"), (198, 128, "n"),
            (214, 148, "w"), (58, 98, "e")]
UTILITY = (210, 48)


def build_terrain(s: Sparse):
    for x in range(SX):
        for z in range(SZ):
            if LAND[x, z]:
                top = H[x, z]
                d = DSEA[x, z]
                beach = is_beach(x, z)
                steep = max(abs(int(H[x, z]) - int(H[min(x + 1, SX - 1), z])),
                            abs(int(H[x, z]) - int(H[max(x - 1, 0), z])),
                            abs(int(H[x, z]) - int(H[x, min(z + 1, SZ - 1)])),
                            abs(int(H[x, z]) - int(H[x, max(z - 1, 0)]))) >= 2
                rocky = (not beach) and d < 3.5
                for y in range(0, top + 1):
                    depth = top - y
                    if depth == 0:
                        if beach and d < 7:
                            b = B("sand")
                        elif rocky or steep:
                            r = h2(x, z, 1)
                            b = B("stone") if r < 0.45 else B("andesite") if r < 0.75 else B("tuff") \
                                if r < 0.85 else B("mossy_cobblestone")
                        elif h2(x, z, 2) < 0.18 or (d < 9 and h2(x, z, 3) < 0.45):
                            b = B("coarse_dirt")
                        else:
                            b = B("grass_block", snowy="false")
                    elif depth < 4:
                        b = B("sandstone") if (beach and d < 7) else B("stone") if rocky else B("dirt")
                    else:
                        b = B("stone")
                    s.set(x, y, z, b)
            else:
                # underwater skirt: a sandy slope down from the shoreline
                d = DLAND[x, z]
                if d <= 10:
                    top = WATER_TOP - int(d * 1.6)
                    if top >= 0:
                        rocky_side = not is_beach(x, z)
                        for y in range(0, top + 1):
                            if y == top:
                                b = B("gravel") if rocky_side and h2(x, z, 4) < 0.4 else B("sand")
                            else:
                                b = B("sandstone") if y > top - 3 else B("stone")
                            s.set(x, y, z, b)


# ------------------------------------------------------------------ helpers
def ground(x, z):
    return int(H[x, z]) + 1        # first free y above the surface


def hip_roof(s, x0, z0, x1, z1, y, mat="light_blue_concrete", edge="light_blue_terracotta"):
    k = 0
    while x0 + k <= x1 - k and z0 + k <= z1 - k:
        for x in range(x0 + k, x1 - k + 1):
            for z in range(z0 + k, z1 - k + 1):
                border = x in (x0 + k, x1 - k) or z in (z0 + k, z1 - k)
                if border or k == 0:
                    s.set(x, y + k, z, B(edge if k == 0 else mat))
        k += 1
        if k > 4:
            for x in range(x0 + k, x1 - k + 1):
                for z in range(z0 + k, z1 - k + 1):
                    s.set(x, y + k - 1, z, B(mat))
            break


def house(s, x0, z0, w, d, door="s", floors=1, roof="light_blue_concrete", wall="white_concrete",
          loot=LOOT, beds=1):
    x1, z1 = x0 + w - 1, z0 + d - 1
    y0 = pad(x0 - 1, z0 - 1, x1 + 1, z1 + 1) + 1
    for x in range(x0 - 1, x1 + 2):
        for z in range(z0 - 1, z1 + 2):
            OCCUPIED.add((x, z))
            s.set(x, y0 - 1, z, B("smooth_quartz") if x0 <= x <= x1 and z0 <= z <= z1 else B("smooth_sandstone"))
    hgt = 4 * floors
    for y in range(y0, y0 + hgt):
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                wallc = x in (x0, x1) or z in (z0, z1)
                if wallc:
                    corner = x in (x0, x1) and z in (z0, z1)
                    win = (not corner) and (y - y0) % 4 == 1 and ((x - x0) % 3 == 1 if z in (z0, z1) else (z - z0) % 3 == 1)
                    s.set(x, y, z, PANE("light_blue") if win else B(wall))
                elif (y - y0) % 4 == 3 and y < y0 + hgt - 1:
                    s.set(x, y, z, B("smooth_quartz"))       # upper floor
                else:
                    s.set(x, y, z, B("air"))
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            s.set(x, y0 + hgt, z, B("white_concrete"))
    hip_roof(s, x0 - 1, z0 - 1, x1 + 1, z1 + 1, y0 + hgt + 1, mat=roof,
             edge="light_blue_terracotta" if roof.startswith("light_blue") else roof)
    # door
    cx, cz = (x0 + x1) // 2, (z0 + z1) // 2
    dx, dz, face = {"s": (cx, z1, "south"), "n": (cx, z0, "north"), "e": (x1, cz, "east"),
                    "w": (x0, cz, "west")}[door]
    s.set(dx, y0, dz, B("birch_door", facing=face, half="lower", hinge="left", open="false", powered="false"))
    s.set(dx, y0 + 1, dz, B("birch_door", facing=face, half="upper", hinge="left", open="false", powered="false"))
    # interior
    s.set(x0 + 1, y0, z0 + 1, chest("south", loot))
    for i in range(beds):
        bx = x1 - 1 - 2 * i
        if bx > x0 + 1:
            s.set(bx, y0, z0 + 1, B("light_blue_bed", facing="north", part="head", occupied="false"))
            s.set(bx, y0, z0 + 2, B("light_blue_bed", facing="north", part="foot", occupied="false"))
    s.set(x0 + 1, y0, z1 - 1, B("potted_fern"))
    s.set(cx, y0 + 2, cz, HLANTERN())
    if floors > 1:
        for y in range(y0, y0 + 4):
            s.set(x1 - 1, y, z1 - 1, B("ladder", facing="north", waterlogged="false"))
    return y0


def pool(s, x0, z0, x1, z1, y_deck):
    for x in range(x0 - 2, x1 + 3):
        for z in range(z0 - 2, z1 + 3):
            OCCUPIED.add((x, z))
            inside = x0 <= x <= x1 and z0 <= z <= z1
            s.set(x, y_deck, z, B("water", level="0") if inside else B("smooth_quartz"))
            if inside:
                s.set(x, y_deck - 1, z, B("water", level="0"))
                s.set(x, y_deck - 2, z, B("light_blue_concrete") if (x + z) % 2 else B("cyan_terracotta"))
            for y in range(y_deck + 1, y_deck + 4):
                s.set(x, y, z, B("air"))
    for x in range(x0 - 2, x1 + 3, 3):
        for z in (z0 - 2, z1 + 2):
            s.set(x, y_deck + 1, z, STAIR("north" if z == z0 - 2 else "south", "birch"))


def ring(s, cx, cz, r, y, block):
    for x in range(cx - r - 1, cx + r + 2):
        for z in range(cz - r - 1, cz + r + 2):
            if abs(math.hypot(x - cx, z - cz) - r) < 0.6:
                s.set(x, y, z, block)


def helipad(s, cx, cz):
    R = 7
    y = pad(cx - R - 1, cz - R - 1, cx + R + 1, cz + R + 1)
    for x in range(cx - R - 1, cx + R + 2):
        for z in range(cz - R - 1, cz + R + 2):
            OCCUPIED.add((x, z))
            if math.hypot(x - cx, z - cz) <= R + 0.5:
                s.set(x, y, z, B("light_gray_concrete"))
                for yy in range(y + 1, y + 5):
                    s.set(x, yy, z, B("air"))
    ring(s, cx, cz, R - 1, y, B("white_concrete"))
    for dz in range(-3, 4):                         # painted H
        s.set(cx - 2, y, cz + dz, B("white_concrete")); s.set(cx + 2, y, cz + dz, B("white_concrete"))
    for dx in (-1, 0, 1):
        s.set(cx + dx, y, cz, B("white_concrete"))
    for a in range(0, 360, 45):
        x = cx + int(round((R + 0.4) * math.cos(math.radians(a))))
        z = cz + int(round((R + 0.4) * math.sin(math.radians(a))))
        s.set(x, y + 1, z, B("lantern", hanging="false", waterlogged="false"))


def tennis(s):
    x0, z0, x1, z1 = 92, 104, 109, 139
    y = pad(x0, z0, x1, z1)
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            OCCUPIED.add((x, z))
            court = x0 + 2 <= x <= x1 - 2 and z0 + 3 <= z <= z1 - 3
            line = court and (x in (x0 + 2, x1 - 2, x0 + 4, x1 - 4) or z in (z0 + 3, z1 - 3, z0 + 11, z1 - 11)
                              or (x == (x0 + x1) // 2 and z0 + 11 <= z <= z1 - 11))
            s.set(x, y, z, B("white_concrete") if line else B("blue_concrete") if court else B("green_concrete"))
            for yy in range(y + 1, y + 4):
                s.set(x, yy, z, B("air"))
            if x in (x0, x1) or z in (z0, z1):
                if not (x == (x0 + x1) // 2 and z == z0):
                    for yy in (y + 1, y + 2, y + 3):
                        s.set(x, yy, z, B("iron_bars", north="false", south="false", east="false", west="false",
                                          waterlogged="false"))
    zn = (z0 + z1) // 2
    for x in range(x0 + 1, x1):
        s.set(x, y + 1, zn, B("white_stained_glass_pane", north="false", south="false", east="false",
                              west="false", waterlogged="false"))
    for x in (x0 + 1, x1 - 1):
        s.set(x, y + 1, zn, SFENCE("dark_oak")); s.set(x, y + 2, zn, SFENCE("dark_oak"))


def water_tank_dome(s):
    cx, cz = HILL
    r = 7
    y = pad(cx - r - 2, cz - r - 2, cx + r + 2, cz + r + 2)
    for x in range(cx - r - 2, cx + r + 3):
        for z in range(cz - r - 2, cz + r + 3):
            OCCUPIED.add((x, z))
            s.set(x, y, z, B("smooth_stone") if math.hypot(x - cx, z - cz) <= r + 1.5 else s_top(x, z))
    for x in range(cx - r, cx + r + 1):
        for z in range(cz - r, cz + r + 1):
            for yy in range(0, r + 1):
                dd = math.sqrt((x - cx) ** 2 + (z - cz) ** 2 + (yy * 1.15) ** 2)
                if dd <= r + 0.3:
                    shell = dd > r - 1.2
                    s.set(x, y + 1 + yy, z, B("white_concrete") if shell else B("water", level="0"))
    s.set(cx, y + 1 + r + 1, cz, B("lightning_rod", facing="up", powered="false", waterlogged="false"))
    for yy in range(1, r + 1):                       # service ladder
        if s.get(cx + r + 1, y + yy, cz) is None:
            pass


def s_top(x, z):
    return B("coarse_dirt") if h2(x, z, 9) < 0.5 else B("grass_block", snowy="false")


def temple(s):
    t = gen_island_temple.build()
    F = gen_island_temple.Rigid.FOOTING
    y = pad(TEMPLE_X0, TEMPLE_Z0, TEMPLE_X0 + t.sx - 1, TEMPLE_Z0 + t.sz - 1)
    for (x, ty, z), st in t.blocks.items():
        ly = ty - F
        if ly < 0:
            continue
        e = t.palette[st]
        if (x, ty, z) in t.block_nbt:
            e = blk(str(e["Name"]), **{k: str(v) for k, v in e.get("Properties", {}).items()})
            e["_nbt"] = t.block_nbt[(x, ty, z)]
        s.set(TEMPLE_X0 + x, y + ly, TEMPLE_Z0 + z, e)
        OCCUPIED.add((TEMPLE_X0 + x, TEMPLE_Z0 + z))


def main_complex(s):
    x0, z0, x1, z1 = COMPLEX
    y = pad(x0, z0, x1, z1, margin=4)
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if LAND[x, z]:
                for yy in range(int(H[x, z]) + 1, y):
                    s.set(x, yy, z, B("dirt"))
                s.set(x, y, z, B("grass_block", snowy="false") if (x + z) % 9 else B("coarse_dirt"))
    # big pool on a quartz deck in the middle
    pool(s, 168, 28, 183, 35, y)
    # main villa (two floors) south of the pool, wings with light-blue hip roofs
    house(s, 158, 41, 22, 11, door="n", floors=2, beds=3)
    house(s, 181, 43, 9, 8, door="n", beds=1)
    # pool houses around the deck (like the aerial photo)
    house(s, 188, 24, 7, 6, door="w", beds=1)
    house(s, 188, 32, 7, 6, door="w", beds=1)
    house(s, 157, 25, 7, 6, door="e", beds=1)
    house(s, 196, 40, 7, 7, door="w", beds=1)
    # paved forecourt + low white walls
    for x in range(x0, x1 + 1):
        for z in (z0, z1):
            if LAND[x, z] and (x, z) not in OCCUPIED:
                s.set(x, y + 1, z, B("smooth_quartz_slab", type="bottom", waterlogged="false"))
    for (px, pz) in ((165, 38), (186, 38), (166, 26), (186, 30), (152, 34), (200, 34), (175, 55), (192, 54),
                     (155, 55), (203, 26)):
        if (px, pz) not in OCCUPIED:
            palm(s, px, pz, y=y + 1, trunk=6 + (px % 3))
            OCCUPIED.add((px, pz))


def utility(s):
    x, z = UTILITY
    house(s, x, z, 8, 6, door="w", roof="gray_concrete", wall="light_gray_concrete", beds=0)
    house(s, x - 1, z + 8, 7, 5, door="w", roof="gray_concrete", wall="light_gray_concrete", beds=0)


def dock(s):
    """Pier from the bay beach north into the water, with a moored boat."""
    x = 118
    zb = max(z for z in range(0, 60) if LAND[x, z] and (z == 0 or not LAND[x, z - 1]))  # first land row
    zb = min(z for z in range(SZ) if LAND[x, z])
    deck = SEA                                   # world y 63: one above the water
    for z in range(zb - 26, zb + 2):
        for dx in (-1, 0, 1):
            if z < 0:
                continue
            s.set(x + dx, deck, z, B("spruce_planks"))
            OCCUPIED.add((x + dx, z))
            if z <= zb:
                for yy in range(deck + 1, deck + 3):
                    s.set(x + dx, yy, z, B("air"))
        if (z - zb) % 4 == 0 and z >= 0:
            for dx in (-2, 2):
                for yy in range(max(0, WATER_TOP - 14), deck + 2):
                    if s.get(x + dx, yy, z) is None or yy > WATER_TOP - 14:
                        s.set(x + dx, yy, z, B("stripped_spruce_log", axis="y"))
                s.set(x + dx, deck + 2, z, B("lantern", hanging="false", waterlogged="false"))
    zt = max(zb - 26, 0)
    for dx in range(-4, 5):                     # T-head
        s.set(x + dx, deck, zt, B("spruce_planks"))
    # boat moored on the east side (block-built hull)
    bx, bz = x + 4, zt + 4
    for z in range(bz, bz + 9):
        w = 1 if z in (bz, bz + 8) else 2
        for dx in range(-w + 1, w + 1):
            s.set(bx + dx, WATER_TOP, z, B("white_concrete"))
            if z not in (bz, bz + 8) and dx in (-w + 1, w):
                s.set(bx + dx, WATER_TOP + 1, z, B("white_concrete"))
            elif z not in (bz, bz + 8):
                s.set(bx + dx, WATER_TOP + 1, z, B("air"))
    for z in range(bz + 3, bz + 6):
        for dx in (0, 1):
            s.set(bx + dx, WATER_TOP + 3, z, B("light_blue_concrete"))
    for z in (bz + 3, bz + 5):
        s.set(bx, WATER_TOP + 2, z, SFENCE("spruce"))
    s.set(bx + 1, WATER_TOP + 1, bz + 4, B("barrel", facing="up", open="false"))


def gazebo(s, cx, cz):
    y = pad(cx - 3, cz - 3, cx + 3, cz + 3, margin=2)
    for x in range(cx - 3, cx + 4):
        for z in range(cz - 3, cz + 4):
            OCCUPIED.add((x, z))
            s.set(x, y, z, B("birch_planks") if abs(x - cx) <= 2 and abs(z - cz) <= 2 else B("sand"))
    for dx, dz in ((-2, -2), (2, -2), (-2, 2), (2, 2)):
        for yy in (1, 2, 3):
            s.set(cx + dx, y + yy, cz + dz, SFENCE("birch"))
    for dx in range(-3, 4):
        for dz in range(-3, 4):
            s.set(cx + dx, y + 4, cz + dz, B("white_wool") if max(abs(dx), abs(dz)) == 3 else B("light_blue_wool"))
    s.set(cx, y + 5, cz, B("white_wool"))
    s.set(cx, y + 1, cz, SFENCE("birch")); s.set(cx, y + 2, cz, B("birch_pressure_plate", powered="false"))
    s.set(cx - 1, y + 1, cz, STAIR("east", "birch")); s.set(cx + 1, y + 1, cz, STAIR("west", "birch"))
    s.set(cx, y + 3, cz, HLANTERN())


def west_pool(s):
    x0, z0, x1, z1 = WPOOL
    y = pad(x0, z0, x1, z1)
    pool(s, x0 + 3, z0 + 6, x0 + 10, z0 + 9, y)
    house(s, x0 + 12, z0 + 3, 6, 6, door="w", beds=0)


def road_points():
    C = (176, 64)
    routes = [
        [(118, 46), (128, 52), (142, 58), (160, 62), C],                    # dock -> complex
        [C, (190, 66), HELI_E],                                              # -> east helipad
        [(204, 30), (210, 38), (212, 46)],                                   # NE point -> utility
        [C, (158, 74), (146, 84), (136, 90), (HILL[0] + 9, HILL[1])],        # -> hill dome
        [(HILL[0], HILL[1] + 9), (118, 104), (110, 110)],                    # -> tennis
        [(HILL[0] - 9, HILL[1]), (102, 92), (86, 96), (70, 104), (56, 112), (TEMPLE_X0 + 13, TEMPLE_Z0 - 1)],
        [(86, 96), (74, 86), (58, 76), (42, 72)],                            # -> west pool
        [HELI_E, (186, 92), (184, 108), (196, 122), (208, 138), (216, 150)], # SE arm
        [(96, 66), (92, 76), (86, 96)],
    ]
    pts = []
    for r in routes:
        for (ax, az), (bx, bz) in zip(r, r[1:]):
            n = int(math.hypot(bx - ax, bz - az) * 2) + 1
            for i in range(n + 1):
                t = i / n
                wig = 2.2 * math.sin(t * math.pi) * math.sin((ax + bz) * 0.7)
                nx, nz = -(bz - az), (bx - ax)
                ln = math.hypot(nx, nz) or 1
                pts.append((ax + t * (bx - ax) + wig * nx / ln, az + t * (bz - az) + wig * nz / ln))
    return pts


def roads(s):
    for px, pz in road_points():
        for dx in (-1, 0, 1):
            for dz in (-1, 0, 1):
                x, z = int(round(px)) + dx, int(round(pz)) + dz
                if 0 <= x < SX and 0 <= z < SZ and LAND[x, z] and (x, z) not in OCCUPIED:
                    ROAD.add((x, z))
    for x, z in ROAD:
        s.set(x, int(H[x, z]), z, B("coarse_dirt"))
        for yy in range(int(H[x, z]) + 1, int(H[x, z]) + 3):
            if s.get(x, yy, z) is None:
                s.set(x, yy, z, B("air"))


def vegetation(s):
    for x in range(1, SX - 1):
        for z in range(1, SZ - 1):
            if not LAND[x, z] or (x, z) in OCCUPIED or (x, z) in ROAD:
                continue
            top = s.get(x, int(H[x, z]), z)
            y = int(H[x, z]) + 1
            if y >= SY - 12 or s.get(x, y, z) is not None:
                continue
            r = h2(x, z, 7)
            near_road = any((x + a, z + b) in ROAD for a in (-1, 0, 1) for b in (-1, 0, 1))
            if top == "minecraft:sand":
                if r < 0.012 and DSEA[x, z] > 2 and not near_road:
                    palm(s, x, z, y=y, trunk=6 + int(r * 400) % 4); OCCUPIED.add((x, z))
            elif top in ("minecraft:grass_block", "minecraft:coarse_dirt"):
                if r < 0.016 and not near_road and x > 2 and z > 2 and x < SX - 3 and z < SZ - 3:
                    palm(s, x, z, y=y, trunk=5 + int(r * 600) % 5); OCCUPIED.add((x, z))
                elif r < 0.07:
                    s.set(x, y, z, B("bush"))
                elif r < 0.16:
                    s.set(x, y, z, B("short_dry_grass"))
                elif r < 0.20:
                    s.set(x, y, z, B("dead_bush"))
                elif r < 0.30 and top == "minecraft:grass_block":
                    s.set(x, y, z, B("short_grass"))
                elif r < 0.315 and top == "minecraft:grass_block":
                    s.set(x, y, z, B("orange_tulip" if h2(x, z, 8) < 0.5 else "red_tulip"))
            elif top in ("minecraft:stone", "minecraft:andesite", "minecraft:tuff"):
                if r < 0.05:
                    s.set(x, y, z, B("cobblestone") if r < 0.03 else B("mossy_cobblestone"))


TERRAIN = {"minecraft:" + n for n in ("grass_block", "dirt", "coarse_dirt", "stone", "andesite", "tuff",
                                        "mossy_cobblestone", "sand", "sandstone", "gravel")}


def no_overhangs(s):
    """Terrain blocks left above a carved-out (air/water) cell would float: remove them and their plants."""
    for x in range(SX):
        for z in range(SZ):
            cut = None
            for y in range(SY):
                n = s.get(x, y, z)
                if n in ("minecraft:air", "minecraft:water") and y > WATER_TOP - 2 and cut is None:
                    cut = y
                elif cut is not None and n in TERRAIN:
                    s.set(x, y, z, B("air"))
                    above = s.get(x, y + 1, z) if y + 1 < SY else None
                    if above and above not in TERRAIN and "log" not in above and "leaves" not in above:
                        s.set(x, y + 1, z, B("air"))


def build() -> Sparse:
    s = Sparse(SX, SY, SZ)
    # pads first (they reshape H), then terrain, then everything on top
    builders = []
    builders.append(lambda: None)
    # shape the pads by running the builders on a scratch structure first
    scratch = Sparse(SX, SY, SZ)
    for f in (main_complex, utility, tennis, water_tank_dome, temple, west_pool):
        f(scratch)
    helipad(scratch, *HELI_NE); helipad(scratch, *HELI_E)
    gazebo(scratch, *GAZEBO)
    for cx, cz, door in COTTAGES:
        house(scratch, cx, cz, 7, 6, door=door, beds=1)
    OCCUPIED.clear()
    build_terrain(s)
    # second pass writes the buildings over the final terrain (pads are already flat)
    main_complex(s); utility(s); tennis(s); water_tank_dome(s); temple(s); west_pool(s)
    helipad(s, *HELI_NE); helipad(s, *HELI_E)
    gazebo(s, *GAZEBO)
    for cx, cz, door in COTTAGES:
        house(s, cx, cz, 7, 6, door=door, beds=1)
    dock(s)
    roads(s)
    vegetation(s)
    no_overhangs(s)
    # centre anchor: the structure start is centred on this jigsaw (start_jigsaw_name), so the
    # 240x170 island stays within 8 chunks of its start chunk and is never clipped
    assert LAND[CENTER[0], CENTER[2]]
    s.set(*CENTER, jigsaw("up_north", ANCHOR, ANCHOR, "minecraft:empty", "minecraft:stone"))
    return s


def write_json() -> None:
    struct = {
        "type": "minecraft:jigsaw",
        "biomes": ["israel_simulator:tropical_island"],
        "max_distance_from_center": 116,
        "size": 1,
        "spawn_overrides": {},
        "start_height": {"absolute": BASE},
        "start_pool": f"israel_simulator:{NAME}",
        "start_jigsaw_name": ANCHOR,
        "step": "surface_structures",
        "terrain_adaptation": "beard_box",
        "use_expansion_hack": False,
    }
    pool = {"fallback": "minecraft:empty", "elements": [{"weight": 1, "element": {
        "element_type": "minecraft:single_pool_element", "location": f"israel_simulator:{NAME}",
        "processors": "minecraft:empty", "projection": "rigid"}}]}
    sset = {"structures": [{"structure": f"israel_simulator:{NAME}", "weight": 1}],
            "placement": {"type": "minecraft:random_spread", "spacing": 40, "separation": 16,
                          "salt": 734120985}}
    for rel, obj in ((f"worldgen/structure/{NAME}.json", struct),
                     (f"worldgen/template_pool/{NAME}.json", pool),
                     (f"worldgen/structure_set/{NAME}.json", sset)):
        p = DATA / rel
        p.write_text(json.dumps(obj, indent=2) + "\n")
        print("wrote", p.relative_to(DATA))


if __name__ == "__main__":
    write_json()
    s = build()
    print("blocks:", len(s.blocks), "land columns:", int(LAND.sum()))
    s.save(DATA / f"structure/{NAME}.nbt")
