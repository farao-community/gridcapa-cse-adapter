package com.farao_community.farao.cse.adapter.app;

import com.farao_community.farao.cse.runner.api.exception.CseInvalidDataException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

@Service
public class FileImporter {

    private final UrlValidationService urlValidationService;

    public FileImporter(UrlValidationService urlValidationService) {
        this.urlValidationService = urlValidationService;
    }

    public UserConfiguration importUserConfiguration(String userConfigurationUrl) {
        try (InputStream is = urlValidationService.openUrlStream(userConfigurationUrl)) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(is.readAllBytes(), UserConfiguration.class);
        } catch (IOException e) {
            throw new CseInvalidDataException(String.format("Cannot import forced PRAs file: %s, check file format", getFilenameFromUrl(userConfigurationUrl)), e);
        }
    }

    public static String getFilenameFromUrl(String url) {
        try {
            return FilenameUtils.getName(new URL(url).getPath());
        } catch (MalformedURLException e) {
            throw new CseInvalidDataException(String.format("URL is invalid: %s", url));
        }
    }
}
