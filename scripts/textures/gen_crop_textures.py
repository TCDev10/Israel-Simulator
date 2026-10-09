#!/usr/bin/env python3
"""Generate 16x16 vanilla-style textures for the fruiting leaves (olive, date palm,
citrus; age 0-3), Mediterranean herbs (age 0-3 + item) and the dates, olives and citrus items.

Colours are baked in (no biome tint needed), so the models use plain
cube_all / cross / item/generated parents without tintindex.
"""
import random
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
BLOCK = ROOT / "src/main/resources/assets/israel_simulator/textures/block"
ITEM = ROOT / "src/main/resources/assets/israel_simulator/textures/item"
AIR = (0, 0, 0, 0)


def new():
    return Image.new("RGBA", (16, 16), AIR)


def px(im, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        im.putpixel((x, y), c)


def leaves_base(seed, palette, holes=10):
    """Vanilla-like leaf noise: clumps of 3-4 shades plus a few see-through holes."""
    rnd = random.Random(seed)
    im = new()
    for y in range(16):
        for x in range(16):
            n = rnd.random()
            idx = 0 if n < 0.18 else 1 if n < 0.55 else 2 if n < 0.85 else 3
            im.putpixel((x, y), palette[idx])
    for _ in range(holes):
        px(im, rnd.randrange(16), rnd.randrange(16), AIR)
    return im


# fruit spots (x, y) shared by all stages so fruit grows in place
SPOTS = [(2, 2), (10, 1), (6, 5), (13, 6), (1, 9), (8, 10), (12, 12), (4, 13)]


def fruit(im, spots, colours, size):
    hi, mid, dk = colours
    for (x, y) in spots:
        if size == 1:
            px(im, x, y, mid)
        else:
            px(im, x, y, hi); px(im, x + 1, y, mid)
            px(im, x, y + 1, mid); px(im, x + 1, y + 1, dk)


def blossom(im, spots, petal, centre):
    for (x, y) in spots:
        px(im, x, y, centre)
        px(im, x - 1, y, petal); px(im, x + 1, y, petal)
        px(im, x, y - 1, petal); px(im, x, y + 1, petal)


def gen_leaves(name, seed, palette, flower, unripe, ripe, ripe_style="round"):
    for age in range(4):
        im = leaves_base(seed, palette)
        if age == 1:
            blossom(im, SPOTS[::2], *flower)
        elif age == 2:
            fruit(im, SPOTS, unripe, 1)
        elif age == 3:
            if ripe_style == "cluster":
                # hanging date clusters: a stalk + 3-4 dates
                hi, mid, dk = ripe
                for (x, y) in SPOTS[::2]:
                    px(im, x, y, (196, 160, 60, 255))
                    for dx, dy, c in [(-1, 1, mid), (1, 1, hi), (0, 2, mid), (-1, 3, dk), (1, 3, mid), (0, 4, dk)]:
                        px(im, x + dx, y + dy, c)
            else:
                fruit(im, SPOTS, ripe, 2)
        im.save(BLOCK / f"{name}_stage{age}.png")


def gen_herbs():
    stem = (86, 110, 58, 255)
    sage = [(122, 150, 104, 255), (150, 176, 128, 255), (96, 124, 82, 255)]
    rosemary = [(58, 102, 64, 255), (80, 128, 82, 255)]
    lav = [(150, 110, 196, 255), (120, 82, 170, 255), (186, 150, 222, 255)]
    def sprig(im, x, base, top, leafy, flowers=False):
        for y in range(top, base + 1):
            px(im, x, y, stem)
            if leafy and (y - top) % 2 == 0:
                c = sage[(y + x) % 3]
                px(im, x - 1, y, c); px(im, x + 1, y, rosemary[(y + x) % 2])
        if flowers:
            for i, y in enumerate(range(top - 3, top)):
                px(im, x, y, lav[i % 3]); px(im, x - 1 + (i % 2) * 2, y, lav[(i + 1) % 3])
    heights = {0: [(6, 15, 12), (9, 15, 13)], 1: [(6, 15, 11), (9, 15, 12)],
               2: [(4, 15, 9), (7, 15, 7), (10, 15, 8), (12, 15, 11)],
               3: [(3, 15, 9), (6, 15, 6), (9, 15, 5), (12, 15, 8)]}
    for age in range(4):
        im = new()
        for (x, base, top) in heights[age]:
            sprig(im, x, base, top, True, flowers=(age == 3))
        im.save(BLOCK / f"mediterranean_herbs_stage{age}.png")
    # item: a tied bundle of sage, rosemary and lavender
    im = new()
    for x, top, fl in [(5, 4, True), (7, 2, False), (9, 3, True), (11, 5, False)]:
        for y in range(top, 13):
            px(im, x - (13 - y) // 5 + 1, y, stem)
            if (y - top) % 2 == 0 and y < 10:
                px(im, x - (13 - y) // 5, y, sage[y % 3]); px(im, x - (13 - y) // 5 + 2, y, rosemary[y % 2])
        if fl:
            for i in range(3):
                px(im, x - (13 - top) // 5 + 1, top - 1 - i, lav[i])
    for x in range(5, 11):
        px(im, x, 11, (176, 120, 70, 255)); px(im, x, 12, (140, 90, 50, 255))
    for x, y in [(6, 13), (7, 14), (9, 13), (10, 14)]:
        px(im, x, y, stem)
    im.save(ITEM / "mediterranean_herbs.png")


def gen_dates_item():
    hi, mid, dk, out = (214, 132, 70, 255), (160, 80, 36, 255), (110, 50, 22, 255), (64, 28, 12, 255)
    stalk = (196, 160, 70, 255); stalk_dk = (150, 118, 48, 255)
    im = new()
    for i, (x, y) in enumerate([(7, 1), (8, 2), (8, 3), (7, 4), (6, 5)]):
        px(im, x, y, stalk if i % 2 == 0 else stalk_dk)
    # oval dates (3x4) around the stalk
    for (cx, cy) in [(3, 6), (8, 5), (5, 10), (10, 9), (7, 12)]:
        for dy in range(4):
            for dx in range(3):
                c = mid
                if dx == 0 and dy in (1, 2) or (dx, dy) == (1, 0): c = hi
                if dx == 2 or dy == 3: c = dk
                px(im, cx + dx, cy + dy, c)
        px(im, cx + 1, cy - 1, out); px(im, cx + 1, cy + 4, out)
        px(im, cx - 1, cy + 1, out); px(im, cx - 1, cy + 2, out)
        px(im, cx + 3, cy + 1, out); px(im, cx + 3, cy + 2, out)
    im.save(ITEM / "dates.png")


def gen_olives_item():
    leaf = [(104, 128, 88, 255), (134, 156, 116, 255), (70, 92, 62, 255)]
    twig = (110, 84, 52, 255)
    green = ((150, 176, 76, 255), (118, 146, 56, 255), (70, 92, 34, 255))
    black = ((120, 86, 140, 255), (62, 36, 74, 255), (26, 14, 30, 255))
    im = new()
    for i in range(10):
        px(im, 2 + i, 2 + i // 2, twig)
    for (x, y, c) in [(4, 1, 0), (5, 1, 1), (6, 2, 1), (9, 2, 0), (10, 3, 1), (11, 3, 2), (12, 5, 0), (13, 6, 1)]:
        px(im, x, y, leaf[c])
    for (cx, cy, col) in [(3, 6, green), (7, 7, black), (10, 9, black), (5, 11, green), (9, 12, black)]:
        hi, mid, dk = col
        for dy in range(3):
            for dx in range(3):
                if (dx, dy) in ((0, 0), (2, 0), (0, 2), (2, 2)):
                    continue
                px(im, cx + dx, cy + dy, mid)
        px(im, cx, cy + 1, hi); px(im, cx + 1, cy, hi); px(im, cx + 2, cy + 1, dk); px(im, cx + 1, cy + 2, dk)
        px(im, cx + 1, cy - 1, twig)
    im.save(ITEM / "olives.png")


def gen_citrus_item():
    hi, mid, dk, out = (255, 214, 110, 255), (246, 150, 28, 255), (204, 104, 14, 255), (150, 70, 10, 255)
    leaf, leaf_dk, stem = (62, 124, 50, 255), (36, 86, 34, 255), (96, 70, 40, 255)
    im = new()
    cx, cy, r = 7.5, 9.0, 5.6
    for y in range(16):
        for x in range(16):
            d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
            if d <= r:
                c = mid
                if d > r - 1.0: c = out
                elif x + y < 13: c = hi
                elif x + y > 20: c = dk
                px(im, x, y, c)
    for (x, y) in [(5, 7), (9, 10), (7, 12), (10, 7)]:
        px(im, x, y, dk)
    px(im, 6, 6, (255, 240, 190, 255))
    px(im, 8, 3, stem); px(im, 8, 2, stem)
    for (x, y, c) in [(9, 2, leaf), (10, 1, leaf), (11, 1, leaf), (10, 2, leaf_dk), (12, 2, leaf_dk), (11, 2, leaf)]:
        px(im, x, y, c)
    im.save(ITEM / "citrus.png")


def main():
    BLOCK.mkdir(parents=True, exist_ok=True); ITEM.mkdir(parents=True, exist_ok=True)
    # olive: silvery grey-green leaves, white blossoms, green -> black olives
    gen_leaves("olive_leaves", 11,
               [(70, 92, 62, 255), (104, 128, 88, 255), (134, 156, 116, 255), (168, 184, 150, 255)],
               ((236, 236, 214, 255), (214, 200, 110, 255)),
               ((150, 176, 76, 255), (118, 146, 56, 255), (86, 110, 40, 255)),
               ((110, 76, 128, 255), (62, 36, 74, 255), (30, 18, 36, 255)))
    # date palm: yellow-green fronds, cream flower strands, yellow -> brown dates
    gen_leaves("date_palm_leaves", 23,
               [(62, 110, 40, 255), (88, 140, 52, 255), (116, 164, 64, 255), (150, 186, 84, 255)],
               ((236, 222, 160, 255), (210, 180, 90, 255)),
               ((230, 196, 70, 255), (210, 168, 48, 255), (170, 130, 30, 255)),
               ((214, 132, 70, 255), (160, 80, 36, 255), (100, 44, 20, 255)), ripe_style="cluster")
    # citrus: dark glossy leaves, white orange blossom, green -> orange fruit
    gen_leaves("citrus_leaves", 37,
               [(28, 74, 30, 255), (44, 98, 40, 255), (62, 124, 50, 255), (92, 150, 66, 255)],
               ((250, 250, 240, 255), (240, 214, 90, 255)),
               ((140, 180, 60, 255), (110, 150, 44, 255), (80, 116, 32, 255)),
               ((255, 196, 70, 255), (244, 140, 24, 255), (196, 96, 14, 255)))
    gen_herbs()
    gen_dates_item()
    gen_olives_item()
    gen_citrus_item()


if __name__ == "__main__":
    main()
