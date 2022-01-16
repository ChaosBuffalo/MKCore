package com.chaosbuffalo.mkcore.effects.instant;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ScalingValueEffectState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

public class MKAbilityDamageEffect extends MKEffect {
    public static final MKAbilityDamageEffect INSTANCE = new MKAbilityDamageEffect();


    public MKAbilityDamageEffect() {
        super(EffectType.HARMFUL);
        setRegistryName(MKCore.makeRL("effect.ability_damage"));
    }

    public static MKEffectBuilder<State> from(LivingEntity source, MKDamageType damageType, float baseDamage,
                                              float scaling, float modifierScaling) {
        return INSTANCE.builder(source).state(s -> {
            s.setDamageType(damageType);
            s.setScalingParameters(baseDamage, scaling, modifierScaling);
        });
    }

    @Override
    public State makeState() {
        return new State();
    }

    @Override
    public MKEffectBuilder<State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    @Override
    public MKEffectBuilder<State> builder(LivingEntity sourceEntity) {
        return new MKEffectBuilder<>(this, sourceEntity, this::makeState);
    }

    public static class State extends ScalingValueEffectState {

        @Override
        public boolean validateOnLoad(MKActiveEffect activeEffect) {
            return damageType != null;
        }

        @Override
        public boolean validateOnApply(IMKEntityData targetData, MKActiveEffect activeEffect) {
            return damageType != null;
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {
            DamageSource damage = MKDamageSource.causeAbilityDamage(damageType, activeEffect.getAbilityId(),
                    activeEffect.getDirectEntity(), activeEffect.getSourceEntity(), getModifierScale());

            float value = getScaledValue(activeEffect.getStackCount(), activeEffect.getSkillLevel());
            targetData.getEntity().attackEntityFrom(damage, value);
            return true;
        }
    }

    @SuppressWarnings("unused")
    @Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    private static class RegisterMe {
        @SubscribeEvent
        public static void register(RegistryEvent.Register<MKEffect> event) {
            event.getRegistry().register(INSTANCE);
        }
    }
}
