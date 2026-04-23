package com.qiumengzhou.tpaqmz.command;

import com.qiumengzhou.tpaqmz.data.BackData;
import com.qiumengzhou.tpaqmz.data.BackPosition;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class BackCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("back")
                        .executes(context -> {
                            ServerPlayer executor = context.getSource().getPlayerOrException();
                            BackPosition pos = BackData.LAST_POS.get(executor.getUUID());
                            if (pos == null) {
                                context.getSource().sendFailure(
                                        Component.translatable("tpa.no_back_position")
                                                .withStyle(ChatFormatting.RED)
                                );
                                return 0;
                            }

                            executor.teleportTo(
                                    executor.server.getLevel(pos.dimension()),
                                    pos.x(),
                                    pos.y(),
                                    pos.z(),
                                    pos.yaw(),
                                    pos.pitch()
                            );
                            executor.sendSystemMessage(
                                    Component.translatable("tpa.back_success")
                                            .withStyle(ChatFormatting.GREEN)
                            );
                            return 1;
                        })
                        .then(Commands.literal("death") // 返回死亡位置
                                .executes(context -> {
                                    ServerPlayer executor = context.getSource().getPlayerOrException();
                                    BackPosition pos = BackData.DEATH_POS.get(executor.getUUID());
                                    if (pos == null) {
                                        context.getSource().sendFailure(
                                                Component.translatable("tpa.no_death_position")
                                                        .withStyle(ChatFormatting.RED)
                                        );
                                        return 0;
                                    }

                                    executor.teleportTo(
                                            executor.server.getLevel(pos.dimension()),
                                            pos.x(),
                                            pos.y(),
                                            pos.z(),
                                            pos.yaw(),
                                            pos.pitch()
                                    );
                                    executor.sendSystemMessage(
                                            Component.translatable("tpa.back_success")
                                                    .withStyle(ChatFormatting.GREEN)
                                    );
                                    return 1;
                                })
                        )

        );
    }
}
