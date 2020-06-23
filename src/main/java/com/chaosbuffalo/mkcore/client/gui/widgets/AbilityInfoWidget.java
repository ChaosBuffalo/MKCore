package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.client.gui.CharacterScreen;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.FontRenderer;

public class AbilityInfoWidget extends MKStackLayoutVertical {

    private final MKPlayerData playerData;
    private MKAbilityInfo abilityInfo;
    private final FontRenderer fontRenderer;
    private final CharacterScreen screen;

    public AbilityInfoWidget(int x, int y, int width, MKPlayerData playerData,
                             FontRenderer fontRenderer, CharacterScreen screen) {
        super(x, y, width);
        this.screen = screen;
        this.playerData = playerData;
        this.fontRenderer = fontRenderer;
        setMargins(4, 4, 4, 4);
        doSetChildWidth(true);
        setup();
    }

    public void setup(){
        if (abilityInfo == null){
            MKText noSelectPrompt = new MKText(fontRenderer,"Select An Ability to inspect it.");
            addWidget(noSelectPrompt);
        } else {
            IconText ability = new AbilityIconText(0, 0, 16,
                    abilityInfo.getAbility().getAbilityName(),
                    abilityInfo.getAbility().getAbilityIcon(),
                    fontRenderer, 16, screen, abilityInfo.getAbility());
            addWidget(ability);
        }
    }


    public void setAbilityInfo(MKAbilityInfo abilityInfo) {
        this.abilityInfo = abilityInfo;
        clearWidgets();
        setup();
    }

    public MKAbilityInfo getAbilityInfo() {
        return abilityInfo;
    }
}