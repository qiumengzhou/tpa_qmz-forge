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

public class TpaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // 在指令树挂载 tpa 指令
        dispatcher.register(
                Commands.literal("tpa")
                        .then(Commands.argument("playername", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer from = context.getSource().getPlayerOrException();
                                    ServerPlayer to = EntityArgument.getPlayer(context, "playername");
                                    teleportDirect(context.getSource(), from, to);
                                    return 1;
                                }))
                        .then(Commands.argument("playername1", EntityArgument.player())
                                .then(Commands.argument("playername2", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer from = EntityArgument.getPlayer(context, "playername1");
                                            ServerPlayer to = EntityArgument.getPlayer(context, "playername2");
                                            teleportDirect(context.getSource(), from, to);
                                            return 1;
                                        })))
        );
    }

    // 直接传送，不再创建请求
    private static void teleportDirect(CommandSourceStack source, ServerPlayer from, ServerPlayer to) {
        // 判断执行者是否为玩家
        if (!(source.getEntity() instanceof ServerPlayer executor)) {
            source.sendFailure(Component.translatable("tpa.illegal_execution").withStyle(ChatFormatting.RED));
            return;
        }

        // 防止传送到自己
        if (from.getUUID().equals(to.getUUID())) {
            source.sendFailure(Component.translatable("tpa.cannot_teleport").withStyle(ChatFormatting.RED));
            return;
        }

        // 直接执行传送
        teleport(from, to);
    }

    public static void teleport(ServerPlayer from, ServerPlayer to) {
        // 记录当前位置，用于回退
        BackData.LAST_POS.put(from.getUUID(),
                new BackPosition(
                        from.level().dimension(),
                        from.getX(),
                        from.getY(),
                        from.getZ(),
                        from.getYRot(),
                        from.getXRot()
                ));

        // 执行传送
        from.teleportTo(
                to.serverLevel(),
                to.getX(),
                to.getY(),
                to.getZ(),
                to.getYRot(),
                to.getXRot()
        );

        // 广播传送成功消息给所有在线玩家
        Component msg = Component.translatable(
                "tpa.teleport_success",
                from.getDisplayName().copy().withStyle(ChatFormatting.GREEN),
                to.getDisplayName().copy().withStyle(ChatFormatting.GREEN)
        ).withStyle(ChatFormatting.GREEN);

        from.getServer().getPlayerList().getPlayers()
                .forEach(player -> player.sendSystemMessage(msg));
    }
}