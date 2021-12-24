package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectInstance;
import com.chaosbuffalo.mkcore.effects.MKEffectTickAction;
import com.chaosbuffalo.mkcore.network.EntityEffectPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityEffectHandler {
    protected final IMKEntityData entityData;
    protected final Map<UUID, EffectSource> sources = new HashMap<>();

    public EntityEffectHandler(IMKEntityData entityData) {
        this.entityData = entityData;
    }

    class EffectSource {
        private final UUID sourceId;
        protected final Map<MKEffect, MKActiveEffect> activeEffectMap = new HashMap<>();
        private final List<MKActiveEffect> removeQueue = new ArrayList<>();

        public EffectSource(UUID sourceId) {
            this.sourceId = sourceId;
        }

        public void tick() {
            if (isEmpty() || !entityData.getEntity().isAlive())
                return;

            activeEffectMap.forEach((effect, active) -> {
                MKEffectTickAction action = active.getBehaviour().behaviourTick(entityData, active);
                if (action == MKEffectTickAction.Update) {
                    onEffectUpdated(active);
                } else if (action == MKEffectTickAction.Remove) {
                    removeQueue.add(active);
                }
            });

            if (removeQueue.size() > 0) {
                removeQueue.forEach(this::removeEffectInstance);
                removeQueue.clear();
            }
        }

        private void removeEffectInstance(MKActiveEffect expiredInstance) {
            MKCore.LOGGER.debug("EntityEffectHandler.removeEffectInstance {} from {}", expiredInstance, entityData.getEntity());
            activeEffectMap.remove(expiredInstance.getEffect());
            // Run the callbacks after removal, so they won't see the effect as active
            onEffectRemoved(expiredInstance);
        }

        public void removeEffect(MKEffect effect) {
            MKCore.LOGGER.debug("EntityEffectHandler.removeEffect {} from {}", effect, entityData.getEntity());
            MKActiveEffect expiredInstance = activeEffectMap.get(effect);
            if (expiredInstance != null) {
                removeEffectInstance(expiredInstance);
            }
        }

        public void addEffect(MKActiveEffect activeEffect) {
            if (activeEffect.hasDuration()) {
                MKCore.LOGGER.debug("EntityEffectHandler.addEffect timed {} to {}", activeEffect, entityData.getEntity());
                MKActiveEffect existing = activeEffectMap.get(activeEffect.getEffect());
                if (existing == null) {
                    activeEffectMap.put(activeEffect.getEffect(), activeEffect);
                    onNewEffect(activeEffect);
                } else {
                    existing.getInstance().combine(existing, activeEffect);
                    onEffectUpdated(existing);
                }
            } else {
                MKCore.LOGGER.debug("EntityEffectHandler.addEffect instant {} to {}", activeEffect, entityData.getEntity());
                if (entityData.isServerSide()) {
                    activeEffect.getInstance().performEffect(entityData, activeEffect);
                }
            }
        }

        // Server-side only
        private void loadEffect(MKActiveEffect activeEffect) {
            MKCore.LOGGER.debug("EntityEffectHandler.EffectSource.loadEffect {}", activeEffect);
            activeEffect.getEffect().onInstanceLoaded(entityData, activeEffect);
            activeEffectMap.put(activeEffect.getEffect(), activeEffect);
        }

        // Server-side only
        private void onWorldReady(MKActiveEffect activeEffect) {
            MKCore.LOGGER.debug("EntityEffectHandler.onWorldReady {}", activeEffect);
            activeEffect.getEffect().onInstanceReady(entityData, activeEffect);
        }

        // Called on both sides
        protected void onNewEffect(MKActiveEffect activeEffect) {
            MKCore.LOGGER.debug("EntityEffectHandler.onNewEffect {}", activeEffect);
            if (entityData.isServerSide()) {
                activeEffect.getEffect().onInstanceAdded(entityData, activeEffect);
                sendEffectSet(activeEffect);
            }
        }

        // Called on both sides
        protected void onEffectUpdated(MKActiveEffect activeEffect) {
            MKCore.LOGGER.debug("EntityEffectHandler.onEffectUpdated {}", activeEffect);
            if (entityData.isServerSide()) {
                activeEffect.getEffect().onInstanceUpdated(entityData, activeEffect);
                sendEffectSet(activeEffect);
            }
        }

        // Called on both sides
        protected void onEffectRemoved(MKActiveEffect activeEffect) {
            MKCore.LOGGER.debug("EntityEffectHandler.onEffectRemoved {}", activeEffect);
            if (entityData.isServerSide()) {
                activeEffect.getEffect().onInstanceRemoved(entityData, activeEffect);
                if (!activeEffect.isExpired()) {
                    // If it was removed early we need to tell the client
                    sendEffectRemove(activeEffect);
                }
            }
        }

        protected void sendEffectSet(MKActiveEffect activeEffect) {
            sendEffectPacket(activeEffect, EntityEffectPacket.Action.SET);
        }

        protected void sendEffectRemove(MKActiveEffect activeEffect) {
            sendEffectPacket(activeEffect, EntityEffectPacket.Action.REMOVE);
        }

        private void sendEffectPacket(MKActiveEffect activeEffect, EntityEffectPacket.Action action) {
            if (entityData.isServerSide()) {
                EntityEffectPacket packet = new EntityEffectPacket(entityData, activeEffect, action);
                PacketHandler.sendToTrackingAndSelf(packet, entityData.getEntity());
            }
        }

        public boolean isEffectActive(MKEffect effect) {
            return activeEffectMap.containsKey(effect);
        }

        public void clearEffects() {
            MKCore.LOGGER.debug("EntityEffectHandler.clearEffects");
            List<MKActiveEffect> remove = new ArrayList<>(activeEffectMap.values());
            remove.forEach(this::removeEffectInstance);
        }

        public boolean hasEffects() {
            return activeEffectMap.size() > 0;
        }

        public boolean isEmpty() {
            return activeEffectMap.size() == 0;
        }

        public Stream<MKActiveEffect> effectsStream() {
            return activeEffectMap.values().stream();
        }

        public void onWorldReady() {
            if (hasEffects()) {
                effects().forEach(this::onWorldReady);
            }
        }

        public void onDeath() {
            MKCore.LOGGER.debug("EffectSource.onDeath {} {}", activeEffectMap.size(), sourceId);
            activeEffectMap.clear();
        }

        public void sendAllEffectsToPlayer(ServerPlayerEntity playerEntity) {
            if (hasEffects()) {
                EntityEffectPacket packet = new EntityEffectPacket(entityData, sourceId, activeEffectMap.values());
                PacketHandler.sendMessage(packet, playerEntity);
            }
        }

        public INBT serializeStorage() {
            ListNBT list = new ListNBT();
            activeEffectMap.forEach(((effect, activeEffect) -> {
                if (!activeEffect.getInstance().isTemporary()) {
                    list.add(activeEffect.serializeStorage());
                }
            }));

            return list;
        }

        public void deserializeStorage(CompoundNBT nbt, String tagName) {

            ListNBT list = nbt.getList(tagName, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundNBT entry = list.getCompound(i);
                MKActiveEffect instance = MKActiveEffect.deserializeStorage(sourceId, entry);
                if (instance != null) {
                    loadEffect(instance);
                }
            }
        }

        public void clientSetEffect(MKActiveEffect activeEffect) {
            MKActiveEffect existing = activeEffectMap.get(activeEffect.getEffect());
            activeEffectMap.put(activeEffect.getEffect(), activeEffect);
            if (existing == null) {
                onNewEffect(activeEffect);
            } else {
                onEffectUpdated(activeEffect);
            }
        }

        public void clientRemoveEffect(MKActiveEffect activeEffect) {
            removeEffectInstance(activeEffect);
        }

        public void clientSetAllEffects(List<MKActiveEffect> activeEffects) {
            activeEffectMap.clear();
            activeEffects.forEach(instance -> activeEffectMap.put(instance.getEffect(), instance));
        }
    }

    protected EffectSource getOrCreateSource(UUID sourceId) {
        return sources.computeIfAbsent(sourceId, EffectSource::new);
    }

    public void tick() {
        if (!hasEffects())
            return;

        sources.values().stream()
                .filter(EffectSource::hasEffects)
                .forEach(EffectSource::tick);
    }

    public void onJoinWorld() {
        if (entityData.isServerSide() && hasEffects()) {
            sources.values().forEach(EffectSource::onWorldReady);
        }
    }

    public void onDeath() {
        MKCore.LOGGER.debug("EntityEventHandler.onDeath");
        if (hasEffects()) {
            sources.values().forEach(EffectSource::onDeath);
        }
    }

    public void sendAllEffectsToPlayer(ServerPlayerEntity playerEntity) {
        sources.forEach((sourceId, source) -> source.sendAllEffectsToPlayer(playerEntity));
    }

    private boolean hasEffects() {
        return sources.size() > 0;
    }

    public boolean isEffectActive(MKEffect effect) {
        if (!hasEffects())
            return false;
        return sources.values().stream().anyMatch(s -> s.isEffectActive(effect));
    }

    private void checkEmpty() {
        sources.values().removeIf(EffectSource::isEmpty);
    }

    public void removeEffect(MKEffect effect) {
        if (hasEffects()) {
            sources.values().forEach(s -> s.removeEffect(effect));
            checkEmpty();
        }
    }

    public void removeEffect(UUID sourceId, MKEffect effect) {
        EffectSource source = sources.get(sourceId);
        if (source != null) {
            source.removeEffect(effect);
        }
    }

    public void clearEffects() {
        if (hasEffects()) {
            sources.values().forEach(EffectSource::clearEffects);
            checkEmpty();
        }
    }

    public void addEffect(MKEffectInstance effectInstance) {
        addEffect(effectInstance.getSourceId(), effectInstance.createApplication());
    }

    public void addEffect(UUID sourceId, MKEffectInstance effectInstance) {
        addEffect(sourceId, effectInstance.createApplication());
    }

    public void addEffect(UUID sourceId, MKActiveEffect effectInstance) {
        getOrCreateSource(sourceId).addEffect(effectInstance);
    }

    public Collection<MKActiveEffect> effects() {
        return sources.values().stream()
                .flatMap(EffectSource::effectsStream)
                .collect(Collectors.toList());
    }

    public CompoundNBT serialize() {
        CompoundNBT nbt = new CompoundNBT();

        ListNBT list = new ListNBT();
        sources.forEach((sourceId, source) -> {
            if (source.hasEffects()) {
                CompoundNBT entry = new CompoundNBT();
                entry.putUniqueId("uuid", sourceId);
                entry.put("effects", source.serializeStorage());
                list.add(entry);
            }
        });
        nbt.put("sources", list);

        return nbt;
    }

    public void deserialize(CompoundNBT nbt) {
        ListNBT list = nbt.getList("sources", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT entry = list.getCompound(i);
            UUID sourceId = entry.getUniqueId("uuid");
            getOrCreateSource(sourceId).deserializeStorage(entry, "effects");
        }
    }

    public void clientSetEffect(UUID sourceId, MKActiveEffect effectInstance) {
        getOrCreateSource(sourceId).clientSetEffect(effectInstance);
    }

    public void clientRemoveEffect(UUID sourceId, MKActiveEffect effectInstance) {
        EffectSource source = sources.get(sourceId);
        if (source != null) {
            source.clientRemoveEffect(effectInstance);
        }
    }

    public void clientSetAllEffects(UUID sourceId, List<MKActiveEffect> instances) {
        getOrCreateSource(sourceId).clientSetAllEffects(instances);
    }
}
