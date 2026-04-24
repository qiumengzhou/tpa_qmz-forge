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
import net.minecraft.server.level.ServerLevel;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;

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
                                            ServerPlayer executor = context.getSource().getPlayerOrException();
                                            ServerPlayer from = EntityArgument.getPlayer(context, "playername1");
                                            ServerPlayer to = EntityArgument.getPlayer(context, "playername2");
                                            // 是否为危险传送 是否进行拦截
                                            if (!executor.getUUID().equals(from.getUUID())) {
                                                if (!BackData.isDangerTpAllowed(executor.serverLevel())) {
                                                    context.getSource().sendFailure(Component.translatable("tpa.danger_tp")
                                                            .withStyle(ChatFormatting.RED));
                                                    return 0;
                                                }
                                            }
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
        dispatcher.register(    // 挂载设置指令
                Commands.literal("tpaConfig")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("setAssist")     // 设置 救援功能的冷却
                                .then(Commands.argument("seconds", IntegerArgumentType.integer(10)) // 限制最小值为 10 秒
                                        .executes(context -> {
                                            int seconds = IntegerArgumentType.getInteger(context, "seconds");
                                            ServerLevel level = context.getSource().getLevel();
                                            BackData.setGlobalCooldown(level, seconds);
                                            context.getSource().sendSuccess(() ->
                                                    Component.translatable(
                                                            "tpa.set_cooldown",
                                                            seconds
                                                    ).withStyle(ChatFormatting.GREEN), true);
                                            return 1;
                                        })))
                        .then(Commands.literal("dangerTp")   // 设置 是否禁用 危险行为
                                .then(Commands.argument("allowed", BoolArgumentType.bool())
                                        .executes(context -> {
                                            boolean allowed = BoolArgumentType.getBool(context, "allowed");
                                            BackData.setDangerTeleportAllowed(context.getSource().getLevel(), allowed);

                                            String status = allowed ? "true" : "false";
                                            context.getSource().sendSuccess(() -> Component.translatable("tpa.danger_tp_status", status)
                                                    .withStyle(ChatFormatting.YELLOW), true);
                                            return 1;
                                        })))
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

        // 执行传送
        teleport(from, to, isQuickAssist);
    }

    public static void teleport(ServerPlayer from, ServerPlayer to, boolean isQuickAssist) {
        // 记录当前位置，用于回退
        BackData.setLastPos(from.serverLevel(), from.getUUID(),
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