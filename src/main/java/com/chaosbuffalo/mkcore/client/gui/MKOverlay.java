package com.chaosbuffalo.mkcore.client.gui;


import com.chaosbuffalo.mkcore.ClientEventHandler;
import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.IActiveAbilityGroup;
import com.chaosbuffalo.mkcore.core.player.PlayerAbilityExecutor;
import com.chaosbuffalo.mkcore.core.player.PlayerAbilityKnowledge;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Arrays;

public class MKOverlay {

    private static final ResourceLocation COOLDOWN_ICON = MKCore.makeRL("textures/abilities/cooldown.png");

    private static final int SLOT_WIDTH = 20;
    private static final int SLOT_HEIGHT = 20;
    private static final int MIN_BAR_START_Y = 80;
    public static final int ABILITY_ICON_SIZE = 16;

    private final Minecraft mc;

    public MKOverlay() {
        mc = Minecraft.getInstance();
    }

    private void drawMana(MatrixStack matrixStack, MKPlayerData data) {
        int height = mc.getMainWindow().getScaledHeight();

        GuiTextures.CORE_TEXTURES.bind(mc);
        RenderSystem.disableLighting();

        final int maxManaPerRow = 20;
        final int manaCellWidth = 4;
        final int manaCellRowSize = 9;

        int manaStartY = height - 24 - 10;
        int manaStartX = 24;

        for (int i = 0; i < data.getStats().getMana(); i++) {
            int manaX = manaCellWidth * (i % maxManaPerRow);
            int manaY = (i / maxManaPerRow) * manaCellRowSize;
            GuiTextures.CORE_TEXTURES.drawRegionAtPos(matrixStack, GuiTextures.MANA_REGION, manaStartX + manaX, manaStartY + manaY);
        }
    }

    private void drawCastBar(MatrixStack matrixStack, MKPlayerData data) {
        PlayerAbilityExecutor executor = data.getAbilityExecutor();
        if (!executor.isCasting()) {
            return;
        }
        MKAbilityInfo info = data.getAbilities().getKnownAbility(executor.getCastingAbility());
        if (info == null) {
            return;
        }
        MKAbility ability = info.getAbility();
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

    private String getTextureForType(AbilityType type) {
        if (type == AbilityType.Basic) {
            return GuiTextures.ABILITY_BAR_REG;
        } else if (type == AbilityType.Ultimate) {
            return GuiTextures.ABILITY_BAR_ULT;
        } else if (type == AbilityType.Item) {
            // TODO: item slot texture?
            return GuiTextures.ABILITY_BAR_REG;
        }
        return null;
    }

    private void drawBarSlots(MatrixStack matrixStack, AbilityType type, int startSlot, int slotCount, int totalSlots) {
        GuiTextures.CORE_TEXTURES.bind(mc);
        RenderSystem.disableLighting();
        int xOffset = 0;
        int yOffset = getBarStartY(totalSlots);
        for (int i = startSlot; i < (startSlot + slotCount); i++) {
            int yPos = yOffset - i + i * SLOT_HEIGHT;
            String texture = getTextureForType(type);
            if (texture != null) {
                GuiTextures.CORE_TEXTURES.drawRegionAtPos(matrixStack, texture, xOffset, yPos);
            }
        }
    }

    private int drawAbilities(MatrixStack matrixStack, MKPlayerData data, AbilityType type, int startingSlot, int totalSlots, float partialTicks) {
        RenderSystem.disableLighting();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        final int slotAbilityOffsetX = 2;
        final int slotAbilityOffsetY = 2;

        int barStartY = getBarStartY(totalSlots);

        PlayerAbilityKnowledge abilityKnowledge = data.getAbilities();
        IActiveAbilityGroup container = data.getLoadout().getAbilityGroup(type);
        int slotCount = container.getCurrentSlotCount();
        drawBarSlots(matrixStack, type, startingSlot, slotCount, totalSlots);

        float globalCooldown = ClientEventHandler.getGlobalCooldown();
        PlayerAbilityExecutor executor = data.getAbilityExecutor();

        for (int i = 0; i < slotCount; i++) {
            ResourceLocation abilityId = container.getSlot(i);
            if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
                continue;

            MKAbilityInfo info = abilityKnowledge.getKnownAbility(abilityId);
            if (info == null)
                continue;

            MKAbility ability = info.getAbility();

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

            ability.getRenderer().drawAbilityBarEffect(matrixStack, mc, slotX, slotY);
        }
        RenderSystem.disableBlend();
        return startingSlot + slotCount;
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        if (mc == null || mc.player == null || mc.gameSettings.hideGUI)
            return;

        mc.player.getCapability(CoreCapabilities.PLAYER_CAPABILITY).ifPresent(cap -> {

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            drawMana(event.getMatrixStack(), cap);
            drawCastBar(event.getMatrixStack(), cap);

            int totalSlots = Arrays.stream(AbilityType.values())
                    .filter(AbilityType::isActive)
                    .mapToInt(type -> cap.getLoadout().getAbilityGroup(type).getCurrentSlotCount())
                    .sum();

            int slot = drawAbilities(event.getMatrixStack(), cap, AbilityType.Basic, 0, totalSlots, event.getPartialTicks());
            slot = drawAbilities(event.getMatrixStack(), cap, AbilityType.Ultimate, slot, totalSlots, event.getPartialTicks());
            slot = drawAbilities(event.getMatrixStack(), cap, AbilityType.Item, slot, totalSlots, event.getPartialTicks());
        });
    }
}
