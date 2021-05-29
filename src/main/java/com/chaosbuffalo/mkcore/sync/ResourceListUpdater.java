package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.function.Supplier;

public class ResourceListUpdater extends SyncListUpdater<ResourceLocation> {
    public ResourceListUpdater(String name, Supplier<List<ResourceLocation>> list) {
        super(name, list, ResourceListUpdater::encode, ResourceListUpdater::decode);
    }

    private static INBT encode(ResourceLocation location) {
        return StringNBT.valueOf(location.toString());
    }

    private static ResourceLocation decode(INBT nbt) {
        return ResourceLocation.tryCreate(nbt.getString());
    }
}
