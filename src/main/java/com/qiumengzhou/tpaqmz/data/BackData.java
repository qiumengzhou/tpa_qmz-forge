package com.qiumengzhou.tpaqmz.data;

import net.minecraft.server.level.ServerLevel;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackData {
    // 记录 玩家发起救援请求 发起时间
    public static final Map<UUID, Long> CLICK_REQUEST_TIME = new HashMap<>();

    // 记录 救援请求冷却（秒）
    public static int getGlobalCooldown(ServerLevel level) {
        return ModSavedData.get(level).globalCooldown;
    }
    public static void setGlobalCooldown(ServerLevel level, int seconds) {
        ModSavedData data = ModSavedData.get(level);
        data.globalCooldown = seconds;
        data.setDirty();    // 脏数据标签，提示存档数据需要更新
    }

    // 记录 救援请求有效期（分钟）
    public static int getAssistTimeout(ServerLevel level) {
        return ModSavedData.get(level).assistTimeout;
    }

    public static void setAssistTimeout(ServerLevel level, int minutes) {
        ModSavedData data = ModSavedData.get(level);
        data.assistTimeout = minutes;
        data.setDirty();
    }

    // 记录 危险传送 是否允许
    public static boolean isDangerTpAllowed(ServerLevel level) {
        return ModSavedData.get(level).allowDangerTp;
    }
    public static void setDangerTeleportAllowed(ServerLevel level, boolean allowed) {
        ModSavedData data = ModSavedData.get(level);
        data.allowDangerTp = allowed;
        data.setDirty();
    }

    // 记录 玩家传送可返回位置 玩家死亡位置
    public static void setLastPos(ServerLevel level, UUID uuid, BackPosition pos) {
        ModSavedData data = ModSavedData.get(level);
        data.lastPos.put(uuid, pos);
        data.setDirty();
    }
    public static BackPosition getLastPos(ServerLevel level, UUID uuid) {
        return ModSavedData.get(level).lastPos.get(uuid);
    }
    public static void setDeathPos(ServerLevel level, UUID uuid, BackPosition pos) {
        ModSavedData data = ModSavedData.get(level);
        data.deathPos.put(uuid, pos);
        data.setDirty();
    }
    public static BackPosition getDeathPos(ServerLevel level, UUID uuid) {
        return ModSavedData.get(level).deathPos.get(uuid);
    }
}
