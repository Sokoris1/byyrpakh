package com.kolurit.mod;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, edition1.MODID);

    // Один тип на все три стадии бутылок
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ByirpahBottleBlockEntity>> BYIRPAH_BOTTLES =
            BLOCK_ENTITIES.register("byirpah_bottles",
                    () -> new BlockEntityType<>(ByirpahBottleBlockEntity::new, Set.of(
                            ModBlocks.BYIRPAH_BOTTLE.get(),
                            ModBlocks.BYIRPAH_AGED_BOTTLE.get(),
                            ModBlocks.BYIRPAH_STRONG_BOTTLE.get())));
}
