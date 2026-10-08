package com.kolurit.mod;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// Хранит метки ферментации бутылок, стоящих на блоке (в порядке постановки)
public class ByirpahBottleBlockEntity extends BlockEntity {
    private static final Codec<List<Long>> STARTS_CODEC = Codec.LONG.listOf();

    private final List<Long> starts = new ArrayList<>();

    public ByirpahBottleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BYIRPAH_BOTTLES.get(), pos, state);
    }

    public List<Long> starts() {
        return List.copyOf(starts);
    }

    public void push(long start) {
        starts.add(start);
        setChanged();
    }

    // Снимает последнюю поставленную бутылку; null — меток нет (блок поставлен до этой версии мода)
    public @Nullable Long pop() {
        if (starts.isEmpty()) {
            return null;
        }
        Long start = starts.remove(starts.size() - 1);
        setChanged();
        return start;
    }

    // Возвращает метку обратно, если бутылку не удалось отдать игроку
    public void unpop(@Nullable Long start) {
        if (start != null) {
            push(start);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("ferment_starts", STARTS_CODEC, starts);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        starts.clear();
        input.read("ferment_starts", STARTS_CODEC).ifPresent(starts::addAll);
    }

    // Раз в секунду: блок показывает стадию самой молодой бутылки и дозревает вместе с ней.
    // Время берётся из меток, так что после выгрузки чанка всё догоняется сразу.
    public static void serverTick(Level level, BlockPos pos, BlockState state, ByirpahBottleBlockEntity be) {
        if (level.getGameTime() % 20 != 0 || be.starts.isEmpty()
                || !(state.getBlock() instanceof ByirpahBottleBlock block)) {
            return;
        }
        long youngest = be.starts.stream().mapToLong(Long::longValue).max().getAsLong();
        ByirpahStage target = block.stage().targetFor(level.getGameTime() - youngest, ByirpahItem.stageTicks());
        if (target == block.stage()) {
            return;
        }

        // Смена блока удаляет старый BlockEntity — переносим метки в новый
        List<Long> starts = List.copyOf(be.starts);
        BlockState next = ModBlocks.byirpahBottleFor(target).defaultBlockState()
                .setValue(ByirpahBottleBlock.BOTTLES, state.getValue(ByirpahBottleBlock.BOTTLES));
        level.setBlock(pos, next, Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof ByirpahBottleBlockEntity moved) {
            starts.forEach(moved::push);
        }
        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
