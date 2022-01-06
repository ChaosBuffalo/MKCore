package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.gui.AbilityPanelScreen;
import com.chaosbuffalo.mkcore.client.gui.IAbilityScreen;
import com.chaosbuffalo.mkwidgets.client.gui.actions.WidgetHoldingDragState;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class AbilityIconText extends IconText {
    private final IAbilityScreen screen;
    private final MKAbility ability;

    public AbilityIconText(int x, int y, int height, ITextComponent text, ResourceLocation iconLoc,
                           FontRenderer font, int iconWidth, IAbilityScreen screen, MKAbility ability) {
        super(x, y, height, text, iconLoc, font, iconWidth, 1);
        this.screen = screen;
        this.ability = ability;
    }

    public AbilityIconText(int x, int y, int height, FontRenderer font, int iconWidth, IAbilityScreen screen, MKAbility ability) {
        super(x, y, height, ability.getAbilityName(), ability.getAbilityIcon(), font, iconWidth, 1);
        this.screen = screen;
        this.ability = ability;
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        if (screen.shouldAbilityDrag()) {
            screen.setDragState(new WidgetHoldingDragState(new MKImage(0, 0, icon.getWidth(),
                    icon.getHeight(), icon.getImageLoc())), this);
            screen.setDragging(ability);
            return true;
        } else {
            return false;
        }

    }
}
