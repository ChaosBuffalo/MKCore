package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.AbilitySlot;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.IPlayerSyncComponentProvider;
import com.chaosbuffalo.mkcore.core.player.PlayerSyncComponent;
import com.chaosbuffalo.mkcore.sync.SyncGroup;
import com.chaosbuffalo.mkcore.sync.SyncInt;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerTalentKnowledge implements IPlayerSyncComponentProvider {
    private final MKPlayerData playerData;
    private final PlayerSyncComponent sync = new PlayerSyncComponent("talents");
    private final SyncInt talentPoints = new SyncInt("points", 0);
    private final SyncInt totalTalentPoints = new SyncInt("totalPoints", 0);
    private final Map<ResourceLocation, TalentTreeRecord> talentTreeRecordMap = new HashMap<>();
    private final KnownTalentCache talentCache = new KnownTalentCache(this);

    public PlayerTalentKnowledge(MKPlayerData playerData) {
        this.playerData = playerData;
        addSyncPrivate(talentPoints);
        addSyncPrivate(totalTalentPoints);
        if (!playerData.isServerSide()) {
            addSyncPrivate(new ClientTreeSyncGroup());
        }
    }

    @Override
    public PlayerSyncComponent getSyncComponent() {
        return sync;
    }

    public int getTotalTalentPoints() {
        return totalTalentPoints.get();
    }

    public int getUnspentTalentPoints() {
        return talentPoints.get();
    }

    public Stream<TalentRecord> getKnownTalentsStream() {
        return talentTreeRecordMap.values()
                .stream()
                .flatMap(TalentTreeRecord::getRecordStream)
                .filter(TalentRecord::isKnown);
    }

    public Stream<TalentRecord> getKnownTalentsStream(TalentType<?> type) {
        return getKnownTalentsStream()
                .filter(r -> r.getNode().getTalentType() == type);
    }

    public Collection<ResourceLocation> getKnownTrees() {
        return Collections.unmodifiableCollection(talentTreeRecordMap.keySet());
    }

    public Set<ResourceLocation> getKnownTalentIds(TalentType<?> type) {
        return Collections.unmodifiableSet(talentCache.getKnownTalentIds(type));
    }

    public boolean unlockTree(ResourceLocation treeId) {
        TalentTreeRecord record = unlockTreeInternal(treeId);
        if (record != null) {
            sync.addPrivate(record.getUpdater(), true);
            return true;
        }
        return false;
    }

    private TalentTreeRecord unlockTreeInternal(ResourceLocation treeId) {
        if (talentTreeRecordMap.containsKey(treeId)) {
            MKCore.LOGGER.warn("Player {} tried to unlock already-known talent tree {}", playerData.getEntity(), treeId);
            return null;
        }

        TalentTreeDefinition tree = MKCore.getTalentManager().getTalentTree(treeId);
        if (tree == null) {
            MKCore.LOGGER.warn("Player {} tried to unlock unknown tree {}", playerData.getEntity(), treeId);
            return null;
        }

        TalentTreeRecord record = tree.createRecord();
        talentTreeRecordMap.put(tree.getTreeId(), record);
        return record;
    }

    public boolean knowsTree(ResourceLocation treeId) {
        return talentTreeRecordMap.containsKey(treeId);
    }

    public TalentTreeRecord getTree(ResourceLocation treeId) {
        return talentTreeRecordMap.get(treeId);
    }

    public TalentRecord getRecord(ResourceLocation treeId, String line, int index) {
        TalentTreeRecord treeRecord = getTree(treeId);
        if (treeRecord == null)
            return null;

        return treeRecord.getNodeRecord(line, index);
    }

    public boolean grantTalentPoints(int amount) {
        if (amount > 0) {
            talentPoints.add(amount);
            totalTalentPoints.add(amount);
            return true;
        }
        return false;
    }

    public boolean removeTalentPoints(int amount) {
        if (amount >= 0 && amount <= talentPoints.get()) {
            talentPoints.add(-amount);
            return true;
        }

        return true;
    }

    public boolean spendTalentPoint(ResourceLocation treeId, String line, int index) {
        if (getUnspentTalentPoints() == 0) {
            MKCore.LOGGER.warn("Player {} attempted to spend talent ({}, {}) - no unspent points", playerData.getEntity(), treeId, line);
            return false;
        }

        TalentTreeRecord treeRecord = getTree(treeId);
        if (treeRecord == null) {
            MKCore.LOGGER.warn("Player {} attempted to spend talent ({}, {}) - tree not known", playerData.getEntity(), treeId, line);
            return false;
        }

        if (!treeRecord.trySpendPoint(line, index)) {
            MKCore.LOGGER.warn("Player {} attempted to spend talent ({}, {}) - requirement not met", playerData.getEntity(), treeId, line);
            return false;
        }

        talentCache.invalidate();
        talentPoints.add(-1);

        TalentRecord record = treeRecord.getNodeRecord(line, index);
        if (record != null) {
            playerData.getTalentHandler().onTalentRecordUpdated(record);
        }
        return true;
    }

    public boolean refundTalentPoint(ResourceLocation treeId, String line, int index) {
        TalentTreeRecord treeRecord = getTree(treeId);
        if (treeRecord == null) {
            MKCore.LOGGER.warn("Player {} attempted to unlearn talent in unknown tree {}", playerData.getEntity(), treeId);
            return false;
        }

        if (!treeRecord.tryRefundPoint(line, index)) {
            MKCore.LOGGER.warn("Player {} attempted to refund talent ({}, {}) - requirement not met", playerData.getEntity(), treeId, line);
            return false;
        }

        talentCache.invalidate();
        talentPoints.add(1);

        TalentRecord record = treeRecord.getNodeRecord(line, index);
        if (record != null) {
            playerData.getTalentHandler().onTalentRecordUpdated(record);
        }
        return true;
    }

    static class PassiveTalentGroup extends ActiveTalentAbilityGroup {

        public PassiveTalentGroup(MKPlayerData playerData, String name) {
            super(playerData, name, AbilitySlot.Passive, GameConstants.DEFAULT_PASSIVES, GameConstants.MAX_PASSIVES, TalentType.PASSIVE);
        }

        @Override
        protected void onSlotChanged(int index, ResourceLocation previous, ResourceLocation newAbility) {
            playerData.getTalentHandler().getTypeHandler(TalentType.PASSIVE).onSlotChanged(index, previous, newAbility);
            super.onSlotChanged(index, previous, newAbility);
        }
    }

    static class UltimateTalentGroup extends ActiveTalentAbilityGroup {

        public UltimateTalentGroup(MKPlayerData playerData, String name) {
            super(playerData, name, AbilitySlot.Ultimate, GameConstants.DEFAULT_ULTIMATES, GameConstants.MAX_ULTIMATES, TalentType.ULTIMATE);
        }

        @Override
        protected void onSlotChanged(int index, ResourceLocation previous, ResourceLocation newAbility) {
            playerData.getTalentHandler().getTypeHandler(TalentType.ULTIMATE).onSlotChanged(index, previous, newAbility);
            super.onSlotChanged(index, previous, newAbility);
        }
    }

    public <T> T serialize(DynamicOps<T> ops) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(ops.createString("talentPoints"), ops.createInt(talentPoints.get()));
        builder.put(ops.createString("totalPoints"), ops.createInt(totalTalentPoints.get()));
        builder.put(ops.createString("trees"), ops.createMap(talentTreeRecordMap.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                kv -> ops.createString(kv.getKey().toString()),
                                kv -> kv.getValue().serialize(ops)
                        )
                )));

        return ops.createMap(builder.build());
    }

    public <T> void deserialize(Dynamic<T> dynamic) {
        talentPoints.set(dynamic.get("talentPoints").asInt(0));
        totalTalentPoints.set(dynamic.get("totalPoints").asInt(0));

        dynamic.get("trees")
                .asMap(Dynamic::asString, Function.identity())
                .forEach((idOpt, dyn) -> idOpt.map(ResourceLocation::new).result().ifPresent(id -> deserializeTree(id, dyn)));

        talentCache.invalidate();
    }

    private <T> void deserializeTree(ResourceLocation id, Dynamic<T> dyn) {
        if (unlockTree(id)) {
            if (!getTree(id).deserialize(dyn)) {
                MKCore.LOGGER.error("Player {} had invalid talent layout. Needs reset.", playerData.getEntity());
            }
        } else {
            MKCore.LOGGER.error("PlayerTalentKnowledge.deserializeTree failed for tree {} {}", id, dyn);
        }
    }

    public INBT serializeNBT() {
        return serialize(NBTDynamicOps.INSTANCE);
    }

    public void deserializeNBT(INBT tag) {
        deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE, tag));
    }

    private static class KnownTalentCache {
        private final Map<ResourceLocation, TalentTreeRecord> parent;
        private final Map<TalentType<?>, Set<MKTalent>> typeCache = new HashMap<>();
        private boolean needsRebuild;

        public KnownTalentCache(PlayerTalentKnowledge knowledge) {
            parent = knowledge.talentTreeRecordMap;
            invalidate();
        }

        public void invalidate() {
            needsRebuild = true;
        }

        public void rebuild() {
            typeCache.clear();
            parent.forEach((treeId, treeRecord) ->
                    treeRecord.getRecordStream().forEach(record -> {
                        TalentNode node = record.getNode();
                        if (record.isKnown()) {
                            typeCache.computeIfAbsent(node.getTalentType(), type -> new HashSet<>()).add(node.getTalent());
                        }
                    }));
        }

        private Map<TalentType<?>, Set<MKTalent>> getTypeCache() {
            if (needsRebuild) {
                rebuild();
                needsRebuild = false;
            }
            return typeCache;
        }

        public boolean hasAnyOfType(TalentType<?> type) {
            return !getTypeCache().getOrDefault(type, Collections.emptySet()).isEmpty();
        }

        public Set<ResourceLocation> getKnownTalentIds(TalentType<?> type) {
            return Collections.unmodifiableSet(getTypeCache()
                    .getOrDefault(type, Collections.emptySet()).stream()
                    .map(ForgeRegistryEntry::getRegistryName)
                    .collect(Collectors.toSet()));
        }
    }

    class ClientTreeSyncGroup extends SyncGroup {

        @Override
        public void deserializeUpdate(CompoundNBT tag) {
            MKCore.getTalentManager()
                    .getTreeNames()
                    .stream()
                    .filter(treeId -> tag.contains(treeId.toString()))
                    .filter(treeId -> !talentTreeRecordMap.containsKey(treeId))
                    .map(PlayerTalentKnowledge.this::unlockTreeInternal)
                    .filter(Objects::nonNull)
                    .forEach(record -> {
                        record.setUpdateCallback(ignore -> talentCache.invalidate());
                        add(record.getUpdater());
                    });

            super.deserializeUpdate(tag);
        }

        @Override
        public void serializeUpdate(CompoundNBT tag) {
            throw new IllegalStateException("ClientTreeSyncGroup should never call serializeUpdate!");
        }

        @Override
        public void serializeFull(CompoundNBT tag) {
            throw new IllegalStateException("ClientTreeSyncGroup should never call serializeFull!");
        }
    }
}
