package top.mty.barklb.remote.clients;

import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.mty.barklb.common.Constants;
import top.mty.barklb.common.ErrorCode;
import top.mty.barklb.common.R;
import top.mty.barklb.config.BarkRemoteConfig;
import top.mty.barklb.entity.BarkDeviceData;
import top.mty.barklb.entity.BarkDeviceKeyMapping;
import top.mty.barklb.remote.params.BarkPushBody;
import top.mty.barklb.service.BarkDeviceMappingService;
import top.mty.barklb.service.BarkRegService;
import top.mty.barklb.service.BarkServerLoadBalancer;
import top.mty.barklb.service.DynamicFeignClientService;
import top.mty.barklb.utils.MachineCache;

import java.util.List;

@Service
@Slf4j
public class BarkPushLoadBalancedClient {
    @Autowired
    private DynamicFeignClientService dynamicFeignClientService;
    @Autowired
    private BarkRemoteConfig config;
    @Autowired
    private BarkServerLoadBalancer loadBalancer;
    @Autowired
    private BarkDeviceMappingService barkDeviceKeyMappingService;
    @Autowired
    private BarkRegService barkRegService;

    public R<BarkNodePushRes> push(String systemDeviceKey, BarkPushBody body) {
        String pickedServer = loadBalancer.pickServer();
        if (StringUtils.isEmpty(pickedServer)) {
            throw new RuntimeException("可选的节点为空");
        }
        BarkDeviceKeyMapping deviceKeyMapping = barkDeviceKeyMappingService.getBySystemDeviceKeyAndRemoteUrl(systemDeviceKey, pickedServer);
        if (null == deviceKeyMapping) {
            log.error("无法根据systemDeviceKey: {} 和pickedServer: {} 找到负载均衡服务注册记录", systemDeviceKey, pickedServer);
            return R.error(ErrorCode.NO_MAPPING_RECORD.getCode(), "无法找到负载均衡服务注册记录",
                    new BarkNodePushRes(false, pickedServer, systemDeviceKey));
        }
        String remoteServerKey = deviceKeyMapping.getRemoteServerKey();
        try {
            R<Void> currentClientPushRes = dynamicFeignClientService.buildClient(BarkClient.class, pickedServer).push(remoteServerKey, body);
            if (currentClientPushRes.isSuccess()) {
                return R.success(null);
            } else {
                return R.error(ErrorCode.RESP_ERROR.getCode(), currentClientPushRes.getMessage(), null);
            }
        } catch (Exception e) {
            log.error("当前选择的节点推送失败 : {}", pickedServer, e);
            return R.error(ErrorCode.REQ_EXCEPTION.getCode(), e.getMessage(), null);
        }
    }

    /**
     * 对所有节点尝试发起一次推送请求
     * @param deviceToken 这个参数在标准的Bark服务端推送时是不需要的. 在推送时如果没有在节点注册, 想要发起注册时则需要这个参数. 目前暂时不这么做
     *                    而是在应用启动时检查注册情况.
     * @param systemDeviceKey LB-... ...
     * @param body 推送body
     * @return 推送结果
     */
    public R<String> tryAllPushAndUpdateServerList(String deviceToken, String systemDeviceKey, BarkPushBody body) {
        log.info("尝试所有节点进行推送并更新在线节点列表... ...");
        List<String> configServers = config.getUrlsList();
        for (String configServer : configServers) {
            BarkDeviceKeyMapping deviceKeyMapping = barkDeviceKeyMappingService.getBySystemDeviceKeyAndRemoteUrl(systemDeviceKey, configServer);
            if (null == deviceKeyMapping) {
                log.error("设备映射不存在: systemDeviceKey: {}, configServer: {}, deviceToken: {} 尝试下一个节点", systemDeviceKey, configServer, deviceToken);
                continue;
            }
            String remoteServerKey = deviceKeyMapping.getRemoteServerKey();
            try {
                R<Void> currentClientPushRes = dynamicFeignClientService.buildClient(BarkClient.class, configServer).push(remoteServerKey, body);
                if (currentClientPushRes.isSuccess()) {
                    log.info("配置中的节点: {} 推送成功, 加入在线列表, 尝试结束", configServer);
                    MachineCache.get(Constants.ONLINE_MACHINES).add(configServer);
                    return R.success(null);
                } else {
                    log.error("配置中的节点 {} 推送失败, 从在线节点列表中剔除", configServer);
                    MachineCache.get(Constants.ONLINE_MACHINES).remove(configServer);
                }
            } catch (Exception e) {
                log.error("当前配置的节点推送失败, 更新在线节点列表 : {}", configServer, e);
                MachineCache.get(Constants.ONLINE_MACHINES).remove(configServer);
            }
        }
        return R.error(ErrorCode.ALL_NODES_FAILURE.getCode(), "所有配置节点推送均失败", null);
    }


    /**
     * Bark节点的推送结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BarkNodePushRes {
        private boolean success;
        private String pickedServer;
        private String systemDeviceKey;
    }
}
