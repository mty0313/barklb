package top.mty.barklb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BarkServerAndDeviceData {
    private String serverUrl;
    private BarkDeviceData data;
}
