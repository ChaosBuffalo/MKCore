package com.chaosbuffalo.mkcore.test.v2;

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
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
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

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NewFireArmor extends MKAbility {
    public static final NewFireArmor INSTANCE = new NewFireArmor();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKAbility> event) {
        event.getRegistry().register(INSTANCE);
    }

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

    private int getDuration(IMKEntityData entityData, int level) {
        int duration = (BASE_DURATION + DURATION_SCALE * level) * GameConstants.TICKS_PER_SECOND;
        return MKCombatFormulas.applyBuffDurationModifier(entityData, duration);
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {
        super.endCast(entity, data, context);
        int level = 1;

        int duration = getDuration(data, level);

        EffectInstance absorbEffect = new EffectInstance(Effects.ABSORPTION, duration, level + 1, false, true);

        EffectInstance fireResistanceEffect = new EffectInstance(Effects.FIRE_RESISTANCE, duration, level, false, true);

        MKEffectBuilder<?> newFireEffect = NewFireArmorEffect.INSTANCE.builder(entity.getUniqueID())
                .ability(this)
                .timed(duration)
                .amplify(level);

        MKEffectBuilder<?> particleEffect = NewParticleEffect.INSTANCE.builder(entity.getUniqueID())
                .ability(this)
                .state(s -> s.setup(entity, ParticleTypes.FLAME, ParticleEffects.CIRCLE_PILLAR_MOTION, false,
                        new Vector3d(1.0, 1.0, 1.0), new Vector3d(0.0, 1.0, 0.0), 40, 5, .1f))
                .amplify(level);

        AreaEffectBuilder.createOnCaster(entity)
                .effect(absorbEffect, getTargetContext())
                .effect(fireResistanceEffect, getTargetContext())
                .effect(newFireEffect, getTargetContext())
                .effect(particleEffect, getTargetContext())
                .instant()
                .particle(ParticleTypes.DRIPPING_LAVA)
                .color(16762905).radius(getDistance(entity), true)
                .spawn();

        PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
                ParticleTypes.FLAME,
                ParticleEffects.CIRCLE_MOTION, 50, 0,
                entity.getPosX(), entity.getPosY() + 1.0,
                entity.getPosZ(), 1.0, 1.0, 1.0, .1f,
                entity.getLookVec()), entity);
    }
}
