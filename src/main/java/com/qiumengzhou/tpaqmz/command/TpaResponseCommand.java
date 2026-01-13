package com.qiumengzhou.tpaqmz.command;

import com.qiumengzhou.tpaqmz.request.TpaRequest;
import com.qiumengzhou.tpaqmz.request.TpaRequestManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class TpaResponseCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // 在指令树挂载 接受请求 拒绝请求 指令
        dispatcher.register(
                Commands.literal("tpa_accept")
                        .then(Commands.argument("id", StringArgumentType.string())
                                .executes(context -> {
                                    handle(context, true);
                                    return 1;
                                }))
        );
        dispatcher.register(
                Commands.literal("tpa_deny")
                        .then(Commands.argument("id", StringArgumentType.string())
                                .executes(context -> {
                                    handle(context, false);
                                    return 1;
                                }))
        );
    }

    private static void handle(CommandContext<CommandSourceStack> context, boolean accept) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("tpa.illegal_execution").withStyle(ChatFormatting.RED));
            return;
        }
        // 获得 请求的id，检查该请求是否失效
        UUID id = UUID.fromString(StringArgumentType.getString(context, "id"));
        TpaRequest req = TpaRequestManager.REQUESTS.get(id);
        if (req == null) {
            player.sendSystemMessage(
                    Component.translatable("tpa.request_invalidated").withStyle(ChatFormatting.RED)
            );
            return;
        }

        // 如果 该指令调用者 不在 请求的代确认名单 中 中止
        if (!req.waitConfirm.contains(player.getUUID())) return;

        if (!accept) {
            broadcastFail(req, player);
            TpaRequestManager.remove(req);
            return;
        }

        req.accepted.add(player.getUUID());

        // 如果所有 待确定人员 都接受，则进行传送
        if (req.isAllAccepted()) {
            TpaCommand.teleport(req.from, req.to);
            TpaRequestManager.remove(req);
        }
    }

    private static void broadcastFail(TpaRequest req, ServerPlayer player) {
        Component msg = Component.translatable(
                "tpa.request_failure",
                player.getDisplayName().copy().withStyle(ChatFormatting.RED)
        ).withStyle(ChatFormatting.RED);
        req.executor.sendSystemMessage(msg);
        if(!(req.from.getUUID().equals(req.executor.getUUID()))) {
            req.from.sendSystemMessage(msg);
        }
        if(!(req.to.getUUID().equals(req.executor.getUUID()))) {
            req.to.sendSystemMessage(msg);
        }
    }
}
