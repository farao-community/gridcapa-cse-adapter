package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.gridcapa.task_manager.api.TaskDto;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public interface CseAdapter {

    void runAsync(TaskDto taskDto);
}
