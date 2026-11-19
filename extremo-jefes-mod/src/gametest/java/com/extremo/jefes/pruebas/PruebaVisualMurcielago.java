package com.extremo.jefes.pruebas;

import com.extremo.jefes.entity.LepismaSulfurico;
import com.extremo.jefes.entity.MurcielagoMutante;
import com.extremo.jefes.registry.ModEntities;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.server.level.ServerLevel;

/**
 * Prueba visual: arranca el juego de verdad, invoca al Murciélago Mutante y saca capturas.
 * También comprueba que funciona en un servidor dedicado (como el de una serie).
 */
public class PruebaVisualMurcielago implements FabricClientGameTest {
	@Override
	public void runTest(final ClientGameTestContext ctx) {
		try (TestSingleplayerContext mundo = ctx.worldBuilder().create()) {
			mundo.getConnection().waitForChunksRender();
			var srv = mundo.getServer();
			srv.runCommand("gamerule advance_time false");
			srv.runCommand("time set noon");
			srv.runCommand("weather clear");
			srv.runCommand("gamemode creative @a");
			srv.runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 0");
			srv.runCommand("execute as @p at @s run fill ~-12 ~-1 ~-2 ~12 ~-1 ~22 minecraft:stone");
			srv.runCommand("execute as @p at @s run fill ~-12 ~ ~-2 ~12 ~14 ~22 minecraft:air");
			ctx.waitTicks(10);

			// 1) De frente, quieto, para ver el diseño.
			srv.runCommand("execute as @p at @s run summon extremo_jefes:murcielago_mutante ~ ~1 ~9 {NoAI:1b,Rotation:[180f,0f]}");
			ctx.waitTicks(40);
			ctx.takeScreenshot("01_murcielago_frente");

			srv.runCommand("execute as @e[type=extremo_jefes:murcielago_mutante] run data merge entity @s {Rotation:[90f,0f]}");
			ctx.waitTicks(20);
			ctx.takeScreenshot("02_murcielago_lado");

			srv.runCommand("execute as @e[type=extremo_jefes:murcielago_mutante] run data merge entity @s {Rotation:[0f,0f]}");
			ctx.waitTicks(20);
			ctx.takeScreenshot("03_murcielago_espalda");

			// 2) Enfurecido y de noche: ojos rojos brillantes.
			srv.runCommand("execute as @e[type=extremo_jefes:murcielago_mutante] run data merge entity @s {Rotation:[180f,0f],Enfurecido:1b}");
			srv.runCommand("time set midnight");
			ctx.waitTicks(40);
			ctx.takeScreenshot("04_murcielago_noche_enfurecido");

			// 3) Lepismas sulfúricos y bola de azufre.
			srv.runCommand("time set noon");
			srv.runCommand("kill @e[type=extremo_jefes:murcielago_mutante]");
			srv.runCommand("execute as @p at @s run summon extremo_jefes:lepisma_sulfurico ~-1 ~ ~3 {NoAI:1b,Rotation:[160f,0f]}");
			srv.runCommand("execute as @p at @s run summon extremo_jefes:lepisma_sulfurico ~1 ~ ~3 {NoAI:1b,Rotation:[200f,0f]}");
			srv.runCommand("execute as @p at @s run summon extremo_jefes:bola_azufre ~ ~1.5 ~4 {Motion:[0d,0d,0d],acceleration_power:0d}");
			ctx.waitTicks(30);
			ctx.takeScreenshot("05_lepismas_y_bola");
			srv.runCommand("kill @e[type=extremo_jefes:lepisma_sulfurico]");
			srv.runCommand("kill @e[type=extremo_jefes:bola_azufre]");

			// 4) Combate real en supervivencia: el jefe tiene que atacar.
			srv.runCommand("gamemode survival @a");
			srv.runCommand("effect give @a minecraft:resistance infinite 3 true");
			srv.runCommand("effect give @a minecraft:regeneration infinite 2 true");
			srv.runCommand("execute as @p at @s run summon extremo_jefes:murcielago_mutante ~ ~4 ~10");
			ctx.waitTicks(200);
			ctx.takeScreenshot("06_combate");
			int[] datos = srv.computeOnServer(server -> {
				ServerLevel level = server.overworld();
				MurcielagoMutante jefe = level.getEntities(ModEntities.MURCIELAGO_MUTANTE, e -> true).stream().findFirst().orElseThrow();
				int lepismas = level.getEntities(ModEntities.LEPISMA_SULFURICO, LepismaSulfurico::isAlive).size();
				return new int[] {jefe.getTotalMordiscos(), jefe.getTotalBolas(), jefe.getTotalLepismas(), lepismas, jefe.getJugadoresConBarra().size()};
			});
			System.out.printf("[PRUEBA] combate: mordiscos=%d bolas=%d lepismasEngendrados=%d lepismasVivos=%d jugadoresConBarra=%d%n",
				datos[0], datos[1], datos[2], datos[3], datos[4]);
			if (datos[4] < 1) {
				throw new AssertionError("El jugador debería ver la barra del jefe");
			}
			if (datos[0] + datos[1] + datos[2] < 1) {
				throw new AssertionError("El jefe no ha atacado en 10 segundos");
			}
			srv.runCommand("kill @e[type=extremo_jefes:murcielago_mutante]");
			ctx.waitTicks(10);
			ctx.takeScreenshot("07_tras_matarlo");
		}

		// 5) Servidor dedicado: el mod tiene que funcionar igual conectándose a un servidor.
		try (TestDedicatedServerContext servidor = ctx.worldBuilder().createServer()) {
			try (var conexion = servidor.connect()) {
				TestServerConnection con = conexion;
				con.waitForChunksRender();
				servidor.runCommand("time set noon");
				servidor.runCommand("gamemode creative @a");
				servidor.runCommand("execute as @a at @s run tp @s ~ ~ ~ 0 0");
				servidor.runCommand("execute as @a at @s run summon extremo_jefes:murcielago_mutante ~ ~2 ~9 {NoAI:1b,Rotation:[180f,0f]}");
				con.waitForClientboundEntityUpdates(ModEntities.MURCIELAGO_MUTANTE);
				ctx.waitTicks(40);
				ctx.takeScreenshot("08_servidor_dedicado");
				boolean visto = ctx.computeOnClient(mc -> mc.level != null
					&& mc.level.getEntitiesOfClass(MurcielagoMutante.class, mc.player.getBoundingBox().inflate(32.0)).size() == 1);
				if (!visto) {
					throw new AssertionError("El cliente debería ver al Murciélago Mutante en el servidor dedicado");
				}
			}
		}
	}
}
