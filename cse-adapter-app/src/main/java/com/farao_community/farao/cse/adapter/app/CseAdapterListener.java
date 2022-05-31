/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.gridcapa.task_manager.api.TaskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@Component
public class CseAdapterListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(CseAdapterListener.class);

    private final CseAdapter cseAdapter;

    public CseAdapterListener(CseAdapter cseAdapter) {
        this.cseAdapter = cseAdapter;
    }

    @Bean
    public Consumer<Flux<TaskDto>> handleRun() {
        return f -> f
            .onErrorContinue((t, r) -> LOGGER.error(t.getMessage(), t))
            .subscribe(cseAdapter::runAsync);
    }
}
