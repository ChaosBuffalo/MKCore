package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.nbt.CompoundNBT;

public class MKEffectBehaviour {

    private int duration;
    private int period;
    private boolean infinite;
    private boolean temporary;

    public MKEffectBehaviour() {

    }

    public MKEffectBehaviour(MKEffectBehaviour template) {
        duration = template.duration;
        period = template.period;
        infinite = template.infinite;
        temporary = template.temporary;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isTimed() {
        return isInfinite() || duration > 0;
    }

    public void modifyDuration(int delta) {
        duration += delta;
    }

    public boolean isExpired() {
        return canExpire() && duration <= 0;
    }

    private boolean canExpire() {
        return !isInfinite();
    }

    public void setTemporary() {
        this.temporary = true;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setInfinite(boolean infinite) {
        this.infinite = infinite;
    }

    public boolean isInfinite() {
        return infinite;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getPeriod() {
        return period;
    }

    public MKEffectTickAction behaviourTick(IMKEntityData entityData, MKActiveEffect activeEffect) {
        MKEffectTickAction action;
        if (isInfinite()) {
            action = infiniteTick(entityData, activeEffect);
        } else {
            action = timedTick(entityData, activeEffect);
        }

        return action;
    }

    public boolean isReady() {
        if (isExpired())
            return false;
        if (getPeriod() > 0) {
            return getDuration() % getPeriod() == 0;
        } else {
            return true;
        }
    }

    private MKEffectTickAction timedTick(IMKEntityData entityData, MKActiveEffect instance) {
        boolean keepTicking = false;
        if (getDuration() > 0) {
            keepTicking = tryPerformEffect(entityData, instance);

            duration--;
        }

        if (isExpired() || !keepTicking) {
            return MKEffectTickAction.Remove;
        }
        return MKEffectTickAction.NoUpdate;
    }

    private MKEffectTickAction infiniteTick(IMKEntityData entityData, MKActiveEffect instance) {

        boolean keepTicking = tryPerformEffect(entityData, instance);
        if (!keepTicking) {
            return MKEffectTickAction.Remove;
        }

        duration++;

        return MKEffectTickAction.NoUpdate;
    }

    private boolean tryPerformEffect(IMKEntityData entityData, MKActiveEffect instance) {
        if (entityData.isServerSide()) {
            if (instance.getState().isReady(entityData, instance)) {
                return instance.getState().performEffect(entityData, instance);
            }
        }
        return true;
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        if (duration > 0) {
            tag.putInt("duration", duration);
        }
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
        duration = tag.getInt("duration");
        period = tag.getInt("period");
        infinite = tag.getBoolean("infinite");
    }

    @Override
    public String toString() {
        return "MKEffectBehaviour{" +
                "duration=" + duration +
                ", period=" + period +
                ", infinite=" + infinite +
                '}';
    }
}
