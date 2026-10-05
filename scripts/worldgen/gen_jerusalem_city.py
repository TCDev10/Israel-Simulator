#!/usr/bin/env python3
"""Generate Jerusalem Old City jigsaw structure NBTs (DataVersion 4903)."""
from __future__ import annotations

import json
from pathlib import Path
from typing import Any

import nbtlib
from nbtlib import Byte, Compound, Int, List, String

from connect_blocks import apply_connections

ROOT = Path(__file__).resolve().parents[2]
STRUCT = ROOT / "src/main/resources/data/israel_simulator/structure/jerusalem"
POOL = ROOT / "src/main/resources/data/israel_simulator/worldgen/template_pool/jerusalem"
STRUCTURE_JSON = ROOT / "src/main/resources/data/israel_simulator/worldgen/structure/jerusalem_city.json"
DATA_VERSION = 4903
BLOCKS_JSON = Path("/workspace/mcreports/out/reports/blocks.json")
FOUNDATION = 0  # vanilla village style: floor at y=0, no deep footings

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
    def __init__(self, sx: int, sy: int, sz: int, with_foundation: bool = False):
        self.sx, self.sy, self.sz = sx, sy, sz
        self.foundation = FOUNDATION if with_foundation else 0
        self.palette: list[Compound] = []
        self.index: dict[str, int] = {}
        self.blocks: dict[tuple[int, int, int], int] = {}
        self.block_nbt: dict[tuple[int, int, int], Compound] = {}
        self.fills(0, 0, 0, sx - 1, sy - 1, sz - 1, blk("minecraft:air"))
        if self.foundation:
            # solid footing under the eventual floor so hilly terrain gets sandstone, not dirt
            self.fills(0, 0, 0, sx - 1, self.foundation - 1, sz - 1, blk("minecraft:sandstone"))

    @property
    def fy(self) -> int:
        """Y of the walkable floor."""
        return self.foundation

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

    def save(self, path: Path) -> None:
        path.parent.mkdir(parents=True, exist_ok=True)

        # Materialize Name+Properties grid, then fix fence/pane/wall connections.
        grid: dict[tuple[int, int, int], dict] = {}
        for (x, y, z), state in self.blocks.items():
            entry = self.palette[state]
            props = (
                {str(k): str(v) for k, v in entry["Properties"].items()}
                if "Properties" in entry
                else {}
            )
            grid[(x, y, z)] = {"Name": str(entry["Name"]), "Properties": props}
        apply_connections(grid, size=(self.sx, self.sy, self.sz))

        # Rebuild palette from connected grid (jigsaw block entity NBT preserved).
        new_palette: list[Compound] = []
        new_index: dict[str, int] = {}
        blocks_list: list[Compound] = []
        for (x, y, z) in sorted(grid.keys()):
            cell = grid[(x, y, z)]
            props = cell["Properties"]
            pe = Compound({"Name": String(cell["Name"])})
            if props:
                pe["Properties"] = Compound({k: String(v) for k, v in props.items()})
            # Include block-entity payload in the dedupe key via parallel map only
            key_obj = {
                "Name": cell["Name"],
                "Properties": props or None,
                "nbt": (
                    {str(k): str(v) for k, v in self.block_nbt[(x, y, z)].items()}
                    if (x, y, z) in self.block_nbt
                    else None
                ),
            }
            key = json.dumps(key_obj, sort_keys=True)
            if key not in new_index:
                new_index[key] = len(new_palette)
                new_palette.append(pe)
            state = new_index[key]
            b = Compound({"pos": List[Int]([Int(x), Int(y), Int(z)]), "state": Int(state)})
            if (x, y, z) in self.block_nbt:
                b["nbt"] = self.block_nbt[(x, y, z)]
            blocks_list.append(b)

        root = Compound(
            {
                "size": List[Int]([Int(self.sx), Int(self.sy), Int(self.sz)]),
                "entities": List[Compound]([]),
                "blocks": List[Compound](blocks_list),
                "palette": List[Compound](new_palette),
                "DataVersion": Int(DATA_VERSION),
            }
        )
        nbtlib.File(root).save(path, gzipped=True)
        print(f"wrote {path.relative_to(ROOT)} size={self.sx}x{self.sy}x{self.sz} fy={self.fy}")


# block helpers
SS = lambda: blk("minecraft:sandstone")
SSS = lambda: blk("minecraft:smooth_sandstone")
CS = lambda: blk("minecraft:cut_sandstone")
CH = lambda: blk("minecraft:chiseled_sandstone")
SW = lambda: blk(
    "minecraft:sandstone_wall",
    up="true",
    north="none",
    south="none",
    east="none",
    west="none",
    waterlogged="false",
)
SB = lambda: blk("minecraft:stone_bricks")
CSB = lambda: blk("minecraft:chiseled_stone_bricks")
YT = lambda: blk("minecraft:yellow_terracotta")
OT = lambda: blk("minecraft:orange_terracotta")
RT = lambda: blk("minecraft:red_terracotta")
WT = lambda: blk("minecraft:white_terracotta")
GP = lambda: blk(
    "minecraft:glass_pane",
    north="false",
    south="false",
    east="false",
    west="false",
    waterlogged="false",
)
LANTERN = lambda: blk("minecraft:lantern", hanging="false", waterlogged="false")
HLANTERN = lambda: blk("minecraft:lantern", hanging="true", waterlogged="false")
BARREL = lambda facing="up": blk("minecraft:barrel", facing=facing, open="false")
COMPOSTER = lambda: blk("minecraft:composter", level="3")
CRAFT = lambda: blk("minecraft:crafting_table")
CHEST = lambda facing="south": blk(
    "minecraft:chest", facing=facing, type="single", waterlogged="false"
)
BED = lambda facing="north", part="foot": blk(
    "minecraft:white_bed", facing=facing, part=part, occupied="false"
)
DOOR = lambda facing="north", half="lower", hinge="left", open="false": blk(
    "minecraft:oak_door",
    facing=facing,
    half=half,
    hinge=hinge,
    open=open,
    powered="false",
)
SSLAB = lambda typ="bottom": blk("minecraft:sandstone_slab", type=typ, waterlogged="false")
FENCE = lambda: blk(
    "minecraft:oak_fence",
    north="false",
    south="false",
    east="false",
    west="false",
    waterlogged="false",
)
POT = lambda: blk("minecraft:flower_pot")
WAT = lambda: blk("minecraft:water", level="0")
WOOL = lambda color="red": blk(f"minecraft:{color}_wool")
CARPET = lambda color="red": blk(f"minecraft:{color}_carpet")
LECTERN = lambda facing="south": blk(
    "minecraft:lectern", facing=facing, has_book="false", powered="false"
)
BOOKS = lambda: blk(
    "minecraft:chiseled_bookshelf",
    facing="south",
    slot_0_occupied="true",
    slot_1_occupied="true",
    slot_2_occupied="false",
    slot_3_occupied="true",
    slot_4_occupied="false",
    slot_5_occupied="true",
)
GLOW = lambda: blk("minecraft:glowstone")
AIR = lambda: blk("minecraft:air")


def plaza() -> None:
    # foundation + floor; street/landmark jigsaws at fy+1 so they align with street pieces
    s = Structure(15, 6 + FOUNDATION, 15, with_foundation=True)
    f = s.fy
    s.fills(0, f, 0, 14, f, 14, SSS())
    s.fills(2, f, 2, 12, f, 12, CS())
    # fountain (water_cauldron survives structure placement; raw water often vanishes)
    s.fills(6, f + 1, 6, 8, f + 1, 8, SS())
    s.set(7, f + 1, 7, blk("minecraft:water_cauldron", level="3"))
    s.set(7, f + 2, 7, CH())
    s.set(7, f + 3, 7, LANTERN())
    for x, z in ((3, 3), (11, 3), (3, 11), (11, 11)):
        s.set(x, f + 1, z, POT())
    s.set(7, f, 3, YT())  # floor marker  # marker
    # North face: synagogue only (dedicated landmarks pool — avoids colliding with a street
    # on the same side, which was why synagogue_small never won a random buildings slot).
    s.set(
        7,
        f + 1,
        0,
        jigsaw(
            "north_up",
            "minecraft:building_entrance",
            "minecraft:building_entrance",
            "israel_simulator:jerusalem/landmarks",
            "minecraft:structure_void",
        ),
    )
    # Other three faces: streets
    s.set(
        7,
        f + 1,
        14,
        jigsaw(
            "south_up",
            "minecraft:street",
            "minecraft:street",
            "israel_simulator:jerusalem/streets",
            "minecraft:structure_void",
        ),
    )
    s.set(
        0,
        f + 1,
        7,
        jigsaw(
            "west_up",
            "minecraft:street",
            "minecraft:street",
            "israel_simulator:jerusalem/streets",
            "minecraft:structure_void",
        ),
    )
    s.set(
        14,
        f + 1,
        7,
        jigsaw(
            "east_up",
            "minecraft:street",
            "minecraft:street",
            "israel_simulator:jerusalem/streets",
            "minecraft:structure_void",
        ),
    )
    for x, z in ((0, 0), (14, 0), (0, 14), (14, 14)):
        s.set(x, f + 1, z, SB())
        s.set(x, f + 2, z, SB())
        s.set(x, f + 3, z, SW())
    s.save(STRUCT / "plaza.nbt")


def street_straight() -> None:
    # Vanilla-style shallow terrain_matching street (no deep sub-layer).
    s = Structure(13, 3, 7)
    s.fills(0, 0, 0, 12, 0, 6, SSS())
    s.fills(0, 0, 2, 12, 0, 4, CS())
    s.set(3, 1, 1, FENCE()); s.set(3, 2, 1, LANTERN())
    s.set(9, 1, 5, FENCE()); s.set(9, 2, 5, LANTERN())
    s.set(0, 1, 3, jigsaw("west_up", "minecraft:street", "minecraft:street",
                          "israel_simulator:jerusalem/streets", "minecraft:structure_void"))
    s.set(12, 1, 3, jigsaw("east_up", "minecraft:street", "minecraft:street",
                           "israel_simulator:jerusalem/streets", "minecraft:structure_void"))
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
    s.set(5, 1, 5, CH()); s.set(5, 2, 5, LANTERN())
    for orient, x, z in (("north_up", 5, 0), ("south_up", 5, 10), ("west_up", 0, 5), ("east_up", 10, 5)):
        s.set(x, 1, z, jigsaw(orient, "minecraft:street", "minecraft:street",
                              "israel_simulator:jerusalem/streets", "minecraft:structure_void"))
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
    # 7 x (6+FOUNDATION) x 7; floor at fy, door/jigsaw at fy+1 (aligns with street y=1)
    s = Structure(7, 6 + FOUNDATION, 7, with_foundation=True)
    f = s.fy
    s.fills(0, f, 0, 6, f, 6, SSS())
    s.fills(0, f + 1, 0, 6, f + 3, 6, SS())
    s.fills(1, f + 1, 1, 5, f + 3, 5, AIR())
    s.fills(0, f + 4, 0, 6, f + 4, 6, CS())
    s.fills(1, f + 4, 1, 5, f + 4, 5, SSLAB("bottom"))
    for x in range(0, 7):
        s.set(x, f + 5, 0, SW())
        s.set(x, f + 5, 6, SW())
    for z in range(1, 6):
        s.set(0, f + 5, z, SW())
        s.set(6, f + 5, z, SW())
    s.fills(0, f + 3, 0, 6, f + 3, 0, accent())
    s.fills(0, f + 3, 6, 6, f + 3, 6, accent())
    s.fills(0, f + 3, 0, 0, f + 3, 6, accent())
    s.fills(6, f + 3, 0, 6, f + 3, 6, accent())
    s.set(0, f + 1, 3, DOOR(facing="west", half="lower"))
    s.set(0, f + 2, 3, DOOR(facing="west", half="upper"))
    s.set(3, f + 2, 0, GP())
    s.set(3, f + 2, 6, GP())
    s.set(6, f + 2, 2, GP())
    s.set(6, f + 2, 4, GP())
    s.set(5, f + 1, 1, BED(facing="south", part="foot"))
    s.set(5, f + 1, 2, BED(facing="south", part="head"))
    s.set(1, f + 1, 1, CRAFT())
    s.set(1, f + 1, 5, BARREL("up"))
    s.set(5, f + 1, 5, POT())
    s.set(3, f + 1, 5, CHEST(facing="north"))
    s.set(3, f + 4, 3, HLANTERN())
    s.set(
        0,
        f + 1,
        2,
        jigsaw(
            "west_up",
            "minecraft:building_entrance",
            "minecraft:building_entrance",
            "minecraft:empty",
            "minecraft:structure_void",
        ),
    )
    if name == "house_a":
        s.set(3, f, 3, YT())
    s.save(STRUCT / f"{name}.nbt")


def shuk() -> None:
    s = Structure(11, 5 + FOUNDATION, 6, with_foundation=True)
    f = s.fy
    s.fills(0, f, 0, 10, f, 5, SSS())
    s.fills(0, f + 1, 5, 10, f + 3, 5, SS())
    s.fills(0, f + 1, 0, 0, f + 3, 5, SS())
    s.fills(10, f + 1, 0, 10, f + 3, 5, SS())
    s.fills(1, f + 1, 2, 9, f + 1, 2, SSLAB("bottom"))
    for x in range(1, 10):
        s.set(x, f + 3, 1, FENCE())
        s.set(x, f + 3, 2, WOOL("red" if x % 2 else "yellow"))
        s.set(x, f + 3, 3, WOOL("yellow" if x % 2 else "red"))
    for x in (2, 5, 8):
        s.set(x, f + 1, 3, BARREL("up"))
        s.set(x + 1, f + 1, 3, COMPOSTER())
        s.set(x, f + 1, 4, POT())
    s.set(1, f + 1, 4, CHEST(facing="south"))
    s.set(9, f + 1, 4, CHEST(facing="south"))
    s.set(5, f + 4, 4, HLANTERN())
    # open west face toward street (clear wall hole around jigsaw)
    s.fills(0, f + 1, 1, 0, f + 3, 3, AIR())
    s.set(
        0,
        f + 1,
        2,
        jigsaw(
            "west_up",
            "minecraft:building_entrance",
            "minecraft:building_entrance",
            "minecraft:empty",
            "minecraft:structure_void",
        ),
    )
    s.save(STRUCT / "shuk_stalls.nbt")


def synagogue() -> None:
    # Slightly smaller footprint (8x8) + dome; foundation; guaranteed via landmarks pool
    s = Structure(8, 9 + FOUNDATION, 8, with_foundation=True)
    f = s.fy
    s.fills(0, f, 0, 7, f, 7, SSS())
    s.fills(0, f + 1, 0, 7, f + 4, 7, SS())
    s.fills(1, f + 1, 1, 6, f + 4, 6, AIR())
    s.fills(0, f + 3, 0, 7, f + 3, 7, WT())
    s.fills(1, f + 3, 1, 6, f + 3, 6, AIR())
    s.fills(0, f + 5, 0, 7, f + 5, 7, CS())
    s.fills(2, f + 5, 2, 5, f + 5, 5, AIR())
    # dome
    s.fills(2, f + 6, 2, 5, f + 6, 5, SSS())
    s.fills(3, f + 7, 3, 4, f + 7, 4, SSS())
    s.set(3, f + 8, 3, GLOW())
    s.set(4, f + 8, 4, GLOW())
    s.set(0, f + 1, 3, DOOR(facing="west", half="lower"))
    s.set(0, f + 2, 3, DOOR(facing="west", half="upper"))
    s.set(0, f + 1, 4, DOOR(facing="west", half="lower", hinge="right"))
    s.set(0, f + 2, 4, DOOR(facing="west", half="upper", hinge="right"))
    for z in (1, 6):
        s.set(0, f + 2, z, GP())
        s.set(0, f + 3, z, GP())
        s.set(7, f + 2, z, GP())
        s.set(7, f + 3, z, GP())
    for x in (2, 5):
        s.set(x, f + 2, 0, GP())
        s.set(x, f + 3, 0, GP())
        s.set(x, f + 2, 7, GP())
        s.set(x, f + 3, 7, GP())
    s.set(3, f + 1, 5, LECTERN(facing="north"))
    s.set(4, f + 1, 5, LECTERN(facing="north"))
    s.set(3, f + 1, 6, BOOKS())
    s.set(4, f + 1, 6, BOOKS())
    s.set(2, f + 1, 6, CARPET("blue"))
    s.set(5, f + 1, 6, CARPET("blue"))
    s.set(3, f + 1, 4, CARPET("blue"))
    s.set(4, f + 1, 4, CARPET("blue"))
    s.set(1, f + 1, 1, FENCE())
    s.set(6, f + 1, 1, FENCE())
    s.set(1, f + 2, 1, POT())
    s.set(6, f + 2, 1, POT())
    s.set(3, f + 5, 3, HLANTERN())
    s.set(
        0,
        f + 1,
        2,
        jigsaw(
            "west_up",
            "minecraft:building_entrance",
            "minecraft:building_entrance",
            "minecraft:empty",
            "minecraft:structure_void",
        ),
    )
    s.save(STRUCT / "synagogue_small.nbt")


def well() -> None:
    s = Structure(5, 5 + FOUNDATION, 5, with_foundation=True)
    f = s.fy
    s.fills(0, f, 0, 4, f, 4, SSS())
    s.fills(1, f, 1, 3, f, 3, CS())
    for x, z in ((1, 1), (2, 1), (3, 1), (1, 3), (2, 3), (3, 3), (1, 2), (3, 2)):
        s.set(x, f + 1, z, SW())
    s.set(2, f, 2, WAT())
    s.set(1, f + 2, 2, FENCE())
    s.set(3, f + 2, 2, FENCE())
    s.set(1, f + 3, 2, FENCE())
    s.set(3, f + 3, 2, FENCE())
    s.set(2, f + 3, 2, FENCE())
    s.set(2, f + 4, 2, LANTERN())
    s.set(
        0,
        f + 1,
        2,
        jigsaw(
            "west_up",
            "minecraft:building_entrance",
            "minecraft:building_entrance",
            "minecraft:empty",
            "minecraft:structure_void",
        ),
    )
    s.save(STRUCT / "well.nbt")


def wall_gate() -> None:
    """Street-aligned gate arch: passage runs along the street axis (local X).

    Previously this was a building_entrance piece beside the street, so from the
    plaza you saw a solid stone-brick flank. Now it is a street piece with
    west/east street jigsaws and an open tunnel through both ends.
    """
    s = Structure(9, 7, 7)
    s.fills(0, 0, 0, 8, 0, 6, SSS())
    s.fills(0, 0, 2, 8, 0, 4, CS())
    s.fills(0, 1, 0, 8, 5, 1, SB())
    s.fills(0, 1, 5, 8, 5, 6, SB())
    s.fills(0, 5, 0, 8, 5, 6, SB())
    s.fills(0, 1, 2, 8, 4, 4, AIR())
    for x in (0, 8):
        s.set(x, 4, 2, CSB()); s.set(x, 4, 3, CSB()); s.set(x, 4, 4, CSB())
        s.set(x, 1, 1, CSB()); s.set(x, 2, 1, CSB()); s.set(x, 3, 1, CSB())
        s.set(x, 1, 5, CSB()); s.set(x, 2, 5, CSB()); s.set(x, 3, 5, CSB())
    for x in range(0, 9, 2):
        s.set(x, 6, 0, SW()); s.set(x, 6, 6, SW())
    s.set(4, 5, 3, LANTERN())
    s.set(0, 1, 3, jigsaw("west_up", "minecraft:street", "minecraft:street",
                          "israel_simulator:jerusalem/streets", "minecraft:structure_void"))
    s.set(8, 1, 3, jigsaw("east_up", "minecraft:street", "minecraft:street",
                          "israel_simulator:jerusalem/streets", "minecraft:structure_void"))
    s.set(4, 1, 6, jigsaw("south_up", "minecraft:building_entrance", "minecraft:building_entrance",
                          "israel_simulator:jerusalem/buildings", "minecraft:structure_void"))
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


def update_structure_json() -> None:
    data = json.loads(STRUCTURE_JSON.read_text())
    # Mirror minecraft:village_desert / village_plains (26.2):
    # beard_thin, size 6, max_distance 80, WORLD_SURFACE_WG, start_height 0.
    data["terrain_adaptation"] = "beard_thin"
    data["max_distance_from_center"] = 80
    data["size"] = 6
    data["project_start_to_heightmap"] = "WORLD_SURFACE_WG"
    data["start_height"] = {"absolute": 0}
    data["use_expansion_hack"] = True
    data["step"] = "surface_structures"
    # Keep our biome filter (not the vanilla village tag).
    data["biomes"] = [
        "israel_simulator:jerusalem",
        "minecraft:savanna_plateau",
    ]
    STRUCTURE_JSON.write_text(json.dumps(data, indent=2) + "\n")
    print(
        f"updated {STRUCTURE_JSON.relative_to(ROOT)} "
        f"terrain_adaptation=beard_thin biomes={data['biomes']}"
    )


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
    update_structure_json()

    start_pool = (
        ROOT
        / "src/main/resources/data/israel_simulator/worldgen/template_pool/jerusalem_city.json"
    )
    write_pool(
        start_pool,
        [("israel_simulator:jerusalem/plaza", 1, "rigid")],
        "minecraft:empty",
    )
    write_pool(
        POOL / "streets.json",
        [
            ("israel_simulator:jerusalem/street_straight", 12, "terrain_matching"),
            ("israel_simulator:jerusalem/street_crossroad", 6, "terrain_matching"),
            ("israel_simulator:jerusalem/street_corner", 6, "terrain_matching"),
            # Rare: ~1/25 street picks -> about 1-2 gates per city
            ("israel_simulator:jerusalem/wall_gate", 1, "terrain_matching"),
        ],
        "israel_simulator:jerusalem/terminators",
    )
    write_pool(
        POOL / "terminators.json",
        [("israel_simulator:jerusalem/terminator", 1, "terrain_matching")],
        "minecraft:empty",
    )
    # synagogue removed from random buildings — it comes from landmarks (plaza-guaranteed)
    write_pool(
        POOL / "buildings.json",
        [
            ("israel_simulator:jerusalem/house_a", 4, "rigid"),
            ("israel_simulator:jerusalem/house_b", 4, "rigid"),
            ("israel_simulator:jerusalem/house_c", 3, "rigid"),
            ("israel_simulator:jerusalem/shuk_stalls", 3, "rigid"),
            ("israel_simulator:jerusalem/well", 1, "rigid"),  # was weight 2 (~9 wells/city)
        ],
        "minecraft:empty",
    )
    write_pool(
        POOL / "landmarks.json",
        [("israel_simulator:jerusalem/synagogue_small", 1, "rigid")],
        "minecraft:empty",
    )

    marker = Structure(3, 3, 3)
    marker.fills(0, 0, 0, 2, 0, 2, SSS())
    marker.set(1, 1, 1, YT())
    marker.save(
        ROOT / "src/main/resources/data/israel_simulator/structure/jerusalem_city.nbt"
    )


if __name__ == "__main__":
    main()
