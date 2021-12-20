package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.PassiveTalentAbility;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.talents.TalentManager;
import com.chaosbuffalo.mkcore.effects.PassiveEffect;
import com.chaosbuffalo.mkcore.effects.PassiveTalentEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;
import java.util.stream.Stream;

public class PassiveAbilityGroup extends AbilityGroup {

    private boolean talentPassivesUnlocked;

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
        }

        if (!newAbilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            PassiveTalentAbility passiveTalent = TalentManager.getPassiveTalentAbility(newAbilityId);
            if (passiveTalent != null) {
                activatePassive(passiveTalent);
            }
        }

        super.onSlotChanged(index, oldAbilityId, newAbilityId);
    }

    @Override
    public void onJoinWorld() {
        super.onJoinWorld();
        if (playerData.isServerSide()) {
            activateAllPassives();
        }
    }

    @Override
    public void onPersonaActivated() {
        super.onPersonaActivated();
        activateAllPassives();
    }

    @Override
    public void onPersonaDeactivated() {
        super.onPersonaDeactivated();
        removeAllPassiveTalents();
    }

    private void activatePassive(PassiveTalentAbility talentAbility) {
        MKAbilityInfo info = playerData.getAbilities().getKnownAbility(talentAbility.getAbilityId());
        talentAbility.executeWithContext(playerData, AbilityContext.selfTarget(playerData), info);
    }

    private void deactivatePassive(PassiveTalentAbility talent) {
        removePassiveEffect(talent.getPassiveEffect());
    }

    private Stream<PassiveTalentAbility> getPassiveAbilitiesStream() {
        return playerData.getLoadout()
                .getPassiveGroup()
                .getAbilities()
                .stream()
                .map(TalentManager::getPassiveTalentAbility)
                .filter(Objects::nonNull);
    }

    private void activateAllPassives() {
        if (!playerData.getEntity().isAddedToWorld()) {
            // We come here during deserialization of the active persona, and it tries to apply effects which will crash the client because it's too early
            // Active persona passives should be caught by onJoinWorld
            // Persona switching while in-game should not go inside this branch
            return;
        }

        getPassiveAbilitiesStream().forEach(this::activatePassive);
    }

    private void removeAllPassiveTalents() {
        PlayerEntity playerEntity = playerData.getEntity();

        getPassiveAbilitiesStream().forEach(talentAbility -> {
            PassiveTalentEffect talentEffect = talentAbility.getPassiveEffect();
            if (playerEntity.isPotionActive(talentEffect)) {
                removePassiveEffect(talentEffect);
            }
        });
    }

    public boolean getPassiveTalentsUnlocked() {
        return talentPassivesUnlocked;
    }

    private void removePassiveEffect(PassiveEffect passiveEffect) {
        talentPassivesUnlocked = true;
        playerData.getEntity().removePotionEffect(passiveEffect);
        talentPassivesUnlocked = false;
    }
}
