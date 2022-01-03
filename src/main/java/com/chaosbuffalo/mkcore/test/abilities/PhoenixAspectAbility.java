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
import com.chaosbuffalo.mkcore.effects.ParticleEffect;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.test.effects.FeatherFallEffect;
import com.chaosbuffalo.mkcore.test.effects.PhoenixAspectEffect;
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
public class PhoenixAspectAbility extends MKAbility {
    public static PhoenixAspectAbility INSTANCE = new PhoenixAspectAbility();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKAbility> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static int BASE_DURATION = 60;
    public static int DURATION_SCALE = 60;

    private PhoenixAspectAbility() {
        super(MKCore.makeRL("ability.test_phoenix_aspect"));
        setCastTime(GameConstants.TICKS_PER_SECOND * 3);
        setCooldownSeconds(400);
        setManaCost(15);
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
        return 12.0f;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PBAOE;
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        int level = 1;

        // What to do for each target hit
        int duration = (BASE_DURATION + DURATION_SCALE * level) * GameConstants.TICKS_PER_SECOND;
//        duration = PlayerFormulas.applyBuffDurationBonus(data, duration);
        SpellCast flying = PhoenixAspectEffect.INSTANCE.newSpellCast(castingEntity);
        SpellCast feather = FeatherFallEffect.INSTANCE.newSpellCast(castingEntity);
        SpellCast particlePotion = ParticleEffect.Create(castingEntity,
                ParticleTypes.FIREWORK,
                ParticleEffects.DIRECTED_SPOUT, false, new Vector3d(1.0, 1.5, 1.0),
                new Vector3d(0.0, 1.0, 0.0), 40, 5, 1.0);

        AreaEffectBuilder.createOnCaster(castingEntity)
                .spellCast(flying, duration, level, getTargetContext())
                .spellCast(feather, duration + 10 * GameConstants.TICKS_PER_SECOND, level, getTargetContext())
                .spellCast(particlePotion, level, getTargetContext())
                .instant()
                .particle(ParticleTypes.FIREWORK)
                .color(65480).radius(getDistance(castingEntity), true)
                .spawn();

        PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
                ParticleTypes.FIREWORK,
                ParticleEffects.CIRCLE_MOTION, 50, 0,
                castingEntity.getPosX(), castingEntity.getPosY() + 1.5,
                castingEntity.getPosZ(), 1.0, 1.0, 1.0, 1.0f,
                castingEntity.getLookVec()), castingEntity);
    }
}
