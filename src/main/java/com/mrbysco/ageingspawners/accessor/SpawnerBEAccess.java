package com.mrbysco.ageingspawners.accessor;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface SpawnerBEAccess {
    BlockEntity getSpawnerBlockEntity();
    void setSpawnerBlockEntity(BlockEntity be);
}
