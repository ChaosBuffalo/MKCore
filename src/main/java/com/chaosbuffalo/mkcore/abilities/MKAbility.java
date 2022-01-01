package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.AbilityUseCondition;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.StandardUseCondition;
import com.chaosbuffalo.mkcore.core.*;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.entities.BaseProjectileEntity;
import com.chaosbuffalo.mkcore.init.CoreSounds;
import com.chaosbuffalo.mkcore.serialization.attributes.ISerializableAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mkcore.utils.TargetUtil;
import com.chaosbuffalo.mkcore.utils.text.IconTextComponent;
import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class MKAbility extends ForgeRegistryEntry<MKAbility> {

    private int castTime;
    private int cooldown;
    private float manaCost;
    private final List<ISerializableAttribute<?>> attributes;
    private AbilityUseCondition useCondition;
    private final Set<Attribute> skillAttributes;
    private static final ResourceLocation EMPTY_PARTICLES = new ResourceLocation(MKCore.MOD_ID, "fx.casting.empty");
    protected final ResourceLocationAttribute casting_particles = new ResourceLocationAttribute("casting_particles", EMPTY_PARTICLES);
    public static final ResourceLocation POOL_SLOT_ICON = new ResourceLocation(MKCore.MOD_ID, "textures/talents/pool_count_icon_filled.png");


    public MKAbility(String domain, String id) {
        this(new ResourceLocation(domain, id));
    }

    public MKAbility(ResourceLocation abilityId) {
        setRegistryName(abilityId);
        this.cooldown = GameConstants.TICKS_PER_SECOND;
        this.castTime = 0;
        this.manaCost = 1;
        this.attributes = new ArrayList<>();
        this.skillAttributes = new HashSet<>();
        setUseCondition(new StandardUseCondition(this));
        addAttribute(casting_particles);
    }

    public boolean hasCastingParticles() {
        return casting_particles.getValue().compareTo(EMPTY_PARTICLES) != 0;
    }

    public ResourceLocation getCastingParticles() {
        return casting_particles.getValue();
    }

    public ITextComponent getDamageDescription(IMKEntityData entityData, MKDamageType damageType, float damage,
                                               float scale, int level, float modifierScaling) {
        float bonus = entityData.getStats().getDamageTypeBonus(damageType) * modifierScaling;
        float abilityDamage = damage + (scale * level) + bonus;
        IFormattableTextComponent damageStr = StringTextComponent.EMPTY.deepCopy();
        damageStr.appendSibling(new StringTextComponent(String.format("%.1f", abilityDamage)).mergeStyle(TextFormatting.BOLD));
        if (bonus != 0) {
            damageStr.appendSibling(new StringTextComponent(String.format(" (+%.1f)", bonus)).mergeStyle(TextFormatting.BOLD));
        }
        damageStr.appendString(" ").appendSibling(damageType.getDisplayName().mergeStyle(damageType.getFormatting()));
        return damageStr;
    }


    protected IFormattableTextComponent formatEffectValue(float damage, float levelScale, int level, float bonus, float scaleMod) {
        float value = damage + (levelScale * level) + (bonus * scaleMod);
        IFormattableTextComponent damageStr = StringTextComponent.EMPTY.deepCopy();
        damageStr.appendSibling(new StringTextComponent(String.format("%.1f", value)).mergeStyle(TextFormatting.BOLD));
        if (bonus != 0) {
            damageStr.appendSibling(new StringTextComponent(String.format(" (+%.1f)", bonus)).mergeStyle(TextFormatting.BOLD));
        }
        return damageStr;
    }

    public ITextComponent getHealDescription(IMKEntityData entityData, float value,
                                             float scale, int level, float modifierScaling) {
        float bonus = entityData.getStats().getHealBonus();
        return formatEffectValue(value, scale, level, bonus, modifierScaling).mergeStyle(TextFormatting.GREEN);
    }

    protected ITextComponent formatManaValue(IMKEntityData entityData, float value, float scale, int level,
                                             float bonus, float modifierScaling) {
        return formatEffectValue(value, scale, level, bonus, modifierScaling).mergeStyle(TextFormatting.BLUE);
    }

    protected ITextComponent getSkillDescription(IMKEntityData entityData) {
        ITextComponent skillList = TextComponentUtils.func_240649_b_(getSkillAttributes(),
                attr -> new TranslationTextComponent(attr.getAttributeName()));
        return new TranslationTextComponent("mkcore.ability.description.skill", skillList);
    }

    public void buildDescription(IMKEntityData entityData, Consumer<ITextComponent> consumer) {
        if (entityData instanceof MKPlayerData) {
            MKPlayerData playerData = (MKPlayerData) entityData;
            MKAbilityInfo info = playerData.getAbilities().getKnownAbility(getAbilityId());
            if (info != null && info.getSource().usesAbilityPool()) {
                consumer.accept(new IconTextComponent(POOL_SLOT_ICON, "mkcore.ability.description.uses_pool").mergeStyle(TextFormatting.ITALIC));
            }
        }
        if (!skillAttributes.isEmpty()) {
            consumer.accept(getSkillDescription(entityData));
        }
        consumer.accept(getManaCostDescription(entityData));
        consumer.accept(getCooldownDescription(entityData));
        consumer.accept(getCastTimeDescription(entityData));
        getTargetSelector().buildDescription(this, entityData, consumer);
        consumer.accept(getAbilityDescription(entityData));
    }

    protected ITextComponent getCooldownDescription(IMKEntityData entityData) {
        float seconds = (float) entityData.getStats().getAbilityCooldown(this) / GameConstants.TICKS_PER_SECOND;
        return new TranslationTextComponent("mkcore.ability.description.cooldown", seconds);
    }

    protected ITextComponent getCastTimeDescription(IMKEntityData entityData) {
        int castTicks = entityData.getStats().getAbilityCastTime(this);
        float seconds = (float) castTicks / GameConstants.TICKS_PER_SECOND;
        ITextComponent time = castTicks > 0 ?
                new TranslationTextComponent("mkcore.ability.description.seconds", seconds) :
                new TranslationTextComponent("mkcore.ability.description.instant");
        return new TranslationTextComponent("mkcore.ability.description.cast_time", time);
    }

    protected ITextComponent getManaCostDescription(IMKEntityData entityData) {
        return new TranslationTextComponent("mkcore.ability.description.mana_cost", getManaCost(entityData));
    }

    protected ITextComponent getAbilityDescription(IMKEntityData entityData) {
        return new TranslationTextComponent(getDescriptionTranslationKey());
    }

    public void setUseCondition(AbilityUseCondition useCondition) {
        this.useCondition = useCondition;
    }


    public List<ISerializableAttribute<?>> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    public AbilityUseCondition getUseCondition() {
        return useCondition;
    }

    public MKAbility addAttribute(ISerializableAttribute<?> attr) {
        attributes.add(attr);
        return this;
    }

    public MKAbility addAttributes(ISerializableAttribute<?>... attrs) {
        attributes.addAll(Arrays.asList(attrs));
        return this;
    }

    public ResourceLocation getAbilityId() {
        return getRegistryName();
    }

    public MKAbilityInfo createAbilityInfo(AbilitySource source) {
        return new MKAbilityInfo(this, source);
    }

    public ITextComponent getAbilityName() {
        return new TranslationTextComponent(getTranslationKey());
    }

    protected String getTranslationKey() {
        ResourceLocation abilityId = getAbilityId();
        return String.format("%s.%s.name", abilityId.getNamespace(), abilityId.getPath());
    }

    protected String getDescriptionTranslationKey() {
        ResourceLocation abilityId = getAbilityId();
        return String.format("%s.%s.description", abilityId.getNamespace(), abilityId.getPath());
    }

    public ResourceLocation getAbilityIcon() {
        ResourceLocation abilityId = getAbilityId();
        return new ResourceLocation(abilityId.getNamespace(), String.format("textures/abilities/%s.png", abilityId.getPath().split(Pattern.quote("."))[1]));
    }

    public AbilityRenderer getRenderer() {
        return AbilityRenderer.INSTANCE;
    }

    protected int getBaseCastTime() {
        return castTime;
    }

    public int getCastTime(IMKEntityData entityData) {
        return getBaseCastTime();
    }

    protected void setCastTime(int castTicks) {
        castTime = castTicks;
    }

    public boolean canApplyCastingSpeedModifier() {
        return true;
    }

    public float getDistance(LivingEntity entity) {
        return 1.0f;
    }

    protected float getMeleeReach(LivingEntity entity) {
        return (float) entity.getAttribute(MKAttributes.ATTACK_REACH).getValue();
    }

    protected void setCooldownTicks(int ticks) {
        this.cooldown = ticks;
    }

    protected void setCooldownSeconds(int seconds) {
        this.cooldown = seconds * GameConstants.TICKS_PER_SECOND;
    }

    protected int getBaseCooldown() {
        return cooldown;
    }

    public int getCooldown(IMKEntityData entityData) {
        return getBaseCooldown();
    }

    public AbilityType getType() {
        return AbilityType.Basic;
    }

    public abstract TargetingContext getTargetContext();

    public boolean isValidTarget(LivingEntity caster, LivingEntity target) {
        return Targeting.isValidTarget(getTargetContext(), caster, target);
    }

    protected float getBaseManaCost() {
        return manaCost;
    }

    public float getManaCost(IMKEntityData entityData) {
        return getBaseManaCost() + getManaCostModifierForSkills(entityData);
    }

    protected float getManaCostModifierForSkills(IMKEntityData entityData) {
        float total = 0.0f;
        for (Attribute attribute : getSkillAttributes()) {
            ModifiableAttributeInstance attr = entityData.getEntity().getAttribute(attribute);
            if (attr != null) {
                total += attr.getValue();
            }
        }
        return total;
    }

    protected void setManaCost(float cost) {
        manaCost = cost;
    }

    public boolean meetsRequirements(IMKEntityData entityData) {
        return entityData.getAbilityExecutor().canActivateAbility(this) &&
                entityData.getStats().canActivateAbility(this);
    }

    public <T> T serializeDynamic(DynamicOps<T> ops) {
        return ops.createMap(
                ImmutableMap.of(
                        ops.createString("cooldown"), ops.createInt(getBaseCooldown()),
                        ops.createString("manaCost"), ops.createFloat(getBaseManaCost()),
                        ops.createString("castTime"), ops.createInt(getBaseCastTime()),
                        ops.createString("attributes"),
                        ops.createMap(attributes.stream().map(attr ->
                                Pair.of(ops.createString(attr.getName()), attr.serialize(ops))
                        ).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)))
                )
        );
    }

    public <T> void deserializeDynamic(Dynamic<T> dynamic) {
        MKCore.LOGGER.debug("ability deserialize {}", dynamic.getValue());
        setCooldownTicks(dynamic.get("cooldown").asInt(getBaseCooldown()));
        setManaCost(dynamic.get("manaCost").asFloat(getBaseManaCost()));
        setCastTime(dynamic.get("castTime").asInt(getBaseCastTime()));

        Map<String, Dynamic<T>> map = dynamic.get("attributes").asMap(d -> d.asString(""), Function.identity());
        getAttributes().forEach(attr -> {
            Dynamic<T> attrValue = map.get(attr.getName());
            if (attrValue != null) {
                attr.deserialize(attrValue);
            }
        });
    }

    @Nullable
    public SoundEvent getCastingSoundEvent() {
        return CoreSounds.casting_default;
    }

    @Nullable
    public SoundEvent getSpellCompleteSoundEvent() {
        return CoreSounds.spell_cast_default;
    }

    public void executeWithContext(IMKEntityData entityData, AbilityContext context, MKAbilityInfo abilityInfo) {
        entityData.getAbilityExecutor().startAbility(context, abilityInfo);
    }

    public ITextComponent getTargetContextLocalization() {
        return new TranslationTextComponent("mkcore.ability_description.target_type",
                getTargetContext().getLocalizedDescription());
    }

    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.NONE;
    }

    public Set<MemoryModuleType<?>> getRequiredMemories() {
        return getTargetSelector().getRequiredMemories();
    }

    public boolean isExecutableContext(AbilityContext context) {
        return getRequiredMemories().stream().allMatch(context::hasMemory);
    }

    public void continueCast(LivingEntity entity, IMKEntityData data, int castTimeLeft, AbilityContext context) {
    }

    public void continueCastClient(LivingEntity entity, IMKEntityData data, int castTimeLeft) {
    }

    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {
    }

    public boolean isInterruptible() {
        return true;
    }

    @Deprecated
    public List<LivingEntity> getTargetsInLine(LivingEntity caster, Vector3d from, Vector3d to, boolean checkValid, float growth) {
        return TargetUtil.getTargetsInLine(caster, from, to, growth, checkValid ? this::isValidTarget : null);
    }

    protected void shootProjectile(BaseProjectileEntity projectileEntity, float velocity, float accuracy,
                                   LivingEntity entity, AbilityContext context) {
        Vector3d startPos = entity.getPositionVec().add(new Vector3d(0, entity.getEyeHeight(), 0));
        startPos.add(Vector3d.fromPitchYaw(entity.getPitchYaw()).mul(.5, 0.0, .5));
        projectileEntity.setPosition(startPos.x, startPos.y, startPos.z);
        if (entity instanceof PlayerEntity) {
            projectileEntity.shoot(entity, entity.rotationPitch, entity.rotationYaw, 0, velocity, accuracy);
        } else {
            context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity ->
                    EntityUtils.shootProjectileAtTarget(projectileEntity, targetEntity, velocity, accuracy));
        }
    }

    public static int getSkillLevel(LivingEntity entity, Attribute skillAttribute) {
        if (skillAttribute == null) {
            return 0;
        }
        ModifiableAttributeInstance skill = entity.getAttribute(skillAttribute);
        return skill != null ? (int) Math.round(skill.getValue()) : 0;
    }

    protected MKAbility addSkillAttribute(Attribute attribute) {
        this.skillAttributes.add(attribute);
        return this;
    }

    public Set<Attribute> getSkillAttributes() {
        return Collections.unmodifiableSet(skillAttributes);
    }

    protected int getBuffDuration(IMKEntityData entityData, int level, int base, int scale) {
        int duration = (base + scale * level) * GameConstants.TICKS_PER_SECOND;
        return MKCombatFormulas.applyBuffDurationModifier(entityData, duration);
    }
}
