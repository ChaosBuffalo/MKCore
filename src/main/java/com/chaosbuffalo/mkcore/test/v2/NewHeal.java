package com.chaosbuffalo.mkcore.test.v2;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.healing.MKHealing;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NewHeal extends MKAbility {
    public static NewHeal INSTANCE = new NewHeal();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKAbility> event) {
        event.getRegistry().register(INSTANCE);
    }

    protected final FloatAttribute base = new FloatAttribute("base", 5.0f);
    protected final FloatAttribute scale = new FloatAttribute("scale", 5.0f);
    protected final FloatAttribute modifierScaling = new FloatAttribute("modifierScaling", 1.0f);

    public NewHeal() {
        super(MKCore.MOD_ID, "ability.new_heal");
        setCooldownSeconds(6);
        setManaCost(4);
        setCastTime(GameConstants.TICKS_PER_SECOND / 4);
        addAttributes(base, scale, modifierScaling);
        addSkillAttribute(MKAttributes.RESTORATION);

    }

    @Override
    protected ITextComponent getAbilityDescription(IMKEntityData entityData) {
        int level = getSkillLevel(entityData.getEntity(), MKAttributes.RESTORATION);
        ITextComponent valueStr = getHealDescription(entityData, base.value(),
                scale.value(), level,
                modifierScaling.value());
        return new TranslationTextComponent(getDescriptionTranslationKey(), valueStr);
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

    @Override
    public boolean isValidTarget(LivingEntity caster, LivingEntity target) {
        return super.isValidTarget(caster, target) || MKHealing.isEnemyUndead(caster, target);
    }

    @Override
    public void endCast(LivingEntity caster, IMKEntityData casterData, AbilityContext context) {
        super.endCast(caster, casterData, context);
        int level = getSkillLevel(caster, MKAttributes.RESTORATION);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {
            MKCore.getEntityData(targetEntity).ifPresent(targetData -> {
                MKEffectBuilder<?> heal = NewHealEffect.INSTANCE.builder(caster.getUniqueID())
                        .ability(this)
                        .state(s -> s.configure(base.value(), scale.value()))
                        .timed(200)
                        .periodic(40)
                        .amplify(level);
                targetData.getEffects().addEffect(heal);
            });

            //            SoundUtils.playSoundAtEntity(targetEntity, ModSounds.spell_heal_3);
            Vector3d lookVec = caster.getLookVec();
            PacketHandler.sendToTrackingAndSelf(
                    new ParticleEffectSpawnPacket(
                            ParticleTypes.HAPPY_VILLAGER,
                            ParticleEffects.SPHERE_MOTION, 50, 10,
                            targetEntity.getPosX(),
                            targetEntity.getPosY() + 1.0f,
                            targetEntity.getPosZ(),
                            1.0, 1.0, 1.0, 1.5, lookVec),
                    targetEntity);
        });
    }
}
