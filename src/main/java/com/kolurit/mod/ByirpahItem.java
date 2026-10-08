package com.kolurit.mod;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jspecify.annotations.Nullable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.function.Consumer;

public class ByirpahItem extends Item {

    private final ByirpahStage stage;

    public ByirpahItem(Item.Properties properties, ByirpahStage stage) {
        super(properties);
        this.stage = stage;
    }

    public ByirpahStage stage() {
        return stage;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (merrimentLevel(player) >= stage.maxLevel()) {
            if (!level.isClientSide()) {
                Component msg = Component.translatable("message.edition1.too_merry");
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(msg));
                }
            }
            return InteractionResult.PASS;
        }
        return super.use(level, player, hand);
    }

    // Допили: применяем строку таблицы
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity); // еда + возврат тары
        if (!level.isClientSide()) {
            int current = merrimentLevel(entity);
            int next = Math.min(Math.max(current + 1, stage.baseLevel()), stage.maxLevel());
            ByirpahStage.StageRow row = stage.row(next);

            // Весёлость: усилитель = уровень - 1
            entity.addEffect(new MobEffectInstance(ModEffects.MERRIMENT, row.merrimentTicks(), next - 1));
            // Остальные эффекты строки
            for (ByirpahStage.EffectEntry entry : row.effects()) {
                entity.addEffect(new MobEffectInstance(entry.effect(), entry.ticks(), 0));
            }
            // Перепил: строка со Слабостью снимает остатки Силы и Спешки
            boolean drunk = row.effects().stream()
                    .anyMatch(entry -> entry.effect() == MobEffects.WEAKNESS);
            if (drunk) {
                entity.removeEffect(MobEffects.STRENGTH);
                entity.removeEffect(MobEffects.HASTE);
            }
        }
        return result;
    }

    // Текущий уровень Весёлости (0, если эффекта нет)
    private static int merrimentLevel(LivingEntity entity) {
        MobEffectInstance instance = entity.getEffect(ModEffects.MERRIMENT);
        return instance == null ? 0 : instance.getAmplifier() + 1;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);

        Long start = stack.get(ModComponents.FERMENT_START.get());
        if (start == null) {                                   // ленивая метка (креатив, /give)
            stack.set(ModComponents.FERMENT_START.get(), level.getGameTime());
            return;
        }

        ItemStack transformed = aged(stack, level.getGameTime());
        if (transformed == null) {
            return;                                            // ещё не настоялось
        }

        if (entity instanceof Player player) {
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i) == stack) {                 // тот же экземпляр = наш слот
                    inv.setItem(i, transformed);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1.0F, 1.0F);
                    break;
                }
            }
        }
    }

    // Порог одной стадии в тиках
    public static long stageTicks() {
        return Config.FERMENT_SECONDS.get() * 20L;
    }

    // Метка ферментации стака или текущее время, если метки ещё нет
    public static long fermentStart(ItemStack stack, Level level) {
        Long start = stack.get(ModComponents.FERMENT_START.get());
        return start != null ? start : level.getGameTime();
    }

    // Бутылка нужной стадии с заданной меткой ферментации
    public static ItemStack bottle(ByirpahStage stage, long start, long gameTime) {
        ItemStack stack = new ItemStack(ModItems.byirpahFor(stage));
        stack.set(ModComponents.FERMENT_START.get(), start);
        ItemStack transformed = aged(stack, gameTime);
        return transformed != null ? transformed : stack;
    }

    // Если стак уже настоялся до следующей стадии — новый стак этой стадии
    // (количество, метка и прочие компоненты сохраняются), иначе null.
    // Ферментация идёт по метке времени, поэтому где лежала бутылка — неважно:
    // подменить её можно в любой момент, когда до неё дотянулись.
    public static @Nullable ItemStack aged(ItemStack stack, long gameTime) {
        if (!(stack.getItem() instanceof ByirpahItem item)) {
            return null;
        }
        Long start = stack.get(ModComponents.FERMENT_START.get());
        if (start == null) {
            return null;
        }
        ByirpahStage target = item.stage.targetFor(gameTime - start, stageTicks());
        return target == item.stage ? null : stack.transmuteCopy(ModItems.byirpahFor(target));
    }

    // Выброшенная бутылка тоже дозревает (раз в секунду)
    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (entity.level() instanceof ServerLevel level && level.getGameTime() % 20 == 0) {
            ItemStack transformed = aged(stack, level.getGameTime());
            if (transformed != null) {
                entity.setItem(transformed);
            }
        }
        return false;                                          // обычное поведение предмета дальше
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);

        Long start = stack.get(ModComponents.FERMENT_START.get());
        if (start == null) {
            tooltipComponents.accept(Component.translatable("tooltip.edition1.fresh")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        Level level = context.level();
        if (level == null) return;

        long now = level.getGameTime();
        long age = now - start;
        long stageTicks = stageTicks();

        switch (stage) {
            case FRESH -> {
                long left = (age < stageTicks ? stageTicks : 2 * stageTicks) - age;
                tooltipComponents.accept(Component.translatable("tooltip.edition1.next_stage", formatTime(left))
                        .withStyle(ChatFormatting.GRAY));
            }
            case AGED -> {
                if (age < 2 * stageTicks) {
                    tooltipComponents.accept(Component.translatable("tooltip.edition1.next_stage", formatTime(2 * stageTicks - age))
                            .withStyle(ChatFormatting.GRAY));
                } else {
                    tooltipComponents.accept(Component.translatable("tooltip.edition1.peak")
                            .withStyle(ChatFormatting.YELLOW));
                }
            }
            case STRONG -> tooltipComponents.accept(Component.translatable("tooltip.edition1.peak")
                    .withStyle(ChatFormatting.YELLOW));
        }
    }
    private static String formatTime(long ticks) {
        long totalSeconds = ticks / 20;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        // Ставим блок только если игрок зажал Shift (присел)
        if (player != null && player.isCrouching()) {
            BlockPos clickedPos = context.getClickedPos();
            Direction face = context.getClickedFace();
            BlockPos targetPos = clickedPos.relative(face);
            Level level = context.getLevel();

            // Кликнули по уже стоящим бутылкам той же стадии — доставляем ещё одну (до трёх)
            BlockState clickedState = level.getBlockState(clickedPos);
            if (clickedState.getBlock() instanceof ByirpahBottleBlock bottleBlock
                    && bottleBlock.canAddBottle(clickedState, stage)) {
                if (!level.isClientSide()) {
                    bottleBlock.addBottle(clickedState, level, clickedPos, player,
                            fermentStart(context.getItemInHand(), level));
                    context.getItemInHand().consume(1, player);
                }
                return InteractionResult.SUCCESS;
            }

            // Проверяем, что целевая позиция — воздух (можно ставить)
            BlockState targetState = level.getBlockState(targetPos);
            if (targetState.isAir()) {
                // Ставим декоративную бутылку
                level.setBlock(targetPos, ModBlocks.byirpahBottleFor(stage).defaultBlockState(), 3);
                // Метка бутылки переезжает в блок — ферментация продолжается
                if (!level.isClientSide() && level.getBlockEntity(targetPos) instanceof ByirpahBottleBlockEntity be) {
                    be.push(fermentStart(context.getItemInHand(), level));
                }
                level.gameEvent(player, net.minecraft.world.level.gameevent.GameEvent.BLOCK_PLACE, targetPos);

                // Уменьшаем стак, если игрок не в креативе
                if (!player.isCreative()) {
                    context.getItemInHand().shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        // Если Shift не зажат или позиция занята, возвращаем PASS, чтобы сработала логика питья
        return InteractionResult.PASS;
    }
}