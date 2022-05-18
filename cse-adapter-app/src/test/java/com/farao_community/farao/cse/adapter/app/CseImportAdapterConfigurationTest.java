package com.farao_community.farao.cse.adapter.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static com.farao_community.farao.cse.adapter.app.ProcessType.IDCC;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-import.yml")
class CseImportAdapterConfigurationTest {

    @Autowired
    private CseImportAdapterConfiguration configuration;

    @Test
    void testConfigLoading() {
        assertEquals(IDCC, configuration.getTargetProcess());
        assertEquals("/path/to/fs/location", configuration.getTargetChFsPath());
        assertEquals("/path/to/minio/location", configuration.getTargetChMinioPath());
    }
}
