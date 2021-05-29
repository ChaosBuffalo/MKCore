package com.chaosbuffalo.mkcore.core.talents;

import com.mojang.serialization.Dynamic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.regex.Pattern;

public abstract class MKTalent extends ForgeRegistryEntry<MKTalent> {

    public MKTalent(ResourceLocation name) {
        setRegistryName(name);
    }

    public abstract TalentType<?> getTalentType();

    public <T> TalentNode createNode(Dynamic<T> dynamic) {
        return new TalentNode(this, dynamic);
    }

    @Nonnull
    public ResourceLocation getTalentId() {
        return Objects.requireNonNull(getRegistryName());
    }

    public ITextComponent getTalentName() {
        ResourceLocation talentId = getTalentId();
        return new TranslationTextComponent(String.format("%s.%s.name", talentId.getNamespace(), talentId.getPath()));
    }

    public ITextComponent getTalentDescription(TalentRecord record) {
        ResourceLocation talentId = getTalentId();
        TranslationTextComponent comp = new TranslationTextComponent(String.format("%s.%s.description", talentId.getNamespace(), talentId.getPath()));
        return comp.mergeStyle(TextFormatting.GRAY);
    }

    public ITextComponent getTalentTypeName() {
        return getTalentType().getDisplayName().mergeStyle(TextFormatting.GOLD);
    }

    public ResourceLocation getIcon() {
        ResourceLocation talentId = getTalentId();
        return new ResourceLocation(talentId.getNamespace(),
                String.format("textures/talents/%s_icon.png",
                        talentId.getPath().split(Pattern.quote("."))[1]));
    }

    public ResourceLocation getFilledIcon() {
        ResourceLocation talentId = getTalentId();
        return new ResourceLocation(talentId.getNamespace(),
                String.format("textures/talents/%s_icon_filled.png",
                        talentId.getPath().split(Pattern.quote("."))[1]));
    }
}
