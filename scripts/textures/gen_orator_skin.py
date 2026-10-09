"""Placeholder 64x64 player skin for the fictional Orator (textures/entity/orator.png).
Generic look: short brown hair, white shirt, navy jacket, grey trousers. Replace freely."""
from PIL import Image
import random, os
random.seed(7)
img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
px = img.load()
def box(x0, y0, w, h, col, noise=6):
    for x in range(x0, x0 + w):
        for y in range(y0, y0 + h):
            n = random.randint(-noise, noise)
            px[x, y] = tuple(max(0, min(255, c + n)) for c in col) + (255,)
SKIN=(214,170,130); HAIR=(92,60,36); SHIRT=(236,236,232); JACKET=(34,46,86); PANTS=(80,82,90); SHOE=(40,30,24)
# head (8x8x8) at 0,0
box(0,8,32,8,SKIN); box(8,0,16,8,SKIN)
box(8,0,8,8,HAIR)                       # top
box(0,8,32,2,HAIR)                      # hairline all round
box(24,8,8,7,HAIR)                      # back of head
box(0,8,2,6,HAIR); box(22,8,2,6,HAIR)   # sideburns side faces
# face (front 8..16, 8..16)
px[10,12]=(255,255,255,255); px[11,12]=(50,70,110,255)
px[13,12]=(50,70,110,255); px[12+2,12]=(255,255,255,255)
for x in range(11,13+1): px[x,14]=(160,100,90,255)
# body 16,16 (front 20..28, 20..32)
box(16,16,24,16,JACKET); box(20,16,16,4,JACKET)
box(23,20,2,12,SHIRT)                   # shirt opening
for y in range(21,28): px[24,y]=(150,30,40,255)  # tie
# right arm 40,16 ; left arm 32,48
box(40,16,16,16,JACKET); box(44,16,8,4,JACKET); box(40,29,16,3,SKIN)
box(32,48,16,16,JACKET); box(36,48,8,4,JACKET); box(32,61,16,3,SKIN)
# right leg 0,16 ; left leg 16,48
box(0,16,16,16,PANTS); box(4,16,8,4,PANTS); box(0,29,16,3,SHOE); box(8,16,4,4,SHOE)
box(16,48,16,16,PANTS); box(20,48,8,4,PANTS); box(16,61,16,3,SHOE); box(24,48,4,4,SHOE)
out = os.path.join(os.path.dirname(__file__), "../../src/main/resources/assets/israel_simulator/textures/entity/orator.png")
img.save(out)
print("wrote", os.path.normpath(out))
