package com.kolurit.mod;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, edition1.MODID);

    // Метка ферментации: игровое время, когда бутылка начала настаиваться
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> FERMENT_START =
            COMPONENTS.register("ferment_start",
                    () -> DataComponentType.<Long>builder()
                            .persistent(Codec.LONG)                  // просто Codec, без fieldOf
                            .networkSynchronized(ByteBufCodecs.VAR_LONG)
                            .build());

}