package com.extremo.jefes.registry;

import com.extremo.jefes.ExtremoJefes;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public final class ModItems {
	public static final Item MURCIELAGO_MUTANTE_SPAWN_EGG = register(
		"murcielago_mutante_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.MURCIELAGO_MUTANTE)
	);
	public static final Item LEPISMA_SULFURICO_SPAWN_EGG = register(
		"lepisma_sulfurico_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.LEPISMA_SULFURICO)
	);
	/** Se usa como "dibujo" de la bola de azufre que dispara el jefe. */
	public static final Item BOLA_AZUFRE = register("bola_azufre", Item::new, new Item.Properties());

	private ModItems() {
	}

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ExtremoJefes.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
	}

	public static void init() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(output -> {
			output.accept(MURCIELAGO_MUTANTE_SPAWN_EGG);
			output.accept(LEPISMA_SULFURICO_SPAWN_EGG);
		});
	}
}
