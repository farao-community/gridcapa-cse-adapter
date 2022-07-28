package com.farao_community.farao.cse.adapter.app.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class AutomatedForcedPras {

    private final Map<String, List<Set<String>>> automatedForcedPrasIds;

    @JsonCreator
    public AutomatedForcedPras(@JsonProperty Map<String, List<Set<String>>> automatedForcedPrasIds) {
        this.automatedForcedPrasIds = automatedForcedPrasIds;
    }

    public Map<String, List<Set<String>>> getAutomatedForcedPrasIds() {
        return automatedForcedPrasIds;
    }
}
