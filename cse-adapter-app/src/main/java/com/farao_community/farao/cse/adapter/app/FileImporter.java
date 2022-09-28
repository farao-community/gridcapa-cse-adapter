/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.cse.adapter.app.model.AutomatedForcedPras;
import com.farao_community.farao.cse.adapter.app.model.UserConfiguration;
import com.farao_community.farao.cse.runner.api.exception.CseInvalidDataException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

@Service
public class FileImporter {

    private final UrlWhitelistConfiguration urlWhitelistConfiguration;
    private final Logger businessLogger;

    public FileImporter(UrlWhitelistConfiguration urlWhitelistConfiguration, Logger businessLogger) {
        this.urlWhitelistConfiguration = urlWhitelistConfiguration;
        this.businessLogger = businessLogger;
    }

    public UserConfiguration importUserConfiguration(String userConfigurationUrl) {
        try (InputStream is = openUrlStream(userConfigurationUrl)) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(is.readAllBytes(), UserConfiguration.class);
        } catch (IOException e) {
            throw new CseInvalidDataException(String.format("Cannot import user configuration file: %s, check file format: %s", getFileNameFromUrl(userConfigurationUrl), e.getMessage()), e);
        }
    }

    public AutomatedForcedPras importAutomatedForcedPras(String automatedForcedPrasUrl) {
        try (InputStream is = openUrlStream(automatedForcedPrasUrl)) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(is.readAllBytes(), AutomatedForcedPras.class);
        } catch (IOException e) {
            throw new CseInvalidDataException(String.format("Cannot import automated forced PRAs file: %s, check file format: %s", getFileNameFromUrl(automatedForcedPrasUrl), e.getMessage()), e);
        }
    }

    private static String getFileNameFromUrl(String url) {
        try {
            return FilenameUtils.getName(new URL(url).getPath());
        } catch (MalformedURLException e) {
            throw new CseInvalidDataException(String.format("URL is invalid: %s", url));
        }
    }

    private InputStream openUrlStream(String urlString) {
        try {
            if (urlWhitelistConfiguration.getWhitelist().stream().noneMatch(urlString::startsWith)) {
                throw new CseInvalidDataException(String.format("URL '%s' is not part of application's whitelisted url's.", urlString));
            }
            URL url = new URL(urlString);
            return url.openStream(); // NOSONAR Usage of whitelist not triggered by Sonar quality assessment, even if listed as a solution to the vulnerability
        } catch (IOException e) {
            businessLogger.error("Error while retrieving content of file : {}, Link may have expired.", getFileNameFromUrl(urlString));
            throw new CseInvalidDataException(String.format("Exception occurred while retrieving file content from : %s Cause: %s ", urlString, e.getMessage()));
        }
    }

}
