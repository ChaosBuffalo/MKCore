package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.sync.ResourceListUpdater;
import com.chaosbuffalo.mkcore.sync.SyncInt;
import com.chaosbuffalo.mkcore.sync.SyncListUpdater;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

public class ActiveAbilityGroup implements IPlayerSyncComponentProvider {
    protected final MKPlayerData playerData;
    protected final SyncComponent sync;
    protected final String name;
    private final List<ResourceLocation> activeAbilities;
    private final SyncListUpdater<ResourceLocation> activeUpdater;
    private final SyncInt slots;
    protected final AbilityGroupId group;

    public ActiveAbilityGroup(MKPlayerData playerData, String name, AbilityGroupId group) {
        this(playerData, name, group, group.getDefaultSlots(), group.getMaxSlots());
    }

    public ActiveAbilityGroup(MKPlayerData playerData, String name, AbilityGroupId group, int defaultSize, int max) {
        sync = new SyncComponent(name);
        this.playerData = playerData;
        this.name = name;
        this.group = group;
        activeAbilities = NonNullList.withSize(max, MKCoreRegistry.INVALID_ABILITY);
        activeUpdater = new ResourceListUpdater("active", () -> activeAbilities);
        slots = new SyncInt("slots", defaultSize);
        addSyncPrivate(activeUpdater);
        addSyncPrivate(slots);
    }

    @Override
    public SyncComponent getSyncComponent() {
        return sync;
    }

    public List<ResourceLocation> getAbilities() {
        return Collections.unmodifiableList(activeAbilities);
    }

    public int getCurrentSlotCount() {
        return slots.get();
    }

    public int getMaximumSlotCount() {
        return activeAbilities.size();
    }

    public boolean setSlots(int newSlotCount) {
        if (newSlotCount < 0 || newSlotCount > getMaximumSlotCount()) {
            MKCore.LOGGER.error("setSlots({}, {}) - bad count", newSlotCount, getMaximumSlotCount());
            return false;
        }

        int currentCount = getCurrentSlotCount();
        slots.set(newSlotCount);

        if (newSlotCount > currentCount) {
            for (int i = currentCount; i < newSlotCount; i++) {
                onSlotUnlocked(i);
            }
        } else if (newSlotCount < currentCount) {
            for (int i = newSlotCount; i < currentCount; i++) {
                onSlotLocked(i);
            }
        }
        return true;
    }

    protected void onSlotLocked(int slot) {
        clearSlot(slot);
    }

    protected void onSlotUnlocked(int slot) {

    }

    protected boolean canSlotAbility(int slot, ResourceLocation abilityId) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability == null)
            return false;

        return this.group.fitsAbilityType(ability.getType());
    }

    protected int getFirstFreeAbilitySlot() {
        return getAbilitySlot(MKCoreRegistry.INVALID_ABILITY);
    }

    public int tryEquip(ResourceLocation abilityId) {
        int slot = getAbilitySlot(abilityId);
        if (slot == GameConstants.ACTION_BAR_INVALID_SLOT) {
            // Skill was just learned so let's try to put it on the bar
            slot = getFirstFreeAbilitySlot();
            if (slot != GameConstants.ACTION_BAR_INVALID_SLOT && slot < getCurrentSlotCount()) {
                setSlot(slot, abilityId);
            }
        }

        return slot;
    }

    public int getAbilitySlot(ResourceLocation abilityId) {
        int slot = getAbilities().indexOf(abilityId);
        if (slot != -1)
            return slot;
        return GameConstants.ACTION_BAR_INVALID_SLOT;
    }

    public boolean isAbilitySlotted(ResourceLocation abilityId) {
        return getAbilitySlot(abilityId) != GameConstants.ACTION_BAR_INVALID_SLOT;
    }

    @Nonnull
    public ResourceLocation getSlot(int slot) {
        List<ResourceLocation> list = getAbilities();
        if (slot < list.size()) {
            return list.get(slot);
        }
        return MKCoreRegistry.INVALID_ABILITY;
    }

    public void setSlot(int index, ResourceLocation abilityId) {
        MKCore.LOGGER.debug("ActiveAbilityContainer.setAbilityInSlot({}, {}, {})", group, index, abilityId);

        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            setSlotInternal(index, MKCoreRegistry.INVALID_ABILITY);
            return;
        }

        if (group.requiresAbilityKnown() && !playerData.getAbilities().knowsAbility(abilityId)) {
            MKCore.LOGGER.error("setAbilityInSlot({}, {}, {}) - player does not know ability!", group, index, abilityId);
            return;
        }

        if (!canSlotAbility(index, abilityId)) {
            MKCore.LOGGER.error("setAbilityInSlot({}, {}, {}) - blocked by ability container", group, index, abilityId);
            return;
        }

        if (index < activeAbilities.size()) {
            for (int i = 0; i < activeAbilities.size(); i++) {
                if (i != index && abilityId.equals(activeAbilities.get(i))) {
                    // Ability is currently at i, but is moving to index
                    internalSwapSlots(i, index);
                }
            }
            setSlotInternal(index, abilityId);
        }
    }

    public boolean isSlotUnlocked(int slot) {
        return slot < getCurrentSlotCount();
    }

    public boolean isSlotFilled(int slot) {
        if (!isSlotUnlocked(slot)) {
            return false;
        }
        return !getSlot(slot).equals(MKCoreRegistry.INVALID_ABILITY);
    }

    public void resetSlots() {
        for (int i = 0; i < getAbilities().size(); i++) {
            clearSlot(i);
        }
    }

    public int getFilledSlotCount() {
        return (int) IntStream.range(0, getCurrentSlotCount())
                .filter(this::isSlotFilled)
                .count();
    }

    public int getHighestFilledSlot() {
        return IntStream.range(0, getCurrentSlotCount())
                .filter(this::isSlotFilled)
                .max()
                .orElse(-1);
    }

    public void clearAbility(ResourceLocation abilityId) {
        int slot = getAbilitySlot(abilityId);
        if (slot != GameConstants.ACTION_BAR_INVALID_SLOT) {
            clearSlot(slot);
        }
    }

    public void executeSlot(int index) {
        ResourceLocation abilityId = getSlot(index);
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
            return;
        playerData.getAbilityExecutor().executeAbility(abilityId);
    }

    public void clearSlot(int slot) {
        setSlot(slot, MKCoreRegistry.INVALID_ABILITY);
    }

    protected void setSlotInternal(int index, ResourceLocation abilityId) {
        MKCore.LOGGER.debug("ActiveAbilityContainer.setSlotInternal({}, {}, {})", group, index, abilityId);
        ResourceLocation previous = activeAbilities.set(index, abilityId);
        activeUpdater.setDirty(index);
        if (playerData.getEntity().isAddedToWorld()) {
            onSlotChanged(index, previous, abilityId);
        }
    }

    protected void internalSwapSlots(int oldSlot, int newSlot) {
        ResourceLocation original = activeAbilities.get(oldSlot);
        ResourceLocation previous = activeAbilities.get(newSlot);
        activeAbilities.set(oldSlot, previous);
        activeAbilities.set(newSlot, original);
        activeUpdater.setDirty(oldSlot);
        activeUpdater.setDirty(newSlot);
    }

    public void onAbilityLearned(MKAbilityInfo info) {
        if (info.getSource().placeOnBarWhenLearned()) {
            tryEquip(info.getId());
        }
    }

    public void onAbilityUnlearned(MKAbilityInfo info) {
        clearAbility(info.getId());
    }

    protected void onSlotChanged(int index, ResourceLocation previous, ResourceLocation newAbility) {
        playerData.getAbilityExecutor().onSlotChanged(group, index, previous, newAbility);
    }

    private void ensureValidAbility(ResourceLocation abilityId) {
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
            return;

        MKAbilityInfo info = playerData.getAbilities().getKnownAbility(abilityId);
        if (info != null)
            return;

        MKCore.LOGGER.debug("ensureValidAbility({}, {}) - bad", group, abilityId);
        clearAbility(abilityId);
    }

    public void onPersonaSwitch() {
        getAbilities().forEach(this::ensureValidAbility);
    }

    public <T> T serialize(DynamicOps<T> ops) {
        return ops.createMap(
                ImmutableMap.of(
                        ops.createString("slots"),
                        ops.createInt(getCurrentSlotCount()),
                        ops.createString("abilities"),
                        ops.createList(activeAbilities.stream().map(ResourceLocation::toString).map(ops::createString))
                )
        );
    }

    public <T> void deserialize(Dynamic<T> dynamic) {
        slots.set(dynamic.get("slots").asInt(getCurrentSlotCount()));
        deserializeAbilityList(dynamic.get("abilities").orElseEmptyList(), this::setSlotInternal);
    }

    public INBT serializeNBT() {
        return serialize(NBTDynamicOps.INSTANCE);
    }

    public void deserializeNBT(INBT tag) {
        deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE, tag));
    }

    private <T> void deserializeAbilityList(Dynamic<T> dynamic, BiConsumer<Integer, ResourceLocation> consumer) {
        List<DataResult<String>> passives = dynamic.asList(Dynamic::asString);
        for (int i = 0; i < passives.size(); i++) {
            int index = i;
            passives.get(i).resultOrPartial(MKCore.LOGGER::error).ifPresent(idString -> {
                ResourceLocation abilityId = new ResourceLocation(idString);
                MKAbility ability = MKCoreRegistry.getAbility(abilityId);
                if (ability != null) {
                    consumer.accept(index, abilityId);
                }
            });
        }
    }
}
