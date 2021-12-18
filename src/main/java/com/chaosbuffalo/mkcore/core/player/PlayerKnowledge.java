package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.core.IMKEntityKnowledge;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entity.EntityEntitlementsKnowledge;
import com.chaosbuffalo.mkcore.core.talents.PlayerTalentKnowledge;
import net.minecraft.nbt.CompoundNBT;

public class PlayerKnowledge implements IMKEntityKnowledge, IPlayerSyncComponentProvider {

    private final MKPlayerData playerData;
    private final SyncComponent sync = new SyncComponent("knowledge");
    private final PlayerAbilityKnowledge abilityKnowledge;
    private final PlayerTalentKnowledge talentKnowledge;
    private final EntityEntitlementsKnowledge entitlementsKnowledge;
    private final PlayerAbilityLoadout loadout;

    public PlayerKnowledge(MKPlayerData playerData) {
        this.playerData = playerData;
        abilityKnowledge = new PlayerAbilityKnowledge(playerData);
        talentKnowledge = new PlayerTalentKnowledge(playerData);
        loadout = new PlayerAbilityLoadout(playerData);
        entitlementsKnowledge = new EntityEntitlementsKnowledge(playerData);
        entitlementsKnowledge.addLoadedCallback(loadout::entitlementsLoadedCallback);
        entitlementsKnowledge.addEntitlementSubscriber(loadout::entitlementsChangedCallback);
        entitlementsKnowledge.addLoadedCallback(abilityKnowledge::entitlementsLoadedCallback);
        entitlementsKnowledge.addEntitlementSubscriber(abilityKnowledge::entitlementsChangedCallback);
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

    public EntityEntitlementsKnowledge getEntitlementsKnowledge() {
        return entitlementsKnowledge;
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.put("abilities", abilityKnowledge.serialize());
        tag.put("talents", talentKnowledge.serializeNBT());
        tag.put("loadout", loadout.serializeNBT());
        tag.put("entitlements", entitlementsKnowledge.serialize());
        return tag;
    }

    public void deserialize(CompoundNBT tag) {
        abilityKnowledge.deserialize(tag.getCompound("abilities"));
        talentKnowledge.deserializeNBT(tag.get("talents"));
        loadout.deserializeNBT(tag.getCompound("loadout"));
        entitlementsKnowledge.deserialize(tag.getCompound("entitlements"));
    }

    public void onPersonaActivated() {
        entitlementsKnowledge.broadcastLoaded();
    }

    public void onPersonaDeactivated() {

    }
}
