package com.kolurit.mod;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;
import terrablender.api.Regions;

@Mod(edition1.MODID)
public class edition1 {
    public static final String MODID = "edition1";
    public static final Logger LOGGER = LogUtils.getLogger();
    public edition1(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        ModItems.ITEMS.register(modEventBus);
        ModItems.CREATIVE_MODE_TABS.register(modEventBus);
        ModEffects.MOB_EFFECTS.register(modEventBus);
        ModComponents.COMPONENTS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);

        // Игровые события: ферментация, крафт, сон
        NeoForge.EVENT_BUS.register(new ModEvents());
        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Регистрируем регион с Долиной Туймаада в генерации обычного мира
        event.enqueueWork(() -> Regions.register(new TuymaadaRegion()));
    }

    // Напитки — ещё и во вкладку «Еда и напитки», сардаана — в «Природные блоки»
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ModItems.BYIRPAH);
            event.accept(ModItems.BYIRPAH_AGED);
            event.accept(ModItems.BYIRPAH_STRONG);
        }
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(ModItems.SARDAANA);
        }
    }
}
