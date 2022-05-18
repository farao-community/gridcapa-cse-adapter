package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.cse.runner.api.resource.CseExportRequest;
import com.farao_community.farao.cse.runner.api.resource.CseExportResponse;
import com.farao_community.farao.cse.runner.api.resource.ProcessType;
import com.farao_community.farao.cse.runner.starter.CseClient;
import com.farao_community.farao.gridcapa.task_manager.api.TaskDto;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(CseExportService.class);

    private final CseExportAdapterConfiguration configuration;
    private final CseClient cseClient;

    public CseExportService(CseExportAdapterConfiguration configuration, CseClient cseClient) {
        this.configuration = configuration;
        this.cseClient = cseClient;
    }

    @Override
    public void runAsync(TaskDto taskDto) {
        CseExportRequest cseExportRequest;
        switch (configuration.getTargetProcess()) {
            case IDCC:
                LOGGER.info("Sending export IDCC request for TS: {}", taskDto.getTimestamp());
                cseExportRequest = getIdccRequest(taskDto);
                break;
            case D2CC:
                LOGGER.info("Sending export D2CC request for TS: {}", taskDto.getTimestamp());
                cseExportRequest = getD2ccRequest(taskDto);
                break;
            default:
                throw new NotImplementedException(String.format("Unknown target process for CSE: %s", configuration.getTargetProcess()));
        }
        CompletableFuture.runAsync(() -> cseClient.run(cseExportRequest, CseExportRequest.class, CseExportResponse.class));
    }

    CseExportRequest getIdccRequest(TaskDto taskDto) {
        return gettRequest(taskDto, ProcessType.IDCC);
    }

    CseExportRequest getD2ccRequest(TaskDto taskDto) {
        return gettRequest(taskDto, ProcessType.D2CC);
    }

    private CseExportRequest gettRequest(TaskDto taskDto, ProcessType processType) {
        Map<String, String> processFileUrlByType = taskDto.getProcessFiles().stream()
            .collect(HashMap::new, (m, v) -> m.put(v.getFileType(), v.getFileUrl()), HashMap::putAll);

        return new CseExportRequest(
            taskDto.getId().toString(),
            taskDto.getTimestamp(),
            processType,
            Optional.ofNullable(processFileUrlByType.get("CGM")).orElseThrow(() -> new CseAdapterException("CGM type not found")),
            Optional.ofNullable(processFileUrlByType.get("CRAC")).orElseThrow(() -> new CseAdapterException("CRAC type not found")));
    }
}
