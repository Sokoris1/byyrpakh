package com.kolurit.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

// Портал в бассейне в центре круга сэргэ. Шагнул в него и постоял пару секунд — перенесло в Туймааду
// (или из неё домой) на те же X/Z, к такому же кругу; если круга там нет — он появляется.
// Неосязаемый, как воздух: его не выделить и не сломать. Если круг погас или разрушен,
// портал снова становится водой.
public class TuymaadaPortalBlock extends Block implements Portal {
    public static final ResourceKey<Level> TUYMAADA = ResourceKey.create(Registries.DIMENSION,
            Identifier.fromNamespaceAndPath(edition1.MODID, "tuymaada"));

    // «Внутри портала» — весь блок бассейна
    private static final VoxelShape INSIDE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    // Как часто портал проверяет, цел ли круг, тиков
    public static final int CHECK_TICKS = 40;
    // Ожидание на пятне перед переносом, тиков
    private static final int TRANSITION_TICKS = 40;
    // В каком радиусе искать уже стоящий портал в месте прибытия
    private static final int SEARCH_RADIUS = 8;

    public TuymaadaPortalBlock(Properties properties) {
        super(properties);
    }

    // Нет формы — нельзя навести курсор и сломать даже в креативе
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    // Круг разрушен или погас — снова вода
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (SergeCircle.portalStillValid(level, pos)) {
            level.scheduleTick(pos, this, CHECK_TICKS);
        } else {
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected VoxelShape getEntityInsideCollisionShape(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return INSIDE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean intersects) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        return entity instanceof Player player && player.getAbilities().invulnerable ? 1 : TRANSITION_TICKS;
    }

    @Override
    public @Nullable TeleportTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {
        ResourceKey<Level> targetKey = TUYMAADA.equals(level.dimension()) ? Level.OVERWORLD : TUYMAADA;
        ServerLevel target = level.getServer().getLevel(targetKey);
        if (target == null) {
            return null;
        }
        int x = pos.getX();
        int z = pos.getZ();
        BlockPos center = SergeCircle.findPortalCenter(target, x, z, SEARCH_RADIUS);
        if (center == null) {
            // Круга на той стороне нет — ставим зажжённый круг с открытым порталом
            center = new BlockPos(x, SergeCircle.surfaceY(target, x, z), z);
            int poolY = SergeCircle.build(target, center, true, true);
            center = new BlockPos(x, poolY, z);
        }
        // Появляемся на бортике бассейна, внутри круга, а не в самом портале
        int ax = center.getX();
        int az = center.getZ() + 2;
        Vec3 arrival = new Vec3(ax + 0.5, center.getY() + 1, az + 0.5);
        return new TeleportTransition(target, arrival, Vec3.ZERO, entity.getYRot(), entity.getXRot(),
                TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.END_ROD,
                    pos.getX() + random.nextDouble(), pos.getY() + 0.9, pos.getZ() + random.nextDouble(),
                    0.0, 0.03 + random.nextDouble() * 0.03, 0.0);
        }
    }
}
