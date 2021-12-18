package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.talents.handlers.EntitlementTalentTypeHandler;
import com.chaosbuffalo.mkcore.core.talents.handlers.UltimateTalentHandler;
import com.chaosbuffalo.mkcore.core.talents.handlers.AttributeTalentHandler;
import com.chaosbuffalo.mkcore.core.talents.handlers.PassiveTalentHandler;
import com.chaosbuffalo.mkcore.init.CoreEntitlements;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Function;

public class TalentType<T extends TalentTypeHandler> {
    public static TalentType<AttributeTalentHandler> ATTRIBUTE =
            new TalentType<AttributeTalentHandler>("mkcore.talent_type.attribute.name", AttributeTalentHandler::new)
                    .setDisplayName("mkcore.talent_type.tooltip_name");
    public static TalentType<PassiveTalentHandler> PASSIVE =
            new TalentType<>("mkcore.talent_type.passive.name", PassiveTalentHandler::new)
                    .setDisplayName("mkcore.talent_type.tooltip_name_with_ability");
    public static TalentType<UltimateTalentHandler> ULTIMATE =
            new TalentType<>("mkcore.talent_type.ultimate.name", UltimateTalentHandler::new)
                    .setDisplayName("mkcore.talent_type.tooltip_name_with_ability");
    public static TalentType<EntitlementTalentTypeHandler> BASIC_SLOT =
            new TalentType<>("mkcore.talent_type.basic_slot.name",
                    (mkPlayerData) -> new EntitlementTalentTypeHandler(mkPlayerData, CoreEntitlements.BasicAbilitySlotCount))
                    .setDisplayName("mkcore.talent_type.tooltip_name");
    public static TalentType<EntitlementTalentTypeHandler> PASSIVE_SLOT =
            new TalentType<>("mkcore.talent_type.passive_slot.name",
                    (mkPlayerData) -> new EntitlementTalentTypeHandler(mkPlayerData, CoreEntitlements.PassiveAbilitySlotCount))
                    .setDisplayName("mkcore.talent_type.tooltip_name");
    public static TalentType<EntitlementTalentTypeHandler> ULTIMATE_SLOT =
            new TalentType<>("mkcore.talent_type.ultimate_slot.name",
                    (mkPlayerData) -> new EntitlementTalentTypeHandler(mkPlayerData, CoreEntitlements.UltimateAbilitySlotCount))
                    .setDisplayName("mkcore.talent_type.tooltip_name");
    public static TalentType<EntitlementTalentTypeHandler> POOL_COUNT =
            new TalentType<>("mkcore.talent_type.pool_slot.name",
                    (mkPlayerData) -> new EntitlementTalentTypeHandler(mkPlayerData, CoreEntitlements.AbilityPoolCount))
                    .setDisplayName("mkcore.talent_type.tooltip_name");

    private final String name;
    private String displayNameKey = "mkcore.talent_type.tooltip_name";
    private final Function<MKPlayerData, T> factory;

    private TalentType(String name, Function<MKPlayerData, T> factory) {
        this.name = name;
        this.factory = factory;
    }

    public TalentType<T> setDisplayName(String tooltipKey) {
        this.displayNameKey = tooltipKey;
        return this;
    }

    public TranslationTextComponent getName() {
        return new TranslationTextComponent(name);
    }

    public TranslationTextComponent getDisplayName() {
        return new TranslationTextComponent(displayNameKey, getName());
    }

    public T createTypeHandler(MKPlayerData playerData) {
        return factory.apply(playerData);
    }
}
