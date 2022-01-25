package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.network.CritMessagePacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.ArrayList;
import java.util.List;

public class PlayerHurtEntityTriggers extends SpellTriggers.TriggerCollectionBase {

    @FunctionalInterface
    public interface Trigger {
        void apply(LivingHurtEvent event, DamageSource source, LivingEntity livingTarget,
                   ServerPlayerEntity playerSource, IMKEntityData sourceData);
    }

    private static final String MELEE_TAG = "PLAYER_HURT_ENTITY.melee";
    private static final String MAGIC_TAG = "PLAYER_HURT_ENTITY.magic";
    private static final String POST_TAG = "PLAYER_HURT_ENTITY.post";
    private static final String PROJECTILE_TAG = "PLAYER_HURT_ENTITY.projectile";
    private static final List<Trigger> playerHurtEntityMeleeTriggers = new ArrayList<>();
    private static final List<Trigger> playerHurtEntityMagicTriggers = new ArrayList<>();
    private static final List<Trigger> playerHurtEntityPostTriggers = new ArrayList<>();
    private static final List<Trigger> playerHurtEntityProjectileTriggers = new ArrayList<>();
    private boolean hasTriggers = false;

    @Override
    public boolean hasTriggers() {
        return hasTriggers;
    }

    public void registerMelee(Trigger trigger) {
        playerHurtEntityMeleeTriggers.add(trigger);
        hasTriggers = true;
    }

    public void registerMagic(Trigger trigger) {
        playerHurtEntityMagicTriggers.add(trigger);
        hasTriggers = true;
    }

    public void registerProjectile(Trigger trigger) {
        playerHurtEntityProjectileTriggers.add(trigger);
        hasTriggers = true;
    }

    public void registerPostHandler(Trigger trigger) {
        playerHurtEntityPostTriggers.add(trigger);
        hasTriggers = true;
    }

    public void onPlayerHurtEntity(LivingHurtEvent event, DamageSource source,
                                   LivingEntity livingTarget, ServerPlayerEntity playerSource,
                                   IMKEntityData sourceData) {
        if (SpellTriggers.isMKDamage(source)) {
            MKDamageSource mkSource = (MKDamageSource) source;
            if (mkSource.isMeleeDamage()) {
                handleMKMelee(event, mkSource, livingTarget, playerSource, sourceData);
            } else {
                handleMKDamage(event, mkSource, livingTarget, playerSource, sourceData);
            }
        }

        // If this is a weapon swing
        if (SpellTriggers.isMinecraftPhysicalDamage(source)) {
            handleVanillaMelee(event, source, livingTarget, playerSource, sourceData);
        }

        if (SpellTriggers.isProjectileDamage(source)) {
            handleProjectile(event, source, livingTarget, playerSource, sourceData);
        }

        if (playerHurtEntityPostTriggers.size() == 0 || startTrigger(playerSource, POST_TAG))
            return;
        playerHurtEntityPostTriggers.forEach(f -> f.apply(event, source, livingTarget, playerSource, sourceData));
        endTrigger(playerSource, POST_TAG);
    }

    private void handleMKDamage(LivingHurtEvent event, MKDamageSource source, LivingEntity livingTarget,
                                ServerPlayerEntity playerSource,
                                IMKEntityData sourceData) {
        calculateMKDamage(event, livingTarget, playerSource, sourceData, source,
                MAGIC_TAG, playerHurtEntityMagicTriggers);
    }

    private void calculateMKDamage(LivingHurtEvent event, LivingEntity livingTarget,
                                   ServerPlayerEntity playerSource, IMKEntityData sourceData,
                                   MKDamageSource source, String typeTag,
                                   List<Trigger> playerHurtTriggers) {
        Entity immediate = source.getImmediateSource() != null ? source.getImmediateSource() : playerSource;
        float newDamage = source.getMKDamageType().applyDamage(playerSource, livingTarget, immediate, event.getAmount(), source.getModifierScaling());
        if (source.getMKDamageType().rollCrit(playerSource, livingTarget, immediate)) {
            newDamage = source.getMKDamageType().applyCritDamage(playerSource, livingTarget, immediate, event.getAmount());
            switch (source.getOrigination()) {
                case MK_ABILITY:
                    sendAbilityCrit(livingTarget, playerSource, source, newDamage);
                    break;
                case DAMAGE_TYPE:
                    sendEffectCrit(livingTarget, playerSource, source, newDamage);
                    break;
            }
        }
        event.setAmount(newDamage);

        if (playerHurtTriggers.size() == 0 || startTrigger(playerSource, typeTag))
            return;
        playerHurtTriggers.forEach(f -> f.apply(event, source, livingTarget, playerSource, sourceData));
        endTrigger(playerSource, typeTag);
    }

    private void sendEffectCrit(LivingEntity livingTarget, ServerPlayerEntity playerSource, MKDamageSource source,
                                float newDamage) {
        if (source instanceof MKDamageSource.EffectDamage) {
            MKDamageSource.EffectDamage effectDamage = (MKDamageSource.EffectDamage) source;
            sendCritPacket(livingTarget, playerSource,
                    new CritMessagePacket(livingTarget.getEntityId(), playerSource.getUniqueID(), newDamage,
                            source.getMKDamageType(), effectDamage.getDamageTypeName()));
        }
    }

    private void sendAbilityCrit(LivingEntity livingTarget, ServerPlayerEntity playerSource, MKDamageSource source,
                                 float newDamage) {
        if (source instanceof MKDamageSource.AbilityDamage) {
            MKDamageSource.AbilityDamage abilityDamage = (MKDamageSource.AbilityDamage) source;
            MKAbility ability = MKCoreRegistry.getAbility(abilityDamage.getAbilityId());
            ResourceLocation abilityName;
            if (ability != null) {
                abilityName = ability.getRegistryName();
            } else {
                abilityName = MKCoreRegistry.INVALID_ABILITY;
            }
            sendCritPacket(livingTarget, playerSource,
                    new CritMessagePacket(livingTarget.getEntityId(), playerSource.getUniqueID(), newDamage,
                            abilityName, source.getMKDamageType()));
        }
    }

    private void handleProjectile(LivingHurtEvent event, DamageSource source, LivingEntity livingTarget,
                                  ServerPlayerEntity playerSource, IMKEntityData sourceData) {

        Entity projectile = source.getImmediateSource();
        float damage = event.getAmount();
        if (SpellTriggers.isNonMKProjectileDamage(source)){
            damage += playerSource.getAttribute(MKAttributes.RANGED_DAMAGE).getValue();
        }
        boolean wasCrit = false;
        if (projectile != null && CoreDamageTypes.RangedDamage.rollCrit(playerSource, livingTarget, projectile)) {
            damage = CoreDamageTypes.RangedDamage.applyCritDamage(playerSource, livingTarget, projectile, damage);
            wasCrit = true;
        }
        damage = (float) (damage * (1.0 - livingTarget.getAttribute(MKAttributes.RANGED_RESISTANCE).getValue()));
        event.setAmount(damage);
        if (wasCrit){
            sendCritPacket(livingTarget, playerSource,
                    new CritMessagePacket(livingTarget.getEntityId(), playerSource.getUniqueID(), damage,
                            projectile.getEntityId()));
        }
        if (playerHurtEntityProjectileTriggers.size() == 0 || startTrigger(playerSource, PROJECTILE_TAG))
            return;
        playerHurtEntityProjectileTriggers.forEach(f -> f.apply(event, source, livingTarget, playerSource, sourceData));
        endTrigger(playerSource, PROJECTILE_TAG);
    }

    private void handleMKMelee(LivingHurtEvent event, MKDamageSource source, LivingEntity livingTarget,
                               ServerPlayerEntity playerSource, IMKEntityData sourceData) {

        calculateMKDamage(event, livingTarget, playerSource, sourceData, source,
                MELEE_TAG, playerHurtEntityMeleeTriggers);
    }

    private void handleVanillaMelee(LivingHurtEvent event, DamageSource source, LivingEntity livingTarget,
                                    ServerPlayerEntity playerSource, IMKEntityData sourceData) {
        if (sourceData instanceof MKPlayerData) {
            if (CoreDamageTypes.MeleeDamage.rollCrit(playerSource, livingTarget)) {
                float newDamage = CoreDamageTypes.MeleeDamage.applyCritDamage(playerSource, livingTarget, event.getAmount());
                event.setAmount(newDamage);
                sendCritPacket(livingTarget, playerSource,
                        new CritMessagePacket(livingTarget.getEntityId(), playerSource.getUniqueID(), newDamage));
            }
        }

        if (playerHurtEntityMeleeTriggers.size() == 0 || startTrigger(playerSource, MELEE_TAG))
            return;
        playerHurtEntityMeleeTriggers.forEach(f -> f.apply(event, source, livingTarget, playerSource, sourceData));
        endTrigger(playerSource, MELEE_TAG);
    }

    private static void sendCritPacket(LivingEntity livingTarget, ServerPlayerEntity playerSource,
                                       CritMessagePacket packet) {
        PacketHandler.sendToTrackingAndSelf(packet, playerSource);
        Vector3d lookVec = livingTarget.getLookVec();
        PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
                ParticleTypes.ENCHANTED_HIT,
                ParticleEffects.SPHERE_MOTION, 12, 4,
                livingTarget.getPosX(), livingTarget.getPosY() + 1.0f,
                livingTarget.getPosZ(), .5f, .5f, .5f, 0.2,
                lookVec), livingTarget);
    }
}
