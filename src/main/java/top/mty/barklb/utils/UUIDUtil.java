package top.mty.barklb.utils;

import java.util.Locale;
import java.util.UUID;

public class UUIDUtil {

    /**
     * 生成基于时间戳的短 UUID
     * @return 一个唯一的短 UUID 字符串
     */
    public static String generateTimeBasedUUID() {
        long currentTimeMillis = System.currentTimeMillis();
        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return (Long.toHexString(currentTimeMillis) + uuidPart).toUpperCase(Locale.ROOT); // 时间戳 + 随机部分
    }
}
