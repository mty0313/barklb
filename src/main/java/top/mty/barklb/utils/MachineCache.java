package top.mty.barklb.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.LinkedHashSet;

public class MachineCache {
    private static final Cache<String, LinkedHashSet<String>> machines = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build();

    public static void put(String key, LinkedHashSet<String> value) {
        machines.put(key, value);
    }

    public static LinkedHashSet<String> get(String key) {
        return machines.getIfPresent(key);
    }

    public static void remove(String key) {
        machines.invalidate(key);
    }

    public static void clear() {
        machines.invalidateAll();
    }
}
