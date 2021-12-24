package com.chaosbuffalo.mkcore.core.player.loadout;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import com.chaosbuffalo.mkcore.core.talents.TalentManager;
import com.chaosbuffalo.mkcore.effects.PassiveEffect;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;
import java.util.stream.Stream;

public class PassiveAbilityGroup extends AbilityGroup {

    private boolean passiveEffectsUnlocked;

    public PassiveAbilityGroup(MKPlayerData playerData) {
        super(playerData, "passive", AbilityGroupId.Passive);
    }

    @Override
    protected void onSlotChanged(int index, ResourceLocation oldAbilityId, ResourceLocation newAbilityId) {
        if (!oldAbilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            PassiveTalentAbility current = TalentManager.getPassiveTalentAbility(oldAbilityId);
            if (current != null) {
                deactivatePassive(current);
            }
            MKAbility oldAbility = MKCoreRegistry.getAbility(oldAbilityId);
            if (oldAbility instanceof MKPassiveAbility) {
                MKPassiveAbility passiveAbility = (MKPassiveAbility) oldAbility;
                deactivatePassive(passiveAbility);
            }
        }

        if (!newAbilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            MKAbility newAbility = MKCoreRegistry.getAbility(newAbilityId);
            if (newAbility instanceof IMKPassiveAbility) {
                activatePassive(newAbility);
            }
        }

        super.onSlotChanged(index, oldAbilityId, newAbilityId);
    }

    @Override
    public void onJoinWorld() {
        super.onJoinWorld();
        activateAllPassives(true);
    }

    @Override
    public void onPersonaActivated() {
        super.onPersonaActivated();
        activateAllPassives(false);
    }

    @Override
    public void onPersonaDeactivated() {
        super.onPersonaDeactivated();
        removeAllPassiveTalents();
    }

    private void activatePassive(MKAbility talentAbility) {
        MKAbilityInfo info = playerData.getAbilities().getKnownAbility(talentAbility.getAbilityId());
        talentAbility.executeWithContext(playerData, AbilityContext.selfTarget(playerData), info);
    }

    private void deactivatePassive(PassiveTalentAbility talent) {
        removePassiveEffect(talent.getPassiveEffect());
    }

    private void deactivatePassive(MKPassiveAbility talent) {
        if (playerData.getEffects().isEffectActive(talent.getPassiveEffect())) {
            playerData.getEffects().removeEffect(talent.getPassiveEffect());
        }
    }

    private Stream<MKAbility> getPassiveAbilitiesStream() {
        return playerData.getLoadout()
                .getPassiveGroup()
                .getAbilities()
                .stream()
                .map(MKCoreRegistry::getAbility)
                .filter(Objects::nonNull)
                .filter(ability -> ability instanceof IMKPassiveAbility);
    }

    private void activateAllPassives(boolean willBeInWorld) {
        if (!playerData.isServerSide())
            return;

        // We come here during deserialization of the active persona, and it tries to apply effects which will crash the client because it's too early
        // Active persona passives should be caught by onJoinWorld
        // Persona switching while in-game should not go inside this branch
        if (willBeInWorld || playerData.getEntity().isAddedToWorld()) {
            getPassiveAbilitiesStream().forEach(this::activatePassive);
        }
    }

    private void removePassive(MKAbility ability) {
        if (ability instanceof PassiveTalentAbility) {
            deactivatePassive((PassiveTalentAbility) ability);
        } else if (ability instanceof MKPassiveAbility) {
            deactivatePassive((MKPassiveAbility) ability);
        }
    }

    private void removeAllPassiveTalents() {
        getPassiveAbilitiesStream().forEach(this::removePassive);
    }

    public boolean canRemovePassiveEffects() {
        return passiveEffectsUnlocked;
    }

    private void removePassiveEffect(PassiveEffect passiveEffect) {
        if (playerData.getEntity().isPotionActive(passiveEffect)) {
            passiveEffectsUnlocked = true;
            playerData.getEntity().removePotionEffect(passiveEffect);
            passiveEffectsUnlocked = false;
        }
    }
}
