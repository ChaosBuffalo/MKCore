package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TalentLineDefinition {
    private final TalentTreeDefinition tree;
    private final String name;
    private final List<TalentNode> nodes;

    public TalentLineDefinition(TalentTreeDefinition tree, String name) {
        this.tree = tree;
        this.name = name;
        nodes = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public TalentTreeDefinition getTree() {
        return tree;
    }

    public int getLength() {
        return nodes.size();
    }

    public TalentNode getNode(int index) {
        if (index < nodes.size()) {
            return nodes.get(index);
        }
        return null;
    }

    private void addNode(TalentNode node) {
        node.link(this, nodes.size());
        nodes.add(node);
    }

    public List<TalentNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public static <T> TalentLineDefinition deserialize(TalentTreeDefinition tree, Dynamic<T> dynamic) {
        Optional<String> nameOpt = dynamic.get("name").asString()
                .resultOrPartial(error -> MKCore.LOGGER.error("Failed to deserialize talent line: {}", error));
        if (!nameOpt.isPresent())
            return null;

        TalentLineDefinition line = new TalentLineDefinition(tree, nameOpt.get());
        for (Optional<TalentNode> node : dynamic.get("talents")
                .asListOpt(line::deserializeNode)
                .resultOrPartial(error -> MKCore.LOGGER.error("Failed to deserialize talent line {}: {}", nameOpt.get(), error))
                .orElse(Collections.emptyList())) {
            if (node.isPresent()) {
                line.addNode(node.get());
            } else {
                MKCore.LOGGER.error("Stopping parsing talent line {} at index {} because it failed to deserialize", line.getName(), line.getNodes().size());
                break;
            }
        }

        return line;
    }

    private <T> Optional<TalentNode> deserializeNode(Dynamic<T> entry) {
        Optional<String> nameOpt = entry.get("name").asString()
                .resultOrPartial(error -> MKCore.LOGGER.error("Failed to deserialize talent node: {}", error));
        if (!nameOpt.isPresent()) {
            MKCore.LOGGER.error("Tried to deserialize talent without a name!");
            return Optional.empty();
        }

        ResourceLocation nodeType = new ResourceLocation(nameOpt.get());
        MKTalent talentType = MKCoreRegistry.TALENTS.getValue(nodeType);
        if (talentType == null) {
            MKCore.LOGGER.error("Tried to deserialize talent node that referenced unknown talent type {}", nodeType);
            return Optional.empty();
//                throw new IllegalArgumentException(String.format("Tried to deserialize talent that referenced unknown talent type %s", nodeType));
        }

        return Optional.of(talentType.createNode(entry));
    }

    public <T> T serialize(DynamicOps<T> ops) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(ops.createString("name"), ops.createString(name));
        builder.put(ops.createString("talents"), ops.createList(nodes.stream().map(n -> n.serialize(ops))));
        return ops.createMap(builder.build());
    }
}
