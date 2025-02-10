package top.mty.barklb.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter @Setter
public class BarkDeviceKeyMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceToken;

    private String systemDeviceKey;

    private String remoteServerKey;

    private String remoteServerUrl;

    private Date created;

    private Date modified;
}
