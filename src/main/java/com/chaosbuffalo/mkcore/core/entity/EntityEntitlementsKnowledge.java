package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.IMKEntityEntitlements;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class EntityEntitlementsKnowledge implements IMKEntityEntitlements {
    protected final IMKEntityData entityData;
    protected final Map<UUID, EntitlementInstance> entitlements = new HashMap<>();
    protected final List<BiConsumer<EntitlementInstance, IMKEntityEntitlements>> changeCallbacks = new ArrayList<>();
    protected final List<Consumer<IMKEntityEntitlements>> loadedCallbacks = new ArrayList<>();

    public EntityEntitlementsKnowledge(IMKEntityData entityData) {
        this.entityData = entityData;
    }

    @Override
    public boolean hasEntitlement(MKEntitlement entitlement) {
        return getEntitlementLevel(entitlement) > 0;
    }

    @Override
    public void addEntitlement(EntitlementInstance instance) {
        addEntitlementInternal(instance, true);
    }

    protected void addEntitlementInternal(EntitlementInstance instance, boolean doBroadcast) {
        if (instance.isValid() && !entitlements.containsKey(instance.getUUID())) {
            entitlements.put(instance.getUUID(), instance);
            if (doBroadcast) {
                broadcastChange(instance);
            }
        } else {
            MKCore.LOGGER.error("Trying to add invalid entitlement or already added entitlement: {}", instance);
        }
    }

    public Stream<EntitlementInstance> getInstanceStream() {
        return entitlements.values().stream();
    }

    @Override
    public void removeEntitlement(EntitlementInstance instance) {
        if (instance.isValid()) {
            EntitlementInstance existing = entitlements.remove(instance.getUUID());
            if (existing != null) {
                broadcastChange(instance);
            }
        } else {
            MKCore.LOGGER.error("Trying to remove entitlement instance will null id");
        }
    }

    protected void clearEntitlements() {
        entitlements.clear();
    }

    @Override
    public void removeEntitlementByUUID(UUID id) {
        if (entitlements.containsKey(id)) {
            EntitlementInstance instance = entitlements.get(id);
            removeEntitlement(instance);
        } else {
            MKCore.LOGGER.error("Trying to remove entitlement with id {} but it doesn't exist", id);
        }
    }

    @Override
    public int getEntitlementLevel(MKEntitlement entitlement) {
        return (int) getInstanceStream().filter(instance -> instance.getEntitlement() == entitlement).count();
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        ListNBT entitlementsTag = new ListNBT();
        for (EntitlementInstance instance : entitlements.values()) {
            entitlementsTag.add(instance.serializeDynamic(NBTDynamicOps.INSTANCE));
        }
        tag.put("entitlements", entitlementsTag);
        return tag;
    }

    @Override
    public boolean deserialize(CompoundNBT tag) {
        clearEntitlements();
        ListNBT entitlementsTag = tag.getList("entitlements", Constants.NBT.TAG_COMPOUND);
        for (INBT entNbt : entitlementsTag) {
            EntitlementInstance newEnt = new EntitlementInstance(new Dynamic<>(NBTDynamicOps.INSTANCE, entNbt));
            if (newEnt.isValid()) {
                addEntitlementInternal(newEnt, false);
            }
        }
        return true;
    }

    protected void broadcastChange(EntitlementInstance instance) {
        changeCallbacks.forEach(x -> x.accept(instance, this));
    }

    public void broadcastLoaded() {
        loadedCallbacks.forEach(x -> x.accept(this));
    }

    @Override
    public void addUpdatedCallback(BiConsumer<EntitlementInstance, IMKEntityEntitlements> entitlementConsumer) {
        changeCallbacks.add(entitlementConsumer);
    }

    @Override
    public void addLoadedCallback(Consumer<IMKEntityEntitlements> loadedConsumer) {
        loadedCallbacks.add(loadedConsumer);
    }
}
