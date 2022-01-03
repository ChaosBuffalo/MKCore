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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKAbilityDamageEffectNew extends MKEffect {
    public static final MKAbilityDamageEffectNew INSTANCE = new MKAbilityDamageEffectNew();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKEffect> event) {
        event.getRegistry().register(INSTANCE);
    }


    public MKAbilityDamageEffectNew() {
        super(EffectType.HARMFUL);
        setRegistryName(MKCore.makeRL("effect.v2.ability_damage"));
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
            if (damageType == null)
                return false;
            return true;
        }

        @Override
        public boolean validateOnApply(IMKEntityData targetData, MKActiveEffect activeEffect) {
            if (damageType == null)
                return false;

            return true;
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            source = findEntity(source, instance.getSourceId(), targetData);

            DamageSource damage;
            if (source != null) {
                MKCore.LOGGER.info("MKAbilityDamageEffectNew has source");
                damage = MKDamageSource.causeAbilityDamage(damageType, instance.getAbilityId(), source, source)
                        .setModifierScaling(getModifierScale());
            } else {
                MKCore.LOGGER.info("MKAbilityDamageEffectNew no source");
                damage = MKDamageSource.causeAbilityDamage(damageType, instance.getAbilityId(), null, null)
                        .setModifierScaling(getModifierScale());
            }

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
