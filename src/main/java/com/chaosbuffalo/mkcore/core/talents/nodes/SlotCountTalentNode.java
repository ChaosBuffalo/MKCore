package com.chaosbuffalo.mkcore.core.talents.nodes;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.talents.TalentNode;
import com.chaosbuffalo.mkcore.core.talents.talent_types.SlotCountTalent;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

import java.util.Optional;
import java.util.UUID;

public class SlotCountTalentNode extends TalentNode {

    private final UUID nodeId;

    public SlotCountTalentNode(SlotCountTalent talent, Dynamic<?> entry) {
        super(talent, entry);
        this.nodeId = entry.get("nodeId").asString().map(UUID::fromString).result().orElse(UUID.randomUUID());
    }

    public SlotCountTalentNode(SlotCountTalent talent, UUID nodeId){
        super(talent);
        this.nodeId = nodeId;
    }

    @Override
    public SlotCountTalent getTalent() {
        return (SlotCountTalent) super.getTalent();
    }

    public UUID getNodeId() {
        return nodeId;
    }

    public <T> T serialize(DynamicOps<T> ops) {
        T value = super.serialize(ops);
        Optional<T> merged = ops.mergeToMap(value, ops.createString("nodeId"), ops.createString(nodeId.toString())).resultOrPartial(MKCore.LOGGER::error);
        return merged.orElse(value);
    }


}
