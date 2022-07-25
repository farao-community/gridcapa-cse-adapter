/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.cse.adapter.app.loader;

import com.farao_community.farao.cse.adapter.app.FileImporter;
import com.farao_community.farao.cse.adapter.app.model.UserConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public final class UserConfigurationLoader {
    public final List<String> forcedPrasIds;
    public final Double initialDichotomyIndex;

    public UserConfigurationLoader(FileImporter fileImporter, String url) {
        Optional<UserConfiguration> userConfigOpt = Optional.ofNullable(url).map(fileImporter::importUserConfiguration);
        forcedPrasIds = userConfigOpt.map(UserConfiguration::getForcedPrasIds).orElse(Collections.emptyList());
        initialDichotomyIndex = userConfigOpt.map(UserConfiguration::getInitialDichotomyIndex).orElse(null);
    }
}
