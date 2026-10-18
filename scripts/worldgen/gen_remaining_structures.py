#!/usr/bin/env python3
"""Generate the remaining real structures (DataVersion 4903):
- grand_market (covered Shuk bazaar)
- startup_office (modern Silicon Alley office)
- historical_house (heritage Jerusalem stone residence)
- synagogue (community prayer hall and Aron Kodesh)
- great_synagogue (monumental cathedral synagogue)
"""
from __future__ import annotations
import sys
from pathlib import Path

# Add scripts/worldgen to path
sys.path.insert(0, str(Path(__file__).resolve().parent))
from structure_lib import Structure, blk, chest, write_jigsaw, ROOT
import exteriors

def slab(name="smooth_stone_slab", t="bottom"):
    return blk(f"minecraft:{name}", type=t, waterlogged="false")

def stair(name="stone_brick_stairs", f="north", half="bottom"):
    return blk(f"minecraft:{name}", facing=f, half=half, shape="straight", waterlogged="false")

def wall(name="cobblestone_wall"):
    return blk(f"minecraft:{name}", up="true", north="none", south="none", east="none", west="none", waterlogged="false")

def fence(name="oak_fence"):
    return blk(f"minecraft:{name}", north="false", south="false", east="false", west="false", waterlogged="false")

def pane(name="glass_pane"):
    return blk(f"minecraft:{name}", north="false", south="false", east="false", west="false", waterlogged="false")

def lantern(hanging="false"):
    return blk("minecraft:lantern", hanging=hanging, waterlogged="false")

def barrel(facing="up"):
    return blk("minecraft:barrel", facing=facing, open="false")


def gen_startup_office():
    """High-tech startup venture office (Tel Aviv Silicon Alley).
    Marker: minecraft:iron_block
    Loot: israel_simulator:chests/startup_office
    """
    SX, SY, SZ = 26, 17, 26
    fy = 1
    s = Structure(SX, SY, SZ)
    s.fills(0, 0, 0, SX - 1, fy - 1, SZ - 1, blk("minecraft:stone"))

    # Polished concrete floors
    for x in range(SX):
        for z in range(SZ):
            if x in (0, SX - 1) or z in (0, SZ - 1):
                s.set(x, fy, z, blk("minecraft:gray_concrete"))
            else:
                s.set(x, fy, z, blk("minecraft:smooth_stone") if (x + z) % 3 == 0 else blk("minecraft:light_gray_concrete"))

    # Building bounds: 2..23 x 2..23
    x0, x1 = 2, 23
    z0, z1 = 2, 23
    h = 10

    # Modern curtain wall facade: white concrete corners, glass walls
    for y in range(fy + 1, fy + h + 1):
        for x in range(x0, x1 + 1):
            if x in (x0, x0 + 1, x1 - 1, x1):
                s.set(x, y, z0, blk("minecraft:white_concrete"))
                s.set(x, y, z1, blk("minecraft:white_concrete"))
            else:
                s.set(x, y, z0, pane("cyan_stained_glass_pane"))
                s.set(x, y, z1, pane("cyan_stained_glass_pane"))
        for z in range(z0, z1 + 1):
            if z in (z0, z0 + 1, z1 - 1, z1):
                s.set(x0, y, z, blk("minecraft:white_concrete"))
                s.set(x1, y, z, blk("minecraft:white_concrete"))
            else:
                s.set(x0, y, z, pane("cyan_stained_glass_pane"))
                s.set(x1, y, z, pane("cyan_stained_glass_pane"))

    # Entrance double doors at front (x=12..13, z=z0)
    s.set(12, fy + 1, z0, blk("minecraft:air"))
    s.set(12, fy + 2, z0, blk("minecraft:air"))
    s.set(13, fy + 1, z0, blk("minecraft:air"))
    s.set(13, fy + 2, z0, blk("minecraft:air"))

    # Second floor slab at fy+5
    s.fills(x0 + 1, fy + 5, z0 + 1, x1 - 1, fy + 5, z1 - 1, slab("smooth_stone_slab", "top"))
    # Staircase to upper level
    for i in range(4):
        s.set(20 - i, fy + 1 + i, 19, stair("polished_deepslate_stairs", f="west"))
        s.set(20 - i, fy + 1 + i, 20, blk("minecraft:polished_deepslate"))
    s.set(16, fy + 5, 19, blk("minecraft:air"))
    s.set(17, fy + 5, 19, blk("minecraft:air"))

    # Modern roof with solar panels & AC units
    s.fills(x0, fy + h + 1, z0, x1, fy + h + 1, z1, slab("deepslate_tile_slab", "bottom"))
    for sx in (6, 7, 8, 16, 17, 18):
        for sz in (6, 7, 16, 17):
            s.set(sx, fy + h + 1, sz, blk("minecraft:daylight_detector", inverted="false", power="0"))

    # Ground Floor: Open Space Developer Workstations
    for desk_x in (6, 10, 14):
        for desk_z in (6, 10):
            # Desk tables
            s.set(desk_x, fy + 1, desk_z, blk("minecraft:spruce_trapdoor", facing="north", half="top", open="false", powered="false", waterlogged="false"))
            s.set(desk_x + 1, fy + 1, desk_z, blk("minecraft:spruce_trapdoor", facing="north", half="top", open="false", powered="false", waterlogged="false"))
            # Chairs
            s.set(desk_x, fy + 1, desk_z + 1, stair("spruce_stairs", f="north"))
            s.set(desk_x + 1, fy + 1, desk_z + 1, stair("spruce_stairs", f="north"))
            # Laptop / keyboard (heavy weighted pressure plate)
            s.set(desk_x, fy + 2, desk_z, blk("minecraft:heavy_weighted_pressure_plate", power="0"))

    # Lounge area: couches and coffee station
    s.set(5, fy + 1, 18, stair("cyan_wool_stairs" if False else "spruce_stairs", f="east"))
    s.set(5, fy + 1, 19, stair("spruce_stairs", f="east"))
    s.set(5, fy + 1, 20, stair("spruce_stairs", f="east"))
    # Espresso machine (cauldron + tripwire hook)
    s.set(10, fy + 1, 20, blk("minecraft:cauldron"))
    s.set(11, fy + 1, 20, blk("minecraft:hopper", facing="down"))

    # Upper Floor: Server Room (z=5..12, x=4..12)
    for sx in (5, 7, 9):
        for sz in (6, 9):
            s.set(sx, fy + 6, sz, blk("minecraft:iron_bars", north="false", south="false", east="false", west="false", waterlogged="false"))
            s.set(sx, fy + 7, sz, blk("minecraft:observer", facing="north", powered="false"))
            s.set(sx, fy + 8, sz, blk("minecraft:copper_block"))
    # Server cooling / sea lantern
    s.set(7, fy + 6, 8, blk("minecraft:sea_lantern"))

    # Tech office chests
    s.set(18, fy + 1, 7, chest(facing="west", loot="israel_simulator:chests/startup_office"))
    s.set(11, fy + 6, 7, chest(facing="south", loot="israel_simulator:chests/startup_office"))
    s.set(5, fy + 6, 17, chest(facing="east", loot="israel_simulator:chests/tel_aviv_tech_office"))

    # Modern lighting: sea lanterns embedded in ceiling
    for lx in (7, 13, 18):
        for lz in (7, 13, 18):
            s.set(lx, fy + 5, lz, blk("minecraft:sea_lantern"))
            s.set(lx, fy + 10, lz, blk("minecraft:sea_lantern"))

    # Required Framework Marker Block
    s.set(1, fy + 1, 1, blk("minecraft:iron_block"))

    write_jigsaw("startup_office", "israel_simulator:urban_area", salt=334455)
    exteriors.startup_office(s, fy)
    s.save(ROOT / "src/main/resources/data/israel_simulator/structure/startup_office.nbt")


def gen_historical_house():
    """Traditional Jerusalem stone heritage house (Yemin Moshe / Old City style).
    Marker: minecraft:bricks
    Loot: israel_simulator:chests/historical_house
    """
    SX, SY, SZ = 24, 14, 24
    fy = 1
    s = Structure(SX, SY, SZ)
    s.fills(0, 0, 0, SX - 1, fy - 1, SZ - 1, blk("minecraft:dirt"))

    # Walled courtyard with paved stones and grass
    for x in range(SX):
        for z in range(SZ):
            if (x < 11 and z < 11):
                s.set(x, fy, z, blk("minecraft:grass_block", snowy="false") if (x + z) % 3 != 0 else blk("minecraft:dirt_path"))
            else:
                s.set(x, fy, z, blk("minecraft:smooth_sandstone") if (x + z) % 2 == 0 else blk("minecraft:cut_sandstone"))

    # Olive tree in courtyard (x=4, z=4)
    for ty in range(fy + 1, fy + 4):
        s.set(4, ty, 4, blk("minecraft:oak_log", axis="y"))
    for dx in range(-2, 3):
        for dz in range(-2, 3):
            if abs(dx) + abs(dz) <= 3:
                s.set(4 + dx, fy + 3, 4 + dz, blk("minecraft:oak_leaves", distance="1", persistent="true", waterlogged="false"))
                if abs(dx) + abs(dz) <= 2:
                    s.set(4 + dx, fy + 4, 4 + dz, blk("minecraft:oak_leaves", distance="1", persistent="true", waterlogged="false"))

    # Courtyard outer perimeter stone wall
    for x in range(12):
        s.set(x, fy + 1, 0, wall("sandstone_wall"))
    for z in range(12):
        s.set(0, fy + 1, z, wall("sandstone_wall"))
    # Wrought iron entry gate
    s.set(5, fy + 1, 0, blk("minecraft:iron_bars", north="false", south="false", east="false", west="false", waterlogged="false"))

    # House envelope: x=9..21, z=9..21, height 6
    hx0, hx1 = 9, 21
    hz0, hz1 = 9, 21
    hh = 6

    # Traditional stone & brick walls
    for y in range(fy + 1, fy + hh + 1):
        for x in range(hx0, hx1 + 1):
            mat = blk("minecraft:bricks") if y in (fy + 1, fy + hh) else blk("minecraft:sandstone")
            s.set(x, y, hz0, mat)
            s.set(x, y, hz1, mat)
        for z in range(hz0, hz1 + 1):
            mat = blk("minecraft:bricks") if y in (fy + 1, fy + hh) else blk("minecraft:sandstone")
            s.set(hx0, y, z, mat)
            s.set(hx1, y, z, mat)

    # Arched windows with shutters
    for win_x in (12, 15, 18):
        s.set(win_x, fy + 3, hz0, pane("glass_pane"))
        s.set(win_x, fy + 3, hz1, pane("glass_pane"))
    for win_z in (12, 15, 18):
        s.set(hx0, fy + 3, win_z, pane("glass_pane"))
        s.set(hx1, fy + 3, win_z, pane("glass_pane"))

    # Entrance door with Mezuzah post (x=11, z=hz0)
    s.set(11, fy + 1, hz0, blk("minecraft:air"))
    s.set(11, fy + 2, hz0, blk("minecraft:air"))

    # Flat domed roof with parapet / railing
    s.fills(hx0, fy + hh + 1, hz0, hx1, fy + hh + 1, hz1, slab("sandstone_slab", "bottom"))
    for x in range(hx0, hx1 + 1):
        s.set(x, fy + hh + 2, hz0, wall("sandstone_wall"))
        s.set(x, fy + hh + 2, hz1, wall("sandstone_wall"))
    for z in range(hz0, hz1 + 1):
        s.set(hx0, fy + hh + 2, z, wall("sandstone_wall"))
        s.set(hx1, fy + hh + 2, z, wall("sandstone_wall"))
    # Small domed center on roof
    s.fills(13, fy + hh + 2, 13, 17, fy + hh + 2, 17, blk("minecraft:smooth_sandstone"))
    s.fills(14, fy + hh + 3, 14, 16, fy + hh + 3, 16, slab("sandstone_slab", "bottom"))

    # Interior furnishing
    # Red & white Persian rug
    for rx in range(12, 18):
        for rz in range(12, 18):
            s.set(rx, fy + 1, rz, blk("minecraft:red_carpet") if (rx + rz) % 2 == 0 else blk("minecraft:white_carpet"))
    
    # Dining table & chairs
    s.set(14, fy + 1, 14, fence("oak_fence"))
    s.set(15, fy + 1, 14, fence("oak_fence"))
    s.set(14, fy + 2, 14, blk("minecraft:oak_pressure_plate", powered="false"))
    s.set(15, fy + 2, 14, blk("minecraft:oak_pressure_plate", powered="false"))
    s.set(13, fy + 1, 14, stair("oak_stairs", f="east"))
    s.set(16, fy + 1, 14, stair("oak_stairs", f="west"))

    # Hearth / Fireplace (x=20, z=14..15)
    s.set(20, fy + 1, 14, blk("minecraft:bricks"))
    s.set(20, fy + 1, 15, blk("minecraft:bricks"))
    s.set(20, fy + 2, 14, blk("minecraft:campfire", facing="west", lit="true", signal_fire="false", waterlogged="false"))

    # Kitchen pantry: barrels & crafting table
    s.set(19, fy + 1, 18, blk("minecraft:crafting_table"))
    s.set(19, fy + 1, 19, barrel("up"))
    s.set(20, fy + 1, 19, barrel("up"))

    # Bedroom area: bed & bookshelf
    s.set(11, fy + 1, 19, blk("minecraft:bookshelf"))
    s.set(11, fy + 2, 19, blk("minecraft:bookshelf"))

    # Historical chest
    s.set(19, fy + 1, 11, chest(facing="south", loot="israel_simulator:chests/historical_house"))
    s.set(12, fy + 1, 19, chest(facing="east", loot="israel_simulator:chests/historical_house"))

    # Lantern lighting
    s.set(15, fy + 5, 15, lantern(hanging="true"))
    s.set(2, fy + 2, 8, lantern())

    # Required Framework Marker Block
    s.set(1, fy + 1, 1, blk("minecraft:bricks"))

    write_jigsaw("historical_house", "israel_simulator:jerusalem", salt=445566)
    exteriors.historical_house(s, fy)
    s.save(ROOT / "src/main/resources/data/israel_simulator/structure/historical_house.nbt")


def gen_synagogue():
    """Community Synagogue prayer hall (Jerusalem).
    Marker: minecraft:purple_stained_glass
    Loot: israel_simulator:chests/synagogue & israel_simulator:chests/synagogue_ark
    """
    SX, SY, SZ = 26, 24, 26
    fy = 1
    s = Structure(SX, SY, SZ)
    s.fills(0, 0, 0, SX - 1, fy - 1, SZ - 1, blk("minecraft:smooth_sandstone"))

    # Paved stone sanctuary floor
    for x in range(SX):
        for z in range(SZ):
            s.set(x, fy, z, blk("minecraft:smooth_stone") if (x + z) % 2 == 0 else blk("minecraft:cut_sandstone"))

    x0, x1 = 2, 23
    z0, z1 = 2, 23
    h = 9

    # Stone perimeter walls
    for y in range(fy + 1, fy + h + 1):
        for x in range(x0, x1 + 1):
            s.set(x, y, z0, blk("minecraft:smooth_sandstone"))
            s.set(x, y, z1, blk("minecraft:smooth_sandstone"))
        for z in range(z0, z1 + 1):
            s.set(x0, y, z, blk("minecraft:smooth_sandstone"))
            s.set(x1, y, z, blk("minecraft:smooth_sandstone"))

    # Stained glass windows with purple & light blue
    for y in (fy + 4, fy + 5, fy + 6):
        for x in (6, 9, 16, 19):
            s.set(x, y, z0, pane("purple_stained_glass_pane"))
            s.set(x, y, z1, pane("purple_stained_glass_pane"))
        for z in (7, 11, 15, 19):
            s.set(x0, y, z, pane("light_blue_stained_glass_pane"))
            s.set(x1, y, z, pane("light_blue_stained_glass_pane"))

    # Entrance doors on West wall (x=x0, z=12..13)
    s.set(x0, fy + 1, 12, blk("minecraft:air"))
    s.set(x0, fy + 2, 12, blk("minecraft:air"))
    s.set(x0, fy + 1, 13, blk("minecraft:air"))
    s.set(x0, fy + 2, 13, blk("minecraft:air"))

    # Vaulted roof with barrel ceiling
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            s.set(x, fy + h + 1, z, slab("smooth_sandstone_slab", "bottom"))
    # Skylight above Bimah
    for x in range(11, 15):
        for z in range(11, 15):
            s.set(x, fy + h + 1, z, blk("minecraft:tinted_glass"))

    # Vestibule lobby: tzedakah box & water basin
    s.set(x0 + 2, fy + 1, 10, blk("minecraft:cauldron"))  # Handwashing
    s.set(x0 + 2, fy + 1, 15, chest(facing="east", loot="israel_simulator:chests/synagogue"))  # Tzedakah chest

    # Central Bimah (raised wooden platform at x=11..14, z=11..14)
    s.fills(11, fy + 1, 11, 14, fy + 1, 14, slab("oak_slab", "bottom"))
    s.fills(12, fy + 1, 12, 13, fy + 1, 13, blk("minecraft:oak_planks"))
    # Blue carpet on Bimah
    s.set(12, fy + 2, 12, blk("minecraft:blue_carpet"))
    s.set(13, fy + 2, 12, blk("minecraft:blue_carpet"))
    s.set(12, fy + 2, 13, blk("minecraft:blue_carpet"))
    s.set(13, fy + 2, 13, blk("minecraft:blue_carpet"))
    # Bimah reading table / lectern facing East
    s.set(13, fy + 2, 12, blk("minecraft:lectern", facing="east", has_book="true"))
    # Wooden railings around Bimah
    for bx in (11, 14):
        for bz in range(11, 15):
            if (bx, bz) != (11, 12):  # leave step open
                s.set(bx, fy + 2, bz, fence("dark_oak_fence"))

    # Aron Kodesh (Torah Ark) on Eastern wall (x=x1, z=11..14)
    for y in range(fy + 1, fy + 6):
        s.set(x1 - 1, y, 11, blk("minecraft:quartz_pillar", axis="y"))
        s.set(x1 - 1, y, 14, blk("minecraft:quartz_pillar", axis="y"))
        s.set(x1 - 1, y, 12, blk("minecraft:chiseled_quartz_block"))
        s.set(x1 - 1, y, 13, blk("minecraft:chiseled_quartz_block"))
    # Ark Pediment & Arch
    s.set(x1 - 1, fy + 6, 12, blk("minecraft:gold_block"))
    s.set(x1 - 1, fy + 6, 13, blk("minecraft:gold_block"))
    # Ner Tamid (Eternal Light) above Ark
    s.set(x1 - 2, fy + 5, 12, lantern(hanging="false"))
    # Sacred Ark Chest inside the Aron Kodesh
    s.set(x1 - 1, fy + 1, 12, chest(facing="west", loot="israel_simulator:chests/synagogue_ark"))
    s.set(x1 - 1, fy + 1, 13, blk("minecraft:chiseled_bookshelf", facing="west"))

    # Pews facing the Ark (East)
    for pz in (5, 7, 9, 16, 18, 20):
        for px in (7, 9, 17, 19):
            s.set(px, fy + 1, pz, stair("spruce_stairs", f="east"))
            s.set(px + 1, fy + 1, pz, stair("spruce_stairs", f="east"))

    # Bookshelves with prayer books along walls
    for z in (4, 5, 20, 21):
        s.set(5, fy + 1, z, blk("minecraft:bookshelf"))
        s.set(5, fy + 2, z, blk("minecraft:bookshelf"))

    # Hanging chandeliers
    for cx, cz in ((9, 8), (17, 8), (9, 17), (17, 17)):
        s.set(cx, fy + 7, cz, lantern(hanging="true"))

    # Required Framework Marker Block
    s.set(1, fy + 1, 1, blk("minecraft:purple_stained_glass"))

    write_jigsaw("synagogue", "israel_simulator:jerusalem", salt=556677)
    exteriors.synagogue(s, fy)
    s.save(ROOT / "src/main/resources/data/israel_simulator/structure/synagogue.nbt")


def gen_great_synagogue():
    """Monumental Jerusalem Great Synagogue.
    Marker: minecraft:blue_stained_glass
    Loot: israel_simulator:chests/synagogue_ark & israel_simulator:chests/synagogue
    """
    SX, SY, SZ = 36, 31, 36
    fy = 1
    s = Structure(SX, SY, SZ)
    s.fills(0, 0, 0, SX - 1, fy - 1, SZ - 1, blk("minecraft:stone"))

    # Polished diorite and smooth stone grand paved floor
    for x in range(SX):
        for z in range(SZ):
            if (x + z) % 3 == 0:
                s.set(x, fy, z, blk("minecraft:polished_diorite"))
            elif (x + z) % 3 == 1:
                s.set(x, fy, z, blk("minecraft:smooth_quartz"))
            else:
                s.set(x, fy, z, blk("minecraft:smooth_stone"))

    x0, x1 = 3, 32
    z0, z1 = 3, 32
    h = 14

    # Grand monumental stone facade
    for y in range(fy + 1, fy + h + 1):
        for x in range(x0, x1 + 1):
            s.set(x, y, z0, blk("minecraft:quartz_block"))
            s.set(x, y, z1, blk("minecraft:quartz_block"))
        for z in range(z0, z1 + 1):
            s.set(x0, y, z, blk("minecraft:quartz_block"))
            s.set(x1, y, z, blk("minecraft:quartz_block"))

    # Towering columns on facade (z=z0)
    for cx in (5, 9, 13, 22, 26, 30):
        for y in range(fy + 1, fy + h):
            s.set(cx, y, z0, blk("minecraft:quartz_pillar", axis="y"))

    # Stained glass rose window & high lancet windows (blue & cyan)
    for y in range(fy + 5, fy + 11):
        for x in (8, 12, 23, 27):
            s.set(x, y, z0, pane("blue_stained_glass_pane"))
            s.set(x, y, z1, pane("blue_stained_glass_pane"))
        for z in (8, 14, 21, 27):
            s.set(x0, y, z, pane("cyan_stained_glass_pane"))
            s.set(x1, y, z, pane("cyan_stained_glass_pane"))

    # Grand entrance portals at front (z=z0, x=16..19)
    for ex in (16, 17, 18, 19):
        s.set(ex, fy + 1, z0, blk("minecraft:air"))
        s.set(ex, fy + 2, z0, blk("minecraft:air"))
        s.set(ex, fy + 3, z0, blk("minecraft:air"))

    # Second-floor Mezzanine / Women's Balcony (y=fy+7) along sides and back
    for my_x in range(x0 + 1, x1):
        for my_z in range(z0 + 1, z1):
            if my_x in range(x0 + 1, x0 + 6) or my_x in range(x1 - 5, x1) or my_z in range(z0 + 1, z0 + 6):
                s.set(my_x, fy + 7, my_z, slab("smooth_quartz_slab", "top"))
                # Balcony carved railings
                s.set(my_x, fy + 8, my_z, wall("sandstone_wall") if (my_x in (x0 + 5, x1 - 5) or my_z == z0 + 5) else blk("minecraft:air"))

    # Monumental Vaulted ceiling and central dome
    s.fills(x0, fy + h + 1, z0, x1, fy + h + 1, z1, slab("smooth_quartz_slab", "bottom"))
    # Central glass dome
    for dx in range(14, 22):
        for dz in range(14, 22):
            s.set(dx, fy + h + 1, dz, blk("minecraft:blue_stained_glass"))
            s.set(dx, fy + h + 2, dz, slab("smooth_stone_slab", "bottom"))

    # Central Grand Bimah (raised marble platform at x=15..20, z=15..20)
    s.fills(15, fy + 1, 15, 20, fy + 1, 20, blk("minecraft:smooth_quartz"))
    s.fills(16, fy + 2, 16, 19, fy + 2, 19, blk("minecraft:blue_carpet"))
    # Bimah Reader's Lectern
    s.set(17, fy + 2, 16, blk("minecraft:lectern", facing="south", has_book="true"))
    s.set(18, fy + 2, 16, blk("minecraft:chiseled_bookshelf", facing="south"))
    # Menorahs on Bimah corners
    for bx, bz in ((15, 15), (20, 15), (15, 20), (20, 20)):
        s.set(bx, fy + 2, bz, wall("sandstone_wall"))
        s.set(bx, fy + 3, bz, lantern())

    # Towering Monumental Aron Kodesh (Holy Ark) at the East wall (z=z1)
    # Gilded multi-tiered Ark facade
    for y in range(fy + 1, fy + 10):
        for ax in (15, 20):
            s.set(ax, y, z1 - 1, blk("minecraft:quartz_pillar", axis="y"))
        for ax in (16, 17, 18, 19):
            s.set(ax, y, z1 - 1, blk("minecraft:smooth_quartz" if y < fy + 8 else "gold_block"))
    # Ark Torot and Treasures inside
    s.set(17, fy + 1, z1 - 1, chest(facing="north", loot="israel_simulator:chests/synagogue_ark"))
    s.set(18, fy + 1, z1 - 1, chest(facing="north", loot="israel_simulator:chests/synagogue_ark"))
    # Ner Tamid
    s.set(17, fy + 8, z1 - 2, lantern(hanging="false"))
    s.set(18, fy + 8, z1 - 2, lantern(hanging="false"))

    # Lower Sanctuary Seating Rows (Pews)
    for pz in (9, 12, 23, 26):
        for px in (7, 10, 24, 27):
            s.set(px, fy + 1, pz, stair("dark_oak_stairs", f="south"))
            s.set(px + 1, fy + 1, pz, stair("dark_oak_stairs", f="south"))

    # Study library bookcases along North & South walls
    for x in (5, 6, 7, 28, 29, 30):
        s.set(x, fy + 1, z1 - 1, blk("minecraft:bookshelf"))
        s.set(x, fy + 2, z1 - 1, blk("minecraft:bookshelf"))
    s.set(6, fy + 1, z1 - 2, chest(facing="north", loot="israel_simulator:chests/synagogue"))

    # Grand Chandeliers hanging from vaulted ceiling
    for lx in (11, 24):
        for lz in (11, 24):
            for ly in range(fy + 10, fy + h):
                s.set(lx, ly, lz, fence("oak_fence"))
            s.set(lx, fy + 9, lz, blk("minecraft:sea_lantern"))

    # Required Framework Marker Block
    s.set(1, fy + 1, 1, blk("minecraft:blue_stained_glass"))

    write_jigsaw("great_synagogue", "israel_simulator:jerusalem", salt=778899)
    exteriors.great_synagogue(s, fy)
    s.save(ROOT / "src/main/resources/data/israel_simulator/structure/great_synagogue.nbt")


if __name__ == "__main__":
    print("Generating remaining structures...")
    gen_startup_office()
    gen_historical_house()
    gen_synagogue()
    gen_great_synagogue()
    print("All 7 structures generated successfully.")
