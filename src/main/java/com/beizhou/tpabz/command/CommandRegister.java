package com.beizhou.tpabz.command;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber     // 标记为 Forge 事件总线监听类
public class CommandRegister {
    @SubscribeEvent         // 监听服务器指令注册事件
    public static void registerCommands(RegisterCommandsEvent event) {
        // 在服务器初始化指令系统时，由 Forge 调用
        TpaCommand.register(event.getDispatcher());
        BackCommand.register(event.getDispatcher());
    }
}
