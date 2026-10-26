"""Synthesises the soft tanpura drone played behind the read-aloud voice.

It is generated from scratch (no samples, no copyright): four strings tuned Pa-Sa-Sa-low Sa
around C# (a comfortable pitch for most voices), plucked in the usual cycle. The buzzing
"jawari" brightness is imitated by a band of overtones that sweeps up after each pluck.
The buffer is rendered circularly so it loops without a click.

Usage:  python scripts/make_tanpura.py   ->  app/src/main/res/raw/tanpura.wav (also used by the web app)
"""
import math
import pathlib
import random
import struct
import wave

SR = 22050
CYCLE = 6.0                      # seconds per pluck cycle (= loop length)
SA = 138.59                      # C#3
STRINGS = [                      # (frequency, pluck time in cycle, loudness)
    (SA * 0.75, 0.0, 0.85),      # Pa (lower octave)
    (SA, 1.25, 0.75),            # Sa
    (SA * 1.0025, 2.5, 0.75),    # Sa, a hair sharp for a gentle chorus
    (SA / 2, 3.75, 1.0),         # low Sa
]
HARMONICS = 16
DECAY = 3.2                      # seconds for a string to fade by 1/e
TAIL_CYCLES = 3                  # how long each pluck rings (in cycles) before it is ignored

random.seed(7)
n = int(SR * CYCLE)
buf = [0.0] * n

for freq, at, loud in STRINGS:
    start = int(at * SR)
    phases = [random.uniform(0, 2 * math.pi) for _ in range(HARMONICS)]
    steps = [2 * math.pi * freq * (h + 1) / SR for h in range(HARMONICS)]
    for t in range(n * TAIL_CYCLES):
        sec = t / SR
        env = (1 - math.exp(-sec / 0.015)) * math.exp(-sec / DECAY) * loud
        if env < 1e-4 and sec > 1:
            break
        centre = 2.0 + 7.0 * (1 - math.exp(-sec / 1.4))   # the jawari band rises to ~9th harmonic
        s = 0.0
        for h in range(HARMONICS):
            k = h + 1
            if freq * k > 4200:
                break
            weight = (1.0 / k ** 1.15) + 0.35 * math.exp(-((k - centre) ** 2) / 6.0) / k ** 0.35
            s += weight * math.sin(phases[h] + steps[h] * t)
        buf[(start + t) % n] += env * s

peak = max(abs(x) for x in buf)
scale = 0.8 * 32767 / peak
out = pathlib.Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "res" / "raw" / "tanpura.wav"
out.parent.mkdir(parents=True, exist_ok=True)
with wave.open(str(out), "wb") as w:
    w.setnchannels(1)
    w.setsampwidth(2)
    w.setframerate(SR)
    w.writeframes(b"".join(struct.pack("<h", int(x * scale)) for x in buf))
print(f"wrote {out} ({out.stat().st_size // 1024} KB, {CYCLE}s loop, loop seam jump {abs(buf[0] - buf[-1]) * scale:.0f}/32767)")
