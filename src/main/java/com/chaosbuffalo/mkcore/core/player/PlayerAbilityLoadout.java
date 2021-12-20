package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.talents.ActiveTalentAbilityGroup;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class PlayerAbilityLoadout implements IPlayerSyncComponentProvider {

    private final MKPlayerData playerData;
    private final SyncComponent sync = new SyncComponent("loadout");

    private final Map<AbilityGroupId, ActiveAbilityGroup> abilityGroups = new HashMap<>();
    private final ActiveTalentAbilityGroup passiveContainer;
    private final ActiveTalentAbilityGroup ultimateContainer;
    private final BasicAbilityGroup basicAbilityContainer;
    private final ItemAbilityGroup itemAbilityContainer;

    public PlayerAbilityLoadout(MKPlayerData playerData) {
        this.playerData = playerData;
        basicAbilityContainer = new BasicAbilityGroup(playerData);
        passiveContainer = new PassiveTalentGroup(playerData);
        ultimateContainer = new UltimateTalentGroup(playerData);
        itemAbilityContainer = new ItemAbilityGroup(playerData);
        registerAbilityContainer(AbilityGroupId.Basic, basicAbilityContainer);
        registerAbilityContainer(AbilityGroupId.Item, itemAbilityContainer);
        registerAbilityContainer(AbilityGroupId.Passive, passiveContainer);
        registerAbilityContainer(AbilityGroupId.Ultimate, ultimateContainer);
    }

    @Override
    public SyncComponent getSyncComponent() {
        return sync;
    }

    @Nonnull
    public ActiveAbilityGroup getAbilityGroup(AbilityGroupId group) {
        return abilityGroups.get(group);
    }

    private void registerAbilityContainer(AbilityGroupId group, ActiveAbilityGroup container) {
        abilityGroups.put(group, container);
        addSyncChild(container);
    }

    public ResourceLocation getAbilityInSlot(AbilityGroupId group, int slot) {
        return getAbilityGroup(group).getSlot(slot);
    }

    public ActiveTalentAbilityGroup getPassiveContainer() {
        return passiveContainer;
    }

    public ActiveTalentAbilityGroup getUltimateGroup() {
        return ultimateContainer;
    }

    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.put("basic", basicAbilityContainer.serializeNBT());
        tag.put("passive", passiveContainer.serializeNBT());
        tag.put("ultimate", ultimateContainer.serializeNBT());
        tag.put("item", itemAbilityContainer.serializeNBT());
        return tag;
    }

    public void deserializeNBT(CompoundNBT tag) {
        basicAbilityContainer.deserializeNBT(tag.get("basic"));
        passiveContainer.deserializeNBT(tag.get("passive"));
        ultimateContainer.deserializeNBT(tag.get("ultimate"));
        itemAbilityContainer.deserializeNBT(tag.get("item"));
    }

    public void onPersonaSwitch() {
        abilityGroups.values().forEach(ActiveAbilityGroup::onPersonaSwitch);
    }


    public static class BasicAbilityGroup extends ActiveAbilityGroup {

        public BasicAbilityGroup(MKPlayerData playerData) {
            super(playerData, "basic", AbilityGroupId.Basic);
        }
    }

    public static class ItemAbilityGroup extends ActiveAbilityGroup {

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
                    info = ability.createAbilityInfo(AbilitySource.ITEM);
                }
            }

            if (info != null) {
                playerData.getAbilityExecutor().executeAbilityInfoWithContext(info, null);
            }
        }
    }

    static class PassiveTalentGroup extends ActiveTalentAbilityGroup {

        public PassiveTalentGroup(MKPlayerData playerData) {
            super(playerData, "passive", AbilityGroupId.Passive, TalentType.PASSIVE);
        }

        @Override
        protected void onSlotChanged(int index, ResourceLocation previous, ResourceLocation newAbility) {
            playerData.getTalents().getTypeHandler(TalentType.PASSIVE).onSlotChanged(index, previous, newAbility);
            super.onSlotChanged(index, previous, newAbility);
        }
    }

    static class UltimateTalentGroup extends ActiveTalentAbilityGroup {

        public UltimateTalentGroup(MKPlayerData playerData) {
            super(playerData, "ultimate", AbilityGroupId.Ultimate, TalentType.ULTIMATE);
        }

        @Override
        protected void onSlotChanged(int index, ResourceLocation previous, ResourceLocation newAbility) {
            playerData.getTalents().getTypeHandler(TalentType.ULTIMATE).onSlotChanged(index, previous, newAbility);
            super.onSlotChanged(index, previous, newAbility);
        }
    }
}
