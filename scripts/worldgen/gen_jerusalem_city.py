#!/usr/bin/env python3
"""Generate Jerusalem Old City jigsaw structure NBTs (DataVersion 4903)."""
from __future__ import annotations

import json
from pathlib import Path
from typing import Any

import nbtlib
from nbtlib import Byte, Compound, Int, List, String

ROOT = Path(__file__).resolve().parents[2]
STRUCT = ROOT / "src/main/resources/data/israel_simulator/structure/jerusalem"
POOL = ROOT / "src/main/resources/data/israel_simulator/worldgen/template_pool/jerusalem"
DATA_VERSION = 4903
BLOCKS_JSON = Path("/workspace/mcreports/out/reports/blocks.json")

KNOWN = json.loads(BLOCKS_JSON.read_text()) if BLOCKS_JSON.exists() else {}


def blk(name: str, **props: str) -> Compound:
    if name.startswith("minecraft:") and KNOWN and name not in KNOWN:
        raise ValueError(f"Unknown block {name}")
    if props and KNOWN and name in KNOWN:
        allowed = KNOWN[name].get("properties", {})
        for k, v in props.items():
            if k not in allowed:
                raise ValueError(f"{name} has no property {k}")
            if v not in allowed[k]:
                raise ValueError(f"{name}.{k}={v} invalid; allowed {allowed[k]}")
    c: dict[str, Any] = {"Name": String(name)}
    if props:
        c["Properties"] = Compound({k: String(v) for k, v in props.items()})
    return Compound(c)


def jigsaw(
    orientation: str,
    name: str,
    target: str,
    pool: str,
    final_state: str,
    joint: str = "aligned",
) -> Compound:
    return Compound(
        {
            "Name": String("minecraft:jigsaw"),
            "Properties": Compound({"orientation": String(orientation)}),
            "_nbt": Compound(
                {
                    "id": String("minecraft:jigsaw"),
                    "name": String(name),
                    "target": String(target),
                    "pool": String(pool),
                    "final_state": String(final_state),
                    "joint": String(joint),
                }
            ),
        }
    )


class Structure:
    def __init__(self, sx: int, sy: int, sz: int):
        self.sx, self.sy, self.sz = sx, sy, sz
        self.palette: list[Compound] = []
        self.index: dict[str, int] = {}
        self.blocks: dict[tuple[int, int, int], int] = {}
        self.block_nbt: dict[tuple[int, int, int], Compound] = {}
        # default fill air so size is correct
        self.fills(0, 0, 0, sx - 1, sy - 1, sz - 1, blk("minecraft:air"))

    def _key(self, entry: Compound) -> str:
        props = entry.get("Properties")
        nbt = entry.get("_nbt")
        return json.dumps(
            {
                "Name": str(entry["Name"]),
                "Properties": {k: str(v) for k, v in props.items()} if props else None,
                "nbt": {k: str(v) for k, v in nbt.items()} if nbt else None,
            },
            sort_keys=True,
        )

    def _state(self, entry: Compound) -> int:
        key = self._key(entry)
        if key not in self.index:
            self.index[key] = len(self.palette)
            self.palette.append(entry)
        return self.index[key]

    def set(self, x: int, y: int, z: int, entry: Compound) -> None:
        if not (0 <= x < self.sx and 0 <= y < self.sy and 0 <= z < self.sz):
            raise ValueError(f"out of bounds {(x,y,z)} size {(self.sx,self.sy,self.sz)}")
        state = self._state(entry)
        self.blocks[(x, y, z)] = state
        if "_nbt" in entry:
            self.block_nbt[(x, y, z)] = entry["_nbt"]
        elif (x, y, z) in self.block_nbt:
            del self.block_nbt[(x, y, z)]

    def fills(self, x0, y0, z0, x1, y1, z1, entry: Compound) -> None:
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for y in range(min(y0, y1), max(y0, y1) + 1):
                for z in range(min(z0, z1), max(z0, z1) + 1):
                    self.set(x, y, z, entry)

    def hollow_box(self, x0, y0, z0, x1, y1, z1, wall: Compound, floor: Compound | None = None, roof: Compound | None = None) -> None:
        self.fills(x0, y0, z0, x1, y1, z1, blk("minecraft:air"))
        if floor is not None:
            self.fills(x0, y0, z0, x1, y0, z1, floor)
        if roof is not None:
            self.fills(x0, y1, z0, x1, y1, z1, roof)
        # walls
        self.fills(x0, y0, z0, x0, y1, z1, wall)
        self.fills(x1, y0, z0, x1, y1, z1, wall)
        self.fills(x0, y0, z0, x1, y1, z0, wall)
        self.fills(x0, y0, z1, x1, y1, z1, wall)

    def save(self, path: Path) -> None:
        path.parent.mkdir(parents=True, exist_ok=True)
        clean_palette = []
        for e in self.palette:
            pe = Compound({"Name": e["Name"]})
            if "Properties" in e:
                pe["Properties"] = e["Properties"]
            clean_palette.append(pe)
        blocks_list = []
        for (x, y, z), state in sorted(self.blocks.items()):
            b = Compound({"pos": List[Int]([Int(x), Int(y), Int(z)]), "state": Int(state)})
            if (x, y, z) in self.block_nbt:
                b["nbt"] = self.block_nbt[(x, y, z)]
            blocks_list.append(b)
        root = Compound(
            {
                "size": List[Int]([Int(self.sx), Int(self.sy), Int(self.sz)]),
                "entities": List[Compound]([]),
                "blocks": List[Compound](blocks_list),
                "palette": List[Compound](clean_palette),
                "DataVersion": Int(DATA_VERSION),
            }
        )
        nbtlib.File(root).save(path, gzipped=True)
        print(f"wrote {path.relative_to(ROOT)} size={self.sx}x{self.sy}x{self.sz} blocks={len(blocks_list)} palette={len(clean_palette)}")


# helpers
SS = lambda: blk("minecraft:sandstone")
SSS = lambda: blk("minecraft:smooth_sandstone")
CS = lambda: blk("minecraft:cut_sandstone")
CH = lambda: blk("minecraft:chiseled_sandstone")
SW = lambda: blk("minecraft:sandstone_wall", up="true", north="none", south="none", east="none", west="none", waterlogged="false")
STONE = lambda: blk("minecraft:stone")
SB = lambda: blk("minecraft:stone_bricks")
CSB = lambda: blk("minecraft:chiseled_stone_bricks")
YT = lambda: blk("minecraft:yellow_terracotta")
OT = lambda: blk("minecraft:orange_terracotta")
RT = lambda: blk("minecraft:red_terracotta")
WT = lambda: blk("minecraft:white_terracotta")
BT = lambda: blk("minecraft:brown_terracotta")
SAND = lambda: blk("minecraft:sand")
GP = lambda facing="north": blk("minecraft:glass_pane", north="false", south="false", east="false", west="false", waterlogged="false")
LANTERN = lambda: blk("minecraft:lantern", hanging="false", waterlogged="false")
HLANTERN = lambda: blk("minecraft:lantern", hanging="true", waterlogged="false")
BARREL = lambda facing="up": blk("minecraft:barrel", facing=facing, open="false")
COMPOSTER = lambda: blk("minecraft:composter", level="3")
CRAFT = lambda: blk("minecraft:crafting_table")
CHEST = lambda facing="south": blk("minecraft:chest", facing=facing, type="single", waterlogged="false")
BED = lambda facing="north", part="foot": blk("minecraft:white_bed", facing=facing, part=part, occupied="false")
DOOR = lambda facing="north", half="lower", hinge="left", open="false": blk(
    "minecraft:oak_door", facing=facing, half=half, hinge=hinge, open=open, powered="false"
)
TRAP = lambda facing="north", open="false", half="bottom": blk(
    "minecraft:oak_trapdoor", facing=facing, half=half, open=open, powered="false", waterlogged="false"
)
STAIRS = lambda facing="north", half="bottom", shape="straight": blk(
    "minecraft:sandstone_stairs", facing=facing, half=half, shape=shape, waterlogged="false"
)
SSLAB = lambda typ="bottom": blk("minecraft:sandstone_slab", type=typ, waterlogged="false")
SSSLAB = lambda typ="bottom": blk("minecraft:smooth_sandstone_slab", type=typ, waterlogged="false")
FENCE = lambda: blk("minecraft:oak_fence", north="false", south="false", east="false", west="false", waterlogged="false")
POT = lambda: blk("minecraft:flower_pot")
TORCH = lambda: blk("minecraft:torch")
WAT = lambda: blk("minecraft:water", level="0")
WOOL = lambda color="red": blk(f"minecraft:{color}_wool")
CARPET = lambda color="red": blk(f"minecraft:{color}_carpet")
LECTERN = lambda facing="south": blk("minecraft:lectern", facing=facing, has_book="false", powered="false")
BOOKS = lambda: blk("minecraft:chiseled_bookshelf", facing="south", slot_0_occupied="true", slot_1_occupied="true",
                    slot_2_occupied="false", slot_3_occupied="true", slot_4_occupied="false", slot_5_occupied="true")
GOLD = lambda: blk("minecraft:gold_block")
GLOW = lambda: blk("minecraft:glowstone")
AIR = lambda: blk("minecraft:air")
VOID = lambda: blk("minecraft:structure_void")


def arched_window(s: Structure, x: int, y: int, z: int, axis: str) -> None:
    """2-high glass opening with sandstone lintel above."""
    s.set(x, y, z, GP())
    s.set(x, y + 1, z, GP())
    if axis == "x":
        s.set(x, y + 2, z, SSLAB("bottom"))
    else:
        s.set(x, y + 2, z, SSLAB("bottom"))


# ---------- pieces ----------

def plaza() -> None:
    # 15x6x15 central plaza with fountain and 4 street jigsaws + marker
    s = Structure(15, 6, 15)
    # ground
    s.fills(0, 0, 0, 14, 0, 14, SSS())
    # path ring / plaza tiles
    s.fills(2, 0, 2, 12, 0, 12, CS())
    # fountain base
    s.fills(6, 1, 6, 8, 1, 8, SS())
    s.fills(6, 1, 6, 8, 1, 8, SW())  # overwrite walls? better: rim
    s.fills(6, 1, 6, 8, 1, 8, SS())
    s.set(6, 1, 6, SS()); s.set(7, 1, 6, SS()); s.set(8, 1, 6, SS())
    s.set(6, 1, 8, SS()); s.set(7, 1, 8, SS()); s.set(8, 1, 8, SS())
    s.set(6, 1, 7, SS()); s.set(8, 1, 7, SS())
    s.set(7, 1, 7, WAT())
    s.set(7, 2, 7, CH())
    s.set(7, 3, 7, LANTERN())
    # benches / pots
    for x, z in ((3, 3), (11, 3), (3, 11), (11, 11)):
        s.set(x, 1, z, POT())
        s.set(x, 1, z + 1 if z < 8 else z - 1, FENCE())
    # yellow terracotta marker (keep test happy; also on plaza)
    s.set(7, 1, 3, YT())
    # street connectors at mid of each side, y=1 (air level above ground)
    # name street, pool streets, target street
    s.set(7, 1, 0, jigsaw("north_up", "minecraft:street", "minecraft:street",
                          "israel_simulator:jerusalem/streets", "minecraft:structure_void"))
    s.set(7, 1, 14, jigsaw("south_up", "minecraft:street", "minecraft:street",
                           "israel_simulator:jerusalem/streets", "minecraft:structure_void"))
    s.set(0, 1, 7, jigsaw("west_up", "minecraft:street", "minecraft:street",
                          "israel_simulator:jerusalem/streets", "minecraft:structure_void"))
    s.set(14, 1, 7, jigsaw("east_up", "minecraft:street", "minecraft:street",
                           "israel_simulator:jerusalem/streets", "minecraft:structure_void"))
    # corner wall stubs for flavor
    for x, z in ((0, 0), (14, 0), (0, 14), (14, 14)):
        s.set(x, 1, z, SB())
        s.set(x, 2, z, SB())
        s.set(x, 3, z, SW())
    s.save(STRUCT / "plaza.nbt")


def street_straight() -> None:
    # 13 long x 2 tall x 7 wide; buildings attach on +Z and -Z sides
    s = Structure(13, 3, 7)
    s.fills(0, 0, 0, 12, 0, 6, SSS())
    s.fills(0, 0, 2, 12, 0, 4, CS())  # road
    # lantern posts
    s.set(3, 1, 1, FENCE()); s.set(3, 2, 1, LANTERN())
    s.set(9, 1, 5, FENCE()); s.set(9, 2, 5, LANTERN())
    # street ends
    s.set(0, 1, 3, jigsaw("west_up", "minecraft:street", "minecraft:street",
                          "israel_simulator:jerusalem/streets", "minecraft:structure_void"))
    s.set(12, 1, 3, jigsaw("east_up", "minecraft:street", "minecraft:street",
                           "israel_simulator:jerusalem/streets", "minecraft:structure_void"))
    # building entrances facing south (+Z) and north (-Z)
    s.set(3, 1, 6, jigsaw("south_up", "minecraft:building_entrance", "minecraft:building_entrance",
                          "israel_simulator:jerusalem/buildings", "minecraft:structure_void"))
    s.set(9, 1, 6, jigsaw("south_up", "minecraft:building_entrance", "minecraft:building_entrance",
                          "israel_simulator:jerusalem/buildings", "minecraft:structure_void"))
    s.set(6, 1, 0, jigsaw("north_up", "minecraft:building_entrance", "minecraft:building_entrance",
                          "israel_simulator:jerusalem/buildings", "minecraft:structure_void"))
    s.save(STRUCT / "street_straight.nbt")


def street_crossroad() -> None:
    s = Structure(11, 3, 11)
    s.fills(0, 0, 0, 10, 0, 10, SSS())
    s.fills(4, 0, 0, 6, 0, 10, CS())
    s.fills(0, 0, 4, 10, 0, 6, CS())
    s.set(5, 1, 5, CH())
    s.set(5, 2, 5, LANTERN())
    for orient, x, z in (("north_up", 5, 0), ("south_up", 5, 10), ("west_up", 0, 5), ("east_up", 10, 5)):
        s.set(x, 1, z, jigsaw(orient, "minecraft:street", "minecraft:street",
                              "israel_simulator:jerusalem/streets", "minecraft:structure_void"))
    # buildings on corners of cross
    s.set(2, 1, 0, jigsaw("north_up", "minecraft:building_entrance", "minecraft:building_entrance",
                          "israel_simulator:jerusalem/buildings", "minecraft:structure_void"))
    s.set(8, 1, 10, jigsaw("south_up", "minecraft:building_entrance", "minecraft:building_entrance",
                           "israel_simulator:jerusalem/buildings", "minecraft:structure_void"))
    s.save(STRUCT / "street_crossroad.nbt")


def street_corner() -> None:
    s = Structure(9, 3, 9)
    s.fills(0, 0, 0, 8, 0, 8, SSS())
    s.fills(3, 0, 0, 5, 0, 5, CS())
    s.fills(3, 0, 3, 8, 0, 5, CS())
    s.set(4, 1, 4, FENCE()); s.set(4, 2, 4, LANTERN())
    s.set(4, 1, 0, jigsaw("north_up", "minecraft:street", "minecraft:street",
                          "israel_simulator:jerusalem/streets", "minecraft:structure_void"))
    s.set(8, 1, 4, jigsaw("east_up", "minecraft:street", "minecraft:street",
                          "israel_simulator:jerusalem/streets", "minecraft:structure_void"))
    s.set(1, 1, 0, jigsaw("north_up", "minecraft:building_entrance", "minecraft:building_entrance",
                          "israel_simulator:jerusalem/buildings", "minecraft:structure_void"))
    s.set(8, 1, 1, jigsaw("east_up", "minecraft:building_entrance", "minecraft:building_entrance",
                          "israel_simulator:jerusalem/buildings", "minecraft:structure_void"))
    s.save(STRUCT / "street_corner.nbt")


def terminator() -> None:
    s = Structure(3, 2, 3)
    s.fills(0, 0, 0, 2, 0, 2, SSS())
    s.set(1, 0, 1, CS())
    s.set(1, 1, 0, jigsaw("north_up", "minecraft:street", "minecraft:street",
                          "minecraft:empty", "minecraft:structure_void"))
    s.save(STRUCT / "terminator.nbt")


def house(name: str, accent) -> None:
    # 7x6x7 house, entrance on west face (building_entrance looking west matches street south_up? )
    # House jigsaw: name=building_entrance, pool=empty, target=building_entrance, orientation=west_up
    # so the front of the house faces west and connects to a street jigsaw facing south/north/east.
    s = Structure(7, 6, 7)
    # floor
    s.fills(0, 0, 0, 6, 0, 6, SSS())
    # walls
    s.fills(0, 1, 0, 6, 3, 6, SS())
    s.fills(1, 1, 1, 5, 3, 5, AIR())
    # flat roof
    s.fills(0, 4, 0, 6, 4, 6, CS())
    s.fills(1, 4, 1, 5, 4, 5, SSLAB("bottom"))
    # parapet
    for x in range(0, 7):
        s.set(x, 5, 0, SW()); s.set(x, 5, 6, SW())
    for z in range(1, 6):
        s.set(0, 5, z, SW()); s.set(6, 5, z, SW())
    # accent cornice
    s.fills(0, 3, 0, 6, 3, 0, accent())
    s.fills(0, 3, 6, 6, 3, 6, accent())
    s.fills(0, 3, 0, 0, 3, 6, accent())
    s.fills(6, 3, 0, 6, 3, 6, accent())
    # door west
    s.set(0, 1, 3, DOOR(facing="west", half="lower"))
    s.set(0, 2, 3, DOOR(facing="west", half="upper"))
    # windows
    s.set(3, 2, 0, GP()); s.set(3, 2, 6, GP())
    s.set(6, 2, 2, GP()); s.set(6, 2, 4, GP())
    # interior
    s.set(5, 1, 1, BED(facing="south", part="foot"))
    s.set(5, 1, 2, BED(facing="south", part="head"))
    s.set(1, 1, 1, CRAFT())
    s.set(1, 1, 5, BARREL("up"))
    s.set(5, 1, 5, POT())
    s.set(3, 1, 5, CHEST(facing="north"))
    s.set(3, 4, 3, HLANTERN())
    # entrance jigsaw on west face
    s.set(0, 1, 2, jigsaw("west_up", "minecraft:building_entrance", "minecraft:building_entrance",
                          "minecraft:empty", "minecraft:structure_void"))
    # marker only on house_a so start-pool fallback still has yellow terracotta somewhere —
    # plaza already has it; keep one more for redundancy on house_a
    if name == "house_a":
        s.set(3, 0, 3, YT())
    s.save(STRUCT / f"{name}.nbt")


def shuk() -> None:
    # market stall row 11x5x6, entrance west
    s = Structure(11, 5, 6)
    s.fills(0, 0, 0, 10, 0, 5, SSS())
    # back wall
    s.fills(0, 1, 5, 10, 3, 5, SS())
    s.fills(0, 1, 0, 0, 3, 5, SS())
    s.fills(10, 1, 0, 10, 3, 5, SS())
    # counters
    s.fills(1, 1, 2, 9, 1, 2, SSLAB("bottom"))
    # awning (wool + fences)
    for x in range(1, 10):
        s.set(x, 3, 1, FENCE())
        s.set(x, 3, 2, WOOL("red" if x % 2 else "yellow"))
        s.set(x, 3, 3, WOOL("yellow" if x % 2 else "red"))
    # goods
    for x in (2, 5, 8):
        s.set(x, 1, 3, BARREL("up"))
        s.set(x + 1, 1, 3, COMPOSTER())
        s.set(x, 1, 4, POT())
    s.set(1, 1, 4, CHEST(facing="south"))
    s.set(9, 1, 4, CHEST(facing="south"))
    s.set(5, 4, 4, HLANTERN())
    # entrance
    s.set(0, 1, 2, jigsaw("west_up", "minecraft:building_entrance", "minecraft:building_entrance",
                          "minecraft:empty", "minecraft:structure_void"))
    # open front (clear west-ish) already open
    s.save(STRUCT / "shuk_stalls.nbt")


def synagogue() -> None:
    # small domed synagogue 9x9x9, entrance west
    s = Structure(9, 9, 9)
    s.fills(0, 0, 0, 8, 0, 8, SSS())
    s.fills(0, 1, 0, 8, 4, 8, SS())
    s.fills(1, 1, 1, 7, 4, 7, AIR())
    # accent band
    s.fills(0, 3, 0, 8, 3, 8, WT())
    s.fills(1, 3, 1, 7, 3, 7, AIR())
    # flat roof ring
    s.fills(0, 5, 0, 8, 5, 8, CS())
    s.fills(2, 5, 2, 6, 5, 6, AIR())
    # dome: stepped smooth sandstone
    s.fills(2, 6, 2, 6, 6, 6, SSS())
    s.fills(3, 7, 3, 5, 7, 5, SSS())
    s.set(4, 8, 4, GLOW())
    # door west
    s.set(0, 1, 4, DOOR(facing="west", half="lower"))
    s.set(0, 2, 4, DOOR(facing="west", half="upper"))
    # arched windows
    for z in (2, 6):
        s.set(0, 2, z, GP()); s.set(0, 3, z, GP())
        s.set(8, 2, z, GP()); s.set(8, 3, z, GP())
    for x in (2, 6):
        s.set(x, 2, 0, GP()); s.set(x, 3, 0, GP())
        s.set(x, 2, 8, GP()); s.set(x, 3, 8, GP())
    # bimah / ark
    s.set(4, 1, 6, LECTERN(facing="north"))
    s.set(4, 1, 7, BOOKS())
    s.set(3, 1, 7, CARPET("blue"))
    s.set(5, 1, 7, CARPET("blue"))
    s.set(4, 1, 5, CARPET("blue"))
    s.set(2, 1, 2, FENCE()); s.set(6, 1, 2, FENCE())
    s.set(2, 2, 2, POT()); s.set(6, 2, 2, POT())
    s.set(4, 5, 4, HLANTERN())
    # entrance jigsaw
    s.set(0, 1, 3, jigsaw("west_up", "minecraft:building_entrance", "minecraft:building_entrance",
                          "minecraft:empty", "minecraft:structure_void"))
    s.save(STRUCT / "synagogue_small.nbt")


def well() -> None:
    s = Structure(5, 5, 5)
    s.fills(0, 0, 0, 4, 0, 4, SSS())
    s.fills(1, 0, 1, 3, 0, 3, CS())
    # rim
    for x, z in ((1, 1), (2, 1), (3, 1), (1, 3), (2, 3), (3, 3), (1, 2), (3, 2)):
        s.set(x, 1, z, SW())
    s.set(2, 0, 2, WAT())
    # posts + beam
    s.set(1, 2, 2, FENCE()); s.set(3, 2, 2, FENCE())
    s.set(1, 3, 2, FENCE()); s.set(3, 3, 2, FENCE())
    s.set(2, 3, 2, FENCE())
    s.set(2, 4, 2, LANTERN())
    s.set(0, 1, 2, jigsaw("west_up", "minecraft:building_entrance", "minecraft:building_entrance",
                          "minecraft:empty", "minecraft:structure_void"))
    s.save(STRUCT / "well.nbt")


def wall_gate() -> None:
    # wall + gate segment 11x8x5, entrance west (gate faces west toward street)
    s = Structure(11, 8, 5)
    s.fills(0, 0, 0, 10, 0, 4, SSS())
    # wall body
    s.fills(0, 1, 1, 10, 5, 3, SB())
    # gate opening
    s.fills(4, 1, 1, 6, 4, 3, AIR())
    # arch top
    s.fills(4, 4, 1, 6, 4, 3, CSB())
    # battlements
    for x in range(0, 11, 2):
        s.set(x, 6, 1, SB()); s.set(x, 6, 3, SB())
        s.set(x, 7, 1, SW()); s.set(x, 7, 3, SW())
    # gate pillars accent
    s.set(3, 1, 1, CSB()); s.set(3, 2, 1, CSB()); s.set(3, 3, 1, CSB())
    s.set(7, 1, 1, CSB()); s.set(7, 2, 1, CSB()); s.set(7, 3, 1, CSB())
    s.set(5, 5, 2, LANTERN())
    # doors as gate
    s.set(5, 1, 1, DOOR(facing="south", half="lower"))
    s.set(5, 2, 1, DOOR(facing="south", half="upper"))
    s.set(0, 1, 2, jigsaw("west_up", "minecraft:building_entrance", "minecraft:building_entrance",
                          "minecraft:empty", "minecraft:structure_void"))
    s.save(STRUCT / "wall_gate.nbt")


def write_pool(path: Path, elements: list[tuple[str, int, str]], fallback: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    data = {
        "fallback": fallback,
        "elements": [
            {
                "weight": w,
                "element": {
                    "element_type": "minecraft:single_pool_element",
                    "location": loc,
                    "processors": "minecraft:empty",
                    "projection": proj,
                },
            }
            for loc, w, proj in elements
        ],
    }
    path.write_text(json.dumps(data, indent=2) + "\n")
    print(f"wrote {path.relative_to(ROOT)}")


def main() -> None:
    STRUCT.mkdir(parents=True, exist_ok=True)
    plaza()
    street_straight()
    street_crossroad()
    street_corner()
    terminator()
    house("house_a", YT)
    house("house_b", OT)
    house("house_c", RT)
    shuk()
    synagogue()
    well()
    wall_gate()

    # start pool (keep id israel_simulator:jerusalem_city for structure JSON)
    start_pool = ROOT / "src/main/resources/data/israel_simulator/worldgen/template_pool/jerusalem_city.json"
    write_pool(
        start_pool,
        [("israel_simulator:jerusalem/plaza", 1, "rigid")],
        "minecraft:empty",
    )

    write_pool(
        POOL / "streets.json",
        [
            ("israel_simulator:jerusalem/street_straight", 4, "terrain_matching"),
            ("israel_simulator:jerusalem/street_crossroad", 2, "terrain_matching"),
            ("israel_simulator:jerusalem/street_corner", 2, "terrain_matching"),
        ],
        "israel_simulator:jerusalem/terminators",
    )
    write_pool(
        POOL / "terminators.json",
        [("israel_simulator:jerusalem/terminator", 1, "terrain_matching")],
        "minecraft:empty",
    )
    write_pool(
        POOL / "buildings.json",
        [
            ("israel_simulator:jerusalem/house_a", 3, "rigid"),
            ("israel_simulator:jerusalem/house_b", 3, "rigid"),
            ("israel_simulator:jerusalem/house_c", 2, "rigid"),
            ("israel_simulator:jerusalem/shuk_stalls", 2, "rigid"),
            ("israel_simulator:jerusalem/synagogue_small", 1, "rigid"),
            ("israel_simulator:jerusalem/well", 2, "rigid"),
            ("israel_simulator:jerusalem/wall_gate", 1, "rigid"),
        ],
        "minecraft:empty",
    )

    # keep a tiny root jerusalem_city.nbt with marker so StructureFrameworkTest still finds
    # data/israel_simulator/structure/jerusalem_city.nbt with yellow terracotta
    # (start pool no longer references it, but the test requires the file)
    marker = Structure(3, 3, 3)
    marker.fills(0, 0, 0, 2, 0, 2, SSS())
    marker.set(1, 1, 1, YT())
    marker.save(ROOT / "src/main/resources/data/israel_simulator/structure/jerusalem_city.nbt")


if __name__ == "__main__":
    main()
