package com.chaosbuffalo.mkcore.test.v2;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.effects.MKEffectInstance;
import com.chaosbuffalo.mkcore.network.MKParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.healing.MKHealing;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

//
@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HealAbility extends MKAbility {
    public static final ResourceLocation CASTING_PARTICLES = new ResourceLocation(MKCore.MOD_ID, "heal_casting");
    public static final ResourceLocation CAST_PARTICLES = new ResourceLocation(MKCore.MOD_ID, "heal_cast");
    public static final HealAbility INSTANCE = new HealAbility();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKAbility> event) {
        event.getRegistry().register(INSTANCE);
    }

    protected final FloatAttribute base = new FloatAttribute("base", 5.0f);
    protected final FloatAttribute scale = new FloatAttribute("scale", 5.0f);
    protected final FloatAttribute modifierScaling = new FloatAttribute("modifierScaling", 1.0f);
    protected final ResourceLocationAttribute cast_particles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);

    public HealAbility(){
        super(MKCore.MOD_ID, "ability.heal");
        setCooldownSeconds(6);
        setManaCost(4);
        setCastTime(GameConstants.TICKS_PER_SECOND / 4);
        addAttributes(base, scale, modifierScaling, cast_particles);
        addSkillAttribute(MKAttributes.RESTORATION);
        casting_particles.setDefaultValue(CASTING_PARTICLES);

    }

    @Override
    protected ITextComponent getAbilityDescription(IMKEntityData entityData) {
        int level = getSkillLevel(entityData.getEntity(), MKAttributes.RESTORATION);
        ITextComponent valueStr = getHealDescription(entityData, base.getValue(),
                scale.getValue(), level,
                modifierScaling.getValue());
        return new TranslationTextComponent(getDescriptionTranslationKey(), valueStr);
    }

    public FloatAttribute getModifierScaling() {
        return modifierScaling;
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 10.0f;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.FRIENDLY;
    }

    @Override
    public Set<MemoryModuleType<?>> getRequiredMemories() {
        return ImmutableSet.of(MKAbilityMemories.ABILITY_TARGET);
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.SINGLE_TARGET_OR_SELF;
    }

//    @Override
//    public SoundEvent getCastingSoundEvent() {
//        return ModSounds.casting_holy;
//    }
//
//    @Override
//    public SoundEvent getSpellCompleteSoundEvent() {
//        return ModSounds.spell_holy_5;
//    }

    @Override
    public boolean isValidTarget(LivingEntity caster, LivingEntity target) {
        return super.isValidTarget(caster, target) || (MKConfig.SERVER.healsDamageUndead.get() &&
                target.isEntityUndead() && MKHealing.isEnemyUndead(caster, target));
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {
        super.endCast(entity, data, context);
        int level = getSkillLevel(entity, MKAttributes.RESTORATION);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {
            MKEffectInstance heal = NewHealEffect.INSTANCE.createInstance(this, entity)
                    .configure(base.getValue(), scale.getValue());
//            SpellCast heal = ClericHealEffect.Create(entity, targetEntity,
//                    base.getValue(), scale.getValue());
            MKCore.getEntityData(targetEntity).ifPresent(targetData -> {
                targetData.getEffects().addEffect(heal);
            });
//            targetEntity.addPotionEffect(heal.toPotionEffect(level));
//            SoundUtils.serverPlaySoundAtEntity(targetEntity, ModSounds.spell_heal_3, targetEntity.getSoundCategory());
            PacketHandler.sendToTrackingAndSelf(new MKParticleEffectSpawnPacket(
                            new Vector3d(0.0, 1.0, 0.0), cast_particles.getValue(),
                            targetEntity.getEntityId()),
                    targetEntity);
        });
    }
}
