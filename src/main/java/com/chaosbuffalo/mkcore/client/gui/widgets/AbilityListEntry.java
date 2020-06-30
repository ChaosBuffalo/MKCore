package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.client.gui.AbilityPanelScreen;
import com.chaosbuffalo.mkcore.client.gui.CharacterScreen;
import com.chaosbuffalo.mkcore.client.gui.constraints.CenterYWithOffsetConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.actions.WidgetHoldingDragState;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class AbilityListEntry extends MKStackLayoutHorizontal {
    private final MKAbilityInfo info;
    private final AbilityInfoWidget infoWidget;
    private AbilityPanelScreen screen;
    private final MKImage icon;


    public AbilityListEntry(int x, int y, int height, MKAbilityInfo info, AbilityInfoWidget infoWidget,
                            FontRenderer font, AbilityPanelScreen screen) {
        super(x, y, height);
        this.info = info;
        this.infoWidget = infoWidget;
        this.screen = screen;
        setPaddingRight(2);
        setPaddingLeft(2);
        icon = new MKImage(0, 0, 16, 16, info.getAbility().getAbilityIcon()) {
            @Override
            public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
                if (screen.shouldAbilityDrag()){
                    screen.setDragState(new WidgetHoldingDragState(new MKImage(0, 0, icon.getWidth(),
                            icon.getHeight(), icon.getImageLoc())), this);
                    screen.setDragging(info.getAbility());
                    infoWidget.setAbilityInfo(info);
                    screen.setAbilityInfo(info);
                    return true;
                }
                return false;
            }
        };
        addWidget(icon);
        MKText name = new MKText(font, info.getAbility().getAbilityName());
        name.setWidth(100);
        name.setColor(0xffffffff);
        addWidget(name);
        addConstraintToWidget(new CenterYWithOffsetConstraint(1), name);
    }

    @Override
    public void postDraw(Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        if (isHovered()){
            mkFill(x, y, x + width, y + height, 0x55ffffff);
        }
        if (info.equals(screen.getAbilityInfo())){
            mkFill(x, y, x + width, y + height, 0x99ffffff);
        }
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        MKCore.LOGGER.info("On mouse press: {}", info.getAbility().getAbilityId());
        infoWidget.setAbilityInfo(info);
        screen.setAbilityInfo(info);
        return true;
    }
}
