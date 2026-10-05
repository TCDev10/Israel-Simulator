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

CULTURAL_ITEMS = {}

# 1. KIPPAH: Knitted white/blue skullcap with Star of David and rim shading (centered in 4..12)
KIPPAH_PALETTE = {
    '.': (0, 0, 0, 0),
    'd': (18, 38, 77, 255),    # dark blue outline
    'b': (34, 75, 153, 255),   # rich royal blue
    'B': (61, 114, 214, 255),  # light blue stitch
    'w': (200, 205, 215, 255), # wool shadow
    'W': (245, 248, 255, 255), # pure white wool
    'h': (255, 255, 255, 255), # bright highlight
}

CULTURAL_ITEMS['kippah'] = (KIPPAH_PALETTE, """
................
................
................
......dddd......
....ddbbbbdd....
...dbBwwwwBbtd..
..dbBwWWWWwBbd..
..dbwWWbhWWwbdd.
..dbwWbbhhWwbdd.
..dbBwWbhWWwBbd.
...dbBwwwwBbtd..
....ddbbbbdd....
......dddd......
................
................
................
""".replace('t', 'b'))

# 2. TALIT: White wool prayer shawl, cobalt blue stripes, atarah gold accents, tzitzit fringes
TALIT_PALETTE = {
    '.': (0, 0, 0, 0),
    's': (180, 185, 195, 255), # wool deep shadow / outline
    'w': (220, 225, 235, 255), # wool weave midtone
    'W': (250, 252, 255, 255), # pure white wool
    'd': (20, 42, 90, 255),    # dark navy stripe
    'D': (14, 28, 62, 255),    # deepest navy stripe
    'b': (38, 80, 168, 255),   # rich tekhelet blue
    'B': (70, 125, 224, 255),  # light blue stripe
    'g': (179, 140, 45, 255),  # gold embroidery (atarah)
    'G': (235, 195, 80, 255),  # gold highlight
    'f': (210, 215, 225, 255), # tzitzit fringes
    'F': (245, 245, 250, 255), # tzitzit fringe light
}

CULTURAL_ITEMS['talit'] = (TALIT_PALETTE, """
................
...ssWWgGWWss...
..swWwWGGWwWws..
..sWWbBWWbBWWs..
..sWwdDbWwdDbs..
..sWWbBWWbBWWs..
..sWwdDbWwdDbs..
..sWWWWWWWWWWs..
..sWWbBWWbBWWs..
..sWwdDbWwdDbs..
..swWbBWWbBWWs..
...ssWWWWWWss...
....f.F..f.F....
....F.f..F.f....
....f.F..f.F....
................
""")

# 3. TEFILLIN: Black polished leather bayit cube with embossed Shin & wound retzuot strapping
TEFILLIN_PALETTE = {
    '.': (0, 0, 0, 0),
    'k': (18, 18, 20, 255),    # deepest leather black
    'K': (38, 39, 43, 255),    # black leather midtone
    'l': (68, 70, 77, 255),    # leather sheen / edge
    'L': (110, 114, 122, 255), # specular reflection
    'g': (194, 158, 64, 255),  # embossed gold Shin
    'G': (245, 214, 115, 255), # Shin specular highlight
    's': (45, 46, 51, 255),    # strap body
    'S': (80, 83, 92, 255),    # strap edge
}

TEFILLIN_PALETTE['b'] = (150, 155, 165, 255) # silver buckle

CULTURAL_ITEMS['tefillin'] = (TEFILLIN_PALETTE, """
................
................
................
.....kkkkkk.....
....kLllllKk....
...kLllllllKk...
...klkGgGgkkk...
..sklgGgGgkksb..
.sSklkGgGgkkksb.
.sSklkkkllkkkSs.
.sSkKKKKKKKKkSs.
..skkkkkkkkkks..
...ssss..ssss...
..sS..SssS..Ss..
..s....ss....s..
................
""")

# 4. RABBIS_CROWN: Mythic crown with velvet dome, gold filigree, rubies, sidecurls (payot) and beard
RABBIS_CROWN_PALETTE = {
    '.': (0, 0, 0, 0),
    # Payot (side curls)
    'p': (41, 25, 14, 255),    # payot dark
    'P': (79, 50, 28, 255),    # payot mid
    'h': (115, 78, 47, 255),   # payot light
    # Crown gold
    'g': (122, 86, 17, 255),   # dark gold filigree
    'G': (196, 148, 33, 255),  # radiant gold
    'y': (247, 205, 62, 255),  # bright gold
    'Y': (255, 240, 145, 255), # specular gold shine
    # Gems (ruby and sapphire)
    'r': (184, 18, 37, 255),   # ruby dark
    'R': (245, 59, 81, 255),   # ruby sparkle
    'b': (18, 59, 168, 255),   # sapphire dark
    'B': (64, 140, 255, 255),  # sapphire sparkle
    # Velvet cap (scarlet)
    'v': (87, 10, 24, 255),    # velvet dark
    'V': (148, 21, 44, 255),   # rich scarlet velvet
    # Flowing silver beard
    'd': (140, 145, 156, 255), # beard shadow
    'w': (195, 200, 212, 255), # silver beard mid
    'W': (240, 243, 250, 255), # pure white beard hair
}

CULTURAL_ITEMS['rabbis_crown'] = (RABBIS_CROWN_PALETTE, """
................
.pp....gYg....pp
.Ph...gGYGg...hP
.Ph..gGvYvGg..hP
.pP.gGvVRVvGg.Pp
.Ph.GyVVbVVyG.hP
.Ph.GyVVbVVyG.hP
.pPggYYYYYYgg.Pp
.PhgGgGgGgGgGhP.
..PgGrGyRyGgBP..
..PgGrGyRyGgBP..
...ggYYYYYYgg...
.....wWwWwW.....
....wWWwWwWw....
.....wWwWwW.....
......dwwd......
""")

# 5. SHOFAR: Curved spiral ram's horn with polished ivory mouthpiece, ridges & amber gradients
SHOFAR_PALETTE = {
    '.': (0, 0, 0, 0),
    'd': (59, 41, 22, 255),    # dark horn rim / shadow
    'b': (102, 72, 39, 255),   # dark amber horn
    'c': (148, 108, 62, 255),  # warm horn body
    'a': (194, 154, 99, 255),  # ivory horn midtone
    'A': (227, 197, 148, 255), # polished horn light
    'h': (250, 235, 205, 255), # horn specular highlight
    'm': (38, 25, 13, 255),    # hollow dark mouthpiece
}

SHOFAR_PALETTE['M'] = (74, 52, 29, 255) # mouthpiece rim

CULTURAL_ITEMS['shofar'] = (SHOFAR_PALETTE, """
................
.............mM.
............maAd
...........maAhd
..........maAhad
.........maAcabd
........maAcbbd.
......ddaAcbbd..
....ddcAacbbd...
...dcAhccbbd....
..dcAhhccbd.....
.dcAhhhcbbd.....
.dcAhhcbbd......
..ddccbbd.......
....dddd........
................
""")

# 6. MEZUZAH: Decorative silver/bronze doorpost case with carved Shin & Hebrew scroll parchment
MEZUZAH_PALETTE = {
    '.': (0, 0, 0, 0),
    'd': (46, 52, 61, 255),    # pewter/silver shadow
    'm': (89, 99, 115, 255),   # silver metal mid
    's': (143, 155, 173, 255), # bright polished silver
    'S': (196, 207, 224, 255), # specular silver reflection
    'w': (240, 245, 255, 255), # pure gleam
    # Inset Shin and scroll
    'p': (191, 169, 126, 255), # parchment scroll
    'P': (227, 209, 172, 255), # light parchment
    'g': (168, 128, 37, 255),  # gold Shin letter
    'G': (235, 192, 70, 255),  # bright gold Shin
}

CULTURAL_ITEMS['mezuzah'] = (MEZUZAH_PALETTE, """
.......dd.......
......dsSmd.....
......dSwmd.....
......dsSmd.....
.....ddssmmd....
....dswppmmd....
...dswpggpmmd...
...dswpGGpmmd...
...dswpggpmmd...
...dswppPpmmd...
...dswpGGpmmd...
....dswppmmd....
.....ddssmmd....
......dsSmd.....
......dSwmd.....
.......dd.......
""")

# 7. STAR_OF_DAVID: Magen David hexagram with silver beveled edge and glowing celestial blue core
STAR_OF_DAVID_PALETTE = {
    '.': (0, 0, 0, 0),
    'd': (35, 45, 66, 255),    # deep border
    's': (90, 115, 150, 255),  # silver bevel shadow
    'S': (160, 185, 220, 255), # bright silver edge
    'w': (235, 245, 255, 255), # specular silver shine
    'c': (14, 82, 171, 255),   # cobalt blue crystal
    'C': (38, 140, 245, 255),  # radiant sky blue
    'l': (102, 196, 255, 255), # luminous cyan glow
    'L': (204, 240, 255, 255), # pure light star core
}

CULTURAL_ITEMS['star_of_david'] = (STAR_OF_DAVID_PALETTE, """
.......ww.......
......wSSw......
.....wSCClw.....
..wwwwSCCllwwww.
..wSSsSCllLsSSw.
...wSSsCllLsSw..
....wSSCllSw....
...wSSsCllLsSw..
..wSSsSCllLsSSw.
..wwwwSCCllwwww.
.....wSCClw.....
......wSSw......
.......ww.......
................
................
................
""")

# 8. PRAYER_NOTE: Creased folded parchment with ancient Hebrew ink script for the Western Wall
PRAYER_NOTE_PALETTE = {
    '.': (0, 0, 0, 0),
    'b': (105, 87, 59, 255),   # paper edge/crease shadow
    'p': (168, 147, 108, 255), # aged papyrus
    'P': (214, 194, 152, 255), # parchment face
    'h': (242, 227, 194, 255), # paper fold highlight
    'w': (255, 248, 230, 255), # crease white reflection
    'i': (56, 45, 31, 255),    # dark Hebrew ink script
    'I': (94, 76, 53, 255),    # faded ink script
}

CULTURAL_ITEMS['prayer_note'] = (PRAYER_NOTE_PALETTE, """
................
...bbbbbbbbb....
..bhhhhhhhhpb...
..bhwPPPPPpppb..
..bhPiIiIipbpb..
..bhPPPPPPpppb..
..bhPiIiiIipbb..
..bbbbbbbbbpbb..
..bhwPPPPPpppb..
..bhPiiIiIipbb..
..bhPPPPPPpppb..
..bhPiIiIipbpb..
..bppppppppbpb..
...bbbbbbbbbpb..
.....bbbbbbbbb..
................
""")

def main():
    target_dir = os.path.abspath("src/main/resources/assets/israel_simulator/textures/item")
    print(f"Generating 8 cultural textures in: {target_dir}")
    for name, (palette, art) in CULTURAL_ITEMS.items():
        img = parse_ascii_art(art, palette)
        out_path = os.path.join(target_dir, f"{name}.png")
        img.save(out_path, "PNG")
        colors = len(set(img.get_flattened_data()))
        print(f"  [OK] {name}.png ({img.width}x{img.height}) with {colors} unique colors -> {out_path}")

if __name__ == "__main__":
    main()
