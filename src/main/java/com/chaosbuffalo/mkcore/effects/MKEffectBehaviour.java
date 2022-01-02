package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.nbt.CompoundNBT;

public class MKEffectBehaviour {

    private int period;
    private boolean infinite;

    public MKEffectTickAction behaviourTick(IMKEntityData entityData, MKActiveEffect activeEffect) {
        if (infinite) {
            return infiniteTick(entityData, activeEffect);
        } else {
            return timedTick(entityData, activeEffect);
        }
    }

    public boolean isReady(MKActiveEffect activeEffect) {
        if (activeEffect.isExpired())
            return false;
        if (period > 0) {
            return activeEffect.getDuration() % period == 0;
        } else {
            return true;
        }
    }

    public boolean isInfinite() {
        return infinite;
    }

    public void setInfinite(boolean infinite) {
        this.infinite = infinite;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    private MKEffectTickAction timedTick(IMKEntityData entityData, MKActiveEffect instance) {
        boolean keepTicking = false;
        if (instance.getDuration() > 0) {
            keepTicking = instance.getInstance().tryPerformEffect(entityData, instance);

            instance.modifyDuration(-1);
        }

        if (instance.isExpired() || !keepTicking) {
            return MKEffectTickAction.Remove;
        }
        return MKEffectTickAction.NoUpdate;
    }

    private MKEffectTickAction infiniteTick(IMKEntityData entityData, MKActiveEffect instance) {

        boolean keepTicking = instance.getInstance().tryPerformEffect(entityData, instance);
        if (!keepTicking) {
            return MKEffectTickAction.Remove;
        }

        instance.modifyDuration(1);

        return MKEffectTickAction.NoUpdate;
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        if (period > 0) {
            tag.putInt("period", period);
        }
        if (infinite) {
            tag.putBoolean("infinite", true);
        }
        return tag;
    }

    public static MKEffectBehaviour deserialize(CompoundNBT tag) {
        MKEffectBehaviour behaviour = new MKEffectBehaviour();
        behaviour.deserializeState(tag);
        return behaviour;
    }

    protected void deserializeState(CompoundNBT tag) {
        period = tag.getInt("period");
        infinite = tag.getBoolean("infinite");
    }

    @Override
    public String toString() {
        return "MKEffectBehaviour{" +
                "period=" + period +
                ", infinite=" + infinite +
                '}';
    }
}
