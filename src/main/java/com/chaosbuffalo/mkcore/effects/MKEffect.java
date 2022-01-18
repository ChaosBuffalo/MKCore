package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

public abstract class MKEffect extends ForgeRegistryEntry<MKEffect> {
    private final Map<Attribute, MKAttributeModifierEntry> attributeModifierMap = new HashMap<>();

    public static class MKAttributeModifierEntry {

        public AttributeModifier modifier;
        public double base;
        @Nullable
        public Attribute skill;
        public boolean skillInvert;


        public MKAttributeModifierEntry(Supplier<String> nameProvider, UUID uuid, double base, double amount,
                                        AttributeModifier.Operation operation, Attribute skill, boolean skillInvert){
            modifier = new AttributeModifier(uuid, nameProvider, amount, operation);
            this.base = base;
            this.skill = skill;
            this.skillInvert = skillInvert;
        }
    }

    @Nullable
    protected String name;
    protected final Lazy<Effect> wrapperEffect = Lazy.of(() -> new WrapperEffect(this));
    protected final EffectType effectType;

    public MKEffect(EffectType effectType) {
        this.effectType = effectType;
    }

    @Nonnull
    public ResourceLocation getId() {
        return Objects.requireNonNull(getRegistryName());
    }

    protected String getOrCreateDescriptionId() {
        if (name == null) {
            name = Util.makeTranslationKey("mk_effect", MKCoreRegistry.EFFECTS.getKey(this));
        }
        return name;
    }

    public String getName() {
        return getOrCreateDescriptionId();
    }

    public ITextComponent getDisplayName() {
        return new TranslationTextComponent(getName());
    }

    public boolean isValidTarget(TargetingContext targetContext, IMKEntityData sourceData, IMKEntityData targetData) {
        return Targeting.isValidTarget(targetContext, sourceData.getEntity(), targetData.getEntity());
    }

    // Effect was added while the entity was in the world
    public void onInstanceAdded(IMKEntityData targetData, MKActiveEffect newInstance) {
        if (hasAttributes()) {
            applyAttributesModifiers(targetData, newInstance);
        }
    }

    // Effect was updated while the entity was in the world
    public void onInstanceUpdated(IMKEntityData targetData, MKActiveEffect activeEffect) {
        if (hasAttributes()) {
            removeAttributesModifiers(targetData);
            applyAttributesModifiers(targetData, activeEffect);
        }
    }

    // Effect was removed while the entity was in the world
    public void onInstanceRemoved(IMKEntityData targetData, MKActiveEffect expiredEffect) {
        if (hasAttributes()) {
            removeAttributesModifiers(targetData);
        }
    }

    // Entity not yet in world when this is called. Called during deserialization from NBT
    public void onInstanceLoaded(IMKEntityData targetData, MKActiveEffect activeInstance) {
        MKCore.LOGGER.debug("MKEffect.onInstanceLoaded {}", activeInstance);
    }

    // Entity is about to be added to the world, but has NOT been added to the UUID map
    // Do not attempt to locate other entities here
    public void onInstanceReady(IMKEntityData targetData, MKActiveEffect activeInstance) {
        MKCore.LOGGER.debug("MKEffect.onInstanceReady {}", activeInstance);
    }

    public MKEffectBuilder<?> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    public MKEffectBuilder<?> builder(LivingEntity sourceEntity) {
        return new MKEffectBuilder<>(this, sourceEntity, this::makeState);
    }

    public abstract MKEffectState makeState();

    public MKActiveEffect createInstance(UUID sourceId) {
        return new MKActiveEffect(this, sourceId);
    }

    public Map<Attribute, MKAttributeModifierEntry> getAttributeModifierMap() {
        return attributeModifierMap;
    }

    private boolean hasAttributes() {
        return attributeModifierMap.size() > 0;
    }

    public MKEffect addAttribute(Attribute attribute, UUID uuid, double amount, AttributeModifier.Operation operation) {
        return this.addAttribute(attribute, uuid, amount, amount, operation, null, false);
    }

    public MKEffect addAttribute(Attribute attribute, UUID uuid, double base, double amount,
                                 AttributeModifier.Operation operation, Attribute skill, boolean invertSkill) {
        attributeModifierMap.put(attribute, new MKAttributeModifierEntry(this::getName, uuid, base, amount, operation,
                skill, invertSkill));
        return this;
    }

    protected void removeAttributesModifiers(IMKEntityData targetData) {
        AttributeModifierManager manager = targetData.getEntity().getAttributeManager();
        for (Map.Entry<Attribute, MKAttributeModifierEntry> entry : getAttributeModifierMap().entrySet()) {
            ModifiableAttributeInstance attrInstance = manager.createInstanceIfAbsent(entry.getKey());
            if (attrInstance != null) {
                attrInstance.removeModifier(entry.getValue().modifier);
            }
        }
    }

    protected void applyAttributesModifiers(IMKEntityData targetData, MKActiveEffect activeEffect) {
        AttributeModifierManager manager = targetData.getEntity().getAttributeManager();
        for (Map.Entry<Attribute, MKAttributeModifierEntry> entry : getAttributeModifierMap().entrySet()) {
            ModifiableAttributeInstance attrInstance = manager.createInstanceIfAbsent(entry.getKey());
            if (attrInstance != null) {
                MKAttributeModifierEntry template = entry.getValue();
                attrInstance.removeModifier(template.modifier);
                attrInstance.applyPersistentModifier(createModifier(template, activeEffect));
            }
        }
    }

    private AttributeModifier createModifier(MKAttributeModifierEntry template, MKActiveEffect activeEffect) {
        int stacks = activeEffect.getStackCount();


        double amount = calculateModifierAmount(template, activeEffect);
        return new AttributeModifier(template.modifier.getID(), getName() + " " + stacks, amount, template.modifier.getOperation());
    }

    public static double calculateModifierDesc(MKAttributeModifierEntry modifier, int stackCount, LivingEntity caster,
                                               float skillLevel){
        float skill = 0;
        if (caster != null){
            if (modifier.skill != null){
                skill = MKAbility.getSkillLevel(caster, modifier.skill);
            } else {
                skill = skillLevel;
            }
            if (modifier.skillInvert){
                skill = 10.0f - skill;
            }
        }
        return modifier.base + (modifier.modifier.getAmount() * stackCount * skill);
    }

    protected double calculateModifierAmount(MKAttributeModifierEntry modifier, MKActiveEffect activeEffect) {
        return calculateModifierDesc(modifier, activeEffect.getStackCount(), activeEffect.getSourceEntity(), activeEffect.getSkillLevel());
    }

    /**
     * If the effect should be displayed in the players inventory
     * @param effect the active MKEffect
     * @return true to display it (default), false to hide it.
     */
    public boolean shouldRender(MKActiveEffect effect) { return true; }

    /**
     * If the standard text (name and duration) should be drawn when this potion is active.
     * @param effect the active MKEffect
     * @return true to draw the standard text
     */
    public boolean shouldRenderInvText(MKActiveEffect effect) { return true; }

    /**
     * If the effect should be displayed in the player's ingame HUD
     * @param effect the active MKEffect
     * @return true to display it (default), false to hide it.
     */
    public boolean shouldRenderHUD(MKActiveEffect effect) { return true; }

    @Override
    public String toString() {
        return "MKEffect{" + getId() + "}";
    }

    // Keep this package-private so no one calls it by accident
    Effect getVanillaWrapper() {
        return wrapperEffect.get();
    }

    public static class WrapperEffect extends Effect {

        private final MKEffect effect;

        protected WrapperEffect(MKEffect effect) {
            super(effect.effectType, 0);
            this.effect = effect;
        }

        public MKEffect getMKEffect() {
            return effect;
        }

        @Nonnull
        @Override
        public String getName() {
            return effect.getName();
        }

        @Nonnull
        @Override
        public ITextComponent getDisplayName() {
            return effect.getDisplayName();
        }

        @Override
        public List<ItemStack> getCurativeItems() {
            return Collections.emptyList();
        }
    }
}
