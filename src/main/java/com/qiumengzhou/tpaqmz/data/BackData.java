package com.qiumengzhou.tpaqmz.data;

import net.minecraft.server.level.ServerLevel;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackData {
    // 记录 玩家发起救援请求 的冷却
    public static final Map<UUID, Long> QUICK_REQUEST_COOLDOWN = new HashMap<>();
    // 获取全局冷却（秒）
    public static int getGlobalCooldown(ServerLevel level) {
        return ModSavedData.get(level).globalCooldown;
    }
    // 设置全局冷却
    public static void setGlobalCooldown(ServerLevel level, int seconds) {
        ModSavedData data = ModSavedData.get(level);
        data.globalCooldown = seconds;
        data.setDirty();    // 脏数据标签，提示存档数据需要更新
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
