package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.abilities.description.AbilityDescriptions;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.PassiveTalentEffect;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Consumer;

public abstract class PassiveTalentAbility extends MKAbility implements IMKPassiveAbility {
    public PassiveTalentAbility(ResourceLocation abilityId) {
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

    public abstract PassiveTalentEffect getPassiveEffect();

    public void buildDescription(IMKEntityData casterData, Consumer<ITextComponent> consumer) {
        consumer.accept(new TranslationTextComponent("mkcore.ability.description.passive"));
        consumer.accept(getTargetContextLocalization());
        consumer.accept(getAbilityDescription(casterData));
        AbilityDescriptions.getEffectModifiers(getPassiveEffect(), casterData, false).forEach(consumer);
    }

    @Override
    public void executeWithContext(IMKEntityData casterData, AbilityContext context, MKAbilityInfo abilityInfo) {
        LivingEntity entity = casterData.getEntity();
        if (entity instanceof PlayerEntity) {
            if (entity.getActivePotionEffect(getPassiveEffect()) == null) {
                entity.addPotionEffect(getPassiveEffect().createSelfCastEffectInstance(entity, 0));
            }
        }
    }
}
