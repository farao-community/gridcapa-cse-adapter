/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.cse.adapter.app;

import java.util.Map;
import java.util.Optional;

/**
 * @author Daniel THIRION {@literal <daniel.thirion at rte-france.com>}
 */
public final class GetUrlUtil {

    private GetUrlUtil() {
        // private constructor to hide implicit one
    }

    static String getUrlOrThrow(final Map<String, String> processFileUrlByType,
                                final FileType type) {
        return Optional.ofNullable(processFileUrlByType.get(type.getName())).orElseThrow(() -> new CseAdapterException(type.getName() + " type not found"));
    }

    static String getUrlOrEmpty(final Map<String, String> processFileUrlByType,
                                final FileType type) {
        return Optional.ofNullable(processFileUrlByType.get(type.getName())).orElse("");
    }
}
