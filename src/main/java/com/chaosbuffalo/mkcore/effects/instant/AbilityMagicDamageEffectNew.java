package com.chaosbuffalo.mkcore.effects.instant;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AbilityMagicDamageEffectNew extends MKEffect {
    public static final AbilityMagicDamageEffectNew INSTANCE = new AbilityMagicDamageEffectNew();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKEffect> event) {
        event.getRegistry().register(INSTANCE);
    }


    public AbilityMagicDamageEffectNew() {
        super(EffectType.HARMFUL);
        setRegistryName(MKCore.makeRL("effect.v2.magic_damage"));
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
        public float base;
        public float scale;
        public Entity source;

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            DamageSource damage;
            float value = base + (scale * instance.getStackCount());
            Entity source = findEntity(this.source, instance.getSourceId(), targetData);
            if (source != null) {
                damage = DamageSource.causeIndirectMagicDamage(source, source);
            } else {
                damage = DamageSource.MAGIC;
            }

            targetData.getEntity().attackEntityFrom(damage, value);
            return true;
        }

        @Override
        public void serializeStorage(CompoundNBT stateTag) {
            super.serializeStorage(stateTag);
            stateTag.putFloat("base", base);
            stateTag.putFloat("scale", scale);
        }

        @Override
        public void deserializeStorage(CompoundNBT stateTag) {
            super.deserializeStorage(stateTag);
            base = stateTag.getFloat("base");
            scale = stateTag.getFloat("scale");
        }
    }
}
