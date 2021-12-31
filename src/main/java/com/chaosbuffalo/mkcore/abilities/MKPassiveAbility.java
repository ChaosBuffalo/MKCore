package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.abilities.description.AbilityDescriptions;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectInstance;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Consumer;

public abstract class MKPassiveAbility extends MKAbility implements IMKPassiveAbility {
    public MKPassiveAbility(ResourceLocation abilityId) {
        super(abilityId);
    }

    @Override
    public AbilityType getType() {
        return AbilityType.Passive;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.SELF;
    }

    public abstract MKEffect getPassiveEffect();

    @Override
    public void buildDescription(IMKEntityData entityData, Consumer<ITextComponent> consumer) {
        consumer.accept(new TranslationTextComponent("mkcore.ability.description.passive"));
        consumer.accept(getTargetContextLocalization());
        consumer.accept(getAbilityDescription(entityData));
        AbilityDescriptions.getEffectModifiers(getPassiveEffect(), entityData, false).forEach(consumer);
    }

    @Override
    public void executeWithContext(IMKEntityData entityData, AbilityContext context, MKAbilityInfo abilityInfo) {
        // TODO: see if this isEffectActive is needed in practice
        if (!entityData.getEffects().isEffectActive(getPassiveEffect())) {
            MKEffectInstance effect = getPassiveEffect().createInstance(entityData.getEntity().getUniqueID());
            effect.temporary(); // Abilities slotted to the passive group are re-executed when joining the world
            entityData.getEffects().addEffect(effect.infinite());
        }
    }
}
