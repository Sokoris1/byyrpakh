package com.kolurit.mod; // ваш пакет

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class ModItems {
    // Реестр предметов нашего мода
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(edition1.MODID);

    public static final DeferredItem<Item> BYIRPAH_EMPTY =
            ITEMS.registerSimpleItem("byirpah_empty");
    // 1) пустая бутылка (уже есть выше)
// 2) три стадии напитка
    public static final DeferredItem<Item> BYIRPAH = ITEMS.registerItem("byirpah",
            props -> new ByirpahItem(drinkProps(props), ByirpahStage.FRESH));

    public static final DeferredItem<Item> BYIRPAH_AGED = ITEMS.registerItem("byirpah_aged",
            props -> new ByirpahItem(drinkProps(props), ByirpahStage.AGED));

    public static final DeferredItem<Item> BYIRPAH_STRONG = ITEMS.registerItem("byirpah_strong",
            props -> new ByirpahItem(drinkProps(props), ByirpahStage.STRONG));
    // Закваска — полуфабрикат для крафта быырпаха
    public static final DeferredItem<Item> STARTER = ITEMS.registerItem("starter",
            props -> new Item(props.stacksTo(16)));
    // Общие свойства напитков — в одном месте, чтобы не дублировать
    private static Item.Properties drinkProps(Item.Properties props) {
        return props.stacksTo(16)
                .food(new FoodProperties.Builder()
                        .nutrition(4)
                        .saturationModifier(0.6F)
                        .alwaysEdible()
                        .build())
                .usingConvertsTo(BYIRPAH_EMPTY.get())
                .component(DataComponents.CONSUMABLE, Consumable.builder()
                        .consumeSeconds(1.6F)
                        .animation(ItemUseAnimation.DRINK)
                        .sound(SoundEvents.GENERIC_DRINK)
                        .build());
    }
    public static Item byirpahFor(ByirpahStage stage) {
        return switch (stage) {
            case FRESH -> BYIRPAH.get();
            case AGED -> BYIRPAH_AGED.get();
            case STRONG -> BYIRPAH_STRONG.get();
        };
    }

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, edition1.MODID);

    public static final Supplier<CreativeModeTab> BYIRPAH_TAB = CREATIVE_MODE_TABS.register("byirpah_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.edition1.byirpah_tab"))
                    .icon(() -> new ItemStack(BYIRPAH_STRONG.get()))
                    .displayItems((params, output) -> {
                        output.accept(BYIRPAH_EMPTY.get());
                        output.accept(STARTER.get());
                        output.accept(BYIRPAH.get());
                        output.accept(BYIRPAH_AGED.get());
                        output.accept(BYIRPAH_STRONG.get());
                    })
                    .build());
}