package com.chaosbuffalo.mkcore.effects.status;

import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.effects.PassiveEffect;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class MKResistance extends PassiveEffect {
    private final float perLevel;


    public MKResistance(ResourceLocation loc, Attribute attribute, UUID attrId, int liquidColorIn, float perLevel) {
        super(perLevel > 0.0f ? EffectType.BENEFICIAL : EffectType.HARMFUL, liquidColorIn);
        setRegistryName(loc);
        this.perLevel = perLevel;
        addAttributesModifier(attribute, attrId.toString(), perLevel, AttributeModifier.Operation.ADDITION);
    }

    public float getPerLevel(){
        return perLevel;
    }
}