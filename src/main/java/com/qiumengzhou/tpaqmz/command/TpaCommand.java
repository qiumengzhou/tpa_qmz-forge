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
        dispatcher.register(    // 在指令树挂载 tpa 指令
                Commands.literal("tpa")
                        .then(Commands.argument("playername", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer from = context.getSource().getPlayerOrException();
                                    ServerPlayer to = EntityArgument.getPlayer(context, "playername");
                                    teleportDirect(context.getSource(), from, to, false);
                                    return 1;
                                }))
                        .then(Commands.argument("playername1", EntityArgument.player())
                                .then(Commands.argument("playername2", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer from = EntityArgument.getPlayer(context, "playername1");
                                            ServerPlayer to = EntityArgument.getPlayer(context, "playername2");
                                            teleportDirect(context.getSource(), from, to, false);
                                            return 1;
                                        })))
        );
        dispatcher.register(    // 挂载救援指令
                Commands.literal("assist")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer helper = context.getSource().getPlayerOrException();
                                    ServerPlayer needy = EntityArgument.getPlayer(context, "target");
                                    teleportDirect(context.getSource(), helper, needy, true);
                                    return 1;
                                }))
        );
    }

    // 直接传送
    private static void teleportDirect(CommandSourceStack source, ServerPlayer from, ServerPlayer to, boolean isQuickAssist) {
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
        teleport(from, to, isQuickAssist);
    }

    public static void teleport(ServerPlayer from, ServerPlayer to, boolean isQuickAssist) {
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

        // 发送 提示信息
        if (isQuickAssist) {
            // 救援模式
            to.sendSystemMessage(Component.translatable("tpa.quick_request_assist", from.getDisplayName())
                    .withStyle(ChatFormatting.AQUA));
        } else {
            // 普通模式
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