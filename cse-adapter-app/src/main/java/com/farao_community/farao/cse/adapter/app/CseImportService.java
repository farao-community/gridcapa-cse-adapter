/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.cse.adapter.app.loader.AutomatedForcedPrasLoader;
import com.farao_community.farao.cse.adapter.app.loader.UserConfigurationLoader;
import com.farao_community.farao.cse.runner.api.resource.CseRequest;
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
import java.util.concurrent.CompletableFuture;

import static com.farao_community.farao.cse.adapter.app.FileType.CGM;
import static com.farao_community.farao.cse.adapter.app.FileType.CRAC;
import static com.farao_community.farao.cse.adapter.app.FileType.GLSK;
import static com.farao_community.farao.cse.adapter.app.FileType.NTC;
import static com.farao_community.farao.cse.adapter.app.FileType.NTC2_AT;
import static com.farao_community.farao.cse.adapter.app.FileType.NTC2_CH;
import static com.farao_community.farao.cse.adapter.app.FileType.NTC2_FR;
import static com.farao_community.farao.cse.adapter.app.FileType.NTC2_SI;
import static com.farao_community.farao.cse.adapter.app.FileType.NTC_RED;
import static com.farao_community.farao.cse.adapter.app.FileType.TARGET_CH;
import static com.farao_community.farao.cse.adapter.app.FileType.VULCANUS;
import static com.farao_community.farao.cse.adapter.app.GetUrlUtil.getUrlOrEmpty;
import static com.farao_community.farao.cse.adapter.app.GetUrlUtil.getUrlOrThrow;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@Service
@ConditionalOnBean(value = {CseImportAdapterConfiguration.class})
public class CseImportService implements CseAdapter {
    private static final String TASK_STATUS_UPDATE = "task-status-update";

    private final CseImportAdapterConfiguration configuration;
    private final CseClient cseClient;
    private final FileImporter fileImporter;
    private final StreamBridge streamBridge;
    private final Logger businessLogger;
    private final MinioAdapter minioAdapter;

    public CseImportService(CseImportAdapterConfiguration configuration, CseClient cseClient, FileImporter fileImporter, StreamBridge streamBridge, Logger businessLogger, MinioAdapter minioAdapter) {
        this.configuration = configuration;
        this.cseClient = cseClient;
        this.fileImporter = fileImporter;
        this.streamBridge = streamBridge;
        this.businessLogger = businessLogger;
        this.minioAdapter = minioAdapter;
    }

    @Override
    public void runAsync(TaskDto taskDto) {
        MDC.put("gridcapa-task-id", taskDto.getId().toString());
        CseRequest cseRequest;
        try {
            switch (configuration.getProcessType()) {
                case IDCC:
                    businessLogger.info("Sending import IDCC request for TS: {}", taskDto.getTimestamp());
                    cseRequest = getIdccRequest(taskDto);
                    break;
                case D2CC:
                    businessLogger.info("Sending import D2CC request for TS: {}", taskDto.getTimestamp());
                    cseRequest = getD2ccRequest(taskDto);
                    break;
                default:
                    throw new NotImplementedException(String.format("Unknown target process for CSE: %s", configuration.getProcessType()));
            }
        } catch (Exception e) {
            businessLogger.error(String.format("Unexpected error occurred during building the request, task %s will not be run. Reason: %s", taskDto.getId().toString(), e.getMessage()));
            streamBridge.send(TASK_STATUS_UPDATE, new TaskStatusUpdate(taskDto.getId(), TaskStatus.ERROR));
            return;
        }
        CompletableFuture.runAsync(() -> cseClient.run(cseRequest, CseRequest.class));
    }

    CseRequest getIdccRequest(TaskDto taskDto) {
        Map<String, String> processFileUrlByType = getUrls(taskDto);
        UserConfigurationLoader userConfigurationLoader = new UserConfigurationLoader(fileImporter, processFileUrlByType.get("USER-CONFIG"));
        AutomatedForcedPrasLoader automatedForcedPrasLoader = new AutomatedForcedPrasLoader(fileImporter, processFileUrlByType.get("AUTOMATED-FORCED-PRAS"));

        return CseRequest.idccProcess(
                taskDto.getId().toString(),
                CseAdapter.getCurrentRunId(taskDto),
                taskDto.getTimestamp(),
                getUrlOrThrow(processFileUrlByType, CGM),
                getUrlOrThrow(processFileUrlByType, CRAC),
                getUrlOrThrow(processFileUrlByType, GLSK),
                getUrlOrEmpty(processFileUrlByType, NTC_RED),
                getUrlOrEmpty(processFileUrlByType, NTC2_AT),
                getUrlOrEmpty(processFileUrlByType, NTC2_CH),
                getUrlOrEmpty(processFileUrlByType, NTC2_FR),
                getUrlOrEmpty(processFileUrlByType, NTC2_SI),
                getUrlOrThrow(processFileUrlByType, VULCANUS),
                getUrlOrThrow(processFileUrlByType, NTC),
                userConfigurationLoader.manualForcedPrasIds,
                automatedForcedPrasLoader.automatedForcedPrasIds,
                userConfigurationLoader.maximumDichotomiesNumber,
                100,
                650,
                userConfigurationLoader.initialDichotomyIndex,
                configuration.isEcImport()
        );
    }

    CseRequest getD2ccRequest(TaskDto taskDto) {
        Map<String, String> processFileUrlByType = getUrls(taskDto);
        UserConfigurationLoader userConfigurationWrapper = new UserConfigurationLoader(fileImporter, processFileUrlByType.get("USER-CONFIG"));
        AutomatedForcedPrasLoader automatedForcedPrasLoader = new AutomatedForcedPrasLoader(fileImporter, processFileUrlByType.get("AUTOMATED-FORCED-PRAS"));

        return CseRequest.d2ccProcess(
                taskDto.getId().toString(),
                CseAdapter.getCurrentRunId(taskDto),
                taskDto.getTimestamp(),
                getUrlOrThrow(processFileUrlByType, CGM),
                getUrlOrThrow(processFileUrlByType, CRAC),
                getUrlOrThrow(processFileUrlByType, GLSK),
                configuration.isEcImport() ? getUrlOrThrow(processFileUrlByType, NTC_RED) : getUrlOrEmpty(processFileUrlByType, NTC_RED),
                getUrlOrThrow(processFileUrlByType, TARGET_CH),
                getUrlOrThrow(processFileUrlByType, NTC),
                getUrlOrThrow(processFileUrlByType, VULCANUS),
                userConfigurationWrapper.manualForcedPrasIds,
                automatedForcedPrasLoader.automatedForcedPrasIds,
                userConfigurationWrapper.maximumDichotomiesNumber,
                100,
                650,
                userConfigurationWrapper.initialDichotomyIndex,
                configuration.isEcImport()
        );
    }

    private Map<String, String> getUrls(TaskDto taskDto) {
        return taskDto.getInputs().stream()
                .filter(f -> f.getFilePath() != null)
                .collect(HashMap::new, (m, v) -> m.put(v.getFileType(), minioAdapter.generatePreSignedUrlFromFullMinioPath(v.getFilePath(), 1)), HashMap::putAll);
    }
}
