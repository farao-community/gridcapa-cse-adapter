package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.cse.runner.api.resource.CseRequest;
import com.farao_community.farao.cse.runner.api.resource.CseResponse;
import com.farao_community.farao.cse.runner.starter.CseClient;
import com.farao_community.farao.gridcapa.task_manager.api.TaskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@Service
@ConditionalOnExpression("'${cse-adapter.process-type}'.equalsIgnoreCase('IDCC') and '${cse-adapter.exchange-type}'.equalsIgnoreCase('import')")
public class CseIdccImportService implements CseAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CseIdccImportService.class);

    private final CseClient cseClient;
    private final FileImporter fileImporter;

    public CseIdccImportService(CseClient cseClient, FileImporter fileImporter) {
        this.cseClient = cseClient;
        this.fileImporter = fileImporter;
    }

    @Override
    public void runAsync(TaskDto taskDto) {
        LOGGER.info("Sending import IDCC request for TS: {}", taskDto.getTimestamp());
        CseRequest cseRequest = getIdccRequest(taskDto);
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

}
