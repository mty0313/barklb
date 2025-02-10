package top.mty.barklb;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.mty.barklb.entity.BarkDeviceKeyMapping;
import top.mty.barklb.repository.BarkDeviceKeyMappingRepository;
import top.mty.barklb.service.BarkDeviceMappingService;

import java.util.List;

@SpringBootTest
public class JpaTest {
    @Autowired
    private BarkDeviceKeyMappingRepository repository;
    @Autowired
    private BarkDeviceMappingService service;

    private static final String SYSTEM_KEY = "";

    @Test
    public void testGetAll() {
        List<BarkDeviceKeyMapping> dataList = repository.findAll();
        System.out.println(dataList);
    }

    @Test
    public void testGeyRemoteKeysBySystemKey() {
        List<BarkDeviceKeyMapping> remoteKeys = service.findRegInfoBySystemDeviceKey(SYSTEM_KEY);
        System.out.println(remoteKeys);
    }
}
