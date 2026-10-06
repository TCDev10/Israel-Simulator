import os
from PIL import Image, ImageDraw

def generate_kippah():
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Shading and stitch colors
    c_rim = (15, 30, 65, 255)
    c_blue_dark = (25, 55, 125, 255)
    c_blue_mid = (45, 95, 200, 255)
    c_blue_light = (85, 145, 245, 255)
    c_wool_shadow = (195, 200, 210, 255)
    c_wool_mid = (228, 232, 240, 255)
    c_wool_light = (252, 253, 255, 255)

    # Base dome (circle from 4,4 to 27,27)
    draw.ellipse([4, 4, 27, 27], fill=c_wool_mid, outline=c_rim)
    draw.ellipse([5, 5, 26, 26], fill=c_wool_light, outline=c_blue_dark)

    # Knitted concentric pattern
    for r in range(7, 13):
        for deg in range(0, 360, 30):
            import math
            rad = math.radians(deg)
            x = int(15.5 + r * math.cos(rad))
            y = int(15.5 + r * math.sin(rad))
            if 0 <= x < 32 and 0 <= y < 32:
                img.putpixel((x, y), c_wool_shadow if (x + y) % 2 == 0 else c_blue_mid)

    # Star of David in center (11..20, 11..20)
    # Triangle 1
    t1 = [(15, 10), (10, 19), (21, 19)]
    # Triangle 2
    t2 = [(15, 21), (10, 12), (21, 12)]
    draw.line([t1[0], t1[1], t1[2], t1[0]], fill=c_blue_dark, width=1)
    draw.line([t2[0], t2[1], t2[2], t2[0]], fill=c_blue_dark, width=1)
    draw.point([(15, 10), (10, 19), (21, 19), (15, 21), (10, 12), (21, 12)], fill=c_blue_light)
    img.putpixel((15, 15), c_blue_mid)

    return img

def generate_rabbis_crown():
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Palette
    c_hat_dark = (15, 15, 20, 255)
    c_hat_mid = (30, 30, 40, 255)
    c_hat_light = (55, 55, 70, 255)
    c_gold_dark = (180, 130, 20, 255)
    c_gold_mid = (240, 195, 45, 255)
    c_gold_light = (255, 235, 120, 255)
    c_ruby = (220, 25, 40, 255)
    c_sapphire = (30, 100, 230, 255)
    c_hair = (45, 30, 20, 255)
    c_hair_light = (85, 60, 40, 255)
    c_beard_shadow = (185, 190, 200, 255)
    c_beard_mid = (220, 225, 235, 255)
    c_beard_light = (250, 252, 255, 255)

    # 1. Wide brim (y: 12..14, x: 2..29)
    draw.rounded_rectangle([3, 12, 28, 14], radius=1, fill=c_hat_dark, outline=c_hat_light)

    # 2. Tall velvet crown dome (y: 2..11, x: 7..24)
    draw.rounded_rectangle([7, 2, 24, 11], radius=3, fill=c_hat_mid, outline=c_hat_dark)
    draw.line([(8, 4), (23, 4)], fill=c_hat_light)

    # 3. Gold Filigree band with jewels (y: 9..11, x: 7..24)
    draw.rectangle([7, 9, 24, 11], fill=c_gold_mid, outline=c_gold_dark)
    # Jewels
    img.putpixel((9, 10), c_ruby)
    img.putpixel((12, 10), c_sapphire)
    img.putpixel((15, 10), c_gold_light)
    img.putpixel((19, 10), c_sapphire)
    img.putpixel((22, 10), c_ruby)

    # 4. Payot (sidecurls) on sides (y: 13..23)
    # Left payot (x: 4..6)
    for y in range(14, 23):
        x = 5 + (1 if (y % 4 < 2) else -1)
        img.putpixel((x, y), c_hair)
        img.putpixel((x + 1, y), c_hair_light)

    # Right payot (x: 25..27)
    for y in range(14, 23):
        x = 26 + (1 if (y % 4 < 2) else -1)
        img.putpixel((x, y), c_hair_light)
        img.putpixel((x - 1, y), c_hair)

    # 5. Flowing sage beard (y: 15..30, x: 9..22)
    draw.polygon([(9, 15), (22, 15), (19, 28), (16, 31), (12, 28)], fill=c_beard_mid, outline=c_beard_shadow)
    for y in range(16, 29):
        for x in range(11, 21):
            if (x + y) % 3 == 0:
                img.putpixel((x, y), c_beard_light)
            elif (x + y) % 5 == 0:
                img.putpixel((x, y), c_beard_shadow)

    return img

def generate_talit():
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    c_wool_dark = (180, 185, 195, 255)
    c_wool_mid = (225, 230, 240, 255)
    c_wool_light = (252, 254, 255, 255)
    c_navy = (16, 32, 75, 255)
    c_blue = (38, 85, 180, 255)
    c_light_blue = (75, 135, 235, 255)
    c_gold = (230, 185, 55, 255)
    c_gold_light = (255, 225, 110, 255)

    # Folded rectangular shawl body (y: 5..25, x: 4..27)
    draw.rounded_rectangle([4, 5, 27, 25], radius=2, fill=c_wool_mid, outline=c_wool_dark)
    draw.rectangle([6, 7, 25, 23], fill=c_wool_light)

    # Atarah (neckband collar with gold embroidery) at top (y: 5..8, x: 7..24)
    draw.rectangle([7, 5, 24, 8], fill=c_gold, outline=c_navy)
    for x in range(8, 24, 2):
        img.putpixel((x, 6), c_gold_light)

    # Ceremonial blue striping (y: 12..14 and 18..20)
    draw.rectangle([5, 12, 26, 13], fill=c_navy)
    draw.rectangle([5, 14, 26, 14], fill=c_blue)
    draw.rectangle([5, 18, 26, 18], fill=c_blue)
    draw.rectangle([5, 19, 26, 20], fill=c_navy)
    draw.line([(5, 13), (26, 13)], fill=c_light_blue)

    # Tzitzit tassels at corners (corners: (4, 25), (27, 25), (4, 5), (27, 5))
    # Bottom left tassel
    draw.line([(4, 25), (2, 29)], fill=c_wool_dark, width=1)
    draw.point([(1, 30), (2, 31), (3, 30)], fill=c_wool_light)
    # Bottom right tassel
    draw.line([(27, 25), (29, 29)], fill=c_wool_dark, width=1)
    draw.point([(30, 30), (29, 31), (28, 30)], fill=c_wool_light)

    return img

def generate_tefillin():
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    c_leather_dark = (12, 12, 16, 255)
    c_leather_mid = (28, 28, 36, 255)
    c_leather_light = (58, 58, 72, 255)
    c_shin = (210, 215, 230, 255)
    c_strap = (18, 18, 24, 255)
    c_strap_light = (45, 45, 58, 255)

    # Ma'abarta base (y: 18..22, x: 7..24)
    draw.rounded_rectangle([7, 18, 24, 22], radius=1, fill=c_leather_dark, outline=c_leather_light)

    # Bayit box (cube y: 7..18, x: 9..22)
    draw.rectangle([9, 7, 22, 18], fill=c_leather_mid, outline=c_leather_dark)
    draw.line([(10, 8), (21, 8)], fill=c_leather_light)
    draw.line([(10, 8), (10, 17)], fill=c_leather_light)

    # Embossed Hebrew character 'Shin' (ש) on box face (y: 10..15, x: 13..18)
    shin_pts = [
        (13, 11), (13, 14),
        (15, 12), (15, 14),
        (18, 11), (18, 14),
        (13, 14), (14, 15), (17, 15), (18, 14)
    ]
    for pt in shin_pts:
        img.putpixel(pt, c_shin)

    # Wound leather straps (retzuot) looping out and under
    # Left loop
    draw.arc([2, 16, 12, 28], 90, 270, fill=c_strap, width=2)
    draw.arc([3, 17, 11, 27], 90, 270, fill=c_strap_light, width=1)
    # Right wound coils
    draw.arc([19, 16, 29, 28], 270, 90, fill=c_strap, width=2)
    draw.arc([20, 17, 28, 27], 270, 90, fill=c_strap_light, width=1)
    # Dangling strap ends
    draw.line([(6, 27), (8, 31)], fill=c_strap, width=2)
    draw.line([(25, 27), (23, 31)], fill=c_strap, width=2)

    return img

def main():
    base_dir = "src/main/resources/assets/israel_simulator/textures/item"
    os.makedirs(base_dir, exist_ok=True)

    items = {
        "kippah.png": generate_kippah(),
        "rabbis_crown.png": generate_rabbis_crown(),
        "talit.png": generate_talit(),
        "tefillin.png": generate_tefillin()
    }

    for name, img in items.items():
        out_path = os.path.join(base_dir, name)
        img.save(out_path, format="PNG")
        print(f"Generated {out_path} ({img.size[0]}x{img.size[1]})")

if __name__ == "__main__":
    main()

