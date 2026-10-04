package com.kolurit.mod; // ваш пакет

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.core.component.DataComponents;

public class ModItems {

    // Реестр предметов нашего мода
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(edition1.MODID);

    // Сам предмет
    public static final DeferredItem<Item> RUBY =
            ITEMS.registerSimpleItem("ruby");
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
}