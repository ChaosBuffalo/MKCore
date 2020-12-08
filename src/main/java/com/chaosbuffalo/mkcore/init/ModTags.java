package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.item.Item;
import net.minecraft.tags.*;
import net.minecraft.util.ResourceLocation;

public class ModTags {
    public static class Items {
        protected static final TagRegistry<Item> collection = TagRegistryManager.create(MKCore.makeRL("item"), ITagCollectionSupplier::getItemTags);
        public static final ITag.INamedTag<Item> LIGHT_ARMOR = tag("armor/light");
        public static final ITag.INamedTag<Item> MEDIUM_ARMOR = tag("armor/medium");
        public static final ITag.INamedTag<Item> HEAVY_ARMOR = tag("armor/heavy");

        private static ITag.INamedTag<Item> tag(String name) {
            return collection.createTag(name);
        }

    }
}
