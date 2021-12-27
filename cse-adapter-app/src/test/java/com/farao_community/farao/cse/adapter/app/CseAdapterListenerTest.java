package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.cse.runner.api.resource.CseRequest;
import com.farao_community.farao.cse.runner.api.resource.CseResponse;
import com.farao_community.farao.cse.runner.api.resource.ProcessType;
import com.farao_community.farao.cse.runner.starter.CseClient;
import com.farao_community.farao.gridcapa.task_manager.api.ProcessFileDto;
import com.farao_community.farao.gridcapa.task_manager.api.ProcessFileStatus;
import com.farao_community.farao.gridcapa.task_manager.api.TaskDto;
import com.farao_community.farao.gridcapa.task_manager.api.TaskStatus;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@SpringBootTest
class CseAdapterListenerTest {

    @SpyBean
    private CseAdapterListener cseAdapterListener;

    @MockBean
    private CseAdapterConfiguration cseAdapterConfiguration;

    @MockBean
    private CseClient cseClient;

    private TaskDto getIdccTaskDto() {
        UUID id = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.parse("2021-12-07T14:30");
        List<ProcessFileDto> processFiles = new ArrayList<>();
        processFiles.add(new ProcessFileDto("CGM", ProcessFileStatus.VALIDATED, "cgm", timestamp, "file://cgm.uct"));
        processFiles.add(new ProcessFileDto("CRAC", ProcessFileStatus.VALIDATED, "crac", timestamp, "file://crac.json"));
        processFiles.add(new ProcessFileDto("GLSK", ProcessFileStatus.VALIDATED, "glsk", timestamp, "file://glsk.xml"));
        processFiles.add(new ProcessFileDto("NTC", ProcessFileStatus.VALIDATED, "ntc", timestamp, "file://ntc.xml"));
        processFiles.add(new ProcessFileDto("NTC-RED", ProcessFileStatus.VALIDATED, "ntc-red", timestamp, "file://ntc-red.xml"));
        processFiles.add(new ProcessFileDto("VULCANUS", ProcessFileStatus.VALIDATED, "vulcanus", timestamp, "file://vulcanus.xls"));
        processFiles.add(new ProcessFileDto("NTC2-AT", ProcessFileStatus.VALIDATED, "at-ntc2", timestamp, "file://at-ntc2.xml"));
        processFiles.add(new ProcessFileDto("NTC2-CH", ProcessFileStatus.VALIDATED, "ch-ntc2", timestamp, "file://ch-ntc2.xml"));
        processFiles.add(new ProcessFileDto("NTC2-FR", ProcessFileStatus.VALIDATED, "fr-ntc2", timestamp, "file://fr-ntc2.xml"));
        processFiles.add(new ProcessFileDto("NTC2-SI", ProcessFileStatus.VALIDATED, "si-ntc2", timestamp, "file://si-ntc2.xml"));
        return new TaskDto(id, timestamp, TaskStatus.READY, processFiles);
    }

    private TaskDto getD2ccTaskDto() {
        UUID id = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.parse("2021-12-07T14:30");
        List<ProcessFileDto> processFiles = new ArrayList<>();
        processFiles.add(new ProcessFileDto("CGM", ProcessFileStatus.VALIDATED, "cgm", timestamp, "file://cgm.uct"));
        processFiles.add(new ProcessFileDto("CRAC", ProcessFileStatus.VALIDATED, "crac", timestamp, "file://crac.json"));
        processFiles.add(new ProcessFileDto("GLSK", ProcessFileStatus.VALIDATED, "glsk", timestamp, "file://glsk.xml"));
        processFiles.add(new ProcessFileDto("NTC", ProcessFileStatus.VALIDATED, "ntc", timestamp, "file://ntc.xml"));
        processFiles.add(new ProcessFileDto("NTC-RED", ProcessFileStatus.VALIDATED, "ntc-red", timestamp, "file://ntc-red.xml"));
        processFiles.add(new ProcessFileDto("VULCANUS", ProcessFileStatus.VALIDATED, "vulcanus", timestamp, "file://vulcanus.xls"));
        processFiles.add(new ProcessFileDto("TARGET-CH", ProcessFileStatus.VALIDATED, "target-ch", timestamp, "file://target-ch.xml"));
        return new TaskDto(id, timestamp, TaskStatus.READY, processFiles);
    }

    @Test
    void testAdapterWithIdccConfig() {
        when(cseAdapterConfiguration.getTargetProcess()).thenReturn("IDCC");
        TaskDto idccTaskDto = getIdccTaskDto();
        CseRequest idccCseRequest = Mockito.mock(CseRequest.class);
        Mockito.when(cseAdapterListener.getIdccRequest(idccTaskDto)).thenReturn(idccCseRequest);
        CseResponse idccCseResponse = new CseResponse(idccCseRequest.getId(), "file://ttc_result.xml");
        AtomicBoolean correctRun = new AtomicBoolean(false);
        when(cseClient.run(idccCseRequest)).thenAnswer(taskDto -> {
            correctRun.set(true);
            return idccCseResponse;
        });

        cseAdapterListener.handleRun().accept(idccTaskDto);
        assertTrue(correctRun.get());
    }

    @Test
    void testAdapterWithD2ccConfig() {
        when(cseAdapterConfiguration.getTargetProcess()).thenReturn("D2CC");
        TaskDto d2ccTaskDto = getD2ccTaskDto();
        CseRequest d2ccCseRequest = Mockito.mock(CseRequest.class);
        Mockito.when(cseAdapterListener.getD2ccRequest(d2ccTaskDto)).thenReturn(d2ccCseRequest);
        CseResponse d2ccCseResponse = Mockito.mock(CseResponse.class);
        AtomicBoolean correctRun = new AtomicBoolean(false);
        when(cseClient.run(d2ccCseRequest)).thenAnswer(taskDto -> {
            correctRun.set(true);
            return d2ccCseResponse;
        });

        cseAdapterListener.handleRun().accept(d2ccTaskDto);
        assertTrue(correctRun.get());
    }

    @Test
    void testAdapterWithInvalidConfig() {
        when(cseAdapterConfiguration.getTargetProcess()).thenReturn("INVALID");
        TaskDto d2ccTaskDto = getD2ccTaskDto();

        assertThrows(NotImplementedException.class, () -> cseAdapterListener.handleRun().accept(d2ccTaskDto));
    }

    @Test
    void testIdccSuccess() {
        TaskDto taskDto = getIdccTaskDto();

        CseRequest cseRequest = cseAdapterListener.getIdccRequest(taskDto);
        assertEquals(ProcessType.IDCC, cseRequest.getProcessType());
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
        assertEquals(50, cseRequest.getDichotomyPrecision());
        assertEquals(650, cseRequest.getInitialDichotomyStep());
        assertNull(cseRequest.getInitialDichotomyIndex());
    }

    @Test
    void testIdccWithMissingFileUrl() {
        UUID id = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.parse("2021-12-07T14:30");
        List<ProcessFileDto> processFiles = new ArrayList<>();
        processFiles.add(new ProcessFileDto("CRAC", ProcessFileStatus.VALIDATED, "crac", timestamp, "file://crac.json"));
        processFiles.add(new ProcessFileDto("GLSK", ProcessFileStatus.VALIDATED, "glsk", timestamp, "file://glsk.xml"));
        processFiles.add(new ProcessFileDto("NTC", ProcessFileStatus.VALIDATED, "ntc", timestamp, "file://ntc.xml"));
        processFiles.add(new ProcessFileDto("NTC-RED", ProcessFileStatus.VALIDATED, "ntc-red", timestamp, "file://ntc-red.xml"));
        processFiles.add(new ProcessFileDto("VULCANUS", ProcessFileStatus.VALIDATED, "vulcanus", timestamp, "file://vulcanus.xls"));
        processFiles.add(new ProcessFileDto("NTC2-AT", ProcessFileStatus.VALIDATED, "at-ntc2", timestamp, "file://at-ntc2.xml"));
        processFiles.add(new ProcessFileDto("NTC2-CH", ProcessFileStatus.VALIDATED, "ch-ntc2", timestamp, "file://ch-ntc2.xml"));
        processFiles.add(new ProcessFileDto("NTC2-FR", ProcessFileStatus.VALIDATED, "fr-ntc2", timestamp, "file://fr-ntc2.xml"));
        processFiles.add(new ProcessFileDto("NTC2-SI", ProcessFileStatus.VALIDATED, "si-ntc2", timestamp, "file://si-ntc2.xml"));
        TaskDto taskDto = new TaskDto(id, timestamp, TaskStatus.READY, processFiles);

        assertThrows(CseAdapterException.class, () -> cseAdapterListener.getIdccRequest(taskDto));
    }

    @Test
    void testD2ccSuccess() {
        TaskDto taskDto = getD2ccTaskDto();

        CseRequest cseRequest = cseAdapterListener.getD2ccRequest(taskDto);
        assertEquals(ProcessType.D2CC, cseRequest.getProcessType());
        assertEquals("2021-12-07T14:30Z", cseRequest.getTargetProcessDateTime().toString());
        assertEquals(taskDto.getId().toString(), cseRequest.getId());
        assertEquals("file://cgm.uct", cseRequest.getCgmUrl());
        assertEquals("file://crac.json", cseRequest.getMergedCracUrl());
        assertEquals("file://glsk.xml", cseRequest.getMergedGlskUrl());
        assertEquals("file://ntc.xml", cseRequest.getYearlyNtcUrl());
        assertEquals("file://ntc-red.xml", cseRequest.getNtcReductionsUrl());
        assertEquals("file://vulcanus.xls", cseRequest.getVulcanusUrl());
        assertEquals("file://target-ch.xml", cseRequest.getTargetChUrl());
        assertEquals(50, cseRequest.getDichotomyPrecision());
        assertEquals(650, cseRequest.getInitialDichotomyStep());
        assertNull(cseRequest.getInitialDichotomyIndex());
    }

    @Test
    void testD2ccWithMissingFileUrl() {
        UUID id = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.parse("2021-12-07T14:30");
        List<ProcessFileDto> processFiles = new ArrayList<>();
        processFiles.add(new ProcessFileDto("CRAC", ProcessFileStatus.VALIDATED, "crac", timestamp, "file://crac.json"));
        processFiles.add(new ProcessFileDto("GLSK", ProcessFileStatus.VALIDATED, "glsk", timestamp, "file://glsk.xml"));
        processFiles.add(new ProcessFileDto("NTC", ProcessFileStatus.VALIDATED, "ntc", timestamp, "file://ntc.xml"));
        processFiles.add(new ProcessFileDto("NTC-RED", ProcessFileStatus.VALIDATED, "ntc-red", timestamp, "file://ntc-red.xml"));
        processFiles.add(new ProcessFileDto("VULCANUS", ProcessFileStatus.VALIDATED, "vulcanus", timestamp, "file://vulcanus.xls"));
        processFiles.add(new ProcessFileDto("TARGET-CH", ProcessFileStatus.VALIDATED, "target-ch", timestamp, "file://target-ch.xml"));
        TaskDto taskDto = new TaskDto(id, timestamp, TaskStatus.READY, processFiles);

        assertThrows(CseAdapterException.class, () -> cseAdapterListener.getD2ccRequest(taskDto));
    }
}
