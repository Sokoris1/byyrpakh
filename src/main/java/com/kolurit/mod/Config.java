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
    public static final ModConfigSpec.IntValue FERMENT_SECONDS = BUILDER
            .comment("Seconds of game time per fermentation stage (1200 = 20 minutes = full MC day)")
            .defineInRange("fermentSeconds", 1200, 1, 86400);

    public static final ModConfigSpec.IntValue SLEEP_BONUS_TICKS = BUILDER
            .comment("Ticks of aging added to inventory bottles when players sleep (12000 = one night, 0 = off)")
            .defineInRange("sleepBonusTicks", 12000, 0, 240000);

    static final ModConfigSpec SPEC = BUILDER.build();

}
