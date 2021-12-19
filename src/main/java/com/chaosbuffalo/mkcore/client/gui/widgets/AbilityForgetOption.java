package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerLearnAbilityRequestPacket;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;

public class AbilityForgetOption extends MKLayout {

    private final ResourceLocation loc;
    private final ForgetAbilityModal popup;
    private final MKAbility ability;
    private final int trainerEntityId;

    public AbilityForgetOption(MKAbility ability,
                               ResourceLocation loc, ForgetAbilityModal popup,
                               FontRenderer font, int trainerEntity) {
        super(0, 0, 200, 16);
        this.loc = loc;
        this.popup = popup;
        this.ability = ability;
        this.trainerEntityId = trainerEntity;
        IconText iconText = new IconText(0, 0, 16, ability.getAbilityName(), ability.getAbilityIcon(), font, 16, 1);
        this.addWidget(iconText);
        addConstraintToWidget(MarginConstraint.TOP, iconText);
        addConstraintToWidget(MarginConstraint.LEFT, iconText);
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        if (popup.isForgetting(ability)){
            popup.cancelForget(ability);
        } else {
            popup.forget(ability);
        }
        return true;
    }

    public MKAbility getAbility() {
        return ability;
    }

    @Override
    public void postDraw(MatrixStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        boolean isForgetting = popup.isForgetting(ability);
        boolean hovered = isHovered();
        if (hovered || isForgetting) {
            int color = isForgetting ? 0x77ff8800 : 0x55ffffff;
            if (hovered && isForgetting){
                color = 0xaaff8800;
            }
            mkFill(matrixStack, x, y, x + width, y + height, color);
        }
    }
}
