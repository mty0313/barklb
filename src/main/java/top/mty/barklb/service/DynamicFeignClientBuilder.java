package top.mty.barklb.service;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.stereotype.Component;

@Component
public class DynamicFeignClientBuilder {

    private final Feign.Builder feignBuilder;

    @Autowired
    public DynamicFeignClientBuilder(Feign.Builder feignBuilder) {
        this.feignBuilder = feignBuilder;
    }

    protected  <T> T buildClient(Class<T> clientClass, String baseUrl) {
        return feignBuilder
                .contract(new SpringMvcContract()) // 使用 SpringMvcContract
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .logger(new Slf4jLogger(clientClass))
                .target(clientClass, baseUrl);
    }
}

