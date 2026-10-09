#!/usr/bin/env python3
"""Placeholder 64x64 player-format skins for the fictional Mossad Agent, Mossad Handler and
informant, plus the Sealed Dossier icon and spawn-egg icons. Replace the skins freely
(textures/entity/<name>.png, standard 64x64 player skin layout)."""
import random
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
TEX = ROOT / "src/main/resources/assets/israel_simulator/textures"


def box(img, u, v, w, h, d, fn):
    px = img.load()
    faces = {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "right": (u, v + d, d, h),
             "front": (u + d, v + d, w, h), "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}
    for face, (x0, y0, fw, fh) in faces.items():
        for y in range(fh):
            for x in range(fw):
                c = fn(face, x, y, fw, fh)
                px[x0 + x, y0 + y] = tuple(c[:3]) + (255,)


def jitter(c, r, n=4):
    return tuple(max(0, min(255, v + r.randint(-n, n))) for v in c)


def skin(name, suit, shirt, tie, hair, skin_c, shades, seed):
    r = random.Random(seed)
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))

    def head(face, x, y, w, h):
        if face == "top":
            return jitter(hair, r)
        if face == "bottom":
            return jitter(skin_c, r)
        if face == "back":
            return jitter(hair, r) if y < 6 else jitter(skin_c, r)
        if face in ("left", "right"):
            return jitter(hair, r) if y < 2 or (y < 4 and x in (0, 7) if face == "right" else y < 4 and x in (0, 7)) else jitter(skin_c, r)
        # front
        if y < 2:
            return jitter(hair, r)
        if y == 3 or y == 4:
            if shades and 1 <= x <= 6:
                return (16, 16, 18) if x not in (3, 4) or y == 3 else jitter(skin_c, r)
            if x in (2, 5) and y == 4:
                return (40, 30, 24)
        if y == 6 and 3 <= x <= 4:
            return (150, 96, 80)
        if y == 7 and hair != skin_c and r.random() < 0.3:
            return jitter(tuple(v * 3 // 4 for v in hair), r, 2)  # stubble
        return jitter(skin_c, r)
    box(img, 0, 0, 8, 8, 8, head)

    def body(face, x, y, w, h):
        if face == "front":
            if y < 3 and 2 <= x <= 5:
                return shirt if not (x in (3, 4) and y >= 1) else tie
            if 3 <= x <= 4 and y < 9:
                return jitter(tie, r, 2)
            if 2 <= x <= 5 and y < 6:
                return jitter(shirt, r, 2)
            if y == 10:
                return (20, 20, 22)  # belt
            if x == 3 or x == 4:
                return jitter(tuple(v - 6 for v in suit), r, 2)
        return jitter(suit, r, 3)
    box(img, 16, 16, 8, 12, 4, body)

    def arm(face, x, y, w, h):
        if face == "bottom" or (face != "top" and y >= h - 3):
            return jitter(skin_c, r)
        if face != "top" and y == h - 4:
            return shirt  # cuff
        return jitter(suit, r, 3)
    box(img, 40, 16, 4, 12, 4, arm)
    box(img, 32, 48, 4, 12, 4, arm)

    def leg(face, x, y, w, h):
        if face == "bottom" or (face != "top" and y >= h - 2):
            return jitter((18, 18, 20), r, 2)
        return jitter(tuple(v - 4 for v in suit), r, 3)
    box(img, 0, 16, 4, 12, 4, leg)
    box(img, 16, 48, 4, 12, 4, leg)
    (TEX / "entity").mkdir(parents=True, exist_ok=True)
    img.save(TEX / f"entity/{name}.png")


def dossier():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rectangle([2, 3, 13, 13], fill=(196, 160, 96), outline=(110, 80, 40))
    d.rectangle([2, 2, 7, 3], fill=(176, 140, 80), outline=(110, 80, 40))
    d.line([(4, 6), (11, 6)], fill=(90, 70, 50))
    d.line([(4, 8), (9, 8)], fill=(90, 70, 50))
    d.ellipse([9, 9, 13, 13], fill=(170, 30, 30), outline=(110, 16, 16))
    im.save(TEX / "item/sealed_dossier.png")


def egg(name, base, spots, seed):
    r = random.Random(seed)
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.ellipse([3, 1, 12, 15], fill=base, outline=tuple(v // 2 for v in base))
    for _ in range(7):
        x, y = r.randint(5, 10), r.randint(3, 13)
        d.point((x, y), fill=spots)
        d.point((x + 1, y), fill=spots)
    im.save(TEX / f"item/{name}.png")


if __name__ == "__main__":
    skin("mossad_agent", (28, 29, 33), (224, 224, 220), (20, 20, 24), (30, 24, 20), (196, 150, 118), True, 1)
    skin("mossad_handler", (70, 72, 78), (230, 230, 232), (36, 52, 96), (120, 116, 112), (204, 160, 128), False, 2)
    skin("mossad_informant", (98, 84, 64), (176, 196, 210), (176, 196, 210), (60, 40, 28), (176, 126, 92), False, 3)
    dossier()
    egg("mossad_agent_spawn_egg", (30, 30, 34), (220, 220, 220), 4)
    egg("mossad_handler_spawn_egg", (70, 72, 78), (36, 52, 140), 5)
