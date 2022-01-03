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
    public boolean isEffectActive(IMKEntityData targetData) {
        return targetData.getEntity().isPotionActive(getToggleEffect());
    }

    @Override
    public void buildDescription(IMKEntityData casterData, Consumer<ITextComponent> consumer) {
        super.buildDescription(casterData, consumer);
        AbilityDescriptions.getEffectModifiers(getToggleEffect(), casterData, false).forEach(consumer);
    }

    @Override
    public void removeEffect(LivingEntity castingEntity, IMKEntityData casterData) {
        casterData.getAbilityExecutor().clearToggleGroupAbility(getToggleGroupId());
        if (isEffectActive(casterData)) {
            castingEntity.removePotionEffect(getToggleEffect());
        }
    }
}
