package top.mty.barklb.remote.params;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class BarkPushBody {
    @JsonProperty("body")
    private String body;
    @JsonProperty("title")
    private String title;
    @JsonProperty("badge")
    private Integer badge;
    @JsonProperty("sound")
    private String sound;
    @JsonProperty("icon")
    private String icon;
    @JsonProperty("group")
    private String group;
    @JsonProperty("url")
    private String url;
}
