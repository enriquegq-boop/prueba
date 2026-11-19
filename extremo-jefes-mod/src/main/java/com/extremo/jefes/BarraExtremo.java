package com.extremo.jefes;

import java.util.UUID;
import net.minecraft.util.RandomSource;

/**
 * Las barras de vida de los jefes de Extremo viajan por el sistema normal de Minecraft
 * (así se sincronizan solas: vida, nombre, quién la ve), pero con un identificador especial.
 * El cliente reconoce ese identificador y, en vez de la barra vanilla del centro, dibuja la
 * barra estilo Extremo arriba a la izquierda.
 */
public final class BarraExtremo {
	/** "EXTREMO1" en ASCII: los 64 bits altos de todas las barras de Extremo. */
	public static final long MARCA = 0x45585452454D4F31L;

	private BarraExtremo() {
	}

	public static UUID nuevoId(final RandomSource random) {
		return new UUID(MARCA, random.nextLong());
	}

	public static boolean esExtremo(final UUID id) {
		return id.getMostSignificantBits() == MARCA;
	}
}
