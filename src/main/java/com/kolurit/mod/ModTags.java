package com.kolurit.mod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {
    // Чем зажигают сэргэ: data/edition1/tags/item/serge_activators.json (пока огниво)
    public static final TagKey<Item> SERGE_ACTIVATORS = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(edition1.MODID, "serge_activators"));
}
