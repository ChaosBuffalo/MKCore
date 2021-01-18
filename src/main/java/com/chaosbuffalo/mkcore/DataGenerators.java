package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.init.ModTags;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.block.Block;
import net.minecraft.data.*;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        if (event.includeServer()) {
            MKBlockTagsProvider blockTagsProvider = new MKBlockTagsProvider(generator);
            generator.addProvider(blockTagsProvider);
            generator.addProvider(new AbilityDataGenerator(generator));
            generator.addProvider(new ArmorClassItemTagProvider(generator, blockTagsProvider, event.getExistingFileHelper()));
        }
    }

    static class AbilityDataGenerator implements IDataProvider {
        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        private final DataGenerator generator;

        public AbilityDataGenerator(DataGenerator generator) {
            this.generator = generator;
        }

        @Override
        public void act(@Nonnull DirectoryCache cache) {
            Path outputFolder = this.generator.getOutputFolder();
            MKCoreRegistry.ABILITIES.forEach(ability -> {
                ResourceLocation key = ability.getAbilityId();
                MKCore.LOGGER.info("Dumping ability {}", key);
                if (!key.getPath().startsWith("ability.")) {
                    MKCore.LOGGER.warn("Skipping {} because it did not have the 'ability.' prefix", key);
                    return;
                }
                String name = key.getPath().substring(8); // skip ability.
                Path path = outputFolder.resolve("data/" + key.getNamespace() + "/player_abilities/" + name + ".json");
                try {
                    JsonElement element = ability.serializeDynamic(JsonOps.INSTANCE);
                    IDataProvider.save(GSON, cache, element, path);
                } catch (IOException e) {
                    MKCore.LOGGER.error("Couldn't write ability {}", path, e);
                }
            });
        }

        @Nonnull
        @Override
        public String getName() {
            return "MKCore Abilities";
        }
    }

    static class MKBlockTagsProvider extends BlockTagsProvider {

        public MKBlockTagsProvider(DataGenerator generatorIn) {
            super(generatorIn);
        }

        @Override
        protected void registerTags() {
        }

        private TagsProvider.Builder<Block> tag(ITag.INamedTag<Block> tag) {
            return this.getOrCreateBuilder(tag);
        }
    }

    public static class ArmorClassItemTagProvider extends ItemTagsProvider {
        public ArmorClassItemTagProvider(DataGenerator dataGenerator, BlockTagsProvider blockTagProvider, ExistingFileHelper existingFileHelper) {
            super(dataGenerator, blockTagProvider, MKCore.MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerTags() {
            tag(ModTags.Items.LIGHT_ARMOR).add(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
            tag(ModTags.Items.MEDIUM_ARMOR).add(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
            tag(ModTags.Items.HEAVY_ARMOR).add(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
            tag(ModTags.Items.ARMOR).addTag(ModTags.Items.LIGHT_ARMOR).addTag(ModTags.Items.MEDIUM_ARMOR).addTag(ModTags.Items.HEAVY_ARMOR);
        }

        private TagsProvider.Builder<Item> tag(ITag.INamedTag<Item> tag) {
            return this.getOrCreateBuilder(tag);
        }

        @Nonnull
        @Override
        public String getName() {
            return "MKCore armor class item tags";
        }
    }
}
