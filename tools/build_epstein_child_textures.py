import os
from PIL import Image, ImageDraw

def generate_epstein_skin():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    p = img.load()

    # Colors
    c_skin = (230, 185, 150, 255)
    c_skin_shadow = (200, 155, 120, 255)
    c_hair_gray = (175, 180, 190, 255)
    c_hair_white = (215, 220, 230, 255)
    c_hair_dark = (120, 125, 135, 255)
    c_eye_brown = (70, 45, 25, 255)
    c_eye_white = (245, 245, 250, 255)

    # Island Palm Shirt (tropical blue and gold/white floral accents)
    c_shirt_blue = (40, 110, 180, 255)
    c_shirt_dark = (25, 75, 130, 255)
    c_shirt_pattern = (240, 215, 80, 255)
    c_khaki = (190, 175, 140, 255)
    c_khaki_dark = (160, 145, 110, 255)
    c_shoes = (50, 40, 35, 255)

    # 1. Head (8, 0 to 32, 16)
    # Head Top: gray/white hair
    for x in range(8, 16):
        for y in range(0, 8):
            p[x, y] = c_hair_white if (x + y) % 2 == 0 else c_hair_gray
    for x in range(16, 24):
        for y in range(0, 8):
            p[x, y] = c_skin_shadow

    # Head Front (8..16, 8..16)
    for x in range(8, 16):
        for y in range(8, 16):
            p[x, y] = c_skin
    # Gray hair line
    for x in range(8, 16):
        p[x, 8] = c_hair_gray
    p[9, 9] = c_hair_gray
    p[14, 9] = c_hair_gray
    # Eyes
    p[10, 12] = c_eye_brown
    p[9, 12] = c_eye_white
    p[13, 12] = c_eye_brown
    p[14, 12] = c_eye_white
    # Nose & Mouth
    p[11, 13] = c_skin_shadow
    p[12, 13] = c_skin_shadow
    p[11, 15] = (190, 110, 95, 255)
    p[12, 15] = (190, 110, 95, 255)

    # Head Sides and Back (Hair)
    for x in range(0, 8):
        for y in range(8, 16):
            p[x, y] = c_hair_gray if y < 13 else c_skin
    for x in range(16, 24):
        for y in range(8, 16):
            p[x, y] = c_hair_gray if y < 13 else c_skin
    for x in range(24, 32):
        for y in range(8, 16):
            p[x, y] = c_hair_dark if y > 12 else c_hair_gray

    # 2. Torso (16..40, 16..32): Tropical Shirt
    for x in range(20, 28):
        for y in range(20, 32):
            if (x * 3 + y * 7) % 5 == 0:
                p[x, y] = c_shirt_pattern
            else:
                p[x, y] = c_shirt_blue if (x + y) % 2 == 0 else c_shirt_dark

    # Back torso
    for x in range(32, 40):
        for y in range(20, 32):
            p[x, y] = c_shirt_blue if (x + y) % 2 == 0 else c_shirt_dark

    # 3. Arms (Tropical short sleeves, skin arms)
    # Right arm (40..56, 16..32)
    for x in range(40, 56):
        for y in range(16, 32):
            p[x, y] = c_shirt_blue if y < 24 else c_skin
    # Left arm (32..48, 48..64)
    for x in range(32, 48):
        for y in range(48, 64):
            p[x, y] = c_shirt_blue if y < 56 else c_skin

    # 4. Legs (Khaki shorts + shoes)
    # Right leg (0..16, 16..32)
    for x in range(0, 16):
        for y in range(16, 32):
            if y < 25:
                p[x, y] = c_khaki
            elif y < 29:
                p[x, y] = c_skin
            else:
                p[x, y] = c_shoes
    # Left leg (16..32, 48..64)
    for x in range(16, 32):
        for y in range(48, 64):
            if y < 57:
                p[x, y] = c_khaki
            elif y < 61:
                p[x, y] = c_skin
            else:
                p[x, y] = c_shoes

    return img

def generate_child_skin():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    p = img.load()

    # Kid Colors
    c_skin = (245, 200, 165, 255)
    c_skin_shadow = (215, 170, 135, 255)
    c_hair = (90, 50, 25, 255)
    c_hair_light = (130, 80, 45, 255)
    c_eye_blue = (40, 120, 220, 255)
    c_eye_white = (255, 255, 255, 255)

    # Colorful kid striped shirt (Red & Yellow stripes)
    c_stripe_red = (225, 45, 55, 255)
    c_stripe_yellow = (245, 200, 35, 255)
    c_jeans = (45, 85, 155, 255)
    c_sneakers = (240, 240, 245, 255)
    c_sneakers_red = (220, 30, 40, 255)

    # 1. Head (Young face, messy brown hair)
    for x in range(8, 16):
        for y in range(0, 8):
            p[x, y] = c_hair_light if (x + y) % 3 == 0 else c_hair
    for x in range(16, 24):
        for y in range(0, 8):
            p[x, y] = c_skin_shadow

    # Face front
    for x in range(8, 16):
        for y in range(8, 16):
            p[x, y] = c_skin
    # Messy kid bangs
    for x in range(8, 16):
        p[x, 8] = c_hair
    p[9, 9] = c_hair
    p[10, 9] = c_hair_light
    p[13, 9] = c_hair
    # Big child eyes
    p[9, 11] = c_eye_white
    p[10, 11] = c_eye_blue
    p[13, 11] = c_eye_blue
    p[14, 11] = c_eye_white
    # Rosy kid cheeks & smile
    p[9, 13] = (240, 140, 130, 255)
    p[14, 13] = (240, 140, 130, 255)
    p[11, 14] = (210, 90, 85, 255)
    p[12, 14] = (210, 90, 85, 255)

    # Sides & Back Hair
    for x in range(0, 8):
        for y in range(8, 16):
            p[x, y] = c_hair if y < 13 else c_skin
    for x in range(16, 24):
        for y in range(8, 16):
            p[x, y] = c_hair if y < 13 else c_skin
    for x in range(24, 32):
        for y in range(8, 16):
            p[x, y] = c_hair

    # 2. Torso (Striped T-shirt)
    for x in range(20, 28):
        for y in range(20, 32):
            p[x, y] = c_stripe_red if (y // 2) % 2 == 0 else c_stripe_yellow
    for x in range(32, 40):
        for y in range(20, 32):
            p[x, y] = c_stripe_red if (y // 2) % 2 == 0 else c_stripe_yellow

    # 3. Arms (Short sleeves)
    for x in range(40, 56):
        for y in range(16, 32):
            p[x, y] = c_stripe_red if y < 22 else c_skin
    for x in range(32, 48):
        for y in range(48, 64):
            p[x, y] = c_stripe_red if y < 54 else c_skin

    # 4. Legs (Blue denim jeans + sneakers)
    for x in range(0, 16):
        for y in range(16, 32):
            p[x, y] = c_jeans if y < 28 else (c_sneakers_red if y == 28 else c_sneakers)
    for x in range(16, 32):
        for y in range(48, 64):
            p[x, y] = c_jeans if y < 60 else (c_sneakers_red if y == 60 else c_sneakers)

    return img

def main():
    base_dir = "src/main/resources/assets/israel_simulator/textures/entity"
    os.makedirs(base_dir, exist_ok=True)

    epstein = generate_epstein_skin()
    epstein.save(os.path.join(base_dir, "jeffrey_epstein.png"), "PNG")
    print("Generated jeffrey_epstein.png")

    child = generate_child_skin()
    child.save(os.path.join(base_dir, "child_zombie.png"), "PNG")
    print("Generated child_zombie.png")

if __name__ == "__main__":
    main()

