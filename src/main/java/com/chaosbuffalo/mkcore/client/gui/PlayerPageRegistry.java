package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.InterModComms;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class PlayerPageRegistry {

    public interface ExtensionProvider extends Supplier<Extension> {
    }

    public interface Extension {
        ResourceLocation getId();

        ITextComponent getDisplayName();

        MKScreen createPage(MKPlayerData playerData);
    }

    private static final List<Extension> extensions = new ArrayList<>(5);

    private static void registerIMC(InterModComms.IMCMessage m) {
        PlayerPageRegistry.ExtensionProvider factory = m.<PlayerPageRegistry.ExtensionProvider>getMessageSupplier().get();
        Extension extension = factory.get();
        MKCore.LOGGER.info("Found IMC player page extension: {}", extension.getId());
        addExtension(extension);
    }

    private static void addExtension(Extension extension) {
        extensions.add(extension);
    }

    private static void registerInternal(ResourceLocation name, Function<MKPlayerData, MKScreen> factory) {
        addExtension(new Extension() {
            @Override
            public ResourceLocation getId() {
                return name;
            }

            @Override
            public ITextComponent getDisplayName() {
                return new TranslationTextComponent(String.format("mkcore.gui.character.%s", getId().getPath()));
            }

            @Override
            public MKScreen createPage(MKPlayerData playerData) {
                return factory.apply(playerData);
            }
        });
    }

    public static void init() {
        registerInternal(MKCore.makeRL("abilities"), PersonalAbilityPage::new);
        registerInternal(MKCore.makeRL("talents"), TalentPage::new);
        registerInternal(MKCore.makeRL("stats"), StatsPage::new);
        registerInternal(MKCore.makeRL("damages"), DamagePage::new);
    }

    @Nullable
    public static MKScreen createPage(MKPlayerData playerData, ResourceLocation name) {
        return extensions.stream()
                .filter(e -> e.getId().equals(name))
                .findFirst()
                .map(e -> e.createPage(playerData))
                .orElse(null);
    }

    public static List<Extension> getAllPages() {
        return extensions;
    }

    public static void openPlayerScreen(MKPlayerData playerData, ResourceLocation name) {
        MKScreen screen = createPage(playerData, name);
        Minecraft.getInstance().displayGuiScreen(screen);
    }

    public static void openDefaultPlayerScreen(MKPlayerData playerData) {
        openPlayerScreen(playerData, MKCore.makeRL("abilities"));
    }

    public static void checkClientIMC() {
        InterModComms.getMessages(MKCore.MOD_ID, m -> m.equals(MKCore.REGISTER_PLAYER_PAGE))
                .forEach(PlayerPageRegistry::registerIMC);
    }
}
