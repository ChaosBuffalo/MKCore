package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.IPlayerSyncComponentProvider;
import com.chaosbuffalo.mkcore.core.player.SyncComponent;
import com.chaosbuffalo.mkcore.init.CoreSounds;
import com.chaosbuffalo.mkcore.sync.DynamicSyncGroup;
import com.chaosbuffalo.mkcore.sync.SyncInt;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerTalentKnowledge implements IPlayerSyncComponentProvider {
    private final MKPlayerData playerData;
    private final SyncComponent sync = new SyncComponent("talents");
    private final SyncInt talentPoints = new SyncInt("points", 0);
    private final SyncInt totalTalentPoints = new SyncInt("totalPoints", 0);
    private final Map<ResourceLocation, TalentTreeRecord> talentTreeRecordMap = new HashMap<>();
    private final SyncInt talentXp = new SyncInt("xp", 0);

    public PlayerTalentKnowledge(MKPlayerData playerData) {
        this.playerData = playerData;
        addSyncPrivate(talentPoints);
        addSyncPrivate(totalTalentPoints);
        addSyncPrivate(talentXp);
        if (!playerData.isServerSide()) {
            addSyncPrivate(new ClientTreeSyncGroup());
        }
        if (playerData.isServerSide()){
            for (TalentTreeDefinition def : MKCore.getTalentManager().getDefaultTrees()){
                if (!unlockTree(def.getTreeId())){
                    MKCore.LOGGER.error("Failed to unlock default talent tree: {}", def.getTreeId());
                }
            }
        }
    }

    public int getTalentXp(){
        return talentXp.get();
    }

    public int getXpToNextLevel(){
        return 100 + Math.round((getTotalTalentPoints() / 2.0f) * 100);
    }

    public boolean shouldLevel(){
        return getTalentXp() >= getXpToNextLevel();
    }

    public void addTalentXp(int value){
        talentXp.add(value);
        if (shouldLevel()){
            performLevel();
        }
    }

    public void performLevel(){
        talentXp.add(-getXpToNextLevel());
        grantTalentPoints(1);
        if (playerData.isServerSide()){
            SoundUtils.serverPlaySoundAtEntity(playerData.getEntity(), CoreSounds.level_up, SoundCategory.PLAYERS);
        }
    }

    @Override
    public SyncComponent getSyncComponent() {
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
        return getKnownTalentsStream(type)
                .map(record -> record.getNode().getTalent().getTalentId())
                .collect(Collectors.toSet());
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

        talentPoints.add(1);

        TalentRecord record = treeRecord.getNodeRecord(line, index);
        if (record != null) {
            playerData.getTalentHandler().onTalentRecordUpdated(record);
        }
        return true;
    }

    public <T> T serialize(DynamicOps<T> ops) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
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
        totalTalentPoints.set(dynamic.get("totalPoints").asInt(0));
        talentPoints.set(totalTalentPoints.get());

        dynamic.get("trees")
                .asMap(Dynamic::asString, Function.identity())
                .forEach((idOpt, dyn) -> idOpt.map(ResourceLocation::new).result().ifPresent(id -> deserializeTree(id, dyn)));
    }

    private <T> void deserializeTree(ResourceLocation treeId, Dynamic<T> dyn) {
        TalentTreeDefinition tree = MKCore.getTalentManager().getTalentTree(treeId);
        if (tree == null) {
            MKCore.LOGGER.warn("Player {} tried to unlock unknown tree {}", playerData.getEntity(), treeId);
            return;
        }

        TalentTreeRecord treeRecord = tree.createRecord();
        if (!treeRecord.deserialize(dyn)) {
            MKCore.LOGGER.error("Player {} had invalid talent layout for tree {}. Points will be refunded.", playerData.getEntity(), treeId);
        } else {
            // If the tree deserializes properly subtract the points spent in it from the total points
            talentPoints.add(-treeRecord.getPointsSpent());

            talentTreeRecordMap.put(tree.getTreeId(), treeRecord);
            sync.addPrivate(treeRecord.getUpdater(), true);
        }
    }

    public INBT serializeNBT() {
        return serialize(NBTDynamicOps.INSTANCE);
    }

    public void deserializeNBT(INBT tag) {
        deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE, tag));
    }

    class ClientTreeSyncGroup extends DynamicSyncGroup {

        @Override
        protected void onKey(String key) {
            ResourceLocation treeId = ResourceLocation.tryCreate(key);
            if (treeId == null)
                return;

            if (MKCore.getTalentManager().getTalentTree(treeId) != null && !talentTreeRecordMap.containsKey(treeId)) {
                TalentTreeRecord treeRecord = unlockTreeInternal(treeId);
                if (treeRecord != null) {
                    add(treeRecord.getUpdater());
                }
            }
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
