package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.MKToggleAbility;
import com.chaosbuffalo.mkcore.core.*;
import com.chaosbuffalo.mkcore.events.PlayerAbilityEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public class PlayerAbilityExecutor extends AbilityExecutor {

    public PlayerAbilityExecutor(MKPlayerData playerData) {
        super(playerData);
    }

    private MKPlayerData getPlayerData() {
        return (MKPlayerData) entityData;
    }

    public void executeHotBarAbility(AbilitySlot type, int slot) {
        ResourceLocation abilityId = getPlayerData().getAbilityLoadout().getAbilityInSlot(type, slot);
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
            return;

        executeAbility(abilityId);
    }

    @Override
    protected void consumeResource(MKAbility ability) {
        float manaCost = getPlayerData().getStats().getAbilityManaCost(ability);
        getPlayerData().getStats().consumeMana(manaCost);
    }

    @Override
    protected boolean abilityExecutionCheck(MKAbility ability, MKAbilityInfo info) {
        return super.abilityExecutionCheck(ability, info) &&
                !MinecraftForge.EVENT_BUS.post(new PlayerAbilityEvent.StartCasting(getPlayerData(), info));
    }

    @Override
    protected void completeAbility(MKAbility ability, MKAbilityInfo info, AbilityContext context) {
        super.completeAbility(ability, info, context);
        MinecraftForge.EVENT_BUS.post(new PlayerAbilityEvent.Completed(getPlayerData(), info));
    }

    public void onPersonaActivated() {
        rebuildActiveToggleMap();
    }

    public void onPersonaDeactivated() {
        deactivateCurrentToggleAbilities();
    }

    public float getCurrentAbilityCooldownPercent(ResourceLocation abilityId, float partialTicks) {
        return getPlayerData().getStats().getTimerPercent(abilityId, partialTicks);
    }

    private void deactivateCurrentToggleAbilities() {
        PlayerAbilityLoadout abilityLoadout = getPlayerData().getAbilityLoadout();
        deactivateCurrentToggleAbilities(abilityLoadout.getAbilityGroup(AbilitySlot.Basic));
        deactivateCurrentToggleAbilities(abilityLoadout.getAbilityGroup(AbilitySlot.Ultimate));
        deactivateCurrentToggleAbilities(abilityLoadout.getAbilityGroup(AbilitySlot.Item));
    }

    private void deactivateCurrentToggleAbilities(IActiveAbilityGroup group) {
        for (int i = 0; i < group.getMaximumSlotCount(); i++) {
            ResourceLocation abilityId = group.getSlot(i);
            MKAbility ability = MKCoreRegistry.getAbility(abilityId);
            if (ability instanceof MKToggleAbility && entityData.getEntity() != null) {
                MKToggleAbility toggle = (MKToggleAbility) ability;
                toggle.removeEffect(entityData.getEntity(), entityData);
            }
        }
    }

    private void rebuildActiveToggleMap() {
        PlayerAbilityLoadout abilityLoadout = getPlayerData().getAbilityLoadout();
        rebuildActiveToggleMap(abilityLoadout.getAbilityGroup(AbilitySlot.Basic));
        rebuildActiveToggleMap(abilityLoadout.getAbilityGroup(AbilitySlot.Ultimate));
        rebuildActiveToggleMap(abilityLoadout.getAbilityGroup(AbilitySlot.Item));
    }

    private void rebuildActiveToggleMap(IActiveAbilityGroup group) {
        // Inspect the player's action bar and see if there are any toggle abilities slotted.
        // If there are, and the corresponding toggle effect is active on the player, set the toggle exclusive group
        for (int i = 0; i < group.getMaximumSlotCount(); i++) {
            ResourceLocation abilityId = group.getSlot(i);
            MKAbility ability = MKCoreRegistry.getAbility(abilityId);
            if (ability instanceof MKToggleAbility && entityData.getEntity() != null) {
                MKToggleAbility toggle = (MKToggleAbility) ability;
                if (entityData.getEntity().isPotionActive(toggle.getToggleEffect()))
                    setToggleGroupAbility(toggle.getToggleGroupId(), toggle);
            }
        }
    }

    public void onSlotChanged(AbilitySlot type, int index, ResourceLocation previous, ResourceLocation newAbility) {
        MKCore.LOGGER.debug("PlayerAbilityExecutor.onSlotChanged({}, {}, {}, {})", type, index, previous, newAbility);

        if (previous.equals(MKCoreRegistry.INVALID_ABILITY))
            return;

        IActiveAbilityGroup container = getPlayerData().getAbilityLoadout().getAbilityGroup(type);
        if (!container.isAbilitySlotted(previous)) {
            MKAbility ability = MKCoreRegistry.getAbility(previous);
            if (ability instanceof MKToggleAbility) {
                ((MKToggleAbility) ability).removeEffect(getPlayerData().getEntity(), getPlayerData());
            }
        }
    }
}
