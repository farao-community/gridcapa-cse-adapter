/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.cse.adapter.app.model.AutomatedForcedPras;
import com.farao_community.farao.cse.adapter.app.model.UserConfiguration;
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
        UserConfiguration userConfiguration = fileImporter.importUserConfiguration(ClassLoader.getSystemResource("userConfigs.json").toString());
        assertEquals(10., userConfiguration.getInitialDichotomyIndex(), 0.);
        assertEquals(2, userConfiguration.getManualForcedPrasIds().size());
        assertEquals(5, userConfiguration.getMaximumDichotomiesNumber());
    }

    @Test
    void testUserConfigurationWithEmptyListOfForcedPrasImport() {
        UserConfiguration userConfiguration = fileImporter.importUserConfiguration(ClassLoader.getSystemResource("userConfigs-empty.json").toString());
        assertEquals(10., userConfiguration.getInitialDichotomyIndex(), 0.);
        assertEquals(0, userConfiguration.getManualForcedPrasIds().size());
    }

    @Test
    void testAutomatedForcedPrasImport() {
        AutomatedForcedPras automatedForcedPras = fileImporter.importAutomatedForcedPras(ClassLoader.getSystemResource("automatedForcedPras.json").toString());
        assertEquals(3, automatedForcedPras.getAutomatedForcedPrasIds().size());
        assertEquals(2, automatedForcedPras.getAutomatedForcedPrasIds().get("380kV Sils-Soazza").size());
        assertEquals(1, automatedForcedPras.getAutomatedForcedPrasIds().get("380kV Divaca-Redipuglia").size());
        assertEquals(2, automatedForcedPras.getAutomatedForcedPrasIds().get("380kV Divaca-Redipuglia").get(0).size());
        assertEquals("PRA_2N_Magenta", automatedForcedPras.getAutomatedForcedPrasIds().get("380kV Divaca-Redipuglia").get(0).iterator().next());
        assertEquals("PRA_2N_Magenta", automatedForcedPras.getAutomatedForcedPrasIds().get("default").get(0).iterator().next());

    }
}
