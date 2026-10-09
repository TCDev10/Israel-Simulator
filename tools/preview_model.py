#!/usr/bin/env python3
"""Tiny software renderer for Java item models (cuboids, y-axis rotations). Usage:
python3 tools/preview_model.py model.json texture.png out.png [yaw] [pitch]"""
import json, math, sys
import numpy as np
from PIL import Image

model = json.load(open(sys.argv[1])); tex = np.array(Image.open(sys.argv[2]).convert("RGBA")).astype(float)
out = sys.argv[3]; yaw = float(sys.argv[4]) if len(sys.argv) > 4 else -35; pitch = float(sys.argv[5]) if len(sys.argv) > 5 else 25
TH, TW = tex.shape[:2]; N = 640
img = np.zeros((N, N, 4)); img[..., :3] = (60, 64, 72); img[..., 3] = 255
zb = np.full((N, N), -1e9)

def rot(axis, a, p, o):
    a = math.radians(a); c, s = math.cos(a), math.sin(a); x, y, z = p - o
    if axis == "y": x, z = x * c + z * s, -x * s + z * c
    elif axis == "x": y, z = y * c - z * s, y * s + z * c
    else: x, y = x * c - y * s, x * s + y * c
    return np.array([x, y, z]) + o

def view(p):
    p = p - 8
    p = rot("y", yaw, p, 0); p = rot("x", pitch, p, 0)
    return p

FACES = {  # corners: top-left, top-right, bottom-right, bottom-left as seen from outside
    "north": lambda f, t: [(t[0], t[1], f[2]), (f[0], t[1], f[2]), (f[0], f[1], f[2]), (t[0], f[1], f[2])],
    "south": lambda f, t: [(f[0], t[1], t[2]), (t[0], t[1], t[2]), (t[0], f[1], t[2]), (f[0], f[1], t[2])],
    "west": lambda f, t: [(f[0], t[1], f[2]), (f[0], t[1], t[2]), (f[0], f[1], t[2]), (f[0], f[1], f[2])],
    "east": lambda f, t: [(t[0], t[1], t[2]), (t[0], t[1], f[2]), (t[0], f[1], f[2]), (t[0], f[1], t[2])],
    "up": lambda f, t: [(f[0], t[1], f[2]), (t[0], t[1], f[2]), (t[0], t[1], t[2]), (f[0], t[1], t[2])],
    "down": lambda f, t: [(f[0], f[1], t[2]), (t[0], f[1], t[2]), (t[0], f[1], f[2]), (f[0], f[1], f[2])],
}
SHADE = {"up": 1.0, "down": 0.5, "north": 0.8, "south": 0.8, "east": 0.6, "west": 0.6}
allp = []
quads = []
for e in model["elements"]:
    f, t = e["from"], e["to"]; r = e.get("rotation")
    for side, fd in e["faces"].items():
        pts = [np.array(c, float) for c in FACES[side](f, t)]
        if r: pts = [rot(r["axis"], r["angle"], p, np.array(r["origin"], float)) for p in pts]
        pts = [view(p) for p in pts]; allp += pts
        quads.append((pts, fd["uv"], SHADE[side]))
allp = np.array(allp); span = max(np.ptp(allp[:, 0]), np.ptp(allp[:, 1])) * 1.15; cen = (allp.max(0) + allp.min(0)) / 2
def scr(p): return np.array([(p[0] - cen[0]) / span * N + N / 2, N / 2 - (p[1] - cen[1]) / span * N, p[2]])
for pts, uv, sh in quads:
    P = [scr(p) for p in pts]
    UVs = [(uv[0], uv[1]), (uv[2], uv[1]), (uv[2], uv[3]), (uv[0], uv[3])]
    for tri in ((0, 1, 2), (0, 2, 3)):
        a, b, c = (P[i] for i in tri); ua, ub, uc = (np.array(UVs[i]) for i in tri)
        x0, x1 = int(max(0, min(a[0], b[0], c[0]))), int(min(N - 1, max(a[0], b[0], c[0]) + 1))
        y0, y1 = int(max(0, min(a[1], b[1], c[1]))), int(min(N - 1, max(a[1], b[1], c[1]) + 1))
        if x1 <= x0 or y1 <= y0: continue
        X, Y = np.meshgrid(np.arange(x0, x1) + .5, np.arange(y0, y1) + .5)
        d = (b[1] - c[1]) * (a[0] - c[0]) + (c[0] - b[0]) * (a[1] - c[1])
        if abs(d) < 1e-9: continue
        w1 = ((b[1] - c[1]) * (X - c[0]) + (c[0] - b[0]) * (Y - c[1])) / d
        w2 = ((c[1] - a[1]) * (X - c[0]) + (a[0] - c[0]) * (Y - c[1])) / d
        w3 = 1 - w1 - w2; m = (w1 >= 0) & (w2 >= 0) & (w3 >= 0)
        z = w1 * a[2] + w2 * b[2] + w3 * c[2]
        u = (w1 * ua[0] + w2 * ub[0] + w3 * uc[0]) / 16 * TW; v = (w1 * ua[1] + w2 * ub[1] + w3 * uc[1]) / 16 * TH
        u = np.clip(u.astype(int), 0, TW - 1); v = np.clip(v.astype(int), 0, TH - 1)
        col = tex[v, u]; m &= col[..., 3] > 10
        sub = zb[y0:y1, x0:x1]; m &= z > sub
        sub[m] = z[m]; img[y0:y1, x0:x1][m] = np.concatenate([col[..., :3] * sh, col[..., 3:]], -1)[m]
Image.fromarray(img.astype(np.uint8)).save(out)
