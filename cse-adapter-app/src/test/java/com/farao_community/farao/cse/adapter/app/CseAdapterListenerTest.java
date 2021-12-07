package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.cse.runner.api.resource.CseRequest;
import com.farao_community.farao.cse.runner.api.resource.ProcessType;
import com.farao_community.farao.gridcapa.task_manager.api.ProcessFileDto;
import com.farao_community.farao.gridcapa.task_manager.api.ProcessFileStatus;
import com.farao_community.farao.gridcapa.task_manager.api.TaskDto;
import com.farao_community.farao.gridcapa.task_manager.api.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@SpringBootTest
class CseAdapterListenerTest {

    @Autowired
    private CseAdapterListener cseAdapterListener;

    @Test
    void testIdccSuccess() {
        UUID id = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.parse("2021-12-07T14:30");
        List<ProcessFileDto> processFiles = new ArrayList<>();
        processFiles.add(new ProcessFileDto("CGM", ProcessFileStatus.VALIDATED, "cgm", timestamp, "file://cgm.uct"));
        processFiles.add(new ProcessFileDto("CRAC", ProcessFileStatus.VALIDATED, "crac", timestamp, "file://crac.json"));
        processFiles.add(new ProcessFileDto("GLSK", ProcessFileStatus.VALIDATED, "glsk", timestamp, "file://glsk.xml"));
        processFiles.add(new ProcessFileDto("NTC", ProcessFileStatus.VALIDATED, "ntc", timestamp, "file://ntc.xml"));
        processFiles.add(new ProcessFileDto("NTC-RED", ProcessFileStatus.VALIDATED, "ntc-red", timestamp, "file://ntc-red.xml"));
        processFiles.add(new ProcessFileDto("VULCANUS", ProcessFileStatus.VALIDATED, "vulcanus", timestamp, "file://vulcanus.xls"));
        processFiles.add(new ProcessFileDto("AT-NTC2", ProcessFileStatus.VALIDATED, "at-ntc2", timestamp, "file://at-ntc2.xml"));
        processFiles.add(new ProcessFileDto("CH-NTC2", ProcessFileStatus.VALIDATED, "ch-ntc2", timestamp, "file://ch-ntc2.xml"));
        processFiles.add(new ProcessFileDto("FR-NTC2", ProcessFileStatus.VALIDATED, "fr-ntc2", timestamp, "file://fr-ntc2.xml"));
        processFiles.add(new ProcessFileDto("SI-NTC2", ProcessFileStatus.VALIDATED, "si-ntc2", timestamp, "file://si-ntc2.xml"));
        TaskDto taskDto = new TaskDto(id, timestamp, TaskStatus.READY, processFiles);

        CseRequest cseRequest = cseAdapterListener.getIdccRequest(taskDto);
        assertEquals(ProcessType.IDCC, cseRequest.getProcessType());
        assertEquals("2021-12-07T14:30Z", cseRequest.getTargetProcessDateTime().toString());
        assertEquals(id.toString(), cseRequest.getId());
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
        processFiles.add(new ProcessFileDto("AT-NTC2", ProcessFileStatus.VALIDATED, "at-ntc2", timestamp, "file://at-ntc2.xml"));
        processFiles.add(new ProcessFileDto("CH-NTC2", ProcessFileStatus.VALIDATED, "ch-ntc2", timestamp, "file://ch-ntc2.xml"));
        processFiles.add(new ProcessFileDto("FR-NTC2", ProcessFileStatus.VALIDATED, "fr-ntc2", timestamp, "file://fr-ntc2.xml"));
        processFiles.add(new ProcessFileDto("SI-NTC2", ProcessFileStatus.VALIDATED, "si-ntc2", timestamp, "file://si-ntc2.xml"));
        TaskDto taskDto = new TaskDto(id, timestamp, TaskStatus.READY, processFiles);

        assertThrows(NullPointerException.class, () -> cseAdapterListener.getIdccRequest(taskDto));
    }

    @Test
    void testD2ccSuccess() {
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
        TaskDto taskDto = new TaskDto(id, timestamp, TaskStatus.READY, processFiles);

        CseRequest cseRequest = cseAdapterListener.getD2ccRequest(taskDto);
        assertEquals(ProcessType.D2CC, cseRequest.getProcessType());
        assertEquals("2021-12-07T14:30Z", cseRequest.getTargetProcessDateTime().toString());
        assertEquals(id.toString(), cseRequest.getId());
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

        assertThrows(NullPointerException.class, () -> cseAdapterListener.getD2ccRequest(taskDto));
    }
}
