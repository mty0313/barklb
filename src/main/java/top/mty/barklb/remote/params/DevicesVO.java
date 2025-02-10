package top.mty.barklb.remote.params;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class DevicesVO implements Serializable {
    private static final long serialVersionUID = -905931789579806573L;

    private String key;
    @JsonProperty("device_key")
    private String deviceKey;
    @JsonProperty("device_token")
    private String deviceToken;
}
