#!/usr/bin/env python3
"""Generate agricultural_farm structure NBT — DataVersion 4903.

Fields, irrigation channels, greenhouses, shed + loot chest. Uses connect_blocks.
"""
from __future__ import annotations

import json
from pathlib import Path
from typing import Any

import nbtlib
from nbtlib import Compound, Int, List, Long, String

from connect_blocks import apply_connections

ROOT = Path(__file__).resolve().parents[2]
STRUCT = ROOT / "src/main/resources/data/israel_simulator/structure/agricultural_farm.nbt"
STRUCTURE_JSON = ROOT / "src/main/resources/data/israel_simulator/worldgen/structure/agricultural_farm.json"
POOL_JSON = ROOT / "src/main/resources/data/israel_simulator/worldgen/template_pool/agricultural_farm.json"
SET_JSON = ROOT / "src/main/resources/data/israel_simulator/worldgen/structure_set/agricultural_farms.json"
DATA_VERSION = 4903
BLOCKS_JSON = Path("/workspace/mcreports/out/reports/blocks.json")
KNOWN = json.loads(BLOCKS_JSON.read_text()) if BLOCKS_JSON.exists() else {}

# Footprint: 48 x 10 x 40
SX, SY, SZ = 48, 10, 40
FY = 1  # floor / walkable


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


class Structure:
    def __init__(self, sx: int, sy: int, sz: int):
        self.sx, self.sy, self.sz = sx, sy, sz
        self.palette: list[Compound] = []
        self.index: dict[str, int] = {}
        self.blocks: dict[tuple[int, int, int], int] = {}
        self.block_nbt: dict[tuple[int, int, int], Compound] = {}
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
            raise ValueError(f"out of bounds {(x, y, z)}")
        self.blocks[(x, y, z)] = self._state(entry)
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

        new_palette: list[Compound] = []
        new_index: dict[str, int] = {}
        blocks_list: list[Compound] = []
        for (x, y, z) in sorted(grid.keys()):
            cell = grid[(x, y, z)]
            props = cell["Properties"]
            pe = Compound({"Name": String(cell["Name"])})
            if props:
                pe["Properties"] = Compound({k: String(v) for k, v in props.items()})
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
        print(f"wrote {path.relative_to(ROOT)} size={self.sx}x{self.sy}x{self.sz} blocks={len(blocks_list)}")


# Materials
def DIRT(): return blk("minecraft:dirt")
def GRASS(): return blk("minecraft:grass_block", snowy="false")
def FARMLAND(moist="true"): return blk("minecraft:farmland", moisture="7" if moist == "true" else "0")
def WATER(): return blk("minecraft:water", level="0")
def WHEAT(age="7"): return blk("minecraft:wheat", age=age)
def CARROT(age="7"): return blk("minecraft:carrots", age=age)
def POTATO(age="7"): return blk("minecraft:potatoes", age=age)
def BEET(age="7"): return blk("minecraft:beetroots", age=age)
def GRAPEVINE(age="7"): return blk("israel_simulator:grapevine", age=age)
def OAK_LOG(axis="y"): return blk("minecraft:oak_log", axis=axis)
def OAK_PLANK(): return blk("minecraft:oak_planks")
def OAK_SLAB(typ="bottom"): return blk("minecraft:oak_slab", type=typ, waterlogged="false")
def OAK_STAIR(facing="east", half="bottom"): return blk(
    "minecraft:oak_stairs", facing=facing, half=half, shape="straight", waterlogged="false"
)
def FENCE(): return blk(
    "minecraft:oak_fence", north="false", south="false", east="false", west="false", waterlogged="false"
)
def GATE(facing="south", open="false"): return blk(
    "minecraft:oak_fence_gate", facing=facing, open=open, powered="false", in_wall="false"
)
def GLASS(): return blk("minecraft:glass")
def GLASS_PANE(): return blk(
    "minecraft:glass_pane", north="false", south="false", east="false", west="false", waterlogged="false"
)
def LANTERN(): return blk("minecraft:lantern", hanging="false", waterlogged="false")
def COMPOSTER(level="5"): return blk("minecraft:composter", level=level)
def HAY(): return blk("minecraft:hay_block", axis="y")
def BARREL(facing="up"): return blk("minecraft:barrel", facing=facing, open="false")
def TORCH(): return blk("minecraft:torch")
def PATH(): return blk("minecraft:dirt_path")
def COBBLE(): return blk("minecraft:cobblestone")
def STONE_BRICK(): return blk("minecraft:stone_bricks")


def CHEST(facing="south", loot=None):
    entry = blk("minecraft:chest", facing=facing, type="single", waterlogged="false")
    if loot:
        entry["_nbt"] = Compound(
            {
                "id": String("minecraft:chest"),
                "LootTable": String(loot),
                "LootTableSeed": Long(0),
            }
        )
    return entry


def crop_for(x: int, z: int) -> Compound:
    r = (x * 3 + z * 7) % 5
    ages = ["5", "6", "7", "7", "4"]
    age = ages[(x + z) % len(ages)]
    if r == 0:
        return WHEAT(age)
    if r == 1:
        return CARROT(age)
    if r == 2:
        return POTATO(age)
    if r == 3:
        return BEET(str(min(int(age), 3)))
    return GRAPEVINE(age)


def build() -> Structure:
    s = Structure(SX, SY, SZ)
    # Footing
    s.fills(0, 0, 0, SX - 1, FY - 1, SZ - 1, DIRT())

    # Paths + perimeter grass
    for x in range(SX):
        for z in range(SZ):
            if z == 0 or z == SZ - 1 or x == 0 or x == SX - 1:
                s.set(x, FY, z, GRASS())
            elif x == 23 or x == 24 or z == 18 or z == 19:
                s.set(x, FY, z, PATH())
            else:
                s.set(x, FY, z, DIRT())

    # --- Field A (west): wheat/carrot rows with irrigation every 4 ---
    for x in range(2, 21):
        for z in range(2, 17):
            if x % 4 == 0:
                s.set(x, FY, z, WATER())
                s.set(x, 0, z, DIRT())
            else:
                s.set(x, FY, z, FARMLAND())
                s.set(x, FY + 1, z, crop_for(x, z))

    # --- Field B (east): grapevines + potatoes ---
    for x in range(26, 46):
        for z in range(2, 17):
            if z % 5 == 0:
                s.set(x, FY, z, WATER())
            else:
                s.set(x, FY, z, FARMLAND())
                if (x + z) % 3 == 0:
                    s.set(x, FY + 1, z, GRAPEVINE("7"))
                else:
                    s.set(x, FY + 1, z, crop_for(x, z))

    # --- Field C (south): smaller plot ---
    for x in range(2, 21):
        for z in range(21, 37):
            if x % 5 == 2:
                s.set(x, FY, z, WATER())
            else:
                s.set(x, FY, z, FARMLAND())
                s.set(x, FY + 1, z, WHEAT(str(4 + (x + z) % 4)))

    # Perimeter fence
    for x in range(SX):
        s.set(x, FY + 1, 0, FENCE())
        s.set(x, FY + 1, SZ - 1, FENCE())
    for z in range(SZ):
        s.set(0, FY + 1, z, FENCE())
        s.set(SX - 1, FY + 1, z, FENCE())
    # Gates on paths
    s.set(23, FY + 1, 0, GATE(facing="south"))
    s.set(24, FY + 1, 0, GATE(facing="south"))
    s.set(23, FY + 1, SZ - 1, GATE(facing="north"))
    s.set(24, FY + 1, SZ - 1, GATE(facing="north"))

    # --- Greenhouses (glass boxes) east-south ---
    def greenhouse(x0, z0, w, d, h=4):
        # floor
        s.fills(x0, FY, z0, x0 + w - 1, FY, z0 + d - 1, PATH())
        # walls glass panes / glass
        for y in range(FY + 1, FY + h):
            for x in range(x0, x0 + w):
                s.set(x, y, z0, GLASS_PANE())
                s.set(x, y, z0 + d - 1, GLASS_PANE())
            for z in range(z0, z0 + d):
                s.set(x0, y, z, GLASS_PANE())
                s.set(x0 + w - 1, y, z, GLASS_PANE())
        # roof glass
        s.fills(x0, FY + h, z0, x0 + w - 1, FY + h, z0 + d - 1, GLASS())
        # door opening
        s.set(x0 + w // 2, FY + 1, z0, blk("minecraft:air"))
        s.set(x0 + w // 2, FY + 2, z0, blk("minecraft:air"))
        # interior farmland + grapevines
        for x in range(x0 + 1, x0 + w - 1):
            for z in range(z0 + 1, z0 + d - 1):
                s.set(x, FY, z, FARMLAND())
                s.set(x, FY + 1, z, GRAPEVINE("6" if (x + z) % 2 else "7"))

    greenhouse(26, 22, 9, 7)
    greenhouse(37, 22, 9, 7)

    # --- Shed (oak) near center path south ---
    sx0, sz0 = 26, 31
    # floor + walls
    s.fills(sx0, FY, sz0, sx0 + 6, FY, sz0 + 5, OAK_PLANK())
    for y in range(FY + 1, FY + 4):
        for x in range(sx0, sx0 + 7):
            s.set(x, y, sz0, OAK_LOG())
            s.set(x, y, sz0 + 5, OAK_LOG())
        for z in range(sz0, sz0 + 6):
            s.set(sx0, y, z, OAK_LOG())
            s.set(sx0 + 6, y, z, OAK_LOG())
    # fill walls with planks between posts
    for y in range(FY + 1, FY + 4):
        for x in range(sx0 + 1, sx0 + 6):
            s.set(x, y, sz0, OAK_PLANK())
            s.set(x, y, sz0 + 5, OAK_PLANK())
        for z in range(sz0 + 1, sz0 + 5):
            s.set(sx0, y, z, OAK_PLANK())
            s.set(sx0 + 6, y, z, OAK_PLANK())
    # door
    s.set(sx0 + 3, FY + 1, sz0, blk("minecraft:air"))
    s.set(sx0 + 3, FY + 2, sz0, blk("minecraft:air"))
    # roof slabs
    s.fills(sx0, FY + 4, sz0, sx0 + 6, FY + 4, sz0 + 5, OAK_SLAB("bottom"))
    # interior
    s.set(sx0 + 1, FY + 1, sz0 + 4, CHEST(facing="south", loot="israel_simulator:chests/agricultural_farm"))
    s.set(sx0 + 5, FY + 1, sz0 + 4, BARREL())
    s.set(sx0 + 2, FY + 1, sz0 + 3, COMPOSTER())
    s.set(sx0 + 4, FY + 1, sz0 + 3, HAY())
    s.set(sx0 + 1, FY + 2, sz0 + 1, LANTERN())

    # Hay stacks + composters outdoors
    for x, z in [(10, 19), (14, 19), (34, 19)]:
        s.set(x, FY + 1, z, HAY())
        s.set(x, FY + 2, z, HAY())
    s.set(12, FY + 1, 19, COMPOSTER("7"))

    # Lamp posts along central path
    for z in (6, 12, 24, 32):
        s.set(23, FY + 1, z, FENCE())
        s.set(23, FY + 2, z, FENCE())
        s.set(23, FY + 3, z, LANTERN())

    # Water well (stone brick ring + water) near path
    wx, wz = 20, 19
    for dx, dz in [(-1, 0), (1, 0), (0, -1), (0, 1), (-1, -1), (1, -1), (-1, 1), (1, 1)]:
        s.set(wx + dx, FY + 1, wz + dz, STONE_BRICK())
    s.set(wx, FY, wz, WATER())
    s.set(wx, FY + 1, wz, blk("minecraft:air"))

    return s


def write_json() -> None:
    structure = {
        "type": "minecraft:jigsaw",
        "biomes": ["israel_simulator:israeli_agriculture"],
        "max_distance_from_center": 80,
        "project_start_to_heightmap": "WORLD_SURFACE_WG",
        "size": 1,
        "spawn_overrides": {},
        "start_height": {"absolute": 0},
        "start_pool": "israel_simulator:agricultural_farm",
        "step": "surface_structures",
        "terrain_adaptation": "beard_box",
        "use_expansion_hack": False,
    }
    STRUCTURE_JSON.write_text(json.dumps(structure, indent=2) + "\n")
    pool = {
        "fallback": "minecraft:empty",
        "elements": [
            {
                "weight": 1,
                "element": {
                    "element_type": "minecraft:single_pool_element",
                    "location": "israel_simulator:agricultural_farm",
                    "processors": "minecraft:empty",
                    "projection": "rigid",
                },
            }
        ],
    }
    POOL_JSON.write_text(json.dumps(pool, indent=2) + "\n")
    aset = {
        "structures": [{"structure": "israel_simulator:agricultural_farm", "weight": 1}],
        "placement": {
            "type": "minecraft:random_spread",
            "spacing": 32,
            "separation": 12,
            "salt": 91827364,
        },
    }
    SET_JSON.write_text(json.dumps(aset, indent=2) + "\n")
    print("updated structure/pool/set JSON")


def main() -> None:
    s = build()
    s.save(STRUCT)
    write_json()


if __name__ == "__main__":
    main()
