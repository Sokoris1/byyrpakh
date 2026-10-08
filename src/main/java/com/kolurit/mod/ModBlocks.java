package com.kolurit.mod;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(edition1.MODID);

    public static final DeferredBlock<Block> BYIRPAH_BOTTLE = BLOCKS.registerBlock("byirpah_bottle",
            props -> new ByirpahBottleBlock(props.noCollision().noOcclusion().sound(SoundType.GLASS), ByirpahStage.FRESH));

    public static final DeferredBlock<Block> BYIRPAH_AGED_BOTTLE = BLOCKS.registerBlock("byirpah_aged_bottle",
            props -> new ByirpahBottleBlock(props.noCollision().noOcclusion().sound(SoundType.GLASS), ByirpahStage.AGED));

    public static final DeferredBlock<Block> BYIRPAH_STRONG_BOTTLE = BLOCKS.registerBlock("byirpah_strong_bottle",
            props -> new ByirpahBottleBlock(props.noCollision().noOcclusion().sound(SoundType.GLASS), ByirpahStage.STRONG));

    public static Block byirpahBottleFor(ByirpahStage stage) {
        return switch (stage) {
            case FRESH -> BYIRPAH_BOTTLE.get();
            case AGED -> BYIRPAH_AGED_BOTTLE.get();
            case STRONG -> BYIRPAH_STRONG_BOTTLE.get();
        };
    }
}