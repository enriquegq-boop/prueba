"""Genera las texturas del mod (Murciélago Mutante, ojos, lepisma, objetos).

Uso:  python3 tools/generar_texturas.py <carpeta_texturas_vanilla>
La carpeta vanilla debe tener silverfish.png, bat_spawn_egg.png y fire_charge.png
(se usan solo como plantilla de forma; los colores se repintan).
"""
import os
import random
import sys

from PIL import Image

RAIZ = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "extremo_jefes", "textures")
VAN = sys.argv[1] if len(sys.argv) > 1 else "."
rnd = random.Random(2026)

# Paleta
PIEL = [(58, 12, 18), (74, 18, 22), (92, 22, 26), (110, 28, 30)]
PIEL_CLARA = [(128, 36, 36), (146, 44, 42)]
VIENTRE = [(150, 118, 70), (168, 132, 80), (138, 104, 62)]
AZUFRE = [(232, 210, 58), (246, 228, 92), (206, 182, 40)]
AZUFRE_OSC = (168, 140, 24)
HUESO = [(214, 200, 170), (196, 182, 150)]
HUESO_OSC = (120, 100, 70)
MEMBRANA = [(118, 30, 34), (128, 34, 38), (108, 26, 30)]
VENA = (52, 10, 14)
BORDE = [(206, 184, 172), (190, 166, 152)]
NARIZ = (70, 22, 30)
COLMILLO = (242, 238, 222)
OJO_BASE = (200, 160, 30)


def nuevo(w, h):
    return Image.new("RGBA", (w, h), (0, 0, 0, 0))


def pinta(img, x0, y0, w, h, paleta, manchas=None, prob=0.0):
    for x in range(x0, x0 + w):
        for y in range(y0, y0 + h):
            c = rnd.choice(paleta)
            if manchas and rnd.random() < prob:
                c = rnd.choice(manchas)
            img.putpixel((x, y), c + (255,))


def caja(img, u, v, w, h, d, lados, frente=None, arriba=None, atras=None, abajo=None, manchas=None, prob=0.0):
    """Pinta el despliegue UV estándar de un cubo de Minecraft."""
    pinta(img, u + d, v, w, d, arriba or lados, manchas, prob)          # arriba
    pinta(img, u + d + w, v, w, d, abajo or lados, manchas, prob)       # abajo
    pinta(img, u, v + d, d, h, lados, manchas, prob)                    # lado
    pinta(img, u + d, v + d, w, h, frente or lados, manchas, prob)      # frente
    pinta(img, u + d + w, v + d, d, h, lados, manchas, prob)            # otro lado
    pinta(img, u + 2 * d + w, v + d, w, h, atras or lados, manchas, prob)  # detrás


def membrana(img, u, v, w, h, espejo=False, dedos=None):
    """Plano de ala: hueso arriba, dedos, borde inferior festoneado, rasgones y manchas de azufre."""
    for cara in range(2):  # delante y detrás
        ox = u + cara * w
        dedos = dedos if dedos is not None else [0, w // 3, (2 * w) // 3, w - 1]
        for x in range(w):
            xx = (w - 1 - x) if (espejo ^ (cara == 1)) else x
            # borde inferior en ondas: profundo entre dedos
            dist = min(abs(xx - d) for d in dedos)
            limite = h - 1 - min(dist, 4)
            for y in range(h):
                if y > limite:
                    continue
                if y == 0 or y == 1:
                    c = rnd.choice(HUESO) if y == 0 else HUESO_OSC
                elif dist == 0:
                    c = VENA
                elif y >= limite - 1:
                    c = rnd.choice(BORDE)
                else:
                    c = rnd.choice(MEMBRANA)
                    if rnd.random() < 0.05:
                        c = rnd.choice(AZUFRE)
                img.putpixel((ox + x, v + y), c + (255,))
        # rasgones (agujeros)
        for _ in range(max(1, w // 6)):
            hx, hy = rnd.randrange(2, w - 1), rnd.randrange(4, max(5, h - 5))
            for dx, dy in ((0, 0), (1, 0), (0, 1)):
                if 0 <= hx + dx < w and hy + dy < h:
                    img.putpixel((ox + hx + dx, v + hy + dy), (0, 0, 0, 0))


def oreja(img, u, v, w, h):
    for cara in range(2):
        ox = u + cara * w
        for x in range(w):
            for y in range(h):
                # forma puntiaguda: más estrecha arriba
                ancho = max(1, int(w * (y + 2) / (h + 1)))
                if x >= ancho:
                    continue
                borde = x == 0 or x == ancho - 1 or y == h - 1
                c = rnd.choice(PIEL) if borde else (176, 84, 90)
                img.putpixel((ox + x, v + y), c + (255,))


def murcielago():
    img = nuevo(128, 64)
    # cuerpo 6x10x4 en (0,0): vientre claro delante, cristales de azufre incrustados detrás
    caja(img, 0, 0, 6, 10, 4, PIEL, frente=VIENTRE, manchas=AZUFRE, prob=0.06)
    for _ in range(6):  # costras de azufre en la espalda
        x, y = rnd.randrange(14, 20), rnd.randrange(4, 14)
        img.putpixel((x, y), rnd.choice(AZUFRE) + (255,))
    # cabeza 8x6x6 en (0,16)
    caja(img, 0, 16, 8, 6, 6, PIEL, frente=PIEL_CLARA + PIEL[2:], manchas=AZUFRE, prob=0.03)
    # cara: frente en (6,22) 8x6 -> ojos y ceño
    for x in range(6, 14):
        img.putpixel((x, 22), PIEL[0] + (255,))
    for x in (7, 8, 11, 12):
        img.putpixel((x, 24), OJO_BASE + (255,))
        img.putpixel((x, 23), PIEL[0] + (255,))
    # hocico 4x3x2 en (28,16)
    caja(img, 28, 16, 4, 3, 2, PIEL_CLARA)
    img.putpixel((31, 19), NARIZ + (255,))
    img.putpixel((32, 19), NARIZ + (255,))
    # colmillos 1x2x1 en (40,16) y (44,16)
    caja(img, 40, 16, 1, 2, 1, [COLMILLO])
    caja(img, 44, 16, 1, 2, 1, [COLMILLO])
    # orejas planas 4x8 en (0,30) y (8,30)
    oreja(img, 0, 30, 4, 8)
    oreja(img, 8, 30, 4, 8)
    # cuernos 2x5x2 en (16,30) y (24,30): hueso con punta oscura
    for u in (16, 24):
        caja(img, u, 30, 2, 5, 2, HUESO, arriba=[HUESO_OSC])
        for x in range(u, u + 8):
            img.putpixel((x, 32), HUESO_OSC + (255,))
    # cristales de azufre 2x3x2 / 2x4x2 en (32,30), (40,30), (48,30)
    caja(img, 32, 30, 2, 3, 2, AZUFRE, arriba=[(255, 244, 140)])
    caja(img, 40, 30, 2, 4, 2, AZUFRE, arriba=[(255, 244, 140)])
    caja(img, 48, 30, 2, 3, 2, AZUFRE, arriba=[(255, 244, 140)])
    for x in range(32, 56):
        for y in range(32, 36):
            p = img.getpixel((x, y))
            if p[3] and rnd.random() < 0.3:
                img.putpixel((x, y), AZUFRE_OSC + (255,))
    # alas internas 4x14 en (48,0) y (56,0); puntas 12x16 en (64,0) y (88,0)
    membrana(img, 48, 0, 4, 14, dedos=[0])
    membrana(img, 56, 0, 4, 14, espejo=True, dedos=[3])
    membrana(img, 64, 0, 12, 16)
    membrana(img, 88, 0, 12, 16, espejo=True)
    # patas 6x3 en (48,16)
    for cara in range(2):
        for x in range(6):
            for y in range(3):
                if x in (0, 2, 3, 5) or y == 0:
                    img.putpixel((48 + cara * 6 + x, 16 + y), (rnd.choice(PIEL) if y < 2 else HUESO_OSC) + (255,))
    return img


def ojos(color):
    img = nuevo(128, 64)
    for x in (7, 8, 11, 12):
        img.putpixel((x, 24), color + (255,))
    return img


def recolorear(ruta, paleta_oscuro_claro, conservar_transparencia=True):
    base = Image.open(ruta).convert("RGBA")
    out = nuevo(*base.size)
    n = len(paleta_oscuro_claro) - 1
    for x in range(base.width):
        for y in range(base.height):
            r, g, b, a = base.getpixel((x, y))
            if a == 0:
                continue
            lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
            out.putpixel((x, y), paleta_oscuro_claro[min(n, int(lum * (n + 1)))] + (a,))
    return out


def bola():
    img = nuevo(16, 16)
    for x in range(16):
        for y in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            if d < 3:
                c = (255, 250, 190)
            elif d < 5:
                c = (246, 226, 80)
            elif d < 6.5:
                c = (210, 170, 30)
            elif d < 7.5 and rnd.random() < 0.6:
                c = (150, 110, 20)
            else:
                continue
            img.putpixel((x, y), c + (255,))
    return img


def guardar(img, rel):
    ruta = os.path.join(RAIZ, rel)
    os.makedirs(os.path.dirname(ruta), exist_ok=True)
    img.save(ruta)
    print("ok", rel)


if __name__ == "__main__":
    guardar(murcielago(), "entity/murcielago_mutante/murcielago_mutante.png")
    guardar(ojos((255, 226, 60)), "entity/murcielago_mutante/murcielago_mutante_ojos.png")
    guardar(ojos((255, 40, 24)), "entity/murcielago_mutante/murcielago_mutante_ojos_furia.png")
    guardar(recolorear(os.path.join(VAN, "silverfish.png"),
                       [(92, 70, 14), (150, 122, 24), (206, 182, 40), (236, 214, 70), (252, 238, 130)]),
            "entity/lepisma_sulfurico.png")
    guardar(recolorear(os.path.join(VAN, "bat_spawn_egg.png"),
                       [(40, 8, 12), (74, 18, 22), (110, 28, 30), (206, 182, 40), (246, 228, 92)]),
            "item/murcielago_mutante_spawn_egg.png")
    guardar(recolorear(os.path.join(VAN, "silverfish_spawn_egg.png"),
                       [(92, 70, 14), (150, 122, 24), (206, 182, 40), (236, 214, 70), (252, 238, 130)]),
            "item/lepisma_sulfurico_spawn_egg.png")
    guardar(bola(), "item/bola_azufre.png")
    icono = murcielago().crop((0, 16, 28, 28)).resize((112, 48), Image.NEAREST)
    lienzo = nuevo(128, 128)
    lienzo.paste(icono, (8, 40))
    guardar(lienzo, "../icon.png")
