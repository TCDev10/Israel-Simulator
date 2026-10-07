import os
from PIL import Image

def create_pistol_texture():
    # 32x32 Security Pistol (matte tactical black with metallic slide highlights and textured grip)
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    pixels = img.load()

    # Colors
    c_outline = (25, 27, 30, 255)
    c_metal_dark = (45, 48, 54, 255)
    c_metal_mid = (70, 74, 82, 255)
    c_metal_light = (108, 115, 126, 255)
    c_metal_spec = (150, 158, 172, 255)
    c_grip_dark = (30, 32, 35, 255)
    c_grip_mid = (48, 50, 56, 255)
    c_grip_texture = (62, 65, 72, 255)
    c_silver = (185, 192, 205, 255)

    # Slide (horizontal top block, roughly y: 8 to 15, x: 5 to 26)
    for x in range(6, 26):
        for y in range(9, 15):
            pixels[x, y] = c_metal_mid

    # Slide outline
    for x in range(5, 27):
        pixels[x, 8] = c_outline
        pixels[x, 15] = c_outline
    for y in range(8, 16):
        pixels[5, y] = c_outline
        pixels[26, y] = c_outline

    # Slide top highlight & bevel
    for x in range(6, 26):
        pixels[x, 9] = c_metal_light
        pixels[x, 10] = c_metal_spec if x in (7, 8, 9, 21, 22) else c_metal_light

    # Slide serrations (rear slide grips)
    for x in (7, 9, 11):
        for y in range(11, 15):
            pixels[x, y] = c_metal_dark

    # Ejection port (silver/shiny)
    for x in range(15, 19):
        for y in range(11, 13):
            pixels[x, y] = c_silver
        pixels[x, 13] = c_metal_dark

    # Barrel front tip
    pixels[26, 11] = c_outline
    pixels[26, 12] = c_metal_dark
    pixels[26, 13] = c_metal_mid

    # Frame & Trigger Guard (y: 15 to 21, x: 10 to 22)
    # Frame lower body
    for x in range(8, 25):
        pixels[x, 15] = c_metal_dark
    for x in range(9, 23):
        pixels[x, 16] = c_metal_dark

    # Trigger guard loop
    for x in range(15, 20):
        pixels[x, 20] = c_outline
    pixels[20, 17] = c_outline
    pixels[20, 18] = c_outline
    pixels[20, 19] = c_outline
    pixels[15, 17] = c_outline
    pixels[15, 18] = c_outline
    pixels[15, 19] = c_outline

    # Trigger inside guard
    pixels[17, 17] = c_silver
    pixels[17, 18] = c_metal_light
    pixels[16, 19] = c_metal_dark

    # Pistol Grip (angled downwards to the left, y: 16 to 27, x: 7 to 15)
    grip_rows = [
        (16, 8, 14),
        (17, 8, 14),
        (18, 7, 13),
        (19, 7, 13),
        (20, 6, 12),
        (21, 6, 12),
        (22, 5, 11),
        (23, 5, 11),
        (24, 5, 11),
        (25, 4, 11),
        (26, 4, 12), # Magazine base plate
    ]

    for y, x_start, x_end in grip_rows:
        for x in range(x_start, x_end + 1):
            if x == x_start or x == x_end or y == 26:
                pixels[x, y] = c_outline
            else:
                # Textured checker pattern
                is_check = (x + y) % 2 == 0
                pixels[x, y] = c_grip_texture if is_check else c_grip_dark

    # Grip front highlight
    for y, x_start, x_end in grip_rows[:-1]:
        if x_end - 1 > x_start:
            pixels[x_end - 1, y] = c_grip_mid

    # Sights (front and rear)
    pixels[6, 7] = c_outline
    pixels[6, 8] = c_metal_light
    pixels[24, 7] = c_outline
    pixels[24, 8] = c_metal_light

    return img

def create_missile_texture():
    # 32x32 Tactical Homing Missile (diagonal / sleek rocket with red/white tip, military olive body, fins, and jet trail)
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    pixels = img.load()

    c_outline = (28, 32, 28, 255)
    c_tip_red = (220, 38, 38, 255)
    c_tip_red_dark = (153, 27, 27, 255)
    c_white = (245, 245, 248, 255)
    c_white_shadow = (180, 185, 195, 255)
    c_body_dark = (48, 64, 45, 255)
    c_body_mid = (76, 98, 70, 255)
    c_body_light = (112, 142, 102, 255)
    c_hazard_yellow = (234, 179, 8, 255)
    c_hazard_black = (30, 35, 30, 255)
    c_fin_dark = (40, 50, 42, 255)
    c_fin_mid = (65, 80, 68, 255)
    c_fin_light = (90, 110, 95, 255)
    c_thruster = (80, 85, 95, 255)
    c_flame_core = (255, 250, 160, 255)
    c_flame_orange = (249, 115, 22, 255)
    c_flame_red = (220, 38, 38, 200)

    # Diagonal missile oriented towards top-right (from x:6, y:25 up to x:27, y:4)
    # Centerline path points
    missile_segments = [
        # (center_x, center_y, radius, type)
        (26, 5, 0, 'tip'),
        (25, 6, 1, 'tip_red'),
        (24, 7, 1, 'tip_red'),
        (23, 8, 1, 'tip_white'),
        (22, 9, 2, 'tip_white'),
        (21, 10, 2, 'body_hazard'),
        (20, 11, 2, 'body'),
        (19, 12, 2, 'body'),
        (18, 13, 2, 'body'),
        (17, 14, 2, 'body'),
        (16, 15, 2, 'body_hazard'),
        (15, 16, 2, 'body'),
        (14, 17, 2, 'body'),
        (13, 18, 2, 'body'),
        (12, 19, 2, 'thruster'),
        (11, 20, 2, 'thruster'),
    ]

    # Fill cylindrical cross sections along perpendicular (-1, 1) direction
    for cx, cy, r, seg_type in missile_segments:
        for offset in range(-r, r + 1):
            px = cx + offset
            py = cy - offset
            if 0 <= px < 32 and 0 <= py < 32:
                if seg_type == 'tip':
                    pixels[px, py] = c_tip_red
                elif seg_type == 'tip_red':
                    pixels[px, py] = c_tip_red if offset >= 0 else c_tip_red_dark
                elif seg_type == 'tip_white':
                    pixels[px, py] = c_white if offset >= 0 else c_white_shadow
                elif seg_type == 'body_hazard':
                    pixels[px, py] = c_hazard_yellow if (cx + cy) % 2 == 0 else c_hazard_black
                elif seg_type == 'thruster':
                    pixels[px, py] = c_thruster if offset >= 0 else c_outline
                else: # body
                    if offset > 0:
                        pixels[px, py] = c_body_light
                    elif offset == 0:
                        pixels[px, py] = c_body_mid
                    else:
                        pixels[px, py] = c_body_dark

    # Stabilizer Fins around rear (x:11-14, y:17-21)
    # Upper-left fin
    fin_top = [(14, 14), (13, 13), (12, 12), (11, 13), (12, 15), (13, 16)]
    for fx, fy in fin_top:
        pixels[fx, fy] = c_fin_light

    # Lower-right fin
    fin_bot = [(15, 20), (14, 21), (13, 22), (12, 23), (11, 22), (12, 20)]
    for fx, fy in fin_bot:
        pixels[fx, fy] = c_fin_dark

    # Add outlines to missile edges
    # Check all transparent neighbors of filled pixels
    filled = set()
    for x in range(32):
        for y in range(32):
            if pixels[x, y][3] > 0:
                filled.add((x, y))

    for x, y in filled:
        for dx, dy in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
            nx, ny = x + dx, y + dy
            if 0 <= nx < 32 and 0 <= ny < 32 and (nx, ny) not in filled:
                pixels[nx, ny] = c_outline

    # Rocket propulsion exhaust flame (trailing down-left from nozzle at 10, 21)
    flame_pixels = [
        (9, 22, c_flame_core),
        (8, 23, c_flame_core),
        (10, 22, c_flame_orange),
        (9, 23, c_flame_orange),
        (8, 24, c_flame_orange),
        (7, 24, c_flame_orange),
        (7, 25, c_flame_red),
        (6, 25, c_flame_red),
        (6, 26, c_flame_red),
        (5, 26, (220, 38, 38, 120)),
        (4, 27, (249, 115, 22, 80)),
    ]
    for fx, fy, fcolor in flame_pixels:
        if 0 <= fx < 32 and 0 <= fy < 32:
            pixels[fx, fy] = fcolor

    return img

def main():
    target_dir = os.path.join("src", "main", "resources", "assets", "israel_simulator", "textures", "item")
    os.makedirs(target_dir, exist_ok=True)

    pistol_img = create_pistol_texture()
    pistol_path = os.path.join(target_dir, "pistol.png")
    pistol_img.save(pistol_path, "PNG")
    print(f"Saved {pistol_path} ({pistol_img.size})")

    missile_img = create_missile_texture()
    missile_path = os.path.join(target_dir, "missile.png")
    missile_img.save(missile_path, "PNG")
    print(f"Saved {missile_path} ({missile_img.size})")

if __name__ == "__main__":
    main()
