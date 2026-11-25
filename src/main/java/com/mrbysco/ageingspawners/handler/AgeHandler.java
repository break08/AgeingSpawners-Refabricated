package com.mrbysco.ageingspawners.handler;

import com.mojang.datafixers.util.Either;
import com.mrbysco.ageingspawners.accessor.SpawnerBEAccess;
import com.mrbysco.ageingspawners.config.SpawnerConfig;
import com.mrbysco.ageingspawners.util.AgeingHelper;
import com.mrbysco.ageingspawners.util.AgeingWorldData;
import com.mrbysco.ageingspawners.util.AgeingWorldData.SpawnerInfo;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

import java.util.Map;

public class AgeHandler {

    public static void registerAllEvents(){
        breakEvent();
    }
    public static void handleSpawner(Level level, Either<BlockEntity, Entity> spawner, Entity entity) {
        Identifier registryName = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        switch (SpawnerConfig.COMMON.spawnerMode.get()) {
            case BLACKLIST -> handleBlacklist(level, spawner, registryName);
            case WHITELIST -> handleWhitelist(level, spawner, registryName);
        }
    }

    public static void handleBlacklist(Level level, Either<BlockEntity, Entity> spawner, Identifier registryName) {
        if (!AgeingHelper.blacklistContains(registryName)) {
            ageTheSpawner(level, spawner, SpawnerConfig.COMMON.blacklistMaxSpawnCount.get());
        } else {
            BlockEntity be = ((SpawnerBEAccess) spawner).getSpawnerBlockEntity();
            if (be != null) {
                BlockPos pos = be.getBlockPos();
                Identifier dimensionLocation = level.dimension().identifier();
                AgeingWorldData worldData = AgeingWorldData.get(level);
                Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);
                locationMap.remove(pos);
                worldData.setMapForWorld(dimensionLocation, locationMap);
                worldData.setDirty();
            }
        }
    }

    public static void handleWhitelist(Level level, Either<BlockEntity, Entity> spawner, Identifier registryName) {
        if (AgeingHelper.whitelistContains(registryName)) {
            int maxSpawnCount = AgeingHelper.getMaxSpawnCount(registryName);
            ageTheSpawner(level, spawner, maxSpawnCount);
        } else {
            BlockEntity be = ((SpawnerBEAccess) spawner).getSpawnerBlockEntity();
            if (be != null) {
                BlockPos pos = be.getBlockPos();
                Identifier dimensionLocation = level.dimension().identifier();
                AgeingWorldData worldData = AgeingWorldData.get(level);
                Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);
                locationMap.remove(pos);
                worldData.setMapForWorld(dimensionLocation, locationMap);
                worldData.setDirty();
            }
        }
    }

    public static void ageTheSpawner(Level level, Either<BlockEntity, Entity> spawner, int maxCount) {
        if (spawner.left().isPresent()) {
            BlockPos pos = spawner.left().orElseThrow().getBlockPos();
            Identifier dimensionLocation = level.dimension().identifier();
            AgeingWorldData worldData = AgeingWorldData.get(level);
            Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);

            if (level.getBlockEntity(pos) != null && level.getBlockEntity(pos) instanceof SpawnerBlockEntity) {
                SpawnerInfo info = locationMap.getOrDefault(pos, new SpawnerInfo(0, false));
                boolean playerPlaced = info.playerPlaced();
                boolean flag = SpawnerConfig.COMMON.playerPlacedOnly.get();
                if (!flag || (flag && playerPlaced)) {
                    int spawnCount = info.spawnCount();
                    spawnCount++;
                    if (spawnCount >= maxCount) {
                        level.removeBlock(pos, false);
                        locationMap.remove(pos);
                    } else {
                        locationMap.put(pos, new SpawnerInfo(spawnCount, playerPlaced));
                    }
                    worldData.setMapForWorld(dimensionLocation, locationMap);
                    worldData.setDirty();
                }
            }
        }
    }

    public static void breakEvent() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, _pos, state, blockEntity) -> {
            if (!world.isClientSide() && world instanceof ServerLevelAccessor serverLevelAccessor) {
                ServerLevel serverLevel = serverLevelAccessor.getLevel();
                Identifier dimensionLocation = serverLevel.dimension().identifier();
                AgeingWorldData worldData = AgeingWorldData.get(serverLevel);
                Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);
                locationMap.remove(_pos);
                worldData.setMapForWorld(dimensionLocation, locationMap);
                worldData.setDirty();
            }
            return true;
        });
    }
    public static Either<BlockEntity, Entity> toEither(BaseSpawner spawner) {
        if (spawner instanceof SpawnerBEAccess be) {
            BlockEntity blockEntity = be.getSpawnerBlockEntity();
            if (blockEntity != null) {
                return Either.left(blockEntity);
            }
        }
        return null;
    }
}