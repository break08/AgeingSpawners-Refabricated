package com.mrbysco.ageingspawners.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Either;
import com.mrbysco.ageingspawners.AgeingSpawners;
import com.mrbysco.ageingspawners.accessor.SpawnerBEAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static com.mrbysco.ageingspawners.handler.AgeHandler.handleSpawner;
import static com.mrbysco.ageingspawners.handler.AgeHandler.toEither;

@Mixin(BaseSpawner.class)
public class BaseSpawnerMixin implements SpawnerBEAccess {
    @ModifyArg(method = "serverTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;tryAddFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)Z"),
            index = 0)
    public Entity ageingServerTick(Entity entity) {
        Either<BlockEntity, Entity> either = toEither((BaseSpawner) (Object) this);
        if (!(entity instanceof Mob)) {
            handleSpawner(entity.level(), either, entity);
        }
        return entity;
    }



    @Unique
    private BlockEntity spawnerBlockEntity;

    @Override public BlockEntity getSpawnerBlockEntity() { return spawnerBlockEntity; }
    @Override public void setSpawnerBlockEntity(BlockEntity be) { this.spawnerBlockEntity = be; }

    @WrapOperation(
            method = "serverTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;"
            )
    )
    private SpawnGroupData onSpawn(Mob mob, ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, SpawnGroupData spawnGroupData, Operation<SpawnGroupData> original) {
        Either<BlockEntity, Entity> either = toEither((BaseSpawner) (Object) this);

        ServerLevel serverLevel = serverLevelAccessor.getLevel();
        if (serverLevel.getGameRules().get(AgeingSpawners.AGE_SPAWNERS_RULE)) {
            handleSpawner(serverLevel, either, mob);
        }
        return original.call(mob, serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }
}