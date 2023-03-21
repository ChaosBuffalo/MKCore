package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.records.IRecordType;
import net.minecraft.resources.ResourceLocation;

public class SimpleEntitlement extends MKEntitlement{
    private final IRecordType<SimpleEntitlementHandler> recordType;

    public SimpleEntitlement(ResourceLocation name, int maxEntitlements) {
        super(name, maxEntitlements);
        recordType = playerData -> new SimpleEntitlementHandler();
    }

    @Override
    public IRecordType<?> getRecordType() {
        return recordType;
    }

    public static class SimpleEntitlementHandler extends EntitlementTypeHandler {

    }
}
