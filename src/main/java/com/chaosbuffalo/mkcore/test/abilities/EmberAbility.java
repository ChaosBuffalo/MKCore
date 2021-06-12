package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.abilities.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.abilities.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.text.*;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;
import java.util.Set;


@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EmberAbility extends MKAbility {
    public static final EmberAbility INSTANCE = new EmberAbility();
    protected final FloatAttribute damage = new FloatAttribute("damage", 6.0f);
    protected final IntAttribute burnTime = new IntAttribute("burnTime", 5);


    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKAbility> event) {
        event.getRegistry().register(INSTANCE);
    }

    private EmberAbility() {
        super(MKCore.makeRL("ability.test_ember"));
        setCastTime(GameConstants.TICKS_PER_SECOND / 2);
        setCooldownSeconds(4);
        setManaCost(6);
        addAttributes(damage, burnTime);
    }

    @Override
    protected ITextComponent getAbilityDescription(IMKEntityData entityData) {
        ITextComponent damageStr = getDamageDescription(entityData, CoreDamageTypes.FireDamage, damage.getValue(), 0.0f, 0, 1.0f);
        ITextComponent burn = new StringTextComponent(burnTime.getValue().toString()).mergeStyle(TextFormatting.UNDERLINE);
        return new TranslationTextComponent(getDescriptionTranslationKey(), damageStr, burn);
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 25.0f;
    }


    @Override
    public void continueCastClient(LivingEntity entity, IMKEntityData data, int castTimeLeft) {
        super.continueCastClient(entity, data, castTimeLeft);
        Random rand = entity.getRNG();
        entity.getEntityWorld().addParticle(ParticleTypes.LAVA,
                entity.getPosX(), entity.getPosY() + 0.5F, entity.getPosZ(),
                rand.nextFloat() / 2.0F, 5.0E-5D, rand.nextFloat() / 2.0F);
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {
        super.endCast(entity, data, context);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {
            int burnDuration = burnTime.getValue();
            float amount = damage.getValue();
            MKCore.LOGGER.info("Ember damage {} burnTime {}", amount, burnDuration);
            targetEntity.setFire(burnDuration);
            targetEntity.attackEntityFrom(MKDamageSource.causeAbilityDamage(CoreDamageTypes.FireDamage,
                    getAbilityId(), entity, entity), damage.getValue());
//            SoundUtils.playSoundAtEntity(targetEntity, ModSounds.spell_fire_6);
            PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
                                ParticleTypes.FLAME,
                                ParticleEffects.CIRCLE_PILLAR_MOTION, 60, 10,
                                targetEntity.getPosX(), targetEntity.getPosY() + 1.0,
                                targetEntity.getPosZ(), 1.0, 1.0, 1.0, .25,
                                entity.getLookVec()), targetEntity);
        });
    }

    @Override
    public Set<MemoryModuleType<?>> getRequiredMemories() {
        return ImmutableSet.of(MKAbilityMemories.ABILITY_TARGET);
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.SINGLE_TARGET;
    }
}