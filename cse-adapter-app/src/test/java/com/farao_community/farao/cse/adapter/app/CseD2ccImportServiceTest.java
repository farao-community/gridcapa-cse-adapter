package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.cse.runner.api.resource.CseRequest;
import com.farao_community.farao.cse.runner.api.resource.ProcessType;
import com.farao_community.farao.gridcapa.task_manager.api.ProcessFileDto;
import com.farao_community.farao.gridcapa.task_manager.api.ProcessFileStatus;
import com.farao_community.farao.gridcapa.task_manager.api.TaskDto;
import com.farao_community.farao.gridcapa.task_manager.api.TaskStatus;
import com.farao_community.farao.minio_adapter.starter.MinioAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@SpringBootTest
@ActiveProfiles("d2cc-import")
class CseD2ccImportServiceTest {

    @SpyBean
    private CseD2ccImportService cseD2ccImportService;

    @MockBean
    private MinioAdapter minioAdapter;

    @BeforeEach
    void setUp() {
        Mockito.doAnswer(a -> null).when(cseD2ccImportService).uploadTargetChFile(any());
        Mockito.when(minioAdapter.generatePreSignedUrl(any())).thenReturn("file://target-ch.xml");
    }

    private TaskDto getD2ccTaskDto(String userConfigFile) {
        UUID id = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.parse("2021-12-07T14:30Z");
        List<ProcessFileDto> processFiles = new ArrayList<>();
        processFiles.add(new ProcessFileDto("CGM", ProcessFileStatus.VALIDATED, "cgm", timestamp, "file://cgm.uct"));
        processFiles.add(new ProcessFileDto("CRAC", ProcessFileStatus.VALIDATED, "crac", timestamp, "file://crac.json"));
        processFiles.add(new ProcessFileDto("GLSK", ProcessFileStatus.VALIDATED, "glsk", timestamp, "file://glsk.xml"));
        processFiles.add(new ProcessFileDto("NTC", ProcessFileStatus.VALIDATED, "ntc", timestamp, "file://ntc.xml"));
        processFiles.add(new ProcessFileDto("NTC-RED", ProcessFileStatus.VALIDATED, "ntc-red", timestamp, "file://ntc-red.xml"));
        processFiles.add(new ProcessFileDto("USER-CONFIG", ProcessFileStatus.VALIDATED, "user-config",
            timestamp, ClassLoader.getSystemResource(userConfigFile).toString()));
        return new TaskDto(id, timestamp, TaskStatus.READY, processFiles, Collections.emptyList());
    }

    @Test
    void testAdapterWithD2ccConfig() {
        TaskDto d2ccTaskDto = getD2ccTaskDto("forcedPras.json");
        CseRequest d2ccCseRequest = Mockito.mock(CseRequest.class);
        Mockito.doReturn(d2ccCseRequest).when(cseD2ccImportService).getD2ccRequest(d2ccTaskDto);
        cseD2ccImportService.runAsync(d2ccTaskDto);
        assertDoesNotThrow((ThrowingSupplier<RuntimeException>) RuntimeException::new);
    }

    @Test
    void testD2ccSuccess() {
        TaskDto taskDto = getD2ccTaskDto("forcedPras.json");

        CseRequest cseRequest = cseD2ccImportService.getD2ccRequest(taskDto);
        assertEquals(ProcessType.D2CC, cseRequest.getProcessType());
        assertEquals("2021-12-07T14:30Z", cseRequest.getTargetProcessDateTime().toString());
        assertEquals(taskDto.getId().toString(), cseRequest.getId());
        assertEquals("file://cgm.uct", cseRequest.getCgmUrl());
        assertEquals("file://crac.json", cseRequest.getMergedCracUrl());
        assertEquals("file://glsk.xml", cseRequest.getMergedGlskUrl());
        assertEquals("file://ntc.xml", cseRequest.getYearlyNtcUrl());
        assertEquals("file://ntc-red.xml", cseRequest.getNtcReductionsUrl());
        assertEquals("file://target-ch.xml", cseRequest.getTargetChUrl());
        assertEquals(2, cseRequest.getForcedPrasIds().size());
        assertEquals(50, cseRequest.getDichotomyPrecision());
        assertEquals(650, cseRequest.getInitialDichotomyStep());
        assertEquals(10, cseRequest.getInitialDichotomyIndex());
    }

    @Test
    void testD2ccWithMissingFileUrl() {
        UUID id = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.parse("2021-12-07T14:30Z");
        List<ProcessFileDto> processFiles = new ArrayList<>();
        processFiles.add(new ProcessFileDto("CRAC", ProcessFileStatus.VALIDATED, "crac", timestamp, "file://crac.json"));
        processFiles.add(new ProcessFileDto("GLSK", ProcessFileStatus.VALIDATED, "glsk", timestamp, "file://glsk.xml"));
        processFiles.add(new ProcessFileDto("NTC", ProcessFileStatus.VALIDATED, "ntc", timestamp, "file://ntc.xml"));
        processFiles.add(new ProcessFileDto("NTC-RED", ProcessFileStatus.VALIDATED, "ntc-red", timestamp, "file://ntc-red.xml"));
        processFiles.add(new ProcessFileDto("TARGET-CH", ProcessFileStatus.VALIDATED, "target-ch", timestamp, "file://target-ch.xml"));
        TaskDto taskDto = new TaskDto(id, timestamp, TaskStatus.READY, processFiles, Collections.emptyList());

        assertThrows(CseAdapterException.class, () -> cseD2ccImportService.getD2ccRequest(taskDto));
    }

    @Test
    void testD2ccSuccessWithNullUserConfiguration() {
        TaskDto taskDto = getD2ccTaskDto("forcedPras-null.json");

        CseRequest cseRequest = cseD2ccImportService.getD2ccRequest(taskDto);
        assertEquals(0, cseRequest.getForcedPrasIds().size());
        assertNull(cseRequest.getInitialDichotomyIndex());
    }
}
