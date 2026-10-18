#!/usr/bin/env python3
"""Generate ancient_sanctuary (hidden desert temple, Tabernacle/Temple plan), DataVersion 4903.

Two variants in the start pool, same plan, different state of ruin:
  * ancient_sanctuary          walls mostly standing, light sand drifts
  * ancient_sanctuary_buried   dunes over the east side, fallen columns, collapsed roof bay

Plan (north = entrance, z grows to the south):
  gate with twin pillars (Jachin & Boaz) and steps -> outer court with bronze altar (copper,
  campfire) and laver (water cauldron on copper) -> portico -> Holy Place (menorah,
  table of showbread, incense altar) -> veil (blue/purple/red wool) -> Holy of Holies (Ark)
  -> secret stair from the Holy of Holies down to a vaulted crypt with scroll jars and the
  ancient_sanctuary loot (Rabbi's Crown source). Framework marker: minecraft:chiseled_sandstone.

Run:  /workspace/nbtvenv/bin/python scripts/worldgen/gen_ancient_sanctuary.py
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from jigsaw_kit import (  # noqa: E402
    AIR, B, BARREL, CHEST, DATA, HLANTERN, LANTERN, NS, SLAB, STAIR, Rigid,
)
from gen_jerusalem_city import blk, write_pool  # noqa: E402

LOOT = f"{NS}:chests/ancient_sanctuary"
W, D, H = 31, 43, 14


def h(x: int, z: int, salt: int = 0) -> int:
    return (x * 73856093 ^ z * 19349663 ^ salt * 83492791) & 0xFFFF


def wall_block(x, y, z, salt):
    v = h(x * 5 + y, z, salt) % 12
    return (B("cut_sandstone") if v < 5 else B("sandstone") if v < 8 else B("smooth_sandstone") if v < 10
            else B("chiseled_sandstone") if v < 11 else B("sandstone_stairs", facing="north", half="top",
                                                          shape="straight", waterlogged="false"))


def pillar(s, x, z, height, cap=True) -> None:
    s.set(x, 1, z, B("chiseled_sandstone"))
    for y in range(2, height):
        s.set(x, y, z, B("sandstone_wall", north="none", south="none", east="none", west="none",
                         up="true", waterlogged="false") if (x + y) % 5 else B("cut_sandstone"))
    if cap:
        s.set(x, height, z, B("chiseled_sandstone"))


class Sanct(Rigid):
    """Rigid piece whose footing must not fill the crypt dug below the floor."""
    FOOTING_BLOCK = "sandstone"

    def __init__(self, sx, sy, sz):
        self.protect: set[tuple[int, int, int]] = set()
        super().__init__(sx, sy, sz)

    def set(self, x, y, z, entry) -> None:
        if self._ready and y < 0:
            self.protect.add((x, y + self.FOOTING, z))
        super().set(x, y, z, entry)

    def save(self, path: Path) -> None:
        from gen_jerusalem_city import Structure
        self._ready = False
        F = self.FOOTING
        for x in range(self.sx):
            for z in range(self.sz):
                if str(self.palette[self.blocks[(x, F, z)]]["Name"]) != "minecraft:air":
                    for y in range(F):
                        if (x, y, z) not in self.protect:
                            Structure.set(self, x, y, z, B(self.FOOTING_BLOCK))
        Structure.save(self, path)


def build(name: str, buried: bool) -> None:
    salt = 7 if buried else 3
    s = Sanct(W, H, D)
    # ground: sand with packed paving inside the court
    for x in range(W):
        for z in range(D):
            s.set(x, 0, z, B("sand") if h(x, z, salt) % 4 else B("smooth_sandstone"))
    cx = W // 2
    # ---------------- outer court wall (2..W-3, 4..D-2)
    X0, Z0, X1, Z1 = 1, 4, W - 2, D - 2
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            if x in (X0, X1) or z in (Z0, Z1):
                if z == Z0 and abs(x - cx) <= 2:
                    continue
                top = 5 - (h(x, z, salt + 1) % (4 if buried else 2))
                for y in range(1, top + 1):
                    s.set(x, y, z, wall_block(x, y, z, salt))
                if top == 5:
                    s.set(x, 6, z, SLAB("sandstone"))
            elif h(x, z, 13) % 3:
                s.set(x, 0, z, B("smooth_sandstone") if (x + z) % 2 else B("cut_sandstone"))
    # ---------------- gate: steps, Jachin & Boaz, lintel
    for x in range(cx - 2, cx + 3):
        s.set(x, 0, 1, STAIR("south", "sandstone")); s.set(x, 0, 2, B("cut_sandstone"))
        s.set(x, 0, 3, B("cut_sandstone")); s.set(x, 0, Z0, B("chiseled_sandstone"))
    for x in (cx - 3, cx + 3):
        pillar(s, x, Z0, 8)
        s.set(x, 9, Z0, B("gold_block") if not buried else B("cut_sandstone"))
    for x in range(cx - 3, cx + 4):
        s.set(x, 8, Z0, B("cut_sandstone") if not (buried and x > cx + 1) else AIR())
    # ---------------- outer court furniture
    # bronze altar with ramp (copper, oxidising)
    ax, az = cx, 11
    for x in range(ax - 2, ax + 3):
        for z in range(az - 2, az + 3):
            s.set(x, 1, z, B("cut_copper") if (x + z) % 2 else B("weathered_cut_copper"))
            if abs(x - ax) < 2 and abs(z - az) < 2:
                s.set(x, 2, z, B("exposed_copper"))
    s.set(ax, 3, az, B("campfire", facing="north", lit="false", signal_fire="false", waterlogged="false"))
    for x in (ax - 2, ax + 2):
        for z in (az - 2, az + 2):
            s.set(x, 2, z, B("lightning_rod", facing="up", powered="false", waterlogged="false"))
    s.set(ax, 1, az - 3, B("cut_copper_stairs", facing="south", half="bottom", shape="straight",
                           waterlogged="false"))
    # laver
    s.set(ax + 6, 1, az + 3, B("oxidized_copper")); s.set(ax + 6, 2, az + 3, B("water_cauldron", level="3"))
    # olive trees and broken column drums in the court
    for (x, z) in ((5, 9), (25, 9), (5, 24), (25, 30)):
        s.set(x, 1, z, B("pale_oak_log", axis="y")); s.set(x, 2, z, B("pale_oak_log", axis="y"))
        for dx in (-1, 0, 1):
            for dz in (-1, 0, 1):
                if s.free(x + dx, 3, z + dz):
                    s.set(x + dx, 3, z + dz, B("pale_oak_leaves", distance="1", persistent="true",
                                               waterlogged="false"))
        s.set(x, 4, z, B("pale_oak_leaves", distance="1", persistent="true", waterlogged="false"))
    for (x, z) in ((8, 16), (22, 14), (9, 34)):
        s.set(x, 1, z, B("chiseled_sandstone")); s.set(x + 1, 1, z, B("cut_sandstone_slab", type="bottom",
                                                                           waterlogged="false"))
    # ---------------- temple house: x 8..22, z 18..39
    TX0, TX1, TZ0, TZ1 = 8, 22, 18, 39
    VEIL = 32                                   # Holy Place z<VEIL, Holy of Holies z>VEIL
    for x in range(TX0, TX1 + 1):
        for z in range(TZ0, TZ1 + 1):
            s.set(x, 0, z, B("smooth_sandstone"))
            s.set(x, 1, z, B("cut_sandstone") if (x + z) % 3 else B("smooth_sandstone"))   # raised floor
    for x in range(TX0, TX1 + 1):                # podium steps
        s.set(x, 1, TZ0 - 1, STAIR("south", "smooth_sandstone"))
    # portico columns
    for x in range(TX0 + 1, TX1, 3):
        if buried and x > cx + 3 and h(x, 0, 41) % 2:
            for i in range(4):                   # fallen column lying in the court
                if s.free(x, 2, TZ0 - 2 - i):
                    s.set(x, 2, TZ0 - 2 - i, B("cut_sandstone") if i % 2 else B("sandstone"))
            s.set(x, 2, TZ0, B("chiseled_sandstone"))
            continue
        s.set(x, 2, TZ0, B("chiseled_sandstone"))
        for y in range(3, 9):
            s.set(x, y, TZ0, B("sandstone_wall", north="none", south="none", east="none", west="none",
                               up="true", waterlogged="false"))
        s.set(x, 9, TZ0, B("chiseled_sandstone"))
    for x in range(TX0, TX1 + 1):
        s.set(x, 10, TZ0, B("cut_sandstone_slab", type="bottom", waterlogged="false"))
    # walls
    WZ0 = TZ0 + 3
    for x in range(TX0, TX1 + 1):
        for z in range(WZ0, TZ1 + 1):
            edge = x in (TX0, TX1) or z in (WZ0, TZ1)
            if edge:
                for y in range(2, 10):
                    if z == WZ0 and abs(x - cx) <= 1 and y <= 5:
                        continue                 # doorway
                    s.set(x, y, z, wall_block(x, y, z, salt + 2))
                s.set(x, 10, z, B("chiseled_sandstone") if (x + z) % 4 == 0 else B("cut_sandstone"))
            else:
                collapsed = buried and z in range(WZ0 + 2, WZ0 + 6) and x > cx
                s.set(x, 10, z, AIR() if collapsed else B("smooth_sandstone"))
                if collapsed:
                    s.set(x, 2, z, B("sand") if h(x, z, 51) % 2 else SLAB("sandstone"))
    # narrow clerestory windows
    for z in range(WZ0 + 2, TZ1, 3):
        for x in (TX0, TX1):
            s.set(x, 7, z, B("iron_bars", north="false", south="false", east="false", west="false",
                             waterlogged="false"))
    s.set(cx, 6, WZ0, B("gold_block") if not buried else B("chiseled_sandstone"))   # golden vine lintel
    # Holy Place: menorah (south-west), table of showbread (north-east), incense altar before veil
    s.set(TX0 + 2, 2, VEIL - 3, B("gold_block")); s.set(TX0 + 2, 3, VEIL - 3, blk(f"{NS}:menorah", candles="7"))
    for z in (VEIL - 6, VEIL - 5, VEIL - 4):
        s.set(TX1 - 2, 2, z, B("spruce_slab", type="top", waterlogged="false"))
        s.set(TX1 - 2, 3, z, B("cake", bites="0") if z == VEIL - 5 else B("candle", candles="1", lit="false",
                                                                           waterlogged="false"))
    s.set(cx, 2, VEIL - 2, B("gold_block")); s.set(cx, 3, VEIL - 2, B("candle", candles="4", lit="false",
                                                                     waterlogged="false"))
    for z in range(WZ0 + 2, VEIL - 1, 4):
        s.set(TX0 + 1, 8, z, B("lantern", hanging="true", waterlogged="false"))
        s.set(TX1 - 1, 8, z, B("lantern", hanging="true", waterlogged="false"))
    # the veil
    colors = ["blue_wool", "purple_wool", "red_wool", "white_wool"]
    for x in range(TX0 + 1, TX1):
        for y in range(2, 10):
            if x == cx and y <= 3:
                continue                         # a torn gap at the bottom
            s.set(x, y, VEIL, B(colors[(x + y) % 4]))
    # Holy of Holies: ark of gold with cherubim, cedar panelling
    for x in range(TX0 + 1, TX1):
        for z in range(VEIL + 1, TZ1):
            if x in (TX0 + 1, TX1 - 1) or z == TZ1 - 1:
                for y in range(2, 9):
                    if s.get(x, y, z) == "minecraft:air":
                        s.set(x, y, z, B("stripped_spruce_wood", axis="y") if y % 3 else B("gold_block"))
    az2 = (VEIL + TZ1) // 2
    for x in (cx - 1, cx, cx + 1):
        s.set(x, 2, az2, B("gold_block"))
    s.set(cx - 1, 3, az2, B("light_weighted_pressure_plate", power="0"))
    s.set(cx + 1, 3, az2, B("light_weighted_pressure_plate", power="0"))
    s.set(cx, 3, az2, B("gold_block") if not buried else B("raw_gold_block"))
    for x in (cx - 1, cx + 1):                   # cherubim wings
        s.set(x, 4, az2, B("end_rod", facing="up"))
    s.set(cx, 8, az2, HLANTERN())
    # crypt under the Holy of Holies: x cx-4..cx+4, z VEIL+1..TZ1-1, floor y=-4 (in the footing)
    for x in range(cx - 4, cx + 5):
        for z in range(VEIL + 1, TZ1):
            wall = x in (cx - 4, cx + 4) or z in (VEIL + 1, TZ1 - 1)
            for y in (-3, -2, -1):
                s.set(x, y, z, B("sandstone") if wall else AIR())
            s.set(x, -4, z, B("smooth_sandstone") if (x + z) % 2 else B("chiseled_sandstone"))
            s.set(x, 0, z, B("cut_sandstone"))
    # secret way down: trapdoor in the floor beside the ark, ladder into the crypt
    sx_, sz_ = cx + 3, VEIL + 2
    s.set(sx_, 1, sz_, B("spruce_trapdoor", facing="south", half="top", open="false", powered="false",
                         waterlogged="false"))
    for y in range(-3, 1):
        s.set(sx_, y, sz_, B("ladder", facing="south", waterlogged="false"))
    for x in (cx - 3, cx - 2, cx - 1):
        s.set(x, -3, TZ1 - 2, B("decorated_pot", cracked="true" if x % 2 else "false", facing="north",
                                 waterlogged="false"))
    s.set(cx, -3, TZ1 - 2, CHEST("north", LOOT))
    s.set(cx + 1, -3, TZ1 - 2, BARREL("up", LOOT))
    s.set(cx - 3, -3, VEIL + 2, B("candle", candles="3", lit="false", waterlogged="false"))
    s.set(cx, -1, (VEIL + TZ1) // 2, HLANTERN())
    s.set(cx - 2, -3, VEIL + 3, B("lectern", facing="south", has_book="false", powered="false"))
    # ---------------- outer chest: priests' storeroom in the court's NE corner
    for x in range(X1 - 5, X1):
        for z in range(Z0 + 1, Z0 + 5):
            if x == X1 - 5 or z == Z0 + 4:
                for y in range(1, 4):
                    if not (x == X1 - 3 and z == Z0 + 4 and y < 3):
                        s.set(x, y, z, wall_block(x, y, z, salt + 4))
            s.set(x, 4, z, SLAB("sandstone"))
    s.set(X1 - 1, 1, Z0 + 1, BARREL("up", LOOT)); s.set(X1 - 2, 1, Z0 + 1, B("decorated_pot",
                                                                          cracked="false", facing="north",
                                                                          waterlogged="false"))
    s.set(X1 - 4, 1, Z0 + 1, B("cauldron"))
    # lanterns at the gate
    for x in (cx - 4, cx + 4):
        s.set(x, 1, 3, B("sandstone_wall", north="none", south="none", east="none", west="none", up="true",
                         waterlogged="false"))
        s.set(x, 2, 3, LANTERN())
    # ---------------- sand drifts
    for x in range(W):
        for z in range(D):
            drift = h(x, z, salt + 9) % 9
            heavy = buried and x > W // 2 + 2
            if (heavy and drift < 6) or drift == 0:
                for y in range(1, 10):
                    if s.free(x, y, z):
                        if y == 1 or (heavy and y <= 1 + (x - W // 2) // 4 and drift < 4):
                            s.set(x, y, z, B("sand"))
                        else:
                            if h(x, z, y) % 4 == 0 and y == 1 + (x - W // 2) // 4:
                                s.set(x, y, z, B("dead_bush"))
                            break
                    else:
                        continue
            elif drift == 1 and s.free(x, 1, z) and s.get(x, 0, z) == "minecraft:sand":
                s.set(x, 1, z, B("dead_bush"))
    # keep the gate, the doorway and the crypt stair clear
    for x in range(cx - 2, cx + 3):
        for z in (Z0, Z0 + 1, Z0 + 2):
            for y in range(1, 5):
                if s.get(x, y, z) == "minecraft:sand" or s.get(x, y, z) == "minecraft:dead_bush":
                    s.set(x, y, z, AIR())
    s.save(DATA / "structure" / f"{name}.nbt")


def main() -> None:
    build("ancient_sanctuary", buried=False)
    build("ancient_sanctuary_buried", buried=True)
    write_pool(DATA / "worldgen/template_pool/ancient_sanctuary.json",
               [(f"{NS}:ancient_sanctuary", 3, "rigid"), (f"{NS}:ancient_sanctuary_buried", 2, "rigid")],
               "minecraft:empty")
    p = DATA / "worldgen/structure/ancient_sanctuary.json"
    data = json.loads(p.read_text())
    data["start_height"] = {"absolute": -Rigid.FOOTING}
    data["terrain_adaptation"] = "beard_box"
    data["size"] = 1
    p.write_text(json.dumps(data, indent=2) + "\n")


if __name__ == "__main__":
    main()
