package top.mty.barklb.controller;

import cn.hutool.core.map.MapUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

/**
 * Misc Controller
 */
@RestController
@Validated
public class MiscController {
    @GetMapping("/ping")
    public Map<Object, Object> ping() {
        return MapUtil.builder()
                .put("code", 200)
                .put("message", "pong")
                .put("timestamp", new Date().getTime())
                .build();
    }

    @GetMapping("/healthz")
    public String healthZ() {
        return "ok";
    }

    @GetMapping("/info")
    public Map<Object, Object> info() {
        return MapUtil.builder()
                .put("version", "v1_load_balancer")
                .build();
    }
}
