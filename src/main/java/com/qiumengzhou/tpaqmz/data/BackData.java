package com.qiumengzhou.tpaqmz.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackData {
    // 记录玩家传送前的位置
    public static final Map<UUID, BackPosition> LAST_POS = new HashMap<>();
    // 记录玩家死亡的位置
    public static final Map<UUID, BackPosition> DEATH_POS = new HashMap<>();
    // 记录玩家上一次发送求救的时间
    public static final Map<UUID, Long> QUICK_REQUEST_COOLDOWN = new HashMap<>();
}
