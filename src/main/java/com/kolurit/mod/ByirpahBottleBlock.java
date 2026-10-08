package com.kolurit.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ByirpahBottleBlock extends Block {
    public static final int MAX_BOTTLES = 3;
    // Сколько бутылок стоит на блоке: 1..3
    public static final IntegerProperty BOTTLES = IntegerProperty.create("bottles", 1, MAX_BOTTLES);

    // Форма бутылки: тело 4×8×4 + горлышко 2×3×2 (хитбокс — по габаритам, 4×11×4)
    // Раскладка совпадает с моделями byirpah_bottle_template_1/2/3
    private static final VoxelShape ONE = Block.box(6.0, 0.0, 6.0, 10.0, 11.0, 10.0);
    private static final VoxelShape TWO = Shapes.or(
            Block.box(3.0, 0.0, 6.0, 7.0, 11.0, 10.0),
            Block.box(9.0, 0.0, 6.0, 13.0, 11.0, 10.0));
    private static final VoxelShape THREE = Shapes.or(
            Block.box(3.0, 0.0, 3.0, 7.0, 11.0, 7.0),
            Block.box(9.0, 0.0, 3.0, 13.0, 11.0, 7.0),
            Block.box(6.0, 0.0, 9.0, 10.0, 11.0, 13.0));

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

    // Можно ли поставить сюда ещё одну бутылку этой стадии
    public boolean canAddBottle(BlockState state, ByirpahStage itemStage) {
        return itemStage == stage && state.getValue(BOTTLES) < MAX_BOTTLES;
    }

    // Добавляет бутылку к уже стоящим (стак уменьшает вызывающий)
    public void addBottle(BlockState state, Level level, BlockPos pos, Player player) {
        level.setBlock(pos, state.setValue(BOTTLES, state.getValue(BOTTLES) + 1), Block.UPDATE_ALL);
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
                addBottle(state, level, pos, player);
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
            ItemStack bottle = new ItemStack(ModItems.byirpahFor(stage));
            if (!player.addItem(bottle)) {
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
}
