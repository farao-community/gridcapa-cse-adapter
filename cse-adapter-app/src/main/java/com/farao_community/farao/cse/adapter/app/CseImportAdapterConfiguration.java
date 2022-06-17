/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.cse.adapter.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@Configuration
@ConditionalOnProperty(prefix = "cse-adapter", value = "exchange-type", havingValue = "import")
public class CseImportAdapterConfiguration {

    @Value("${cse-adapter.process-type}")
    private String processType;

    public ProcessType getProcessType() {
        return ProcessType.valueOf(processType);
    }

}
