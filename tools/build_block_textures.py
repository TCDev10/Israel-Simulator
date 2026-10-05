import os
from PIL import Image

def parse_ascii_art(art_str, palette):
    lines = [line for line in art_str.strip().splitlines() if line.strip()]
    height = len(lines)
    width = len(lines[0])
    img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    for y, line in enumerate(lines):
        for x, char in enumerate(line):
            if char in palette:
                img.putpixel((x, y), palette[char])
            else:
                raise ValueError(f"Unknown char '{char}' at ({x}, {y})")
    return img

BLOCK_TEXTURES = {}

# 1. JERUSALEM_STONE: Warm cream/golden limestone with chiseled ashlar courses and subtle mortar
JERUSALEM_STONE_PALETTE = {
    'm': (125, 108, 82, 255),  # mortar line
    'M': (158, 138, 107, 255), # mortar shadow
    's': (194, 174, 136, 255), # limestone shadow
    'S': (217, 200, 163, 255), # limestone midtone
    'l': (235, 222, 190, 255), # limestone light
    'L': (247, 237, 212, 255), # limestone highlight
}

BLOCK_TEXTURES['jerusalem_stone'] = (JERUSALEM_STONE_PALETTE, """
SLllSSLsSSLllSSL
LllllSSLllLLllSL
SllSSLlsSSLlllSL
llSSLsSSLllSSLlS
MMMMMMMMMMMMMMMM
mmmmmmmmmmmmmmmm
SSllSSLlsSSLlllS
LlllSSLllLLllSSL
llSSLssSSLllSSLS
SSLlsSSLllSSLllS
MMMMMMMMMMMMMMMM
mmmmmmmmmmmmmmmm
LlllSSLllLLllSSL
llSSLssSSLllSSLS
SllSSLlsSSLlllSL
SSLlsSSLllSSLllS
""")

# 2. WESTERN_WALL_STONE: Monumental Herodion ashlar block with drafted sunken margin & weathered central boss
WESTERN_WALL_PALETTE = {
    # Drafted margins (sunken border around edge of block)
    'g': (105, 89, 64, 255),   # deep margin groove
    'G': (143, 123, 91, 255),  # sunken margin shadow
    'm': (176, 153, 116, 255), # sunken margin midtone
    # Raised weathered antique limestone boss
    's': (199, 178, 139, 255), # boss shadow
    'b': (222, 203, 164, 255), # boss body
    'B': (237, 222, 187, 255), # boss light
    'h': (247, 236, 207, 255), # boss highlight
}

BLOCK_TEXTURES['western_wall_stone'] = (WESTERN_WALL_PALETTE, """
gggggggggggggggg
gGGGGGGGGGGGGGGg
gGmmmmmmmmmmmmGg
gGmssssssssssmGg
gGmsbbhBBBbhsmGg
gGmsbBBhBBBbsmGg
gGmsbBBBBBhbsmGg
gGmsbbhBBBbhsmGg
gGmsbBBhBBBbsmGg
gGmsbBBBBBhbsmGg
gGmsbbhBBBbhsmGg
gGmsbBBhBBBbsmGg
gGmssssssssssmGg
gGmmmmmmmmmmmmGg
gGGGGGGGGGGGGGGg
gggggggggggggggg
""")

# 3. SALT_BLOCK: Dead Sea crystalline mineral salt with cubic crystal facets & shimmering highlights
SALT_BLOCK_PALETTE = {
    'c': (148, 176, 189, 255), # deep cyan crystal shadow
    'C': (181, 205, 214, 255), # cyan mineral facet
    's': (212, 228, 235, 255), # light salt crystal
    'S': (235, 245, 247, 255), # cubic facet face
    'w': (255, 255, 255, 255), # pure white crystal highlight
}

BLOCK_TEXTURES['salt_block'] = (SALT_BLOCK_PALETTE, """
wSSssSSswSSssSSs
SwwsSCswSwwsSCsw
sSswCCssSswCCssS
swCCssSSswCCssSS
CSswSSswCSswSSsw
sSswwSsSsSswwSsS
swCSswsSswCSswsS
sSCsswSssSCsswSs
wSSssSSswSSssSSs
SwwsSCswSwwsSCsw
sSswCCssSswCCssS
swCCssSSswCCssSS
CSswSSswCSswSSsw
sSswwSsSsSswwSsS
swCSswsSswCSswsS
sSCsswSssSCsswSs
""")

# 4. PAVED_ROAD: Dark asphalt with mineral aggregate gravel texture
PAVED_ROAD_PALETTE = {
    'a': (41, 43, 46, 255),    # dark asphalt binder
    'A': (54, 56, 61, 255),    # asphalt base
    'g': (74, 77, 84, 255),    # aggregate gravel shadow
    'G': (99, 103, 112, 255),  # fine mineral stone
    's': (135, 140, 150, 255), # light stone aggregate
    'S': (165, 170, 180, 255), # specular stone aggregate
}

BLOCK_TEXTURES['paved_road'] = (PAVED_ROAD_PALETTE, """
AGaAAgGAAGaAAgGA
AaGsAaAAAaGsAaAA
gAAasGAagAAasGAa
GAaAgGAaGAaAgGAa
aAAgAaAAaAAgAaAA
GsAaGAgAGsAaGAgA
AgAaAaSAAgAaAaSA
aAgGAaaGaAgGAaaG
AGaAAgGAAGaAAgGA
AaGsAaAAAaGsAaAA
gAAasGAagAAasGAa
GAaAgGAaGAaAgGAa
aAAgAaAAaAAgAaAA
GsAaGAgAGsAaGAgA
AgAaAaSAAgAaAaSA
aAgGAaaGaAgGAaaG
""")

# 5. TRANSPORT_STOP: Israeli bus shelter with blue/red transit signs and brushed steel frame
TRANSPORT_STOP_PALETTE = {
    'f': (51, 56, 64, 255),    # frame dark
    'F': (84, 91, 102, 255),   # frame steel
    's': (138, 147, 161, 255), # steel light
    'S': (194, 202, 214, 255), # steel highlight
    # Sign colors (Israeli bus stop logo: red and blue)
    'r': (209, 31, 31, 255),   # transit red
    'b': (28, 97, 194, 255),   # transit blue
    'w': (255, 255, 255, 255), # white signage background
    # Glass panel
    'g': (168, 202, 214, 255), # glass reflection
    'G': (212, 235, 242, 255), # clean glass
}

BLOCK_TEXTURES['transport_stop'] = (TRANSPORT_STOP_PALETTE, """
ffffffffffffffff
fSSSSSSSSSSSSSSf
fSrrwwbbbbwwrrSf
fSrrwwbbbbwwrrSf
fSrrwwbwwbwwrrSf
fSrrwwbwwbwwrrSf
fSSSSSSSSSSSSSSf
fFFFFFFFFFFFFFFf
fSGGgGGGGGGgGGSf
fsGGgGGGGGGgGGsf
fFGGgGGGGGGgGGFF
fsGGgGGGGGGgGGsf
fsGGgGGGGGGgGGsf
fFGGgGGGGGGgGGFF
fSSSSSSSSSSSSSSf
ffffffffffffffff
""")

# 6. MENORAH: Polished brass/gold texture with warm shadows, highlights and flame tips
MENORAH_PALETTE = {
    'g': (120, 84, 18, 255),   # deep brass shadow
    'G': (184, 137, 31, 255),  # polished brass
    'y': (237, 192, 55, 255),  # bright gold
    'Y': (255, 226, 105, 255), # gold specular
    # Flame tips (at top)
    'r': (214, 45, 24, 255),   # flame red
    'o': (255, 140, 20, 255),  # flame orange
    'f': (255, 235, 77, 255),  # flame yellow
    'w': (255, 255, 255, 255), # flame white core
}

BLOCK_TEXTURES['menorah'] = (MENORAH_PALETTE, """
.r.o.r.w.r.o.r..
.o.f.o.f.o.f.o..
YYgGYYgGYYgGYYgG
yYYgyYYgyYYgyYYg
GyYyGyYyGyYyGyYy
gGyGgGyGgGyGgGyG
gGgGgGgGgGgGgGgG
GyYyGyYyGyYyGyYy
yYYgyYYgyYYgyYYg
YYgGYYgGYYgGYYgG
GyYyGyYyGyYyGyYy
gGyGgGyGgGyGgGyG
GyYyGyYyGyYyGyYy
yYYgyYYgyYYgyYYg
YYgGYYgGYYgGYYgG
gGgGgGgGgGgGgGgG
""".replace('.', 'g'))

def main():
    target_dir = os.path.abspath("src/main/resources/assets/israel_simulator/textures/block")
    print(f"Generating 6 block textures in: {target_dir}")
    for name, (palette, art) in BLOCK_TEXTURES.items():
        img = parse_ascii_art(art, palette)
        out_path = os.path.join(target_dir, f"{name}.png")
        img.save(out_path, "PNG")
        colors = len(set(img.get_flattened_data()))
        print(f"  [OK] {name}.png ({img.width}x{img.height}) with {colors} unique colors -> {out_path}")

if __name__ == "__main__":
    main()
