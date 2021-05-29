package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.core.IMKEntityKnowledge;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.talents.PlayerTalentKnowledge;
import net.minecraft.nbt.CompoundNBT;

public class PlayerKnowledge implements IMKEntityKnowledge, IPlayerSyncComponentProvider {

    private final MKPlayerData playerData;
    private final PlayerSyncComponent sync = new PlayerSyncComponent("knowledge");
    private final PlayerAbilityKnowledge abilityKnowledge;
    private final PlayerTalentKnowledge talentKnowledge;
    private final PlayerAbilityLoadout loadout;

    public PlayerKnowledge(MKPlayerData playerData) {
        this.playerData = playerData;
        abilityKnowledge = new PlayerAbilityKnowledge(playerData);
        talentKnowledge = new PlayerTalentKnowledge(playerData);
        loadout = new PlayerAbilityLoadout(playerData);
        addSyncChild(abilityKnowledge);
        addSyncChild(talentKnowledge);
        addSyncChild(loadout);
    }

    @Override
    public PlayerSyncComponent getSyncComponent() {
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

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.put("abilities", abilityKnowledge.serialize());
        tag.put("talents", talentKnowledge.serializeNBT());
        tag.put("loadout", loadout.serializeNBT());
        return tag;
    }

    public void deserialize(CompoundNBT tag) {
        abilityKnowledge.deserialize(tag.getCompound("abilities"));
        talentKnowledge.deserializeNBT(tag.get("talents"));
        loadout.deserializeNBT(tag.getCompound("loadout"));
    }

    public void onPersonaActivated() {
    }

    public void onPersonaDeactivated() {

    }
}
