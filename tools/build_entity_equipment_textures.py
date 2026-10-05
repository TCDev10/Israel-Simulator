import os
from PIL import Image

def generate_kippah_equipment():
    # 64x32 armor texture
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    # Head Top: (8, 0) to (16, 8)
    # Woven blue & white kippah with Star of David
    top_pixels = {
        (10, 1): (20, 45, 95, 255), (11, 1): (20, 45, 95, 255), (12, 1): (20, 45, 95, 255), (13, 1): (20, 45, 95, 255),
        (9, 2): (20, 45, 95, 255), (10, 2): (45, 90, 180, 255), (11, 2): (240, 245, 255, 255), (12, 2): (240, 245, 255, 255), (13, 2): (45, 90, 180, 255), (14, 2): (20, 45, 95, 255),
        (9, 3): (20, 45, 95, 255), (10, 3): (240, 245, 255, 255), (11, 3): (30, 70, 150, 255), (12, 3): (30, 70, 150, 255), (13, 3): (240, 245, 255, 255), (14, 3): (20, 45, 95, 255),
        (9, 4): (20, 45, 95, 255), (10, 4): (240, 245, 255, 255), (11, 4): (30, 70, 150, 255), (12, 4): (30, 70, 150, 255), (13, 4): (240, 245, 255, 255), (14, 4): (20, 45, 95, 255),
        (9, 5): (20, 45, 95, 255), (10, 5): (45, 90, 180, 255), (11, 2): (240, 245, 255, 255), (11, 5): (240, 245, 255, 255), (12, 5): (240, 245, 255, 255), (13, 5): (45, 90, 180, 255), (14, 5): (20, 45, 95, 255),
        (10, 6): (20, 45, 95, 255), (11, 6): (20, 45, 95, 255), (12, 6): (20, 45, 95, 255), (13, 6): (20, 45, 95, 255),
    }
    for pos, col in top_pixels.items():
        img.putpixel(pos, col)
    # Slight edge visible on sides row 8
    for x in range(10, 14):
        img.putpixel((x, 8), (20, 45, 95, 255))
        img.putpixel((x + 16, 8), (20, 45, 95, 255))
    return img

def generate_rabbis_crown_equipment():
    # 64x32 armor texture
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    # Head top velvet scarlet cap: (8, 0) to (16, 8)
    for y in range(1, 7):
        for x in range(9, 15):
            img.putpixel((x, y), (140, 20, 40, 255))
    img.putpixel((11, 3), (255, 215, 0, 255))
    img.putpixel((12, 3), (255, 215, 0, 255))
    img.putpixel((11, 4), (255, 215, 0, 255))
    img.putpixel((12, 4), (255, 215, 0, 255))
    # Gold crown perimeter across Front(8..16), Left(16..24), Back(24..32), Right(0..8) on rows 6..10
    gold_dark = (160, 120, 20, 255)
    gold_mid = (225, 180, 30, 255)
    gold_hi = (255, 235, 100, 255)
    ruby = (230, 30, 50, 255)
    sapphire = (30, 90, 240, 255)
    for x in range(32):
        # Base ring
        img.putpixel((x, 9), gold_dark)
        img.putpixel((x, 8), gold_mid)
        # Crown arches/points
        if x % 4 == 0:
            img.putpixel((x, 7), gold_mid)
            img.putpixel((x, 6), gold_hi)
        elif x % 4 == 2:
            img.putpixel((x, 7), gold_mid)
        # Gemstones on row 8
        if x % 4 == 0:
            img.putpixel((x, 8), ruby)
        elif x % 4 == 2:
            img.putpixel((x, 8), sapphire)
    return img

def generate_tefillin_equipment():
    # 64x32 armor texture
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    # Forehead box (bayit) on Front head: (8..16, 8..16), specifically at (11, 8) to (13, 10)
    leather_black = (20, 20, 22, 255)
    leather_sheen = (60, 62, 70, 255)
    gold_shin = (235, 195, 60, 255)
    for y in range(7, 10):
        for x in range(11, 14):
            img.putpixel((x, y), leather_black)
    img.putpixel((12, 7), leather_sheen)
    img.putpixel((12, 8), gold_shin)
    # Head strap around brow: row 9 across all sides
    for x in range(32):
        img.putpixel((x, 9), leather_black)
        if x % 2 == 0:
            img.putpixel((x, 9), leather_sheen)
    # Arm strap windings on right arm (40..48, 16..32)
    for y in range(18, 30, 2):
        for x in range(40, 48):
            img.putpixel((x, y), leather_black)
            if (x + y) % 2 == 0:
                img.putpixel((x, y), leather_sheen)
    return img

def generate_talit_equipment():
    # 64x32 armor texture
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    # Body torso: (16..40, 16..32)
    wool_white = (248, 250, 255, 255)
    wool_shade = (215, 220, 230, 255)
    tekhelet = (30, 80, 185, 255)
    navy = (15, 40, 95, 255)
    gold_atarah = (230, 185, 45, 255)
    # Shoulders top (20..28, 16..20)
    for y in range(16, 20):
        for x in range(20, 28):
            img.putpixel((x, y), gold_atarah if y == 16 else wool_white)
    # Front torso (20..28, 20..32)
    for y in range(20, 31):
        for x in range(20, 28):
            if y in (23, 24, 27, 28):
                img.putpixel((x, y), tekhelet if y in (23, 27) else navy)
            else:
                img.putpixel((x, y), wool_white if (x + y) % 3 != 0 else wool_shade)
    # Back torso (32..40, 20..32)
    for y in range(20, 31):
        for x in range(32, 40):
            if y in (23, 24, 27, 28):
                img.putpixel((x, y), tekhelet if y in (23, 27) else navy)
            else:
                img.putpixel((x, y), wool_white if (x + y) % 3 != 0 else wool_shade)
    # Right side (16..20) and Left side (28..32)
    for y in range(20, 31):
        for x in range(16, 20):
            img.putpixel((x, y), wool_white if y not in (23, 27) else tekhelet)
        for x in range(28, 32):
            img.putpixel((x, y), wool_white if y not in (23, 27) else tekhelet)
    # Tzitzit fringe knots at bottom hem corners
    img.putpixel((20, 31), (255, 255, 255, 255))
    img.putpixel((27, 31), (255, 255, 255, 255))
    img.putpixel((32, 31), (255, 255, 255, 255))
    img.putpixel((39, 31), (255, 255, 255, 255))
    return img

def generate_bibi_guard_skin():
    # 64x64 complete professional bodyguard skin
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    skin_shadow = (200, 155, 125, 255)
    skin_mid = (235, 195, 160, 255)
    skin_light = (245, 215, 185, 255)
    hair_dark = (35, 30, 30, 255)
    hair_grey = (80, 80, 85, 255)
    glasses_frame = (20, 20, 20, 255)
    glasses_lens = (30, 45, 75, 255)
    glasses_glare = (140, 180, 230, 255)
    suit_dark = (18, 20, 24, 255)
    suit_mid = (32, 35, 42, 255)
    suit_light = (50, 55, 65, 255)
    shirt_white = (245, 248, 252, 255)
    shirt_shadow = (205, 212, 220, 255)
    tie_blue = (25, 80, 175, 255)
    tie_light = (45, 110, 225, 255)
    tie_clip = (235, 195, 60, 255)
    shoe_leather = (12, 12, 14, 255)
    shoe_sole = (30, 30, 35, 255)
    earpiece_wire = (210, 210, 215, 255)

    # 1. HEAD (0..32, 0..16)
    # Head top (8..16, 0..8): Dark combed hair with slight salt-and-pepper
    for y in range(0, 8):
        for x in range(8, 16):
            img.putpixel((x, y), hair_dark if (x + y) % 5 != 0 else hair_grey)
    # Head Front (8..16, 8..16)
    # Hairline row 8
    for x in range(8, 16):
        img.putpixel((x, 8), hair_dark if x in (8, 15) else hair_grey)
    # Forehead row 9
    for x in range(8, 16):
        img.putpixel((x, 9), skin_light if 9 <= x <= 14 else hair_dark)
    # Sunglasses row 10 & 11
    for x in range(8, 16):
        if x in (9, 10, 13, 14):
            img.putpixel((x, 10), glasses_glare if x in (10, 14) else glasses_lens)
            img.putpixel((x, 11), glasses_lens)
        else:
            img.putpixel((x, 10), glasses_frame)
            img.putpixel((x, 11), skin_mid)
    # Cheeks & mouth rows 12..15
    for y in range(12, 16):
        for x in range(8, 16):
            if y == 14 and x in (11, 12):
                img.putpixel((x, y), skin_shadow) # mouth
            else:
                img.putpixel((x, y), skin_mid if x in (10, 11, 12, 13) else skin_shadow)

    # Head Sides (Right 0..8, Left 16..24)
    for y in range(8, 16):
        for x in range(0, 8):
            img.putpixel((x, y), hair_dark if y <= 11 else skin_mid)
        for x in range(16, 24):
            img.putpixel((x, y), hair_dark if y <= 11 else skin_mid)
    # Earpiece curly cord on Left ear (19, 12..15)
    img.putpixel((19, 12), earpiece_wire)
    img.putpixel((20, 13), earpiece_wire)
    img.putpixel((19, 14), earpiece_wire)
    img.putpixel((20, 15), earpiece_wire)

    # Head Back (24..32, 8..16)
    for y in range(8, 16):
        for x in range(24, 32):
            img.putpixel((x, y), hair_dark if y <= 12 else skin_shadow)
    # Cord down back of neck
    img.putpixel((25, 14), earpiece_wire)
    img.putpixel((25, 15), earpiece_wire)

    # 2. TORSO (16..40, 16..32)
    # Front Torso (20..28, 20..32): Charcoal suit jacket, lapels, shirt, blue tie
    for y in range(20, 32):
        for x in range(20, 28):
            # Suit base
            img.putpixel((x, y), suit_mid if x in (20, 27) else suit_dark)
    # White collar & V-neck on rows 20..24
    img.putpixel((23, 20), shirt_white)
    img.putpixel((24, 20), shirt_white)
    img.putpixel((22, 21), suit_light) # lapel
    img.putpixel((23, 21), tie_blue)
    img.putpixel((24, 21), tie_light)
    img.putpixel((25, 21), suit_light) # lapel
    img.putpixel((22, 22), suit_light)
    img.putpixel((23, 22), tie_blue)
    img.putpixel((24, 22), tie_light)
    img.putpixel((25, 22), suit_light)
    # Tie clip on row 23
    img.putpixel((23, 23), tie_clip)
    img.putpixel((24, 23), tie_clip)
    img.putpixel((23, 24), tie_blue)
    img.putpixel((24, 24), tie_light)
    # Suit button on row 26
    img.putpixel((23, 26), suit_light)
    img.putpixel((24, 26), suit_light)

    # Back Torso (32..40, 20..32)
    for y in range(20, 32):
        for x in range(32, 40):
            img.putpixel((x, y), suit_mid if x in (32, 39) else suit_dark)
    # Shoulders top (20..28, 16..20)
    for y in range(16, 20):
        for x in range(20, 28):
            img.putpixel((x, y), suit_mid)

    # 3. ARMS
    # Right arm (40..56, 16..32)
    for y in range(16, 30):
        for x in range(40, 56):
            img.putpixel((x, y), suit_dark if (x + y) % 4 != 0 else suit_mid)
    # White shirt cuff & hands at bottom of arm
    for y in range(30, 32):
        for x in range(40, 56):
            img.putpixel((x, y), shirt_white if y == 30 else skin_mid)
    # Wristwatch on right wrist (42, 29)
    img.putpixel((42, 29), (18, 18, 20, 255))
    img.putpixel((43, 29), (220, 180, 40, 255))

    # Left arm (32..48, 48..64)
    for y in range(48, 62):
        for x in range(32, 48):
            img.putpixel((x, y), suit_dark if (x + y) % 4 != 0 else suit_mid)
    for y in range(62, 64):
        for x in range(32, 48):
            img.putpixel((x, y), shirt_white if y == 62 else skin_mid)

    # 4. LEGS
    # Right leg (0..16, 16..32)
    for y in range(16, 28):
        for x in range(0, 16):
            img.putpixel((x, y), suit_dark if (x + y) % 3 != 0 else suit_mid)
    # Shoes on rows 28..32
    for y in range(28, 32):
        for x in range(0, 16):
            img.putpixel((x, y), shoe_leather if y < 31 else shoe_sole)

    # Left leg (16..32, 48..64)
    for y in range(48, 60):
        for x in range(16, 32):
            img.putpixel((x, y), suit_dark if (x + y) % 3 != 0 else suit_mid)
    for y in range(60, 64):
        for x in range(16, 32):
            img.putpixel((x, y), shoe_leather if y < 63 else shoe_sole)

    return img

def main():
    base_dir = os.path.abspath("src/main/resources/assets/israel_simulator/textures/entity")
    
    # 1. Bibi Guard (64x64)
    guard_path = os.path.join(base_dir, "bibi_guard.png")
    guard_img = generate_bibi_guard_skin()
    guard_img.save(guard_path, "PNG")
    print(f"Generated bibi_guard.png ({guard_img.size}) with {len(set(guard_img.get_flattened_data()))} colors")

    # 2. Equipment layers (humanoid & humanoid_baby)
    equipment = {
        "kippah": generate_kippah_equipment(),
        "rabbis_crown": generate_rabbis_crown_equipment(),
        "tefillin": generate_tefillin_equipment(),
        "talit": generate_talit_equipment(),
    }

    for folder in ["humanoid", "humanoid_baby"]:
        target = os.path.join(base_dir, "equipment", folder)
        os.makedirs(target, exist_ok=True)
        for name, img in equipment.items():
            path = os.path.join(target, f"{name}.png")
            img.save(path, "PNG")
            print(f"  [OK] equipment/{folder}/{name}.png ({img.size}) -> {len(set(img.get_flattened_data()))} colors")

if __name__ == "__main__":
    main()
