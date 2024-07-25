/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.cse.runner.api.resource.CseExportRequest;
import com.farao_community.farao.cse.runner.api.resource.ProcessType;
import com.farao_community.farao.cse.runner.starter.CseClient;
import com.farao_community.farao.gridcapa.task_manager.api.TaskDto;
import com.farao_community.farao.gridcapa.task_manager.api.TaskStatus;
import com.farao_community.farao.gridcapa.task_manager.api.TaskStatusUpdate;
import com.farao_community.farao.minio_adapter.starter.MinioAdapter;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@Service
@ConditionalOnBean(value = { CseExportAdapterConfiguration.class })
public class CseExportService implements CseAdapter {
    private static final String TASK_STATUS_UPDATE = "task-status-update";

    private final CseExportAdapterConfiguration configuration;
    private final CseClient cseClient;
    private final StreamBridge streamBridge;
    private final Logger businessLogger;
    private final MinioAdapter minioAdapter;

    public CseExportService(CseExportAdapterConfiguration configuration, CseClient cseClient, StreamBridge streamBridge, Logger businessLogger, MinioAdapter minioAdapter) {
        this.configuration = configuration;
        this.cseClient = cseClient;
        this.streamBridge = streamBridge;
        this.businessLogger = businessLogger;
        this.minioAdapter = minioAdapter;
    }

    @Override
    public void runAsync(TaskDto taskDto) {
        MDC.put("gridcapa-task-id", taskDto.getId().toString());
        CseExportRequest cseExportRequest;
        try {
            switch (configuration.getProcessType()) {
                case IDCC:
                    businessLogger.info("Sending export IDCC request for TS: {}", taskDto.getTimestamp());
                    cseExportRequest = getIdccRequest(taskDto);
                    break;
                case D2CC:
                    businessLogger.info("Sending export D2CC request for TS: {}", taskDto.getTimestamp());
                    cseExportRequest = getD2ccRequest(taskDto);
                    break;
                default:
                    throw new NotImplementedException(String.format("Unknown target process for CSE: %s", configuration.getProcessType()));
            }
        } catch (Exception e) {
            businessLogger.error(String.format("Unexpected error occurred during building the request, task %s will not be run. Reason: %s", taskDto.getId().toString(), e.getMessage()));
            streamBridge.send(TASK_STATUS_UPDATE, new TaskStatusUpdate(taskDto.getId(), TaskStatus.ERROR));
            return;
        }
        CompletableFuture.runAsync(() -> cseClient.run(cseExportRequest, CseExportRequest.class));
    }

    CseExportRequest getIdccRequest(TaskDto taskDto) {
        return getRequest(taskDto, ProcessType.IDCC);
    }

    CseExportRequest getD2ccRequest(TaskDto taskDto) {
        return getRequest(taskDto, ProcessType.D2CC);
    }

    private CseExportRequest getRequest(TaskDto taskDto, ProcessType processType) {
        Map<String, String> processFileUrlByType = taskDto.getInputs().stream()
                .filter(f -> f.getFilePath() != null)
            .collect(HashMap::new, (m, v) -> m.put(v.getFileType(), minioAdapter.generatePreSignedUrlFromFullMinioPath(v.getFilePath(), 1)), HashMap::putAll);

        return new CseExportRequest(
            taskDto.getId().toString(),
            CseAdapter.getCurrentRunId(taskDto),
            taskDto.getTimestamp(),
            processType,
            Optional.ofNullable(processFileUrlByType.get("CGM")).orElseThrow(() -> new CseAdapterException("CGM type not found")),
            Optional.ofNullable(processFileUrlByType.get("CRAC")).orElseThrow(() -> new CseAdapterException("CRAC type not found")));
    }
}
