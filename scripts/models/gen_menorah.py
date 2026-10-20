#!/usr/bin/env python3
"""Generates the Hanukkah menorah (hanukkiah) block model, its 8 lit variants,
the blockstate and the 16x16 texture.

Layout (block space, 16 units):
- base: stepped foot (y 0-3), central stem up to the raised shamash (y 3-12)
- 4 nested U-shaped arm pairs: crossbars at y 4.5 / 6 / 7.5 / 9, uprights up
  to y 10.5, so the 8 cups all sit at the same height (y 10.5-11.5)
- shamash cup raised in the middle (y 12-13)
- candles in every holder; flames only on lit candles.

candles=N lights the shamash plus N candles, added from the east (+X) side,
i.e. right to left when looking at the south face. MenorahBlock.FLAME_X must
match BRANCH_X below (particles).

Run from the repo root: python3 scripts/models/gen_menorah.py
"""
import json
import os

from PIL import Image

ASSETS = "src/main/resources/assets/israel_simulator"
TEX = "israel_simulator:block/menorah"

# x centres of the 8 branch holders, west -> east
BRANCH_X = [1.5, 3.0, 4.5, 6.0, 10.0, 11.5, 13.0, 14.5]
SHAMASH_X = 8.0
Z0, Z1 = 7.5, 8.5  # thin arm depth
CUP_Y, CANDLE_Y, CANDLE_TOP = 10.5, 11.5, 13.5
SHAMASH_CUP_Y, SHAMASH_CANDLE_Y, SHAMASH_CANDLE_TOP = 12.0, 13.0, 15.0
CROSSBAR_Y = {1: 9.0, 2: 7.5, 3: 6.0, 4: 4.5}  # pair k (1 = innermost)

# --- texture UV regions (pixels) ---------------------------------------------
UV_BAR_FRONT = 0        # rows 0 (light) / 1 (shade): horizontal bars, x 0-15
UV_BASE = (0, 2, 8, 8)  # 8x6 gold block with bevel shading
UV_ROD_FRONT, UV_ROD_SIDE = 8, 9  # vertical rod columns, rows 2-15
UV_CUP = (10, 2, 12, 4)  # rim row + body row, 2 px wide
UV_WAX = (12, 2, 13, 4)
UV_WICK = (13, 2, 14, 3)
UV_FLAME = (14, 2, 15, 4)
UV_FLAME_TOP = (15, 2, 16, 3)


def face(uv, cull=None):
    f = {"uv": [round(v, 4) for v in uv], "texture": "#menorah"}
    if cull:
        f["cullface"] = cull
    return f


def box(name, frm, to, faces, **extra):
    e = {"name": name, "from": frm, "to": to, "faces": faces}
    e.update(extra)
    return e


def gold_block(name, frm, to, cull_down=False):
    """Base pieces: every face samples the bevelled 8x6 gold block."""
    u0, v0, u1, v1 = UV_BASE
    w, h, d = to[0] - frm[0], to[1] - frm[1], to[2] - frm[2]
    side = lambda span: (u0, v0 + 1, u0 + min(span, 8), v0 + 1 + min(h, 4))
    top = (u0, v0, u0 + min(w, 8), v0 + min(d, 6))
    faces = {
        "up": face(top),
        "down": face(top, "down" if cull_down else None),
        "north": face(side(w)), "south": face(side(w)),
        "west": face(side(d)), "east": face(side(d)),
    }
    return box(name, frm, to, faces)


def rod(name, x0, x1, y0, y1, z0=Z0, z1=Z1, ends=True):
    """Thin vertical rod: light column on front/back, shaded column on sides."""
    h = y1 - y0
    v0 = 2 + (15.0 - 2 - h) / 2 if h < 13 else 2
    front = (UV_ROD_FRONT, v0, UV_ROD_FRONT + 1, v0 + h)
    side = (UV_ROD_SIDE, v0, UV_ROD_SIDE + 1, v0 + h)
    faces = {"north": face(front), "south": face(front), "west": face(side), "east": face(side)}
    if ends:
        faces["up"] = face((UV_ROD_FRONT, 2, UV_ROD_FRONT + 1, 3))
        faces["down"] = face((UV_ROD_SIDE, 2, UV_ROD_SIDE + 1, 3))
    return box(name, [x0, y0, z0], [x1, y1, z1], faces)


def crossbar(name, x0, x1, y):
    w = x1 - x0
    u0 = (16 - w) / 2
    return box(name, [x0, y, Z0], [x1, y + 1, Z1], {
        "up": face((u0, 0, u0 + w, 1)),
        "down": face((u0, 1, u0 + w, 2)),
        "north": face((u0, 0, u0 + w, 1)),
        "south": face((u0, 0, u0 + w, 1)),
        "west": face((UV_ROD_SIDE, 2, UV_ROD_SIDE + 1, 3)),
        "east": face((UV_ROD_SIDE, 2, UV_ROD_SIDE + 1, 3)),
    })


def cup(name, cx, y, half=0.625):
    u0, v0, u1, v1 = UV_CUP
    s = (u0, v0, u1, v1 - 1 + 1)
    return box(name, [cx - half, y, 8 - half], [cx + half, y + 1, 8 + half], {
        "up": face((u0, v0, u1, v0 + 1)),
        "down": face((u0, v1 - 1, u1, v1)),
        "north": face(s), "south": face(s), "west": face(s), "east": face(s),
    })


def candle(name, cx, y0, y1, half=0.375):
    u0, v0, u1, v1 = UV_WAX
    s = (u0, v0, u1, v1)
    return box(name, [cx - half, y0, 8 - half], [cx + half, y1, 8 + half], {
        "up": face((u0, v0, u1, v0 + 1)),
        "north": face(s), "south": face(s), "west": face(s), "east": face(s),
    })


def wick(name, cx, y0):
    return box(name, [cx - 0.125, y0, 7.875], [cx + 0.125, y0 + 0.5, 8.125], {
        "up": face(UV_WICK), "north": face(UV_WICK), "south": face(UV_WICK),
        "west": face(UV_WICK), "east": face(UV_WICK),
    })


def flame(name, cx, y0):
    s = face(UV_FLAME)
    return box(name, [cx - 0.25, y0, 7.75], [cx + 0.25, y0 + 1, 8.25], {
        "up": face(UV_FLAME_TOP), "north": s, "south": s, "west": s, "east": s,
    }, shade=False, light_emission=15)


def base_elements():
    els = [
        gold_block("base_foot", [4, 0, 6], [12, 1, 10], cull_down=True),
        gold_block("base_tier", [5, 1, 6.5], [11, 2, 9.5]),
        gold_block("base_knob", [6.5, 2, 7], [9.5, 3, 9]),
        rod("center_stem", 7.5, 8.5, 3, SHAMASH_CUP_Y, ends=False),
    ]
    for k in range(4, 0, -1):
        lx, rx = BRANCH_X[4 - k], BRANCH_X[3 + k]
        y = CROSSBAR_Y[k]
        names = {1: "inner", 2: "mid", 3: "outer", 4: "outermost"}[k]
        els.append(crossbar(f"{names}_crossbar", lx - 0.5, rx + 0.5, y))
        els.append(rod(f"{names}_left_branch", lx - 0.5, lx + 0.5, y + 1, CUP_Y, ends=False))
        els.append(rod(f"{names}_right_branch", rx - 0.5, rx + 0.5, y + 1, CUP_Y, ends=False))
    for i, x in enumerate(BRANCH_X):
        els.append(cup(f"branch_tips_cup_{i + 1}", x, CUP_Y))
    els.append(cup("branch_tips_shamash_cup", SHAMASH_X, SHAMASH_CUP_Y, half=0.75))
    return els


def candle_elements(lit):
    """lit = number of lit branch candles (0-8); the shamash is lit when lit > 0."""
    els = []
    lit_idx = set(range(8 - lit, 8))  # east side first
    for i, x in enumerate(BRANCH_X):
        els.append(candle(f"candle_{i + 1}", x, CANDLE_Y, CANDLE_TOP))
        if i in lit_idx:
            els.append(flame(f"flame_{i + 1}", x, CANDLE_TOP))
        else:
            els.append(wick(f"wick_{i + 1}", x, CANDLE_TOP))
    els.append(candle("candle_shamash", SHAMASH_X, SHAMASH_CANDLE_Y, SHAMASH_CANDLE_TOP))
    if lit > 0:
        els.append(flame("flame_shamash", SHAMASH_X, SHAMASH_CANDLE_TOP))
    else:
        els.append(wick("wick_shamash", SHAMASH_X, SHAMASH_CANDLE_TOP))
    return els


DISPLAY = {
    "gui": {"rotation": [15, -25, 0], "translation": [0, 0, 0], "scale": [0.85, 0.85, 0.85]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.45, 0.45, 0.45]},
    "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.8, 0.8, 0.8]},
    "head": {"rotation": [0, 0, 0], "translation": [0, 9, 0], "scale": [0.8, 0.8, 0.8]},
    "thirdperson_righthand": {"rotation": [0, 0, 0], "translation": [0, 2.5, 1.5], "scale": [0.45, 0.45, 0.45]},
    "thirdperson_lefthand": {"rotation": [0, 0, 0], "translation": [0, 2.5, 1.5], "scale": [0.45, 0.45, 0.45]},
    "firstperson_righthand": {"rotation": [0, -20, 0], "translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
    "firstperson_lefthand": {"rotation": [0, 20, 0], "translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
}


def model(lit):
    m = {
        "parent": "minecraft:block/block",
        "ambientocclusion": False,
        "textures": {"particle": TEX, "menorah": TEX},
        "elements": base_elements() + candle_elements(lit),
    }
    if lit == 0:
        m["display"] = DISPLAY
    return m


def texture():
    P = {
        "hi": (255, 236, 150), "light": (246, 207, 84), "mid": (219, 170, 46),
        "dark": (163, 117, 24), "deep": (110, 76, 14),
        "wax": (247, 241, 226), "wax2": (226, 216, 192), "wick": (46, 34, 24),
        "fy": (255, 236, 128), "fo": (255, 150, 30), "fw": (255, 250, 210),
    }
    im = Image.new("RGBA", (16, 16), P["mid"] + (255,))
    px = im.load()
    for x in range(16):  # horizontal bars
        px[x, 0] = P["light"] + (255,)
        px[x, 1] = P["mid"] + (255,)
    for x in range(0, 8):  # bevelled base block (rows 2-7)
        for y in range(2, 8):
            c = "mid"
            if y == 2 or x == 0:
                c = "hi" if y == 2 else "light"
            elif y == 7 or x == 7:
                c = "dark"
            elif (x + y) % 5 == 0:
                c = "light"
            px[x, y] = P[c] + (255,)
    for y in range(2, 16):  # rods
        px[8, y] = P["light"] + (255,)
        px[9, y] = P["dark"] + (255,)
    px[8, 2] = P["hi"] + (255,)
    px[10, 2] = px[11, 2] = P["hi"] + (255,)       # cup rim
    px[10, 3] = px[11, 3] = P["dark"] + (255,)     # cup body
    px[12, 2] = P["wax"] + (255,)
    px[12, 3] = P["wax2"] + (255,)
    px[13, 2] = P["wick"] + (255,)
    px[14, 2] = P["fy"] + (255,)
    px[14, 3] = P["fo"] + (255,)
    px[15, 2] = P["fw"] + (255,)
    for x in range(10, 16):  # unused area: darker gold
        for y in range(4, 16):
            px[x, y] = P["deep" if (x + y) % 3 == 0 else "dark"] + (255,)
    return im


def main():
    mdir = os.path.join(ASSETS, "models/block")
    for lit in range(9):
        name = "menorah" if lit == 0 else f"menorah_lit_{lit}"
        with open(os.path.join(mdir, name + ".json"), "w") as f:
            json.dump(model(lit), f, indent=2)
            f.write("\n")
    variants = {f"candles={n}": {"model": "israel_simulator:block/" + ("menorah" if n == 0 else f"menorah_lit_{n}")}
                for n in range(9)}
    with open(os.path.join(ASSETS, "blockstates/menorah.json"), "w") as f:
        json.dump({"variants": variants}, f, indent=2)
        f.write("\n")
    texture().save(os.path.join(ASSETS, "textures/block/menorah.png"))


if __name__ == "__main__":
    main()
