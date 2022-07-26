/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.cse.adapter.app.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UserConfiguration {
    private final Double initialDichotomyIndex;
    private final List<String> manualForcedPrasIds;

    @JsonCreator
    public UserConfiguration(@JsonProperty("initialDichotomyIndex") Double initialDichotomyIndex,
                             @JsonProperty("manualForcedPrasIds") List<String> manualForcedPrasIds) {
        this.initialDichotomyIndex = initialDichotomyIndex;
        this.manualForcedPrasIds = manualForcedPrasIds;
    }

    public Double getInitialDichotomyIndex() {
        return initialDichotomyIndex;
    }

    public List<String> getManualForcedPrasIds() {
        return manualForcedPrasIds;
    }
}
