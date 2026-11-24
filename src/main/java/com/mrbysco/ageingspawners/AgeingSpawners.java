package com.mrbysco.ageingspawners;

import com.mrbysco.ageingspawners.config.SpawnerConfig;
import com.mrbysco.ageingspawners.handler.AgeHandler;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;

import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgeingSpawners implements ModInitializer {
	public static final String MOD_ID = "ageingspawners";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static GameRule<Boolean> AGE_SPAWNERS_RULE;

	/*
	public static final GameRules.Key<GameRules.BooleanValue> AGE_SPAWNERS_RULE =
			GameRules.register("ageingSpawners", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));
	 */

	public static GameRule<Boolean> initRule() {
		return GameRuleBuilder
				.forBoolean(true)
				.category(GameRuleCategory.UPDATES)
				.buildAndRegister(Identifier.fromNamespaceAndPath(MOD_ID, "ageing_spawners"));
	}


	@Override
	public void onInitialize() {
		AGE_SPAWNERS_RULE = initRule();
		ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.COMMON, SpawnerConfig.commonSpec);
		SpawnerConfig.loadAllConfigEvents();
		AgeHandler.registerAllEvents();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
