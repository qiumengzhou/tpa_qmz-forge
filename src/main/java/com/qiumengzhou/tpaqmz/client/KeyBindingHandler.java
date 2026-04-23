package com.qiumengzhou.tpaqmz.client;

import com.qiumengzhou.tpaqmz.TpaQMZMod;
import com.qiumengzhou.tpaqmz.network.PacketHandler;
import com.qiumengzhou.tpaqmz.network.QuickRequestPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class KeyBindingHandler {
    public static final KeyMapping QUICK_TPA_KEY = new KeyMapping(
            "key.tpaqmz.quick_request",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            "key.categories.tpaqmz"
    );

    @Mod.EventBusSubscriber(modid = TpaQMZMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            // 将按键注册在游戏按键菜单
            event.register(QUICK_TPA_KEY);
        }
    }

    @Mod.EventBusSubscriber(modid = TpaQMZMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                while (QUICK_TPA_KEY.consumeClick()) {
                    PacketHandler.CHANNEL.sendToServer(new QuickRequestPacket());
                }
            }
        }
    }
}
