package com.chaosbuffalo.mkcore.test.v2;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.effects.*;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.google.common.reflect.TypeToken;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TestFallCountingEffect extends MKEffect {
    public static final TestFallCountingEffect INSTANCE = new TestFallCountingEffect();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKEffect> event) {
        event.getRegistry().register(INSTANCE);
    }

    private final TypeToken<State> STATE = new TypeToken<State>() {};

    public TestFallCountingEffect() {
        super(EffectType.BENEFICIAL);
        setRegistryName(MKCore.makeRL("effect.v2.fall_counter"));
        SpellTriggers.FALL.register(this::onFall);
    }

    private void onFall(LivingHurtEvent event, DamageSource source, LivingEntity entity) {
        MKCore.LOGGER.info("onFall {} {}", entity, event.getAmount());

        MKPlayerData targetData = MKCore.getPlayerOrNull(entity);
        if (targetData == null)
            return;

        targetData.getEffects().effects(this).forEach(activeEffect -> {
            ChatUtils.sendMessage(targetData.getEntity(), "onFall");
            activeEffect.getState(STATE).counter++;
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

    public static class State extends MKEffectState {

        private int lastCounter;
        private int counter;
        private final int max = 5;

        @Override
        public boolean isReady(IMKEntityData targetData, MKActiveEffect instance) {
            return counter > lastCounter;
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            lastCounter = counter;
            if (!(targetData instanceof MKPlayerData)) {
                return false;
            }

            MKPlayerData playerData = (MKPlayerData) targetData;
            ChatUtils.sendMessage(playerData.getEntity(), "Fall counter %d", counter);

            instance.modifyStackCount(1);

            if (counter >= max) {
                ChatUtils.sendMessage(playerData.getEntity(), "Fall counter done");
                return false;
            }

            return true;
        }
    }
}
