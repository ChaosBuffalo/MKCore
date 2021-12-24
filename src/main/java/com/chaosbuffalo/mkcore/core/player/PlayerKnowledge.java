package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityKnowledge;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.talents.PlayerTalentKnowledge;
import net.minecraft.nbt.CompoundNBT;

public class PlayerKnowledge implements IMKEntityKnowledge, IPlayerSyncComponentProvider {

    private final SyncComponent sync = new SyncComponent("knowledge");
    private final PlayerAbilityKnowledge abilityKnowledge;
    private final PlayerTalentKnowledge talentKnowledge;
    private final PlayerEntitlementKnowledge entitlementsKnowledge;
    private final PlayerAbilityLoadout loadout;

    public PlayerKnowledge(MKPlayerData playerData) {
        abilityKnowledge = new PlayerAbilityKnowledge(playerData);
        talentKnowledge = new PlayerTalentKnowledge(playerData);
        loadout = new PlayerAbilityLoadout(playerData);
        entitlementsKnowledge = new PlayerEntitlementKnowledge(playerData);
        addSyncChild(abilityKnowledge);
        addSyncChild(talentKnowledge);
        addSyncChild(loadout);
    }

    @Override
    public SyncComponent getSyncComponent() {
        return sync;
    }

    @Override
    public PlayerAbilityKnowledge getAbilityKnowledge() {
        return abilityKnowledge;
    }

    public PlayerTalentKnowledge getTalentKnowledge() {
        return talentKnowledge;
    }

    public PlayerAbilityLoadout getAbilityLoadout() {
        return loadout;
    }

    public PlayerEntitlementKnowledge getEntitlementsKnowledge() {
        return entitlementsKnowledge;
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.put("abilities", abilityKnowledge.serialize());
        tag.put("talents", talentKnowledge.serializeNBT());
        tag.put("entitlements", entitlementsKnowledge.serialize());
        tag.put("loadout", loadout.serializeNBT());
        return tag;
    }

    public void deserialize(CompoundNBT tag) {
        abilityKnowledge.deserialize(tag.getCompound("abilities"));
        talentKnowledge.deserializeNBT(tag.get("talents"));
        entitlementsKnowledge.deserialize(tag.getCompound("entitlements"));
        loadout.deserializeNBT(tag.getCompound("loadout"));
    }

    public void onPersonaActivated() {
        MKCore.LOGGER.debug("PlayerKnowledge.onPersonaActivated");
        entitlementsKnowledge.onPersonaActivated();
        talentKnowledge.onPersonaActivated();
        loadout.onPersonaActivated();
    }

    public void onPersonaDeactivated() {
        MKCore.LOGGER.debug("PlayerKnowledge.onPersonaDeactivated");
        entitlementsKnowledge.onPersonaDeactivated();
        talentKnowledge.onPersonaDeactivated();
        loadout.onPersonaDeactivated();
    }

    public void onJoinWorld() {
        MKCore.LOGGER.debug("PlayerKnowledge.onJoinWorld");
        entitlementsKnowledge.onJoinWorld();
        talentKnowledge.onJoinWorld();
        loadout.onJoinWorld();
    }
}
