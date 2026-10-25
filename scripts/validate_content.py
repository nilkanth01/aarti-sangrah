"""Checks content/content.json before it is pushed.

Every phone running the app downloads this file, so a mistake here reaches everyone.
The app also refuses broken files, but catching problems here is kinder.

Usage:  python scripts/validate_content.py
"""
import json
import pathlib
import sys

path = pathlib.Path(__file__).resolve().parent.parent / "content" / "content.json"
errors = []

try:
    data = json.loads(path.read_text(encoding="utf-8"))
except json.JSONDecodeError as e:
    sys.exit(f"content.json is not valid JSON: {e}")

for key in ("version", "categories", "deities", "items"):
    if key not in data:
        errors.append(f"missing top-level key '{key}'")

if not isinstance(data.get("version"), int):
    errors.append("'version' must be a whole number — increase it by 1 for every change")

categories = {c.get("id") for c in data.get("categories", [])}
deities = {d.get("id") for d in data.get("deities", [])}
ids = set()
for n, item in enumerate(data.get("items", []), 1):
    where = f"item #{n} ({item.get('id', '?')})"
    for key in ("id", "title", "category", "verses"):
        if not item.get(key):
            errors.append(f"{where}: missing '{key}'")
    if item.get("id") in ids:
        errors.append(f"{where}: duplicate id")
    ids.add(item.get("id"))
    if item.get("category") not in categories:
        errors.append(f"{where}: unknown category '{item.get('category')}'")
    if item.get("deity") and item["deity"] not in deities:
        errors.append(f"{where}: unknown deity '{item['deity']}'")
    if not isinstance(item.get("verses", []), list) or not all(isinstance(v, str) and v.strip() for v in item.get("verses", [])):
        errors.append(f"{where}: 'verses' must be a list of non-empty strings")

for p in data.get("playlists", []):
    for i in p.get("items", []):
        if i not in ids:
            errors.append(f"playlist '{p.get('id')}': unknown item '{i}'")

if errors:
    print("content.json has problems:")
    for e in errors:
        print("  -", e)
    sys.exit(1)

print(f"OK - version {data['version']}, {len(ids)} items, {len(data.get('playlists', []))} playlists")
