package com.chaosbuffalo.mkcore.fx.particles.effect_instances;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCBone;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCSkeleton;
import com.chaosbuffalo.mkcore.serialization.attributes.StringAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

import java.util.UUID;

public class BoneEffectInstance extends ParticleEffectInstance{
    public static final ResourceLocation TYPE = MKCore.makeRL("effect_instance.bone");
    protected final StringAttribute boneName = new StringAttribute("boneName", BipedSkeleton.ROOT_BONE_NAME);

    public BoneEffectInstance() {
        super(TYPE);
        addAttribute(boneName);
    }

    public BoneEffectInstance(UUID instanceUUID, String boneName, ResourceLocation particleName) {
        super(TYPE, instanceUUID);
        addAttribute(this.boneName);
        this.boneName.setValue(boneName);
        this.particleAnimName.setValue(particleName);
    }

    @Override
    public void update(Entity entity, MCSkeleton skeleton, float partialTicks, Vector3d offset) {
        if (entity instanceof LivingEntity){
            MCBone.getPositionOfBoneInWorld((LivingEntity) entity, skeleton,
                    partialTicks, offset, boneName.getValue()).ifPresent(x ->
                    getAnimation().ifPresent(anim -> anim.spawn(entity.getEntityWorld(), x, null)));
        }
    }
}
