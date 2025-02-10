package top.mty.barklb.remote.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import top.mty.barklb.common.R;
import top.mty.barklb.config.BarkClientConfiguration;
import top.mty.barklb.config.FeignClientConfiguration;
import top.mty.barklb.remote.params.BarkPushBody;
import top.mty.barklb.remote.params.BarkRegResp;

@FeignClient(name = "bark-client", configuration = BarkClientConfiguration.class)
public interface BarkClient {
    @GetMapping(value = "/register")
    BarkRegResp register(@RequestParam("devicetoken") String deviceToken, @RequestParam("key") String deviceKey);

    @GetMapping(value = "/ping")
    R<Void> ping();

    @PostMapping(value = "/{device}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    R<Void> push(@PathVariable("device") String device, @RequestBody BarkPushBody body);
}
