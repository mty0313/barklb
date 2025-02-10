package top.mty.barklb.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BarkDeviceData {
    /**
     * key赋值同device_key, 通过看bark服务端源码确认的, 不知道是什么原因
     */
    @JsonProperty("key")
    private String key;
    @JsonProperty("device_key")
    private String deviceKey;
    @JsonProperty("device_token")
    private String deviceToken;
}
