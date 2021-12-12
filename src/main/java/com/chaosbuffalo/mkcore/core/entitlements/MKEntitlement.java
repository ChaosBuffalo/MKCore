package com.chaosbuffalo.mkcore.core.entitlements;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class MKEntitlement extends ForgeRegistryEntry<MKEntitlement> {
    private final int maxEntitlements;

    public MKEntitlement(ResourceLocation name, int maxEntitlements){
        setRegistryName(name);
        this.maxEntitlements = maxEntitlements;
    }

    public int getMaxEntitlements() {
        return maxEntitlements;
    }
}
