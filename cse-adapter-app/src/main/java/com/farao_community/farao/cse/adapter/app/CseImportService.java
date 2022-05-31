/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.cse.runner.api.resource.CseRequest;
import com.farao_community.farao.cse.runner.api.resource.CseResponse;
import com.farao_community.farao.cse.runner.starter.CseClient;
import com.farao_community.farao.gridcapa.task_manager.api.TaskDto;
import com.farao_community.farao.minio_adapter.starter.MinioAdapter;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@Service
@ConditionalOnBean(value = { CseImportAdapterConfiguration.class })
public class CseImportService implements CseAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CseImportService.class);

    private final CseImportAdapterConfiguration configuration;
    private final CseClient cseClient;
    private final FileImporter fileImporter;
    private final MinioAdapter minioAdapter;

    public CseImportService(CseImportAdapterConfiguration configuration, CseClient cseClient, FileImporter fileImporter, MinioAdapter minioAdapter) {
        this.configuration = configuration;
        this.cseClient = cseClient;
        this.fileImporter = fileImporter;
        this.minioAdapter = minioAdapter;
    }

    @Override
    public void runAsync(TaskDto taskDto) {
        CseRequest cseRequest;
        switch (configuration.getProcessType()) {
            case IDCC:
                LOGGER.info("Sending import IDCC request for TS: {}", taskDto.getTimestamp());
                cseRequest = getIdccRequest(taskDto);
                break;
            case D2CC:
                LOGGER.info("Sending import D2CC request for TS: {}", taskDto.getTimestamp());
                cseRequest = getD2ccRequest(taskDto);
                break;
            default:
                throw new NotImplementedException(String.format("Unknown target process for CSE: %s", configuration.getProcessType()));
        }
        CompletableFuture.runAsync(() -> cseClient.run(cseRequest, CseRequest.class, CseResponse.class));
    }

    CseRequest getIdccRequest(TaskDto taskDto) {
        Map<String, String> processFileUrlByType = taskDto.getProcessFiles().stream()
            .collect(HashMap::new, (m, v) -> m.put(v.getFileType(), v.getFileUrl()), HashMap::putAll);

        UserConfigurationLoader userConfigurationLoader = new UserConfigurationLoader(fileImporter, processFileUrlByType.get("USER-CONFIG"));

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
            userConfigurationLoader.forcedPrasIds,
            50,
            650,
            userConfigurationLoader.initialDichotomyIndex
        );
    }

    CseRequest getD2ccRequest(TaskDto taskDto) {
        Map<String, String> processFileUrlByType = taskDto.getProcessFiles().stream()
            .collect(HashMap::new, (m, v) -> m.put(v.getFileType(), v.getFileUrl()), HashMap::putAll);

        uploadTargetChFile(taskDto.getTimestamp());
        UserConfigurationLoader userConfigurationWrapper = new UserConfigurationLoader(fileImporter, processFileUrlByType.get("USER-CONFIG"));

        return CseRequest.d2ccProcess(
            taskDto.getId().toString(),
            taskDto.getTimestamp(),
            Optional.ofNullable(processFileUrlByType.get("CGM")).orElseThrow(() -> new CseAdapterException("CGM type not found")),
            Optional.ofNullable(processFileUrlByType.get("CRAC")).orElseThrow(() -> new CseAdapterException("CRAC type not found")),
            Optional.ofNullable(processFileUrlByType.get("GLSK")).orElseThrow(() -> new CseAdapterException("GLSK type not found")),
            Optional.ofNullable(processFileUrlByType.get("NTC-RED")).orElseThrow(() -> new CseAdapterException("NTC-RED type not found")),
            minioAdapter.generatePreSignedUrl(configuration.getTargetChMinioPath()),
            Optional.ofNullable(processFileUrlByType.get("NTC")).orElseThrow(() -> new CseAdapterException("NTC type not found")),
            userConfigurationWrapper.forcedPrasIds,
            50,
            650,
            userConfigurationWrapper.initialDichotomyIndex
        );
    }

    void uploadTargetChFile(OffsetDateTime timestamp) {
        try (InputStream is = new FileInputStream(configuration.getTargetChFsPath())) {
            minioAdapter.uploadArtifactForTimestamp(
                configuration.getTargetChMinioPath(),
                is,
                "CSE_D2CC",
                "TARGET_CH",
                timestamp);
        } catch (IOException e) {
            throw new CseAdapterException("Impossible to find Target CH file");
        }
    }
}