package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class PlayerSkills {
    private final MKPlayerData playerData;

    public PlayerSkills(MKPlayerData playerData){
        this.playerData = playerData;
    }


    public void onCastAbility(MKAbility cast){
        for (Attribute attribute : cast.getSkillAttributes()){
            tryIncreaseSkill(attribute);
        }
    }

    public void tryIncreaseSkill(Attribute attribute){
        PlayerEntity player = playerData.getEntity();
        double currentSkill = player.getAttributeManager().getAttributeBaseValue(attribute);
        if (currentSkill < GameConstants.NATURAL_SKILL_MAX){
            double chance = getChanceToIncreaseSkill(currentSkill);
            if (player.getRNG().nextDouble() <= chance){
                ModifiableAttributeInstance attrInst = player.getAttributeManager().createInstanceIfAbsent(attribute);
                if (attrInst != null){
                    attrInst.setBaseValue(currentSkill + 1.0);
                    player.sendMessage(new TranslationTextComponent("mkcore.skill.increase",
                            I18n.format(attrInst.getAttribute().getAttributeName(), attrInst.getBaseValue()))
                            .mergeStyle(TextFormatting.AQUA), Util.DUMMY_UUID);
                }
            }
        }
    }

    private double getChanceToIncreaseSkill(double currentSkill){
        return 1.0 / (5.0 + currentSkill);
    }
}
