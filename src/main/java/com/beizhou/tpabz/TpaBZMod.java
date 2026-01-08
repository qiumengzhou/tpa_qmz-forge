package com.beizhou.tpabz;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;

@Mod(TpaBZMod.MOD_ID) // 注册模组
public class TpaBZMod {
    public static final String MOD_ID = "tpa_bz";
    public static final Logger logger = LogManager.getLogger(TpaBZMod.MOD_ID);

    public TpaBZMod() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        logger.log(Level.INFO, "tpa_bz Initializing client...");
    }

    @SubscribeEvent
    public static void onServerAboutToStart(FMLDedicatedServerSetupEvent event) {
        logger.log(Level.INFO, "tpa_bz starting...");
    }
}