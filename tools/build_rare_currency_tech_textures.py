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

PHASE4_ITEMS = {}

# 1. FIRST_AMENDMENT: Legendary ancient illuminated scroll, royal scarlet ribbon & gold wax seal
FIRST_AMENDMENT_PALETTE = {
    '.': (0, 0, 0, 0),
    'p': (94, 69, 39, 255),    # parchment dark edge
    'P': (176, 145, 99, 255),  # aged parchment
    'h': (227, 202, 157, 255), # parchment face
    'H': (250, 237, 207, 255), # parchment highlight
    'r': (140, 18, 30, 255),   # scarlet ribbon shadow
    'R': (212, 34, 52, 255),   # bright scarlet ribbon
    's': (255, 94, 110, 255),  # ribbon highlight
    'g': (168, 126, 25, 255),  # wax seal gold edge
    'G': (235, 190, 47, 255),  # radiant wax seal
    'y': (255, 235, 128, 255), # seal specular
    'i': (64, 48, 28, 255),    # ancient script ink
    'I': (117, 93, 62, 255),   # faded ink script
}

PHASE4_ITEMS['first_amendment'] = (FIRST_AMENDMENT_PALETTE, """
......pppp......
....ppHHHHpp....
...pHhPiIiIpp...
..pHhPPPPPPPpp..
..pHPiIiiIiIPp..
.pHhPrrRRRspPp..
.pHPrRgGGgyRPp..
.pHPrRGyyygyRPp.
.pHPrRgGGgyRPp..
.pHhPrrRRRspPp..
..pHPiIiiIiIPp..
..pHhPPPPPPPpp..
...pHhPiIiIpp...
....ppHHHHpp....
......pppp......
................
""")

# 2. ANCIENT_COIN: Hammered Judean bronze prutah with verdigris patina & ancient menorah emblem
ANCIENT_COIN_PALETTE = {
    '.': (0, 0, 0, 0),
    'b': (54, 38, 20, 255),    # bronze coin dark edge
    'B': (105, 75, 42, 255),   # aged bronze midtone
    'c': (158, 118, 71, 255),  # warm bronze face
    'C': (207, 165, 114, 255), # bronze highlight
    # Verdigris green/cyan patina
    'v': (31, 77, 66, 255),    # dark verdigris
    'V': (56, 128, 110, 255),  # patina green
    'p': (102, 184, 162, 255), # bright turquoise patina
    # Stamped emblem (menorah/palm)
    'e': (38, 26, 13, 255),    # stamped depression
    'E': (77, 53, 28, 255),    # stamped shadow
}

PHASE4_ITEMS['ancient_coin'] = (ANCIENT_COIN_PALETTE, """
................
......bbbb......
....bbBCCBbb....
...bBCCcCcpbb...
..bBCCCCeCppVb..
..bCCeeEeEeCpvb.
.bCCeEeEeEeCVVb.
.bCCCeeeeeeCcVb.
.bCcCceEEecCcVb.
.bBcVceEEeCCCVb.
..bVvpeEEepCVb..
..bVVppeepCVVb..
...bbVVpVVVbb...
....bbVVVVbb....
......bbbb......
................
""")

# 3. DEAD_SEA_SCROLL_FRAGMENT: Torn irregular ancient papyrus shard with micro-cracks & Hebrew script
DEAD_SEA_SCROLL_PALETTE = {
    '.': (0, 0, 0, 0),
    'd': (64, 46, 24, 255),    # torn ragged burnt edge
    'D': (107, 81, 46, 255),   # papyrus edge shadow
    'p': (161, 131, 84, 255),  # aged papyrus fiber
    'P': (207, 178, 126, 255), # papyrus surface
    'h': (235, 213, 169, 255), # papyrus highlight
    'i': (36, 26, 14, 255),    # dark ancient ink script
    'I': (74, 56, 33, 255),    # faded ink text
}

PHASE4_ITEMS['dead_sea_scroll_fragment'] = (DEAD_SEA_SCROLL_PALETTE, """
................
...dddd.........
..dPhhPd........
..dPpiIiPd..dd..
.dPhPiIiiIddPPd.
.dPPPPPpPPPhhPd.
.dPhPiIiIIPpiId.
.dPPiiIiiIPPpPd.
..dPhPiIiIiPPhd.
..dPpiIiiIiPPd..
...dPhPPPpPPd...
...dPPiIiiIPd...
....dPhPPPpd....
.....dddddd.....
................
................
""")

# 4. OLIVE_WOOD_CARVING: Beautiful dove/hamsa figurine carved in polished olive wood with grain
OLIVE_WOOD_CARVING_PALETTE = {
    '.': (0, 0, 0, 0),
    'w': (54, 33, 15, 255),    # dark grain streak / outline
    'W': (102, 65, 31, 255),   # rich walnut/olive dark
    'o': (156, 107, 56, 255),  # golden olive wood mid
    'O': (204, 152, 92, 255),  # warm olive wood light
    'h': (237, 196, 142, 255), # polished surface sheen
    's': (255, 228, 189, 255), # specular polish reflection
}

PHASE4_ITEMS['olive_wood_carving'] = (OLIVE_WOOD_CARVING_PALETTE, """
................
......wwww......
.....wOshOw.....
....wOsOhwOw....
...wOwhOOwOhw...
..wOwhwOwOOhOw..
..wOwhwOwOOOhw..
.wOwhwOwOOOOOw..
.wOwhwOwOOOOOw..
.wOwhwOwOhhhOw..
.wOshwOwOhshOw..
..wOshOwOhshOw..
..wwOshwOshOww..
...wwOOwwOOww...
.....wwwwww.....
................
""")

# 5. DREIDEL: Wooden spinning top with faceted sides, handle & carved Hebrew letters
DREIDEL_PALETTE = {
    '.': (0, 0, 0, 0),
    # Handle and body wood
    'w': (71, 46, 20, 255),    # wood outline
    'W': (125, 87, 44, 255),   # wood shadow
    'o': (176, 131, 74, 255),  # wood midtone
    'O': (219, 178, 121, 255), # wood light face
    'h': (247, 217, 171, 255), # wood highlight
    # Carved Hebrew letter (Gimel)
    'c': (51, 31, 11, 255),    # engraved dark groove
    'C': (99, 66, 31, 255),    # engraved shadow
}

PHASE4_ITEMS['dreidel'] = (DREIDEL_PALETTE, """
.......ww.......
.......wOw......
.......wOw......
.....wwwwww.....
....wOOhhhOw....
...wOOOhhhOOw...
..wOOOOchhhOOw..
..wOOOcccChhOw..
..wOOOOccChhOw..
..wOOOOccChhOw..
..wOOOOccChhOw..
...wOOOcccCOw...
....wOOOOOOw....
.....wOOOOw.....
......wOOw......
.......ww.......
""")

# 6. SHEKEL: Modern 1-New-Shekel coin, silver bimetallic with rim, lily emblem & Hebrew motif
SHEKEL_PALETTE = {
    '.': (0, 0, 0, 0),
    'd': (50, 56, 66, 255),    # rim dark border
    's': (95, 105, 122, 255),  # silver bevel shadow
    'S': (150, 162, 184, 255), # silver body
    'l': (200, 210, 227, 255), # silver highlight
    'w': (245, 250, 255, 255), # specular reflection
    # Inner emblem (lily / menorah design)
    'e': (40, 60, 95, 255),    # emblem deep groove
    'E': (65, 95, 145, 255),   # emblem blue-silver
}

PHASE4_ITEMS['shekel'] = (SHEKEL_PALETTE, """
......dddd......
....ddwllwdd....
...dwllllllldd..
..dwllSddSlllsd.
.dwllSdEEeSlllsd
.dwlSdEEEEeSllsd
dwllSdEEeEesllsd
dwllSdEeEeesllsd
dwllSdEEEEesllsd
dwllSdEeEEesllsd
dwlsSdEEeeesllsd
.dwlsSddddsslsd.
.dwlsssssssslsd.
..dwlsssssslssd.
...ddssssssdd...
......dddd......
""")

# 7. AGORA: 10-Agurot golden-bronze scalloped coin with ancient lyre/harp motif
AGORA_PALETTE = {
    '.': (0, 0, 0, 0),
    'b': (87, 56, 16, 255),    # bronze outline
    'B': (148, 101, 33, 255),  # bronze shadow
    'g': (204, 152, 53, 255),  # golden bronze mid
    'G': (242, 197, 85, 255),  # bright golden face
    'h': (255, 227, 138, 255), # specular coin shine
    # Harp / lyre emblem
    'e': (74, 46, 10, 255),    # emblem groove
    'E': (122, 80, 20, 255),   # emblem relief
}

PHASE4_ITEMS['agora'] = (AGORA_PALETTE, """
......bbbb......
...bbbGhhGbbb...
..bGGhGGGGhhGb..
.bGhGGeEEeGhhGb.
.bGhGEeeeeEGhGb.
bGhGeEeeEeEGhGbb
bGhGEeEeEeEGhGbb
bGhGEeeeeeeGhGbb
bGhGEeeEeEEGhGbb
bGhGeEeeeeEGhGbb
bGhGGeEEeeGGhGbb
.bGGhGeeeeGGhGb.
.bGGhGGGGGGhhGb.
..bGGhhhhhhGGb..
...bbbGGGGbbb...
......bbbb......
""")

# 8. SMARTPHONE: Modern sleek smartphone, bezel, glass screen glare & colorful app icons
SMARTPHONE_PALETTE = {
    '.': (0, 0, 0, 0),
    'c': (30, 32, 36, 255),    # dark chassis frame
    'C': (70, 75, 84, 255),    # chassis metallic edge
    'w': (210, 225, 245, 255), # glass reflection line
    'k': (15, 17, 20, 255),    # black screen base
    'K': (25, 28, 33, 255),    # screen background
    # Status bar & notch
    'n': (50, 55, 64, 255),    # camera notch
    # App icons (green, blue, orange, red)
    'g': (48, 186, 76, 255),   # whatsapp/message green
    'b': (33, 133, 247, 255),  # browser/social blue
    'o': (250, 140, 22, 255),  # camera/gallery orange
    'r': (240, 50, 50, 255),   # music/youtube red
    'y': (255, 215, 0, 255),   # notes/mail yellow
}

PHASE4_ITEMS['smartphone'] = (SMARTPHONE_PALETTE, """
....cccccc......
...cKKnnKKc.....
...cwkKKkkc.....
...cwgKbKkc.....
...cwKkkKKc.....
...cwoKrKkc.....
...cwKKkKKc.....
...cwyKgKkc.....
...cwKKkkKc.....
...cwkKKkKc.....
...cKKKKKKc.....
...cKKKKKKc.....
...cKKKKKKc.....
...cKKKKKKc.....
....cCCCCc......
................
""")

# 9. LAPTOP: Open ultrabook laptop, glowing blue display, keyboard deck & metallic chassis
LAPTOP_PALETTE = {
    '.': (0, 0, 0, 0),
    'c': (46, 51, 61, 255),    # chassis frame dark
    'C': (88, 97, 115, 255),   # aluminum chassis
    'h': (140, 153, 179, 255), # metallic highlight
    'w': (220, 230, 250, 255), # specular reflection
    # Screen display
    'b': (18, 59, 130, 255),   # deep screen blue
    'B': (38, 114, 219, 255),  # desktop bright blue
    'l': (102, 178, 255, 255), # window / document light
    's': (235, 245, 255, 255), # cursor / icon white
    # Keyboard & trackpad
    'k': (26, 28, 33, 255),    # keyboard keys
    't': (65, 72, 84, 255),    # trackpad
}

PHASE4_ITEMS['laptop'] = (LAPTOP_PALETTE, """
................
...chhhhhhhhc...
..chbBbBlBsbhc..
..chBbBbllsbhc..
..chbBbBbBsbhc..
..chBbBbBbsbhc..
..chbBbBbBsbhc..
..chCCCCCCCCc...
.chhhhhhhhhhhc..
chkkkkkkkkkkkkhc
chkkkkkkkkkkkkhc
chhhhhhtthhhhhhc
.chhhhhhhhhhhc..
..cccccccccc....
................
................
""")

# 10. DRONE_PART: Carbon fiber motor arm with brushless copper winding and rotor hub
DRONE_PART_PALETTE = {
    '.': (0, 0, 0, 0),
    'c': (28, 30, 33, 255),    # carbon fiber black
    'C': (56, 60, 66, 255),    # carbon weave dark
    'g': (95, 102, 112, 255),  # carbon weave light
    'h': (150, 160, 173, 255), # metallic fastener
    'w': (230, 240, 250, 255), # specular gleam
    # Copper motor coils
    'm': (138, 55, 21, 255),   # copper shadow
    'M': (199, 93, 38, 255),   # bright copper wire
    'y': (245, 148, 81, 255),  # copper highlight
    # Rotor shaft
    's': (64, 73, 84, 255),    # steel shaft
    'S': (116, 130, 148, 255), # polished steel
}

PHASE4_ITEMS['drone_part'] = (DRONE_PART_PALETTE, """
......hhhh......
....hhcwwchhh...
...hcgCSsSCgch..
..hcgCsmMmsCgch.
..hcCsmMyyMmsCh.
...hcgCsmMmsCgh.
....hhcSSSSch...
.....hccggcch...
.....hCgCgCCh...
......hCgCgh....
......hCgCgh....
.......hCgCh....
.......hCgCh....
........hCCh....
.........hh.....
................
""")

# 11. RAV_KAV: Israeli transit smartcard in signature emerald green with gold microchip
RAV_KAV_PALETTE = {
    '.': (0, 0, 0, 0),
    'd': (14, 66, 36, 255),    # card border dark green
    'g': (28, 122, 68, 255),   # rav kav forest green
    'G': (46, 179, 102, 255),  # vibrant emerald green
    'l': (92, 222, 144, 255),  # card highlight green
    'w': (255, 255, 255, 255), # white transit graphics
    # Gold RFID contact chip
    'c': (145, 108, 22, 255),  # chip dark gold
    'C': (219, 174, 46, 255),  # bright gold chip
    'y': (255, 225, 115, 255), # chip specular
}

PHASE4_ITEMS['rav_kav'] = (RAV_KAV_PALETTE, """
................
.dddddddddddddd.
.dGllGGGGGGGGGd.
.dlGGGGGGGGGGGd.
.dGGccyGGGGGGGd.
.dGCyyCGGwwGGGd.
.dGCyyCGwwwwGGd.
.dGGccGGwGwwGGd.
.dGGGGGwwGGwGGd.
.dGGGGwwGGGGGGd.
.dGGGwwwwGGGGGd.
.dGGwGGwwwGGGGd.
.dGwwGGGGwwGGGd.
.dGGGGGGGGGGGGd.
.dddddddddddddd.
................
""")

def main():
    target_dir = os.path.abspath("src/main/resources/assets/israel_simulator/textures/item")
    print(f"Generating 11 rare/currency/tech textures in: {target_dir}")
    for name, (palette, art) in PHASE4_ITEMS.items():
        img = parse_ascii_art(art, palette)
        out_path = os.path.join(target_dir, f"{name}.png")
        img.save(out_path, "PNG")
        colors = len(set(img.get_flattened_data()))
        print(f"  [OK] {name}.png ({img.width}x{img.height}) with {colors} unique colors -> {out_path}")

if __name__ == "__main__":
    main()
