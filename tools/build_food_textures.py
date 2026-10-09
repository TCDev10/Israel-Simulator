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

FOOD_ITEMS = {}

# 1. FALAFEL: Pita pocket with 3 crisp falafel balls, tahini drizzle, tomato & cucumber salad
FALAFEL_PALETTE = {
    '.': (0, 0, 0, 0),
    # Pita bread
    'p': (84, 52, 23, 255),    # dark crust outline
    'P': (168, 114, 61, 255),  # golden pita crust
    'b': (212, 166, 114, 255), # pita inner bread
    'B': (237, 207, 168, 255), # pita highlight
    # Falafel balls
    'f': (54, 30, 11, 255),    # dark fried crust
    'F': (110, 68, 26, 255),   # fried brown crumb
    'h': (148, 97, 44, 255),   # light golden fried crumb
    'g': (58, 107, 34, 255),   # parsley herb fleck
    # Tahini drizzle
    't': (248, 245, 232, 255), # creamy white tahini
    'T': (214, 204, 180, 255), # tahini shadow
    # Salad: tomato & cucumber
    'r': (194, 39, 23, 255),   # diced tomato
    'R': (230, 78, 55, 255),   # tomato highlight
    'c': (51, 117, 36, 255),   # diced cucumber
    'C': (92, 166, 71, 255),   # cucumber light
}

FOOD_ITEMS['falafel'] = (FALAFEL_PALETTE, """
................
....pPPp........
...PBBBBp.pp....
..PBBtBBBPffp...
..PBtTtBffFhhp..
.PBtTtRfFhghFp..
.ptttRrfhFFfhp..
.pTRrrcfhhFfFp..
.pTrcCcfFfFFhp..
.pTrCccffhhfPp..
.pBtrccfhhFPBp..
..pBrcfffPPBBp..
..pBBffPPBBBPp..
...pPPBBBBBPp...
....ppPPPPpp....
................
""")

# 2. HUMMUS: Terracotta bowl, creamy chickpea swirl, olive oil pool, paprika & parsley
HUMMUS_PALETTE = {
    '.': (0, 0, 0, 0),
    'b': (102, 43, 23, 255),   # bowl rim/shadow
    'B': (158, 71, 41, 255),   # terracotta clay
    'h': (186, 92, 58, 255),   # clay highlight
    'c': (176, 146, 95, 255),  # hummus shadow
    'C': (219, 193, 138, 255), # creamy hummus
    'H': (240, 222, 175, 255), # hummus light swirl
    'o': (184, 155, 29, 255),  # olive oil shadow
    'O': (224, 198, 45, 255),  # glowing olive oil
    'r': (186, 37, 20, 255),   # paprika dust
    'R': (224, 58, 38, 255),   # paprika bright
    'g': (41, 99, 25, 255),    # parsley leaf
    'G': (75, 150, 48, 255),   # parsley light
    'k': (204, 168, 104, 255), # whole chickpea
}

FOOD_ITEMS['hummus'] = (HUMMUS_PALETTE, """
................
.....bbbbbb.....
...bbBhhhhBbb...
..bBccccccccBb..
.bBCHHOOgHCcCBb.
.bCHHOOgGHrCcBb.
.bCHkkogGGrCcBb.
.bCHkCcoorCCCcBb
.bCHHCCHkCCCcCBb
.bBCCCCCCCCCCBb.
..bBccccccccBb..
...bBBBBBBBBb...
....bBBBBBBb....
.....bbbbbb.....
................
................
""")

# 3. SHAKSHUKA: Black skillet, steaming tomato sauce, two eggs with runny yolks, herbs
SHAKSHUKA_PALETTE = {
    '.': (0, 0, 0, 0),
    's': (28, 30, 33, 255),    # skillet iron dark
    'S': (55, 58, 66, 255),    # skillet iron mid
    'h': (85, 89, 99, 255),    # skillet rim highlight
    't': (130, 24, 14, 255),   # dark stewed tomato
    'T': (186, 39, 22, 255),   # rich tomato red
    'r': (222, 60, 38, 255),   # bright simmering sauce
    'w': (224, 228, 235, 255), # egg white shadow
    'W': (255, 255, 255, 255), # pure egg white
    'y': (224, 112, 0, 255),   # yolk shadow
    'Y': (255, 179, 13, 255),  # bright sunny yolk
    'j': (255, 224, 92, 255),  # yolk specular
    'g': (38, 97, 27, 255),    # herb garnish
    'G': (68, 156, 47, 255),   # herb leaf light
}

FOOD_ITEMS['shakshuka'] = (SHAKSHUKA_PALETTE, """
..........hSS...
..sshhssss.ssS..
.shTTrTTtths.ss.
shTrWwTrgTTths..
sTWWwyyTgWwTts..
sTWwYjYTrWWWTs..
stTrwyytWWwYjs..
stTrrTTtwWYjYs..
stGgrTTtTwyyTs..
sttGTrrTrrTTts..
shTTtTrgTrTTts..
.shTTTrrtTths...
..shhttttths....
....ssssss......
................
................
""")

# 4. CHALLAH: Golden braided sabbath loaf with glossy egg wash & sesame seeds
CHALLAH_PALETTE = {
    '.': (0, 0, 0, 0),
    'c': (77, 38, 12, 255),    # dark baked crust
    'C': (140, 77, 24, 255),   # rich amber crust
    'b': (194, 116, 37, 255),  # golden brown loaf
    'B': (230, 157, 57, 255),  # glossy baked surface
    'h': (247, 196, 104, 255), # egg-wash highlight
    's': (255, 248, 230, 255), # white sesame seeds
}

FOOD_ITEMS['challah'] = (CHALLAH_PALETTE, """
................
......cc........
....ccBBcc......
...cBhhBsBcc....
..cBhsshBBBBc...
.cBBhBBsBhBsBc..
.cBssBhBBsBhhBc.
cBBhBBsBhBBsBBc.
cBshBBsBBshsBBCc
.cBBhBBsBhBBBCc.
.cCBshBsBBsBCCc.
..cCBsBsBBCCCc..
...cCCBBCCCCc...
.....cccccc.....
................
................
""")

# 5. RUGELACH: Rolled flaky pastry crescent with chocolate swirls & sugar shine
RUGELACH_PALETTE = {
    '.': (0, 0, 0, 0),
    'd': (38, 17, 8, 255),     # dark chocolate swirl
    'D': (74, 36, 16, 255),    # milk chocolate
    'c': (117, 68, 25, 255),   # baked pastry edge
    'C': (179, 115, 48, 255),  # golden flaky crust
    'p': (222, 159, 80, 255),  # light golden pastry
    'P': (242, 198, 131, 255), # pastry layer
    'h': (255, 232, 184, 255), # sugar glaze shine
}

FOOD_ITEMS['rugelach'] = (RUGELACH_PALETTE, """
................
........cc......
......ccppc.....
.....cppPhpc....
....cPhPPdDpc...
...cPhPdDddDpc..
..cPhPdDddDpPc..
..cPPdDddDpphc..
..cPdDddDpphPc..
..cDddDpphPPhc..
..cddDpphPPhCc..
...cDpphPPhCCc..
....cpphPCCCc...
.....ccCCCCc....
.......cccc.....
................
""")

# 6. SABICH: Pita stuffed with fried eggplant, hardboiled egg slice, potato & yellow amba sauce
SABICH_PALETTE = {
    '.': (0, 0, 0, 0),
    'p': (84, 52, 23, 255),    # pita crust
    'P': (171, 117, 63, 255),  # golden pita
    'b': (219, 172, 121, 255), # pita inner
    'B': (240, 209, 170, 255), # pita light
    # Eggplant (deep purple skin + golden fried meat)
    'e': (36, 16, 41, 255),    # purple skin
    'E': (74, 38, 82, 255),    # eggplant violet
    'm': (140, 102, 59, 255),  # fried eggplant flesh
    # Hardboiled egg
    'w': (220, 224, 230, 255), # egg white shadow
    'W': (255, 255, 255, 255), # egg white highlight
    'y': (245, 175, 24, 255),  # egg yolk
    # Amba sauce (bright yellow-orange pickled mango)
    'a': (219, 132, 7, 255),   # amba shadow
    'A': (255, 191, 15, 255),  # radiant yellow amba
    # Herbs & potato
    'g': (49, 107, 33, 255),   # parsley
    't': (212, 173, 97, 255),  # potato chunk
}

FOOD_ITEMS['sabich'] = (SABICH_PALETTE, """
................
....pPPp........
...PBBBBp.pp....
..PBEeEEmpeep...
..PBeEmAAaEmep..
.PBeEaAWWyAmep..
.pBmaAWWWyAtep..
.pBaAWWWyytmPp..
.pBAAWyytgePBp..
.pBAyytgmmPBBp..
.pBmgeemmPPBBPp.
..pBmmmPPBBBPp..
..pBBPPBBBBBPp..
...pPPBBBBBPp...
....ppPPPPpp....
................
""")

# 7. SUFGANIYAH: Plump golden Hanukkah donut, powdered sugar snow, bursting red jelly dollop
SUFGANIYAH_PALETTE = {
    '.': (0, 0, 0, 0),
    'c': (102, 51, 15, 255),   # deep fried crust
    'C': (168, 97, 35, 255),   # golden fried dough
    'd': (217, 147, 67, 255),  # donut midtone
    'D': (240, 187, 115, 255), # fluffy donut surface
    's': (222, 222, 237, 255), # powdered sugar shading
    'S': (255, 255, 255, 255), # pure powdered sugar
    'j': (133, 12, 30, 255),   # dark jelly center
    'J': (196, 22, 47, 255),   # bright ruby jam
    'h': (237, 74, 98, 255),   # jelly glossy shine
}

FOOD_ITEMS['sufganiyah'] = (SUFGANIYAH_PALETTE, """
................
......cccc......
....ccJjjJcc....
...cJJhhJJJjc...
..cSSjJJJjSSSc..
.cSSsdjJjdssSSc.
.cSsddDddDdsSSc.
csddDDDDDDDddsCc
cDdDDDDDDDDDddCc
cDDDDDDDDDDDDCCc
cDDDDDDDDDDDDCCc
.cDDDDDDDDDDCCc.
.cCCDDDDDDCCCCc.
..ccCCCCCCCCcc..
....cccccccc....
................
""")

# 8. MATZO: Square crisp flatbread with toasted perforation lines & blisters
MATZO_PALETTE = {
    '.': (0, 0, 0, 0),
    'c': (107, 82, 45, 255),   # crisp edge outline
    'p': (64, 43, 19, 255),    # dark perforation holes
    't': (153, 114, 63, 255),  # toasted blister brown
    'T': (191, 151, 94, 255),  # light toasted crust
    'm': (224, 197, 148, 255), # unleavened matzo body
    'M': (242, 222, 184, 255), # pale cracker surface
    'h': (255, 242, 214, 255), # blister highlight
}

FOOD_ITEMS['matzo'] = (MATZO_PALETTE, """
................
.cccccccccccccc.
.cMmMmMmMtMmMMc.
.cmmpmtmpmpmtmc.
.cMmtMmMmMtMmMc.
.cmmmpmpmpmtmmc.
.cMtMhTtMmMhMMc.
.cmptmpmpmpmtmc.
.cMmMtMmMtMmMmc.
.cmmpmpmtmpmpmc.
.cMhTMmMmMtMmMc.
.cmptmpmtmpmtmc.
.cMtMmMmMhTMmMc.
.cTmtMmMmMtMmtc.
.cccccccccccccc.
................
""")

# 9. TAHINI: Glass jar with golden lid and creamy sesame paste with pouring drip
TAHINI_PALETTE = {
    '.': (0, 0, 0, 0),
    # Metal lid
    'l': (89, 73, 45, 255),    # lid shadow
    'L': (153, 129, 84, 255),  # brass lid
    'k': (212, 187, 138, 255), # lid highlight
    # Glass bottle
    'g': (64, 88, 94, 255),    # glass outline
    'G': (120, 155, 163, 255), # glass reflection
    'w': (235, 250, 252, 255), # glass bright shine
    # Tahini sesame paste
    's': (168, 144, 103, 255), # dark sesame paste
    'S': (209, 185, 140, 255), # creamy tahini
    'h': (232, 214, 179, 255), # light tahini swirl
    # Label
    'e': (54, 99, 48, 255),    # green label
    'E': (93, 153, 84, 255),   # label light
}

FOOD_ITEMS['tahini'] = (TAHINI_PALETTE, """
......lllll.....
.....lLLkkLl....
.....lLLLLLl....
....ggsssssgg...
...gwwsSSShhhg..
..gwGgsSSShhhsg.
..gwGgsshSSsssg.
..gwGgeeeEEesgg.
..gwGgeeeEEesgS.
..gwGgeeeEEesgSh
..gwGgsShhSssgSS
..gwGgsSSShssgg.
..gwggsSSShssg..
...ggsssssssgg..
....ggggggggg...
................
""")

# 10. HAMANTASH: Triangular folded Purim cookie with dark poppyseed/lekvar filling
HAMANTASH_PALETTE = {
    '.': (0, 0, 0, 0),
    'c': (105, 59, 21, 255),   # baked crust outline
    'C': (166, 104, 43, 255),  # golden pastry crust
    'p': (217, 156, 80, 255),  # warm cookie dough
    'P': (242, 194, 126, 255), # cookie face
    'h': (255, 222, 168, 255), # dough highlight
    # Poppy seed / chocolate filling (Mohn)
    'm': (31, 18, 13, 255),    # dark poppy seed shadow
    'M': (59, 36, 27, 255),    # poppy seed filling
    'f': (92, 59, 46, 255),    # filling texture
    's': (133, 91, 74, 255),   # filling specular
}

FOOD_ITEMS['hamantash'] = (HAMANTASH_PALETTE, """
................
.......cc.......
......cPPc......
.....cPhhhc.....
....cPhPCPPc....
...cPhPcmmCPc...
...cPhPcmmMCPc..
..cPhPcMmMfMCPc.
.cPhPcmMfsMmMCPc
.cPPccMmmMfsMMcc
.cPhPcmmMmmMMCPc
cPhPPPPcmmMPPCCc
cPPPPPPPccPPPPcc
.ccCCCCCCcCCCc..
...cccccccc.....
................
""")

# 11. OLIVES: Branch with green and black Kalamata olives and silver-green leaves
OLIVES_PALETTE = {
    '.': (0, 0, 0, 0),
    'w': (69, 44, 21, 255),    # wood twig
    'W': (112, 77, 43, 255),   # wood light
    # Olive leaves (silver-green)
    'l': (51, 74, 43, 255),    # leaf shadow
    'L': (85, 117, 74, 255),   # leaf green
    'e': (136, 166, 124, 255), # silver-green underside
    # Green olive
    'g': (49, 64, 23, 255),    # green olive shadow
    'G': (86, 107, 44, 255),   # olive green
    'y': (136, 163, 75, 255),  # green olive highlight
    'Y': (184, 209, 123, 255), # specular shine
    # Kalamata black olive
    'k': (23, 14, 28, 255),    # deep purple-black shadow
    'K': (54, 33, 66, 255),    # kalamata purple
    'v': (89, 58, 107, 255),   # violet highlight
    'V': (135, 96, 158, 255),  # purple specular
}

FOOD_ITEMS['olives'] = (OLIVES_PALETTE, """
............wW..
..........wWll..
........wWlllL..
.......wlLLeL...
......wLleL.....
.....wLle..ggg..
...llLl...gYyGg.
..llLLe..gYyyGGg
.lLLeL...gyyGGgg
.LeL.kkk..gGGgg.
..e.kVKk...ggg..
...kVvKKk.......
...kVvKKkk......
...kvKKkkk......
....kkkkk.......
................
""")

# 12. GRAPES: Cluster of deep purple grapes with curled vine and emerald leaf
GRAPES_PALETTE = {
    '.': (0, 0, 0, 0),
    'v': (84, 52, 23, 255),    # vine stalk
    'V': (128, 85, 43, 255),   # vine highlight
    # Grape leaf
    'l': (36, 79, 24, 255),    # leaf shadow
    'L': (64, 130, 42, 255),   # leaf green
    'e': (109, 184, 73, 255),  # bright leaf
    # Purple grapes
    'p': (28, 10, 43, 255),    # grape deep shadow
    'P': (54, 22, 79, 255),    # dark purple
    'u': (88, 39, 125, 255),   # royal purple
    'U': (130, 65, 181, 255),  # grape highlight
    'h': (178, 116, 230, 255), # specular grape bloom
}

FOOD_ITEMS['grapes'] = (GRAPES_PALETTE, """
.......vV.......
.....ll.v.lle...
....lLLlvvLLLe..
...lLeLLLvLle...
....llLle.v.....
......pp..pp....
.....phUp.phUp..
....pUUuPpUUuPp.
....pUuuppUuupp.
...ppup.ppup.pp.
..phUppphUppphUp
..pUuPppUuPppUuP
...pp...pUup.pp.
.......ppup.....
......phUpp.....
.......pp.......
""")

# 13. DATES: Cluster of wrinkled, glossy Medjool dates on palm branch
DATES_PALETTE = {
    '.': (0, 0, 0, 0),
    's': (110, 89, 39, 255),   # palm stalk shadow
    'S': (161, 133, 66, 255),  # golden palm stalk
    # Medjool dates
    'd': (51, 23, 10, 255),    # deep wrinkled date shadow
    'D': (92, 45, 20, 255),    # amber date skin
    'm': (140, 72, 34, 255),   # rich caramel date flesh
    'M': (184, 107, 53, 255),  # golden date highlight
    'h': (222, 149, 93, 255),  # honey gloss shine
}

FOOD_ITEMS['dates'] = (DATES_PALETTE, """
........sS......
.......sS.......
......sS.ddd....
.....sS.dhMDd...
....sS.dhmMDDd..
...sS..dhmMDDd..
...sS.d.dMDDdd..
..sS.dhM.ddd.d..
..s.dhmMDd..dhMd
...dhmMDDd.dhmMD
...dhmMDDd.dhMMD
....dMDDdd..dMDd
.....ddd.....dd.
................
................
................
""")

# 14. CITRUS: Etrog / citron fruit with textured peel, green stem & pitam
CITRUS_PALETTE = {
    '.': (0, 0, 0, 0),
    # Stem
    't': (49, 74, 26, 255),    # green stem shadow
    'T': (83, 117, 49, 255),   # green stem light
    # Citrus body (Etrog)
    'c': (148, 106, 12, 255),  # rind shadow
    'C': (199, 149, 18, 255),  # warm golden peel
    'y': (235, 184, 28, 255),  # bright citron yellow
    'Y': (255, 213, 56, 255),  # peel highlight
    'h': (255, 237, 122, 255), # specular highlight
    # Pitam (woody tip at the bottom)
    'p': (87, 62, 26, 255),    # woody brown
    'P': (130, 97, 47, 255),   # light wood
}

FOOD_ITEMS['citrus'] = (CITRUS_PALETTE, """
........tT......
........tT......
.......ccCc.....
.....ccCYYycc...
...ccCYYhYYyCc..
..cCYhhYYYYyyCc.
.cCYhYYYYYYYYyCc
.cYYYYYYYYYYYyCc
.cYYYYYYYYYYYyCc
.cYYYYYYYYYYyCCc
..cYYYYYYYYyCCc.
...cYYYYYYyCCc..
....ccCYYyCCc...
......ccCcc.....
........p.......
.......pPp......
""")

# olives, dates and citrus are now generated by scripts/textures/gen_crop_textures.py
# (matching the fruiting-leaves stages); don't overwrite them here.
for _moved in ('olives', 'dates', 'citrus'):
    FOOD_ITEMS.pop(_moved, None)


def main():
    target_dir = os.path.abspath("src/main/resources/assets/israel_simulator/textures/item")
    os.makedirs(target_dir, exist_ok=True)
    print(f"Generating 14 food textures in: {target_dir}")
    for name, (palette, art) in FOOD_ITEMS.items():
        img = parse_ascii_art(art, palette)
        out_path = os.path.join(target_dir, f"{name}.png")
        img.save(out_path, "PNG")
        colors = len(set(img.getdata()))
        print(f"  [OK] {name}.png ({img.width}x{img.height}) with {colors} unique colors -> {out_path}")

if __name__ == "__main__":
    main()
