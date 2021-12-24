package com.chaosbuffalo.mkcore.core.editor;

import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleAnimationEditorSyncPacket;
import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;

public class ParticleEditorSyncComponent implements ISyncObject {
    private final String name;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;
    private ParticleAnimation animation;
    private boolean dirty;
    private int currentFrame;

    public ParticleEditorSyncComponent(String name){
        this.name = name;
        this.currentFrame = -1;
        this.animation = null;
        this.dirty = false;
    }

    public ParticleAnimation getAnimation() {
        return animation;
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void update(ParticleAnimation animation, int currentKeyFrame, boolean sync){
        this.currentFrame = currentKeyFrame;
        setAnimationAndSpawn(animation,false);
        if (sync){
            PacketHandler.sendMessageToServer(new ParticleAnimationEditorSyncPacket(animation, currentKeyFrame));
        }
    }

    public void setAnimationAndSpawn(ParticleAnimation animation, boolean flagDirty){
        this.animation = animation;
        if (flagDirty){
            markDirty();
        }
    }

    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        if (tag.contains(name)){
            CompoundNBT syncTag = tag.getCompound(name);
            if (syncTag.contains("animation")){
                this.animation = ParticleAnimation.deserializeFromDynamic(
                        ParticleAnimationManager.RAW_EFFECT,
                        new Dynamic<>(NBTDynamicOps.INSTANCE, syncTag.getCompound("animation")));
            } else {
                this.animation = null;
            }
        }
    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {
        if (isDirty()){
            serializeFull(tag);
        }
    }

    @Override
    public void serializeFull(CompoundNBT tag) {
        CompoundNBT syncTag = new CompoundNBT();
        if (animation != null){
            syncTag.put("animation", animation.serialize(NBTDynamicOps.INSTANCE));
        }
        syncTag.putInt("currentKeyFrame", currentFrame);
        tag.put(name, syncTag);
        dirty = false;
    }
}
