package com.kolurit.mod;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    // Сколько секунд игрового времени идёт одна стадия ферментации.
// 1200 секунд = 20 минут = полные игровые сутки.
    public static final ModConfigSpec.IntValue FERMENT_SECONDS = BUILDER
            .comment("Seconds of game time per fermentation stage (1200 = 20 minutes = full MC day)")
            .defineInRange("fermentSeconds", 1200, 1, 86400);

    // Сколько тиков возраста добавляет бутылкам в инвентаре проспанные ночи.
// 12000 = длина ночи. 0 = выключить бонус сна.
    public static final ModConfigSpec.IntValue SLEEP_BONUS_TICKS = BUILDER
            .comment("Ticks of aging added to inventory bottles when players sleep (12000 = one night, 0 = off)")
            .defineInRange("sleepBonusTicks", 12000, 0, 240000);

    public static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    public static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", Config::validateItemName);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(Identifier.parse(itemName));
    }
}
