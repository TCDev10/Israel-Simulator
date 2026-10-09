#!/usr/bin/env python3
"""Generate the 3D Security Pistol item model (Blockbench-style cuboids) and its
32x32 texture atlas.

Outputs:
  models/item/pistol.json        3D model used in hand, on the ground, in frames, on the head
  textures/item/pistol_model.png 32x32 atlas (gunmetal slide, black frame, darker grip, steel details)
The flat inventory icon stays models/item/pistol_icon.json + textures/item/pistol.png
(selected for the gui/on_shelf display contexts by items/pistol.json).

Model axes: muzzle toward -X (west), top toward +Y, width along Z (centred on z=8).
"""
import json
import random
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src/main/resources/assets/israel_simulator"
TEX_OUT = ASSETS / "textures/item/pistol_model.png"
MODEL_OUT = ASSETS / "models/item/pistol.json"

# ---------------------------------------------------------------- texture atlas
# Zones in texels (32x32). UV units in the model are texels / 2.
ZONES = {
    "slide": (0, 0, 16, 16),
    "frame": (16, 0, 32, 16),
    "grip": (0, 16, 16, 32),
    "bore": (16, 16, 20, 20),
    "steel": (20, 16, 24, 20),
    "dot": (24, 16, 28, 20),
    "sight": (28, 16, 32, 20),
    "serration": (16, 24, 24, 32),
    "magbase": (24, 24, 32, 32),
    "trigger": (16, 20, 24, 24),
    "slide_top": (24, 20, 32, 24),
}


def shade(c, d):
    return tuple(max(0, min(255, v + d)) for v in c[:3]) + (255,)


def build_texture():
    rnd = random.Random(1948)
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 255))
    px = im.load()

    def fill(zone, fn):
        x0, y0, x1, y1 = ZONES[zone]
        for y in range(y0, y1):
            for x in range(x0, x1):
                px[x, y] = fn(x - x0, y - y0, x1 - x0, y1 - y0)

    gunmetal = (58, 63, 70)
    # slide: brushed gunmetal, horizontal grain, light top edge, dark bottom edge
    row_grain = [rnd.randint(-5, 5) for _ in range(16)]
    fill("slide", lambda x, y, w, h: shade(gunmetal, row_grain[y] + rnd.randint(-2, 2)
                                           + (14 if y == 0 else 0) + (-10 if y == h - 1 else 0)))
    # slide top: flat gunmetal with a darker ejection-port line
    fill("slide_top", lambda x, y, w, h: shade(gunmetal, 8 + rnd.randint(-3, 3) - (22 if y in (1, 2) and 2 <= x <= 5 else 0)))
    # frame: matte black polymer
    polymer = (30, 31, 34)
    fill("frame", lambda x, y, w, h: shade(polymer, rnd.randint(-3, 3) + (8 if y == 0 else 0)))
    # grip: slightly warmer/brown-black, stippled diamond texture
    grip = (43, 38, 34)

    def grip_px(x, y, w, h):
        d = 0
        if (x + y) % 3 == 0:
            d -= 9
        if (x - y) % 3 == 0:
            d += 6
        return shade(grip, d + rnd.randint(-2, 2))
    fill("grip", grip_px)
    steel = (92, 98, 106)
    fill("steel", lambda x, y, w, h: shade(steel, rnd.randint(-4, 4) + (10 if y == 0 else 0)))
    fill("bore", lambda x, y, w, h: (10, 10, 12, 255) if 1 <= x <= 2 and 1 <= y <= 2 else shade(steel, rnd.randint(-4, 4)))
    black = (20, 20, 22)
    fill("sight", lambda x, y, w, h: shade(black, rnd.randint(-2, 2)))
    fill("dot", lambda x, y, w, h: (236, 236, 228, 255) if 1 <= x <= 2 and 1 <= y <= 2 else shade(black, rnd.randint(-2, 2)))
    fill("serration", lambda x, y, w, h: shade(gunmetal, -22 if x % 2 == 0 else 6))
    fill("magbase", lambda x, y, w, h: shade((24, 24, 26), (10 if y == 0 or x == 0 else 0) + rnd.randint(-2, 2)))
    fill("trigger", lambda x, y, w, h: shade((36, 37, 40), rnd.randint(-3, 3) + (10 if x == 0 else 0)))
    return im


# ---------------------------------------------------------------- geometry
GRIP_ROT = {"angle": 22.5, "axis": "z", "origin": [11.5, 8, 8]}

# name, from, to, material (or per-face dict), rotation
ELEMENTS = [
    ("slide", [3, 9, 7], [13.5, 11.5, 9], {"*": "slide", "up": "slide_top", "west": "steel", "east": "slide"}, None),
    ("barrel", [2, 9.75, 7.5], [3, 10.75, 8.5], {"*": "steel", "west": "bore"}, None),
    ("serration_1", [11, 9.5, 6.8], [11.5, 11.5, 9.2], {"*": "serration"}, None),
    ("serration_2", [12, 9.5, 6.8], [12.5, 11.5, 9.2], {"*": "serration"}, None),
    ("serration_3", [13, 9.5, 6.8], [13.5, 11.5, 9.2], {"*": "serration"}, None),
    ("frame", [3.5, 8, 7.1], [13, 9, 8.9], {"*": "frame"}, None),
    ("rail", [3.5, 7.5, 7.25], [6.5, 8, 8.75], {"*": "frame"}, None),
    ("trigger_guard_bottom", [6.5, 6, 7.4], [10.5, 6.5, 8.6], {"*": "frame"}, None),
    ("trigger_guard_front", [6.5, 6.5, 7.4], [7, 8, 8.6], {"*": "frame"}, None),
    ("trigger", [8.5, 6.5, 7.75], [9, 8, 8.25], {"*": "trigger"},
     {"angle": -22.5, "axis": "z", "origin": [8.75, 8, 8]}),
    ("grip", [10, 1.5, 7], [13, 8, 9], {"*": "grip", "up": "frame"}, GRIP_ROT),
    ("beavertail", [12.5, 8, 7.2], [14, 9, 8.8], {"*": "frame"}, None),
    ("magazine_base", [9.75, 0.75, 6.85], [13.25, 1.5, 9.15], {"*": "magbase"}, GRIP_ROT),
    ("front_sight", [3.5, 11.5, 7.6], [4.25, 12.25, 8.4], {"*": "sight", "east": "dot"}, None),
    ("rear_sight_left", [12.25, 11.5, 7.25], [13, 12.25, 7.75], {"*": "sight", "east": "dot"}, None),
    ("rear_sight_right", [12.25, 11.5, 8.25], [13, 12.25, 8.75], {"*": "sight", "east": "dot"}, None),
]

FACE_DIMS = {  # face -> (axis index for u, axis index for v)
    "north": (0, 1), "south": (0, 1), "east": (2, 1), "west": (2, 1), "up": (0, 2), "down": (0, 2),
}


def face_uv(zone, f, t, face):
    x0, y0, x1, y1 = [c / 2 for c in ZONES[zone]]
    if zone in ("bore", "dot"):
        return [x0, y0, x1, y1]
    ua, va = FACE_DIMS[face]
    w = min(abs(t[ua] - f[ua]), x1 - x0)
    h = min(abs(t[va] - f[va]), y1 - y0)
    w, h = max(w, 0.25), max(h, 0.25)
    return [round(x0, 3), round(y0, 3), round(x0 + w, 3), round(y0 + h, 3)]


# Display transforms. Derived by simulating the vanilla transform chain
# (ItemInHandLayer: arm -> rotX(-90) -> rotY(180) -> hand offset; ItemInHandRenderer for first person)
# so that in third person, with the vanilla ITEM arm pose (-18 deg), the barrel points forward and the
# grip sits in the fist; in first person the pistol points at the crosshair from the lower right.
DISPLAY = {
    "thirdperson_righthand": {"rotation": [0, -90, -72], "translation": [0, 1.25, 3.5], "scale": [0.6, 0.6, 0.6]},
    "thirdperson_lefthand": {"rotation": [0, 90, 72], "translation": [0, 1.25, 3.5], "scale": [0.6, 0.6, 0.6]},
    "firstperson_righthand": {"rotation": [0, -85, 0], "translation": [-1.75, 5.25, 0], "scale": [0.5, 0.5, 0.5]},
    "firstperson_lefthand": {"rotation": [0, 95, 0], "translation": [-1.75, 5.25, 0], "scale": [0.5, 0.5, 0.5]},
    "gui": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [1.1, 1.1, 1.1]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
    "fixed": {"rotation": [0, 180, 0], "translation": [0, 1, 0], "scale": [1.1, 1.1, 1.1]},
    "head": {"rotation": [0, 90, 0], "translation": [0, 9, 0], "scale": [0.8, 0.8, 0.8]},
}


def build_model():
    elements = []
    for name, f, t, mats, rot in ELEMENTS:
        faces = {}
        for face in ("north", "east", "south", "west", "up", "down"):
            zone = mats.get(face, mats["*"])
            faces[face] = {"uv": face_uv(zone, f, t, face), "texture": "#gun"}
        el = {"name": name, "from": f, "to": t}
        if rot:
            el["rotation"] = rot
        el["faces"] = faces
        elements.append(el)
    return {
        "credit": "Israel Simulator - generated by scripts/textures/gen_pistol_model.py",
        "texture_size": [32, 32],
        "textures": {"gun": "israel_simulator:item/pistol_model", "particle": "israel_simulator:item/pistol_model"},
        "elements": elements,
        "display": DISPLAY,
    }


def main():
    TEX_OUT.parent.mkdir(parents=True, exist_ok=True)
    build_texture().save(TEX_OUT)
    MODEL_OUT.write_text(json.dumps(build_model(), indent=2) + "\n")
    print(f"wrote {TEX_OUT} and {MODEL_OUT} ({len(ELEMENTS)} elements)")


if __name__ == "__main__":
    main()
