package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingEvaluation;
import com.chaosbuffalo.mkcore.client.gui.widgets.ForgetAbilityModal;
import com.chaosbuffalo.mkcore.client.gui.widgets.LearnAbilityTray;
import com.chaosbuffalo.mkcore.client.gui.widgets.ScrollingListPanelLayout;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerLearnAbilityRequestPacket;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.OffsetConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.instructions.HoveringTextInstruction;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import com.chaosbuffalo.mkwidgets.utils.TextureRegion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Map;

public class LearnAbilitiesScreen extends AbilityPanelScreen {
    private final Map<MKAbility, AbilityTrainingEvaluation> abilities;
    private final int entityId;
    private LearnAbilityTray abilityTray;
    private MKLayout footer;
    private MKLayout root;

    public LearnAbilitiesScreen(ITextComponent title, Map<MKAbility, AbilityTrainingEvaluation> abilities, int entityId) {
        super(title);
        this.abilities = abilities;
        this.entityId = entityId;
        states.add("choose_ability");
        shortDataBoxScreens.add("choose_ability");
    }

    @Override
    public void setAbility(MKAbility ability) {
        super.setAbility(ability);
        if (abilityTray != null && abilities.containsKey(ability)) {
            abilityTray.setAbility(ability, abilities.get(ability));
            resetFooter();
        }
    }

    private MKLayout createPoolManagementFooter(MKPlayerData playerData){
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        int xOffset = GuiTextures.CORE_TEXTURES.getCenterXOffset(
                GuiTextures.DATA_BOX, GuiTextures.BACKGROUND_320_240);
        int yStart = yPos + DATA_BOX_OFFSET + 136;
        MKStackLayoutHorizontal layout = new MKStackLayoutHorizontal(xPos + xOffset, yStart, 20);
        layout.setMarginLeft(10);
        layout.setPaddingLeft(30);
        layout.setPaddingRight(30);
        TranslationTextComponent manageText = new TranslationTextComponent("mkcore.gui.manage_memory");
        MKButton manage = new MKButton(0, 0, manageText);
        manage.setWidth(font.getStringPropertyWidth(manageText) + 10);

        manage.setPressedCallback((but, click) -> {
            ForgetAbilityModal modal = getChoosePoolSlotWidget(playerData, abilityTray.getAbility(), abilityTray.getTrainerEntityId());
            addModal(modal);
            return true;
        });


        String learnButtonText = I18n.format("mkcore.gui.character.learn");
        MKButton learnButton = new MKButton(0, 0, learnButtonText) {

            @Override
            public boolean checkHovered(int mouseX, int mouseY) {
                return this.isVisible() && this.isInBounds(mouseX, mouseY);
            }

            @Override
            public void onMouseHover(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
                super.onMouseHover(mc, mouseX, mouseY, partialTicks);
                if (abilityTray != null && abilityTray.getEvaluation() != null && abilityTray.getEvaluation().getRequirements().size() > 0) {
                    if (getScreen() != null) {
                        getScreen().addPostRenderInstruction(new HoveringTextInstruction(
                                I18n.format("mkcore.gui.character.unmet_req_tooltip"),
                                getParentCoords(new Vec2i(mouseX, mouseY))));
                    }
                }
            }
        };
        learnButton.setWidth(font.getStringWidth(learnButtonText) + 10);
        learnButton.setEnabled(canLearnCurrentAbility(playerData));
        learnButton.setPressedCallback((button, buttonType) -> {
            if (abilityTray.getEvaluation().usesAbilityPool() && playerData.getAbilities().isAbilityPoolFull()) {
                addModal(getChoosePoolSlotWidget(playerData, abilityTray.getAbility(), abilityTray.getTrainerEntityId()));
            } else {
                PacketHandler.sendMessageToServer(new PlayerLearnAbilityRequestPacket(
                        abilityTray.getAbility().getAbilityId(), abilityTray.getTrainerEntityId()));
            }
            return true;
        });
        layout.addWidget(learnButton);

        TranslationTextComponent poolUsageText = new TranslationTextComponent("mkcore.gui.memory_pool",
                playerData.getAbilities().getCurrentPoolCount(), playerData.getAbilities().getAbilityPoolSize());
        MKText poolText = new MKText(font, poolUsageText);
        poolText.setWidth(font.getStringPropertyWidth(poolUsageText));
        layout.addWidget(poolText);
        layout.addConstraintToWidget(new OffsetConstraint(0, (20 - font.FONT_HEIGHT) / 2 + 1, false, true), poolText);
        layout.addWidget(manage);
        this.footer = layout;
        return layout;

    }

    private boolean canLearnCurrentAbility(MKPlayerData playerData){
        if (abilityTray.getAbility() != null && abilityTray.getEvaluation() != null){
            boolean isKnown = playerData.getAbilities().knowsAbility(abilityTray.getAbility().getAbilityId());
            boolean canLearn = abilityTray.getEvaluation().canLearn();
            return !isKnown && canLearn;
        } else {
            return false;
        }
    }

    private MKWidget createAbilitiesPage() {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        TextureRegion dataBoxRegion = GuiTextures.CORE_TEXTURES.getRegion(GuiTextures.DATA_BOX_SHORT);
        if (minecraft == null || minecraft.player == null || dataBoxRegion == null) {
            return new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        }
        int xOffset = GuiTextures.CORE_TEXTURES.getCenterXOffset(
                GuiTextures.DATA_BOX, GuiTextures.BACKGROUND_320_240);
        MKLayout root = getRootLayout(xPos, yPos, xOffset, dataBoxRegion.width, false);
        minecraft.player.getCapability(CoreCapabilities.PLAYER_CAPABILITY).ifPresent((pData) -> {
            int contentX = xPos + xOffset;
            int contentY = yPos + DATA_BOX_OFFSET;
            int contentWidth = dataBoxRegion.width;
            int contentHeight = dataBoxRegion.height;
            LearnAbilityTray tray = new LearnAbilityTray(contentX, yPos + 3, contentWidth, pData, font, entityId);
            abilityTray = tray;
            root.addWidget(tray);
            ScrollingListPanelLayout panel = getAbilityScrollPanel(contentX, contentY,
                    contentWidth, contentHeight, pData, new ArrayList<>(abilities.keySet()));
            currentScrollingPanel = panel;
            abilitiesScrollPanel = panel;

            MKLayout footer = createPoolManagementFooter(pData);
            root.addWidget(panel);
            root.addWidget(footer);

        });
        this.root = root;
        return root;
    }

    public void resetFooter(){
        if (minecraft != null && minecraft.player != null && root != null && footer != null){
            this.root.removeWidget(footer);
            minecraft.player.getCapability(CoreCapabilities.PLAYER_CAPABILITY).ifPresent(
                    (pData) -> root.addWidget(createPoolManagementFooter(pData)));
        }


    }

    @Override
    public void setupScreen() {
        super.setupScreen();
        infoWidget = null;
        currentScrollingPanel = null;
        abilityTray = null;
        addState("choose_ability", this::createAbilitiesPage);
        pushState("choose_ability");
    }

    @Override
    public void addRestoreStateCallbacks() {
        String state = getState();
        super.addRestoreStateCallbacks();
        if (state.equals("choose_ability")) {
            final MKAbility abilityInf = getAbility();
            addPostSetupCallback(() -> {
                if (infoWidget != null && abilities.containsKey(abilityInf)) {
                    infoWidget.setAbility(abilityInf);
                    abilityTray.setAbility(abilityInf, abilities.get(abilityInf));
                    resetFooter();
                }
            });
        }
    }
}
