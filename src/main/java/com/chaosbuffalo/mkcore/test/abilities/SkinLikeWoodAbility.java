package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.abilities.MKToggleAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.test.effects.SkinLikeWoodEffect;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.LivingEntity;

public class SkinLikeWoodAbility extends MKToggleAbility {
    public SkinLikeWoodAbility() {
        super();
        setCooldownSeconds(6);
        setManaCost(4);
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.SELF;
    }

    @Override
    public MKEffect getToggleEffect() {
        return SkinLikeWoodEffect.INSTANCE;
    }

    @Override
    public void applyEffect(LivingEntity castingEntity, IMKEntityData casterData) {
        super.applyEffect(castingEntity, casterData);

        MKEffectBuilder<?> instance = getToggleEffect().builder(castingEntity)
                .amplify(2)
                .infinite();
        casterData.getEffects().addEffect(instance);
    }
}
