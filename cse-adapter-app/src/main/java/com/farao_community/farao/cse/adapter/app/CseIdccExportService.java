package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.cse.runner.api.resource.CseExportRequest;
import com.farao_community.farao.cse.runner.api.resource.CseExportResponse;
import com.farao_community.farao.cse.runner.api.resource.ProcessType;
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
@ConditionalOnExpression("'${cse-adapter.process-type}'.equalsIgnoreCase('IDCC') and '${cse-adapter.exchange-type}'.equalsIgnoreCase('export')")
public class CseIdccExportService implements CseAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CseIdccExportService.class);

    private final CseClient cseClient;

    public CseIdccExportService(CseClient cseClient) {
        this.cseClient = cseClient;
    }

    @Override
    public void runAsync(TaskDto taskDto) {
        LOGGER.info("Sending export IDCC request for TS: {}", taskDto.getTimestamp());
        CseExportRequest cseExportRequest = getIdccRequest(taskDto);
        CompletableFuture.runAsync(() -> cseClient.run(cseExportRequest, CseExportRequest.class, CseExportResponse.class));
    }

    CseExportRequest getIdccRequest(TaskDto taskDto) {
        return gettRequest(taskDto);
    }

    private CseExportRequest gettRequest(TaskDto taskDto) {
        Map<String, String> processFileUrlByType = taskDto.getProcessFiles().stream()
            .collect(HashMap::new, (m, v) -> m.put(v.getFileType(), v.getFileUrl()), HashMap::putAll);
        return new CseExportRequest(
            taskDto.getId().toString(),
            taskDto.getTimestamp(),
            ProcessType.IDCC,
            Optional.ofNullable(processFileUrlByType.get("CGM")).orElseThrow(() -> new CseAdapterException("CGM type not found")),
            Optional.ofNullable(processFileUrlByType.get("CRAC")).orElseThrow(() -> new CseAdapterException("CRAC type not found")));
    }
}
