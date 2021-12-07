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
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@Component
public class CseAdapterListener {

    private final CseAdapterConfiguration cseAdapterConfiguration;

    public CseAdapterListener(CseAdapterConfiguration cseAdapterConfiguration) {
        this.cseAdapterConfiguration = cseAdapterConfiguration;
    }

    @Bean
    public Function<TaskDto, CseRequest> handleRun() {
        return taskDto -> {
            switch (cseAdapterConfiguration.getTargetProcess()) {
                case "IDCC":
                    return getIdccRequest(taskDto);
                case "D2CC":
                    return getD2ccRequest(taskDto);
                default:
                    throw new NotImplementedException(String.format("Unknown target process for CSE: %s", cseAdapterConfiguration.getTargetProcess()));
            }
        };
    }

    CseRequest getIdccRequest(TaskDto taskDto) {
        String cgmUrl = null;
        String cracUrl = null;
        String glskUrl = null;
        String yearlyNtcUrl = null;
        String dailyNtcUrl = null;
        String atItNtc2Url = null;
        String frItNtc2Url = null;
        String chItNtc2Url = null;
        String siItNtc2Url = null;
        String vulcanusUrl = null;
        for (ProcessFileDto processFileDto : taskDto.getProcessFiles()) {
            switch (processFileDto.getFileType()) {
                case "CGM":
                    cgmUrl = processFileDto.getFileUrl();
                    break;
                case "CRAC":
                    cracUrl = processFileDto.getFileUrl();
                    break;
                case "GLSK":
                    glskUrl = processFileDto.getFileUrl();
                    break;
                case "NTC":
                    yearlyNtcUrl = processFileDto.getFileUrl();
                    break;
                case "NTC-RED":
                    dailyNtcUrl = processFileDto.getFileUrl();
                    break;
                case "VULCANUS":
                    vulcanusUrl = processFileDto.getFileUrl();
                    break;
                case "AT-NTC2":
                    atItNtc2Url = processFileDto.getFileUrl();
                    break;
                case "FR-NTC2":
                    frItNtc2Url = processFileDto.getFileUrl();
                    break;
                case "CH-NTC2":
                    chItNtc2Url = processFileDto.getFileUrl();
                    break;
                case "SI-NTC2":
                    siItNtc2Url = processFileDto.getFileUrl();
                    break;
                default:
                    throw new NotImplementedException(String.format("File type is not handled for IDCC: %s", processFileDto.getFileType()));
            }
        }
        return CseRequest.idccProcess(
            taskDto.getId().toString(),
            taskDto.getTimestamp().atZone(ZoneId.of("UTC")).toOffsetDateTime(),
            Objects.requireNonNull(cgmUrl),
            Objects.requireNonNull(cracUrl),
            Objects.requireNonNull(glskUrl),
            Objects.requireNonNull(dailyNtcUrl),
            Objects.requireNonNull(atItNtc2Url),
            Objects.requireNonNull(chItNtc2Url),
            Objects.requireNonNull(frItNtc2Url),
            Objects.requireNonNull(siItNtc2Url),
            Objects.requireNonNull(vulcanusUrl),
            Objects.requireNonNull(yearlyNtcUrl),
            50,
            650,
            null
        );
    }

    CseRequest getD2ccRequest(TaskDto taskDto) {
        String cgmUrl = null;
        String cracUrl = null;
        String glskUrl = null;
        String yearlyNtcUrl = null;
        String dailyNtcUrl = null;
        String targetChUrl = null;
        String vulcanusUrl = null;
        for (ProcessFileDto processFileDto : taskDto.getProcessFiles()) {
            switch (processFileDto.getFileType()) {
                case "CGM":
                    cgmUrl = processFileDto.getFileUrl();
                    break;
                case "CRAC":
                    cracUrl = processFileDto.getFileUrl();
                    break;
                case "GLSK":
                    glskUrl = processFileDto.getFileUrl();
                    break;
                case "NTC":
                    yearlyNtcUrl = processFileDto.getFileUrl();
                    break;
                case "NTC-RED":
                    dailyNtcUrl = processFileDto.getFileUrl();
                    break;
                case "VULCANUS":
                    vulcanusUrl = processFileDto.getFileUrl();
                    break;
                case "TARGET-CH":
                    targetChUrl = processFileDto.getFileUrl();
                    break;
                default:
                    throw new NotImplementedException(String.format("File type is not handled for IDCC: %s", processFileDto.getFileType()));
            }
        }
        return CseRequest.d2ccProcess(
            taskDto.getId().toString(),
            taskDto.getTimestamp().atZone(ZoneId.of("UTC")).toOffsetDateTime(),
            Objects.requireNonNull(cgmUrl),
            Objects.requireNonNull(cracUrl),
            Objects.requireNonNull(glskUrl),
            Objects.requireNonNull(dailyNtcUrl),
            Objects.requireNonNull(targetChUrl),
            Objects.requireNonNull(vulcanusUrl),
            Objects.requireNonNull(yearlyNtcUrl),
            50,
            650,
            null
        );
    }
}
