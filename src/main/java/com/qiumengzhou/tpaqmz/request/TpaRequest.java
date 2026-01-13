
package com.qiumengzhou.tpaqmz.request;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TpaRequest {

    public final UUID requestId = UUID.randomUUID();

    public final ServerPlayer executor;
    public final ServerPlayer from;
    public final ServerPlayer to;

    // 声明 waitConfirm 和 accepted 集合
    // set集合，不关注排序，自动去重
    public final Set<UUID> waitConfirm = new HashSet<>();
    public final Set<UUID> accepted = new HashSet<>();

    public final long createTime;

    public TpaRequest(ServerPlayer executor, ServerPlayer from, ServerPlayer to) {
        this.executor = executor;
        this.from = from;
        this.to = to;
        this.createTime = System.currentTimeMillis();
        // 添加需要确认的对象 waitConfirm
        if (!executor.getUUID().equals(from.getUUID())) {
            waitConfirm.add(from.getUUID());
        }
        if (!executor.getUUID().equals(to.getUUID())) {
            waitConfirm.add(to.getUUID());
        }
    }

    public boolean isAllAccepted() {
        // 检查是否所有 waitConfirm 都包含在 accepted中
        // 即是否所有人都同意请求
        return accepted.containsAll(waitConfirm);
    }
}
