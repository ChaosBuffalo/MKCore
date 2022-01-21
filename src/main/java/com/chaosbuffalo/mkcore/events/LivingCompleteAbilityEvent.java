package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class LivingCompleteAbilityEvent extends LivingEvent {
    private final MKAbility ability;
    private final IMKEntityData entityData;

    public LivingCompleteAbilityEvent(LivingEntity entity, IMKEntityData entityData, MKAbility ability) {
        super(entity);
        this.ability = ability;
        this.entityData = entityData;
    }

    public MKAbility getAbility() {
        return ability;
    }

    public IMKEntityData getEntityData() {
        return entityData;
    }
}
