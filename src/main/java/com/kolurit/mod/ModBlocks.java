package com.kolurit.mod;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(edition1.MODID);

    public static final DeferredBlock<Block> BYIRPAH_BOTTLE = BLOCKS.registerBlock("byirpah_bottle",
            props -> new ByirpahBottleBlock(bottleProps(props), ByirpahStage.FRESH));

    public static final DeferredBlock<Block> BYIRPAH_AGED_BOTTLE = BLOCKS.registerBlock("byirpah_aged_bottle",
            props -> new ByirpahBottleBlock(bottleProps(props), ByirpahStage.AGED));

    public static final DeferredBlock<Block> BYIRPAH_STRONG_BOTTLE = BLOCKS.registerBlock("byirpah_strong_bottle",
            props -> new ByirpahBottleBlock(bottleProps(props), ByirpahStage.STRONG));

    // Общие свойства блоков-бутылок; поршень разбивает их с дропом
    private static BlockBehaviour.Properties bottleProps(BlockBehaviour.Properties props) {
        return props.noCollision().noOcclusion().sound(SoundType.GLASS).pushReaction(PushReaction.DESTROY);
    }

    public static Block byirpahBottleFor(ByirpahStage stage) {
        return switch (stage) {
            case FRESH -> BYIRPAH_BOTTLE.get();
            case AGED -> BYIRPAH_AGED_BOTTLE.get();
            case STRONG -> BYIRPAH_STRONG_BOTTLE.get();
        };
    }
}