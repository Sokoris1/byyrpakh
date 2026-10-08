package com.kolurit.mod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class ModEvents {

    // Игрок что-то скрафтил: если это бутылка быырпаха — ставим метку рождения
    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack stack = event.getCrafting();
        if (stack.getItem() instanceof ByirpahItem
                && !stack.has(ModComponents.FERMENT_START.get())) {
            stack.set(ModComponents.FERMENT_START.get(),
                    event.getEntity().level().getGameTime());
        }
    }
    @SubscribeEvent
    public void onSleepFinished(SleepFinishedTimeEvent event) {
        int bonus = Config.SLEEP_BONUS_TICKS.get();
        if (bonus <= 0) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        for (Player player : serverLevel.players()) {
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.getItem() instanceof ByirpahItem) {
                    Long start = stack.get(ModComponents.FERMENT_START.get());
                    if (start != null) {
                        stack.set(ModComponents.FERMENT_START.get(), start - bonus);
                    }
                }
            }
        }
    }

    // Сундук, бочка, шалкер и т.п.: бутылки внутри настаиваются по своим меткам всё время,
    // а подменяем их на следующую стадию, как только игрок открыл контейнер...
    @SubscribeEvent
    public void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity().level() instanceof ServerLevel level) {
            ageBottles(event.getContainer(), level.getGameTime());
        }
    }

    // ...и раз в секунду, пока он открыт (инвентарь игрока обновляет inventoryTick)
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.tickCount % 20 == 0
                && player.containerMenu != player.inventoryMenu
                && player.level() instanceof ServerLevel level) {
            ageBottles(player.containerMenu, level.getGameTime());
        }
    }

    private static void ageBottles(AbstractContainerMenu menu, long gameTime) {
        for (Slot slot : menu.slots) {
            ItemStack transformed = ByirpahItem.aged(slot.getItem(), gameTime);
            if (transformed != null) {
                slot.set(transformed);
            }
        }
    }
}
