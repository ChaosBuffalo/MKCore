package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.AbilitySlot;
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
    private final PlayerSyncComponent sync = new PlayerSyncComponent("loadout");

    private final Map<AbilitySlot, IActiveAbilityGroup> abilityGroups = new HashMap<>();
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
        registerAbilityContainer(AbilitySlot.Basic, basicAbilityContainer);
        registerAbilityContainer(AbilitySlot.Item, itemAbilityContainer);
        registerAbilityContainer(AbilitySlot.Passive, passiveContainer);
        registerAbilityContainer(AbilitySlot.Ultimate, ultimateContainer);
    }

    @Override
    public PlayerSyncComponent getSyncComponent() {
        return sync;
    }

    @Nonnull
    public IActiveAbilityGroup getAbilityGroup(AbilitySlot type) {
        return abilityGroups.getOrDefault(type, IActiveAbilityGroup.EMPTY);
    }

    private void registerAbilityContainer(AbilitySlot type, ActiveAbilityGroup container) {
        abilityGroups.put(type, container);
        addSyncChild(container);
    }

    public ResourceLocation getAbilityInSlot(AbilitySlot type, int slot) {
        return getAbilityGroup(type).getSlot(slot);
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
        abilityGroups.values().forEach(IActiveAbilityGroup::onPersonaSwitch);
    }


    public static class BasicAbilityGroup extends ActiveAbilityGroup {

        public BasicAbilityGroup(MKPlayerData playerData) {
            super(playerData, "basic", AbilitySlot.Basic, GameConstants.DEFAULT_ACTIVES, GameConstants.MAX_ACTIVES);
        }
    }

    public static class ItemAbilityGroup extends ActiveAbilityGroup {

        public ItemAbilityGroup(MKPlayerData playerData) {
            super(playerData, "item", AbilitySlot.Item, 1, 1);
        }

        @Override
        public int getCurrentSlotCount() {
            // Only report nonzero if the slot is filled
            return !getSlot(0).equals(MKCoreRegistry.INVALID_ABILITY) ? 1 : 0;
        }
    }

    static class PassiveTalentGroup extends ActiveTalentAbilityGroup {

        public PassiveTalentGroup(MKPlayerData playerData) {
            super(playerData, "passive", AbilitySlot.Passive, GameConstants.DEFAULT_PASSIVES, GameConstants.MAX_PASSIVES, TalentType.PASSIVE);
        }

        @Override
        protected void onSlotChanged(int index, ResourceLocation previous, ResourceLocation newAbility) {
            playerData.getTalentHandler().getTypeHandler(TalentType.PASSIVE).onSlotChanged(index, previous, newAbility);
            super.onSlotChanged(index, previous, newAbility);
        }
    }

    static class UltimateTalentGroup extends ActiveTalentAbilityGroup {

        public UltimateTalentGroup(MKPlayerData playerData) {
            super(playerData, "ultimate", AbilitySlot.Ultimate, GameConstants.DEFAULT_ULTIMATES, GameConstants.MAX_ULTIMATES, TalentType.ULTIMATE);
        }

        @Override
        protected void onSlotChanged(int index, ResourceLocation previous, ResourceLocation newAbility) {
            playerData.getTalentHandler().getTypeHandler(TalentType.ULTIMATE).onSlotChanged(index, previous, newAbility);
            super.onSlotChanged(index, previous, newAbility);
        }
    }
}
