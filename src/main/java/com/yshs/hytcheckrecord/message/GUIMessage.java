package com.yshs.hytcheckrecord.message;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class GUIMessage {
    public static void printMessage(TextFormatting color, String message) {
        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(color + message));
    }
}
