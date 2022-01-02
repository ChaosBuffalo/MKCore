package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.abilities.description.AbilityDescriptions;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Consumer;

public abstract class MKToggleAbility extends MKToggleAbilityBase {

    public MKToggleAbility(ResourceLocation abilityId) {
        super(abilityId);
    }

    public MKToggleAbility(String namespace, String path) {
        super(namespace, path);
    }

    public abstract Effect getToggleEffect();

    @Override
    public boolean isEffectActive(IMKEntityData entityData) {
        return entityData.getEntity().isPotionActive(getToggleEffect());
    }

    @Override
    public void buildDescription(IMKEntityData entityData, Consumer<ITextComponent> consumer) {
        super.buildDescription(entityData, consumer);
        AbilityDescriptions.getEffectModifiers(getToggleEffect(), entityData, false).forEach(consumer);
    }

    @Override
    public void removeEffect(LivingEntity entity, IMKEntityData entityData) {
        entityData.getAbilityExecutor().clearToggleGroupAbility(getToggleGroupId());
        if (isEffectActive(entityData)) {
            entity.removePotionEffect(getToggleEffect());
        }
    }
}
