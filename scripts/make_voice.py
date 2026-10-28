"""Generates the natural "Listen" voice with AI4Bharat Indic Parler-TTS (Apache-2.0).

For every item in content/content.json it voices each line exactly as the apps show it, then packs
one small MP3 per item plus the start/end time of every line into voice/. The apps download an
item's MP3 on first play (then cache it), highlight lines from the timings, and fall back to the
phone's own voice when an item has no audio or its text has changed since the audio was made.

Setup (once): see README "Natural voice". Run with the TTS venv, not the system Python:
    C:\\Users\\nilkanth\\AndroidDev\\tts\\venv\\Scripts\\python.exe scripts\\make_voice.py
Options:
    --only sukhakarta,pasaydan   regenerate just these items
    --samples                    write a few lines in each voice to build/voice-samples/ and stop
    --batch 6                    lines generated together on the GPU
Generated lines are cached in build/voice-cache/, so re-runs only voice new or changed lines.
The non-Vedic Sanskrit texts are chanted by scripts/make_chant.py instead; this script skips them.
"""
import argparse
import hashlib
import json
import math
import pathlib
import re
import sys
import time

import numpy as np

ROOT = pathlib.Path(__file__).resolve().parent.parent
CONTENT = ROOT / "content" / "content.json"
OUT = ROOT / "voice"
CACHE = ROOT / "build" / "voice-cache"
MODEL = "ai4bharat/indic-parler-tts"
RATE = 24000                 # output sample rate (speech needs no more)
MP3_KBPS = 48
LEAD_IN, LINE_GAP, BLOCK_GAP = 0.25, 0.45, 0.85   # seconds of silence

SPEAKERS = {"mr": "Sunita", "hi": "Divya"}
DESCRIPTION = ("{name} speaks in a calm, clear and slightly slow voice with a gentle devotional tone. "
               "The recording is of very high quality, with the speaker's voice sounding clear and very "
               "close up, with no background noise.")

# ---- The same line splitting and cleaning as the apps (Recitation.kt / app.js) ----
VERSE_NO = re.compile(r"॥\s*[०-९0-9]+\s*॥")
NOISE = re.compile(r"[।॥'\"“”‘’()०-९0-9]")


def clean(line):
    return re.sub(r"\s+", " ", NOISE.sub(" ", VERSE_NO.sub(" ", line)).replace("—", " ")).strip()


def prompt_text(line):
    """What the model reads: like clean() but keeps sentence breaks as dandas for natural pauses."""
    p = VERSE_NO.sub(" ", line).replace("॥", "।").replace("—", ", ").replace("ॐ", "ओम्")
    p = re.sub(r"['\"“”‘’()०-९0-9]", " ", p)
    p = re.sub(r"\s*।[\s।]*", "। ", p)
    p = re.sub(r"\s+", " ", p).strip().lstrip("। ").strip()
    return p if p.endswith("।") else p + "।"


def script(item):
    """[(block, line, text, prompt)] in reading order — refrain after every verse, like the reader."""
    out = []

    def add(block, text):
        for i, raw in enumerate(text.split("\n")):
            c = clean(raw)
            if c:
                out.append((block, i, c, prompt_text(raw)))

    refrain = item.get("refrain")
    if refrain and item.get("refrainFirst"):
        add("rt", refrain)
    for k, verse in enumerate(item["verses"]):
        add(f"v{k}", verse)
        if refrain:
            add(f"r{k}", refrain)
    return out


def script_hash(lines):
    """Must match Recitation.hash() in the app: SHA-1 of the spoken lines joined by newlines."""
    return hashlib.sha1("\n".join(l[2] for l in lines).encode("utf-8")).hexdigest()


def lang_of(item):
    return "hi" if item.get("language") in ("हिंदी", "अवधी") else "mr"


# ---- Audio helpers ----
def trim(x, sr):
    """Cut leading/trailing silence (keeps 60 ms either side)."""
    frame = int(sr * 0.02)
    if len(x) < frame * 3:
        return x
    rms = np.sqrt(np.convolve(x ** 2, np.ones(frame) / frame, mode="same"))
    loud = np.where(rms > max(rms.max() * 0.04, 1e-4))[0]
    if not len(loud):
        return x
    pad = int(sr * 0.06)
    return x[max(0, loud[0] - pad): min(len(x), loud[-1] + pad)]


def normalise(x):
    rms = math.sqrt(float(np.mean(x ** 2)) + 1e-12)
    y = x * (0.08 / rms)                      # about -22 dBFS
    peak = float(np.abs(y).max())
    return y * (0.95 / peak) if peak > 0.95 else y


def resample(x, src):
    from scipy.signal import resample_poly
    g = math.gcd(src, RATE)
    return resample_poly(x, RATE // g, src // g).astype(np.float32)


def to_mp3(x):
    import lameenc
    enc = lameenc.Encoder()
    enc.set_bit_rate(MP3_KBPS)
    enc.set_in_sample_rate(RATE)
    enc.set_channels(1)
    enc.set_quality(2)
    pcm = (np.clip(x, -1, 1) * 32767).astype("<i2").tobytes()
    return enc.encode(pcm) + enc.flush()


# ---- Generation ----
class Voice:
    def __init__(self, batch):
        import torch
        from parler_tts import ParlerTTSForConditionalGeneration
        from transformers import AutoTokenizer
        self.torch = torch
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        dtype = torch.float16 if self.device == "cuda" else torch.float32
        print(f"loading {MODEL} on {self.device} …", flush=True)
        self.model = ParlerTTSForConditionalGeneration.from_pretrained(
            MODEL, torch_dtype=dtype, low_cpu_mem_usage=True).to(self.device)
        self.tok = AutoTokenizer.from_pretrained(MODEL)
        self.desc_tok = AutoTokenizer.from_pretrained(self.model.config.text_encoder._name_or_path)
        self.sr = self.model.config.sampling_rate
        self.batch = batch

    def generate(self, jobs, seed):
        """jobs: [(lang, prompt)] -> list of float32 arrays at self.sr."""
        torch = self.torch
        torch.manual_seed(seed)
        descs = [DESCRIPTION.format(name=SPEAKERS[lang]) for lang, _ in jobs]
        d = self.desc_tok(descs, return_tensors="pt", padding=True).to(self.device)
        p = self.tok([t for _, t in jobs], return_tensors="pt", padding=True).to(self.device)
        longest = max(len(t) for _, t in jobs)
        max_frames = int((longest / 4.0 + 4) * 86)          # generous cap: ~4 chars/s + 4 s
        with torch.inference_mode():
            g = self.model.generate(
                input_ids=d.input_ids, attention_mask=d.attention_mask,
                prompt_input_ids=p.input_ids, prompt_attention_mask=p.attention_mask,
                do_sample=True, return_dict_in_generate=True, max_new_tokens=max_frames)
        out = []
        for i in range(len(jobs)):
            n = int(g.audios_length[i]) if hasattr(g, "audios_length") else g.sequences.shape[-1]
            out.append(g.sequences[i, :n].float().cpu().numpy().astype(np.float32))
        return out


TYPICAL_RATE = 9.2      # characters per second measured over the whole collection


def speech_rate(text, seconds):
    return len(text.replace(" ", "")) / max(seconds, 0.05)


def plausible(text, seconds):
    """Rough check that the model said the whole line at a normal pace, not rushed, clipped or mumbled."""
    return 4.0 <= speech_rate(text, seconds) <= 15.0


def cache_path(lang, prompt):
    key = hashlib.sha1(f"{MODEL}|{SPEAKERS[lang]}|{DESCRIPTION}|{prompt}".encode("utf-8")).hexdigest()
    return CACHE / f"{key}.npy"


def voice_lines(voice, todo, log):
    """todo: [(lang, prompt, text)] not yet cached. Generates, checks, retries, caches."""
    todo = sorted(todo, key=lambda j: len(j[1]))          # similar lengths batch well
    done = 0
    started = time.time()
    for b in range(0, len(todo), voice.batch):
        chunk = todo[b: b + voice.batch]
        results = [None] * len(chunk)
        pending = list(range(len(chunk)))
        attempts = 4
        for attempt in range(attempts):
            if not pending:
                break
            audios = voice.generate([(chunk[i][0], chunk[i][1]) for i in pending], seed=1000 + 17 * attempt + b)
            still = []
            for i, a in zip(pending, audios):
                a = trim(a, voice.sr)
                text = chunk[i][2]
                # Keep whichever attempt is closest to a natural pace.
                best = results[i]
                if best is None or abs(speech_rate(text, len(a) / voice.sr) - TYPICAL_RATE) < abs(
                        speech_rate(text, len(best) / voice.sr) - TYPICAL_RATE):
                    results[i] = a
                if not plausible(text, len(a) / voice.sr):
                    still.append(i)
                    if attempt == attempts - 1:
                        log.append(f"CHECK [{chunk[i][0]}] {text}  ({len(results[i]) / voice.sr:.1f}s)")
            pending = still
        for (lang, prompt, _), a in zip(chunk, results):
            np.save(cache_path(lang, prompt), normalise(resample(a, voice.sr)))
        done += len(chunk)
        rate = (time.time() - started) / done
        print(f"  voiced {done}/{len(todo)} lines · {rate:.1f}s per line · ~{rate * (len(todo) - done) / 60:.0f} min left", flush=True)


def build_item(item, lines):
    """Joins cached line audio into one MP3 and returns (mp3 bytes, [[start, end]...], duration)."""
    parts = [np.zeros(int(RATE * LEAD_IN), np.float32)]
    t = LEAD_IN
    times = []
    lang = lang_of(item)
    for n, (block, _, text, prompt) in enumerate(lines):
        a = np.load(cache_path(lang, prompt))
        times.append([round(t, 2), round(t + len(a) / RATE, 2)])
        parts.append(a)
        t += len(a) / RATE
        last = n == len(lines) - 1
        gap = 0.6 if last else (BLOCK_GAP if lines[n + 1][0] != block else LINE_GAP)
        parts.append(np.zeros(int(RATE * gap), np.float32))
        t += gap
    audio = np.concatenate(parts)
    return to_mp3(audio), times, round(len(audio) / RATE, 2)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--only", default="")
    ap.add_argument("--samples", action="store_true")
    ap.add_argument("--batch", type=int, default=6)
    args = ap.parse_args()
    sys.stdout.reconfigure(encoding="utf-8")
    CACHE.mkdir(parents=True, exist_ok=True)
    content = json.loads(CONTENT.read_text(encoding="utf-8"))
    items = content["items"]

    if args.samples:
        return samples(items, args.batch)

    only = {x for x in args.only.split(",") if x}
    # Items chanted by scripts/make_chant.py (Vāgdhenu) are left alone unless named in --only.
    old_index = OUT / "index.json"
    chant = {k for k, v in json.loads(old_index.read_text(encoding="utf-8"))["items"].items()
             if v.get("voice")} if old_index.exists() else set()
    chosen = [it for it in items if (it["id"] in only if only else it["id"] not in chant)]
    scripts = {it["id"]: script(it) for it in chosen}
    todo = {}
    for it in chosen:
        lang = lang_of(it)
        for _, _, text, prompt in scripts[it["id"]]:
            if not cache_path(lang, prompt).exists():
                todo[(lang, prompt)] = (lang, prompt, text)
    log = []
    if todo:
        print(f"{len(todo)} lines to voice", flush=True)
        voice_lines(Voice(args.batch), list(todo.values()), log)

    OUT.mkdir(exist_ok=True)
    index_path = OUT / "index.json"
    index = json.loads(index_path.read_text(encoding="utf-8")) if index_path.exists() else {"items": {}}
    for it in chosen:
        lines = scripts[it["id"]]
        mp3, times, duration = build_item(it, lines)
        h = script_hash(lines)
        name = f"{it['id']}-{h[:8]}.mp3"
        (OUT / name).write_bytes(mp3)
        index["items"][it["id"]] = {"file": name, "hash": h, "bytes": len(mp3), "duration": duration, "lines": times}
        print(f"  {name}  {duration:.0f}s  {len(mp3) // 1024} KB", flush=True)

    live = {it["id"] for it in items}
    index["items"] = {k: v for k, v in sorted(index["items"].items()) if k in live}
    index.update({
        "version": 1,
        "model": MODEL,
        "voices": SPEAKERS,
        "totalBytes": sum(v["bytes"] for v in index["items"].values()),
    })
    index = {k: index[k] for k in ("version", "model", "voices", "totalBytes", "items")}
    index_path.write_text(json.dumps(index, ensure_ascii=False, separators=(",", ":")) + "\n", encoding="utf-8", newline="\n")
    keep = {v["file"] for v in index["items"].values()}
    for f in OUT.glob("*.mp3"):
        if f.name not in keep:
            f.unlink()
    print(f"index: {len(index['items'])} items, {index['totalBytes'] / 1e6:.1f} MB")
    if log:
        (ROOT / "build" / "voice-check.txt").write_text("\n".join(log) + "\n", encoding="utf-8")
        print(f"{len(log)} lines need a listen — see build/voice-check.txt")


def samples(items, batch):
    """A few lines in two Marathi and two Hindi voices, to choose the speakers."""
    out = ROOT / "build" / "voice-samples"
    out.mkdir(parents=True, exist_ok=True)
    by_id = {it["id"]: it for it in items}
    lines = [l for l in script(by_id["sukhakarta"])[:6]] + [l for l in script(by_id["ramraksha"])[2:4]]
    hindi = script(by_id["hanuman-chalisa"])[4:7]
    voice = Voice(batch)
    for name, lang, chosen in (("Sunita", "mr", lines), ("Sanjay", "mr", lines), ("Divya", "hi", hindi), ("Rohit", "hi", hindi)):
        SPEAKERS[lang] = name
        audios = voice.generate([(lang, l[3]) for l in chosen], seed=1000)
        parts = []
        for a in audios:
            parts += [normalise(resample(trim(a, voice.sr), voice.sr)), np.zeros(int(RATE * 0.5), np.float32)]
        (out / f"sample-{lang}-{name}.mp3").write_bytes(to_mp3(np.concatenate(parts)))
        print(f"  wrote sample-{lang}-{name}.mp3", flush=True)


if __name__ == "__main__":
    main()
