package top.mty.barklb.service;

import feign.Feign;
import feign.Logger;
import org.springframework.stereotype.Service;
import top.mty.barklb.remote.clients.BarkClient;

@Service
public class DynamicFeignClient {
    // 动态创建 FeignClient 方法
    public BarkClient createBarkClient(String dynamicUrl) {
        // 使用 Feign.Builder 来构造 FeignClient
        return Feign.builder()
                .requestInterceptor(template -> template.target(dynamicUrl))  // 设置动态的 URL
                .logger(new Logger.JavaLogger("http.log"))
                .logLevel(Logger.Level.FULL)
                .target(BarkClient.class, dynamicUrl);  // 返回 FeignClient 实例
    }
}

