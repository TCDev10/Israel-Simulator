#!/usr/bin/env python3
"""Generate government_building (urban_area), DataVersion 4903. Two variants in the start pool:

  * government_building           Knesset-style parliament: plaza with the bronze Knesset Menorah,
                                  flags and security booth; podium with a ring of tall square
                                  columns; lobby with a tapestry mural, horseshoe plenary hall with
                                  blue seats and visitor gallery, offices, library, cafeteria,
                                  skylit roof (framework marker minecraft:polished_deepslate)
  * government_building_ministry  Kiryat HaLeom ministry block: five-storey glass office tower
                                  with an atrium, staircase, open-plan offices, meeting rooms,
                                  minister's office and a rooftop helipad

Run:  /workspace/nbtvenv/bin/python scripts/worldgen/gen_government_building.py
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from jigsaw_kit import (  # noqa: E402
    AIR, B, BARREL, CHEST, DATA, DOOR2, HLANTERN, LANTERN, NS, PANE, SFENCE, SLAB, STAIR, Rigid,
)
from gen_jerusalem_city import blk, write_pool  # noqa: E402

LOOT = f"{NS}:chests/government_building"


class Civic(Rigid):
    FOOTING_BLOCK = "stone"


def h(x: int, z: int, salt: int = 0) -> int:
    return (x * 73856093 ^ z * 19349663 ^ salt * 83492791) & 0xFFFF


def paving(x, z):
    v = (x // 2 + z // 2) % 2
    return B("polished_andesite") if v else B("smooth_stone")


def flagpole(s, x, z, y=1, hgt=7) -> None:
    for dy in range(hgt):
        s.set(x, y + dy, z, B("iron_bars", north="false", south="false", east="false", west="false",
                              waterlogged="false"))
    # Israeli flag: white field, blue stripes top and bottom, blue star in the middle
    for i, dx in enumerate((1, 2, 3)):
        for k, dy in enumerate(range(hgt - 3, hgt)):
            stripe = k in (0, 2)
            star = (k == 1 and dx == 2)
            s.set(x + dx, y + dy, z, B("blue_wool") if stripe or star else B("white_wool"))


def menorah_sculpture(s, cx, z, y=1) -> None:
    """The bronze Knesset Menorah (7 branches) on a stone base."""
    for dx in range(-3, 4):
        s.set(cx + dx, y, z, B("polished_blackstone"))
    s.set(cx, y + 1, z, B("cut_copper"))
    s.set(cx, y + 2, z, B("exposed_cut_copper"))
    for dx in range(-3, 4):
        s.set(cx + dx, y + 3, z, B("weathered_cut_copper"))
    for dx in range(-3, 4):
        top = 6 if dx == 0 else 5
        for yy in range(y + 4, y + top):
            s.set(cx + dx, yy, z, B("lightning_rod", facing="up", powered="false", waterlogged="false")
                  if dx % 2 else B("weathered_copper"))
        s.set(cx + dx, y + top, z, B("copper_lantern", hanging="false", waterlogged="false")
              if "minecraft:copper_lantern" in KNOWN else B("lantern", hanging="false", waterlogged="false"))


from jigsaw_kit import KNOWN  # noqa: E402


def chair(f):
    return STAIR(f, "dark_oak")


def desk(s, x, y, z, f="south") -> None:
    s.set(x, y, z, B("dark_oak_slab", type="top", waterlogged="false"))
    s.set(x + 1, y, z, B("dark_oak_slab", type="top", waterlogged="false"))
    dz = 1 if f == "north" else -1
    if s.free(x, y, z + dz):
        s.set(x, y, z + dz, chair(f))


def knesset() -> None:
    W, D = 41, 47
    s = Civic(W, 18, D)
    cx = W // 2
    for x in range(W):
        for z in range(D):
            s.set(x, 0, z, paving(x, z))
    # --------- plaza (z 0..10)
    menorah_sculpture(s, cx, 5)
    for x in (cx - 10, cx - 6, cx + 6, cx + 10):
        flagpole(s, x - 2 if x > cx else x, 8, hgt=8)
    # security booth + barrier at the west entrance
    for x in range(2, 6):
        for z in range(1, 4):
            if x in (2, 5) or z in (1, 3):
                for y in (1, 2):
                    s.set(x, y, z, B("white_concrete") if y == 1 else PANE("light_blue"))
            s.set(x, 3, z, SLAB("smooth_stone"))
    s.set(4, 1, 3, AIR()); s.set(4, 2, 3, AIR())
    s.set(3, 1, 2, B("lever", face="floor", facing="north", powered="false"))
    s.set(6, 1, 2, B("polished_blackstone_wall", north="none", south="none", east="none", west="none",
                     up="true", waterlogged="false"))                  # barrier post
    for x in range(7, 11):
        s.set(x, 2, 2, B("red_concrete") if x % 2 else B("white_concrete"))     # lift barrier
    # planters with olive saplings / roses
    for x in list(range(3, 12, 4)) + list(range(W - 12, W - 2, 4)):
        s.set(x, 1, 10, B("stone_bricks")); s.set(x + 1, 1, 10, B("stone_bricks"))
        s.set(x, 2, 10, B("rose_bush", half="lower")); s.set(x, 3, 10, B("rose_bush", half="upper"))
        s.set(x + 1, 2, 10, B("azalea"))
    # --------- podium and steps
    PX0, PX1, PZ0, PZ1 = 3, W - 4, 13, D - 2
    for x in range(PX0, PX1 + 1):
        for z in range(PZ0, PZ1 + 1):
            s.set(x, 1, z, B("smooth_stone"))
            s.set(x, 2, z, B("polished_deepslate") if x in (PX0, PX1) or z in (PZ0, PZ1) else
                  B("smooth_quartz") if (x + z) % 2 else B("polished_diorite"))
    for x in range(cx - 7, cx + 8):
        s.set(x, 1, PZ0 - 2, B("smooth_stone_slab", type="bottom", waterlogged="false"))
        s.set(x, 1, PZ0 - 1, B("smooth_stone"))
        s.set(x, 2, PZ0 - 1, B("smooth_stone_slab", type="bottom", waterlogged="false"))
    # --------- ring of tall square columns, deep roof slab
    CX0, CX1, CZ0, CZ1 = PX0 + 1, PX1 - 1, PZ0 + 1, PZ1 - 1
    for x in range(CX0, CX1 + 1):
        for z in range(CZ0, CZ1 + 1):
            edge = x in (CX0, CX1) or z in (CZ0, CZ1)
            if edge and ((x - CX0) % 3 == 0 and z in (CZ0, CZ1) or (z - CZ0) % 3 == 0 and x in (CX0, CX1)):
                for y in range(3, 14):
                    s.set(x, y, z, B("quartz_pillar", axis="y"))
            if x in range(CX0, CX1 + 1) and z in range(CZ0, CZ1 + 1):
                s.set(x, 14, z, B("polished_deepslate") if edge else B("smooth_quartz"))
                if edge:
                    s.set(x, 15, z, B("deepslate_brick_slab", type="bottom", waterlogged="false"))
    # --------- inner box (set back): glass ground floor, stone upper floor
    IX0, IX1, IZ0, IZ1 = CX0 + 2, CX1 - 2, CZ0 + 2, CZ1 - 2
    for x in range(IX0, IX1 + 1):
        for z in range(IZ0, IZ1 + 1):
            if x in (IX0, IX1) or z in (IZ0, IZ1):
                for y in range(3, 14):
                    if y <= 7:
                        s.set(x, y, z, B("light_gray_concrete") if (x + z) % 4 == 0 or y == 7
                              else PANE("light_blue"))
                    else:
                        s.set(x, y, z, B("smooth_stone") if y % 3 else B("tinted_glass"))
    DOOR2(s, cx, 3, IZ0, "dark_oak"); DOOR2(s, cx - 1, 3, IZ0, "dark_oak")
    # second floor slab (offices/gallery), open over the plenary
    PLX0, PLX1, PLZ0, PLZ1 = IX0 + 6, IX1 - 6, IZ0 + 7, IZ1 - 7       # plenary hall footprint
    for x in range(IX0 + 1, IX1):
        for z in range(IZ0 + 1, IZ1):
            if not (PLX0 <= x <= PLX1 and PLZ0 - 2 <= z <= PLZ1):
                s.set(x, 8, z, B("smooth_stone_slab", type="top", waterlogged="false"))
    # lobby: reception, Chagall-style tapestry, benches
    for x in range(cx - 3, cx + 4):
        s.set(x, 3, IZ0 + 4, B("polished_blackstone_slab", type="top", waterlogged="false"))
    s.set(cx, 3, IZ0 + 5, chair("north"))
    mural = ["yellow_wool", "orange_wool", "red_wool", "lime_wool", "light_blue_wool", "magenta_wool",
             "blue_wool"]
    for x in range(PLX0, PLX1 + 1):
        for y in range(3, 8):
            s.set(x, y, PLZ0 - 3, B(mural[(x * 2 + y * 3 + (x * y) % 3) % len(mural)]))
    for x in (cx - 1, cx, cx + 1):
        s.set(x, 3, PLZ0 - 3, AIR()); s.set(x, 4, PLZ0 - 3, AIR())
    for x in (IX0 + 2, IX1 - 3):
        s.set(x, 3, IZ0 + 2, chair("south")); s.set(x + 1, 3, IZ0 + 2, chair("south"))
    # plenary hall: horseshoe of blue seats facing the south dais
    for x in range(PLX0, PLX1 + 1):
        for z in range(PLZ0 - 2, PLZ1 + 1):
            if x in (PLX0, PLX1) or z == PLZ1:
                for y in range(3, 14):
                    s.set(x, y, z, B("dark_oak_planks") if y < 8 else B("smooth_quartz"))
            s.set(x, 2, z, B("polished_diorite"))
    hcx, hcz = cx, PLZ1 - 3
    for x in range(PLX0 + 1, PLX1):
        for z in range(PLZ0 - 1, PLZ1):
            dx, dz = x - hcx, z - hcz
            r = (dx * dx + (dz * 1.3) ** 2) ** 0.5
            if 4 <= r < 9 and dz <= 1:
                row = int(r) - 4
                face = "south" if abs(dz) >= abs(dx) else ("east" if dx < 0 else "west")
                s.set(x, 3, z, B("blue_carpet") if row % 2 else chair(face))
            elif r < 4 and (x + z) % 2 == 0 and dz < 1:
                s.set(x, 3, z, B("blue_carpet"))
    for x in range(hcx - 3, hcx + 4):              # speaker's dais and government table
        s.set(x, 3, PLZ1 - 1, B("polished_deepslate_slab", type="bottom", waterlogged="false"))
        s.set(x, 3, PLZ1 - 3, B("dark_oak_slab", type="top", waterlogged="false"))
    s.set(hcx, 4, PLZ1 - 1, B("lectern", facing="north", has_book="false", powered="false"))
    s.set(hcx, 6, PLZ1 - 1, blk(f"{NS}:menorah", candles="7"))
    s.set(hcx, 5, PLZ1 - 1, B("polished_deepslate"))
    for x in (hcx - 2, hcx + 2):
        flagpole_small = B("blue_wool") if x < hcx else B("white_wool")
        s.set(x, 4, PLZ1 - 1, SFENCE("dark_oak")); s.set(x, 5, PLZ1 - 1, flagpole_small)
    # visitor gallery behind glass on the north side of the hall, level with the upper floor
    for x in range(PLX0 + 1, PLX1):
        s.set(x, 8, PLZ0 - 2, B("smooth_stone_slab", type="top", waterlogged="false"))
        s.set(x, 9, PLZ0 - 1, PANE())
        s.set(x, 9, PLZ0 - 2, chair("south"))
    for z in range(PLZ0, PLZ1, 3):
        s.set(hcx, 12, z, HLANTERN())
    # offices on the side wings, ground floor
    for side in (0, 1):
        x0 = IX0 + 1 if side == 0 else PLX1 + 1
        x1 = PLX0 - 1 if side == 0 else IX1 - 1
        for z in range(IZ0 + 7, IZ1, 6):
            for x in range(x0, x1 + 1):
                s.set(x, 3, z, B("white_concrete")); s.set(x, 4, z, PANE()); s.set(x, 5, z, B("white_concrete"))
                s.set(x, 6, z, B("white_concrete")); s.set(x, 7, z, B("white_concrete"))
            s.set((x0 + x1) // 2, 3, z, AIR()); s.set((x0 + x1) // 2, 4, z, AIR())
            if z + 3 < IZ1:
                desk(s, x0 + 1, 3, z + 3, "north")
                if s.free(x0, 4, z + 2):
                    s.set(x0, 4, z + 2, B("bookshelf"))
                s.set(x1, 3, z + 4, B("potted_azalea_bush"))
                s.set((x0 + x1) // 2, 7, z + 3, HLANTERN())
    # library (south-west) and cafeteria (south-east) on the upper floor
    for x in range(IX0 + 1, PLX0):
        for y in (9, 10):
            s.set(x, y, IZ1 - 1, B("bookshelf") if (x + y) % 3 else B("chiseled_bookshelf", facing="north",
                                                                         slot_0_occupied="true", slot_1_occupied="false",
                                                                         slot_2_occupied="true", slot_3_occupied="false",
                                                                         slot_4_occupied="true", slot_5_occupied="true"))
    s.set(IX0 + 3, 9, IZ1 - 4, B("lectern", facing="south", has_book="false", powered="false"))
    s.set(IX0 + 2, 9, IZ1 - 3, CHEST("north", LOOT))
    for x in range(PLX1 + 2, IX1, 3):
        s.set(x, 9, IZ1 - 3, SFENCE("birch")); s.set(x, 10, IZ1 - 3, B("birch_pressure_plate", powered="false"))
        if s.free(x - 1, 9, IZ1 - 3):
            s.set(x - 1, 9, IZ1 - 3, STAIR("east", "birch"))
    s.set(IX1 - 1, 9, IZ0 + 2, B("smoker", facing="west", lit="false"))
    s.set(IX1 - 1, 9, IZ0 + 3, BARREL("up", LOOT))
    # stairs up to the upper floor (west side of the lobby)
    for i in range(6):
        s.set(IX0 + 1, 3 + i, IZ0 + 2 + i, STAIR("south", "polished_deepslate"))
        for y in range(4 + i, 8):
            s.set(IX0 + 1, y, IZ0 + 2 + i, AIR())
    s.set(IX0 + 1, 8, IZ0 + 6, AIR()); s.set(IX0 + 1, 8, IZ0 + 5, AIR())
    # skylight over the plenary, rooftop plant
    for x in range(PLX0 + 2, PLX1 - 1):
        for z in range(PLZ0, PLZ1 - 1):
            if (x + z) % 2 == 0:
                s.set(x, 14, z, B("glass"))
    for x, z in ((CX0 + 3, CZ1 - 3), (CX0 + 5, CZ1 - 3), (CX1 - 3, CZ0 + 3)):
        s.set(x, 15, z, B("smooth_stone")); s.set(x, 16, z, B("iron_trapdoor", facing="north", half="bottom",
                                                               open="false", powered="false", waterlogged="false"))
    s.save(DATA / "structure/government_building.nbt")


def ministry() -> None:
    W, D = 33, 39
    FLOORS = 5
    s = Civic(W, FLOORS * 4 + 8, D)
    cx = W // 2
    for x in range(W):
        for z in range(D):
            s.set(x, 0, z, paving(x, z))
    # fountain and flags in the forecourt
    for x in range(cx - 3, cx + 4):
        for z in range(3, 8):
            edge = x in (cx - 3, cx + 3) or z in (3, 7)
            s.set(x, 1, z, B("polished_deepslate") if edge else B("water", level="0"))
            if not edge:
                s.set(x, 0, z, B("prismarine_bricks"))
    s.set(cx, 1, 5, B("polished_deepslate")); s.set(cx, 2, 5, B("water", level="0"))
    for x in (3, W - 7):
        flagpole(s, x, 4, hgt=8)
    # tower x 4..28, z 11..35
    X0, X1, Z0, Z1 = 4, W - 5, 11, D - 4
    AX0, AX1, AZ0, AZ1 = cx - 3, cx + 3, Z0 + 6, Z1 - 6          # atrium
    for k in range(FLOORS):
        yb = 1 + 4 * k
        for x in range(X0, X1 + 1):
            for z in range(Z0, Z1 + 1):
                edge = x in (X0, X1) or z in (Z0, Z1)
                in_atrium = AX0 <= x <= AX1 and AZ0 <= z <= AZ1
                if not (k > 0 and in_atrium):
                    s.set(x, yb, z, B("polished_deepslate") if edge else B("smooth_stone") if k == 0
                          else B("light_gray_concrete"))
                if edge:
                    col = (x - X0) % 4 == 0 if z in (Z0, Z1) else (z - Z0) % 4 == 0
                    for y in range(yb + 1, yb + 4):
                        s.set(x, y, z, B("light_gray_concrete") if col else
                              B("cyan_stained_glass") if y < yb + 3 else B("white_concrete"))
                elif in_atrium and k > 0 and (x in (AX0, AX1) or z in (AZ0, AZ1)):
                    s.set(x, yb, z, B("smooth_quartz"))
                    s.set(x, yb + 1, z, PANE())                      # atrium balustrade
        # interiors per floor
        y = yb + 1
        if k == 0:
            for x in range(cx - 2, cx + 3):                           # reception desk
                s.set(x, y, Z0 + 3, B("quartz_slab", type="top", waterlogged="false"))
            for x, z in ((X0 + 2, Z0 + 2), (X1 - 2, Z0 + 2)):
                s.set(x, y, z, B("potted_bamboo"))
            for x in range(AX0 + 1, AX1):                              # atrium garden
                for z in range(AZ0 + 1, AZ1):
                    s.set(x, yb, z, B("moss_block") if (x + z) % 3 else B("grass_block", snowy="false"))
            s.set(cx, y, (AZ0 + AZ1) // 2, B("flowering_azalea"))
            s.set(cx, y + 1, (AZ0 + AZ1) // 2, B("flowering_azalea_leaves", distance="1", persistent="true",
                                                 waterlogged="false"))
        elif k < FLOORS - 1:
            for z in range(Z0 + 2, Z1 - 1, 3):                       # open-plan desks
                for x in (X0 + 2, X1 - 3):
                    if not (AX0 - 1 <= x <= AX1 + 1 and AZ0 - 1 <= z <= AZ1 + 1):
                        desk(s, x, y, z, "north")
            for x in range(X0 + 1, X0 + 6):                          # meeting room wall
                s.set(x, y, Z1 - 5, B("white_concrete")); s.set(x, y + 1, Z1 - 5, PANE()); s.set(x, y + 2, Z1 - 5, B("white_concrete"))
            s.set(X0 + 3, y, Z1 - 5, AIR()); s.set(X0 + 3, y + 1, Z1 - 5, AIR())
            for x in range(X0 + 2, X0 + 5):
                s.set(x, y, Z1 - 3, B("spruce_slab", type="top", waterlogged="false"))
            s.set(X1 - 1, y, Z0 + 1, B("water_cauldron", level="3"))
        else:                                                         # minister's office
            for x in range(X1 - 8, X1):
                s.set(x, y, Z0 + 7, B("dark_oak_planks")); s.set(x, y + 1, Z0 + 7, B("dark_oak_planks"))
                s.set(x, y + 2, Z0 + 7, B("dark_oak_planks"))
            s.set(X1 - 4, y, Z0 + 7, AIR()); s.set(X1 - 4, y + 1, Z0 + 7, AIR())
            desk(s, X1 - 5, y, Z0 + 3, "south")
            s.set(X1 - 1, y, Z0 + 1, CHEST("west", LOOT))
            s.set(X1 - 1, y, Z0 + 5, B("bookshelf")); s.set(X1 - 1, y + 1, Z0 + 5, B("bookshelf"))
            s.set(X1 - 2, y, Z0 + 1, blk(f"{NS}:menorah", candles="0"))
            s.set(X1 - 6, y, Z0 + 1, SFENCE("dark_oak")); s.set(X1 - 6, y + 1, Z0 + 1, B("blue_wool"))
            s.set(X0 + 2, y, Z0 + 2, BARREL("up", LOOT))
        for z in range(Z0 + 3, Z1 - 2, 6):
            if not AZ0 <= z <= AZ1:
                s.set(cx, yb + 3, z, HLANTERN())
    # staircase in the NW corner of every floor, rising along +z, cut through the slab above
    for k in range(FLOORS - 1):
        yb = 1 + 4 * k
        for i in range(4):
            s.set(X0 + 1, yb + 1 + i, Z0 + 1 + i, STAIR("south", "polished_deepslate"))
            if i < 3:
                s.set(X0 + 1, yb + 4, Z0 + 1 + i, AIR())          # stairwell opening
            for y in range(yb + 2 + i, yb + 4):
                if s.get(X0 + 1, y, Z0 + 1 + i) != "minecraft:air":
                    s.set(X0 + 1, y, Z0 + 1 + i, AIR())
    DOOR2(s, cx, 2, Z0, "iron"); DOOR2(s, cx + 1, 2, Z0, "iron")
    # roof: atrium glass, parapet, helipad
    top = 1 + 4 * FLOORS
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            edge = x in (X0, X1) or z in (Z0, Z1)
            if AX0 <= x <= AX1 and AZ0 <= z <= AZ1:
                s.set(x, top, z, B("glass"))
            else:
                s.set(x, top, z, B("smooth_stone") if not edge else B("polished_deepslate"))
            if edge:
                s.set(x, top + 1, z, B("polished_deepslate_wall", north="none", south="none", east="none",
                                       west="none", up="true", waterlogged="false"))
    hx, hz = cx, Z1 - 4
    for x in range(hx - 2, hx + 3):
        for z in range(hz - 2, hz + 3):
            s.set(x, top, z, B("yellow_concrete") if (x in (hx - 1, hx + 1) or (x == hx and z == hz))
                  and abs(z - hz) <= 1 else B("gray_concrete"))
    s.set(X0 + 1, top + 1, Z0 + 1, B("lightning_rod", facing="up", powered="false", waterlogged="false"))
    s.save(DATA / "structure/government_building_ministry.nbt")


def main() -> None:
    knesset(); ministry()
    write_pool(DATA / "worldgen/template_pool/government_building.json",
               [(f"{NS}:government_building", 2, "rigid"), (f"{NS}:government_building_ministry", 1, "rigid")],
               "minecraft:empty")
    p = DATA / "worldgen/structure/government_building.json"
    data = json.loads(p.read_text())
    data["start_height"] = {"absolute": -Rigid.FOOTING}
    data["terrain_adaptation"] = "beard_box"
    data["size"] = 1
    p.write_text(json.dumps(data, indent=2) + "\n")


if __name__ == "__main__":
    main()
