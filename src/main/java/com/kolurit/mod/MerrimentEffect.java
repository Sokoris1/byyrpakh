package com.kolurit.mod;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class MerrimentEffect extends MobEffect {
    public MerrimentEffect(MobEffectCategory category, int color) {
        super(category, color);   // защищённый конструктор родителя доступен из наследника
    }
}