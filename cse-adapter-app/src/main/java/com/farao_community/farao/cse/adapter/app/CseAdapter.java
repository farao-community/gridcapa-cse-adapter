/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.gridcapa.task_manager.api.ProcessRunDto;
import com.farao_community.farao.gridcapa.task_manager.api.TaskDto;

import java.util.List;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public interface CseAdapter {

    void runAsync(TaskDto taskDto);

    static String getCurrentRunId(TaskDto taskDto) {
        List<ProcessRunDto> runHistory = taskDto.getRunHistory();
        if (runHistory == null || runHistory.isEmpty()) {
            throw new CseAdapterException("Failed to handle manual run request on timestamp because it has no run history");
        }
        if (runHistory.size() > 1) {
            runHistory.sort((o1, o2) -> o2.getExecutionDate().compareTo(o1.getExecutionDate()));
        }
        return runHistory.get(0).getId().toString();
    }
}
