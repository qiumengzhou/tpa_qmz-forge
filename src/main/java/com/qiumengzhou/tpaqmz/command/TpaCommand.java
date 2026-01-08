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
        // 在指令树挂载tpa指令
        dispatcher.register(
                Commands.literal("tpa")   // 定义指令表示符 tpa
                        // 定义指令语法 /tpa name
                        .then(Commands.argument("playername", EntityArgument.player())
                                // 定义指令执行
                                .executes(context -> {  // context 指令执行时的上下文
                                    ServerPlayer from = context.getSource().getPlayerOrException();
                                    ServerPlayer to = EntityArgument.getPlayer(context, "playername");
                                    teleport(from, to, context.getSource());
                                    return 1;
                                }))
                        // /tpa name1 name2
                        .then(Commands.argument("playername1", EntityArgument.player())
                                .then(Commands.argument("playername2", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer p1 = EntityArgument.getPlayer(context, "playername1");
                                            ServerPlayer p2 = EntityArgument.getPlayer(context, "playername2");
                                            teleport(p1, p2, context.getSource());
                                            return 1;
                                        })))
        );
    }

    private static void teleport(ServerPlayer from, ServerPlayer to, CommandSourceStack source) {
        if (from.getUUID().equals(to.getUUID())) {
            source.sendFailure(
                    Component.translatable("tpa.cannot_teleport")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }
        else{
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
}
