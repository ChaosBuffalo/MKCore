package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntitlementInstance {

    @Nullable
    protected MKEntitlement entitlement;
    @Nullable
    protected UUID uuid;

    public EntitlementInstance(){
        this(null, null);
    }

    public EntitlementInstance(MKEntitlement entitlement, UUID uuid){
        this.entitlement = entitlement;
        this.uuid = uuid;
    }

    public <T> T serializeDynamic(DynamicOps<T> ops) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(ops.createString("entitlement"),
                entitlement != null && entitlement.getRegistryName() != null ?
                        ops.createString(entitlement.getRegistryName().toString())
                        : ops.createString(MKCoreRegistry.INVALID_ENTITLEMENT.toString()));
        if (uuid != null){
            builder.put(ops.createString("entitlementId"), ops.createString(uuid.toString()));
        }
        return ops.createMap(builder.build());
    }

    public <T> void deserializeDynamic(Dynamic<T> dynamic) {
        ResourceLocation loc = dynamic.get("entitlement").asString().map(ResourceLocation::new).result()
                .orElse(MKCoreRegistry.INVALID_ENTITLEMENT);
        this.entitlement = MKCoreRegistry.getEntitlement(loc);
        this.uuid = dynamic.get("entitlementId").asString().map(UUID::fromString).result().orElse(null);
    }

    @Nullable
    public UUID getUUID() {
        return uuid;
    }

    @Nullable
    public MKEntitlement getEntitlement() {
        return entitlement;
    }

    public boolean isValid(){
        return uuid != null && entitlement != null;
    }
}
