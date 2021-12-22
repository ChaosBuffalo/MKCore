package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkwidgets.utils.ManualAtlas;

public class GuiTextures {

    public static final ManualAtlas CORE_TEXTURES = new ManualAtlas(MKCore.makeRL("textures/gui/mkcore_ui.png"),
            1024, 1024);
    public static final String MANA_REGION = "mana";
    public static final String CAST_BAR_REGION = "cast_bar";
    public static final String ABILITY_BAR_REG = "ability_bar_regular";
    public static final String ABILITY_BAR_ULT = "ability_bar_ultimate";
    public static final String ABILITY_SLOT_REG_LOCKED = "ability_slot_reg_locked";
    public static final String ABILITY_SLOT_ULT_LOCKED = "ability_slot_ult_locked";
    public static final String ABILITY_SLOT_PASSIVE_LOCKED = "ability_slot_passive_locked";
    public static final String ABILITY_SLOT_REG = "ability_slot_reg";
    public static final String ABILITY_SLOT_ULT = "ability_slot_ult";
    public static final String ABILITY_SLOT_PASSIVE = "ability_slot_passive";
    public static final String BACKGROUND_320_240 = "background_320_240";
    public static final String DATA_BOX = "data_box";
    public static final String DATA_BOX_SHORT = "data_box_short";
    public static final String BACKGROUND_180_200 = "background_180_200";
    public static final String XP_BAR_BACKGROUND = "xp_background";
    public static final String XP_BAR_BLUE = "xp_bar_blue";
    public static final String XP_BAR_YELLOW = "xp_bar_yellow";
    public static final String POISE_BAR = "poise_bar";
    public static final String POISE_BREAK = "poise_break";

    static {
        CORE_TEXTURES.addTextureRegion(MANA_REGION, 326, 51, 3, 8);
        CORE_TEXTURES.addTextureRegion(CAST_BAR_REGION, 326, 45, 50, 3);
        CORE_TEXTURES.addTextureRegion(ABILITY_BAR_ULT, 392, 0, 20, 20);
        CORE_TEXTURES.addTextureRegion(ABILITY_BAR_REG, 392, 22, 20, 20);
        CORE_TEXTURES.addTextureRegion(ABILITY_SLOT_REG, 326, 0, 20, 20);
        CORE_TEXTURES.addTextureRegion(ABILITY_SLOT_REG_LOCKED, 326, 22, 20, 20);
        CORE_TEXTURES.addTextureRegion(ABILITY_SLOT_ULT, 348, 0, 20, 20);
        CORE_TEXTURES.addTextureRegion(ABILITY_SLOT_ULT_LOCKED, 348, 22, 20, 20);
        CORE_TEXTURES.addTextureRegion(ABILITY_SLOT_PASSIVE, 370, 0, 20, 20);
        CORE_TEXTURES.addTextureRegion(ABILITY_SLOT_PASSIVE_LOCKED, 370, 22, 20, 20);
        CORE_TEXTURES.addTextureRegion(BACKGROUND_320_240, 0, 0, 320, 240);
        CORE_TEXTURES.addTextureRegion(DATA_BOX, 0, 245, 308, 155);
        CORE_TEXTURES.addTextureRegion(BACKGROUND_180_200, 310, 245, 180, 200);
        CORE_TEXTURES.addTextureRegion(XP_BAR_BACKGROUND, 326, 74, 67, 11);
        CORE_TEXTURES.addTextureRegion(XP_BAR_BLUE, 330, 89, 59, 11);
        CORE_TEXTURES.addTextureRegion(XP_BAR_YELLOW, 330, 105, 59, 11);
        CORE_TEXTURES.addTextureRegion(DATA_BOX_SHORT, 0, 404, 308, 133);
        CORE_TEXTURES.addTextureRegion(POISE_BAR, 326, 118, 50, 3);
        CORE_TEXTURES.addTextureRegion(POISE_BREAK, 326, 125, 50, 3);


    }

}
