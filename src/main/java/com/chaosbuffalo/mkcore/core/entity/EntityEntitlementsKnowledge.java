package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.IMKEntityEntitlements;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EntityEntitlementsKnowledge implements IMKEntityEntitlements {
    protected final IMKEntityData entityData;

    private final Map<UUID, EntitlementInstance> entitlements = new HashMap<>();
    private final Map<MKEntitlement, List<EntitlementInstance>> entitlementCounts = new HashMap<>();
    private final List<BiConsumer<MKEntitlement, IMKEntityEntitlements>> changeCallbacks = new ArrayList<>();
    private final List<Consumer<IMKEntityEntitlements>> loadedCallbacks = new ArrayList<>();

    public EntityEntitlementsKnowledge(IMKEntityData entityData) {
        this.entityData = entityData;
    }

    @Override
    public boolean hasEntitlement(MKEntitlement entitlement) {
        return entitlementCounts.containsKey(entitlement) && entitlementCounts.get(entitlement).size() > 0;
    }

    @Override
    public void addEntitlement(EntitlementInstance instance, boolean doBroadcast) {
        if (instance.isValid() && !entitlements.containsKey(instance.getUUID())){
            entitlements.put(instance.getUUID(), instance);
            entitlementCounts.computeIfAbsent(instance.getEntitlement(), (inst) -> new ArrayList<>()).add(instance);
            if (doBroadcast){
                broadcastChange(instance.getEntitlement());
            }

        } else {
            MKCore.LOGGER.error("Trying to add invalid entitlement or already added entitlement: {}", instance);
        }
    }

    @Override
    public void removeEntitlement(EntitlementInstance instance) {
        if (instance.getUUID() != null){
            removeEntitlementByUUID(instance.getUUID());
        } else {
            MKCore.LOGGER.error("Trying to remove entitlement instance will null id");
        }
    }

    protected void clearEntitlements(){
        entitlementCounts.clear();
        entitlements.clear();
    }

    @Override
    public void removeEntitlementByUUID(UUID id) {
        if (entitlements.containsKey(id)){
            EntitlementInstance instance = entitlements.get(id);
            entitlements.remove(instance.getUUID());
            entitlementCounts.computeIfPresent(instance.getEntitlement(),
                    (ent, instances) -> instances.stream()
                            .filter(inst -> inst.getUUID() != null && !inst.getUUID().equals(instance.getUUID()))
                            .collect(Collectors.toList()));
            broadcastChange(instance.getEntitlement());
        } else {
            MKCore.LOGGER.error("Trying to remove entitlement with id {} but it doesn't exist", id);
        }
    }

    @Override
    public int getEntitlementLevel(MKEntitlement entitlement) {
        return hasEntitlement(entitlement) ? Math.min(entitlementCounts.get(entitlement).size(), entitlement.getMaxEntitlements()) : 0;
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        ListNBT entitlementsTag = new ListNBT();
        for (EntitlementInstance instance : entitlements.values()){
            entitlementsTag.add(instance.serializeDynamic(NBTDynamicOps.INSTANCE));
        }
        tag.put("entitlements", entitlementsTag);
        return tag;
    }

    @Override
    public boolean deserialize(CompoundNBT tag) {
        clearEntitlements();
        ListNBT entitlementsTag = tag.getList("entitlements", Constants.NBT.TAG_COMPOUND);
        for (INBT entNbt : entitlementsTag){
            EntitlementInstance newEnt = new EntitlementInstance();
            newEnt.deserializeDynamic(new Dynamic<>(NBTDynamicOps.INSTANCE, entNbt));
            if (newEnt.isValid()){
                addEntitlement(newEnt, false);
            }
        }
        return true;
    }

    protected void broadcastChange(MKEntitlement entitlement){
        changeCallbacks.forEach(x -> x.accept(entitlement, this));
    }

    public void broadcastLoaded(){
        loadedCallbacks.forEach(x -> x.accept(this));
    }

    @Override
    public void addEntitlementSubscriber(BiConsumer<MKEntitlement, IMKEntityEntitlements> entitlementConsumer) {
        changeCallbacks.add(entitlementConsumer);
    }

    @Override
    public void addLoadedCallback(Consumer<IMKEntityEntitlements> loadedConsumer) {
        loadedCallbacks.add(loadedConsumer);
    }
}
