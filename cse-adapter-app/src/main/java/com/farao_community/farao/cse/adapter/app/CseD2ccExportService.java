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
@ConditionalOnExpression("'${cse-adapter.process-type}'.equalsIgnoreCase('D2CC') and '${cse-adapter.exchange-type}'.equalsIgnoreCase('export')")
public class CseD2ccExportService implements CseAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CseD2ccExportService.class);

    private final CseClient cseClient;

    public CseD2ccExportService(CseClient cseClient) {
        this.cseClient = cseClient;
    }

    @Override
    public void runAsync(TaskDto taskDto) {
        LOGGER.info("Sending export D2CC request for TS: {}", taskDto.getTimestamp());
        CseExportRequest cseExportRequest = getD2ccRequest(taskDto);
        CompletableFuture.runAsync(() -> cseClient.run(cseExportRequest, CseExportRequest.class, CseExportResponse.class));
    }

    CseExportRequest getD2ccRequest(TaskDto taskDto) {
        return gettRequest(taskDto);
    }

    private CseExportRequest gettRequest(TaskDto taskDto) {
        Map<String, String> processFileUrlByType = taskDto.getProcessFiles().stream()
            .collect(HashMap::new, (m, v) -> m.put(v.getFileType(), v.getFileUrl()), HashMap::putAll);
        return new CseExportRequest(
            taskDto.getId().toString(),
            taskDto.getTimestamp(),
            ProcessType.D2CC,
            Optional.ofNullable(processFileUrlByType.get("CGM")).orElseThrow(() -> new CseAdapterException("CGM type not found")),
            Optional.ofNullable(processFileUrlByType.get("CRAC")).orElseThrow(() -> new CseAdapterException("CRAC type not found")));
    }
}
