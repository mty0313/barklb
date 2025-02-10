package top.mty.barklb.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mty.barklb.common.Constants;
import top.mty.barklb.utils.MachineCache;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class BarkServerLoadBalancer {
    private final Random random = new Random();

    public String pickServer() {
        LinkedHashSet<String> servers = MachineCache.get(Constants.ONLINE_MACHINES);
        List<String> urlList = new ArrayList<>(servers);
        int randomIndex = random.nextInt(urlList.size());
        String url = urlList.get(randomIndex);
        log.info("Bark负载均衡节点当前使用: {}", url);
        return url;
    }
}
