package ru.netology.cloudstorage.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FileNotFoundException extends RuntimeException {
    private final Map<String, String[]> errors;

    public FileNotFoundException(String message) {
        super(message);
        this.errors = Map.of("file", new String[]{message});
    }
}
