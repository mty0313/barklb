package top.mty.barklb.config;

import feign.Client;
import org.springframework.context.annotation.Bean;

public class BarkClientConfiguration extends FeignClientConfiguration{

    // 禁用负载均衡
    @Bean
    public Client feignClient() {
        return new Client.Default(null, null);
    }

}
