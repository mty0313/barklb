package top.mty.barklb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import top.mty.barklb.entity.BarkDeviceKeyMapping;

import java.util.List;

public interface BarkDeviceKeyMappingRepository extends JpaRepository<BarkDeviceKeyMapping, Long> {

    List<BarkDeviceKeyMapping> findBarkDeviceKeyMappingsBySystemDeviceKey(String systemDeviceKey);

    List<BarkDeviceKeyMapping> findBarkDeviceKeyMappingsByDeviceToken(String deviceToken);

    void deleteBarkDeviceKeyMappingsBySystemDeviceKey(String systemDeviceKey);

    void deleteBarkDeviceKeyMappingsByDeviceToken(String deviceToken);

    void deleteByDeviceTokenAndRemoteServerUrl(String deviceToken, String remoteServerUrl);

    void deleteAllByRemoteServerUrlNotIn(List<String> urls);

    BarkDeviceKeyMapping findBarkDeviceKeyMappingBySystemDeviceKeyAndRemoteServerUrl(String systemDeviceKey, String remoteServerUrl);

    BarkDeviceKeyMapping findBarkDeviceKeyMappingByDeviceTokenAndSystemDeviceKeyAndRemoteServerUrl(String deviceToken, String systemDeviceKey, String remoteServerUrl);

    @Query("SELECT DISTINCT b.deviceToken FROM BarkDeviceKeyMapping b")
    List<String> findDistinctDeviceTokens();
}
