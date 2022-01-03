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
    protected int maxStacks = -1;

    public boolean isReady(IMKEntityData targetData, MKActiveEffect instance) {
        return instance.getBehaviour().isReady();
    }

    protected int clampMaxStacks(int newValue) {
        if (maxStacks == -1)
            return newValue;
        return Math.min(maxStacks, newValue);
    }


    public void combine(MKActiveEffect existing, MKActiveEffect otherInstance) {
        MKCore.LOGGER.debug("MKEffectState.combine {} + {}", existing, otherInstance);
        if (otherInstance.getDuration() > existing.getDuration()) {
            existing.setDuration(otherInstance.getDuration());
        }
        int newStacks = clampMaxStacks(existing.getStackCount() + otherInstance.getStackCount());
        existing.setStackCount(newStacks);
        MKCore.LOGGER.debug("MKEffectState.combine result {}", existing);
    }

    public abstract boolean performEffect(IMKEntityData targetData, MKActiveEffect instance);

    protected boolean isEffectSource(Entity entity, MKActiveEffect activeEffect) {
        return entity.getUniqueID().equals(activeEffect.getSourceId());
    }

    public void setMaxStacks(int max) {
        this.maxStacks = max;
    }

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

    public boolean validateOnApply(IMKEntityData targetData, MKActiveEffect activeEffect) {
        return true;
    }

    public boolean validateOnLoad(MKActiveEffect activeEffect) {
        return true;
    }

    public void serializeStorage(CompoundNBT stateTag) {
        if (maxStacks != -1) {
            stateTag.putInt("maxStacks", maxStacks);
        }
    }

    public void deserializeStorage(CompoundNBT tag) {
        if (tag.contains("maxStacks")) {
            maxStacks = tag.getInt("maxStacks");
        }
    }
}
