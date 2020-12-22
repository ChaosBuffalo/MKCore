package com.chaosbuffalo.mkcore.core.talents;

import com.mojang.serialization.Dynamic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.regex.Pattern;

public abstract class BaseTalent extends ForgeRegistryEntry<BaseTalent> {

    public BaseTalent(ResourceLocation name) {
        setRegistryName(name);
    }

    public abstract TalentType<?> getTalentType();

    public <T> TalentNode createNode(Dynamic<T> dynamic) {
        return new TalentNode(this, dynamic);
    }

    public ITextComponent getTalentName() {
        return new TranslationTextComponent(String.format("%s.%s.name",
                getRegistryName().getNamespace(), getRegistryName().getPath()));
    }

    public ITextComponent getTalentDescription(TalentRecord record) {
        TranslationTextComponent comp = new TranslationTextComponent(String.format("%s.%s.description",
                getRegistryName().getNamespace(), getRegistryName().getPath()));
        return comp.mergeStyle(TextFormatting.GRAY);
    }

    public ITextComponent getTalentTypeName() {
        return getTalentType().getFullName().mergeStyle(TextFormatting.GOLD);
    }

    public ResourceLocation getIcon() {
        return new ResourceLocation(getRegistryName().getNamespace(),
                String.format("textures/talents/%s_icon.png",
                        getRegistryName().getPath().split(Pattern.quote("."))[1]));
    }

    public ResourceLocation getFilledIcon() {
        return new ResourceLocation(getRegistryName().getNamespace(),
                String.format("textures/talents/%s_icon_filled.png",
                        getRegistryName().getPath().split(Pattern.quote("."))[1]));
    }
}
