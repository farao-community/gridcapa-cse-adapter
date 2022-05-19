package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.cse.runner.api.resource.CseRequest;
import com.farao_community.farao.cse.runner.api.resource.CseResponse;
import com.farao_community.farao.cse.runner.starter.CseClient;
import com.farao_community.farao.gridcapa.task_manager.api.TaskDto;
import com.farao_community.farao.minio_adapter.starter.MinioAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
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
@ConditionalOnExpression("'${cse-adapter.process-type}'.equalsIgnoreCase('D2CC') and '${cse-adapter.exchange-type}'.equalsIgnoreCase('import')")
public class CseD2ccImportService implements CseAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CseD2ccImportService.class);

    private final CseClient cseClient;
    private final FileImporter fileImporter;
    private final MinioAdapter minioAdapter;

    @Value("${cse-adapter.target-ch-fs-location}")
    private String targetChFsPath;

    @Value("${cse-adapter.target-ch-minio-location}")
    private String targetChMinioPath;

    public CseD2ccImportService(CseClient cseClient, FileImporter fileImporter, MinioAdapter minioAdapter) {
        this.cseClient = cseClient;
        this.fileImporter = fileImporter;
        this.minioAdapter = minioAdapter;
    }

    @Override
    public void runAsync(TaskDto taskDto) {
        LOGGER.info("Sending import IDCC request for TS: {}", taskDto.getTimestamp());
        CseRequest cseRequest = getD2ccRequest(taskDto);
        CompletableFuture.runAsync(() -> cseClient.run(cseRequest, CseRequest.class, CseResponse.class));
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
            minioAdapter.generatePreSignedUrl(targetChMinioPath),
            Optional.ofNullable(processFileUrlByType.get("NTC")).orElseThrow(() -> new CseAdapterException("NTC type not found")),
            userConfigurationWrapper.forcedPrasIds,
            50,
            650,
            userConfigurationWrapper.initialDichotomyIndex
        );
    }

    void uploadTargetChFile(OffsetDateTime timestamp) {
        try (InputStream is = new FileInputStream(targetChFsPath)) {
            minioAdapter.uploadArtifactForTimestamp(
                targetChMinioPath,
                is,
                "CSE_D2CC",
                "TARGET_CH",
                timestamp);
        } catch (IOException e) {
            throw new CseAdapterException("Impossible to find Target CH file");
        }
    }
}
