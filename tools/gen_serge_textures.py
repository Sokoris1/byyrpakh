#!/usr/bin/env python3
"""Генерирует текстуры сэргэ (резного столба) и портала в Туймааду.

Запуск из корня репозитория:  python3 tools/gen_serge_textures.py
Нужен Pillow:                 pip install pillow
"""
import random
from pathlib import Path

from PIL import Image

OUT = Path(__file__).resolve().parent.parent / "src/main/resources/assets/edition1/textures/block"

WOOD = [(176, 138, 92), (164, 126, 82), (188, 150, 102), (156, 118, 76)]   # светлая лиственница
GROOVE = (96, 68, 42)                                                     # резные бороздки
GLOW = [(255, 196, 92), (255, 168, 64), (255, 220, 140)]                  # тлеющая резьба
# Строки с резьбой: пояса по высоте части столба
GROOVE_ROWS = {2, 3, 7, 12, 13}
NOTCH_ROWS = {5, 10}   # ряды с ромбиками-насечками


def side(lit, rnd):
    im = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            c = WOOD[(x * 7 + y * 3 + rnd.randrange(3)) % len(WOOD)]
            if x % 5 == 0:                       # продольные волокна
                c = tuple(v - 10 for v in c)
            groove = y in GROOVE_ROWS or (y in NOTCH_ROWS and x % 4 in (1, 2))
            if groove:
                c = rnd.choice(GLOW) if lit else GROOVE
            im.putpixel((x, y), c + (255,))
    return im


def top(lit, rnd):
    im = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            ring = int(d) % 3 == 0                # годовые кольца на торце
            c = WOOD[(x + y + rnd.randrange(2)) % len(WOOD)]
            if ring:
                c = rnd.choice(GLOW) if lit and d < 4 else tuple(v - 25 for v in c)
            im.putpixel((x, y), c + (255,))
    return im


def portal_frames(rnd, frames=8):
    """Золотистое мерцающее пятно: кадры сверху вниз, анимация через .mcmeta."""
    im = Image.new("RGBA", (16, 16 * frames))
    sparks = [(rnd.randrange(16), rnd.randrange(16), rnd.randrange(frames)) for _ in range(18)]
    for f in range(frames):
        for y in range(16):
            for x in range(16):
                t = (x * 3 + y * 5 + f * 4) % 16 / 16
                r, g, b = 255, int(160 + 70 * t), int(40 + 90 * t)
                im.putpixel((x, f * 16 + y), (r, g, b, 225))
        for sx, sy, sf in sparks:
            if (f - sf) % frames < 2:
                im.putpixel((sx, f * 16 + sy), (255, 255, 240, 255))
    return im


def main():
    rnd = random.Random(1632)   # фиксированный сид — текстуры не меняются от запуска к запуску
    OUT.mkdir(parents=True, exist_ok=True)
    side(False, rnd).save(OUT / "serge.png")
    side(True, rnd).save(OUT / "serge_lit.png")
    top(False, rnd).save(OUT / "serge_top.png")
    top(True, rnd).save(OUT / "serge_top_lit.png")
    portal_frames(rnd).save(OUT / "tuymaada_portal.png")
    (OUT / "tuymaada_portal.png.mcmeta").write_text('{\n  "animation": {\n    "frametime": 3\n  }\n}\n')
    print("textures written to", OUT)


if __name__ == "__main__":
    main()
