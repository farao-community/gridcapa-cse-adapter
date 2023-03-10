/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.cse.adapter.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.farao_community.farao.cse.adapter.app.ProcessType.IDCC;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@SpringBootTest
@ActiveProfiles("idcc-import")
class CseIdccImportAdapterConfigurationTest {

    @Autowired
    private CseImportAdapterConfiguration configuration;

    @Test
    void testConfigLoading() {
        assertEquals(IDCC, configuration.getProcessType());
        assertFalse(configuration.isEcImport());
    }
}
