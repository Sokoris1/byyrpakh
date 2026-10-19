#!/usr/bin/env python3
"""Текстуры золотой лиственницы: хвоя (листья) и саженец, 16×16.

Запуск из корня репозитория:  python3 tools/gen_larch_textures.py
Нужен Pillow:                 pip install pillow
"""
import random
from pathlib import Path

from PIL import Image

OUT = Path(__file__).resolve().parent.parent / "src/main/resources/assets/edition1/textures/block"

GOLD = [(214, 158, 34), (232, 182, 48), (244, 204, 72), (198, 138, 28), (252, 222, 110)]
TWIG = (120, 84, 52)


def leaves(rnd):
    """Пучки хвои: короткие штрихи-иголки разных оттенков золота, между ними — просветы."""
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    # Основа: почти сплошная, с редкими просветами (на «быстрой» графике просветы не видны)
    for y in range(16):
        for x in range(16):
            if rnd.random() < 0.82:
                im.putpixel((x, y), rnd.choice(GOLD[:4]) + (255,))
    # Иголки: короткие диагональные штрихи светлого и тёмного золота
    for _ in range(26):
        x, y = rnd.randrange(16), rnd.randrange(16)
        c = rnd.choice([GOLD[4], GOLD[3], GOLD[2]])
        dx = rnd.choice([-1, 1])
        for i in range(3):
            im.putpixel(((x + i * dx) % 16, (y + i) % 16), c + (255,))
    # Редкие веточки
    for _ in range(3):
        im.putpixel((rnd.randrange(16), rnd.randrange(16)), TWIG + (255,))
    return im


SAPLING = [
    "................",
    "................",
    ".......y........",
    "......yGy.......",
    ".....yGgGy......",
    "......gtg.......",
    ".....yGtGy......",
    "....yGgtgGy.....",
    "......gtg.......",
    ".....yGtGGy.....",
    "....yGGtgGGy....",
    "...yGgGtGgGy....",
    ".......t........",
    ".......t........",
    ".......t........",
    ".......t........",
]
SAPLING_PALETTE = {
    ".": (0, 0, 0, 0),
    "y": GOLD[4] + (255,),
    "G": GOLD[1] + (255,),
    "g": GOLD[3] + (255,),
    "t": TWIG + (255,),
}


def sapling():
    im = Image.new("RGBA", (16, 16))
    for y, row in enumerate(SAPLING):
        assert len(row) == 16, (y, row)
        for x, ch in enumerate(row):
            im.putpixel((x, y), SAPLING_PALETTE[ch])
    return im


def main():
    rnd = random.Random(1997)
    OUT.mkdir(parents=True, exist_ok=True)
    leaves(rnd).save(OUT / "golden_larch_leaves.png")
    sapling().save(OUT / "larch_sapling.png")
    print("textures written to", OUT)


if __name__ == "__main__":
    main()
