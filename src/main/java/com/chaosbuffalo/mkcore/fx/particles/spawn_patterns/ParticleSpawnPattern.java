package com.chaosbuffalo.mkcore.fx.particles.spawn_patterns;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.serialization.IDynamicMapTypedSerializer;
import com.chaosbuffalo.mkcore.serialization.ISerializableAttributeContainer;
import com.chaosbuffalo.mkcore.serialization.attributes.ISerializableAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.Entity;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ParticleSpawnPattern implements ISerializableAttributeContainer, IDynamicMapTypedSerializer {
    public static class ParticleSpawnEntry {
        public Vector3d position;
        public Vector3d motion;
        public MKParticleData particleData;

        public ParticleSpawnEntry(MKParticleData particleData, Vector3d position, Vector3d motion){
            this.position = position;
            this.motion = motion;
            this.particleData = particleData;
        }

    }

    private static final String TYPE_NAME_FIELD = "type";
    public final static ResourceLocation INVALID_OPTION = new ResourceLocation(MKCore.MOD_ID, "particle_spawn_pattern.invalid");
    protected final List<ISerializableAttribute<?>> attributes;
    private final ResourceLocation type;
    protected final IntAttribute count = new IntAttribute("count", 10);


    public ParticleSpawnPattern(ResourceLocation type){
        this.type = type;
        this.attributes = new ArrayList<>();
        addAttributes(count);
    }

    public abstract ParticleSpawnPattern copy();

    public ITextComponent getDescription(){
        return new TranslationTextComponent(String.format("%s.spawn_pattern.%s.name", type.getNamespace(), type.getPath()));
    }

    @Override
    public List<ISerializableAttribute<?>> getAttributes() {
        return attributes;
    }

    @Override
    public void addAttribute(ISerializableAttribute<?> attribute) {
        attributes.add(attribute);
    }

    @Override
    public void addAttributes(ISerializableAttribute<?>... attributes) {
        this.attributes.addAll(Arrays.asList(attributes));
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        builder.put(ops.createString("attributes"), serializeAttributeMap(ops));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        deserializeAttributeMap(dynamic, "attributes");
    }

    @Override
    public ResourceLocation getTypeName() {
        return type;
    }

    @Override
    public String getTypeEntryName() {
        return TYPE_NAME_FIELD;
    }

    public static <D> ResourceLocation getType(Dynamic<D> dynamic) {
        return IDynamicMapTypedSerializer.getType(dynamic, TYPE_NAME_FIELD).orElse(INVALID_OPTION);
    }

//    public abstract Tuple<Vector3d, Vector3d> getParticleStart(Vector3d position, int particleNumber, @Nullable List<Vector3d> additionalLocs, World world);

//    public void spawn(ParticleType<MKParticleData> particleType,
//                      Vector3d position, World world, ParticleAnimation anim, @Nullable List<Vector3d> additionalLocs){
//        MKParticleData particleData = new MKParticleData(particleType, position, anim);
//        for (int i = 0; i < count.value(); i++) {
//            Tuple<Vector3d, Vector3d> posAndMotion = getParticleStart(position, i, additionalLocs, world);
//            Vector3d pos = posAndMotion.getA();
//            Vector3d mot = posAndMotion.getB();
//            spawnParticle(world, particleData, pos, mot);
//        }
//    }

//    protected void spawnParticle(World world, ParticleSpawnEntry entry){
//        world.addOptionalParticle(entry.particleData, true,
//                entry.position.getX(), entry.position.getY(), entry.position.getZ(),
//                entry.motion.getX(), entry.motion.getY(), entry.motion.getZ());
//    }
//
//    protected void spawnParticle(World world, MKParticleData particleData, Vector3d pos, Vector3d motion){
//        world.addOptionalParticle(particleData, true, pos.getX(), pos.getY(), pos.getZ(),
//                motion.getX(), motion.getY(), motion.getZ());
//    }
//
//    public void spawnOffsetFromEntity(ParticleType<MKParticleData> particleType,
//                                      Vector3d offset, World world,
//                                      ParticleAnimation anim, Entity entity, List<Vector3d> additionalLocs){
//        MKParticleData particleData = new MKParticleData(particleType, offset, anim, entity.getEntityId());
//        Vector3d position = offset.add(entity.getPositionVec());
//        for (int i = 0; i < count.value(); i++) {
//            Tuple<Vector3d, Vector3d> posAndMotion = getParticleStart(position, i, additionalLocs, world);
//            Vector3d pos = posAndMotion.getA();
//            Vector3d mot = posAndMotion.getB();
//            spawnParticle(world, particleData, pos, mot);
//        }
//    }

    public void spawn(ParticleType<MKParticleData> particleType,
                      Vector3d position, World world, ParticleAnimation anim, @Nullable List<Vector3d> additionalLocs){
        List<ParticleSpawnEntry> finalParticles = new ArrayList<>();
        for (int i = 0; i < count.value(); i++) {
            produceParticlesForIndex(position, i, additionalLocs, world,
                    (pos) -> new MKParticleData(particleType, pos, anim),
                    finalParticles);
        }
        for (ParticleSpawnEntry entry : finalParticles){
            spawnParticle(world, entry);
        }
    }

    public abstract void produceParticlesForIndex(Vector3d origin, int particleNumber,
                                                  @Nullable List<Vector3d> additionalLocs, World world,
                                                  Function<Vector3d, MKParticleData> particleDataSupplier,
                                                  List<ParticleSpawnEntry> finalParticles);

    protected void spawnParticle(World world, ParticleSpawnEntry entry){
        world.addOptionalParticle(entry.particleData, true,
                entry.position.getX(), entry.position.getY(), entry.position.getZ(),
                entry.motion.getX(), entry.motion.getY(), entry.motion.getZ());
    }


    public void spawnOffsetFromEntity(ParticleType<MKParticleData> particleType,
                                      Vector3d offset, World world,
                                      ParticleAnimation anim, Entity entity, List<Vector3d> additionalLocs){
        Vector3d position = offset.add(entity.getPositionVec());
        List<Vector3d> finalLocs = additionalLocs.stream().map(
                x -> x.add(entity.getPositionVec())).collect(Collectors.toList());
        List<ParticleSpawnEntry> finalParticles = new ArrayList<>();
        for (int i = 0; i < count.value(); i++) {
            produceParticlesForIndex(position, i, finalLocs, world,
                    (pos) -> new MKParticleData(particleType, offset, anim, entity.getEntityId()),
                    finalParticles);
        }
        for (ParticleSpawnEntry entry : finalParticles){
            spawnParticle(world, entry);
        }
    }
}
