package top.mty.barklb.config;

import feign.Logger;
import feign.Request;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

public class FeignClientConfiguration {
  @Bean
  public Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
  }

  @Bean
  public Request.Options requestOptions() {
    // 配置连接超时时间和读取超时时间
    return new Request.Options(
            5000, // 连接超时时间（单位：毫秒）
            TimeUnit.MILLISECONDS,
            10000, // 读取超时时间（单位：毫秒）
            TimeUnit.MILLISECONDS,
            true // 是否跟随重定向
    );
  }
}
