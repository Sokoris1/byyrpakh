package com.kolurit.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ByirpahBottleBlock extends Block {
    // Форма бутылки: немного уже полного блока, высотой 8 пикселей
    // Форма бутылки: тело 4×8×4 + горлышко 2×3×2
    protected static final VoxelShape SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 11.0, 10.0);
    private final ByirpahStage stage;

    public ByirpahBottleBlock(Properties properties, ByirpahStage stage) {
        super(properties);
        this.stage = stage;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // Обработка ПКМ по блоку (забрать бутылку)
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            ItemStack bottle = new ItemStack(ModItems.byirpahFor(stage));
            if (player.addItem(bottle)) {
                level.removeBlock(pos, false);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}