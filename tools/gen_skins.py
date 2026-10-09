"""Generates the original 64x64 player-layout skins for the mod's humanoid mobs.
Run: python3 tools/gen_skins.py  (writes into src/main/resources/assets/israel_simulator/textures/entity)."""
import random
from PIL import Image

OUT = "src/main/resources/assets/israel_simulator/textures/entity/"

def faces(u, v, w, h, d):
    return {"top": (u+d, v, w, d), "bottom": (u+d+w, v, w, d), "right": (u, v+d, d, h),
            "front": (u+d, v+d, w, h), "left": (u+d+w, v+d, d, h), "back": (u+2*d+w, v+d, w, h)}

def shade(c, rnd, amt=10):
    k = rnd.randint(-amt, amt)
    return tuple(max(0, min(255, x+k)) for x in c[:3]) + (255,)

class Skin:
    def __init__(self, seed):
        self.im = Image.new("RGBA", (64, 64), (0, 0, 0, 0)); self.rnd = random.Random(seed)
    def fill(self, rect, c, amt=8):
        x0, y0, w, h = rect
        for x in range(x0, x0+w):
            for y in range(y0, y0+h): self.im.putpixel((x, y), shade(c, self.rnd, amt))
    def box(self, u, v, w, h, d, c, amt=8):
        for r in faces(u, v, w, h, d).values(): self.fill(r, c, amt)
    def px(self, x, y, c): self.im.putpixel((x, y), c + (255,) if len(c) == 3 else c)
    def rows(self, u, v, w, h, d, y_from, y_to, c, sides=("front", "right", "left", "back")):
        f = faces(u, v, w, h, d)
        for s in sides:
            x0, y0, fw, fh = f[s]; self.fill((x0, y0+y_from, fw, y_to-y_from), c)

def make(name, seed, skin, hair, top, pants, shoes, tie=None, shirt=(235, 235, 235), beard=None,
         glasses=None, eyes=(60, 90, 140), bald=False, hairlen=2, cap=None, sleeves=None):
    s = Skin(seed)
    # head
    s.box(0, 0, 8, 8, 8, skin, 5)
    s.fill(faces(0, 0, 8, 8, 8)["top"], hair)
    if bald:
        s.rows(0, 0, 8, 8, 8, 0, 1, hair, ("right", "left", "back"))
        s.rows(0, 0, 8, 8, 8, 1, 4, hair, ("right", "left", "back"))
        s.fill(faces(0, 0, 8, 8, 8)["top"], skin, 5)
        for x in (8, 15): s.fill((x, 8, 1, 1), skin)
    else:
        s.rows(0, 0, 8, 8, 8, 0, hairlen, hair, ("front",))
        s.rows(0, 0, 8, 8, 8, 0, 4, hair, ("right", "left"))
        s.rows(0, 0, 8, 8, 8, 0, 7, hair, ("back",))
    fx, fy = 8, 8  # front face origin
    for ex in (1, 5):
        s.px(fx+ex, fy+4, (245, 245, 245)); s.px(fx+ex+1, fy+4, eyes)
    s.px(fx+1, fy+3, tuple(max(0, c-40) for c in hair)); s.px(fx+2, fy+3, tuple(max(0, c-40) for c in hair))
    s.px(fx+5, fy+3, tuple(max(0, c-40) for c in hair)); s.px(fx+6, fy+3, tuple(max(0, c-40) for c in hair))
    nose = tuple(max(0, c-25) for c in skin)
    s.px(fx+3, fy+5, nose); s.px(fx+4, fy+5, nose)
    for x in range(3, 5 + 1): s.px(fx+x-0, fy+6, (150, 80, 70)) if x < 5 else None
    if beard:
        s.rows(0, 0, 8, 8, 8, 6, 8, beard, ("front", "right", "left"))
        s.px(fx+3, fy+6, (130, 70, 60)); s.px(fx+4, fy+6, (130, 70, 60))
    if glasses:
        for x in range(1, 7): s.px(fx+x, fy+4, glasses)
        s.px(fx+3, fy+4, (20, 20, 20)); s.px(fx+4, fy+4, (20, 20, 20))
    if cap:  # hat overlay band
        f = faces(32, 0, 8, 8, 8)
        s.fill(f["top"], cap)
        for k in ("front", "right", "left", "back"):
            x0, y0, w, h = f[k]; s.fill((x0, y0, w, 2), cap)
    # body: jacket over shirt with tie
    s.box(16, 16, 8, 12, 4, top)
    bx, by = 20, 20
    if shirt:
        for y in range(0, 5):
            for x in range(3, 5): s.px(bx+x, by+y, shirt)
        s.px(bx+2, by, shirt); s.px(bx+5, by, shirt)
    if tie:
        for y in range(1, 7): s.px(bx+3 + (1 if y % 2 else 0) * 0, by+y, tie); s.px(bx+4, by+y, tie)
    belt = (30, 25, 20)
    s.rows(16, 16, 8, 12, 4, 11, 12, belt)
    # arms
    for (u, v) in ((40, 16), (32, 48)):
        s.box(u, v, 4, 12, 4, sleeves or top)
        s.rows(u, v, 4, 12, 4, 10, 12, skin)
        s.fill(faces(u, v, 4, 12, 4)["bottom"], skin)
    # legs
    for (u, v) in ((0, 16), (16, 48)):
        s.box(u, v, 4, 12, 4, pants)
        s.rows(u, v, 4, 12, 4, 10, 12, shoes); s.fill(faces(u, v, 4, 12, 4)["bottom"], shoes)
    s.im.save(OUT + name + ".png")

SKIN_L, SKIN_M, SKIN_T = (232, 190, 160), (214, 168, 128), (190, 140, 100)
make("orator", 1, SKIN_L, (120, 80, 45), (35, 45, 95), (40, 40, 50), (25, 20, 18), tie=(170, 30, 40), eyes=(50, 110, 70))
make("mossad_agent", 2, SKIN_M, (25, 22, 20), (28, 28, 32), (24, 24, 28), (15, 15, 15), tie=(15, 15, 15),
     glasses=(10, 10, 10), shirt=(220, 220, 225))
make("mossad_handler", 3, SKIN_M, (150, 150, 150), (85, 80, 70), (60, 55, 50), (40, 30, 22), shirt=(200, 210, 225),
     beard=(140, 140, 140), tie=None, eyes=(80, 60, 40))
make("mossad_informant", 4, SKIN_T, (40, 30, 25), (120, 95, 60), (70, 80, 110), (90, 80, 70), shirt=None,
     cap=(40, 60, 40), eyes=(70, 50, 30), sleeves=(110, 88, 55))
make("bibi_boss", 5, SKIN_L, (190, 190, 195), (30, 35, 60), (30, 35, 55), (20, 18, 16), tie=(40, 70, 150))
make("jeffrey_epstein", 6, SKIN_L, (175, 175, 180), (20, 20, 25), (225, 225, 220), (35, 30, 28), shirt=(235, 235, 235),
     eyes=(70, 90, 120))
make("trump_miniboss", 7, (235, 170, 120), (240, 205, 120), (20, 25, 50), (20, 25, 45), (15, 15, 15), tie=(200, 25, 30),
     hairlen=3)
make("money_changer", 8, SKIN_M, (60, 40, 25), (110, 30, 30), (70, 50, 35), (45, 30, 20), shirt=(230, 220, 190),
     beard=(55, 38, 25), cap=(30, 30, 30), eyes=(70, 50, 30))
make("child_zombie", 9, (95, 150, 90), (40, 55, 35), (60, 120, 160), (50, 60, 120), (60, 60, 60), shirt=None,
     eyes=(20, 20, 20), sleeves=(60, 120, 160))
