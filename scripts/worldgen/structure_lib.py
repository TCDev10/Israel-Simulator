"""Minimal Structure writer with connect_blocks (DataVersion 4903)."""
from __future__ import annotations
import json
from pathlib import Path
from typing import Any
import nbtlib
from nbtlib import Compound, Int, List, Long, String
from connect_blocks import apply_connections

DATA_VERSION = 4903
ROOT = Path(__file__).resolve().parents[2]
BLOCKS_JSON = Path("/workspace/mcreports/out/reports/blocks.json")
KNOWN = json.loads(BLOCKS_JSON.read_text()) if BLOCKS_JSON.exists() else {}

def blk(name: str, **props: str) -> Compound:
    if name.startswith("minecraft:") and KNOWN and name not in KNOWN:
        raise ValueError(f"Unknown block {name}")
    if props and KNOWN and name in KNOWN:
        allowed = KNOWN[name].get("properties", {})
        for k, v in props.items():
            if k not in allowed or v not in allowed[k]:
                raise ValueError(f"{name}.{k}={v} invalid")
    c: dict[str, Any] = {"Name": String(name)}
    if props:
        c["Properties"] = Compound({k: String(v) for k, v in props.items()})
    return Compound(c)

def chest(facing="south", loot=None):
    e = blk("minecraft:chest", facing=facing, type="single", waterlogged="false")
    if loot:
        e["_nbt"] = Compound({"id": String("minecraft:chest"), "LootTable": String(loot), "LootTableSeed": Long(0)})
    return e

class Structure:
    def __init__(self, sx, sy, sz):
        self.sx, self.sy, self.sz = sx, sy, sz
        self.palette=[]; self.index={}; self.blocks={}; self.block_nbt={}
        self.fills(0,0,0,sx-1,sy-1,sz-1, blk("minecraft:air"))
    def _key(self, entry):
        props=entry.get("Properties"); nbt=entry.get("_nbt")
        return json.dumps({"Name":str(entry["Name"]),
            "Properties":{k:str(v) for k,v in props.items()} if props else None,
            "nbt":{k:str(v) for k,v in nbt.items()} if nbt else None}, sort_keys=True)
    def _state(self, entry):
        k=self._key(entry)
        if k not in self.index:
            self.index[k]=len(self.palette); self.palette.append(entry)
        return self.index[k]
    def set(self,x,y,z,entry):
        if not (0<=x<self.sx and 0<=y<self.sy and 0<=z<self.sz):
            raise ValueError((x,y,z))
        self.blocks[(x,y,z)]=self._state(entry)
        if "_nbt" in entry: self.block_nbt[(x,y,z)]=entry["_nbt"]
        elif (x,y,z) in self.block_nbt: del self.block_nbt[(x,y,z)]
    def fills(self,x0,y0,z0,x1,y1,z1,entry):
        for x in range(min(x0,x1),max(x0,x1)+1):
            for y in range(min(y0,y1),max(y0,y1)+1):
                for z in range(min(z0,z1),max(z0,z1)+1):
                    self.set(x,y,z,entry)
    def save(self, path: Path):
        path.parent.mkdir(parents=True, exist_ok=True)
        grid={}
        for (x,y,z), state in self.blocks.items():
            e=self.palette[state]
            props={str(k):str(v) for k,v in e["Properties"].items()} if "Properties" in e else {}
            grid[(x,y,z)]={"Name":str(e["Name"]),"Properties":props}
        apply_connections(grid, size=(self.sx,self.sy,self.sz))
        new_palette=[]; new_index={}; blocks_list=[]
        for (x,y,z) in sorted(grid.keys()):
            cell=grid[(x,y,z)]; props=cell["Properties"]
            pe=Compound({"Name":String(cell["Name"])})
            if props: pe["Properties"]=Compound({k:String(v) for k,v in props.items()})
            key_obj={"Name":cell["Name"],"Properties":props or None,
                     "nbt":({str(k):str(v) for k,v in self.block_nbt[(x,y,z)].items()} if (x,y,z) in self.block_nbt else None)}
            key=json.dumps(key_obj, sort_keys=True)
            if key not in new_index:
                new_index[key]=len(new_palette); new_palette.append(pe)
            b=Compound({"pos":List[Int]([Int(x),Int(y),Int(z)]),"state":Int(new_index[key])})
            if (x,y,z) in self.block_nbt: b["nbt"]=self.block_nbt[(x,y,z)]
            blocks_list.append(b)
        root=Compound({"size":List[Int]([Int(self.sx),Int(self.sy),Int(self.sz)]),
                       "entities":List[Compound]([]),"blocks":List[Compound](blocks_list),
                       "palette":List[Compound](new_palette),"DataVersion":Int(DATA_VERSION)})
        nbtlib.File(root).save(path, gzipped=True)
        print(f"wrote {path.relative_to(ROOT)} {self.sx}x{self.sy}x{self.sz} n={len(blocks_list)}")

def write_jigsaw(name: str, biome: str, spacing=28, separation=10, salt=1, adaptation="beard_box"):
    struct = ROOT/f"src/main/resources/data/israel_simulator/worldgen/structure/{name}.json"
    pool = ROOT/f"src/main/resources/data/israel_simulator/worldgen/template_pool/{name}.json"
    # structure_set name: plural variants differ — update in place if exists
    structure = {
        "type":"minecraft:jigsaw","biomes":[biome],"max_distance_from_center":80,
        "project_start_to_heightmap":"WORLD_SURFACE_WG","size":1,"spawn_overrides":{},
        "start_height":{"absolute":0},"start_pool":f"israel_simulator:{name}",
        "step":"surface_structures","terrain_adaptation":adaptation,"use_expansion_hack":False,
    }
    struct.write_text(json.dumps(structure, indent=2)+"\n")
    pool.write_text(json.dumps({"fallback":"minecraft:empty","elements":[{
        "weight":1,"element":{"element_type":"minecraft:single_pool_element",
            "location":f"israel_simulator:{name}","processors":"minecraft:empty","projection":"rigid"}
    }]}, indent=2)+"\n")
