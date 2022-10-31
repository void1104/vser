package com.pjx.vser.common.constant;

import java.util.HashMap;
import java.util.Map;

public class ServerConst {

    private static final Map<String, String> tMap = new HashMap<>();

    static {
        tMap.put("httpConnector.alreadyStarted", "HTTP connector has already been started");
    }

    public static String get(String key) {
        return tMap.get(key);
    }
}
