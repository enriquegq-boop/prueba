package com.extremo.jefes.pruebas;

import com.extremo.jefes.BarraExtremo;
import com.extremo.jefes.client.hud.BarrasExtremoCliente;
import com.extremo.jefes.entity.MurcielagoMutante;
import com.extremo.jefes.registry.ModEntities;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.server.level.ServerLevel;

/**
 * Prueba visual: arranca el juego de verdad, invoca al Murciélago Mutante y saca capturas.
 * Comprueba que la barra sale con el estilo Extremo (arriba a la izquierda) y no la vanilla,
 * y que funciona igual en un servidor dedicado.
 */
public class PruebaVisualMurcielago implements FabricClientGameTest {
	private static void comprobarBarraExtremo(final ClientGameTestContext ctx, final int esperadas) {
		int barras = ctx.computeOnClient(mc -> BarrasExtremoCliente.barras().size());
		if (barras != esperadas) {
			throw new AssertionError("Debería haber " + esperadas + " barra(s) Extremo en pantalla y hay " + barras);
		}
		boolean todasExtremo = ctx.computeOnClient(mc -> BarrasExtremoCliente.barras().stream().map(LerpingBossEvent::getId).allMatch(BarraExtremo::esExtremo));
		if (!todasExtremo) {
			throw new AssertionError("Hay una barra que no es de Extremo en la lista de Extremo");
		}
	}

	private static void preparar(final TestServerContext srv) {
		srv.runCommand("gamerule advance_time false");
		srv.runCommand("time set noon");
		srv.runCommand("weather clear");
		srv.runCommand("gamemode creative @a");
		srv.runCommand("execute as @a at @s run tp @s ~ ~ ~ 0 0");
		srv.runCommand("execute as @a at @s run fill ~-12 ~-1 ~-2 ~12 ~-1 ~22 minecraft:stone");
		srv.runCommand("execute as @a at @s run fill ~-12 ~ ~-2 ~12 ~14 ~22 minecraft:air");
	}

	@Override
	public void runTest(final ClientGameTestContext ctx) {
		try (TestSingleplayerContext mundo = ctx.worldBuilder().create()) {
			mundo.getConnection().waitForChunksRender();
			TestServerContext srv = mundo.getServer();
			preparar(srv);
			ctx.waitTicks(10);

			// 1) Barra Extremo con vida llena.
			srv.runCommand("execute as @p at @s run summon extremo_jefes:murcielago_mutante ~ ~1 ~9 {NoAI:1b,Rotation:[180f,0f]}");
			ctx.waitTicks(40);
			comprobarBarraExtremo(ctx, 1);
			ctx.takeScreenshot("01_barra_vida_llena");

			// 2) Con daño: la barra baja.
			srv.runCommand("execute as @e[type=extremo_jefes:murcielago_mutante] run data merge entity @s {Health:195f}");
			ctx.waitTicks(30);
			float progreso = ctx.computeOnClient(mc -> BarrasExtremoCliente.barras().iterator().next().getProgress());
			if (Math.abs(progreso - 0.65F) > 0.02F) {
				throw new AssertionError("La barra debería marcar 65% y marca " + progreso);
			}
			ctx.takeScreenshot("02_barra_con_dano");

			// 3) Enfurecido: barra en modo furia.
			srv.runCommand("execute as @e[type=extremo_jefes:murcielago_mutante] run data merge entity @s {Health:110f,Enfurecido:1b}");
			srv.runCommand("time set midnight");
			ctx.waitTicks(40);
			ctx.takeScreenshot("03_barra_furia_noche");

			// 4) Otro jefe vanilla (Wither) sigue usando su barra normal en el centro.
			srv.runCommand("time set noon");
			srv.runCommand("execute as @p at @s run summon minecraft:wither ~6 ~2 ~14 {NoAI:1b}");
			ctx.waitTicks(40);
			comprobarBarraExtremo(ctx, 1);
			ctx.takeScreenshot("04_con_wither_vanilla");
			srv.runCommand("kill @e[type=minecraft:wither]");
			srv.runCommand("kill @e[type=extremo_jefes:murcielago_mutante]");
			ctx.waitTicks(20);
			comprobarBarraExtremo(ctx, 0);

			// 5) Bola de azufre.
			srv.runCommand("execute as @p at @s run summon extremo_jefes:bola_azufre ~ ~1.5 ~4 {Motion:[0d,0d,0d],acceleration_power:0d}");
			ctx.waitTicks(20);
			ctx.takeScreenshot("05_bola_azufre");
			srv.runCommand("kill @e[type=extremo_jefes:bola_azufre]");

			// 6) Combate real en supervivencia: el jefe tiene que atacar.
			srv.runCommand("gamemode survival @a");
			srv.runCommand("effect give @a minecraft:resistance infinite 3 true");
			srv.runCommand("effect give @a minecraft:regeneration infinite 2 true");
			srv.runCommand("execute as @p at @s run summon extremo_jefes:murcielago_mutante ~ ~4 ~10");
			ctx.waitTicks(200);
			ctx.takeScreenshot("06_combate");
			int[] datos = srv.computeOnServer(server -> {
				ServerLevel level = server.overworld();
				MurcielagoMutante jefe = level.getEntities(ModEntities.MURCIELAGO_MUTANTE, e -> true).stream().findFirst().orElseThrow();
				return new int[] {jefe.getTotalMordiscos(), jefe.getTotalBolas(), jefe.getJugadoresConBarra().size()};
			});
			System.out.printf("[PRUEBA] combate: mordiscos=%d bolas=%d jugadoresConBarra=%d%n", datos[0], datos[1], datos[2]);
			if (datos[2] < 1) {
				throw new AssertionError("El jugador debería ver la barra del jefe");
			}
			if (datos[0] + datos[1] < 1) {
				throw new AssertionError("El jefe no ha atacado en 10 segundos");
			}
			srv.runCommand("kill @e[type=extremo_jefes:murcielago_mutante]");
			ctx.waitTicks(20);
			comprobarBarraExtremo(ctx, 0);
		}

		// 7) Servidor dedicado: el mod tiene que funcionar igual conectándose a un servidor.
		try (TestDedicatedServerContext servidor = ctx.worldBuilder().createServer()) {
			try (var conexion = servidor.connect()) {
				TestServerConnection con = conexion;
				con.waitForChunksRender();
				preparar(servidor);
				servidor.runCommand("execute as @a at @s run summon extremo_jefes:murcielago_mutante ~ ~2 ~9 {NoAI:1b,Rotation:[180f,0f],Health:240f}");
				con.waitForClientboundEntityUpdates(ModEntities.MURCIELAGO_MUTANTE);
				ctx.waitTicks(40);
				comprobarBarraExtremo(ctx, 1);
				ctx.takeScreenshot("07_servidor_dedicado");
				boolean visto = ctx.computeOnClient(mc -> mc.level != null
					&& mc.level.getEntitiesOfClass(MurcielagoMutante.class, mc.player.getBoundingBox().inflate(32.0)).size() == 1);
				if (!visto) {
					throw new AssertionError("El cliente debería ver al Murciélago Mutante en el servidor dedicado");
				}
			}
		}
		comprobarBarraExtremo(ctx, 0);
	}
}
