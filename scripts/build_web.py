"""Assembles the web / iPhone app into build/site.

It copies web/, the shared content/content.json and the app's fonts, and stamps a build id
into the service worker so every deploy updates installed copies.

Usage:  python scripts/build_web.py [--build-id abc1234]
Preview: python -m http.server 8765 -d build/site   (then open http://localhost:8765)
"""
import argparse
import pathlib
import shutil
import time

root = pathlib.Path(__file__).resolve().parent.parent
out = root / "build" / "site"

parser = argparse.ArgumentParser()
parser.add_argument("--build-id", default=f"dev-{int(time.time())}")
build_id = parser.parse_args().build_id

if out.exists():
    shutil.rmtree(out)
shutil.copytree(root / "web", out)
shutil.copy2(root / "content" / "content.json", out / "content.json")

fonts = out / "fonts"
fonts.mkdir()
for name in ("tiro_marathi", "mukta_regular", "mukta_semibold", "mukta_bold"):
    shutil.copy2(root / "app" / "src" / "main" / "res" / "font" / f"{name}.ttf", fonts / f"{name}.ttf")

# The read-aloud background drone is shared with the Android app.
audio = out / "audio"
audio.mkdir()
shutil.copy2(root / "app" / "src" / "main" / "res" / "raw" / "tanpura.wav", audio / "tanpura.wav")

for name in ("index.html", "sw.js"):
    path = out / name
    path.write_text(path.read_text(encoding="utf-8").replace("__BUILD_ID__", build_id), encoding="utf-8", newline="\n")

# GitHub Pages: serve files as-is (no Jekyll processing).
(out / ".nojekyll").touch()
print(f"Built {out} (build {build_id})")
