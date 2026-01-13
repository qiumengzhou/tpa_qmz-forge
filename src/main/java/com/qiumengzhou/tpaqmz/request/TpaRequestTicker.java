package com.qiumengzhou.tpaqmz.request;

import net.minecraft.ChatFormatting;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.network.chat.Component;

import java.util.Iterator;

@Mod.EventBusSubscriber
public class TpaRequestTicker {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        long now = System.currentTimeMillis();

        // 获取 请求map 的游标
        Iterator<TpaRequest> it = TpaRequestManager.REQUESTS.values().iterator();
        while (it.hasNext()) {
            TpaRequest req = it.next();
            if (now - req.createTime >= TpaRequestManager.TIMEOUT_MS) {
                req.executor.sendSystemMessage(Component.translatable("tpa.request_timeout").withStyle(ChatFormatting.RED));
                it.remove();    // 删除 next() 返回的请求
            }
        }
    }
}
