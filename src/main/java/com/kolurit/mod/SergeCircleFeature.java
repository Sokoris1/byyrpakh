package com.kolurit.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

// Генерация незажжённого круга сэргэ в Долине Туймаада (частота — в placed_feature/serge_circle.json)
public class SergeCircleFeature extends Feature<NoneFeatureConfiguration> {
    // Наибольший перепад высот под столбами, при котором круг ещё ставится
    private static final int MAX_SLOPE = 3;

    public SergeCircleFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int dx = -SergeCircle.CORNER; dx <= SergeCircle.CORNER; dx += SergeCircle.CORNER) {
            for (int dz = -SergeCircle.CORNER; dz <= SergeCircle.CORNER; dz += SergeCircle.CORNER) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int y = SergeCircle.surfaceY(level, x, z);
                // Не на воде
                if (!level.getBlockState(new BlockPos(x, y - 1, z)).getFluidState().isEmpty()) {
                    return false;
                }
                min = Math.min(min, y);
                max = Math.max(max, y);
            }
        }
        if (max - min > MAX_SLOPE) {
            return false;   // слишком крутой склон
        }
        SergeCircle.build(level, origin, false, false);
        return true;
    }
}
