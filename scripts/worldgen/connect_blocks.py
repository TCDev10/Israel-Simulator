#!/usr/bin/env python3
"""Compute fence / pane / wall connection properties for structure NBT grids.

Given a dense dict[(x,y,z)] -> {"Name": str, "Properties": dict[str,str]},
rewrite north/south/east/west/(up) on connectable blocks from neighbors.
Keeps waterlogged and unrelated properties (facing, open, …).
"""
from __future__ import annotations

import json
from pathlib import Path
from typing import Iterable

# Optional vanilla block report (NeoForge data gen) for definition types.
_BLOCKS_JSON = Path("/workspace/mcreports/out/reports/blocks.json")
_KNOWN: dict[str, dict] = {}
if _BLOCKS_JSON.exists():
    try:
        _KNOWN = json.loads(_BLOCKS_JSON.read_text())
    except Exception:
        _KNOWN = {}

DIRS = {
    "north": (0, 0, -1),
    "south": (0, 0, 1),
    "west": (-1, 0, 0),
    "east": (1, 0, 0),
}

# Definition types that present a sturdy full face for fence/pane/wall attachment.
_SOLID_TYPES = frozenset(
    {
        "minecraft:block",
        "minecraft:rotated_pillar",
        "minecraft:grass",
        "minecraft:nylium",
        "minecraft:snowy_dirt",
        "minecraft:glazed_terracotta",
        "minecraft:stained_glass",
        "minecraft:transparent",  # glass — panes attach; fences use sturdy-face which glass lacks in-game,
        # but treating glass as attachable for panes is handled via family; solid check excludes it below.
        "minecraft:drop_experience",
        "minecraft:ore",
        "minecraft:copper_block",
        "minecraft:weathering_copper",
        "minecraft:concrete_powder",
        "minecraft:mangrove_roots",  # not solid — omit
    }
)

# Safer solid allow-list: types that are full cubes for connection purposes.
_SOLID_DEF_TYPES = frozenset(
    {
        "minecraft:block",
        "minecraft:rotated_pillar",
        "minecraft:grass",
        "minecraft:glazed_terracotta",
        "minecraft:drop_experience",
        "minecraft:concrete_powder",
    }
)

_NON_SOLID_NAME_HINTS = (
    "_stairs",
    "_slab",
    "_fence",
    "_wall",
    "_pane",
    "_door",
    "_trapdoor",
    "_button",
    "_pressure_plate",
    "_carpet",
    "_sign",
    "_banner",
    "_candle",
    "_rail",
    "_leaves",
    "torch",
    "lantern",
    "flower",
    "sapling",
    "carpet",
    "air",
    "water",
    "lava",
    "chest",
    "barrel",
    "lectern",
    "composter",
    "bed",
    "potted_",
    "flower_pot",
    "jigsaw",
    "structure_block",
)


def _def_type(name: str) -> str | None:
    b = _KNOWN.get(name)
    if not b:
        return None
    return b.get("definition", {}).get("type")


def is_air(name: str) -> bool:
    return name in ("minecraft:air", "minecraft:cave_air", "minecraft:void_air")


def is_fence(name: str) -> bool:
    t = _def_type(name)
    if t == "minecraft:fence":
        return True
    return name.endswith("_fence") and not name.endswith("_fence_gate")


def is_fence_gate(name: str) -> bool:
    t = _def_type(name)
    if t == "minecraft:fence_gate":
        return True
    return name.endswith("_fence_gate")


def is_pane_or_bars(name: str) -> bool:
    t = _def_type(name)
    if t == "minecraft:iron_bars":
        return True
    return name.endswith("_pane") or name == "minecraft:iron_bars"


def is_wall(name: str) -> bool:
    t = _def_type(name)
    if t == "minecraft:wall":
        return True
    return name.endswith("_wall") and "banner" not in name and "sign" not in name


def is_full_solid(name: str, props: dict[str, str] | None = None) -> bool:
    """True if the block should attach fences/panes/walls like a sturdy full cube."""
    if is_air(name):
        return False
    props = props or {}
    t = _def_type(name)
    if t == "minecraft:slab":
        return props.get("type") == "double"
    if t in _SOLID_DEF_TYPES:
        return True
    if t is not None:
        # Known non-solid families from the report
        if t in (
            "minecraft:stair",
            "minecraft:fence",
            "minecraft:fence_gate",
            "minecraft:wall",
            "minecraft:iron_bars",
            "minecraft:door",
            "minecraft:trapdoor",
            "minecraft:button",
            "minecraft:pressure_plate",
            "minecraft:wool_carpet",
            "minecraft:lantern",
            "minecraft:chest",
            "minecraft:bed",
            "minecraft:flower",
            "minecraft:flower_pot",
            "minecraft:transparent",
            "minecraft:stained_glass",
            "minecraft:tinted_particle_leaves",
            "minecraft:leaves",
        ):
            return False
    # Fallback heuristics when the report is missing an id
    lower = name.split(":")[-1]
    if any(h in lower for h in _NON_SOLID_NAME_HINTS):
        return False
    if name.endswith("_slab"):
        return props.get("type") == "double"
    return True


def is_connectable(name: str) -> bool:
    return is_fence(name) or is_pane_or_bars(name) or is_wall(name) or is_fence_gate(name)


def _neighbor(
    grid: dict[tuple[int, int, int], dict],
    x: int,
    y: int,
    z: int,
    dx: int,
    dy: int,
    dz: int,
) -> tuple[str, dict[str, str]] | None:
    cell = grid.get((x + dx, y + dy, z + dz))
    if cell is None:
        return None
    return str(cell["Name"]), dict(cell.get("Properties") or {})


def fence_connects_to(self_name: str, other_name: str, other_props: dict[str, str]) -> bool:
    if is_air(other_name):
        return False
    if is_fence(other_name) or is_fence_gate(other_name):
        return True
    return is_full_solid(other_name, other_props)


def pane_connects_to(self_name: str, other_name: str, other_props: dict[str, str]) -> bool:
    if is_air(other_name):
        return False
    if is_pane_or_bars(other_name):
        return True
    # Full glass blocks attach to panes in vanilla IronBarsBlock via sturdy/exception rules;
    # treat transparent / stained glass as attachable for panes.
    t = _def_type(other_name)
    if t in ("minecraft:transparent", "minecraft:stained_glass"):
        return True
    return is_full_solid(other_name, other_props)


def wall_connects_to(self_name: str, other_name: str, other_props: dict[str, str]) -> bool:
    if is_air(other_name):
        return False
    if is_wall(other_name) or is_fence_gate(other_name):
        return True
    return is_full_solid(other_name, other_props)


def wall_side_height(
    grid: dict[tuple[int, int, int], dict],
    x: int,
    y: int,
    z: int,
    dx: int,
    dz: int,
) -> str:
    """none / low / tall — tall when the block above this wall or above the neighbor is solid/wall."""
    nb = _neighbor(grid, x, y, z, dx, 0, dz)
    if nb is None:
        return "none"
    other_name, other_props = nb
    if not wall_connects_to("", other_name, other_props):
        return "none"
    above_self = _neighbor(grid, x, y, z, 0, 1, 0)
    above_nb = _neighbor(grid, x, y, z, dx, 1, dz)

    def raises(cell: tuple[str, dict[str, str]] | None) -> bool:
        if cell is None:
            return False
        n, p = cell
        return is_wall(n) or is_full_solid(n, p)

    if raises(above_self) or raises(above_nb):
        return "tall"
    return "low"


def wall_up(north: str, south: str, east: str, west: str) -> str:
    """Center post: omit on a straight run with matching opposite sides (vanilla-ish)."""
    ns = north != "none" and south != "none" and east == "none" and west == "none"
    ew = east != "none" and west != "none" and north == "none" and south == "none"
    if ns and north == south:
        return "false"
    if ew and east == west:
        return "false"
    return "true"


def apply_connections(
    grid: dict[tuple[int, int, int], dict],
    size: tuple[int, int, int] | None = None,
) -> dict[tuple[int, int, int], dict]:
    """Mutate and return grid with corrected connection properties."""
    # Work on a snapshot of names so updates don't feed each other mid-pass
    # (connection depends on block identity, not on neighbor connection props).
    items = list(grid.items())
    for (x, y, z), cell in items:
        name = str(cell["Name"])
        props = dict(cell.get("Properties") or {})
        if not is_connectable(name):
            continue

        if is_fence(name):
            for dname, (dx, dy, dz) in DIRS.items():
                nb = _neighbor(grid, x, y, z, dx, dy, dz)
                props[dname] = (
                    "true"
                    if nb and fence_connects_to(name, nb[0], nb[1])
                    else "false"
                )
            props.setdefault("waterlogged", props.get("waterlogged", "false"))

        elif is_pane_or_bars(name):
            for dname, (dx, dy, dz) in DIRS.items():
                nb = _neighbor(grid, x, y, z, dx, dy, dz)
                props[dname] = (
                    "true"
                    if nb and pane_connects_to(name, nb[0], nb[1])
                    else "false"
                )
            props.setdefault("waterlogged", props.get("waterlogged", "false"))

        elif is_wall(name):
            sides = {
                dname: wall_side_height(grid, x, y, z, dx, dz)
                for dname, (dx, dy, dz) in DIRS.items()
            }
            props.update(sides)
            props["up"] = wall_up(sides["north"], sides["south"], sides["east"], sides["west"])
            props.setdefault("waterlogged", props.get("waterlogged", "false"))

        elif is_fence_gate(name):
            # in_wall when a wall sits on either side perpendicular to facing
            facing = props.get("facing", "south")
            if facing in ("north", "south"):
                left = _neighbor(grid, x, y, z, -1, 0, 0)
                right = _neighbor(grid, x, y, z, 1, 0, 0)
            else:
                left = _neighbor(grid, x, y, z, 0, 0, -1)
                right = _neighbor(grid, x, y, z, 0, 0, 1)
            in_wall = False
            for nb in (left, right):
                if nb and is_wall(nb[0]):
                    in_wall = True
                    break
            props["in_wall"] = "true" if in_wall else "false"

        cell["Properties"] = props
        grid[(x, y, z)] = cell
    return grid


def grid_from_palette_blocks(
    palette: Iterable[dict],
    blocks: Iterable[dict],
) -> dict[tuple[int, int, int], dict]:
    """Build a grid from structure-template style palette + blocks lists (plain dicts)."""
    names = []
    props_list = []
    for e in palette:
        names.append(str(e["Name"]))
        pr = e.get("Properties") or {}
        props_list.append({str(k): str(v) for k, v in pr.items()})
    grid: dict[tuple[int, int, int], dict] = {}
    for b in blocks:
        pos = b["pos"]
        x, y, z = int(pos[0]), int(pos[1]), int(pos[2])
        state = int(b["state"])
        grid[(x, y, z)] = {"Name": names[state], "Properties": dict(props_list[state])}
    return grid
