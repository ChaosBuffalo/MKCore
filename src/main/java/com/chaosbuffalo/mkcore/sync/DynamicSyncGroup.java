package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundNBT;

public abstract class DynamicSyncGroup extends SyncGroup {

    protected abstract void onKey(String key);

    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        tag.keySet().forEach(this::onKey);
        super.deserializeUpdate(tag);
    }
}
