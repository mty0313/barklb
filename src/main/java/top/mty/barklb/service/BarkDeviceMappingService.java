package top.mty.barklb.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.mty.barklb.common.Assert;
import top.mty.barklb.entity.BarkDeviceKeyMapping;
import top.mty.barklb.entity.BarkServerAndDeviceData;
import top.mty.barklb.repository.BarkDeviceKeyMappingRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class BarkDeviceMappingService {
    @Autowired
    private BarkDeviceKeyMappingRepository repository;

    /**
     * 新增
     */
    public void insert(List<BarkDeviceKeyMapping> deviceKeyMappings) {
        if (!validate(deviceKeyMappings)) {
            return;
        }
        for (BarkDeviceKeyMapping deviceKeyMapping : deviceKeyMappings) {
            deviceKeyMapping.setCreated(new Date());
            deviceKeyMapping.setModified(new Date());
        }
        repository.saveAll(deviceKeyMappings);
    }

    /**
     * 删除 by deviceKey
     */
    public void delete(String systemDeviceKey) {
        if (StringUtils.isEmpty(systemDeviceKey)) {
            return;
        }
        repository.deleteBarkDeviceKeyMappingsBySystemDeviceKey(systemDeviceKey);
    }

    /**
     * 删除 by deviceToken和节点url
     */
    public void deleteByTokenAndUrl(String token, String url) {
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(url)) {
            return;
        }
        repository.deleteByDeviceTokenAndRemoteServerUrl(token, url);
    }

    /**
     * 删除url不在参数中的记录
     * @param notInUrls 配置中的节点url, 不在配置节点中的url都从db删除
     */
    public void deleteByNotInUrls(List<String> notInUrls) {
        if (CollectionUtils.isEmpty(notInUrls)) {
            return;
        }
        repository.deleteAllByRemoteServerUrlNotIn(notInUrls);
    }

    public List<String> findDistinctTokens() {
        return repository.findDistinctDeviceTokens();
    }

    /**
     * 通过deviceKey和url查找, 应当只有1条记录
     */
    public BarkDeviceKeyMapping getBySystemDeviceKeyAndRemoteUrl(String systemDeviceKey, String remoteServerUrl) {
        return repository.findBarkDeviceKeyMappingBySystemDeviceKeyAndRemoteServerUrl(systemDeviceKey, remoteServerUrl);
    }

    /**
     * 通过deviceToken, 系统的deviceKey, 节点的地址查找, 应当只有1条记录
     */
    public BarkDeviceKeyMapping getByTokenAndSystemDeviceKeyAndRemoteUrl(String token, String systemDeviceKey, String remoteServerUrl) {
        return repository.findBarkDeviceKeyMappingByDeviceTokenAndSystemDeviceKeyAndRemoteServerUrl(token, systemDeviceKey, remoteServerUrl);
    }

    /**
     * 通过bark的注册数据插入本地注册记录
     * 一个systemDeviceKey可能存在多条记录, 因为一个systemDeviceKey可能对应多个remoteServer
     * @param systemDeviceKey 系统DeviceKey, LB-xxxxx
     * @param barkServerAndDeviceDataList 原版Bark服务的注册数据.
     */
    public void insertSystemKeyWithBarkDeviceDataList(String systemDeviceKey, List<BarkServerAndDeviceData> barkServerAndDeviceDataList) {
        Assert.notEmpty(systemDeviceKey, "bark system device key");
        Assert.notEmpty(barkServerAndDeviceDataList, "bark remote server data");
        List<BarkDeviceKeyMapping> deviceKeyMappingList = new ArrayList<>();
        String deviceToken = barkServerAndDeviceDataList.get(0).getData().getDeviceToken();
        List<BarkDeviceKeyMapping> existedMappingsByToken = this.findByDeviceToken(deviceToken);
        String existedSystemDeviceKeyByToken = null;
        if (!CollectionUtils.isEmpty(existedMappingsByToken)) {
            existedSystemDeviceKeyByToken = existedMappingsByToken.get(0).getSystemDeviceKey();
        }
        systemDeviceKey = StringUtils.isEmpty(existedSystemDeviceKeyByToken) ? systemDeviceKey : existedSystemDeviceKeyByToken;
        for (BarkServerAndDeviceData serverAndDeviceData : barkServerAndDeviceDataList) {
            BarkDeviceKeyMapping barkDeviceKeyMapping = new BarkDeviceKeyMapping();
            barkDeviceKeyMapping.setDeviceToken(serverAndDeviceData.getData().getDeviceToken());
            barkDeviceKeyMapping.setSystemDeviceKey(systemDeviceKey);
            barkDeviceKeyMapping.setRemoteServerKey(serverAndDeviceData.getData().getKey());
            barkDeviceKeyMapping.setRemoteServerUrl(serverAndDeviceData.getServerUrl());
            deviceKeyMappingList.add(barkDeviceKeyMapping);
        }
        this.insert(deviceKeyMappingList);
    }

    /**
     * 通过systemDeviceKey查找多条记录
     */
    public List<BarkDeviceKeyMapping> findRegInfoBySystemDeviceKey(String systemDeviceKey) {
        List<BarkDeviceKeyMapping> deviceKeyMappings = repository.findBarkDeviceKeyMappingsBySystemDeviceKey(systemDeviceKey);
        if (CollectionUtils.isEmpty(deviceKeyMappings)) {
            log.error("{} 没有对应的负载均衡节点信息", systemDeviceKey);
            return new ArrayList<>();
        }
        return deviceKeyMappings;
    }

    /**
     * 通过deviceToken查找多条记录
     */
    public List<BarkDeviceKeyMapping> findByDeviceToken(String deviceToken) {
        if (StringUtils.isEmpty(deviceToken)) {
            return new ArrayList<>();
        }
        return repository.findBarkDeviceKeyMappingsByDeviceToken(deviceToken);
    }

    public void deleteByDeviceToken(String deviceToken) {
        if (StringUtils.isEmpty(deviceToken)) {
            log.error("没有deviceToken无法删除本地注册记录");
        }
        repository.deleteBarkDeviceKeyMappingsByDeviceToken(deviceToken);
    }

    private boolean validate(List<BarkDeviceKeyMapping> deviceKeyMappings) {
        try {
            for (BarkDeviceKeyMapping keyMapping : deviceKeyMappings) {
                Assert.notEmpty(keyMapping.getSystemDeviceKey(), "getSystemDeviceKey");
                Assert.notEmpty(keyMapping.getRemoteServerKey(), "getRemoteServerKey");
                Assert.notEmpty(keyMapping.getRemoteServerUrl(), "getRemoteServerUrl");
            }
        } catch (Exception e) {
            log.error("deviceKeyMappings 参数不通过 : ", e);
        }
        return true;
    }
}
