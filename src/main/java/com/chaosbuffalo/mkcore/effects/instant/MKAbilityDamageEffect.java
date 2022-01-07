package com.chaosbuffalo.mkcore.effects.instant;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.effects.*;
import com.chaosbuffalo.mkcore.utils.MKNBTUtil;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKAbilityDamageEffect extends MKEffect {
    public static final MKAbilityDamageEffect INSTANCE = new MKAbilityDamageEffect();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKEffect> event) {
        event.getRegistry().register(INSTANCE);
    }


    public MKAbilityDamageEffect() {
        super(EffectType.HARMFUL);
        setRegistryName(MKCore.makeRL("effect.ability_damage"));
    }

    public static MKEffectBuilder<State> from(Entity source, MKDamageType damageType, float baseDamage, float scaling, float modifierScaling) {
        return INSTANCE.builder(source.getUniqueID()).state(s -> {
                    s.damageType = damageType;
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

    public static class State extends ScalingValueEffectState {
        // Serialized
        public MKDamageType damageType;

        // Non-serialized
        public Entity source;

        @Override
        public boolean validateOnLoad(MKActiveEffect activeEffect) {
            return damageType != null;
        }

        @Override
        public boolean validateOnApply(IMKEntityData targetData, MKActiveEffect activeEffect) {
            return damageType != null;
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            source = findEntity(source, instance.getSourceId(), targetData);

            DamageSource damage = MKDamageSource.causeAbilityDamage(damageType, instance.getAbilityId(),
                    source, source, getModifierScale());

            float value = getScaledValue(instance.getStackCount());
            targetData.getEntity().attackEntityFrom(damage, value);
            return true;
        }

        @Override
        public void serializeStorage(CompoundNBT stateTag) {
            super.serializeStorage(stateTag);
            MKNBTUtil.writeResourceLocation(stateTag, "damageType", damageType.getId());
        }

        @Override
        public void deserializeStorage(CompoundNBT stateTag) {
            super.deserializeStorage(stateTag);
            damageType = MKCoreRegistry.getDamageType(MKNBTUtil.readResourceLocation(stateTag, "damageType"));
        }
    }
}
