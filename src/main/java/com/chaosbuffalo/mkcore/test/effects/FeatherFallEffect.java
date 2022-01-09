package com.chaosbuffalo.mkcore.test.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class FeatherFallEffect extends MKEffect {

    public static final FeatherFallEffect INSTANCE = new FeatherFallEffect();

    public static MKEffectBuilder<?> from(Entity source) {
        return INSTANCE.builder(source.getUniqueID());
    }

    private FeatherFallEffect() {
        super(EffectType.BENEFICIAL);
        setRegistryName("effect.test_featherfall");
        SpellTriggers.FALL.register(this::onFall);
    }


    private void onFall(LivingHurtEvent event, DamageSource source, LivingEntity entity) {
        MKCore.getEntityData(entity).ifPresent(targetData -> {
            if (targetData.getEffects().isEffectActive(INSTANCE)) {
                event.setAmount(0.0f);
                if (entity instanceof PlayerEntity) {
                    entity.sendMessage(new StringTextComponent("My legs are OK"), Util.DUMMY_UUID);
                }
            }
        });
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
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
