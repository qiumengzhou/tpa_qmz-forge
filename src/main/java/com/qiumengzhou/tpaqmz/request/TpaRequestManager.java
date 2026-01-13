package com.qiumengzhou.tpaqmz.request;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TpaRequestManager {

    public static final Map<UUID, TpaRequest> REQUESTS = new HashMap<>();

    // 请求最大时效
    public static final long TIMEOUT_MS = 10000;

    public static void add(TpaRequest request) {
        REQUESTS.put(request.requestId, request);
    }

    public static void remove(TpaRequest request) {
        REQUESTS.remove(request.requestId);
    }
}
