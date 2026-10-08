#!/usr/bin/env python3
"""Генерирует текстуры 32×32 для блоков-бутылок из текстур предметов.

Запуск из корня репозитория:  python3 tools/gen_bottle_textures.py
Нужен Pillow:                 pip install pillow

Развёртка (пиксели текстуры 32×32; в моделях UV = пиксели / 2):
  y 0..19,  x 0..32   бока тела: север | запад | юг | восток (по 8 px)
  y 19..27, x 0..8    верх тела          x 8..16   низ тела
            x 16..22  верх плечиков (6×6) x 22..26  верх крышки (4×4)
  y 27..29, x 0..24   бока плечиков (4 × 6 px)
  y 29..31, x 0..16   бока горлышка (4 × 4 px)
            x 16..32  бока крышки   (4 × 4 px)

Север и юг берут тело бутылки с текстуры предмета, запад и восток — его
зеркало, поэтому этикетка и жидкость сходятся на углах без шва.
"""
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent / "src/main/resources/assets/edition1/textures"

# предмет -> блок
PAIRS = {
    "byirpah": "byirpah_bottle",
    "byirpah_aged": "byirpah_aged_bottle",
    "byirpah_strong": "byirpah_strong_bottle",
}

# Где что лежит на текстуре предмета 32×32
# 8 столбцов тела: внутренние 12..18 без контуров по краям + 17 ещё раз, чтобы
# на стыке с зеркальной гранью вышло 18,17 | 17,18
BODY_X = [12, 13, 14, 15, 16, 17, 18, 17]
BODY_ROWS = list(range(10, 29))  # 19 строк: жидкость (10..18), этикетка (19..26), низ (27..28)
LIQUID_ROWS = list(range(6, 19))
SHOULDER_ROW = 5
NECK_ROW = 3
CAP_ROWS = (1, 2)
CAP_X = range(14, 17)


def px(im, x, y):
    return im.getpixel((x, y))


def mix(c, k):
    """Осветлить (k > 1) или затемнить (k < 1) цвет."""
    return tuple(max(0, min(255, round(v * k))) for v in c[:3]) + (255,)


def average(colors):
    n = len(colors)
    return tuple(round(sum(c[i] for c in colors) / n) for i in range(3)) + (255,)


def flatten_columns(im):
    """Убирает запечённое «цилиндрическое» затенение предмета (тёмный левый край,
    светлый правый): в мире свет по граням и так даёт Minecraft."""
    lum = lambda c: 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]
    col_lum = {x: sum(lum(px(im, x, y)) for y in LIQUID_ROWS) / len(LIQUID_ROWS) for x in set(BODY_X)}
    target = sum(col_lum.values()) / len(col_lum)
    return {x: target / col_lum[x] for x in BODY_X}


def body_face(item):
    """8×19: бок тела, снятый с предмета."""
    gain = flatten_columns(item)
    face = Image.new("RGBA", (8, 19))
    for fx, x in enumerate(BODY_X):
        for fy, y in enumerate(BODY_ROWS):
            c = px(item, x, y)
            # выравниваем только жидкость, этикетку не трогаем
            face.putpixel((fx, fy), mix(c, gain[x]) if y in LIQUID_ROWS else c)
    return face


def ring(size, inner, edge, fill):
    """Квадрат size×size: рамка цвета edge, внутри fill (видимая кромка вокруг детали сверху)."""
    im = Image.new("RGBA", (size, size), fill)
    for i in range(size):
        for j in range(size):
            if min(i, j, size - 1 - i, size - 1 - j) < (size - inner) // 2:
                im.putpixel((i, j), edge)
    return im


def generate(item):
    out = Image.new("RGBA", (32, 32), (0, 0, 0, 0))

    # Бока тела: Л | зеркало | Л | зеркало — без швов на углах
    face = body_face(item)
    mirror = face.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
    for i, part in enumerate((face, mirror, face, mirror)):
        out.paste(part, (i * 8, 0))

    liquid = average([px(item, x, y) for x in BODY_X for y in LIQUID_ROWS])
    plastic = average([px(item, x, SHOULDER_ROW) for x in range(13, 19)])

    # Верх тела: кромка пластика вокруг плечиков; низ — донышко
    out.paste(ring(8, 6, mix(plastic, 1.05), liquid), (0, 19))
    out.paste(ring(8, 6, mix(plastic, 0.8), mix(liquid, 0.85)), (8, 19))

    # Плечики: 6 px из строки плечиков предмета, бока Л/зеркало
    shoulder = Image.new("RGBA", (6, 2))
    for i, x in enumerate(range(13, 19)):
        c = px(item, x, SHOULDER_ROW)
        shoulder.putpixel((i, 0), mix(c, 1.05))
        shoulder.putpixel((i, 1), c)
    shoulder_m = shoulder.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
    for i, part in enumerate((shoulder, shoulder_m, shoulder, shoulder_m)):
        out.paste(part, (i * 6, 27))
    out.paste(ring(6, 4, mix(plastic, 1.1), plastic), (16, 19))

    # Горлышко: прозрачный пластик из строки горлышка предмета
    neck_l, neck_r = px(item, CAP_X[0], NECK_ROW), px(item, CAP_X[-1], NECK_ROW)
    neck = Image.new("RGBA", (4, 2))
    for i, c in enumerate((neck_l, neck_r, neck_r, neck_l)):
        neck.putpixel((i, 0), c)
        neck.putpixel((i, 1), mix(c, 0.9))
    for i in range(4):
        out.paste(neck, (i * 4, 29))

    # Крышка: синяя, верхняя строка светлее
    cap_dark = px(item, CAP_X[0], CAP_ROWS[1])
    cap_mid = px(item, CAP_X[1], CAP_ROWS[0])
    cap_light = px(item, CAP_X[2], CAP_ROWS[0])
    cap = Image.new("RGBA", (4, 2))
    for i, c in enumerate((cap_mid, cap_light, cap_mid, cap_dark)):
        cap.putpixel((i, 0), c)
        cap.putpixel((i, 1), mix(c, 0.8))
    for i in range(4):
        out.paste(cap, (16 + i * 4, 29))
    cap_top = ring(4, 2, cap_mid, cap_light)
    out.paste(cap_top, (22, 19))

    return out


def main():
    for item_name, block_name in PAIRS.items():
        item = Image.open(ROOT / "item" / f"{item_name}.png").convert("RGBA")
        generate(item).save(ROOT / "block" / f"{block_name}.png")
        print(f"item/{item_name}.png -> block/{block_name}.png")


if __name__ == "__main__":
    main()
