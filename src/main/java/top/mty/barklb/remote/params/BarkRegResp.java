package top.mty.barklb.remote.params;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import top.mty.barklb.entity.BarkDeviceData;

@Getter @Setter
public class BarkRegResp {

    @JsonProperty("code")
    private int code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("data")
    private DataDTO data;
    @JsonProperty("timestamp")
    private Integer timestamp;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @JsonProperty("key")
        private String key;
        @JsonProperty("device_key")
        private String deviceKey;
        @JsonProperty("device_token")
        private String deviceToken;
    }

    public boolean success() {
        return 200 == this.code && null != this.data && StringUtils.isNotBlank(this.data.deviceToken)
                && StringUtils.isNotBlank(this.data.key) && StringUtils.isNotBlank(this.data.deviceKey);
    }

    public BarkDeviceData toBarkDeviceData() {
        if (!this.success()) {
            return null;
        }
        BarkDeviceData barkDeviceData = new BarkDeviceData();
        barkDeviceData.setDeviceKey(this.data.deviceKey);
        barkDeviceData.setKey(this.data.key);
        barkDeviceData.setDeviceToken(this.data.deviceToken);
        return barkDeviceData;
    }
}
