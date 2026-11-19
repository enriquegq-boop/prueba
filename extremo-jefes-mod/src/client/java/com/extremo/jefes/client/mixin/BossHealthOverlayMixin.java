package com.extremo.jefes.client.mixin;

import com.extremo.jefes.client.hud.BarrasExtremoCliente;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Aparta las barras de los jefes de Extremo para que no salgan con el estilo vanilla del centro. */
@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayMixin {
	@Inject(method = "update", at = @At("HEAD"), cancellable = true)
	private void extremoJefes$apartarBarrasExtremo(final ClientboundBossEventPacket packet, final CallbackInfo ci) {
		if (BarrasExtremoCliente.manejar(packet)) {
			ci.cancel();
		}
	}

	@Inject(method = "reset", at = @At("HEAD"))
	private void extremoJefes$limpiar(final CallbackInfo ci) {
		BarrasExtremoCliente.limpiar();
	}
}
