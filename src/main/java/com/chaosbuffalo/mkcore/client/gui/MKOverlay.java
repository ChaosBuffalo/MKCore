package com.chaosbuffalo.mkcore.client.gui;


import com.chaosbuffalo.mkcore.*;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.gui.widgets.OnScreenXpBarWidget;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.pets.MKPet;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import com.chaosbuffalo.mkcore.core.player.PlayerAbilityExecutor;
import com.chaosbuffalo.mkcore.events.ClientEventHandler;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKRectangle;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MKOverlay {

    private static final ResourceLocation COOLDOWN_ICON = MKCore.makeRL("textures/abilities/cooldown.png");

    private static final int SLOT_WIDTH = 20;
    private static final int SLOT_HEIGHT = 20;
    private static final int MIN_BAR_START_Y = 80;
    public static final int ABILITY_ICON_SIZE = 16;
    public static final OnScreenXpBarWidget xpBarWidget = new OnScreenXpBarWidget(2, 0, 63, 5);

    private final Minecraft mc;

    public MKOverlay() {
        mc = Minecraft.getInstance();
    }

    public static class StringComparator implements Comparator<String> {
        public int compare(String obj1, String obj2) {
            if (obj1 == obj2) {
                return 0;
            }
            if (obj1 == null) {
                return -1;
            }
            if (obj2 == null) {
                return 1;
            }
            return obj1.compareTo(obj2);
        }
    }

    private void drawTeam(MatrixStack matrixStack, MKPlayerData data, float partialTicks){
        int height = mc.getMainWindow().getScaledHeight();
        int winWidth = mc.getMainWindow().getScaledWidth();
        int teamX = winWidth - 55;

        int perMember = 18;
        int perPet = 14;

        List<PlayerEntity> players = data.getEntity().getEntityWorld().getPlayers().stream()
                .filter(otherPlayer ->
                        !data.getEntity().isEntityEqual(otherPlayer) &&
                                data.getEntity().isOnSameTeam(otherPlayer))
                .collect(Collectors.toList());
        Map<ResourceLocation, MKPet.ClientMKPet> ownerPets = data.getPets().getClientPets();
        List<MKPet.ClientMKPet> sortedPets = ownerPets.values().stream()
                .sorted(Comparator.comparing(x -> x.getName().toString())).collect(Collectors.toList());

        int memberCount = players.size();
        int totalSize = perMember * memberCount + ownerPets.size() * perPet;
        int teamY = (height / 2) - (totalSize / 2);


        if (memberCount + sortedPets.size() > 0){
            MKRectangle teamBg = new MKRectangle(teamX - 2, teamY - 4, 54, totalSize + 8, 0xaa333333);
            teamBg.drawWidget(matrixStack, mc, 0, 0, partialTicks);
            for (MKPet.ClientMKPet pet : sortedPets) {
                if (pet.getEntity() != null) {
                    MKText text = new MKText(mc.fontRenderer, pet.getEntity().getName(), teamX, teamY);
                    text.setColor(0xffffffff);
                    text.drawWidget(matrixStack, mc, 0, 0, partialTicks);
                    int finalTeamY = teamY;
                    MKCore.getEntityData(pet.getEntity()).ifPresent(x -> {
                        drawTeamHP(matrixStack, x, partialTicks, teamX, finalTeamY + 10);
                    });
                    teamY += perPet;
                }

            }
            for (PlayerEntity teamMember : players){
                MKText text = new MKText(mc.fontRenderer, teamMember.getDisplayName(), teamX, teamY);
                text.setColor(0xffffffff);
                text.drawWidget(matrixStack, mc, 0, 0, partialTicks);
                int finalTeamY = teamY;
                MKCore.getPlayer(teamMember).ifPresent(x -> {
                    drawTeamHP(matrixStack, x, partialTicks, teamX, finalTeamY + 10);
                    drawTeamMana(matrixStack, x, teamX, finalTeamY + 16);
                });
                teamY += perMember;

            }
        }


    }

    private void drawTeamHP(MatrixStack matrixStack, IMKEntityData data, float partialTick, int x, int y) {
        boolean isWithered = data.getEntity().getActivePotionEffect(Effects.WITHER) != null;
        float absorption = data.getEntity().getAbsorptionAmount();
        float maxHp = data.getEntity().getMaxHealth();
        float current_hp = data.getEntity().getHealth();
        String textureName = isWithered ? GuiTextures.HP_WITHER_BAR : GuiTextures.HP_BAR;
        float percentage = current_hp / maxHp;
        if (percentage > 1.0f){
            percentage = 1.0f;
        }
        int width = 50;
        int barSize = Math.round(width * percentage);
        if (current_hp > 0 && barSize < 1){
            barSize = 1;
        }
        GuiTextures.CORE_TEXTURES.bind(mc);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        GuiTextures.CORE_TEXTURES.drawRegionAtPosPartialWidth(matrixStack, textureName, x, y, barSize);
        GuiTextures.CORE_TEXTURES.drawRegionAtPos(matrixStack, GuiTextures.SHORT_BAR_OUTLINE, x, y - 1);
        if (absorption > 0.0f){
            float absorpPercentage = absorption / maxHp;
            if (absorpPercentage > 1.0f){
                absorpPercentage = 1.0f;
            }
            int abarSize = Math.round(width * absorpPercentage);
            if (abarSize < 1){
                abarSize = 1;
            }
            GuiTextures.CORE_TEXTURES.drawRegionAtPosPartialWidth(matrixStack, GuiTextures.ABSORPTON_BAR,
                    x, y - 1, abarSize);
        }

    }

    private void drawTeamMana(MatrixStack matrixStack, MKPlayerData data, int x, int y) {
        float maxMana = data.getStats().getMaxMana();
        float currentMana = data.getStats().getMana();
        String textureName = GuiTextures.MANA_BAR;
        float percentage = currentMana / maxMana;
        if (percentage > 1.0f){
            percentage = 1.0f;
        }
        int width = 50;
        int barSize = Math.round(width * percentage);
        if (currentMana > 0 && barSize < 1){
            barSize = 1;
        }
        GuiTextures.CORE_TEXTURES.bind(mc);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        GuiTextures.CORE_TEXTURES.drawRegionAtPosPartialWidth(matrixStack, textureName, x, y, barSize);
        GuiTextures.CORE_TEXTURES.drawRegionAtPos(matrixStack, GuiTextures.SHORT_BAR_OUTLINE, x, y - 1);
    }

    private void drawMana(MatrixStack matrixStack, MKPlayerData data) {
        float maxMana = data.getStats().getMaxMana();
        float currentMana = data.getStats().getMana();
        String textureName = GuiTextures.MANA_BAR_LONG;
        float percentage = currentMana / maxMana;
        if (percentage > 1.0f){
            percentage = 1.0f;
        }
        int width = 75;
        int barSize = Math.round(width * percentage);
        if (currentMana > 0 && barSize < 1){
            barSize = 1;
        }
        int height = mc.getMainWindow().getScaledHeight();
        int winWidth = mc.getMainWindow().getScaledWidth();
        int castStartY = height - 34;
        int castStartX = (winWidth / 2) - 89;
        GuiTextures.CORE_TEXTURES.bind(mc);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        GuiTextures.CORE_TEXTURES.drawRegionAtPosPartialWidth(matrixStack, textureName, castStartX, castStartY, barSize);
        GuiTextures.CORE_TEXTURES.drawRegionAtPos(matrixStack, GuiTextures.PLAYER_BAR_OUTLINE, castStartX, castStartY - 1);
    }

    private void drawXpBar(MatrixStack matrixStack, MKPlayerData data, float partialTick){
        int height = mc.getMainWindow().getScaledHeight();
        int castStartY = height - 8;
        int winWidth = mc.getMainWindow().getScaledWidth();
        int castStartX = (winWidth / 2) - 89 - 100;
        xpBarWidget.syncPlayerXp(data);
        xpBarWidget.setY(castStartY);
        xpBarWidget.setX(castStartX);
        xpBarWidget.drawWidget(matrixStack, mc, 0, 0, partialTick);
    }



    private void drawPoise(MatrixStack matrixStack, MKPlayerData data, float partialTick) {
        float percentage;
        boolean isBroken = data.getStats().isPoiseBroke();
        if (data.getStats().getMaxPoise() > 0){
            if (isBroken) {
                percentage = data.getStats().getPoiseBreakPercent(partialTick);
            } else {
                percentage = data.getStats().getPoise() / data.getStats().getMaxPoise();
            }
            if (percentage > 1.0f){
                percentage = 1.0f;
            }
            int width = 50;
            int barSize = Math.round(width * percentage);
            int castStartX;
            int height = mc.getMainWindow().getScaledHeight();
            int castStartY;
            if (data.getEntity().isActiveItemStackBlocking()) {
                castStartY = height / 2 + 8;
                castStartX = mc.getMainWindow().getScaledWidth() / 2 - barSize / 2;
            } else {
                castStartX = (mc.getMainWindow().getScaledWidth() / 2) - 89 - 100;
                castStartY = height - 14;
            }

            GuiTextures.CORE_TEXTURES.bind(mc);
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            GuiTextures.CORE_TEXTURES.drawRegionAtPosPartialWidth(matrixStack, isBroken ? GuiTextures.POISE_BREAK : GuiTextures.POISE_BAR, castStartX, castStartY, barSize);
            GuiTextures.CORE_TEXTURES.drawRegionAtPos(matrixStack, GuiTextures.SHORT_BAR_OUTLINE, castStartX, castStartY - 1);
        }
    }

    private void drawHP(MatrixStack matrixStack, MKPlayerData data, float partialTick) {
        boolean isWithered = data.getEntity().getActivePotionEffect(Effects.WITHER) != null;
        float absorption = data.getEntity().getAbsorptionAmount();
        float maxHp = data.getEntity().getMaxHealth();
        float current_hp = data.getEntity().getHealth();
        String textureName = isWithered ? GuiTextures.WITHER_BAR_LONG : GuiTextures.HP_BAR_LONG;
        float percentage = current_hp / maxHp;
        if (percentage > 1.0f){
            percentage = 1.0f;
        }
        int width = 75;
        int barSize = Math.round(width * percentage);
        if (current_hp > 0 && barSize < 1){
            barSize = 1;
        }
        int height = mc.getMainWindow().getScaledHeight();
        int winWidth = mc.getMainWindow().getScaledWidth();
        int castStartY = height - 40;
        int castStartX = (winWidth / 2) - 89;
        GuiTextures.CORE_TEXTURES.bind(mc);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        GuiTextures.CORE_TEXTURES.drawRegionAtPosPartialWidth(matrixStack, textureName, castStartX, castStartY, barSize);
        GuiTextures.CORE_TEXTURES.drawRegionAtPos(matrixStack, GuiTextures.PLAYER_BAR_OUTLINE, castStartX, castStartY - 1);
        if (absorption > 0.0f){
            float absorpPercentage = absorption / maxHp;
            if (absorpPercentage > 1.0f){
                absorpPercentage = 1.0f;
            }
            int abarSize = Math.round(width * absorpPercentage);
            if (abarSize < 1){
                abarSize = 1;
            }
            GuiTextures.CORE_TEXTURES.drawRegionAtPosPartialWidth(matrixStack, GuiTextures.ABSORPTION_BAR_LONG,
                    castStartX, castStartY - 1, abarSize);
        }

    }

    private void drawCastBar(MatrixStack matrixStack, MKPlayerData data) {
        PlayerAbilityExecutor executor = data.getAbilityExecutor();
        if (!executor.isCasting()) {
            return;
        }

        MKAbility ability = executor.getCastingAbility();
        if (ability == null) {
            return;
        }

        int castTime = data.getStats().getAbilityCastTime(ability);
        if (castTime == 0) {
            return;
        }
        int height = mc.getMainWindow().getScaledHeight();
        int castStartY = height / 2 + 8;
        int width = 50;
        int barSize = width * executor.getCastTicks() / castTime;
        int castStartX = mc.getMainWindow().getScaledWidth() / 2 - barSize / 2;

        GuiTextures.CORE_TEXTURES.bind(mc);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        GuiTextures.CORE_TEXTURES.drawRegionAtPosPartialWidth(matrixStack, GuiTextures.CAST_BAR_REGION, castStartX, castStartY, barSize);
    }

    private int getBarStartY(int slotCount) {
        int height = mc.getMainWindow().getScaledHeight();
        int barStart = height / 2 - (slotCount * SLOT_HEIGHT) / 2;
        return Math.max(barStart, MIN_BAR_START_Y);
    }

    private String getAbilityGroupTexture(AbilityGroupId group) {
        if (group == AbilityGroupId.Basic) {
            return GuiTextures.ABILITY_BAR_REG;
        } else if (group == AbilityGroupId.Ultimate) {
            return GuiTextures.ABILITY_BAR_ULT;
        } else if (group == AbilityGroupId.Item) {
            // TODO: item slot texture?
            return GuiTextures.ABILITY_BAR_REG;
        }
        return null;
    }

    private void drawBarSlots(MatrixStack matrixStack, AbilityGroupId group, int startSlot, int slotCount, int totalSlots) {
        GuiTextures.CORE_TEXTURES.bind(mc);
        int xOffset = 0;
        int yOffset = getBarStartY(totalSlots);
        for (int i = startSlot; i < (startSlot + slotCount); i++) {
            int yPos = yOffset - i + i * SLOT_HEIGHT;
            String texture = getAbilityGroupTexture(group);
            if (texture != null) {
                GuiTextures.CORE_TEXTURES.drawRegionAtPos(matrixStack, texture, xOffset, yPos);
            }
        }
    }

    private int drawAbilities(MatrixStack matrixStack, MKPlayerData data, AbilityGroupId group, int startingSlot, int totalSlots, float partialTicks) {
        RenderSystem.disableLighting();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        final int slotAbilityOffsetX = 2;
        final int slotAbilityOffsetY = 2;

        int barStartY = getBarStartY(totalSlots);

        AbilityGroup abilityGroup = data.getLoadout().getAbilityGroup(group);
        int slotCount = abilityGroup.getCurrentSlotCount();
        drawBarSlots(matrixStack, group, startingSlot, slotCount, totalSlots);

        PlayerAbilityExecutor executor = data.getAbilityExecutor();
        float globalCooldown = executor.getGlobalCooldownPercent(partialTicks);

        for (int i = 0; i < slotCount; i++) {
            ResourceLocation abilityId = abilityGroup.getSlot(i);
            if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
                continue;

            MKAbility ability = MKCoreRegistry.getAbility(abilityId);
            if (ability == null)
                continue;

            float manaCost = data.getStats().getAbilityManaCost(ability);
            if (!executor.isCasting() && data.getStats().getMana() >= manaCost) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            } else {
                RenderSystem.color4f(0.5f, 0.5f, 0.5f, 1.0F);
            }

            int slotX = slotAbilityOffsetX;
            int slotY = barStartY + slotAbilityOffsetY - (startingSlot + i) + ((startingSlot + i) * SLOT_HEIGHT);

            mc.getTextureManager().bindTexture(ability.getAbilityIcon());
            AbstractGui.blit(matrixStack, slotX, slotY, 0, 0, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE);

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            float cooldownFactor = executor.getCurrentAbilityCooldownPercent(abilityId, partialTicks);
            if (globalCooldown > 0.0f && cooldownFactor == 0) {
                cooldownFactor = globalCooldown / ClientEventHandler.getTotalGlobalCooldown();
            }

            // TODO: introduce min cooldown time so there is always a visual indicator that it's on cooldown
            if (cooldownFactor > 0) {
                int coolDownHeight = (int) (cooldownFactor * ABILITY_ICON_SIZE);
                if (coolDownHeight < 1) {
                    coolDownHeight = 1;
                }
                mc.getTextureManager().bindTexture(COOLDOWN_ICON);
                AbstractGui.blit(matrixStack, slotX, slotY, 0, 0, ABILITY_ICON_SIZE, coolDownHeight, ABILITY_ICON_SIZE, coolDownHeight);
            }

            ability.getRenderer().drawAbilityBarEffect(data, matrixStack, mc, slotX, slotY);
        }
        RenderSystem.disableBlend();
        return startingSlot + slotCount;
    }


    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.HEALTH){
            event.setCanceled(true);
            ForgeIngameGui.left_height += 13;
            return;
        }
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        if (mc == null || mc.player == null || mc.gameSettings.hideGUI)
            return;

        mc.player.getCapability(CoreCapabilities.PLAYER_CAPABILITY).ifPresent(cap -> {

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            if (mc.playerController != null && mc.playerController.shouldDrawHUD() && mc.getRenderViewEntity() instanceof PlayerEntity){
                drawHP(event.getMatrixStack(), cap, event.getPartialTicks());
                drawMana(event.getMatrixStack(), cap);
                drawPoise(event.getMatrixStack(), cap, event.getPartialTicks());
                drawXpBar(event.getMatrixStack(), cap, event.getPartialTicks());
                drawTeam(event.getMatrixStack(), cap, event.getPartialTicks());
            }
            drawCastBar(event.getMatrixStack(), cap);


            int totalSlots = Arrays.stream(AbilityGroupId.values())
                    .filter(AbilityGroupId::isActive)
                    .mapToInt(type -> cap.getLoadout().getAbilityGroup(type).getCurrentSlotCount())
                    .sum();

            int slot = drawAbilities(event.getMatrixStack(), cap, AbilityGroupId.Basic, 0, totalSlots, event.getPartialTicks());
            slot = drawAbilities(event.getMatrixStack(), cap, AbilityGroupId.Ultimate, slot, totalSlots, event.getPartialTicks());
            slot = drawAbilities(event.getMatrixStack(), cap, AbilityGroupId.Item, slot, totalSlots, event.getPartialTicks());
        });
    }
}
