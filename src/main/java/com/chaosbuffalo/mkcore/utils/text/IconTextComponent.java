package com.chaosbuffalo.mkcore.utils.text;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class IconTextComponent extends TranslationTextComponent {

    private final ResourceLocation icon;

    public IconTextComponent(ResourceLocation icon, String translationKey) {
        super(translationKey);
        this.icon = icon;
    }

    public IconTextComponent(ResourceLocation icon, String translationKey, Object... args) {
        super(translationKey, args);
        this.icon = icon;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public IconTextComponent copyRaw() {
        return new IconTextComponent(getIcon(), getKey(), getFormatArgs());
    }
}
