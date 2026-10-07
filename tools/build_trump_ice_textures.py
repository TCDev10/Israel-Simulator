import os
from PIL import Image

def generate_trump_skin():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    p = img.load()

    # Colors
    c_skin = (235, 175, 130, 255)
    c_skin_shadow = (210, 150, 105, 255)
    c_skin_highlight = (248, 195, 155, 255)
    c_hair_light = (254, 240, 138, 255)
    c_hair_mid = (250, 204, 21, 255)
    c_hair_dark = (202, 138, 4, 255)
    c_eye_blue = (59, 130, 246, 255)
    c_eye_white = (245, 245, 250, 255)
    c_suit_dark = (15, 23, 42, 255)
    c_suit_mid = (30, 41, 59, 255)
    c_suit_light = (51, 65, 85, 255)
    c_shirt = (248, 250, 252, 255)
    c_shirt_shadow = (203, 213, 225, 255)
    c_tie_red = (220, 38, 38, 255)
    c_tie_dark = (185, 28, 28, 255)
    c_tie_spec = (239, 68, 68, 255)
    c_shoe = (20, 20, 25, 255)

    # 1. Head (8, 0 to 32, 16)
    # Head Top (8..16, 0..8): blonde swept hair
    for x in range(8, 16):
        for y in range(0, 8):
            p[x, y] = c_hair_mid if (x + y) % 2 == 0 else c_hair_light
    # Head Bottom (16..24, 0..8): neck / under jaw
    for x in range(16, 24):
        for y in range(0, 8):
            p[x, y] = c_skin_shadow

    # Head Right (0..8, 8..16): hair on top, ear/skin below
    for x in range(0, 8):
        for y in range(8, 16):
            p[x, y] = c_hair_mid if y < 12 else c_skin

    # Head Front (8..16, 8..16): face
    for x in range(8, 16):
        for y in range(8, 16):
            p[x, y] = c_skin
    # Hairline on forehead
    for x in range(8, 16):
        p[x, 8] = c_hair_mid
    p[9, 9] = c_hair_light
    p[10, 9] = c_hair_light
    p[11, 9] = c_hair_mid
    p[12, 9] = c_hair_mid
    p[13, 9] = c_hair_dark
    # Eyes (y: 12)
    p[9, 12] = c_eye_white
    p[10, 12] = c_eye_blue
    p[13, 12] = c_eye_blue
    p[14, 12] = c_eye_white
    # Nose & Mouth
    p[11, 13] = c_skin_shadow
    p[12, 13] = c_skin_shadow
    p[11, 15] = (210, 130, 110, 255)
    p[12, 15] = (210, 130, 110, 255)

    # Head Left (16..24, 8..16): hair and skin
    for x in range(16, 24):
        for y in range(8, 16):
            p[x, y] = c_hair_mid if y < 12 else c_skin

    # Head Back (24..32, 8..16): blonde hair
    for x in range(24, 32):
        for y in range(8, 16):
            p[x, y] = c_hair_dark if y > 13 else c_hair_mid

    # Hair Hat Layer (32..64, 0..16) for voluminous hairstyle
    for x in range(32 + 8, 32 + 16):
        for y in range(0, 8):
            p[x, y] = c_hair_light if (x + y) % 3 == 0 else c_hair_mid
    # Front fringe bangs
    for x in range(32 + 8, 32 + 16):
        p[x, 8] = c_hair_light
        p[x, 9] = c_hair_mid if x % 2 == 0 else c_hair_light
    p[32 + 9, 10] = c_hair_light
    p[32 + 10, 10] = c_hair_mid

    # 2. Torso (16..40, 16..32)
    # Front (20..28, 20..32): Navy Suit + White Shirt + Long Red Tie
    for x in range(20, 28):
        for y in range(20, 32):
            p[x, y] = c_suit_mid
    # White dress shirt V-neck
    p[23, 20] = c_shirt
    p[24, 20] = c_shirt
    p[23, 21] = c_shirt
    p[24, 21] = c_shirt
    p[22, 21] = c_shirt_shadow
    p[25, 21] = c_shirt_shadow

    # Iconic Long Red Tie (y: 21 to 31!)
    for y in range(21, 32):
        p[23, y] = c_tie_red
        p[24, y] = c_tie_spec if y % 3 == 0 else c_tie_red
    p[23, 21] = c_tie_dark # tie knot
    p[24, 21] = c_tie_dark

    # Suit lapels
    for y in range(20, 26):
        p[21, y] = c_suit_light
        p[26, y] = c_suit_light

    # Torso Back (32..40, 20..32): Navy suit back
    for x in range(32, 40):
        for y in range(20, 32):
            p[x, y] = c_suit_dark if (x + y) % 2 == 0 else c_suit_mid

    # Torso Top (20..28, 16..20): shoulders
    for x in range(20, 28):
        for y in range(16, 20):
            p[x, y] = c_suit_mid
    # Torso Bottom (28..36, 16..20): waist/belt
    for x in range(28, 36):
        for y in range(16, 20):
            p[x, y] = (25, 25, 30, 255)

    # 3. Arms (40..56, 16..32) and (32..48, 48..64)
    # Right Arm
    for x in range(40, 56):
        for y in range(16, 32):
            p[x, y] = c_suit_mid if y < 28 else (c_shirt if y == 28 else c_skin)
    # Left Arm
    for x in range(32, 48):
        for y in range(48, 64):
            p[x, y] = c_suit_mid if y < 60 else (c_shirt if y == 60 else c_skin)

    # 4. Legs (0..16, 16..32) and (16..32, 48..64)
    # Right Leg
    for x in range(0, 16):
        for y in range(16, 32):
            p[x, y] = c_suit_dark if y < 29 else c_shoe
    # Left Leg
    for x in range(16, 32):
        for y in range(48, 64):
            p[x, y] = c_suit_dark if y < 61 else c_shoe

    return img

def generate_ice_agent_skin():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    p = img.load()

    # Tactical Colors
    c_skin = (215, 175, 140, 255)
    c_hair = (30, 25, 25, 255)
    c_shades_frame = (15, 15, 15, 255)
    c_shades_lens = (30, 45, 65, 255)
    c_vest_dark = (18, 24, 38, 255)
    c_vest_mid = (30, 41, 59, 255)
    c_vest_light = (51, 65, 85, 255)
    c_badge_gold = (234, 179, 8, 255)
    c_badge_gold_dark = (161, 98, 7, 255)
    c_ice_yellow = (250, 204, 21, 255)
    c_camo_dark = (15, 20, 28, 255)
    c_boot = (20, 20, 24, 255)
    c_glove = (25, 28, 35, 255)

    # 1. Head (8..32, 0..16)
    # Top & sides: dark tactical crew cut
    for x in range(8, 16):
        for y in range(0, 8):
            p[x, y] = c_hair
    for x in range(0, 8):
        for y in range(8, 16):
            p[x, y] = c_hair if y < 11 else c_skin
    for x in range(16, 24):
        for y in range(8, 16):
            p[x, y] = c_hair if y < 11 else c_skin
    for x in range(24, 32):
        for y in range(8, 16):
            p[x, y] = c_hair

    # Face (8..16, 8..16)
    for x in range(8, 16):
        for y in range(8, 16):
            p[x, y] = c_skin
    # Tactical dark sunglasses across eyes (y: 11..12)
    for x in range(9, 15):
        p[x, 11] = c_shades_frame
        p[x, 12] = c_shades_lens
    p[8, 11] = c_shades_frame
    p[15, 11] = c_shades_frame

    # 2. Torso (Tactical Plate Carrier with "ICE" and Federal Badge)
    # Front (20..28, 20..32)
    for x in range(20, 28):
        for y in range(20, 32):
            p[x, y] = c_vest_mid
    # Collar
    p[23, 20] = (20, 30, 45, 255)
    p[24, 20] = (20, 30, 45, 255)

    # Tactical Federal Badge on chest (left breast, x: 25, 26; y: 22, 23)
    p[25, 22] = c_badge_gold
    p[26, 22] = c_badge_gold
    p[25, 23] = c_badge_gold_dark
    p[26, 23] = c_badge_gold_dark

    # Chest "ICE" lettering across front (y: 25..27)
    # I
    p[21, 25] = c_ice_yellow
    p[21, 26] = c_ice_yellow
    p[21, 27] = c_ice_yellow
    # C
    p[23, 25] = c_ice_yellow
    p[24, 25] = c_ice_yellow
    p[23, 26] = c_ice_yellow
    p[23, 27] = c_ice_yellow
    p[24, 27] = c_ice_yellow
    # E
    p[26, 25] = c_ice_yellow
    p[27, 25] = c_ice_yellow
    p[26, 26] = c_ice_yellow
    p[26, 27] = c_ice_yellow
    p[27, 27] = c_ice_yellow

    # Utility belt & pouches (y: 30..31)
    for x in range(20, 28):
        p[x, 30] = (25, 25, 30, 255)
        p[x, 31] = (15, 15, 20, 255)
    p[22, 30] = (60, 65, 75, 255) # buckle/pouch
    p[25, 30] = (60, 65, 75, 255)

    # Back (32..40, 20..32): Big "ICE" on back
    for x in range(32, 40):
        for y in range(20, 32):
            p[x, y] = c_vest_dark
    # Large ICE on back
    # I (x: 33)
    p[33, 23] = c_ice_yellow
    p[33, 24] = c_ice_yellow
    p[33, 25] = c_ice_yellow
    # C (x: 35, 36)
    p[35, 23] = c_ice_yellow
    p[36, 23] = c_ice_yellow
    p[35, 24] = c_ice_yellow
    p[35, 25] = c_ice_yellow
    p[36, 25] = c_ice_yellow
    # E (x: 38, 39)
    p[38, 23] = c_ice_yellow
    p[39, 23] = c_ice_yellow
    p[38, 24] = c_ice_yellow
    p[38, 25] = c_ice_yellow
    p[39, 25] = c_ice_yellow

    # 3. Arms: Tactical sleeves and combat gloves
    for x in range(40, 56):
        for y in range(16, 32):
            p[x, y] = c_vest_mid if y < 28 else c_glove
    for x in range(32, 48):
        for y in range(48, 64):
            p[x, y] = c_vest_mid if y < 60 else c_glove

    # 4. Legs: Tactical cargo pants and combat boots
    for x in range(0, 16):
        for y in range(16, 32):
            p[x, y] = c_camo_dark if y < 28 else c_boot
    for x in range(16, 32):
        for y in range(48, 64):
            p[x, y] = c_camo_dark if y < 60 else c_boot

    return img

def main():
    target_dir = os.path.join("src", "main", "resources", "assets", "israel_simulator", "textures", "entity")
    os.makedirs(target_dir, exist_ok=True)

    trump_img = generate_trump_skin()
    trump_path = os.path.join(target_dir, "trump_miniboss.png")
    trump_img.save(trump_path, "PNG")
    print(f"Saved {trump_path} ({trump_img.size})")

    ice_img = generate_ice_agent_skin()
    ice_path = os.path.join(target_dir, "ice_agent.png")
    ice_img.save(ice_path, "PNG")
    print(f"Saved {ice_path} ({ice_img.size})")

if __name__ == "__main__":
    main()

