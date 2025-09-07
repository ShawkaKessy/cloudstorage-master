package ru.netology.cloudstorage.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginRequest(
        @JsonProperty("login") @JsonAlias("email") String login,
        @JsonProperty("password") String password
) {}
