#!/usr/bin/env python3
"""Inject Israel-Simulator biome surface rules into vanilla 26.2 noise settings.

Copies the EXACT vanilla overworld / amplified / large_biomes noise_settings JSON
from the Minecraft jar (or a provided path) and prepends biome-specific surface
rules inside the ``above_preliminary_surface`` branch so bedrock / deepslate rules
stay intact. Non-Israeli biomes keep vanilla behavior.

Regenerate after Minecraft version bumps:
  python3 scripts/worldgen/gen_israel_surface_rules.py \\
      --jar build/moddev/artifacts/minecraft-patched-26.2.0.88.jar
"""

from __future__ import annotations

import argparse
import json
import zipfile
from copy import deepcopy
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT_DIR = ROOT / "src/main/resources/data/minecraft/worldgen/noise_settings"

NOISE_SETTINGS = ("overworld.json", "amplified.json", "large_biomes.json")

# Biomes that keep vanilla grass/dirt (no injected rule).
VANILLA_SURFACE_BIOMES = ("israeli_agriculture",)

# Israeli biomes that get custom surfaces (must match worldgen/biome/*.json).
# Mapping: biome path -> (surface_top, under_ceiling, optional noise patches on top)
# noise patches: list of (min, max, block) using minecraft:surface noise.
SURFACE_SPECS: dict[str, dict] = {
    "jerusalem": {
        "top": "israel_simulator:jerusalem_stone",
        "under": "minecraft:sandstone",
        "patches": [
            # Coarse dirt patches on the hills around the city
            (-0.95, -0.55, "minecraft:coarse_dirt"),
            (0.55, 0.95, "minecraft:coarse_dirt"),
        ],
    },
    "judean_desert": {
        "top": "minecraft:red_sand",
        "under": "minecraft:red_sandstone",
        "patches": [],
    },
    "dead_sea": {
        "top": "minecraft:sand",
        "under": "minecraft:sandstone",
        "patches": [
            # Salt crust patches near the Dead Sea shore
            (-0.35, 0.05, "israel_simulator:salt_block"),
            (0.45, 0.75, "israel_simulator:salt_block"),
        ],
    },
    "mediterranean_coast": {
        "top": "minecraft:sand",
        "under": "minecraft:sandstone",
        "patches": [
            # Grass inland patches mixed into coastal sand
            (-0.2, 0.35, "minecraft:grass_block"),
        ],
    },
    "tropical_island": {
        # warm sandy sea floor around the island structure (the island itself is the structure)
        "top": "minecraft:sand",
        "under": "minecraft:sandstone",
        "patches": [(0.5, 0.9, "minecraft:gravel")],
    },
    "urban_area": {
        "top": "minecraft:grass_block",
        "under": "minecraft:dirt",
        "patches": [
            (-0.9, -0.55, "minecraft:gravel"),
            (0.35, 0.65, "minecraft:stone"),
            (0.7, 0.95, "israel_simulator:paved_road"),
        ],
    },
}


def block_state(name: str) -> dict:
    if name == "minecraft:grass_block":
        return {"Name": name, "Properties": {"snowy": "false"}}
    return {"Name": name}


def block_rule(name: str) -> dict:
    return {"type": "minecraft:block", "result_state": block_state(name)}


def biome_is(biome_id: str) -> dict:
    return {"type": "minecraft:biome", "biome_is": biome_id}


def stone_depth_floor(*, add_surface_depth: bool = False, secondary: int = 0) -> dict:
    return {
        "type": "minecraft:stone_depth",
        "add_surface_depth": add_surface_depth,
        "offset": 0,
        "secondary_depth_range": secondary,
        "surface_type": "floor",
    }


def stone_depth_ceiling() -> dict:
    return {
        "type": "minecraft:stone_depth",
        "add_surface_depth": False,
        "offset": 0,
        "secondary_depth_range": 0,
        "surface_type": "ceiling",
    }


def noise_threshold(min_t: float, max_t: float) -> dict:
    return {
        "type": "minecraft:noise_threshold",
        "noise": "minecraft:surface",
        "min_threshold": min_t,
        "max_threshold": max_t,
    }


def condition(if_true: dict, then_run: dict) -> dict:
    return {"type": "minecraft:condition", "if_true": if_true, "then_run": then_run}


def sequence(rules: list) -> dict:
    return {"type": "minecraft:sequence", "sequence": rules}


def desert_style_surface(top: str, under: str, patches: list) -> dict:
    """Ceiling -> under; optional noise patches; else top. Mirrors vanilla desert."""
    rules: list = [
        condition(stone_depth_ceiling(), block_rule(under)),
    ]
    for lo, hi, block in patches:
        patch_block = block
        # grass_block needs snowy property
        rules.append(condition(noise_threshold(lo, hi), block_rule(patch_block)))
    rules.append(block_rule(top))
    return sequence(rules)


def deep_under_band(under: str) -> dict:
    """Thicker sandstone/dirt band under the surface (vanilla desert secondary_depth_range)."""
    return condition(
        stone_depth_floor(add_surface_depth=True, secondary=30),
        block_rule(under),
    )


def build_israeli_floor_rule() -> dict:
    biome_rules = []
    for biome, spec in SURFACE_SPECS.items():
        biome_id = f"israel_simulator:{biome}"
        biome_rules.append(
            condition(
                biome_is(biome_id),
                desert_style_surface(spec["top"], spec["under"], spec["patches"]),
            )
        )
    return condition(stone_depth_floor(add_surface_depth=False), sequence(biome_rules))


def build_israeli_under_rule() -> dict:
    biome_rules = []
    for biome, spec in SURFACE_SPECS.items():
        biome_id = f"israel_simulator:{biome}"
        biome_rules.append(condition(biome_is(biome_id), deep_under_band(spec["under"])))
    return condition(stone_depth_floor(add_surface_depth=True), sequence(biome_rules))


def find_above_preliminary(surface_rule: dict) -> dict:
    """Return the condition node whose if_true is above_preliminary_surface."""
    if surface_rule.get("type") != "minecraft:sequence":
        raise ValueError("Expected top-level surface_rule sequence")
    for node in surface_rule["sequence"]:
        ift = node.get("if_true") or {}
        if ift.get("type") == "minecraft:above_preliminary_surface":
            return node
    raise ValueError("above_preliminary_surface branch not found")


def inject_israel_rules(noise_settings: dict) -> dict:
    data = deepcopy(noise_settings)
    above = find_above_preliminary(data["surface_rule"])
    then = above.get("then_run")
    if not then or then.get("type") != "minecraft:sequence":
        raise ValueError("above_preliminary_surface.then_run must be a sequence")
    # Prepend: floor surfaces first, then under-band (matches vanilla ordering intent)
    then["sequence"] = [
        build_israeli_floor_rule(),
        build_israeli_under_rule(),
        *then["sequence"],
    ]
    return data


def load_vanilla_from_jar(jar: Path, name: str) -> dict:
    path = f"data/minecraft/worldgen/noise_settings/{name}"
    with zipfile.ZipFile(jar) as zf:
        with zf.open(path) as fh:
            return json.load(fh)


def load_vanilla_from_dir(directory: Path, name: str) -> dict:
    return json.loads((directory / name).read_text(encoding="utf-8"))


def write_pretty(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    # Match vanilla compact-ish but stable formatting for diffs/tests
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument(
        "--jar",
        type=Path,
        default=ROOT / "build/moddev/artifacts/minecraft-patched-26.2.0.88.jar",
        help="Minecraft jar containing vanilla noise_settings",
    )
    ap.add_argument(
        "--vanilla-dir",
        type=Path,
        default=None,
        help="Optional directory of vanilla noise_settings JSON (overrides --jar)",
    )
    ap.add_argument(
        "--out-dir",
        type=Path,
        default=OUT_DIR,
        help="Output directory under data/minecraft/worldgen/noise_settings",
    )
    args = ap.parse_args()

    for name in NOISE_SETTINGS:
        if args.vanilla_dir:
            vanilla = load_vanilla_from_dir(args.vanilla_dir, name)
        else:
            if not args.jar.is_file():
                raise SystemExit(f"Jar not found: {args.jar}")
            vanilla = load_vanilla_from_jar(args.jar, name)
        injected = inject_israel_rules(vanilla)
        out = args.out_dir / name
        write_pretty(out, injected)
        print(f"wrote {out.relative_to(ROOT)} ({out.stat().st_size} bytes)")

    print("Israeli surface biomes:", ", ".join(SURFACE_SPECS))
    print("Vanilla-kept biomes:", ", ".join(VANILLA_SURFACE_BIOMES))


if __name__ == "__main__":
    main()
