package com.chaosbuffalo.mkcore.utils.text;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TranslatableComponent;

public class IconTextComponent extends TranslatableComponent {

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

    public IconTextComponent plainCopy() {
        return new IconTextComponent(getIcon(), getKey(), getArgs());
    }
}
