/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.cse.adapter.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@Configuration
public class CseAdapterConfiguration {

    @Value("${cse-adapter.target-process}")
    private String targetProcess;

    @Value("${cse-adapter.target-ch-fs-location}")
    private String targetChFsPath;

    @Value("${cse-adapter.target-ch-minio-location}")
    private String targetChMinioPath;

    public String getTargetProcess() {
        return targetProcess;
    }

    public String getTargetChFsPath() {
        return targetChFsPath;
    }

    public String getTargetChMinioPath() {
        return targetChMinioPath;
    }

}
