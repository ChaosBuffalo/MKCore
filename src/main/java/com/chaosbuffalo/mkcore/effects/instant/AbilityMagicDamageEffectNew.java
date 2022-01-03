package com.chaosbuffalo.mkcore.effects.instant;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.*;
import net.minecraft.entity.Entity;
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

    public static class State extends ScalingValueEffectState {
        private Entity source;

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            source = findEntity(source, instance.getSourceId(), targetData);

            DamageSource damage;
            if (source != null) {
                damage = DamageSource.causeIndirectMagicDamage(source, source);
            } else {
                damage = DamageSource.MAGIC;
            }

            float value = getScaledValue(instance.getStackCount());
            targetData.getEntity().attackEntityFrom(damage, value);
            return true;
        }
    }
}
