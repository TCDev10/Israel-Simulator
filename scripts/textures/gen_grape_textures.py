#!/usr/bin/env python3
"""Regenerate grapevine (age 0-7) and grapes item 16x16 textures."""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
BLOCK = ROOT / "src/main/resources/assets/israel_simulator/textures/block"
ITEM = ROOT / "src/main/resources/assets/israel_simulator/textures/item"

STEM = (62, 92, 40, 255); STEM_DK = (40, 64, 28, 255)
LEAF = (78, 140, 52, 255); LEAF_LT = (110, 170, 70, 255); LEAF_DK = (48, 100, 36, 255)
GRAPE = (98, 42, 140, 255); GRAPE_LT = (140, 70, 180, 255); GRAPE_DK = (60, 20, 90, 255)
AIR = (0, 0, 0, 0)

def new(): return Image.new("RGBA", (16, 16), AIR)
def px(im, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16: im.putpixel((x, y), c)
def stem_column(im, x, y0, y1):
    for y in range(y0, y1 + 1):
        px(im, x, y, STEM if y % 2 == 0 else STEM_DK)
        if y % 3 == 0: px(im, x + 1, y, STEM_DK)
def leaf(im, cx, cy, big=False):
    coords = [(-1,0),(0,-1),(0,0),(0,1),(1,0)]
    if big: coords += [(-1,-1),(-1,1),(1,-1),(1,1),(0,-2),(0,2)]
    for dx, dy in coords:
        c = LEAF_LT if (dx + dy) % 2 == 0 else LEAF
        if abs(dx) + abs(dy) > 1: c = LEAF_DK if (dx * dy) < 0 else LEAF
        px(im, cx + dx, cy + dy, c)
def grapes(im, cx, cy, count):
    offsets = [(0,0),(1,0),(-1,1),(0,1),(1,1),(0,2),(1,2),(-1,2),(0,3)]
    for i in range(min(count, len(offsets))):
        dx, dy = offsets[i]
        c = GRAPE_LT if i % 3 == 0 else (GRAPE_DK if i % 2 else GRAPE)
        px(im, cx + dx, cy + dy, c)

def main():
    BLOCK.mkdir(parents=True, exist_ok=True); ITEM.mkdir(parents=True, exist_ok=True)
    im = new(); stem_column(im, 7, 13, 15); leaf(im, 7, 12); im.save(BLOCK / "grapevine_stage0.png")
    im = new(); stem_column(im, 7, 10, 15); leaf(im, 6, 11); leaf(im, 8, 12); im.save(BLOCK / "grapevine_stage1.png")
    im = new(); stem_column(im, 7, 7, 15); leaf(im, 5, 9, True); leaf(im, 9, 10); leaf(im, 7, 8); im.save(BLOCK / "grapevine_stage2.png")
    im = new(); stem_column(im, 7, 4, 15)
    for ly in (5, 8, 11, 13):
        leaf(im, 5 if ly % 2 else 9, ly, ly < 10); leaf(im, 9 if ly % 2 else 5, ly + 1)
    im.save(BLOCK / "grapevine_stage3.png")
    im = new(); stem_column(im, 7, 2, 15)
    for ly in (3, 5, 7, 9, 11, 13):
        leaf(im, 4 + (ly % 3), ly, True); leaf(im, 10 - (ly % 3), ly + 1)
    im.save(BLOCK / "grapevine_stage4.png")
    im = new(); stem_column(im, 7, 1, 15)
    for ly in (3, 6, 9, 12):
        leaf(im, 5, ly, True); leaf(im, 9, ly + 1, True)
    grapes(im, 6, 8, 3); grapes(im, 9, 11, 2); im.save(BLOCK / "grapevine_stage5.png")
    im = new(); stem_column(im, 7, 0, 15)
    for ly in (2, 5, 8, 11, 13):
        leaf(im, 4, ly, True); leaf(im, 10, ly, True); leaf(im, 7, ly + 1)
    grapes(im, 5, 6, 5); grapes(im, 9, 9, 6); grapes(im, 6, 12, 4); im.save(BLOCK / "grapevine_stage6.png")
    im = new(); stem_column(im, 7, 0, 15)
    for ly in (1, 4, 7, 10, 13):
        leaf(im, 3, ly, True); leaf(im, 11, ly, True); leaf(im, 7, ly + 1, True)
    grapes(im, 4, 4, 7); grapes(im, 9, 6, 8); grapes(im, 5, 10, 7); grapes(im, 10, 12, 5)
    im.save(BLOCK / "grapevine_stage7.png")
    im = new()
    for y in range(2, 6):
        px(im, 7, y, STEM); px(im, 8, y, STEM_DK)
    leaf(im, 6, 3); leaf(im, 9, 4)
    for dx, dy, c in [
        (6,6,GRAPE_LT),(7,6,GRAPE),(8,6,GRAPE_DK),(9,6,GRAPE),
        (5,7,GRAPE),(6,7,GRAPE_DK),(7,7,GRAPE_LT),(8,7,GRAPE),(9,7,GRAPE_DK),(10,7,GRAPE),
        (5,8,GRAPE_DK),(6,8,GRAPE),(7,8,GRAPE_DK),(8,8,GRAPE_LT),(9,8,GRAPE),(10,8,GRAPE_DK),
        (6,9,GRAPE),(7,9,GRAPE_LT),(8,9,GRAPE),(9,9,GRAPE_DK),
        (6,10,GRAPE_DK),(7,10,GRAPE),(8,10,GRAPE_DK),(7,11,GRAPE),(8,11,GRAPE_LT),
    ]:
        px(im, dx, dy, c)
    im.save(ITEM / "grapes.png")
    print("ok")

if __name__ == "__main__":
    main()
