import gzip
import struct
import time
import nbtlib

def main():
    t0 = time.time()
    path = 'src/main/resources/data/israel_simulator/structure/torri_gemelle.nbt'
    print(f"Loading {path}...")
    
    with open(path, 'rb') as f:
        raw = gzip.decompress(f.read())
        
    # Read size
    size_idx = raw.find(b'size') + len('size') + 5 # tag_list + count
    sx, sy, sz = struct.unpack('>iii', raw[size_idx:size_idx+12])
    print(f"Structure size: {sx} x {sy} x {sz} (Total: {sx * sy * sz} blocks)")
    
    # Read blocks section
    blocks_idx = raw.find(b'blocks')
    blocks_tag_type = raw[blocks_idx + len('blocks')] # 10 (Compound)
    blocks_count = struct.unpack('>i', raw[blocks_idx + len('blocks') + 1:blocks_idx + len('blocks') + 5])[0]
    print(f"Original solid blocks: {blocks_count}")
    
    # Parse existing block coordinates and retain raw block slice
    pal_idx = raw.find(b'palette')
    existing_blocks_bytes = raw[blocks_idx + len('blocks') + 5 : pal_idx - 3] # up to tag_type of palette
    
    # Parse existing coordinates using nbtlib to be 100% accurate
    nbt = nbtlib.load(path)
    existing_coords = set()
    for b in nbt['blocks']:
        p = b['pos']
        existing_coords.add((int(p[0]), int(p[1]), int(p[2])))
    
    print(f"Collected {len(existing_coords)} coordinates in {time.time()-t0:.2f}s")
    
    # Prepare palette: add minecraft:air if not present
    palette = nbt['palette']
    air_idx = -1
    for idx, entry in enumerate(palette):
        if str(entry.get('Name', '')) == 'minecraft:air':
            air_idx = idx
            break
            
    if air_idx == -1:
        air_idx = len(palette)
        palette.append(nbtlib.Compound({'Name': nbtlib.String('minecraft:air')}))
        print(f"Added minecraft:air to palette at index {air_idx}")
    else:
        print(f"Found minecraft:air in palette at index {air_idx}")
        
    # Build air blocks bytes directly with struct for maximum speed
    print("Generating air blocks bytes...")
    air_blocks = bytearray()
    pos_header = b'\t\x00\x03pos\x03\x00\x00\x00\x03'
    state_bytes = b'\x03\x00\x05state' + struct.pack('>i', air_idx) + b'\x00'
    
    air_count = 0
    for y in range(sy):
        for z in range(sz):
            for x in range(sx):
                if (x, y, z) not in existing_coords:
                    air_blocks += pos_header
                    air_blocks += struct.pack('>iii', x, y, z)
                    air_blocks += state_bytes
                    air_count += 1
                    
    total_blocks = blocks_count + air_count
    print(f"Created {air_count} air blocks in {time.time()-t0:.2f}s (Total blocks: {total_blocks})")
    
    # Serialize palette and DataVersion using nbtlib
    palette_root = nbtlib.Compound({'palette': palette, 'DataVersion': nbt.get('DataVersion', nbtlib.Int(4903))})
    import io
    pal_buf = io.BytesIO()
    nbtlib.File(palette_root).write(pal_buf)
    pal_raw = pal_buf.getvalue()
    
    # Extract palette and dataversion from pal_buf (skip root compound header and end)
    # Root compound starts with \n\x00\x00, let's find 'palette'
    pal_start = pal_raw.find(b'palette') - 3 # tag_list (9)
    palette_bytes = pal_raw[pal_start:]
    
    # Build complete NBT:
    # Header up to blocks count
    header = raw[:blocks_idx + len('blocks') + 1] # up to tag_type 10
    new_blocks_header = struct.pack('>i', total_blocks)
    
    full_nbt = header + new_blocks_header + existing_blocks_bytes + air_blocks + palette_bytes
    
    print(f"Full uncompressed NBT length: {len(full_nbt)} bytes (~{len(full_nbt)//(1024*1024)} MB)")
    
    compressed = gzip.compress(full_nbt)
    print(f"Compressed GZIP length: {len(compressed)} bytes (~{len(compressed)//1024} KB)")
    
    # Write to torri_gemelle.nbt
    with open(path, 'wb') as f:
        f.write(compressed)
        
    print(f"Successfully saved updated {path} in {time.time()-t0:.2f}s!")
    
    # Verify with nbtlib
    print("Verifying updated file with nbtlib...")
    verify_nbt = nbtlib.load(path)
    print(f"Verified! Size: {verify_nbt['size']}, Blocks: {len(verify_nbt['blocks'])}, Palette: {len(verify_nbt['palette'])}")

if __name__ == '__main__':
    main()

