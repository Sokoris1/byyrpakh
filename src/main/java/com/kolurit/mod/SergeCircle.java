package com.kolurit.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

// Круг сэргэ: четыре столба по углам квадрата 7×7 и портал 3×3 в центре.
// Все столбы и клетки портала стоят каждый на своей высоте поверхности, так что круг ложится на склон.
public final class SergeCircle {
    // Смещение столбов от центра по X и Z
    public static final int CORNER = 3;
    private static final int[][] CORNERS = {{CORNER, CORNER}, {CORNER, -CORNER}, {-CORNER, CORNER}, {-CORNER, -CORNER}};
    // Насколько выше или ниже центра искать основания столбов
    private static final int SEARCH_DY = 6;

    private SergeCircle() {}

    // Верхний свободный блок над поверхностью (растения и вода считаются поверхностью не будут)
    public static int surfaceY(LevelAccessor level, int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    // Строит круг вокруг center (по X/Z; высоту берёт с поверхности)
    public static void build(LevelAccessor level, BlockPos center, boolean lit, boolean withPortal) {
        BlockState serge = ModBlocks.SERGE.get().defaultBlockState().setValue(SergeBlock.LIT, lit);
        for (int[] c : CORNERS) {
            int x = center.getX() + c[0];
            int z = center.getZ() + c[1];
            int y = surfaceY(level, x, z);
            for (int i = 0; i < SergeBlock.HEIGHT; i++) {
                level.setBlock(new BlockPos(x, y + i, z), serge.setValue(SergeBlock.PART, i), Block.UPDATE_ALL);
            }
        }
        if (withPortal) {
            placePortal(level, center);
        }
    }

    // Пятно портала 3×3 в центре круга, поверх травы и цветов
    public static void placePortal(LevelAccessor level, BlockPos center) {
        BlockState portal = ModBlocks.TUYMAADA_PORTAL.get().defaultBlockState();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                BlockPos pos = new BlockPos(x, surfaceY(level, x, z), z);
                BlockState here = level.getBlockState(pos);
                // Ставим только туда, где нет твёрдого блока и жидкости: воздух, трава, цветы
                if (here.getCollisionShape(level, pos).isEmpty() && here.getFluidState().isEmpty()) {
                    level.setBlock(pos, portal, Block.UPDATE_ALL);
                }
            }
        }
    }

    // Зажгли столб с основанием base: если он входит в круг, где горят все четыре, — открываем портал
    public static void tryOpenPortal(Level level, BlockPos base) {
        for (int[] c : CORNERS) {
            BlockPos center = base.offset(-c[0], 0, -c[1]);
            if (allLit(level, center)) {
                placePortal(level, center);
                level.playSound(null, center, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.7F, 1.2F);
                return;
            }
        }
    }

    private static boolean allLit(Level level, BlockPos center) {
        for (int[] c : CORNERS) {
            BlockPos base = findSergeBase(level, center.getX() + c[0], center.getY(), center.getZ() + c[1]);
            if (base == null || !level.getBlockState(base).getValue(SergeBlock.LIT)) {
                return false;
            }
        }
        return true;
    }

    // Основание столба в колонке x/z не дальше SEARCH_DY блоков по высоте от y
    private static @Nullable BlockPos findSergeBase(Level level, int x, int y, int z) {
        for (int dy = -SEARCH_DY; dy <= SEARCH_DY; dy++) {
            BlockPos pos = new BlockPos(x, y + dy, z);
            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlocks.SERGE.get()) && state.getValue(SergeBlock.PART) == 0) {
                return pos;
            }
        }
        return null;
    }

    // Ищет портал рядом с x/z (в квадрате radius): возвращает клетку портала с наибольшим числом
    // соседей-порталов, то есть центр пятна 3×3, или null
    public static @Nullable BlockPos findPortalCenter(LevelAccessor level, int x, int z, int radius) {
        BlockPos best = null;
        int bestScore = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int px = x + dx;
                int pz = z + dz;
                BlockPos pos = new BlockPos(px, surfaceY(level, px, pz), pz);
                if (!isPortal(level, pos)) {
                    continue;
                }
                int score = 0;
                for (int ox = -1; ox <= 1; ox++) {
                    for (int oz = -1; oz <= 1; oz++) {
                        BlockPos n = new BlockPos(px + ox, surfaceY(level, px + ox, pz + oz), pz + oz);
                        if (isPortal(level, n)) {
                            score++;
                        }
                    }
                }
                if (score > bestScore) {
                    bestScore = score;
                    best = pos;
                }
            }
        }
        return best;
    }

    private static boolean isPortal(LevelAccessor level, BlockPos pos) {
        // Портал не блокирует движение, поэтому лежит на уровне surfaceY или на блок ниже
        return level.getBlockState(pos).is(ModBlocks.TUYMAADA_PORTAL.get())
                || level.getBlockState(pos.below()).is(ModBlocks.TUYMAADA_PORTAL.get());
    }
}
