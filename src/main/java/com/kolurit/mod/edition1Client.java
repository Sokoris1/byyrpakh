package com.kolurit.mod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// Только клиент: экран настроек мода (Моды → Быырпах → Настройки)
@Mod(value = edition1.MODID, dist = Dist.CLIENT)
public class edition1Client {
    public edition1Client(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
