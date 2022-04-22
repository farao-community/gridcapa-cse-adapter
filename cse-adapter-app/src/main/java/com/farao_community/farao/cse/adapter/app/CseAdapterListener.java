/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.cse.runner.api.resource.CseRequest;
import com.farao_community.farao.cse.runner.starter.CseClient;
import com.farao_community.farao.gridcapa.task_manager.api.ProcessFileDto;
import com.farao_community.farao.gridcapa.task_manager.api.TaskDto;
import com.farao_community.farao.minio_adapter.starter.MinioAdapter;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@Component
public class CseAdapterListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(CseAdapterListener.class);

    private final CseClient cseClient;
    private final CseAdapterConfiguration cseAdapterConfiguration;
    private final MinioAdapter minioAdapter;

    public CseAdapterListener(CseClient cseClient, CseAdapterConfiguration cseAdapterConfiguration, MinioAdapter minioAdapter) {
        this.cseClient = cseClient;
        this.cseAdapterConfiguration = cseAdapterConfiguration;
        this.minioAdapter = minioAdapter;
    }

    @Bean
    public Consumer<Flux<TaskDto>> handleRun() {
        return f -> f
            .onErrorContinue((t, r) -> LOGGER.error(t.getMessage(), t))
            .subscribe(this::runRequest);
    }

    void runRequest(TaskDto taskDto) {
        switch (cseAdapterConfiguration.getTargetProcess()) {
            case "IDCC":
                LOGGER.info("Sending IDCC request for TS: {}", taskDto.getTimestamp());
                CompletableFuture.runAsync(() -> cseClient.run(getIdccRequest(taskDto)));
                break;
            case "D2CC":
                LOGGER.info("Sending D2CC request for TS: {}", taskDto.getTimestamp());
                CompletableFuture.runAsync(() -> cseClient.run(getD2ccRequest(taskDto)));
                break;
            default:
                throw new NotImplementedException(String.format("Unknown target process for CSE: %s", cseAdapterConfiguration.getTargetProcess()));
        }
    }

    CseRequest getIdccRequest(TaskDto taskDto) {
        Map<String, String> processFileUrlByType = taskDto.getProcessFiles().stream()
            .collect(Collectors.toMap(
                ProcessFileDto::getFileType,
                ProcessFileDto::getFileUrl
            ));
        return CseRequest.idccProcess(
            taskDto.getId().toString(),
            taskDto.getTimestamp(),
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

        uploadTargetChFile(taskDto.getTimestamp());

        return CseRequest.d2ccProcess(
            taskDto.getId().toString(),
            taskDto.getTimestamp(),
            Optional.ofNullable(processFileUrlByType.get("CGM")).orElseThrow(() -> new CseAdapterException("CGM type not found")),
            Optional.ofNullable(processFileUrlByType.get("CRAC")).orElseThrow(() -> new CseAdapterException("CRAC type not found")),
            Optional.ofNullable(processFileUrlByType.get("GLSK")).orElseThrow(() -> new CseAdapterException("GLSK type not found")),
            Optional.ofNullable(processFileUrlByType.get("NTC-RED")).orElseThrow(() -> new CseAdapterException("NTC-RED type not found")),
            minioAdapter.generatePreSignedUrl(cseAdapterConfiguration.getTargetChMinioPath()),
            Optional.ofNullable(processFileUrlByType.get("NTC")).orElseThrow(() -> new CseAdapterException("NTC type not found")),
            50,
            650,
            null
        );
    }

    void uploadTargetChFile(OffsetDateTime timestamp) {
        try (InputStream is = new FileInputStream(cseAdapterConfiguration.getTargetChFsPath())) {
            minioAdapter.uploadInputForTimestamp(
                cseAdapterConfiguration.getTargetChMinioPath(),
                is,
                "CSE_D2CC",
                "TARGET_CH",
                timestamp);
        } catch (IOException e) {
            throw new CseAdapterException("Impossible to find Target CH file");
        }
    }
}
