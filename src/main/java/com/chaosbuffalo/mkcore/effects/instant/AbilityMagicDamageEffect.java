package com.chaosbuffalo.mkcore.effects.instant;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

public class AbilityMagicDamageEffect extends MKEffect {
    public static final AbilityMagicDamageEffect INSTANCE = new AbilityMagicDamageEffect();


    public AbilityMagicDamageEffect() {
        super(EffectType.HARMFUL);
        setRegistryName(MKCore.makeRL("effect.ability_magic_damage"));
    }

    public static MKEffectBuilder<State> from(LivingEntity source, float baseDamage, float scaling, float modifierScaling) {
        return INSTANCE.builder(source).state(s -> s.setScalingParameters(baseDamage, scaling, modifierScaling));
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
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {

            DamageSource damage;
            if (activeEffect.getDirectEntity() != null) {
                damage = DamageSource.causeIndirectMagicDamage(activeEffect.getDirectEntity(), activeEffect.getSourceEntity());
            } else {
                damage = DamageSource.MAGIC;
            }

            float value = getScaledValue(activeEffect.getStackCount());
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
