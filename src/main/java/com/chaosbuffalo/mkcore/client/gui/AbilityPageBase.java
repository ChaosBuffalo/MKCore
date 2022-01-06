package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.gui.widgets.*;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKRectangle;
import com.chaosbuffalo.mkwidgets.utils.TextureRegion;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbilityPageBase extends PlayerPageBase implements IAbilityScreen {
    protected boolean isDraggingAbility;
    protected MKAbility dragging;
    protected AbilityInfoWidget infoWidget;
    protected ScrollingListPanelLayout abilitiesScrollPanel;
    private MKAbility ability;


    public AbilityPageBase(MKPlayerData playerData, ITextComponent title) {
        super(playerData, title);
    }

    protected String getDataBoxTexture() {
        return GuiTextures.DATA_BOX_SHORT;
    }

    protected MKLayout createDataBoxLayout(Consumer<MKLayout> contentMaker, boolean addStateButtons) {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        TextureRegion dataBoxRegion = GuiTextures.CORE_TEXTURES.getRegion(getDataBoxTexture());
        if (minecraft == null || minecraft.player == null || dataBoxRegion == null) {
            return new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        }
        int xOffset = GuiTextures.CORE_TEXTURES.getCenterXOffset(getDataBoxTexture(), GuiTextures.BACKGROUND_320_240);
        MKLayout root = getRootLayout(xPos, yPos, xOffset, dataBoxRegion.width, addStateButtons);
        int contentX = xPos + xOffset;
        int contentY = yPos + DATA_BOX_OFFSET;
        int contentWidth = dataBoxRegion.width;
        int contentHeight = dataBoxRegion.height;
        MKLayout content = new MKLayout(contentX, contentY, contentWidth, contentHeight);
        contentMaker.accept(content);
        return root;
    }

    public ScrollingListPanelLayout getAbilityScrollPanel(int xPos, int yPos, int width, int height,
                                                          List<MKAbility> abilities) {
        ScrollingListPanelLayout panel = new ScrollingListPanelLayout(xPos, yPos, width, height);
        infoWidget = new AbilityInfoWidget(0, 0, panel.getContentScrollView().getWidth(), playerData, font, this);
        panel.setContent(infoWidget);

        MKStackLayoutVertical stackLayout = new MKStackLayoutVertical(0, 0, panel.getListScrollView().getWidth());
        stackLayout.setMargins(4, 4, 4, 4);
        stackLayout.setPaddings(0, 2, 2, 2);
        stackLayout.doSetChildWidth(true);
        abilities.stream()
                .sorted(Comparator.comparing(a -> a.getAbilityName().getString()))
                .forEach(ability -> {
                    MKLayout abilityEntry = new AbilityListEntry(0, 0, 16, ability, infoWidget, font, this);
                    stackLayout.addWidget(abilityEntry);
                    MKRectangle div = new MKRectangle(0, 0, panel.getListScrollView().getWidth() - 8, 1, 0x99ffffff);
                    stackLayout.addWidget(div);
                });
        panel.setList(stackLayout);
        return panel;
    }

    protected ForgetAbilityModal getChoosePoolSlotWidget(MKPlayerData playerData, MKAbility tryingToLearn, int trainingId) {
        int screenWidth = getWidth();
        int screenHeight = getHeight();
        int xPos = (screenWidth - POPUP_WIDTH) / 2;
        int yPos = (screenHeight - POPUP_HEIGHT) / 2;
        return new ForgetAbilityModal(tryingToLearn, playerData, xPos, yPos, POPUP_WIDTH, POPUP_HEIGHT, font, trainingId);
    }

    protected MKButton createManageButton() {
        TranslationTextComponent manageText = new TranslationTextComponent("mkcore.gui.manage_memory");
        MKButton manage = new MKButton(0, 0, manageText);
        manage.setWidth(60);

        manage.setPressedCallback((but, click) -> {
            ForgetAbilityModal modal = getChoosePoolSlotWidget(playerData, null, -1);
            addModal(modal);
            return true;
        });
        return manage;
    }

    @Nonnull
    protected IconText createPoolUsageText(MKPlayerData playerData) {
        TranslationTextComponent poolUsageText = new TranslationTextComponent("mkcore.gui.memory_pool",
                playerData.getAbilities().getCurrentPoolCount(), playerData.getAbilities().getAbilityPoolSize());
        IconText poolText = new IconText(0, 0, 16, poolUsageText, MKAbility.POOL_SLOT_ICON, font, 16, 2);
        poolText.setTooltip(new TranslationTextComponent("mkcore.gui.memory_pool_tooltip"));
        poolText.manualRecompute();
        int margins = 100 - poolText.getWidth();
        poolText.setMarginLeft(margins / 2);
        poolText.setMarginRight(margins / 2);
        poolText.getText().setColor(0xff000000);
        return poolText;
    }

    @Override
    protected void persistState(boolean wasResized) {
        super.persistState(wasResized);
        final MKAbility abilityInf = getAbility();
        addPostSetupCallback(() -> setAbility(abilityInf));
        persistScrollingListPanelState(() -> abilitiesScrollPanel, wasResized);
    }

    @Override
    public boolean shouldAbilityDrag() {
        return false;
    }

    public MKAbility getDragging() {
        return dragging;
    }

    public void setDragging(MKAbility dragging) {
        this.dragging = dragging;
        isDraggingAbility = true;
    }

    public void setAbility(MKAbility ability) {
        this.ability = ability;
        abilitiesScrollPanel.getContentScrollView().setToTop();
        abilitiesScrollPanel.getContentScrollView().setToRight();
    }

    public MKAbility getAbility() {
        return ability;
    }

    public void clearDragging() {
        this.dragging = null;
        isDraggingAbility = false;
    }

    public boolean isDraggingAbility() {
        return isDraggingAbility;
    }
}
