package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.HealCondition;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.fx.particles.CoreParticleAnimations;
import com.chaosbuffalo.mkcore.init.CoreParticles;
import com.chaosbuffalo.mkcore.network.MKParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.test.effects.ClericHealEffect;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.core.Core;

import java.util.Set;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClericHeal extends MKAbility {
    public static final ClericHeal INSTANCE = new ClericHeal();

    public static final ResourceLocation PARTICLES = MKCore.makeRL("particle_anim.blue_magic");

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKAbility> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static float BASE_VALUE = 5.0f;
    public static float VALUE_SCALE = 5.0f;

    private ClericHeal() {
        super(MKCore.makeRL("ability.test_heal"));
        setCastTime(GameConstants.TICKS_PER_SECOND / 4);
        setCooldownSeconds(5);
        setManaCost(4);
        setUseCondition(new HealCondition(this, .75f));
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.FRIENDLY;
    }

//    @Override
//    public SoundEvent getCastingSoundEvent() {
//        return ModSounds.casting_holy;
//    }

//    @Nullable
//    @Override
//    public SoundEvent getSpellCompleteSoundEvent() {
//        return ModSounds.spell_holy_5;
//    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {
        super.endCast(entity, data, context);

        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(target -> {
            int level = 1;
            SpellCast heal = ClericHealEffect.Create(entity, BASE_VALUE, VALUE_SCALE).setTarget(target);
            target.addPotionEffect(heal.toPotionEffect(level));
//            SoundUtils.playSoundAtEntity(target, CoreSounds.spell_heal_3);
            PacketHandler.sendToTrackingAndSelf(new MKParticleEffectSpawnPacket(
                                CoreParticles.BLUE_MAGIC_CROSS,
                                ParticleEffects.SPHERE_MOTION, 50, 10,
                                target.getPosX(), target.getPosY() + 1.0f,
                                target.getPosZ(), 1.0, 1.0, 1.0, 0.0,
                                entity.getLookVec(), PARTICLES), target);
        });
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 10.0f + 5.0f;
    }

    @Override
    public boolean isValidTarget(LivingEntity caster, LivingEntity target) {
        return ClericHealEffect.INSTANCE.isValidTarget(getTargetContext(), caster, target);
    }

    @Override
    public Set<MemoryModuleType<?>> getRequiredMemories() {
        return ImmutableSet.of(MKAbilityMemories.ABILITY_TARGET);
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.SINGLE_TARGET_OR_SELF;
    }
}
