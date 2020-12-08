package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.events.ServerSideLeftClickEmpty;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.init.ModDamageTypes;
import com.chaosbuffalo.mkcore.network.CritMessagePacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class SpellTriggers {


    public static boolean isMKDamage(DamageSource source) {
        return source instanceof MKDamageSource;
    }

    public static boolean isMinecraftPhysicalDamage(DamageSource source) {
        return !source.isFireDamage() &&
                !source.isExplosion() &&
                !source.isMagicDamage() &&
                (source.getDamageType().equals("player") || source.getDamageType().equals("mob"));
    }

    public static boolean isProjectileDamage(DamageSource source) {
        return source.isProjectile();
    }

    private static boolean startTrigger(Entity source, String tag) {
        if (source instanceof PlayerEntity) {
//            Log.info("startTrigger - %s", tag);
            return source.getCapability(CoreCapabilities.PLAYER_CAPABILITY).map(cap -> {
                if (cap.hasSpellTag(tag)) {
                    return false;
                }
                cap.addSpellTag(tag);
                return true;
            }).orElse(true);
        }
        return true;
    }

    private static void endTrigger(Entity source, String tag) {
        if (source instanceof PlayerEntity) {
//            Log.info("endTrigger - %s", tag);
            source.getCapability(CoreCapabilities.PLAYER_CAPABILITY).ifPresent(cap -> cap.removeSpellTag(tag));
        }
    }

    public static class FALL {
        private static final String TAG = FALL.class.getName();
        private static final List<FallTrigger> fallTriggers = new ArrayList<>();

        @FunctionalInterface
        public interface FallTrigger {
            void apply(LivingHurtEvent event, DamageSource source, LivingEntity entity);
        }

        public static void register(FallTrigger trigger) {
            fallTriggers.add(trigger);
        }

        public static void onLivingFall(LivingHurtEvent event, DamageSource source, LivingEntity entity) {
            if (!startTrigger(entity, TAG))
                return;
            fallTriggers.forEach(f -> f.apply(event, source, entity));
            endTrigger(entity, TAG);
        }
    }

    public static class PLAYER_HURT_ENTITY {

        @FunctionalInterface
        public interface PlayerHurtEntityTrigger {
            void apply(LivingHurtEvent event, DamageSource source, LivingEntity livingTarget,
                       ServerPlayerEntity playerSource, IMKEntityData sourceData);
        }

        private static final String MELEE_TAG = "PLAYER_HURT_ENTITY.melee";
        private static final String MAGIC_TAG = "PLAYER_HURT_ENTITY.magic";
        private static final String POST_TAG = "PLAYER_HURT_ENTITY.post";
        private static final String PROJECTILE_TAG = "PLAYER_HURT_ENTITY.projectile";
        private static final List<PlayerHurtEntityTrigger> playerHurtEntityMeleeTriggers = new ArrayList<>();
        private static final List<PlayerHurtEntityTrigger> playerHurtEntityMagicTriggers = new ArrayList<>();
        private static final List<PlayerHurtEntityTrigger> playerHurtEntityPostTriggers = new ArrayList<>();
        private static final List<PlayerHurtEntityTrigger> playerHurtEntityProjectileTriggers = new ArrayList<>();

        public static void registerMelee(PlayerHurtEntityTrigger trigger) {
            playerHurtEntityMeleeTriggers.add(trigger);
        }

        public static void registerMagic(PlayerHurtEntityTrigger trigger) {
            playerHurtEntityMagicTriggers.add(trigger);
        }

        public static void registerProjectile(PlayerHurtEntityTrigger trigger){
            playerHurtEntityProjectileTriggers.add(trigger);
        }

        public static void registerPostHandler(PlayerHurtEntityTrigger trigger) {
            playerHurtEntityPostTriggers.add(trigger);
        }

        public static void onPlayerHurtEntity(LivingHurtEvent event, DamageSource source,
                                              LivingEntity livingTarget, ServerPlayerEntity playerSource,
                                              IMKEntityData sourceData) {
            if (isMKDamage(source)) {
                MKDamageSource mkSource = (MKDamageSource) source;
                if (mkSource.isMeleeDamage()) {
                    handleMKMelee(event, mkSource, livingTarget, playerSource, sourceData);
                } else {
                    handleMKDamage(event, mkSource, livingTarget, playerSource, sourceData);
                }
            }

            // If this is a weapon swing
            if (isMinecraftPhysicalDamage(source)) {
                handleVanillaMelee(event, source, livingTarget, playerSource, sourceData);
            }

            if (isProjectileDamage(source)) {
                handleProjectile(event, source, livingTarget, playerSource, sourceData);
            }

            if (!startTrigger(playerSource, POST_TAG))
                return;
            playerHurtEntityPostTriggers.forEach(f -> f.apply(event, source, livingTarget, playerSource, sourceData));
            endTrigger(playerSource, POST_TAG);
        }

        private static void handleMKDamage(LivingHurtEvent event, MKDamageSource source, LivingEntity livingTarget,
                                           ServerPlayerEntity playerSource,
                                           IMKEntityData sourceData) {
            calculateMKDamage(event, livingTarget, playerSource, sourceData, source,
                    MAGIC_TAG, playerHurtEntityMagicTriggers);
        }

        private static void calculateMKDamage(LivingHurtEvent event, LivingEntity livingTarget,
                                              ServerPlayerEntity playerSource, IMKEntityData sourceData,
                                              MKDamageSource source, String typeTag,
                                              List<PlayerHurtEntityTrigger> playerHurtTriggers) {
            Entity immediate = source.getImmediateSource() != null ? source.getImmediateSource() : playerSource;
            float newDamage = source.getMKDamageType().applyDamage(playerSource, livingTarget, immediate, event.getAmount(), source.getModifierScaling());
            if (source.getMKDamageType().rollCrit(playerSource, livingTarget, immediate)) {
                newDamage = source.getMKDamageType().applyCritDamage(playerSource, livingTarget, immediate, event.getAmount());
                switch (source.getOrigination()){
                    case MK_ABILITY:
                        MKAbility ability = MKCoreRegistry.getAbility(source.getAbilityId());
                        ResourceLocation abilityName;
                        if (ability != null) {
                            abilityName = ability.getRegistryName();
                        } else {
                            abilityName = MKCoreRegistry.INVALID_ABILITY;
                        }
                        sendCritPacket(livingTarget, playerSource,
                                new CritMessagePacket(livingTarget.getEntityId(), playerSource.getUniqueID(), newDamage,
                                        abilityName, source.getMKDamageType()));
                        break;
                    case DAMAGE_TYPE:
                        sendCritPacket(livingTarget, playerSource,
                                new CritMessagePacket(livingTarget.getEntityId(), playerSource.getUniqueID(), newDamage,
                                        source.getMKDamageType(), source.getDamageTypeName()));
                        break;

                }


            }
            event.setAmount(newDamage);

            if (!startTrigger(playerSource, typeTag))
                return;
            playerHurtTriggers.forEach(f -> f.apply(event, source, livingTarget, playerSource, sourceData));
            endTrigger(playerSource, typeTag);
        }

        private static void handleProjectile(LivingHurtEvent event, DamageSource source, LivingEntity livingTarget,
                                             ServerPlayerEntity playerSource, IMKEntityData sourceData) {

            Entity projectile = source.getImmediateSource();
            if (projectile != null && ModDamageTypes.RANGED.rollCrit(playerSource, livingTarget, projectile)) {
                float newDamage = ModDamageTypes.RANGED.applyCritDamage(playerSource, livingTarget, projectile, event.getAmount());
                event.setAmount(newDamage);
                sendCritPacket(livingTarget, playerSource,
                        new CritMessagePacket(livingTarget.getEntityId(), playerSource.getUniqueID(), newDamage,
                                projectile.getEntityId()));
            }
            if (!startTrigger(playerSource, PROJECTILE_TAG))
                return;
            playerHurtEntityProjectileTriggers.forEach(f -> f.apply(event, source, livingTarget, playerSource, sourceData));
            endTrigger(playerSource, PROJECTILE_TAG);
        }

        private static void handleMKMelee(LivingHurtEvent event, MKDamageSource source, LivingEntity livingTarget,
                                          ServerPlayerEntity playerSource, IMKEntityData sourceData) {

            calculateMKDamage(event, livingTarget, playerSource, sourceData, source,
                    MELEE_TAG, playerHurtEntityMeleeTriggers);
        }

        private static void handleVanillaMelee(LivingHurtEvent event, DamageSource source, LivingEntity livingTarget,
                                               ServerPlayerEntity playerSource, IMKEntityData sourceData) {
            if (sourceData instanceof MKPlayerData) {
                if (ModDamageTypes.MeleeDamage.rollCrit(playerSource, livingTarget)) {
                    float newDamage = ModDamageTypes.MeleeDamage.applyCritDamage(playerSource, livingTarget, event.getAmount());
                    event.setAmount(newDamage);
                    sendCritPacket(livingTarget, playerSource,
                            new CritMessagePacket(livingTarget.getEntityId(), playerSource.getUniqueID(), newDamage));
                }
            }


            if (!startTrigger(playerSource, MELEE_TAG))
                return;
            playerHurtEntityMeleeTriggers.forEach(f -> f.apply(event, source, livingTarget, playerSource, sourceData));
            endTrigger(playerSource, MELEE_TAG);
        }
    }

    public static class ENTITY_HURT_PLAYER {
        @FunctionalInterface
        public interface EntityHurtPlayerTrigger {
            void apply(LivingHurtEvent event, DamageSource source, PlayerEntity livingTarget,
                       MKPlayerData targetData);
        }

        private static final String TAG = ENTITY_HURT_PLAYER.class.getName();
        private static final List<EntityHurtPlayerTrigger> entityHurtPlayerPreTriggers = new ArrayList<>();
        private static final List<EntityHurtPlayerTrigger> entityHurtPlayerPostTriggers = new ArrayList<>();

        public static void registerPreScale(EntityHurtPlayerTrigger trigger) {
            entityHurtPlayerPreTriggers.add(trigger);
        }

        public static void registerPostScale(EntityHurtPlayerTrigger trigger) {
            entityHurtPlayerPostTriggers.add(trigger);
        }

        public static void onEntityHurtPlayer(LivingHurtEvent event, DamageSource source, PlayerEntity livingTarget,
                                              MKPlayerData targetData) {
            if (!startTrigger(livingTarget, TAG))
                return;
            entityHurtPlayerPreTriggers.forEach(f -> f.apply(event, source, livingTarget, targetData));

            if (isMKDamage(source)) {
                MKDamageSource mkDamageSource = (MKDamageSource) source;
                if (mkDamageSource.isUnblockable()) {
                    event.setAmount(mkDamageSource.getMKDamageType().applyResistance(livingTarget, event.getAmount()));
                }
            }

            entityHurtPlayerPostTriggers.forEach(f -> f.apply(event, source, livingTarget, targetData));
            endTrigger(livingTarget, TAG);
        }
    }

    private static void sendCritPacket(LivingEntity livingTarget, ServerPlayerEntity playerSource,
                                       CritMessagePacket packet) {
        PacketHandler.sendToTrackingAndSelf(packet, playerSource);
        Vector3d lookVec = livingTarget.getLookVec();
        PacketHandler.sendToTrackingMaybeSelf(
                new ParticleEffectSpawnPacket(
                        ParticleTypes.ENCHANTED_HIT,
                        ParticleEffects.SPHERE_MOTION, 12, 4,
                        livingTarget.getPosX(), livingTarget.getPosY() + 1.0f,
                        livingTarget.getPosZ(), .5f, .5f, .5f, 0.2,
                        lookVec),
                livingTarget);
    }

    static <T> void selectiveTrigger(LivingEntity entity, Map<SpellEffectBase, T> triggers, BiConsumer<T, EffectInstance> consumer) {
        for (EffectInstance effectInstance : entity.getActivePotionEffects()) {
            if (effectInstance.getPotion() instanceof SpellEffectBase) {
                SpellEffectBase effect = (SpellEffectBase) effectInstance.getPotion();
                T trigger = triggers.get(effect);
                if (trigger != null) {
                    consumer.accept(trigger, effectInstance);
                }
            }
        }
    }

    public static class ATTACK_ENTITY {

        @FunctionalInterface
        public interface AttackEntityTrigger {
            void apply(LivingEntity player, Entity target, EffectInstance effect);
        }

        private static final String TAG = ATTACK_ENTITY.class.getName();
        private static final Map<SpellEffectBase, AttackEntityTrigger> attackEntityTriggers = new HashMap<>();

        public static void register(SpellEffectBase potion, AttackEntityTrigger trigger) {
            attackEntityTriggers.put(potion, trigger);
        }

        public static void onAttackEntity(LivingEntity attacker, Entity target) {
            if (!startTrigger(attacker, TAG))
                return;

            selectiveTrigger(attacker, attackEntityTriggers, (trigger, instance) -> trigger.apply(attacker, target, instance));
            endTrigger(attacker, TAG);
        }
    }

    public static class PLAYER_ATTACK_ENTITY {
        @FunctionalInterface
        public interface PlayerAttackEntityTrigger {
            void apply(LivingEntity player, Entity target, EffectInstance effect);
        }

        private static final String TAG = PLAYER_ATTACK_ENTITY.class.getName();
        private static final Map<SpellEffectBase, PlayerAttackEntityTrigger> attackEntityTriggers = new HashMap<>();

        public static void register(SpellEffectBase potion, PlayerAttackEntityTrigger trigger) {
            attackEntityTriggers.put(potion, trigger);
        }

        public static void onAttackEntity(LivingEntity attacker, Entity target) {
            if (!startTrigger(attacker, TAG))
                return;

            selectiveTrigger(attacker, attackEntityTriggers, (trigger, instance) -> trigger.apply(attacker, target, instance));
            endTrigger(attacker, TAG);
        }
    }

    public static class EMPTY_LEFT_CLICK {

        @FunctionalInterface
        public interface EmptyLeftClickTrigger {
            void apply(ServerSideLeftClickEmpty event, PlayerEntity player, EffectInstance effect);
        }

        private static final String TAG = EMPTY_LEFT_CLICK.class.getName();
        private static final Map<SpellEffectBase, EmptyLeftClickTrigger> emptyLeftClickTriggers = new HashMap<>();

        public static void register(SpellEffectBase potion, EmptyLeftClickTrigger trigger) {
            emptyLeftClickTriggers.put(potion, trigger);
        }

        public static void onEmptyLeftClick(PlayerEntity player, ServerSideLeftClickEmpty event) {
            if (!startTrigger(player, TAG))
                return;

            selectiveTrigger(player, emptyLeftClickTriggers, (trigger, instance) -> trigger.apply(event, player, instance));
            endTrigger(player, TAG);
        }
    }

    public static class PLAYER_KILL_ENTITY {
        private static final String TAG = PLAYER_KILL_ENTITY.class.getName();
        private static final Map<SpellEffectBase, PlayerKillEntityTrigger> killTriggers = new HashMap<>();

        @FunctionalInterface
        public interface PlayerKillEntityTrigger {
            void apply(LivingDeathEvent event, DamageSource source, PlayerEntity player);
        }

        public static void register(SpellEffectBase potion, PlayerKillEntityTrigger trigger) {
            killTriggers.put(potion, trigger);
        }

        public static void onEntityDeath(LivingDeathEvent event, DamageSource source, PlayerEntity entity) {
            if (!startTrigger(entity, TAG))
                return;

            selectiveTrigger(entity, killTriggers, (trigger, instance) -> trigger.apply(event, source, entity));
            endTrigger(entity, TAG);
        }
    }

    public static class PLAYER_DEATH {
        @FunctionalInterface
        public interface PlayerKillEntityTrigger {
            void apply(LivingDeathEvent event, DamageSource source, PlayerEntity player);
        }

        private static final String TAG = PLAYER_DEATH.class.getName();
        private static final Map<SpellEffectBase, PlayerKillEntityTrigger> killTriggers = new HashMap<>();

        public static void register(SpellEffectBase potion, PlayerKillEntityTrigger trigger) {
            killTriggers.put(potion, trigger);
        }

        public static void onEntityDeath(LivingDeathEvent event, DamageSource source, PlayerEntity entity) {
            if (!startTrigger(entity, TAG))
                return;

            selectiveTrigger(entity, killTriggers, (trigger, instance) -> trigger.apply(event, source, entity));
            endTrigger(entity, TAG);
        }
    }

    public static class PLAYER_EQUIPMENT_CHANGE {
        private static final String TAG = PLAYER_EQUIPMENT_CHANGE.class.getName();
        private static final Map<SpellEffectBase, PlayerEquipmentChangeTrigger> triggers = new HashMap<>();

        @FunctionalInterface
        public interface PlayerEquipmentChangeTrigger {
            void apply(LivingEquipmentChangeEvent event, IMKEntityData data, PlayerEntity player);
        }

        public static void register(SpellEffectBase potion, PlayerEquipmentChangeTrigger trigger) {
            triggers.put(potion, trigger);
        }

        public static void onEquipmentChange(LivingEquipmentChangeEvent event, IMKEntityData data, PlayerEntity player) {
            if (!startTrigger(player, TAG))
                return;

            selectiveTrigger(player, triggers, (trigger, instance) -> trigger.apply(event, data, player));
            endTrigger(player, TAG);
        }
    }
}