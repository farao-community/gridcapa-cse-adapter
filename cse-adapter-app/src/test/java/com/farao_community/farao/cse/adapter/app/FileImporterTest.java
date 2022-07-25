/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.cse.adapter.app;

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
}
