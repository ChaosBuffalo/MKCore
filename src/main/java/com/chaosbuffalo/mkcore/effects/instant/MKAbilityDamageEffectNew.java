package com.chaosbuffalo.mkcore.effects.instant;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
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

    public static class State extends MKEffectState {
        // Serialized
        public float base;
        public float scale;
        public MKDamageType damageType;
        public float modifierScaling = 1.0f;


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
            DamageSource damage;
            float value = base + (scale * instance.getStackCount());


            source = findEntity(source, instance.getSourceId(), targetData);
            if (source != null) {
                MKCore.LOGGER.info("MKAbilityDamageEffectNew has source");
                damage = MKDamageSource.causeAbilityDamage(damageType, instance.getAbilityId(), source, source)
                        .setModifierScaling(modifierScaling);
            } else {
                MKCore.LOGGER.info("MKAbilityDamageEffectNew no source");
                damage = MKDamageSource.causeAbilityDamage(damageType, instance.getAbilityId(), null, null)
                        .setModifierScaling(modifierScaling);
            }

            targetData.getEntity().attackEntityFrom(damage, value);
            return true;
        }

        @Override
        public void serializeStorage(CompoundNBT stateTag) {
            stateTag.putFloat("base", base);
            stateTag.putFloat("scale", scale);
            stateTag.putFloat("modScale", modifierScaling);
            stateTag.putString("damageType", damageType.getId().toString());
        }

        @Override
        public void deserializeStorage(CompoundNBT stateTag) {
            base = stateTag.getFloat("base");
            scale = stateTag.getFloat("scale");
            modifierScaling = stateTag.getFloat("modScale");
            damageType = MKCoreRegistry.getDamageType(ResourceLocation.tryCreate(stateTag.getString("damageType")));
        }
    }
}
