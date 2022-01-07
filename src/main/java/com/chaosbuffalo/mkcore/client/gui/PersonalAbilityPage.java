package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.client.gui.widgets.AbilitySlotWidget;
import com.chaosbuffalo.mkcore.client.gui.widgets.IconText;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.OffsetConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import com.chaosbuffalo.mkwidgets.utils.TextureRegion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PersonalAbilityPage extends AbilityPageBase implements IAbilityScreen {

    public static class AbilitySlotKey {
        public AbilityGroupId group;
        public int slot;

        public AbilitySlotKey(AbilityGroupId group, int index) {
            this.group = group;
            this.slot = index;
        }

        @Override
        public int hashCode() {
            return slot + group.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof AbilitySlotKey) {
                AbilitySlotKey otherKey = (AbilitySlotKey) other;
                return slot == otherKey.slot && group.equals(otherKey.group);
            }
            return false;
        }
    }

    private final Map<AbilitySlotKey, AbilitySlotWidget> abilitySlots = new HashMap<>();

    public PersonalAbilityPage(MKPlayerData playerData) {
        super(playerData, new TranslationTextComponent("mk_character_screen.title"));
    }

    @Override
    public ResourceLocation getPageId() {
        return MKCore.makeRL("abilities");
    }


    @Override
    public void setupScreen() {
        super.setupScreen();
        addWidget(createAbilitiesPage());
    }

    @Override
    protected void persistState(boolean wasResized) {
        super.persistState(wasResized);
        final MKAbility abilityInf = getAbility();
        addPostSetupCallback(() -> {
            if (infoWidget != null) {
                infoWidget.setAbility(abilityInf);
            }
        });
    }

    private MKWidget createAbilitiesPage() {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        TextureRegion dataBoxRegion = GuiTextures.CORE_TEXTURES.getRegion(getDataBoxTexture());
        if (dataBoxRegion == null) {
            return new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        }
        int xOffset = GuiTextures.CORE_TEXTURES.getCenterXOffset(getDataBoxTexture(), GuiTextures.BACKGROUND_320_240);
        MKLayout root = getRootLayout(xPos, yPos, xOffset, dataBoxRegion.width, true);

        // Stat Panel
        int slotsY = yPos + DATA_BOX_OFFSET - 28;
        int slotsX = xPos + xOffset + 4;
        MKText activesLabel = new MKText(font, new TranslationTextComponent("mkcore.gui.actives"));
        activesLabel.setX(slotsX);
        activesLabel.setY(slotsY - 12);
        root.addWidget(activesLabel);
        MKLayout regularSlots = createAbilityGroupLayout(slotsX, slotsY, AbilityGroupId.Basic);
        root.addWidget(regularSlots);
        regularSlots.manualRecompute();

        int ultSlotsX = regularSlots.getX() + regularSlots.getWidth() + 30;
        MKLayout ultSlots = createAbilityGroupLayout(ultSlotsX, slotsY, AbilityGroupId.Ultimate);
        root.addWidget(ultSlots);
        ultSlots.manualRecompute();
        MKText ultLabel = new MKText(font, new TranslationTextComponent("mkcore.gui.ultimates"));
        ultLabel.setX(ultSlotsX);
        ultLabel.setY(slotsY - 12);
        root.addWidget(ultLabel);

        int passiveSlotX = ultSlots.getX() + ultSlots.getWidth() + 30;
        MKLayout passiveSlots = createAbilityGroupLayout(passiveSlotX, slotsY, AbilityGroupId.Passive);
        MKText passivesLabel = new MKText(font, new TranslationTextComponent("mkcore.gui.passives"));
        passivesLabel.setX(passiveSlotX);
        passivesLabel.setY(slotsY - 12);
        root.addWidget(passivesLabel);
        root.addWidget(passiveSlots);
        int contentX = xPos + xOffset;
        int contentY = yPos + DATA_BOX_OFFSET;
        int contentWidth = dataBoxRegion.width;
        int contentHeight = dataBoxRegion.height;
        List<MKAbility> abilities = playerData.getAbilities()
                .getKnownStream()
                .map(MKAbilityInfo::getAbility)
                .collect(Collectors.toList());
        abilitiesScrollPanel = getAbilityScrollPanel(contentX, contentY, contentWidth, contentHeight, abilities);
        root.addWidget(abilitiesScrollPanel);
        MKLayout footer = createPoolManagementFooter(playerData);
        root.addWidget(footer);
        return root;
    }

    private MKLayout createPoolManagementFooter(MKPlayerData playerData) {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        int xOffset = GuiTextures.CORE_TEXTURES.getCenterXOffset(getDataBoxTexture(), GuiTextures.BACKGROUND_320_240);
        int yStart = yPos + DATA_BOX_OFFSET + 136;
        MKStackLayoutHorizontal layout = new MKStackLayoutHorizontal(xPos + xOffset, yStart, 20);
        layout.setPaddingLeft(16);
        layout.setPaddingRight(16);
        int marginLeft = 116;
        layout.setMarginLeft(marginLeft);
        MKButton manage = createManageButton();

        IconText poolText = createPoolUsageText(playerData);
        layout.addWidget(poolText, new OffsetConstraint(0, 2, false, true));
        layout.addWidget(manage);
        return layout;

    }

    private MKLayout createAbilityGroupLayout(int x, int y, AbilityGroupId group) {
        MKStackLayoutHorizontal layout = new MKStackLayoutHorizontal(x, y, 24);
        layout.setPaddings(2, 2, 0, 0);
        layout.setMargins(2, 2, 2, 2);
        for (int i = 0; i < group.getMaxSlots(); i++) {
            AbilitySlotWidget slot = new AbilitySlotWidget(0, 0, group, i, this);
            abilitySlots.put(new AbilitySlotKey(slot.getSlotGroup(), slot.getSlotIndex()), slot);
            layout.addWidget(slot);
        }
        return layout;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        boolean handled = super.mouseReleased(mouseX, mouseY, mouseButton);
        if (isDraggingAbility) {
            clearDragging();
            clearDragState();
            return true;
        }
        return handled;
    }


    @Override
    public boolean shouldAbilityDrag() {
        return true;
    }

    @Override
    public void clearDragging() {
        for (AbilitySlotWidget widget : abilitySlots.values()) {
            widget.setBackgroundColor(0xffffffff);
            widget.setIconColor(0xffffffff);
        }
        super.clearDragging();
    }

    @Override
    public void setDragging(MKAbility dragging) {
        super.setDragging(dragging);
        abilitySlots.forEach((key, widget) -> {
            if (!key.group.fitsAbilityType(dragging.getType())) {
                widget.setBackgroundColor(0xff555555);
                widget.setIconColor(0xff555555);
            }
        });
    }
}
