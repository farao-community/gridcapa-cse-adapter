package com.farao_community.farao.cse.adapter.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static com.farao_community.farao.cse.adapter.app.ProcessType.D2CC;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-export.yml")
class CseExportAdapterConfigurationTest {

    @Autowired
    private CseExportAdapterConfiguration configuration;

    @Test
    void testConfigLoading() {
        assertEquals(D2CC, configuration.getTargetProcess());
    }
}
