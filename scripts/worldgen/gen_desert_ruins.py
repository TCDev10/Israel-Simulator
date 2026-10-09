#!/usr/bin/env python3
"""Generate the desert_ruins jigsaw (ancient Judean-desert settlement, Qumran-style), DataVersion 4903.

  * start      ruins/courtyard (rigid): cracked-stone court (framework marker), dry cistern,
               broken colonnade; the temple ruin is guaranteed on its north side
  * paths      terrain_matching sand/gravel/stone tracks with rubble
  * buildings  rigid, deterministically "decayed" ruins on the shared buried footing:
               houses, scriptorium (scroll jars), pottery workshop, watchtower, mikveh

Run:  /workspace/nbtvenv/bin/python scripts/worldgen/gen_desert_ruins.py
"""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from jigsaw_kit import (  # noqa: E402
    AIR, B, BARREL, CHEST, NS, SLAB, STAIR, Kit, Rigid, entrance_j,
)

LOOT = f"{NS}:chests/desert_ruins"
LOOT_SCROLLS = f"{NS}:chests/desert_ruins_scriptorium"


def h(x: int, z: int, salt: int = 0) -> int:
    return (x * 73856093 ^ z * 19349663 ^ salt * 83492791) & 0xFFFF


def track(x: int, z: int):
    v = h(x, z, 1) % 10
    return (B("sand") if v < 4 else B("coarse_dirt") if v < 6 else B("smooth_sandstone") if v < 8
            else B("cracked_stone_bricks") if v < 9 else B("gravel"))


def stone(x: int, y: int, z: int):
    v = h(x * 7 + y, z, 2) % 20
    return (B("stone_bricks") if v < 7 else B("cracked_stone_bricks") if v < 14 else
            B("sandstone") if v < 17 else B("cobblestone") if v < 19 else B("chiseled_stone_bricks"))


kit = Kit("desert_ruins", "ruins", porch=track)


def pot(cracked: bool = False):
    return B("decorated_pot", cracked="true" if cracked else "false", facing="north", waterlogged="false")


def ruin_walls(s: Rigid, x0, z0, x1, z1, height, salt, door=None) -> None:
    """Walls around the rectangle; each column loses 0..height-1 blocks (never below 1)."""
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if x not in (x0, x1) and z not in (z0, z1):
                continue
            if door and (x, z) in door:
                continue
            loss = h(x, z, salt) % (height + 1)
            top = max(1, height - loss) if h(x, z, salt + 9) % 4 else height
            for y in range(1, top + 1):
                s.set(x, y, z, stone(x, y, z))


def rubble(s: Rigid, x0, z0, x1, z1, salt, n=4) -> None:
    cells = [(x, z) for x in range(x0, x1 + 1) for z in range(z0, z1 + 1) if s.free(x, 1, z)]
    cells.sort(key=lambda c: h(c[0], c[1], salt))
    for i, (x, z) in enumerate(cells[:n]):
        if i % 4 == 3 and s.get(x, 0, z) == "minecraft:sand":
            s.set(x, 1, z, B("dead_bush"))
        else:
            s.set(x, 1, z, [SLAB("stone_brick"), SLAB("sandstone"), SLAB("cobblestone"), SLAB("sandstone")][i % 4])


def floor(s: Rigid, x0, z0, x1, z1, salt) -> None:
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            v = h(x, z, salt) % 6
            s.set(x, 0, z, B("sand") if v < 2 else B("smooth_sandstone") if v < 4 else stone(x, 0, z))


def front(s: Rigid, W: int) -> None:
    for x in range(W):
        s.set(x, 0, 0, track(x, 0))
    s.set(W // 2, 1, 0, entrance_j())


def column(s, x, z, height, broken=True) -> None:
    s.set(x, 1, z, B("chiseled_stone_bricks"))
    for y in range(2, height + 1):
        s.set(x, y, z, B("stone_brick_wall", north="none", south="none", east="none", west="none",
                         up="true", waterlogged="false") if y < height or not broken else SLAB("stone_brick"))


# ---------------------------------------------------------------- start
def courtyard() -> None:
    s = Rigid(15, 7, 15)
    for x in range(15):
        for z in range(15):
            s.set(x, 0, z, B("cracked_stone_bricks") if h(x, z, 3) % 3 else track(x, z))
    # dry cistern: 3x3, two deep, with steps
    for x in range(6, 9):
        for z in range(6, 9):
            s.set(x, 0, z, AIR()); s.set(x, -1, z, AIR()); s.set(x, -2, z, B("cobblestone"))
            s.set(x, -3, z, B("cobblestone")); s.set(x, -4, z, B("cobblestone"))
    s.set(7, 0, 9, STAIR("north", "stone_brick")); s.set(7, -1, 8, STAIR("north", "stone_brick"))
    s.set(6, -1, 6, pot(True)); s.set(8, -1, 6, CHEST("south", LOOT))
    for x, z, ht in ((2, 2, 4), (5, 2, 2), (9, 2, 3), (12, 2, 4), (2, 12, 3), (5, 12, 4),
                     (9, 12, 1), (12, 12, 4)):
        column(s, x, z, ht)
    for x in range(3, 7):                          # fallen column
        s.set(x, 1, 10, B("stone_bricks") if x % 2 else B("cracked_stone_bricks"))
    rubble(s, 1, 1, 13, 13, 31, n=6)
    s.set(7, 1, 0, kit.bld_j("north_up", "landmarks"))
    s.set(7, 1, 14, kit.street_j("south_up"))
    s.set(0, 1, 7, kit.street_j("west_up"))
    s.set(14, 1, 7, kit.street_j("east_up"))
    s.save(kit.struct / "courtyard.nbt")


# ---------------------------------------------------------------- paths
def paths() -> None:
    s = kit.straight("path", 11, 5, track, entrances=(2, 8))
    s.set(5, 1, 0, SLAB("stone_brick")); s.set(4, 0, 4, B("cracked_stone_bricks"))
    s.set(6, 0, 4, B("sand")); s.set(6, 1, 4, B("dead_bush"))
    s.save(kit.struct / "path_straight.nbt")
    kit.junction("path_crossroad", 5, {"n", "s", "w", "e"}, track).save(kit.struct / "path_crossroad.nbt")
    kit.junction("path_corner", 5, {"n", "e"}, track).save(kit.struct / "path_corner.nbt")
    kit.junction("path_t", 5, {"w", "e", "s"}, track).save(kit.struct / "path_t.nbt")
    kit.terminator(5, track, B("chiseled_stone_bricks")).save(kit.struct / "terminator.nbt")


# ---------------------------------------------------------------- buildings
def house_small() -> None:
    s = Rigid(7, 5, 7)
    front(s, 7); floor(s, 0, 1, 6, 6, 41)
    ruin_walls(s, 0, 1, 6, 6, 4, 41, door={(3, 1)})
    s.set(1, 1, 5, B("furnace", facing="east", lit="false")); s.set(5, 1, 5, BARREL("up", LOOT))
    s.set(5, 1, 2, pot(True)); rubble(s, 1, 2, 5, 5, 42)
    s.save(kit.struct / "house_small.nbt")


def house_large() -> None:
    s = Rigid(11, 6, 9)
    front(s, 11); floor(s, 0, 1, 10, 8, 51)
    ruin_walls(s, 0, 1, 10, 8, 5, 51, door={(5, 1)})
    for z in range(2, 8):                           # inner partition with a gap
        if z != 4:
            for y in range(1, 1 + max(1, 3 - h(5, z, 52) % 3)):
                s.set(5, y, z, stone(5, y, z))
    s.set(5, 1, 1, AIR()); s.set(5, 2, 1, AIR())
    s.set(2, 1, 7, CHEST("north", LOOT)); s.set(8, 1, 7, pot()); s.set(9, 1, 7, pot(True))
    s.set(1, 1, 2, B("cauldron")); rubble(s, 1, 2, 9, 7, 53, n=6)
    s.save(kit.struct / "house_large.nbt")


def scriptorium() -> None:
    s = Rigid(11, 5, 8)
    front(s, 11); floor(s, 0, 1, 10, 7, 61)
    ruin_walls(s, 0, 1, 10, 7, 4, 61, door={(5, 1)})
    for x in range(2, 9):                           # long plastered writing table
        s.set(x, 1, 4, SLAB("smooth_sandstone", "top"))
    s.set(3, 2, 4, B("candle", candles="2", lit="false", waterlogged="false"))
    s.set(7, 2, 4, B("candle", candles="1", lit="false", waterlogged="false"))
    s.set(5, 1, 6, B("lectern", facing="north", has_book="false", powered="false"))
    for x in (1, 2, 8, 9):                          # scroll jars
        s.set(x, 1, 6, pot(x == 2))
    s.set(1, 1, 2, CHEST("east", LOOT_SCROLLS)); s.set(9, 1, 2, BARREL("up", LOOT_SCROLLS))
    s.save(kit.struct / "scriptorium.nbt")


def pottery() -> None:
    s = Rigid(9, 4, 7)
    front(s, 9); floor(s, 0, 1, 8, 6, 71)
    ruin_walls(s, 0, 1, 8, 6, 3, 71, door={(4, 1)})
    for x, z in ((2, 4), (2, 5), (3, 5)):          # kiln
        s.set(x, 1, z, B("bricks")); s.set(x, 2, z, B("bricks"))
    s.set(3, 1, 4, B("furnace", facing="north", lit="false")); s.set(3, 2, 4, B("bricks"))
    for x in (5, 6, 7):
        s.set(x, 1, 5, pot(x == 6))
    s.set(6, 1, 3, B("clay")); s.set(7, 1, 3, BARREL("up", LOOT))
    s.save(kit.struct / "pottery_workshop.nbt")


def watchtower() -> None:
    s = Rigid(7, 10, 7)
    front(s, 7); floor(s, 0, 1, 6, 6, 81)
    for y in range(1, 10):
        for x in range(1, 6):
            for z in range(1, 6):
                if x in (1, 5) or z in (1, 5):
                    if y > 6 and h(x, z, 82 + y) % 3 == 0:
                        continue
                    if (x, z) == (3, 1) and y <= 2:
                        continue
                    s.set(x, y, z, stone(x, y, z))
    for y in range(1, 9):
        s.set(3, y, 4, B("ladder", facing="north", waterlogged="false"))
    for x in range(2, 5):
        for z in range(2, 5):
            if (x, z) != (3, 4):
                s.set(x, 5, z, B("spruce_planks"))
    s.set(2, 6, 2, CHEST("south", LOOT)); s.set(4, 1, 2, pot(True))
    s.save(kit.struct / "watchtower.nbt")


def mikveh() -> None:
    """Ritual bath: stepped plastered pool (dry, sand at the bottom) inside low walls."""
    s = Rigid(9, 4, 9)
    front(s, 9); floor(s, 0, 1, 8, 8, 91)
    ruin_walls(s, 0, 1, 8, 8, 3, 91, door={(4, 1)})
    for x in range(2, 7):
        for z in range(3, 8):
            for d in range(0, 3):
                s.set(x, -d, z, AIR())
            s.set(x, -3, z, B("sand") if (x + z) % 2 else B("smooth_sandstone"))
            s.set(x, -4, z, B("smooth_sandstone"))
        for d, z in ((0, 3), (1, 4), (2, 5)):
            s.set(x, -d - 1, z, B("smooth_sandstone_stairs", facing="north", half="bottom",
                                  shape="straight", waterlogged="false"))
    for z in range(3, 8):
        for d in range(1, 4):
            s.set(1, -d, z, B("smooth_sandstone")); s.set(7, -d, z, B("smooth_sandstone"))
    for x in range(1, 8):
        for d in range(1, 4):
            s.set(x, -d, 8, B("smooth_sandstone"))
    s.set(6, -2, 7, pot(True)); s.set(2, 1, 2, BARREL("up", LOOT))
    s.save(kit.struct / "mikveh.nbt")


def temple() -> None:
    """Landmark: raised podium, colonnade (some columns fallen), cella, altar and treasury."""
    W, D = 15, 19
    s = Rigid(W, 9, D)
    front(s, W)
    for x in range(W):
        for z in range(1, D):
            s.set(x, 0, z, B("smooth_sandstone"))
            s.set(x, 1, z, B("cut_sandstone") if (x + z) % 3 else B("chiseled_sandstone"))
    for x in range(5, 10):
        s.set(x, 1, 1, STAIR("north", "sandstone"))
    for x in (1, 13):
        for z in (3, 6, 9, 12, 15):
            ht = 3 + h(x, z, 101) % 4
            if h(x, z, 102) % 5 == 0:               # fallen: lies along x
                for i in range(3):
                    s.set(x + (1 if x == 1 else -1) * i, 2, z, B("stone_bricks"))
                s.set(x, 2, z, B("chiseled_stone_bricks"))
            else:
                s.set(x, 2, z, B("chiseled_stone_bricks"))
                for y in range(3, ht + 2):
                    s.set(x, y, z, B("stone_brick_wall", north="none", south="none", east="none",
                                     west="none", up="true", waterlogged="false"))
                if ht >= 6:
                    s.set(x, ht + 2, z, B("stone_bricks"))
    for x in range(4, 11):                          # cella
        for z in range(6, 17):
            if x in (4, 10) or z in (6, 16):
                if z == 6 and 6 <= x <= 8:
                    continue
                top = 2 + max(1, 5 - h(x, z, 103) % 5)
                for y in range(2, top + 1):
                    s.set(x, y, z, stone(x, y, z))
    s.set(7, 2, 13, B("chiseled_sandstone")); s.set(7, 3, 13, B("chiseled_stone_bricks"))
    s.set(7, 4, 13, SLAB("smooth_stone"))
    s.set(5, 2, 15, CHEST("north", LOOT)); s.set(9, 2, 15, CHEST("north", LOOT))
    for x in (6, 8):
        s.set(x, 2, 15, pot()); s.set(x, 2, 11, pot(True))
    s.set(7, 2, 8, SLAB("stone_brick"))
    s.save(kit.struct / "temple_ruin.nbt")


def main() -> None:
    courtyard(); paths(); house_small(); house_large(); scriptorium(); pottery(); watchtower()
    mikveh(); temple()
    kit.write_structure_json("israel_simulator:judean_desert", size=5, max_distance=64)
    kit.write_pools(
        "courtyard",
        streets=[("path_straight", 8, "terrain_matching"), ("path_crossroad", 2, "terrain_matching"),
                 ("path_corner", 3, "terrain_matching"), ("path_t", 3, "terrain_matching")],
        terminators=[("terminator", 1, "terrain_matching")],
        buildings=[("house_small", 4, "rigid"), ("house_large", 4, "rigid"), ("scriptorium", 2, "rigid"),
                   ("pottery_workshop", 3, "rigid"), ("watchtower", 2, "rigid"), ("mikveh", 1, "rigid")],
        landmarks=[("temple_ruin", 1, "rigid")],
    )


if __name__ == "__main__":
    main()
