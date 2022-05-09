package com.farao_community.farao.cse.adapter.app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ForcedPras {
    private final double initialDichotomyStep;
    private final List<String> forcedPrasIds;

    @JsonCreator
    public ForcedPras(@JsonProperty("initialDichotomyStep") double initialDichotomyStep, @JsonProperty("forcedPrasIds") List<String> forcedPrasIds) {
        this.initialDichotomyStep = initialDichotomyStep;
        this.forcedPrasIds = forcedPrasIds;
    }

    public double getInitialDichotomyStep() {
        return initialDichotomyStep;
    }

    public List<String> getForcedPrasIds() {
        return forcedPrasIds;
    }
}
