"""
连连看水果蔬菜皮肤生成器
输出：resource/0.png ~ 15.png（64×64 像素棋盘格）
0 = 透明空位, 1-7 = 蔬菜, 8-15 = 水果
"""

from PIL import Image, ImageDraw
import os

SIZE = 64
OUT = r"D:\game-lianliankan\resource"
os.makedirs(OUT, exist_ok=True)


def fc(r, g, b, a=255):
    return (r, g, b, a)


class Canvas:
    """封装 Image + ImageDraw，各绘制函数接收 canvas"""
    def __init__(self):
        self.img = Image.new("RGBA", (SIZE, SIZE), (255, 255, 255, 0))
        self.d = ImageDraw.Draw(self.img)

    def fill(self, color):
        self.d.rectangle([(0, 0), (SIZE-1, SIZE-1)], fill=color)

    def ellipse(self, box, **kw):
        self.d.ellipse(box, **kw)

    def rectangle(self, box, **kw):
        self.d.rectangle(box, **kw)

    def polygon(self, xy, **kw):
        self.d.polygon(xy, **kw)

    def line(self, xy, **kw):
        self.d.line(xy, **kw)

    def arc(self, box, *args, **kw):
        self.d.arc(box, *args, **kw)

    def chord(self, box, *args, **kw):
        self.d.chord(box, *args, **kw)

    def save(self, path):
        self.img.save(path, "PNG")


# ============================================================
# VEGETABLES (1-7)
# ============================================================

def draw_carrot(c):
    """🥕 胡萝卜"""
    c.fill(fc(0xe8, 0xdc, 0xc0))
    c.polygon([(14, 52), (32, 8), (50, 52)], fill=fc(0xFF, 0x8C, 0x00))
    c.polygon([(14, 52), (32, 8), (32, 52)], fill=fc(0xE6, 0x7E, 0x22))
    c.line([(20, 38), (28, 20)], fill=fc(0xCC, 0x6A, 0x10), width=1)
    c.line([(28, 42), (32, 25)], fill=fc(0xCC, 0x6A, 0x10), width=1)
    c.line([(36, 38), (36, 22)], fill=fc(0xCC, 0x6A, 0x10), width=1)
    c.ellipse([(26, 2), (36, 14)], fill=fc(0x2E, 0x8B, 0x57))
    c.ellipse([(20, 4), (30, 16)], fill=fc(0x3C, 0xB3, 0x71))
    c.ellipse([(34, 4), (44, 16)], fill=fc(0x3C, 0xB3, 0x71))


def draw_tomato(c):
    """🍅 番茄"""
    c.fill(fc(0xe8, 0xdc, 0xc0))
    c.ellipse([(8, 12), (56, 56)], fill=fc(0xDC, 0x14, 0x3C))
    c.ellipse([(12, 16), (52, 52)], fill=fc(0xE5, 0x3E, 0x3E))
    c.ellipse([(18, 20), (30, 30)], fill=fc(0xFF, 0x99, 0x99, 160))
    c.polygon([(32, 6), (24, 16), (16, 10), (22, 18), (32, 20), (42, 18), (48, 10), (40, 16)], fill=fc(0x2E, 0x8B, 0x57))
    c.line([(32, 20), (32, 28)], fill=fc(0x1E, 0x6E, 0x3E), width=2)


def draw_broccoli(c):
    """🥦 西兰花"""
    c.fill(fc(0xe8, 0xdc, 0xc0))
    c.rectangle([(24, 40), (40, 56)], fill=fc(0x5B, 0x8C, 0x3E))
    c.ellipse([(10, 14), (38, 44)], fill=fc(0x2D, 0x6A, 0x2D))
    c.ellipse([(26, 10), (54, 40)], fill=fc(0x3A, 0x7D, 0x3A))
    c.ellipse([(16, 8), (48, 38)], fill=fc(0x4A, 0x8C, 0x4A))
    c.ellipse([(12, 20), (20, 30)], fill=fc(0x5B, 0xA3, 0x5B))
    c.ellipse([(28, 14), (36, 24)], fill=fc(0x5B, 0xA3, 0x5B))
    c.ellipse([(40, 20), (50, 30)], fill=fc(0x5B, 0xA3, 0x5B))


def draw_corn(c):
    """🌽 玉米"""
    c.fill(fc(0xe8, 0xdc, 0xc0))
    c.ellipse([(16, 14), (48, 54)], fill=fc(0xFD, 0xDA, 0x0E))
    c.ellipse([(20, 18), (44, 50)], fill=fc(0xFF, 0xE0, 0x4D))
    for y in range(22, 48, 6):
        for x in range(22, 44, 6):
            c.ellipse([(x, y), (x+4, y+4)], fill=fc(0xFF, 0xD7, 0x00))
    c.polygon([(16, 14), (8, 24), (14, 26), (18, 18)], fill=fc(0x6B, 0xA3, 0x3E))
    c.polygon([(48, 14), (56, 24), (50, 26), (46, 18)], fill=fc(0x5B, 0x8C, 0x3E))
    c.line([(30, 12), (28, 4)], fill=fc(0x8B, 0x6F, 0x3E), width=1)
    c.line([(34, 12), (36, 4)], fill=fc(0x8B, 0x6F, 0x3E), width=1)


def draw_eggplant(c):
    """🍆 茄子"""
    c.fill(fc(0xe8, 0xdc, 0xc0))
    c.ellipse([(18, 14), (46, 54)], fill=fc(0x48, 0x1E, 0x7A))
    c.ellipse([(22, 18), (42, 50)], fill=fc(0x5E, 0x2E, 0x8E))
    c.rectangle([(22, 20), (26, 38)], fill=fc(0x7B, 0x52, 0xA6, 100))
    c.ellipse([(26, 8), (38, 18)], fill=fc(0x2E, 0x7D, 0x32))
    c.polygon([(30, 8), (32, 2), (34, 8)], fill=fc(0x2E, 0x7D, 0x32))


def draw_cucumber(c):
    """🥒 黄瓜"""
    c.fill(fc(0xe8, 0xdc, 0xc0))
    c.ellipse([(14, 10), (50, 56)], fill=fc(0x3C, 0xA8, 0x3E))
    c.ellipse([(18, 14), (46, 52)], fill=fc(0x4C, 0xB8, 0x5E))
    c.line([(24, 16), (22, 50)], fill=fc(0x2E, 0x8B, 0x3A), width=2)
    c.line([(34, 14), (34, 48)], fill=fc(0x2E, 0x8B, 0x3A), width=2)
    c.line([(44, 16), (44, 44)], fill=fc(0x2E, 0x8B, 0x3A), width=2)
    c.ellipse([(18, 24), (22, 28)], fill=fc(0x5B, 0xC8, 0x6E))
    c.ellipse([(38, 34), (42, 38)], fill=fc(0x5B, 0xC8, 0x6E))
    c.ellipse([(26, 4), (38, 16)], fill=fc(0xFD, 0xDA, 0x0E))
    c.ellipse([(30, 6), (34, 10)], fill=fc(0xE8, 0x8A, 0x0E))


def draw_mushroom(c):
    """🍄 蘑菇"""
    c.fill(fc(0xe8, 0xdc, 0xc0))
    c.ellipse([(24, 34), (40, 56)], fill=fc(0xF5, 0xE6, 0xC8))
    c.ellipse([(26, 36), (38, 54)], fill=fc(0xFF, 0xF0, 0xD0))
    c.ellipse([(8, 10), (56, 42)], fill=fc(0xCC, 0x18, 0x18))
    c.ellipse([(12, 14), (52, 38)], fill=fc(0xE2, 0x30, 0x30))
    c.ellipse([(16, 16), (22, 22)], fill=fc(0xFF, 0xFF, 0xFF))
    c.ellipse([(30, 12), (36, 18)], fill=fc(0xFF, 0xFF, 0xFF))
    c.ellipse([(40, 20), (46, 26)], fill=fc(0xFF, 0xFF, 0xFF))
    c.ellipse([(22, 24), (26, 28)], fill=fc(0xFF, 0xFF, 0xFF))


# ============================================================
# FRUITS (8-15)
# ============================================================

def draw_apple(c):
    """🍎 苹果"""
    c.fill(fc(0xe8, 0xdc, 0xc0))
    c.ellipse([(10, 14), (54, 56)], fill=fc(0xDB, 0x22, 0x22))
    c.ellipse([(14, 18), (50, 52)], fill=fc(0xEE, 0x33, 0x33))
    c.ellipse([(42, 18), (50, 28)], fill=fc(0xFF, 0x66, 0x66, 100))
    c.line([(32, 8), (32, 18)], fill=fc(0x5C, 0x3A, 0x1E), width=2)
    c.ellipse([(28, 2), (36, 12)], fill=fc(0x3C, 0xB3, 0x71))
    c.ellipse([(24, 4), (32, 14)], fill=fc(0x2E, 0x8B, 0x57))


def draw_banana(c):
    """🍌 香蕉"""
    c.fill(fc(0xe8, 0xdc, 0xc0))
    c.arc([(8, 10), (52, 52)], 200, 340, fill=fc(0xFD, 0xDA, 0x0E), width=18)
    c.arc([(12, 14), (48, 48)], 200, 340, fill=fc(0xFF, 0xE6, 0x4D), width=14)
    c.line([(30, 12), (48, 42)], fill=fc(0x8B, 0x6F, 0x3E), width=1)
    c.ellipse([(38, 48), (48, 56)], fill=fc(0x6B, 0x4E, 0x2E))


def draw_orange(c):
    """🍊 橙子"""
    c.fill(fc(0xe8, 0xdc, 0xc0))
    c.ellipse([(10, 12), (54, 56)], fill=fc(0xFF, 0x8C, 0x00))
    c.ellipse([(14, 16), (50, 52)], fill=fc(0xFF, 0xA0, 0x20))
    c.ellipse([(38, 18), (48, 28)], fill=fc(0xFF, 0xCC, 0x66, 120))
    c.ellipse([(26, 48), (38, 56)], fill=fc(0xE6, 0x7E, 0x22))
    c.ellipse([(30, 50), (34, 54)], fill=fc(0xCC, 0x6A, 0x10))
    c.ellipse([(28, 4), (36, 14)], fill=fc(0x2E, 0x8B, 0x57))


def draw_grape(c):
    """🍇 葡萄"""
    c.fill(fc(0xe8, 0xdc, 0xc0))
    positions = [(30, 10), (20, 18), (40, 18), (14, 28), (26, 26), (38, 26), (50, 28),
                 (18, 38), (30, 36), (42, 38), (24, 48), (36, 48)]
    for px, py in positions:
        c.ellipse([(px-5, py-5), (px+5, py+5)], fill=fc(0x8B, 0x20, 0xA0))
        c.ellipse([(px-3, py-3), (px+3, py+3)], fill=fc(0x9B, 0x30, 0xB8))
        c.ellipse([(px-1, py-3), (px+1, py-1)], fill=fc(0xB8, 0x60, 0xD0, 140))
    c.line([(30, 4), (30, 14)], fill=fc(0x5C, 0x3A, 0x1E), width=2)
    c.ellipse([(20, 2), (34, 12)], fill=fc(0x3C, 0xA8, 0x3E))


def draw_watermelon(c):
    """🍉 西瓜"""
    c.fill(fc(0xe8, 0xdc, 0xc0))
    c.ellipse([(6, 14), (58, 56)], fill=fc(0x2E, 0x8B, 0x57))
    c.ellipse([(10, 18), (54, 52)], fill=fc(0x3C, 0xA8, 0x3E))
    c.chord([(6, 14), (58, 56)], 0, 180, fill=fc(0xDC, 0x14, 0x3C))
    seeds = [(18, 40), (28, 44), (38, 42), (48, 40), (23, 36), (33, 34), (43, 36)]
    for sx, sy in seeds:
        c.ellipse([(sx-1, sy-1), (sx+1, sy+1)], fill=fc(0x1A, 0x1A, 0x1A))
    c.arc([(6, 14), (58, 56)], 0, 180, fill=fc(0x1E, 0x6E, 0x3E), width=2)


def draw_peach(c):
    """🍑 桃子"""
    c.fill(fc(0xe8, 0xdc, 0xc0))
    c.ellipse([(12, 14), (52, 54)], fill=fc(0xFF, 0x99, 0x99))
    c.ellipse([(16, 18), (48, 50)], fill=fc(0xFF, 0xB3, 0xB3))
    c.arc([(16, 20), (48, 50)], 20, 160, fill=fc(0xF0, 0x80, 0x80), width=1)
    c.ellipse([(40, 20), (48, 28)], fill=fc(0xFF, 0xDD, 0xDD, 140))
    c.ellipse([(30, 4), (40, 14)], fill=fc(0x3C, 0xA8, 0x3E))
    c.line([(35, 8), (35, 18)], fill=fc(0x5C, 0x3A, 0x1E), width=1)


def draw_lemon(c):
    """🍋 柠檬"""
    c.fill(fc(0xe8, 0xdc, 0xc0))
    c.ellipse([(12, 12), (52, 54)], fill=fc(0xFD, 0xDA, 0x0E))
    c.ellipse([(16, 16), (48, 50)], fill=fc(0xFF, 0xE6, 0x4D))
    c.ellipse([(38, 18), (46, 26)], fill=fc(0xFF, 0xF8, 0xAA, 140))
    c.ellipse([(16, 24), (20, 40)], fill=fc(0xE8, 0xC8, 0x0E))
    c.ellipse([(44, 24), (48, 40)], fill=fc(0xE8, 0xC8, 0x0E))
    c.ellipse([(28, 6), (38, 16)], fill=fc(0x3C, 0xA8, 0x3E))


def draw_strawberry(c):
    """🍓 草莓"""
    c.fill(fc(0xe8, 0xdc, 0xc0))
    c.polygon([(12, 52), (32, 10), (52, 52)], fill=fc(0xDB, 0x22, 0x22))
    c.polygon([(18, 52), (32, 14), (46, 52)], fill=fc(0xEE, 0x33, 0x33))
    c.polygon([(18, 52), (26, 20), (32, 18)], fill=fc(0xFF, 0x66, 0x66, 120))
    seeds = [(22, 28), (30, 22), (38, 28), (26, 36), (34, 34), (30, 42)]
    for sx, sy in seeds:
        c.ellipse([(sx-1, sy-1), (sx+1, sy+1)], fill=fc(0xFF, 0xEB, 0x3B))
    c.polygon([(20, 8), (16, 18), (26, 14), (32, 8), (38, 14), (48, 18), (44, 8), (32, 4)], fill=fc(0x2E, 0x8B, 0x57))


# ============================================================
# GENERATE ALL
# ============================================================

generators = [
    ("0.png", lambda c: None),          # 透明空位
    ("1.png", draw_carrot),
    ("2.png", draw_tomato),
    ("3.png", draw_broccoli),
    ("4.png", draw_corn),
    ("5.png", draw_eggplant),
    ("6.png", draw_cucumber),
    ("7.png", draw_mushroom),
    ("8.png", draw_apple),
    ("9.png", draw_banana),
    ("10.png", draw_orange),
    ("11.png", draw_grape),
    ("12.png", draw_watermelon),
    ("13.png", draw_peach),
    ("14.png", draw_lemon),
    ("15.png", draw_strawberry),
]

for name, fn in generators:
    c = Canvas()
    fn(c)
    c.save(os.path.join(OUT, name))
    print(f"OK {name}")

print(f"\nDone. Generated {len(generators)} tiles.")
