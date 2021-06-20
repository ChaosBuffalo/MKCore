package com.chaosbuffalo.mkcore.fx.particles;

import com.chaosbuffalo.mkcore.MKCore;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.stream.Stream;


public class MKParticleData implements IParticleData {

    protected final Vector3d origin;
    protected final ParticleAnimation animation;

    private final ParticleType<MKParticleData> particleType;

    public static PrimitiveCodec<MKParticleData> typeCodec(ParticleType<MKParticleData> type) {
        return new PrimitiveCodec<MKParticleData>() {


            @Override
            public <T> DataResult<MKParticleData> read(DynamicOps<T> ops, T input) {
                Dynamic<T> d = new Dynamic<>(ops, input);
                List<Double> vecD = d.get("origin").asList(x -> x.asDouble(0.0));
                Vector3d origin = new Vector3d(0.0, 0.0, 0.0);
                if (vecD.size() == 3){
                    origin = new Vector3d(vecD.get(0), vecD.get(1), vecD.get(2));
                } else {
                    MKCore.LOGGER.warn("Failed to read origin from MKParticleData {}", input);
                }
                ParticleAnimation newAnim = d.get("animation").map(x -> {
                    ParticleAnimation anim = new ParticleAnimation();
                    anim.deserialize(x);
                    return anim;
                }).result().orElse(new ParticleAnimation());
                return DataResult.success(new MKParticleData(type, origin, newAnim));
            }

            @Override
            public <T> T write(DynamicOps<T> ops, MKParticleData value) {
                ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
                builder.put(ops.createString("origin"),
                        ops.createList(Stream.of(ops.createDouble(value.origin.getX()),
                                ops.createDouble(value.origin.getY()), ops.createDouble(value.origin.getZ()))));
                builder.put(ops.createString("animation"), value.animation.serialize(ops));
                return ops.createMap(builder.build());
            }
        };

    }

    public static final IParticleData.IDeserializer<MKParticleData> DESERIALIZER = new IParticleData.IDeserializer<MKParticleData>() {
        public MKParticleData deserialize(ParticleType<MKParticleData> particleTypeIn, StringReader reader) throws CommandSyntaxException {
            // todo make this read json nbt
            return new MKParticleData(particleTypeIn, new Vector3d(reader.readDouble(), reader.readDouble(), reader.readDouble()),
                    new ParticleAnimation());
        }

        public MKParticleData read(ParticleType<MKParticleData> particleTypeIn, PacketBuffer buffer) {
            Vector3d origin = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            Dynamic<?> dynamic = new Dynamic<>(NBTDynamicOps.INSTANCE, buffer.readCompoundTag());
            ParticleAnimation newAnim = dynamic.into(d -> {
                ParticleAnimation anim = new ParticleAnimation();
                anim.deserialize(d);
                return anim;
            });
            return new MKParticleData(particleTypeIn, origin, newAnim);
        }
    };

    public MKParticleData(ParticleType<MKParticleData> typeIn, Vector3d origin, ParticleAnimation animation){
        this.particleType = typeIn;
        this.origin = origin;
        this.animation = animation;
    }


    @Override
    public ParticleType<MKParticleData> getType() {
        return particleType;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeDouble(origin.x);
        buffer.writeDouble(origin.y);
        buffer.writeDouble(origin.z);
        INBT dyn = animation.serialize(NBTDynamicOps.INSTANCE);
        if (dyn instanceof CompoundNBT) {
            buffer.writeCompoundTag((CompoundNBT) dyn);
        } else {
            throw new RuntimeException(String.format("Particle Animation %s did not serialize to a CompoundNBT!", getType().getRegistryName().toString()));
        }
    }

    @Override
    public String getParameters() {
        return ForgeRegistries.PARTICLE_TYPES.getKey(this.getType()) + " " + origin.toString();
    }

}
