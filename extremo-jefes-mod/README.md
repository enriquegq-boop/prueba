# Extremo Jefes (mod para Fabric 26.2)

Jefes para la serie **Extremo**. Se van añadiendo de uno en uno.

| # | Jefe | Estado |
|---|---|---|
| 1 | Murciélago Mutante (+ Lepisma Sulfúrico) | ✅ hecho |

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
> **EXTREMO »** El Lepisma Sulfúrico ha despertado: pequeños parásitos engendrados por el temible Murciélago Mutante. Habilidad especial: si te golpea, te teletransportará un bloque hacia abajo.

| | |
|---|---|
| Vida | 300 (150 corazones), armadura 6 |
| Barra de jefe | Amarilla con su nombre; **roja** cuando se enfurece |
| Mordisco en picado | Se lanza contra ti y muerde (9 de daño, 12 aprox. enfurecido) |
| Bola de azufre | Escupe una bola que hace 7 de daño y envenena 4 s; enfurecido escupe 3 en abanico |
| Lepismas Sulfúricos | Engendra 2 cada ~11 s (máx. 6 vivos); enfurecido 4 cada ~8 s (máx. 10) |
| Furia (≤ 50 % de vida) | Chillido que te empuja hacia atrás, ojos rojos, ataques más rápidos y una oleada de 4 lepismas |
| Al morir | Todos **sus** lepismas mueren con él. Suelta 3-6 diamantes, 4-10 azufre potente, 1-2 manzanas de oro y 150 de experiencia |

**Lepisma Sulfúrico**: 10 de vida, 2 de daño. Si te golpea te hunde un bloque hacia abajo
(no funciona con bedrock/bloques irrompibles ni en creativo).

## Para desarrolladores

```bash
./gradlew build                      # compila -> build/libs/extremo-jefes-0.1.0.jar
./gradlew runGameTest                # pruebas automáticas en servidor
xvfb-run ./gradlew runClientGameTest # prueba visual (capturas en build/run/clientGameTest/screenshots)
python3 tools/generar_texturas.py <carpeta con texturas vanilla>   # regenera texturas
```

Requiere Java 25.
