"""Chants the non-Vedic Sanskrit texts with Vāgdhenu (IISc, Apache-2.0), a metre-aware Sanskrit chant voice.

Writes into the same voice/ folder and voice/index.json as scripts/make_voice.py, so the apps play it
exactly like the other recordings: one MP3 per item plus the start/end of every line. Files are named
<id>-vg<hash>.mp3 so phones and browsers fetch the chant instead of a cached older recording.

The Vāgdhenu author asks for it to be used only for non-Vedic chants, so the Vedic mantras (VEDIC
below) keep the Parler-TTS voice. make_voice.py leaves items chanted here alone.

Setup (once): clone https://github.com/prathoshap/vagdhenu to %VAGDHENU% (default below), make a
Python 3.10 venv with its requirements + torch 2.4.1+cu121 + lameenc, clone NVIDIA BigVGAN into it,
and download voice_steer_ema_2026-06-17.pt, voc_bigvgan_EMA_2026-06-11.pth and vocab.txt from
huggingface.co/prathoshap/vagdhenu into its models/ folder. Then:
    set PYTHONPATH=%VAGDHENU%\\BigVGAN
    C:\\Users\\nilkanth\\AndroidDev\\tts\\vg-venv\\Scripts\\python.exe scripts\\make_chant.py [--only ramraksha]
Rendered lines are cached in build/chant-cache/, so re-runs only chant new or changed lines.
"""
import argparse
import hashlib
import json
import os
import pathlib
import sys
import time

import numpy as np

HERE = pathlib.Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
import make_voice as MV  # noqa: E402  (same line splitting, hash, MP3 and index format)

VAGDHENU = pathlib.Path(os.environ.get("VAGDHENU", r"C:\Users\nilkanth\AndroidDev\tts\vagdhenu"))
sys.path.insert(0, str(VAGDHENU / "src"))
CACHE = MV.ROOT / "build" / "chant-cache"
VOICE_NAME = "Vāgdhenu"
SEED = 60
VEDIC = {"gayatri", "mahamrityunjay", "mantrapushpanjali", "shanti-mantra", "atharvashirsha"}


def chanted(item):
    return item.get("language") == "संस्कृत" and item["id"] not in VEDIC


def meter_of(rc, verse):
    """The verse's vṛtta, or the prose template for lines that are not a recognised metre."""
    name = rc.detect_meter_key(verse)
    return name or "gadya"


def cache_path(meter, text, seed):
    key = hashlib.sha1(f"vagdhenu|steer-2026-06-17|{meter}|{seed}|{text}".encode("utf-8")).hexdigest()
    return CACHE / f"{key}.npy"


def plausible(rc, text, seconds):
    """Chant runs at roughly 2–5 syllables a second; far outside that means a clipped or garbled line."""
    rate = rc.n_aksharas(text) / max(seconds, 0.05)
    return 1.2 <= rate <= 7.0


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--only", default="")
    ap.add_argument("--list", action="store_true", help="print the items and detected metres, then stop")
    args = ap.parse_args()
    sys.stdout.reconfigure(encoding="utf-8")
    import render_core as rc

    items = json.loads(MV.CONTENT.read_text(encoding="utf-8"))["items"]
    only = {x for x in args.only.split(",") if x}
    chosen = [it for it in items if chanted(it) and (not only or it["id"] in only)]

    # (block, line, text, prompt) like make_voice, plus the metre of the verse each line belongs to.
    jobs = {}
    for it in chosen:
        lines = MV.script(it)
        meters = {}
        for k, verse in enumerate(it["verses"]):
            meters[f"v{k}"] = meter_of(rc, verse)
        if it.get("refrain"):
            m = meter_of(rc, it["refrain"])
            meters.update({b: m for b, *_ in lines if b.startswith("r")})
        jobs[it["id"]] = [(b, i, text, meters[b]) for b, i, text, _ in lines]
        if args.list:
            print(it["id"], sorted(set(meters.values())))
    if args.list:
        return

    CACHE.mkdir(parents=True, exist_ok=True)
    todo = {(m, t) for lines in jobs.values() for _, _, t, m in lines if not cache_path(m, t, SEED).exists()}
    log = []
    if todo:
        print(f"{len(todo)} lines to chant", flush=True)
        r = rc.Renderer(str(VAGDHENU / "models" / "voice_steer_ema_2026-06-17.pt"),
                        str(VAGDHENU / "models" / "voc_bigvgan_EMA_2026-06-11.pth"),
                        str(VAGDHENU / "src" / "reference_bank" / "bank.json"), device="cuda")
        started = time.time()
        for n, (meter, text) in enumerate(sorted(todo), 1):
            best = None
            for seed in (SEED, SEED + 7, SEED + 19):
                sr, a = r.render_one([text], meter, seed=seed)
                a = MV.trim(np.asarray(a, np.float32), sr)
                if best is None or plausible(rc, text, len(a) / sr):
                    best = a
                if plausible(rc, text, len(a) / sr):
                    break
            else:
                log.append(f"CHECK [{meter}] {text}  ({len(best) / sr:.1f}s)")
            np.save(cache_path(meter, text, SEED), MV.normalise(MV.resample(best, sr) if sr != MV.RATE else best))
            rate = (time.time() - started) / n
            print(f"  chanted {n}/{len(todo)} · {rate:.1f}s per line · ~{rate * (len(todo) - n) / 60:.0f} min left", flush=True)

    index_path = MV.OUT / "index.json"
    index = json.loads(index_path.read_text(encoding="utf-8"))
    for it in chosen:
        lines = jobs[it["id"]]
        parts = [np.zeros(int(MV.RATE * MV.LEAD_IN), np.float32)]
        t, times = MV.LEAD_IN, []
        for n, (block, _, text, meter) in enumerate(lines):
            a = np.load(cache_path(meter, text, SEED))
            times.append([round(t, 2), round(t + len(a) / MV.RATE, 2)])
            parts.append(a)
            t += len(a) / MV.RATE
            gap = 0.6 if n == len(lines) - 1 else (MV.BLOCK_GAP if lines[n + 1][0] != block else MV.LINE_GAP)
            parts.append(np.zeros(int(MV.RATE * gap), np.float32))
            t += gap
        audio = np.concatenate(parts)
        mp3 = MV.to_mp3(audio)
        h = MV.script_hash(lines)
        name = f"{it['id']}-vg{h[:8]}.mp3"
        (MV.OUT / name).write_bytes(mp3)
        index["items"][it["id"]] = {"file": name, "hash": h, "bytes": len(mp3),
                                    "duration": round(len(audio) / MV.RATE, 2), "lines": times, "voice": VOICE_NAME}
        print(f"  {name}  {len(audio) / MV.RATE:.0f}s  {len(mp3) // 1024} KB", flush=True)

    index["totalBytes"] = sum(v["bytes"] for v in index["items"].values())
    index_path.write_text(json.dumps(index, ensure_ascii=False, separators=(",", ":")) + "\n", encoding="utf-8", newline="\n")
    keep = {v["file"] for v in index["items"].values()}
    for f in MV.OUT.glob("*.mp3"):
        if f.name not in keep:
            f.unlink()
    print(f"index: {len(index['items'])} items, {index['totalBytes'] / 1e6:.1f} MB")
    if log:
        (MV.ROOT / "build" / "chant-check.txt").write_text("\n".join(log) + "\n", encoding="utf-8")
        print(f"{len(log)} lines need a listen — see build/chant-check.txt")


if __name__ == "__main__":
    main()
