/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.cse.adapter.app;

/**
 * @author Daniel THIRION {@literal <daniel.thirion at rte-france.com>}
 */
public enum FileType {
    CGM("CGM"),
    CRAC("CRAC"),
    GLSK("GLSK"),
    NTC("NTC"),
    NTC_RED("NTC-RED"),
    NTC2_AT("NTC2-AT"),
    NTC2_CH("NTC2-CH"),
    NTC2_FR("NTC2-FR"),
    NTC2_SI("NTC2-SI"),
    VULCANUS("VULCANUS"),
    TARGET_CH("TARGET-CH");

    public String getName() {
        return name;
    }

    private final String name;

    FileType(final String name) {
        this.name = name;
    }
}
