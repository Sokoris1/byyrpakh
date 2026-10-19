package com.kolurit.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

// Круг сэргэ: четыре столба по углам квадрата 7×7, в центре вкопанный бассейн воды 3×3.
// Когда горят все четыре столба, вода в бассейне становится порталом (как в Twilight Forest).
public final class SergeCircle {
    // Смещение столбов от центра по X и Z
    public static final int CORNER = 3;
    private static final int[][] CORNERS = {{CORNER, CORNER}, {CORNER, -CORNER}, {-CORNER, CORNER}, {-CORNER, -CORNER}};
    // Насколько выше или ниже искать основания столбов и воду бассейна
    private static final int SEARCH_DY = 6;
    // Наибольший перепад высот под столбами, при котором круг ещё ставится
    private static final int MAX_SLOPE = 3;
    // Шаг поиска подходящего места
    private static final int SITE_STEP = 4;

    private SergeCircle() {}

    // Подходит ли место под круг с центром x/z: под центром и столбами твёрдая земля, а не вода,
    // и перепад высот между ними не больше MAX_SLOPE
    public static boolean isGoodSite(LevelAccessor level, int x, int z) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int dx = -CORNER; dx <= CORNER; dx += CORNER) {
            for (int dz = -CORNER; dz <= CORNER; dz += CORNER) {
                int px = x + dx;
                int pz = z + dz;
                int y = surfaceY(level, px, pz);
                if (!level.getBlockState(new BlockPos(px, y - 1, pz)).getFluidState().isEmpty()) {
                    return false;
                }
                min = Math.min(min, y);
                max = Math.max(max, y);
            }
        }
        return max - min <= MAX_SLOPE;
    }

    // Ближайшее к x/z подходящее место (по расширяющимся квадратам), не дальше radius; null — не нашли
    public static @Nullable BlockPos findSiteNear(LevelAccessor level, int x, int z, int radius) {
        for (int r = 0; r <= radius; r += SITE_STEP) {
            for (int dx = -r; dx <= r; dx += SITE_STEP) {
                for (int dz = -r; dz <= r; dz += SITE_STEP) {
                    // Только периметр квадрата: внутренние точки проверили на прошлых кругах
                    if (Math.abs(dx) != r && Math.abs(dz) != r) {
                        continue;
                    }
                    if (isGoodSite(level, x + dx, z + dz)) {
                        return new BlockPos(x + dx, surfaceY(level, x + dx, z + dz), z + dz);
                    }
                }
            }
        }
        return null;
    }

    // Высота первого свободного блока над землёй (трава и цветы не считаются, вода считается)
    public static int surfaceY(LevelAccessor level, int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    // Строит круг вокруг center по X/Z (высота берётся с поверхности).
    // Возвращает высоту воды бассейна.
    public static int build(LevelAccessor level, BlockPos center, boolean lit, boolean withPortal) {
        BlockState serge = ModBlocks.SERGE.get().defaultBlockState().setValue(SergeBlock.LIT, lit);
        for (int[] c : CORNERS) {
            int x = center.getX() + c[0];
            int z = center.getZ() + c[1];
            int y = surfaceY(level, x, z);
            for (int i = 0; i < SergeBlock.HEIGHT; i++) {
                level.setBlock(new BlockPos(x, y + i, z), serge.setValue(SergeBlock.PART, i), Block.UPDATE_ALL);
            }
        }
        int poolY = buildPool(level, center);
        if (withPortal) {
            openPortal(level, center.getX(), poolY, center.getZ());
        }
        return poolY;
    }

    // Бассейн 3×3 глубиной в блок, вровень с землёй в центре круга; вокруг — бортик из дёрна
    private static int buildPool(LevelAccessor level, BlockPos center) {
        int poolY = surfaceY(level, center.getX(), center.getZ()) - 1;
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                boolean inside = Math.abs(dx) <= 1 && Math.abs(dz) <= 1;
                // Дно и борта: твёрдая земля, чтобы вода никуда не утекла
                if (!level.getBlockState(new BlockPos(x, poolY - 1, z)).isSolid()) {
                    level.setBlock(new BlockPos(x, poolY - 1, z), dirt, Block.UPDATE_ALL);
                }
                level.setBlock(new BlockPos(x, poolY, z), inside ? water : grass, Block.UPDATE_ALL);
                // Над бассейном и бортом — чисто (без травы, цветов и кочек)
                for (int dy = 1; dy <= 3; dy++) {
                    BlockPos above = new BlockPos(x, poolY + dy, z);
                    if (!level.getBlockState(above).is(ModBlocks.SERGE.get())) {
                        level.setBlock(above, air, Block.UPDATE_ALL);
                    }
                }
            }
        }
        return poolY;
    }

    // Вода бассейна с центром x/z на высоте y становится порталом
    public static void openPortal(LevelAccessor level, int x, int y, int z) {
        BlockState portal = ModBlocks.TUYMAADA_PORTAL.get().defaultBlockState();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = new BlockPos(x + dx, y, z + dz);
                if (level.getBlockState(pos).is(Blocks.WATER)) {
                    level.setBlock(pos, portal, Block.UPDATE_ALL);
                    level.scheduleTick(pos, ModBlocks.TUYMAADA_PORTAL.get(), TuymaadaPortalBlock.CHECK_TICKS);
                }
            }
        }
    }

    // Зажгли столб с основанием base: если он входит в круг, где горят все четыре, — открываем портал
    public static void tryOpenPortal(Level level, BlockPos base) {
        for (int[] c : CORNERS) {
            BlockPos center = base.offset(-c[0], 0, -c[1]);
            if (!allLit(level, center)) {
                continue;
            }
            Integer poolY = findPoolY(level, center);
            if (poolY != null) {
                openPortal(level, center.getX(), poolY, center.getZ());
                level.playSound(null, center, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.7F, 1.2F);
                return;
            }
        }
    }

    // Портал в pos ещё цел? — есть круг, в который он входит, и все четыре столба горят
    public static boolean portalStillValid(LevelAccessor level, BlockPos portalPos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (allLit(level, portalPos.offset(dx, 0, dz))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean allLit(LevelAccessor level, BlockPos center) {
        for (int[] c : CORNERS) {
            BlockPos base = findSergeBase(level, center.getX() + c[0], center.getY(), center.getZ() + c[1]);
            if (base == null || !level.getBlockState(base).getValue(SergeBlock.LIT)) {
                return false;
            }
        }
        return true;
    }

    // Основание столба в колонке x/z не дальше SEARCH_DY блоков по высоте от y
    private static @Nullable BlockPos findSergeBase(LevelAccessor level, int x, int y, int z) {
        for (int dy = -SEARCH_DY; dy <= SEARCH_DY; dy++) {
            BlockPos pos = new BlockPos(x, y + dy, z);
            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlocks.SERGE.get()) && state.getValue(SergeBlock.PART) == 0) {
                return pos;
            }
        }
        return null;
    }

    // Высота воды бассейна в центре круга (ищем сверху вниз рядом с высотой столбов)
    private static @Nullable Integer findPoolY(LevelAccessor level, BlockPos center) {
        for (int dy = SEARCH_DY; dy >= -SEARCH_DY; dy--) {
            BlockPos pos = center.above(dy);
            if (level.getBlockState(pos).is(Blocks.WATER)) {
                return pos.getY();
            }
        }
        return null;
    }

    // Ищет портал рядом с x/z (в квадрате radius): возвращает центр пятна 3×3 — клетку
    // с наибольшим числом соседей-порталов — или null
    public static @Nullable BlockPos findPortalCenter(LevelAccessor level, int x, int z, int radius) {
        BlockPos best = null;
        int bestScore = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos pos = portalIn(level, x + dx, z + dz);
                if (pos == null) {
                    continue;
                }
                int score = 0;
                for (int ox = -1; ox <= 1; ox++) {
                    for (int oz = -1; oz <= 1; oz++) {
                        if (level.getBlockState(pos.offset(ox, 0, oz)).is(ModBlocks.TUYMAADA_PORTAL.get())) {
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

    // Блок портала в колонке x/z: портал не мешает движению, поэтому лежит на высоте surfaceY или чуть ниже
    private static @Nullable BlockPos portalIn(LevelAccessor level, int x, int z) {
        int top = surfaceY(level, x, z);
        for (int dy = 0; dy >= -2; dy--) {
            BlockPos pos = new BlockPos(x, top + dy, z);
            if (level.getBlockState(pos).is(ModBlocks.TUYMAADA_PORTAL.get())) {
                return pos;
            }
        }
        return null;
    }
}
