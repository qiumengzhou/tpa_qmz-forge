package com.qiumengzhou.tpaqmz.command;

import com.qiumengzhou.tpaqmz.data.BackData;
import com.qiumengzhou.tpaqmz.data.BackPosition;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.qiumengzhou.tpaqmz.request.TpaRequest;
import com.qiumengzhou.tpaqmz.request.TpaRequestManager;
import net.minecraft.network.chat.ClickEvent;


public class TpaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // 在指令树挂载 tpa 指令
        dispatcher.register(
                Commands.literal("tpa")   // 定义指令表示符 tpa
                        // 定义指令语法 /tpa name
                        .then(Commands.argument("playername", EntityArgument.player())
                                // 定义指令执行
                                .executes(context -> {  // context 指令执行时的上下文
                                    ServerPlayer from = context.getSource().getPlayerOrException();
                                    ServerPlayer to = EntityArgument.getPlayer(context, "playername");
                                    createRequest(context.getSource(), from, to);
                                    return 1;
                                }))
                        // /tpa name1 name2
                        .then(Commands.argument("playername1", EntityArgument.player())
                                .then(Commands.argument("playername2", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer from = EntityArgument.getPlayer(context, "playername1");
                                            ServerPlayer to = EntityArgument.getPlayer(context, "playername2");
                                            createRequest(context.getSource(), from, to);
                                            return 1;
                                        })))
        );
    }

    private static void createRequest(CommandSourceStack source, ServerPlayer from, ServerPlayer to) {
        // 判断执行者 是否为 玩家
        if (!(source.getEntity() instanceof ServerPlayer executor)) {
            source.sendFailure(Component.translatable("tpa.illegal_execution").withStyle(ChatFormatting.RED));
            return;
        }
        // 判断 是否存在自己传自己的情况
        if (from.getUUID().equals(to.getUUID())) {
            source.sendFailure(Component.translatable("tpa.cannot_teleport").withStyle(ChatFormatting.RED));
            return;
        }
        else{
            // 创建 传送请求， 并添加到 请求管理
            TpaRequest request = new TpaRequest(executor, from, to);
            TpaRequestManager.add(request);

            // 遍历所有 需要确认的玩家，向他们发送请求
            request.waitConfirm.forEach(uuid -> {
                ServerPlayer player = executor.getServer().getPlayerList().getPlayer(uuid);

                if (player == null) return;

                Component msg = Component.translatable(
                        "tpa.request_send",
                        executor.getDisplayName(),
                        from.getDisplayName(),
                        to.getDisplayName()
                )
                .append(
                        Component.translatable("tpa.request_accept")
                                .withStyle(style -> style
                                        .withColor(ChatFormatting.GREEN)
                                        .withClickEvent(new ClickEvent(
                                                ClickEvent.Action.RUN_COMMAND,
                                                "/tpa_accept " + request.requestId
                                        ))))
                .append(Component.literal(" "))
                .append(
                        Component.translatable("tpa.request_deny")
                                .withStyle(style -> style
                                        .withColor(ChatFormatting.RED)
                                        .withClickEvent(new ClickEvent(
                                                ClickEvent.Action.RUN_COMMAND,
                                                        "/tpa_deny " + request.requestId
                                        ))));

                player.sendSystemMessage(msg);
            });
        }
    }

    public static void teleport(ServerPlayer from, ServerPlayer to) {
        BackData.LAST_POS.put(from.getUUID(),
                new BackPosition(
                        from.level().dimension(),
                        from.getX(),
                        from.getY(),
                        from.getZ(),
                        from.getYRot(),
                        from.getXRot()
                ));

        from.teleportTo(
                to.serverLevel(),
                to.getX(),
                to.getY(),
                to.getZ(),
                to.getYRot(),
                to.getXRot()
        );

        // 给所有在线玩家广播消息
        Component msg = Component.translatable(
                "tpa.teleport_success",
                from.getDisplayName().copy().withStyle(ChatFormatting.GREEN),
                to.getDisplayName().copy().withStyle(ChatFormatting.GREEN)
        ).withStyle(ChatFormatting.GREEN);
        from.getServer().getPlayerList().getPlayers()
                .forEach(player -> player.sendSystemMessage(msg));
    }
}
