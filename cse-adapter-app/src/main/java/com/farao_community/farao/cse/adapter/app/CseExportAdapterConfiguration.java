package com.farao_community.farao.cse.adapter.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@Configuration
@ConditionalOnProperty(prefix = "cse-adapter", value = "exchange-type", havingValue = "export")
public class CseExportAdapterConfiguration {

    @Value("${cse-adapter.process-type}")
    private String processType;

    public ProcessType getProcessType() {
        return ProcessType.valueOf(processType);
    }
}
