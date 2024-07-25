/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.cse.runner.api.resource.CseRequest;
import com.farao_community.farao.cse.runner.api.resource.ProcessType;
import com.farao_community.farao.gridcapa.task_manager.api.*;
import com.farao_community.farao.minio_adapter.starter.MinioAdapter;
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
import static org.mockito.Mockito.when;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@SpringBootTest
@ActiveProfiles("idcc-import")
class CseImportServiceTest {

    @SpyBean
    private CseImportService cseImportService;

    @MockBean
    private CseImportAdapterConfiguration cseImportAdapterConfiguration;

    @MockBean
    private MinioAdapter minioAdapter;

    private TaskDto getIdccTaskDto(String userConfigFile) {
        return getIdccTaskDto(userConfigFile, true);
    }

    private TaskDto getIdccTaskDto(String userConfigFile, boolean withTaskHistory) {
        UUID id = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.parse("2021-12-07T14:30Z");
        List<ProcessFileDto> inputFiles = new ArrayList<>();
        inputFiles.add(new ProcessFileDto("/CGM", "CGM", ProcessFileStatus.VALIDATED, "cgm", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/CRAC", "CRAC", ProcessFileStatus.VALIDATED, "crac", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/GLSK", "GLSK", ProcessFileStatus.VALIDATED, "glsk", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/NTC", "NTC", ProcessFileStatus.VALIDATED, "ntc", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/NTC-RED", "NTC-RED", ProcessFileStatus.VALIDATED, "ntc-red", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/VULCANUS", "VULCANUS", ProcessFileStatus.VALIDATED, "vulcanus", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/NTC2-AT", "NTC2-AT", ProcessFileStatus.VALIDATED, "at-ntc2", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/NTC2-CH", "NTC2-CH", ProcessFileStatus.VALIDATED, "ch-ntc2", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/NTC2-FR", "NTC2-FR", ProcessFileStatus.VALIDATED, "fr-ntc2", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/NTC2-SI", "NTC2-SI", ProcessFileStatus.VALIDATED, "si-ntc2", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto(ClassLoader.getSystemResource(userConfigFile).toString(), "USER-CONFIG", ProcessFileStatus.VALIDATED, "user-config",
                "documentId", timestamp));

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
        inputFiles.add(new ProcessFileDto("/TARGET-CH", "TARGET-CH", ProcessFileStatus.VALIDATED, "target-ch", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/NTC-RED", "NTC-RED", ProcessFileStatus.VALIDATED, "ntc-red", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/VULCANUS", "VULCANUS", ProcessFileStatus.VALIDATED, "vulcanus", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto(ClassLoader.getSystemResource(userConfigFile).toString(), "USER-CONFIG", ProcessFileStatus.VALIDATED, "user-config",
                "documentId", timestamp));
        List<ProcessRunDto> runHistory = Collections.emptyList();
        if (withTaskHistory) {
            runHistory = new ArrayList<>();
            runHistory.add(new ProcessRunDto(UUID.randomUUID(), OffsetDateTime.now(), Collections.emptyList()));
        }
        return new TaskDto(id, timestamp, TaskStatus.READY, inputFiles, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), runHistory, Collections.emptyList());
    }

    @Test
    void testAdapterWithIdccConfig() {
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CGM", 1)).thenReturn("file://cgm.uct");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CRAC", 1)).thenReturn("file://crac.json");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/GLSK", 1)).thenReturn("file://glsk.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC-RED", 1)).thenReturn("file://ntc-red.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC2-AT", 1)).thenReturn("file://at-ntc2.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC2-CH", 1)).thenReturn("file://ch-ntc2.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC2-FR", 1)).thenReturn("file://fr-ntc2.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC2-SI", 1)).thenReturn("file://si-ntc2.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/VULCANUS", 1)).thenReturn("file://vulcanus.xls");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC", 1)).thenReturn("file://ntc.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath(ClassLoader.getSystemResource("userConfigs.json").toString(), 1)).thenReturn(ClassLoader.getSystemResource("userConfigs.json").toString());
        when(cseImportAdapterConfiguration.getProcessType()).thenReturn(com.farao_community.farao.cse.adapter.app.ProcessType.IDCC);
        TaskDto idccTaskDto = getIdccTaskDto("userConfigs.json");
        cseImportService.runAsync(idccTaskDto);
        assertDoesNotThrow((ThrowingSupplier<RuntimeException>) RuntimeException::new);
    }

    @Test
    void testAdapterWithD2ccConfig() {
        when(cseImportAdapterConfiguration.getProcessType()).thenReturn(com.farao_community.farao.cse.adapter.app.ProcessType.D2CC);
        TaskDto d2ccTaskDto = getD2ccTaskDto("userConfigs.json");
        CseRequest d2ccCseRequest = Mockito.mock(CseRequest.class);
        Mockito.doReturn(d2ccCseRequest).when(cseImportService).getD2ccRequest(d2ccTaskDto);
        cseImportService.runAsync(d2ccTaskDto);
        assertDoesNotThrow((ThrowingSupplier<RuntimeException>) RuntimeException::new);
    }

    @Test
    void testIdccSuccess() {
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CGM", 1)).thenReturn("file://cgm.uct");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CRAC", 1)).thenReturn("file://crac.json");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/GLSK", 1)).thenReturn("file://glsk.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC-RED", 1)).thenReturn("file://ntc-red.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC2-AT", 1)).thenReturn("file://at-ntc2.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC2-CH", 1)).thenReturn("file://ch-ntc2.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC2-FR", 1)).thenReturn("file://fr-ntc2.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC2-SI", 1)).thenReturn("file://si-ntc2.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/VULCANUS", 1)).thenReturn("file://vulcanus.xls");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC", 1)).thenReturn("file://ntc.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath(ClassLoader.getSystemResource("userConfigs.json").toString(), 1)).thenReturn(ClassLoader.getSystemResource("userConfigs.json").toString());

        TaskDto taskDto = getIdccTaskDto("userConfigs.json");

        CseRequest cseRequest = cseImportService.getIdccRequest(taskDto);
        assertEquals(com.farao_community.farao.cse.runner.api.resource.ProcessType.IDCC, cseRequest.getProcessType());
        assertEquals("2021-12-07T14:30Z", cseRequest.getTargetProcessDateTime().toString());
        assertEquals(taskDto.getId().toString(), cseRequest.getId());
        assertEquals("file://cgm.uct", cseRequest.getCgmUrl());
        assertEquals("file://crac.json", cseRequest.getMergedCracUrl());
        assertEquals("file://glsk.xml", cseRequest.getMergedGlskUrl());
        assertEquals("file://ntc.xml", cseRequest.getYearlyNtcUrl());
        assertEquals("file://ntc-red.xml", cseRequest.getNtcReductionsUrl());
        assertEquals("file://vulcanus.xls", cseRequest.getVulcanusUrl());
        assertEquals("file://at-ntc2.xml", cseRequest.getNtc2AtItUrl());
        assertEquals("file://ch-ntc2.xml", cseRequest.getNtc2ChItUrl());
        assertEquals("file://fr-ntc2.xml", cseRequest.getNtc2FrItUrl());
        assertEquals("file://si-ntc2.xml", cseRequest.getNtc2SiItUrl());
        assertEquals(2, cseRequest.getManualForcedPrasIds().size());
        assertEquals(100, cseRequest.getDichotomyPrecision());
        assertEquals(650, cseRequest.getInitialDichotomyStep());
        assertEquals(10, cseRequest.getInitialDichotomyIndex());
    }

    @Test
    void testIdccNoTaskHistory() {
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CGM", 1)).thenReturn("file://cgm.uct");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CRAC", 1)).thenReturn("file://crac.json");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/GLSK", 1)).thenReturn("file://glsk.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC-RED", 1)).thenReturn("file://ntc-red.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC2-AT", 1)).thenReturn("file://at-ntc2.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC2-CH", 1)).thenReturn("file://ch-ntc2.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC2-FR", 1)).thenReturn("file://fr-ntc2.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC2-SI", 1)).thenReturn("file://si-ntc2.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/VULCANUS", 1)).thenReturn("file://vulcanus.xls");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC", 1)).thenReturn("file://ntc.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath(ClassLoader.getSystemResource("userConfigs.json").toString(), 1)).thenReturn(ClassLoader.getSystemResource("userConfigs.json").toString());

        TaskDto taskDto = getIdccTaskDto("userConfigs.json", false);

        assertThrows(CseAdapterException.class, () -> cseImportService.getIdccRequest(taskDto));
    }

    @Test
    void testIdccSuccessWithNullUserConfiguration() {
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CGM", 1)).thenReturn("file://cgm.uct");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CRAC", 1)).thenReturn("file://crac.json");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/GLSK", 1)).thenReturn("file://glsk.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC-RED", 1)).thenReturn("file://ntc-red.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC2-AT", 1)).thenReturn("file://at-ntc2.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC2-CH", 1)).thenReturn("file://ch-ntc2.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC2-FR", 1)).thenReturn("file://fr-ntc2.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC2-SI", 1)).thenReturn("file://si-ntc2.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/VULCANUS", 1)).thenReturn("file://vulcanus.xls");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC", 1)).thenReturn("file://ntc.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath(ClassLoader.getSystemResource("userConfigs-null.json").toString(), 1)).thenReturn(ClassLoader.getSystemResource("userConfigs-null.json").toString());

        TaskDto taskDto = getIdccTaskDto("userConfigs-null.json");

        CseRequest cseRequest = cseImportService.getIdccRequest(taskDto);
        assertEquals(0, cseRequest.getManualForcedPrasIds().size());
        assertNull(cseRequest.getInitialDichotomyIndex());
    }

    @Test
    void testIdccWithMissingFileUrl() {
        UUID id = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.parse("2021-12-07T14:30Z");
        List<ProcessFileDto> inputFiles = new ArrayList<>();
        inputFiles.add(new ProcessFileDto("/CRAC", "CRAC", ProcessFileStatus.VALIDATED, "crac", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/GLSK", "GLSK", ProcessFileStatus.VALIDATED, "glsk", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/NTC", "NTC", ProcessFileStatus.VALIDATED, "ntc", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/NTC-RED", "NTC-RED", ProcessFileStatus.VALIDATED, "ntc-red", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/VULCANUS", "VULCANUS", ProcessFileStatus.VALIDATED, "vulcanus", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/NTC2-AT", "NTC2-AT", ProcessFileStatus.VALIDATED, "at-ntc2", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/NTC2-CH", "NTC2-CH", ProcessFileStatus.VALIDATED, "ch-ntc2", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/NTC2-FR", "NTC2-FR", ProcessFileStatus.VALIDATED, "fr-ntc2", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/NTC2-SI", "NTC2-SI", ProcessFileStatus.VALIDATED, "si-ntc2", "documentId", timestamp));
        TaskDto taskDto = new TaskDto(id, timestamp, TaskStatus.READY, inputFiles, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        assertThrows(CseAdapterException.class, () -> cseImportService.getIdccRequest(taskDto));
    }

    @Test
    void testD2ccSuccess() {
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CGM", 1)).thenReturn("file://cgm.uct");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CRAC", 1)).thenReturn("file://crac.json");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/GLSK", 1)).thenReturn("file://glsk.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC-RED", 1)).thenReturn("file://ntc-red.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/TARGET-CH", 1)).thenReturn("file://target-ch.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC", 1)).thenReturn("file://ntc.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/VULCANUS", 1)).thenReturn("file://vulcanus.xlsx");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath(ClassLoader.getSystemResource("userConfigs.json").toString(), 1)).thenReturn(ClassLoader.getSystemResource("userConfigs.json").toString());

        TaskDto taskDto = getD2ccTaskDto("userConfigs.json");

        CseRequest cseRequest = cseImportService.getD2ccRequest(taskDto);
        assertEquals(ProcessType.D2CC, cseRequest.getProcessType());
        assertEquals("2021-12-07T14:30Z", cseRequest.getTargetProcessDateTime().toString());
        assertEquals(taskDto.getId().toString(), cseRequest.getId());
        assertEquals("file://cgm.uct", cseRequest.getCgmUrl());
        assertEquals("file://crac.json", cseRequest.getMergedCracUrl());
        assertEquals("file://glsk.xml", cseRequest.getMergedGlskUrl());
        assertEquals("file://ntc.xml", cseRequest.getYearlyNtcUrl());
        assertEquals("file://ntc-red.xml", cseRequest.getNtcReductionsUrl());
        assertEquals("file://target-ch.xml", cseRequest.getTargetChUrl());
        assertEquals(2, cseRequest.getManualForcedPrasIds().size());
        assertEquals(100, cseRequest.getDichotomyPrecision());
        assertEquals(650, cseRequest.getInitialDichotomyStep());
        assertEquals(10, cseRequest.getInitialDichotomyIndex());
    }

    @Test
    void testD2ccNoTaskHistory() {
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CGM", 1)).thenReturn("file://cgm.uct");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CRAC", 1)).thenReturn("file://crac.json");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/GLSK", 1)).thenReturn("file://glsk.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC-RED", 1)).thenReturn("file://ntc-red.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/TARGET-CH", 1)).thenReturn("file://target-ch.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC", 1)).thenReturn("file://ntc.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/VULCANUS", 1)).thenReturn("file://vulcanus.xlsx");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath(ClassLoader.getSystemResource("userConfigs.json").toString(), 1)).thenReturn(ClassLoader.getSystemResource("userConfigs.json").toString());

        TaskDto taskDto = getD2ccTaskDto("userConfigs.json", false);

        assertThrows(CseAdapterException.class, () -> cseImportService.getD2ccRequest(taskDto));
    }

    @Test
    void testD2ccWithMissingFileUrl() {
        UUID id = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.parse("2021-12-07T14:30Z");
        List<ProcessFileDto> inputFiles = new ArrayList<>();
        inputFiles.add(new ProcessFileDto("/CRAC", "CRAC", ProcessFileStatus.VALIDATED, "crac", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/GLSK", "GLSK", ProcessFileStatus.VALIDATED, "glsk", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/NTC", "NTC", ProcessFileStatus.VALIDATED, "ntc", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/NTC-RED", "NTC-RED", ProcessFileStatus.VALIDATED, "ntc-red", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/TARGET-CH", "TARGET-CH", ProcessFileStatus.VALIDATED, "target-ch", "documentId", timestamp));
        inputFiles.add(new ProcessFileDto("/VULCANUS", "VULCANUS", ProcessFileStatus.VALIDATED, "vulcanus", "documentId", timestamp));
        TaskDto taskDto = new TaskDto(id, timestamp, TaskStatus.READY, inputFiles, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        assertThrows(CseAdapterException.class, () -> cseImportService.getD2ccRequest(taskDto));
    }

    @Test
    void testD2ccSuccessWithNullUserConfiguration() {
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CGM", 1)).thenReturn("file://cgm.uct");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/CRAC", 1)).thenReturn("file://crac.json");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/GLSK", 1)).thenReturn("file://glsk.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC-RED", 1)).thenReturn("file://ntc-red.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/TARGET-CH", 1)).thenReturn("file://target-ch.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/NTC", 1)).thenReturn("file://ntc.xml");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath("/VULCANUS", 1)).thenReturn("file://vulcanus.xlsx");
        Mockito.when(minioAdapter.generatePreSignedUrlFromFullMinioPath(ClassLoader.getSystemResource("userConfigs-null.json").toString(), 1)).thenReturn(ClassLoader.getSystemResource("userConfigs-null.json").toString());

        TaskDto taskDto = getD2ccTaskDto("userConfigs-null.json");

        CseRequest cseRequest = cseImportService.getD2ccRequest(taskDto);
        assertEquals(0, cseRequest.getManualForcedPrasIds().size());
        assertNull(cseRequest.getInitialDichotomyIndex());
    }
}
