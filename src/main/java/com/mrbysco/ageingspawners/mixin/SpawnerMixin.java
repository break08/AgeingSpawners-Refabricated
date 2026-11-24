package com.mrbysco.ageingspawners.mixin;

import com.mrbysco.ageingspawners.accessor.SpawnerBEAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpawnerBlockEntity.class)
public abstract class SpawnerMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void ageingspawners$onInit(BlockPos pos, BlockState state, CallbackInfo ci) {
        SpawnerBlockEntity self = (SpawnerBlockEntity) (Object) this;
        ((SpawnerBEAccess) self.getSpawner()).setSpawnerBlockEntity(self);
    }
}
