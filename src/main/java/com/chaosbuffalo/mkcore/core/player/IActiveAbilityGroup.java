package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public interface IActiveAbilityGroup {

    List<ResourceLocation> getAbilities();

    void setSlot(int index, ResourceLocation abilityId);

    default int tryEquip(ResourceLocation abilityId) {
        return GameConstants.ACTION_BAR_INVALID_SLOT;
    }

    @Nonnull
    default ResourceLocation getSlot(int slot) {
        List<ResourceLocation> list = getAbilities();
        if (slot < list.size()) {
            return list.get(slot);
        }
        return MKCoreRegistry.INVALID_ABILITY;
    }

    int getCurrentSlotCount();

    int getMaximumSlotCount();

    boolean setSlots(int count);

    void clearSlot(int slot);

    void clearAbility(ResourceLocation abilityId);

    default void onAbilityLearned(MKAbilityInfo info) {
        if (info.getSource().placeOnBarWhenLearned()) {
            tryEquip(info.getId());
        }
    }

    default void onAbilityUnlearned(MKAbilityInfo info) {
        clearAbility(info.getId());
    }

    default boolean isSlotUnlocked(int slot) {
        return slot < getCurrentSlotCount();
    }

    default void resetSlots() {
        for (int i = 0; i < getAbilities().size(); i++) {
            clearSlot(i);
        }
    }

    default int getAbilitySlot(ResourceLocation abilityId) {
        int slot = getAbilities().indexOf(abilityId);
        if (slot != -1)
            return slot;
        return GameConstants.ACTION_BAR_INVALID_SLOT;
    }

    default boolean isAbilitySlotted(ResourceLocation abilityId) {
        return getAbilitySlot(abilityId) != GameConstants.ACTION_BAR_INVALID_SLOT;
    }

    void onPersonaSwitch();

    IActiveAbilityGroup EMPTY = new IActiveAbilityGroup() {
        @Override
        public List<ResourceLocation> getAbilities() {
            return Collections.emptyList();
        }

        @Override
        public void setSlot(int index, ResourceLocation abilityId) {

        }

        @Nonnull
        @Override
        public ResourceLocation getSlot(int slot) {
            return MKCoreRegistry.INVALID_ABILITY;
        }

        @Override
        public int getCurrentSlotCount() {
            return 0;
        }

        public int getMaximumSlotCount() {
            return 0;
        }

        @Override
        public boolean setSlots(int count) {
            return false;
        }

        @Override
        public void clearSlot(int slot) {

        }

        @Override
        public void clearAbility(ResourceLocation abilityId) {

        }

        @Override
        public void onPersonaSwitch() {

        }
    };
}
