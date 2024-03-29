package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundNBT;

public class SyncBool implements ISyncObject {
    private final String name;
    private boolean value;
    private boolean dirty;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncBool(String name, boolean value) {
        this.name = name;
        set(value);
    }

    public void set(boolean value) {
        this.value = value;
        this.dirty = true;
        parentNotifier.notifyUpdate(this);
    }

    public boolean get() {
        return value;
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        if (tag.contains(name)) {
            this.value = tag.getBoolean(name);
        }
    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {
        if (dirty) {
            serializeFull(tag);
            dirty = false;
        }
    }

    @Override
    public void serializeFull(CompoundNBT tag) {
        tag.putBoolean(name, value);
        dirty = false;
    }
}
