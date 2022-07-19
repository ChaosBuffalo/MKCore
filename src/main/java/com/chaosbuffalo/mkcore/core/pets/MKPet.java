package com.chaosbuffalo.mkcore.core.pets;


import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class MKPet<T extends LivingEntity & IMKPet> {

    @Nullable
    protected T entity;
    protected int duration;
    protected final ResourceLocation name;

    public MKPet(ResourceLocation name, T entity) {
        this.entity = entity;
        this.name = name;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public ResourceLocation getName() {
        return name;
    }

    public void addThreat(LivingEntity source, float threat){
        if (isActive()) {
            entity.addThreat(source, threat);
        }
    }

    @Nullable
    public T getEntity() {
        return entity;
    }

    public boolean tick() {
        if (isActive()) {
            duration--;
            if (duration < 0) {
                entity.remove();
                entity = null;
                return true;
            }
            return false;
        }
        return true;
    }

    public boolean isActive() {
        return entity != null && entity.isAlive();
    }
}
