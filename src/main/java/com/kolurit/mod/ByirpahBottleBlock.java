package com.kolurit.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ByirpahBottleBlock extends Block implements EntityBlock {
    public static final int MAX_BOTTLES = 3;
    // Сколько бутылок стоит на блоке: 1..3
    public static final IntegerProperty BOTTLES = IntegerProperty.create("bottles", 1, MAX_BOTTLES);

    // Форма бутылки: тело 4×9.5×4, плечики, горлышко и крышка (хитбокс — по габаритам, 4×12.5×4)
    // Раскладка совпадает с моделями byirpah_bottle_template_1/2/3
    private static final VoxelShape ONE = Block.box(6.0, 0.0, 6.0, 10.0, 12.5, 10.0);
    private static final VoxelShape TWO = Shapes.or(
            Block.box(3.0, 0.0, 6.0, 7.0, 12.5, 10.0),
            Block.box(9.0, 0.0, 6.0, 13.0, 12.5, 10.0));
    private static final VoxelShape THREE = Shapes.or(
            Block.box(3.0, 0.0, 3.0, 7.0, 12.5, 7.0),
            Block.box(9.0, 0.0, 3.0, 13.0, 12.5, 7.0),
            Block.box(6.0, 0.0, 9.0, 10.0, 12.5, 13.0));

    private final ByirpahStage stage;

    public ByirpahBottleBlock(Properties properties, ByirpahStage stage) {
        super(properties);
        this.stage = stage;
        registerDefaultState(stateDefinition.any().setValue(BOTTLES, 1));
    }

    public ByirpahStage stage() {
        return stage;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BOTTLES);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(BOTTLES)) {
            case 2 -> TWO;
            case 3 -> THREE;
            default -> ONE;
        };
    }

    // Бутылкам нужна опора снизу (как свечам)
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    // Убрали блок снизу — бутылки разбиваются; дроп отдаёт getDrops
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    // Дроп при любом разрушении (игрок не в креативе, взрыв, пропавшая опора, поршень):
    // каждая бутылка — со своей меткой ферментации и в своей стадии
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        long gameTime = params.getLevel().getGameTime();
        List<Long> starts = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof ByirpahBottleBlockEntity be
                ? be.starts() : List.of();
        List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i < state.getValue(BOTTLES); i++) {
            drops.add(i < starts.size()
                    ? ByirpahItem.bottle(stage, starts.get(i), gameTime)
                    : new ItemStack(ModItems.byirpahFor(stage)));   // блок старой версии — без меток
        }
        return drops;
    }

    // Можно ли поставить сюда ещё одну бутылку этой стадии
    public boolean canAddBottle(BlockState state, ByirpahStage itemStage) {
        return itemStage == stage && state.getValue(BOTTLES) < MAX_BOTTLES;
    }

    // Добавляет бутылку к уже стоящим (стак уменьшает вызывающий); только на сервере
    public void addBottle(BlockState state, Level level, BlockPos pos, Player player, long fermentStart) {
        level.setBlock(pos, state.setValue(BOTTLES, state.getValue(BOTTLES) + 1), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof ByirpahBottleBlockEntity be) {
            be.push(fermentStart);
        }
        level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
    }

    // ПКМ бутылкой той же стадии — доставляем ещё одну
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof ByirpahItem item) {
            if (!canAddBottle(state, item.stage())) {
                return InteractionResult.PASS;                 // места нет или другая стадия — просто пьём
            }
            if (!level.isClientSide()) {
                addBottle(state, level, pos, player, ByirpahItem.fermentStart(stack, level));
                stack.consume(1, player);
            }
            return InteractionResult.SUCCESS;
        }
        // Другой предмет — пробуем как пустой рукой (useWithoutItem)
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    // ПКМ пустой рукой — забираем одну бутылку
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            // Бутылка уходит со своей меткой и сразу нужной стадии (могла дозреть раньше блока)
            ByirpahBottleBlockEntity be = level.getBlockEntity(pos) instanceof ByirpahBottleBlockEntity found ? found : null;
            Long start = be != null ? be.pop() : null;
            ItemStack bottle = start != null
                    ? ByirpahItem.bottle(stage, start, level.getGameTime())
                    : new ItemStack(ModItems.byirpahFor(stage));
            if (!player.addItem(bottle)) {
                if (be != null) {
                    be.unpop(start);
                }
                return InteractionResult.PASS;                 // инвентарь полон
            }
            int bottles = state.getValue(BOTTLES);
            if (bottles > 1) {
                level.setBlock(pos, state.setValue(BOTTLES, bottles - 1), Block.UPDATE_ALL);
            } else {
                level.removeBlock(pos, false);
            }
            level.playSound(null, pos, SoundEvents.GLASS_HIT, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ByirpahBottleBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.BYIRPAH_BOTTLES.get()) {
            return null;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<ByirpahBottleBlockEntity>) ByirpahBottleBlockEntity::serverTick;
    }
}
