package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.AbilityExecutor;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
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

    public void executeHotBarAbility(AbilityGroupId group, int slot) {
        getPlayerData().getLoadout().getAbilityGroup(group).executeSlot(slot);
    }

    public boolean clientSimulateAbility(MKAbility ability, AbilityGroupId executingGroup) {
        MKAbilityInfo info = getPlayerData().getAbilities().getKnownAbility(ability.getAbilityId());
        if (executingGroup.requiresAbilityKnown() && info == null) {
            return false;
        }

        if (abilityExecutionCheck(ability, info)) {
            AbilityTargetSelector selector = ability.getTargetSelector();
            AbilityContext context = selector.createContext(entityData, ability);
            if (context != null) {
                return selector.validateContext(entityData, context);
            } else {
                MKCore.LOGGER.warn("CLIENT Entity {} tried to execute ability {} with a null context!", entityData.getEntity(), ability.getAbilityId());
            }
        }
        return false;
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
        PlayerAbilityLoadout abilityLoadout = getPlayerData().getLoadout();
        deactivateCurrentToggleAbilities(abilityLoadout.getAbilityGroup(AbilityGroupId.Basic));
        deactivateCurrentToggleAbilities(abilityLoadout.getAbilityGroup(AbilityGroupId.Ultimate));
        deactivateCurrentToggleAbilities(abilityLoadout.getAbilityGroup(AbilityGroupId.Item));
    }

    private void deactivateCurrentToggleAbilities(AbilityGroup group) {
        for (int i = 0; i < group.getMaximumSlotCount(); i++) {
            ResourceLocation abilityId = group.getSlot(i);
            MKAbility ability = MKCoreRegistry.getAbility(abilityId);
            if (ability instanceof MKToggleAbilityBase) {
                MKToggleAbilityBase toggle = (MKToggleAbilityBase) ability;
                toggle.removeEffect(entityData.getEntity(), entityData);
            }
        }
    }

    private void rebuildActiveToggleMap() {
        PlayerAbilityLoadout abilityLoadout = getPlayerData().getLoadout();
        rebuildActiveToggleMap(abilityLoadout.getAbilityGroup(AbilityGroupId.Basic));
        rebuildActiveToggleMap(abilityLoadout.getAbilityGroup(AbilityGroupId.Ultimate));
        rebuildActiveToggleMap(abilityLoadout.getAbilityGroup(AbilityGroupId.Item));
    }

    private void rebuildActiveToggleMap(AbilityGroup group) {
        // Inspect the player's action bar and see if there are any toggle abilities slotted.
        // If there are, and the corresponding toggle effect is active on the player, set the toggle exclusive group
        for (int i = 0; i < group.getMaximumSlotCount(); i++) {
            ResourceLocation abilityId = group.getSlot(i);
            MKAbility ability = MKCoreRegistry.getAbility(abilityId);
            if (ability instanceof MKToggleAbilityBase) {
                MKToggleAbilityBase toggle = (MKToggleAbilityBase) ability;
                if (toggle.isEffectActive(entityData))
                    setToggleGroupAbility(toggle.getToggleGroupId(), toggle);
            }
        }
    }

    public void onSlotChanged(AbilityGroupId group, int index, ResourceLocation previous, ResourceLocation newAbility) {
        MKCore.LOGGER.debug("PlayerAbilityExecutor.onSlotChanged({}, {}, {}, {})", group, index, previous, newAbility);

        if (previous.equals(MKCoreRegistry.INVALID_ABILITY))
            return;

        AbilityGroup abilityGroup = getPlayerData().getLoadout().getAbilityGroup(group);
        if (!abilityGroup.isAbilitySlotted(previous)) {
            MKAbility ability = MKCoreRegistry.getAbility(previous);
            if (ability instanceof MKToggleAbilityBase) {
                ((MKToggleAbilityBase) ability).removeEffect(getPlayerData().getEntity(), getPlayerData());
            }
        }
    }
}
