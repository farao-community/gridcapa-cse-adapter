/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.cse.runner.api.resource.CseExportRequest;
import com.farao_community.farao.cse.runner.api.resource.ProcessType;
import com.farao_community.farao.gridcapa.task_manager.api.ProcessFileDto;
import com.farao_community.farao.gridcapa.task_manager.api.ProcessFileStatus;
import com.farao_community.farao.gridcapa.task_manager.api.ProcessRunDto;
import com.farao_community.farao.gridcapa.task_manager.api.TaskDto;
import com.farao_community.farao.gridcapa.task_manager.api.TaskStatus;
import com.farao_community.farao.minio_adapter.starter.MinioAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@SpringBootTest
@ActiveProfiles("d2cc-export")
class CseExportServiceTest {

    @MockitoSpyBean
    private CseExportService cseExportService;

    @MockitoBean
    private CseExportAdapterConfiguration cseExportAdapterConfiguration;

    @MockitoBean
    private MinioAdapter minioAdapter;

    private TaskDto getIdccTaskDto() {
        return getIdccTaskDto(true);
    }

    private TaskDto getIdccTaskDto(boolean withTaskHistory) {
        UUID id = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.parse("2021-12-07T14:30Z");
        List<ProcessFileDto> inputFiles = new ArrayList<>();
        inputFiles.add(new ProcessFileDto("/CGM", "CGM", ProcessFileStatus.VALIDATED, "cgm", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/CRAC", "CRAC", ProcessFileStatus.VALIDATED, "crac", "documentId", timestamp));

        List<ProcessRunDto> runHistory = Collections.emptyList();
        if (withTaskHistory) {
            runHistory = new ArrayList<>();
            runHistory.add(new ProcessRunDto(UUID.randomUUID(), OffsetDateTime.now(), Collections.emptyList()));
        }
        return new TaskDto(id, timestamp, TaskStatus.READY, inputFiles, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), runHistory, Collections.emptyList());
    }

    private TaskDto getD2ccTaskDto(String userConfigFile) {
        return getD2ccTaskDto(userConfigFile, true);
    }

    private TaskDto getD2ccTaskDto(String userConfigFile, boolean withTaskHistory) {
        UUID id = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.parse("2021-12-07T14:30Z");
        List<ProcessFileDto> inputFiles = new ArrayList<>();
        inputFiles.add(new ProcessFileDto("/CGM", "CGM", ProcessFileStatus.VALIDATED, "cgm", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/CRAC", "CRAC", ProcessFileStatus.VALIDATED, "crac", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/GLSK", "GLSK", ProcessFileStatus.VALIDATED, "glsk", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/NTC", "NTC", ProcessFileStatus.VALIDATED, "ntc", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/NTC-RED", "NTC-RED", ProcessFileStatus.VALIDATED, "ntc-red", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto(ClassLoader.getSystemResource(userConfigFile).toString(), "USER-CONFIG", ProcessFileStatus.VALIDATED, "user-config",
                "documentId", timestamp));
        List<ProcessRunDto> runHistory = Collections.emptyList();
        if (withTaskHistory) {
            runHistory = new ArrayList<>();
            runHistory.add(new ProcessRunDto(UUID.randomUUID(), OffsetDateTime.now(), Collections.emptyList()));
            runHistory.add(new ProcessRunDto(UUID.randomUUID(), OffsetDateTime.now(), Collections.emptyList()));
        }
        return new TaskDto(id, timestamp, TaskStatus.READY, inputFiles, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), runHistory, Collections.emptyList());
    }

    @Test
    void testAdapterWithIdccConfig() {
        when(cseExportAdapterConfiguration.getProcessType()).thenReturn(com.farao_community.farao.cse.adapter.app.ProcessType.IDCC);
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath(anyString(), anyInt())).thenReturn("URL");
        TaskDto idccTaskDto = getIdccTaskDto();
        cseExportService.runAsync(idccTaskDto);
        assertDoesNotThrow((ThrowingSupplier<RuntimeException>) RuntimeException::new);
    }

    @Test
    void testAdapterWithD2ccConfig() {
        when(cseExportAdapterConfiguration.getProcessType()).thenReturn(com.farao_community.farao.cse.adapter.app.ProcessType.D2CC);
        TaskDto d2ccTaskDto = getD2ccTaskDto("userConfigs.json");
        CseExportRequest d2ccCseRequest = Mockito.mock(CseExportRequest.class);
        Mockito.doReturn(d2ccCseRequest).when(cseExportService).getD2ccRequest(d2ccTaskDto);
        cseExportService.runAsync(d2ccTaskDto);
        assertDoesNotThrow((ThrowingSupplier<RuntimeException>) RuntimeException::new);
    }

    @Test
    void testIdccSuccess() {
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CGM", 1)).thenReturn("file://cgm.uct");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CRAC", 1)).thenReturn("file://crac.json");
        TaskDto taskDto = getIdccTaskDto();

        CseExportRequest cseRequest = cseExportService.getIdccRequest(taskDto);
        assertEquals(ProcessType.IDCC, cseRequest.getProcessType());
        assertEquals("2021-12-07T14:30Z", cseRequest.getTargetProcessDateTime().toString());
        assertEquals(taskDto.getId().toString(), cseRequest.getId());
        assertEquals("file://cgm.uct", cseRequest.getCgmUrl());
        assertEquals("file://crac.json", cseRequest.getMergedCracUrl());
    }

    @Test
    void testIdccNoTaskHistory() {
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CGM", 1)).thenReturn("file://cgm.uct");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CRAC", 1)).thenReturn("file://crac.json");
        TaskDto taskDto = getIdccTaskDto(false);

        assertThrows(CseAdapterException.class, () -> cseExportService.getIdccRequest(taskDto));
    }

    @Test
    void testIdccWithMissingFileUrl() {
        UUID id = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.parse("2021-12-07T14:30Z");
        List<ProcessFileDto> inputFiles = new ArrayList<>();
        inputFiles.add(new ProcessFileDto("/CRAC", "CRAC", ProcessFileStatus.VALIDATED, "crac", "documentId", timestamp));
        TaskDto taskDto = new TaskDto(id, timestamp, TaskStatus.READY, inputFiles, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        assertThrows(CseAdapterException.class, () -> cseExportService.getIdccRequest(taskDto));
    }

    @Test
    void testD2ccSuccess() {
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CGM", 1)).thenReturn("file://cgm.uct");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CRAC", 1)).thenReturn("file://crac.json");
        TaskDto taskDto = getD2ccTaskDto("userConfigs.json");

        CseExportRequest cseRequest = cseExportService.getD2ccRequest(taskDto);
        assertEquals(ProcessType.D2CC, cseRequest.getProcessType());
        assertEquals("2021-12-07T14:30Z", cseRequest.getTargetProcessDateTime().toString());
        assertEquals(taskDto.getId().toString(), cseRequest.getId());
        assertEquals("file://cgm.uct", cseRequest.getCgmUrl());
        assertEquals("file://crac.json", cseRequest.getMergedCracUrl());
    }

    @Test
    void testD2ccNoTaskHistory() {
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CGM", 1)).thenReturn("file://cgm.uct");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CRAC", 1)).thenReturn("file://crac.json");
        TaskDto taskDto = getD2ccTaskDto("userConfigs.json", false);

        assertThrows(CseAdapterException.class, () -> cseExportService.getD2ccRequest(taskDto));
    }

    @Test
    void testD2ccWithMissingFileUrl() {
        UUID id = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.parse("2021-12-07T14:30Z");
        List<ProcessFileDto> inputFiles = new ArrayList<>();
        inputFiles.add(new ProcessFileDto("/CRAC", "CRAC", ProcessFileStatus.VALIDATED, "crac", "documentId", timestamp));
        TaskDto taskDto = new TaskDto(id, timestamp, TaskStatus.READY, inputFiles, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        assertThrows(CseAdapterException.class, () -> cseExportService.getD2ccRequest(taskDto));
    }
}
