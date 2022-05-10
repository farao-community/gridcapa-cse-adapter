package com.farao_community.farao.cse.adapter.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileImporterTest {

    @Autowired
    private FileImporter fileImporter;

    @Test
    void testUserConfigurationImport() {
        UserConfiguration userConfiguration = fileImporter.importUserConfiguration(ClassLoader.getSystemResource("forcedPras.json").toString());
        assertEquals(10., userConfiguration.getInitialDichotomyIndex(), 0.);
        assertEquals(2, userConfiguration.getForcedPrasIds().size());
    }

    @Test
    void testUserConfigurationWithEmptyListOfForcedPrasImport() {
        UserConfiguration userConfiguration = fileImporter.importUserConfiguration(ClassLoader.getSystemResource("forcedPras-empty.json").toString());
        assertEquals(10., userConfiguration.getInitialDichotomyIndex(), 0.);
        assertEquals(0, userConfiguration.getForcedPrasIds().size());
    }

    @Test
    void testUserConfigurationWithNullForForcedPrasImport() {
        UserConfiguration userConfiguration = fileImporter.importUserConfiguration(ClassLoader.getSystemResource("forcedPras-null.json").toString());
        assertEquals(10., userConfiguration.getInitialDichotomyIndex(), 0.);
        assertEquals(0, userConfiguration.getForcedPrasIds().size());
    }
}
