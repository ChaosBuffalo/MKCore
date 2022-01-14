package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.editor.PlayerEditorModule;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtension;
import com.chaosbuffalo.mkcore.core.persona.PersonaManager;
import com.chaosbuffalo.mkcore.core.player.*;
import com.chaosbuffalo.mkcore.core.talents.PlayerTalentKnowledge;
import com.chaosbuffalo.mkcore.sync.PlayerUpdateEngine;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;
import java.util.Objects;

public class MKPlayerData implements IMKEntityData {
    private final PlayerEntity player;
    private final PlayerAbilityExecutor abilityExecutor;
    private final PlayerStats stats;
    private final PersonaManager personaManager;
    private final PlayerUpdateEngine updateEngine;
    private final PlayerAnimationModule animationModule;
    private final PlayerEquipment equipment;
    private final PlayerCombatExtensionModule combatExtensionModule;
    private final PlayerEditorModule editorModule;
    private final PlayerEffectHandler effectHandler;

    public MKPlayerData(PlayerEntity playerEntity) {
        player = Objects.requireNonNull(playerEntity);
        updateEngine = new PlayerUpdateEngine(this);
        personaManager = PersonaManager.getPersonaManager(this);
        abilityExecutor = new PlayerAbilityExecutor(this);
        combatExtensionModule = new PlayerCombatExtensionModule(this);
        combatExtensionModule.getSyncComponent().attach(updateEngine);
        stats = new PlayerStats(this);
        stats.getSyncComponent().attach(updateEngine);

        animationModule = new PlayerAnimationModule(this);
        abilityExecutor.setStartCastCallback(animationModule::startCast);
        abilityExecutor.setCompleteAbilityCallback(this::completeAbility);
        abilityExecutor.setInterruptCastCallback(animationModule::interruptCast);
        animationModule.getSyncComponent().attach(updateEngine);

        equipment = new PlayerEquipment(this);
        editorModule = new PlayerEditorModule(this);
        editorModule.getSyncComponent().attach(updateEngine);
        effectHandler = new PlayerEffectHandler(this);
    }

    private void completeAbility(MKAbility ability){
        animationModule.endCast(ability);
        if (isServerSide()){
            getKnowledge().getSkills().onCastAbility(ability);
        }

    }

    public PlayerSkills getSkills() {
        return getKnowledge().getSkills();
    }

    @Override
    public PlayerStats getStats() {
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

    public PlayerAbilityLoadout getLoadout() {
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

    public PlayerEquipment getEquipment() {
        return equipment;
    }

    @Nonnull
    @Override
    public PlayerEntity getEntity() {
        return player;
    }

    public PlayerAnimationModule getAnimationModule() {
        return animationModule;
    }

    @Override
    public PlayerEffectHandler getEffects() {
        return effectHandler;
    }

    @Override
    public boolean isServerSide() {
        return player instanceof ServerPlayerEntity;
    }

    @Override
    public void onJoinWorld() {
        getPersonaManager().ensurePersonaLoaded();
        getKnowledge().onJoinWorld();
        getStats().onJoinWorld();
        getAbilityExecutor().onJoinWorld();
        getEffects().onJoinWorld();
        if (isServerSide()) {
            MKCore.LOGGER.info("server player joined world!");
            initialSync();
        } else {
            MKCore.LOGGER.info("client player joined world!");
        }
    }

    private void onDeath() {
        getEffects().onDeath();
    }

    public void update() {
        getEntity().getEntityWorld().getProfiler().startSection("MKPlayerData.update");

        getEntity().getEntityWorld().getProfiler().startSection("PlayerEffects.tick");
        getEffects().tick();
        getEntity().getEntityWorld().getProfiler().endStartSection("PlayerStats.tick");
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

    public void clone(MKPlayerData previous, boolean death) {
        if (death) {
            previous.onDeath();
        }
        CompoundNBT tag = previous.serialize();
        deserialize(tag);
    }

    private void syncState() {
        updateEngine.syncUpdates();
    }

    public void initialSync() {
        if (isServerSide()) {
            MKCore.LOGGER.debug("Sending initial sync for {}", player);
            updateEngine.sendAll((ServerPlayerEntity) player);
        }
    }

    @Override
    public void onPlayerStartTracking(ServerPlayerEntity otherPlayer) {
        updateEngine.sendAll(otherPlayer);
        getEffects().sendAllEffectsToPlayer(otherPlayer);
    }

    public void onPersonaActivated() {
        getEquipment().onPersonaActivated();
        getAbilityExecutor().onPersonaActivated();
        getStats().onPersonaActivated();
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
        tag.put("effects", getEffects().serialize());
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
        getEffects().deserialize(tag.getCompound("effects"));
    }
}
