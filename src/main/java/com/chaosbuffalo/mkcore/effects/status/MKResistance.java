package com.chaosbuffalo.mkcore.effects.status;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.effects.MKSimplePassiveState;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class MKResistance extends MKEffect {
    private final float perLevel;

    public MKResistance(ResourceLocation loc, Attribute attribute, UUID attrId, float perLevel) {
        super(perLevel > 0.0f ? EffectType.BENEFICIAL : EffectType.HARMFUL);
        setRegistryName(loc);
        this.perLevel = perLevel;
        addAttribute(attribute, attrId, perLevel, perLevel, AttributeModifier.Operation.ADDITION, MKAttributes.ABJURATION, false);
    }

    public float getPerLevel(){
        return perLevel;
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }
}
