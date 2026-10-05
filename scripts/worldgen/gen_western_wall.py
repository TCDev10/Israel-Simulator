#!/usr/bin/env python3
"""Generate the Western Wall (Kotel) structure NBT — DataVersion 4903.

Prayer works by right-clicking israel_simulator:western_wall_stone (ModGameEvents),
so the plaza-facing wall face is mostly that block.
"""
from __future__ import annotations

import json
from pathlib import Path
from typing import Any

import nbtlib
from nbtlib import Byte, Compound, Int, List, Long, String

from connect_blocks import apply_connections

ROOT = Path(__file__).resolve().parents[2]
STRUCT = ROOT / "src/main/resources/data/israel_simulator/structure/western_wall.nbt"
STRUCTURE_JSON = ROOT / "src/main/resources/data/israel_simulator/worldgen/structure/western_wall.json"
POOL_JSON = ROOT / "src/main/resources/data/israel_simulator/worldgen/template_pool/western_wall.json"
SET_JSON = ROOT / "src/main/resources/data/israel_simulator/worldgen/structure_set/western_walls.json"
DATA_VERSION = 4903
BLOCKS_JSON = Path("/workspace/mcreports/out/reports/blocks.json")
KNOWN = json.loads(BLOCKS_JSON.read_text()) if BLOCKS_JSON.exists() else {}

# Layout (X along wall, Z toward plaza, Y up)
# Wall length 56, plaza depth 36, wall thickness ~4 with setbacks, height ~18
SX, SY, SZ = 56, 20, 42
WALL_Z0, WALL_Z1 = 0, 3          # wall occupies z=0..3 (front face at z=3)
PLAZA_Z0, PLAZA_Z1 = 4, 39       # plaza in front of wall
FLOOR_Y = 1                      # walkable floor; y=0 footing


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
        print(f"wrote {path.relative_to(ROOT)} size={self.sx}x{self.sy}x{self.sz}")


# Materials
JS = lambda: blk("israel_simulator:jerusalem_stone")
WWS = lambda: blk("israel_simulator:western_wall_stone")
SS = lambda: blk("minecraft:sandstone")
SSS = lambda: blk("minecraft:smooth_sandstone")
CS = lambda: blk("minecraft:cut_sandstone")
CH = lambda: blk("minecraft:chiseled_sandstone")
CALC = lambda: blk("minecraft:calcite")
SQ = lambda: blk("minecraft:smooth_quartz")
SSLAB = lambda typ="bottom": blk("minecraft:sandstone_slab", type=typ, waterlogged="false")
SSTAIR = lambda facing="south", half="bottom": blk(
    "minecraft:sandstone_stairs",
    facing=facing,
    half=half,
    shape="straight",
    waterlogged="false",
)
FENCE = lambda: blk(
    "minecraft:oak_fence",
    north="false",
    south="false",
    east="false",
    west="false",
    waterlogged="false",
)
WALL = lambda: blk(
    "minecraft:sandstone_wall",
    up="true",
    north="none",
    south="none",
    east="none",
    west="none",
    waterlogged="false",
)
LANTERN = lambda: blk("minecraft:lantern", hanging="false", waterlogged="false")
HLANTERN = lambda: blk("minecraft:lantern", hanging="true", waterlogged="false")
LECTERN = lambda facing="south": blk(
    "minecraft:lectern", facing=facing, has_book="true", powered="false"
)
FERN = lambda: blk("minecraft:fern")
VINE = lambda: blk(
    "minecraft:vine",
    north="false",
    south="true",
    east="false",
    west="false",
    up="false",
)
GRASS = lambda: blk("minecraft:short_grass")
POT = lambda: blk("minecraft:potted_fern")


def CHEST(facing: str = "south", loot: str | None = None) -> Compound:
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


def ashlar(y: int, face: bool) -> Compound:
    """Pick a stone for a course; plaza-facing front is western_wall_stone for prayer."""
    if face:
        # Mostly prayer stone on the front face; occasional variants for texture
        if y % 7 == 3:
            return CH()
        if y % 11 == 5:
            return CALC()
        return WWS()
    # Core / back of wall
    r = (y * 3) % 5
    if r == 0:
        return JS()
    if r == 1:
        return CS()
    if r == 2:
        return SSS()
    if r == 3:
        return SS()
    return SQ()


def build() -> Structure:
    s = Structure(SX, SY, SZ)
    fy = FLOOR_Y

    # Footing under whole footprint
    s.fills(0, 0, 0, SX - 1, fy - 1, SZ - 1, SS())

    # --- Plaza floor (pale stone courses) ---
    for x in range(SX):
        for z in range(PLAZA_Z0, PLAZA_Z1 + 1):
            # Slight checker / banding
            if (x + z) % 9 == 0:
                s.set(x, fy, z, SSS())
            elif (x + z) % 5 == 0:
                s.set(x, fy, z, CS())
            else:
                s.set(x, fy, z, JS())
            # Extra depth under plaza for beard
            s.set(x, 0, z, SS())

    # Steps: two shallow terraces near the wall
    for x in range(2, SX - 2):
        s.set(x, fy, PLAZA_Z0, SSTAIR(facing="south", half="bottom"))
        s.set(x, fy, PLAZA_Z0 + 1, SSLAB("bottom"))
        s.set(x, fy + 1, PLAZA_Z0, SSLAB("bottom"))

    # Raised prayer strip right against the wall (y = fy+1)
    for x in range(1, SX - 1):
        s.set(x, fy + 1, WALL_Z1 + 1, SSS())
        s.set(x, fy, WALL_Z1 + 1, SS())

    # --- Herodian wall with setbacks ---
    # Courses: lower = thicker & taller blocks feel; setback every few rows
    wall_top = fy + 17  # ~18 high from footing
    for y in range(fy, wall_top + 1):
        # Setback: lower courses thicker toward plaza
        if y < fy + 4:
            z_back, z_front = WALL_Z0, WALL_Z1  # full 4 thick
        elif y < fy + 9:
            z_back, z_front = WALL_Z0, WALL_Z1 - 1
        elif y < fy + 14:
            z_back, z_front = WALL_Z0, WALL_Z1 - 2
        else:
            z_back, z_front = WALL_Z0, WALL_Z0 + 1

        for x in range(SX):
            for z in range(z_back, z_front + 1):
                face = z == z_front
                # Leave a small alcove for treasury at x=2..5, y=fy+1..fy+3, z=front hollowed inward
                if 2 <= x <= 5 and fy + 1 <= y <= fy + 3 and z == z_front:
                    continue  # opening into alcove
                s.set(x, y, z, ashlar(y, face))

        # Horizontal joint line every 3 rows (chiseled course on front)
        if (y - fy) % 3 == 2 and y < wall_top:
            for x in range(SX):
                s.set(x, y, z_front, CH())

    # Rebuild alcove walls + chest
    # Alcove carved into wall at left side
    for y in range(fy + 1, fy + 4):
        for z in range(WALL_Z1 - 1, WALL_Z1 + 1):
            s.set(2, y, z, WWS())
            s.set(5, y, z, WWS())
        s.set(3, y, WALL_Z1 - 1, WWS())
        s.set(4, y, WALL_Z1 - 1, WWS())
    s.set(3, fy + 1, WALL_Z1 - 1, CHEST(facing="south", loot="israel_simulator:chests/western_wall_treasury"))
    s.set(4, fy + 1, WALL_Z1 - 1, WWS())
    # Alcove ceiling / floor
    for x in range(2, 6):
        s.set(x, fy + 4, WALL_Z1 - 1, SSS())
        s.set(x, fy, WALL_Z1, SSS())
        s.set(x, fy, WALL_Z1 - 1, SSS())

    # Cracks / plants on lower wall face
    for x, y in [(8, fy + 2), (14, fy + 3), (22, fy + 2), (31, fy + 4), (40, fy + 3), (48, fy + 2)]:
        s.set(x, y, WALL_Z1 + 1, FERN() if x % 2 == 0 else GRASS())
    for x in (10, 27, 44):
        s.set(x, fy + 5, WALL_Z1, VINE())

    # Cap / coping
    for x in range(SX):
        s.set(x, wall_top + 1 if wall_top + 1 < SY else wall_top, WALL_Z0, SSLAB("bottom"))
        if WALL_Z0 + 1 < SX:
            s.set(x, wall_top, WALL_Z0 + 1, SSLAB("bottom"))

    # --- Mechitza (partition) dividing plaza: larger section x=0..37, smaller x=38..55 ---
    mx = 38
    for z in range(PLAZA_Z0 + 2, PLAZA_Z1 - 1):
        s.set(mx, fy + 1, z, FENCE())
        if z % 4 == 0:
            s.set(mx, fy + 2, z, LANTERN())
    # Gate opening in mechitza near wall
    for z in range(PLAZA_Z0 + 2, PLAZA_Z0 + 5):
        s.set(mx, fy + 1, z, blk("minecraft:air"))
        s.set(mx, fy + 2, z, blk("minecraft:air"))
    # Arch posts
    s.set(mx, fy + 1, PLAZA_Z0 + 2, WALL())
    s.set(mx, fy + 2, PLAZA_Z0 + 2, WALL())
    s.set(mx, fy + 1, PLAZA_Z0 + 5, WALL())
    s.set(mx, fy + 2, PLAZA_Z0 + 5, WALL())

    # Perimeter low wall around plaza edges
    for x in range(SX):
        s.set(x, fy + 1, PLAZA_Z1, WALL())
    for z in range(PLAZA_Z0, PLAZA_Z1 + 1):
        s.set(0, fy + 1, z, WALL())
        s.set(SX - 1, fy + 1, z, WALL())
    # Entry gap at +Z center of large section
    for x in range(18, 24):
        s.set(x, fy + 1, PLAZA_Z1, blk("minecraft:air"))
    s.set(17, fy + 1, PLAZA_Z1, WALL())
    s.set(24, fy + 1, PLAZA_Z1, WALL())
    s.set(17, fy + 2, PLAZA_Z1, LANTERN())
    s.set(24, fy + 2, PLAZA_Z1, LANTERN())

    # Benches (stairs) facing the wall
    for x in range(6, 16, 3):
        s.set(x, fy + 1, 18, SSTAIR(facing="north", half="bottom"))
        s.set(x + 1, fy + 1, 18, SSTAIR(facing="north", half="bottom"))
    for x in range(42, 52, 3):
        s.set(x, fy + 1, 18, SSTAIR(facing="north", half="bottom"))
        s.set(x + 1, fy + 1, 18, SSTAIR(facing="north", half="bottom"))

    # Prayer lecterns
    s.set(12, fy + 1, 10, LECTERN(facing="north"))
    s.set(28, fy + 1, 10, LECTERN(facing="north"))
    s.set(45, fy + 1, 10, LECTERN(facing="north"))

    # Lamp posts (fence + lantern)
    for x, z in [(4, 12), (4, 28), (20, 22), (34, 22), (51, 12), (51, 28)]:
        s.set(x, fy + 1, z, FENCE())
        s.set(x, fy + 2, z, FENCE())
        s.set(x, fy + 3, z, LANTERN())

    # Potted ferns near wall corners
    s.set(1, fy + 1, WALL_Z1 + 2, POT())
    s.set(SX - 2, fy + 1, WALL_Z1 + 2, POT())

    # Optional short Mughrabi-style ramp on +X side (simple stairs up toward wall top access)
    ramp_x = SX - 3
    for i, z in enumerate(range(PLAZA_Z1 - 8, WALL_Z1 + 1, -1)):
        h = fy + 1 + min(i, 6)
        if h < SY and 0 <= z < SZ:
            s.set(ramp_x, h, z, SSTAIR(facing="north", half="bottom"))
            s.set(ramp_x - 1, h, z, SSS())
            s.set(ramp_x + 1, h, z, WALL())

    # Side gateway arch at plaza entry of small section
    for y in range(fy + 1, fy + 4):
        s.set(mx + 8, y, PLAZA_Z1, WALL())
        s.set(mx + 12, y, PLAZA_Z1, WALL())
    for x in range(mx + 9, mx + 12):
        s.set(x, fy + 1, PLAZA_Z1, blk("minecraft:air"))
        s.set(x, fy + 3, PLAZA_Z1, SSS())

    return s


def write_json() -> None:
    structure = {
        "type": "minecraft:jigsaw",
        "biomes": ["israel_simulator:jerusalem"],
        "max_distance_from_center": 80,
        "project_start_to_heightmap": "WORLD_SURFACE_WG",
        "size": 1,
        "spawn_overrides": {},
        "start_height": {"absolute": 0},
        "start_pool": "israel_simulator:western_wall",
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
                    "location": "israel_simulator:western_wall",
                    "processors": "minecraft:empty",
                    "projection": "rigid",
                },
            }
        ],
    }
    POOL_JSON.write_text(json.dumps(pool, indent=2) + "\n")
    # Slightly rarer than before but still /locate-friendly in jerusalem biome
    aset = {
        "structures": [{"structure": "israel_simulator:western_wall", "weight": 1}],
        "placement": {
            "type": "minecraft:random_spread",
            "spacing": 28,
            "separation": 10,
            "salt": 81928472,
        },
    }
    SET_JSON.write_text(json.dumps(aset, indent=2) + "\n")
    print(f"updated {STRUCTURE_JSON.relative_to(ROOT)}")
    print(f"updated {POOL_JSON.relative_to(ROOT)}")
    print(f"updated {SET_JSON.relative_to(ROOT)}")


def main() -> None:
    s = build()
    s.save(STRUCT)
    write_json()


if __name__ == "__main__":
    main()
