package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.nbt.CompoundNBT;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface IMKEntityEntitlements extends IMKSerializable<CompoundNBT> {

    boolean hasEntitlement(MKEntitlement entitlement);

    void addEntitlement(EntitlementInstance instance, boolean doBroadcast);

    void removeEntitlement(EntitlementInstance instance);

    void removeEntitlementByUUID(UUID id);

    int getEntitlementLevel(MKEntitlement entitlement);

    void addEntitlementSubscriber(BiConsumer<MKEntitlement, IMKEntityEntitlements> entitlementConsumer);

    void addLoadedCallback(Consumer<IMKEntityEntitlements> loadedConsumer);
}
