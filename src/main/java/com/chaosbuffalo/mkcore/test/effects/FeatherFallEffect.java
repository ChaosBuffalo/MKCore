package com.chaosbuffalo.mkcore.test.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.*;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class FeatherFallEffect extends MKEffect {

    public static final FeatherFallEffect INSTANCE = new FeatherFallEffect();

    public static MKEffectBuilder<?> from(LivingEntity source) {
        return INSTANCE.builder(source);
    }

    private FeatherFallEffect() {
        super(MobEffectCategory.BENEFICIAL);
        setRegistryName("effect.test_featherfall");
        SpellTriggers.FALL.register(this::onFall);
    }


    private void onFall(LivingHurtEvent event, DamageSource source, LivingEntity entity) {
        MKCore.getEntityData(entity).ifPresent(targetData -> {
            if (targetData.getEffects().isEffectActive(INSTANCE)) {
                event.setAmount(0.0f);
                if (entity instanceof Player) {
                    entity.sendMessage(new TextComponent("My legs are OK"), Util.NIL_UUID);
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
