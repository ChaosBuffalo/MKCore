package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenCustomHashMap;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerSkills implements IMKSerializable<CompoundNBT> {
    private static final UUID blockScalerUUID = UUID.fromString("8cabfe08-4ad3-4b8a-9b94-cb146f743c36");

    protected abstract static class SkillHandler {
        protected abstract void onSkillChange(MKPlayerData playerData, double value);

        protected static class BlockHandler extends SkillHandler {

            @Override
            protected void onSkillChange(MKPlayerData playerData, double value) {
                ModifiableAttributeInstance inst = playerData.getEntity().getAttribute(MKAttributes.MAX_POISE);
                if (inst != null){
                    inst.removeModifier(blockScalerUUID);
                    inst.applyNonPersistentModifier(new AttributeModifier(blockScalerUUID, "block skill",
                            MKAbility.convertSkillToMultiplier(value), AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            }
        }
    }


    private final MKPlayerData playerData;
    private final Object2DoubleMap<Attribute> skillValues = new Object2DoubleOpenCustomHashMap<>(Util.identityHashStrategy());
    private final Map<Attribute, SkillHandler> skillHandlers = new HashMap<>();

    public PlayerSkills(MKPlayerData playerData) {
        this.playerData = playerData;
        skillHandlers.put(MKAttributes.BLOCK, new SkillHandler.BlockHandler());
    }

    public void onCastAbility(MKAbility cast) {
        for (Attribute attribute : cast.getSkillAttributes()) {
            tryIncreaseSkill(attribute);
        }
    }

    public void onPersonaActivated() {
        PlayerEntity player = playerData.getEntity();
        for (Object2DoubleMap.Entry<Attribute> entry : skillValues.object2DoubleEntrySet()) {
            ModifiableAttributeInstance attrInst = player.getAttribute(entry.getKey());
            if (attrInst != null) {
                attrInst.setBaseValue(entry.getDoubleValue());
                onSetSkill(entry.getKey(), entry.getDoubleValue());
            }
        }
    }

    public void onSetSkill(Attribute attribute, double skillLevel){
        if (skillHandlers.containsKey(attribute)){
            skillHandlers.get(attribute).onSkillChange(playerData, skillLevel);
        }
    }

    public void onPersonaDeactivated() {
        PlayerEntity player = playerData.getEntity();
        for (Attribute key : skillValues.keySet()) {
            ModifiableAttributeInstance attrInst = player.getAttribute(key);
            if (attrInst != null) {
                attrInst.setBaseValue(0.0);
                onSetSkill(key, 0.0);
            }
        }
    }

    private double getSkillValue(Attribute attribute) {
        return skillValues.getOrDefault(attribute, 0.0);
    }

    public void tryIncreaseSkill(Attribute attribute) {
        double currentSkill = getSkillValue(attribute);
        double chance = getChanceToIncreaseSkill(currentSkill);
        tryIncreaseSkill(attribute, chance);
    }

    public void tryScaledIncreaseSkill(Attribute attribute, double scale){
        double currentSkill = getSkillValue(attribute);
        double chance = getChanceToIncreaseSkill(currentSkill) * scale;
        tryIncreaseSkill(attribute, chance);
    }

    public void tryIncreaseSkill(Attribute attribute, double chance) {
        double currentSkill = getSkillValue(attribute);
        if (currentSkill < GameConstants.NATURAL_SKILL_MAX) {
            PlayerEntity player = playerData.getEntity();
            if (player.getRNG().nextDouble() <= chance) {
                skillValues.put(attribute, currentSkill + 1.0);
                player.sendMessage(new TranslationTextComponent("mkcore.skill.increase",
                        new TranslationTextComponent(attribute.getAttributeName()), currentSkill + 1.0)
                        .mergeStyle(TextFormatting.AQUA), Util.DUMMY_UUID);
                ModifiableAttributeInstance attrInst = player.getAttribute(attribute);
                if (attrInst != null) {
                    double newValue = currentSkill + 1.0;
                    attrInst.setBaseValue(newValue);
                    onSetSkill(attribute, newValue);
                }
            }
        }
    }

    private double getChanceToIncreaseSkill(double currentSkill) {
        return 1.0 / (5.0 + currentSkill);
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        CompoundNBT skillsNbt = new CompoundNBT();
        for (Object2DoubleMap.Entry<Attribute> entry : skillValues.object2DoubleEntrySet()) {
            ResourceLocation attrId = Objects.requireNonNull(entry.getKey().getRegistryName());
            skillsNbt.putDouble(attrId.toString(), entry.getDoubleValue());
        }
        tag.put("skills", skillsNbt);
        return tag;
    }

    @Override
    public boolean deserialize(CompoundNBT tag) {
        CompoundNBT skillsNbt = tag.getCompound("skills");
        for (String key : skillsNbt.keySet()) {
            Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(key));
            if (attr != null) {
                skillValues.put(attr, skillsNbt.getDouble(key));
            }
        }
        return true;
    }
}
