package ru.netology.cloudstorage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponseWrapper(
        @JsonProperty("auth-token") String authToken,
        String[] email
) {}
