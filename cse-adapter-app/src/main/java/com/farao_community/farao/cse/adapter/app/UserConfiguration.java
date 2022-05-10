package com.farao_community.farao.cse.adapter.app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UserConfiguration {
    private final double initialDichotomyIndex;
    private final List<String> forcedPrasIds;

    @JsonCreator
    public UserConfiguration(@JsonProperty("initialDichotomyIndex") double initialDichotomyIndex,
                             @JsonProperty("forcedPrasIds") List<String> forcedPrasIds) {
        this.initialDichotomyIndex = initialDichotomyIndex;
        this.forcedPrasIds = forcedPrasIds;
    }

    public double getInitialDichotomyIndex() {
        return initialDichotomyIndex;
    }

    public List<String> getForcedPrasIds() {
        return forcedPrasIds;
    }
}