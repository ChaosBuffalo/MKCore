package com.chaosbuffalo.mkcore.test.v2;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.healing.MKHealSource;
import com.chaosbuffalo.mkcore.core.healing.MKHealing;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NewHealEffect extends MKEffect {
    public static final NewHealEffect INSTANCE = new NewHealEffect();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKEffect> event) {
        event.getRegistry().register(INSTANCE);
    }

    private NewHealEffect() {
        super(EffectType.BENEFICIAL);
        setRegistryName("effect.new_heal");
    }

    @Override
    public void onInstanceAdded(IMKEntityData targetData, MKActiveEffect newInstance) {
        super.onInstanceAdded(targetData, newInstance);
        if (targetData.getEffects().isEffectActive(SkinLikeWoodEffectV2.INSTANCE)) {
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
    public State makeState() {
        return new State();
    }

    static class State extends MKEffectState {
        private float base = 0.0f;
        private float scale = 1.0f;
        private Entity source;

        public void configure(float base, float scale) {
            this.base = base;
            this.scale = scale;
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            source = findEntity(source, instance.getSourceId(), targetData);
//            MKCore.LOGGER.info("NewHealEffect.performEffect trying to recover source {} = {}", sourceId, source);

            LivingEntity target = targetData.getEntity();
            float value = base + (scale * instance.getStackCount());
            MKCore.LOGGER.info("NewHealEffect.performEffect {} on {} from {} {}", value, target, source, instance);
            MKHealSource heal = MKHealSource.getHolyHeal(instance.getAbilityId(), source, 1.0f);
            MKHealing.healEntityFrom(target, value, heal);
            return true;
        }

        @Override
        public void serializeStorage(CompoundNBT stateTag) {
            stateTag.putFloat("base", base);
            stateTag.putFloat("scale", scale);
        }

        @Override
        public void deserializeStorage(CompoundNBT stateTag) {
            base = stateTag.getFloat("base");
            scale = stateTag.getFloat("scale");
        }
    }
}
