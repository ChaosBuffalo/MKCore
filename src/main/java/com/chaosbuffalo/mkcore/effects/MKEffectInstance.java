package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

public abstract class MKEffectInstance {

    protected final MKEffect effect;
    protected final UUID sourceId;
    protected final MKEffectBehaviour behaviour;
    private int baseDuration;
    private int baseStackCount;
    private boolean temporary;

    public MKEffectInstance(MKEffect effect, UUID sourceId) {
        this.effect = Objects.requireNonNull(effect);
        this.sourceId = Objects.requireNonNull(sourceId);
        baseStackCount = 1;
        baseDuration = 0;
        behaviour = new MKEffectBehaviour();
    }

    public MKEffect getEffect() {
        return effect;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public int getInitialDuration() {
        return baseDuration;
    }

    public int getInitialStackCount() {
        return baseStackCount;
    }

    public MKEffectInstance amplify(int level) {
        baseStackCount += level;
        return this;
    }

    public MKEffectInstance timed(int duration) {
        baseDuration = duration;
        return this;
    }

    public MKEffectInstance periodic(int period) {
        behaviour.setPeriod(period);
        return this;
    }

    public MKEffectInstance infinite() {
        // Give it a duration so MKActiveEffect.hasDuration returns true for infinite effects
        timed(1);
        behaviour.setInfinite(true);
        return this;
    }

    public MKEffectInstance temporary() {
        this.temporary = true;
        return this;
    }

    public boolean isTemporary() {
        return temporary;
    }

    protected boolean tryPerformEffect(IMKEntityData entityData, MKActiveEffect instance) {
        if (!entityData.getEntity().getEntityWorld().isRemote()) {
            if (isReady(entityData, instance)) {
                return performEffect(entityData, instance);
            }
        }
        return true;
    }

    public boolean isReady(IMKEntityData entityData, MKActiveEffect instance) {
        return instance.getBehaviour().isReady(instance);
    }

    public void combine(MKActiveEffect existing, MKActiveEffect otherInstance) {
        MKCore.LOGGER.info("MKEffectInstance.combine {} + {}", this, otherInstance);
        if (otherInstance.getDuration() > existing.getDuration()) {
            existing.setDuration(otherInstance.getDuration());
        }
        existing.modifyStackCount(otherInstance.getStackCount());
    }

    protected Entity findEntity(Entity entity, UUID entityId, World world) {
        if (entity != null)
            return entity;
        if (!world.isRemote()) {
            return ((ServerWorld) world).getEntityByUuid(entityId);
        }
        return null;
    }

    protected Entity findEntity(Entity entity, UUID entityId, IMKEntityData targetData) {
        return findEntity(entity, entityId, targetData.getEntity().getEntityWorld());
    }

    public abstract boolean performEffect(IMKEntityData entityData, MKActiveEffect instance);

    public MKActiveEffect createApplication() {
        return new MKActiveEffect(this, behaviour);
    }

    public CompoundNBT serializeStorage() {
        return serializeState();
    }

    public CompoundNBT serializeClient() {
        return new CompoundNBT();
    }

    private void deserialize(CompoundNBT tag) {
        deserializeState(tag);
    }

    public void deserializeClient(CompoundNBT tag) {
    }

    protected CompoundNBT serializeState() {
        return serializeClient();
    }

    public void deserializeState(CompoundNBT stateTag) {
        deserializeClient(stateTag);
    }

    public static MKEffectInstance deserializeClient(ResourceLocation effectId, UUID sourceId, CompoundNBT tag) {
        return deserializeCommon(effectId, sourceId, tag, MKEffectInstance::deserializeClient);
    }

    private static MKEffectInstance deserializeCommon(ResourceLocation effectId, UUID sourceId, CompoundNBT tag,
                                                      BiConsumer<MKEffectInstance, CompoundNBT> callback) {
        MKEffect effect = MKCoreRegistry.EFFECTS.getValue(effectId);
        if (effect == null) {
            return null;
        }

        MKEffectInstance instance = effect.createInstance(sourceId);
        callback.accept(instance, tag);
        return instance;
    }

    public static MKEffectInstance deserializeStorage(ResourceLocation effectId, UUID sourceId, CompoundNBT tag) {
        return deserializeCommon(effectId, sourceId, tag, MKEffectInstance::deserialize);
    }

    @Override
    public String toString() {
        return "MKEffectInstance{" +
                "effect=" + effect +
                ", sourceId=" + sourceId +
                ", baseDuration=" + baseDuration +
                ", baseStackCount=" + baseStackCount +
                '}';
    }
}
