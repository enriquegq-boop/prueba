package com.extremo.jefes.pruebas;

import com.extremo.jefes.entity.BolaAzufre;
import com.extremo.jefes.entity.LepismaSulfurico;
import com.extremo.jefes.entity.MurcielagoMutante;
import com.extremo.jefes.registry.ModEntities;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.Vec3;

/** Pruebas automáticas del Murciélago Mutante (se ejecutan en un servidor de pruebas). */
public class PruebasMurcielago {
	private static final String ARENA = "extremo_jefes_pruebas:arena";

	private static Villager dianaResistente(final GameTestHelper h, final Vec3 pos) {
		Villager v = h.spawn(EntityTypes.VILLAGER, pos);
		v.setNoAi(true);
		v.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
		v.setHealth(1000.0F);
		return v;
	}

	@GameTest(structure = ARENA)
	public void jefeApareceConSuBarraYVida(final GameTestHelper h) {
		MurcielagoMutante jefe = h.spawn(ModEntities.MURCIELAGO_MUTANTE, new Vec3(8, 4, 8));
		h.assertTrue(jefe.getMaxHealth() == 300.0F, "La vida máxima debería ser 300 y es " + jefe.getMaxHealth());
		h.assertTrue(jefe.getHealth() == 300.0F, "Debería aparecer con la vida llena");
		h.assertTrue(jefe.isNoGravity(), "Debería volar (sin gravedad)");
		h.assertTrue(!jefe.isEnfurecido(), "No debería empezar enfurecido");
		h.assertTrue(jefe.getBossEvent().getColor() == BossEvent.BossBarColor.YELLOW, "La barra debería ser amarilla");
		h.assertTrue(
			jefe.getBossEvent().getName().getContents() instanceof TranslatableContents t && t.getKey().equals("entity.extremo_jefes.murcielago_mutante"),
			"La barra debería llamarse Murciélago Mutante, se llama " + jefe.getBossEvent().getName()
		);
		h.succeed();
	}

	@GameTest(structure = ARENA, maxTicks = 60)
	public void laBarraSigueLaVida(final GameTestHelper h) {
		MurcielagoMutante jefe = h.spawn(ModEntities.MURCIELAGO_MUTANTE, new Vec3(8, 4, 8));
		jefe.setNoAi(false);
		jefe.hurtServer(h.getLevel(), h.getLevel().damageSources().generic(), 60.0F);
		h.runAfterDelay(5, () -> {
			float esperado = jefe.getHealth() / jefe.getMaxHealth();
			h.assertTrue(jefe.getHealth() < 300.0F, "Debería haber recibido daño");
			h.assertTrue(
				Math.abs(jefe.getBossEvent().getProgress() - esperado) < 0.01F,
				"La barra marca " + jefe.getBossEvent().getProgress() + " y debería marcar " + esperado
			);
			h.succeed();
		});
	}

	@GameTest(structure = ARENA, maxTicks = 60)
	public void seEnfureceAlCincuentaPorCiento(final GameTestHelper h) {
		MurcielagoMutante jefe = h.spawn(ModEntities.MURCIELAGO_MUTANTE, new Vec3(8, 4, 8));
		jefe.setHealth(140.0F);
		h.succeedWhen(() -> {
			h.assertTrue(jefe.isEnfurecido(), "Por debajo del 50% debería enfurecerse");
			h.assertTrue(jefe.getBossEvent().getColor() == BossEvent.BossBarColor.RED, "Enfurecido, la barra debería ponerse roja");
		});
	}

	@GameTest(structure = ARENA, maxTicks = 600)
	public void muerdeDisparaBolasYEngendraLepismas(final GameTestHelper h) {
		Villager diana = dianaResistente(h, new Vec3(8.5, 1, 8.5));
		MurcielagoMutante jefe = h.spawn(ModEntities.MURCIELAGO_MUTANTE, new Vec3(3, 6, 3));
		jefe.setTarget(diana);
		h.succeedWhen(() -> {
			if (jefe.getTarget() == null) {
				jefe.setTarget(diana);
			}
			h.assertTrue(jefe.getTotalMordiscos() >= 1, "Todavía no ha mordido (" + jefe.getTotalMordiscos() + ")");
			h.assertTrue(jefe.getTotalBolas() >= 1, "Todavía no ha disparado bolas de azufre (" + jefe.getTotalBolas() + ")");
			h.assertTrue(jefe.getTotalLepismas() >= 1, "Todavía no ha engendrado lepismas (" + jefe.getTotalLepismas() + ")");
			h.assertTrue(diana.getHealth() < 1000.0F, "La diana debería haber recibido daño");
		});
	}

	@GameTest(structure = ARENA, maxTicks = 200)
	public void elLepismaHundeUnBloque(final GameTestHelper h) {
		Villager diana = dianaResistente(h, new Vec3(8.5, 1, 8.5));
		double alturaInicial = diana.getY();
		LepismaSulfurico lepisma = h.spawn(ModEntities.LEPISMA_SULFURICO, new Vec3(6.5, 1, 8.5));
		lepisma.setTarget(diana);
		h.succeedWhen(() -> {
			if (lepisma.getTarget() == null) {
				lepisma.setTarget(diana);
			}
			h.assertTrue(
				diana.getY() <= alturaInicial - 0.99,
				"El lepisma debería haber hundido a la diana un bloque (y=" + diana.getY() + ", inicial=" + alturaInicial
					+ ", vidaDiana=" + diana.getHealth() + ", lepisma=" + lepisma.position() + " vivo=" + lepisma.isAlive()
					+ " objetivo=" + lepisma.getTarget() + " diana=" + diana.position() + ")"
			);
		});
	}

	@GameTest(structure = ARENA, maxTicks = 20)
	public void elLepismaNuncaEntierraLaCabeza(final GameTestHelper h) {
		Villager diana = dianaResistente(h, new Vec3(4.5, 1, 4.5));
		double alturaInicial = diana.getY();
		boolean primero = LepismaSulfurico.hundirUnBloque(h.getLevel(), diana);
		boolean segundo = LepismaSulfurico.hundirUnBloque(h.getLevel(), diana);
		h.assertTrue(primero, "El primer golpe debería hundir un bloque");
		h.assertTrue(!segundo, "El segundo golpe no debería hundir más (enterraría la cabeza)");
		h.assertTrue(Math.abs(diana.getY() - (alturaInicial - 1.0)) < 0.01, "Debería haber bajado exactamente un bloque");
		h.assertTrue(!diana.isInWall(), "La cabeza no debería quedar dentro de un bloque");
		h.succeed();
	}

	@GameTest(structure = ARENA, maxTicks = 100)
	public void laBolaDeAzufreHaceDanoYEnvenena(final GameTestHelper h) {
		Villager diana = h.spawn(EntityTypes.VILLAGER, new Vec3(8.5, 1, 8.5));
		diana.setNoAi(true);
		MurcielagoMutante jefe = h.spawn(ModEntities.MURCIELAGO_MUTANTE, new Vec3(2, 7, 2));
		jefe.setNoAi(true);
		Vec3 origen = h.absoluteVec(new Vec3(8.5, 2, 3.5));
		Vec3 dir = diana.getEyePosition().subtract(origen).normalize();
		BolaAzufre bola = new BolaAzufre(h.getLevel(), jefe, dir, 7.0F);
		bola.setPos(origen);
		h.getLevel().addFreshEntity(bola);
		h.succeedWhen(() -> {
			h.assertTrue(diana.getHealth() < diana.getMaxHealth(), "La bola debería hacer daño");
			h.assertTrue(diana.hasEffect(MobEffects.POISON), "La bola debería envenenar");
		});
	}

	@GameTest(structure = ARENA, maxTicks = 60)
	public void alMorirMuerenSusLepismas(final GameTestHelper h) {
		MurcielagoMutante jefe = h.spawn(ModEntities.MURCIELAGO_MUTANTE, new Vec3(8, 5, 8));
		List<LepismaSulfurico> lepismas = h.spawn(ModEntities.LEPISMA_SULFURICO, new Vec3(3, 1, 3), 3);
		lepismas.forEach(l -> l.setJefe(jefe.getUUID()));
		LepismaSulfurico ajeno = h.spawn(ModEntities.LEPISMA_SULFURICO, new Vec3(12, 1, 12));
		ajeno.setNoAi(true);
		h.runAfterDelay(2, () -> jefe.kill(h.getLevel()));
		h.succeedWhen(() -> {
			h.assertTrue(!jefe.isAlive(), "El jefe debería estar muerto");
			for (LepismaSulfurico l : lepismas) {
				h.assertTrue(!l.isAlive(), "Todos sus lepismas deberían morir con el jefe");
			}
			h.assertTrue(ajeno.isAlive(), "Un lepisma que no es suyo no debería morir");
		});
	}
}
