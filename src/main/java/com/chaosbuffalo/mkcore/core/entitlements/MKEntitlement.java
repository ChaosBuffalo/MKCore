package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.records.IRecordType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class MKEntitlement extends ForgeRegistryEntry<MKEntitlement> {
    private final int maxEntitlements;

    public MKEntitlement(ResourceLocation name, int maxEntitlements) {
        setRegistryName(name);
        this.maxEntitlements = maxEntitlements;
    }

    public int getMaxEntitlements() {
        return maxEntitlements;
    }

    public abstract IRecordType<?> getRecordType();
}
