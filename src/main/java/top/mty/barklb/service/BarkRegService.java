package top.mty.barklb.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import top.mty.barklb.common.Assert;
import top.mty.barklb.common.BException;
import top.mty.barklb.config.BarkRemoteConfig;
import top.mty.barklb.entity.BarkDeviceData;
import top.mty.barklb.entity.BarkDeviceKeyMapping;
import top.mty.barklb.entity.BarkServerAndDeviceData;
import top.mty.barklb.remote.clients.BarkClient;
import top.mty.barklb.remote.params.BarkRegResp;
import top.mty.barklb.utils.UUIDUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BarkRegService {

    @Value("${bark.remote.urls}")
    private String barkServerUrls;
    @Autowired
    private DynamicFeignClientService dynamicFeignClientService;
    @Autowired
    private BarkDeviceMappingService deviceMappingService;
    @Autowired
    private BarkRemoteConfig barkRemoteConfig;

    private static final String DELETED = "deleted";
    @Autowired
    private BarkDeviceMappingService barkDeviceMappingService;

    /**
     * 节点地址配置变更, 启动时自动向变更后的服务器发起注册
     * @throws BException 配置错误
     */
    @PostConstruct
    public void checkConfig() throws BException {
        List<String> urlsList = barkRemoteConfig.getUrlsList();
        if (CollectionUtils.isEmpty(urlsList)) {
            throw new BException("Bark服务器节点配置为空");
        }
        this.autoRegFromConfigUrls(urlsList);
    }

    /**
     * 向单个服务器节点发起注册
     * @param deviceToken 设备
     * @param url 服务节点地址
     * @return 注册结果
     */
    public BarkDeviceData tryReg2SingleNode(String deviceToken, String url) {
        log.info("向单个服务器节点进行注册: token: {}, url: {}", deviceToken, url);
        if (StringUtils.isEmpty(deviceToken) || StringUtils.isEmpty(url)) {
            log.error("没有设备token和节点地址无法进行注册");
            return null;
        }
        try {
            return this.handleNewReg(deviceToken, Collections.singletonList(url));
        } catch (Exception e) {
            log.error("向单节点发起注册失败: {}, {}", deviceToken, url);
            return null;
        }
    }

    /**
     * 自动根据配置的节点地址做注册
     * 1. 获取已有的所有token(代表唯一设备)
     * 2. 根据token查到所有已经注册的节点
     * 3. configUrls中排除已经注册的节点地址, 剩下的所有节点地址做注册.
     */
    private void autoRegFromConfigUrls(List<String> configUrls) {
        List<String> distinctTokens = barkDeviceMappingService.findDistinctTokens();
        log.info("应用已启动, 当前注册设备: {} 台", distinctTokens.size());
        for (String token : distinctTokens) {
            try {
                List<BarkDeviceKeyMapping> deviceKeyMappings = barkDeviceMappingService.findByDeviceToken(token);
                List<String> registeredNodeUrls = deviceKeyMappings.stream().map(BarkDeviceKeyMapping::getRemoteServerUrl).toList();
                List<String> urls4AutoReg = configUrls.stream().filter(url -> !registeredNodeUrls.contains(url)).toList();
                if (CollectionUtils.isEmpty(urls4AutoReg)) {
                    log.info("应用启动, 检查设备{}, 无新增需要注册的节点", token);
                } else {
                    log.info("应用启动, 检查设备{}, 自动向以下新节点发起注册: {}", token, urls4AutoReg);
                    this.handleNewReg(token, urls4AutoReg);
                }
            } catch (Exception e) {
                log.error("自动根据配置的节点地址做注册发生异常: {}, {}", token, e.getMessage());
            }
        }
    }

    /**
     * 注册配置中的所有节点
     *
     * @param deviceToken 机器本身的token 删除时为deleted
     * @param deviceKey   机器注册的key (注册时为"", 删除时为系统的"LB-xxxx", 每次新打开bark时似乎会进行一次注册检查, 此时给过来的deviceKey有值)
     * @return 注册的结果
     */
    @Transactional
    public BarkDeviceData registerOrDeleteAll(String deviceToken, String deviceKey) {
        if (StringUtils.isBlank(deviceToken)) {
            throw new RuntimeException("没有deviceToken无法注册");
        }
        // 所有远程节点地址
        List<String> urls = barkRemoteConfig.getUrlsList();
        if (CollectionUtils.isEmpty(urls)) {
            throw new RuntimeException("没有节点地址无法注册");
        }
        // 针对3类不同的deviceKey做不同的处理
        // 删除
        if (DELETED.equals(deviceToken)) {
            log.info("APP调用reg方法进行删除, token: {}, key: {}", deviceToken, deviceKey);
            return handleDelete(deviceKey, deviceToken, urls);
        }
        // 新注册
        if (StringUtils.isBlank(deviceKey)) {
            log.info("APP调用reg方法进行注册, token: {}, key: {}", deviceToken, deviceKey);
            return handleNewReg(deviceToken, urls);
        }
        // Bark启动时的再次调用
        if (StringUtils.isNotBlank(deviceKey)) {
            log.info("APP重启调用reg方法, token: {}, key: {}", deviceToken, deviceKey);
            return handleAppRestart(deviceToken, deviceKey, urls);
        }
        log.warn("无法识别的Bark客户端注册调用: token: {}, key: {}", deviceToken, deviceKey);
        return null;

    }

    private BarkDeviceData handleNewReg(String deviceToken, List<String> urls) {
        // 对每个节点做注册设备
        List<BarkServerAndDeviceData> barkServerAndDeviceDataList = new ArrayList<>();
        for (String url : urls) {
            BarkServerAndDeviceData regData = this.remoteRegisterOrDelete(deviceToken, "", url);
            if (null != regData) {
                barkServerAndDeviceDataList.add(regData);
            }
        }
        // 有节点注册成功则返回注册数据加入库
        if (!CollectionUtils.isEmpty(barkServerAndDeviceDataList)) {
            String lbSystemKey = "LB-" + UUIDUtil.generateTimeBasedUUID();
            deviceMappingService.insertSystemKeyWithBarkDeviceDataList(lbSystemKey, barkServerAndDeviceDataList);
            // 构造返回
            BarkDeviceData data = barkServerAndDeviceDataList.get(0).getData();
            BarkDeviceData result = new BarkDeviceData();
            result.setDeviceToken(data.getDeviceToken());
            result.setDeviceKey(lbSystemKey);
            result.setKey(lbSystemKey);
            return result;
        }
        return null;
    }

    /**
     * Bark app重启时会调用此接口
     * @param deviceToken 唯一deviceToken a2086... ...83b7d0
     * @param systemDeviceKey LB-... ...
     * @param urls 节点
     * @return 注册信息
     */
    private BarkDeviceData handleAppRestart(String deviceToken, String systemDeviceKey, List<String> urls) {
        // 如果第一次注册时异常, deviceKey则不正常. 直接调用新注册
        if (invalidDeviceKey(systemDeviceKey)) {
            return handleNewReg(deviceToken, urls);
        }
        // 1. 检查设备注册情况 2. 远端调用注册
        // 需要重新注册的设备token和对应节点地址数组
        List<RemoteRegisterInfo> reRemoteRegisterInfoList = new ArrayList<>();
        // 需要新注册的设备token和对应节点地址数组
        List<RemoteRegisterInfo> newRemoteRegisterInfoList = new ArrayList<>();
        for (String url : urls) {
            BarkDeviceKeyMapping existedKeyMapping = barkDeviceMappingService.getByTokenAndSystemDeviceKeyAndRemoteUrl(deviceToken, systemDeviceKey, url);
            // 如果没有注册记录, 可能是因为一些故障或者配置变更导致存在节点地址但没有注册信息, 则正好新注册一下.
            // 如果存在记录则取出对应的节点key, 做重新注册用
            String remoteServerKey = existedKeyMapping != null ? existedKeyMapping.getRemoteServerKey() : null;
            RemoteRegisterInfo remoteRegisterInfo = new RemoteRegisterInfo(deviceToken, remoteServerKey, url);
            if (invalidDeviceKey(remoteServerKey)) {
                newRemoteRegisterInfoList.add(remoteRegisterInfo);
            } else {
                reRemoteRegisterInfoList.add(remoteRegisterInfo);
            }
        }
        // 如果全部是新注册, 则可以return
        if (CollectionUtils.isEmpty(reRemoteRegisterInfoList)) {
            return handleNewReg(deviceToken, urls);
        }
        // 处理新注册
        List<String> newRegUrls = newRemoteRegisterInfoList.stream().map(RemoteRegisterInfo::getUrl).toList();
        handleNewReg(deviceToken, newRegUrls);
        // 处理重新注册
        List<BarkServerAndDeviceData> barkServerAndDeviceReRegDataList = new ArrayList<>();
        for (RemoteRegisterInfo remoteRegisterInfo : reRemoteRegisterInfoList) {
            String url = remoteRegisterInfo.getUrl();
            String token = remoteRegisterInfo.getDeviceToken();
            String key = remoteRegisterInfo.getKey();

            try {
                BarkServerAndDeviceData regResp = this.remoteRegisterOrDelete(token, key, url);
                if (null != regResp) {
                    barkServerAndDeviceReRegDataList.add(regResp);
                }
            } catch (Exception e) {
                log.error("App重启重新注册时发生异常: token: {}, key: {}, url: {}", token, key, url);
                // 重新注册发生异常, 则需要把本地注册记录删除
                deviceMappingService.deleteByTokenAndUrl(deviceToken, url);
            }
        }
        // 更新注册信息
        if (!CollectionUtils.isEmpty(barkServerAndDeviceReRegDataList)) {
            // 这里不确定是否还需要再更新一次本地数据库.
            // 远端节点在明确deviceToken和deviceKey的情况下, 重新调用注册是否还会返回不一样的deviceKey?
            log.info("APP重启, 节点重新注册的结果: {}", JSON.toJSONString(barkServerAndDeviceReRegDataList));
        }
        // 所有异常情况都处理完, 正常情况下按照请求的原数据返回即可
        BarkDeviceData barkDeviceData = new BarkDeviceData();
        barkDeviceData.setDeviceToken(deviceToken);
        barkDeviceData.setDeviceKey(systemDeviceKey);
        barkDeviceData.setKey(systemDeviceKey);
        return barkDeviceData;
    }

    /**
     * 与远程节点的交互 (注册和删除都是这个接口, 删除时deviceToken=deleted)
     * @param deviceToken 设备的token
     * @param deviceKey   原版bark节点的key
     */
    private BarkServerAndDeviceData remoteRegisterOrDelete(String deviceToken, String deviceKey, String url) {
        if (StringUtils.isEmpty(deviceToken)) {
            return null;
        }
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        try {
            // 调用 FeignClient 方法
            BarkRegResp response = dynamicFeignClientService.buildClient(BarkClient.class, url).register(deviceToken, deviceKey);
            Assert.notNull(response, "BarkRegResp");
            log.info("{} 请求结果: 来自 {}: {}", DELETED.equals(deviceToken) ? "delete" : "register", url, JSONObject.toJSONString(response));
            if (response.success()) {
                BarkDeviceData data = response.toBarkDeviceData();
                if (null != data) {
                    return new BarkServerAndDeviceData(url, data);
                }
            }
        } catch (Exception e) {
            log.error("Bark负载均衡节点 {} 错误: {}", url, e.getMessage());
        }
        return null;
    }

    /**
     * 处理删除 远程节点删除注册+本地删除注册
     */
    private BarkDeviceData handleDelete(String deviceKey, String deviceToken, List<String> urls) {
        if (StringUtils.isEmpty(deviceKey)) {
            throw new RuntimeException("没有deviceKey无法删除");
        }
        BarkDeviceData returnData = null;
        // 找到每个节点对应的真实deviceKey并删除
        for (String url : urls) {
            try {
                BarkDeviceKeyMapping deviceKeyMapping = deviceMappingService.getBySystemDeviceKeyAndRemoteUrl(deviceKey, url);
                if (null == deviceKeyMapping) {
                    throw new RuntimeException(String.format("用户操作设备删除服务器, 负载均衡服务注册记录找不到%s, %s的映射记录", deviceKey, url));
                }
                BarkServerAndDeviceData deleteRes = this.remoteRegisterOrDelete(deviceToken, deviceKeyMapping.getRemoteServerKey(), url);
                log.info("已删除{}, {}", deviceKey, url);
                Assert.notNull(deleteRes, "远程节点删除结果");
                if (returnData == null) {
                    returnData = deleteRes.getData();
                }
            } catch (Exception e) {
                log.error("设备删除注册时出错", e);
            }
        }
        // 本地系统删除
        deviceMappingService.delete(deviceKey);
        return returnData;
    }

    private boolean invalidDeviceKey(String deviceKey) {
        return StringUtils.isBlank(deviceKey) || "null".equals(deviceKey);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class RemoteRegisterInfo {
        private String deviceToken;
        private String key;
        private String url;
    }
}

