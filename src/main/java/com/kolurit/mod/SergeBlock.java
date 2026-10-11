package com.kolurit.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// Сэргэ — резной коновязный столб высотой в три блока. Генерируется кругом из четырёх в Долине
// Туймаада; когда все четыре зажжены, в центре круга открывается портал в Туймааду.
public class SergeBlock extends Block {
    public static final int HEIGHT = 3;
    // Часть столба снизу вверх: 0 — основание, 1 — середина, 2 — навершие
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, HEIGHT - 1);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private static final VoxelShape POST = Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape TOP = Shapes.or(
            Block.box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0),
            Block.box(4.0, 10.0, 4.0, 12.0, 14.0, 12.0),
            Block.box(6.0, 14.0, 6.0, 10.0, 16.0, 10.0));

    public SergeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, 0).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, LIT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(PART) == HEIGHT - 1 ? TOP : POST;
    }

    // Столб держится, только пока цел целиком: без соседней части (сломали в креативе) исчезает
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        int part = state.getValue(PART);
        boolean lostAbove = direction == Direction.UP && part < HEIGHT - 1 && !neighborState.is(this);
        boolean lostBelow = direction == Direction.DOWN && part > 0 && !neighborState.is(this);
        if (lostAbove || lostBelow) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    // Активатор (тег edition1:serge_activators, пока огниво) по незажжённому сэргэ — зажигает весь столб
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(ModTags.SERGE_ACTIVATORS)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (state.getValue(LIT)) {
            return InteractionResult.SUCCESS;   // уже горит — не даём огниву поджечь что-то рядом
        }
        if (!level.isClientSide()) {
            BlockPos base = pos.below(state.getValue(PART));
            for (int i = 0; i < HEIGHT; i++) {
                BlockPos partPos = base.above(i);
                BlockState partState = level.getBlockState(partPos);
                if (partState.is(this)) {
                    level.setBlock(partPos, partState.setValue(LIT, true), Block.UPDATE_ALL);
                }
            }
            level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (stack.isDamageableItem()) {
                stack.hurtAndBreak(1, player, hand);
            } else {
                stack.consume(1, player);
            }
            SergeCircle.tryOpenPortal(level, base);
        }
        return InteractionResult.SUCCESS;
    }

    // Зажжённое навершие тлеет: редкие искры
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT) && state.getValue(PART) == HEIGHT - 1 && random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.SMALL_FLAME,
                    pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                    pos.getY() + 1.0,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                    0.0, 0.01, 0.0);
        }
    }
}
