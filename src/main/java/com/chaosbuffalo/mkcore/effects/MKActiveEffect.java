package com.chaosbuffalo.mkcore.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class MKActiveEffect {

    protected final MKEffectInstance effect;
    protected final Lazy<EffectInstance> displayEffectInstance = Lazy.of(() -> createDisplayEffectInstance(this));
    protected int duration;
    protected int stackCount;
    protected MKEffectBehaviour behaviour;

    protected MKActiveEffect(MKEffectInstance effectInstance, MKEffectBehaviour behaviour) {
        effect = Objects.requireNonNull(effectInstance);
        this.behaviour = Objects.requireNonNull(behaviour);
        duration = effectInstance.getInitialDuration();
        stackCount = effectInstance.getInitialStackCount();
    }

    public MKEffectInstance getInstance() {
        return effect;
    }

    public MKEffect getEffect() {
        return effect.getEffect();
    }

    public MKEffectBehaviour getBehaviour() {
        return behaviour;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void modifyDuration(int delta) {
        duration += delta;
    }

    public boolean hasDuration() {
        return duration > 0;
    }

    public int getStackCount() {
        return stackCount;
    }

    public void setStackCount(int count) {
        stackCount = count;
    }

    public void modifyStackCount(int delta) {
        stackCount += delta;
    }

    public boolean isExpired() {
        return duration <= 0;
    }

    public CompoundNBT serializeClient() {
        CompoundNBT stateTag = new CompoundNBT();
        stateTag.put("effect", effect.serializeClient());
        stateTag.put("state", serializeState());
        return stateTag;
    }

    public static MKActiveEffect deserializeClient(ResourceLocation effectId, UUID sourceId, CompoundNBT tag) {
        MKEffectInstance instance = MKEffectInstance.deserializeClient(effectId, sourceId, tag.getCompound("effect"));
        if (instance == null)
            return null;

        MKActiveEffect active = instance.createApplication();
        active.deserializeState(tag.getCompound("state"));
        return active;
    }

    public CompoundNBT serializeState() {
        CompoundNBT stateTag = new CompoundNBT();
        stateTag.put("behaviour", behaviour.serialize());
        stateTag.putInt("duration", getDuration());
        stateTag.putInt("amplifier", getStackCount());
        return stateTag;
    }

    public void deserializeState(CompoundNBT stateTag) {
        duration = stateTag.getInt("duration");
        stackCount = stateTag.getInt("amplifier");
        behaviour = MKEffectBehaviour.deserialize(stateTag.getCompound("behaviour"));
    }


    public CompoundNBT serializeStorage() {
        CompoundNBT tag = new CompoundNBT();
        serializeId(tag);
        tag.put("effect", effect.serializeStorage());
        tag.put("state", serializeState());
        return tag;
    }

    public static MKActiveEffect deserializeStorage(UUID sourceId, CompoundNBT tag) {
        ResourceLocation effectId = deserializeId(tag);
        if (effectId == null)
            return null;

        MKEffectInstance instance = MKEffectInstance.deserializeStorage(effectId, sourceId, tag.getCompound("effect"));
        if (instance == null)
            return null;

        MKActiveEffect active = instance.createApplication();
        active.deserializeState(tag.getCompound("state"));
        return active;
    }

    private void serializeId(CompoundNBT nbt) {
        nbt.putString("effectId", effect.getEffect().getId().toString());
    }

    @Nullable
    private static ResourceLocation deserializeId(CompoundNBT tag) {
        return ResourceLocation.tryCreate(tag.getString("effectId"));
    }

    @Override
    public String toString() {
        return "MKActiveEffect{" +
                "effect=" + effect +
                ", duration=" + duration +
                ", stackCount=" + stackCount +
                ", behaviour=" + behaviour +
                '}';
    }

    // Only called for the local player on the client
    public EffectInstance getClientDisplayEffectInstance() {
        return displayEffectInstance.get();
    }

    private static EffectInstance createDisplayEffectInstance(MKActiveEffect effectInstance) {
        return new EffectInstance(effectInstance.getEffect().getVanillaWrapper()) {

            @Override
            public boolean getIsPotionDurationMax() {
                return effectInstance.getBehaviour().isInfinite();
            }

            @Override
            public int getDuration() {
                // Even though we override getIsPotionDurationMax we still need a large number so the
                // in-game GUI doesn't flash continuously
                if (getIsPotionDurationMax())
                    return Integer.MAX_VALUE;
                return effectInstance.getDuration();
            }

            @Override
            public int getAmplifier() {
                // "Amplifier" in vanilla is the number of ranks above 1
                return Math.max(0, effectInstance.getStackCount() - 1);
            }

            @Override
            public void renderHUDEffect(AbstractGui gui, MatrixStack mStack, int x, int y, float z, float alpha) {
                super.renderHUDEffect(gui, mStack, x, y, z, alpha);
            }
        };
    }

}
