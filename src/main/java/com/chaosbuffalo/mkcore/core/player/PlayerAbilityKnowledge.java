package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.IMKAbilityKnowledge;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.sync.SyncInt;
import com.chaosbuffalo.mkcore.sync.SyncMapUpdater;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class PlayerAbilityKnowledge implements IMKAbilityKnowledge, IPlayerSyncComponentProvider {
    private final MKPlayerData playerData;
    private final PlayerSyncComponent sync = new PlayerSyncComponent("abilities");
    private final Map<ResourceLocation, MKAbilityInfo> abilityInfoMap = new HashMap<>();
    private final SyncInt poolSize = new SyncInt("poolSize", GameConstants.DEFAULT_ABILITY_POOL_SIZE);
    private final SyncMapUpdater<ResourceLocation, MKAbilityInfo> knownAbilityUpdater =
            new SyncMapUpdater<>("known",
                    () -> abilityInfoMap,
                    ResourceLocation::toString,
                    ResourceLocation::tryCreate,
                    PlayerAbilityKnowledge::createAbilityInfo
            );

    public PlayerAbilityKnowledge(MKPlayerData playerData) {
        this.playerData = playerData;
        addSyncPrivate(knownAbilityUpdater);
        addSyncPrivate(poolSize);
    }

    public int getAbilityPoolSize() {
        return poolSize.get();
    }

    public void modifyAbilityPoolSize(int delta) {
        poolSize.add(delta);
    }

    public void setAbilityPoolSize(int count) {
        poolSize.set(MathHelper.clamp(count, GameConstants.DEFAULT_ABILITY_POOL_SIZE, GameConstants.MAX_ABILITY_POOL_SIZE));
    }

    private Stream<ResourceLocation> getPoolAbilityStream() {
        // This can be cached easily if it ever becomes a problem
        return getKnownStream()
                .filter(info -> info.getAbility().getType().isPoolAbility())
                .map(MKAbilityInfo::getId);
    }

    public List<ResourceLocation> getPoolAbilities() {
        return getPoolAbilityStream().collect(Collectors.toList());
    }

    public int getCurrentPoolCount() {
        return (int) getPoolAbilityStream().count();
    }

    public boolean isAbilityPoolFull() {
        return getCurrentPoolCount() >= getAbilityPoolSize();
    }

    @Override
    public PlayerSyncComponent getSyncComponent() {
        return sync;
    }

    @Override
    public Collection<MKAbilityInfo> getAllAbilities() {
        return Collections.unmodifiableCollection(abilityInfoMap.values());
    }

    public Stream<MKAbilityInfo> getKnownStream() {
        return abilityInfoMap.values().stream().filter(MKAbilityInfo::isCurrentlyKnown);
    }

    private boolean hasRoomForAbility(MKAbility ability) {
        return !ability.getType().isPoolAbility() || !isAbilityPoolFull();
    }

    private IActiveAbilityGroup getAbilityGroup(MKAbility ability) {
        return playerData.getAbilityLoadout().getAbilityGroup(ability.getType().getSlotType());
    }

    @Override
    public boolean learnAbility(MKAbility ability, AbilitySource source) {
        if (!hasRoomForAbility(ability)) {
            MKCore.LOGGER.warn("Player {} tried to learn pool ability {} with a full pool ({}/{})",
                    playerData.getEntity(), ability.getAbilityId(), getCurrentPoolCount(), getAbilityPoolSize());
            return false;
        }

        MKAbilityInfo info = abilityInfoMap.computeIfAbsent(ability.getAbilityId(), id -> ability.createAbilityInfo(source));
        if (info.isCurrentlyKnown()) {
            MKCore.LOGGER.warn("Player {} tried to learn already-known ability {}", playerData.getEntity(), ability.getAbilityId());
            return true;
        }

        info.setKnown(true);
        markDirty(info);

        getAbilityGroup(ability).onAbilityLearned(info);
        return true;
    }

    public boolean learnAbility(MKAbility ability, AbilitySource source, ResourceLocation replacingAbilityId) {
        if (!hasRoomForAbility(ability)) {
            return false;
        }

        if (!replacingAbilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            if (!unlearnAbility(replacingAbilityId)) {
                return false;
            }
        }

        return learnAbility(ability, source);
    }

    @Override
    public boolean unlearnAbility(ResourceLocation abilityId) {
        MKAbilityInfo info = getKnownAbility(abilityId);
        if (info == null) {
            MKCore.LOGGER.error("{} tried to unlearn unknown ability {}", playerData.getEntity(), abilityId);
            return false;
        }
        info.setKnown(false);
        markDirty(info);

        MKAbility ability = info.getAbility();
        playerData.getAbilityExecutor().onAbilityUnlearned(ability);
        getAbilityGroup(ability).onAbilityUnlearned(info);
        return true;
    }

    @Override
    public boolean knowsAbility(ResourceLocation abilityId) {
        return getKnownAbility(abilityId) != null;
    }

    @Nullable
    public MKAbilityInfo getKnownAbility(ResourceLocation abilityId) {
        MKAbilityInfo info = abilityInfoMap.get(abilityId);
        if (info == null || !info.isCurrentlyKnown())
            return null;
        return info;
    }

    private void markDirty(MKAbilityInfo info) {
        knownAbilityUpdater.markDirty(info.getId());
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.put("known", knownAbilityUpdater.serializeStorage());
        tag.putInt("poolSize", poolSize.get());
        return tag;
    }

    public void deserialize(CompoundNBT tag) {
        knownAbilityUpdater.deserializeStorage(tag.get("known"));
        setAbilityPoolSize(tag.getInt("poolSize"));
    }

    private static MKAbilityInfo createAbilityInfo(ResourceLocation abilityId) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability == null)
            return null;

        return ability.createAbilityInfo(AbilitySource.TRAINED);
    }
}
