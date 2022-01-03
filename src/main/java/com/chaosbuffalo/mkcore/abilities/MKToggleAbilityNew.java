package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.abilities.description.AbilityDescriptions;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Consumer;

public abstract class MKToggleAbilityNew extends MKToggleAbilityBase {

    public MKToggleAbilityNew(ResourceLocation abilityId) {
        super(abilityId);
    }

    public MKToggleAbilityNew(String namespace, String path) {
        super(namespace, path);
    }

    public abstract MKEffect getToggleEffect();

    @Override
    public boolean isEffectActive(IMKEntityData targetData) {
        return targetData.getEffects().isEffectActive(getToggleEffect());
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
            casterData.getEffects().removeEffect(getToggleEffect());
        }
    }
}
