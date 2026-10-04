package com.kolurit.mod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;

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
}