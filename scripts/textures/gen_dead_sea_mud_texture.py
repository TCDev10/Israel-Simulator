#!/usr/bin/env python3
"""Generate the 16x16 Dead Sea mineral mud item texture: a dark grey-brown mud
lump with white salt crystals and ochre/teal mineral flecks (vanilla item style)."""
import random
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "src/main/resources/assets/israel_simulator/textures/item/dead_sea_mud.png"

OUTLINE = (34, 28, 24, 255)
DARK = (58, 50, 44, 255)
MID = (82, 72, 62, 255)
LIGHT = (108, 96, 82, 255)
SHINE = (140, 126, 108, 255)
SALT = (236, 234, 224, 255)
SALT_DK = (196, 192, 180, 255)
OCHRE = (176, 138, 74, 255)
TEAL = (92, 134, 128, 255)


def main():
    rnd = random.Random(20)
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    cx, cy, rx, ry = 7.5, 8.5, 6.4, 5.4
    body = set()
    for y in range(16):
        for x in range(16):
            # blobby lump: ellipse with a small bump on top
            d = ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2
            bump = ((x - 6) / 3.0) ** 2 + ((y - 3.5) / 2.2) ** 2
            if d <= 1.0 or bump <= 1.0:
                body.add((x, y))
    for (x, y) in body:
        edge = any((x + dx, y + dy) not in body for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        if edge:
            c = OUTLINE
        else:
            shade = (x + y) - 15 + rnd.uniform(-2.5, 2.5)
            c = LIGHT if shade < -4 else MID if shade < 2 else DARK
        im.putpixel((x, y), c)
    for (x, y) in [(4, 6), (5, 5), (4, 7)]:
        im.putpixel((x, y), SHINE)
    for (x, y) in [(9, 6), (6, 10), (11, 10), (8, 12), (3, 9)]:
        im.putpixel((x, y), SALT)
        if (x + 1, y) in body and im.getpixel((x + 1, y)) != OUTLINE:
            im.putpixel((x + 1, y), SALT_DK)
    for (x, y) in [(7, 8), (12, 8), (5, 12)]:
        im.putpixel((x, y), OCHRE)
    for (x, y) in [(10, 12), (8, 4)]:
        im.putpixel((x, y), TEAL)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    im.save(OUT)


if __name__ == "__main__":
    main()
