package top.mty.barklb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.mty.barklb.utils.FeignClientCache;

@Service
public class DynamicFeignClientService {
    @Autowired
    private DynamicFeignClientBuilder builder;

    public <T> T buildClient(Class<T> clientClass, String url) {
        T feignClient = FeignClientCache.getClient(clientClass, url);
        if (null == feignClient) {
            feignClient = builder.buildClient(clientClass, url);
            FeignClientCache.putClient(clientClass, url, feignClient);
            return feignClient;
        }
        return feignClient;
    }
}
