#!/usr/bin/env python3
"""Generate the gaza_strip jigsaw (big ruined district), DataVersion 4903.

The old gaza_strip was one 32x32 template (walled compound: ground-level foundations,
dry well, four watchtowers). It is now a jigsaw district roughly 10x the area:

  * start      gaza/square (rigid, 23x23): dry well with broken pillars (framework marker
               minecraft:cobbled_deepslate), rubble, two stub towers
  * streets    terrain_matching rubble roads (several lengths and seeded variants),
               crossroads, T, corners, checkpoints, rubble terminators
  * buildings  rigid, each in many seeded variants: ground-level foundations with rubble
               and rebar (no intact houses), watchtowers up to 15 blocks with ladders,
               iron-bar slits and battlements, breached perimeter walls, rubble yards,
               ruined walled compounds
  * processors per-placement random material swaps + height-based crumbling, so the
               same piece never looks the same twice

Materials stay in the old palette: cobblestone, stone, cobbled deepslate, gravel,
cracked stone bricks (+ their walls/slabs), iron bars, ladders. No sand, no sandstone.

Run:  /workspace/nbtvenv/bin/python scripts/worldgen/gen_gaza_strip.py
"""
from __future__ import annotations

import json
import random
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from jigsaw_kit import (  # noqa: E402
    AIR, B, DATA, LADDER, NS, ROOT, SLAB, STAIR, WALL, Kit, Rigid, Structure, end_j, entrance_j,
    iron_bars, jigsaw,
)

PREFIX = "gaza"
RUIN_PROC = f"{NS}:gaza_ruins_weathering"
STREET_PROC = f"{NS}:gaza_street_rubble"
SIZE, MAX_DISTANCE = 12, 80


class GRigid(Rigid):
    """Rigid piece with a buried stone/deepslate footing (never sandstone)."""

    FOOTING = 4

    def save(self, path: Path) -> None:
        self._ready = False
        F = self.FOOTING
        r = random.Random(str(path))
        for x in range(self.sx):
            for z in range(self.sz):
                if str(self.palette[self.blocks[(x, F, z)]]["Name"]) not in ("minecraft:air",
                                                                              "minecraft:structure_void"):
                    for y in range(F):
                        if str(self.palette[self.blocks[(x, y, z)]]["Name"]) == "minecraft:air":
                            self.set(x, y, z, B("cobbled_deepslate") if r.random() < 0.6 else B("stone"))
        Structure.save(self, path)


kit = Kit("gaza_strip", PREFIX)


# ---------------------------------------------------------------- palettes
def rubble_block(r: random.Random):
    v = r.random()
    return (B("cobbled_deepslate") if v < 0.32 else B("cobblestone") if v < 0.64 else
            B("stone") if v < 0.80 else B("cracked_stone_bricks"))


def ground(r: random.Random):
    v = r.random()
    return (B("gravel") if v < 0.28 else B("cobblestone") if v < 0.52 else
            B("cobbled_deepslate") if v < 0.76 else B("stone") if v < 0.90 else B("cracked_stone_bricks"))


def debris(r: random.Random):
    v = r.random()
    return (SLAB("cobblestone") if v < 0.3 else SLAB("cobbled_deepslate") if v < 0.55 else
            SLAB("stone") if v < 0.7 else STAIR(r.choice("north south east west".split()), "cobblestone")
            if v < 0.82 else rubble_block(r))


DWALL = lambda: WALL("cobbled_deepslate")
CWALL = lambda: WALL("cobblestone")


def paver(seed: int):
    cache: dict[tuple[int, int], object] = {}

    def f(x: int, z: int):
        if (x, z) not in cache:
            cache[(x, z)] = ground(random.Random(seed * 100003 + x * 917 + z * 131))
        return cache[(x, z)]
    return f


def free(s, x, y, z) -> bool:
    if isinstance(s, Rigid):
        return s.free(x, y, z)
    return (0 <= x < s.sx and 0 <= y < s.sy and 0 <= z < s.sz
            and str(s.palette[s.blocks[(x, y, z)]]["Name"]) == "minecraft:air")


def scatter(s, r: random.Random, cells, n: int, y: int = 1) -> None:
    cells = [c for c in cells if free(s, c[0], y, c[1])]
    r.shuffle(cells)
    for x, z in cells[:n]:
        v = r.random()
        if v < 0.55:
            s.set(x, y, z, debris(r))
        elif v < 0.8:
            s.set(x, y, z, rubble_block(r))
            if r.random() < 0.35 and free(s, x, y + 1, z):
                s.set(x, y + 1, z, debris(r))
        elif v < 0.92:
            s.set(x, y, z, iron_bars())
        else:
            s.set(x, y, z, CWALL())


def front(s, W: int, r: random.Random) -> int:
    for x in range(W):
        s.set(x, 0, 0, ground(r))
    cx = W // 2
    s.set(cx, 1, 0, entrance_j())
    return cx


def broken_pillar(s, x, z, h: int, r: random.Random, y0: int = 1) -> None:
    s.set(x, y0, z, B("cracked_stone_bricks") if r.random() < 0.6 else B("cobbled_deepslate"))
    for y in range(y0 + 1, y0 + h):
        s.set(x, y, z, DWALL() if r.random() < 0.7 else CWALL())


# ---------------------------------------------------------------- start
def square() -> None:
    r = random.Random(7001)
    N = 23
    s = GRigid(N, 11, N)
    c = N // 2
    for x in range(N):
        for z in range(N):
            s.set(x, 0, z, B("cobbled_deepslate") if r.random() < 0.45 else ground(r))
    # dry well: 3x3 shaft, 4 deep, cobbled deepslate rim, gravel bottom
    for x in range(c - 2, c + 3):
        for z in range(c - 2, c + 3):
            ring = x in (c - 2, c + 2) or z in (c - 2, c + 2)
            if ring:
                s.set(x, 1, z, B("cobbled_deepslate"))
                for y in range(-3, 0):
                    s.set(x, y, z, B("cobbled_deepslate"))
            else:
                for y in range(-3, 1):
                    s.set(x, y, z, AIR())
                s.set(x, -4, z, B("gravel"))
    for x, z in ((c - 2, c - 2), (c + 2, c - 2), (c - 2, c + 2), (c + 2, c + 2)):
        broken_pillar(s, x, z, r.randint(2, 5), r, y0=2)
    s.set(c + 1, -3, c, iron_bars()); s.set(c - 1, -3, c - 1, SLAB("cobblestone"))
    # broken colonnade around the square
    for i in (3, 7, 15, 19):
        for x, z in ((i, 3), (i, N - 4), (3, i), (N - 4, i)):
            if r.random() < 0.8:
                broken_pillar(s, x, z, r.randint(1, 6), r)
    # two stub watchtowers in opposite corners (the old compound's towers)
    for (tx, tz) in ((0, 0), (N - 3, N - 3)):
        h = r.randint(5, 8)
        for y in range(1, h + 1):
            for dx in range(3):
                for dz in range(3):
                    if (dx, dz) != (1, 1):
                        s.set(tx + dx, y, tz + dz, rubble_block(r))
        s.set(tx + 1, h + 1, tz, DWALL()); s.set(tx + 1, h + 1, tz + 2, DWALL())
        s.set(tx, h + 1, tz + 1, DWALL()); s.set(tx + 2, h + 1, tz + 1, DWALL())
        s.set(tx + 1, 4, tz, iron_bars()); s.set(tx, 4, tz + 1, iron_bars())
    scatter(s, r, [(x, z) for x in range(1, N - 1) for z in range(1, N - 1)], 40)
    # final_state air (not structure_void): unused jigsaws must not leave terrain blocks on the square
    sj = lambda o: jigsaw(o, "minecraft:street", "minecraft:street", f"{NS}:{PREFIX}/streets", "minecraft:air")
    bj = lambda o: jigsaw(o, "minecraft:building_entrance", "minecraft:building_entrance",
                          f"{NS}:{PREFIX}/buildings", "minecraft:air")
    s.set(c, 1, 0, sj("north_up"))
    s.set(c, 1, N - 1, sj("south_up"))
    s.set(0, 1, c, sj("west_up"))
    s.set(N - 1, 1, c, sj("east_up"))
    for o, x, z in (("north_up", 6, 0), ("north_up", 17, 0), ("south_up", 6, N - 1), ("south_up", 17, N - 1),
                    ("west_up", 0, 6), ("west_up", 0, 17), ("east_up", N - 1, 6), ("east_up", N - 1, 17)):
        s.set(x, 1, z, bj(o))
    s.save(kit.struct / "square.nbt")


# ---------------------------------------------------------------- streets
def roads() -> list[tuple[str, int, str]]:
    out = []
    W = 5
    # (length, building entrances, side-street branch x or None, weight)
    for i, (L, ents, branch, w) in enumerate(((19, (3, 9, 15), None, 5), (19, (4, 14), 9, 5),
                                              (13, (3, 9), None, 5), (9, (4,), None, 4),
                                              (25, (4, 20), 12, 4), (15, (3, 11), 7, 4),
                                              (21, (3, 10, 17), None, 4), (11, (2, 8), 5, 3))):
        r = random.Random(7100 + i)
        s = kit.straight(f"road_{i}", L, W, paver(7100 + i), entrances=ents)
        if branch is not None:                     # alley branching off one side
            s.set(branch, 1, 0, kit.street_j("north_up"))
        scatter(s, r, [(x, z) for x in range(1, L - 1) for z in (0, W - 1)], r.randint(2, L // 3))
        if r.random() < 0.5:                       # burnt-out barricade remains at the side
            x = r.randrange(2, L - 2)
            s.set(x, 1, 0 if r.random() < 0.5 else W - 1, rubble_block(r))
        name = f"road_{i}"
        s.save(kit.struct / f"{name}.nbt")
        out.append((name, w, "terrain_matching"))
    for i, (arms, w, nm) in enumerate((({"n", "s", "w", "e"}, 5, "crossroad"), ({"w", "e", "s"}, 6, "t_junction"),
                                       ({"n", "e"}, 3, "corner"), ({"n", "s", "w", "e"}, 3, "crossroad_b"))):
        r = random.Random(7200 + i)
        n = 5 if i < 3 else 7
        s = kit.junction(nm, n, arms, paver(7200 + i))
        if n == 7:
            for x, z in ((0, 0), (n - 1, 0), (0, n - 1), (n - 1, n - 1)):
                broken_pillar(s, x, z, r.randint(1, 3), r)
        s.save(kit.struct / f"{nm}.nbt")
        out.append((nm, w, "terrain_matching"))
    # checkpoints: road crossing a breached wall with iron-bar gates
    for i in range(2):
        r = random.Random(7300 + i)
        L = 9
        s = kit.straight(f"checkpoint_{i}", L, 5, paver(7300 + i), sy=6, entrances=(1, 7))
        x = 4
        for z in (0, 4):
            for y in range(1, r.randint(3, 5)):
                s.set(x, y, z, rubble_block(r))
            s.set(x - 1, 1, z, DWALL()); s.set(x + 1, 1, z, DWALL())
        for z in (1, 3):
            s.set(x, 1, z, iron_bars())
            if r.random() < 0.6:
                s.set(x, 2, z, iron_bars())
        s.set(x, 1, 2, AIR())
        s.set(x, 0, 2, B("cobbled_deepslate"))
        s.save(kit.struct / f"checkpoint_{i}.nbt")
        out.append((f"checkpoint_{i}", 2, "terrain_matching"))
    return out


def terminators() -> list[tuple[str, int, str]]:
    out = []
    for i in range(3):
        r = random.Random(7400 + i)
        s = Structure(3, 4, 5)
        p = paver(7400 + i)
        for x in range(3):
            for z in range(5):
                s.set(x, 0, z, p(x, z))
        for z in range(5):
            if r.random() < 0.8:
                s.set(2, 1, z, rubble_block(r))
                if r.random() < 0.4:
                    s.set(2, 2, z, debris(r))
            if r.random() < 0.4:
                s.set(1, 1, z, debris(r))
        s.set(0, 1, 2, end_j("west_up"))
        s.save(kit.struct / f"terminator_{i}.nbt")
        out.append((f"terminator_{i}", 1, "terrain_matching"))
    return out


# ---------------------------------------------------------------- buildings
def foundation(name: str, seed: int) -> None:
    """Ground-level foundations of a destroyed house: low broken walls, rubble, rebar."""
    r = random.Random(seed)
    W, D = r.choice([7, 9, 9, 11, 11, 13, 15]), r.randint(7, 13)
    s = GRigid(W, 6, D)
    cx = front(s, W, r)
    for x in range(W):
        for z in range(1, D):
            s.set(x, 0, z, ground(r))
    hmax = r.choice([1, 2, 2, 3])
    rooms = []
    if W >= 9 and r.random() < 0.7:
        rooms.append(("x", r.randint(3, W - 4)))
    if D >= 9 and r.random() < 0.6:
        rooms.append(("z", r.randint(4, D - 4)))
    gaps = {(cx, 1)} | ({(cx + 1, 1)} if r.random() < 0.4 else set())
    for x in range(W):
        for z in range(1, D):
            edge = x in (0, W - 1) or z in (1, D - 1)
            inner = any((a == "x" and x == k) or (a == "z" and z == k) for a, k in rooms)
            if not (edge or inner) or (x, z) in gaps:
                continue
            if inner and r.random() < 0.25:
                continue                                # doorways / breaches in partitions
            top = r.choice(range(0, hmax + 1)) if r.random() < 0.75 else hmax
            if edge and r.random() < 0.12:
                top = 0
            for y in range(1, top + 1):
                s.set(x, y, z, rubble_block(r))
            if top >= 1 and r.random() < 0.08:          # rebar sticking out of the wall
                s.set(x, top + 1, z, iron_bars())
    if r.random() < 0.55:                               # one corner survived a little higher
        ox, oz = r.choice([(0, 1), (W - 1, 1), (0, D - 1), (W - 1, D - 1)])
        for y in range(1, r.randint(3, 4) + 1):
            s.set(ox, y, oz, B("cracked_stone_bricks") if r.random() < 0.5 else rubble_block(r))
        dx = 1 if ox == 0 else -1
        for y in range(1, r.randint(2, 3) + 1):
            s.set(ox + dx, y, oz, rubble_block(r))
    inside = [(x, z) for x in range(1, W - 1) for z in range(2, D - 1)]
    scatter(s, r, inside, max(4, len(inside) // 5))
    for _ in range(r.randint(1, 3)):                    # rebar grids lying in the rubble
        x, z = r.choice(inside)
        for d in range(r.randint(1, 3)):
            if s.free(x + d if x + d < W - 1 else x, 1, z):
                s.set(x + d if x + d < W - 1 else x, 1, z, iron_bars())
    s.save(kit.struct / f"{name}.nbt")


def tower(name: str, seed: int, height: int, base: int) -> None:
    r = random.Random(seed)
    W = base + 2
    s = GRigid(W, height + 3, base + 1)
    cx = front(s, W, r)
    x0, x1, z0, z1 = 1, W - 2, 1, base
    for x in range(W):
        for z in range(1, base + 1):
            s.set(x, 0, z, ground(r))
    for y in range(1, height + 1):
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                if x in (x0, x1) or z in (z0, z1):
                    if (x, z) == (cx, z0) and y <= 2:
                        continue
                    s.set(x, y, z, rubble_block(r))
    lad_z = z1 - 1
    for y in range(1, height + 1):
        s.set(cx, y, lad_z, LADDER("north"))
    for x in range(x0 + 1, x1):                        # top platform
        for z in range(z0 + 1, z1):
            if (x, z) != (cx, lad_z):
                s.set(x, height, z, SLAB("cobbled_deepslate", "top"))
    if height >= 10:                                   # mid landing
        mid = height // 2
        for x in range(x0 + 1, x1):
            for z in range(z0 + 1, z1):
                if (x, z) != (cx, lad_z) and r.random() < 0.75:
                    s.set(x, mid, z, SLAB("cobblestone", "top"))
    for y in range(3, height, 3):                      # iron-bar arrow slits on every face
        mid_x, mid_z = (x0 + x1) // 2, (z0 + z1) // 2
        for (x, z) in ((mid_x, z0), (mid_x, z1), (x0, mid_z), (x1, mid_z)):
            if (x, z) != (cx, lad_z):
                s.set(x, y, z, iron_bars())
    for x in range(x0, x1 + 1):                        # battlements, some knocked off
        for z in range(z0, z1 + 1):
            if (x in (x0, x1) or z in (z0, z1)) and (x + z) % 2 == 0 and r.random() < 0.8:
                s.set(x, height + 1, z, DWALL())
    for _ in range(r.randint(2, 6)):                   # shell damage near the top
        x, z = r.choice([(x, z) for x in range(x0, x1 + 1) for z in (z0, z1)]
                        + [(x, z) for x in (x0, x1) for z in range(z0, z1 + 1)])
        y = r.randint(height - 3, height)
        if (x, z) != (cx, lad_z) and s.get(x, y, z) != "minecraft:ladder":
            s.set(x, y, z, AIR())
    scatter(s, r, [(x, 0) for x in range(W) if x != cx] + [(0, z) for z in range(1, base + 1)]
            + [(W - 1, z) for z in range(1, base + 1)], r.randint(2, 5))
    s.save(kit.struct / f"{name}.nbt")


def wall_segment(name: str, seed: int) -> None:
    """Stretch of the old perimeter wall with battlements, slits and shell breaches."""
    r = random.Random(seed)
    L, h = r.choice([11, 13, 15, 17]), r.randint(4, 7)
    s = GRigid(L, h + 3, 5)
    front(s, L, r)
    for x in range(L):
        for z in range(1, 5):
            s.set(x, 0, z, ground(r))
    breaches = set()
    for _ in range(r.randint(0, 2)):
        bx, bw = r.randrange(1, L - 3), r.randint(1, 3)
        breaches |= set(range(bx, bx + bw))
    for x in range(L):
        top = h if x not in breaches else r.randint(0, 2)
        for y in range(1, top + 1):
            for z in (2, 3):
                s.set(x, y, z, rubble_block(r))
        if top == h and x % 2 == 0 and r.random() < 0.85:
            s.set(x, h + 1, 3, DWALL())
        if top == h and x % 4 == 2:
            s.set(x, h - 1, 2, iron_bars()); s.set(x, h - 1, 3, AIR())
        if x in breaches:
            for z in (1, 4):
                if r.random() < 0.6:
                    s.set(x, 1, z, debris(r))
    s.set(L // 2, 1, 1, AIR())
    scatter(s, r, [(x, 1) for x in range(L) if x != L // 2] + [(x, 4) for x in range(L)], r.randint(3, 7))
    s.save(kit.struct / f"{name}.nbt")


def rubble_yard(name: str, seed: int) -> None:
    """Flattened block: gravel and rubble heaps, broken pillars, twisted rebar."""
    r = random.Random(seed)
    W, D = r.choice([9, 11, 13]), r.choice([9, 11, 13])
    s = GRigid(W, 7, D)
    front(s, W, r)
    for x in range(W):
        for z in range(1, D):
            s.set(x, 0, z, ground(r))
    for _ in range(r.randint(2, 4)):                   # heaps
        hx, hz, rad = r.randrange(2, W - 2), r.randrange(3, D - 2), r.randint(1, 3)
        for x in range(hx - rad, hx + rad + 1):
            for z in range(hz - rad, hz + rad + 1):
                if not (0 <= x < W and 1 <= z < D):
                    continue
                hh = rad + 1 - max(abs(x - hx), abs(z - hz)) - (1 if r.random() < 0.3 else 0)
                for y in range(1, hh + 1):
                    s.set(x, y, z, rubble_block(r) if y > 1 or r.random() < 0.7 else B("gravel"))
                if hh >= 0 and s.free(x, hh + 1, z) and r.random() < 0.3:
                    s.set(x, hh + 1, z, debris(r))
    for _ in range(r.randint(1, 4)):
        x, z = r.randrange(1, W - 1), r.randrange(2, D - 1)
        if s.free(x, 1, z):
            broken_pillar(s, x, z, r.randint(1, 5), r)
    scatter(s, r, [(x, z) for x in range(W) for z in range(1, D)], (W * D) // 8)
    s.save(kit.struct / f"{name}.nbt")


def compound(name: str, seed: int) -> None:
    """Ruined walled compound like the old gaza_strip, smaller: breached wall, towers, dry well."""
    r = random.Random(seed)
    N = r.choice([17, 19, 21])
    s = GRigid(N, 18, N + 1)
    cx = front(s, N, r)
    for x in range(N):
        for z in range(1, N + 1):
            s.set(x, 0, z, ground(r))
    wh = r.randint(3, 5)
    towers = r.sample([(0, 1), (N - 3, 1), (0, N - 2), (N - 3, N - 2)], r.randint(2, 4))
    for x in range(N):
        for z in range(1, N + 1):
            if not (x in (0, N - 1) or z in (1, N)):
                continue
            if z == 1 and abs(x - cx) <= 1:
                continue
            top = wh if r.random() < 0.7 else r.randint(0, wh)
            for y in range(1, top + 1):
                s.set(x, y, z, rubble_block(r))
            if top == wh and (x + z) % 2 == 0 and r.random() < 0.7:
                s.set(x, wh + 1, z, DWALL())
    for tx, tz in towers:
        th = r.randint(9, 15)
        for y in range(1, th + 1):
            for dx in range(3):
                for dz in range(3):
                    if (dx, dz) != (1, 1):
                        s.set(tx + dx, y, tz + dz, rubble_block(r))
            s.set(tx + 1, y, tz + 1, LADDER("north"))
        lz = tz + 2 if tz == 1 else tz
        for y in (1, 2):
            s.set(tx + 1, y, lz, AIR())                 # doorway on the courtyard side
        if tz != 1:
            s.set(tx + 1, 1, tz + 1, AIR()); s.set(tx + 1, 2, tz + 1, AIR())
            for y in range(3, th + 1):
                s.set(tx + 1, y, tz + 1, LADDER("north"))
        for y in range(4, th, 3):
            s.set(tx + 1 if tx else tx, y, tz + 1 if tx else tz + 1, iron_bars())
        for dx, dz in ((0, 0), (2, 0), (0, 2), (2, 2)):
            if r.random() < 0.85:
                s.set(tx + dx, th + 1, tz + dz, DWALL())
    c = N // 2
    for x in range(c - 1, c + 2):                     # dry well
        for z in range(c, c + 3):
            if (x, z) == (c, c + 1):
                for y in range(-3, 1):
                    s.set(x, y, z, AIR())
                s.set(x, -4, z, B("gravel"))
            else:
                s.set(x, 1, z, B("cobbled_deepslate"))
                for y in range(-3, 1):
                    s.set(x, y, z, B("cobbled_deepslate"))
    for x, z in ((c - 1, c), (c + 1, c + 2)):
        broken_pillar(s, x, z, r.randint(2, 4), r, y0=2)
    for _ in range(r.randint(1, 3)):                  # inner foundations
        fx, fz, fw, fd = r.randrange(3, N - 8), r.randrange(4, N - 7), r.randint(4, 6), r.randint(4, 5)
        for x in range(fx, fx + fw):
            for z in range(fz, fz + fd):
                if (x in (fx, fx + fw - 1) or z in (fz, fz + fd - 1)) and s.free(x, 1, z) \
                        and not (c - 2 <= x <= c + 2 and c - 1 <= z <= c + 3) and r.random() < 0.75:
                    s.set(x, 1, z, rubble_block(r))
    scatter(s, r, [(x, z) for x in range(1, N - 1) for z in range(2, N)], N * 2)
    s.save(kit.struct / f"{name}.nbt")


def buildings() -> list[tuple[str, int, str]]:
    out = []
    for i in range(12):
        foundation(f"foundation_{i}", 8000 + i); out.append((f"foundation_{i}", 3, "rigid"))
    for i, (h, b) in enumerate(((15, 5), (13, 5), (11, 5), (9, 5), (15, 7), (12, 7))):
        tower(f"watchtower_{i}", 8100 + i, h, b); out.append((f"watchtower_{i}", 1, "rigid"))
    for i in range(4):
        wall_segment(f"wall_{i}", 8200 + i); out.append((f"wall_{i}", 2, "rigid"))
    for i in range(4):
        rubble_yard(f"rubble_yard_{i}", 8300 + i); out.append((f"rubble_yard_{i}", 2, "rigid"))
    for i in range(3):
        compound(f"compound_{i}", 8400 + i); out.append((f"compound_{i}", 2, "rigid"))
    return out


# ---------------------------------------------------------------- json
def rule(block, out, prob=None, ydist=None):
    r = {"input_predicate": ({"predicate_type": "minecraft:random_block_match", "block": block, "probability": prob}
                             if prob is not None else {"predicate_type": "minecraft:block_match", "block": block}),
         "location_predicate": {"predicate_type": "minecraft:always_true"},
         "output_state": {"Name": out}}
    if ydist:
        lo, hi, cmax = ydist
        r["position_predicate"] = {"predicate_type": "minecraft:axis_aligned_linear_pos", "axis": "y",
                                   "min_dist": lo, "max_dist": hi, "min_chance": 0.0, "max_chance": cmax}
    return r


def processors() -> None:
    d = DATA / "worldgen/processor_list"
    F = GRigid.FOOTING
    ruins = [
        # crumble: the higher above the floor, the more likely a block is gone
        rule("minecraft:cobbled_deepslate_wall", "minecraft:air", ydist=(F + 4, F + 16, 0.45)),
        rule("minecraft:cobblestone_wall", "minecraft:air", ydist=(F + 2, F + 10, 0.5)),
        rule("minecraft:cracked_stone_bricks", "minecraft:air", ydist=(F + 4, F + 16, 0.35)),
        rule("minecraft:cobblestone", "minecraft:air", ydist=(F + 6, F + 18, 0.3)),
        # material shuffle
        rule("minecraft:cobblestone", "minecraft:cobbled_deepslate", 0.25),
        rule("minecraft:cobbled_deepslate", "minecraft:cobblestone", 0.2),
        rule("minecraft:stone", "minecraft:cobblestone", 0.3),
        rule("minecraft:cobbled_deepslate", "minecraft:cracked_stone_bricks", 0.06),
        rule("minecraft:gravel", "minecraft:cobblestone", 0.2),
    ]
    streets = [
        rule("minecraft:cobblestone", "minecraft:gravel", 0.2),
        rule("minecraft:cobblestone", "minecraft:cobbled_deepslate", 0.2),
        rule("minecraft:cobbled_deepslate", "minecraft:cobblestone", 0.2),
        rule("minecraft:stone", "minecraft:gravel", 0.25),
        rule("minecraft:gravel", "minecraft:cobbled_deepslate", 0.15),
    ]
    for n, rules in (("gaza_ruins_weathering", ruins), ("gaza_street_rubble", streets)):
        p = d / f"{n}.json"
        p.write_text(json.dumps({"processors": [{"processor_type": "minecraft:rule", "rules": rules}]},
                                indent=2) + "\n")
        print(f"wrote {p.relative_to(ROOT)}")


def pool(path: Path, items, fallback: str, proc: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    data = {"fallback": fallback, "elements": [
        {"weight": w, "element": {"element_type": "minecraft:single_pool_element", "location": kit.loc(p),
                                  "processors": proc, "projection": proj}} for p, w, proj in items]}
    path.write_text(json.dumps(data, indent=2) + "\n")
    print(f"wrote {path.relative_to(ROOT)}")


def main() -> None:
    old = DATA / "structure/gaza_strip.nbt"
    if old.exists():
        old.unlink()
    square()
    st, te, bu = roads(), terminators(), buildings()
    processors()
    pool(DATA / "worldgen/template_pool/gaza_strip.json", [("square", 1, "rigid")], "minecraft:empty", RUIN_PROC)
    pool(kit.pool / "streets.json", st, kit.loc("terminators"), STREET_PROC)
    pool(kit.pool / "terminators.json", te, "minecraft:empty", STREET_PROC)
    pool(kit.pool / "buildings.json", bu, "minecraft:empty", RUIN_PROC)
    data = {
        "type": "minecraft:jigsaw",
        "biomes": ["israel_simulator:mediterranean_coast"],
        "step": "surface_structures",
        "spawn_overrides": {},
        # no beard: the coast's sand/sandstone must not be pulled up under the ruins;
        # rigid pieces carry their own stone footing instead
        "terrain_adaptation": "none",
        "start_pool": f"{NS}:gaza_strip",
        "size": SIZE,
        "start_height": {"absolute": -GRigid.FOOTING},
        "project_start_to_heightmap": "WORLD_SURFACE_WG",
        "max_distance_from_center": MAX_DISTANCE,
        "use_expansion_hack": False,
    }
    p = DATA / "worldgen/structure/gaza_strip.json"
    p.write_text(json.dumps(data, indent=2) + "\n")
    print(f"wrote {p.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
