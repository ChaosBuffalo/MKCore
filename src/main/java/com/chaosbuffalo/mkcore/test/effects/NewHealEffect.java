package com.chaosbuffalo.mkcore.test.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.healing.MKHealSource;
import com.chaosbuffalo.mkcore.core.healing.MKHealing;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ScalingValueEffectState;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

public class NewHealEffect extends MKEffect {
    public static final NewHealEffect INSTANCE = new NewHealEffect();

    private NewHealEffect() {
        super(MobEffectCategory.BENEFICIAL);
        setRegistryName("effect.new_heal");
    }

    @Override
    public void onInstanceAdded(IMKEntityData targetData, MKActiveEffect newInstance) {
        super.onInstanceAdded(targetData, newInstance);
        if (targetData.getEffects().isEffectActive(SkinLikeWoodEffect.INSTANCE)) {
            MKCore.LOGGER.info("NewHealEffect.onInstanceAdded found SkinLikeWoodEffectV2 so adding an extra stack");
            newInstance.modifyDuration(300);
            newInstance.modifyStackCount(1);
        }
    }

    @Override
    public MKEffectBuilder<State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    @Override
    public MKEffectBuilder<State> builder(LivingEntity sourceEntity) {
        return new MKEffectBuilder<>(this, sourceEntity, this::makeState);
    }

    @Override
    public State makeState() {
        return new State();
    }

    public static class State extends ScalingValueEffectState {

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {
            if (activeEffect.getSourceEntity() == null) {
                MKCore.LOGGER.debug("NewHealEffect.performEffect before trying to recover source {} = {} {}",
                        activeEffect.getSourceId(), activeEffect.getSourceEntity(), activeEffect.getDirectEntity());
                activeEffect.recoverState(targetData);
                MKCore.LOGGER.debug("NewHealEffect.performEffect after trying to recover source {} = {} {}",
                        activeEffect.getSourceId(), activeEffect.getSourceEntity(), activeEffect.getDirectEntity());
            }

            LivingEntity target = targetData.getEntity();
            float value = getScaledValue(activeEffect.getStackCount(), activeEffect.getSkillLevel());
            MKCore.LOGGER.debug("NewHealEffect.performEffect {} on {} from {} {}", value, target,
                    activeEffect.getSourceEntity(), activeEffect);
            MKHealSource heal = MKHealSource.getHolyHeal(activeEffect.getAbilityId(), activeEffect.getDirectEntity(),
                    activeEffect.getSourceEntity(), getModifierScale());
            MKHealing.healEntityFrom(target, value, heal);
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
