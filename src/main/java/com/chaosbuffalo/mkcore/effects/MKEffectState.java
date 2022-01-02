package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class MKEffectState {

    public boolean isReady(IMKEntityData entityData, MKActiveEffect instance) {
        return instance.getBehaviour().isReady();
    }

    public void combine(MKActiveEffect existing, MKActiveEffect otherInstance) {
        MKCore.LOGGER.info("MKEffectHandler.combine {} + {}", this, otherInstance);
        if (otherInstance.getDuration() > existing.getDuration()) {
            existing.setDuration(otherInstance.getDuration());
        }
        existing.modifyStackCount(otherInstance.getStackCount());
    }

    public abstract boolean performEffect(IMKEntityData entityData, MKActiveEffect instance);

    @Nullable
    protected Entity findEntity(Entity entity, UUID entityId, World world) {
        if (entity != null)
            return entity;
        if (!world.isRemote()) {
            return ((ServerWorld) world).getEntityByUuid(entityId);
        }
        return null;
    }

    @Nullable
    protected Entity findEntity(Entity entity, UUID entityId, IMKEntityData targetData) {
        return findEntity(entity, entityId, targetData.getEntity().getEntityWorld());
    }


    public void serializeStorage(CompoundNBT stateTag) {

    }

    public void deserializeStorage(CompoundNBT tag) {

    }
}
