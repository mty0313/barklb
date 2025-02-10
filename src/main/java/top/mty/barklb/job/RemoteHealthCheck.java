package top.mty.barklb.job;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.mty.barklb.common.Constants;
import top.mty.barklb.common.R;
import top.mty.barklb.config.BarkRemoteConfig;
import top.mty.barklb.remote.clients.BarkClient;
import top.mty.barklb.service.DynamicFeignClientBuilder;
import top.mty.barklb.service.DynamicFeignClientService;
import top.mty.barklb.utils.MachineCache;

import java.util.LinkedHashSet;
import java.util.List;

@Component
@Slf4j
public class RemoteHealthCheck {

    @Autowired
    private DynamicFeignClientService clientService;
    @Autowired
    private BarkRemoteConfig config;

    /**
     * 健康检查, 维护machines缓存列表.
     */
    @Scheduled(fixedRate = 300000)
    public void healthCheck() {
        List<String> remoteServers = config.getUrlsList();
        for (String remote : remoteServers) {
            try {
                BarkClient client = clientService.buildClient(BarkClient.class, remote);
                R<Void> pong = client.ping();
                if (!pong.isSuccess()) {
                    deleteOnlineMachine(remote);
                    throw new RuntimeException("Bark负载均衡节点故障: " + remote);
                } else {
                    log.info("{} 在线, 维护到在线节点列表中", remote);
                    addOnlineMachine(remote);
                }
            } catch (Exception e) {
                deleteOnlineMachine(remote);
                log.error("Bark负载均衡节点 {} 健康检查未通过, 已剔除", remote);
            }
        }
        log.info("当前所有Bark服务在线节点: {}", JSON.toJSONString(MachineCache.get(Constants.ONLINE_MACHINES)));
    }

    private void addOnlineMachine(String url) {
        MachineCache.get(Constants.ONLINE_MACHINES).add(url);
    }

    private void deleteOnlineMachine(String url) {
        LinkedHashSet<String> machines = MachineCache.get(Constants.ONLINE_MACHINES);
        if (null == machines) {
            MachineCache.put(Constants.ONLINE_MACHINES, new LinkedHashSet<>());
            return;
        }
        machines.remove(url);
    }

    @PostConstruct
    public void init() {
        MachineCache.put(Constants.ONLINE_MACHINES, new LinkedHashSet<>());
    }
}
