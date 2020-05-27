package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.CastState;
import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import com.chaosbuffalo.mkcore.abilities.PlayerAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.PlayerToggleAbility;
import com.chaosbuffalo.mkcore.events.PlayerAbilityEvent;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerStartCastPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.Map;

public class PlayerAbilityExecutor {
    private final MKPlayerData playerData;
    private PlayerCastingState currentCast;
    private final Map<ResourceLocation, PlayerToggleAbility> activeToggleMap = new HashMap<>();

    public PlayerAbilityExecutor(MKPlayerData playerData) {
        this.playerData = playerData;
    }

    private PlayerEntity getPlayer() {
        return playerData.getPlayer();
    }

    private boolean isServerSide() {
        return getPlayer() instanceof ServerPlayerEntity;
    }

    public void executeHotBarAbility(int slot) {
        ResourceLocation abilityId = playerData.getKnowledge().getActionBar().getAbilityInSlot(slot);
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
            return;

        executeAbility(abilityId);
    }

    public void executeAbility(ResourceLocation abilityId) {
        PlayerAbilityInfo info = playerData.getKnowledge().getAbilityInfo(abilityId);
        if (info == null || !info.isCurrentlyKnown())
            return;

        if (playerData.getStats().getCurrentAbilityCooldown(abilityId) == 0) {

            PlayerAbility ability = info.getAbility();
            if (ability != null &&
                    ability.meetsRequirements(playerData) &&
                    !MinecraftForge.EVENT_BUS.post(new PlayerAbilityEvent.StartCasting(playerData, info))) {
                ability.execute(getPlayer(), playerData, getPlayer().getEntityWorld());
            }
        }
    }

    public boolean canActivateAbility(PlayerAbility ability) {
        return !isCasting();
    }

    public void tick() {
        updateCurrentCast();
    }

    public void onJoinWorld() {
        if (isServerSide()) {
            rebuildActiveToggleMap();
        }
    }

    public void setCooldown(ResourceLocation id, int ticks) {
        MKCore.LOGGER.info("setCooldown({}, {})", id, ticks);

        if (!id.equals(MKCoreRegistry.INVALID_ABILITY)) {
            playerData.getStats().setTimer(id, ticks);
        }
    }

    public boolean isCasting() {
        return currentCast != null;
    }

    public int getCastTicks() {
        return currentCast != null ? currentCast.getCastTicks() : 0;
    }

    public ResourceLocation getCastingAbility() {
        return currentCast != null ? currentCast.getAbilityId() : MKCoreRegistry.INVALID_ABILITY;
    }

    private void clearCastingAbility() {
        currentCast = null;
    }

    private CastState startCast(PlayerAbilityInfo abilityInfo, int castTime) {
        MKCore.LOGGER.info("startCast {} {}", abilityInfo.getId(), castTime);
        ServerCastingState serverCastingState = new ServerCastingState(this, abilityInfo, castTime);
        currentCast = serverCastingState;
        if (getPlayer() instanceof ServerPlayerEntity) {
            PacketHandler.sendToTrackingAndSelf(new PlayerStartCastPacket(abilityInfo.getId(), castTime), (ServerPlayerEntity) getPlayer());
        }

        return serverCastingState.getAbilityCastState();
    }

    public void startCastClient(ResourceLocation abilityId, int castTicks) {
        MKCore.LOGGER.info("startCastClient {} {}", abilityId, castTicks);
        PlayerAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability != null) {
            currentCast = new ClientCastingState(this, ability, castTicks);
        } else {
            clearCastingAbility();
        }
    }

    private void updateCurrentCast() {
        if (!isCasting())
            return;

        if (!currentCast.tick()) {
            clearCastingAbility();
        }
    }

    public CastState startAbility(PlayerAbility ability) {
        PlayerAbilityInfo info = playerData.getKnowledge().getAbilityInfo(ability.getAbilityId());
        if (info == null || !info.isCurrentlyKnown() || isCasting()) {
            MKCore.LOGGER.info("startAbility null {} {}", info, isCasting());
            return null;
        }

        float manaCost = playerData.getStats().getAbilityManaCost(ability.getAbilityId());
        playerData.consumeMana(manaCost);

        int castTime = ability.getCastTime();
        CastState state = startCast(info, castTime);
        if (castTime > 0) {
            return state;
        } else {
            completeAbility(ability, info, state);
        }
        return null;
    }

    private void completeAbility(PlayerAbility ability, PlayerAbilityInfo info, CastState castState) {
        // Finish the cast
        ability.endCast(getPlayer(), playerData, getPlayer().getEntityWorld(), castState);

        int cooldown = ability.getCooldownTicks();
        cooldown = PlayerFormulas.applyCooldownReduction(playerData, cooldown);
        setCooldown(info.getId(), cooldown);
//        SoundEvent sound = ability.getSpellCompleteSoundEvent();
//        if (sound != null) {
//            AbilityUtils.playSoundAtServerEntity(player, sound, SoundCategory.PLAYERS);
//        }
        clearCastingAbility();
        MinecraftForge.EVENT_BUS.post(new PlayerAbilityEvent.Completed(playerData, info));
    }

    public void onAbilityUnlearned(PlayerAbility ability) {
        updateToggleAbility(ability);
    }

    static abstract class PlayerCastingState {
        boolean started = false;
        int castTicks;
        PlayerAbility ability;
        PlayerAbilityExecutor executor;

        public PlayerCastingState(PlayerAbilityExecutor executor, PlayerAbility ability, int castTicks) {
            this.executor = executor;
            this.ability = ability;
            this.castTicks = castTicks;
        }

        public int getCastTicks() {
            return castTicks;
        }

        public ResourceLocation getAbilityId() {
            return ability.getAbilityId();
        }

        public boolean tick() {
            if (castTicks <= 0)
                return false;

            if (!started) {
                begin();
                started = true;
            }

            activeTick();
            castTicks--;
            boolean active = castTicks > 0;
            if (!active) {
                finish();
            }
            return active;
        }

        void begin() {

        }

        abstract void activeTick();

        abstract void finish();
    }

    static class ServerCastingState extends PlayerCastingState {
        PlayerAbilityInfo info;
        CastState abilityCastState;

        public ServerCastingState(PlayerAbilityExecutor executor, PlayerAbilityInfo abilityInfo, int castTicks) {
            super(executor, abilityInfo.getAbility(), castTicks);
            this.info = abilityInfo;
            abilityCastState = abilityInfo.getAbility().createCastState(castTicks);
        }

        public CastState getAbilityCastState() {
            return abilityCastState;
        }

        @Override
        void activeTick() {
            ability.continueCast(executor.getPlayer(), executor.playerData, executor.getPlayer().getEntityWorld(), castTicks, abilityCastState);
        }

        @Override
        void finish() {
            executor.completeAbility(ability, info, abilityCastState);
        }
    }

    static class ClientCastingState extends PlayerCastingState {
        //        MovingSoundCasting sound; TODO: sound
        boolean playing = false;

        public ClientCastingState(PlayerAbilityExecutor executor, PlayerAbility ability, int castTicks) {
            super(executor, ability, castTicks);
        }

        @Override
        void begin() {
            SoundEvent event = ability.getCastingSoundEvent();
            if (event != null) {
                // TODO: sound
//                sound = new MovingSoundCasting(playerData.player, event, SoundCategory.PLAYERS, castTicks);
//                Minecraft.getMinecraft().getSoundHandler().playSound(sound);
                playing = true;
            }
        }

        @Override
        void activeTick() {
            ability.continueCastClient(executor.getPlayer(), executor.playerData, executor.getPlayer().getEntityWorld(), castTicks);
        }

        @Override
        void finish() {
            // TODO: sound
//            if (playing && sound != null) {
//                Minecraft.getInstance().getSoundHandler().stopSound(sound);
//                playing = false;
//            }
        }
    }

    private void rebuildActiveToggleMap() {
        for (int i = 0; i < GameConstants.ACTION_BAR_SIZE; i++) {
            ResourceLocation abilityId = playerData.getKnowledge().getActionBar().getAbilityInSlot(i);
            PlayerAbility ability = MKCoreRegistry.getAbility(abilityId);
            if (ability instanceof PlayerToggleAbility && playerData.getPlayer() != null) {
                PlayerToggleAbility toggle = (PlayerToggleAbility) ability;
                if (playerData.getPlayer().isPotionActive(toggle.getToggleEffect()))
                    setToggleGroupAbility(toggle.getToggleGroupId(), toggle);
            }
        }
    }

    private void updateToggleAbility(PlayerAbility ability) {
        if (!(ability instanceof PlayerToggleAbility)) {
            return;
        }
        PlayerToggleAbility toggle = (PlayerToggleAbility) ability;

        PlayerEntity player = playerData.getPlayer();
        PlayerAbilityInfo info = playerData.getKnowledge().getAbilityInfo(ability.getAbilityId());
        if (info != null && info.isCurrentlyKnown()) {
            // If this is a toggle ability we must re-apply the effect to make sure it's working at the proper rank
            if (player.isPotionActive(toggle.getToggleEffect())) {
                toggle.removeEffect(player, playerData, player.getEntityWorld());
                toggle.applyEffect(player, playerData, player.getEntityWorld());
            }
        } else {
            // Unlearning, remove the effect
            toggle.removeEffect(player, playerData, player.getEntityWorld());
        }
    }

    public void clearToggleGroupAbility(ResourceLocation groupId) {
        activeToggleMap.remove(groupId);
    }

    public void setToggleGroupAbility(ResourceLocation groupId, PlayerToggleAbility ability) {
        PlayerEntity player = playerData.getPlayer();
        PlayerToggleAbility current = activeToggleMap.get(ability.getToggleGroupId());
        // This can also be called when rebuilding the activeToggleMap after transferring dimensions and in that case
        // ability will be the same as current
        if (current != null && current != ability) {
            current.removeEffect(player, playerData, player.getEntityWorld());
            setCooldown(current.getAbilityId(), playerData.getStats().getAbilityCooldown(current));
        }
        activeToggleMap.put(groupId, ability);
    }
}
