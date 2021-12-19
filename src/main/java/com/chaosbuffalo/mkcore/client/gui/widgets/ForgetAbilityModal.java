package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.gui.GuiTextures;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerLearnAbilityRequestPacket;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.*;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ForgetAbilityModal extends MKModal {

    private final List<MKAbility> forgeting = new ArrayList<>();
    private final int numberToForget;
    private final MKButton forgetButton;
    private final MKAbility tryingToLearn;
    private final int trainerEntityId;

    public ForgetAbilityModal(MKAbility tryingToLearn, MKPlayerData playerData, int xPos, int yPos, int width, int height, FontRenderer font, int trainerEntityId){
        MKImage background = GuiTextures.CORE_TEXTURES.getImageForRegion(
                GuiTextures.BACKGROUND_180_200, xPos, yPos, width, height);
        addWidget(background);
        this.tryingToLearn = tryingToLearn;
        this.trainerEntityId = trainerEntityId;
        int count = playerData.getAbilities().getCurrentPoolCount() + 1 - playerData.getAbilities().getAbilityPoolSize();
        numberToForget = count;
        ITextComponent promptText = new TranslationTextComponent("mkcore.gui.character.forget_ability", count, tryingToLearn.getAbilityName());
        MKText prompt = new MKText(font, promptText, xPos + 6, yPos + 6);
        prompt.setWidth(width - 10);
        prompt.setMultiline(true);
        addWidget(prompt);
        addWidget(new MKRectangle(xPos + 10, yPos + 27, width - 20, height - 60, 0x44000000));
        MKScrollView scrollview = new MKScrollView(xPos + 15, yPos + 32, width - 30,
                height - 70, true);

        addWidget(scrollview);
        TranslationTextComponent text = new TranslationTextComponent("mkcore.gui.character.forget_confirm");
        forgetButton = new MKButton(scrollview.getX(), scrollview.getY() + scrollview.getHeight() + 10, text);
        forgetButton.setWidth(font.getStringPropertyWidth(text) + 20);
        forgetButton.setX(scrollview.getX() + (scrollview.getWidth() - forgetButton.getWidth()) / 2);
        forgetButton.setEnabled(ready());
        forgetButton.setPressedCallback(this::forgetCallback);
        addWidget(forgetButton);
        MKStackLayoutVertical abilities = new MKStackLayoutVertical(0, 0, scrollview.getWidth());
        abilities.setPaddingBot(2);
        abilities.setPaddingTop(2);
        abilities.setMargins(2, 2, 0, 0);
        abilities.doSetChildWidth(true);
        playerData.getAbilities().getPoolAbilities().forEach(abilityId -> {
            if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
                return;
            }
            MKAbility ability = MKCoreRegistry.getAbility(abilityId);
            if (ability != null) {
                AbilityForgetOption abilityIcon = new AbilityForgetOption(ability, tryingToLearn.getAbilityId(), this, font, trainerEntityId);
                abilities.addWidget(abilityIcon);
            }
        });
        scrollview.addWidget(abilities);
        abilities.manualRecompute();
        scrollview.setToRight();
        scrollview.setToTop();
    }

    private boolean forgetCallback(MKButton button, int click){
        PacketHandler.sendMessageToServer(new PlayerLearnAbilityRequestPacket(
                forgeting.stream().map(MKAbility::getAbilityId).collect(Collectors.toList()),
                tryingToLearn.getAbilityId(), trainerEntityId));
        if (getScreen() != null) {
            getScreen().closeModal(this);
        }
        return true;
    }

    private void checkStatus(){
        forgetButton.setEnabled(ready());
    }

    public void forget(MKAbility ability){
        forgeting.add(ability);
        checkStatus();
    }

    public void cancelForget(MKAbility ability){
        forgeting.remove(ability);
        checkStatus();
    }

    public boolean isForgetting(MKAbility ability){
        return forgeting.contains(ability);
    }

    public boolean ready(){
        return forgeting.size() == numberToForget;
    }

}
