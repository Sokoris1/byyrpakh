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
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

// Светящееся пятно в центре круга сэргэ. Постоял на нём пару секунд — перенесло в Туймааду
// (или из неё домой) на те же X/Z, к такому же кругу; если круга там нет — он появляется.
public class TuymaadaPortalBlock extends Block implements Portal {
    public static final ResourceKey<Level> TUYMAADA = ResourceKey.create(Registries.DIMENSION,
            Identifier.fromNamespaceAndPath(edition1.MODID, "tuymaada"));

    // Тонкое пятно на земле; «внутри портала» считаемся, если стоим на нём
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private static final VoxelShape INSIDE = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    // Ожидание на пятне перед переносом, тиков
    private static final int TRANSITION_TICKS = 40;
    // В каком радиусе искать уже стоящий портал в месте прибытия
    private static final int SEARCH_RADIUS = 8;

    public TuymaadaPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
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
            SergeCircle.build(target, center, true, true);
        }
        // Появляемся рядом с пятном, внутри круга, а не на самом портале
        int ax = center.getX();
        int az = center.getZ() + 2;
        Vec3 arrival = new Vec3(ax + 0.5, SergeCircle.surfaceY(target, ax, az), az + 0.5);
        return new TeleportTransition(target, arrival, Vec3.ZERO, entity.getYRot(), entity.getXRot(),
                TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.END_ROD,
                    pos.getX() + random.nextDouble(), pos.getY() + 0.1, pos.getZ() + random.nextDouble(),
                    0.0, 0.03 + random.nextDouble() * 0.03, 0.0);
        }
    }
}
