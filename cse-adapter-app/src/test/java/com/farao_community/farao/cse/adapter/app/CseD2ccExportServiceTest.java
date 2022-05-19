package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.cse.runner.api.resource.CseExportRequest;
import com.farao_community.farao.cse.runner.api.resource.ProcessType;
import com.farao_community.farao.gridcapa.task_manager.api.ProcessFileDto;
import com.farao_community.farao.gridcapa.task_manager.api.ProcessFileStatus;
import com.farao_community.farao.gridcapa.task_manager.api.TaskDto;
import com.farao_community.farao.gridcapa.task_manager.api.TaskStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@SpringBootTest
@ActiveProfiles("d2cc-export")
class CseD2ccExportServiceTest {

    @SpyBean
    private CseD2ccExportService cseD2ccExportService;

    private TaskDto getD2ccTaskDto() {
        UUID id = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.parse("2021-12-07T14:30Z");
        List<ProcessFileDto> processFiles = new ArrayList<>();
        processFiles.add(new ProcessFileDto("CGM", ProcessFileStatus.VALIDATED, "cgm", timestamp, "file://cgm.uct"));
        processFiles.add(new ProcessFileDto("CRAC", ProcessFileStatus.VALIDATED, "crac", timestamp, "file://crac.json"));
        return new TaskDto(id, timestamp, TaskStatus.READY, processFiles, Collections.emptyList());
    }

    @Test
    void testAdapterWithD2ccConfig() {
        TaskDto d2ccTaskDto = getD2ccTaskDto();
        CseExportRequest cseExportRequest = Mockito.mock(CseExportRequest.class);
        Mockito.when(cseD2ccExportService.getD2ccRequest(d2ccTaskDto)).thenReturn(cseExportRequest);
        cseD2ccExportService.runAsync(d2ccTaskDto);
        assertDoesNotThrow((ThrowingSupplier<RuntimeException>) RuntimeException::new);
    }

    @Test
    void testD2ccSuccess() {
        TaskDto taskDto = getD2ccTaskDto();
        CseExportRequest cseExportRequest = cseD2ccExportService.getD2ccRequest(taskDto);
        assertEquals(ProcessType.D2CC, cseExportRequest.getProcessType());
        assertEquals("2021-12-07T14:30Z", cseExportRequest.getTargetProcessDateTime().toString());
        assertEquals(taskDto.getId().toString(), cseExportRequest.getId());
        assertEquals("file://cgm.uct", cseExportRequest.getCgmUrl());
        assertEquals("file://crac.json", cseExportRequest.getMergedCracUrl());
    }

    @Test
    void testD2ccWithMissingFileUrl() {
        UUID id = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.parse("2021-12-07T14:30Z");
        List<ProcessFileDto> processFiles = new ArrayList<>();
        processFiles.add(new ProcessFileDto("CRAC", ProcessFileStatus.VALIDATED, "crac", timestamp, "file://crac.json"));
        TaskDto taskDto = new TaskDto(id, timestamp, TaskStatus.READY, processFiles, Collections.emptyList());
        assertThrows(CseAdapterException.class, () -> cseD2ccExportService.getD2ccRequest(taskDto));
    }
}
