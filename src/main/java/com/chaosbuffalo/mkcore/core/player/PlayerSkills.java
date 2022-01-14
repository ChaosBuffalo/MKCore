package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtension;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class PlayerSkills implements IMKSerializable<CompoundNBT> {
    private final MKPlayerData playerData;
    private final Map<Attribute, Double> skillValues = new HashMap<>();

    public PlayerSkills(MKPlayerData playerData){
        this.playerData = playerData;
    }


    public void onCastAbility(MKAbility cast){
        for (Attribute attribute : cast.getSkillAttributes()){
            tryIncreaseSkill(attribute);
        }
    }

    public void onPersonaActivated() {
        PlayerEntity player = playerData.getEntity();
        for (Map.Entry<Attribute, Double> entry : skillValues.entrySet()){
            ModifiableAttributeInstance attrInst = player.getAttribute(entry.getKey());
            if (attrInst != null){
                attrInst.setBaseValue(entry.getValue());
            }
        }
    }

    public void onPersonaDeactivated() {
        PlayerEntity player = playerData.getEntity();
        for (Map.Entry<Attribute, Double> entry : skillValues.entrySet()){
            ModifiableAttributeInstance attrInst = player.getAttribute(entry.getKey());
            if (attrInst != null){
                attrInst.setBaseValue(0.0);
            }
        }
    }

    private double getSkillValue(Attribute attribute){
        return skillValues.getOrDefault(attribute, 0.0);
    }

    public void tryIncreaseSkill(Attribute attribute){
        PlayerEntity player = playerData.getEntity();
        double currentSkill = getSkillValue(attribute);
        if (currentSkill < GameConstants.NATURAL_SKILL_MAX){
            double chance = getChanceToIncreaseSkill(currentSkill);
            if (player.getRNG().nextDouble() <= chance){
                skillValues.put(attribute, currentSkill + 1.0);
                player.sendMessage(new TranslationTextComponent("mkcore.skill.increase",
                        new TranslationTextComponent(attribute.getAttributeName()), currentSkill + 1.0)
                        .mergeStyle(TextFormatting.AQUA), Util.DUMMY_UUID);
                ModifiableAttributeInstance attrInst = player.getAttribute(attribute);
                if (attrInst != null){
                    attrInst.setBaseValue(currentSkill + 1.0);
                }
            }
        }
    }

    private double getChanceToIncreaseSkill(double currentSkill){
        return 1.0 / (5.0 + currentSkill);
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        CompoundNBT skillsNbt = new CompoundNBT();
        for (Map.Entry<Attribute, Double> entry : skillValues.entrySet()){
            skillsNbt.putDouble(entry.getKey().getRegistryName().toString(), entry.getValue());
        }
        tag.put("skills", skillsNbt);
        return tag;
    }

    @Override
    public boolean deserialize(CompoundNBT tag) {
        CompoundNBT skillsNbt = tag.getCompound("skills");
        for (String key : skillsNbt.keySet()){
            Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(key));
            if (attr != null){
                skillValues.put(attr, skillsNbt.getDouble(key));
            }
        }
        return true;
    }
}
