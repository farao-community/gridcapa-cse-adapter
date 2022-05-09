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
    void testForcedPrasImport() {
        ForcedPras forcedPras = fileImporter.importInputForcedPras(ClassLoader.getSystemResource("forcedPras.json").toString());
        assertEquals(1000., forcedPras.getInitialDichotomyStep(), 0.);
        assertEquals(2, forcedPras.getForcedPrasIds().size());
    }
}
