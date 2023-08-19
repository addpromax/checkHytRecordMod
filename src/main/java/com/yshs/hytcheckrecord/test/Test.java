package com.yshs.hytcheckrecord.test;

import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Test {
    //private static final Logger LOGGER = LogManager.getLogger();

    //入口
    @SubscribeEvent
    public void execute(ClientChatEvent event) {

        //Minecraft mc = Minecraft.getMinecraft();
        //String message = event.getMessage();
        //if (!message.contains("/red")) {
        //    return;
        //}
        //event.setCanceled(true);
        //mc.player.sendChatMessage("red message");
    }
}
