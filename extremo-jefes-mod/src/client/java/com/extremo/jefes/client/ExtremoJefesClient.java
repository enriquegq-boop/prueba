package com.extremo.jefes.client;

import com.extremo.jefes.client.hud.BarraExtremoHud;
import com.extremo.jefes.client.model.MurcielagoMutanteModel;
import com.extremo.jefes.client.render.MurcielagoMutanteRenderer;
import com.extremo.jefes.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class ExtremoJefesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(MurcielagoMutanteModel.LAYER, MurcielagoMutanteModel::createBodyLayer);
		EntityRendererRegistry.register(ModEntities.MURCIELAGO_MUTANTE, MurcielagoMutanteRenderer::new);
		EntityRendererRegistry.register(ModEntities.BOLA_AZUFRE, context -> new ThrownItemRenderer<>(context, 2.0F, true));
		// Barra de vida estilo Extremo (arriba a la izquierda).
		HudElementRegistry.attachElementAfter(VanillaHudElements.BOSS_BAR, BarraExtremoHud.ID, new BarraExtremoHud());
	}
}
