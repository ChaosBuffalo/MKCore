package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.NeedsBuffCondition;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKCombatFormulas;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.utility.MKOldParticleEffect;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.test.effects.NewFireArmorEffect;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class NewFireArmor extends MKAbility {
    public static final NewFireArmor INSTANCE = new NewFireArmor();

    public static int BASE_DURATION = 60;
    public static int DURATION_SCALE = 30;

    private NewFireArmor() {
        super(MKCore.makeRL("ability.v2.fire_armor"));
        setCastTime(GameConstants.TICKS_PER_SECOND);
        setCooldownSeconds(135);
        setManaCost(12);
        setUseCondition(new NeedsBuffCondition(this, Effects.FIRE_RESISTANCE));
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.FRIENDLY;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PBAOE;
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 12f;
    }

    private int getDuration(IMKEntityData casterData, int level) {
        int duration = (BASE_DURATION + DURATION_SCALE * level) * GameConstants.TICKS_PER_SECOND;
        return MKCombatFormulas.applyBuffDurationModifier(casterData, duration);
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        int level = 1;

        int duration = getDuration(casterData, level);

        EffectInstance absorbEffect = new EffectInstance(Effects.ABSORPTION, duration, level + 1, false, true);

        EffectInstance fireResistanceEffect = new EffectInstance(Effects.FIRE_RESISTANCE, duration, level, false, true);

        MKEffectBuilder<?> newFireEffect = NewFireArmorEffect.INSTANCE.builder(castingEntity.getUniqueID())
                .ability(this)
                .timed(duration)
                .amplify(level);

        MKEffectBuilder<?> particleEffect = MKOldParticleEffect.from(castingEntity, ParticleTypes.FLAME,
                        ParticleEffects.CIRCLE_PILLAR_MOTION, false, new Vector3d(1.0, 1.0, 1.0),
                        new Vector3d(0.0, 1.0, 0.0), 40, 5, .1f)
                .ability(this)
                .amplify(level);

        AreaEffectBuilder.createOnCaster(castingEntity)
                .effect(absorbEffect, getTargetContext())
                .effect(fireResistanceEffect, getTargetContext())
                .effect(newFireEffect, getTargetContext())
                .effect(particleEffect, getTargetContext())
                .instant()
                .particle(ParticleTypes.DRIPPING_LAVA)
                .color(16762905)
                .radius(getDistance(castingEntity), true)
                .spawn();

        PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
                ParticleTypes.FLAME,
                ParticleEffects.CIRCLE_MOTION, 50, 0,
                castingEntity.getPosX(), castingEntity.getPosY() + 1.0,
                castingEntity.getPosZ(), 1.0, 1.0, 1.0, .1f,
                castingEntity.getLookVec()), castingEntity);
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
