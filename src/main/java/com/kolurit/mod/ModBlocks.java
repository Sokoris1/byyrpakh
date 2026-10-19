package com.kolurit.mod;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(edition1.MODID);

    public static final DeferredBlock<Block> BYIRPAH_BOTTLE = BLOCKS.registerBlock("byirpah_bottle",
            props -> new ByirpahBottleBlock(bottleProps(props), ByirpahStage.FRESH));

    public static final DeferredBlock<Block> BYIRPAH_AGED_BOTTLE = BLOCKS.registerBlock("byirpah_aged_bottle",
            props -> new ByirpahBottleBlock(bottleProps(props), ByirpahStage.AGED));

    public static final DeferredBlock<Block> BYIRPAH_STRONG_BOTTLE = BLOCKS.registerBlock("byirpah_strong_bottle",
            props -> new ByirpahBottleBlock(bottleProps(props), ByirpahStage.STRONG));

    // Сэргэ: в выживании не ломается и ничего не роняет; зажжённое светится
    public static final DeferredBlock<Block> SERGE = BLOCKS.registerBlock("serge",
            props -> new SergeBlock(props.mapColor(MapColor.WOOD).strength(-1.0F, 3600000.0F).noLootTable()
                    .sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.BLOCK)
                    .lightLevel(state -> state.getValue(SergeBlock.LIT) ? 10 : 0)));

    // Портал в Туймааду: заполняет бассейн круга, сквозь него проходят, не ломается, ярко светится
    public static final DeferredBlock<Block> TUYMAADA_PORTAL = BLOCKS.registerBlock("tuymaada_portal",
            props -> new TuymaadaPortalBlock(props.mapColor(MapColor.GOLD).noCollision().noOcclusion().strength(-1.0F, 3600000.0F)
                    .noLootTable().sound(SoundType.GLASS).pushReaction(PushReaction.BLOCK)
                    .lightLevel(state -> 12)));

    // Сардаана — лилия даурская, национальный цветок Якутии. В рагу даёт огнестойкость
    public static final DeferredBlock<Block> SARDAANA = BLOCKS.registerBlock("sardaana",
            props -> new FlowerBlock(MobEffects.FIRE_RESISTANCE, 4.0F, props.mapColor(MapColor.COLOR_RED)
                    .noCollision().instabreak().sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

    // Сардаана в горшке: конструктор сам связывает цветок с горшком
    public static final DeferredBlock<Block> POTTED_SARDAANA = BLOCKS.registerBlock("potted_sardaana",
            props -> new FlowerPotBlock(SARDAANA.get(), props.instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

    // Золотая хвоя лиственницы: опадает без ствола, роняет золотые хвоинки
    public static final DeferredBlock<Block> GOLDEN_LARCH_LEAVES = BLOCKS.registerBlock("golden_larch_leaves",
            props -> new UntintedParticleLeavesBlock(0.02F,
                    ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, 0xFFE8B030),
                    props.mapColor(MapColor.GOLD).strength(0.2F).randomTicks().sound(SoundType.GRASS)
                            .noOcclusion().isSuffocating((state, level, pos) -> false)
                            .isViewBlocking((state, level, pos) -> false)
                            .isRedstoneConductor((state, level, pos) -> false)
                            .ignitedByLava().pushReaction(PushReaction.DESTROY)));

    // Саженец лиственницы растит дерево data/edition1/worldgen/configured_feature/golden_larch.json
    public static final ResourceKey<ConfiguredFeature<?, ?>> GOLDEN_LARCH_TREE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(edition1.MODID, "golden_larch"));
    public static final TreeGrower LARCH_GROWER = new TreeGrower(edition1.MODID + "_larch",
            Optional.empty(), Optional.of(GOLDEN_LARCH_TREE), Optional.empty());

    public static final DeferredBlock<Block> LARCH_SAPLING = BLOCKS.registerBlock("larch_sapling",
            props -> new SaplingBlock(LARCH_GROWER, props.mapColor(MapColor.PLANT).noCollision().randomTicks()
                    .instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)) {});

    public static final DeferredBlock<Block> POTTED_LARCH_SAPLING = BLOCKS.registerBlock("potted_larch_sapling",
            props -> new FlowerPotBlock(LARCH_SAPLING.get(), props.instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

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