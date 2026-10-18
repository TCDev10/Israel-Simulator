#!/usr/bin/env python3
"""Grapevine crop, age 0-7 (16x16, drawn on the vanilla crop '#' model).

A real trained vine instead of a wheat-like stalk: a gnarled brown trunk that rises from the
soil and splits into a horizontal cordon along a trellis wire, broad lobed vine leaves, curling
tendrils, then flowers, green bunches and finally ripe purple bunches hanging under the cordon.

Writes only textures/block/grapevine_stage0..7.png (the grapes item is drawn separately).
Run:  python3 scripts/textures/gen_grapevine_crop.py
"""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
BLOCK = ROOT / "src/main/resources/assets/israel_simulator/textures/block"

T = (0, 0, 0, 0)
BARK, BARK_DK, BARK_LT = (112, 80, 50, 255), (74, 52, 32, 255), (146, 108, 70, 255)
POST, POST_DK = (156, 124, 84, 255), (118, 90, 58, 255)
WIRE = (150, 150, 146, 255)
LEAF, LEAF_LT, LEAF_DK, VEIN = (78, 136, 44, 255), (112, 166, 60, 255), (50, 96, 32, 255), (150, 190, 92, 255)
LEAF_OLD = (150, 150, 54, 255)                      # first leaves turning at harvest
TENDRIL = (150, 186, 82, 255)
BUD = (176, 196, 100, 255)
FLOWER, FLOWER_LT = (186, 196, 104, 255), (222, 226, 150, 255)
UNRIPE, UNRIPE_LT, UNRIPE_DK = (170, 198, 86, 255), (206, 224, 132, 255), (126, 158, 58, 255)
RIPE, RIPE_LT, RIPE_DK, RIPE_HI = (96, 40, 122, 255), (132, 70, 160, 255), (58, 22, 80, 255), (178, 132, 206, 255)


class Tex:
    def __init__(self):
        self.im = Image.new("RGBA", (16, 16), T)

    def px(self, x, y, c, over=True):
        if 0 <= x < 16 and 0 <= y < 16 and (over or self.im.getpixel((x, y))[3] == 0):
            self.im.putpixel((x, y), c)

    def trunk(self, top, x=7):
        """Gnarled two-pixel trunk from the ground (y=15) up to `top`, with a slight twist."""
        for y in range(top, 16):
            dx = 1 if (y // 3) % 2 else 0
            self.px(x + dx, y, BARK)
            self.px(x + 1 - dx, y, BARK_DK if y % 2 else BARK)
            if y % 4 == 1:
                self.px(x + dx, y, BARK_LT)
        self.px(x - 1, 15, BARK_DK); self.px(x + 2, 15, BARK_DK)       # root flare

    def cordon(self, y, x0, x1):
        """Horizontal arms of old wood trained along the wire."""
        for x in range(x0, x1 + 1):
            self.px(x, y, BARK if x % 3 else BARK_DK)
            if x % 4 == 0:
                self.px(x, y + 1, BARK_DK)          # knobbly spur

    def post(self, x, top):
        for y in range(top, 16):
            self.px(x, y, POST if y % 3 else POST_DK)

    def wire(self, y, x0, x1):
        for x in range(x0, x1 + 1):
            self.px(x, y, WIRE, over=False)

    def leaf(self, cx, cy, size=2, old=False):
        """Palmate vine leaf, lobes up, stalk at the bottom centre."""
        main, lt, dk = (LEAF_OLD, LEAF_OLD, LEAF) if old else (LEAF, LEAF_LT, LEAF_DK)
        if size == 1:
            for dx, dy, c in ((0, 0, lt), (-1, 0, main), (1, 0, main), (0, -1, main)):
                self.px(cx + dx, cy + dy, c)
            return
        shape = [
            "x.x.x",
            "xxxxx",
            ".xvx.",
            "..s..",
        ] if size == 2 else [
            ".x.x.x.",
            "xxxxxxx",
            "xxxvxxx",
            ".xxvxx.",
            "...s...",
        ]
        w = len(shape[0]) // 2
        for r, row in enumerate(shape):
            for c, ch in enumerate(row):
                x, y = cx + c - w, cy + r - len(shape) + 2
                if ch == "x":
                    edge = r == 0 or c in (0, len(row) - 1)
                    self.px(x, y, dk if edge and (x + y) % 2 else (lt if (x * 3 + y) % 4 == 0 else main))
                elif ch == "v":
                    self.px(x, y, VEIN if not old else LEAF)
                elif ch == "s":
                    self.px(x, y, dk)

    def tendril(self, x, y, right=True):
        d = 1 if right else -1
        for dx, dy in ((0, 0), (d, -1), (2 * d, -1), (2 * d, 0)):
            self.px(x + dx, y + dy, TENDRIL, over=False)

    def flowers(self, x, y):
        self.px(x, y, LEAF_DK)
        for dx, dy, c in ((0, 1, FLOWER), (-1, 2, FLOWER_LT), (1, 2, FLOWER), (0, 2, FLOWER), (0, 3, FLOWER_LT)):
            self.px(x + dx, y + dy, c)

    def bunch(self, x, y, ripe, big=True):
        """Bunch hanging from (x, y): stalk, then a downward triangle of berries."""
        main, lt, dk, hi = (RIPE, RIPE_LT, RIPE_DK, RIPE_HI) if ripe else (UNRIPE, UNRIPE_LT, UNRIPE_DK, UNRIPE_LT)
        self.px(x, y, BARK_DK)
        rows = [(-1, 1), (-1, 2), (-1, 1), (0, 1), (0, 0)] if big else [(-1, 1), (-1, 0), (0, 0)]
        for r, (a, b) in enumerate(rows):
            for dx in range(a, b + 1):
                c = dk if (dx + r) % 2 else main
                if (dx, r) in ((0, 0), (1, 1), (-1, 2)):
                    c = lt
                self.px(x + dx, y + 1 + r, c)
        self.px(x, y + 1, hi)


def stage(n):
    t = Tex()
    if n == 0:                                   # planted cutting: woody stub with a bud
        t.trunk(12)
        t.px(8, 11, BUD); t.px(9, 10, LEAF_LT); t.leaf(9, 10, 1)
    elif n == 1:                                 # first shoot, two small leaves, a stake
        t.post(12, 6)
        t.trunk(11)
        t.px(8, 10, LEAF_DK); t.px(8, 9, LEAF_DK)
        t.leaf(6, 9, 1); t.leaf(9, 8, 2)
    elif n == 2:                                 # shoot climbs the stake
        t.post(12, 3)
        t.trunk(9)
        for y in range(5, 9):
            t.px(8 + (y % 2), y, LEAF_DK)
        t.leaf(5, 9, 2); t.leaf(10, 6, 2); t.leaf(8, 4, 1)
        t.tendril(10, 8)
    elif n == 3:                                 # trunk reaches the wire, arms start
        t.wire(5, 0, 15); t.post(1, 3); t.post(14, 3)
        t.trunk(5); t.cordon(5, 4, 11)
        t.leaf(5, 4, 2); t.leaf(10, 4, 2); t.leaf(7, 9, 2)
        t.tendril(12, 4); t.tendril(3, 7, False)
    elif n == 4:                                 # arms along the whole wire, canopy above
        t.wire(5, 0, 15); t.post(1, 3); t.post(14, 3)
        t.trunk(5); t.cordon(5, 1, 14)
        for x in (3, 8, 13):
            t.leaf(x, 3, 2)
        t.leaf(5, 8, 2); t.leaf(11, 8, 2)
        t.tendril(10, 1); t.tendril(2, 9, False)
    elif n == 5:                                 # full canopy, flower clusters
        t.wire(5, 0, 15); t.post(1, 3); t.post(14, 3)
        t.trunk(5); t.cordon(5, 1, 14)
        for x in (2, 7, 12):
            t.leaf(x, 2, 3)
        t.leaf(4, 9, 2); t.leaf(11, 9, 2)
        t.flowers(6, 6); t.flowers(10, 6); t.flowers(3, 6)
        t.tendril(13, 10)
    else:                                        # 6: green bunches, 7: ripe purple bunches
        ripe = n == 7
        t.wire(5, 0, 15); t.post(1, 3); t.post(14, 3)
        t.trunk(5); t.cordon(5, 1, 14)
        for x, old in ((2, ripe), (7, False), (12, False)):
            t.leaf(x, 2, 3, old)
        t.leaf(12, 10, 2, ripe); t.leaf(2, 11, 2)
        t.bunch(4, 6, ripe); t.bunch(11, 6, ripe)
        if ripe:
            t.bunch(8, 7, ripe, big=False)
        t.tendril(13, 12)
    return t.im


def main():
    for n in range(8):
        stage(n).save(BLOCK / f"grapevine_stage{n}.png")
    print("ok")


if __name__ == "__main__":
    main()
