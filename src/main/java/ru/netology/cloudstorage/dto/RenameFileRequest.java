package ru.netology.cloudstorage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RenameFileRequest(
        @JsonProperty("oldFilename") String oldFilename,
        @JsonProperty("newFilename") String newFilename
) {}
