package com.qiumengzhou.tpaqmz.network;

import com.qiumengzhou.tpaqmz.data.BackData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class QuickRequestPacket {
    public QuickRequestPacket() {}
    public static void encode(QuickRequestPacket msg, FriendlyByteBuf buffer) {}
    public static QuickRequestPacket decode(FriendlyByteBuf buffer) { return new QuickRequestPacket(); }

    public static void handle(QuickRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) return;

            long currentTime = System.currentTimeMillis();
            long lastTime = BackData.QUICK_REQUEST_COOLDOWN.getOrDefault(sender.getUUID(), 0L);
            // 读取全局冷却时间
            int cooldownInSeconds = BackData.getGlobalCooldown(sender.serverLevel());
            long cooldownMillis = (long) cooldownInSeconds * 1000;

            if (currentTime - lastTime < cooldownMillis) {  // 检查 玩家发送求救 的频率
                long secondsLeft = (cooldownMillis - (currentTime - lastTime)) / 1000;
                sender.sendSystemMessage(Component.translatable("tpa.cooldown_msg", secondsLeft)
                        .withStyle(ChatFormatting.RED));
                return;
            }

            BackData.QUICK_REQUEST_COOLDOWN.put(sender.getUUID(), currentTime);

            // 构建可点击的文本：[点击救援]
            MutableComponent clickText = Component.translatable("tpa.click_to_assist")
                    .withStyle(style -> style
                            .withColor(ChatFormatting.GOLD)
                            .withBold(true)
                            .withUnderlined(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/assist " + sender.getScoreboardName()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("tpa.tip_for_clicking")))
                    );

            // 全局广播 求救信息
            Component broadcast = Component.translatable("tpa.quick_request", sender.getDisplayName())
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(" "))
                    .append(clickText);
            sender.getServer().getPlayerList().broadcastSystemMessage(broadcast, false);

            // 发送穿云箭声音
            for (ServerPlayer player : sender.getServer().getPlayerList().getPlayers()) {
                player.playNotifySound(
                        net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_LAUNCH, // 声音类型：烟花发射
                        net.minecraft.sounds.SoundSource.AMBIENT,
                        1.0F,                                           // 音量
                        1.0F                                                    // 音调
                );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
