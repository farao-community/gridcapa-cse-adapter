package com.farao_community.farao.cse.adapter.app.loader;

import com.farao_community.farao.cse.adapter.app.FileImporter;
import com.farao_community.farao.cse.adapter.app.model.AutomatedForcedPras;

import java.util.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class AutomatedForcedPrasLoader {
    public final Map<String, List<Set<String>>> automatedForcedPrasIds;

    public AutomatedForcedPrasLoader(FileImporter fileImporter, String url) {
        automatedForcedPrasIds = Optional.ofNullable(url)
            .map(fileImporter::importAutomatedForcedPras)
            .map(AutomatedForcedPras::getAutomatedForcedPrasIds)
            .orElse(Collections.emptyMap());
    }
}
