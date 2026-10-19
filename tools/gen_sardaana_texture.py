#!/usr/bin/env python3
"""Рисует текстуру сардааны (лилия даурская) 16×16 по символьной карте.

Запуск из корня репозитория:  python3 tools/gen_sardaana_texture.py
Нужен Pillow:                 pip install pillow
"""
from pathlib import Path

from PIL import Image

OUT = Path(__file__).resolve().parent.parent / "src/main/resources/assets/edition1/textures/block/sardaana.png"

# Чашка из шести лепестков, раскрытая вверх, с тёмными крапинками; стебель с узкими листьями
ART = [
    "................",
    "..R....RR....R..",
    "..RO..ROOR..OR..",
    "...RO.RodR.OR...",
    "..RROdROOR.OdRR.",
    "..ROOOROORdOOOR.",
    "...ROoOyyOoOR...",
    "....RROyyORR....",
    ".....RrRRrR.....",
    ".......g.l......",
    "....l..gl.......",
    ".....l.g........",
    "......Lg..l.....",
    ".......gLl......",
    "......lg........",
    ".......g........",
]

PALETTE = {
    ".": (0, 0, 0, 0),
    "R": (214, 40, 24, 255),     # огненно-красный край лепестков
    "O": (246, 98, 30, 255),     # оранжевая середина лепестка
    "o": (255, 146, 58, 255),    # светлый блик
    "r": (150, 26, 18, 255),     # тень под чашкой
    "d": (90, 20, 14, 255),      # тёмные крапинки
    "y": (255, 206, 70, 255),    # тычинки
    "g": (58, 120, 40, 255),     # стебель
    "l": (82, 150, 52, 255),     # листья
    "L": (46, 100, 32, 255),     # тень листьев
}


def main():
    im = Image.new("RGBA", (16, 16))
    for y, row in enumerate(ART):
        assert len(row) == 16, (y, row)
        for x, ch in enumerate(row):
            im.putpixel((x, y), PALETTE[ch])
    OUT.parent.mkdir(parents=True, exist_ok=True)
    im.save(OUT)
    print("texture written to", OUT)


if __name__ == "__main__":
    main()
