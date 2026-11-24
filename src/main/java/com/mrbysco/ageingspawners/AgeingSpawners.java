package com.mrbysco.ageingspawners;

import com.mrbysco.ageingspawners.config.SpawnerConfig;
import com.mrbysco.ageingspawners.handler.AgeHandler;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.level.GameRules;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgeingSpawners implements ModInitializer {
	public static final String MOD_ID = "ageingspawners";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final GameRules.Key<GameRules.BooleanValue> AGE_SPAWNERS_RULE =
			GameRules.register("ageingSpawners", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));


	@Override
	public void onInitialize() {
		NeoForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.COMMON, SpawnerConfig.commonSpec);
		SpawnerConfig.loadAllConfigEvents();
		AgeHandler.registerAllEvents();
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
