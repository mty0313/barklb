package top.mty.barklb.controller;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.mty.barklb.common.R;
import top.mty.barklb.entity.BarkDeviceKeyMapping;
import top.mty.barklb.remote.clients.BarkPushLoadBalancedClient;
import top.mty.barklb.remote.params.BarkPushBody;
import top.mty.barklb.service.BarkDeviceMappingService;

@RestController
@Slf4j
public class BarkPushController {
    @Autowired
    private BarkPushLoadBalancedClient lbClient;
    @Autowired
    private BarkDeviceMappingService barkDeviceMappingService;

    @PostMapping("/{systemDevice}")
    public R<String> push(@PathVariable("systemDevice") String systemDevice, @RequestBody BarkPushBody body) {
        try {
            R<BarkPushLoadBalancedClient.BarkNodePushRes> lbClientPushRes = lbClient.push(systemDevice, body);
            if (lbClientPushRes.isSuccess()) {
                return R.success(null);
            } else {
                log.error("推送失败 {}", JSON.toJSONString(lbClientPushRes));
                String deviceToken = this.tryGetDeviceTokenBySystemDeviceKeyAndUrl(systemDevice, body.getUrl());
                return lbClient.tryAllPushAndUpdateServerList(deviceToken, systemDevice, body);
            }
        } catch (Exception e) {
            log.error("调用节点推送发生异常: {}", e.getMessage());
            String deviceToken = this.tryGetDeviceTokenBySystemDeviceKeyAndUrl(systemDevice, body.getUrl());
            return lbClient.tryAllPushAndUpdateServerList(deviceToken, systemDevice, body);
        }
    }

    private String tryGetDeviceTokenBySystemDeviceKeyAndUrl(String systemDeviceKey, String url) {
        BarkDeviceKeyMapping existed = barkDeviceMappingService.getBySystemDeviceKeyAndRemoteUrl(systemDeviceKey, url);
        return existed == null ? null : existed.getDeviceToken();
    }
}
