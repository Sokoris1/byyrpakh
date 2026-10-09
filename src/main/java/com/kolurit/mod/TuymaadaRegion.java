package com.kolurit.mod;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

// Регион TerraBlender: копия ванильного обычного мира, где равнины и умеренные леса
// на той же плоской земле заменены Долиной Туймаада. Так долина выходит сплошной и широкой,
// а берёзовые рощи внутри неё заменяют собой леса.
public class TuymaadaRegion extends Region {
    // Вес региона: ванильный регион по умолчанию весит 10
    public static final int WEIGHT = 4;

    public TuymaadaRegion() {
        super(Identifier.fromNamespaceAndPath(edition1.MODID, "overworld"), RegionType.OVERWORLD, WEIGHT);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        addModifiedVanillaOverworldBiomes(mapper, builder -> {
            builder.replaceBiome(Biomes.PLAINS, ModBiomes.TUYMAADA_VALLEY);
            builder.replaceBiome(Biomes.SUNFLOWER_PLAINS, ModBiomes.TUYMAADA_VALLEY);
            builder.replaceBiome(Biomes.FOREST, ModBiomes.TUYMAADA_VALLEY);
            builder.replaceBiome(Biomes.FLOWER_FOREST, ModBiomes.TUYMAADA_VALLEY);
            builder.replaceBiome(Biomes.BIRCH_FOREST, ModBiomes.TUYMAADA_VALLEY);
            builder.replaceBiome(Biomes.OLD_GROWTH_BIRCH_FOREST, ModBiomes.TUYMAADA_VALLEY);
        });
    }
}
