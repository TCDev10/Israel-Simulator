"""Exterior dressing for the single-piece buildings in gen_remaining_structures.py.

Each function adds a facade/roofline/surroundings to an already-built interior without
touching the framework marker or the loot containers.
"""
from __future__ import annotations

import math

from structure_lib import blk


def B(name: str, **p):
    return blk(f"minecraft:{name}", **p)


def stair(mat, facing, half="bottom"):
    return B(f"{mat}_stairs", facing=facing, half=half, shape="straight", waterlogged="false")


def slab(mat, t="bottom"):
    return B(f"{mat}_slab", type=t, waterlogged="false")


def wallb(mat):
    return B(f"{mat}_wall", up="true", north="none", south="none", east="none", west="none", waterlogged="false")


def pane(color=None):
    n = f"{color}_stained_glass_pane" if color else "glass_pane"
    return B(n, north="false", south="false", east="false", west="false", waterlogged="false")


def lantern(hanging="false"):
    return B("lantern", hanging=hanging, waterlogged="false")


def cornice(s, x0, z0, x1, z1, y, mat) -> None:
    """Upside-down stairs one block outside the wall ring."""
    for x in range(x0, x1 + 1):
        s.set(x, y, z0 - 1, stair(mat, "south", "top")); s.set(x, y, z1 + 1, stair(mat, "north", "top"))
    for z in range(z0, z1 + 1):
        s.set(x0 - 1, y, z, stair(mat, "east", "top")); s.set(x1 + 1, y, z, stair(mat, "west", "top"))


def parapet(s, x0, z0, x1, z1, y, wall_mat, merlon) -> None:
    for x in range(x0, x1 + 1):
        for z in (z0, z1):
            s.set(x, y, z, B(merlon) if x % 3 == 0 else wallb(wall_mat))
    for z in range(z0, z1 + 1):
        for x in (x0, x1):
            s.set(x, y, z, B(merlon) if z % 3 == 0 else wallb(wall_mat))


def dome(s, cx, cz, y0, r, shell, finial=None, drum=0, drum_mat=None, window=None) -> int:
    """Drum (rings of height `drum`) + hemispherical shell; returns the top y."""
    def ring(y, rad, mat, win=False):
        for x in range(int(cx - rad - 1), int(cx + rad + 2)):
            for z in range(int(cz - rad - 1), int(cz + rad + 2)):
                d = math.hypot(x - cx, z - cz)
                if rad - 1 < d <= rad:
                    ang = math.atan2(z - cz, x - cx)
                    s.set(x, y, z, window if win and window and int((ang + math.pi) / (math.pi / 6)) % 2 == 0
                          else mat)
    for k in range(drum):
        ring(y0 + k, r, B(drum_mat), win=(k == drum // 2))
    base = y0 + drum
    for k in range(r + 1):
        rad = math.sqrt(max(r * r - k * k, 0)) + 0.5
        if rad <= 1.0:
            s.set(int(round(cx)), base + k, int(round(cz)), B(shell))
            continue
        ring(base + k, rad, B(shell))
        if k == r - 1 or rad < 2:
            for x in range(int(cx - rad), int(cx + rad) + 1):
                for z in range(int(cz - rad), int(cz + rad) + 1):
                    if math.hypot(x - cx, z - cz) <= rad:
                        s.set(x, base + k, z, B(shell))
    top = base + r
    if finial:
        for i, f in enumerate(finial):
            s.set(int(round(cx)), top + 1 + i, int(round(cz)), f)
        top += len(finial)
    return top


# ---------------------------------------------------------------- synagogue
def synagogue(s, fy: int) -> None:
    x0, z0, x1, z1, h = 2, 2, 23, 23, 9
    cornice(s, x0, z0, x1, z1, fy + h, "sandstone")
    parapet(s, x0, z0, x1, z1, fy + h + 2, "sandstone", "chiseled_sandstone")
    for tx, tz in ((x0, z0), (x1 - 1, z0), (x0, z1 - 1), (x1 - 1, z1 - 1)):     # corner turrets
        for y in range(fy + h + 1, fy + h + 4):
            for dx in (0, 1):
                for dz in (0, 1):
                    s.set(tx + dx, y, tz + dz, B("cut_sandstone"))
        for dx in (0, 1):
            for dz in (0, 1):
                s.set(tx + dx, fy + h + 4, tz + dz, slab("smooth_sandstone"))
    dome(s, 12.5, 12.5, fy + h + 2, 6, "smooth_quartz", drum=3, drum_mat="cut_sandstone",
         window=pane("light_blue"), finial=[B("chiseled_quartz_block"), B("lightning_rod", facing="up",
                                                                           powered="false", waterlogged="false")])
    # west portico in front of the doors (x0, z12..13)
    for z in (10, 15):
        for y in range(fy + 1, fy + 5):
            s.set(0, y, z, B("quartz_pillar", axis="y"))
    for z in range(9, 17):
        for x in (0, 1):
            s.set(x, fy + 5, z, B("cut_sandstone"))
        s.set(0, fy + 6, z, slab("sandstone"))
    s.set(1, fy + 4, 11, lantern("true")); s.set(1, fy + 4, 14, lantern("true"))
    for z in (12, 13):                                                     # arched door head
        s.set(x0, fy + 3, z, B("chiseled_sandstone"))
    # rose window high on the west facade
    for z in range(10, 16):
        for y in range(fy + 6, fy + 10):
            if math.hypot(z - 12.5, y - (fy + 7.5)) <= 2.2:
                s.set(x0, y, z, pane("blue") if (z + y) % 2 else pane("light_blue"))


# ---------------------------------------------------------------- great synagogue
def great_synagogue(s, fy: int) -> None:
    x0, z0, x1, z1, h = 3, 3, 32, 32, 14
    cornice(s, x0, z0, x1, z1, fy + h, "quartz")
    parapet(s, x0, z0, x1, z1, fy + h + 2, "diorite", "chiseled_quartz_block")
    # big copper dome on a windowed drum over the bimah
    for x in range(14, 22):
        for z in range(14, 22):
            s.set(x, fy + h + 2, z, B("air"))
    dome(s, 17.5, 17.5, fy + h + 2, 8, "weathered_copper", drum=3, drum_mat="smooth_quartz",
         window=pane("blue"), finial=[B("gold_block"), B("lightning_rod", facing="up", powered="false",
                                                         waterlogged="false")])
    # twin front towers with small domes
    for tx in (x0, x1 - 3):
        for y in range(fy + h + 1, fy + h + 7):
            for dx in range(4):
                for dz in range(4):
                    if dx in (0, 3) or dz in (0, 3):
                        win = y == fy + h + 4 and (dx in (1, 2) or dz in (1, 2))
                        s.set(tx + dx, y, z0 + dz, pane("cyan") if win else B("quartz_block"))
        for dx in range(4):
            for dz in range(4):
                s.set(tx + dx, fy + h + 7, z0 + dz, B("smooth_quartz"))
        dome(s, tx + 1.5, z0 + 1.5, fy + h + 8, 2, "weathered_copper", finial=[B("gold_block")])
    # columned portico with a pediment and the tablets of the law
    for x in (14, 21):
        for y in range(fy + 1, fy + 8):
            s.set(x, y, 0, B("quartz_pillar", axis="y"))
    for x in range(13, 23):
        for z in range(0, 3):
            s.set(x, fy + 8, z, B("smooth_quartz"))
    for i in range(5):
        for x in range(13 + i, 23 - i):
            s.set(x, fy + 9 + i, 0, B("chiseled_quartz_block") if i == 4 else stair("quartz", "south")
                  if x in (13 + i, 22 - i) else B("quartz_block"))
    s.set(17, fy + 11, 0, B("chiseled_quartz_block")); s.set(18, fy + 11, 0, B("chiseled_quartz_block"))
    for x in (15, 20):
        s.set(x, fy + 7, 1, lantern("true"))


# ---------------------------------------------------------------- historical house
def historical_house(s, fy: int) -> None:
    hx0, hz0, hx1, hz1, hh = 9, 9, 21, 21, 6
    # replace the stubby roof dome by a proper Old City dome
    for x in range(13, 18):
        for z in range(13, 18):
            s.set(x, fy + hh + 2, z, B("air")); s.set(x, fy + hh + 3, z, B("air"))
    dome(s, 15, 15, fy + hh + 2, 3, "smooth_sandstone", finial=[slab("sandstone")])
    # arched window heads and green iron shutters
    for wx in (12, 15, 18):
        for z, oz, f in ((hz0, hz0 - 1, "north"), (hz1, hz1 + 1, "south")):
            s.set(wx, fy + 4, z, B("chiseled_sandstone"))
            for sx in (wx - 1, wx + 1):
                s.set(sx, fy + 3, oz, B("oxidized_copper_trapdoor", facing=f, half="bottom", open="true",
                                        powered="false", waterlogged="false"))
    for wz in (12, 15, 18):
        for x, ox, f in ((hx0, hx0 - 1, "west"), (hx1, hx1 + 1, "east")):
            s.set(x, fy + 4, wz, B("chiseled_sandstone"))
            for sz in (wz - 1, wz + 1):
                s.set(ox, fy + 3, sz, B("oxidized_copper_trapdoor", facing=f, half="bottom", open="true",
                                        powered="false", waterlogged="false"))
    # arched doorway with a lamp
    s.set(10, fy + 3, hz0, stair("sandstone", "east", "top")); s.set(12, fy + 3, hz0, stair("sandstone", "west", "top"))
    s.set(11, fy + 3, hz0, B("chiseled_sandstone")); s.set(10, fy + 4, hz0 - 1, lantern())
    # external stone staircase to the roof along the east wall
    for i in range(hh + 1):
        y, z = fy + 1 + i, hz0 + 1 + i
        s.set(hx1 + 1, y, z, stair("sandstone", "south"))
        for yy in range(fy + 1, y):
            s.set(hx1 + 1, yy, z, B("sandstone"))
    s.set(hx1, fy + hh + 2, hz0 + hh + 1, B("air"))                    # gap in the roof railing
    # bougainvillea over the courtyard-facing walls, pots by the door
    for x in range(hx0, hx0 + 4):
        for y in range(fy + 3, fy + hh + 1):
            if (x + y) % 2 == 0:
                s.set(x, y, hz0 - 1, B("flowering_azalea_leaves", distance="1", persistent="true",
                                       waterlogged="false"))
    s.set(13, fy + 1, hz0 - 1, B("potted_red_tulip")); s.set(9, fy + 1, hz0 - 1, B("potted_cactus"))


# ---------------------------------------------------------------- startup office
def startup_office(s, fy: int) -> None:
    x0, z0, x1, z1, h = 2, 2, 23, 23, 10
    # entrance canopy and glowing sign
    for x in range(9, 17):
        for z in range(0, 2):
            s.set(x, fy + 4, z, slab("smooth_quartz", "top"))
    for x in (9, 16):
        for y in range(fy + 1, fy + 4):
            s.set(x, y, 0, B("iron_bars", north="false", south="false", east="false", west="false",
                             waterlogged="false"))
    for x in range(8, 18):
        s.set(x, fy + h, z0 - 1, B("sea_lantern") if x % 3 == 0 else B("light_blue_concrete"))
    # vertical sun fins on the upper storey
    for x in range(x0 + 3, x1 - 2, 3):
        if 9 <= x <= 16:
            continue
        for y in range(fy + 6, fy + h):
            s.set(x, y, z0 - 1, B("white_concrete"))
    # plaza: planters with trees, benches, bike rack
    for x, z in ((24, 1), (1, 24), (24, 24)):   # (1,1) holds the framework marker
        s.set(x, fy, z, B("moss_block")); s.set(x, fy + 1, z, B("flowering_azalea"))
    for x in (4, 6, 19, 21):
        s.set(x, fy + 1, 0, stair("smooth_quartz", "south"))
    for z in range(5, 10):
        s.set(0, fy + 1, z, B("iron_bars", north="true", south="true", east="false", west="false",
                              waterlogged="false"))
    # rooftop terrace: glass railing, umbrella tables, antenna mast
    for x in range(x0, x1 + 1):
        s.set(x, fy + h + 2, z0, pane()); s.set(x, fy + h + 2, z1, pane())
    for z in range(z0, z1 + 1):
        s.set(x0, fy + h + 2, z, pane()); s.set(x1, fy + h + 2, z, pane())
    for x, z, c in ((11, 11, "orange"), (14, 14, "lime")):
        s.set(x, fy + h + 2, z, B("spruce_fence", north="false", south="false", east="false", west="false",
                                  waterlogged="false"))
        s.set(x, fy + h + 3, z, B("spruce_fence", north="false", south="false", east="false", west="false",
                                  waterlogged="false"))
        for dx in (-1, 0, 1):
            for dz in (-1, 0, 1):
                s.set(x + dx, fy + h + 4, z + dz, B(f"{c}_carpet") if (dx, dz) != (0, 0) else B(f"{c}_wool"))
    for y in range(fy + h + 2, fy + h + 5):
        s.set(21, y, 21, B("iron_bars", north="false", south="false", east="false", west="false",
                           waterlogged="false"))
    s.set(21, fy + h + 5, 21, B("lightning_rod", facing="up", powered="false", waterlogged="false"))
