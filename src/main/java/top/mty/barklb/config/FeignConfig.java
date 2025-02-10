package top.mty.barklb.config;

import feign.Feign;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public Feign.Builder feignBuilder() {
        return Feign.builder();
    }
}

