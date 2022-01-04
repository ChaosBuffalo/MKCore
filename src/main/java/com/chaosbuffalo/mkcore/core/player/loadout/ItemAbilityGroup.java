package com.chaosbuffalo.mkcore.core.player.loadout;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import net.minecraft.util.ResourceLocation;

public class ItemAbilityGroup extends AbilityGroup {

    public ItemAbilityGroup(MKPlayerData playerData) {
        super(playerData, "item", AbilityGroupId.Item);
    }

    @Override
    public int getCurrentSlotCount() {
        // Only report nonzero if the slot is filled
        return !getSlot(0).equals(MKCoreRegistry.INVALID_ABILITY) ? 1 : 0;
    }

    @Override
    public void executeSlot(int index) {
        ResourceLocation abilityId = getSlot(index);
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
            return;

        // If the ability is already known by another method, lookup that info
        MKAbilityInfo info = playerData.getAbilities().getKnownAbility(abilityId);
        if (info == null) {
            // If not, create a temporary info struct
            MKAbility ability = MKCoreRegistry.getAbility(abilityId);
            if (ability != null) {
                info = ability.createAbilityInfo();
                info.addSource(AbilitySource.forItem(playerData.getEntity().getHeldItemMainhand()));
            }
        }

        if (info != null) {
            playerData.getAbilityExecutor().executeAbilityInfoWithContext(info, null);
        }
    }
}
