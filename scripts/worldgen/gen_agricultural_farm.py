#!/usr/bin/env python3
"""Generate the agricultural_farm jigsaw (Israeli moshav farmstead), DataVersion 4903.

  * start      farm/farmyard (rigid): packed-earth yard with hay stacks (framework marker
               minecraft:hay_block), concrete water tower, tractor, well, beehives;
               the farmhouse is guaranteed on its north side
  * lanes      terrain_matching dirt farm tracks with grass verges, fence posts, lamps
  * plots      rigid, on a buried dirt footing, entrance on the north porch row:
               wheat field, vegetable strips, vineyard, olive & citrus orchard, date grove,
               plastic hoop greenhouse (polytunnel), ridged glass greenhouse,
               irrigation reservoir with pump house, tool shed (loot), cowshed, chicken coop

Run:  /workspace/nbtvenv/bin/python scripts/worldgen/gen_agricultural_farm.py
"""
from __future__ import annotations

import math
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from jigsaw_kit import (  # noqa: E402
    AIR, B, BARREL, CHEST, CRAFT, DOOR2, HLANTERN, LANTERN, NS, PANE, SFENCE, SLAB, STAIR, Kit,
    Rigid, entrance_j, house, lamp, palm,
)
from gen_jerusalem_city import blk  # noqa: E402

LOOT = f"{NS}:chests/agricultural_farm"


class Plot(Rigid):
    """Farm pieces sit on earth, not on a sandstone plinth."""
    FOOTING_BLOCK = "dirt"


def h(x: int, z: int, salt: int = 0) -> int:
    return (x * 73856093 ^ z * 19349663 ^ salt * 83492791) & 0xFFFF


def track(x: int, z: int):
    v = h(x, z, 5) % 12
    return (B("dirt_path") if v < 6 else B("coarse_dirt") if v < 9 else B("gravel") if v < 10
            else B("rooted_dirt") if v < 11 else B("packed_mud"))


def verge(x: int, z: int):
    return B("grass_block", snowy="false") if h(x, z, 6) % 5 else B("coarse_dirt")


kit = Kit("agricultural_farm", "farm", porch=track)

FARMLAND = lambda: B("farmland", moisture="7")
WATER = lambda: B("water", level="0")
GATE = lambda f="north": B("oak_fence_gate", facing=f, in_wall="false", open="false", powered="false")
HAY = lambda axis="y": B("hay_block", axis=axis)


def mod(name: str, **p: str):
    return blk(f"{NS}:{name}", **p)


def crop(name: str, x: int, z: int, salt: int):
    """Mostly ripe crop, a few younger plants so rows don't look copy-pasted."""
    top = 3 if name == "beetroots" else 7
    v = h(x, z, salt) % 10
    age = top if v < 6 else max(0, top - 1 - v % 3)
    if name in ("wheat", "carrots", "potatoes", "beetroots"):
        return B(name, age=str(age))
    return mod(name, age=str(age))


def fruit_leaves(kind: str, x: int, z: int, y: int):
    age = 3 if h(x, z, y) % 3 else 2
    return mod(f"{kind}_leaves", age=str(age), distance="1", persistent="true", waterlogged="false")


def front(s: Rigid, W: int, gate_x: int | None = None) -> None:
    """Porch row z=0 with the entrance jigsaw; optional fence line on z=1 with a gate."""
    for x in range(W):
        s.set(x, 0, 0, track(x, 0))
    s.set(W // 2, 1, 0, entrance_j())


def fence_ring(s: Rigid, x0, z0, x1, z1, gate=None, mat="oak") -> None:
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if x in (x0, x1) or z in (z0, z1):
                if gate and (x, z) in gate:
                    s.set(x, 1, z, GATE("north" if z in (z0, z1) else "east"))
                else:
                    s.set(x, 1, z, SFENCE(mat))


def rows(s: Rigid, x0, z0, x1, z1, crops, salt, channel_every=5, axis="x") -> None:
    """Crop rows along `axis` with a water channel every `channel_every` rows."""
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            r = (z - z0) if axis == "x" else (x - x0)
            if r % channel_every == channel_every // 2:
                s.set(x, 0, z, WATER()); s.set(x, -1, z, B("dirt"))
                continue
            s.set(x, 0, z, FARMLAND())
            band = r // channel_every
            s.set(x, 1, z, crop(crops[band % len(crops)], x, z, salt))


def scarecrow(s: Rigid, x, z, facing="north") -> None:
    s.set(x, 1, z, SFENCE("spruce")); s.set(x, 2, z, HAY())
    s.set(x, 3, z, B("carved_pumpkin", facing=facing))
    for dx in (-1, 1):
        if s.free(x + dx, 2, z):
            s.set(x + dx, 2, z, SFENCE("spruce"))


def olive_tree(s: Rigid, x, z, kind="olive") -> None:
    """Gnarled olive (pale-oak bark) or round citrus tree with fruiting leaves."""
    log = "pale_oak_log" if kind == "olive" else "oak_log"
    s.set(x, 1, z, B(log, axis="y")); s.set(x, 2, z, B(log, axis="y"))
    if kind == "olive" and h(x, z, 9) % 2:
        s.set(x + 1, 3, z, B(log, axis="x"))
    s.set(x, 3, z, B(log, axis="y"))
    for dx in range(-2, 3):
        for dz in range(-2, 3):
            for y in (3, 4, 5):
                r = abs(dx) + abs(dz)
                lim = {3: 3, 4: 2, 5: 1}[y]
                if r > lim or (r == lim and h(x + dx, z + dz, y) % 3 == 0):
                    continue
                if 0 <= x + dx < s.sx and 0 <= z + dz < s.sz and s.free(x + dx, y, z + dz):
                    s.set(x + dx, y, z + dz, fruit_leaves(kind, x + dx, z + dz, y))


def date_palm(s: Rigid, x, z, trunk) -> None:
    for dy in range(trunk):
        s.set(x, 1 + dy, z, B("jungle_log", axis="y"))
    top = 1 + trunk
    s.set(x, top, z, fruit_leaves("date_palm", x, z, top))
    for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        for k in (1, 2):
            s.set(x + k * dx, top, z + k * dz, fruit_leaves("date_palm", x + k * dx, z + k * dz, top))
        s.set(x + 3 * dx, top - 1, z + 3 * dz, fruit_leaves("date_palm", x + 3 * dx, z + 3 * dz, top))
    for dx, dz in ((1, 1), (-1, 1), (1, -1), (-1, -1)):
        s.set(x + dx, top, z + dz, fruit_leaves("date_palm", x + dx, z + dz, top))
        s.set(x + 2 * dx, top - 1, z + 2 * dz, fruit_leaves("date_palm", x + 2 * dx, z + 2 * dz, top))


def ground(s: Rigid, x0, z0, x1, z1, salt, fn=verge) -> None:
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            s.set(x, 0, z, fn(x, z + salt))


# ---------------------------------------------------------------- start
def farmyard() -> None:
    S = 19
    s = Plot(S, 18, S)
    c = S // 2
    for x in range(S):
        for z in range(S):
            v = h(x, z, 11) % 9
            s.set(x, 0, z, B("packed_mud") if v < 3 else B("coarse_dirt") if v < 6 else
                  B("dirt_path") if v < 8 else B("gravel"))
    # concrete water tower (kibbutz style) in the NW corner
    for x, z in ((2, 2), (5, 2), (2, 5), (5, 5)):
        for y in range(1, 9):
            s.set(x, y, z, B("smooth_stone") if y % 4 else B("light_gray_concrete"))
    for x in range(1, 7):
        for z in range(1, 7):
            for y in range(9, 13):
                edge = x in (1, 6) or z in (1, 6)
                if edge:
                    s.set(x, y, z, B("light_gray_concrete") if y != 11 else B("white_concrete"))
                elif y == 9:
                    s.set(x, y, z, B("light_gray_concrete"))
                else:
                    s.set(x, y, z, WATER())
            s.set(x, 13, z, SLAB("smooth_stone"))
    for y in range(1, 9):                          # service ladder up a tower leg
        s.set(2, y, 6, B("ladder", facing="south", waterlogged="false"))
    # hay stacks (framework marker) along the east side
    for x in range(13, 18):
        for z in range(2, 6):
            ht = 3 if (x, z) in ((14, 3), (15, 3), (14, 4), (15, 4), (16, 3)) else 2 if (x + z) % 3 else 1
            for y in range(1, ht + 1):
                s.set(x, y, z, HAY("x" if (y + x) % 2 else "z"))
    # tractor: red body, black wheels, iron grille, seat
    tx, tz = 12, 12
    for x in range(tx, tx + 4):
        s.set(x, 2, tz, B("red_concrete")); s.set(x, 2, tz + 1, B("red_concrete"))
    s.set(tx + 3, 3, tz, B("red_concrete")); s.set(tx + 3, 3, tz + 1, B("red_concrete"))
    s.set(tx + 3, 4, tz, B("iron_bars", north="false", south="false", east="false", west="false",
                           waterlogged="false"))
    s.set(tx, 3, tz, STAIR("east", "dark_oak")); s.set(tx, 3, tz + 1, STAIR("east", "dark_oak"))
    s.set(tx + 4, 2, tz, B("iron_trapdoor", facing="east", half="bottom", open="true",
                           powered="false", waterlogged="false"))
    for x, z in ((tx, tz - 1), (tx, tz + 2)):
        s.set(x, 1, z, B("black_concrete")); s.set(x, 2, z, B("black_concrete"))
    for x, z in ((tx + 3, tz - 1), (tx + 3, tz + 2)):
        s.set(x, 1, z, B("black_concrete"))
    # trailer of hay behind the tractor
    for x in range(tx - 4, tx - 1):
        for z in (tz, tz + 1):
            s.set(x, 1, z, B("spruce_slab", type="top", waterlogged="false"))
            s.set(x, 2, z, HAY())
    # stone well with a pulley
    for x in range(3, 6):
        for z in range(12, 15):
            if (x, z) == (4, 13):
                s.set(x, 0, z, WATER()); s.set(x, -1, z, WATER()); s.set(x, -2, z, B("cobblestone"))
            else:
                s.set(x, 1, z, B("cobblestone_wall", north="none", south="none", east="none",
                                 west="none", up="true", waterlogged="false"))
    for x in (3, 5):
        s.set(x, 2, 13, SFENCE("spruce")); s.set(x, 3, 13, SFENCE("spruce"))
    for x in range(3, 6):
        s.set(x, 4, 13, SLAB("spruce"))
    s.set(4, 3, 13, HLANTERN())
    # beehives on the south edge (Israeli honey for Rosh Hashana)
    for x in (8, 10):
        s.set(x, 1, 17, B("beehive", facing="north", honey_level=str(3 + x % 3)))
    s.set(9, 1, 17, B("flowering_azalea"))
    for x in (7, 11):
        s.set(x, 1, 17, B("cornflower") if x == 7 else B("poppy"))
    s.set(1, 1, 16, B("composter", level="6")); s.set(2, 1, 16, BARREL("up"))
    for x, z in ((8, 7), (10, 7)):
        lamp(s, x, z)
    # jigsaws: farmhouse to the north, lanes to the south / west / east
    s.set(c, 1, 0, kit.bld_j("north_up", "landmarks"))
    s.set(c, 1, S - 1, kit.street_j("south_up"))
    s.set(0, 1, c, kit.street_j("west_up"))
    s.set(S - 1, 1, c, kit.street_j("east_up"))
    s.save(kit.struct / "farmyard.nbt")


# ---------------------------------------------------------------- lanes
def lanes() -> None:
    W = 7
    def paving(x, z):
        return verge(x, z) if z in (0, W - 1) else track(x, z)
    s = kit.straight("lane", 17, W, paving, entrances=(4, 12))
    for x in (1, 8, 15):
        s.set(x, 1, 0 if x != 8 else W - 1, SFENCE("spruce"))
    lamp(s, 8, 0)
    s.set(2, 1, W - 1, B("short_grass")); s.set(14, 1, 0, B("short_grass"))
    s.save(kit.struct / "lane_straight.nbt")
    s = kit.straight("lane", 11, W, paving, entrances=(5,))
    s.set(1, 1, 0, B("hay_block", axis="y")); s.set(9, 1, W - 1, B("composter", level="3"))
    s.save(kit.struct / "lane_short.nbt")
    kit.junction("lane_crossroad", W, {"n", "s", "w", "e"}, track).save(kit.struct / "lane_crossroad.nbt")
    kit.junction("lane_corner", W, {"n", "e"}, track).save(kit.struct / "lane_corner.nbt")
    kit.junction("lane_t", W, {"w", "e", "s"}, track).save(kit.struct / "lane_t.nbt")
    kit.terminator(W, track, HAY()).save(kit.struct / "terminator.nbt")


# ---------------------------------------------------------------- plots
def wheat_field() -> None:
    W, D = 17, 17
    s = Plot(W, 6, D)
    front(s, W)
    fence_ring(s, 0, 1, W - 1, D - 1, gate={(W // 2, 1)})
    for x in range(W):
        s.set(x, 0, 1, verge(x, 1)); s.set(x, 0, D - 1, verge(x, D - 1))
    for z in range(1, D):
        s.set(0, 0, z, verge(0, z)); s.set(W - 1, 0, z, verge(W - 1, z))
    s.set(W // 2, 0, 2, B("dirt_path"))
    rows(s, 1, 3, W - 2, D - 2, ["wheat"], 21, channel_every=7)
    for x in range(1, W - 1):
        s.set(x, 0, 2, B("coarse_dirt"))
    scarecrow(s, 5, 6); scarecrow(s, 11, 12)
    s.save(kit.struct / "wheat_field.nbt")


def vegetable_plots() -> None:
    W, D = 15, 15
    s = Plot(W, 5, D)
    front(s, W)
    ground(s, 0, 1, W - 1, D - 1, 0)
    rows(s, 1, 2, W - 2, D - 2, ["carrots", "tomato_crop", "potatoes", "eggplant_crop",
                                 "beetroots", "chickpea_crop"], 31, channel_every=3, axis="z")
    for x, z in ((0, 1), (W - 1, 1), (0, D - 1), (W - 1, D - 1)):
        s.set(x, 1, z, SFENCE("spruce")); s.set(x, 2, z, LANTERN())
    s.set(W // 2, 0, 1, B("dirt_path"))
    s.set(1, 1, 1, B("composter", level="7")); s.set(W - 2, 1, 1, BARREL("up"))
    s.save(kit.struct / "vegetable_plots.nbt")


def vineyard() -> None:
    W, D = 17, 15
    s = Plot(W, 5, D)
    front(s, W)
    ground(s, 0, 1, W - 1, D - 1, 3)
    for x in range(1, W - 1):
        s.set(x, 0, 1, B("dirt_path"))
    for z in range(3, D - 1):
        for x in range(1, W - 1):
            row = (x - 1) % 3
            if z == 9 and row != 1:                       # cross ditch half-way down the rows
                s.set(x, 0, z, WATER()); s.set(x, -1, z, B("dirt"))
            elif z == 9:
                s.set(x, 0, z, B("mud_bricks"))
            elif row == 1:
                s.set(x, 0, z, B("coarse_dirt") if (x + z) % 4 else B("dirt_path"))
            else:
                s.set(x, 0, z, FARMLAND())
                s.set(x, 1, z, crop("grapevine", x, z, 41))
    for x in range(1, W - 1):
        if (x - 1) % 3 != 1:
            s.set(x, 0, 2, WATER()); s.set(x, -1, 2, B("dirt"))
        else:
            s.set(x, 0, 2, B("mud_bricks"))
    # trellis posts at both ends of every row
    for x in range(1, W - 1, 3):
        for z in (2, D - 1):
            s.set(x, 1, z, SFENCE("spruce")); s.set(x, 2, z, SFENCE("spruce"))
    s.set(W - 2, 1, 1, B("barrel", facing="up", open="false"))
    s.set(W - 3, 1, 1, B("cauldron"))                         # treading vat
    s.save(kit.struct / "vineyard.nbt")


def orchard() -> None:
    W, D = 17, 17
    s = Plot(W, 8, D)
    front(s, W)
    ground(s, 0, 1, W - 1, D - 1, 7)
    for z in range(1, D):
        s.set(W // 2, 0, z, B("dirt_path") if z % 3 else B("coarse_dirt"))
    trees = [(3, 4), (3, 9), (3, 14), (13, 4), (13, 9), (13, 14)]
    for i, (x, z) in enumerate(trees):
        olive_tree(s, x, z, "olive" if i % 3 != 2 else "citrus")
        s.set(x + 1, 0, z + 1, B("rooted_dirt"))
    # drip line: a trickle of water at the foot of every tree row
    for z in (6, 11):
        for x in (1, 15):
            s.set(x, 0, z, WATER()); s.set(x, -1, z, B("dirt"))
    s.set(W // 2 + 1, 1, 2, B("composter", level="4")); s.set(W // 2 - 1, 1, 2, HAY())
    s.set(W // 2 - 1, 1, D - 2, BARREL("up"))
    s.save(kit.struct / "orchard.nbt")


def date_grove() -> None:
    W, D = 15, 15
    s = Plot(W, 11, D)
    front(s, W)
    ground(s, 0, 1, W - 1, D - 1, 9, lambda x, z: B("sand") if h(x, z) % 3 else B("coarse_dirt"))
    # irrigation basins around each palm (classic Arava date plantation)
    for i, (x, z) in enumerate([(3, 4), (11, 4), (7, 8), (3, 11), (11, 11)]):
        for dx in (-1, 0, 1):
            for dz in (-1, 0, 1):
                if (dx, dz) != (0, 0):
                    s.set(x + dx, 0, z + dz, B("mud") if (dx + dz) % 2 else B("rooted_dirt"))
        date_palm(s, x, z, 6 + i % 3)
    for z in range(1, D):
        if z < 4:
            s.set(W // 2, 0, z, B("dirt_path"))
    s.set(1, 1, 1, SFENCE("acacia")); s.set(1, 2, 1, LANTERN())
    for x, z in ((W - 2, 1), (W - 3, 1)):
        s.set(x, 1, z, B("barrel", facing="up", open="false"))
    s.save(kit.struct / "date_grove.nbt")


def polytunnel() -> None:
    """Plastic hoop greenhouse: semicircular white tunnel, tomato & eggplant beds inside."""
    W, D, R = 11, 19, 5
    s = Plot(W, R + 3, D)
    front(s, W)
    cx = W // 2
    for x in range(W):
        for z in range(1, D):
            s.set(x, 0, z, B("coarse_dirt"))
    # profile cells of a half-circle with radius R centred at (cx, 0)
    shell = set()
    for x in range(W):
        dx = abs(x - cx) + 0.5
        top = int(math.sqrt(max(0.0, (R + 0.5) ** 2 - dx * dx)))
        inner = int(math.sqrt(max(0.0, (R - 0.5) ** 2 - dx * dx))) if dx < R - 0.5 else 0
        for y in range(max(1, inner + 1), top + 1):
            shell.add((x, y))
        if dx > R - 0.5:
            for y in range(1, top + 1):
                shell.add((x, y))
    for z in range(1, D):
        rib = z in (1, D - 1) or z % 4 == 1
        for (x, y) in shell:
            if z in (1, D - 1):
                s.set(x, y, z, PANE("white"))
            else:
                s.set(x, y, z, B("white_stained_glass") if not rib else B("smooth_quartz"))
    # end walls (panes) with door openings
    for z in (1, D - 1):
        for x in range(W):
            dx = abs(x - cx) + 0.5
            top = int(math.sqrt(max(0.0, (R - 0.5) ** 2 - dx * dx)))
            for y in range(1, top + 1):
                s.set(x, y, z, PANE("white"))
    DOOR2(s, cx, 1, 1, "birch")
    s.set(cx, 1, D - 1, AIR()); s.set(cx, 2, D - 1, AIR())
    # raised beds: two long beds with a central water channel each, aisle in the middle
    for z in range(3, D - 2):
        for x in (1, 2, 3, 7, 8, 9):
            if x in (2, 8):
                s.set(x, 0, z, WATER()); s.set(x, -1, z, B("dirt"))
            else:
                s.set(x, 0, z, FARMLAND())
                s.set(x, 1, z, crop("tomato_crop" if x < cx else "eggplant_crop", x, z, 51))
        s.set(cx, 0, z, B("dirt_path"))
    for z in (5, 10, 15):
        s.set(cx, R - 1, z, HLANTERN())
    s.set(cx - 1, 1, 2, BARREL("up")); s.set(cx + 1, 1, 2, B("composter", level="5"))
    s.save(kit.struct / "polytunnel.nbt")


def glasshouse() -> None:
    """Venlo glasshouse: steel frame, three ridged glass roofs, potted herbs and melons."""
    W, D = 15, 15
    s = Plot(W, 9, D)
    front(s, W)
    x0, z0, x1, z1 = 1, 1, W - 2, D - 2
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            s.set(x, 0, z, B("smooth_stone"))
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            edge = x in (x0, x1) or z in (z0, z1)
            if not edge:
                continue
            post = (x - x0) % 4 == 0 or (z - z0) % 4 == 0 and x in (x0, x1)
            for y in range(1, 5):
                s.set(x, y, z, B("iron_block") if post and y == 4 else
                      B("light_gray_concrete") if post else PANE())
    # three ridges along z: each 4 wide, gable of glass, gutter on smooth stone slabs
    for x in range(x0, x1 + 1):
        k = (x - x0) % 4
        for z in range(z0, z1 + 1):
            if k == 0:
                s.set(x, 5, z, SLAB("smooth_stone"))
            elif k in (1, 3):
                s.set(x, 5, z, B("glass"))
            else:
                s.set(x, 5, z, B("glass")); s.set(x, 6, z, B("glass"))
        if k == 2:
            for z in (z0, z1):
                s.set(x, 6, z, B("glass"))
    DOOR2(s, W // 2, 1, z0, "iron")
    # benches with soil, crops and melons
    for z in range(z0 + 2, z1):
        for x in (x0 + 1, x0 + 2, x1 - 2, x1 - 1):
            s.set(x, 0, z, FARMLAND())
            s.set(x, 1, z, crop("sesame_crop" if x < W // 2 else "chickpea_crop", x, z, 61))
        s.set(x0 + 4, 0, z, WATER()); s.set(x0 + 4, -1, z, B("dirt"))
        s.set(x1 - 4, 0, z, WATER()); s.set(x1 - 4, -1, z, B("dirt"))
    for z in range(z0 + 3, z1 - 1, 3):
        s.set(W // 2, 1, z, B("melon") if z % 2 else B("pumpkin"))
    for z in range(z0 + 2, z1, 2):
        s.set(x0 + 5, 1, z, B("potted_fern") if z % 4 else B("potted_cactus"))
    for z in (4, 9):
        s.set(W // 2, 4, z, HLANTERN())
    s.save(kit.struct / "glasshouse.nbt")


def reservoir() -> None:
    """Lined irrigation pond (Israeli 'maagar') with a pump house and pipes."""
    W, D = 15, 15
    s = Plot(W, 7, D)
    front(s, W)
    ground(s, 0, 1, W - 1, D - 1, 13)
    for x in range(2, 13):
        for z in range(4, 13):
            edge = x in (2, 12) or z in (4, 12)
            if edge:
                s.set(x, 0, z, B("mud_bricks"))
                if (x + z) % 2 == 0:
                    s.set(x, 1, z, B("mud_brick_wall", north="none", south="none", east="none",
                                     west="none", up="true", waterlogged="false"))
            else:
                s.set(x, 0, z, WATER()); s.set(x, -1, z, WATER()); s.set(x, -2, z, B("clay"))
    s.set(5, 1, 8, B("lily_pad")); s.set(9, 1, 10, B("lily_pad"))
    # pump house
    for x in range(1, 6):
        for z in range(1, 4):
            s.set(x, 0, z, B("smooth_stone"))
            if x in (1, 5) or z in (1, 3):
                for y in (1, 2):
                    s.set(x, y, z, B("white_concrete") if y == 1 else B("light_gray_concrete"))
            s.set(x, 3, z, SLAB("smooth_stone"))
    s.set(3, 1, 1, AIR()); s.set(3, 2, 1, AIR())
    s.set(2, 1, 2, B("piston", facing="up", extended="false")); s.set(4, 1, 2, B("cauldron"))
    s.set(3, 2, 3, B("lever", face="wall", facing="north", powered="false"))
    # pipe to the pond
    for z in (4, 5):
        s.set(5, 1, z, B("cyan_terracotta"))
    for x, z in ((13, 2), (1, 13)):
        lamp(s, x, z)
    s.save(kit.struct / "reservoir.nbt")


def tool_shed() -> None:
    W, D = 9, 9
    s = Plot(W, 7, D)
    front(s, W)
    for x in range(1, W - 1):
        for z in range(1, D - 1):
            s.set(x, 0, z, B("spruce_planks") if (x + z) % 5 else B("stone"))
            if x in (1, W - 2) or z in (1, D - 2):
                corner = x in (1, W - 2) and z in (1, D - 2)
                for y in range(1, 4):
                    s.set(x, y, z, B("spruce_log", axis="y") if corner else B("spruce_planks"))
    # pitched roof (stairs + slabs), ridge along x
    for x in range(0, W):
        for z in range(1, D):
            dz = min(z - 1, D - 1 - z)
            if dz < D // 2:
                s.set(x, 4 + dz // 2, z, STAIR("south" if z < D // 2 else "north", "dark_oak")
                      if dz % 2 == 0 else SLAB("dark_oak", "top"))
            else:
                s.set(x, 4 + dz // 2, z, SLAB("dark_oak"))
    for x in (1, W - 2):
        for z in range(2, D - 2):
            s.set(x, 4, z, B("spruce_planks"))
    DOOR2(s, W // 2, 1, 1, "spruce")
    s.set(2, 2, 1, PANE()); s.set(W - 3, 2, 1, PANE())
    s.set(2, 1, D - 3, CHEST("north", LOOT)); s.set(3, 1, D - 3, B("anvil", facing="east"))
    s.set(W - 3, 1, D - 3, B("grindstone", face="floor", facing="north"))
    s.set(W - 3, 1, 3, B("smithing_table")); s.set(2, 1, 3, BARREL("up"))
    s.set(2, 2, 3, BARREL("up"))
    s.set(W // 2, 3, D // 2, HLANTERN())
    s.set(W // 2, 1, D - 3, CRAFT())
    s.save(kit.struct / "tool_shed.nbt")


def cowshed() -> None:
    """Open-sided dairy shed (refet): steel posts, shade roof, feed alley, stalls."""
    W, D = 15, 13
    s = Plot(W, 8, D)
    front(s, W)
    for x in range(W):
        for z in range(1, D):
            s.set(x, 0, z, B("smooth_stone") if 1 <= x <= W - 2 and 2 <= z <= D - 2 else verge(x, z))
    for x in range(1, W - 1, 4):
        for z in (2, D - 2):
            for y in range(1, 5):
                s.set(x, y, z, B("iron_block") if y == 4 else B("light_gray_concrete"))
    for x in range(0, W):
        for z in range(1, D):
            s.set(x, 5, z, SLAB("smooth_stone") if (x + z) % 7 else B("light_gray_stained_glass"))
    # feed alley down the middle with hay and troughs on both sides
    for x in range(2, W - 2):
        s.set(x, 0, D // 2, B("packed_mud"))
        if x % 3 == 0:
            s.set(x, 1, D // 2, HAY("x"))
    for x in range(2, W - 2):
        for z in (D // 2 - 1, D // 2 + 1):
            s.set(x, 1, z, B("cauldron") if x % 4 == 2 else B("water_cauldron", level="3") if x % 4 == 0
                  else SFENCE("spruce"))
    # stall rails along the outer sides with gates
    for x in range(1, W - 1):
        for z in (2, D - 2):
            if s.free(x, 1, z):
                s.set(x, 1, z, GATE() if x == W // 2 else SFENCE("spruce"))
    for x in (3, 11):
        s.set(x, 1, 3, B("hay_block", axis="y")); s.set(x, 1, D - 3, B("hay_block", axis="y"))
    for x, z in ((1, 1), (W - 2, 1)):
        s.set(x, 1, z, B("composter", level="8"))
    s.set(W // 2, 4, D // 2, HLANTERN())
    s.save(kit.struct / "cowshed.nbt")


def chicken_coop() -> None:
    W, D = 11, 9
    s = Plot(W, 6, D)
    front(s, W)
    ground(s, 0, 1, W - 1, D - 1, 17)
    fence_ring(s, 0, 1, W - 1, D - 1, gate={(W // 2, 1)}, mat="birch")
    for x in range(5, W - 1):
        for z in range(3, D - 1):
            s.set(x, 0, z, B("birch_planks"))
            if x in (5, W - 2) or z in (3, D - 2):
                for y in (1, 2):
                    s.set(x, y, z, B("birch_planks") if y == 1 else B("birch_fence", north="false",
                          south="false", east="false", west="false", waterlogged="false"))
            s.set(x, 3, z, SLAB("birch"))
    s.set(5, 1, 5, AIR()); s.set(5, 2, 5, AIR())
    for x in range(6, W - 2):
        s.set(x, 1, D - 3, HAY("x"))
    s.set(2, 1, 4, B("water_cauldron", level="2")); s.set(2, 1, 6, B("composter", level="2"))
    for x, z in ((1, 3), (3, 7)):
        s.set(x, 0, z, B("coarse_dirt"))
    s.set(1, 1, 2, B("dandelion")); s.set(3, 1, 5, B("short_grass"))
    s.save(kit.struct / "chicken_coop.nbt")


def farmhouse() -> None:
    # reuse the parametric whitewashed house (Jerusalem/Jaffa kit), with a farm loot chest
    house(kit, "farmhouse", 13, 11, 2, "white_concrete", upper=(1, 1, 7, 9), door="spruce",
          shutter="spruce", trim="smooth_quartz", beds=("green", "lime"), loot=LOOT, pergola=True,
          vines=True, floor_block="spruce_planks")


def main() -> None:
    farmyard(); lanes(); wheat_field(); vegetable_plots(); vineyard(); orchard(); date_grove()
    polytunnel(); glasshouse(); reservoir(); tool_shed(); cowshed(); chicken_coop(); farmhouse()
    kit.write_structure_json("israel_simulator:israeli_agriculture", size=5, max_distance=72)
    kit.write_pools(
        "farmyard",
        streets=[("lane_straight", 8, "terrain_matching"), ("lane_short", 4, "terrain_matching"),
                 ("lane_crossroad", 2, "terrain_matching"), ("lane_corner", 3, "terrain_matching"),
                 ("lane_t", 3, "terrain_matching")],
        terminators=[("terminator", 1, "terrain_matching")],
        buildings=[("wheat_field", 4, "rigid"), ("vegetable_plots", 4, "rigid"), ("vineyard", 3, "rigid"),
                   ("orchard", 3, "rigid"), ("date_grove", 2, "rigid"), ("polytunnel", 3, "rigid"),
                   ("glasshouse", 2, "rigid"), ("reservoir", 2, "rigid"), ("tool_shed", 2, "rigid"),
                   ("cowshed", 2, "rigid"), ("chicken_coop", 2, "rigid")],
        landmarks=[("farmhouse", 1, "rigid")],
    )


if __name__ == "__main__":
    main()
