#!/usr/bin/env python3
"""jaffa_port, mediterranean_village, tel_aviv_city — DataVersion 4903."""
from structure_lib import Structure, blk, chest, write_jigsaw, ROOT

def fence(name="oak_fence"):
    return blk(f"minecraft:{name}", north="false", south="false", east="false", west="false", waterlogged="false")
def wall(name="cobblestone_wall"):
    return blk(f"minecraft:{name}", up="true", north="none", south="none", east="none", west="none", waterlogged="false")
def pane(name="glass_pane"):
    return blk(f"minecraft:{name}", north="false", south="false", east="false", west="false", waterlogged="false")
def lantern():
    return blk("minecraft:lantern", hanging="false", waterlogged="false")
def barrel(facing="up"):
    return blk("minecraft:barrel", facing=facing, open="false")
def slab(name="oak_slab", t="bottom"):
    return blk(f"minecraft:{name}", type=t, waterlogged="false")
def stair(name="oak_stairs", f="east"):
    return blk(f"minecraft:{name}", facing=f, half="bottom", shape="straight", waterlogged="false")

def gen_jaffa():
    SX,SY,SZ=40,12,36; fy=1
    s=Structure(SX,SY,SZ)
    s.fills(0,0,0,SX-1,fy-1,SZ-1, blk("minecraft:sand"))
    for x in range(SX):
        for z in range(SZ):
            if z < 10:
                s.set(x,fy,z, blk("minecraft:water", level="0"))
            elif z < 14:
                s.set(x,fy,z, blk("minecraft:sand"))
            else:
                s.set(x,fy,z, blk("minecraft:smooth_sandstone") if (x+z)%3==0 else blk("minecraft:sandstone"))
    # Pier into water
    for x in range(16,24):
        for z in range(2,14):
            s.set(x,fy,z, blk("minecraft:oak_planks"))
            if z < 10:
                s.set(x,0,z, blk("minecraft:oak_log", axis="y"))
    for z in (2,6,10):
        s.set(16,fy+1,z, fence()); s.set(23,fy+1,z, fence())
    # Warehouse
    s.fills(8,fy,18,20,fy,30, blk("minecraft:stone_bricks"))
    for y in range(fy+1, fy+4):
        for x in range(8,21):
            s.set(x,y,18, blk("minecraft:stone_bricks")); s.set(x,y,30, blk("minecraft:stone_bricks"))
        for z in range(18,31):
            s.set(8,y,z, blk("minecraft:stone_bricks")); s.set(20,y,z, blk("minecraft:stone_bricks"))
    s.set(14,fy+1,18, blk("minecraft:air")); s.set(14,fy+2,18, blk("minecraft:air"))
    s.fills(8,fy+4,18,20,fy+4,30, slab("stone_brick_slab"))
    s.set(10,fy+1,28, chest(facing="south", loot="israel_simulator:chests/jaffa_flea_market"))
    s.set(18,fy+1,28, blk("minecraft:barrel", facing="up", open="false"))
    # Market stalls
    for x0 in (24, 30):
        s.fills(x0,fy,20,x0+4,fy,26, blk("minecraft:oak_planks"))
        for y in range(fy+1, fy+3):
            s.set(x0,y,20, fence()); s.set(x0+4,y,20, fence())
            s.set(x0,y,26, fence()); s.set(x0+4,y,26, fence())
        s.fills(x0,fy+3,20,x0+4,fy+3,26, slab())
        s.set(x0+2,fy+1,23, chest(facing="south", loot="israel_simulator:chests/jaffa_flea_market"))
    s.set(1,fy+1,1, blk("minecraft:prismarine_bricks"))
    write_jigsaw("jaffa_port", "israel_simulator:mediterranean_coast", salt=771122)
    s.save(ROOT/"src/main/resources/data/israel_simulator/structure/jaffa_port.nbt")

def gen_tel_aviv():
    SX, SY, SZ = 48, 18, 48
    fy = 1
    s = Structure(SX, SY, SZ)
    s.fills(0, 0, 0, SX - 1, fy - 1, SZ - 1, blk("minecraft:stone"))

    # Base pavement: city street grid
    for x in range(SX):
        for z in range(SZ):
            if x < 6:  # Tayelet Beach coast border
                s.set(x, fy, z, blk("minecraft:sand") if x < 3 else blk("minecraft:oak_planks"))
            elif (x in (6, 7, 23, 24, 46, 47) or z in (0, 1, 23, 24, 46, 47)):
                s.set(x, fy, z, blk("minecraft:gray_concrete"))  # Streets & bike lanes
            else:
                s.set(x, fy, z, blk("minecraft:smooth_stone") if (x + z) % 2 == 0 else blk("minecraft:white_concrete"))

    # 1. TAYELET & BEACHFRONT (x=0..6, z=0..47)
    for z in range(4, 44, 8):
        # Beach umbrellas (wool)
        s.set(2, fy + 1, z, fence("oak_fence"))
        s.set(2, fy + 2, z, fence("oak_fence"))
        s.set(2, fy + 3, z, blk("minecraft:red_wool" if z % 16 == 4 else "light_blue_wool"))
        # Beach loungers
        s.set(1, fy + 1, z, stair("oak_stairs", f="west"))

    # 2. ROTHSCHILD BOULEVARD (Central pedestrian median: x=21..26, z=2..45)
    for z in range(2, 46):
        s.set(23, fy, z, blk("minecraft:dirt_path"))
        s.set(24, fy, z, blk("minecraft:dirt_path"))
        # Shaded tree promenade
        if z % 6 == 0:
            for y in range(fy + 1, fy + 4):
                s.set(23, y, z, blk("minecraft:oak_log", axis="y"))
            for dx in range(-1, 2):
                for dz in range(-1, 2):
                    s.set(23 + dx, fy + 4, z + dz, blk("minecraft:oak_leaves", distance="1", persistent="true", waterlogged="false"))
        # Benches & lighting
        if z % 6 == 3:
            s.set(22, fy + 1, z, stair("spruce_stairs", f="east"))
            s.set(25, fy + 1, z, stair("spruce_stairs", f="west"))
            s.set(23, fy + 1, z, lantern())
    # Rothschild Green Coffee Kiosk (x=22..25, z=21..24)
    s.fills(22, fy + 1, 21, 25, fy + 3, 24, blk("minecraft:green_concrete"))
    s.set(23, fy + 1, 21, blk("minecraft:air"))
    s.set(24, fy + 1, 21, blk("minecraft:air"))
    s.fills(22, fy + 4, 21, 25, fy + 4, 24, slab("oak_slab"))
    s.set(23, fy + 1, 23, barrel("up"))

    # 3. WHITE CITY (NW: x=8..20, z=4..20) - Bauhaus architecture
    bx0, bz0 = 8, 4
    bw, bd, bh = 12, 16, 9
    s.fills(bx0, fy, bz0, bx0 + bw - 1, fy, bz0 + bd - 1, blk("minecraft:smooth_stone"))
    for y in range(fy + 1, fy + bh + 1):
        for x in range(bx0, bx0 + bw):
            s.set(x, y, bz0, blk("minecraft:white_concrete"))
            s.set(x, y, bz0 + bd - 1, blk("minecraft:white_concrete"))
        for z in range(bz0, bz0 + bd):
            s.set(bx0, y, z, blk("minecraft:white_concrete"))
            s.set(bx0 + bw - 1, y, z, blk("minecraft:white_concrete"))
        # Ribbon windows & balconies (Bauhaus hallmark)
        if y in (fy + 3, fy + 6):
            for x in range(bx0 + 2, bx0 + bw - 2):
                s.set(x, y, bz0, pane("glass_pane"))
                s.set(x, y, bz0 + bd - 1, pane("glass_pane"))
    s.set(bx0 + bw // 2, fy + 1, bz0, blk("minecraft:air"))
    s.set(bx0 + bw // 2, fy + 2, bz0, blk("minecraft:air"))
    s.fills(bx0, fy + bh + 1, bz0, bx0 + bw - 1, fy + bh + 1, bz0 + bd - 1, slab("smooth_stone_slab"))
    s.set(bx0 + 2, fy + 1, bz0 + bd - 2, chest(facing="north", loot="israel_simulator:chests/tel_aviv_apartment"))

    # 4. STARTUP & TECHNOLOGY DISTRICT (NE: x=28..44, z=4..20) - Modern high-tech skyscraper
    tx0, tz0 = 28, 4
    tw, td, th = 16, 16, 14
    s.fills(tx0, fy, tz0, tx0 + tw - 1, fy, tz0 + td - 1, blk("minecraft:deepslate_bricks"))
    for y in range(fy + 1, fy + th + 1):
        for x in range(tx0, tx0 + tw):
            s.set(x, y, tz0, blk("minecraft:cyan_concrete") if x in (tx0, tx0 + tw - 1) else pane("cyan_stained_glass_pane"))
            s.set(x, y, tz0 + td - 1, blk("minecraft:cyan_concrete") if x in (tx0, tx0 + tw - 1) else pane("cyan_stained_glass_pane"))
        for z in range(tz0, tz0 + td):
            s.set(tx0, y, z, blk("minecraft:cyan_concrete") if z in (tz0, tz0 + td - 1) else pane("cyan_stained_glass_pane"))
            s.set(tx0 + tw - 1, y, z, blk("minecraft:cyan_concrete") if z in (tz0, tz0 + td - 1) else pane("cyan_stained_glass_pane"))
    s.set(tx0 + tw // 2, fy + 1, tz0, blk("minecraft:air"))
    s.set(tx0 + tw // 2, fy + 2, tz0, blk("minecraft:air"))
    s.fills(tx0, fy + th + 1, tz0, tx0 + tw - 1, fy + th + 1, tz0 + td - 1, slab("smooth_stone_slab"))
    # IT workstations & server rack
    s.set(tx0 + 3, fy + 1, tz0 + 3, blk("minecraft:sea_lantern"))
    s.set(tx0 + 4, fy + 1, tz0 + 3, blk("minecraft:iron_bars", north="false",south="false",east="false",west="false",waterlogged="false"))
    s.set(tx0 + 5, fy + 1, tz0 + 3, chest(facing="south", loot="israel_simulator:chests/tel_aviv_tech_office"))

    # 5. FLORENTIN BOHEMIAN ARTISAN QUARTER (SW: x=8..20, z=27..43) - Brick lofts & street art
    fx0, fz0 = 8, 27
    fw, fd, fh = 12, 16, 7
    s.fills(fx0, fy, fz0, fx0 + fw - 1, fy, fz0 + fd - 1, blk("minecraft:terracotta"))
    for y in range(fy + 1, fy + fh + 1):
        for x in range(fx0, fx0 + fw):
            s.set(x, y, fz0, blk("minecraft:bricks"))
            s.set(x, y, fz0 + fd - 1, blk("minecraft:bricks"))
        for z in range(fz0, fz0 + fd):
            s.set(fx0, y, z, blk("minecraft:bricks"))
            s.set(fx0 + fw - 1, y, z, blk("minecraft:bricks"))
    # Street art mural on exterior wall (x=fx0)
    for my in range(fy + 1, fy + 5):
        s.set(fx0, my, fz0 + 4, blk("minecraft:yellow_terracotta"))
        s.set(fx0, my, fz0 + 5, blk("minecraft:red_terracotta"))
        s.set(fx0, my, fz0 + 6, blk("minecraft:light_blue_terracotta"))
        s.set(fx0, my, fz0 + 7, blk("minecraft:purple_terracotta"))
    s.set(fx0 + fw // 2, fy + 1, fz0, blk("minecraft:air"))
    s.set(fx0 + fw // 2, fy + 2, fz0, blk("minecraft:air"))
    s.fills(fx0, fy + fh + 1, fz0, fx0 + fw - 1, fy + fh + 1, fz0 + fd - 1, slab("brick_slab"))
    s.set(fx0 + 2, fy + 1, fz0 + 2, barrel("up"))
    s.set(fx0 + 3, fy + 1, fz0 + 2, chest(facing="south", loot="israel_simulator:chests/tel_aviv_apartment"))

    # 6. SARONA TEMPLAR HERITAGE & MARKET (SE: x=28..44, z=27..43) - Restored stone & culinary market
    sx0, sz0 = 28, 27
    sw, sd, sh = 16, 16, 8
    s.fills(sx0, fy, sz0, sx0 + sw - 1, fy, sz0 + sd - 1, blk("minecraft:cut_sandstone"))
    for y in range(fy + 1, fy + sh + 1):
        for x in range(sx0, sx0 + sw):
            s.set(x, y, sz0, blk("minecraft:sandstone"))
            s.set(x, y, sz0 + sd - 1, blk("minecraft:sandstone"))
        for z in range(sz0, sz0 + sd):
            s.set(sx0, y, z, blk("minecraft:sandstone"))
            s.set(sx0 + sw - 1, y, z, blk("minecraft:sandstone"))
    s.set(sx0 + sw // 2, fy + 1, sz0, blk("minecraft:air"))
    s.set(sx0 + sw // 2, fy + 2, sz0, blk("minecraft:air"))
    # Restored red-tiled roof
    s.fills(sx0, fy + sh + 1, sz0, sx0 + sw - 1, fy + sh + 1, sz0 + sd - 1, slab("sandstone_slab"))
    s.set(sx0 + 3, fy + 1, sz0 + 3, blk("minecraft:crafting_table"))
    s.set(sx0 + 4, fy + 1, sz0 + 3, barrel("up"))
    s.set(sx0 + 5, fy + 1, sz0 + 3, chest(facing="south", loot="israel_simulator:chests/tel_aviv_apartment"))

    # Required Framework Marker Block
    s.set(1, fy + 1, 1, blk("minecraft:light_blue_stained_glass"))

    write_jigsaw("tel_aviv_city", "israel_simulator:urban_area", salt=993344)
    s.save(ROOT / "src/main/resources/data/israel_simulator/structure/tel_aviv_city.nbt")

if __name__ == "__main__":
    gen_jaffa(); gen_tel_aviv()  # mediterranean_village: gen_mediterranean_village.py
