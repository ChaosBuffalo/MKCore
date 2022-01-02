package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.targeting_api.Targeting;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;


@Mixin(IngameGui.class)
public abstract class InGameGuiMixins extends AbstractGui {

    @Shadow @Final protected Minecraft mc;

    @Shadow protected abstract boolean isTargetNamedMenuProvider(RayTraceResult rayTraceIn);

    @Shadow protected int scaledWidth;

    @Shadow protected int scaledHeight;

    /**
     * @author kovak
     * @reason testing better cursor rendering
     */

    @Overwrite
    protected void renderCrosshair(MatrixStack matrixStack) {
        GameSettings gamesettings = this.mc.gameSettings;
        if (gamesettings.getPointOfView().func_243192_a()) {
            if (this.mc.playerController.getCurrentGameType() != GameType.SPECTATOR || this.isTargetNamedMenuProvider(this.mc.objectMouseOver)) {
                if (gamesettings.showDebugInfo && !gamesettings.hideGUI && !this.mc.player.hasReducedDebug() && !gamesettings.reducedDebugInfo) {
                    RenderSystem.pushMatrix();
                    RenderSystem.translatef((float)(this.scaledWidth / 2), (float)(this.scaledHeight / 2), (float)this.getBlitOffset());
                    ActiveRenderInfo activerenderinfo = this.mc.gameRenderer.getActiveRenderInfo();
                    RenderSystem.rotatef(activerenderinfo.getPitch(), -1.0F, 0.0F, 0.0F);
                    RenderSystem.rotatef(activerenderinfo.getYaw(), 0.0F, 1.0F, 0.0F);
                    RenderSystem.scalef(-1.0F, -1.0F, -1.0F);
                    RenderSystem.renderCrosshair(10);
                    RenderSystem.popMatrix();
                } else {
                    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    int i = 15;
                    Vector3f color = getColorForSituation();
                    RenderSystem.color4f(color.getX(), color.getY(), color.getZ(), 1.0f);
                    this.blit(matrixStack, (this.scaledWidth - 15) / 2, (this.scaledHeight - 15) / 2, 0, 0, 15, 15);
                    if (this.mc.gameSettings.attackIndicator == AttackIndicatorStatus.CROSSHAIR) {
                        float f = this.mc.player.getCooledAttackStrength(0.0F);
                        boolean shouldDrawAttackIndicator = false;
                        Optional<Entity> pointedEntity = MKCore.getPlayer(mc.player).map(x -> x.getCombatExtension().getPointedEntity()).orElse(Optional.empty());
                        if (pointedEntity.isPresent() && pointedEntity.get() instanceof LivingEntity && f >= 1.0F) {
                            shouldDrawAttackIndicator = this.mc.player.getCooldownPeriod() > 5.0F;
                            shouldDrawAttackIndicator = shouldDrawAttackIndicator & pointedEntity.get().isAlive();
                        }

                        int j = this.scaledHeight / 2 - 7 + 16;
                        int k = this.scaledWidth / 2 - 8;

                        if (shouldDrawAttackIndicator) {
                            this.blit(matrixStack, k, j, 68, 94, 16, 16);
                        } else if (f < 1.0F) {
                            int l = (int)(f * 17.0F);
                            this.blit(matrixStack, k, j, 36, 94, 16, 4);
                            this.blit(matrixStack, k, j, 52, 94, l, 4);
                        }

                    }
                    RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                }

            }
        }
    }

    private Vector3f getColorForSituation(){
        if (mc.player != null){
            return MKCore.getPlayer(mc.player).map(x -> {
                Optional<Entity> target = x.getCombatExtension().getPointedEntity();
                return target.map(ent -> {
                    Targeting.TargetRelation relation = Targeting.getTargetRelation(mc.player, ent);
                    switch (relation){
                        case FRIEND:
                            return new Vector3f(0.0f, 1.0f, 0.0f);
                        case ENEMY:
                            return new Vector3f(1.0f, 0.0f, 0.0f);
                        case NEUTRAL:
                            return new Vector3f(1.0f, 1.0f, 0.0f);
                        case UNHANDLED:
                        default:
                            return new Vector3f(1.0f, 1.0f, 1.0f);
                    }
                }).orElse(new Vector3f(1.0f, 1.0f, 1.0f));
            }).orElse(new Vector3f(1.0f, 1.0f, 1.0f));
        }
        return new Vector3f(1.0f, 1.0f, 1.0f);
    }



}
