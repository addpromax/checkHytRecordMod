package com.yshs.hytcheckrecord;

import com.yshs.hytcheckrecord.commands.CheckAllPlayerRecord;
import com.yshs.hytcheckrecord.commands.CheckRecord;
import com.yshs.hytcheckrecord.test.Test;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Example.MOD_ID)
public class Example {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "hytcheckrecord";

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        LOGGER.info("hytcheckrecord init");
        MinecraftForge.EVENT_BUS.register(new CheckAllPlayerRecord());
        MinecraftForge.EVENT_BUS.register(new CheckRecord());
        MinecraftForge.EVENT_BUS.register(new Test());
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
    }
}
