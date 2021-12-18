package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.editor.PlayerEditorModule;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtension;
import com.chaosbuffalo.mkcore.core.persona.PersonaManager;
import com.chaosbuffalo.mkcore.core.player.*;
import com.chaosbuffalo.mkcore.core.talents.PlayerTalentKnowledge;
import com.chaosbuffalo.mkcore.sync.PlayerUpdateEngine;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.LogicalSide;

public class MKPlayerData implements IMKEntityData {
    private final PlayerEntity player;
    private final PlayerAbilityExecutor abilityExecutor;
    private final PlayerStatsModule stats;
    private final PersonaManager personaManager;
    private final PlayerUpdateEngine updateEngine;
    private final PlayerAnimationModule animationModule;
    private final PlayerEquipmentModule equipmentModule;
    private final PlayerCombatExtensionModule combatExtensionModule;
    private final PlayerEditorModule editorModule;

    public MKPlayerData(PlayerEntity playerEntity) {
        player = playerEntity;
        updateEngine = new PlayerUpdateEngine(this);
        personaManager = PersonaManager.getPersonaManager(this);
        abilityExecutor = new PlayerAbilityExecutor(this);
        combatExtensionModule = new PlayerCombatExtensionModule(this);
        combatExtensionModule.getSyncComponent().attach(updateEngine);
        stats = new PlayerStatsModule(this);
        stats.getSyncComponent().attach(updateEngine);

        animationModule = new PlayerAnimationModule(this);
        abilityExecutor.setStartCastCallback(animationModule::startCast);
        abilityExecutor.setCompleteAbilityCallback(animationModule::endCast);
        abilityExecutor.setInterruptCastCallback(animationModule::interruptCast);
        animationModule.getSyncComponent().attach(updateEngine);

        equipmentModule = new PlayerEquipmentModule(this);
        editorModule = new PlayerEditorModule(this);
        editorModule.getSyncComponent().attach(updateEngine);
    }

    public void onJoinWorld() {
        getPersonaManager().ensurePersonaLoaded();
        getKnowledge().onJoinWorld();
        getStats().onJoinWorld();
        getAbilityExecutor().onJoinWorld();
        if (isServerSide()) {
            MKCore.LOGGER.info("server player joined world!");
            initialSync();
        } else {
            MKCore.LOGGER.info("client player joined world!");
        }
    }

    @Override
    public PlayerStatsModule getStats() {
        return stats;
    }

    @Override
    public PlayerCombatExtensionModule getCombatExtension() {
        return combatExtensionModule;
    }

    @Override
    public PlayerAbilityExecutor getAbilityExecutor() {
        return abilityExecutor;
    }

    @Override
    public PlayerKnowledge getKnowledge() {
        return getPersonaManager().getActivePersona().getKnowledge();
    }

    public PlayerAbilityLoadout getAbilityLoadout() {
        return getKnowledge().getAbilityLoadout();
    }

    public PlayerAbilityKnowledge getAbilities() {
        return getKnowledge().getAbilityKnowledge();
    }

    public PlayerUpdateEngine getUpdateEngine() {
        return updateEngine;
    }

    public PersonaManager getPersonaManager() {
        return personaManager;
    }

    public PlayerTalentKnowledge getTalents() {
        return getKnowledge().getTalentKnowledge();
    }

    public PlayerEntitlementKnowledge getEntitlements() {
        return getKnowledge().getEntitlementsKnowledge();
    }

    public PlayerEquipmentModule getEquipment() {
        return equipmentModule;
    }

    public void clone(IMKEntityData previous, boolean death) {
        CompoundNBT tag = previous.serialize();
        deserialize(tag);
    }

    @Override
    public PlayerEntity getEntity() {
        return player;
    }

    public PlayerAnimationModule getAnimationModule() {
        return animationModule;
    }

    public boolean isServerSide() {
        return player instanceof ServerPlayerEntity;
    }

    public LogicalSide getSide() {
        return isServerSide() ? LogicalSide.SERVER : LogicalSide.CLIENT;
    }

    public void update() {
        getEntity().getEntityWorld().getProfiler().startSection("MKPlayerData.update");

        getEntity().getEntityWorld().getProfiler().startSection("PlayerStats.tick");
        getStats().tick();
        getEntity().getEntityWorld().getProfiler().endStartSection("AbilityExecutor.tick");
        getAbilityExecutor().tick();
        getEntity().getEntityWorld().getProfiler().endStartSection("Animation.tick");
        getAnimationModule().tick();
        getEntity().getEntityWorld().getProfiler().endStartSection("PlayerCombat.tick");
        getCombatExtension().tick();

        if (isServerSide()) {
            getEntity().getEntityWorld().getProfiler().endStartSection("Updater.sync");
            syncState();
        }
        getEntity().getEntityWorld().getProfiler().endSection();

        getEntity().getEntityWorld().getProfiler().endSection();
    }

    private void syncState() {
        updateEngine.syncUpdates();
    }

    public void fullSyncTo(ServerPlayerEntity otherPlayer) {
        MKCore.LOGGER.info("Full public sync {} -> {}", player, otherPlayer);
        updateEngine.sendAll(otherPlayer);
    }

    public void initialSync() {
        MKCore.LOGGER.info("Sending initial sync for {}", player);
        if (isServerSide()) {
            updateEngine.sendAll((ServerPlayerEntity) player);
        }
    }

    public void onPersonaActivated() {
        getEquipment().onPersonaActivated();
        getAbilityExecutor().onPersonaActivated();
        getStats().onPersonaActivated();
        getAbilityLoadout().onPersonaSwitch();
    }

    public void onPersonaDeactivated() {
        getEquipment().onPersonaDeactivated();
        getAbilityExecutor().onPersonaDeactivated();
        getStats().onPersonaDeactivated();
    }

    public <T extends IPersonaExtension> T getPersonaExtension(Class<T> clazz) {
        return getPersonaManager().getActivePersona().getExtension(clazz);
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.put("persona", personaManager.serialize());
        tag.put("stats", getStats().serialize());
        tag.put("editor", getEditor().serialize());
        return tag;
    }

    public PlayerEditorModule getEditor() {
        return editorModule;
    }


    @Override
    public void deserialize(CompoundNBT tag) {
        personaManager.deserialize(tag.getCompound("persona"));
        getStats().deserialize(tag.getCompound("stats"));
        getEditor().deserialize(tag.getCompound("editor"));
    }
}
