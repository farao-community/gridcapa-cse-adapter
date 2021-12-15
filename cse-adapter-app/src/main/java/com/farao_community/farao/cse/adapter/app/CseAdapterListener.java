/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.cse.runner.api.resource.CseRequest;
import com.farao_community.farao.gridcapa.task_manager.api.ProcessFileDto;
import com.farao_community.farao.gridcapa.task_manager.api.TaskDto;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@Component
public class CseAdapterListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(CseAdapterListener.class);

    private final CseAdapterConfiguration cseAdapterConfiguration;

    public CseAdapterListener(CseAdapterConfiguration cseAdapterConfiguration) {
        this.cseAdapterConfiguration = cseAdapterConfiguration;
    }

    @Bean
    public Function<TaskDto, CseRequest> handleRun() {
        return taskDto -> {
            LOGGER.info("Handling timestamp {}", taskDto.getTimestamp());
            switch (cseAdapterConfiguration.getTargetProcess()) {
                case "IDCC":
                    LOGGER.info("Sending IDCC request");
                    return getIdccRequest(taskDto);
                case "D2CC":
                    LOGGER.info("Sending IDCC request");
                    return getD2ccRequest(taskDto);
                default:
                    throw new NotImplementedException(String.format("Unknown target process for CSE: %s", cseAdapterConfiguration.getTargetProcess()));
            }
        };
    }

    CseRequest getIdccRequest(TaskDto taskDto) {
        Map<String, String> processFileUrlByType = taskDto.getProcessFiles().stream()
            .collect(Collectors.toMap(
                ProcessFileDto::getFileType,
                ProcessFileDto::getFileUrl
            ));
        return CseRequest.idccProcess(
            taskDto.getId().toString(),
            taskDto.getTimestamp().atZone(ZoneId.of("UTC")).toOffsetDateTime(),
            Optional.ofNullable(processFileUrlByType.get("CGM")).orElseThrow(() -> new CseAdapterException("CGM type not found")),
            Optional.ofNullable(processFileUrlByType.get("CRAC")).orElseThrow(() -> new CseAdapterException("CRAC type not found")),
            Optional.ofNullable(processFileUrlByType.get("GLSK")).orElseThrow(() -> new CseAdapterException("GLSK type not found")),
            Optional.ofNullable(processFileUrlByType.get("NTC-RED")).orElseThrow(() -> new CseAdapterException("NTC-RED type not found")),
            Optional.ofNullable(processFileUrlByType.get("NTC2-AT")).orElseThrow(() -> new CseAdapterException("NTC2-AT type not found")),
            Optional.ofNullable(processFileUrlByType.get("NTC2-CH")).orElseThrow(() -> new CseAdapterException("NTC2-CH type not found")),
            Optional.ofNullable(processFileUrlByType.get("NTC2-FR")).orElseThrow(() -> new CseAdapterException("NTC2-FR type not found")),
            Optional.ofNullable(processFileUrlByType.get("NTC2-SI")).orElseThrow(() -> new CseAdapterException("NTC2-SI type not found")),
            Optional.ofNullable(processFileUrlByType.get("VULCANUS")).orElseThrow(() -> new CseAdapterException("VULCANUS type not found")),
            Optional.ofNullable(processFileUrlByType.get("NTC")).orElseThrow(() -> new CseAdapterException("NTC type not found")),
            50,
            650,
            null
        );
    }

    CseRequest getD2ccRequest(TaskDto taskDto) {
        Map<String, String> processFileUrlByType = taskDto.getProcessFiles().stream()
            .collect(Collectors.toMap(
                ProcessFileDto::getFileType,
                ProcessFileDto::getFileUrl
            ));
        return CseRequest.d2ccProcess(
            taskDto.getId().toString(),
            taskDto.getTimestamp().atZone(ZoneId.of("UTC")).toOffsetDateTime(),
            Optional.ofNullable(processFileUrlByType.get("CGM")).orElseThrow(() -> new CseAdapterException("CGM type not found")),
            Optional.ofNullable(processFileUrlByType.get("CRAC")).orElseThrow(() -> new CseAdapterException("CRAC type not found")),
            Optional.ofNullable(processFileUrlByType.get("GLSK")).orElseThrow(() -> new CseAdapterException("GLSK type not found")),
            Optional.ofNullable(processFileUrlByType.get("NTC-RED")).orElseThrow(() -> new CseAdapterException("NTC-RED type not found")),
            Optional.ofNullable(processFileUrlByType.get("TARGET-CH")).orElseThrow(() -> new CseAdapterException("TARGET-CH type not found")),
            Optional.ofNullable(processFileUrlByType.get("VULCANUS")).orElseThrow(() -> new CseAdapterException("VULCANUS type not found")),
            Optional.ofNullable(processFileUrlByType.get("NTC")).orElseThrow(() -> new CseAdapterException("NTC type not found")),
            50,
            650,
            null
        );
    }
}
