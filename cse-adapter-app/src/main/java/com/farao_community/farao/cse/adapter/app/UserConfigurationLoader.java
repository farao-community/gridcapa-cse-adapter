package com.farao_community.farao.cse.adapter.app;

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
