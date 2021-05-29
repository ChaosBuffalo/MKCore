package com.chaosbuffalo.mkcore.effects.instant;


import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.effects.SpellEffectBase;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SoundEffect extends SpellEffectBase {

    public static final SoundEffect INSTANCE = new SoundEffect();


    @SubscribeEvent
    public static void register(RegistryEvent.Register<Effect> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static SpellCast Create(Entity source, SoundEvent event, float pitch, float volume,
                                   SoundCategory cat) {
        SpellCast cast = INSTANCE.newSpellCast(source);
        INSTANCE.setParameters(cast, event, pitch, volume, cat);
        return cast;
    }

    public static SpellCast Create(Entity source, SoundEvent event, SoundCategory cat){
        return Create(source, event, 1.0f, 1.0f, cat);
    }


    protected SoundEffect() {
        super(EffectType.NEUTRAL, 123);
        setRegistryName(MKCore.MOD_ID, "effect.sound_potion");
    }

    private SoundEffect setParameters(SpellCast cast, SoundEvent event, float pitch, float volume,
                                      SoundCategory cat) {

        cast.setResourceLocation("soundEvent", event.getRegistryName());
        cast.setFloat("volume", volume);
        cast.setFloat("pitch", pitch);
        cast.setInt("category", cat.ordinal());
        return this;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ALL;
    }

    @Override
    public void doEffect(Entity applier, Entity caster, LivingEntity target, int amplifier, SpellCast cast) {
        SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(cast.getResourceLocation("soundEvent"));
        if (event != null){
            SoundUtils.playSoundAtEntity(target, event, SoundCategory.values()[cast.getInt("category")],
                    cast.getFloat("volume"), cast.getFloat("pitch"));
        }
    }
}