package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingEvaluation;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerLearnAbilityRequestPacket;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeWidthConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.instructions.HoveringTextInstruction;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.stream.Collectors;

public class LearnAbilityTray extends MKStackLayoutVertical {
    private MKAbility ability;
    private AbilityTrainingEvaluation evaluation;
    private final MKPlayerData playerData;
    private final FontRenderer font;
    private final int trainerEntityId;

    public LearnAbilityTray(int x, int y, int width, MKPlayerData playerData, FontRenderer font, int trainerEntityId) {
        super(x, y, width);
        this.playerData = playerData;
        this.trainerEntityId = trainerEntityId;
        this.font = font;
        this.ability = null;
        setMarginTop(2);
        setMarginBot(2);
        setPaddingTop(2);
        setPaddingBot(2);
        setup();
    }

    public AbilityTrainingEvaluation getEvaluation() {
        return evaluation;
    }

    public int getTrainerEntityId() {
        return trainerEntityId;
    }

    public void setup() {
        clearWidgets();
        if (getAbility() != null) {
            MKStackLayoutHorizontal nameTray = new MKStackLayoutHorizontal(0, 0, 20);
            nameTray.setPaddingRight(4);
            nameTray.setPaddingLeft(4);
            IconText abilityName = new IconText(0, 0, 16, getAbility().getAbilityName(),
                    getAbility().getAbilityIcon(), font, 16, 1);
            nameTray.addWidget(abilityName);
            addWidget(nameTray);
            boolean isKnown = playerData.getAbilities().knowsAbility(getAbility().getAbilityId());
            boolean canLearn = evaluation.canLearn();
            String knowText;
            if (isKnown) {
                knowText = I18n.format("mkcore.gui.character.already_known");
            } else if (!canLearn) {
                knowText = I18n.format("mkcore.gui.character.unmet_req");
            } else {
                knowText = I18n.format("mkcore.gui.character.can_learn");
            }
            MKText doesKnowWid = new MKText(font, knowText);
            doesKnowWid.setWidth(font.getStringWidth(knowText));
            addWidget(doesKnowWid);
            MKScrollView reqScrollView = new MKScrollView(0, 0, getWidth(), 40,
                    true);
            addWidget(reqScrollView);
            manualRecompute();
            MKStackLayoutVertical reqlayout = new MKStackLayoutVertical(0, 0, getWidth());
            reqlayout.setPaddingBot(1);
            reqlayout.setPaddingTop(1);
            reqScrollView.addWidget(reqlayout);
            List<ITextComponent> texts = evaluation.getRequirements().stream()
                    .map((x) -> new StringTextComponent("  - ")
                            .appendSibling(x.description())
                            .mergeStyle(x.isMet() ? TextFormatting.DARK_GREEN : TextFormatting.BLACK))
                    .collect(Collectors.toList());
            for (ITextComponent text : texts) {
                MKText reqText = new MKText(font, text);
                reqText.setMultiline(true);
                reqlayout.addConstraintToWidget(new LayoutRelativeWidthConstraint(1.0f), reqText);
                reqlayout.addWidget(reqText);
            }
            reqlayout.manualRecompute();
        } else {
            MKText prompt = new MKText(font, I18n.format("mkcore.gui.character.learn_ability_prompt"));
            addWidget(prompt);
        }
    }

    public void setAbility(MKAbility ability, AbilityTrainingEvaluation requirements) {
        this.ability = ability;
        this.evaluation = requirements;
        setup();
    }

    public MKAbility getAbility() {
        return ability;
    }

}
