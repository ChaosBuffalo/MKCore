package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.AbilitySlot;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ParticleEffect;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.test.effects.ClericHealEffect;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HealingRain extends MKAbility {
    public static final HealingRain INSTANCE = new HealingRain();

    public static float BASE_AMOUNT = 2.0f;
    public static float AMOUNT_SCALE = 1.0f;

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKAbility> event) {
        event.getRegistry().register(INSTANCE);
    }


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
    public AbilitySlot getType() {
        return AbilitySlot.Ultimate;
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
    public void continueCast(LivingEntity entity, IMKEntityData data, int castTimeLeft, AbilityContext context) {
        super.continueCast(entity, data, castTimeLeft, context);
        int tickSpeed = 5;
        if (castTimeLeft % tickSpeed == 0) {
            int level = 0;
            SpellCast heal = ClericHealEffect.Create(entity, BASE_AMOUNT, AMOUNT_SCALE);
            SpellCast particlePotion = ParticleEffect.Create(entity,
                    ParticleTypes.BUBBLE,
                    ParticleEffects.CIRCLE_MOTION, false,
                    new Vector3d(1.0, 1.0, 1.0),
                    new Vector3d(0.0, 1.0, 0.0),
                    10, 0, 1.0);

            float dist = getDistance(entity);
            AreaEffectBuilder.createOnCaster(entity)
                    .spellCast(heal, level, getTargetContext())
                    .spellCast(particlePotion, level, getTargetContext())
                    .instant()
                    .color(16409620).radius(dist, true)
                    .disableParticle()
                    .spawn();

            PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
                                ParticleTypes.BUBBLE,
                                ParticleEffects.RAIN_EFFECT, 30, 4,
                                entity.getPosX(), entity.getPosY() + 3.0,
                                entity.getPosZ(), dist, 0.5, dist, 1.0,
                                entity.getLookVec()), entity);
        }
    }
}
