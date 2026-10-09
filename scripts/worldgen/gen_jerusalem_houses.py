#!/usr/bin/env python3
"""Jerusalem house variants for the jerusalem_city buildings pool, DataVersion 4903.

Rigid pieces on the shared 4-block buried footing (jigsaw_kit), entrance at (cx,1,0):
  * house_courtyard  single-storey courtyard house with an olive tree
  * house_domed      Old City house with a stone dome on the roof
  * house_terrace    Nachlaot two-storey house, upper floor set back behind a roof terrace
  * house_templer    German Colony two-storey Templer house with a red tiled hip roof
  * house_rehavia    three-storey 1930s Rehavia apartment block with balconies
  * house_arched     narrow Old City house with arched door and iron window grilles

Called from gen_jerusalem_city.main(); `build()` returns the pool entries.
"""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from jigsaw_kit import B, NS, SLAB, Kit, house, iron_bars, olive  # noqa: E402

LOOT_HOUSE = f"{NS}:chests/jerusalem_house"
LOOT_PANTRY = f"{NS}:chests/jerusalem_pantry"


def paving(x: int, z: int):
    return B("smooth_sandstone") if (x + z) % 3 else B("cut_sandstone")


kit = Kit("jerusalem_city", "jerusalem", porch=paving)
COMMON = dict(trim="cut_sandstone", floor_block="smooth_stone", vines=False)


def dome(s, cx, cz, y, r=2, mat="smooth_sandstone") -> None:
    for dx in range(-r, r + 1):
        for dz in range(-r, r + 1):
            if abs(dx) == r and abs(dz) == r:
                continue
            s.set(cx + dx, y, cz + dz, B(mat))
    for dx in range(-r + 1, r):
        for dz in range(-r + 1, r):
            s.set(cx + dx, y + 1, cz + dz, B(mat))
    s.set(cx, y + 2, cz, B("chiseled_sandstone"))
    s.set(cx, y + 3, cz, SLAB("sandstone"))


def hip_roof(s, x0, z0, x1, z1, y) -> None:
    """Red tiled hip roof (brick stairs) over the rectangle, starting at height y."""
    i = 0
    while x0 + i <= x1 - i and z0 + i <= z1 - i:
        a, b, c, d = x0 + i, z0 + i, x1 - i, z1 - i
        if a == c or b == d:
            for x in range(a, c + 1):
                for z in range(b, d + 1):
                    s.set(x, y + i, z, B("bricks"))
            break
        for x in range(a, c + 1):
            s.set(x, y + i, b, B("brick_stairs", facing="south", half="bottom", shape="straight",
                                 waterlogged="false"))
            s.set(x, y + i, d, B("brick_stairs", facing="north", half="bottom", shape="straight",
                                 waterlogged="false"))
        for z in range(b + 1, d):
            s.set(a, y + i, z, B("brick_stairs", facing="east", half="bottom", shape="straight",
                                 waterlogged="false"))
            s.set(c, y + i, z, B("brick_stairs", facing="west", half="bottom", shape="straight",
                                 waterlogged="false"))
        i += 1


def build() -> list[tuple[str, int, str]]:
    house(kit, "house_courtyard", 11, 11, 1, "sandstone", cut=(4, 5, 6, 7), door="dark_oak",
          shutter="spruce", beds=("green",), loot=LOOT_HOUSE, pergola=False,
          extra=lambda s: (olive(s, 5, 6), s.set(4, 1, 7, B("potted_red_tulip")),
                           s.set(6, 1, 5, B("water_cauldron", level="3"))), **COMMON)

    def domed(s):
        dome(s, 4, 4, 5)
    house(kit, "house_domed", 9, 9, 1, "smooth_sandstone", door="spruce", shutter="dark_oak",
          beds=("blue",), loot=LOOT_PANTRY, pergola=False, extra=domed, **COMMON)

    house(kit, "house_terrace", 9, 11, 2, "sandstone", upper=(1, 4, 7, 9), door="spruce",
          shutter="spruce", beds=("yellow", "white"), loot=LOOT_HOUSE, **COMMON)

    def templer(s):
        hip_roof(s, 0, 0, 10, 8, 8)
        s.set(5, 6, 0, B("chiseled_sandstone"))          # date stone over the door
    house(kit, "house_templer", 11, 9, 2, "smooth_sandstone", door="dark_oak", shutter="dark_oak",
          beds=("red", "white"), loot=LOOT_HOUSE, pergola=False, extra=templer, **COMMON)

    def rehavia(s):
        for y in (5, 9):                               # cantilevered balconies on the facade
            for x in (2, 3, 7, 8):
                s.set(x, y - 1, 0, SLAB("smooth_quartz", "top"))
                s.set(x, y, 0, iron_bars())
    house(kit, "house_rehavia", 11, 9, 3, "smooth_sandstone", door="birch", shutter="oxidized_copper",
          beds=("light_gray", "cyan", "white"), loot=LOOT_HOUSE, extra=rehavia, **COMMON)

    def arched(s):
        s.set(2, 3, 1, B("sandstone_stairs", facing="east", half="top", shape="straight", waterlogged="false"))
        s.set(4, 3, 1, B("sandstone_stairs", facing="west", half="top", shape="straight", waterlogged="false"))
        for x in range(7):
            for z in range(9):
                if s.get(x, 6, z) == "minecraft:glass_pane":
                    s.set(x, 6, z, iron_bars())
                if s.get(x, 2, z) == "minecraft:glass_pane":
                    s.set(x, 2, z, iron_bars())
    house(kit, "house_arched", 7, 9, 2, "cut_sandstone", door="dark_oak", shutter=None,
          beds=("brown", "orange"), loot=LOOT_PANTRY, pergola=False, extra=arched, **COMMON)

    names = {"house_courtyard": 2, "house_domed": 3, "house_terrace": 3, "house_templer": 2,
             "house_rehavia": 2, "house_arched": 3}
    return [(f"{NS}:jerusalem/{n}", w, "rigid") for n, w in names.items()]


if __name__ == "__main__":
    for e in build():
        print(e)
