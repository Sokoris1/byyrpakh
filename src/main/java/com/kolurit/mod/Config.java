package com.kolurit.mod;

import net.neoforged.neoforge.common.ModConfigSpec;

// Настройки мода (config/edition1-common.toml и экран настроек в игре)
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
