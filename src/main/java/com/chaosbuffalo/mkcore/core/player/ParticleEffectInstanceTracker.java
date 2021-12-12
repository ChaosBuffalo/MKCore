package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.ParticleEffectInstance;
import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.mojang.serialization.Dynamic;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ParticleEffectInstanceTracker implements ISyncObject {

    protected List<ParticleEffectInstance> particleInstances;
    protected final MKPlayerData playerData;


    public ParticleEffectInstanceTracker(MKPlayerData playerData){
        particleInstances = new ArrayList<>();
        this.playerData = playerData;
    }

    public MKPlayerData getPlayerData() {
        return playerData;
    }

    public List<ParticleEffectInstance> getParticleInstances() {
        return particleInstances;
    }

    public boolean addParticleInstance(ParticleEffectInstance instance){
        Optional<ParticleEffectInstance> alreadyExists = particleInstances.stream().filter(
                x -> x.getInstanceUUID().equals(instance.getInstanceUUID())).findAny();
        if (alreadyExists.isPresent()){
            MKCore.LOGGER.error("Tried to add same particle instance twice {} to player {}", instance, playerData.getEntity());
            return false;
        } else {
            particleInstances.add(instance);
            return true;
        }
    }

    public void removeParticleInstance(UUID uuid){
        this.particleInstances = particleInstances.stream().filter(x -> !x.getInstanceUUID().equals(uuid))
                .collect(Collectors.toList());
    }


    @Override
    public void setNotifier(ISyncNotifier notifier) {

    }

    public void clearParticleEffects(){
        for (ParticleEffectInstance inst : particleInstances){
            removeParticleInstance(inst.getInstanceUUID());
        }
    }

    @Override
    public boolean isDirty() {
        return false;
    }


    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        if (tag.contains("effectInstances")){
            particleInstances.clear();
            ListNBT effectsNbt = tag.getList("effectInstances", Constants.NBT.TAG_COMPOUND);
            for (INBT effNbt : effectsNbt){
                Dynamic<?> dyn = new Dynamic<>(NBTDynamicOps.INSTANCE, effNbt);
                ResourceLocation type = ParticleEffectInstance.getType(dyn);
                ParticleEffectInstance inst = ParticleAnimationManager.getEffectInstance(type);
                if (inst != null){
                    inst.deserialize(dyn);
                    addParticleInstance(inst);
                }
            }
        }
        if (tag.contains("effectInstancesAdd")){
            ListNBT effectsNbt = tag.getList("effectInstancesAdd", Constants.NBT.TAG_COMPOUND);
            for (INBT effNbt : effectsNbt){
                Dynamic<?> dyn = new Dynamic<>(NBTDynamicOps.INSTANCE, effNbt);
                ResourceLocation type = ParticleEffectInstance.getType(dyn);
                ParticleEffectInstance inst = ParticleAnimationManager.getEffectInstance(type);
                if (inst != null){
                    inst.deserialize(dyn);
                    addParticleInstance(inst);
                }
            }
        }
        if (tag.contains("effectInstancesRemove")){
            ListNBT toRemoveNbt = tag.getList("effectInstancesRemove", Constants.NBT.TAG_STRING);
            for (INBT inbt : toRemoveNbt){
                UUID id = UUID.fromString(inbt.getString());
                removeParticleInstance(id);
            }
        }

    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {

    }

    @Override
    public void serializeFull(CompoundNBT tag) {

    }


    static class ParticleEffectInstanceTrackerServer extends ParticleEffectInstanceTracker {
        private final List<UUID> toRemoveDirty;
        private final List<ParticleEffectInstance> toAddDirty;
        private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

        public ParticleEffectInstanceTrackerServer(MKPlayerData playerData) {
            super(playerData);
            toRemoveDirty = new ArrayList<>();
            toAddDirty = new ArrayList<>();
        }

        @Override
        public boolean addParticleInstance(ParticleEffectInstance instance) {
            boolean wasAdded = super.addParticleInstance(instance);
            if (wasAdded){
                toAddDirty.add(instance);
                parentNotifier.notifyUpdate(this);
            }
            return wasAdded;
        }

        @Override
        public void removeParticleInstance(UUID uuid) {
            super.removeParticleInstance(uuid);
            toRemoveDirty.add(uuid);
            parentNotifier.notifyUpdate(this);
        }

        @Override
        public boolean isDirty() {
            return !toRemoveDirty.isEmpty() || !toAddDirty.isEmpty();
        }

        @Override
        public void serializeFull(CompoundNBT tag) {
            ListNBT effectsNbt = new ListNBT();
            for (ParticleEffectInstance instance : particleInstances){
                effectsNbt.add(instance.serialize(NBTDynamicOps.INSTANCE));
            }
            tag.put("effectInstances", effectsNbt);
            toRemoveDirty.clear();
            toAddDirty.clear();
        }

        @Override
        public void serializeUpdate(CompoundNBT tag) {
            ListNBT toRemove = new ListNBT();
            for (UUID id : toRemoveDirty){
                toRemove.add(StringNBT.valueOf(id.toString()));
            }
            tag.put("effectInstancesRemove", toRemove);
            toRemoveDirty.clear();
            ListNBT toAdd = new ListNBT();
            for (ParticleEffectInstance instance : toAddDirty){
                toAdd.add(instance.serialize(NBTDynamicOps.INSTANCE));
            }
            tag.put("effectInstancesAdd", toAdd);
            toAddDirty.clear();
        }

        @Override
        public void setNotifier(ISyncNotifier notifier) {
            parentNotifier = notifier;
        }
    }

    public static ParticleEffectInstanceTracker getTracker(MKPlayerData playerData) {
        if (playerData.getEntity() instanceof ServerPlayerEntity) {
            return new ParticleEffectInstanceTrackerServer(playerData);
        } else {
            return new ParticleEffectInstanceTracker(playerData);
        }
    }
}
