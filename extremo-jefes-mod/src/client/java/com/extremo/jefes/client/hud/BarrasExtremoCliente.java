package com.extremo.jefes.client.hud;

import com.extremo.jefes.BarraExtremo;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.world.BossEvent;

/** Guarda en el cliente las barras de jefe de Extremo (las que Minecraft ya no dibuja). */
public final class BarrasExtremoCliente {
	private static final Map<UUID, LerpingBossEvent> BARRAS = new LinkedHashMap<>();

	private BarrasExtremoCliente() {
	}

	public static Collection<LerpingBossEvent> barras() {
		return Collections.unmodifiableCollection(BARRAS.values());
	}

	public static void limpiar() {
		BARRAS.clear();
	}

	/**
	 * Procesa el paquete si pertenece a una barra de Extremo.
	 *
	 * @return true si era nuestra (y Minecraft no debe tocarla)
	 */
	public static boolean manejar(final ClientboundBossEventPacket packet) {
		final boolean[] nuestra = {false};
		packet.dispatch(new ClientboundBossEventPacket.Handler() {
			@Override
			public void add(
				final UUID id, final Component name, final float progress, final BossEvent.BossBarColor color, final BossEvent.BossBarOverlay overlay,
				final boolean darkenScreen, final boolean playMusic, final boolean createWorldFog
			) {
				if (BarraExtremo.esExtremo(id)) {
					nuestra[0] = true;
					BARRAS.put(id, new LerpingBossEvent(id, name, progress, color, overlay, false, false, false));
				}
			}

			@Override
			public void remove(final UUID id) {
				if (BarraExtremo.esExtremo(id)) {
					nuestra[0] = true;
					BARRAS.remove(id);
				}
			}

			@Override
			public void updateProgress(final UUID id, final float progress) {
				if (BarraExtremo.esExtremo(id)) {
					nuestra[0] = true;
					LerpingBossEvent barra = BARRAS.get(id);
					if (barra != null) {
						barra.setProgress(progress);
					}
				}
			}

			@Override
			public void updateName(final UUID id, final Component name) {
				if (BarraExtremo.esExtremo(id)) {
					nuestra[0] = true;
					LerpingBossEvent barra = BARRAS.get(id);
					if (barra != null) {
						barra.setName(name);
					}
				}
			}

			@Override
			public void updateStyle(final UUID id, final BossEvent.BossBarColor color, final BossEvent.BossBarOverlay overlay) {
				if (BarraExtremo.esExtremo(id)) {
					nuestra[0] = true;
					LerpingBossEvent barra = BARRAS.get(id);
					if (barra != null) {
						barra.setColor(color);
						barra.setOverlay(overlay);
					}
				}
			}

			@Override
			public void updateProperties(final UUID id, final boolean darkenScreen, final boolean playMusic, final boolean createWorldFog) {
				if (BarraExtremo.esExtremo(id)) {
					nuestra[0] = true;
				}
			}
		});
		return nuestra[0];
	}
}
