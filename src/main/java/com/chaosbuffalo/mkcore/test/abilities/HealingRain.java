package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.utility.MKOldParticleEffect;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.test.effects.NewHealEffect;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class HealingRain extends MKAbility {
    public static final HealingRain INSTANCE = new HealingRain();

    public static float BASE_AMOUNT = 2.0f;
    public static float AMOUNT_SCALE = 1.0f;

    public HealingRain() {
        super(MKCore.makeRL("ability.test_healing_rain"));
        setCastTime(2 * GameConstants.TICKS_PER_SECOND);
        setManaCost(10);
        setCooldownSeconds(20);
    }

    @Override
    public boolean isInterruptible() {
        return false;
    }

    @Override
    public AbilityType getType() {
        return AbilityType.Ultimate;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.FRIENDLY;
    }


    @Override
    public float getDistance(LivingEntity entity) {
        return 6.0f;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PBAOE;
    }

    @Override
    public void continueCast(LivingEntity castingEntity, IMKEntityData casterData, int castTimeLeft, AbilityContext context) {
        super.continueCast(castingEntity, casterData, castTimeLeft, context);
        int tickSpeed = 5;
        if (castTimeLeft % tickSpeed == 0) {
            int level = 0;
            MKEffectBuilder<?> heal = NewHealEffect.INSTANCE.builder(castingEntity)
                    .state(s -> s.setScalingParameters(BASE_AMOUNT, AMOUNT_SCALE))
                    .ability(this)
                    .amplify(level);
            MKEffectBuilder<?> particlePotion = MKOldParticleEffect.from(castingEntity,
                    ParticleTypes.BUBBLE,
                    ParticleEffects.CIRCLE_MOTION, false,
                    new Vector3d(1.0, 1.0, 1.0),
                    new Vector3d(0.0, 1.0, 0.0),
                    10, 0, 1.0)
                    .ability(this);

            float dist = getDistance(castingEntity);
            AreaEffectBuilder.createOnCaster(castingEntity)
                    .effect(heal, getTargetContext())
                    .effect(particlePotion, getTargetContext())
                    .instant()
                    .color(16409620)
                    .radius(dist, true)
                    .disableParticle()
                    .spawn();

            PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
                                ParticleTypes.BUBBLE,
                                ParticleEffects.RAIN_EFFECT, 30, 4,
                                castingEntity.getPosX(), castingEntity.getPosY() + 3.0,
                                castingEntity.getPosZ(), dist, 0.5, dist, 1.0,
                                castingEntity.getLookVec()), castingEntity);
        }
    }

    @SuppressWarnings("unused")
    @Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    private static class RegisterMe {
        @SubscribeEvent
        public static void register(RegistryEvent.Register<MKAbility> event) {
            event.getRegistry().register(INSTANCE);
        }
    }
}
