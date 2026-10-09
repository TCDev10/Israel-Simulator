#!/usr/bin/env python3
"""Generate the 3D cuboid item models, 32x32 texture atlases and flat 32x32 icons for the
mod's firearms (assault rifle, SMG, sniper rifle), the frag grenade and the ammo items.

Same conventions as gen_pistol_model.py: muzzle toward -X, top toward +Y, width along Z
(centred on z=8). Every weapon keeps its pistol grip where the Security Pistol's grip is, so the
pistol's tuned hand transforms (and the Bibi Guard / Mossad Agent aiming correction) still put the
grip in the fist; the display transforms only change the scale and compensate the translation.

Outputs per weapon <id>:
  models/item/<id>.json            3D model (hand, ground, frame, head)
  models/item/<id>_icon.json       flat icon (gui, on_shelf)
  items/<id>.json                  select on display_context
  textures/item/<id>_model.png     32x32 atlas
  textures/item/<id>.png           32x32 icon
Ammo: models/item/<ammo>.json (item/generated) + textures/item/<ammo>.png + items/<ammo>.json
"""
import json
import math
import random
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src/main/resources/assets/israel_simulator"
NS = "israel_simulator"

ZONES = {
    "body": (0, 0, 16, 16),
    "polymer": (16, 0, 32, 16),
    "grip": (0, 16, 16, 32),
    "bore": (16, 16, 20, 20),
    "steel": (20, 16, 24, 20),
    "lens": (24, 16, 28, 20),
    "sight": (28, 16, 32, 20),
    "rail": (16, 20, 24, 24),
    "mag": (24, 20, 32, 24),
    "accent": (16, 24, 24, 32),
    "rubber": (24, 24, 32, 32),
}


def shade(c, d):
    return tuple(max(0, min(255, v + d)) for v in c[:3]) + (255,)


def build_texture(pal, seed):
    rnd = random.Random(seed)
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 255))
    px = im.load()

    def fill(zone, fn):
        x0, y0, x1, y1 = ZONES[zone]
        for y in range(y0, y1):
            for x in range(x0, x1):
                px[x, y] = fn(x - x0, y - y0, x1 - x0, y1 - y0)

    grain = [rnd.randint(-5, 5) for _ in range(16)]
    if pal.get("wood"):
        # wood stock: long horizontal grain lines with darker streaks
        streak = [rnd.randint(-14, 8) for _ in range(16)]
        fill("body", lambda x, y, w, h: shade(pal["body"], streak[y] + rnd.randint(-3, 3) + (8 if (x + 3 * y) % 11 == 0 else 0)))
    else:
        fill("body", lambda x, y, w, h: shade(pal["body"], grain[y] + rnd.randint(-2, 2) + (12 if y == 0 else 0) + (-10 if y == h - 1 else 0)))
    fill("polymer", lambda x, y, w, h: shade(pal["polymer"], rnd.randint(-3, 3) + (8 if y == 0 else 0)))

    def grip_px(x, y, w, h):
        d = (-9 if (x + y) % 3 == 0 else 0) + (6 if (x - y) % 3 == 0 else 0)
        return shade(pal["grip"], d + rnd.randint(-2, 2))
    fill("grip", grip_px)
    steel = pal["steel"]
    fill("steel", lambda x, y, w, h: shade(steel, rnd.randint(-4, 4) + (10 if y == 0 else 0)))
    fill("bore", lambda x, y, w, h: (8, 8, 10, 255) if 1 <= x <= 2 and 1 <= y <= 2 else shade(steel, rnd.randint(-4, 4)))
    fill("lens", lambda x, y, w, h: (40, 90, 120, 255) if 1 <= x <= 2 and 1 <= y <= 2 else (20, 40, 60, 255) if 0 < x < 3 and 0 < y < 3 else shade((20, 20, 22), 0))
    fill("sight", lambda x, y, w, h: shade((22, 22, 24), rnd.randint(-2, 2)))
    fill("rail", lambda x, y, w, h: shade(pal["polymer"], -14 if x % 2 == 0 else 10))
    fill("mag", lambda x, y, w, h: shade(pal["mag"], (8 if x == 0 or y == 0 else 0) + rnd.randint(-3, 3) + (-8 if y % 2 else 0)))
    fill("accent", lambda x, y, w, h: shade(pal["accent"], rnd.randint(-4, 4) + (10 if y == 0 else 0)))
    fill("rubber", lambda x, y, w, h: shade((26, 26, 28), (-6 if y % 2 else 4) + rnd.randint(-2, 2)))
    return im


FACE_DIMS = {"north": (0, 1), "south": (0, 1), "east": (2, 1), "west": (2, 1), "up": (0, 2), "down": (0, 2)}


def face_uv(zone, f, t, face):
    x0, y0, x1, y1 = [c / 2 for c in ZONES[zone]]
    if zone in ("bore", "lens"):
        return [x0, y0, x1, y1]
    ua, va = FACE_DIMS[face]
    w = max(min(abs(t[ua] - f[ua]), x1 - x0), 0.25)
    h = max(min(abs(t[va] - f[va]), y1 - y0), 0.25)
    return [round(x0, 3), round(y0, 3), round(x0 + w, 3), round(y0 + h, 3)]


GRIP_ROT = {"angle": 22.5, "axis": "z", "origin": [11.5, 8, 8]}
# The Security Pistol's grip and trigger group, shared by every firearm (see module doc).
GRIP_GROUP = [
    ("trigger_guard_bottom", [6.5, 6, 7.4], [10.5, 6.5, 8.6], {"*": "polymer"}, None),
    ("trigger_guard_front", [6.5, 6.5, 7.4], [7, 8, 8.6], {"*": "polymer"}, None),
    ("trigger", [8.5, 6.5, 7.75], [9, 8, 8.25], {"*": "steel"}, {"angle": -22.5, "axis": "z", "origin": [8.75, 8, 8]}),
]

WEAPONS = {
    "assault_rifle": {
        "palette": {"body": (58, 60, 50), "polymer": (30, 31, 33), "grip": (36, 37, 34), "steel": (88, 92, 98),
                    "mag": (40, 41, 44), "accent": (72, 76, 62)},
        "elements": [
            ("flash_hider", [-14, 9.5, 7.25], [-12, 11.5, 8.75], {"*": "steel", "west": "bore"}, None),
            ("barrel", [-12, 10, 7.5], [-4, 11, 8.5], {"*": "steel"}, None),
            ("gas_block", [-6, 9.5, 7.4], [-5, 11.5, 8.6], {"*": "sight"}, None),
            ("handguard", [-4, 8.5, 6.8], [6, 12, 9.2], {"*": "body", "west": "polymer"}, None),
            ("receiver", [6, 7.5, 6.6], [25.5, 12.5, 9.4], {"*": "body"}, None),
            ("trigger_guard_long", [1, 5.5, 7.4], [6.5, 6, 8.6], {"*": "polymer"}, None),
            ("guard_front", [1, 6, 7.4], [1.5, 8.5, 8.6], {"*": "polymer"}, None),
            ("grip", [10, 1.5, 7], [13, 8, 9], {"*": "grip", "up": "polymer"}, GRIP_ROT),
            ("magazine", [15.5, 0.5, 7.1], [18.5, 7.5, 8.9], {"*": "mag"}, {"angle": -22.5, "axis": "z", "origin": [17, 7.5, 8]}),
            ("mag_well", [15, 6.5, 6.9], [19, 7.5, 9.1], {"*": "polymer"}, None),
            ("ejection_port", [18, 10, 9.4], [21.5, 11.5, 9.6], {"*": "steel"}, None),
            ("cheek_rest", [18, 12.5, 7], [25, 13, 9], {"*": "polymer"}, None),
            ("butt_pad", [25.5, 7, 6.5], [27, 13, 9.5], {"*": "rubber"}, None),
            ("top_rail", [2, 12.5, 7.25], [17, 13.25, 8.75], {"*": "rail"}, None),
            ("optic_base", [9, 13.25, 7.25], [14, 13.75, 8.75], {"*": "polymer"}, None),
            ("optic", [9.5, 13.75, 7], [13.5, 15.75, 9], {"*": "sight", "west": "lens", "east": "lens"}, None),
            ("charging_handle", [-1, 11.5, 9.2], [0.5, 12.25, 10.2], {"*": "steel"}, None),
            ("sling_mount", [24, 8, 9.4], [25, 9, 9.7], {"*": "steel"}, None),
        ] + GRIP_GROUP,
        "scale_tp": 0.45, "scale_fp": 0.42,
    },
    "smg": {
        "palette": {"body": (66, 68, 72), "polymer": (28, 28, 30), "grip": (34, 33, 32), "steel": (90, 94, 100),
                    "mag": (52, 54, 58), "accent": (60, 62, 66)},
        "elements": [
            ("barrel", [-4, 9.75, 7.5], [-1, 10.75, 8.5], {"*": "steel", "west": "bore"}, None),
            ("barrel_nut", [-1, 9.25, 7.25], [0, 11.25, 8.75], {"*": "sight"}, None),
            ("receiver", [0, 8.25, 6.9], [15, 12, 9.1], {"*": "body"}, None),
            ("front_grip", [0.5, 7.25, 7.0], [5.5, 8.25, 9.0], {"*": "polymer"}, None),
            ("grip", [10, 1.5, 7], [13, 8, 9], {"*": "grip", "up": "polymer"}, GRIP_ROT),
            ("magazine", [10.4, -3.5, 7.35], [12.6, 1.5, 8.65], {"*": "mag"}, GRIP_ROT),
            ("cocking_knob", [6, 12, 7.5], [7.5, 12.75, 8.5], {"*": "steel"}, None),
            ("front_sight", [0.5, 12, 7.6], [1.5, 13.25, 8.4], {"*": "sight"}, None),
            ("rear_sight", [13, 12, 7.25], [14.25, 13, 8.75], {"*": "sight"}, None),
            ("stock_hinge", [15, 9, 7.2], [16, 11.5, 8.8], {"*": "steel"}, None),
            ("stock_arm_top", [16, 10.5, 7.3], [22, 11.25, 7.8], {"*": "steel"}, None),
            ("stock_arm_top_r", [16, 10.5, 8.2], [22, 11.25, 8.7], {"*": "steel"}, None),
            ("stock_butt", [22, 7.5, 7.1], [22.75, 11.5, 8.9], {"*": "steel"}, None),
            ("grip_safety", [13, 4, 7.6], [13.75, 7, 8.4], {"*": "steel"}, None),
            ("ejection_port", [6.5, 10.25, 9.1], [10, 11.5, 9.3], {"*": "polymer"}, None),
        ] + GRIP_GROUP,
        "scale_tp": 0.5, "scale_fp": 0.48,
    },
    "sniper_rifle": {
        "palette": {"body": (104, 68, 40), "wood": True, "polymer": (30, 31, 34), "grip": (92, 58, 34),
                    "steel": (52, 58, 66), "mag": (40, 42, 46), "accent": (34, 36, 40)},
        "elements": [
            ("muzzle_brake", [-16, 9.5, 7.3], [-14, 11.5, 8.7], {"*": "accent", "west": "bore"}, None),
            ("barrel", [-14, 10, 7.5], [2, 11, 8.5], {"*": "steel"}, None),
            ("forend", [-6, 8, 7], [6, 10.5, 9], {"*": "body"}, None),
            ("action", [2, 9.5, 7.1], [15, 12, 8.9], {"*": "steel"}, None),
            ("stock_wrist", [6, 7, 6.9], [17, 10, 9.1], {"*": "body"}, None),
            ("grip", [10, 1.5, 7], [13, 8, 9], {"*": "grip", "up": "body"}, GRIP_ROT),
            ("stock", [17, 4.5, 6.8], [28, 10.5, 9.2], {"*": "body"}, None),
            ("cheek_piece", [17, 10.5, 7], [25, 12, 9], {"*": "body"}, None),
            ("butt_pad", [28, 4, 6.7], [29, 10.75, 9.3], {"*": "rubber"}, None),
            ("bolt", [13, 10.5, 8.9], [14, 11.5, 11], {"*": "steel"}, None),
            ("bolt_knob", [12.75, 10, 10.75], [14.25, 11.75, 12], {"*": "accent"}, None),
            ("magazine", [7.5, 6, 7.3], [10, 7.25, 8.7], {"*": "mag"}, None),
            ("scope_mount_front", [3.5, 12, 7.5], [4.5, 13, 8.5], {"*": "accent"}, None),
            ("scope_mount_rear", [11, 12, 7.5], [12, 13, 8.5], {"*": "accent"}, None),
            ("scope_tube", [1, 13, 7.25], [14, 14.5, 8.75], {"*": "accent"}, None),
            ("scope_objective", [-2, 12.6, 6.85], [1, 14.9, 9.15], {"*": "accent", "west": "lens"}, None),
            ("scope_eyepiece", [14, 12.75, 7.0], [16, 14.75, 9.0], {"*": "accent", "east": "lens"}, None),
            ("scope_turret", [7, 14.5, 7.5], [8, 15.5, 8.5], {"*": "steel"}, None),
            ("bipod_stub", [-5, 7.25, 7.6], [-4, 8, 8.4], {"*": "steel"}, None),
        ] + GRIP_GROUP,
        "scale_tp": 0.4, "scale_fp": 0.38,
    },
}

GRENADE = {
    "palette": {"body": (78, 88, 52), "polymer": (60, 66, 42), "grip": (70, 78, 46), "steel": (150, 152, 156),
                "mag": (120, 124, 130), "accent": (200, 170, 60)},
    "elements": [
        ("body", [5.5, 2.5, 5.5], [10.5, 8.5, 10.5], {"*": "body"}, None),
        ("body_x", [5, 3.5, 6], [11, 7.5, 10], {"*": "grip"}, None),
        ("body_z", [6, 3.5, 5], [10, 7.5, 11], {"*": "grip"}, None),
        ("bottom", [6.5, 2, 6.5], [9.5, 2.5, 9.5], {"*": "polymer"}, None),
        ("fuse_neck", [7, 8.5, 7], [9, 10, 9], {"*": "steel"}, None),
        ("fuse_top", [6.75, 10, 6.75], [9.25, 10.75, 9.25], {"*": "mag"}, None),
        ("spoon", [9, 7, 7.4], [9.75, 10.25, 8.6], {"*": "steel"}, None),
        ("spoon_top", [8.5, 10.25, 7.4], [9.75, 10.75, 8.6], {"*": "steel"}, None),
        ("pin_ring", [5.5, 9, 7.75], [6.75, 10.5, 8.25], {"*": "accent"}, None),
        ("pin", [6.75, 9.5, 7.8], [7, 9.75, 8.2], {"*": "accent"}, None),
    ],
}

# Security Pistol transforms (tuned in gen_pistol_model.py); weapons reuse them at another scale.
PISTOL = {
    "thirdperson_righthand": ([0, -90, -72], [0, 1.25, 3.5], 0.6),
    "firstperson_righthand": ([0, -85, 0], [-1.75, 5.25, 0], 0.5),
}
GRIP_POINT = np.array([12.6, 5.2, 8.0])  # centre of the pistol grip after its 22.5 degree tilt, model px


def rot_matrix(rot):
    rx, ry, rz = [math.radians(a) for a in rot]
    cx, sx, cy, sy, cz, sz = math.cos(rx), math.sin(rx), math.cos(ry), math.sin(ry), math.cos(rz), math.sin(rz)
    Rx = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]])
    Ry = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
    Rz = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
    return Rx @ Ry @ Rz


def compensated(ctx, scale):
    """Same rotation as the pistol, new scale, translation moved so the grip stays in the same place."""
    rot, tr, s0 = PISTOL[ctx]
    g = GRIP_POINT - 8.0
    t = np.array(tr) + rot_matrix(rot) @ ((s0 - scale) * g)
    return rot, [round(float(v), 2) for v in t], scale


def display_for(w):
    rot, t, s = compensated("thirdperson_righthand", w["scale_tp"])
    frot, ft, fs = compensated("firstperson_righthand", w["scale_fp"])
    sc = lambda v: [v, v, v]
    return {
        "thirdperson_righthand": {"rotation": rot, "translation": t, "scale": sc(s)},
        "thirdperson_lefthand": {"rotation": [rot[0], -rot[1], -rot[2]], "translation": t, "scale": sc(s)},
        "firstperson_righthand": {"rotation": frot, "translation": ft, "scale": sc(fs)},
        "firstperson_lefthand": {"rotation": [frot[0], 180 + frot[1] if frot[1] < 0 else frot[1], frot[2]], "translation": ft, "scale": sc(fs)},
        "gui": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [0.55, 0.55, 0.55]},
        "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.35, 0.35, 0.35]},
        "fixed": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [0.6, 0.6, 0.6]},
        "head": {"rotation": [0, 90, 0], "translation": [0, 9, 0], "scale": [0.5, 0.5, 0.5]},
    }


GRENADE_DISPLAY = {
    "thirdperson_righthand": {"rotation": [0, 0, 0], "translation": [0, 1.5, 1.5], "scale": [0.5, 0.5, 0.5]},
    "thirdperson_lefthand": {"rotation": [0, 0, 0], "translation": [0, 1.5, 1.5], "scale": [0.5, 0.5, 0.5]},
    "firstperson_righthand": {"rotation": [0, -20, 0], "translation": [1, 3, 1], "scale": [0.6, 0.6, 0.6]},
    "firstperson_lefthand": {"rotation": [0, 20, 0], "translation": [1, 3, 1], "scale": [0.6, 0.6, 0.6]},
    "gui": {"rotation": [20, 45, 0], "translation": [0, 1, 0], "scale": [1.3, 1.3, 1.3]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
    "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [1, 1, 1]},
    "head": {"rotation": [0, 0, 0], "translation": [0, 10, 0], "scale": [1, 1, 1]},
}


def build_model(wid, elements, display):
    out = []
    for name, f, t, mats, rot in elements:
        faces = {face: {"uv": face_uv(mats.get(face, mats["*"]), f, t, face), "texture": "#gun"}
                 for face in ("north", "east", "south", "west", "up", "down")}
        el = {"name": name, "from": f, "to": t}
        if rot:
            el["rotation"] = rot
        el["faces"] = faces
        out.append(el)
    return {
        "credit": "Israel Simulator - generated by scripts/textures/gen_weapon_models.py",
        "texture_size": [32, 32],
        "textures": {"gun": f"{NS}:item/{wid}_model", "particle": f"{NS}:item/{wid}_model"},
        "elements": out,
        "display": display,
    }


def zone_colour(tex, zone):
    x0, y0, x1, y1 = ZONES[zone]
    arr = np.array(tex.crop((x0, y0, x1, y1)).convert("RGB")).reshape(-1, 3).mean(axis=0)
    return tuple(int(v) for v in arr)


def build_icon(elements, tex, diagonal=True):
    """Side view (from +Z) of the model drawn as a 32x32 pixel-art icon, muzzle to the upper right."""
    SS = 8
    polys = []
    for name, f, t, mats, rot in elements:
        pts = [(f[0], f[1]), (t[0], f[1]), (t[0], t[1]), (f[0], t[1])]
        if rot and rot["axis"] == "z":
            a = math.radians(rot["angle"])
            ox, oy = rot["origin"][0], rot["origin"][1]
            pts = [(ox + (x - ox) * math.cos(a) - (y - oy) * math.sin(a),
                    oy + (x - ox) * math.sin(a) + (y - oy) * math.cos(a)) for x, y in pts]
        polys.append((t[2], zone_colour(tex, mats.get("south", mats["*"])), pts))
    polys.sort(key=lambda p: p[0])
    ang = math.radians(35 if diagonal else 0)

    def tf(x, y):
        x = -x  # muzzle to the right
        return (x * math.cos(ang) - y * math.sin(ang), x * math.sin(ang) + y * math.cos(ang))
    allp = [tf(x, y) for _, _, pts in polys for x, y in pts]
    minx, maxx = min(p[0] for p in allp), max(p[0] for p in allp)
    miny, maxy = min(p[1] for p in allp), max(p[1] for p in allp)
    s = 29.0 / max(maxx - minx, maxy - miny)
    cx, cy = (minx + maxx) / 2, (miny + maxy) / 2
    big = Image.new("RGBA", (32 * SS, 32 * SS), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    for _, col, pts in polys:
        q = []
        for x, y in pts:
            X, Y = tf(x, y)
            q.append(((16 + (X - cx) * s) * SS, (16 - (Y - cy) * s) * SS))
        d.polygon(q, fill=col + (255,))
    small = big.resize((32, 32), Image.BOX)
    px = small.load()
    for y in range(32):
        for x in range(32):
            r, g, b, a = px[x, y]
            px[x, y] = (r, g, b, 255) if a >= 110 else (0, 0, 0, 0)
    # dark outline and a highlight on top edges for readability
    out = small.copy()
    op = out.load()
    for y in range(32):
        for x in range(32):
            if px[x, y][3] == 0:
                if any(0 <= x + dx < 32 and 0 <= y + dy < 32 and px[x + dx, y + dy][3] for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                    op[x, y] = (14, 14, 16, 255)
            elif y > 0 and px[x, y - 1][3] == 0:
                op[x, y] = shade(px[x, y], 30)
    return out


def ammo_icon(kind):
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    brass, brass_d, brass_l = (196, 156, 64), (140, 104, 36), (232, 202, 120)
    tip = (176, 104, 64)
    specs = {"rifle_ammo": ([(6, 4), (13, 6), (20, 8)], 5, 20, 5),
             "smg_ammo": ([(4, 12), (11, 13), (18, 14), (25, 15)], 4, 11, 4),
             "sniper_ammo": ([(9, 1), (18, 3)], 6, 25, 6)}
    pos, w, body, tiph = specs[kind]
    for x, y in pos:
        d.rectangle([x, y + tiph, x + w - 1, y + tiph + body], fill=brass)
        d.line([x, y + tiph, x, y + tiph + body], fill=brass_l)
        d.line([x + w - 1, y + tiph, x + w - 1, y + tiph + body], fill=brass_d)
        d.rectangle([x, y + tiph + body - 1, x + w - 1, y + tiph + body], fill=brass_d)
        d.polygon([(x, y + tiph), (x + w - 1, y + tiph), (x + w // 2, y)], fill=tip)
        if kind == "sniper_ammo":
            d.rectangle([x + 1, y + tiph + 3, x + w - 2, y + tiph + 4], fill=brass_d)
    out = im.copy()
    px, op = im.load(), out.load()
    for y in range(32):
        for x in range(32):
            if px[x, y][3] == 0 and any(0 <= x + a < 32 and 0 <= y + b < 32 and px[x + a, y + b][3]
                                        for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                op[x, y] = (40, 30, 14, 255)
    return out


def item_def(wid):
    return {"model": {"type": "minecraft:select", "property": "minecraft:display_context",
                      "cases": [{"when": ["gui", "on_shelf"], "model": {"type": "minecraft:model", "model": f"{NS}:item/{wid}_icon"}}],
                      "fallback": {"type": "minecraft:model", "model": f"{NS}:item/{wid}"}}}


def write_json(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n")


def emit(wid, spec, display, seed, diagonal=True):
    tex = build_texture(spec["palette"], seed)
    tex.save(ASSETS / f"textures/item/{wid}_model.png")
    build_icon(spec["elements"], tex, diagonal).save(ASSETS / f"textures/item/{wid}.png")
    write_json(ASSETS / f"models/item/{wid}.json", build_model(wid, spec["elements"], display))
    write_json(ASSETS / f"models/item/{wid}_icon.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{wid}"}})
    write_json(ASSETS / f"items/{wid}.json", item_def(wid))
    print(f"{wid}: {len(spec['elements'])} elements")


def main():
    for i, (wid, spec) in enumerate(WEAPONS.items()):
        emit(wid, spec, display_for(spec), 1948 + i)
    emit("frag_grenade", GRENADE, GRENADE_DISPLAY, 1973, diagonal=False)
    for ammo in ("rifle_ammo", "smg_ammo", "sniper_ammo"):
        ammo_icon(ammo).save(ASSETS / f"textures/item/{ammo}.png")
        write_json(ASSETS / f"models/item/{ammo}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{ammo}"}})
        write_json(ASSETS / f"items/{ammo}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{ammo}"}})


if __name__ == "__main__":
    main()


def palantrio_drone():
    """Entity texture (32x32, layout of PalantrioDroneModel) and flat item icon (top view)."""
    rnd = random.Random(2026)
    tex = Image.new("RGBA", (32, 32), (0, 0, 0, 255))
    px = tex.load()
    for y in range(32):
        for x in range(32):
            c = (48, 52, 58)
            if y < 8 and x < 22:
                c = (62, 66, 74) if y >= 2 else (90, 96, 106)  # body shell, lighter top
            elif 8 <= y < 14 and x < 14:
                c = (34, 36, 40)  # battery
            elif 8 <= y < 14 and 14 <= x < 22:
                c = (18, 18, 20) if not (16 <= x <= 17 and 10 <= y <= 11) else (60, 120, 170)  # camera + lens
            elif x >= 22 and y < 8:
                c = (120, 124, 130)  # motors and legs
            elif y >= 30:
                c = (200, 204, 210)  # rotor blades
            px[x, y] = shade(c, rnd.randint(-4, 4))
    px[3, 2] = (220, 40, 40, 255)  # status LED
    px[18, 2] = (40, 200, 80, 255)
    (ASSETS / "textures/entity").mkdir(parents=True, exist_ok=True)
    tex.save(ASSETS / "textures/entity/palantrio_drone.png")

    icon = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(icon)
    d.line([(7, 7), (24, 24)], fill=(70, 74, 80), width=3)
    d.line([(24, 7), (7, 24)], fill=(70, 74, 80), width=3)
    for cx, cy in ((7, 7), (24, 7), (7, 24), (24, 24)):
        d.ellipse([cx - 6, cy - 6, cx + 6, cy + 6], outline=(190, 196, 204), width=1)
        d.line([(cx - 5, cy - 1), (cx + 5, cy + 1)], fill=(220, 224, 230))
        d.rectangle([cx - 1, cy - 1, cx + 1, cy + 1], fill=(130, 134, 140))
    d.rectangle([12, 11, 19, 20], fill=(58, 62, 70), outline=(20, 20, 24))
    d.rectangle([14, 13, 17, 15], fill=(34, 36, 40))
    d.point((15, 18), fill=(220, 40, 40))
    icon.save(ASSETS / "textures/item/palantrio_drone.png")
    write_json(ASSETS / "models/item/palantrio_drone.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/palantrio_drone"}})
    write_json(ASSETS / "items/palantrio_drone.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/palantrio_drone"}})


if __name__ == "__main__":
    palantrio_drone()
