package com.chaosbuffalo.mkcore.core.editor;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.IPlayerSyncComponentProvider;
import com.chaosbuffalo.mkcore.core.player.SyncComponent;
import net.minecraft.nbt.CompoundNBT;

public class PlayerEditorModule implements IPlayerSyncComponentProvider {
    private final SyncComponent sync = new SyncComponent("editor");
    private final ParticleEditorSyncComponent particleEditorData = new ParticleEditorSyncComponent("particle_editor");
    protected final MKPlayerData playerData;

    public PlayerEditorModule(MKPlayerData playerData){
        this.playerData = playerData;
        addSyncPrivate(particleEditorData);
    }

    @Override
    public SyncComponent getSyncComponent() {
        return sync;
    }

    public ParticleEditorSyncComponent getParticleEditorData() {
        return particleEditorData;
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        CompoundNBT particlesTag = new CompoundNBT();
        particleEditorData.serializeFull(particlesTag);
        tag.put("particleEditor", particlesTag);
        return tag;
    }

    public void deserialize(CompoundNBT nbt) {
        if (nbt.contains("particleEditor")){
            CompoundNBT particlesTag = nbt.getCompound("particleEditor");
            particleEditorData.deserializeUpdate(particlesTag);
            particleEditorData.markDirty();
        }
    }
}
