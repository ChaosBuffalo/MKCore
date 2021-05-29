package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;

public class CoreTags {
    public static class Items {
        public static final ITag.INamedTag<Item> ARMOR = tag("armor");
        public static final ITag.INamedTag<Item> LIGHT_ARMOR = tag("armor/light");
        public static final ITag.INamedTag<Item> MEDIUM_ARMOR = tag("armor/medium");
        public static final ITag.INamedTag<Item> HEAVY_ARMOR = tag("armor/heavy");

        private static ITag.INamedTag<Item> tag(String name) {
            return ItemTags.makeWrapperTag(MKCore.makeRL(name).toString());
        }
    }
}
