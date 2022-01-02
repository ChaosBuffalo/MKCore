package com.chaosbuffalo.mkcore.effects.status;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.*;
import com.chaosbuffalo.mkcore.init.CoreSounds;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.EffectType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class StunEffectV2 extends MKEffect {
    private final UUID MODIFIER_ID = UUID.fromString("e27f71ce-26f0-465e-b465-7e5ea711e53c");

    public static final StunEffectV2 INSTANCE = new StunEffectV2();

    protected StunEffectV2() {
        super(EffectType.HARMFUL);
        setRegistryName(MKCore.MOD_ID, "effect.v2.stun");
        addAttribute(Attributes.MOVEMENT_SPEED, MODIFIER_ID, -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKEffect> event) {
        event.getRegistry().register(INSTANCE);
    }

    @Override
    public void onInstanceAdded(IMKEntityData targetData, MKActiveEffect newInstance) {
        super.onInstanceAdded(targetData, newInstance);
        applyEffect(targetData, newInstance);
    }

    @Override
    public void onInstanceRemoved(IMKEntityData targetData, MKActiveEffect expiredEffect) {
        super.onInstanceRemoved(targetData, expiredEffect);
        LivingEntity target = targetData.getEntity();
        if (target instanceof MobEntity) {
            MobEntity mob = (MobEntity) target;
            mob.setNoAI(false);
        }
    }

    @Override
    public void onInstanceReady(IMKEntityData targetData, MKActiveEffect activeInstance) {
        applyEffect(targetData, activeInstance);
    }


    private void applyEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {
        LivingEntity target = targetData.getEntity();;
        if (target instanceof MobEntity) {
            MobEntity mob = (MobEntity) target;
            mob.setNoAI(true);
        }
        MKCore.getEntityData(target).ifPresent(entityData -> entityData.getAbilityExecutor().interruptCast());
        SoundUtils.serverPlaySoundAtEntity(target, CoreSounds.stun_sound, target.getSoundCategory());
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }
}

