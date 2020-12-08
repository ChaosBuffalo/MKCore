package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.AbilitySlot;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class ActiveAbilityGroup implements IActiveAbilityGroup, IPlayerSyncComponentProvider {
    protected final MKPlayerData playerData;
    protected final PlayerSyncComponent sync;
    protected final String name;
    private final List<ResourceLocation> activeAbilities;
    private final SyncListUpdater<ResourceLocation> activeUpdater;
    private final SyncInt slots;
    protected final AbilitySlot slot;

    public ActiveAbilityGroup(MKPlayerData playerData, String name, AbilitySlot slot, int defaultSize, int max) {
        sync = new PlayerSyncComponent(name);
        this.playerData = playerData;
        this.name = name;
        this.slot = slot;
        activeAbilities = NonNullList.withSize(max, MKCoreRegistry.INVALID_ABILITY);
        activeUpdater = new ResourceListUpdater("active", () -> activeAbilities);
        slots = new SyncInt("slots", defaultSize);
        addSyncPrivate(activeUpdater);
        addSyncPrivate(slots);
    }

    @Override
    public PlayerSyncComponent getSyncComponent() {
        return sync;
    }

    @Override
    public List<ResourceLocation> getAbilities() {
        return Collections.unmodifiableList(activeAbilities);
    }

    @Override
    public int getCurrentSlotCount() {
        return slots.get();
    }

    @Override
    public int getMaximumSlotCount() {
        return activeAbilities.size();
    }

    @Override
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

        return ability.getType().fitsSlot(this.slot);
    }

    protected int getFirstFreeAbilitySlot() {
        return getAbilitySlot(MKCoreRegistry.INVALID_ABILITY);
    }

    @Override
    public int trySlot(ResourceLocation abilityId) {
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

    @Override
    public void setSlot(int index, ResourceLocation abilityId) {
        MKCore.LOGGER.debug("ActiveAbilityContainer.setAbilityInSlot({}, {}, {})", slot, index, abilityId);

        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            setSlotInternal(index, MKCoreRegistry.INVALID_ABILITY);
            return;
        }

        if (!playerData.getAbilities().knowsAbility(abilityId)) {
            MKCore.LOGGER.error("setAbilityInSlot({}, {}, {}) - player does not know ability!", slot, index, abilityId);
            return;
        }

        if (!canSlotAbility(index, abilityId)) {
            MKCore.LOGGER.error("setAbilityInSlot({}, {}, {}) - blocked by ability container", slot, index, abilityId);
            return;
        }

        if (index < activeAbilities.size()) {
            for (int i = 0; i < activeAbilities.size(); i++) {
                if (i != index && abilityId.equals(activeAbilities.get(i))) {
                    setSlotInternal(i, activeAbilities.get(index));
                }
            }
            setSlotInternal(index, abilityId);
        }
    }

    @Override
    public void clearAbility(ResourceLocation abilityId) {
        int slot = getAbilitySlot(abilityId);
        if (slot != GameConstants.ACTION_BAR_INVALID_SLOT) {
            clearSlot(slot);
        }
    }

    @Override
    public void clearSlot(int slot) {
        setSlot(slot, MKCoreRegistry.INVALID_ABILITY);
    }

    protected void setSlotInternal(int index, ResourceLocation abilityId) {
        MKCore.LOGGER.debug("ActiveAbilityContainer.setSlotInternal({}, {}, {})", slot, index, abilityId);
        ResourceLocation previous = activeAbilities.set(index, abilityId);
        activeUpdater.setDirty(index);
        if (playerData.getEntity().isAddedToWorld()) {
            onSlotChanged(index, previous, abilityId);
        }
    }

    protected void onSlotChanged(int index, ResourceLocation previous, ResourceLocation newAbility) {
        playerData.getAbilityExecutor().onSlotChanged(slot, index, previous, newAbility);
    }

    private void ensureValidAbility(ResourceLocation abilityId) {
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
            return;

        MKAbilityInfo info = playerData.getAbilities().getKnownAbility(abilityId);
        if (info != null)
            return;

        MKCore.LOGGER.debug("checkHotBar({}, {}) - bad", slot, abilityId);
        clearAbility(abilityId);
    }

    @Override
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
            passives.get(i).result().ifPresent(idString -> {
                ResourceLocation abilityId = new ResourceLocation(idString);
                MKAbility ability = MKCoreRegistry.getAbility(abilityId);
                if (ability != null) {
                    consumer.accept(index, abilityId);
                }
            });
        }
    }
}
