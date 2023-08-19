package com.yshs.hytcheckrecord.message;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public class GUIMessage {
    public static void printMessage(String message) {
        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
    }

    //打印空行
    public static void printEmptyLine() {
        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(""));
    }
}
