package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementTypeHandler;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.records.IRecordType;
import net.minecraft.util.ResourceLocation;

public class AbilitySlotEntitlement extends MKEntitlement {
    private final AbilityType abilityType;
    private final IRecordType<AbilitySlotEntitlementHandler> recordType;

    public AbilitySlotEntitlement(ResourceLocation name, AbilityType abilityType, int maxEntitlements) {
        super(name, maxEntitlements);
        this.abilityType = abilityType;
        recordType = playerData -> new AbilitySlotEntitlementHandler(playerData, this);
    }

    @Override
    public IRecordType<?> getRecordType() {
        return recordType;
    }

    public static class AbilitySlotEntitlementHandler extends EntitlementTypeHandler {
        private final MKPlayerData playerData;
        private final AbilitySlotEntitlement entitlement;

        public AbilitySlotEntitlementHandler(MKPlayerData playerData, AbilitySlotEntitlement entitlement) {
            this.playerData = playerData;
            this.entitlement = entitlement;
        }

        private void recalculateSlots() {
            int count = playerData.getKnowledge().getEntitlementsKnowledge().getEntitlementLevel(entitlement);
            playerData.getAbilityLoadout().getAbilityGroup(entitlement.abilityType).setSlots(count);
        }

        @Override
        public void onRecordUpdated(EntitlementInstance record) {
            recalculateSlots();
        }

        @Override
        public void onRecordLoaded(EntitlementInstance record) {
            recalculateSlots();
        }
    }
}
