package top.mty.barklb.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import top.mty.barklb.common.BException;
import top.mty.barklb.service.BarkDeviceMappingService;
import top.mty.barklb.service.BarkRegService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "bark.remote")
@Getter @Setter
@Slf4j
public class BarkRemoteConfig {
    private String urls;  // 将urls配置为String类型

    @Autowired
    private BarkDeviceMappingService deviceMappingService;
    public List<String> getUrlsList() {
        return urls != null && !urls.isEmpty() ? Arrays.asList(urls.split(",")) : Collections.emptyList();
    }

    /**
     * 每次重启如果有配置变动在这里处理
     * 1. 找到已有的deviceToken
     * 2. 重新拿deviceToken向服务器注册
     * 3. 删除原来的记录
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void configReload() {
        try {
            List<String> urls = this.getUrlsList();
            // 每次启动时可能有新的配置变更, 如果变更了配置则删除DB中原来的那些Bark节点
            deviceMappingService.deleteByNotInUrls(urls);
        } catch (Exception e) {
            log.error("启动时清理数据库出错", e);
        }
    }

}
