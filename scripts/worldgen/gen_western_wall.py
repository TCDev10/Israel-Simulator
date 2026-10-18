#!/usr/bin/env python3
"""Generate the Western Wall (Kotel) plaza, jerusalem biome, DataVersion 4903.

One rigid piece (64 x 48 footprint), floor at y=0 above a 4-block buried footing:

  * the wall       Herodian courses of LARGE pale ashlars (2 high, 3-7 wide, running bond,
                   mixed western_wall_stone / jerusalem_stone / white terracotta / smooth sandstone so the
                   joints read), then smaller Umayyad courses set back one block, then small
                   Ottoman courses with hyssop, capers and vines on the ledges; cypresses on top
  * prayer area    raised strip along the wall, men's section with shtenders (lecterns), plastic
                   chairs and book tables, mechitza (fence + lanterns) to the women's section
  * Wilson's Arch  vaulted prayer hall on the north end: Torah ark, bookshelves, benches,
                   chandeliers; the treasury chest (chests/western_wall_treasury) in a niche
  * Mughrabi ramp  covered wooden ramp on the south end climbing to a gate high in the wall,
                   over an archaeological garden with column drums
  * plaza          paved Jerusalem stone, hand-washing basins, the big Hanukkah menorah,
                   Israeli flags, lamp posts, a stepped upper terrace and a security checkpoint

Prayer is a right-click on israel_simulator:western_wall_stone (ModGameEvents), so the lowest,
reachable courses are mostly that block.

Run:  /workspace/nbtvenv/bin/python scripts/worldgen/gen_western_wall.py
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from jigsaw_kit import (  # noqa: E402
    AIR, B, CHEST, DATA, HLANTERN, LANTERN, LEAF, NS, SFENCE, SLAB, STAIR, WALL, Rigid, olive,
)
from gen_jerusalem_city import blk, write_pool  # noqa: E402
from gen_government_building import flagpole  # noqa: E402

LOOT = f"{NS}:chests/western_wall_treasury"
SX, SZ, SY = 64, 48, 22
WALL_TOP = 19                     # last solid course
FRONT = 4                         # plaza-facing face of the Herodian courses (z)


class Kotel(Rigid):
    FOOTING_BLOCK = "sandstone"


def h(*v: int) -> int:
    r = 2166136261
    for n in v:
        r = ((r ^ (n & 0xFFFFFFFF)) * 16777619) & 0xFFFFFFFF
    return r


WWS = lambda: blk(f"{NS}:western_wall_stone")
JS = lambda: blk(f"{NS}:jerusalem_stone")
PALE = ["western_wall", "jerusalem", "smooth_sandstone", "jerusalem", "white_terracotta"]


def stone(kind: str):
    return {"western_wall": WWS, "jerusalem": JS}.get(kind, lambda: B(kind))()


# ------------------------------------------------------------------ the wall
def courses():
    """(y0, height, min_w, max_w, depth_front) from the ground up."""
    out = []
    y = 0
    while y < 12:                         # six Herodian courses of big stones
        out.append((y, 2, 4, 7, FRONT)); y += 2
    while y < 16:                         # Umayyad: one block high, set back one
        out.append((y, 1, 2, 4, FRONT - 1)); y += 1
    while y <= WALL_TOP:                  # Ottoman: small stones, set back again
        out.append((y, 1, 1, 2, FRONT - 2)); y += 1
    return out


def wall(s: Kotel, gate_x: tuple[int, int], gate_y: int) -> None:
    prev: dict[int, str] = {}
    for ci, (y0, hgt, wmin, wmax, zf) in enumerate(courses()):
        x = -(h(ci, 7) % wmax)           # running bond: every course starts at its own offset
        row: dict[int, str] = {}
        last = None
        k = 0
        while x < SX:
            w = wmin + h(ci, k) % (wmax - wmin + 1)
            low = y0 < 4                   # reachable prayer courses: mostly western wall stone
            choices = (["western_wall"] * 5 + ["jerusalem", "smooth_sandstone"]) if low else \
                (["western_wall"] * 3 + PALE[1:]) if hgt == 2 else \
                ["jerusalem", "smooth_sandstone", "jerusalem", "cut_sandstone", "sandstone"]
            kind = choices[h(ci, k, 3) % len(choices)]
            mid = prev.get(min(max(x + w // 2, 0), SX - 1))
            if kind == last or (kind == mid and not low):
                kind = choices[(choices.index(kind) + 1) % len(choices)]
                if kind == last:
                    kind = choices[(choices.index(kind) + 1) % len(choices)]
            for xx in range(max(x, 0), min(x + w, SX)):
                row[xx] = kind
                for y in range(y0, y0 + hgt):
                    for z in range(0, zf + 1):
                        s.set(xx, y, z, stone(kind) if z == zf else JS() if (xx + y + z) % 3 else B("sandstone"))
            last = kind
            x += w
            k += 1
        prev = row
    # ledges where the wall steps back: hyssop, capers, grass; vines down the face
    for x in range(SX):
        if h(x, 1) % 5 == 0:
            s.set(x, 12, FRONT, B("fern") if h(x, 2) % 2 else B("short_grass"))
        if h(x, 3) % 6 == 0:
            s.set(x, 16, FRONT - 1, B("sweet_berry_bush", age="1") if h(x, 4) % 3 == 0 else B("fern"))
        if h(x, 5) % 11 == 0:
            for y in range(13 + h(x, 6) % 2, 16):
                s.set(x, y, FRONT, B("vine", north="true", south="false", east="false", west="false", up="false"))
    # coping and cypresses of the Temple Mount esplanade peeking over the top
    for x in range(SX):
        s.set(x, WALL_TOP + 1, 0, SLAB("sandstone"))
        s.set(x, WALL_TOP + 1, 1, SLAB("sandstone") if x % 2 else B("air"))
    for x in (9, 30, 47):
        s.set(x, WALL_TOP + 1, 0, B("spruce_log", axis="y"))
        s.set(x, WALL_TOP + 2, 0, LEAF("spruce"))
        if x + 1 < SX:
            s.set(x + 1, WALL_TOP + 1, 0, LEAF("spruce"))
        s.set(x - 1, WALL_TOP + 1, 0, LEAF("spruce"))
    # Mughrabi gate high in the wall at the head of the ramp
    gx0, gx1 = gate_x
    for x in range(gx0, gx1 + 1):
        for y in range(gate_y, gate_y + 3):
            for z in range(FRONT - 2, FRONT + 1):
                s.set(x, y, z, AIR())
        s.set(x, gate_y - 1, FRONT - 2, B("spruce_planks"))
        s.set(x, gate_y + 3, FRONT, B("chiseled_sandstone"))
    for y in range(gate_y, gate_y + 2):
        for x in range(gx0, gx1 + 1):
            s.set(x, y, FRONT - 3, B("spruce_planks"))    # gate leaves, shut


# ------------------------------------------------------------------ Wilson's Arch hall
def wilsons_arch(s: Kotel, X0: int, X1: int, Z1: int) -> None:
    H = 7
    for z in range(FRONT + 1, Z1 + 1):
        for x in range(X0, X1 + 1):
            s.set(x, 0, z, B("polished_diorite") if (x + z) % 2 else B("smooth_sandstone"))
            for y in range(1, H + 2):
                s.set(x, y, z, AIR())
        # side walls and the barrel vault
        for y in range(1, H):
            s.set(X0, y, z, JS()); s.set(X1, y, z, JS())
        for x in range(X0, X1 + 1):
            d = min(x - X0, X1 - x)
            top = H + (1 if d >= 2 else 0) + (1 if d >= 4 else 0)
            s.set(x, top, z, JS() if (x + z) % 4 else B("chiseled_sandstone"))
            for y in range(H, top):
                s.set(x, y, z, JS())
            if d == 1:
                s.set(x, H, z, STAIR("west" if x - X0 == 1 else "east", "sandstone", "top"))
        if (z - FRONT) % 4 == 2:
            s.set((X0 + X1) // 2, H + 1, z, HLANTERN())
            s.set((X0 + X1) // 2 - 2, H, z, HLANTERN())
            s.set((X0 + X1) // 2 + 2, H, z, HLANTERN())
    # arch mouth facing the plaza: two rows of voussoirs
    for x in range(X0, X1 + 1):
        s.set(x, H + 1, Z1, B("chiseled_sandstone") if x % 2 else B("cut_sandstone"))
    # Torah ark against the wall (inside the hall the wall face is still prayer stone)
    cx = (X0 + X1) // 2
    for x in range(cx - 1, cx + 2):
        s.set(x, 1, FRONT + 1, B("dark_oak_planks"))
        s.set(x, 2, FRONT + 1, B("dark_oak_trapdoor", facing="south", half="bottom", open="true",
                                   powered="false", waterlogged="false"))
        s.set(x, 3, FRONT + 1, B("dark_oak_trapdoor", facing="south", half="bottom", open="true",
                                   powered="false", waterlogged="false"))
        s.set(x, 4, FRONT + 1, B("red_wool") if x == cx else B("dark_oak_planks"))
    s.set(cx, 5, FRONT + 1, blk(f"{NS}:menorah", candles="0"))
    s.set(cx, 1, FRONT + 3, B("lectern", facing="north", has_book="true", powered="false"))
    for z in (FRONT + 1, FRONT + 2):
        for y in (1, 2, 3):
            s.set(X0 + 1, y, z, B("bookshelf")); s.set(X1 - 1, y, z, B("bookshelf"))
    for z in range(FRONT + 5, Z1 - 1, 2):
        for x in range(X0 + 2, X1 - 1):
            if x != cx:
                s.set(x, 1, z, STAIR("north", "dark_oak"))
    # treasury niche in the left side wall, behind a bookshelf column
    s.set(X0, 1, FRONT + 4, CHEST("east", LOOT))
    s.set(X0, 2, FRONT + 4, B("chiseled_sandstone"))


# ------------------------------------------------------------------ Mughrabi ramp
def mughrabi(s: Kotel, X0: int, gate_y: int) -> None:
    """Covered timber ramp climbing toward the wall (-z) to the gate at gate_y."""
    z_top = FRONT + 1
    steps = gate_y - 1
    z_start = z_top + steps * 2 + 1
    for i in range(z_start - z_top + 1):
        z = z_start - i
        y = 1 + i // 2 if i // 2 < steps else gate_y - 1
        for x in range(X0, X0 + 4):
            s.set(x, y, z, B("spruce_planks") if i % 2 == 0 or y == gate_y - 1 else
                  B("spruce_slab", type="top", waterlogged="false"))
        # rails and roof
        for x in (X0 - 1, X0 + 4):
            s.set(x, y + 1, z, SFENCE())
            if z % 3 == 0:
                for yy in range(y + 2, y + 4):
                    s.set(x, yy, z, SFENCE())
                s.set(x, y + 4, z, SLAB("dark_oak", "top"))
        for x in range(X0 - 1, X0 + 5):
            s.set(x, y + 5, z, SLAB("dark_oak"))
        # trestle posts down to the plaza
        if z % 4 == 0:
            for yy in range(1, y):
                s.set(X0, yy, z, B("stripped_spruce_log", axis="y"))
                s.set(X0 + 3, yy, z, B("stripped_spruce_log", axis="y"))
    # archaeological garden under the ramp: column drums, rubble, a cistern opening
    for x, z, n in ((X0 - 3, z_top + 6, 2), (X0 + 6, z_top + 10, 1), (X0 - 2, z_top + 14, 3),
                    (X0 + 6, z_top + 3, 1)):
        for yy in range(1, 1 + n):
            s.set(x, yy, z, B("smooth_quartz") if yy % 2 else B("calcite"))
    for x, z in ((X0 + 1, z_top + 9), (X0 + 2, z_top + 13), (X0 - 3, z_top + 11)):
        s.set(x, 1, z, B("cobblestone_slab", type="bottom", waterlogged="false"))
    s.set(X0 + 6, 1, z_top + 15, B("mossy_cobblestone"))


# ------------------------------------------------------------------ plaza
def paving(x: int, z: int):
    if x % 8 == 0 or z % 8 == 0:
        return B("smooth_sandstone")
    v = h(x, z, 11) % 10
    return JS() if v < 7 else B("cut_sandstone") if v < 9 else B("calcite")


def chair(s, x, z, f="north"):
    s.set(x, 1, z, B("quartz_stairs", facing=f, half="bottom", shape="straight", waterlogged="false"))


def build() -> Kotel:
    s = Kotel(SX, SY, SZ)
    for x in range(SX):
        for z in range(SZ):
            s.set(x, 0, z, paving(x, z))
    gate_y = 9
    RAMP_X = 57
    wall(s, (RAMP_X, RAMP_X + 3), gate_y)
    # prayer strip: a step up along the wall for the men's and women's sections
    for x in range(12, RAMP_X - 3):
        for z in (FRONT + 1, FRONT + 2):
            s.set(x, 0, z, B("smooth_sandstone"))
        s.set(x, 0, FRONT + 3, B("calcite"))
    wilsons_arch(s, 0, 11, 17)
    MECH = 42
    # mechitza: fence screen with lanterns, open at the far end so you can walk round
    for z in range(FRONT + 1, 26):
        s.set(MECH, 1, z, SFENCE("dark_oak"))
        s.set(MECH, 2, z, SFENCE("dark_oak") if z % 3 else LANTERN())
    # men's section: shtenders, plastic chairs, book tables
    for x in range(14, MECH - 1, 5):
        s.set(x, 1, FRONT + 4, B("lectern", facing="north", has_book="true", powered="false"))
        chair(s, x + 2, FRONT + 6); chair(s, x + 3, FRONT + 6)
    for x in range(MECH + 3, RAMP_X - 4, 4):
        s.set(x, 1, FRONT + 4, B("lectern", facing="north", has_book="true", powered="false"))
        chair(s, x + 1, FRONT + 6)
    for x0 in (16, 30, MECH + 4):
        for x in range(x0, x0 + 3):
            s.set(x, 1, 14, SLAB("smooth_quartz", "top"))
            s.set(x, 2, 14, B("chiseled_bookshelf", facing="south", slot_0_occupied="true",
                              slot_1_occupied="true", slot_2_occupied="false", slot_3_occupied="true",
                              slot_4_occupied="false", slot_5_occupied="true") if x == x0 + 1 else AIR())
            chair(s, x, 15, "north")
    # hand-washing basins at the entrance to the prayer area
    for x in (20, 26, 34, MECH + 6):
        s.set(x, 1, 18, B("water_cauldron", level="3"))
        s.set(x, 2, 18, B("iron_bars", north="false", south="false", east="false", west="false", waterlogged="false"))
        s.set(x, 3, 18, B("iron_chain", axis="y", waterlogged="false"))
    # big Hanukkah menorah on a plinth in the middle of the plaza
    mx, mz = 30, 26
    for dx in range(-1, 2):
        for dz in range(-1, 2):
            s.set(mx + dx, 1, mz + dz, B("polished_diorite"))
    s.set(mx, 2, mz, blk(f"{NS}:menorah", candles="8"))
    # Israeli flags and lamp posts
    for fx in (14, 40):
        flagpole(s, fx, 30)
    for x, z in ((16, 22), (44, 22), (24, 34), (38, 34), (52, 30), (6, 26)):
        s.set(x, 1, z, B("dark_oak_fence", north="false", south="false", east="false", west="false", waterlogged="false"))
        s.set(x, 2, z, B("dark_oak_fence", north="false", south="false", east="false", west="false", waterlogged="false"))
        s.set(x, 3, z, B("dark_oak_fence", north="false", south="false", east="false", west="false", waterlogged="false"))
        s.set(x, 4, z, LANTERN())
    mughrabi(s, RAMP_X, gate_y)
    # upper terrace: three wide steps up toward the Jewish Quarter
    for step, z0 in enumerate((38, 40, 42)):
        for x in range(0, RAMP_X - 6):
            for z in range(z0, SZ):
                s.set(x, step + 1, z, B("smooth_sandstone") if z == z0 else paving(x, z))
            s.set(x, step + 1, z0, STAIR("south", "smooth_sandstone"))
    for x in range(0, RAMP_X - 6):
        s.set(x, 4, SZ - 1, WALL("sandstone"))
    for x in (4, 10, 44):
        olive(s, x, 45, 4)
    for x in range(1, RAMP_X - 6, 9):          # benches on the terrace looking down at the wall
        s.set(x, 4, 44, STAIR("north", "smooth_sandstone"))
        s.set(x + 1, 4, 44, STAIR("north", "smooth_sandstone"))
    # security checkpoint where the street enters the terrace
    cx = 28
    for x in range(cx - 4, cx + 5):
        s.set(x, 4, SZ - 1, AIR())
    for x in (cx - 3, cx, cx + 3):
        for y in (4, 5, 6):
            s.set(x, y, SZ - 3, B("iron_bars", north="false", south="false", east="true", west="true", waterlogged="false")
                  if y < 6 else SLAB("smooth_stone"))
    for x in range(cx - 3, cx + 4):
        s.set(x, 7, SZ - 3, SLAB("smooth_stone"))
    for x in range(cx + 5, cx + 8):
        for z in range(SZ - 4, SZ - 1):
            edge = x in (cx + 5, cx + 7) or z in (SZ - 4, SZ - 2)
            for y in (4, 5, 6):
                s.set(x, y, z, (B("glass_pane", north="false", south="false", east="false", west="false",
                                  waterlogged="false") if y == 5 else B("white_concrete")) if edge else AIR())
            s.set(x, 7, z, B("smooth_stone_slab", type="bottom", waterlogged="false"))
    s.set(cx + 6, 4, SZ - 4, AIR()); s.set(cx + 6, 5, SZ - 4, AIR())
    s.set(cx + 6, 4, SZ - 3, B("stone_button", face="floor", facing="north", powered="false"))
    # low parapet along the open side of the plaza (archaeological park side)
    for z in range(FRONT + 1, 38):
        if not (24 <= z <= 28):
            s.set(SX - 1, 1, z, WALL("sandstone"))
    return s


def main() -> None:
    s = build()
    s.save(DATA / "structure/western_wall.nbt")
    write_pool(DATA / "worldgen/template_pool/western_wall.json",
               [(f"{NS}:western_wall", 1, "rigid")], "minecraft:empty")
    structure = {
        "type": "minecraft:jigsaw",
        "biomes": [f"{NS}:jerusalem"],
        "max_distance_from_center": 80,
        "project_start_to_heightmap": "WORLD_SURFACE_WG",
        "size": 1,
        "spawn_overrides": {},
        "start_height": {"absolute": -Rigid.FOOTING},
        "start_pool": f"{NS}:western_wall",
        "step": "surface_structures",
        "terrain_adaptation": "beard_box",
        "use_expansion_hack": False,
    }
    (DATA / "worldgen/structure/western_wall.json").write_text(json.dumps(structure, indent=2) + "\n")


if __name__ == "__main__":
    main()
