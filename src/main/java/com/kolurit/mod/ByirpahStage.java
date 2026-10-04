package com.kolurit.mod;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

import java.util.List;
import java.util.Map;

public enum ByirpahStage {

    // Свежий: база I, максимум II
    FRESH(1, 2, Map.of(
            1, new StageRow(1800, List.of(                          // Весёлость I 1:30
                    new EffectEntry(MobEffects.STRENGTH, 1200))),    // + Сила 1:00
            2, new StageRow(3000, List.of(                          // Весёлость II 2:30
                    new EffectEntry(MobEffects.STRENGTH, 1800),      // + Сила 1:30
                    new EffectEntry(MobEffects.HASTE, 1800)))    // + Спешка 1:30
    )),

    // Стоявший: база II, максимум IV
    AGED(2, 4, Map.of(
            2, new StageRow(3000, List.of(
                    new EffectEntry(MobEffects.STRENGTH, 1800),
                    new EffectEntry(MobEffects.HASTE, 1800))),
            3, new StageRow(3000, List.of(
                    new EffectEntry(MobEffects.STRENGTH, 1800),
                    new EffectEntry(MobEffects.HASTE, 1800),
                    new EffectEntry(MobEffects.FIRE_RESISTANCE, 1800))),
            4, new StageRow(3000, List.of(
                    new EffectEntry(MobEffects.WEAKNESS, 1800),
                    new EffectEntry(MobEffects.FIRE_RESISTANCE, 1800)))
    )),

    // Крепкий: база II, максимум V
    STRONG(2, 5, Map.of(
            2, new StageRow(3000, List.of(
                    new EffectEntry(MobEffects.STRENGTH, 1800),
                    new EffectEntry(MobEffects.HASTE, 1800),
                    new EffectEntry(MobEffects.FIRE_RESISTANCE, 1800))),
            3, new StageRow(3000, List.of(
                    new EffectEntry(MobEffects.WEAKNESS, 1800),
                    new EffectEntry(MobEffects.FIRE_RESISTANCE, 1800))),
            4, new StageRow(3000, List.of(
                    new EffectEntry(MobEffects.WEAKNESS, 1800),
                    new EffectEntry(MobEffects.SLOWNESS, 1800))),
            5, new StageRow(4200, List.of(
                    new EffectEntry(MobEffects.WEAKNESS, 4200),
                    new EffectEntry(MobEffects.NAUSEA, 300),
                    new EffectEntry(MobEffects.SLOWNESS, 1800),
                    new EffectEntry(MobEffects.BLINDNESS, 600)))
    ));

    private final int baseLevel;
    private final int maxLevel;
    private final Map<Integer, StageRow> rows;

    ByirpahStage(int baseLevel, int maxLevel, Map<Integer, StageRow> rows) {
        this.baseLevel = baseLevel;
        this.maxLevel = maxLevel;
        this.rows = rows;
    }
    public ByirpahStage targetFor(long age, long stageTicks) {
        return switch (this) {
            case FRESH -> age >= 2 * stageTicks ? STRONG : age >= stageTicks ? AGED : FRESH;
            case AGED -> age >= 2 * stageTicks ? STRONG : AGED;
            case STRONG -> STRONG;
        };
    }
    public int baseLevel() { return baseLevel; }
    public int maxLevel() { return maxLevel; }
    public StageRow row(int level) { return rows.get(level); }

    // Строка таблицы: длительность Весёлости + список ванильных эффектов
    public record StageRow(int merrimentTicks, List<EffectEntry> effects) {}

    // Один эффект: какой и на сколько тиков (усилитель всегда 0, кроме Весёлости)
    public record EffectEntry(Holder<MobEffect> effect, int ticks) {}
}