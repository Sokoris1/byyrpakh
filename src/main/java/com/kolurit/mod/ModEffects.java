package com.kolurit.mod;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {

    // Реестр эффектов нашего мода
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, edition1.MODID);

    // Сама Весёлость: нейтральная категория + цвет в HUD
    public static final DeferredHolder<MobEffect, MobEffect> MERRIMENT =
            MOB_EFFECTS.register("merriment",
                    () -> new MerrimentEffect(MobEffectCategory.NEUTRAL, 0xE4588C));

}
