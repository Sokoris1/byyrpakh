package com.kolurit.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

// Генерация незажжённого круга сэргэ в Долине Туймаада (частота — в placed_feature/serge_circle.json)
public class SergeCircleFeature extends Feature<NoneFeatureConfiguration> {
    public SergeCircleFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        if (!SergeCircle.isGoodSite(level, origin.getX(), origin.getZ())) {
            return false;   // вода или слишком крутой склон
        }
        SergeCircle.build(level, origin, false, false);
        return true;
    }
}
