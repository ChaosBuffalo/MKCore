package com.chaosbuffalo.mkcore.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;

public class ChatUtils {

    private static void sendPlayerChatMessage(PlayerEntity playerEntity, ITextComponent message, boolean brackets) {
        if (brackets)
            message = TextComponentUtils.wrapWithSquareBrackets(message);
        playerEntity.sendStatusMessage(message, false);
    }

    public static void sendMessageWithBrackets(PlayerEntity playerEntity, String format, Object... args) {
        String message = String.format(format, args);
        sendMessageWithBrackets(playerEntity, message);
    }

    public static void sendMessageWithBrackets(PlayerEntity playerEntity, String message) {
        sendMessageWithBrackets(playerEntity, new StringTextComponent(message));
    }

    public static void sendMessageWithBrackets(PlayerEntity playerEntity, ITextComponent message) {
        sendPlayerChatMessage(playerEntity, message, true);
    }

    public static void sendMessage(PlayerEntity playerEntity, String format, Object... args) {
        String message = String.format(format, args);
        sendMessage(playerEntity, message);
    }

    public static void sendMessage(PlayerEntity playerEntity, String format) {
        sendMessage(playerEntity, new StringTextComponent(format));
    }

    public static void sendMessage(PlayerEntity playerEntity, ITextComponent message) {
        sendPlayerChatMessage(playerEntity, message, false);
    }
}
