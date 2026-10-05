"""
Generates the official 512x512 mod icon for Israel-Simulator.
Used by:
- NeoForge mod list (in-game Mods menu)
- CurseForge project avatar (requires >= 512x512)
- Modrinth project icon (requires square PNG)
- GitHub repository social card / avatar
"""
from PIL import Image, ImageDraw, ImageFont
import math

def create_mod_icon(output_paths):
    size = 512
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # 1. Rounded rectangle background with Mediterranean / Middle-Eastern theme
    # Gradient from sky blue to warm Jerusalem stone gold
    margin = 16
    radius = 64
    
    # Base background block with bevel
    for y in range(margin, size - margin):
        t = (y - margin) / float(size - 2 * margin)
        # Gradient: Top is vibrant azure blue (0x1E, 0x88, 0xE5), bottom is warm stone (0xDE, 0xB8, 0x87)
        r = int(24 * (1 - t) + 218 * t)
        g = int(118 * (1 - t) + 180 * t)
        b = int(210 * (1 - t) + 130 * t)
        draw.line([(margin, y), (size - margin, y)], fill=(r, g, b, 255))

    # Mask to rounded rect
    mask = Image.new("L", (size, size), 0)
    mask_draw = ImageDraw.Draw(mask)
    mask_draw.rounded_rectangle([margin, margin, size - margin, size - margin], radius=radius, fill=255)
    
    # Apply mask
    final_bg = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    final_bg.paste(img, (0, 0), mask)
    draw = ImageDraw.Draw(final_bg)

    # Outer border (Minecraft block-like dark frame)
    draw.rounded_rectangle([margin, margin, size - margin, size - margin], radius=radius, outline=(20, 45, 80, 255), width=8)
    draw.rounded_rectangle([margin + 4, margin + 4, size - margin - 4, size - margin - 4], radius=radius - 4, outline=(255, 255, 255, 120), width=3)

    # Two horizontal blue stripes (Israeli flag theme)
    stripe_w = 34
    draw.rounded_rectangle([margin + 16, margin + 50, size - margin - 16, margin + 50 + stripe_w], radius=8, fill=(0, 56, 184, 230), outline=(0, 35, 120, 255), width=2)
    draw.rounded_rectangle([margin + 16, size - margin - 50 - stripe_w, size - margin - 16, size - margin - 50], radius=8, fill=(0, 56, 184, 230), outline=(0, 35, 120, 255), width=2)

    # Center Star of David (Magen David)
    cx, cy = size // 2, size // 2
    r_outer = 130
    line_w = 18

    # Helper for equilateral triangle points
    def triangle_points(center_x, center_y, radius, angle_offset_deg):
        pts = []
        for i in range(3):
            angle = math.radians(angle_offset_deg + i * 120)
            pts.append((center_x + radius * math.cos(angle), center_y + radius * math.sin(angle)))
        return pts

    t1 = triangle_points(cx, cy, r_outer, -90) # Point up
    t2 = triangle_points(cx, cy, r_outer, 90)  # Point down

    # Shadow for depth
    shadow_offset = 6
    t1_s = [(x + shadow_offset, y + shadow_offset) for x, y in t1]
    t2_s = [(x + shadow_offset, y + shadow_offset) for x, y in t2]
    draw.polygon(t1_s, outline=(0, 0, 0, 90), width=line_w)
    draw.polygon(t2_s, outline=(0, 0, 0, 90), width=line_w)

    # Main Star in Deep Cobalt Blue
    star_color = (0, 56, 184, 255)
    star_highlight = (80, 140, 255, 255)

    draw.polygon(t1, outline=star_color, width=line_w)
    draw.polygon(t2, outline=star_color, width=line_w)

    # Inner bevel highlight
    draw.polygon(triangle_points(cx, cy, r_outer - 4, -90), outline=star_highlight, width=3)
    draw.polygon(triangle_points(cx, cy, r_outer - 4, 90), outline=star_highlight, width=3)

    # Center glow point
    draw.ellipse([cx - 12, cy - 12, cx + 12, cy + 12], fill=(255, 215, 0, 220), outline=(218, 165, 32, 255), width=2)

    for path in output_paths:
        final_bg.save(path, format="PNG")
        print(f"Saved mod icon to: {path}")

if __name__ == "__main__":
    targets = [
        "src/main/resources/israel_simulator.png",
        "israel_simulator.png"
    ]
    create_mod_icon(targets)
