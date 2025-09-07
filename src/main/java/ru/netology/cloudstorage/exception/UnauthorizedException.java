package ru.netology.cloudstorage.exception;

import java.util.Map;

public class UnauthorizedException extends RuntimeException {

    private final Map<String, String[]> errors;

    public UnauthorizedException(Map<String, String[]> errors) {
        super("Unauthorized");
        this.errors = errors;
    }

    public Map<String, String[]> getErrors() {
        return errors;
    }
}
