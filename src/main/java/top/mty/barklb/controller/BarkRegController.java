package top.mty.barklb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import top.mty.barklb.common.Assert;
import top.mty.barklb.common.R;
import top.mty.barklb.entity.BarkDeviceData;
import top.mty.barklb.entity.BarkDeviceKeyMapping;
import top.mty.barklb.service.BarkDeviceMappingService;
import top.mty.barklb.service.BarkRegService;

import java.util.List;

@RestController
public class BarkRegController {
    @Autowired
    private BarkRegService barkRegService;
    @Autowired
    private BarkDeviceMappingService deviceMappingService;

    /**
     * 注册设备或删除设备(删除时deviceToken=deleted)
     * 特殊情况是在bark客户端重复添加同一节点地址时前端似乎会并发调用此接口引起sqlite的锁问题
     * 先简单处理为synchronized
     * @param deviceToken 设备Token
     * @param deviceKey   推送Key
     */
    @RequestMapping("/register")
    public synchronized R<BarkDeviceData> registerAllDevices(@RequestParam("devicetoken") String deviceToken,
                                                @RequestParam("key") String deviceKey) {
        Assert.notEmpty(deviceToken);
        BarkDeviceData data = barkRegService.registerOrDeleteAll(deviceToken, deviceKey);
        return R.success(data);
    }

    /**
     * 注册检查
     *
     * @param deviceKey 推送Key
     */
    @GetMapping("/register/{deviceKey}")
    public void register(@PathVariable("deviceKey") String deviceKey) {
        Assert.notEmpty(deviceKey, "Device key");
        List<BarkDeviceKeyMapping> exists = deviceMappingService.findRegInfoBySystemDeviceKey(deviceKey);
        Assert.assertTrue(!CollectionUtils.isEmpty(exists), "存在设备注册信息");
    }
}

