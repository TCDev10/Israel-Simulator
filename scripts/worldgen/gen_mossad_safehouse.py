#!/usr/bin/env python3
"""Small Mossad safehouse (fictional) for the Tel Aviv and Jerusalem city building pools.

A plain two-storey house from jigsaw_kit.house (so it uses the cities' footing and entrance
jigsaw conventions) with a Mossad Handler NPC inside, a radio desk, a map wall and a chest
with israel_simulator:chests/mossad_safehouse. Adds one weight-1 entry to each buildings pool.

Run with the nbtlib venv: /workspace/nbtvenv/bin/python scripts/worldgen/gen_mossad_safehouse.py
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import nbtlib  # noqa: E402
from nbtlib.tag import Byte, Compound, Double, Int, List, String  # noqa: E402
from jigsaw_kit import B, CHEST, DATA, NS, Kit, house  # noqa: E402

LOOT = f"{NS}:chests/mossad_safehouse"
HANDLER = (5, 1, 5)  # floor-relative interior position (x, y above the floor, z)


def furnish(s):
    s.set(6, 1, 5, B("lectern", facing="west", has_book="false", powered="false"))
    s.set(6, 1, 6, CHEST("west", LOOT))
    s.set(2, 1, 6, B("note_block"))                           # radio set
    s.set(2, 2, 6, B("lightning_rod", facing="up", powered="false"))  # antenna
    s.set(4, 2, 7, B("cartography_table"))                    # map wall
    s.set(3, 2, 6, B("black_wall_banner", facing="north"))
    for x in (2, 6):
        s.set(x, 2, 1, B("black_stained_glass_pane"))


def place_handler(path: Path) -> None:
    f = nbtlib.load(path)
    root = f
    footing = 4
    x, y, z = HANDLER[0], HANDLER[1] + footing, HANDLER[2]
    root["entities"] = List[Compound]([Compound({
        "pos": List[Double]([Double(x + 0.5), Double(y), Double(z + 0.5)]),
        "blockPos": List[Int]([Int(x), Int(y), Int(z)]),
        "nbt": Compound({
            "id": String(f"{NS}:mossad_handler"),
            "PersistenceRequired": Byte(1),
            "Invulnerable": Byte(1),
        }),
    })])
    f.save(path, gzipped=True)


def add_to_pool(prefix: str) -> None:
    p = DATA / f"worldgen/template_pool/{prefix}/buildings.json"
    data = json.loads(p.read_text())
    loc = f"{NS}:{prefix}/mossad_safehouse"
    data["elements"] = [e for e in data["elements"] if e["element"].get("location") != loc]
    data["elements"].append({"weight": 1, "element": {"element_type": "minecraft:single_pool_element",
                                                      "location": loc, "processors": "minecraft:empty",
                                                      "projection": "rigid"}})
    p.write_text(json.dumps(data, indent=2) + "\n")


def main() -> None:
    for structure, prefix, mat in (("tel_aviv_city", "tel_aviv", "white_concrete"),
                                   ("jerusalem_city", "jerusalem", "smooth_sandstone")):
        kit = Kit(structure, prefix)
        house(kit, "mossad_safehouse", 9, 9, 2, mat, door="dark_oak", shutter="dark_oak",
              beds=("gray",), loot=LOOT, pergola=False, vines=False, extra=furnish)
        place_handler(kit.struct / "mossad_safehouse.nbt")
        add_to_pool(prefix)
        print(f"wrote {prefix}/mossad_safehouse")


if __name__ == "__main__":
    main()
