# Extremo Jefes (mod para Fabric 26.2)

Jefes para la serie **Extremo**. Se van añadiendo de uno en uno.

| # | Jefe | Estado |
|---|---|---|
| 1 | Murciélago Mutante | ✅ hecho |

## Instalación

1. Minecraft **26.2** con **Fabric Loader 0.20.1** o superior.
2. Arrastra `extremo-jefes-0.1.0.jar` a la carpeta `mods`. **Fabric API ya va dentro**, no hace falta
   descargarla aparte (si ya la tienes instalada, no pasa nada: se usa la más nueva).
3. En un servidor, el mod debe estar **en el servidor y en todos los jugadores**.

## Murciélago Mutante

Invocarlo:

- Huevo: pestaña *Huevos de generación* → **Huevo de Murciélago Mutante**.
- Comando: `/summon extremo_jefes:murcielago_mutante`

Al aparecer avisa en el chat a los jugadores cercanos (96 bloques):

> **EXTREMO »** ¡El Murciélago Mutante ha despertado en las minas infectadas!

| | |
|---|---|
| Vida | 300 (150 corazones), armadura 6 |
| Barra de vida | **Estilo Extremo, arriba a la izquierda**: medallón dorado con goterones de azufre, barra dorada naranja-amarilla y el nombre debajo. En furia se pone roja. Las barras de otros jefes (Wither, Dragón) siguen saliendo normales en el centro |
| Mordisco en picado | Se lanza contra ti y muerde (9 de daño) |
| Bola de azufre | Escupe una bola que hace 7 de daño y envenena 4 s; enfurecido escupe 3 en abanico |
| Furia (≤ 50 % de vida) | Chillido que te empuja hacia atrás, ojos y barra rojos, ataques más rápidos |
| Al morir | Suelta 3-6 diamantes, 4-10 azufre potente, 1-2 manzanas de oro y 150 de experiencia |

## Integración con Extremo3 (mod de WasterTV)

Si en la carpeta `mods` está también **Extremo3** (con GeckoLib), el Murciélago Mutante:

- Engendra los **Lepismas Sulfúricos de Extremo3** junto a ti: 2 cada ~11 s (máx. 6 vivos);
  enfurecido 4 cada ~8 s (máx. 10) y una oleada de 4 al enfurecerse.
- Al morir, **sus** lepismas mueren con él (los demás no).
- Usa **la misma barra de vida que el Dragón de Azufre y el Piglin Brutal Mutante** de Extremo3
  (su textura, misma posición arriba a la izquierda y mismos colores).

Sin Extremo3 el jefe funciona igual, sin lepismas y con su barra propia.
Este mod **no incluye ni modifica** Extremo3: solo lo usa si está instalado (su licencia no permite
redistribuirlo ni modificarlo).

## Para desarrolladores

```bash
./gradlew build                      # compila -> build/libs/extremo-jefes-0.1.0.jar
./gradlew runGameTest                # pruebas automáticas en servidor
xvfb-run ./gradlew runClientGameTest # prueba visual (capturas en build/run/clientGameTest/screenshots)
python3 tools/generar_texturas.py <carpeta con texturas vanilla>   # regenera texturas
```

Requiere Java 25.
