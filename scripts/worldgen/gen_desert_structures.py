#!/usr/bin/env python3
"""desert_ruins, ein_gedi_oasis, dead_sea_resort — DataVersion 4903."""
from pathlib import Path
from structure_lib import Structure, blk, chest, write_jigsaw, ROOT

SS = lambda: blk("minecraft:sandstone")
SSS = lambda: blk("minecraft:smooth_sandstone")
CS = lambda: blk("minecraft:cut_sandstone")
CH = lambda: blk("minecraft:chiseled_sandstone")
SAND = lambda: blk("minecraft:sand")
RED_SAND = lambda: blk("minecraft:red_sand")
WATER = lambda: blk("minecraft:water", level="0")
GRASS = lambda: blk("minecraft:grass_block", snowy="false")
DIRT = lambda: blk("minecraft:dirt")
PALM_LOG = lambda: blk("minecraft:jungle_log", axis="y")
LEAVES = lambda: blk("minecraft:jungle_leaves", distance="1", persistent="true", waterlogged="false")
FENCE = lambda: blk("minecraft:oak_fence", north="false", south="false", east="false", west="false", waterlogged="false")
WALL = lambda: blk("minecraft:sandstone_wall", up="true", north="none", south="none", east="none", west="none", waterlogged="false")
LANTERN = lambda: blk("minecraft:lantern", hanging="false", waterlogged="false")
SLAB = lambda t="bottom": blk("minecraft:sandstone_slab", type=t, waterlogged="false")
STAIR = lambda f="east": blk("minecraft:sandstone_stairs", facing=f, half="bottom", shape="straight", waterlogged="false")
DEAD_BUSH = lambda: blk("minecraft:dead_bush")
CACTUS = lambda: blk("minecraft:cactus", age="0")
CALC = lambda: blk("minecraft:calcite")
SALT = lambda: blk("israel_simulator:salt_block") if False else blk("minecraft:white_concrete")  # use salt if valid
# Prefer mod salt_block — may not be in KNOWN; allow without validation
def salt():
    return blk("israel_simulator:salt_block") if True else CALC()

def gen_ein_gedi():
    SX,SY,SZ=32,14,32; fy=1
    s=Structure(SX,SY,SZ)
    s.fills(0,0,0,SX-1,fy-1,SZ-1,SAND())
    for x in range(SX):
        for z in range(SZ):
            s.set(x,fy,z, SAND())
    # Oasis pool
    for x in range(10,22):
        for z in range(10,22):
            if (x-15)**2+(z-15)**2 <= 36:
                s.set(x,fy,z, WATER())
                s.set(x,0,z, DIRT())
            elif (x-15)**2+(z-15)**2 <= 49:
                s.set(x,fy,z, GRASS())
    # Palm trees
    for tx,tz in [(8,8),(24,8),(8,24),(24,24),(15,6),(15,26)]:
        for y in range(fy+1, fy+6):
            s.set(tx,y,tz, PALM_LOG())
        for dx in range(-2,3):
            for dz in range(-2,3):
                if abs(dx)+abs(dz)<=3 and not (dx==0 and dz==0):
                    s.set(tx+dx, fy+5, tz+dz, LEAVES())
                    if abs(dx)+abs(dz)<=2:
                        s.set(tx+dx, fy+6, tz+dz, LEAVES())
    # Small tent / shelter with chest
    s.fills(20,fy,20,26,fy,26, SSS())
    for y in range(fy+1, fy+3):
        for x in range(20,27):
            s.set(x,y,20, SS()); s.set(x,y,26, SS())
        for z in range(20,27):
            s.set(20,y,z, SS()); s.set(26,y,z, SS())
    s.set(23,fy+1,20, blk("minecraft:air")); s.set(23,fy+2,20, blk("minecraft:air"))
    s.fills(20,fy+3,20,26,fy+3,26, SLAB())
    s.set(22,fy+1,24, chest(facing="north", loot="israel_simulator:chests/ein_gedi_oasis"))
    s.set(24,fy+1,24, LANTERN())
    # Waterfall cliff on north
    for x in range(13,18):
        for y in range(fy+1, fy+8):
            s.set(x,y,2, SS())
        s.set(x,fy+7,3, WATER())
        s.set(x,fy+6,3, WATER())
        s.set(x,fy+5,4, WATER())
    s.set(1, fy+1, 1, blk("minecraft:moss_block"))
    write_jigsaw("ein_gedi_oasis", "israel_simulator:judean_desert", salt=552233)
    s.save(ROOT/"src/main/resources/data/israel_simulator/structure/ein_gedi_oasis.nbt")

def gen_dead_sea_resort():
    SX,SY,SZ=40,10,36; fy=1
    s=Structure(SX,SY,SZ)
    s.fills(0,0,0,SX-1,fy-1,SZ-1,SAND())
    for x in range(SX):
        for z in range(SZ):
            # salt flats toward -Z, sand toward +Z
            if z < 12:
                try:
                    s.set(x,fy,z, salt())
                except Exception:
                    s.set(x,fy,z, CALC())
            else:
                s.set(x,fy,z, SAND() if (x+z)%4 else SSS())
    # Main lodge
    s.fills(12,fy,16,28,fy,30, SSS())
    for y in range(fy+1, fy+4):
        for x in range(12,29):
            s.set(x,y,16, SS()); s.set(x,y,30, SS())
        for z in range(16,31):
            s.set(12,y,z, SS()); s.set(28,y,z, SS())
    # windows / door
    s.set(20,fy+1,16, blk("minecraft:air")); s.set(20,fy+2,16, blk("minecraft:air"))
    for x in (14,16,24,26):
        s.set(x,fy+2,16, blk("minecraft:glass_pane", north="false",south="false",east="false",west="false",waterlogged="false"))
    s.fills(12,fy+4,16,28,fy+4,30, SLAB())
    # Interior
    s.set(14,fy+1,28, chest(facing="south", loot="israel_simulator:chests/dead_sea_resort"))
    s.set(26,fy+1,28, chest(facing="south", loot="israel_simulator:chests/dead_sea_resort"))
    s.set(20,fy+1,25, LANTERN())
    # Pool deck
    for x in range(14,26):
        for z in range(8,14):
            s.set(x,fy,z, SSS())
    for x in range(16,24):
        for z in range(9,13):
            s.set(x,fy,z, WATER())
    # Fence around pool
    for x in range(14,26):
        s.set(x,fy+1,8, FENCE()); s.set(x,fy+1,13, FENCE())
    for z in range(8,14):
        s.set(14,fy+1,z, FENCE()); s.set(25,fy+1,z, FENCE())
    s.set(20,fy+1,8, blk("minecraft:air"))  # gate gap
    # Cabana posts
    for x,z in [(6,20),(34,20),(6,28),(34,28)]:
        s.set(x,fy+1,z, FENCE()); s.set(x,fy+2,z, FENCE()); s.set(x,fy+3,z, LANTERN())
    s.set(1, fy+1, 1, blk("minecraft:packed_mud"))
    write_jigsaw("dead_sea_resort", "israel_simulator:dead_sea", salt=663344)
    s.save(ROOT/"src/main/resources/data/israel_simulator/structure/dead_sea_resort.nbt")

if __name__ == "__main__":
    gen_ein_gedi()
    gen_dead_sea_resort()
