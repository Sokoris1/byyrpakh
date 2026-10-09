package com.kolurit.mod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public class ModBiomes {
    // Сам биом описан в data/edition1/worldgen/biome/tuymaada_valley.json
    public static final ResourceKey<Biome> TUYMAADA_VALLEY = ResourceKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath(edition1.MODID, "tuymaada_valley"));
}
