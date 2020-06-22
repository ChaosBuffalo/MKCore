package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.gui.CharacterScreen;
import com.chaosbuffalo.mkcore.client.gui.GuiTextures;
import com.chaosbuffalo.mkcore.client.gui.constraints.CopyConstraint;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerSlotAbilityPacket;
import com.chaosbuffalo.mkwidgets.client.gui.actions.WidgetHoldingDragState;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.*;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class AbilitySlotWidget extends MKLayout {
    private MKAbility.AbilityType slotType;
    private boolean unlocked;
    private final int slotIndex;
    private CharacterScreen screen;
    private ResourceLocation abilityName;
    private MKImage icon;

    public AbilitySlotWidget(int x, int y, MKAbility.AbilityType slotType, int slotIndex, CharacterScreen screen) {
        super(x, y, 20, 20);
        this.slotType = slotType;
        this.screen = screen;
        this.slotIndex = slotIndex;
        this.setMargins(2, 2, 2, 2);
        this.abilityName = MKCoreRegistry.INVALID_ABILITY;
        this.unlocked = getUnlocked(slotType, slotIndex);
        this.icon = null;
        MKImage background = getImageForSlotType(slotType, unlocked);
        addWidget(background);
        addConstraintToWidget(new CopyConstraint(), background);
        Minecraft.getInstance().player.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent((playerData -> {
            ResourceLocation abilityName = playerData.getKnowledge().getActionBar()
                    .getAbilityInSlot(getSlotIndexForSlotType(slotType, slotIndex));
            this.abilityName = abilityName;
            setupIcon(abilityName);

        }));
    }

    private void setupIcon(ResourceLocation abilityName){
        if (icon != null){
            removeWidget(icon);
        }
        if (!this.abilityName.equals(MKCoreRegistry.INVALID_ABILITY)){
            MKAbility ability = MKCoreRegistry.getAbility(abilityName);
            if (ability != null){
                MKCore.LOGGER.info("Adding icon to slot {} {}", ability.getAbilityIcon(), slotIndex);
                icon = new MKImage(0, 0, 16, 16, ability.getAbilityIcon());
                addWidget(icon);
                addConstraintToWidget(new MarginConstraint(MarginConstraint.MarginType.TOP), icon);
                addConstraintToWidget(new MarginConstraint(MarginConstraint.MarginType.LEFT), icon);
            }
        }
    }

    private int getSlotIndexForSlotType(MKAbility.AbilityType slotType, int slotIndex){
        switch (slotType){
            case Ultimate:
                return 4 + slotIndex;
            case Passive:
                return 7 + slotIndex;
            default:
                return slotIndex;
        }
    }

    private boolean getUnlocked(MKAbility.AbilityType slotType, int slotIndex){
        switch (slotType){
            case Ultimate:
                return slotIndex < 1;
            case Passive:
                return slotIndex < 2;
            default:
                return slotIndex < 4;
        }
    }

    private MKImage getImageForSlotType(MKAbility.AbilityType slotType, boolean unlocked){
        switch (slotType){
            case Ultimate:
                return GuiTextures.CORE_TEXTURES.getImageForRegion(unlocked ?
                                GuiTextures.ABILITY_SLOT_ULT : GuiTextures.ABILITY_SLOT_ULT_LOCKED,
                        getX(), getY(), getWidth(), getHeight());
            case Passive:
                return GuiTextures.CORE_TEXTURES.getImageForRegion(unlocked ?
                                GuiTextures.ABILITY_SLOT_PASSIVE : GuiTextures.ABILITY_SLOT_PASSIVE_LOCKED,
                        getX(), getY(), getWidth(), getHeight());
            default:
                return GuiTextures.CORE_TEXTURES.getImageForRegion(unlocked ?
                                GuiTextures.ABILITY_SLOT_REG : GuiTextures.ABILITY_SLOT_REG_LOCKED,
                        getX(), getY(), getWidth(), getHeight());
        }
    }

    private void setSlotToAbility(ResourceLocation ability){
        int typedSlotIndex = getSlotIndexForSlotType(slotType, slotIndex);
        PacketHandler.sendMessageToServer(new PlayerSlotAbilityPacket(typedSlotIndex, ability));
        this.abilityName = ability;
        setupIcon(ability);
        Minecraft.getInstance().player.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(playerData -> {
            playerData.getKnowledge().getActionBar().setAbilityInSlot(typedSlotIndex, ability);
        });
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        if (!(abilityName.equals(MKCoreRegistry.INVALID_ABILITY))){
            MKAbility ability = MKCoreRegistry.getAbility(abilityName);
            if (ability == null){
                return false;
            }
            screen.setDragState(new WidgetHoldingDragState(new MKImage(0, 0, icon.getWidth(),
                    icon.getHeight(), icon.getImageLoc())));
            screen.setDragging(ability);
            setSlotToAbility(MKCoreRegistry.INVALID_ABILITY);
            return true;
        }
        return false;
    }

    @Override
    public boolean onMouseRelease(double mouseX, double mouseY, int mouseButton) {
        if (screen.isDraggingAbility()){
            MKCore.LOGGER.info("adding ability {} to slot {}", screen.getDragging(), slotIndex);
            if (unlocked && screen.getDragging().getType().equals(slotType)){
                ResourceLocation ability = screen.getDragging().getAbilityId();
                setSlotToAbility(ability);
            }
            screen.clearDragging();
            screen.clearDragState();
            return true;
        }
        return false;
    }
}
