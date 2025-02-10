package top.mty.barklb.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

public class FeignClientCache {

    private static final Cache<String, Object> feignClients = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .maximumSize(100)
            .build();

    @SuppressWarnings("unchecked")
    public static <T> T getClient(Class<T> clazz, String url) {
        String cacheKey = buildCacheKey(clazz, url);
        Object client = feignClients.getIfPresent(cacheKey);
        if (clazz.isInstance(client)) {
            return (T) client;
        }
        return null;
    }

    public static <T> void putClient(Class<T> clazz, String url, T client) {
        String cacheKey = buildCacheKey(clazz, url);
        feignClients.put(cacheKey, client);
    }

    private static String buildCacheKey(Class<?> clazz, String url) {
        return clazz.getName() + "@" + url;
    }
}
