package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.world.item.Item;
import net.minecraft.tags.Tag;
import net.minecraft.tags.ItemTags;

public class CoreTags {
    public static class Items {
        public static final Tag.Named<Item> ARMOR = tag("armor");
        public static final Tag.Named<Item> LIGHT_ARMOR = tag("armor/light");
        public static final Tag.Named<Item> MEDIUM_ARMOR = tag("armor/medium");
        public static final Tag.Named<Item> HEAVY_ARMOR = tag("armor/heavy");

        private static Tag.Named<Item> tag(String name) {
            return ItemTags.bind(MKCore.makeRL(name).toString());
        }
    }
}
