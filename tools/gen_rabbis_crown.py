#!/usr/bin/env python3
"""Generates the Blockbench-style 3D Rabbi's Crown: item model JSON + 64x64 texture.
Run from the repo root: python3 tools/gen_rabbis_crown.py"""
import json, math, random
from PIL import Image, ImageDraw

A = "src/main/resources/assets/israel_simulator/"
random.seed(42)
S = 64
img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
px = img.load()

def gold(t):  # t in 0..1 dark->bright
    stops = [(0, (92, 56, 10)), (0.35, (170, 112, 20)), (0.65, (232, 176, 48)), (0.85, (255, 222, 110)), (1, (255, 248, 200))]
    for (a, ca), (b, cb) in zip(stops, stops[1:]):
        if t <= b:
            k = (t - a) / (b - a)
            return tuple(int(ca[i] + (cb[i] - ca[i]) * k) for i in range(3)) + (255,)
    return stops[-1][1] + (255,)

# 1) Band with filigree: px (0,0)-(64,16)
for y in range(16):
    for x in range(64):
        t = 0.45 + 0.18 * math.sin(y / 15 * math.pi) - (0.12 if y in (0, 15) else 0)
        px[x, y] = gold(max(0, t + random.uniform(-0.03, 0.03)))
for x in range(64):  # beaded edges
    px[x, 1] = gold(0.82 if x % 2 == 0 else 0.4)
    px[x, 14] = gold(0.82 if x % 2 == 0 else 0.4)
for x in range(64):  # scroll vine
    for ph in (0, math.pi):
        y = int(round(7.5 + 3.6 * math.sin(x / 16 * 2 * math.pi + ph)))
        px[x, y] = gold(0.95)
        if 0 < y < 15:
            px[x, y + 1] = gold(0.25)
for cx in range(4, 64, 8):  # tiny rosettes in vine gaps
    px[cx, 7] = gold(1.0); px[cx, 8] = gold(0.8)
    for dx, dy in ((-1, 7), (1, 7), (0, 6), (0, 9)):
        px[cx + dx, dy] = gold(0.7)

# 2) Plain shaded gold: (0,16)-(32,32)
for y in range(16, 32):
    for x in range(32):
        t = 0.55 + 0.3 * (1 - (y - 16) / 15) - 0.1 * abs(x - 16) / 16
        if x in (0, 31) or y in (16, 31):
            t -= 0.25
        px[x, y] = gold(min(1, max(0, t + random.uniform(-0.04, 0.04))))
px[6, 19] = px[7, 19] = px[6, 20] = gold(1.0)  # specular

# 3) Velvet: (32,16)-(64,32)
for y in range(16, 32):
    for x in range(32, 64):
        v = 0.75 + random.uniform(-0.12, 0.12) + (0.1 if (x + y) % 4 == 0 else 0)
        px[x, y] = (int(22 * v), int(30 * v), int(92 * v), 255)
for x in range(32, 64, 4):  # gold stitching
    px[x, 23] = gold(0.75)

# 4) Gems with facets: 8x8 each at y 32
def gem(ox, base, light):
    for y in range(8):
        for x in range(8):
            d = abs(x - 3.5) + abs(y - 3.5)
            k = 1.0 - d / 8
            c = tuple(int(base[i] * (0.45 + 0.75 * k)) for i in range(3))
            if x + y < 6: c = tuple(min(255, int(c[i] * 1.25)) for i in range(3))
            if x == 0 or y == 0 or x == 7 or y == 7: c = gold(0.5)[:3]
            px[ox + x, 32 + y] = c + (255,)
    px[ox + 2, 34] = px[ox + 3, 34] = px[ox + 2, 35] = light + (255,)
gem(0, (40, 90, 235), (210, 230, 255))     # sapphire
gem(8, (225, 230, 240), (255, 255, 255))   # diamond
gem(16, (220, 30, 45), (255, 200, 200))    # ruby

# 5) Point gold (vertical gradient): (24,32)-(40,64)
for y in range(32, 64):
    for x in range(24, 40):
        t = 0.95 - (y - 32) / 31 * 0.55 - 0.15 * abs(x - 31.5) / 8
        if x in (24, 39): t -= 0.2
        px[x, y] = gold(min(1, max(0, t)))

# 6) Star of David plate: (40,32)-(56,48), transparent around
Q = 8  # supersample, then threshold the alpha for crisp pixel art
st = Image.new("RGBA", (16 * Q, 16 * Q), (0, 0, 0, 0))
d = ImageDraw.Draw(st)
c, r = (8 * Q, 8 * Q), 7.6 * Q
tri = lambda a0, rr: [(c[0] + rr * math.sin(math.radians(a0 + i * 120)), c[1] - rr * math.cos(math.radians(a0 + i * 120))) for i in range(3)]
for a0 in (0, 180):
    d.polygon(tri(a0, r), fill=gold(0.35))
for a0 in (0, 180):
    d.polygon(tri(a0, r - 1.6 * Q), fill=gold(0.92))
for a0 in (0, 180):
    d.polygon(tri(a0, r - 3.6 * Q), fill=gold(0.6))
d.regular_polygon((8 * Q, 8 * Q, 2.3 * Q), 6, rotation=30, fill=(40, 90, 235, 255))
st = st.resize((16, 16), Image.NEAREST)
st.putpixel((7, 7), (210, 230, 255, 255))
img.paste(st, (40, 32))
# star edge gold strip for plate sides: (56,32)-(64,48)
for y in range(32, 48):
    for x in range(56, 64):
        px[x, y] = gold(0.6)

img.save(A + "textures/item/rabbis_crown.png")

# ---------- model ----------
T = "#0"
UV = {
    "band": [0, 0, 16, 4], "gold": [0, 4, 8, 8], "velvet": [8, 4, 16, 8],
    "blue": [0, 8, 2, 10], "white": [2, 8, 4, 10], "red": [4, 8, 6, 10],
    "point": [6, 8, 10, 16], "star": [10, 8, 14, 12], "edge": [14, 8, 16, 12],
}
els = []
def box(name, f, t, tex, rot=None, faces=None, shade=True):
    fc = {}
    for side in ("north", "east", "south", "west", "up", "down"):
        u = (faces or {}).get(side, tex)
        if u is None: continue
        fc[side] = {"uv": UV[u], "texture": T}
    e = {"name": name, "from": [round(v, 3) for v in f], "to": [round(v, 3) for v in t], "faces": fc}
    if rot: e["rotation"] = rot
    els.append(e)

LO, HI = 1.2, 14.8   # band outer extents (head spans 1.6..14.4 with the vanilla head transform)
W = 0.8              # band thickness
Y0, Y1 = 13.6, 17.0
# band walls with filigree outside, plain gold elsewhere
side_band = {"north": "band", "south": "band", "east": "band", "west": "band", "up": "gold", "down": "gold"}
box("band_north", [LO, Y0, LO], [HI, Y1, LO + W], "gold", faces=side_band)
box("band_south", [LO, Y0, HI - W], [HI, Y1, HI], "gold", faces=side_band)
box("band_west", [LO, Y0, LO + W], [LO + W, Y1, HI - W], "gold", faces=side_band)
box("band_east", [HI - W, Y0, LO + W], [HI, Y1, HI - W], "gold", faces=side_band)
# lower and upper rims (slightly proud of the band)
for nm, y0, y1 in (("rim_low", Y0 - 0.4, Y0 + 0.4), ("rim_high", Y1 - 0.3, Y1 + 0.4)):
    o = 0.25
    box(nm + "_north", [LO - o, y0, LO - o], [HI + o, y1, LO + W], "gold")
    box(nm + "_south", [LO - o, y0, HI - W], [HI + o, y1, HI + o], "gold")
    box(nm + "_west", [LO - o, y0, LO + W], [LO + W, y1, HI - W], "gold")
    box(nm + "_east", [HI - W, y0, LO + W], [HI + o, y1, HI - W], "gold")
# velvet cap
box("cap_base", [LO + W, Y0 + 0.4, LO + W], [HI - W, 18.6, HI - W], "velvet")
box("cap_mid", [3.6, 18.6, 3.6], [12.4, 19.8, 12.4], "velvet")
box("cap_top", [5.6, 19.8, 5.6], [10.4, 20.6, 10.4], "velvet")
box("cap_orb", [7.3, 20.6, 7.3], [8.7, 22.0, 8.7], "gold")
box("cap_orb_gem", [7.6, 22.0, 7.6], [8.4, 22.8, 8.4], "white")
# 7 pointed spikes (the front-centre slot carries the Star of David)
m = (LO + HI) / 2
spots = [(LO, LO, "blue"), (HI, LO, "red"), (HI, m, "white"), (HI, HI, "blue"), (m, HI, "red"), (LO, HI, "white"), (LO, m, "red")]
for i, (x, z, g) in enumerate(spots):
    cx = min(max(x, LO + 0.8), HI - 0.8); cz = min(max(z, LO + 0.8), HI - 0.8)
    box(f"point_{i}_base", [cx - 0.8, Y1, cz - 0.8], [cx + 0.8, Y1 + 2.4, cz + 0.8], "point")
    box(f"point_{i}_mid", [cx - 0.55, Y1 + 2.4, cz - 0.55], [cx + 0.55, Y1 + 4.0, cz + 0.55], "point")
    box(f"point_{i}_tip", [cx - 0.3, Y1 + 4.0, cz - 0.3], [cx + 0.3, Y1 + 5.4, cz + 0.3], "point",
        rot={"angle": 45, "axis": "y", "origin": [cx, Y1 + 4.5, cz]})
    box(f"point_{i}_gem", [cx - 0.5, Y1 + 5.4, cz - 0.5], [cx + 0.5, Y1 + 6.4, cz + 0.5], g,
        rot={"angle": 45, "axis": "y", "origin": [cx, Y1 + 5.9, cz]})
# band gems, set into the band surface below each point and between them
def band_gem(name, x, z, g, axis):
    if axis == "z":
        box(name, [x - 0.55, 14.6, z - 0.35], [x + 0.55, 16.0, z + 0.35], g)
    else:
        box(name, [x - 0.35, 14.6, z - 0.55], [x + 0.35, 16.0, z + 0.55], g)
cols = ["blue", "white", "red"]
k = 0
for t in (4.2, 11.8):
    band_gem(f"gem_n_{k}", t, LO - 0.2, cols[k % 3], "z"); k += 1
    band_gem(f"gem_s_{k}", t, HI + 0.2, cols[k % 3], "z"); k += 1
    band_gem(f"gem_w_{k}", LO - 0.2, t, cols[k % 3], "x"); k += 1
    band_gem(f"gem_e_{k}", HI + 0.2, t, cols[k % 3], "x"); k += 1
# Star of David ornament on the front
box("star_of_david", [5.6, 15.4, LO - 0.6], [10.4, 20.2, LO - 0.3], "edge",
    faces={"north": "star", "south": "star", "east": None, "west": None, "up": None, "down": None})
box("star_mount", [7.4, Y1 - 0.2, LO - 0.35], [8.6, 16.0 + 0.6, LO], "gold")

model = {
    "credit": "Israel Simulator - Rabbi's Crown, Blockbench-style 3D model (tools/gen_rabbis_crown.py)",
    "texture_size": [64, 64],
    "textures": {"0": "israel_simulator:item/rabbis_crown", "particle": "israel_simulator:item/rabbis_crown"},
    "elements": els,
    "gui_light": "side",
    "display": {
        "gui": {"rotation": [25, -35, 0], "translation": [0, -8.6, 0], "scale": [0.8, 0.8, 0.8]},
        "ground": {"rotation": [0, 0, 0], "translation": [0, -2.5, 0], "scale": [0.4, 0.4, 0.4]},
        "fixed": {"rotation": [0, 180, 0], "translation": [0, -7.5, 0], "scale": [0.8, 0.8, 0.8]},
        "head": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [1, 1, 1]},
        "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, -2.5, 1], "scale": [0.4, 0.4, 0.4]},
        "thirdperson_lefthand": {"rotation": [75, 45, 0], "translation": [0, -2.5, 1], "scale": [0.4, 0.4, 0.4]},
        "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, -3.5, 0], "scale": [0.45, 0.45, 0.45]},
        "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, -3.5, 0], "scale": [0.45, 0.45, 0.45]},
        "on_shelf": {"rotation": [0, 180, 0], "translation": [0, -6.5, 0], "scale": [0.75, 0.75, 0.75]},
    },
}
with open(A + "models/item/rabbis_crown.json", "w") as fh:
    json.dump(model, fh, indent=2); fh.write("\n")
print("elements:", len(els))
