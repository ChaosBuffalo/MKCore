package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.gui.MKOverlay;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

public abstract class MKToggleAbilityBase extends MKAbility {
    public static final ResourceLocation TOGGLE_EFFECT = MKCore.makeRL("textures/abilities/ability_toggle.png");
    private final AbilityRenderer renderer = new ToggleRenderer();

    public MKToggleAbilityBase(ResourceLocation abilityId) {
        super(abilityId);
    }

    public MKToggleAbilityBase(String namespace, String path) {
        super(namespace, path);
    }

    public ResourceLocation getToggleGroupId() {
        return getAbilityId();
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.SELF;
    }

    @Override
    public AbilityType getType() {
        return AbilityType.Basic;
    }

    @Override
    public float getManaCost(IMKEntityData entityData) {
        if (isEffectActive(entityData)) {
            return 0f;
        }
        return super.getManaCost(entityData);
    }

    @Override
    public int getCastTime(IMKEntityData entityData) {
        // Active effects can be disabled instantly
        if (isEffectActive(entityData)) {
            return 0;
        }
        return super.getCastTime(entityData);
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData entityData, AbilityContext context) {
        if (isEffectActive(entityData)) {
            removeEffect(entity, entityData);
        } else {
            applyEffect(entity, entityData);
        }
    }

    public abstract boolean isEffectActive(IMKEntityData entityData);

    public void applyEffect(LivingEntity entity, IMKEntityData entityData) {
        entityData.getAbilityExecutor().setToggleGroupAbility(getToggleGroupId(), this);
    }

    public abstract void removeEffect(LivingEntity entity, IMKEntityData entityData);

    @Override
    public AbilityRenderer getRenderer() {
        return renderer;
    }

    public class ToggleRenderer extends AbilityRenderer {
        @Override
        public void drawAbilityBarEffect(MKPlayerData playerData, MatrixStack matrixStack, Minecraft mc, int slotX, int slotY) {
            if (isEffectActive(playerData)) {
                int iconSize = MKOverlay.ABILITY_ICON_SIZE + 2;
                mc.getTextureManager().bindTexture(TOGGLE_EFFECT);

                AbstractGui.blit(matrixStack, slotX - 1, slotY - 1, 0, 0, iconSize, iconSize, iconSize, iconSize);
            }
        }
    }
}
