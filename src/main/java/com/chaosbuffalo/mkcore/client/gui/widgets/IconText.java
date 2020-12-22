package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.client.gui.constraints.CenterYWithOffsetConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class IconText extends MKStackLayoutHorizontal {

    protected MKText text;
    protected MKImage icon;

    public IconText(int x, int y, int height, ITextComponent textStr, ResourceLocation iconLoc,
                    FontRenderer font, int iconWidth, int offset) {
        super(x, y, height);
        setPaddingRight(2);
        setPaddingLeft(2);
        icon = new MKImage(0, 0, iconWidth, height, iconLoc);
        addWidget(icon);
        text = new MKText(font, textStr);
        text.setWidth(font.getStringPropertyWidth(textStr));
        text.setColor(0xffffffff);
        addWidget(text);
        addConstraintToWidget(new CenterYWithOffsetConstraint(offset), text);
    }

    public MKText getText() {
        return text;
    }

}
