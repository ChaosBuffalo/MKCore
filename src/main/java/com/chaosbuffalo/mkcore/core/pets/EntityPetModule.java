package com.chaosbuffalo.mkcore.core.pets;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EntityPetModule {
    protected final IMKEntityData entityData;
    protected boolean isPet;
    @Nullable
    protected LivingEntity owner;

    protected Map<ResourceLocation, MKPet<?>> pets;

    public EntityPetModule(IMKEntityData entityData) {
        this.pets = new HashMap<>();
        this.entityData = entityData;
        this.isPet = false;
        this.owner = null;
    }

    public void addPet(MKPet<?> pet) {
        if (pet.isActive()) {
            MKCore.getEntityData(pet.getEntity()).ifPresent(x -> x.getPets().setOwner(entityData.getEntity()));
            pets.put(pet.getName(), pet);
        } else {
            MKCore.LOGGER.debug("Tried to add invalid pet {} to {}", pet.getName(), entityData.getEntity());
        }

    }

    public void addThreatToPets(LivingEntity source, float threatValue) {
        pets.values().forEach(x -> x.addThreat(source, threatValue));
    }

    public void tick() {
        pets.values().stream().filter(MKPet::tick).forEach(this::removePet);
    }

    public void removePet(MKPet<?> pet){
        pets.remove(pet.getName());
    }

    public boolean hasPet() {
        return pets.values().stream().anyMatch(MKPet::isActive);
    }

    public boolean isPetActive(ResourceLocation name) {
        return pets.containsKey(name) && pets.get(name).isActive();
    }

    public Optional<MKPet<?>> getPet(ResourceLocation name){
        return Optional.ofNullable(pets.get(name));
    }

    public boolean isPet() {
        return isPet;
    }

    public void setOwner(LivingEntity owner) {
        isPet = true;
        this.owner = owner;
    }

    @Nullable
    public LivingEntity getOwner() {
        return owner;
    }
}
