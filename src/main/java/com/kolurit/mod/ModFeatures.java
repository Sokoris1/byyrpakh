package com.kolurit.mod;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, edition1.MODID);

    public static final DeferredHolder<Feature<?>, SergeCircleFeature> SERGE_CIRCLE =
            FEATURES.register("serge_circle", SergeCircleFeature::new);
}
