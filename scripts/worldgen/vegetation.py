#!/usr/bin/env python3
"""Make every plant in a structure template physically possible (DataVersion 4903).

Called from connect_blocks.apply_connections, so every generator that saves through
gen_jerusalem_city / structure_lib / jigsaw_kit gets it, and runnable on finished NBT files:

    /workspace/nbtvenv/bin/python scripts/worldgen/vegetation.py [--check] <file.nbt|dir> ...

Rules (the same ones the game applies at the first block update):
  * a plant needs its soil right below: grass, flowers, ferns, bushes, saplings, azaleas on a dirt
    block, crops on farmland, dead bushes / dry grass on dirt, sand or terracotta, cactus on sand.
    A plant standing on a full masonry block (a planter rim, a paving slab) gets that block turned
    into soil; one standing on air, a slab, a stair, a fence... is removed;
  * a double plant needs both halves;
  * a vine keeps only the faces that touch a full block (or the same face of the vine above);
    with no face left it is removed;
  * leaves and logs must touch something: a leaf blob that only touches other blobs diagonally is
    joined to them with one leaf, a blob touching nothing at all is removed;
  * template leaves are persistent, so they never decay into holes.
Cells that are not in the grid (structure void) or outside it count as unknown and are trusted.
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

AIR = {"minecraft:air", "minecraft:cave_air", "minecraft:void_air"}
UNKNOWN = {"minecraft:structure_void", "minecraft:jigsaw"}
SOIL = {"grass_block", "dirt", "coarse_dirt", "podzol", "rooted_dirt", "mud", "moss_block", "farmland",
        "mycelium", "pale_moss_block", "muddy_mangrove_roots", "dirt_path"}
DRY_SOIL = SOIL | {"sand", "red_sand", "suspicious_sand", "terracotta"}
FLOWERS = {"dandelion", "poppy", "blue_orchid", "allium", "azure_bluet", "red_tulip", "orange_tulip",
           "white_tulip", "pink_tulip", "oxeye_daisy", "cornflower", "lily_of_the_valley", "torchflower",
           "closed_eyeblossom", "open_eyeblossom", "pink_petals", "wildflowers", "rose_bush", "peony",
           "lilac", "sunflower", "pitcher_plant", "azalea", "flowering_azalea", "sweet_berry_bush"}
SMALL = {"short_grass", "fern", "bush", "firefly_bush", "leaf_litter", "mediterranean_herbs"} | FLOWERS
DRY = {"dead_bush", "short_dry_grass", "tall_dry_grass"}
DOUBLE = {"tall_grass", "large_fern", "sunflower", "lilac", "rose_bush", "peony", "pitcher_plant"}
CROPS = {"wheat", "carrots", "potatoes", "beetroots", "melon_stem", "pumpkin_stem", "torchflower_crop",
         "grapevine", "tomato_crop", "eggplant_crop", "chickpea_crop", "sesame_crop"}
MASONRY = re.compile(r"(stone|brick|sandstone|calcite|terracotta|concrete|deepslate|andesite|diorite|granite|"
                     r"tuff|quartz|packed_mud|cobble|blackstone|basalt|prismarine|purpur|end_stone|dripstone|"
                     r"jerusalem_stone|western_wall_stone|limestone|smooth|cut_|chiseled|polished|gravel|clay)")
NOT_FULL = re.compile(r"(_slab|_stairs|_wall|fence|_pane|bars|door|trapdoor|button|plate|carpet|sign|banner|"
                      r"torch|lantern|chain|ladder|rail|pot|candle|lever|head|skull|campfire|rod|bed|chest|"
                      r"barrel|lectern|anvil|cauldron|hopper|bell|grindstone|stonecutter|beehive|jigsaw|"
                      r"composter|glass|leaves|_log|_wood|vine|snow|water|lava)")
FACES = {"north": (0, 0, -1), "south": (0, 0, 1), "east": (1, 0, 0), "west": (-1, 0, 0), "up": (0, 1, 0)}
N6 = ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1))


def short(name: str) -> str:
    return name.split(":", 1)[-1]


def is_leaves(n: str) -> bool:
    return n.endswith("_leaves")


def is_log(n: str) -> bool:
    s = short(n)
    return (s.endswith("_log") or s.endswith("_wood")) and not s.startswith("stripped_")


def is_plant(n: str) -> bool:
    s = short(n)
    return (s in SMALL or s in DRY or s in DOUBLE or s in CROPS or s.endswith("_sapling")
            and not s.startswith("potted_") or s in ("cactus", "sugar_cane"))


def is_veg(n: str) -> bool:
    return is_plant(n) or is_leaves(n) or is_log(n) or short(n) == "vine"


def _cell(name: str, props=None) -> dict:
    return {"Name": name, "Properties": dict(props or {})}


def sanitize_vegetation(grid: dict, size=None) -> dict:
    """Fix grid in place. grid: {(x,y,z): {"Name", "Properties"}}. Returns counts of fixes."""
    fixes: dict[str, int] = {}
    for c in grid.values():
        c["Name"] = str(c["Name"])
        c["Properties"] = {str(k): str(v) for k, v in (c.get("Properties") or {}).items()}

    def bump(k):
        fixes[k] = fixes.get(k, 0) + 1

    def name(p):
        c = grid.get(p)
        return None if c is None else c["Name"]

    def known(p):
        n = name(p)
        return n is not None and n not in UNKNOWN

    def full(p):
        n = name(p)
        if n is None or n in AIR:
            return False
        if n in UNKNOWN:
            return True          # trust what we cannot see
        if is_plant(n) or short(n) == "vine":
            return False
        if is_leaves(n) or is_log(n):
            return True
        return not NOT_FULL.search(short(n))

    def soil_ok(plant, below):
        s, b = short(plant), short(below)
        if s in CROPS:
            return b == "farmland"
        if s == "cactus":
            return b in ("sand", "red_sand", "cactus")
        if s == "sugar_cane":
            return b in DRY_SOIL or b == "sugar_cane"
        if s in DRY:
            return b in DRY_SOIL or b.endswith("_terracotta")
        if s == "leaf_litter":
            return True
        if s == "mediterranean_herbs":
            return b in SOIL
        return b in SOIL

    def soil_for(plant):
        s = short(plant)
        if s in CROPS:
            return _cell("minecraft:farmland", {"moisture": "7"})
        if s in ("cactus", "sugar_cane"):
            return None
        if s in FLOWERS:
            return _cell("minecraft:grass_block", {"snowy": "false"})
        return _cell("minecraft:coarse_dirt")

    def remove(p, why):
        grid[p] = _cell("minecraft:air")
        bump(why)

    # ---- plants and double plants
    for p in sorted(grid):
        n = name(p)
        if n is None or not is_plant(n):
            continue
        x, y, z = p
        props = grid[p]["Properties"]
        s = short(n)
        if s in DOUBLE and props.get("half") == "upper":
            if name((x, y - 1, z)) != n:
                remove(p, "double_plant_half")
            continue
        below = (x, y - 1, z)
        if s in ("cactus", "sugar_cane") and name(below) == n:
            continue
        if not known(below):
            continue
        bn = name(below)
        if not soil_ok(n, bn):
            if full(below) and MASONRY.search(short(bn)) and soil_for(n) is not None:
                grid[below] = soil_for(n)
                bump("soil_added")
            else:
                remove(p, "plant_removed")
                if s in DOUBLE and name((x, y + 1, z)) == n:
                    remove((x, y + 1, z), "double_plant_half")
                continue
        if s in DOUBLE and props.get("half", "lower") == "lower" and name((x, y + 1, z)) != n:
            above = (x, y + 1, z)
            if name(above) in AIR:
                grid[above] = _cell(n, {**props, "half": "upper"})
                bump("double_plant_completed")
            else:
                remove(p, "double_plant_half")

    # ---- vines (top-down so a hanging vine sees the faces of the one above)
    for p in sorted((q for q in grid if name(q) == "minecraft:vine"), key=lambda q: -q[1]):
        x, y, z = p
        props = dict(grid[p]["Properties"])
        above = grid.get((x, y + 1, z))
        changed = False
        for f, (dx, dy, dz) in FACES.items():
            if props.get(f) != "true":
                continue
            nb = (x + dx, y + dy, z + dz)
            ok = full(nb) or (f != "up" and above is not None and above["Name"] == "minecraft:vine"
                              and above["Properties"].get(f) == "true")
            if not ok:
                props[f] = "false"
                changed = True
        if not any(props.get(f) == "true" for f in FACES):
            remove(p, "vine_removed")
        elif changed:
            grid[p]["Properties"] = props
            bump("vine_faces_trimmed")

    # ---- leaves / logs
    for p, c in grid.items():
        if is_leaves(c["Name"]) and c["Properties"].get("persistent") == "false":
            c["Properties"]["persistent"] = "true"
            bump("leaves_made_persistent")

    # trunks standing on air: grow them down to the ground (max 6), otherwise the tree goes
    for p in sorted(q for q, c in grid.items() if is_log(c["Name"]) and c["Properties"].get("axis", "y") == "y"):
        x, y, z = p
        if name((x, y - 1, z)) not in AIR:
            continue
        depth = 0
        while depth < 6 and name((x, y - 1 - depth, z)) in AIR:
            depth += 1
        ground = (x, y - 1 - depth, z)
        if depth < 6 and full(ground):
            for k in range(1, depth + 1):
                grid[(x, y - k, z)] = _cell(grid[p]["Name"], grid[p]["Properties"])
            bump("trunk_grounded")

    def tree_cells():
        return {q for q, c in grid.items() if is_leaves(c["Name"]) or is_log(c["Name"])}

    def components(cells):
        seen = set()
        for start in sorted(cells):
            if start in seen:
                continue
            comp, stack = [], [start]
            seen.add(start)
            while stack:
                q = stack.pop()
                comp.append(q)
                for d in N6:
                    r = (q[0] + d[0], q[1] + d[1], q[2] + d[2])
                    if r in cells and r not in seen:
                        seen.add(r)
                        stack.append(r)
            yield comp

    def inside(r):
        return size is None or all(0 <= r[i] < size[i] for i in range(3))

    def anchored(comp, cells):
        for q in comp:
            for d in N6:
                r = (q[0] + d[0], q[1] + d[1], q[2] + d[2])
                if r in cells:
                    continue
                if not inside(r):
                    continue          # the world outside a piece is air more often than not
                n = name(r)
                if n is None or n in UNKNOWN:
                    return True
                if n not in AIR and not is_veg(n) and "water" not in n:
                    return True
                if short(n) in ("azalea", "flowering_azalea") and d == (0, -1, 0):
                    return True          # leafy crown on an azalea bush
        return False

    for _ in range(4):
        cells = tree_cells()
        comps = list(components(cells))
        good = set()
        loose = []
        for comp in comps:
            if anchored(comp, cells):
                good.update(comp)
            else:
                loose.append(comp)
        if not loose:
            break
        progress = False
        for comp in loose:
            bridged = False
            for q in comp:
                for dx in (-1, 0, 1):
                    for dy in (-1, 0, 1):
                        for dz in (-1, 0, 1):
                            if abs(dx) + abs(dy) + abs(dz) < 2:
                                continue
                            t = (q[0] + dx, q[1] + dy, q[2] + dz)
                            if t not in good:
                                continue
                            # one orthogonal step from q toward t that is free
                            for step in ((dx, 0, 0), (0, dy, 0), (0, 0, dz)):
                                if step == (0, 0, 0):
                                    continue
                                b = (q[0] + step[0], q[1] + step[1], q[2] + step[2])
                                if name(b) in AIR:
                                    src = grid[q] if is_leaves(grid[q]["Name"]) else grid[t]
                                    leaf = src if is_leaves(src["Name"]) else {"Name": "minecraft:oak_leaves",
                                                                               "Properties": {}}
                                    grid[b] = _cell(leaf["Name"], {**leaf["Properties"], "persistent": "true"})
                                    bump("leaves_bridged")
                                    bridged = True
                                    break
                            if bridged:
                                break
                        if bridged:
                            break
                    if bridged:
                        break
                if bridged:
                    break
            if bridged:
                progress = True
        if not progress:
            for comp in loose:
                for q in comp:
                    remove(q, "floating_tree_removed")
            break
    return fixes


# ------------------------------------------------------------------ NBT files
def sanitize_file(path: Path, check: bool = False) -> dict:
    import nbtlib
    from nbtlib import Compound, Int, List, String

    t = nbtlib.load(path)
    palette = [(str(e["Name"]), {str(k): str(v) for k, v in e.get("Properties", {}).items()}) for e in t["palette"]]
    grid, nbt_at, orig = {}, {}, {}
    for b in t["blocks"]:
        pos = tuple(int(v) for v in b["pos"])
        n, pr = palette[int(b["state"])]
        grid[pos] = _cell(n, pr)
        orig[pos] = n
        if "nbt" in b:
            nbt_at[pos] = b["nbt"]
    fixes = sanitize_vegetation(grid, tuple(int(v) for v in t["size"]))
    if fixes and not check:
        new_pal, index, blocks = [], {}, []
        for pos in sorted(grid):
            c = grid[pos]
            key = (c["Name"], tuple(sorted(c["Properties"].items())))
            if key not in index:
                index[key] = len(new_pal)
                e = Compound({"Name": String(c["Name"])})
                if c["Properties"]:
                    e["Properties"] = Compound({k: String(v) for k, v in c["Properties"].items()})
                new_pal.append(e)
            b = Compound({"pos": List[Int]([Int(v) for v in pos]), "state": Int(index[key])})
            if pos in nbt_at and c["Name"] == orig.get(pos):
                b["nbt"] = nbt_at[pos]
            blocks.append(b)
        t["palette"] = List[Compound](new_pal)
        t["blocks"] = List[Compound](blocks)
        t.save(path, gzipped=True)
    return fixes


def main(argv) -> int:
    check = "--check" in argv
    paths = [Path(a) for a in argv if not a.startswith("--")]
    files = []
    for p in paths:
        files += sorted(p.rglob("*.nbt")) if p.is_dir() else [p]
    bad = 0
    for f in files:
        fixes = sanitize_file(f, check)
        if fixes:
            bad += 1
            print(f"{f}: {fixes}")
    print(f"{'would fix' if check else 'fixed'} {bad} of {len(files)} templates")
    return 1 if check and bad else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
