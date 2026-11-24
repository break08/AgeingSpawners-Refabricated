package com.mrbysco.ageingspawners.mixin;

import com.mrbysco.ageingspawners.util.AgeingWorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(BlockItem.class)
public class PlaceEvent {
    @Inject(method = "place", at = @At("HEAD"))
    private void place(BlockPlaceContext blockPlaceContext, CallbackInfoReturnable<InteractionResult> cir){
        BlockItem blockItem = (BlockItem)(Object) this;
        if (!blockPlaceContext.getLevel().isClientSide() && blockPlaceContext.getLevel() instanceof ServerLevelAccessor serverLevelAccessor &&
                blockItem.getBlock() == Blocks.SPAWNER && blockPlaceContext.getPlayer() != null) {
            BlockPos pos = blockPlaceContext.getClickedPos();
            ServerLevel serverLevel = serverLevelAccessor.getLevel();
            ResourceLocation dimensionLocation = serverLevel.dimension().location();
            AgeingWorldData worldData = AgeingWorldData.get(serverLevel);
            Map<BlockPos, AgeingWorldData.SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);
            AgeingWorldData.SpawnerInfo info = locationMap.getOrDefault(pos, new AgeingWorldData.SpawnerInfo(0, true));
            locationMap.put(pos, info);
            worldData.setMapForWorld(dimensionLocation, locationMap);
            worldData.setDirty();
        }
    }
}
